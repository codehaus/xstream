/*
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 06. May 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.basic;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Wrapper around java.text.SimpleDateFormat that can
 * be called by multiple threads concurrently.
 *
 * @author Joe Walnes
 * @deprecated since 1.2.1, moved to com.thoughtworks.xstream.core.util. 
 * It is not part of public API, use on your own risk.
 */
public class ThreadSafeSimpleDateFormat extends com.thoughtworks.xstream.core.util.ThreadSafeSimpleDateFormat {

    
    /**
     * @deprecated since 1.2.1 
     */
    public ThreadSafeSimpleDateFormat(String format, int initialPoolSize, int maxPoolSize) {
        super(format, TimeZone.getDefault(),  initialPoolSize, maxPoolSize, true);
    }

    /**
     * @deprecated since 1.2.1 
     */
    public String format(Date date) {
        return super.format(date);
    }

    /**
     * @deprecated since 1.2.1 
     */
    public Date parse(String date) throws ParseException {
        return super.parse(date);
    }
}
