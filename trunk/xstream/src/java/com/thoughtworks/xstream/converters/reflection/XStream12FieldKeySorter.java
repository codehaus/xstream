/*
 * Copyright (C) 2007 XStream Committers
 * Created on 19.09.2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.converters.reflection;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;


/**
 * Sort the fields in the order of XStream 1.2.x. Fields are returned in their declaration order,
 * fields of base classes last.
 * 
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public class XStream12FieldKeySorter implements FieldKeySorter {

    public Map sort(final Class type, final Map keyedByFieldKey) {
        final Map map = new TreeMap(new Comparator() {

            public int compare(final Object o1, final Object o2) {
                final FieldKey fieldKey1 = (FieldKey)o1;
                final FieldKey fieldKey2 = (FieldKey)o2;
                int i = fieldKey2.getDepth() - fieldKey1.getDepth();
                if (i == 0) {
                    i = fieldKey1.getOrder() - fieldKey2.getOrder();
                }
                return i;
            }
        });
        map.putAll(keyedByFieldKey);
        return map;
    }

}
