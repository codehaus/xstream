package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import junit.framework.TestCase;

public abstract class AbstractXMLReaderTest extends TestCase {

    // factory method
    protected abstract HierarchicalStreamReader createReader(String xml) throws Exception;

    public void testStartsAtRootTag() throws Exception {
        HierarchicalStreamReader xmlReader = createReader("<hello/>");
        assertEquals("hello", xmlReader.getNodeName());
    }

    public void testCanNavigateDownChildTagsByIndex() throws Exception {
        HierarchicalStreamReader xmlReader = createReader("<a><b><ooh/></b><b><aah/></b></a>");

        assertEquals("a", xmlReader.getNodeName());

        assertTrue(xmlReader.hasMoreChildren());

        xmlReader.moveDown(); // /a/b

        assertEquals("b", xmlReader.getNodeName());

        assertTrue(xmlReader.hasMoreChildren());

        xmlReader.moveDown(); // a/b/ooh
        assertEquals("ooh", xmlReader.getNodeName());
        assertFalse(xmlReader.hasMoreChildren());
        xmlReader.moveUp(); // a/b

        assertFalse(xmlReader.hasMoreChildren());

        xmlReader.moveUp(); // /a

        assertTrue(xmlReader.hasMoreChildren());

        xmlReader.moveDown(); // /a/b[2]

        assertEquals("b", xmlReader.getNodeName());

        assertTrue(xmlReader.hasMoreChildren());

        xmlReader.moveDown(); // a/b[2]/aah

        assertEquals("aah", xmlReader.getNodeName());
        assertFalse(xmlReader.hasMoreChildren());

        xmlReader.moveUp(); // a/b[2]

        assertFalse(xmlReader.hasMoreChildren());

        xmlReader.moveUp(); // a

        assertFalse(xmlReader.hasMoreChildren());
    }

    public void testChildTagsCanBeMixedWithOtherNodes() throws Exception {
        HierarchicalStreamReader xmlReader = createReader("<!-- xx --><a> <hello/> <!-- x --> getValue <world/></a>");

        assertTrue(xmlReader.hasMoreChildren());
        xmlReader.moveDown();
        assertEquals("hello", xmlReader.getNodeName());
        xmlReader.moveUp();

        assertTrue(xmlReader.hasMoreChildren());
        xmlReader.moveDown();
        assertEquals("world", xmlReader.getNodeName());
        xmlReader.moveUp();

        assertFalse(xmlReader.hasMoreChildren());
    }

    public void testAttributesCanBeFetchedFromTags() throws Exception {
        HierarchicalStreamReader xmlReader = createReader("" +
                "<hello one=\"1\" two=\"2\">" +
                "  <child three=\"3\"/>" +
                "</hello>"); // /hello

        assertEquals("1", xmlReader.getAttribute("one"));
        assertEquals("2", xmlReader.getAttribute("two"));
        assertNull(xmlReader.getAttribute("three"));

        xmlReader.moveDown(); // /hello/child
        assertNull(xmlReader.getAttribute("one"));
        assertNull(xmlReader.getAttribute("two"));
        assertEquals("3", xmlReader.getAttribute("three"));

    }

    public void testTextCanBeExtractedFromTag() throws Exception {
        HierarchicalStreamReader xmlReader = createReader("<root><a>some<!-- ignore me --> getValue!</a><b>more</b></root>");

        xmlReader.moveDown();
        assertEquals("some getValue!", xmlReader.getValue());
        xmlReader.moveUp();

        xmlReader.moveDown();
        assertEquals("more", xmlReader.getValue());
        xmlReader.moveUp();
    }

    public void testDoesNotIgnoreWhitespaceAroundText() throws Exception {
        HierarchicalStreamReader xmlReader = createReader("<root> hello world </root>");

        assertEquals(" hello world ", xmlReader.getValue());
    }

    public void testReturnsEmptyStringForEmptyTags() throws Exception {
        HierarchicalStreamReader xmlReader = createReader("<root></root>");

        String text = xmlReader.getValue();
        assertNotNull(text);
        assertEquals("", text);
    }

}
