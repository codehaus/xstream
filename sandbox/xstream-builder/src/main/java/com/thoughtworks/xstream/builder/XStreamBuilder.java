package com.thoughtworks.xstream.builder;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.ReadOnlyXStream;
import com.thoughtworks.xstream.converters.Converter;

import java.util.List;
import java.util.ArrayList;

/**
 * The base xstream builder. This is xstream's new entrypoint. Instantiate a builder, configure it
 * and invoke buildXStream at the end. 
 * @author Guilherme Silveira
 * @since upcoming
 */
public class XStreamBuilder {

    private final List childrenNodes = new ArrayList();
    
    public XStreamClassConfig handle(Class type) {
        XStreamClassConfig classConfig = new XStreamClassConfig(type);
        childrenNodes.add(classConfig);
        return classConfig;
    }

    public ReadOnlyXStream buildXStream() {
        XStream instance = createBasicInstance();
        for(int i=0;i<childrenNodes.size();i++) {
            XStreamBuilderConfigNode node = (XStreamBuilderConfigNode) childrenNodes.get(i);
            node.process(instance);
        }
        return new ReadOnlyXStream(instance);
    }

    /**
     * Extension point to allow lower-level programmers to create their own xstream instance.
     * @return the xstream instance to configure and wrap
     */
    protected XStream createBasicInstance() {
        return new XStream();
    }

    public XStreamBuilder register(Converter converter) {
        childrenNodes.add(new ConverterRegistrationNode(converter));
        return this;
    }
}
