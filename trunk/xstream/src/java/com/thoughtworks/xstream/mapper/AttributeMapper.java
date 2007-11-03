package com.thoughtworks.xstream.mapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * Mapper that allows the usage of attributes for fields and corresponding 
 * types or specified arbitrary types. It is responsible for the lookup of the 
 * {@link SingleValueConverter} for item types and attribute names.
 *
 * @author Paul Hammant 
 * @author Ian Cartwright
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 * @author Guilherme Silveira
 * @since 1.2
 */
public class AttributeMapper extends MapperWrapper {

    private final Map fieldNameToTypeMap = new HashMap();
    private final Set typeSet = new HashSet();
    private ConverterLookup converterLookup;
    private final Set fieldToUseAsAttribute = new HashSet();

    /**
     * @deprecated since upcoming
     */
    public AttributeMapper(Mapper wrapped) {
        this(wrapped, null);
    }

    public AttributeMapper(Mapper wrapped, ConverterLookup converterLookup) {
        super(wrapped);
        this.converterLookup = converterLookup;
    }
    
    /**
     * @deprecated since upcoming
     */
    public void setConverterLookup(ConverterLookup converterLookup) {
        this.converterLookup = converterLookup;
    }

    public void addAttributeFor(final String fieldName, final Class type) {
        fieldNameToTypeMap.put(fieldName, type);
    }

    public void addAttributeFor(final Class type) {
        typeSet.add(type);
    }

    protected SingleValueConverter getLocalConverterFromItemType(Class type) {
        Converter converter = converterLookup.lookupConverterForType(type);
        if (converter != null && converter instanceof SingleValueConverter) {
            return (SingleValueConverter)converter;
        } else {
            return null;
        }
    }

    public SingleValueConverter getConverterFromItemType(String fieldName, Class type) {
        if (fieldNameToTypeMap.get(fieldName) == type) {
            return getLocalConverterFromItemType(type);
        } else {
            return null;
        }
    }

    public SingleValueConverter getConverterFromItemType(String fieldName, Class type,
        Class definedIn) {
        Field field = getField(definedIn, fieldName);
        if (fieldToUseAsAttribute.contains(field)) {
            return getLocalConverterFromItemType(type);
        } else {
            return getConverterFromItemType(fieldName, type);
        }
    }

    public SingleValueConverter getConverterFromItemType(Class type) {
        if (typeSet.contains(type)) {
            return getLocalConverterFromItemType(type);
        } else {
            return null;
        }
    }

    public SingleValueConverter getConverterFromAttribute(String attributeName) {
        SingleValueConverter converter = null;
        Class type = (Class)fieldNameToTypeMap.get(attributeName);
        if (type != null) {
            converter = getLocalConverterFromItemType(type);
        }
        return converter;
    }

    public SingleValueConverter getConverterFromAttribute(Class type, String attribute) {
        Field field = getField(type, attribute);
        if (fieldToUseAsAttribute.contains(field)) {
            return getLocalConverterFromItemType(field.getType());
        }
        return getConverterFromAttribute(attribute);
    }

    /**
     * Tells this mapper to use an attribute for this field.
     * 
     * @param field the field itself
     * since 1.2.2
     */
    public void addAttributeFor(Field field) {
        fieldToUseAsAttribute.add(field);
    }

    private Field getField(Class definedIn, String fieldName) {
        try {
            return definedIn.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(fieldName
                + " is not a proper field of "
                + definedIn.getName());
        }
    }

}
