package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.StreamException;

public class ErrorTest extends AbstractAcceptanceTest {

    class Thing {
        String one;
        int two;
    }

    protected void setUp() throws Exception {
        super.setUp();
        xstream.alias("thing", Thing.class);
    }

    public void testUnmarshallerThrowsExceptionWithDebuggingInfo() {
        try {
            xstream.fromXML("<thing>\n" +
                    "  <one>string 1</one>\n" +
                    "  <two>another string</two>\n" +
                    "</thing>");
            fail("Error expected");
        } catch (ConversionException e) {
            assertEquals("java.lang.NumberFormatException",
                    e.get("exception"));
            assertEquals("For input string: \"another string\"",
                    e.get("message"));
            assertEquals(Thing.class.getName(),
                    e.get("class"));
            assertEquals("/thing/two",
                    e.get("path"));
            assertEquals("3",
                    e.get("line number"));
            assertEquals("java.lang.Integer",
                    e.get("required-type"));
        }
    }

    public void testInvalidXml() {
        try {
            xstream.fromXML("<thing>\n" +
                    "  <one>string 1</one>\n" +
                    "  <two><<\n" +
                    "</thing>");
            fail("Error expected");
        } catch (ConversionException e) {
            assertEquals(StreamException.class.getName(),
                    e.get("exception"));
            assertContains("unexpected character in markup",
                    e.get("message"));
            assertEquals("/thing/two",
                    e.get("path"));
            assertEquals("3",
                    e.get("line number"));
        }

    }

    private void assertContains(String expected, String actual) {
        assertTrue("Substring not found. Expected <" + expected + "> but got <" + actual + ">",
                actual.indexOf(expected) > -1);
    }
}
