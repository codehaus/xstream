package com.thoughtworks.acceptance;

import com.thoughtworks.acceptance.objects.Hardware;
import com.thoughtworks.acceptance.objects.Software;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapTest extends AbstractAcceptanceTest {

    public void testMapCanContainBasicObjects() {
        Map map = new HashMap();
        map.put("benny", "hill");
        map.put("joe", "walnes");

        String expected = "" +
                "<map>\n" +
                "  <entry>\n" +
                "    <string>benny</string>\n" +
                "    <string>hill</string>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>joe</string>\n" +
                "    <string>walnes</string>\n" +
                "  </entry>\n" +
                "</map>";

        assertBothWays(map, expected);
    }

    public void testMapCanContainCustomObjects() {
        Map map = new HashMap();
        map.put(new Software("microsoft", "windows"), new Hardware("x86", "p4"));

        xstream.alias("software", Software.class);
        xstream.alias("hardware", Hardware.class);

        String expected = "" +
                "<map>\n" +
                "  <entry>\n" +
                "    <software>\n" +
                "      <name>windows</name>\n" +
                "      <vendor>microsoft</vendor>\n" +
                "    </software>\n" +
                "    <hardware>\n" +
                "      <arch>x86</arch>\n" +
                "      <name>p4</name>\n" +
                "    </hardware>\n" +
                "  </entry>\n" +
                "</map>";

        assertBothWays(map, expected);
    }

    class ThingWithMap extends StandardObject {
        Map stuff = new HashMap();
    }

    public void testObjectCanContainMapAsField() {
        ThingWithMap t = new ThingWithMap();
        t.stuff.put("hi", "bye");

        xstream.alias("thing-with-map", ThingWithMap.class);

        String expected = "" +
                "<thing-with-map>\n" +
                "  <stuff>\n" +
                "    <entry>\n" +
                "      <string>hi</string>\n" +
                "      <string>bye</string>\n" +
                "    </entry>\n" +
                "  </stuff>\n" +
                "</thing-with-map>";

        assertBothWays(t, expected);
    }

    public void testSupportsOldHashtables() {

        Hashtable hashtable = new Hashtable();
        hashtable.put("hello", "world");

        String expected = "" +
                "<hashtable>\n" +
                "  <entry>\n" +
                "    <string>hello</string>\n" +
                "    <string>world</string>\n" +
                "  </entry>\n" +
                "</hashtable>";

        assertBothWays(hashtable, expected);
    }

    class ThingWithDifferentTypesOfMaps extends StandardObject {
        private Map m1 = new HashMap();
        private Map m2 = new Hashtable();
        private HashMap m3 = new HashMap();
        private Hashtable m4 = new Hashtable();
    }

    public void testObjectCanContainDifferentMapImplementations() {

        xstream.alias("thing", ThingWithDifferentTypesOfMaps.class);

        ThingWithDifferentTypesOfMaps thing = new ThingWithDifferentTypesOfMaps();

        String expected = "" +
                "<thing>\n" +
                "  <m1/>\n" +
                "  <m2 class=\"hashtable\"/>\n" +
                "  <m3/>\n" +
                "  <m4/>\n" +
                "</thing>";

        assertBothWays(thing, expected);

    }

    public void testLinkedHashMapRetainsOrdering() {
        Map map = new LinkedHashMap();
        map.put("Z", "a");
        map.put("C", "c");
        map.put("X", "b");

        LinkedHashMap result = (LinkedHashMap) assertBothWays(map,
                "<linked-hash-map>\n" +
                "  <entry>\n" +
                "    <string>Z</string>\n" +
                "    <string>a</string>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>C</string>\n" +
                "    <string>c</string>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>X</string>\n" +
                "    <string>b</string>\n" +
                "  </entry>\n" +
                "</linked-hash-map>");

        Object[] keys = result.keySet().toArray();
        assertEquals("Z", keys[0]);
        assertEquals("C", keys[1]);
        assertEquals("X", keys[2]);
    }
}
