package com.thoughtworks.xstream;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.alias.DefaultClassMapper;
import com.thoughtworks.xstream.alias.DefaultNameMapper;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.basic.ByteConverter;
import com.thoughtworks.xstream.converters.basic.CharConverter;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.basic.DoubleConverter;
import com.thoughtworks.xstream.converters.basic.FloatConverter;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.JavaClassConverter;
import com.thoughtworks.xstream.converters.basic.LongConverter;
import com.thoughtworks.xstream.converters.basic.ShortConverter;
import com.thoughtworks.xstream.converters.basic.StringBufferConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.converters.collections.PropertiesConverter;
import com.thoughtworks.xstream.converters.composite.ObjectWithFieldsConverter;
import com.thoughtworks.xstream.converters.lookup.DefaultConverterLookup;
import com.thoughtworks.xstream.objecttree.ObjectTree;
import com.thoughtworks.xstream.objecttree.reflection.ObjectFactory;
import com.thoughtworks.xstream.objecttree.reflection.ReflectionObjectGraph;
import com.thoughtworks.xstream.objecttree.reflection.SunReflectionObjectFactory;
import com.thoughtworks.xstream.xml.XMLReader;
import com.thoughtworks.xstream.xml.XMLReaderDriver;
import com.thoughtworks.xstream.xml.XMLWriter;
import com.thoughtworks.xstream.xml.dom.DomXMLReaderDriver;
import com.thoughtworks.xstream.xml.text.PrettyPrintXMLWriter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class XStream {

    protected ConverterLookup converterLookup = new DefaultConverterLookup();
    protected XMLReaderDriver xmlReaderDriver;
    protected ClassMapper classMapper;
    protected ObjectFactory objectFactory;

    public XStream() {
        this(new SunReflectionObjectFactory(), new DefaultClassMapper(new DefaultNameMapper()), new DomXMLReaderDriver());
    }

    public XStream(ObjectFactory objectFactory, ClassMapper classMapper, XMLReaderDriver xmlReaderDriver) {
        this.classMapper = classMapper;
        this.objectFactory = objectFactory;
        this.xmlReaderDriver = xmlReaderDriver;

        alias("int", Integer.class);
        alias("float", Float.class);
        alias("double", Double.class);
        alias("long", Long.class);
        alias("short", Short.class);
        alias("char", Character.class);
        alias("byte", Byte.class);
        alias("boolean", Boolean.class);
        alias("number", Number.class);
        alias("object", Object.class);

        alias("string-buffer", StringBuffer.class);
        alias("string", String.class);
        alias("java-class", Class.class);
        alias("date", Date.class);

        alias("map", Map.class, HashMap.class);
        alias("properties", Properties.class);
        alias("list", List.class, ArrayList.class);
        alias("set", Set.class, HashSet.class);

        alias("linked-list", LinkedList.class);
        alias("tree-map", TreeMap.class);
        alias("tree-set", TreeSet.class);

        registerConverter(new ObjectWithFieldsConverter(classMapper));

        registerConverter(new IntConverter());
        registerConverter(new FloatConverter());
        registerConverter(new DoubleConverter());
        registerConverter(new LongConverter());
        registerConverter(new ShortConverter());
        registerConverter(new CharConverter());
        registerConverter(new BooleanConverter());
        registerConverter(new ByteConverter());

        registerConverter(new StringConverter());
        registerConverter(new StringBufferConverter());
        registerConverter(new DateConverter());
        registerConverter(new JavaClassConverter());

        registerConverter(new ArrayConverter(classMapper));
        registerConverter(new CollectionConverter(classMapper));
        registerConverter(new MapConverter(classMapper));
        registerConverter(new PropertiesConverter());

    }

    public void alias(String elementName, Class type, Class defaultImplementation) {
        classMapper.alias(elementName, type, defaultImplementation);
    }

    public void alias(String elementName, Class type) {
        alias(elementName, type, type);
    }

    public String toXML(Object obj) {
        Writer stringWriter = new StringWriter();
        XMLWriter xmlWriter = new PrettyPrintXMLWriter(stringWriter);
        toXML(obj, xmlWriter);
        return stringWriter.toString();
    }

    public void toXML(Object obj, XMLWriter xmlWriter) {
        ObjectTree objectGraph = new ReflectionObjectGraph(obj, objectFactory);
        Converter rootConverter = converterLookup.lookupConverterForType(obj.getClass());
        xmlWriter.startElement(classMapper.lookupName(obj.getClass()));
        rootConverter.toXML(objectGraph, xmlWriter, converterLookup);
        xmlWriter.endElement();
    }

    public Object fromXML(String xml) {
        return fromXML(xmlReaderDriver.createReader(xml));
    }

    public Object fromXML(XMLReader xmlReader) {
        Class type = classMapper.lookupType(xmlReader.name());
        ObjectTree objectGraph = new ReflectionObjectGraph(type, objectFactory);
        Converter rootConverter = converterLookup.lookupConverterForType(type);
        rootConverter.fromXML(objectGraph, xmlReader, converterLookup, type);
        return objectGraph.get();
    }

    public Object fromXML(XMLReader xmlReader, Object root) {
        Class type = root.getClass();
        ObjectTree objectGraph = new ReflectionObjectGraph(root, objectFactory);
        Converter rootConverter = converterLookup.lookupConverterForType(type);
        rootConverter.fromXML(objectGraph, xmlReader, converterLookup, type);
        return objectGraph.get();
    }

    public void registerConverter(Converter converter) {
        converterLookup.registerConverter(converter);
    }
}
