package com.thoughtworks.xstream.tools.benchmark.products;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.tools.benchmark.Product;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Uses XStream with the Stax driver for parsing XML.
 *
 * @author Joe Walnes
 * @see com.thoughtworks.xstream.tools.benchmark.Harness
 * @see Product
 * @see XStream
 * @see StaxDriver
 */
public class XStreamStax implements Product {

    private final XStream xstream;

    public XStreamStax() {
        this.xstream = new XStream(new StaxDriver());
    }

    public void serialize(Object object, OutputStream output) throws Exception {
        xstream.toXML(object, output);
    }

    public Object deserialize(InputStream input) throws Exception {
        return xstream.fromXML(input);
    }

    public String toString() {
        return "XStream (XML with Stax parser)";
    }

}
