package com.thoughtworks.xstream.benchmark.xmlfriendly.product;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.tools.benchmark.Product;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Uses a combined lookup and replaces characters.
 *
 * @author J&ouml;rg Schaible
 */
public class IterativeReplacer implements Product {

    private final XStream xstream;

    public IterativeReplacer() {
        this.xstream = new XStream(new XppDriver(new XmlFriendlyReplacer()));
    }

    public void serialize(Object object, OutputStream output) throws Exception {
        xstream.toXML(object, output);
    }

    public Object deserialize(InputStream input) throws Exception {
        return xstream.fromXML(input);
    }

    public String toString() {
        return "Iterative Replacer";
    }
    
    public static class XmlFriendlyReplacer extends AbstractXmlFriendlyReplacer {

        public XmlFriendlyReplacer() {
            super("_-", "__", 0);
        }

        public XmlFriendlyReplacer(String dollarReplacement, String underscoreReplacement, int bufferIncrement) {
            super(dollarReplacement, underscoreReplacement, bufferIncrement);
        }
        
        public String escapeName(String name) {
            return super.escapeIterativelyReplacing(name);
        }
        
        public String unescapeName(String name) {
            return super.unescapeIterativelyReplacing(name);
        }
    }
}
