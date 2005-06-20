package com.thoughtworks.xstream.mapper;

import com.thoughtworks.xstream.alias.ClassMapper;

import java.util.*;

/**
 * Mapper that allows a field of a specific class to be replaced with a shorter alias, or omitted
 * entirely.
 *
 * @author Joe Walnes
 */
public class FieldAliasingMapper extends MapperWrapper {

    protected final Map fieldToAliasMap = Collections.synchronizedMap(new HashMap());
    protected final Map aliasToFieldMap = Collections.synchronizedMap(new HashMap());
    protected final Set fieldsToOmit = Collections.synchronizedSet(new HashSet());

    public FieldAliasingMapper(ClassMapper wrapped) {
        super(wrapped);
    }

    public void addFieldAlias(String alias, Class type, String fieldName) {
        fieldToAliasMap.put(key(type, fieldName), alias);
        aliasToFieldMap.put(key(type, alias), fieldName);
    }

    private Object key(Class type, String value) {
        return type.getName() + '.' + value;
    }

    public String serializedMember(Class type, String memberName) {
        String alias = (String) fieldToAliasMap.get(key(type, memberName));
        if (alias == null) {
            return super.serializedMember(type, memberName);
        } else {
            return alias;
        }
    }

    public String realMember(Class type, String serialized) {
        String real = (String) aliasToFieldMap.get(key(type, serialized));
        if (real == null) {
            return super.realMember(type, serialized);
        } else {
            return real;
        }
    }

    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
        return !fieldsToOmit.contains(key(definedIn, fieldName));
    }

    public void omitField(Class type, String fieldName) {
        fieldsToOmit.add(key(type, fieldName));
    }
}
