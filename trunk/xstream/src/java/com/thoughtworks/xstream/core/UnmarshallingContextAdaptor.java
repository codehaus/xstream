package com.thoughtworks.xstream.core;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.ClassStack;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.alias.ClassMapper;

public class UnmarshallingContextAdaptor implements UnmarshallingContext {

    private Object root;
    private HierarchicalStreamReader reader;
    private ConverterLookup converterLookup;
    private ClassMapper classMapper;
    private String classAttributeIdentifier;
    private ClassStack types = new ClassStack(16);

    public UnmarshallingContextAdaptor(Object root, HierarchicalStreamReader reader,
                                       ConverterLookup converterLookup, ClassMapper classMapper,
                                       String classAttributeIdentifier) {
        this.root = root;
        this.reader = reader;
        this.converterLookup = converterLookup;
        this.classMapper = classMapper;
        this.classAttributeIdentifier = classAttributeIdentifier;
    }

    public Object convertAnother(Class type) {
        Converter converter = converterLookup.lookupConverterForType(type);
        types.push(type);
        Object result = converter.unmarshal(reader, this);
        types.popSilently();
        return result;
    }

    public Object currentObject() {
        return root;
    }

    public Class getRequiredType() {
        return types.peek();
    }

    public Object start() {
        String classAttribute = reader.getAttribute(classAttributeIdentifier);
        Class type;
        if (classAttribute == null) {
            type = classMapper.lookupType(reader.getNodeName());
        } else {
            type = classMapper.lookupType(classAttribute);
        }
        return convertAnother(type);
    }

}
