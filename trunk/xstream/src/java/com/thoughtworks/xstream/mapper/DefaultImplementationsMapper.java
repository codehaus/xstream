/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2009, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 22. January 2005 by Joe Walnes
 */
package com.thoughtworks.xstream.mapper;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.InitializationException;


/**
 * Mapper that resolves default implementations of classes. For example, mapper.serializedClass(ArrayList.class) will
 * return java.util.List. Calling mapper.defaultImplementationOf(List.class) will return ArrayList.
 * 
 * @author Joe Walnes
 */
public class DefaultImplementationsMapper extends MapperWrapper {

    private final Map<Class<?>, Class<?>> typeToImpl = new HashMap<Class<?>, Class<?>>();
    private transient Map<Class<?>, Class<?>> implToType = new HashMap<Class<?>, Class<?>>();

    public DefaultImplementationsMapper(final Mapper wrapped) {
        super(wrapped);
        addDefaults();
    }

    protected void addDefaults() {
        // null handling
        addDefaultImplementation(null, Mapper.Null.class);
        // register primitive types
        addDefaultImplementation(Boolean.class, boolean.class);
        addDefaultImplementation(Character.class, char.class);
        addDefaultImplementation(Integer.class, int.class);
        addDefaultImplementation(Float.class, float.class);
        addDefaultImplementation(Double.class, double.class);
        addDefaultImplementation(Short.class, short.class);
        addDefaultImplementation(Byte.class, byte.class);
        addDefaultImplementation(Long.class, long.class);
    }

    public void addDefaultImplementation(final Class<?> defaultImplementation, final Class<?> ofType) {
        if (defaultImplementation != null && defaultImplementation.isInterface()) {
            throw new InitializationException("Default implementation is not a concrete class: "
                + defaultImplementation.getName());
        }
        typeToImpl.put(ofType, defaultImplementation);
        implToType.put(defaultImplementation, ofType);
    }

    @Override
    public String serializedClass(final Class<?> type) {
        final Class<?> baseType = implToType.get(type);
        return baseType == null ? super.serializedClass(type) : super.serializedClass(baseType);
    }

    @Override
    public Class<?> defaultImplementationOf(final Class<?> type) {
        if (typeToImpl.containsKey(type)) {
            return typeToImpl.get(type);
        } else {
            return super.defaultImplementationOf(type);
        }
    }

    private Object readResolve() {
        implToType = new HashMap<Class<?>, Class<?>>();
        for (final Class<?> type : typeToImpl.keySet()) {
            implToType.put(typeToImpl.get(type), type);
        }
        return this;
    }
}
