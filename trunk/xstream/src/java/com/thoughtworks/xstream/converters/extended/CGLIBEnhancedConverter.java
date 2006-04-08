package com.thoughtworks.xstream.converters.extended;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.CGLIBMapper;
import com.thoughtworks.xstream.mapper.Mapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;


/**
 * Converts a proxy created by the CGLIB {@link Enhancer}. Such a proxy is recreated while
 * deserializing the proxy. The converter does only work, if<br>
 * <ul>
 * <li>the DefaultNamingPolicy is used for the proxy's name</li>
 * <li>only one CAllback is registered</li>
 * <li>a possible super class has at least a protected default constructor</li>
 * </ul>
 * Note, that the this converter relies on the CGLIBMapper.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.2
 */
public class CGLIBEnhancedConverter implements Converter {

    // An alternative implementation is possible by using Enhancer.setCallbackType and 
    // Enhancer.createClass().
    // In this case the converter must be deived from the AbstractReflectionConverter,
    // the proxy info must be written/read in a separate structure first, then the
    // Enhancer must create the type and the functionality of the ReflectionConveter
    // must be used to create the instance. But let's see user feedback first.
    // No support for multiple callbacks though ...
    
    private static String DEFAULT_NAMING_MARKER = "$$EnhancerByCGLIB$$";
    private static String CALLBACK_MARKER = "CGLIB$CALLBACK_";
    private transient Map fieldCache;
    private final Mapper mapper;

    public CGLIBEnhancedConverter(Mapper mapper) {
        this.mapper = mapper;
        this.fieldCache = new HashMap();
    }

    public boolean canConvert(Class type) {
        return (Enhancer.isEnhanced(type) && type.getName().indexOf(DEFAULT_NAMING_MARKER) > 0)
                || type == CGLIBMapper.Marker.class;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Class type = source.getClass();
        boolean hasFactory = Factory.class.isAssignableFrom(type);
        writer.startNode("type");
        context.convertAnother(type.getSuperclass());
        writer.endNode();
        writer.startNode("interfaces");
        Class[] interfaces = type.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i] == Factory.class) {
                continue;
            }
            writer.startNode(mapper.serializedClass(interfaces[i].getClass()));
            context.convertAnother(interfaces[i]);
            writer.endNode();
        }
        writer.endNode();
        writer.startNode("hasFactory");
        writer.setValue(String.valueOf(hasFactory && type.getSuperclass() != Object.class));
        writer.endNode();
        Callback[] callbacks = hasFactory ? ((Factory)source).getCallbacks() : getCallbacks(source);
        if (callbacks.length > 1) {
            throw new ConversionException("Cannot handle CGLIB enhanced proxies with multiple callbacks");
        }
        writer.startNode(mapper.serializedClass(callbacks[0].getClass()));
        context.convertAnother(callbacks[0]);
        writer.endNode();
        try {
            final Field field = type.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            long serialVersionUID = field.getLong(null);
            writer.startNode("serialVersionUID");
            writer.setValue(String.valueOf(serialVersionUID));
            writer.endNode();
        } catch (NoSuchFieldException e) {
            // OK, ignore
        } catch (IllegalAccessException e) {
            // OK, ignore
        }
    }

    private Callback[] getCallbacks(Object source) {
        Class type = source.getClass();
        List fields = (List)fieldCache.get(type.getName());
        if (fields == null) {
            fields = new ArrayList();
            fieldCache.put(type.getName(), fields);
            for (int i = 0; true; ++i) {
                try {
                    Field field = type.getDeclaredField(CALLBACK_MARKER + i);
                    field.setAccessible(true);
                    fields.add(field);
                } catch (NoSuchFieldException e) {
                    break;
                }
            }
        }
        List list = new ArrayList();
        for (int i = 0; i < fields.size(); ++i) {
            try {
                Field field = (Field)fields.get(i);
                list.add(field.get(source));
            } catch (IllegalAccessException e) {
                throw new ObjectAccessException("Access to "
                        + type.getName()
                        + "."
                        + CALLBACK_MARKER
                        + i
                        + " not allowed");
            }
        }
        return (Callback[])list.toArray(new Callback[list.size()]);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final Enhancer enhancer = new Enhancer();
        reader.moveDown();
        enhancer.setSuperclass((Class)context.convertAnother(null, Class.class));
        reader.moveUp();
        reader.moveDown();
        List interfaces = new ArrayList();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            interfaces.add(context.convertAnother(null, mapper.realClass(reader.getNodeName())));
            reader.moveUp();
        }
        enhancer.setInterfaces((Class[])interfaces.toArray(new Class[interfaces.size()]));
        reader.moveUp();
        reader.moveDown();
        enhancer.setUseFactory(Boolean.getBoolean(reader.getValue()));
        reader.moveUp();
        reader.moveDown();
        enhancer.setCallback((Callback)context.convertAnother(null, mapper.realClass(reader.getNodeName())));
        reader.moveUp();
        if (reader.hasMoreChildren()) {
            reader.moveDown();
            enhancer.setSerialVersionUID(Long.valueOf(reader.getValue()));
            reader.moveUp();
        }
        return enhancer.create();
    }

    private Object readResolve() {
        fieldCache = new HashMap();
        return this;
    }
}
