/*
 * Copyright (C) 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 01. January 2008 by Joerg Schaible
 */
package com.thoughtworks.xstream.benchmark.cache.targets;

import com.thoughtworks.xstream.benchmark.cache.model.Five;
import com.thoughtworks.xstream.benchmark.cache.model.One;
import com.thoughtworks.xstream.tools.benchmark.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Target containing basic types.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public class ReflectionTarget implements Target {

    private List list;
    
    public ReflectionTarget() {
        list = new ArrayList();
        for (int i = 0; i < 5; ++i) {
            list.add(new One(Integer.toString(i)));
        }
        list.add(new Five("1", 2, true, '4', new StringBuffer("5")));
    }
    
    public boolean isEqual(Object other) {
        return list.equals(other);
    }

    public Object target() {
        return list;
    }

    public String toString() {
        return "Reflection types";
    }
}
