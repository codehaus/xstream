/*
 * Copyright (c) 2006, 2007, 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 11. October 2006 by Joerg Schaible
 */
package com.thoughtworks.xstream.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for primitives.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.2.1
 */
public final class Primitives {
    private final static Map BOX = new HashMap();
    private final static Map UNBOX = new HashMap();
    private final static Map NAMED_PRIMITIVE = new HashMap();
    private final static Map REPRESENTING_CHAR = new HashMap();
    
    static {
        final Class[][] boxing = new Class[][]{
            { Byte.TYPE, Byte.class},
            { Character.TYPE, Character.class},
            { Short.TYPE, Short.class},
            { Integer.TYPE, Integer.class},
            { Long.TYPE, Long.class},
            { Float.TYPE, Float.class},
            { Double.TYPE, Double.class},
            { Boolean.TYPE, Boolean.class},
            { Void.TYPE, Void.class},
        };
        final Character[] representingChars = { 
            Character.valueOf('B'), 
            Character.valueOf('C'), 
            Character.valueOf('S'), 
            Character.valueOf('I'), 
            Character.valueOf('J'), 
            Character.valueOf('F'), 
            Character.valueOf('D'), 
            Character.valueOf('Z'),
            null
         };
        for (int i = 0; i < boxing.length; i++) {
            final Class primitiveType = boxing[i][0];
            final Class boxedType = boxing[i][1];
            BOX.put(primitiveType, boxedType);
            UNBOX.put(boxedType, primitiveType);
            NAMED_PRIMITIVE.put(primitiveType.getName(), primitiveType);
            REPRESENTING_CHAR.put(primitiveType, representingChars[i]);
        }
    }

    static public Class box(final Class type) {
        return (Class)BOX.get(type);
    }
    
    static public Class unbox(final Class type) {
        return (Class)UNBOX.get(type);
    }

    /**
     * Check for a boxed type.
     * 
     * @param type the type to check
     * @return <code>true</code> if the type is boxed
     * @since upcoming
     */
    static public boolean isBoxed(final Class type) {
        return UNBOX.containsKey(type);
    }

    /**
     * Get the primitive type by name.
     * 
     * @param name the name of the type
     * @return the Java type or <code>null</code>
     * @since upcoming
     */
    static public Class primitiveType(final String name) {
        return (Class)NAMED_PRIMITIVE.get(name);
    }

    /**
     * Get the representing character of a primitive type.
     * 
     * @param type the primitive type
     * @return the representing character or 0
     * @since upcoming
     */
    static public char representingChar(final Class type) {
        Character ch = (Character)REPRESENTING_CHAR.get(type);
        return ch == null ? 0 : ch.charValue();
    }
}
