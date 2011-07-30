/*
 * Copyright (C) 2006, 2007, 2009, 2010, 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 30. July 2011 by Joerg Schaible by merging AbsolutXPathCircularReferenceTest,
 * AbsolutXPathDuplicateReferenceTest, AbsolutXPathNestedCircularReferenceTest and
 * AbsolutXPathReplacedReferenceTest.
 */
package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;


public class AbsoluteXPathReferenceTest extends AbstractReferenceTest {

    // tests inherited from superclass

    protected void setUp() throws Exception {
        super.setUp();
        xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    }

    public void testXmlContainsReferencePaths() {

        Thing sameThing = new Thing("hello");
        Thing anotherThing = new Thing("hello");

        List list = new ArrayList();
        list.add(sameThing);
        list.add(sameThing);
        list.add(anotherThing);

        String expected = ""
            + "<list>\n"
            + "  <thing>\n"
            + "    <field>hello</field>\n"
            + "  </thing>\n"
            + "  <thing reference=\"/list/thing\"/>\n"
            + "  <thing>\n"
            + "    <field>hello</field>\n"
            + "  </thing>\n"
            + "</list>";

        assertEquals(expected, xstream.toXML(list));
    }

    public void testCircularReferenceXml() {
        Person bob = new Person("bob");
        Person jane = new Person("jane");
        bob.likes = jane;
        jane.likes = bob;

        String expected = ""
            + "<person>\n"
            + "  <firstname>bob</firstname>\n"
            + "  <likes>\n"
            + "    <firstname>jane</firstname>\n"
            + "    <likes reference=\"/person\"/>\n"
            + "  </likes>\n"
            + "</person>";

        assertEquals(expected, xstream.toXML(bob));
    }

    public void testCircularReferenceToSelfXml() {
        Person bob = new Person("bob");
        bob.likes = bob;

        String expected = ""
            + "<person>\n"
            + "  <firstname>bob</firstname>\n"
            + "  <likes reference=\"/person\"/>\n"
            + "</person>";

        assertEquals(expected, xstream.toXML(bob));
    }

    public void testRing() {
        LinkedElement tom = new LinkedElement("Tom");
        LinkedElement dick = new LinkedElement("Dick");
        LinkedElement harry = new LinkedElement("Harry");
        tom.next = dick;
        dick.next = harry;
        harry.next = tom;

        xstream.alias("elem", LinkedElement.class);
        String expected = ""
            + "<elem>\n"
            + "  <name>Tom</name>\n"
            + "  <next>\n"
            + "    <name>Dick</name>\n"
            + "    <next>\n"
            + "      <name>Harry</name>\n"
            + "      <next reference=\"/elem\"/>\n"
            + "    </next>\n"
            + "  </next>\n"
            + "</elem>";

        assertEquals(expected, xstream.toXML(tom));
    }

    public void testTree() {
        TreeElement root = new TreeElement("X");
        TreeElement left = new TreeElement("Y");
        TreeElement right = new TreeElement("Z");
        root.left = left;
        root.right = right;
        left.left = new TreeElement(root.name);
        right.right = new TreeElement(left.name);
        right.left = left.left;

        xstream.alias("elem", TreeElement.class);
        String expected = ""
            + "<elem>\n"
            + "  <name>X</name>\n"
            + "  <left>\n"
            + "    <name>Y</name>\n"
            + "    <left>\n"
            + "      <name reference=\"/elem/name\"/>\n"
            + "    </left>\n"
            + "  </left>\n"
            + "  <right>\n"
            + "    <name>Z</name>\n"
            + "    <left reference=\"/elem/left/left\"/>\n"
            + "    <right>\n"
            + "      <name reference=\"/elem/left/name\"/>\n"
            + "    </right>\n"
            + "  </right>\n"
            + "</elem>";

        assertEquals(expected, xstream.toXML(root));
    }

    public void testReplacedReference() {
        String expectedXml = ""
            + "<element>\n"
            + "  <data>parent</data>\n"
            + "  <children>\n"
            + "    <anonymous-element resolves-to=\"element\">\n"
            + "      <data>child</data>\n"
            + "      <parent reference=\"/element\"/>\n"
            + "      <children/>\n"
            + "    </anonymous-element>\n"
            + "  </children>\n"
            + "</element>";

        replacedReference(expectedXml);
    }

}
