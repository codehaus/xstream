package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.acceptance.someobjects.X;
import com.thoughtworks.acceptance.someobjects.Y;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.StringReader;
import java.util.List;

public class JDomAcceptanceTest extends TestCase {

    private XStream xstream;

    protected void setUp() throws Exception {
        super.setUp();
        xstream = new XStream();
        xstream.alias("x", X.class);
    }

    public void testUnmarshalsObjectFromJDOM() throws Exception {
        String xml =
                "<x>" +
                "  <aStr>joe</aStr>" +
                "  <anInt>8</anInt>" +
                "  <innerObj>" +
                "    <yField>walnes</yField>" +
                "  </innerObj>" +
                "</x>";

        Document doc = new SAXBuilder().build(new StringReader(xml));

        X x = (X) xstream.unmarshal(new JDomReader(doc));

        assertEquals("joe", x.aStr);
        assertEquals(8, x.anInt);
        assertEquals("walnes", x.innerObj.yField);
    }

    public void testMarshalsObjectToJDOM() {
        X x = new X();
        x.anInt = 9;
        x.aStr = "zzz";
        x.innerObj = new Y();
        x.innerObj.yField = "ooo";

        String expected =
                "<x>\n" +
                "  <aStr>zzz</aStr>\n" +
                "  <anInt>9</anInt>\n" +
                "  <innerObj>\n" +
                "    <yField>ooo</yField>\n" +
                "  </innerObj>\n" +
                "</x>";

        JDomWriter writer = new JDomWriter();
        xstream.marshal(x, writer);
        List result = writer.getResult();

        assertEquals("Result list should contain exactly 1 element",
                                                        1, result.size());

        XMLOutputter outputter = new XMLOutputter("  ", true);
        outputter.setOmitDeclaration(true);
        outputter.setLineSeparator("\n");

        assertEquals(expected, outputter.outputString(result));
    }

    public void testXStreamPopulatingAnObjectGraphStartingWithALiveRootObject()
                                                throws Exception {
        String xml =
                "<component>" +
                "  <host>host</host>" +
                "  <port>8000</port>" +
                "</component>";

        xstream.alias("component", Component.class);

        HierarchicalStreamDriver driver = new JDomDriver();

        Component component0 = new Component();

        Component component1 = (Component) xstream.unmarshal(
                        driver.createReader(new StringReader(xml)), component0);

        assertSame(component0, component1);

        assertEquals("host", component0.host);

        assertEquals(8000, component0.port);
    }

    static class Component {
        String host;
        int port;
    }
}

