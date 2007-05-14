package com.thoughtworks.xstream.builder;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;

public class AnnotatedConfigNode implements XStreamBuilderConfigNode {

    private final Class type;

    public AnnotatedConfigNode(Class type) {
        this.type = type;
    }

    public void process(XStream instance) {
        Annotations.configureAliases(instance, new Class[]{this.type});
    }
}
