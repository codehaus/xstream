package com.thoughtworks.xstream.io.xml;

import junit.framework.TestCase;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class AbstractXMLWriterTest extends TestCase {

    protected HierarchicalStreamWriter writer;

    protected abstract void assertXmlProducedIs(String expected);

    public void testProducesXmlElements() {
        writer.startNode("hello");
        writer.setValue("world");
        writer.endNode();

        assertXmlProducedIs("<hello>world</hello>");
    }

    public void testSupportsNestedElements() {

        writer.startNode("a");

        writer.startNode("b");
        writer.setValue("one");
        writer.endNode();

        writer.startNode("b");
        writer.setValue("two");
        writer.endNode();

        writer.startNode("c");
        writer.startNode("d");
        writer.setValue("three");
        writer.endNode();
        writer.endNode();

        writer.endNode();

        assertXmlProducedIs("<a><b>one</b><b>two</b><c><d>three</d></c></a>");
    }

    public void testSupportsEmptyTags() {
        writer.startNode("empty");
        writer.endNode();

        assertXmlProducedIs("<empty/>");
    }

    public void testSupportsAttributes() {
        writer.startNode("person");
        writer.addAttribute("firstname", "Joe");
        writer.addAttribute("lastname", "Walnes");
        writer.endNode();

        assertXmlProducedIs("<person firstname=\"Joe\" lastname=\"Walnes\"/>");
    }

    public void testEscapesXmlUnfriendlyCharacters() {
        writer.startNode("evil");
        writer.addAttribute("attr", "w0000 $ <x\"x> &!;");
        writer.setValue("w0000 $ <xx> &!;");
        writer.endNode();

        assertXmlProducedIs("<evil attr=\"w0000 $ &lt;x&quot;x&gt; &amp;!;\">w0000 $ &lt;xx&gt; &amp;!;</evil>");
    }

    public void testEscapesWhitespaceCharacters() {
        writer.startNode("evil");
        writer.setValue("one\ntwo\rthree\r\nfour\n\rfive\tsix");
        writer.endNode();

        assertXmlProducedIs("<evil>one\n"
                + "two&#x0D;three&#x0D;\n"
                + "four\n"
                + "&#x0D;five\tsix</evil>");
    }

}
