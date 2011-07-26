/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 03. October 2005 by Joerg Schaible
 */
package com.thoughtworks.xstream.converters.extended;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.testutil.TimeZoneChanger;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class ISO8601GregorianCalendarConverterTest extends TestCase {

    private ISO8601GregorianCalendarConverter converter;

    protected void setUp() throws Exception {
        super.setUp();
        converter = new ISO8601GregorianCalendarConverter();
        
        // Ensure that this test always run as if it were in the timezone of Panama.
        // This prevents failures when running the tests in different zones.
        // Note: 'America/Panama' has no relevance - it was just a randomly chosen zone.
        TimeZoneChanger.change("America/Panama");
    }

    protected void tearDown() throws Exception {
        TimeZoneChanger.reset();
        super.tearDown();
    }

    public void testRetainsDetailDownToMillisecondLevel() {
        // setup
        Calendar in = Calendar.getInstance();

        // execute
        String text = converter.toString(in);
        Calendar out = (Calendar) converter.fromString(text);

        // verify
        assertEquals(in, out);
    }
    
    public void testSavedTimeIsInUTC() {
        Calendar in = Calendar.getInstance();
        final String iso8601;
        iso8601 = new DateTime(in).toString();
        String converterXML =  converter.toString(in);
        assertEquals(iso8601, converterXML);
        
        Calendar out = (Calendar) converter.fromString(converterXML);
        assertEquals(in, out);
    }
    
    public void testCanLoadTimeInDifferentTimeZone() {
        Calendar in = Calendar.getInstance();
        String converterXML =  converter.toString(in);

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        Calendar timeInMoscow = Calendar.getInstance();
        timeInMoscow.setTime(in.getTime());
        Calendar out = (Calendar) converter.fromString(converterXML);
        assertEquals(timeInMoscow, out);
    }
    
    public void testCalendarWithExplicitTimeZone() {
        Calendar timeInMoscow = Calendar.getInstance();
        timeInMoscow.set(2010, 6, 3, 10, 20, 36);
        timeInMoscow.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        
        String converterXML =  converter.toString(timeInMoscow);
        Calendar out = (Calendar) converter.fromString(converterXML);
        assertEquals(timeInMoscow.getTimeInMillis(), out.getTimeInMillis());
        
        out.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        assertEquals(timeInMoscow, out);
    }

    public void testIsThreadSafe() throws InterruptedException {
        final List results = Collections.synchronizedList(new ArrayList());
        final ISO8601GregorianCalendarConverter converter = new ISO8601GregorianCalendarConverter();
        final Object monitor = new Object();
        final int numberOfCallsPerThread = 20;
        final int numberOfThreads = 20;

        // spawn some concurrent threads, that hammer the converter
        Runnable runnable = new Runnable() {
            public void run() {
                for (int i = 0; i < numberOfCallsPerThread; i++) {
                    try {
                        converter.fromString("1993-02-14T13:10:30");
                        results.add("PASS");
                    } catch (ConversionException e) {
                        results.add("FAIL");
                    } finally {
                        synchronized (monitor) {
                            monitor.notifyAll();
                        }
                    }
                }
            }
        };
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(runnable).start();
        }

        // wait for all results
        while (results.size() < numberOfThreads * numberOfCallsPerThread) {
            synchronized (monitor) {
                monitor.wait(100);
            }
        }

        assertTrue("Nothing succeded", results.contains("PASS"));
        assertFalse("At least one attempt failed", results.contains("FAIL"));
    }
}
