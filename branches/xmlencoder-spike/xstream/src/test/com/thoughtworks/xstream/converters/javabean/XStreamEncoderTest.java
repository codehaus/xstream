package com.thoughtworks.xstream.converters.javabean;

import junit.framework.TestCase;

import javax.swing.*;
import java.beans.*;
import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.io.StringWriter;
import java.awt.*;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.Mapper;

public class XStreamEncoderTest extends TestCase {

    JComponent input;

    protected void setUp() throws Exception {
        super.setUp();
        JTable table = new JTable();
        table.setToolTipText("tooltip!");
        table.setShowGrid(false);
        table.setRowMargin(table.getRowMargin());
        table.setSize(new Dimension(10, 20));
        JButton button = new JButton("hi");
        button.setForeground(Color.GREEN);
        button.setToolTipText("button tooltip");
        button.setEnabled(false);
        BorderLayout layout = new BorderLayout();
        layout.setHgap(22);
        input = new JPanel(layout);
        input.add(table, BorderLayout.CENTER);
        input.add(button, BorderLayout.SOUTH);
    }

    public void test() {
        Writer xx = new StringWriter();
        XEncoder xmlEncoder = new XEncoder(new PrettyPrintWriter(xx));
        xmlEncoder.writeObject(input);

        System.out.println(xx);
    }

    public void test2() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        XMLEncoder xmlEncoder = new XMLEncoder(buffer);
        xmlEncoder.writeObject(input);
        xmlEncoder.close();

        System.out.println(new String(buffer.toByteArray()));
    }

    public void test3() {
        Writer xx = new StringWriter();
        XEncoder xmlEncoder = new XEncoder(new PrettyPrintWriter(xx));
        DefaultPersistenceDelegate delegate = new DefaultPersistenceDelegate();
        delegate.writeObject(input, xmlEncoder);

        System.out.println(xx);
    }

    public void test4() {
        XStream xstream = new XStream();
        xstream.registerConverter(new XConverter(xstream.getClassMapper()), -10);

        System.out.println(xstream.toXML(input));
    }
}

class XConverter implements Converter {

    private Mapper mapper;

    public XConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    public boolean canConvert(Class type) {
        return true;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        CustomEncoder encoder = new CustomEncoder(writer, context);
        PersistenceDelegate delegate = encoder.getPersistenceDelegate(source.getClass());
        delegate.writeObject(source, encoder);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        throw new UnsupportedOperationException();
    }

    class CustomEncoder extends Encoder {

        private HierarchicalStreamWriter writer;
        private MarshallingContext context;

        public CustomEncoder(HierarchicalStreamWriter writer, MarshallingContext context) {
            this.writer = writer;
            this.context = context;
        }

        public void writeStatement(Statement oldStm) {
            writer.startNode(oldStm.getMethodName());
            Object[] arguments = oldStm.getArguments();
            if (arguments.length == 1) {
                writer.addAttribute("class", mapper.serializedClass(arguments[0].getClass()));
                try {
                    context.convertAnother(arguments[0]);
                } catch (Exception e) {
                //    writer.setValue(e.toString());
                }
            } else {
                writer.addAttribute("multiple", "true");
                for (int i = 0; i < arguments.length; i++) {
                    if (arguments[i] == null) {
                        writer.startNode("null");
                        writer.endNode();
                    } else {
                        writer.startNode(mapper.serializedClass(arguments[i].getClass()));
                        try {
                            context.convertAnother(arguments[i]);
                        } catch (Exception e) {
                  //          writer.setValue(e.toString());
                        }
                        writer.endNode();
                    }
                }
            }
            writer.endNode();
        }
    }
}
class XEncoder extends java.beans.Encoder {

    HierarchicalStreamWriter writer;

    public XEncoder(HierarchicalStreamWriter writer) {
        this.writer = writer;
        push(false);
    }
    boolean dump = false;
    boolean[] stack = new boolean[32];
    int pointer = -1;

