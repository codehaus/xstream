package com.thoughtworks.acceptance;

import com.thoughtworks.acceptance.objects.Hardware;
import com.thoughtworks.acceptance.objects.SampleLists;
import com.thoughtworks.acceptance.objects.Software;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.JVM;

import java.util.*;

public class CollectionsTest extends AbstractAcceptanceTest {

    public void testListsCanContainCustomObjects() {
        SampleLists lists = new SampleLists();
        lists.good.add(new Software("apache", "geronimo"));
        lists.good.add(new Software("caucho", "resin"));
        lists.good.add(new Hardware("risc", "strong-arm"));
        lists.bad.add(new Software("apache", "jserv"));

        xstream.alias("lists", SampleLists.class);
        xstream.alias("software", Software.class);
        xstream.alias("hardware", Hardware.class);

        String expected = "" +
                "<lists>\n" +
                "  <good>\n" +
                "    <software>\n" +
                "      <vendor>apache</vendor>\n" +
                "      <name>geronimo</name>\n" +
                "    </software>\n" +
                "    <software>\n" +
                "      <vendor>caucho</vendor>\n" +
                "      <name>resin</name>\n" +
                "    </software>\n" +
                "    <hardware>\n" +
                "      <arch>risc</arch>\n" +
                "      <name>strong-arm</name>\n" +
                "    </hardware>\n" +
                "  </good>\n" +
                "  <bad class=\"list\">\n" +
                "    <software>\n" +
                "      <vendor>apache</vendor>\n" +
                "      <name>jserv</name>\n" +
                "    </software>\n" +
                "  </bad>\n" +
                "</lists>";

        assertBothWays(lists, expected);
    }

    public void testListsCanContainBasicObjects() {
        SampleLists lists = new SampleLists();
        lists.good.add("hello");
        lists.good.add(new Integer(3));
        lists.good.add(Boolean.TRUE);

        xstream.alias("lists", SampleLists.class);

        String expected = "" +
                "<lists>\n" +
                "  <good>\n" +
                "    <string>hello</string>\n" +
                "    <int>3</int>\n" +
                "    <boolean>true</boolean>\n" +
                "  </good>\n" +
                "  <bad class=\"list\"/>\n" +
                "</lists>";

        assertBothWays(lists, expected);
    }

    public void testListCanBeRootObject() {
        Collection list = new ArrayList();
        list.add("hi");
        list.add("bye");

        String expected = "" +
                "<list>\n" +
                "  <string>hi</string>\n" +
                "  <string>bye</string>\n" +
                "</list>";

        assertBothWays(list, expected);
    }

    public void testSetCanBeRootObject() {
        Collection set = new HashSet();
        set.add("hi");
        set.add("bye");

        String expected = "" +
                "<set>\n" +
                "  <string>hi</string>\n" +
                "  <string>bye</string>\n" +
                "</set>";

        assertBothWays(set, expected);
    }

    public void testVector() {
        Vector vector = new Vector();
        vector.addElement("a");
        vector.addElement("b");

        assertBothWays(vector,
                "<vector>\n" +
                "  <string>a</string>\n" +
                "  <string>b</string>\n" +
                "</vector>");
    }

    public void testSyncronizedWrapper() {
        if (JVM.is15()) {
            return; // TODO: The list has changed on Java 1.5
        }

        // syncronized list has circular reference
        xstream.setMode(XStream.XPATH_REFERENCES);

        List list = Collections.synchronizedList(new LinkedList());
        list.add("hi");

        assertBothWays(list,
                "<java.util.Collections-SynchronizedList>\n" +
                "  <list class=\"linked-list\">\n" +
                "    <string>hi</string>\n" +
                "  </list>\n" +
                "  <c class=\"linked-list\" reference=\"../list\"/>\n" +
                "  <mutex class=\"java.util.Collections-SynchronizedList\" reference=\"..\"/>\n" +
                "</java.util.Collections-SynchronizedList>");
    }

    public void testEmptyList() {
        assertBothWays(Collections.EMPTY_LIST, "<java.util.Collections-EmptyList/>");
    }

    public void testUnmodifiableList() {
        // unodifiable list has duplicate refs
        xstream.setMode(XStream.XPATH_REFERENCES);

        List list = new ArrayList();
        list.add("hi");
        list = Collections.unmodifiableList(list);

        assertBothWays(list,
                "<java.util.Collections-UnmodifiableRandomAccessList resolves-to=\"java.util.Collections-UnmodifiableList\">\n" +
                "  <list>\n" +
                "    <string>hi</string>\n" +
                "  </list>\n" +
                "  <c class=\"list\" reference=\"../list\"/>\n" +
                "</java.util.Collections-UnmodifiableRandomAccessList>");
    }

    public void testLinkedHashSetRetainsOrdering() {
        Set set = new LinkedHashSet();
        set.add("Z");
        set.add("C");
        set.add("X");

        LinkedHashSet result = (LinkedHashSet) assertBothWays(set,
                "<linked-hash-set>\n" +
                "  <string>Z</string>\n" +
                "  <string>C</string>\n" +
                "  <string>X</string>\n" +
                "</linked-hash-set>");

        Object[] values = result.toArray();
        assertEquals("Z", values[0]);
        assertEquals("C", values[1]);
        assertEquals("X", values[2]);
    }
}
