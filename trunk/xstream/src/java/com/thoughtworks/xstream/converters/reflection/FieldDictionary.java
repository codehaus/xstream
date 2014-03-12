/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 14. May 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.core.Caching;
import com.thoughtworks.xstream.core.JVM;


/**
 * A field dictionary instance caches information about classes fields.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @author Guilherme Silveira
 */
public class FieldDictionary implements Caching {

    private transient Map<Class<?>, Map<String, Field>> keyedByFieldNameCache;
    private transient Map<Class<?>, Map<FieldKey, Field>> keyedByFieldKeyCache;
    private final FieldKeySorter sorter;

    public FieldDictionary() {
        this(new ImmutableFieldKeySorter());
    }

    public FieldDictionary(final FieldKeySorter sorter) {
        this.sorter = sorter;
        init();
    }

    private void init() {
        keyedByFieldNameCache = new HashMap<Class<?>, Map<String, Field>>();
        keyedByFieldKeyCache = new HashMap<Class<?>, Map<FieldKey, Field>>();
        keyedByFieldNameCache.put(Object.class, Collections.<String, Field>emptyMap());
        keyedByFieldKeyCache.put(Object.class, Collections.<FieldKey, Field>emptyMap());
    }

    /**
     * Returns an iterator for all fields for some class
     * 
     * @param cls the class you are interested on
     * @return an iterator for its fields
     */
    public Iterator<Field> fieldsFor(final Class<?> cls) {
        return buildMap(cls, true).values().iterator();
    }

    /**
     * Returns an specific field of some class. If definedIn is null, it searches for the field named 'name' inside the
     * class cls. If definedIn is different than null, tries to find the specified field name in the specified class cls
     * which should be defined in class definedIn (either equals cls or a one of it's superclasses)
     * 
     * @param cls the class where the field is to be searched
     * @param name the field name
     * @param definedIn the superclass (or the class itself) of cls where the field was defined
     * @return the field itself
     * @throws ObjectAccessException if no field can be found
     */
    public Field field(final Class<?> cls, final String name, final Class<?> definedIn) {
        final Field field = fieldOrNull(cls, name, definedIn);
        if (field == null) {
            throw new MissingFieldException(cls.getName(), name);
        } else {
            return field;
        }
    }

    /**
     * Returns an specific field of some class. If definedIn is null, it searches for the field named 'name' inside the
     * class cls. If definedIn is different than null, tries to find the specified field name in the specified class cls
     * which should be defined in class definedIn (either equals cls or a one of it's superclasses)
     * 
     * @param cls the class where the field is to be searched
     * @param name the field name
     * @param definedIn the superclass (or the class itself) of cls where the field was defined
     * @return the field itself or <code>null</code>
     * @since 1.4
     */
    public Field fieldOrNull(final Class<?> cls, final String name, final Class<?> definedIn) {
        final Map<?, Field> fields = buildMap(cls, definedIn != null);
        final Field field = fields.get(definedIn != null ? (Object)new FieldKey(name, definedIn, -1) : (Object)name);
        return field;
    }

    private Map<?, Field> buildMap(final Class<?> type, final boolean tupleKeyed) {
        Class<?> cls = type;
        synchronized (this) {
            if (!keyedByFieldNameCache.containsKey(type)) {
                final List<Class<?>> superClasses = new ArrayList<Class<?>>();
                while (!Object.class.equals(cls) && cls != null) {
                    superClasses.add(0, cls);
                    cls = cls.getSuperclass();
                }
                Map<String, Field> lastKeyedByFieldName = Collections.emptyMap();
                Map<FieldKey, Field> lastKeyedByFieldKey = Collections.emptyMap();
                for (final Class<?> element : superClasses) {
                    cls = element;
                    if (!keyedByFieldNameCache.containsKey(cls)) {
                        final Map<String, Field> keyedByFieldName = new HashMap<String, Field>(lastKeyedByFieldName);
                        final Map<FieldKey, Field> keyedByFieldKey = new LinkedHashMap<FieldKey, Field>(
                            lastKeyedByFieldKey);
                        final Field[] fields = cls.getDeclaredFields();
                        if (JVM.reverseFieldDefinition()) {
                            for (int i = fields.length >> 1; i-- > 0;) {
                                final int idx = fields.length - i - 1;
                                final Field field = fields[i];
                                fields[i] = fields[idx];
                                fields[idx] = field;
                            }
                        }
                        for (int i = 0; i < fields.length; i++) {
                            final Field field = fields[i];
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            final FieldKey fieldKey = new FieldKey(field.getName(), field.getDeclaringClass(), i);
                            final Field existent = keyedByFieldName.get(field.getName());
                            if (existent == null
                            // do overwrite statics
                                || (existent.getModifiers() & Modifier.STATIC) != 0
                                // overwrite non-statics with non-statics only
                                || existent != null
                                && (field.getModifiers() & Modifier.STATIC) == 0) {
                                keyedByFieldName.put(field.getName(), field);
                            }
                            keyedByFieldKey.put(fieldKey, field);
                        }
                        final Map<FieldKey, Field> sortedFieldKeys = sorter.sort(cls, keyedByFieldKey);
                        keyedByFieldNameCache.put(cls, keyedByFieldName);
                        keyedByFieldKeyCache.put(cls, sortedFieldKeys);
                        lastKeyedByFieldName = keyedByFieldName;
                        lastKeyedByFieldKey = sortedFieldKeys;
                    } else {
                        lastKeyedByFieldName = keyedByFieldNameCache.get(cls);
                        lastKeyedByFieldKey = keyedByFieldKeyCache.get(cls);
                    }
                }
                return tupleKeyed ? lastKeyedByFieldKey : lastKeyedByFieldName;
            }
        }
        return tupleKeyed ? keyedByFieldKeyCache.get(type) : keyedByFieldNameCache.get(type);
    }

    @Override
    public synchronized void flushCache() {
        final Set<Class<?>> objectTypeSet = Collections.<Class<?>>singleton(Object.class);
        keyedByFieldNameCache.keySet().retainAll(objectTypeSet);
        keyedByFieldKeyCache.keySet().retainAll(objectTypeSet);
        if (sorter instanceof Caching) {
            ((Caching)sorter).flushCache();
        }
    }

    protected Object readResolve() {
        init();
        return this;
    }
}
