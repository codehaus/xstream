package com.thoughtworks.xstream.builder;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.XStream;

/**
 * @author Guilherme Silveira
 * @since upcoming
 */
public class ConverterRegistrationNode implements XStreamBuilderConfigNode {
    
    private final Converter converter;

    public ConverterRegistrationNode(Converter converter) {
        this.converter = converter;
    }


    public void process(XStream instance) {
        instance.registerConverter(converter);
    }
}
