package com.thoughtworks.acceptance.objects;


public class Hardware extends StandardObject {
    public String arch;
    public String name;

    public Hardware() {
    }

    public Hardware(String arch, String name) {
        this.arch = arch;
        this.name = name;
    }

}
