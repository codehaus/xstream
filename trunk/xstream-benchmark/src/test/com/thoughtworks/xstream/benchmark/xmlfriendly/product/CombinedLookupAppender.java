package com.thoughtworks.xstream.benchmark.xmlfriendly.product;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.tools.benchmark.Product;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Uses a combined lookup and appends characters.
 *
 * @author J&ouml;rg Schaible
 */
public class CombinedLookupAppender implements Product {

    private final XStream xstream;

    public CombinedLookupAppender() {
        this.xstream = new XStream(new XppDriver(new XmlFriendlyReplacer()));
    }

    public void serialize(Object object, OutputStream output) throws Exception {
        xstream.toXML(object, output);
    }

    public Object deserialize(InputStream input) throws Exception {
        return xstream.fromXML(input);
    }

    public String toString() {
        return "Compined Lookup Appending";
    }
    
    public static class XmlFriendlyReplacer extends AbstractXmlFriendlyReplacer {

        public XmlFriendlyReplacer() {
            super("_-", "__", 0);
        }

        public XmlFriendlyReplacer(String dollarReplacement, String underscoreReplacement, int bufferIncrement) {
            super(dollarReplacement, underscoreReplacement, bufferIncrement);
        }
        
        public String escapeName(String name) {
            return super.escapeByCombinedLookupAppending(name);
        }
        
        public String unescapeName(String name) {
            return super.unescapeByCombinedLookupAppending(name);
        }
    }
}
