/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 02. September 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.naming.NameCoder;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class XomReader extends AbstractDocumentReader {

    private Element currentElement;

    public XomReader(Element rootElement) {
        super(rootElement);
    }

    public XomReader(Document document) {
        super(document.getRootElement());
    }

    /**
     * @since upcoming
     */
    public XomReader(Element rootElement, NameCoder nameCoder) {
        super(rootElement, nameCoder);
    }

    /**
     * @since upcoming
     */
    public XomReader(Document document, NameCoder nameCoder) {
        super(document.getRootElement(), nameCoder);
    }

    /**
     * @since 1.2
     * @deprecated As of upcoming use {@link XomReader#XomReader(Element, NameCoder)} instead.
     */
    public XomReader(Element rootElement, XmlFriendlyReplacer replacer) {
        this(rootElement, (NameCoder)replacer);
    }

    /**
     * @since 1.2
     * @deprecated As of upcoming use {@link XomReader#XomReader(Element, NameCoder)} instead.
     */
    public XomReader(Document document, XmlFriendlyReplacer replacer) {
       this(document.getRootElement(), (NameCoder)replacer);
    }
    
    public String getNodeName() {
        return decodeNode(currentElement.getLocalName());
    }

    public String getValue() {
        // currentElement.getValue() not used as this includes text of child elements, which we don't want.
        StringBuffer result = new StringBuffer();
        int childCount = currentElement.getChildCount();
        for(int i = 0; i < childCount; i++) {
            Node child = currentElement.getChild(i);
            if (child instanceof Text) {
                Text text = (Text) child;
                result.append(text.getValue());
            }
        }
        return result.toString();
    }

    public String getAttribute(String name) {
        return currentElement.getAttributeValue(encodeAttribute(name));
    }

    public String getAttribute(int index) {
        return currentElement.getAttribute(index).getValue();
    }

    public int getAttributeCount() {
        return currentElement.getAttributeCount();
    }

    public String getAttributeName(int index) {
        return decodeAttribute(currentElement.getAttribute(index).getQualifiedName());
    }

    protected int getChildCount() {
        return currentElement.getChildElements().size();
    }

    protected Object getParent() {
        return currentElement.getParent();
    }

    protected Object getChild(int index) {
        return currentElement.getChildElements().get(index);
    }

    protected void reassignCurrentElement(Object current) {
        currentElement = (Element) current;
    }
}
