package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.builder.XStreamBuilder;
import com.thoughtworks.xstream.builder.XStreamClassConfig;
import com.thoughtworks.xstream.ReadOnlyXStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Guilherme Silveira
 */
public class XStreamBuilderTest extends AbstractAcceptanceTest {
    private XStreamBuilder builder;

    public void setUp() {
        builder = new XStreamBuilder();
    }

    public static class House {
    }

    public void testHandleCorrectlyClassAliases() {

        builder.handle(House.class).as("house");

        House house = new House();
        String expected = "<house/>";
                          
        assertBothWays(builder.buildXStream(), house, expected);

    }

    public static class Office {
        private String address;
        public Office(String address) {
            this.address = address;
        }
    }

    public void testHandleCorrectlyFieldAliases() {

        XStreamClassConfig officeConf = builder.handle(Office.class).as("office");
        officeConf.handle("address").as("logradouro");

        Office office = new Office("Rua Vergueiro");
        String expected = "<office>\n  <logradouro>Rua Vergueiro</logradouro>\n</office>";

        assertBothWays(builder.buildXStream(), office, expected);

    }


    protected Object assertBothWays(ReadOnlyXStream xstream, Object root, String xml) {

        // First, serialize the object to XML and check it matches the expected XML.
        String resultXml = xstream.toXML(root);
        assertEquals(xml, resultXml);

        // Now deserialize the XML back into the object and check it equals the original object.
        // We do not really care about arrays and so on, because the tests are related to xstreambuilder
        Object resultRoot = xstream.fromXML(resultXml);
        assertEquals(xml, xstream.toXML(resultRoot));

        return resultRoot;
    }

    public void testHandleCorrectlyFieldOmmission() {

        XStreamClassConfig officeConf = builder.handle(Office.class).as("office");
        officeConf.ignores("address");

        Office office = new Office("Rua Vergueiro");
        String expected = "<office/>";

        assertBothWays(builder.buildXStream(), office, expected);

    }

    public void testHandleCorrectlyAttributes() {

        XStreamClassConfig officeConf = builder.handle(Office.class).as("office");
        officeConf.handle("address").asAttribute().as("logradouro");

        Office office = new Office("Rua Vergueiro");
        String expected = "<office logradouro=\"Rua Vergueiro\"/>";

        assertBothWays(builder.buildXStream(), office, expected);

    }

    public static class CollectionContainer {
        Collection collection;
    }

    public void testHandleCorrectlyDefaultImplementations() {

        XStreamClassConfig officeConf = builder.handle(Collection.class).implementedBy(HashSet.class);
        builder.handle(CollectionContainer.class).as("cc");

        CollectionContainer root = new CollectionContainer();
        root.collection = new HashSet();
        String expected = "<cc>\n  <collection/>\n</cc>";

        assertBothWays(builder.buildXStream(), root, expected);

    }

    public static class DoNothingConverter implements Converter {
        private final Class support;

        public DoNothingConverter(Class aClass) {
            this.support = aClass;
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            writer.startNode("wow");
            writer.endNode();
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            try {
                return support.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean canConvert(Class type) {
            return support.equals(type);
        }
    }

    public void testHandleCorrectlyConverterRegistrations() {

        XStreamClassConfig officeConf = builder.handle(CollectionContainer.class).as("cc");
        builder.register(new DoNothingConverter(CollectionContainer.class));

        CollectionContainer root = new CollectionContainer();
        root.collection = new HashSet();
        String expected = "<cc>\n  <wow/>\n</cc>";

        assertBothWays(builder.buildXStream(), root, expected);

    }

    @XStreamAlias("annotated")
    public static class Annotated {
    }

    public void testHandleCorrectlyAnnotatedClasses() {

        builder.handle(Annotated.class).annotated();

        Annotated root = new Annotated();
        String expected = "<annotated/>";

        assertBothWays(builder.buildXStream(), root, expected);

    }

}
