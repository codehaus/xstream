/*
 * Copyright (C) 2006, 2007, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 13. April 2006 by Joerg Schaible
 */
package com.thoughtworks.xstream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.TypePermission;

/**
 * Self-contained XStream generator. The class is a utility to write XML streams that contain
 * additionally the XStream that was used to serialize the object graph. Such a stream can
 * be unmarshalled using this embedded XStream instance, that kept any settings.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.2
 */
public class XStreamer {

    private final static TypePermission[] PERMISSIONS = {
        AnyTypePermission.ANY
    };

    /**
     * Serialize an object including the XStream to a pretty-printed XML String.
     * 
     * @throws ObjectStreamException if the XML contains non-serializable elements
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be serialized
     * @since 1.2
     * @see #toXML(XStream, Object, Writer)
     */
    public String toXML(final XStream xstream, final Object obj) throws ObjectStreamException {
        final Writer writer = new StringWriter();
        try {
            toXML(xstream, obj, writer);
        } catch (final ObjectStreamException e) {
            throw e;
        } catch (final IOException e) {
            throw new ConversionException("Unexpected IO error from a StringWriter", e);
        }
        return writer.toString();
    }

    /**
     * Serialize an object including the XStream to the given Writer as pretty-printed XML.
     * <p>
     * Warning: XStream will serialize itself into this XML stream. To read such an XML code, you
     * should use {@link XStreamer#fromXML(Reader)} or one of the other overloaded
     * methods. Since a lot of internals are written into the stream, you cannot expect to use such
     * an XML to work with another XStream version or with XStream running on different JDKs and/or
     * versions. We have currently no JDK 1.3 support, nor will the PureReflectionConverter work
     * with a JDK less than 1.5.
     * </p>
     * 
     * @throws IOException if an error occurs reading from the Writer.
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be serialized
     * @since 1.2
     */
    public void toXML(final XStream xstream, final Object obj, final Writer out)
            throws IOException {
        final XStream outer = new XStream();
        final ObjectOutputStream oos = outer.createObjectOutputStream(out);
        try {
            oos.writeObject(xstream);
            oos.flush();
            xstream.toXML(obj, out);
        } finally {
            oos.close();
        }
    }

    /**
     * Deserialize a self-contained XStream with object from a String. The method will use
     * internally an XppDriver to load the contained XStream instance with default permissions.
     * 
     * @param xml the XML data
     * @throws ClassNotFoundException if a class in the XML stream cannot be found
     * @throws ObjectStreamException if the XML contains non-deserializable elements
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be deserialized
     * @since 1.2
     * @see #toXML(XStream, Object, Writer)
     */
    public Object fromXML(final String xml) throws ClassNotFoundException, ObjectStreamException {
        try {
            return fromXML(new StringReader(xml));
        } catch (final ObjectStreamException e) {
            throw e;
        } catch (final IOException e) {
            throw new ConversionException("Unexpected IO error from a StringReader", e);
        }
    }

    /**
     * Deserialize a self-contained XStream with object from a String. The method will use
     * internally an XppDriver to load the contained XStream instance.
     * 
     * @param xml the XML data
     * @param permissions the permissions to use (ensure that they include the defaults)
     * @throws ClassNotFoundException if a class in the XML stream cannot be found
     * @throws ObjectStreamException if the XML contains non-deserializable elements
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be deserialized
     * @since upcoming
     * @see #toXML(XStream, Object, Writer)
     */
    public Object fromXML(final String xml, final TypePermission[] permissions) throws ClassNotFoundException, ObjectStreamException {
        try {
            return fromXML(new StringReader(xml), permissions);
        } catch (final ObjectStreamException e) {
            throw e;
        } catch (final IOException e) {
            throw new ConversionException("Unexpected IO error from a StringReader", e);
        }
    }

    /**
     * Deserialize a self-contained XStream with object from a String.
     * 
     * @param driver the implementation to use
     * @param xml the XML data
     * @throws ClassNotFoundException if a class in the XML stream cannot be found
     * @throws ObjectStreamException if the XML contains non-deserializable elements
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be deserialized
     * @since 1.2
     * @see #toXML(XStream, Object, Writer)
     */
    public Object fromXML(final HierarchicalStreamDriver driver, final String xml)
            throws ClassNotFoundException, ObjectStreamException {
        try {
            return fromXML(driver, new StringReader(xml));
        } catch (final ObjectStreamException e) {
            throw e;
        } catch (final IOException e) {
            throw new ConversionException("Unexpected IO error from a StringReader", e);
        }
    }

