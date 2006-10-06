package com.thoughtworks.xstream.io.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class Dom4JWriter extends AbstractXmlWriter {

    private final DocumentFactory documentFactory;
    private final FastStack elementStack = new FastStack(16);

    public Dom4JWriter(final DocumentFactory documentFactory, final Branch root, XmlFriendlyReplacer replacer) {
        super(replacer);
        this.documentFactory = documentFactory;
        elementStack.push(root);
    }

    public Dom4JWriter(final DocumentFactory documentFactory, XmlFriendlyReplacer replacer) {
        this(documentFactory, documentFactory.createDocument(), replacer);
    }

    public Dom4JWriter(final DocumentFactory documentFactory) {
        this(documentFactory, documentFactory.createDocument(), new XmlFriendlyReplacer());
    }

    public void startNode(String name) {
        Element element = documentFactory.createElement(escapeXmlName(name));
        top().add(element);
        elementStack.push(element);
    }

    public void setValue(String text) {
        top().setText(text);
    }

    public void addAttribute(String key, String value) {
        ((Element) top()).addAttribute(escapeXmlName(key), value);
    }

    public void endNode() {
        elementStack.popSilently();
    }

    private Branch top() {
        return (Branch) elementStack.peek();
    }

    public void flush() {
    }

    public void close() {
    }
    
    public List getResult() {
        final List list = new ArrayList();
        list.add(elementStack.get(0));
        return list;
    }

    public HierarchicalStreamWriter underlyingWriter() {
        return this;
    }
}
