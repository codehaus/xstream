/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009, 2011, 2013, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 29. September 2004 by James Strachan
 */
package com.thoughtworks.xstream.io.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.ReaderWrapper;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.naming.NameCoder;


/**
 * A driver using the StAX API to create XML reader and writer.
 * 
 * @author James Strachan
 * @author J&ouml;rg Schaible
 * @version $Revision$
 */
public class StaxDriver extends AbstractXmlDriver {

    private QNameMap qnameMap;
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;

    public StaxDriver() {
        this(new QNameMap());
    }

    public StaxDriver(final QNameMap qnameMap) {
        this(qnameMap, new XmlFriendlyNameCoder());
    }

    /**
     * @since 1.4
     */
    public StaxDriver(final QNameMap qnameMap, final NameCoder nameCoder) {
        super(nameCoder);
        this.qnameMap = qnameMap;
    }

    /**
     * @since 1.4
     */
    public StaxDriver(final NameCoder nameCoder) {
        this(new QNameMap(), nameCoder);
    }

    /**
     * @since 1.2
     * @deprecated As of 1.4, use {@link StaxDriver#StaxDriver(QNameMap, NameCoder)} instead.
     */
    @Deprecated
    public StaxDriver(final QNameMap qnameMap, final XmlFriendlyReplacer replacer) {
        this(qnameMap, (NameCoder)replacer);
    }

    /**
     * @since 1.2
     * @deprecated As of 1.4, use {@link StaxDriver#StaxDriver(NameCoder)} instead.
     */
    @Deprecated
    public StaxDriver(final XmlFriendlyReplacer replacer) {
        this(new QNameMap(), (NameCoder)replacer);
    }

    @Override
    public HierarchicalStreamReader createReader(final Reader xml) {
        try {
            return createStaxReader(createParser(xml));
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    @Override
    public HierarchicalStreamReader createReader(final InputStream in) {
        try {
            return createStaxReader(createParser(in));
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public HierarchicalStreamReader createReader(final URL in) {
        final InputStream stream;
        try {
            stream = in.openStream();
            final HierarchicalStreamReader reader = createStaxReader(createParser(new StreamSource(stream, in
                .toExternalForm())));
            return new ReaderWrapper(reader) {

                @Override
                public void close() {
                    super.close();
                    try {
                        stream.close();
                    } catch (final IOException e) {
                        // ignore
                    }
                }
            };
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        } catch (final IOException e) {
            throw new StreamException(e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public HierarchicalStreamReader createReader(final File in) {
        final InputStream stream;
        try {
            stream = new FileInputStream(in);
            final HierarchicalStreamReader reader = createStaxReader(createParser(new StreamSource(stream, in
                .toURI()
                .toASCIIString())));
            return new ReaderWrapper(reader) {

                @Override
                public void close() {
                    super.close();
                    try {
                        stream.close();
                    } catch (final IOException e) {
                        // ignore
                    }
                }
            };
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        } catch (final FileNotFoundException e) {
            throw new StreamException(e);
        }
    }

    @Override
    public HierarchicalStreamWriter createWriter(final Writer out) {
        try {
            return createStaxWriter(getOutputFactory().createXMLStreamWriter(out));
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    @Override
    public HierarchicalStreamWriter createWriter(final OutputStream out) {
        try {
            return createStaxWriter(getOutputFactory().createXMLStreamWriter(out));
        } catch (final XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    public AbstractPullReader createStaxReader(final XMLStreamReader in) {
        return new StaxReader(qnameMap, in, getNameCoder());
    }

    public StaxWriter createStaxWriter(final XMLStreamWriter out, final boolean writeStartEndDocument)
            throws XMLStreamException {
        return new StaxWriter(qnameMap, out, writeStartEndDocument, isRepairingNamespace(), getNameCoder());
    }

    public StaxWriter createStaxWriter(final XMLStreamWriter out) throws XMLStreamException {
        return createStaxWriter(out, true);
    }

    // Properties
    // -------------------------------------------------------------------------
    public QNameMap getQnameMap() {
        return qnameMap;
    }

    public void setQnameMap(final QNameMap qnameMap) {
        this.qnameMap = qnameMap;
    }

    public XMLInputFactory getInputFactory() {
        if (inputFactory == null) {
            inputFactory = createInputFactory();
        }
        return inputFactory;
    }

    public XMLOutputFactory getOutputFactory() {
        if (outputFactory == null) {
            outputFactory = createOutputFactory();
        }
        return outputFactory;
    }

    public boolean isRepairingNamespace() {
        return Boolean.TRUE.equals(getOutputFactory().getProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES));
    }

    /**
     * @since 1.2
     */
    public void setRepairingNamespace(final boolean repairing) {
        getOutputFactory().setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES,
            repairing ? Boolean.TRUE : Boolean.FALSE);
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected XMLStreamReader createParser(final Reader xml) throws XMLStreamException {
        return getInputFactory().createXMLStreamReader(xml);
    }

    protected XMLStreamReader createParser(final InputStream xml) throws XMLStreamException {
        return getInputFactory().createXMLStreamReader(xml);
    }

    protected XMLStreamReader createParser(final Source source) throws XMLStreamException {
        return getInputFactory().createXMLStreamReader(source);
    }

    /**
     * @since 1.4
     */
    protected XMLInputFactory createInputFactory() {
        return XMLInputFactory.newInstance();
    }

    /**
     * @since 1.4
     */
    protected XMLOutputFactory createOutputFactory() {
        return XMLOutputFactory.newInstance();
    }
}
