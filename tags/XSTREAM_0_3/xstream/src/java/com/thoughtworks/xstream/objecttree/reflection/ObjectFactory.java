package com.thoughtworks.xstream.objecttree.reflection;

/**
 * An ObjectFactory is responsible for instantiating a new instance of a type.
 */
public interface ObjectFactory {
    Object create(Class type);
}
