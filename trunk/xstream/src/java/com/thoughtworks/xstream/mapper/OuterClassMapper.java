package com.thoughtworks.xstream.mapper;

import com.thoughtworks.xstream.alias.ClassMapper;

/**
 * Mapper that uses a more meaningful alias for the field in an inner class (this$0) that refers to the outer class.
 *
 * @author Joe Walnes
 */
public class OuterClassMapper extends MapperWrapper {

    private final String alias;

    public OuterClassMapper(ClassMapper wrapped, String alias) {
        super(wrapped);
        this.alias = alias;
    }

    public OuterClassMapper(ClassMapper wrapped) {
        this(wrapped, "outer-class");
    }

    public String serializedMember(Class type, String memberName) {
        if (memberName.equals("this$0")) {
            return alias;
        } else {
            return super.serializedMember(type, memberName);
        }
    }

    public String realMember(Class type, String serialized) {
        if (serialized.equals(alias)) {
            return "this$0";
        } else {
            return super.realMember(type, serialized);
        }
    }

    // --- Maintain backwards compatability

    public String mapNameToXML(String javaName) {
        if (javaName.equals("this$0")) {
            return alias;
        } else {
            return super.mapNameToXML(javaName);
        }
    }

    public String mapNameFromXML(String xmlName) {
        if (xmlName.equals(alias)) {
            return "this$0";
        } else {
            return super.mapNameFromXML(xmlName);
        }
    }

}
