package com.thoughtworks.xstream.benchmark.strings.products;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.tools.benchmark.Product;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Uses no cache at all StringConverter.
 *
 * @author J&ouml;rg Schaible
 * @see com.thoughtworks.xstream.tools.benchmark.Harness
 * @see Product
 */
public class StringNonCachingConverter implements Product {

    private final XStream xstream;

    public StringNonCachingConverter() {
        xstream = new XStream(new XppDriver());
        xstream.registerConverter(new StringConverter());
    }

    public void serialize(Object object, OutputStream output) throws Exception {
        xstream.toXML(object, output);
    }

    public Object deserialize(InputStream input) throws Exception {
        return xstream.fromXML(input);
    }

    public String toString() {
        return "StringConverter using no cache at all";
    }

    /**
     * Converts a String to a String ;).
     *
     * @author J&ouml;rg Schaible
     * @see String#intern()
     */
    public static class StringConverter extends AbstractSingleValueConverter {

        public boolean canConvert(Class type) {
            return type.equals(String.class);
        }

        public Object fromString(String str) {
            return str;
        }
    }
}
