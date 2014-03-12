/*
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2013, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 08. July 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.converters.extended;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.plaf.FontUIResource;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * Converts an AWT {@link Font}.
 */
public class FontConverter implements Converter {

    private final SingleValueConverter textAttributeConverter;
    private final Mapper mapper;

    /**
     * Constructs a FontConverter.
     * 
     * @deprecated As of 1.4.5
     */
    @Deprecated
    public FontConverter() {
        this(null);
    }

    /**
     * Constructs a FontConverter.
     * 
     * @param mapper
     * @since 1.4.5
     */
    public FontConverter(final Mapper mapper) {
        this.mapper = mapper;
        if (mapper == null) {
            textAttributeConverter = null;
        } else {
            textAttributeConverter = new TextAttributeConverter();
        }
    }

    @Override
    public boolean canConvert(final Class<?> type) {
        // String comparison is used here because Font.class loads the class which in turns instantiates AWT,
        // which is nasty if you don't want it.
        return type.getName().equals("java.awt.Font") || type.getName().equals("javax.swing.plaf.FontUIResource");
    }

    @Override
    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Font font = (Font)source;
        final Map<TextAttribute, ?> attributes = font.getAttributes();
        if (mapper != null) {
            final String classAlias = mapper.aliasForSystemAttribute("class");
            for (final Map.Entry<TextAttribute, ?> entry : attributes.entrySet()) {
                final String name = textAttributeConverter.toString(entry.getKey());
                final Object value = entry.getValue();
                final Class<?> type = value != null ? value.getClass() : Mapper.Null.class;
                ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, type);
                writer.addAttribute(classAlias, mapper.serializedClass(type));
                if (value != null) {
                    context.convertAnother(value);
                }
                writer.endNode();
            }
        } else {
            writer.startNode("attributes"); // <attributes>
            context.convertAnother(attributes);
            writer.endNode(); // </attributes>
        }
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final Map<TextAttribute, Object> attributes;
        if (reader.hasMoreChildren()) {
            reader.moveDown();
            if (!reader.getNodeName().equals("attributes")) {
                final String classAlias = mapper.aliasForSystemAttribute("class");
                attributes = new HashMap<TextAttribute, Object>();
                do {
                    if (!attributes.isEmpty()) {
                        reader.moveDown();
                    }
                    final Class<?> type = mapper.realClass(reader.getAttribute(classAlias));
                    final TextAttribute attribute = (TextAttribute)textAttributeConverter.fromString(reader.getNodeName());
                    final Object value = type == Mapper.Null.class ? null : context.convertAnother(null, type);
                    attributes.put(attribute, value);
                    reader.moveUp();
                } while (reader.hasMoreChildren());
            } else {
                // in <attributes>
                @SuppressWarnings("unchecked")
                final Map<TextAttribute, Object> typedAttributes = (Map<TextAttribute, Object>)context.convertAnother(
                    null, Map.class);
                attributes = typedAttributes;
                reader.moveUp(); // out of </attributes>
            }
        } else {
            attributes = Collections.emptyMap();
        }
        for (final Iterator<?> iter = attributes.values().iterator(); iter.hasNext();) {
            if (iter.next() == null) {
                iter.remove();
            }
        }
        final Font font = Font.getFont(attributes);
        if (context.getRequiredType() == FontUIResource.class) {
            return new FontUIResource(font);
        } else {
            return font;
        }
    }
}
