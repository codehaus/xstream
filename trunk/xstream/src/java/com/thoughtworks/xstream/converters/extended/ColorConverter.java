package com.thoughtworks.xstream.converters.extended;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorConverter implements Converter {

    public boolean canConvert(Class type) {
        return type.equals(Color.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Color color = (Color) source;
        write("red", color.getRed(), writer);
        write("green", color.getGreen(), writer);
        write("blue", color.getBlue(), writer);
        write("alpha", color.getAlpha(), writer);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map elements = new HashMap();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            elements.put(reader.getNodeName(), Integer.valueOf(reader.getValue()));
            reader.moveUp();
        }
        Color color = new Color(
                ((Integer) elements.get("red")).intValue(),
                ((Integer) elements.get("green")).intValue(),
                ((Integer) elements.get("blue")).intValue(),
                ((Integer) elements.get("alpha")).intValue()
        );
        return color;
    }

    private void write(String fieldName, int value, HierarchicalStreamWriter writer) {
        writer.startNode(fieldName);
        writer.setValue(String.valueOf(value));
        writer.endNode();
    }

}
