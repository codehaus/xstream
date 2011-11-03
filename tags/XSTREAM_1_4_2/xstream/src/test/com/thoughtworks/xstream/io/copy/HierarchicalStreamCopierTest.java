/*
 * Copyright (C) 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 04. June 2006 by Joe Walnes
 */
package com.thoughtworks.xstream.io.copy;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.AbstractXMLReaderTest;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.io.xml.xppdom.XppFactory;

import org.xmlpull.v1.XmlPullParserException;

import java.io.StringReader;
import java.io.StringWriter;

public class HierarchicalStreamCopierTest extends AbstractXMLReaderTest {

    private HierarchicalStreamCopier copier = new HierarchicalStreamCopier();

    // This test leverages the existing (comprehensive) tests for the XML readers
    // and adds an additional stage of copying in.

    // factory method - overriding base class.
    protected HierarchicalStreamReader createReader(String xml) throws Exception {
        HierarchicalStreamReader sourceReader = 
                new Xpp3Driver().createReader(new StringReader(xml));

        StringWriter buffer = new StringWriter();
        HierarchicalStreamWriter destinationWriter = new CompactWriter(buffer);

        copier.copy(sourceReader, destinationWriter);

        return new XppReader(new StringReader(buffer.toString()), XppFactory.createDefaultParser());
    }

    public void testSkipsValueIfEmpty() throws XmlPullParserException {
        String input = "<root><empty1/><empty2></empty2><not-empty>blah</not-empty></root>";
        String expected = "<root><empty1/><empty2/><not-empty>blah</not-empty></root>";
        HierarchicalStreamReader sourceReader = new XppReader(
            new StringReader(input), XppFactory.createDefaultParser());

        StringWriter buffer = new StringWriter();
        HierarchicalStreamWriter destinationWriter = new CompactWriter(buffer);

        copier.copy(sourceReader, destinationWriter);

        assertEquals(expected, buffer.toString());
    }

}
