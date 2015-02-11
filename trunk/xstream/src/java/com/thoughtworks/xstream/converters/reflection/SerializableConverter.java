/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2010, 2011, 2012, 2013, 2014, 2015 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 21. December 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.reflection;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.core.util.CustomObjectInputStream;
import com.thoughtworks.xstream.core.util.CustomObjectOutputStream;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * Emulates the mechanism used by standard Java Serialization for classes that implement java.io.Serializable AND
 * implement or inherit a custom readObject()/writeObject() method. <h3>Supported features of serialization</h3>
 * <ul>
 * <li>readObject(), writeObject()</li>
 * <li>class inheritance</li>
 * <li>readResolve(), writeReplace()</li>
 * </ul>
 * <h3>Currently unsupported features</h3>
 * <ul>
 * <li>putFields(), writeFields(), readFields()</li>
 * <li>ObjectStreamField[] serialPersistentFields</li>
 * <li>ObjectInputValidation</li>
 * </ul>
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 */
public class SerializableConverter extends AbstractReflectionConverter {

    private static final String ELEMENT_NULL = "null";
    private static final String ELEMENT_DEFAULT = "default";
    private static final String ELEMENT_UNSERIALIZABLE_PARENTS = "unserializable-parents";
    private static final String ATTRIBUTE_CLASS = "class";
    private static final String ATTRIBUTE_SERIALIZATION = "serialization";
    private static final String ATTRIBUTE_VALUE_CUSTOM = "custom";
    private static final String ELEMENT_FIELDS = "fields";
    private static final String ELEMENT_FIELD = "field";
    private static final String ATTRIBUTE_NAME = "name";

    private final ClassLoaderReference classLoaderReference;

    /**
     * Construct a SerializableConverter.
     *
     * @param mapper the mapper chain instance
     * @param reflectionProvider the reflection provider
     * @param classLoaderReference the reference to the {@link ClassLoader} of the XStream instance
     * @since 1.4.5
     */
    public SerializableConverter(
            final Mapper mapper, final ReflectionProvider reflectionProvider,
            final ClassLoaderReference classLoaderReference) {
        super(mapper, new UnserializableParentsReflectionProvider(reflectionProvider));
        this.classLoaderReference = classLoaderReference;
    }

    /**
     * @deprecated As of 1.4.5 use {@link #SerializableConverter(Mapper, ReflectionProvider, ClassLoaderReference)}
     */
    @Deprecated
    public SerializableConverter(
            final Mapper mapper, final ReflectionProvider reflectionProvider, final ClassLoader classLoader) {
        this(mapper, reflectionProvider, new ClassLoaderReference(classLoader));
    }

    /**
     * @deprecated As of 1.4 use {@link #SerializableConverter(Mapper, ReflectionProvider, ClassLoaderReference)}
     */
    @Deprecated
    public SerializableConverter(final Mapper mapper, final ReflectionProvider reflectionProvider) {
        this(mapper, reflectionProvider, new ClassLoaderReference(null));
    }

    @Override
    public boolean canConvert(final Class<?> type) {
        return JVM.canCreateDerivedObjectOutputStream() && isSerializable(type);
    }

