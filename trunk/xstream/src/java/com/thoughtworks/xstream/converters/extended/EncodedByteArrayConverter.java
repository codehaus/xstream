/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2010, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 03. March 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.extended;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.ByteConverter;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


/**
 * Converts a byte array to a single Base64 encoding string.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 */
public class EncodedByteArrayConverter implements Converter, SingleValueConverter {

    private static final Base64Encoder base64 = new Base64Encoder();
    private static final ByteConverter byteConverter = new ByteConverter();

    @Override
    public boolean canConvert(final Class<?> type) {
        return type.isArray() && type.getComponentType().equals(byte.class);
    }

    @Override
    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        writer.setValue(toString(source));
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final String data = reader.getValue(); // needs to be called before hasMoreChildren.
        if (!reader.hasMoreChildren()) {
            return fromString(data);
        } else {
            // backwards compatibility ... try to unmarshal byte arrays that haven't been encoded
            return unmarshalIndividualByteElements(reader, context);
        }
    }

    private Object unmarshalIndividualByteElements(final HierarchicalStreamReader reader,
            final UnmarshallingContext context) {
        // have to create a temporary list because we don't know the size of the array
        final List<Byte> bytes = new ArrayList<Byte>();
        boolean firstIteration = true;
        while (firstIteration || reader.hasMoreChildren()) { // hangover from previous hasMoreChildren
            reader.moveDown();
            bytes.add((Byte)byteConverter.fromString(reader.getValue()));
            reader.moveUp();
            firstIteration = false;
        }
        // copy into real array
        final byte[] result = new byte[bytes.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = bytes.get(i).byteValue();
        }
        return result;
    }

    @Override
    public String toString(final Object obj) {
        return base64.encode((byte[])obj);
    }

    @Override
    public Object fromString(final String str) {
        return base64.decode(str);
    }
}
