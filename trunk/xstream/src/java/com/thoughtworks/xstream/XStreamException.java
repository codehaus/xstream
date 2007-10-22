/*
 * Copyright (C) 2007 XStream committers.
 * Created on 22.10.2007 by Joerg Schaible
 */
package com.thoughtworks.xstream;

import com.thoughtworks.xstream.core.BaseException;


/**
 * Base exception for all thrown exceptions with XStream. JDK 1.3 friendly cause handling.
 * 
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public class XStreamException extends BaseException {

    private Throwable cause;

    /**
     * Default constructor.
     * 
     * @since upcoming
     */
    protected XStreamException() {
        this("", null);
    }

    /**
     * Constructs an XStreamException with a message.
     * 
     * @param message
     * @since upcoming
     */
    public XStreamException(String message) {
        this(message, null);
    }

    /**
     * Constructs an XStreamException as wrapper for a different causing {@link Throwable}.
     * 
     * @param cause
     * @since upcoming
     */
    public XStreamException(Throwable cause) {
        this("", cause);
    }

    /**
     * Constructs an XStreamException with a message as wrapper for a different causing
     * {@link Throwable}.
     * 
     * @param message
     * @param cause
     * @since upcoming
     */
    public XStreamException(String message, Throwable cause) {
        super(message + (cause == null ? "" : " : " + cause.getMessage()));
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

}
