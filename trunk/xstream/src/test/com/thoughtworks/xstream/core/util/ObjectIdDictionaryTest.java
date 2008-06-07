/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 30. May 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.core.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;


public class ObjectIdDictionaryTest extends TestCase {

    public void testMapsIdsToObjectReferences() {
        ObjectIdDictionary dict = new ObjectIdDictionary();
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        dict.associateId(a, "id a");
        dict.associateId(b, "id b");
        dict.associateId(c, "id c");
        assertEquals("id a", dict.lookupId(a));
        assertEquals("id b", dict.lookupId(b));
        assertEquals("id c", dict.lookupId(c));
    }

    public void testTreatsObjectsThatAreEqualButNotSameInstanceAsDifferentReference() {
        ObjectIdDictionary dict = new ObjectIdDictionary();
        Integer a = new Integer(3);
        Integer b = new Integer(3);
        dict.associateId(a, "id a");
        dict.associateId(b, "id b");
        assertEquals("id a", dict.lookupId(a));
        assertEquals("id b", dict.lookupId(b));
    }

    public void testEnforceSameSystemHashCodeForGCedObjects() {
        StringBuffer memInfo = new StringBuffer("MemoryInfo:\n");
        memInfo.append(memoryInfo());
        memInfo.append('\n');
        
        // create 100000 Strings and call GC after creation of 10000
        final int loop = 10;
        final int elements = 10000;
        final int[] dictSizes = new int[loop * elements];
        
        // create memory shortage to force gc 
        List blockList = new ArrayList();
        while (true) {
            try {
                blockList.add(new byte[1024 * 1024]);
            } catch(OutOfMemoryError error) {
                break;
            }
        }
        
        // free some blocks again
        for (int i = 0; i < 5; i++ ) {
            blockList.remove(0);
        }

        // run test with memory shortage
        ObjectIdDictionary dict = new ObjectIdDictionary();
        for (int i = 0; i < loop; ++i) {
            System.gc();
            System.runFinalization();
            memInfo.append(memoryInfo());
            memInfo.append('\n');
            for (int j = 0; j < elements; ++j) {
                int count = i * elements + j;
                final String s = new String("JUnit ") + count; // enforce new object
                dictSizes[count] = dict.size();
                assertFalse("Failed in (" + i + "/" + j + ")", dict.containsId(s));
                dict.associateId(s, "X");
            }
        }
        memInfo.append(memoryInfo());
        memInfo.append('\n');

        assertFalse("Algorithm did not reach last element", 0 == dictSizes[loop * elements - 1]);
        assertFalse("Dictionary did not shrink\n" + memInfo, loop * elements - 1 == dictSizes[loop * elements - 1]);
        
        blockList.clear(); // prevents compiler optimization
    }
    
    private String memoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        StringBuffer buffer = new StringBuffer("Memory: ");
        buffer.append(runtime.freeMemory());
        buffer.append(" free / ");
        buffer.append(runtime.maxMemory());
        buffer.append(" max / ");
        buffer.append(runtime.totalMemory());
        buffer.append(" total");
        return buffer.toString();
    }
}
