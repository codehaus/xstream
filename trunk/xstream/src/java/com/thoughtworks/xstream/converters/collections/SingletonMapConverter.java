/*
 * Copyright (C) 2011, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 11. October 2011 by Joerg Schaible
 */
package com.thoughtworks.xstream.converters.collections;

import java.util.Collections;
import java.util.Map;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * Converts a singleton map to XML, specifying an 'entry' element with 'key' and 'value' children.
 * <p>
 * Note: 'key' and 'value' is not the name of the generated tag. The children are serialized as normal elements and the
 * implementation expects them in the order 'key'/'value'.
 * </p>
 * <p>
 * Supports Collections.singletonMap.
 * </p>
 * 
 * @author J&ouml;rg Schaible
 * @since 1.4.2
 */
public class SingletonMapConverter extends MapConverter {

    private static final Class<?> MAP = Collections.singletonMap(Boolean.TRUE, null).getClass();

    /**
     * Construct a SingletonMapConverter.
     * 
     * @param mapper
     * @since 1.4.2
     */
    public SingletonMapConverter(final Mapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canConvert(final Class<?> type) {
        return MAP == type;
    }

    @Override
    public Map<?, ?> unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        reader.moveDown();
        reader.moveDown();
        final Object key = readItem(reader, context, null);
        reader.moveUp();

        reader.moveDown();
        final Object value = readItem(reader, context, null);
        reader.moveUp();
        reader.moveUp();

        return Collections.singletonMap(key, value);
    }

}
