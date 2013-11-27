/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2013 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 30. May 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.extended;

/**
 * Factory for creating StackTraceElements.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley (binkley)</a>
 * @author Joe Walnes
 */
public class StackTraceElementFactory {

    public StackTraceElement nativeMethodElement(String declaringClass, String methodName) {
        return create(declaringClass, methodName, "Native Method", -2);
    }

    public StackTraceElement unknownSourceElement(String declaringClass, String methodName) {
        return create(declaringClass, methodName, "Unknown Source", -1);
    }

    public StackTraceElement element(String declaringClass, String methodName, String fileName) {
        return create(declaringClass, methodName, fileName, -1);
    }

    public StackTraceElement element(String declaringClass, String methodName, String fileName, int lineNumber) {
        return create(declaringClass, methodName, fileName, lineNumber);
    }

    private StackTraceElement create(String declaringClass, String methodName, String fileName, int lineNumber) {
        return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }
}
