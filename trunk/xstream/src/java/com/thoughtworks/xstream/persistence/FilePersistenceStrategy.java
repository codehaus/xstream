/*
 * Copyright (C) 2008, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 20. November 2008 by Joerg Schaible
 */
package com.thoughtworks.xstream.persistence;

import java.io.File;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;


/**
 * PersistenceStrategy to assign keys with single value to objects persisted in files.
 * <p>
 * The default naming strategy is based on the key's type and its {@link SingleValueConverter}. It escapes all
 * characters that are normally illegal in the most common file systems. Such a character is escaped with percent
 * escaping as it is done by URL encoding. The XStream used to marshal the values is also requested for the key's
 * SingleValueConverter. A {@link StreamException} is thrown if no such converter is registered.
 * </p>
 * 
 * @author J&ouml;rg Schaible
 * @author Guilherme Silveira
 * @since 1.3.1
 */
public class FilePersistenceStrategy<K, V> extends AbstractFilePersistenceStrategy<K, V> {

    private final String illegalChars;

    /**
     * Create a new FilePersistenceStrategy. Use a standard XStream instance with a {@link DomDriver}.
     * 
     * @param baseDirectory the directory for the serialized values
     * @since 1.3.1
     */
    public FilePersistenceStrategy(final File baseDirectory) {
        this(baseDirectory, new XStream(new DomDriver()));
    }

    /**
     * Create a new FilePersistenceStrategy with a provided XStream instance.
     * 
     * @param baseDirectory the directory for the serialized values
     * @param xstream the XStream instance to use for (de)serialization
     * @since 1.3.1
     */
    public FilePersistenceStrategy(final File baseDirectory, final XStream xstream) {
        this(baseDirectory, xstream, "utf-8", "<>?:/\\\"|*%");
    }

    /**
     * Create a new FilePersistenceStrategy with a provided XStream instance and the characters to encode.
     * 
     * @param baseDirectory the directory for the serialized values
     * @param xstream the XStream instance to use for (de)serialization
     * @param encoding encoding used to write the files
     * @param illegalChars illegal characters for file names (should always include '%' as long as you do not overwrite
     *            the (un)escape methods)
     * @since 1.3.1
     */
    public FilePersistenceStrategy(
            final File baseDirectory, final XStream xstream, final String encoding, final String illegalChars) {
        super(baseDirectory, xstream, encoding);
        this.illegalChars = illegalChars;
    }

    @Override
    protected boolean isValid(final File dir, final String name) {
        return super.isValid(dir, name) && name.indexOf('@') > 0;
    }

    /**
     * Given a filename, the unescape method returns the key which originated it.
     * 
     * @param name the filename
     * @return the original key
     */
    @Override
    protected K extractKey(final String name) {
        final String key = unescape(name.substring(0, name.length() - 4));
        if ("null@null".equals(key)) {
            return null;
        }
        final int idx = key.indexOf('@');
        if (idx < 0) {
            throw new StreamException("Not a valid key: " + key);
        }
        final Class<?> type = getMapper().realClass(key.substring(0, idx));
        final Converter converter = getConverterLookup().lookupConverterForType(type);
        if (converter instanceof SingleValueConverter) {
            final SingleValueConverter svConverter = (SingleValueConverter)converter;
            @SuppressWarnings("unchecked")
            final K k = (K)svConverter.fromString(key.substring(idx + 1));
            return k;
        } else {
            throw new StreamException("No SingleValueConverter for type " + type.getName() + " available");
        }
    }

    protected String unescape(String name) {
        final StringBuilder buffer = new StringBuilder();
        for (int idx = name.indexOf('%'); idx >= 0; idx = name.indexOf('%')) {
            buffer.append(name.substring(0, idx));
            final int c = Integer.parseInt(name.substring(idx + 1, idx + 3), 16);
            buffer.append((char)c);
            name = name.substring(idx + 3);
        }
        buffer.append(name);
        return buffer.toString();
    }

    /**
     * Given a key, the escape method returns the filename which shall be used.
     * 
     * @param key the key
     * @return the desired and escaped filename
     */
    @Override
    protected String getName(final Object key) {
        if (key == null) {
            return "null@null.xml";
        }
        final Class<?> type = key.getClass();
        final Converter converter = getConverterLookup().lookupConverterForType(type);
        if (converter instanceof SingleValueConverter) {
            final SingleValueConverter svConverter = (SingleValueConverter)converter;
            return getMapper().serializedClass(type) + '@' + escape(svConverter.toString(key)) + ".xml";
        } else {
            throw new StreamException("No SingleValueConverter for type " + type.getName() + " available");
        }
    }

    protected String escape(final String key) {
        final StringBuilder buffer = new StringBuilder();
        final char[] array = key.toCharArray();
        for (final char c : array) {
            if (c >= ' ' && illegalChars.indexOf(c) < 0) {
                buffer.append(c);
            } else {
                buffer.append("%" + Integer.toHexString(c).toUpperCase());
            }
        }
        return buffer.toString();
    }
}