    /**
     * Deserialize a self-contained XStream with object from a String.
     * 
     * @param driver the implementation to use
     * @param xml the XML data
     * @param permissions the permissions to use (ensure that they include the defaults)
     * @throws ClassNotFoundException if a class in the XML stream cannot be found
     * @throws ObjectStreamException if the XML contains non-deserializable elements
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be deserialized
     * @since upcoming
     * @see #toXML(XStream, Object, Writer)
     */
    public Object fromXML(final HierarchicalStreamDriver driver, final String xml, final TypePermission[] permissions)
            throws ClassNotFoundException, ObjectStreamException {
        try {
            return fromXML(driver, new StringReader(xml), permissions);
        } catch (final ObjectStreamException e) {
            throw e;
        } catch (final IOException e) {
            throw new ConversionException("Unexpected IO error from a StringReader", e);
        }
    }

    /**
     * Deserialize a self-contained XStream with object from an XML Reader. The method will use
     * internally an XppDriver to load the contained XStream instance with default permissions.
     * 
     * @param xml the {@link Reader} providing the XML data
     * @throws IOException if an error occurs reading from the Reader.
     * @throws ClassNotFoundException if a class in the XML stream cannot be found
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be deserialized
     * @since 1.2
     * @see #toXML(XStream, Object, Writer)
     */
    public Object fromXML(final Reader xml)
            throws IOException, ClassNotFoundException {
        return fromXML(new XppDriver(), xml);
    }

    /**
     * Deserialize a self-contained XStream with object from an XML Reader. The method will use
     * internally an XppDriver to load the contained XStream instance.
     * 
     * @param xml the {@link Reader} providing the XML data
     * @param permissions the permissions to use (ensure that they include the defaults)
     * @throws IOException if an error occurs reading from the Reader.
     * @throws ClassNotFoundException if a class in the XML stream cannot be found
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be deserialized
     * @since upcoming
     * @see #toXML(XStream, Object, Writer)
     */
    public Object fromXML(final Reader xml, final TypePermission[] permissions)
            throws IOException, ClassNotFoundException {
        return fromXML(new XppDriver(), xml, permissions);
    }

    /**
     * Deserialize a self-contained XStream with object from an XML Reader.
     *
     * @param driver the implementation to use
     * @param xml the {@link Reader} providing the XML data
     * @throws IOException if an error occurs reading from the Reader.
     * @throws ClassNotFoundException if a class in the XML stream cannot be found
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be deserialized
     * @since 1.2
     */
    public Object fromXML(final HierarchicalStreamDriver driver, final Reader xml)
            throws IOException, ClassNotFoundException {
        return fromXML(driver, xml, PERMISSIONS);
    }

    /**
     * Deserialize a self-contained XStream with object from an XML Reader.
     * 
     * @param driver the implementation to use
     * @param xml the {@link Reader} providing the XML data
     * @param permissions the permissions to use (ensure that they include the defaults)
     * @throws IOException if an error occurs reading from the Reader.
     * @throws ClassNotFoundException if a class in the XML stream cannot be found
     * @throws com.thoughtworks.xstream.XStreamException if the object cannot be deserialized
     * @since upcoming
     */
    public Object fromXML(final HierarchicalStreamDriver driver, final Reader xml, final TypePermission[] permissions)
            throws IOException, ClassNotFoundException {
        final XStream outer = new XStream(driver);
        for(int i = 0; i < permissions.length; ++i) {
            outer.addPermission(permissions[i]);
        }
        final HierarchicalStreamReader reader = driver.createReader(xml);
        final ObjectInputStream configIn = outer.createObjectInputStream(reader);
        try {
            final XStream configured = (XStream)configIn.readObject();
            final ObjectInputStream in = configured.createObjectInputStream(reader);
            try {
                return in.readObject();
            } finally {
                in.close();
            }
        } finally {
            configIn.close();
        }
    }

    /**
     * Retrieve the default permissions to unmarshal an XStream instance.
     * <p>
     * The returned list will only cover permissions for XStream's own types. If your custom converters or mappers keep
     * references to other types, you will have to add permission for those types on your own.
     * </p>
     * 
     * @since upcoming
     */
    public static TypePermission[] getDefaultPermissions() {
        return (TypePermission[])PERMISSIONS.clone();
    }
}
