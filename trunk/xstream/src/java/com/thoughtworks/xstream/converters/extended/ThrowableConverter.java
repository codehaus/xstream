package com.thoughtworks.xstream.converters.extended;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for Throwable (and Exception) that retains stack trace, for JDK1.4 only.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley (binkley)</a>
 * @author Joe Walnes
 */
public class ThrowableConverter implements Converter {
    
    private Converter defaultConverter;

    public ThrowableConverter(Converter defaultConverter) {
        this.defaultConverter = defaultConverter;
    }

    public boolean canConvert(final Class type) {
        return Throwable.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Throwable throwable = (Throwable) source;
        if (throwable.getCause() == null) {
            try {
                throwable.initCause(null);
            } catch (IllegalArgumentException e) {
                // ignore, initCause failed, cause was already set
            }
        }
        throwable.getStackTrace(); // Force stackTrace field to be lazy loaded by special JVM native witchcraft (outside our control).
        defaultConverter.marshal(throwable, writer, context);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return defaultConverter.unmarshal(reader, context);
    }
}
