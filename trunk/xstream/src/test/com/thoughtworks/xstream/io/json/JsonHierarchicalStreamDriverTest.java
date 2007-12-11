/*
 * Copyright (C) 2006 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 22. June 2006 by Mauro Talevi
 */
package com.thoughtworks.xstream.io.json;

import com.thoughtworks.xstream.XStream;

import junit.framework.TestCase;

import java.awt.Color;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Some of these test cases are taken from example JSON listed at
 * http://www.json.org/example.html
 * 
 * @author Paul Hammant
 * @author J&ouml;rg Schaible
 */
public class JsonHierarchicalStreamDriverTest extends TestCase {

    public void testDoesNotSupportReader() {
        try {
            new JsonHierarchicalStreamDriver().createReader((Reader)null);
            fail("should have barfed");
        } catch (UnsupportedOperationException uoe) {
            // expected
        }
    }

    public void testDoesNotSupportInputStream() {
        try {
            new JsonHierarchicalStreamDriver().createReader((InputStream)null);
            fail("should have barfed");
        } catch (UnsupportedOperationException uoe) {
            // expected
        }
    }

    public void testCanMarshalSimpleTypes() {

        String expected = ("{'innerMessage': {\n"
            + "  'long1': 5,\n"
            + "  'long2': 42,\n"
            + "  'greeting': 'hello',\n"
            + "  'num1': 2,\n"
            + "  'num2': 3,\n"
            + "  'bool': true,\n"
            + "  'bool2': true,\n"
            + "  'char1': 'A',\n"
            + "  'char2': 'B',\n"
            + "  'innerMessage': {\n"
            + "    'long1': 0,\n"
            + "    'greeting': 'bonjour',\n"
            + "    'num1': 3,\n"
            + "    'bool': false,\n"
            + "    'char1': '\\u00'\n"
            + "  }\n"
            + "}}").replace('\'', '"');

        XStream xs = new XStream(new JsonHierarchicalStreamDriver());

        xs.alias("innerMessage", Message.class);

        Message message = new Message("hello");
        message.long1 = 5L;
        message.long2 = new Long(42);
        message.num1 = 2;
        message.num2 = new Integer(3);
        message.bool = true;
        message.bool2 = Boolean.TRUE;
        message.char1 = 'A';
        message.char2 = new Character('B');

        Message message2 = new Message("bonjour");
        message2.num1 = 3;

        message.innerMessage = message2;

        assertEquals(expected, xs.toXML(message));
    }

    public static class Message {
        long long1;
        Long long2;
        String greeting;
        int num1;
        Integer num2;
        boolean bool;
        Boolean bool2;
        char char1;
        Character char2;
        Message innerMessage;

        public Message(String greeting) {
            this.greeting = greeting;
        }
    }

    String expectedMenuStart = ""
        + "{'menu': {\n"
        + "  'id': 'file',\n"
        + "  'value': 'File:',\n"
        + "  'popup': {\n"
        + "    'menuitem': [";
    String expectedNew = ""
        + "      {\n"
        + "        'value': 'New',\n"
        + "        'onclick': 'CreateNewDoc()'\n"
        + "      }";
    String expectedOpen = ""
        + "      {\n"
        + "        'value': 'Open',\n"
        + "        'onclick': 'OpenDoc()'\n"
        + "      }";
    String expectedClose = ""
        + "      {\n"
        + "        'value': 'Close',\n"
        + "        'onclick': 'CloseDoc()'\n"
        + "      }";
    String expectedMenuEnd = "" + "    ]\n" + "  }\n" + "}}";
    String expected = (expectedMenuStart
        + "\n"
        + expectedNew
        + ",\n"
        + expectedOpen
        + ",\n"
        + expectedClose
        + "\n" + expectedMenuEnd).replace('\'', '"');

    public void testCanMarshalLists() {

        // This from http://www.json.org/example.html

        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("menu", MenuWithList.class);
        xs.alias("menuitem", MenuItem.class);

        MenuWithList menu = new MenuWithList();

        assertEquals(expected, xs.toXML(menu));
    }

    public void testCanMarshalArrays() {

        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("menu", MenuWithArray.class);
        xs.alias("menuitem", MenuItem.class);

        MenuWithArray menu = new MenuWithArray();

        assertEquals(expected, xs.toXML(menu));
    }

