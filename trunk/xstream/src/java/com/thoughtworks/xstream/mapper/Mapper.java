package com.thoughtworks.xstream.mapper;



public interface Mapper {

    /**
     * How a class name should be represented in its serialized form.
     */
    String serializedClass(Class type);

    /**
     * How a serialized class representation should be mapped back to a real class.
     */
    Class realClass(String elementName);

    /**
     * How a class member should be represented in its serialized form.
     */
    String serializedMember(Class type, String memberName);

    /**
     * How a serialized member representation should be mapped back to a real member.
     */
    String realMember(Class type, String serialized);

    /**
     * Whether this type is a simple immutable value (int, boolean, String, URL, etc.
     * Immutable types will be repeatedly written in the serialized stream, instead of using object references.
     */
    boolean isImmutableValueType(Class type);

    Class defaultImplementationOf(Class type);

    String attributeForImplementationClass();

    String attributeForClassDefiningField();

    String attributeForReadResolveField();

    /**
     * Get the name of the field that acts as the default collection for an object, or return null if there is none.
     *
     * @param definedIn     owning type
     * @param itemType      item type
     * @param itemFieldName optional item element name
     */
    String getFieldNameForItemTypeAndName(Class definedIn, Class itemType, String itemFieldName);

    Class getItemTypeForItemFieldName(Class definedIn, String itemFieldName);

    ImplicitCollectionDef getImplicitCollectionDefForFieldName(Class definedIn, String fieldName);

    interface ImplicitCollectionDef {
        String getFieldName();
        String getItemFieldName();
        Class getItemType();
    }

}
