package com.thoughtworks.xstream.mapper;

import com.thoughtworks.xstream.alias.ClassMapper;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * Mapper that caches which names map to which classes. Prevents repetitive searching and class loading.
 *
 * @author Joe Walnes
 */
public class CachingMapper extends MapperWrapper {

    private final Map cache = Collections.synchronizedMap(new HashMap());

    public CachingMapper(ClassMapper wrapped) {
        super(wrapped);
    }

    public Class realClass(String elementName) {
        Class cached = (Class) cache.get(elementName);
        if (cached != null) {
            return cached;
        } else {
            Class result = super.realClass(elementName);
            cache.put(elementName, result);
            return result;
        }
    }

}
