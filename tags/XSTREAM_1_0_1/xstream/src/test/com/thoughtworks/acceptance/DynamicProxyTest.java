package com.thoughtworks.acceptance;

import com.thoughtworks.acceptance.objects.SampleDynamicProxy;
import com.thoughtworks.xstream.converters.extended.DynamicProxyConverter;

public class DynamicProxyTest extends AbstractAcceptanceTest {

    public void testDynamicProxy() {
        xstream.registerConverter(new DynamicProxyConverter(xstream.getClassMapper()));

        assertBothWays(SampleDynamicProxy.newInstance(),
                "<dynamic-proxy>\n" +
                "  <interface>com.thoughtworks.acceptance.objects.SampleDynamicProxy-InterfaceOne</interface>\n" +
                "  <interface>com.thoughtworks.acceptance.objects.SampleDynamicProxy-InterfaceTwo</interface>\n" +
                "  <handler class=\"com.thoughtworks.acceptance.objects.SampleDynamicProxy\">\n" +
                "    <aField>hello</aField>\n" +
                "  </handler>\n" +
                "</dynamic-proxy>");
    }

}