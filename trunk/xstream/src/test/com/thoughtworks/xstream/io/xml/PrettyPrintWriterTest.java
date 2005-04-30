package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.core.util.QuickWriter;
import junit.framework.TestCase;

import java.io.StringWriter;

public class PrettyPrintWriterTest extends AbstractXMLWriterTest {
    private StringWriter buffer;

    protected void setUp() throws Exception {
        super.setUp();
        buffer = new StringWriter();
        writer = new PrettyPrintWriter(buffer, "  ");
    }

    protected void assertXmlProducedIs(String expected) {
        assertEquals(expected, buffer.toString());
    }

    public void testSupportsNestedElements() { // Note: This overrides a test in superclass to include indentation

        writer.startNode("hello");
        writer.startNode("world");
        writer.addAttribute("id", "one");

        writer.startNode("one");
        writer.setValue("potato");
        writer.endNode();

        writer.startNode("two");
        writer.addAttribute("id", "two");
        writer.setValue("potatae");
        writer.endNode();

        writer.endNode();

        writer.startNode("empty");
        writer.endNode();

        writer.endNode();

        String expected =
                "<hello>\n" +
                "  <world id=\"one\">\n" +
                "    <one>potato</one>\n" +
                "    <two id=\"two\">potatae</two>\n" +
                "  </world>\n" +
                "  <empty/>\n" +
                "</hello>";

        assertXmlProducedIs(expected);
    }

    public void testAllowsUserToOverrideTextAndAttributeEscapingRules() {
        writer = new PrettyPrintWriter(buffer, "  ") {
            protected void writeAttributeValue(QuickWriter writer, String text) {
                writer.write(text.replaceAll("&", "_&_"));
            }

            protected void writeText(QuickWriter writer, String text) {
                writer.write(text.replaceAll("&", "AND"));
            }
        };

        writer.startNode("evil");
        writer.addAttribute("attr", "hello & stuff");
        writer.setValue("bye & stuff");
        writer.endNode();

        assertXmlProducedIs("<evil attr=\"hello _&_ stuff\">bye AND stuff</evil>");
    }
}
