package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import com.thoughtworks.xstream.core.*;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import junit.framework.TestCase;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class DuplicateReferenceTest extends TestCase {

    private ClassMapper classMapper;
    private DefaultConverterLookup converterLookup;

    protected void setUp() throws Exception {
        super.setUp();
        classMapper = new DefaultClassMapper();
        converterLookup = new DefaultConverterLookup(
                        new Sun14ReflectionProvider(),
                        classMapper, "class");
        classMapper.alias("thing", Thing.class, Thing.class);
        converterLookup.setupDefaults();
    }

    public void testReferencesAreWrittenToXml() {

        Thing sameThing = new Thing("hello");
        Thing anotherThing = new Thing("hello");

        List list = new ArrayList();
        list.add(sameThing);
        list.add(sameThing);
        list.add(anotherThing);

        String expected = "" +
                "<list id=\"1\">\n" +
                "  <thing id=\"2\">\n" +
                "    <field>hello</field>\n" +
                "  </thing>\n" +
                "  <thing reference=\"2\"/>\n" +
                "  <thing id=\"3\">\n" +
                "    <field>hello</field>\n" +
                "  </thing>\n" +
                "</list>";

        String xml = toXML(list);

        assertEquals(expected, xml);

        List result = (List) fromXML(xml);

        assertEquals(list, result);
    }

    public void testReferencesAreTheSameObjectWhenDeserialized() {

        Thing sameThing = new Thing("hello");
        Thing anotherThing = new Thing("hello");

        List list = new ArrayList();
        list.add(sameThing);
        list.add(sameThing);
        list.add(anotherThing);

        String xml = toXML(list);
        List result = (List)fromXML(xml);

        Thing t0 = (Thing) result.get(0);
        Thing t1 = (Thing) result.get(1);
        Thing t2 = (Thing) result.get(2);

        t0.field = "bye";

        assertEquals("bye", t0.field);
        assertEquals("bye", t1.field);
        assertEquals("hello", t2.field);

    }

    private String toXML(Object obj) {
        StringWriter buffer = new StringWriter();
        HierarchicalStreamWriter writer = new PrettyPrintWriter(buffer);
        Marshaller marshaller = new ReferenceByIdMarshaller(
                        writer, converterLookup, classMapper);

        marshaller.start(obj);
        return buffer.toString();
    }

    private Object fromXML(String xml) {
        XppReader reader = new XppReader(new StringReader(xml));

        Unmarshaller unmarshaller = new ReferenceByIdUnmarshaller(
                null, reader, converterLookup, classMapper, "class");

        return unmarshaller.start();
    }

    class Thing extends StandardObject {
        public String field;

        public Thing(String field) {
            this.field = field;
        }
    }


}
