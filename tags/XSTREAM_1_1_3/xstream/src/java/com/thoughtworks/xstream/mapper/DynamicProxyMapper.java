package com.thoughtworks.xstream.mapper;

import com.thoughtworks.xstream.alias.ClassMapper;

import java.lang.reflect.Proxy;

/**
 * Mapper for handling special cases of aliasing dynamic proxies. The alias property specifies the name an instance
 * of a dynamic proxy should be serialized with.
 *
 * @author Joe Walnes
 */
public class DynamicProxyMapper extends MapperWrapper {

    private String alias = "dynamic-proxy";

    public DynamicProxyMapper(ClassMapper wrapped) {
        super(wrapped);
    }

    public DynamicProxyMapper(ClassMapper wrapped, String alias) {
        super(wrapped);
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String serializedClass(Class type) {
        if (Proxy.isProxyClass(type)) {
            return alias;
        } else {
            return super.serializedClass(type);
        }
    }

    public Class realClass(String elementName) {
        if (elementName.equals(alias)) {
            return DynamicProxy.class;
        } else {
            return super.realClass(elementName);
        }
    }

    /**
     * Place holder type used for dynamic proxies.
     */
    public static class DynamicProxy {}

}
