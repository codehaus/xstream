package com.thoughtworks.xstream.builder;

import com.thoughtworks.xstream.XStream;

/**
 * @author Guilherme Silveira
 * @since upcoming
 */
public class XStreamFieldConfig implements XStreamBuilderConfigNode {

    private final String fieldName;
    // leave alias as null per default so there is no aliasing invocation for default field names
    private String alias;
    private boolean attribute;
    private final Class type;

    public XStreamFieldConfig(Class type, String fieldName) {
        this.type = type;
        this.fieldName = fieldName;
        this.alias = null;
        this.attribute = false;
    }

    public XStreamFieldConfig as(String alias) {
        this.alias = alias;
        return this;
    }

    public XStreamFieldConfig asAttribute() {
        this.attribute = true;
        return this;
    }

    public void process(XStream instance) {
        if(this.attribute) {
            instance.useAttributeFor(type, fieldName);
            if(this.alias != null) {
                instance.aliasAttribute(type, fieldName, alias);
            }
        } else if(this.alias!=null) {
            instance.aliasField(alias, type, fieldName);
        }
    }
}