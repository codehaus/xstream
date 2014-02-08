/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 29. May 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.extended;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.core.JVM;

/**
 * Converter for StackTraceElement (the lines of a stack trace) - JDK 1.4+ only.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley (binkley)</a>
 * @author Joe Walnes
 */
public class StackTraceElementConverter extends AbstractSingleValueConverter {

    // Regular expression to parse a line of a stack trace. Returns 4 groups.
    //
    // Example:       com.blah.MyClass.doStuff(MyClass.java:123)
    //                |-------1------| |--2--| |----3-----| |4|
    // (Note group 4 is optional is optional and only present if a colon char exists.)

    private static final Pattern PATTERN = Pattern.compile("^(.+)\\.([^\\(]+)\\(([^:]*)(:(\\d+))?\\)$");
    private static final StackTraceElementFactory FACTORY;
    static {
        StackTraceElementFactory factory = null;
        if (JVM.is15()) {
            Class factoryType = JVM.loadClassForName(
                "com.thoughtworks.xstream.converters.extended.StackTraceElementFactory15",
                false);
            try {
                factory = (StackTraceElementFactory)factoryType.newInstance();
            } catch (Exception e) {
                // N/A
            } catch (LinkageError e) {
                // N/A
            }
        }
        if (factory == null) {
            factory = new StackTraceElementFactory();
        }
        try {
            factory.unknownSourceElement("a", "b");
        } catch (Exception e) {
            factory = null;
        } catch (NoClassDefFoundError e) { // GAE
            factory = null;
        }
        FACTORY = factory;
    }

    public boolean canConvert(Class type) {
        return StackTraceElement.class.equals(type) && FACTORY != null;
    }
    
    public String toString(Object obj) {
        String s = super.toString(obj);
        // JRockit adds ":???" for invalid line number
        return s.replaceFirst(":\\?\\?\\?", "");
    }

    public Object fromString(String str) {
        Matcher matcher = PATTERN.matcher(str);
        if (matcher.matches()) {
            String declaringClass = matcher.group(1);
            String methodName = matcher.group(2);
            String fileName = matcher.group(3);
            if (fileName.equals("Unknown Source")) {
                return FACTORY.unknownSourceElement(declaringClass, methodName);
            } else if (fileName.equals("Native Method")) {
                return FACTORY.nativeMethodElement(declaringClass, methodName);
            } else {
                if (matcher.group(4) != null) {
                    int lineNumber = Integer.parseInt(matcher.group(5));
                    return FACTORY.element(declaringClass, methodName, fileName, lineNumber);
                } else {
                    return FACTORY.element(declaringClass, methodName, fileName);
                }
            }
        } else {
            throw new ConversionException("Could not parse StackTraceElement : " + str);
        }
    }

}