    public void testCanMarshalSets() {

        // This from http://www.json.org/example.html

        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("menu", MenuWithSet.class);
        xs.alias("menuitem", MenuItem.class);

        MenuWithSet menu = new MenuWithSet();

        String json = xs.toXML(menu);
        assertTrue(json.startsWith(expectedMenuStart.replace('\'', '"')));
        assertTrue(json.indexOf(expectedNew.replace('\'', '"')) > 0);
        assertTrue(json.indexOf(expectedOpen.replace('\'', '"')) > 0);
        assertTrue(json.indexOf(expectedClose.replace('\'', '"')) > 0);
        assertTrue(json.endsWith(expectedMenuEnd.replace('\'', '"')));
    }

    public static class MenuWithList {
        String id = "file";
        String value = "File:";
        PopupWithList popup = new PopupWithList();
    }

    public static class PopupWithList {
        List menuitem;
        {
            menuitem = new ArrayList();
            menuitem.add(new MenuItem("New", "CreateNewDoc()"));
            menuitem.add(new MenuItem("Open", "OpenDoc()"));
            menuitem.add(new MenuItem("Close", "CloseDoc()"));
        }
    }

    public static class MenuWithArray {
        String id = "file";
        String value = "File:";
        PopupWithArray popup = new PopupWithArray();
    }

    public static class PopupWithArray {
        MenuItem[] menuitem = new MenuItem[]{
            new MenuItem("New", "CreateNewDoc()"), new MenuItem("Open", "OpenDoc()"),
            new MenuItem("Close", "CloseDoc()")};
    }

    public static class MenuWithSet {
        String id = "file";
        String value = "File:";
        PopupWithSet popup = new PopupWithSet();
    }

    public static class PopupWithSet {
        Set menuitem;
        {
            menuitem = new HashSet();
            menuitem.add(new MenuItem("New", "CreateNewDoc()"));
            menuitem.add(new MenuItem("Open", "OpenDoc()"));
            menuitem.add(new MenuItem("Close", "CloseDoc()"));
        }

    }

    public static class MenuItem {
        public String value; // assume unique
        public String onclick;

        public MenuItem(String value, String onclick) {
            this.value = value;
            this.onclick = onclick;
        }

        public int hashCode() {
            return value.hashCode();
        }

    }

    public void testCanMarshalTypesWithPrimitives() {

        // This also from http://www.expected.org/example.html

        String expected = ("{'widget': {\n"
            + "  'debug': 'on',\n"
            + "  'window': {\n"
            + "    'title': 'Sample Konfabulator Widget',\n"
            + "    'name': 'main_window',\n"
            + "    'width': 500,\n"
            + "    'height': 500\n"
            + "  },\n"
            + "  'image': {\n"
            + "    'src': 'Images/Sun.png',\n"
            + "    'name': 'sun1',\n"
            + "    'hOffset': 250,\n"
            + "    'vOffset': 250,\n"
            + "    'alignment': 'center'\n"
            + "  },\n"
            + "  'text': {\n"
            + "    'data': 'Click Here',\n"
            + "    'size': 36,\n"
            + "    'style': 'bold',\n"
            + "    'name': 'text1',\n"
            + "    'hOffset': 250,\n"
            + "    'vOffset': 100,\n"
            + "    'alignment': 'center',\n"
            + "    'onMouseUp': 'sun1.opacity = (sun1.opacity / 100) * 90;'\n"
            + "  }\n"
            + "}}").replace('\'', '"');

        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("widget", Widget.class);
        xs.alias("window", Window.class);
        xs.alias("image", Image.class);
        xs.alias("text", Text.class);

        Widget widget = new Widget();

        assertEquals(expected, xs.toXML(widget));

    }

    public static class Widget {
        String debug = "on";
        Window window = new Window();
        Image image = new Image();
        Text text = new Text();
    }

    public static class Window {
        String title = "Sample Konfabulator Widget";
        String name = "main_window";
        int width = 500;
        int height = 500;
    }

    public static class Image {
        String src = "Images/Sun.png";
        String name = "sun1";
        int hOffset = 250;
        int vOffset = 250;
        String alignment = "center";
    }

