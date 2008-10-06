/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 22. January 2005 by Joe Walnes
 */
package com.thoughtworks.xstream.mapper;

import com.thoughtworks.xstream.alias.ClassMapper;

import java.util.Arrays;
import java.util.Collection;

/**
 * Mapper that detects arrays and changes the name so it can identified as an array
 * (for example Foo[] gets serialized as foo-array). Supports multi-dimensional arrays.
 *
 * @author Joe Walnes 
 */
public class ArrayMapper extends MapperWrapper {

    private final static Collection BOXED_TYPES = Arrays.asList(
            new Class[] {
                    Boolean.class,
                    Byte.class,
                    Character.class,
                    Short.class,
                    Integer.class,
                    Long.class,
                    Float.class,
                    Double.class
            });

    public ArrayMapper(Mapper wrapped) {
        super(wrapped);
    }

    /**
     * @deprecated As of 1.2, use {@link #ArrayMapper(Mapper)}
     */
    public ArrayMapper(ClassMapper wrapped) {
        this((Mapper)wrapped);
    }

    public String serializedClass(Class type) {
        StringBuffer arraySuffix = new StringBuffer();
        String name = null;
        while (type.isArray()) {
            name = super.serializedClass(type);
            if (type.getName().equals(name)) {
                type = type.getComponentType();
                arraySuffix.append("-array");
                name = null;
            } else {
                break;
            }
        }
        if (name == null) {
            name = boxedTypeName(type);
        }
        if (name == null) {
            name = super.serializedClass(type);
        }
        if (arraySuffix.length() > 0) {
            return name + arraySuffix;
        } else {
            return name;
        }
    }

    public Class realClass(String elementName) {
        int dimensions = 0;

        // strip off "-array" suffix
        while (elementName.endsWith("-array")) {
            elementName = elementName.substring(0, elementName.length() - 6); // cut off -array
            ++dimensions;
        }

        if (dimensions > 0) {
            Class componentType = primitiveClassNamed(elementName);
            if (componentType == null) {
                componentType = super.realClass(elementName);
            }
            while (componentType.isArray()) {
                componentType = componentType.getComponentType();
                ++dimensions;
            }
            return super.realClass(arrayType(dimensions, componentType));
        } else {
            return super.realClass(elementName);
        }
    }

    private String arrayType(int dimensions, Class componentType) {
        StringBuffer className = new StringBuffer();
        for (int i = 0; i < dimensions; i++) {
            className.append('[');
        }
        if (componentType.isPrimitive()) {
            className.append(charThatJavaUsesToRepresentPrimitiveArrayType(componentType));
            return className.toString();
        } else {
            className.append('L').append(componentType.getName()).append(';');
            return className.toString();
        }
    }

    private Class primitiveClassNamed(String name) {
        return
                name.equals("void") ? Void.TYPE :
                name.equals("boolean") ? Boolean.TYPE :
                name.equals("byte") ? Byte.TYPE :
                name.equals("char") ? Character.TYPE :
                name.equals("short") ? Short.TYPE :
                name.equals("int") ? Integer.TYPE :
                name.equals("long") ? Long.TYPE :
                name.equals("float") ? Float.TYPE :
                name.equals("double") ? Double.TYPE :
                null;
    }

    private char charThatJavaUsesToRepresentPrimitiveArrayType(Class primvCls) {
        return
                (primvCls == boolean.class) ? 'Z' :
                (primvCls == byte.class) ? 'B' :
                (primvCls == char.class) ? 'C' :
                (primvCls == short.class) ? 'S' :
                (primvCls == int.class) ? 'I' :
                (primvCls == long.class) ? 'J' :
                (primvCls == float.class) ? 'F' :
                (primvCls == double.class) ? 'D' :
                0;
    }
    
    private String boxedTypeName(Class type) {
        return BOXED_TYPES.contains(type) ? type.getName() : null;
    }
}
