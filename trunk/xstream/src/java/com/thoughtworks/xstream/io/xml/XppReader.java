package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.core.util.IntQueue;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.StreamException;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class XppReader implements HierarchicalStreamReader {

    private final XmlPullParser parser;
    private final FastStack elementStack = new FastStack(16);
    private final IntQueue lookaheadQueue = new IntQueue(4);

    public XppReader(Reader reader) {
        try {
            parser = createParser();
            parser.setInput(new BufferedReader(reader));
            moveDown();
        } catch (XmlPullParserException e) {
            throw new StreamException(e);
        }
    }

    protected XmlPullParser createParser() {
        // WARNING, read comment in getValue() before switching
        // to a different parser.
        return new MXParser();
    }

    public boolean hasMoreChildren() {
        while (true) {
            switch (lookahead()) {
                case XmlPullParser.START_TAG:
                    return true;
                case XmlPullParser.END_TAG:
                case XmlPullParser.END_DOCUMENT:
                    return false;
                default:
                    continue;
            }
        }
    }

    private int lookahead() {
        try {
            int event = parser.next();
            lookaheadQueue.write(event);
            return event;
        } catch (XmlPullParserException e) {
            throw new StreamException(e);
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    private int next() {
        if (!lookaheadQueue.isEmpty()) {
            return lookaheadQueue.read();
        } else {
            try {
                return parser.next();
            } catch (XmlPullParserException e) {
                throw new StreamException(e);
            } catch (IOException e) {
                throw new StreamException(e);
            }
        }
    }

    public void moveDown() {
        int currentDepth = elementStack.size();
        while (elementStack.size() <= currentDepth) {
            read();
            if (elementStack.size() < currentDepth) {
                throw new RuntimeException(); // sanity check
            }
        }
    }

    public void moveUp() {
        int currentDepth = elementStack.size();
        while (elementStack.size() >= currentDepth) {
            read();
        }
    }

    public String getNodeName() {
        return (String)elementStack.peek();
    }

    public String getValue() {
        // MXP1 (pull parser) collapses all text into a single
        // text event. This allows us to only need to lookahead
        // one step. However if using a different pull parser
        // impl, you may need to look ahead further.
        if (lookahead() == XmlPullParser.TEXT) {
            String text = parser.getText();
            return text == null ? "" : text;
        } else {
            return "";
        }
    }

    public String getAttribute(String name) {
        return parser.getAttributeValue(null, name);
    }

    public Object peekUnderlyingNode() {
        throw new UnsupportedOperationException();
    }

    private void read() {
        switch (next()) {
            case XmlPullParser.START_TAG:
                elementStack.push(parser.getName());
                break;
            case XmlPullParser.END_TAG:
            case XmlPullParser.END_DOCUMENT:
                elementStack.pop();
                break;
        }
    }

    public void appendErrors(ErrorWriter errorWriter) {
        errorWriter.add("line number", String.valueOf(parser.getLineNumber()));
    }
}