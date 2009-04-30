/*
 * Copyright (C) 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 30. April 2009 by Joerg Schaible
 */
package com.thoughtworks.xstream.tools.benchmark.products;

import com.thoughtworks.xstream.io.xml.BEAStaxDriver;

/**
 * Uses XStream with the BEA StAX driver for parsing XML.
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @see com.thoughtworks.xstream.tools.benchmark.Harness
 * @see com.thoughtworks.xstream.tools.benchmark.Product
 * @see com.thoughtworks.xstream.XStream
 * @see BEAStaxDriver
 */
public class XStreamBEAStax extends XStreamDriver {

    public XStreamBEAStax() {
        super(new BEAStaxDriver(), "XML with BEA StAX parser");
    }
}
