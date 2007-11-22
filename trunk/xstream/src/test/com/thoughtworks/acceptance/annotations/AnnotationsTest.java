package com.thoughtworks.acceptance.annotations;

import com.thoughtworks.acceptance.AbstractAcceptanceTest;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;


/**
 * Tests for annotation detection.
 * 
 * @author Chung-Onn Cheong
 * @author Mauro Talevi
 * @author Guilherme Silveira
 * @author J&ouml;rg Schaible
 */
public class AnnotationsTest extends AbstractAcceptanceTest {

    @XStreamAlias("param")
    public static class ParameterizedContainer {

        private ParameterizedType<InternalType> type;

        public ParameterizedContainer() {
            type = new ParameterizedType<InternalType>(new InternalType());
        }

    }

    @XStreamAlias("param")
    public static class DoubleParameterizedContainer {

        private ArrayList<ArrayList<InternalType>> list;

        public DoubleParameterizedContainer() {
            list = new ArrayList<ArrayList<InternalType>>();
            list.add(new ArrayList<InternalType>());
            list.get(0).add(new InternalType());
        }

    }

    @XStreamAlias("second")
    public static class InternalType {
        @XStreamAlias("aliased")
        private String original = "value";

        @Override
        public boolean equals(Object obj) {
            return obj instanceof InternalType
                ? original.equals(((InternalType)obj).original)
                : false;
        }

    }

    @XStreamAlias("typeAlias")
    public static class ParameterizedType<T> {
        @XStreamAlias("fieldAlias")
        private T object;

        public ParameterizedType(T object) {
            this.object = object;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ParameterizedType ? object
                .equals(((ParameterizedType)obj).object) : false;
        }
    }

    public void testAreDetectedInParameterizedTypes() {
        String xml = ""
            + "<param>\n"
            + "  <type>\n"
            + "    <fieldAlias class=\"second\">\n"
            + "      <aliased>value</aliased>\n"
            + "    </fieldAlias>\n"
            + "  </type>\n"
            + "</param>";
        assertBothWays(new ParameterizedContainer(), xml);
    }

    public void testAreDetectedInNestedParameterizedTypes() {
        String xml = ""
            + "<param>\n"
            + "  <list>\n"
            + "    <list>\n"
            + "      <second>\n"
            + "        <aliased>value</aliased>\n"
            + "      </second>\n"
            + "    </list>\n"
            + "  </list>\n"
            + "</param>";
        assertBothWays(new DoubleParameterizedContainer(), xml);
    }

    public void testAreDetectedInArrays() {
        InternalType[] internalTypes = new InternalType[]{
            new InternalType(), new InternalType()};
        String xml = ""
            + "<second-array>\n"
            + "  <second>\n"
            + "    <aliased>value</aliased>\n"
            + "  </second>\n"
            + "  <second>\n"
            + "    <aliased>value</aliased>\n"
            + "  </second>\n"
            + "</second-array>";
        assertBothWays(internalTypes, xml);
    }

    public void testAreDetectedInParametrizedArrays() {
        ParameterizedType<String>[] types = new ParameterizedType[]{
            new ParameterizedType<String>("foo"), new ParameterizedType<String>("bar")};
        String xml = ""
            + "<typeAlias-array>\n"
            + "  <typeAlias>\n"
            + "    <fieldAlias class=\"string\">foo</fieldAlias>\n"
            + "  </typeAlias>\n"
            + "  <typeAlias>\n"
            + "    <fieldAlias class=\"string\">bar</fieldAlias>\n"
            + "  </typeAlias>\n"
            + "</typeAlias-array>";
        assertBothWays(types, xml);
    }
    
    public void testAreDetectedInJDKCollection() {
        List<InternalType> list = new ArrayList<InternalType>();
        list.add(new InternalType());
        String xml = ""
            + "<list>\n"
            + "  <second>\n"
            + "    <aliased>value</aliased>\n"
            + "  </second>\n"
            + "</list>";
        assertBothWays(list, xml);
    }

    public void testForClassIsDetectedAtDeserialization() {
        // must preprocess annotations here
        xstream.processAnnotations(InternalType.class);
        InternalType internalType = new InternalType();
        String xml = "" // 
            + "<second>\n" // 
            + "  <aliased>value</aliased>\n" // 
            + "</second>";
        assertEquals(internalType, xstream.fromXML(xml));
    }
}