    public static class Text {
        String data = "Click Here";
        int size = 36;
        String style = "bold";
        String name = "text1";
        int hOffset = 250;
        int vOffset = 100;
        String alignment = "center";
        String onMouseUp = "sun1.opacity = (sun1.opacity / 100) * 90;";
    }

    public void testColor() {
        Color color = Color.black;
        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        String expected = ("{'awt-color': {\n"
            + "  'red': 0,\n"
            + "  'green': 0,\n"
            + "  'blue': 0,\n"
            + "  'alpha': 255\n"
            + "}}").replace('\'', '"');
        assertEquals(expected, xs.toXML(color));
    }

    public void testDoesHandleQuotesAndEscapes() {
        String[] strings = new String[]{
            "last\"", "\"first", "\"between\"", "around \"\" it", "back\\slash",};
        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        String expected = (""
            + "{#string-array#: [\n"
            + "  #last\\\"#,\n"
            + "  #\\\"first#,\n"
            + "  #\\\"between\\\"#,\n"
            + "  #around \\\"\\\" it#,\n"
            + "  #back\\\\slash#\n"
            + "]}").replace('#', '"');
        assertEquals(expected, xs.toXML(strings));
    }

    public void testCanMarshalSimpleTypesWithNullMembers() {
        Msg message = new Msg("hello");
        Msg message2 = new Msg(null);
        message.innerMessage = message2;

        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("innerMessage", Msg.class);

        String expected = (""
            + "{'innerMessage': {\n"
            + "  'greeting': 'hello',\n"
            + "  'innerMessage': {\n"
            + "  }\n"
            + "}}").replace('\'', '"');
        assertEquals(expected, xs.toXML(message));
    }

    public static class Msg {
        String greeting;
        Msg innerMessage;

        public Msg(String greeting) {
            this.greeting = greeting;
        }
    }

    public void testCanMarshalElementWithEmptyArray() {
        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("element", ElementWithEmptyArray.class);

        String expected = ("" + "{'element': {\n" + "  'array': [\n" + "  ]\n" + "}}").replace(
            '\'', '"');
        assertEquals(expected, xs.toXML(new ElementWithEmptyArray()));
    }

    public static class ElementWithEmptyArray {
        String[] array = new String[0];
    }

    public void testCanMarshalJavaMap() {
        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        String expected = (""
            + "{'map': {\n"
            + "  'entry': {\n"
            + "    'one',\n"
            + "    1\n"
            + "  }\n"
            + "}}").replace('\'', '"');

        final Map map = new HashMap();
        map.put("one", new Integer(1));
        assertEquals(expected, xs.toXML(map));
    }

    final static class MapHolder {
        private Map map = new HashMap();
    }

    public void testCanMarshalNestedMap() {
        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("holder", MapHolder.class);
        String expected = (""
            + "{'holder': {\n"
            + "  'map': {\n"
            + "    'entry': {\n"
            + "      'one',\n"
            + "      1\n"
            + "    }\n"
            + "  }\n"
            + "}}").replace('\'', '"');

        final MapHolder holder = new MapHolder();
        holder.map.put("one", new Integer(1));
        assertEquals(expected, xs.toXML(holder));
    }

    static class CollectionKeeper {
        Collection coll = new ArrayList();
    }

    public void testIgnoresAttributeForCollectionMember() {
        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("keeper", CollectionKeeper.class);
        String expected = (""
            + "{'keeper': {\n"
            + "  'coll': [\n"
            + "    'one',\n"
            + "    'two'\n"
            + "  ]\n"
            + "}}").replace('\'', '"');

        final CollectionKeeper holder = new CollectionKeeper();
        holder.coll.add("one");
        holder.coll.add("two");
        assertEquals(expected, xs.toXML(holder));
    }
    
    // Writing attributes, the writer has no clue about their original type.
    public void testDoesWriteAttributesAsStringValues() {
        XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.alias("window", Window.class);
        xs.useAttributeFor("width", int.class);
        xs.useAttributeFor("height", int.class);
        String expected = (""
            + "{'window': {\n"
            + "  '@width': '500',\n"
            + "  '@height': '500',\n"
            + "  'title': 'JUnit'\n"
            + "}}").replace('\'', '"');

        final Window window = new Window();
        window.title = "JUnit";
        window.name = null;
        assertEquals(expected, xs.toXML(window));
    }
}
