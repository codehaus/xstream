/*
 * Copyright (C) 2003, 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2012, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 01. October 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.extended;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.TimeZone;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.core.util.ThreadSafeSimpleDateFormat;


/**
 * Converts a {@link Timestamp} to a string.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 */
public class SqlTimestampConverter extends AbstractSingleValueConverter {

    private final ThreadSafeSimpleDateFormat format = new ThreadSafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss", TimeZone
        .getTimeZone("UTC"), 0, 5, false);

    @Override
    public boolean canConvert(final Class<?> type) {
        return type.equals(Timestamp.class);
    }

    @Override
    public String toString(final Object obj) {
        final Timestamp timestamp = (Timestamp)obj;
        final StringBuilder buffer = new StringBuilder(format.format(timestamp)).append('.');
        if (timestamp.getNanos() == 0) {
            buffer.append('0');
        } else {
            final String nanos = String.valueOf(timestamp.getNanos() + 1000000000);
            int last = 10;
            while (last > 2 && nanos.charAt(last - 1) == '0') {
                --last;
            }
            buffer.append(nanos.subSequence(1, last));
        }
        return buffer.toString();
    }

    @Override
    public Object fromString(final String str) {
        final int idx = str.lastIndexOf('.');
        if (idx < 0 || str.length() - idx < 2 || str.length() - idx > 10) {
            throw new ConversionException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
        }
        try {
            final Timestamp timestamp = new Timestamp(format.parse(str.substring(0, idx)).getTime());
            final StringBuilder buffer = new StringBuilder(str.substring(idx + 1));
            while (buffer.length() != 9) {
                buffer.append('0');
            }
            timestamp.setNanos(Integer.parseInt(buffer.toString()));
            return timestamp;
        } catch (final NumberFormatException e) {
            throw new ConversionException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]", e);
        } catch (final ParseException e) {
            throw new ConversionException("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]");
        }
    }

}
