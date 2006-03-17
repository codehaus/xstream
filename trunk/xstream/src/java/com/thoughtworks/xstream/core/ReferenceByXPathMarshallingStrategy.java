package com.thoughtworks.xstream.core;

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ReferenceByXPathMarshallingStrategy implements MarshallingStrategy {

    public static int RELATIVE = 0;
    public static int ABSOLUTE = 1;
    private final int mode;

    /**
     * @deprecated As of 1.2, use {@link #ReferenceByXPathMarshallingStrategy(int)}
     */
    public ReferenceByXPathMarshallingStrategy() {
        this(RELATIVE);
    }

    public ReferenceByXPathMarshallingStrategy(int mode) {
        this.mode = mode;
    }

    public Object unmarshal(Object root, HierarchicalStreamReader reader, DataHolder dataHolder, ConverterLookup converterLookup, Mapper mapper) {
        return new ReferenceByXPathUnmarshaller(root, reader, converterLookup, mapper, mode)
            .start(dataHolder);
    }

    public void marshal(HierarchicalStreamWriter writer, Object obj, ConverterLookup converterLookup, Mapper mapper, DataHolder dataHolder) {
        new ReferenceByXPathMarshaller(writer, converterLookup, mapper, mode)
            .start(obj, dataHolder);
    }

    /**
     * @deprecated As of 1.2, use {@link #unmarshal(Object, HierarchicalStreamReader, DataHolder, DefaultConverterLookup, Mapper)}
     */
    public Object unmarshal(Object root, HierarchicalStreamReader reader, DataHolder dataHolder, DefaultConverterLookup converterLookup, ClassMapper classMapper) {
        return new ReferenceByXPathUnmarshaller(root, reader, converterLookup,
                classMapper).start(dataHolder);
    }

    /**
     * @deprecated As of 1.2, use {@link #marshal(HierarchicalStreamWriter, Object, ConverterLookup, Mapper, DataHolder)}
     */
    public void marshal(HierarchicalStreamWriter writer, Object obj, DefaultConverterLookup converterLookup, ClassMapper classMapper, DataHolder dataHolder) {
        new ReferenceByXPathMarshaller(writer, converterLookup, classMapper).start(obj, dataHolder);
    }
}
