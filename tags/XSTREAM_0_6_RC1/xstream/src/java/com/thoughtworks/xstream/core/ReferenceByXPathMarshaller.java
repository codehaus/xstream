package com.thoughtworks.xstream.core;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.basic.AbstractBasicConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.path.PathTracker;
import com.thoughtworks.xstream.io.path.PathTrackingWriter;
import com.thoughtworks.xstream.io.path.RelativePathCalculator;

import java.util.IdentityHashMap;
import java.util.Map;

public class ReferenceByXPathMarshaller extends TreeMarshaller {

    private PathTracker pathTracker = new PathTracker();
    private Map references = new IdentityHashMap();
    private RelativePathCalculator relativePathCalculator = new RelativePathCalculator();

    public ReferenceByXPathMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, ClassMapper classMapper) {
        super(writer, converterLookup, classMapper);
        this.writer = new PathTrackingWriter(writer, pathTracker);
    }

    public void convertAnother(Object item) {
        Converter converter = converterLookup.lookupConverterForType(item.getClass());

        if (isImmutableBasicType(converter)) {
            // strings, ints, dates, etc... don't bother using references.
            converter.marshal(item, writer, this);
        } else {
            String currentPath = pathTracker.getCurrentPath();
            String pathOfExistingReference = (String) references.get(item);
            if (pathOfExistingReference != null) {
                String absolutePath = relativePathCalculator.relativePath(currentPath, pathOfExistingReference);
                writer.addAttribute("reference", absolutePath);
            } else {
                references.put(item, currentPath);
                converter.marshal(item, writer, this);
            }
        }
    }

    private boolean isImmutableBasicType(Converter converter) {
        return converter instanceof AbstractBasicConverter;
    }
}