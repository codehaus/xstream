package com.thoughtworks.xstream.io.xml;

import org.jdom.output.XMLOutputter;

import java.util.List;

public class JDomWriterTest extends AbstractXMLWriterTest {

    private List result;

    protected void setUp() throws Exception {
        super.setUp();
        writer = new JDomWriter();
        result = ((JDomWriter)writer).getResult();
    }

    protected void assertXmlProducedIs(String expected) {
        XMLOutputter outputter = new XMLOutputter();
        String actual = outputter.outputString(result);
        assertEquals(expected, actual);
    }

    // inherits tests from superclass
}