    private boolean isSerializable(final Class<?> type) {
        if (type != null
            && Serializable.class.isAssignableFrom(type)
            && !type.isInterface()
            && (serializationMethodInvoker.supportsReadObject(type, true) || serializationMethodInvoker
                .supportsWriteObject(type, true))) {
            for (final Class<?> clazz : hierarchyFor(type)) {
                if (!Serializable.class.isAssignableFrom(clazz)) {
                    return canAccess(type);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void doMarshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final String attributeName = mapper.aliasForSystemAttribute(ATTRIBUTE_SERIALIZATION);
        if (attributeName != null) {
            writer.addAttribute(attributeName, ATTRIBUTE_VALUE_CUSTOM);
        }

        // this is an array as it's a non final value that's accessed from an anonymous inner class.
        final Class<?>[] currentTypeRef = new Class<?>[1];
        final boolean[] writtenClassWrapper = {false};

        final CustomObjectOutputStream.StreamCallback callback = new CustomObjectOutputStream.StreamCallback() {

            @Override
            public void writeToStream(final Object object) {
                if (object == null) {
                    writer.startNode(ELEMENT_NULL);
                    writer.endNode();
                } else {
                    ExtendedHierarchicalStreamWriterHelper.startNode(writer, mapper.serializedClass(object.getClass()),
                        object.getClass());
                    context.convertAnother(object);
                    writer.endNode();
                }
            }

            @Override
            public void writeFieldsToStream(final Map<String, Object> fields) {
                final Class<?> currentType = currentTypeRef[0];
                final ObjectStreamClass objectStreamClass = ObjectStreamClass.lookup(currentType);

                writer.startNode(ELEMENT_DEFAULT);
                for (final String name : fields.keySet()) {
                    if (!mapper.shouldSerializeMember(currentType, name)) {
                        continue;
                    }
                    final ObjectStreamField field = objectStreamClass.getField(name);
                    final Object value = fields.get(name);
                    if (field == null) {
                        throw new ObjectAccessException("Class "
                            + value.getClass().getName()
                            + " may not write a field named '"
                            + name
                            + "'");
                    }
                    if (value != null) {
                        ExtendedHierarchicalStreamWriterHelper.startNode(writer, mapper.serializedMember(source
                            .getClass(), name), value.getClass());
                        if (field.getType() != value.getClass() && !field.getType().isPrimitive()) {
                            final String attributeName = mapper.aliasForSystemAttribute(ATTRIBUTE_CLASS);
                            if (attributeName != null) {
                                writer.addAttribute(attributeName, mapper.serializedClass(value.getClass()));
                            }
                        }
                        context.convertAnother(value);
                        writer.endNode();
                    }
                }
                writer.endNode();
            }

            @Override
            public void defaultWriteObject() {
                boolean writtenDefaultFields = false;

                final Class<?> currentType = currentTypeRef[0];
                final ObjectStreamClass objectStreamClass = ObjectStreamClass.lookup(currentType);
                if (objectStreamClass == null) {
                    return;
                }

                for (final ObjectStreamField field : objectStreamClass.getFields()) {
                    final Object value = readField(field, currentType, source);
                    if (value != null) {
                        if (!writtenClassWrapper[0]) {
                            writer.startNode(mapper.serializedClass(currentType));
                            writtenClassWrapper[0] = true;
                        }
                        if (!writtenDefaultFields) {
                            writer.startNode(ELEMENT_DEFAULT);
                            writtenDefaultFields = true;
                        }
                        if (!mapper.shouldSerializeMember(currentType, field.getName())) {
                            continue;
                        }

                        final Class<?> actualType = value.getClass();
                        ExtendedHierarchicalStreamWriterHelper.startNode(writer, mapper.serializedMember(source
                            .getClass(), field.getName()), actualType);
                        final Class<?> defaultType = mapper.defaultImplementationOf(field.getType());
                        if (!actualType.equals(defaultType)) {
                            final String attributeName = mapper.aliasForSystemAttribute(ATTRIBUTE_CLASS);
                            if (attributeName != null) {
                                writer.addAttribute(attributeName, mapper.serializedClass(actualType));
                            }
                        }

                        context.convertAnother(value);

                        writer.endNode();
                    }
                }
                if (writtenClassWrapper[0] && !writtenDefaultFields) {
                    writer.startNode(ELEMENT_DEFAULT);
                    writer.endNode();
                } else if (writtenDefaultFields) {
                    writer.endNode();
                }
            }

            @Override
            public void flush() {
                writer.flush();
            }

            @Override
            public void close() {
                throw new UnsupportedOperationException(
                    "Objects are not allowed to call ObjectOutputStream.close() from writeObject()");
            }
        };

        try {
            boolean mustHandleUnserializableParent = false;
            for (final Class<?> currentType : hierarchyFor(source.getClass())) {
                currentTypeRef[0] = currentType;
                if (!Serializable.class.isAssignableFrom(currentType)) {
                    mustHandleUnserializableParent = true;
                    continue;
                } else {
                    if (mustHandleUnserializableParent) {
                        marshalUnserializableParent(writer, context, source);
                        mustHandleUnserializableParent = false;
                    }
                    if (serializationMethodInvoker.supportsWriteObject(currentType, false)) {
                        writtenClassWrapper[0] = true;
                        writer.startNode(mapper.serializedClass(currentType));
                        if (currentType != mapper.defaultImplementationOf(currentType)) {
                            final String classAttributeName = mapper.aliasForSystemAttribute(ATTRIBUTE_CLASS);
                            if (classAttributeName != null) {
                                writer.addAttribute(classAttributeName, currentType.getName());
                            }
                        }
                        @SuppressWarnings("resource")
                        final CustomObjectOutputStream objectOutputStream = CustomObjectOutputStream.getInstance(
                            context, callback);
                        serializationMethodInvoker.callWriteObject(currentType, source, objectOutputStream);
                        objectOutputStream.popCallback();
                        writer.endNode();
                    } else if (serializationMethodInvoker.supportsReadObject(currentType, false)) {
                        // Special case for objects that have readObject(), but not writeObject().
                        // The class wrapper is always written, whether or not this class in the hierarchy has
                        // serializable fields. This guarantees that readObject() will be called upon deserialization.
                        writtenClassWrapper[0] = true;
                        writer.startNode(mapper.serializedClass(currentType));
                        if (currentType != mapper.defaultImplementationOf(currentType)) {
                            final String classAttributeName = mapper.aliasForSystemAttribute(ATTRIBUTE_CLASS);
                            if (classAttributeName != null) {
                                writer.addAttribute(classAttributeName, currentType.getName());
                            }
                        }
                        callback.defaultWriteObject();
                        writer.endNode();
                    } else {
                        writtenClassWrapper[0] = false;
                        callback.defaultWriteObject();
                        if (writtenClassWrapper[0]) {
                            writer.endNode();
                        }
                    }
                }
            }
        } catch (final IOException e) {
            throw new ObjectAccessException("Could not call defaultWriteObject()", e);
        }
    }

    protected void marshalUnserializableParent(final HierarchicalStreamWriter writer, final MarshallingContext context,
            final Object replacedSource) {
        writer.startNode(ELEMENT_UNSERIALIZABLE_PARENTS);
        super.doMarshal(replacedSource, writer, context);
        writer.endNode();
    }

    private Object readField(final ObjectStreamField field, final Class<?> type, final Object instance) {
        try {
            final Field javaField = type.getDeclaredField(field.getName());
            if (!javaField.isAccessible()) {
                javaField.setAccessible(true);
            }
            return javaField.get(instance);
        } catch (final IllegalArgumentException e) {
            throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
        } catch (final IllegalAccessException e) {
            throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
        } catch (final NoSuchFieldException e) {
            throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
        } catch (final SecurityException e) {
            throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
        }
    }

    protected List<Class<?>> hierarchyFor(Class<?> type) {
        final List<Class<?>> result = new ArrayList<Class<?>>();
        while (type != Object.class && type != null) {
            result.add(type);
            type = type.getSuperclass();
        }

        // In Java Object Serialization, the classes are deserialized starting from parent class and moving down.
        Collections.reverse(result);

        return result;
    }

    @Override
    public Object doUnmarshal(final Object result, final HierarchicalStreamReader reader,
            final UnmarshallingContext context) {
        // this is an array as it's a non final value that's accessed from an anonymous inner class.
        final Class<?>[] currentType = new Class<?>[1];

        final String attributeName = mapper.aliasForSystemAttribute(ATTRIBUTE_SERIALIZATION);
        if (attributeName != null && !ATTRIBUTE_VALUE_CUSTOM.equals(reader.getAttribute(attributeName))) {
            throw new ConversionException("Cannot deserialize object with new readObject()/writeObject() methods");
        }

        final CustomObjectInputStream.StreamCallback callback = new CustomObjectInputStream.StreamCallback() {
            @Override
            public Object readFromStream() {
                reader.moveDown();
                final Class<?> type = HierarchicalStreams.readClassType(reader, mapper);
                final Object value = context.convertAnother(result, type);
                reader.moveUp();
                return value;
            }

            @Override
            public Map<String, Object> readFieldsFromStream() {
                final Map<String, Object> fields = new HashMap<String, Object>();
                reader.moveDown();
                if (reader.getNodeName().equals(ELEMENT_FIELDS)) {
                    // Maintain compatibility with XStream 1.1.0
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        if (!reader.getNodeName().equals(ELEMENT_FIELD)) {
                            throw new ConversionException("Expected <"
                                + ELEMENT_FIELD
                                + "/> element inside <"
                                + ELEMENT_FIELD
                                + "/>");
                        }
                        final String name = reader.getAttribute(ATTRIBUTE_NAME);
                        final Class<?> type = mapper.realClass(reader.getAttribute(ATTRIBUTE_CLASS));
                        final Object value = context.convertAnother(result, type);
                        fields.put(name, value);
                        reader.moveUp();
                    }
                } else if (reader.getNodeName().equals(ELEMENT_DEFAULT)) {
                    // New format introduced in XStream 1.1.1
                    final ObjectStreamClass objectStreamClass = ObjectStreamClass.lookup(currentType[0]);
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        final String name = mapper.realMember(currentType[0], reader.getNodeName());
                        if (mapper.shouldSerializeMember(currentType[0], name)) {
                            final String classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper);
                            Class<?> type;
                            if (classAttribute != null) {
                                type = mapper.realClass(classAttribute);
                            } else {
                                final ObjectStreamField field = objectStreamClass.getField(name);
                                if (field == null) {
                                    throw new MissingFieldException(currentType[0].getName(), name);
                                }
                                type = field.getType();
                            }
                            final Object value = context.convertAnother(result, type);
                            fields.put(name, value);
                        }
                        reader.moveUp();
                    }
                } else {
                    throw new ConversionException("Expected <"
                        + ELEMENT_FIELDS
                        + "/> or <"
                        + ELEMENT_DEFAULT
                        + "/> element when calling ObjectInputStream.readFields()");
                }
                reader.moveUp();
                return fields;
            }

            @Override
            public void defaultReadObject() {
                if (!reader.hasMoreChildren()) {
                    return;
                }
                reader.moveDown();
                if (!reader.getNodeName().equals(ELEMENT_DEFAULT)) {
                    throw new ConversionException("Expected <" + ELEMENT_DEFAULT + "/> element in readObject() stream");
                }
                while (reader.hasMoreChildren()) {
                    reader.moveDown();

                    final String fieldName = mapper.realMember(currentType[0], reader.getNodeName());
                    if (mapper.shouldSerializeMember(currentType[0], fieldName)) {
                        final String classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper);
                        final Class<?> type;
                        if (classAttribute != null) {
                            type = mapper.realClass(classAttribute);
                        } else {
                            type = mapper.defaultImplementationOf(reflectionProvider.getFieldType(result, fieldName,
                                currentType[0]));
                        }

                        final Object value = context.convertAnother(result, type);
                        reflectionProvider.writeField(result, fieldName, value, currentType[0]);
                    }

                    reader.moveUp();
                }
                reader.moveUp();
            }

            @Override
            public void registerValidation(final ObjectInputValidation validation, final int priority) {
                context.addCompletionCallback(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            validation.validateObject();
                        } catch (final InvalidObjectException e) {
                            throw new ObjectAccessException("Cannot validate object : " + e.getMessage(), e);
                        }
                    }
                }, priority);
            }

            @Override
            public void close() {
                throw new UnsupportedOperationException(
                    "Objects are not allowed to call ObjectInputStream.close() from readObject()");
            }
        };

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            final String nodeName = reader.getNodeName();
            if (nodeName.equals(ELEMENT_UNSERIALIZABLE_PARENTS)) {
                super.doUnmarshal(result, reader, context);
            } else {
                final String classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper);
                if (classAttribute == null) {
                    currentType[0] = mapper.defaultImplementationOf(mapper.realClass(nodeName));
                } else {
                    currentType[0] = mapper.realClass(classAttribute);
                }
                if (serializationMethodInvoker.supportsReadObject(currentType[0], false)) {
                    @SuppressWarnings("resource")
                    final CustomObjectInputStream objectInputStream = CustomObjectInputStream.getInstance(context,
                        callback, classLoaderReference);
                    serializationMethodInvoker.callReadObject(currentType[0], result, objectInputStream);
                    objectInputStream.popCallback();
                } else {
                    try {
                        callback.defaultReadObject();
                    } catch (final IOException e) {
                        throw new ObjectAccessException("Could not call defaultWriteObject()", e);
                    }
                }
            }
            reader.moveUp();
        }

        return result;
    }

    protected void doMarshalConditionally(final Object source, final HierarchicalStreamWriter writer,
            final MarshallingContext context) {
        if (isSerializable(source.getClass())) {
            doMarshal(source, writer, context);
        } else {
            super.doMarshal(source, writer, context);
        }
    }

    protected Object doUnmarshalConditionally(final Object result, final HierarchicalStreamReader reader,
            final UnmarshallingContext context) {
        return isSerializable(result.getClass()) ? doUnmarshal(result, reader, context) : super.doUnmarshal(result,
            reader, context);
    }

    private static class UnserializableParentsReflectionProvider extends ReflectionProviderWrapper {

        public UnserializableParentsReflectionProvider(final ReflectionProvider reflectionProvider) {
            super(reflectionProvider);
        }

        @Override
        public void visitSerializableFields(final Object object, final Visitor visitor) {
            wrapped.visitSerializableFields(object, new Visitor() {
                @Override
                public void visit(final String name, final Class<?> type, final Class<?> definedIn, final Object value) {
                    if (!Serializable.class.isAssignableFrom(definedIn)) {
                        visitor.visit(name, type, definedIn, value);
                    }
                }
            });
        }
    }
}
