package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.core.util.IntQueue;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.AttributeNameIterator;

import java.util.Iterator;

/**
 * Base class that contains common functionality across HierarchicalStreamReader implementations
 * that need to read from a pull parser.
 *
 * @author Joe Walnes
 * @author James Strachan
 */
public abstract class AbstractPullReader implements HierarchicalStreamReader {

    protected static final int START_NODE = 1;
    protected static final int END_NODE = 2;
    protected static final int TEXT = 3;
    protected static final int COMMENT = 4;
    protected static final int OTHER = 0;

    private final FastStack elementStack = new FastStack(16);
    private final IntQueue lookaheadQueue = new IntQueue(4);

    private boolean hasMoreChildrenCached;
    private boolean hasMoreChildrenResult;

    /**
     * Pull the next event from the stream.
     *
     * <p>This MUST return {@link #START_NODE}, {@link #END_NODE}, {@link #TEXT}, {@link #COMMENT},
     * {@link #OTHER} or throw {@link com.thoughtworks.xstream.io.StreamException}.</p>
     *
     * <p>The underlying pull parser will most likely return its own event types. These must be
     * mapped to the appropriate events.</p>
     */
    protected abstract int pullNextEvent();

    /**
     * Pull the name of the current element from the stream.
     */
    protected abstract String pullElementName();

    /**
     * Pull the contents of the current text node from the stream.
     */
    protected abstract String pullText();

    public boolean hasMoreChildren() {
        if (hasMoreChildrenCached) {
            return hasMoreChildrenResult;
        }
        while (true) {
            switch (lookahead()) {
                case START_NODE:
                    hasMoreChildrenCached = true;
                    hasMoreChildrenResult = true;
                    return true;
                case END_NODE:
                    hasMoreChildrenCached = true;
                    hasMoreChildrenResult = false;
                    return false;
                default:
                    continue;
            }
        }
    }

    public void moveDown() {
        hasMoreChildrenCached = false;
        int currentDepth = elementStack.size();
        while (elementStack.size() <= currentDepth) {
            read();
            if (elementStack.size() < currentDepth) {
                throw new RuntimeException(); // sanity check
            }
        }
    }

    public void moveUp() {
        hasMoreChildrenCached = false;
        int currentDepth = elementStack.size();
        while (elementStack.size() >= currentDepth) {
            read();
        }
    }

    private void read() {
        switch (next()) {
            case START_NODE:
                elementStack.push(pullElementName());
                break;
            case END_NODE:
                elementStack.pop();
                break;
        }
    }

    private int lookahead() {
        int event = pullNextEvent();
        lookaheadQueue.write(event);
        return event;
    }

    private int next() {
        if (!lookaheadQueue.isEmpty()) {
            return lookaheadQueue.read();
        } else {
            return pullNextEvent();
        }
    }

    private void mark() {
        throw new UnsupportedOperationException();
    }

    private void reset() {
        throw new UnsupportedOperationException();
    }

    public String getValue() {
        // we should collapse together any text which
        // contains comments

        // lets only use a string buffer when we get 2 strings
        // to avoid copying strings
        String last = null;
        StringBuffer buffer = null;

        mark();
        int value = lookahead();
        while (true) {
            if (value == TEXT) {
                String text = pullText();
                if (text != null && text.length() > 0) {
                    if (last == null) {
                        last = text;
                    } else {
                        if (buffer == null) {
                            buffer = new StringBuffer(last);
                        }
                        buffer.append(text);
                    }
                }
            } else if (value == END_NODE) {
                // if we lookahead and see the end of an element, we should remember there's no more children,
                // as the hasMoreChildren() call will skip what we've just read.
                hasMoreChildrenCached = true;
                hasMoreChildrenResult = false;
                break;
            } else if (value != COMMENT) {
                break;
            }
            value = lookahead();
        }
        reset();
        if (buffer != null) {
            return buffer.toString();
        } else {
            return (last == null) ? "" : last;
        }
    }

    public Iterator getAttributeNames() {
        return new AttributeNameIterator(this);
    }

    public String getNodeName() {
        return (String) elementStack.peek();
    }

    public Object peekUnderlyingNode() {
        throw new UnsupportedOperationException();
    }

    public HierarchicalStreamReader underlyingReader() {
        return this;
    }

}
