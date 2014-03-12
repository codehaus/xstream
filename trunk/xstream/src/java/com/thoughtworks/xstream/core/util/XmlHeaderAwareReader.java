/*
 * Copyright (C) 2007, 2008, 2010, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 13. September 2007 by Joerg Schaible.
 */

package com.thoughtworks.xstream.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * A {@link Reader} that evaluates the XML header. It selects its encoding based on the encoding read with the XML
 * header of the provided {@link InputStream}. The default encoding is <em>UTF-8</em> and the version is 1.0 if the
 * stream does not contain an XML header or the attributes are not set within the header.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.3
 */
public final class XmlHeaderAwareReader extends Reader {

    private final InputStreamReader reader;
    private final double version;

    private static final String KEY_ENCODING = "encoding";
    private static final String KEY_VERSION = "version";

    private static final String XML_TOKEN = "?xml";

    private static final int STATE_BOM = 0;
    private static final int STATE_START = 1;
    private static final int STATE_AWAIT_XML_HEADER = 2;
    private static final int STATE_ATTR_NAME = 3;
    private static final int STATE_ATTR_VALUE = 4;

    /**
     * Constructs an XmlHeaderAwareReader.
     * 
     * @param in the {@link InputStream}
     * @throws UnsupportedEncodingException if the encoding is not supported
     * @throws IOException occurred while reading the XML header
     * @since 1.3
     */
    public XmlHeaderAwareReader(final InputStream in) throws UnsupportedEncodingException, IOException {
        final PushbackInputStream[] pin = new PushbackInputStream[]{in instanceof PushbackInputStream
            ? (PushbackInputStream)in
            : new PushbackInputStream(in, 64)};
        final Map<String, String> header = getHeader(pin);
        version = Double.parseDouble(header.get(KEY_VERSION));
        reader = new InputStreamReader(pin[0], header.get(KEY_ENCODING));
    }

    private Map<String, String> getHeader(final PushbackInputStream[] in) throws IOException {
        final Map<String, String> header = new HashMap<String, String>();
        header.put(KEY_ENCODING, "utf-8");
        header.put(KEY_VERSION, "1.0");

        int state = STATE_BOM;
        final ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        int i = 0;
        char ch = 0;
        char valueEnd = 0;
        final StringBuffer name = new StringBuffer();
        final StringBuffer value = new StringBuffer();
        boolean escape = false;
        while (i != -1 && (i = in[0].read()) != -1) {
            out.write(i);
            ch = (char)i;
            switch (state) {
            case STATE_BOM:
                if (ch == 0xEF && out.size() == 1 || ch == 0xBB && out.size() == 2 || ch == 0xBF && out.size() == 3) {
                    if (ch == 0xBF) {
                        out.reset();
                        state = STATE_START;
                    }
                    break;
                } else if (out.size() > 1) {
                    i = -1;
                    break;
                } else {
                    state = STATE_START;
                }
                //$FALL-THROUGH$
            case STATE_START:
                if (!Character.isWhitespace(ch)) {
                    if (ch == '<') {
                        state = STATE_AWAIT_XML_HEADER;
                    } else {
                        i = -1;
                    }
                }
                break;
            case STATE_AWAIT_XML_HEADER:
                if (!Character.isWhitespace(ch)) {
                    name.append(Character.toLowerCase(ch));
                    if (!XML_TOKEN.startsWith(name.substring(0))) {
                        i = -1;
                    }
                } else {
                    if (name.toString().equals(XML_TOKEN)) {
                        state = STATE_ATTR_NAME;
                        name.setLength(0);
                    } else {
                        i = -1;
                    }
                }
                break;
            case STATE_ATTR_NAME:
                if (!Character.isWhitespace(ch)) {
                    if (ch == '=') {
                        state = STATE_ATTR_VALUE;
                    } else {
                        ch = Character.toLowerCase(ch);
                        if (Character.isLetter(ch)) {
                            name.append(ch);
                        } else {
                            i = -1;
                        }
                    }
                } else if (name.length() > 0) {
                    i = -1;
                }
                break;
            case STATE_ATTR_VALUE:
                if (valueEnd == 0) {
                    if (ch == '"' || ch == '\'') {
                        valueEnd = ch;
                    } else {
                        i = -1;
                    }
                } else {
                    if (ch == '\\' && !escape) {
                        escape = true;
                        break;
                    }
                    if (ch == valueEnd && !escape) {
                        valueEnd = 0;
                        state = STATE_ATTR_NAME;
                        header.put(name.toString(), value.toString());
                        name.setLength(0);
                        value.setLength(0);
                    } else {
                        escape = false;
                        if (ch != '\n') {
                            value.append(ch);
                        } else {
                            i = -1;
                        }
                    }
                }
                break;
            }
        }

        final byte[] pushbackData = out.toByteArray();
        for (i = pushbackData.length; i-- > 0;) {
            final byte b = pushbackData[i];
            try {
                in[0].unread(b);
            } catch (final IOException ex) {
                in[0] = new PushbackInputStream(in[0], ++i);
            }
        }
        return header;
    }

    /**
     * @see InputStreamReader#getEncoding()
     * @since 1.3
     */
    public String getEncoding() {
        return reader.getEncoding();
    }

    /**
     * @return the XML version
     * @since 1.3
     */
    public double getVersion() {
        return version;
    }

    @Override
    public void mark(final int readAheadLimit) throws IOException {
        reader.mark(readAheadLimit);
    }

    @Override
    public boolean markSupported() {
        return reader.markSupported();
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public int read(final char[] cbuf, final int offset, final int length) throws IOException {
        return reader.read(cbuf, offset, length);
    }

    @Override
    public int read(final char[] cbuf) throws IOException {
        return reader.read(cbuf);
    }

    @Override
    public int read(final CharBuffer target) throws IOException {
        return reader.read(target);
    }

    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
    }

    @Override
    public long skip(final long n) throws IOException {
        return reader.skip(n);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public boolean equals(final Object obj) {
        return reader.equals(obj);
    }

    @Override
    public int hashCode() {
        return reader.hashCode();
    }

    @Override
    public String toString() {
        return reader.toString();
    }
}
