/*
 * Copyright (C) 2003, 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 26. September 2003 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.basic;

/**
 * Converts a double primitive or {@link Double} wrapper to a string.
 * 
 * @author Joe Walnes
 */
public class DoubleConverter extends AbstractSingleValueConverter {

    @Override
    public boolean canConvert(final Class<?> type) {
        return type.equals(double.class) || type.equals(Double.class);
    }

    @Override
    public Object fromString(final String str) {
        return Double.valueOf(str);
    }

}
