package com.thoughtworks.xstream;

import com.thoughtworks.acceptance.StandardObject;
import com.thoughtworks.acceptance.objects.Software;
import com.thoughtworks.acceptance.someobjects.*;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import junit.framework.TestCase;
import org.dom4j.Element;

import java.io.StringReader;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.FileReader;

public class XStreamTest extends TestCase {

    private XStream xstream;

    protected void setUp() throws Exception {
        super.setUp();
        xstream = new XStream();
        xstream.alias("x", X.class);
        xstream.alias("y", Y.class);
        xstream.alias("z", Z.class);
        xstream.alias("funny", FunnyConstructor.class);
        xstream.alias("with-list", WithList.class);
    }

    public void testUnmarshalsObjectFromXml() {

        String xml =
                "<x>" +
                "  <aStr>joe</aStr>" +
                "  <anInt>8</anInt>" +
                "  <innerObj>" +
                "    <yField>walnes</yField>" +
                "  </innerObj>" +
                "</x>";

        X x = (X) xstream.fromXML(xml);

        assertEquals("joe", x.aStr);
        assertEquals(8, x.anInt);
        assertEquals("walnes", x.innerObj.yField);
    }

    public void testMarshalsObjectToXml() {
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

        assertEquals(expected, xstream.toXML(x));
    }

    public void testUnmarshalsClassWithoutDefaultConstructor() {
        if (!JVM.is14()) return;

        String xml =
                "<funny>" +
                "  <i>999</i>" +
                "</funny>";

        FunnyConstructor funnyConstructor = (FunnyConstructor) xstream.fromXML(xml);

        assertEquals(999, funnyConstructor.i);
    }

    public void testHandlesLists() {
        WithList original = new WithList();
        Y y = new Y();
        y.yField = "a";
        original.things.add(y);
        original.things.add(new X(3));
        original.things.add(new X(1));

        String xml = xstream.toXML(original);

        String expected =
                "<with-list>\n" +
                "  <things>\n" +
                "    <y>\n" +
                "      <yField>a</yField>\n" +
                "    </y>\n" +
                "    <x>\n" +
                "      <anInt>3</anInt>\n" +
                "    </x>\n" +
                "    <x>\n" +
                "      <anInt>1</anInt>\n" +
                "    </x>\n" +
                "  </things>\n" +
                "</with-list>";

        assertEquals(expected, xml);

        WithList result = (WithList) xstream.fromXML(xml);
        assertEquals(original, result);

    }

    public void testCanHandleNonStaticPrivateInnerClass() {
        if (!JVM.is14()) return;

        NonStaticInnerClass obj = new NonStaticInnerClass();
        obj.field = 3;

        xstream.alias("inner", NonStaticInnerClass.class);

        String xml = xstream.toXML(obj);

        String expected =
                "<inner>\n" +
                "  <field>3</field>\n" +
                "</inner>";

        assertEquals(expected, xml);

        NonStaticInnerClass result = (NonStaticInnerClass) xstream.fromXML(xml);
        assertEquals(obj.field, result.field);
    }

    public void testClassWithoutMappingUsesFullyQualifiedName() {
        Person obj = new Person();

        String xml = xstream.toXML(obj);

        String expected = "<com.thoughtworks.xstream.XStreamTest-Person/>";

        assertEquals(expected, xml);

        Person result = (Person) xstream.fromXML(xml);
        assertEquals(obj, result);
    }

    private class NonStaticInnerClass extends StandardObject {
        int field;
    }

    public void testCanBeBeUsedMultipleTimesWithSameInstance() {
        Y obj = new Y();
        obj.yField = "x";

        assertEquals(xstream.toXML(obj), xstream.toXML(obj));
    }

