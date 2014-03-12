/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 02. March 2006 by Joerg Schaible
 */
package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.Caching;
import com.thoughtworks.xstream.core.ReferencingMarshallingContext;
import com.thoughtworks.xstream.core.util.ArrayIterator;
import com.thoughtworks.xstream.core.util.FastField;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.core.util.Primitives;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.Mapper;


public abstract class AbstractReflectionConverter implements Converter, Caching {

    protected final ReflectionProvider reflectionProvider;
    protected final Mapper mapper;
    protected transient SerializationMethodInvoker serializationMethodInvoker;
    private transient ReflectionProvider pureJavaReflectionProvider;

    public AbstractReflectionConverter(final Mapper mapper, final ReflectionProvider reflectionProvider) {
        this.mapper = mapper;
        this.reflectionProvider = reflectionProvider;
        serializationMethodInvoker = new SerializationMethodInvoker();
    }

    protected boolean canAccess(final Class<?> type) {
        try {
            reflectionProvider.getFieldOrNull(type, "%");
            return true;
        } catch (final NoClassDefFoundError e) {
            // restricted type in GAE
        }
        return false;
    }

    @Override
    public void marshal(final Object original, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Object source = serializationMethodInvoker.callWriteReplace(original);

        if (source != original && context instanceof ReferencingMarshallingContext) {
            ((ReferencingMarshallingContext<?>)context).replace(original, source);
        }
        if (source.getClass() != original.getClass()) {
            final String attributeName = mapper.aliasForSystemAttribute("resolves-to");
            if (attributeName != null) {
                writer.addAttribute(attributeName, mapper.serializedClass(source.getClass()));
            }
            context.convertAnother(source);
        } else {
            doMarshal(source, writer, context);
        }
    }

