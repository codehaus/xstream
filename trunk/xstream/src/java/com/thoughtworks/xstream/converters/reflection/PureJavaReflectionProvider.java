package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import java.io.ObjectStreamConstants;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Pure Java ObjectFactory that instantiates objects using standard Java reflection, however the types of objects
 * that can be constructed are limited.
 * <p/>
 * Can newInstance: classes with public visibility, outer classes, static inner classes, classes with default constructors
 * and any class that implements java.io.Serializable.
 * Cannot newInstance: classes without public visibility, non-static inner classes, classes without default constructors.
 * Note that any code in the constructor of a class will be executed when the ObjectFactory instantiates the object.
 * </p>
 */
public class PureJavaReflectionProvider implements ReflectionProvider {

    private final Map serializedDataCache = new HashMap();

    protected FieldDictionary fieldDictionary = new FieldDictionary();

    public Object newInstance(Class type) {
        try {
            Constructor[] constructors = type.getDeclaredConstructors();
            for (int i = 0; i < constructors.length; i++) {
                if (constructors[i].getParameterTypes().length == 0) {
                    if (!Modifier.isPublic(constructors[i].getModifiers())) {
                        constructors[i].setAccessible(true);
                    }
                    return constructors[i].newInstance(new Object[0]);
                }
            }
            if (Serializable.class.isAssignableFrom(type)) {
                return instantiateUsingSerialization(type);
            } else {
                throw new ObjectAccessException("Cannot construct " + type.getName()
                        + " as it does not have a no-args constructor");
            }
        } catch (InstantiationException e) {
            throw new ObjectAccessException("Cannot construct " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ObjectAccessException("Cannot construct " + type.getName(), e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            } else if (e.getTargetException() instanceof Error) {
                throw (Error)e.getTargetException();
            } else {
                throw new ObjectAccessException("Constructor for " + type.getName() + " threw an exception", e);
            }
        }
    }

    private Object instantiateUsingSerialization(Class type) {
        try {
            byte[] data;
            if (serializedDataCache.containsKey(type)) {
                data = (byte[]) serializedDataCache.get(type);
            } else {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                DataOutputStream stream = new DataOutputStream(bytes);
                stream.writeShort(ObjectStreamConstants.STREAM_MAGIC);
                stream.writeShort(ObjectStreamConstants.STREAM_VERSION);
                stream.writeByte(ObjectStreamConstants.TC_OBJECT);
                stream.writeByte(ObjectStreamConstants.TC_CLASSDESC);
                stream.writeUTF(type.getName());
                stream.writeLong(ObjectStreamClass.lookup(type).getSerialVersionUID());
                stream.writeByte(2);  // classDescFlags (2 = Serializable)
                stream.writeShort(0); // field count
                stream.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);
                stream.writeByte(ObjectStreamConstants.TC_NULL);
                data = bytes.toByteArray();
                serializedDataCache.put(type, data);
            }

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            return in.readObject();
        } catch (IOException e) {
            throw new ObjectAccessException("", e);
        } catch (ClassNotFoundException e) {
            throw new ObjectAccessException("", e);
        }
    }

    public void visitSerializableFields(Object object, ReflectionProvider.Visitor visitor) {
        for (Iterator iterator = fieldDictionary.serializableFieldsFor(object.getClass()); iterator.hasNext();) {
            Field field = (Field) iterator.next();
            if (!fieldModifiersSupported(field)) {
                continue;
            }
            validateFieldAccess(field);
            Object value = null;
            try {
                value = field.get(object);
            } catch (IllegalArgumentException e) {
                throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
            } catch (IllegalAccessException e) {
                throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
            }
            visitor.visit(field.getName(), field.getType(), field.getDeclaringClass(), value);
        }
    }

    public void writeField(Object object, String fieldName, Object value, Class definedIn) {
        Field field = fieldDictionary.field(object.getClass(), fieldName, definedIn);
        validateFieldAccess(field);
        try {
            field.set(object, value);
        } catch (IllegalArgumentException e) {
            throw new ObjectAccessException("Could not set field " + object.getClass() + "." + field.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ObjectAccessException("Could not set field " + object.getClass() + "." + field.getName(), e);
        }
    }

    public Class getFieldType(Object object, String fieldName, Class definedIn) {
        return fieldDictionary.field(object.getClass(), fieldName, definedIn).getType();
    }

    public boolean fieldDefinedInClass(String fieldName, Class type) {
        try {
            fieldDictionary.field(type, fieldName, null);
            return true;
        } catch (ObjectAccessException e) {
            return false;
        }
    }

    protected boolean fieldModifiersSupported(Field field) {
        return !(Modifier.isStatic(field.getModifiers())
                || Modifier.isTransient(field.getModifiers()));
    }

    protected void validateFieldAccess(Field field) {
        if (Modifier.isFinal(field.getModifiers())) {
            throw new ObjectAccessException("Invalid final field "
                    + field.getDeclaringClass().getName() + "." + field.getName());
        }
    }

}
