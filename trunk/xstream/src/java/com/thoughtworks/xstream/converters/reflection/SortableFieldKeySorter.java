/*
 * Copyright (C) 2007, 2009, 2011, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 10. April 2007 by Guilherme Silveira
 */
package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.thoughtworks.xstream.core.Caching;
import com.thoughtworks.xstream.io.StreamException;


/**
 * The default implementation for sorting fields. Invoke registerFieldOrder in order to set the field order for an
 * specific type.
 * 
 * @author Guilherme Silveira
 * @since 1.2.2
 */
public class SortableFieldKeySorter implements FieldKeySorter, Caching {

    private final Map<Class<?>, Comparator<FieldKey>> map = new HashMap<Class<?>, Comparator<FieldKey>>();

    @Override
    public Map<FieldKey, Field> sort(final Class<?> type, final Map<FieldKey, Field> keyedByFieldKey) {
        if (map.containsKey(type)) {
            final Map<FieldKey, Field> result = new LinkedHashMap<FieldKey, Field>();
            final FieldKey[] fieldKeys = keyedByFieldKey.keySet().toArray(new FieldKey[keyedByFieldKey.size()]);
            Arrays.sort(fieldKeys, map.get(type));
            for (final FieldKey fieldKey : fieldKeys) {
                result.put(fieldKey, keyedByFieldKey.get(fieldKey));
            }
            return result;
        } else {
            return keyedByFieldKey;
        }
    }

    /**
     * Registers the field order to use for a specific type. This will not affect any of the type's super or sub
     * classes. If you skip a field which will be serialized, XStream will thrown an StreamException during the
     * serialization process.
     * 
     * @param type the type
     * @param fields the field order
     */
    public void registerFieldOrder(final Class<?> type, final String[] fields) {
        map.put(type, new FieldComparator(fields));
    }

    private class FieldComparator implements Comparator<FieldKey> {

        private final String[] fieldOrder;

        public FieldComparator(final String[] fields) {
            fieldOrder = fields;
        }

        private int compare(final String first, final String second) {
            int firstPosition = -1, secondPosition = -1;
            for (int i = 0; i < fieldOrder.length; i++) {
                if (fieldOrder[i].equals(first)) {
                    firstPosition = i;
                }
                if (fieldOrder[i].equals(second)) {
                    secondPosition = i;
                }
            }
            if (firstPosition == -1 || secondPosition == -1) {
                // field not defined!!!
                throw new StreamException("You have not given XStream a list of all fields to be serialized.");
            }
            return firstPosition - secondPosition;
        }

        @Override
        public int compare(final FieldKey first, final FieldKey second) {
            return compare(first.getFieldName(), second.getFieldName());
        }

    }

    @Override
    public void flushCache() {
        map.clear();
    }
}
