/*
 * Copyright (C) 2007 XStream Committers.
 * Created on 06.11.2007 by Joerg Schaible
 */
package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;


/**
 * @author J&ouml;rg Schaible
 */
public class LocalConverterTest extends AbstractAcceptanceTest {

    public static class MultiBoolean {
        private boolean bool;
        private boolean speech;
        private boolean bit;

        private MultiBoolean() {
            this(false, false, false);
        }

        public MultiBoolean(boolean bool, boolean speech, boolean bit) {
            this.bool = bool;
            this.speech = speech;
            this.bit = bit;
        }

    }

    protected void setUp() throws Exception {
        super.setUp();
        xstream.alias("mbool", MultiBoolean.class);
        xstream.registerConverter(new ReflectionConverter(
            xstream.getMapper(), new PureJavaReflectionProvider()), XStream.PRIORITY_VERY_LOW);
    }

    public void testCanBeAppliedToIndividualFields() {
        MultiBoolean multiBool = new MultiBoolean(true, true, true);
        String xml = ""
            + "<mbool>\n"
            + "  <bool>true</bool>\n"
            + "  <speech>yes</speech>\n"
            + "  <bit>1</bit>\n"
            + "</mbool>";

        xstream.registerLocalConverter(MultiBoolean.class, "speech", BooleanConverter.YES_NO);
        xstream.registerLocalConverter(MultiBoolean.class, "bit", BooleanConverter.BINARY);
        assertBothWays(multiBool, xml);
    }
}
