package com.thoughtworks.xstream.converters.composite;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.alias.ElementMapper;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.objecttree.ObjectTree;
import com.thoughtworks.xstream.xml.XMLReader;
import com.thoughtworks.xstream.xml.XMLWriter;

public class ObjectWithFieldsConverter implements Converter {

    private ClassMapper classMapper;
//    private CircularityTracker circularityTracker = new CircularityTracker();

    private ElementMapper elementMapper;

    public ObjectWithFieldsConverter(ClassMapper classMapper,ElementMapper elementMapper) {
        this.classMapper = classMapper;
        this.elementMapper = elementMapper;
    }

    public boolean canConvert(Class type) {
        return true;
    }

    public void toXML(ObjectTree objectGraph, XMLWriter xmlWriter, ConverterLookup converterLookup) {
        String[] fieldNames = objectGraph.fieldNames();
//        circularityTracker.track(objectGraph.get());
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];

            objectGraph.push(fieldName);

            if (objectGraph.get() != null) {
                writeFieldAsXML(xmlWriter, elementMapper.toXml(fieldName), objectGraph, converterLookup);
            }

            objectGraph.pop();
        }
    }

    private void writeFieldAsXML(XMLWriter xmlWriter, String fieldName, ObjectTree objectGraph, ConverterLookup converterLookup) {
        xmlWriter.startElement(fieldName);

        writeClassAttributeInXMLIfNotDefaultImplementation(objectGraph, xmlWriter);
        Converter converter = converterLookup.lookupConverterForType(objectGraph.type());
        converter.toXML(objectGraph, xmlWriter, converterLookup);

        xmlWriter.endElement();
    }

    protected void writeClassAttributeInXMLIfNotDefaultImplementation(ObjectTree objectGraph, XMLWriter xmlWriter) {
        Class actualType = objectGraph.get().getClass();
        Class defaultType = classMapper.lookupDefaultType(objectGraph.type());
        if (!actualType.equals(defaultType)) {
            xmlWriter.addAttribute("class", classMapper.lookupName(actualType));
        }
    }

    public void fromXML(final ObjectTree objectGraph, XMLReader xmlReader, ConverterLookup converterLookup, Class requiredType) {

        // Only create the root if one has not been provided.

        if ( objectGraph.get() == null ) {
            objectGraph.create(requiredType);
        }

        while (xmlReader.nextChild()) {
            objectGraph.push(elementMapper.fromXml(xmlReader.name()));

            Class type = determineWhichImplementationToUse(xmlReader, objectGraph);
            Converter converter = converterLookup.lookupConverterForType(type);
            converter.fromXML(objectGraph, xmlReader, converterLookup, type);
            objectGraph.pop();

            xmlReader.pop();
        }
    }

    private Class determineWhichImplementationToUse(XMLReader xmlReader, final ObjectTree objectGraph) {
        String classAttribute = xmlReader.attribute("class");
        Class type;
        if (classAttribute == null) {
            type = objectGraph.type();
        } else {
            type = classMapper.lookupType(classAttribute);
        }
        return type;
    }

}
