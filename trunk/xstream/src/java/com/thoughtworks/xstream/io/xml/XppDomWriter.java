package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;

import java.util.LinkedList;

public class XppDomWriter implements HierarchicalStreamWriter {
    private LinkedList elementStack = new LinkedList();

    private Xpp3Dom configuration;

    public XppDomWriter() {
    }

    public Xpp3Dom getConfiguration() {
        return configuration;
    }

    public void startNode(String name) {
        Xpp3Dom configuration = new Xpp3Dom(name);

        if (this.configuration == null) {
            this.configuration = configuration;
        } else {
            top().addChild(configuration);
        }

        elementStack.addLast(configuration);
    }

    public void setValue(String text) {
        top().setValue(text);
    }

    public void addAttribute(String key, String value) {
        top().setAttribute(key, value);
    }

    public void endNode() {
        elementStack.removeLast();
    }

    private Xpp3Dom top() {
        return (Xpp3Dom) elementStack.getLast();
    }

    public void flush() {
        // don't need to do anything
    }

    public void close() {
        // don't need to do anything
    }

    public HierarchicalStreamWriter underlyingWriter() {
        return this;
    }
    
}
