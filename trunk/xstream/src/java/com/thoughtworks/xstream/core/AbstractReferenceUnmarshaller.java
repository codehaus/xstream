/*
 * Copyright (C) 2006, 2007, 2008, 2011, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 15. March 2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.core;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * Abstract base class for a TreeUnmarshaller, that resolves references.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 * @since 1.2
 */
public abstract class AbstractReferenceUnmarshaller<R> extends TreeUnmarshaller {

    private static final Object NULL = new Object();
    private final Map<R, Object> values = new HashMap<R, Object>();
    private final FastStack<R> parentStack = new FastStack<R>(16);

    public AbstractReferenceUnmarshaller(
            final Object root, final HierarchicalStreamReader reader, final ConverterLookup converterLookup,
            final Mapper mapper) {
        super(root, reader, converterLookup, mapper);
    }

    @Override
    protected Object convert(final Object parent, final Class<?> type, final Converter converter) {
        if (parentStack.size() > 0) { // handles circular references
            final R parentReferenceKey = parentStack.peek();
            if (parentReferenceKey != null) {
                // see AbstractCircularReferenceTest.testWeirdCircularReference()
                if (!values.containsKey(parentReferenceKey)) {
                    values.put(parentReferenceKey, parent);
                }
            }
        }
        final Object result;
        final String attributeName = getMapper().aliasForSystemAttribute("reference");
        final String reference = attributeName == null ? null : reader.getAttribute(attributeName);
        if (reference != null) {
            final Object cache = values.get(getReferenceKey(reference));
            if (cache == null) {
                final ConversionException ex = new ConversionException("Invalid reference");
                ex.add("reference", reference);
                throw ex;
            }
            result = cache == NULL ? null : cache;
        } else {
            final R currentReferenceKey = getCurrentReferenceKey();
            parentStack.push(currentReferenceKey);
            result = super.convert(parent, type, converter);
            if (currentReferenceKey != null) {
                values.put(currentReferenceKey, result == null ? NULL : result);
            }
            parentStack.popSilently();
        }
        return result;
    }

    protected abstract R getReferenceKey(String reference);

    protected abstract R getCurrentReferenceKey();
}
