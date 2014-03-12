/*
 * Copyright (c) 2007, 2008, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 20. September 2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.core.util;

import java.beans.PropertyEditor;

import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;


/**
 * Wrapper around {@link PropertyEditor} that can be called by multiple threads concurrently.
 * <p>
 * A PropertyEditor is not thread safe. To make best use of resources, the PropertyEditor provides a dynamically sizing
 * pool of instances, each of which will only be called by a single thread at a time.
 * </p>
 * <p>
 * The pool has a maximum capacity, to limit overhead. If all instances in the pool are in use and another is required,
 * it shall block until one becomes available.
 * </p>
 * 
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public class ThreadSafePropertyEditor {

    private final Class<? extends PropertyEditor> editorType;
    private final Pool<PropertyEditor> pool;

    public ThreadSafePropertyEditor(
            final Class<? extends PropertyEditor> type, final int initialPoolSize, final int maxPoolSize) {
        if (!PropertyEditor.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName() + " is not a " + PropertyEditor.class.getName());
        }
        editorType = type;
        pool = new Pool<PropertyEditor>(initialPoolSize, maxPoolSize, new Pool.Factory<PropertyEditor>() {
            @Override
            public PropertyEditor newInstance() {
                try {
                    return editorType.newInstance();
                } catch (final InstantiationException e) {
                    throw new ObjectAccessException("Could not call default constructor of " + editorType.getName(), e);
                } catch (final IllegalAccessException e) {
                    throw new ObjectAccessException("Could not call default constructor of " + editorType.getName(), e);
                }
            }

        });
    }

    public String getAsText(final Object object) {
        final PropertyEditor editor = fetchFromPool();
        try {
            editor.setValue(object);
            return editor.getAsText();
        } finally {
            pool.putInPool(editor);
        }
    }

    public Object setAsText(final String str) {
        final PropertyEditor editor = fetchFromPool();
        try {
            editor.setAsText(str);
            return editor.getValue();
        } finally {
            pool.putInPool(editor);
        }
    }

    private PropertyEditor fetchFromPool() {
        return pool.fetchFromPool();
    }
}