    StringBuffer indent = new StringBuffer();

    private void push(boolean v) {
        pointer++;
        stack[pointer] = v;
    }

    private void pop() {
        pointer--;
    }

    private boolean peek() {
        return stack[pointer];
    }

    public void writeObject(Object o) {
        if (dump) System.out.println(indent + "writeObject(" + o + ")");
        indent.append("  ");

        super.writeObject(o);
        indent.setLength(indent.length() - 2);
    }

    public void setExceptionListener(ExceptionListener exceptionListener) {
        if (dump) System.out.println(indent + "setExceptionListener");
        indent.append("  ");
        super.setExceptionListener(exceptionListener);
        indent.setLength(indent.length() - 2);
    }

    public ExceptionListener getExceptionListener() {
        if (dump) System.out.println(indent + "getExceptionListener");
        indent.append("  ");
        ExceptionListener o = super.getExceptionListener();
        indent.setLength(indent.length() - 2);
        if (dump) System.out.println(indent + "(return: " + o + ")");
        return o;
    }

    public PersistenceDelegate getPersistenceDelegate(Class<?> type) {
        if (dump) System.out.println(indent + "getPersistenceDelegate(" + type + ")");
        indent.append("  ");
        PersistenceDelegate o = super.getPersistenceDelegate(type);
        indent.setLength(indent.length() - 2);
        if (dump) System.out.println(indent + "(return: " + o + ")");
        return o;
    }

    public void setPersistenceDelegate(Class<?> type, PersistenceDelegate persistenceDelegate) {
        if (dump) System.out.println(indent + "getPersistenceDelegate(" + type.getName() + ")");
        indent.append("  ");
        super.setPersistenceDelegate(type, persistenceDelegate);
        indent.setLength(indent.length() - 2);
    }

    public Object remove(Object oldInstance) {
        if (dump) System.out.println(indent + "remove(" + oldInstance + ")");
        indent.append("  ");
        Object o = super.remove(oldInstance);
        indent.setLength(indent.length() - 2);
        if (dump) System.out.println(indent + "(return: " + o + ")");
        return o;
    }

    public Object get(Object oldInstance) {
        if (dump) System.out.println(indent + "get(" + oldInstance + ")");
        indent.append("  ");
        Object o = super.get(oldInstance);
        indent.setLength(indent.length() - 2);
        if (dump) System.out.println(indent + "(return: " + o + ")");
        return o;
    }


    public void writeStatement(Statement oldStm) {
        push(true);
        writer.startNode("statement");
        writer.addAttribute("method-name", oldStm.getMethodName());
        writer.addAttribute("target", String.valueOf(oldStm.getTarget().getClass().getName()));
        Object[] arguments = oldStm.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            writer.startNode("attribute");
            writer.setValue(String.valueOf(arguments[i]));
            writer.endNode();
        }

        if (dump) System.out.println(indent + "writeStatment(" + oldStm + ")");
        indent.append("  ");
        super.writeStatement(oldStm);
        indent.setLength(indent.length() - 2);
        writer.endNode();
        pop();
    }

    public void writeExpression(Expression oldExp) {
        boolean show = true;
        if (show) {
            writer.startNode("expression");
            writer.addAttribute("method-name", oldExp.getMethodName());
            try {
                Object value = oldExp.getValue();
                writer.addAttribute("value", String.valueOf(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
            writer.addAttribute("target", oldExp.getTarget().getClass().getName());
            Object[] arguments = oldExp.getArguments();
            for (int i = 0; i < arguments.length; i++) {
                writer.startNode("attribute");
                writer.setValue(String.valueOf(arguments[i]));
                writer.endNode();
            }
            if (dump) System.out.println(indent + "writeExpression(" + oldExp + ")");
            indent.append("  ");
        }
        super.writeExpression(oldExp);
        if (show) {
            indent.setLength(indent.length() - 2);
            writer.endNode();
        }
    }
}