package com.thoughtworks.xstream.builder;

import com.thoughtworks.xstream.XStream;

/**
 * @author Guilherme Silveira
 * @since upcoming
 */
public class OmitFieldStrategy implements XStreamBuilderConfigNode {
    private final Class type;
    private final String fieldName;

    public OmitFieldStrategy(Class type, String fieldName) {
        this.type = type;
        this.fieldName = fieldName;
    }

    public void process(XStream instance) {
        instance.omitField(type,fieldName);
    }
}
