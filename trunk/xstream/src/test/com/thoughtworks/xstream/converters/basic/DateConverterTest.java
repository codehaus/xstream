package com.thoughtworks.xstream.converters.basic;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.testutil.TimeZoneChanger;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class DateConverterTest extends TestCase {

    private DateConverter converter = new DateConverter();

    protected void setUp() throws Exception {
        super.setUp();

        // Ensure that this test always run as if it were in the EST timezone.
        // This prevents failures when running the tests in different zones.
        // Note: 'EST' has no relevance - it was just a randomly chosen zone.
        TimeZoneChanger.change("EST");
    }

    protected void tearDown() throws Exception {
        TimeZoneChanger.reset();
        super.tearDown();
    }

    public void testRetainsDetailDownToMillisecondLevel() {
        // setup
        Date in = new Date();

        // execute
        String text = converter.toString(in);
        Date out = (Date)converter.fromString(text);

        // verify
        assertEquals(in, out);
        assertEquals(in.toString(), out.toString());
        assertEquals(in.getTime(), out.getTime());
    }

    public void testUnmarshallsOldXStreamDatesThatLackMillisecond() {
        Date expected = (Date)converter.fromString("2004-02-22 15:16:04.0 EST");

        assertEquals(expected, converter.fromString("2004-02-22 15:16:04.0 EST"));
        assertEquals(expected, converter.fromString("2004-02-22 15:16:04.0 PM"));
        assertEquals(expected, converter.fromString("2004-02-22 15:16:04PM"));
        assertEquals(expected, converter.fromString("2004-02-22 15:16:04 EST"));
        assertEquals(expected, converter.fromString("2004-02-22 15:16:04EST"));
    }

    public void testRetainsTimeZone() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2007, Calendar.MAY, 17, 19, 20, 32);
        Date dateEST = cal.getTime();
        String strEST = converter.toString(dateEST);
        TimeZone timeZone = TimeZone.getDefault();
        if (!JVM.is14() && timeZone.inDaylightTime(dateEST)) {
            // JDK 1.3 does not support TimeZone without daylight saving!
            assertEquals("2007-05-17 19:20:32.0 EDT", strEST);
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
            Date dateGMT = (Date)converter.fromString(strEST);
            assertEquals(dateEST, dateGMT);
            assertEquals("2007-05-18 00:20:32.0 GMT+01:00", converter.toString(dateGMT));
        } else {
            assertEquals("2007-05-17 19:20:32.0 EST", strEST);
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
            Date dateGMT = (Date)converter.fromString(strEST);
            assertEquals(dateEST, dateGMT);
            assertEquals("2007-05-18 01:20:32.0 GMT+01:00", converter.toString(dateGMT));
        }
    }

    public void testIsThreadSafe() throws InterruptedException {
        final List results = Collections.synchronizedList(new ArrayList());
        final DateConverter converter = new DateConverter();
        final Object monitor = new Object();
        final int numberOfCallsPerThread = 20;
        final int numberOfThreads = 20;

        // spawn some concurrent threads, that hammer the converter
        Runnable runnable = new Runnable() {
            public void run() {
                for (int i = 0; i < numberOfCallsPerThread; i++) {
                    try {
                        converter.fromString("2004-02-22 15:16:04.0 EST");
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

        assertTrue("Nothing suceeded", results.contains("PASS"));
        assertFalse("At least one attempt failed", results.contains("FAIL"));
    }
}
