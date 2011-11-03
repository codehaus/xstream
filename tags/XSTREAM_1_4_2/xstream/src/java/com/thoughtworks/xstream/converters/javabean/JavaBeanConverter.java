/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 12. April 2005 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.javabean;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.MissingFieldException;
import com.thoughtworks.xstream.core.util.FastField;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Can convert any bean with a public default constructor. The {@link BeanProvider} used as
 * default is based on {@link java.beans.BeanInfo}. Indexed properties are currently not supported.
 */
public class JavaBeanConverter implements Converter {

    /*
     * TODO:
     *  - support indexed properties
     *  - support attributes (XSTR-620)
     *  - support local converters (XSTR-601)
     *  Problem: Mappers take definitions based on reflection, they don't know about bean info
     */
    protected final Mapper mapper;
    protected final JavaBeanProvider beanProvider;
    /**
     * @deprecated As of 1.3, no necessity for field anymore.
     */
    private String classAttributeIdentifier;

    public JavaBeanConverter(Mapper mapper) {
        this(mapper, new BeanProvider());
    }

    public JavaBeanConverter(Mapper mapper, JavaBeanProvider beanProvider) {
        this.mapper = mapper;
        this.beanProvider = beanProvider;
    }

    /**
     * @deprecated As of 1.3, use {@link #JavaBeanConverter(Mapper)} and {@link com.thoughtworks.xstream.XStream#aliasAttribute(String, String)}
     */
    public JavaBeanConverter(Mapper mapper, String classAttributeIdentifier) {
        this(mapper, new BeanProvider());
        this.classAttributeIdentifier = classAttributeIdentifier;
    }

    /**
     * Only checks for the availability of a public default constructor.
     * If you need stricter checks, subclass JavaBeanConverter
     */
    public boolean canConvert(Class type) {
        return beanProvider.canInstantiate(type);
    }

    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final String classAttributeName = classAttributeIdentifier != null ? classAttributeIdentifier : mapper.aliasForSystemAttribute("class");
        beanProvider.visitSerializableProperties(source, new JavaBeanProvider.Visitor() {
            public boolean shouldVisit(String name, Class definedIn) {
                return mapper.shouldSerializeMember(definedIn, name);
            }
            
            public void visit(String propertyName, Class fieldType, Class definedIn, Object newObj) {
                if (newObj != null) {
                    writeField(propertyName, fieldType, newObj, definedIn);
                }
            }

            private void writeField(String propertyName, Class fieldType, Object newObj, Class definedIn) {
                String serializedMember = mapper.serializedMember(source.getClass(), propertyName);
				ExtendedHierarchicalStreamWriterHelper.startNode(writer, serializedMember, fieldType);
                Class actualType = newObj.getClass();
                Class defaultType = mapper.defaultImplementationOf(fieldType);
                if (!actualType.equals(defaultType) && classAttributeName != null) {
                    writer.addAttribute(classAttributeName, mapper.serializedClass(actualType));
                }
                context.convertAnother(newObj);

                writer.endNode();
            }
        });
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final Object result = instantiateNewInstance(context);
        final Set seenProperties = new HashSet() {
            public boolean add(Object e) {
                if (!super.add(e)) {
                    throw new DuplicatePropertyException(((FastField)e).getName());
                }
                return true;
            }
        };

        Class resultType = result.getClass();
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String propertyName = mapper.realMember(resultType, reader.getNodeName());

            if (mapper.shouldSerializeMember(resultType, propertyName)) {
                boolean propertyExistsInClass = beanProvider.propertyDefinedInClass(propertyName, resultType);
    
                if (propertyExistsInClass) {
                    Class type = determineType(reader, result, propertyName);
                    Object value = context.convertAnother(result, type);
                    beanProvider.writeProperty(result, propertyName, value);
                    seenProperties.add(new FastField(resultType, propertyName));
                } else {
                    throw new MissingFieldException(resultType.getName(), propertyName);
                }
            }
            reader.moveUp();
        }

        return result;
    }

    private Object instantiateNewInstance(UnmarshallingContext context) {
        Object result = context.currentObject();
        if (result == null) {
            result = beanProvider.newInstance(context.getRequiredType());
        }
        return result;
    }

    private Class determineType(HierarchicalStreamReader reader, Object result, String fieldName) {
        final String classAttributeName = classAttributeIdentifier != null ? classAttributeIdentifier : mapper.aliasForSystemAttribute("class");
        String classAttribute = classAttributeName == null ? null : reader.getAttribute(classAttributeName);
        if (classAttribute != null) {
            return mapper.realClass(classAttribute);
        } else {
            return mapper.defaultImplementationOf(beanProvider.getPropertyType(result, fieldName));
        }
    }

    /**
     * @deprecated As of 1.3
     */
    public static class DuplicateFieldException extends ConversionException {
        public DuplicateFieldException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception to indicate double processing of a property to avoid silent clobbering.
     * 
     * @author J&ouml;rg Schaible
     * @since 1.4.2
     */
    public static class DuplicatePropertyException extends ConversionException {
        public DuplicatePropertyException(String msg) {
            super("Duplicate property " + msg);
            add("property", msg);
        }
    }
}