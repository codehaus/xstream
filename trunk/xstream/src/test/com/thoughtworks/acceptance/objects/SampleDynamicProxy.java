/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 25. April 2004 by Joe Walnes
 */
package com.thoughtworks.acceptance.objects;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SampleDynamicProxy implements InvocationHandler {

    private String aField = "hello";

    public static interface InterfaceOne {
        String doSomething();
    }

    public static interface InterfaceTwo {
        String doSomething();
    }

    public static Object newInstance() {
        return Proxy.newProxyInstance(InterfaceOne.class.getClassLoader(),
                new Class[]{InterfaceOne.class, InterfaceTwo.class},
                new SampleDynamicProxy());
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("equals")) {
            return equals(args[0]) ? Boolean.TRUE : Boolean.FALSE;
        } else if (method.getName().equals("hashCode")) {
            return new Integer(System.identityHashCode(proxy));
        } else {
            return aField;
        }
    }

    public boolean equals(Object obj) {
        return equalsInterfaceOne(obj) && equalsInterfaceTwo(obj);
    }

    private boolean equalsInterfaceOne(Object o) {
        if (o instanceof InterfaceOne) {
            InterfaceOne interfaceOne = (InterfaceOne) o;
            return aField.equals(interfaceOne.doSomething());
        } else {
            return false;
        }
    }

    private boolean equalsInterfaceTwo(Object o) {
        if (o instanceof InterfaceTwo) {
            InterfaceTwo interfaceTwo = (InterfaceTwo) o;
            return aField.equals(interfaceTwo.doSomething());
        } else {
            return false;
        }
    }

}
