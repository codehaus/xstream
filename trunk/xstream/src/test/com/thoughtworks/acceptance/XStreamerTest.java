/*
 * Copyright (C) 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 01. April 2006 by Joerg Schaible
 */
package com.thoughtworks.acceptance;

import com.thoughtworks.acceptance.objects.OpenSourceSoftware;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamer;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.security.TypePermission;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.ObjectStreamException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;


/**
 * @author J&ouml;rg Schaible
 */
public class XStreamerTest extends AbstractAcceptanceTest {

    private Transformer transformer; 
    
    protected void setUp() throws Exception {
        super.setUp();
        
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final URL url = getClass().getResource("XStreamer.xsl");
        transformer = transformerFactory.newTransformer(new StreamSource(url.openStream()));
    }

    final static class ImplicitXStreamContainer {
        private XStream myXStream;
    }

    public void testDetectsSelfMarshalling() {
        ImplicitXStreamContainer c = new ImplicitXStreamContainer();
        c.myXStream = xstream;
        try {
            xstream.toXML(c);
            fail("Thrown " + ConversionException.class.getName() + " expected");
        } catch (final ConversionException e) {
            assertTrue(e.getMessage().contains("XStream instance"));
        }
    }
    
    public void testCanConvertAnotherInstance() throws TransformerException { 
        XStream x = createXStream();
        final String xml = normalizedXStreamXML(xstream.toXML(x));
        for(final TypePermission permission : XStreamer.getDefaultPermissions())
            xstream.addPermission(permission);
        final XStream serialized = (XStream)xstream.fromXML(xml);
        final String xmlSerialized = normalizedXStreamXML(xstream.toXML(serialized));
        assertEquals(xml, xmlSerialized);
    }
    
    public void testCanBeUsedAfterSerialization() throws TransformerException {
        final String xml = xstream.toXML(createXStream());
        for(final TypePermission permission : XStreamer.getDefaultPermissions())
            xstream.addPermission(permission);
        xstream = (XStream)xstream.fromXML(xml);
        testCanConvertAnotherInstance();
    }
    
    public void testCanSerializeSelfContained() throws ClassNotFoundException, ObjectStreamException {
        final OpenSourceSoftware oos = new OpenSourceSoftware("Walnes", "XStream", "BSD");
        xstream.alias("software", OpenSourceSoftware.class);
        String xml = new XStreamer().toXML(xstream, oos);
        assertEquals(oos, new XStreamer().fromXML(xml));
    }
    
    private String normalizedXStreamXML(String xml) throws TransformerException {
        final StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));
        return writer.toString();
    }
}
