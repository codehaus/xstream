/*
 * Copyright (C) 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 04. January 2008 by Joerg Schaible
 */
package com.thoughtworks.xstream.benchmark.cache.products;

import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * Uses XStream with a CachingMapper caching the aliasForAttribute method.
 *
 * @author J&ouml;rg Schaible
 */
public class SerializedClassCache extends XStreamCache {

    protected List getMappers(JVM jvm) {
        List list = super.getMappers(jvm);
        list.add(CachingMapper.class);
        return list;
    }

    public String toString() {
        return "Serialized Class Cache";
    }
    
    public static class CachingMapper extends MapperWrapper {

        private transient Map serializedClassCache;

        public CachingMapper(Mapper wrapped) {
            super(wrapped);
            readResolve();
        }

        public String serializedClass(Class type) {
            String alias = (String) serializedClassCache.get(type);
            if (alias != null) {
                return alias;
            }
            
            String result = super.serializedClass(type);
            serializedClassCache.put(type, alias);
            return result;
        }

        private Object readResolve() {
            serializedClassCache = Collections.synchronizedMap(new WeakHashMap(128));
            return this;
        }

    }
}
