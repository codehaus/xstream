/*
 * Copyright (C) 2006, 2007, 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 18. October 2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.naming.NameCoder;

import java.util.ArrayList;
import java.util.List;


/**
 * A generic {@link com.thoughtworks.xstream.io.HierarchicalStreamWriter} for DOM writer
 * implementations. The implementation manages a list of top level DOM nodes. Every time the
 * last node is closed on the node stack, the next started node is added to the list. This list
 * can be retrieved using the {@link DocumentWriter#getTopLevelNodes()} method.
 * 
 * @author Laurent Bihanic
 * @author J&ouml;rg Schaible
 * @since 1.2.1
 */
public abstract class AbstractDocumentWriter extends AbstractXmlWriter implements
    DocumentWriter {

    private final List result = new ArrayList();
    private final FastStack nodeStack = new FastStack(16);

    /**
     * Constructs an AbstractDocumentWriter.
     * 
     * @param container the top level container for the nodes to create (may be
     *            <code>null</code>)
     * @param nameCoder the object that creates XML-friendly names
     * @since upcoming
     */
    public AbstractDocumentWriter(final Object container, final NameCoder nameCoder) {
        super(nameCoder);
        if (container != null) {
            nodeStack.push(container);
            result.add(container);
        }
    }

    /**
     * Constructs an AbstractDocumentWriter.
     * 
     * @param container the top level container for the nodes to create (may be
     *            <code>null</code>)
     * @param replacer the object that creates XML-friendly names
     * @since 1.2.1
     * @deprecated As of upcoming use
     *             {@link AbstractDocumentWriter#AbstractDocumentWriter(Object, NameCoder)}
     *             instead.
     */
    public AbstractDocumentWriter(final Object container, final XmlFriendlyReplacer replacer) {
        this(container, (NameCoder)replacer);
    }

    public final void startNode(final String name) {
        final Object node = createNode(name);
        nodeStack.push(node);
    }

    /**
     * Create a node. The provided node name is not yet XML friendly. If {@link #getCurrent()}
     * returns <code>null</code> the node is a top level node.
     * 
     * @param name the node name
     * @return the new node
     * @since 1.2.1
     */
    protected abstract Object createNode(String name);

    public final void endNode() {
        endNodeInternally();
        final Object node = nodeStack.pop();
        if (nodeStack.size() == 0) {
            result.add(node);
        }
    }

    /**
     * Called when a node ends. Hook for derived implementations.
     * 
     * @since 1.2.1
     */
    public void endNodeInternally() {
    }

    /**
     * @since 1.2.1
     */
    protected final Object getCurrent() {
        return nodeStack.peek();
    }

    public List getTopLevelNodes() {
        return result;
    }

    public void flush() {
        // don't need to do anything
    }

    public void close() {
        // don't need to do anything
    }
}
