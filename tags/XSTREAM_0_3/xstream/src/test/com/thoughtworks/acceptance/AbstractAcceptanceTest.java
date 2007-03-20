package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.XStream;
import junit.framework.TestCase;

import java.lang.reflect.Array;

public abstract class AbstractAcceptanceTest extends TestCase {

    protected XStream xstream = new XStream();

    protected void assertBothWays(Object root, String xml) {
        String resultXml = xstream.toXML(root);
        assertEquals(xml, resultXml);
        Object resultRoot = xstream.fromXML(resultXml);
        compareObjects(root, resultRoot);
    }

    private void compareObjects(Object expected, Object actual) {
        if (actual.getClass().isArray()) {
            compareArrays(expected, actual);
        } else {
            assertEquals(expected.getClass(), actual.getClass());
            assertEquals(expected, actual);
        }
    }

    private void compareArrays(Object expected, Object actual) {
        assertEquals(Array.getLength(expected), Array.getLength(actual));
        for (int i = 0; i < Array.getLength(expected); i++) {
            assertEquals(Array.get(expected, i), Array.get(actual, i));
        }
    }
}