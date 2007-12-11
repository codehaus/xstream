/*
 * Copyright (C) 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 13. September 2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.benchmark.xmlfriendly.product;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.tools.benchmark.Product;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Uses XmlFriendlyReplacer dummy.
 *
 * @author J&ouml;rg Schaible
 */
public class NoReplacer implements Product {

    private final XStream xstream;

    public NoReplacer() {
        this.xstream = new XStream(new XppDriver(new XmlFriendlyReplacer()));
    }

    public void serialize(Object object, OutputStream output) throws Exception {
        xstream.toXML(object, output);
    }

    public Object deserialize(InputStream input) throws Exception {
        return xstream.fromXML(input);
    }

    public String toString() {
        return "";
    }
    
    public static class XmlFriendlyReplacer extends AbstractXmlFriendlyReplacer {

        public XmlFriendlyReplacer() {
            super("_-", "__", 0);
        }
        
        public String escapeName(String name) {
            return name;
        }
        
        public String unescapeName(String name) {
            return name;
        }
    }
}
