package com.thoughtworks.xstream.converters.extended;

import com.thoughtworks.xstream.converters.basic.AbstractBasicConverter;

import java.sql.Timestamp;

/**
 * Converts a java.sql.Timestamp to text.
 *
 * @author Joe Walnes
 */
public class SqlTimestampConverter extends AbstractBasicConverter {

    public boolean canConvert(Class type) {
        return type.equals(Timestamp.class);
    }

    protected Object fromString(String str) {
        return Timestamp.valueOf(str);
    }

}