package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Dom4JReaderTest extends AbstractReaderTest {

    // factory method
    protected HierarchicalStreamReader createReader(String xml) throws Exception {
        return new Dom4Reader(DocumentHelper.parseText(xml));
    }

    public void testCanReadFromElementOfLargerDocument() throws DocumentException {
        Document document = DocumentHelper.parseText("" +
                "<big>" +
                "  <small>" +
                "    <tiny/>" +
                "  </small>" +
                "  <small-two>" +
                "  </small-two>" +
                "</big>");
        Element small = document.getRootElement().element("small");

        HierarchicalStreamReader xmlReader = new Dom4Reader(small);
        assertEquals("small", xmlReader.name());
        xmlReader.nextChild();
        assertEquals("tiny", xmlReader.name());
    }

}
