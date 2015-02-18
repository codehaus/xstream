/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009, 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 07. March 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.naming.NameCoder;

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;


public class Dom4JXmlWriter extends AbstractXmlWriter {

    private final XMLWriter writer;
    private final FastStack elementStack;
    private AttributesImpl attributes;
    private boolean started;
    private boolean children;

    public Dom4JXmlWriter(XMLWriter writer) {
        this(writer, new XmlFriendlyNameCoder());
    }

    /**
     * @since 1.4
     */
    public Dom4JXmlWriter(XMLWriter writer, NameCoder nameCoder) {
        super(nameCoder);
        this.writer = writer;
        this.elementStack = new FastStack(16);
        this.attributes = new AttributesImpl();
        try {
            writer.startDocument();
        } catch (SAXException e) {
            throw new StreamException(e);
        }
    }

    /**
     * @since 1.2
     * @deprecated As of 1.4 use {@link Dom4JXmlWriter#Dom4JXmlWriter(XMLWriter, NameCoder)} instead.
     */
    public Dom4JXmlWriter(XMLWriter writer, XmlFriendlyReplacer replacer) {
        this(writer, (NameCoder)replacer);
    }

    public void startNode(String name) {
        if (elementStack.size() > 0) {
            try {
                startElement();
            } catch (SAXException e) {
                throw new StreamException(e);
            }
            started = false;
        }
        elementStack.push(encodeNode(name));
        children = false;
    }

    public void setValue(String text) {
        char[] value = text.toCharArray();
        if (value.length > 0) {
            try {
                startElement();
                writer.characters(value, 0, value.length);
            } catch (SAXException e) {
                throw new StreamException(e);
            }
            children = true;
        }
    }

    public void addAttribute(String key, String value) {
        attributes.addAttribute("", "", encodeAttribute(key), "string", value);
    }

    public void endNode() {
        try {
            if (!children) {
                Element element = new DefaultElement((String)elementStack.pop());
                for (int i = 0; i < attributes.getLength(); ++i) {
                    element.addAttribute(attributes.getQName(i), attributes.getValue(i));
                }
                writer.write(element);
                attributes.clear();
                children = true;   // node just closed is child of node on top of stack
                started = true;
            } else {
                startElement();
                writer.endElement("", "", (String)elementStack.pop());
            }
        } catch (SAXException e) {
            throw new StreamException(e);
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    public void close() {
        try {
            writer.endDocument();
        } catch (SAXException e) {
            throw new StreamException(e);
        }
    }

    private void startElement() throws SAXException {
        if (!started) {
            writer.startElement("", "", (String)elementStack.peek(), attributes);
            attributes.clear();
            started = true;
        }
    }
}
