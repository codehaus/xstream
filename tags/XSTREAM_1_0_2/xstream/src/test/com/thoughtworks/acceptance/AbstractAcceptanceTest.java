package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import junit.framework.TestCase;

import java.lang.reflect.Array;

public abstract class AbstractAcceptanceTest extends TestCase {

    protected XStream xstream = new XStream(new XppDriver());

    protected Object assertBothWays(Object root, String xml) {
        String resultXml = xstream.toXML(root);
        assertEquals(xml, resultXml);
        Object resultRoot = xstream.fromXML(resultXml);
        compareObjects(root, resultRoot);
        return resultRoot;
    }

    protected void compareObjects(Object expected, Object actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            if (actual.getClass().isArray()) {
                assertArrayEquals(expected, actual);
            } else {
                assertEquals(expected.getClass(), actual.getClass());
                assertEquals(expected, actual);
            }
        }
    }

    protected void assertArrayEquals(Object expected, Object actual) {
        assertEquals(Array.getLength(expected), Array.getLength(actual));
        for (int i = 0; i < Array.getLength(expected); i++) {
            assertEquals(Array.get(expected, i), Array.get(actual, i));
        }
    }

    protected void assertByteArrayEquals(byte expected[], byte actual[]) {
        assertEquals(dumpBytes(expected), dumpBytes(actual));
    }

    private String dumpBytes(byte bytes[]) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            result.append(bytes[i]).append(' ');
            if (bytes[i] < 100) result.append(' ');
            if (bytes[i] < 10) result.append(' ');
            if (i % 16 == 15) result.append('\n');
        }
        return result.toString();
    }

}
