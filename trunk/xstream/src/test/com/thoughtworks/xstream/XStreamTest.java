package com.thoughtworks.xstream;

import com.thoughtworks.acceptance.StandardObject;
import com.thoughtworks.someobjects.FunnyConstructor;
import com.thoughtworks.someobjects.WithList;
import com.thoughtworks.someobjects.X;
import com.thoughtworks.someobjects.Y;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.objecttree.ObjectTree;
import com.thoughtworks.xstream.xml.XMLReader;
import com.thoughtworks.xstream.xml.XMLWriter;
import com.thoughtworks.xstream.xml.dom4j.Dom4JXMLReaderDriver;
import junit.framework.TestCase;
import org.dom4j.Element;

import java.util.List;
import java.util.ArrayList;

public class XStreamTest extends TestCase {

    private XStream xstream;

    protected void setUp() throws Exception {
        super.setUp();
        xstream = new XStream();
        xstream.alias("x", X.class);
        xstream.alias("y", Y.class);
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
        original.things.add(new FunnyConstructor(3));
        original.things.add(new FunnyConstructor(1));

        String xml = xstream.toXML(original);

        String expected =
                "<with-list>\n" +
                "  <things>\n" +
                "    <y>\n" +
                "      <yField>a</yField>\n" +
                "    </y>\n" +
                "    <funny>\n" +
                "      <i>3</i>\n" +
                "    </funny>\n" +
                "    <funny>\n" +
                "      <i>1</i>\n" +
                "    </funny>\n" +
                "  </things>\n" +
                "</with-list>";

        assertEquals(expected, xml);

        WithList result = (WithList) xstream.fromXML(xml);
        assertEquals(original, result);

    }

    public void testCanHandleNonStaticPrivateInnerClass() {
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
        NonStaticInnerClass obj = new NonStaticInnerClass();
        obj.field = 3;

        String xml = xstream.toXML(obj);

        String expected =
                "<com.thoughtworks.xstream.XStreamTest-NonStaticInnerClass>\n" +
                "  <field>3</field>\n" +
                "</com.thoughtworks.xstream.XStreamTest-NonStaticInnerClass>";

        assertEquals(expected, xml);

        NonStaticInnerClass result = (NonStaticInnerClass) xstream.fromXML(xml);
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

        xstream.registerConverter( new ElementConverter() );

        xstream.alias( "person", Person.class );

        Dom4JXMLReaderDriver driver = new Dom4JXMLReaderDriver();

        Person person = (Person) xstream.fromXML( driver.createReader( xml ) );

        assertEquals( "jason", person.firstName );

        assertEquals( "van Zyl", person.lastName );

        assertNotNull( person.element );

        assertEquals( "bar", person.element.element( "foo" ).getText() );
    }

    static class Person {
        String firstName;
        String lastName;
        Element element;
    }

    private class ElementConverter
        implements Converter {

        public boolean canConvert( Class type ) {
            return Element.class.isAssignableFrom( type );
        }

        public void toXML( ObjectTree objectGraph, XMLWriter xmlWriter, ConverterLookup converterLookup ) {
        }

        public void fromXML( ObjectTree objectGraph, XMLReader xmlReader, ConverterLookup converterLookup, Class requiredType ) {

            Element element = (Element) xmlReader.peek();

            while ( xmlReader.nextChild() )
            {
                xmlReader.pop();
            }

            objectGraph.set( element );
        }
    }

    public void testXStreamPopulatingAnObjectGraphStartingWithALiveRootObject()
        throws Exception {

        String xml =
            "<component>" +
            "  <host>host</host>" +
            "  <port>8000</port>" +
            "</component>";

        xstream.alias( "component", Component.class );

        Dom4JXMLReaderDriver driver = new Dom4JXMLReaderDriver();

        Component component0 = new Component();

        Component component1 = (Component) xstream.fromXML( driver.createReader( xml ), component0 );

        assertEquals( component0, component1 );

        assertEquals( "host", component0.host );

        assertEquals( 8000, component0.port );
    }

    static class Component
    {
        String host;
        int port;
    }

    public void xtestXStreamOutputWithEmptyCollections() {

        String expected =
            "<class-with-an-empty-collection>" +
            "</class-with-an-empty-collection>";

        xstream.alias( "class-with-an-empty-collection", ClassWithAnEmptyCollection.class );

        String s = xstream.toXML( new ClassWithAnEmptyCollection() );

        assertEquals( s, expected );
    }

    static class ClassWithAnEmptyCollection
    {
        List components = new ArrayList();
    }
}
