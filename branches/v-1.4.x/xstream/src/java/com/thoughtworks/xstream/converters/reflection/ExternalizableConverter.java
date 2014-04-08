/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2010, 2011, 2013 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 24. August 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.reflection;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.core.ReferencingMarshallingContext;
import com.thoughtworks.xstream.core.util.CustomObjectInputStream;
import com.thoughtworks.xstream.core.util.CustomObjectOutputStream;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.mapper.Mapper;

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputValidation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Converts any object that implements the java.io.Externalizable interface, allowing compatibility with native Java
 * serialization.
 *
 * @author Joe Walnes
 */
public class ExternalizableConverter implements Converter {

    private Mapper mapper;
    private final ClassLoaderReference classLoaderReference;
    private transient SerializationMethodInvoker serializationMethodInvoker;

    /**
     * Construct an ExternalizableConverter.
     *
     * @param mapper the Mapper chain
     * @param classLoaderReference the reference to XStream's {@link ClassLoader} instance
     * @since 1.4.5
     */
    public ExternalizableConverter(Mapper mapper, ClassLoaderReference classLoaderReference) {
        this.mapper = mapper;
        this.classLoaderReference = classLoaderReference;
        serializationMethodInvoker = new SerializationMethodInvoker();
    }

    /**
     * @deprecated As of 1.4.5 use {@link #ExternalizableConverter(Mapper, ClassLoaderReference)}
     */
    public ExternalizableConverter(Mapper mapper, ClassLoader classLoader) {
        this(mapper, new ClassLoaderReference(classLoader));
    }

    /**
     * @deprecated As of 1.4 use {@link #ExternalizableConverter(Mapper, ClassLoader)}
     */
    public ExternalizableConverter(Mapper mapper) {
        this(mapper, ExternalizableConverter.class.getClassLoader());
    }

    public boolean canConvert(Class type) {
        return JVM.canCreateDerivedObjectOutputStream() && Externalizable.class.isAssignableFrom(type);
    }

    public void marshal(final Object original, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Object source = serializationMethodInvoker.callWriteReplace(original);
        if (source != original && context instanceof ReferencingMarshallingContext) {
            ((ReferencingMarshallingContext)context).replace(original, source);
        }
        if (source.getClass() != original.getClass()) {
            final String attributeName = mapper.aliasForSystemAttribute("resolves-to");
            if (attributeName != null) {
                writer.addAttribute(attributeName, mapper.serializedClass(source.getClass()));
            }
            context.convertAnother(source);
        } else {
            try {
                Externalizable externalizable = (Externalizable)source;
                CustomObjectOutputStream.StreamCallback callback = new CustomObjectOutputStream.StreamCallback() {
                    public void writeToStream(final Object object) {
                        if (object == null) {
                            writer.startNode("null");
                            writer.endNode();
                        } else {
                            ExtendedHierarchicalStreamWriterHelper.startNode(writer, mapper.serializedClass(object.getClass()), object.getClass());
                            context.convertAnother(object);
                            writer.endNode();
                        }
                    }

                    public void writeFieldsToStream(final Map fields) {
                        throw new UnsupportedOperationException();
                    }

                    public void defaultWriteObject() {
                        throw new UnsupportedOperationException();
                    }

                    public void flush() {
                        writer.flush();
                    }

                    public void close() {
                        throw new UnsupportedOperationException("Objects are not allowed to call ObjectOutput.close() from writeExternal()");
                    }
                };
                final CustomObjectOutputStream objectOutput = CustomObjectOutputStream.getInstance(context, callback);
                externalizable.writeExternal(objectOutput);
                objectOutput.popCallback();
            } catch (IOException e) {
                throw new ConversionException("Cannot serialize " + source.getClass().getName() + " using Externalization", e);
            }
        }
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final Class type = context.getRequiredType();
        final Constructor defaultConstructor;
        try {
            defaultConstructor = type.getDeclaredConstructor((Class[]) null);
            if (!defaultConstructor.isAccessible()) {
                defaultConstructor.setAccessible(true);
            }
            final Externalizable externalizable = (Externalizable) defaultConstructor.newInstance((Object[]) null);
            CustomObjectInputStream.StreamCallback callback = new CustomObjectInputStream.StreamCallback() {
                public Object readFromStream() {
                    reader.moveDown();
                    Class type = HierarchicalStreams.readClassType(reader, mapper);
                    Object streamItem = context.convertAnother(externalizable, type);
                    reader.moveUp();
                    return streamItem;
                }

                public Map readFieldsFromStream() {
                    throw new UnsupportedOperationException();
                }

                public void defaultReadObject() {
                    throw new UnsupportedOperationException();
                }

                public void registerValidation(ObjectInputValidation validation, int priority) throws NotActiveException {
                    throw new NotActiveException("stream inactive");
                }

                public void close() {
                    throw new UnsupportedOperationException("Objects are not allowed to call ObjectInput.close() from readExternal()");
                }
            };
            CustomObjectInputStream objectInput = CustomObjectInputStream.getInstance(context, callback, classLoaderReference);
            externalizable.readExternal(objectInput);
            objectInput.popCallback();
            return serializationMethodInvoker.callReadResolve(externalizable);
        } catch (NoSuchMethodException e) {
            throw new ConversionException("Cannot construct " + type.getClass() + ", missing default constructor", e);
        } catch (InvocationTargetException e) {
            throw new ConversionException("Cannot construct " + type.getClass(), e);
        } catch (InstantiationException e) {
            throw new ConversionException("Cannot construct " + type.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new ConversionException("Cannot construct " + type.getClass(), e);
        } catch (IOException e) {
            throw new ConversionException("Cannot externalize " + type.getClass(), e);
        } catch (ClassNotFoundException e) {
            throw new ConversionException("Cannot externalize " + type.getClass(), e);
        }
    }

    private Object readResolve() {
        serializationMethodInvoker = new SerializationMethodInvoker();
        return this;
    }
}
