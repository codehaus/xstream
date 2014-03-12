/*
 * Copyright (C) 2007, 2008, 2011, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 21.09.2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.converters.extended;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;


/**
 * A Converter for the XML Schema datatype <a href="http://www.w3.org/TR/xmlschema-2/#duration">duration</a> and the
 * Java type {@link Duration}.
 * <p>
 * The implementation uses a {@link DatatypeFactory} to create Duration objects. If no factory is provided and the
 * instantiation of the internal factory fails with a {@link DatatypeConfigurationException} , the converter will not
 * claim the responsibility for Duration objects.
 * </p>
 * 
 * @author John Kristian
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public class DurationConverter extends AbstractSingleValueConverter {
    private final DatatypeFactory factory;

    public DurationConverter() {
        this(new Object() {
            DatatypeFactory getFactory() {
                try {
                    return DatatypeFactory.newInstance();
                } catch (final DatatypeConfigurationException e) {
                    return null;
                }
            }
        }.getFactory());
    }

    public DurationConverter(final DatatypeFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean canConvert(final Class<?> c) {
        return factory != null && Duration.class.isAssignableFrom(c);
    }

    @Override
    public Object fromString(final String s) {
        return factory.newDuration(s);
    }
}
