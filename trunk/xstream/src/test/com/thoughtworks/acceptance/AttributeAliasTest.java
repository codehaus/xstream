package com.thoughtworks.acceptance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.testutil.TimeZoneChanger;

/**
 * @author Paul Hammant
 * @author Ian Cartwright
 * @author Mauro Talevi
 * @author J&ouml;rg Schaible
 */
public class AttributeAliasTest extends AbstractAcceptanceTest {

    protected void setUp() throws Exception {
        super.setUp();
        TimeZoneChanger.change("GMT");
    }

    protected void tearDown() throws Exception {
        TimeZoneChanger.reset();
        super.tearDown();
    }

    public void testWithCustomConverterAndFieldName() {
        One one = new One();
        one.two = new Two();
        one.id  = new ID("hullo");

        xstream.alias("one", One.class);
        xstream.aliasAttribute("id", ID.class);
        xstream.registerConverter(new MyIDConverter());

        String expected =
                "<one id=\"hullo\">\n" +
                "  <two/>\n" +
                "</one>";
        assertBothWays(one, expected);
    }

    public void testWithCustomConverterAndDifferentFieldName() {
        One one = new One();
        one.two = new Two();
        one.id  = new ID("hullo");

        xstream.alias("one", One.class);
        xstream.aliasAttribute("foo", ID.class);
        xstream.registerConverter(new MyIDConverter());

        String expected =
                "<one>\n" +
                "  <id>hullo</id>\n" +
                "  <two/>\n" +
                "</one>";
        assertBothWays(one, expected);
    }

    public void testWithKnownConverterAndFieldName() throws Exception {
        Three three = new Three();
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        three.date = format.parse("19/02/2006");

        xstream.alias("three", Three.class);
        xstream.aliasAttribute("date", Date.class);
        
        String expected =
            "<three date=\"2006-02-19 00:00:00.0 GMT\"/>";
        assertBothWays(three, expected);
    }

    public void testWithArbitraryFieldType() {
        One one = new One();
        one.two = new Two();
        one.id  = new ID("hullo");

        xstream.alias("one", One.class);
        xstream.aliasAttribute(ID.class);
        xstream.registerConverter(new MyIDConverter());

        String expected =
                "<one id=\"hullo\">\n" +
                "  <two/>\n" +
                "</one>";
        assertBothWays(one, expected);
    }

    public void testWithNullAttribute() {
        One one = new One();
        one.two = new Two();

        xstream.alias("one", One.class);
        xstream.aliasAttribute(ID.class);
        xstream.registerConverter(new MyIDConverter());

        String expected =
                "<one>\n" +
                "  <two/>\n" +
                "</one>";
        assertBothWays(one, expected);
    }

    public static class One implements HasID {
        public ID id;
        public Two two;

        public void setID(ID id) {
            this.id = id;
        }
    }

    public static interface HasID {
        void setID(ID id);
    }

    public static class Two {}

    public static class Three {
        public Date date;
    }

    public static class ID {
        public ID(String value) {
            this.value = value;
        }

        public String value;
    }

    private static class MyIDConverter extends AbstractSingleValueConverter {
        public boolean canConvert(Class type) {
            return type.equals(ID.class);
        }

        public String toString(Object obj) {
            return obj == null ? null : ((ID) obj).value;
        }

        public Object fromString(String str) {
            return new ID(str);
        }
    }

}



