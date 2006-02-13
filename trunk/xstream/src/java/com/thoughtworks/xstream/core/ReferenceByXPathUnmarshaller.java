package com.thoughtworks.xstream.core;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.path.Path;
import com.thoughtworks.xstream.io.path.PathTracker;
import com.thoughtworks.xstream.io.path.PathTrackingReader;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.HashMap;
import java.util.Map;

public class ReferenceByXPathUnmarshaller extends TreeUnmarshaller {

    private Map values = new HashMap();
    private FastStack parentPathStack = new FastStack(16);
    private PathTracker pathTracker = new PathTracker();

    public ReferenceByXPathUnmarshaller(Object root, HierarchicalStreamReader reader,
                                        ConverterLookup converterLookup, Mapper mapper) {
        super(root, reader, converterLookup, mapper);
        this.reader = new PathTrackingReader(reader, pathTracker);
    }

    /**
     * @deprecated As of 1.2, use {@link #ReferenceByXPathUnmarshaller(Object, HierarchicalStreamReader, ConverterLookup, Mapper)}
     */
    public ReferenceByXPathUnmarshaller(Object root, HierarchicalStreamReader reader,
                                        ConverterLookup converterLookup, ClassMapper classMapper) {
        this(root, reader, converterLookup, (Mapper)classMapper);
    }

    public Object convertAnother(Object parent, Class type) {
        if (parentPathStack.size() > 0) { // handles circular references
            Object parentPath = parentPathStack.peek();
            if (!values.containsKey(parentPath)) { // see AbstractCircularReferenceTest.testWeirdCircularReference()
                values.put(parentPath, parent);
            }
        }
        String relativePathOfReference = reader.getAttribute("reference");
        Path currentPath = pathTracker.getPath();
        if (relativePathOfReference != null) {
            return values.get(currentPath.apply(new Path(relativePathOfReference)));
        } else {
            parentPathStack.push(currentPath);
            Object result = super.convertAnother(parent, type);
            values.put(currentPath, result);
            parentPathStack.popSilently();
            return result;
        }
    }

}
