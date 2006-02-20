package com.thoughtworks.xstream.mapper;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.SingleValueConverter;

public abstract class MapperWrapper implements Mapper {

    private final Mapper wrapped;

    public MapperWrapper(Mapper wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * @deprecated As of 1.2, use {@link #MapperWrapper(Mapper)}
     */
    public MapperWrapper(ClassMapper wrapped) {
        this((Mapper)wrapped);
    }

    public String serializedClass(Class type) {
        return wrapped.serializedClass(type);
    }

    public Class realClass(String elementName) {
        return wrapped.realClass(elementName);
    }

    public String serializedMember(Class type, String memberName) {
        return wrapped.serializedMember(type, memberName);
    }

    public String realMember(Class type, String serialized) {
        return wrapped.realMember(type, serialized);
    }

    public boolean isImmutableValueType(Class type) {
        return wrapped.isImmutableValueType(type);
    }

    public Class defaultImplementationOf(Class type) {
        return wrapped.defaultImplementationOf(type);
    }

    public String attributeForClassDefiningField() {
        return wrapped.attributeForClassDefiningField();
    }

    public String attributeForImplementationClass() {
        return wrapped.attributeForImplementationClass();
    }

    public String attributeForReadResolveField() {
        return wrapped.attributeForReadResolveField();
    }

    public String attributeForEnumType() {
        return wrapped.attributeForEnumType();
    }

    public String getFieldNameForItemTypeAndName(Class definedIn, Class itemType, String itemFieldName) {
        return wrapped.getFieldNameForItemTypeAndName(definedIn, itemType, itemFieldName);
    }

    public Class getItemTypeForItemFieldName(Class definedIn, String itemFieldName) {
        return wrapped.getItemTypeForItemFieldName(definedIn, itemFieldName);
    }

    public ImplicitCollectionMapping getImplicitCollectionDefForFieldName(Class itemType, String fieldName) {
        return wrapped.getImplicitCollectionDefForFieldName(itemType, fieldName);
    }

    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
        return wrapped.shouldSerializeMember(definedIn, fieldName);
    }

    public SingleValueConverter getConverterFromItemType(Class clazz) {
        return wrapped.getConverterFromItemType(clazz);
    }

    public SingleValueConverter getConverterFromAttribute(String name) {
        return wrapped.getConverterFromAttribute(name);
    }

}
