package com.thoughtworks.xstream.core;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.util.ObjectIdDictionary;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.path.Path;
import com.thoughtworks.xstream.io.path.PathTracker;
import com.thoughtworks.xstream.io.path.PathTrackingWriter;

public class ReferenceByXPathMarshaller extends TreeMarshaller {

    private PathTracker pathTracker = new PathTracker();
    private ObjectIdDictionary references = new ObjectIdDictionary();

    public ReferenceByXPathMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, ClassMapper classMapper) {
        super(writer, converterLookup, classMapper);
        this.writer = new PathTrackingWriter(writer, pathTracker);
    }

    public void convertAnother(Object item) {
        Converter converter = converterLookup.lookupConverterForType(item.getClass());

        if (classMapper.isImmutableValueType(item.getClass())) {
            // strings, ints, dates, etc... don't bother using references.
            converter.marshal(item, writer, this);
        } else {
            Path currentPath = pathTracker.getPath();
            Path pathOfExistingReference = (Path) references.lookupId(item);
            if (pathOfExistingReference != null) {
                Path absolutePath = currentPath.relativeTo(pathOfExistingReference);
                writer.addAttribute("reference", absolutePath.toString());
            } else {
                references.associateId(item, currentPath);
                converter.marshal(item, writer, this);
            }
        }
    }

}
