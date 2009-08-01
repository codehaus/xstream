/*
 * Copyright (C) 2006, 2007, 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 30. July 2009 by Joerg Schaible as copy of AbstractXpathDuplicateRefeenceTest.
 */
package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

public class AbsoluteSingleNodeXPathDuplicateReferenceTest extends AbstractDuplicateReferenceTest {

    // tests inherited from superclass

    protected void setUp() throws Exception {
        super.setUp();
        xstream.setMode(XStream.SINGLE_NODE_XPATH_ABSOLUTE_REFERENCES);
    }

    public void testXmlContainsReferencePaths() {

        Thing sameThing = new Thing("hello");
        Thing anotherThing = new Thing("hello");

        List list = new ArrayList();
        list.add(sameThing);
        list.add(sameThing);
        list.add(anotherThing);

        String expected = "" +
                "<list>\n" +
                "  <thing>\n" +
                "    <field>hello</field>\n" +
                "  </thing>\n" +
                "  <thing reference=\"/list[1]/thing[1]\"/>\n" +
                "  <thing>\n" +
                "    <field>hello</field>\n" +
                "  </thing>\n" +
                "</list>";

        assertEquals(expected, xstream.toXML(list));
    }

}
