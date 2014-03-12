/*
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2011, 2013, 2014 XStream Committers.
 * All rights reserved.
 *
 * Created on 08. January 2014 by Joerg Schaible, factored out from SunUnsafeReflectionProvider
 */
package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Field;

import sun.misc.Unsafe;


/**
 * Instantiates a new object bypassing the constructor using undocumented internal JDK features.
 * <p>
 * The code in the constructor will never be executed and parameters do not have to be known. This is the same method
 * used by the internals of standard Java serialization, but relies on internal code (sun.misc.Unsafe) that may not be
 * present on all JVMs.
 * <p>
 * <p>
 * The implementation will use standard Java functionality to write any fields. This requires Java 5 as minimum runtime
 * and is used as fallback on platforms that do not provide the complete implementation level for the internals (like
 * Dalvik).
 * <p>
 * 
 * @author J&ouml;rg Schaible
 * @author Joe Walnes
 * @author Brian Slesinsky
 * @since 1.4.7
 */
public class SunLimitedUnsafeReflectionProvider extends PureJavaReflectionProvider {

    protected static final Unsafe unsafe;
    protected static final Exception exception;
    static {
        Unsafe u = null;
        Exception ex = null;
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            u = (Unsafe)unsafeField.get(null);
        } catch (final SecurityException e) {
            ex = e;
        } catch (final NoSuchFieldException e) {
            ex = e;
        } catch (final IllegalArgumentException e) {
            ex = e;
        } catch (final IllegalAccessException e) {
            ex = e;
        }
        exception = ex;
        unsafe = u;
    }

    /**
     * @since 1.4.7
     */
    public SunLimitedUnsafeReflectionProvider() {
        super();
    }

    /**
     * @since 1.4.7
     */
    public SunLimitedUnsafeReflectionProvider(final FieldDictionary fieldDictionary) {
        super(fieldDictionary);
    }

    @Override
    public Object newInstance(final Class<?> type) {
        if (exception != null) {
            throw new ObjectAccessException("Cannot construct " + type.getName(), exception);
        }
        try {
            return unsafe.allocateInstance(type);
        } catch (final SecurityException e) {
            throw new ObjectAccessException("Cannot construct " + type.getName(), e);
        } catch (final InstantiationException e) {
            throw new ObjectAccessException("Cannot construct " + type.getName(), e);
        } catch (final IllegalArgumentException e) {
            throw new ObjectAccessException("Cannot construct " + type.getName(), e);
        }
    }

    @Override
    protected void validateFieldAccess(final Field field) {
        // (overriden) don't mind final fields.
    }

    private Object readResolve() {
        init();
        return this;
    }
}
