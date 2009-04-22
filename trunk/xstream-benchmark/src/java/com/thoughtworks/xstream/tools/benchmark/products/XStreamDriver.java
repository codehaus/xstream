/*
 * Copyright (C) 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 19.02.2009 by Joerg Schaible
 */
package com.thoughtworks.xstream.tools.benchmark.products;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.tools.benchmark.Product;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * Generic XStream product based on an arbitrary driver.
 * 
 * @see XStream
 * @see Product
 * @see HierarchicalStreamDriver
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public class XStreamDriver implements Product {

    private final XStream xstream;
    private final String desc;

    /**
     * Create a XStream product based on a driver.
     * 
     * @param driver the driver to use for serialization/deserialization 
     * @param desc the driver description
     * 
     * @since upcoming
     */
    public XStreamDriver(HierarchicalStreamDriver driver, String desc) {
        this.xstream = new XStream(driver);
        this.desc = desc;
    }

    public void serialize(Object object, OutputStream output) throws Exception {
        xstream.toXML(object, output);
    }

    public Object deserialize(InputStream input) throws Exception {
        return xstream.fromXML(input);
    }

    public String toString() {
        return "XStream (" + desc + ")";
    }

}
