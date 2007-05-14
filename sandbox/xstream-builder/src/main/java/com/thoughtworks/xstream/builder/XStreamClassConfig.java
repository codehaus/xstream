package com.thoughtworks.xstream.builder;

import com.thoughtworks.xstream.XStream;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Guilherme Silveira
 * @since upcoming
 */
public class XStreamClassConfig implements XStreamBuilderConfigNode {
    private final Class type;
    private String alias;
    private final List childrenNodes;

    public XStreamClassConfig(Class type) {
        this.type = type;
        this.alias = null;
        this.childrenNodes = new ArrayList();
    }

    public XStreamClassConfig as(String alias) {
        this.alias = alias;
        return this;
    }

    public XStreamFieldConfig handle(String fieldName) {
        XStreamFieldConfig fieldConfig = new XStreamFieldConfig(type, fieldName);
        this.childrenNodes.add(fieldConfig);
        return fieldConfig;
    }

    public XStreamClassConfig ignores(String fieldName) {
        this.childrenNodes.add(new OmitFieldStrategy(type, fieldName));
        return this;
    }

    public void process(XStream instance) {
        if(this.alias!=null) {
            instance.alias(alias, type);
        }
        for(int i=0;i<childrenNodes.size();i++) {
            XStreamBuilderConfigNode node = (XStreamBuilderConfigNode) childrenNodes.get(i);
            node.process(instance);
        }
    }

    public XStreamClassConfig implementedBy(Class defaultImplementation) {
        this.childrenNodes.add(new DefaultImplementationStrategy(type, defaultImplementation));
        return this;
    }

    public XStreamClassConfig annotated() {
        this.childrenNodes.add(new AnnotatedConfigNode(type));
        return this;
    }
}
