package com.thoughtworks.xstream.builder;

import com.thoughtworks.xstream.XStream;

/**
 * @author Guilherme Silveira
 */
public class DefaultImplementationStrategy implements XStreamBuilderConfigNode {
    private final Class type;
    private final Class defaultImplementation;

    public DefaultImplementationStrategy(Class type, Class defaultImplementation) {
        this.type = type;
        this.defaultImplementation = defaultImplementation;
    }

    public void process(XStream instance) {
        instance.addDefaultImplementation(this.defaultImplementation, this.type);
    }
}
