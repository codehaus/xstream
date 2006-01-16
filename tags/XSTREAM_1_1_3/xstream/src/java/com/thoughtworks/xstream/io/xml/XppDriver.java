package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.io.*;

public class XppDriver implements HierarchicalStreamDriver {

    private static boolean xppLibraryPresent;

    public HierarchicalStreamReader createReader(Reader xml) {
        loadLibrary();
        return new XppReader(xml);
    }

    public HierarchicalStreamReader createReader(InputStream in) {
        return createReader(new InputStreamReader(in));
    }

    private void loadLibrary() {
        if (!xppLibraryPresent) {
            try {
                Class.forName("org.xmlpull.mxp1.MXParser");
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("XPP3 pull parser library not present. Specify another driver." +
                        " For example: new XStream(new DomDriver())");
            }
            xppLibraryPresent = true;
        }
    }

    public HierarchicalStreamWriter createWriter(Writer out) {
        return new PrettyPrintWriter(out);
    }

    public HierarchicalStreamWriter createWriter(OutputStream out) {
        return createWriter(new OutputStreamWriter(out));
    }
}
