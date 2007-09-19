package com.thoughtworks.xstream.core.util;

import java.beans.PropertyEditor;

/**
 * Wrapper around {@link PropertyEditor} that can
 * be called by multiple threads concurrently.
 * <p>A PropertyEditor is not thread safe. To make best 
 * use of resources, the PropertyEditor provides a dynamically
 * sizing pool of instances, each of which will only
 * be called by a single thread at a time.</p>
 * <p>The pool has a maximum capacity, to limit overhead.
 * If all instances in the pool are in use and another is
 * required, it shall block until one becomes available.</p>
 *
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public class ThreadSafePropertyEditor {

    private final Class editorType;
    private final Pool pool;

    public ThreadSafePropertyEditor(Class type, int initialPoolSize, int maxPoolSize) {
        if (!PropertyEditor.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName() + " is not a " + PropertyEditor.class.getName());
        }
        editorType = type;
        pool = new Pool(initialPoolSize, maxPoolSize, new Pool.Factory() {
            public Object newInstance() {
                try {
                    return editorType.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
        });
    }
    
    public String getAsText(Object object) {
        PropertyEditor editor = fetchFromPool();
        try {
            editor.setValue(object);
            return editor.getAsText();
        } finally {
            pool.putInPool(editor);
        }
    }

    public Object setAsText(String str) {
        PropertyEditor editor = fetchFromPool();
        try {
            editor.setAsText(str);
            return editor.getValue();
        } finally {
            pool.putInPool(editor);
        }
    }

    private PropertyEditor fetchFromPool() {
        PropertyEditor editor = (PropertyEditor)pool.fetchFromPool();
        return editor;
    }
}