    public void testXStreamWithPeekMethodWithUnderlyingDom4JImplementation()
            throws Exception {

        String xml =
                "<person>" +
                "  <firstName>jason</firstName>" +
                "  <lastName>van Zyl</lastName>" +
                "  <element>" +
                "    <foo>bar</foo>" +
                "  </element>" +
                "</person>";

        xstream.registerConverter(new ElementConverter());

        xstream.alias("person", Person.class);

        Dom4JDriver driver = new Dom4JDriver();

        Person person = (Person) xstream.unmarshal(driver.createReader(new StringReader(xml)));

        assertEquals("jason", person.firstName);

        assertEquals("van Zyl", person.lastName);

        assertNotNull(person.element);

        assertEquals("bar", person.element.element("foo").getText());
    }

    public static class Person extends StandardObject {
        String firstName;
        String lastName;
        Element element;
    }

    private class ElementConverter implements Converter {

        public boolean canConvert(Class type) {
            return Element.class.isAssignableFrom(type);
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

            Element element = (Element) reader.peekUnderlyingNode();

            while (reader.hasMoreChildren()) {
                reader.moveDown();
                reader.moveUp();
            }

            return element;
        }
    }

    public void testXStreamPopulatingAnObjectGraphStartingWithALiveRootObject()
            throws Exception {

        String xml =
                "<component>" +
                "  <host>host</host>" +
                "  <port>8000</port>" +
                "</component>";

        xstream.alias("component", Component.class);

        Dom4JDriver driver = new Dom4JDriver();

        Component component0 = new Component();

        Component component1 = (Component) xstream.unmarshal(driver.createReader(new StringReader(xml)), component0);

        assertSame(component0, component1);

        assertEquals("host", component0.host);

        assertEquals(8000, component0.port);
    }

    static class Component {
        String host;
        int port;
    }

    public void testUnmarshallingWhereAllImplementationsAreSpecifiedUsingAClassIdentifier()
            throws Exception {

        String xml =
                "<handlerManager class='com.thoughtworks.acceptance.someobjects.HandlerManager'>" +
                "  <handlers>" +
                "    <handler class='com.thoughtworks.acceptance.someobjects.Handler'>" +
                "      <protocol class='com.thoughtworks.acceptance.someobjects.Protocol'>" +
                "        <id>foo</id> " +
                "      </protocol>  " +
                "    </handler>" +
                "  </handlers>" +
                "</handlerManager>";

        HandlerManager hm = (HandlerManager) xstream.fromXML(xml);

        Handler h = (Handler) hm.getHandlers().get(0);

        Protocol p = h.getProtocol();

        assertEquals("foo", p.getId());
    }
    
    public void testUnmarshalsObjectFromXmlWithCustomDefaultConverter() {
    		
        xstream.changeDefaultConverter(new ZConverter());
        
        String xml =
                "<z>" +
                "  <any-old-suff/>" +
                "</z>";
        
        Z z = (Z) xstream.fromXML(xml);

        assertEquals("z", z.field);
    }

    public void testAllowsStreamDrivenFromObjectOutputStream() throws IOException, ClassNotFoundException {
        Writer writer = new StringWriter();
        xstream.alias("software", Software.class);

        ObjectOutputStream oos = xstream.createObjectOutputStream(writer);
        oos.writeInt(123);
        oos.writeObject("hello");
        oos.writeObject(new Software("tw", "xs"));
        oos.close();

        String expectedXml = ""
                + "<object-stream>\n"
                + "  <int>123</int>\n"
                + "  <string>hello</string>\n"
                + "  <software>\n"
                + "    <vendor>tw</vendor>\n"
                + "    <name>xs</name>\n"
                + "  </software>\n"
                + "</object-stream>";

        assertEquals(expectedXml, writer.toString());

        ObjectInputStream ois = xstream.createObjectInputStream(new StringReader(writer.toString()));
        assertEquals(123, ois.readInt());
        assertEquals("hello", ois.readObject());
        assertEquals(new Software("tw", "xs"), ois.readObject());

        try {
            ois.readObject(); // As far as I can see this is the only clue the ObjectInputStream gives that it's done.
            fail("Expected EOFException");
        } catch (EOFException expectedException) {
            // good
        }
    }

}