    protected void doMarshal(final Object source, final HierarchicalStreamWriter writer,
            final MarshallingContext context) {
        final List<FieldInfo> fields = new ArrayList<FieldInfo>();
        final Map<String, Field> defaultFieldDefinition = new HashMap<String, Field>();

        // Attributes might be preferred to child elements ...
        reflectionProvider.visitSerializableFields(source, new ReflectionProvider.Visitor() {
            final Set<String> writtenAttributes = new HashSet<String>();

            @Override
            public void visit(final String fieldName, final Class<?> type, final Class<?> definedIn, final Object value) {
                if (!mapper.shouldSerializeMember(definedIn, fieldName)) {
                    return;
                }
                if (!defaultFieldDefinition.containsKey(fieldName)) {
                    Class<?> lookupType = source.getClass();
                    // See XSTR-457 and OmitFieldsTest
                    if (definedIn != source.getClass() && !mapper.shouldSerializeMember(lookupType, fieldName)) {
                        lookupType = definedIn;
                    }
                    defaultFieldDefinition.put(fieldName, reflectionProvider.getField(lookupType, fieldName));
                }

                final SingleValueConverter converter = mapper.getConverterFromItemType(fieldName, type, definedIn);
                if (converter != null) {
                    final String attribute = mapper.aliasForAttribute(mapper.serializedMember(definedIn, fieldName));
                    if (value != null) {
                        if (writtenAttributes.contains(fieldName)) { // TODO: use attribute
                            throw new ConversionException("Cannot write field with name '"
                                + fieldName
                                + "' twice as attribute for object of type "
                                + source.getClass().getName());
                        }
                        final String str = converter.toString(value);
                        if (str != null) {
                            writer.addAttribute(attribute, str);
                        }
                    }
                    writtenAttributes.add(fieldName); // TODO: use attribute
                } else {
                    fields.add(new FieldInfo(fieldName, type, definedIn, value));
                }
            }
        });

        new Object() {
            {
                for (final FieldInfo fieldInfo : fields) {
                    final FieldInfo info = fieldInfo;
                    if (info.value != null) {
                        final Mapper.ImplicitCollectionMapping mapping = mapper.getImplicitCollectionDefForFieldName(
                            source.getClass(), info.fieldName);
                        if (mapping != null) {
                            if (context instanceof ReferencingMarshallingContext) {
                                if (info.value != Collections.EMPTY_LIST
                                    && info.value != Collections.EMPTY_SET
                                    && info.value != Collections.EMPTY_MAP) {
                                    final ReferencingMarshallingContext<?> refContext = (ReferencingMarshallingContext<?>)context;
                                    refContext.registerImplicit(info.value);
                                }
                            }
                            final boolean isCollection = info.value instanceof Collection;
                            final boolean isMap = info.value instanceof Map;
                            final boolean isEntry = isMap && mapping.getKeyFieldName() == null;
                            final boolean isArray = info.value.getClass().isArray();
                            for (final Iterator<?> iter = isArray ? new ArrayIterator(info.value) : isCollection
                                ? ((Collection<?>)info.value).iterator()
                                : isEntry ? ((Map<?, ?>)info.value).entrySet().iterator() : ((Map<?, ?>)info.value)
                                    .values()
                                    .iterator(); iter.hasNext();) {
                                final Object obj = iter.next();
                                final String itemName;
                                final Class<?> itemType;
                                if (obj == null) {
                                    itemType = Object.class;
                                    itemName = mapper.serializedClass(null);
                                } else if (isEntry) {
                                    final String entryName = mapping.getItemFieldName() != null ? mapping
                                        .getItemFieldName() : mapper.serializedClass(Map.Entry.class);
                                    final Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
                                    ExtendedHierarchicalStreamWriterHelper.startNode(writer, entryName, entry
                                        .getClass());
                                    writeItem(entry.getKey(), context, writer);
                                    writeItem(entry.getValue(), context, writer);
                                    writer.endNode();
                                    continue;
                                } else if (mapping.getItemFieldName() != null) {
                                    itemType = mapping.getItemType();
                                    itemName = mapping.getItemFieldName();
                                } else {
                                    itemType = obj.getClass();
                                    itemName = mapper.serializedClass(itemType);
                                }
                                writeField(info.fieldName, itemName, itemType, info.definedIn, obj);
                            }
                        } else {
                            writeField(info.fieldName, null, info.type, info.definedIn, info.value);
                        }
                    }
                }

            }

            void writeField(final String fieldName, final String aliasName, final Class<?> fieldType,
                    final Class<?> definedIn, final Object newObj) {
                final Class<?> actualType = newObj != null ? newObj.getClass() : fieldType;
                ExtendedHierarchicalStreamWriterHelper.startNode(writer, aliasName != null ? aliasName : mapper
                    .serializedMember(source.getClass(), fieldName), actualType);

                if (newObj != null) {
                    final Class<?> defaultType = mapper.defaultImplementationOf(fieldType);
                    if (!actualType.equals(defaultType)) {
                        final String serializedClassName = mapper.serializedClass(actualType);
                        if (!serializedClassName.equals(mapper.serializedClass(defaultType))) {
                            final String attributeName = mapper.aliasForSystemAttribute("class");
                            if (attributeName != null) {
                                writer.addAttribute(attributeName, serializedClassName);
                            }
                        }
                    }

                    final Field defaultField = defaultFieldDefinition.get(fieldName);
                    if (defaultField.getDeclaringClass() != definedIn) {
                        final String attributeName = mapper.aliasForSystemAttribute("defined-in");
                        if (attributeName != null) {
                            writer.addAttribute(attributeName, mapper.serializedClass(definedIn));
                        }
                    }

                    final Field field = reflectionProvider.getField(definedIn, fieldName);
                    marshallField(context, newObj, field);
                }
                writer.endNode();
            }

            void writeItem(final Object item, final MarshallingContext context, final HierarchicalStreamWriter writer) {
                if (item == null) {
                    final String name = mapper.serializedClass(null);
                    ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, Mapper.Null.class);
                    writer.endNode();
                } else {
                    final String name = mapper.serializedClass(item.getClass());
                    ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, item.getClass());
                    context.convertAnother(item);
                    writer.endNode();
                }
            }
        };
    }

    protected void marshallField(final MarshallingContext context, final Object newObj, final Field field) {
        context.convertAnother(newObj, mapper.getLocalConverter(field.getDeclaringClass(), field.getName()));
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        Object result = instantiateNewInstance(reader, context);
        result = doUnmarshal(result, reader, context);
        return serializationMethodInvoker.callReadResolve(result);
    }

    public Object doUnmarshal(final Object result, final HierarchicalStreamReader reader,
            final UnmarshallingContext context) {
        final Class<?> resultType = result.getClass();
        final Set<FastField> seenFields = new HashSet<FastField>() {
            @Override
            public boolean add(final FastField e) {
                if (!super.add(e)) {
                    throw new DuplicateFieldException(e.getName());
                }
                return true;
            }
        };

        // process attributes before recursing into child elements.
        final Iterator<String> it = reader.getAttributeNames();
        while (it.hasNext()) {
            final String attrAlias = it.next();
            // TODO: realMember should return FastField
            final String attrName = mapper.realMember(resultType, mapper.attributeForAlias(attrAlias));
            final Field field = reflectionProvider.getFieldOrNull(resultType, attrName);
            if (field != null && shouldUnmarshalField(field)) {
                final Class<?> classDefiningField = field.getDeclaringClass();
                if (!mapper.shouldSerializeMember(classDefiningField, attrName)) {
                    continue;
                }

                // we need a converter that produces a string representation only
                final SingleValueConverter converter = mapper.getConverterFromAttribute(classDefiningField, attrName,
                    field.getType());
                Class<?> type = field.getType();
                if (converter != null) {
                    final Object value = converter.fromString(reader.getAttribute(attrAlias));
                    if (type.isPrimitive()) {
                        type = Primitives.box(type);
                    }
                    if (value != null && !type.isAssignableFrom(value.getClass())) {
                        throw new ConversionException("Cannot convert type "
                            + value.getClass().getName()
                            + " to type "
                            + type.getName());
                    }
                    seenFields.add(new FastField(classDefiningField, attrName));
                    reflectionProvider.writeField(result, attrName, value, classDefiningField);
                }
            }
        }

        Map<String, Collection<? super Object>> implicitCollectionsForCurrentObject = null;
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String originalNodeName = reader.getNodeName();
            final Class<?> explicitDeclaringClass = readDeclaringClass(reader);
            final Class<?> fieldDeclaringClass = explicitDeclaringClass == null ? resultType : explicitDeclaringClass;
            final String fieldName = mapper.realMember(fieldDeclaringClass, originalNodeName);
            final Mapper.ImplicitCollectionMapping implicitCollectionMapping = mapper
                .getImplicitCollectionDefForFieldName(fieldDeclaringClass, fieldName);
            final Object value;
            String implicitFieldName = null;
            Field field = null;
            Class<?> type = null;
            if (implicitCollectionMapping == null) {
                // no item of an implicit collection for this name ... do we have a field?
                field = reflectionProvider.getFieldOrNull(fieldDeclaringClass, fieldName);
                if (field == null) {
                    // it is not a field ... do we have a field alias?
                    final Class<?> itemType = mapper.getItemTypeForItemFieldName(resultType, fieldName);
                    if (itemType != null) {
                        final String classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper);
                        if (classAttribute != null) {
                            type = mapper.realClass(classAttribute);
                        } else {
                            type = itemType;
                        }
                    } else {
                        // it is not an alias ... do we have an element of an implicit
                        // collection based on type only?
                        try {
                            type = mapper.realClass(originalNodeName);
                            implicitFieldName = mapper.getFieldNameForItemTypeAndName(context.getRequiredType(), type,
                                originalNodeName);
                        } catch (final CannotResolveClassException e) {
                            // type stays null ...
                        }
                        if (type == null || type != null && implicitFieldName == null) {
                            // either not a type or element is a type alias, but does not
                            // belong to an implicit field
                            handleUnknownField(explicitDeclaringClass, fieldName, resultType, originalNodeName);

                            // element is unknown in declaring class, ignore it now
                            type = null;
                        }
                    }
                    if (type == null) {
                        // no type, no value
                        value = null;
                    } else {
                        if (Map.Entry.class.equals(type)) {
                            // it is an element of an implicit map with two elements now for
                            // key and value
                            reader.moveDown();
                            final Object key = context.convertAnother(result, HierarchicalStreams.readClassType(reader,
                                mapper));
                            reader.moveUp();
                            reader.moveDown();
                            final Object v = context.convertAnother(result, HierarchicalStreams.readClassType(reader,
                                mapper));
                            reader.moveUp();
                            value = Collections.singletonMap(key, v).entrySet().iterator().next();
                        } else {
                            // recurse info hierarchy
                            value = context.convertAnother(result, type);
                        }
                    }
                } else {
                    boolean fieldAlreadyChecked = false;

                    // we have a field, but do we have to address a hidden one?
                    if (explicitDeclaringClass == null) {
                        while (field != null
                            && !(fieldAlreadyChecked = shouldUnmarshalField(field)
                                && mapper.shouldSerializeMember(field.getDeclaringClass(), fieldName))) {
                            field = reflectionProvider.getFieldOrNull(field.getDeclaringClass().getSuperclass(),
                                fieldName);
                        }
                    }
                    if (field != null
                        && (fieldAlreadyChecked || shouldUnmarshalField(field)
                            && mapper.shouldSerializeMember(field.getDeclaringClass(), fieldName))) {

                        final String classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper);
                        if (classAttribute != null) {
                            type = mapper.realClass(classAttribute);
                        } else {
                            type = mapper.defaultImplementationOf(field.getType());
                        }
                        // TODO the reflection provider should already return the proper field
                        value = unmarshallField(context, result, type, field);
                        final Class<?> definedType = field.getType();
                        if (!definedType.isPrimitive()) {
                            type = definedType;
                        }
                    } else {
                        value = null;
                    }
                }
            } else {
                // we have an implicit collection with defined names
                implicitFieldName = implicitCollectionMapping.getFieldName();
                type = implicitCollectionMapping.getItemType();
                if (type == null) {
                    final String classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper);
                    type = mapper.realClass(classAttribute != null ? classAttribute : originalNodeName);
                }
                value = context.convertAnother(result, type);
            }

            if (value != null && !type.isAssignableFrom(value.getClass())) {
                throw new ConversionException("Cannot convert type "
                    + value.getClass().getName()
                    + " to type "
                    + type.getName());
            }

            if (field != null) {
                reflectionProvider.writeField(result, fieldName, value, field.getDeclaringClass());
                seenFields.add(new FastField(field.getDeclaringClass(), fieldName));
            } else if (type != null) {
                if (implicitFieldName == null) {
                    // look for implicit field
                    implicitFieldName = mapper.getFieldNameForItemTypeAndName(context.getRequiredType(), value != null
                        ? value.getClass()
                        : Mapper.Null.class, originalNodeName);
                }
                if (implicitCollectionsForCurrentObject == null) {
                    implicitCollectionsForCurrentObject = new HashMap<String, Collection<? super Object>>();
                }
                writeValueToImplicitCollection(value, implicitCollectionsForCurrentObject, result, implicitFieldName);
            }

            reader.moveUp();
        }

        if (implicitCollectionsForCurrentObject != null) {
            for (final Map.Entry<String, Collection<? super Object>> entry : implicitCollectionsForCurrentObject
                .entrySet()) {
                final Object value = entry.getValue();
                if (value instanceof ArraysList) {
                    final Object array = ((ArraysList)value).toPhysicalArray();
                    reflectionProvider.writeField(result, entry.getKey(), array, null);
                }
            }
        }

        return result;
    }

    protected Object unmarshallField(final UnmarshallingContext context, final Object result, final Class<?> type,
            final Field field) {
        return context.convertAnother(result, type, mapper
            .getLocalConverter(field.getDeclaringClass(), field.getName()));
    }

    protected boolean shouldUnmarshalTransientFields() {
        return false;
    }

    protected boolean shouldUnmarshalField(final Field field) {
        return !(Modifier.isTransient(field.getModifiers()) && !shouldUnmarshalTransientFields());
    }

    private void handleUnknownField(final Class<?> classDefiningField, final String fieldName,
            final Class<?> resultType, final String originalNodeName) {
        if (classDefiningField == null) {
            for (Class<?> cls = resultType; cls != null; cls = cls.getSuperclass()) {
                if (!mapper.shouldSerializeMember(cls, originalNodeName)) {
                    return;
                }
            }
        }
        throw new UnknownFieldException(resultType.getName(), fieldName);
    }

    private void writeValueToImplicitCollection(final Object value,
            final Map<String, Collection<? super Object>> implicitCollections, final Object result,
            final String implicitFieldName) {
        Collection<? super Object> collection = implicitCollections.get(implicitFieldName);
        if (collection == null) {
            final Class<?> physicalFieldType = reflectionProvider.getFieldType(result, implicitFieldName, null);
            if (physicalFieldType.isArray()) {
                collection = new ArraysList(physicalFieldType);
            } else {
                final Class<?> fieldType = mapper.defaultImplementationOf(physicalFieldType);
                if (!(Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType))) {
                    throw new ObjectAccessException("Field "
                        + implicitFieldName
                        + " of "
                        + result.getClass().getName()
                        + " is configured for an implicit Collection or Map, but field is of type "
                        + fieldType.getName());
                }
                if (pureJavaReflectionProvider == null) {
                    pureJavaReflectionProvider = new PureJavaReflectionProvider();
                }
                final Object instance = pureJavaReflectionProvider.newInstance(fieldType);
                if (instance instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    final Collection<? super Object> uncheckedCollection = (Collection<? super Object>)instance;
                    collection = uncheckedCollection;
                } else {
                    final Mapper.ImplicitCollectionMapping implicitCollectionMapping = mapper
                        .getImplicitCollectionDefForFieldName(result.getClass(), implicitFieldName);
                    @SuppressWarnings("unchecked")
                    final Map<Object, Object> map = (Map<Object, Object>)instance;
                    collection = new MappingList(map, implicitCollectionMapping.getKeyFieldName());
                }
                reflectionProvider.writeField(result, implicitFieldName, instance, null);
            }
            implicitCollections.put(implicitFieldName, collection);
        }
        collection.add(value);
    }

    private Class<?> readDeclaringClass(final HierarchicalStreamReader reader) {
        final String attributeName = mapper.aliasForSystemAttribute("defined-in");
        final String definedIn = attributeName == null ? null : reader.getAttribute(attributeName);
        return definedIn == null ? null : mapper.realClass(definedIn);
    }

    protected Object instantiateNewInstance(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final String attributeName = mapper.aliasForSystemAttribute("resolves-to");
        final String readResolveValue = attributeName == null ? null : reader.getAttribute(attributeName);
        final Object currentObject = context.currentObject();
        if (currentObject != null) {
            return currentObject;
        } else if (readResolveValue != null) {
            return reflectionProvider.newInstance(mapper.realClass(readResolveValue));
        } else {
            return reflectionProvider.newInstance(context.getRequiredType());
        }
    }

    @Override
    public void flushCache() {
        serializationMethodInvoker.flushCache();
    }

    private Object readResolve() {
        serializationMethodInvoker = new SerializationMethodInvoker();
        return this;
    }

    public static class DuplicateFieldException extends ConversionException {
        public DuplicateFieldException(final String msg) {
            super("Duplicate field " + msg);
            add("field", msg);
        }
    }

    public static class UnknownFieldException extends ConversionException {
        public UnknownFieldException(final String type, final String field) {
            super("No such field " + type + "." + field);
            add("field", field);
        }
    }

    private static class FieldInfo {
        final String fieldName;
        final Class<?> type;
        final Class<?> definedIn;
        final Object value;

        FieldInfo(final String fieldName, final Class<?> type, final Class<?> definedIn, final Object value) {
            this.fieldName = fieldName;
            this.type = type;
            this.definedIn = definedIn;
            this.value = value;
        }
    }

    private static class ArraysList extends ArrayList<Object> {
        final Class<?> physicalFieldType;

        ArraysList(final Class<?> physicalFieldType) {
            this.physicalFieldType = physicalFieldType;
        }

        Object toPhysicalArray() {
            final Object[] objects = toArray();
            final Object array = Array.newInstance(physicalFieldType.getComponentType(), objects.length);
            if (physicalFieldType.getComponentType().isPrimitive()) {
                for (int i = 0; i < objects.length; ++i) {
                    Array.set(array, i, Array.get(objects, i));
                }
            } else {
                System.arraycopy(objects, 0, array, 0, objects.length);
            }
            return array;
        }
    }

    private class MappingList extends AbstractList<Object> {

        private final Map<Object, Object> map;
        private final String keyFieldName;
        private final Map<Class<?>, Field> fieldCache = new HashMap<Class<?>, Field>();

        public MappingList(final Map<Object, Object> map, final String keyFieldName) {
            this.map = map;
            this.keyFieldName = keyFieldName;
        }

        @Override
        public boolean add(final Object object) {
            if (object == null) {
                final boolean containsNull = !map.containsKey(null);
                map.put(null, null);
                return containsNull;
            }
            final Class<?> itemType = object.getClass();
            if (keyFieldName != null) {
                Field field = fieldCache.get(itemType);
                if (field == null) {
                    field = reflectionProvider.getField(itemType, keyFieldName);
                    fieldCache.put(itemType, field);
                }
                if (field != null) {
                    try {
                        final Object key = field.get(object);
                        return map.put(key, object) == null;
                    } catch (final IllegalArgumentException e) {
                        throw new ObjectAccessException("Could not get field "
                            + field.getClass()
                            + "."
                            + field.getName(), e);
                    } catch (final IllegalAccessException e) {
                        throw new ObjectAccessException("Could not get field "
                            + field.getClass()
                            + "."
                            + field.getName(), e);
                    }
                }
            } else if (object instanceof Map.Entry) {
                @SuppressWarnings("unchecked")
                final Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>)object;
                return map.put(entry.getKey(), entry.getValue()) == null;
            }

            throw new ConversionException("Element of type "
                + object.getClass().getName()
                + " is not defined as entry for map of type "
                + map.getClass().getName());
        }

        @Override
        public Object get(final int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }
    }
}
