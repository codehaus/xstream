/*
 * Copyright (C) 2003, 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 26. September 2003 by Joe Walnes
 */
package com.thoughtworks.acceptance;

import com.thoughtworks.acceptance.objects.Hardware;
import com.thoughtworks.acceptance.objects.Software;
import com.thoughtworks.acceptance.objects.StandardObject;
import com.thoughtworks.xstream.core.JVM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
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

        assertBothWaysNormalized(map, expected, "map", "entry", "string[1]");
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
                "      <vendor>microsoft</vendor>\n" +
                "      <name>windows</name>\n" +
                "    </software>\n" +
                "    <hardware>\n" +
                "      <arch>x86</arch>\n" +
                "      <name>p4</name>\n" +
                "    </hardware>\n" +
                "  </entry>\n" +
                "</map>";

        assertBothWays(map, expected);
    }

    static class ThingWithMap extends StandardObject {
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

    static class ThingWithDifferentTypesOfMaps extends StandardObject {
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
    
    public void testAllowsEntryToBeAliasedToSomethingElse() {
        Map map = new HashMap();
        map.put("benny", "hill");
        map.put("joe", "walnes");

        String expected = "" +
                "<map>\n" +
                "  <thing>\n" +
                "    <string>benny</string>\n" +
                "    <string>hill</string>\n" +
                "  </thing>\n" +
                "  <thing>\n" +
                "    <string>joe</string>\n" +
                "    <string>walnes</string>\n" +
                "  </thing>\n" +
                "</map>";

        xstream.alias("thing", Map.Entry.class);
        assertBothWaysNormalized(map, expected, "map", "thing", "string[1]");
    }

    public static class MyMap extends HashMap {

    }

    public void testSubclassesOfMapAreHandled() {
        MyMap myMap = new MyMap();
        myMap.put("hehe", "hoho");
        String xml = xstream.toXML(myMap);
        MyMap myOtherMap = (MyMap) xstream.fromXML(xml);
        assertEquals(myMap, myOtherMap);
    }
    
    public void testSynchronizedMap() {
        final String expected;
        if (JVM.is15()) {
            expected = "" +
                "<java.util.Collections_-SynchronizedMap serialization=\"custom\">\n" +
                "  <java.util.Collections_-SynchronizedMap>\n" +
                "    <default>\n" +
                "      <m/>\n" +
                "      <mutex class=\"java.util.Collections$SynchronizedMap\" reference=\"../../..\"/>\n" +
                "    </default>\n" +
                "  </java.util.Collections_-SynchronizedMap>\n" +
                "</java.util.Collections_-SynchronizedMap>";
        } else {
            expected = "" + 
                "<java.util.Collections_-SynchronizedMap>\n" + 
                "  <m/>\n" + 
                "  <mutex class=\"java.util.Collections$SynchronizedMap\" reference=\"..\"/>\n" + 
                "</java.util.Collections_-SynchronizedMap>";
        }

        assertBothWays(Collections.synchronizedMap(new HashMap()), expected);
    }
    
    public void testUnmodifiableMap() {
        String expected = "" +
            "<java.util.Collections_-UnmodifiableMap>\n" +
            "  <m/>\n" +
            "</java.util.Collections_-UnmodifiableMap>";

        assertBothWays(Collections.unmodifiableMap(new HashMap()), expected);
    }

    public void testEmptyMap() {
        assertBothWays(Collections.EMPTY_MAP, "<empty-map/>");
    }

    public void testEmptyMapIsImmutable() {
        List list = new ArrayList();
        list.add(Collections.EMPTY_MAP);
        list.add(Collections.EMPTY_MAP);
        assertBothWays(list, 
            "<list>\n" +
            "  <empty-map/>\n" +
            "  <empty-map/>\n" +
            "</list>");
    }

    public void testEmptyMapIsSingleton() {
        assertSame(Collections.EMPTY_MAP, xstream.fromXML("<empty-map/>"));
    }
    
    public void testSingletonMap() {
        String expected =""+
            "<singleton-map>\n" +
            "  <entry>\n" +
            "    <com.thoughtworks.acceptance.objects.Software>\n" + 
            "      <vendor>microsoft</vendor>\n" + 
            "      <name>windows</name>\n" + 
            "    </com.thoughtworks.acceptance.objects.Software>\n" + 
            "    <com.thoughtworks.acceptance.objects.Hardware>\n" + 
            "      <arch>x86</arch>\n" + 
            "      <name>p4</name>\n" + 
            "    </com.thoughtworks.acceptance.objects.Hardware>\n" + 
            "  </entry>\n" +
            "</singleton-map>";

        assertBothWays(Collections.singletonMap(new Software("microsoft", "windows"), new Hardware("x86", "p4")), expected);
    }
}
