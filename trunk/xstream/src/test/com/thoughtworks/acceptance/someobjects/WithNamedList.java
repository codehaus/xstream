/*
 * Copyright (C) 2006 J�rg Schaible
 * Created on 15.03.2006 by J�rg Schaible
 */
package com.thoughtworks.acceptance.someobjects;


public class WithNamedList extends WithList {
    private String name;

    public WithNamedList(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}