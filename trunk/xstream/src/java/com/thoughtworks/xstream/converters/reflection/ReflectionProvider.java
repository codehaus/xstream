package com.thoughtworks.xstream.converters.reflection;

/**
 * Provides core reflection services.
 */
public interface ReflectionProvider {

    Object newInstance(Class type);

    void visitSerializableFields(Object object, Visitor visitor);

    void writeField(Object object, String fieldName, Object value, Class definedIn);

    Class getFieldType(Object object, String fieldName);

    interface Visitor {
        void visit(String name, Class type, Class definedIn, Object value);
    }
}
