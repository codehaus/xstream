package com.thoughtworks.xstream.converters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConversionException extends RuntimeException implements ErrorWriter {

    private Map stuff = new HashMap();

    public ConversionException(String msg, Exception cause) {
        super(msg, cause);
        if (cause != null) {
            add("exception", cause.getClass().getName());
            add("message", cause.getMessage());
        }
    }

    public ConversionException(String msg) {
        super(msg, null);
    }

    public ConversionException(Exception cause) {
        this(null, cause);
    }

    public String get(String errorKey) {
        return (String) stuff.get(errorKey);
    }

    public void add(String name, String information) {
        stuff.put(name, information);
    }

    public Iterator keys() {
        return stuff.keySet().iterator();
    }

    public String getMessage() {
        StringBuffer result = new StringBuffer();
        if (super.getMessage() != null) {
            result.append(super.getMessage());
        }
        result.append("\n---- Debugging information ----");
        for (Iterator iterator = keys(); iterator.hasNext();) {
            String k = (String) iterator.next();
            String v = get(k);
            result.append("\n" + k);
            int padding = 20 - k.length();
            for (int i = 0; i < padding; i++) {
                result.append(' ');
            }
            result.append(": " + v + " ");
        }
        result.append("\n-------------------------------");
        return result.toString();
    }

}
