package com.thoughtworks.xstream.converters.reflection;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.util.Iterator;

public class FieldDictionaryTest extends TestCase {

    private FieldDictionary fieldDictionary;

    protected void setUp() throws Exception {
        super.setUp();
        fieldDictionary = new FieldDictionary();
    }

    static class SomeClass {
        private String a;
        private String c;
        private String b;
    }

    public void testListsFieldsInClassInDefinitionOrder() {
        Iterator fields = fieldDictionary.serializableFieldsFor(SomeClass.class);
        assertEquals("a", ((Field)fields.next()).getName());
        assertEquals("c", ((Field)fields.next()).getName());
        assertEquals("b", ((Field)fields.next()).getName());
        assertFalse("No more fields should be present", fields.hasNext());
    }

    static class SpecialClass extends SomeClass {
        private String brilliant;
    }

    public void testIncludesFieldsInSuperClasses() {
        Iterator fields = fieldDictionary.serializableFieldsFor(SpecialClass.class);
        assertEquals("brilliant", ((Field)fields.next()).getName());
        assertEquals("a", ((Field)fields.next()).getName());
        assertEquals("c", ((Field)fields.next()).getName());
        assertEquals("b", ((Field)fields.next()).getName());
        assertFalse("No more fields should be present", fields.hasNext());
    }

    class InnerClass { // note: no static makes this an inner class, not nested class.
        private String someThing;
    }

    public void testIncludesOuterClassReferenceForInnerClass() {
        Iterator fields = fieldDictionary.serializableFieldsFor(InnerClass.class);
        assertEquals("someThing", ((Field)fields.next()).getName());
        Field innerField = ((Field)fields.next());
        assertEquals("this$0", innerField.getName());
        assertEquals(FieldDictionaryTest.class, innerField.getType());
        assertFalse("No more fields should be present", fields.hasNext());
    }
}
