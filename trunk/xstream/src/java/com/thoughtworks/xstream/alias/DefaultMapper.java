package com.thoughtworks.xstream.alias;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.alias.CannotResolveClassException;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;

public class DefaultMapper implements ClassMapper {

    private final ClassLoader classLoader;
    private final String classAttributeIdentifier;

    public DefaultMapper(ClassLoader classLoader) {
        this(classLoader, "class");
    }

    public DefaultMapper(ClassLoader classLoader, String classAttributeIdentifier) {
        this.classLoader = classLoader;
        this.classAttributeIdentifier = classAttributeIdentifier == null ? "class" : classAttributeIdentifier;
    }

    public String lookupName(Class type) {
        return type.getName();
    }

    public Class lookupType(String elementName) {
        try {
            return classLoader.loadClass(elementName);
        } catch (ClassNotFoundException e) {
            throw new CannotResolveClassException(elementName + " : " + e.getMessage());
        }
    }

    public Class lookupDefaultType(Class baseType) {
        return baseType;
    }

    public Class defaultImplementationOf(Class type) {
        return type;
    }

    public String attributeForClassDefiningField() {
        return "defined-in";
    }

    public String attributeForImplementationClass() {
        return classAttributeIdentifier;
    }

    public boolean isImmutableValueType(Class type) {
        return false;
    }

    public String mapNameFromXML(String xmlName) {
        return xmlName;
    }

    public String mapNameToXML(String javaName) {
        return javaName;
    }

    public void alias(String elementName, Class type, Class defaultImplementation) {
    }

}
