/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 31. January 2005 by Joe Walnes
 */
package com.thoughtworks.xstream.mapper;

/**
 * Mapper that uses a more meaningful alias for the field in an inner class (this$0) that refers to the outer class.
 *
 * @author Joe Walnes
 */
public class OuterClassMapper extends MapperWrapper {

    private final String alias;

    public OuterClassMapper(Mapper wrapped) {
        this(wrapped, "outer-class");
    }

    public OuterClassMapper(Mapper wrapped, String alias) {
        super(wrapped);
        this.alias = alias;
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
}
