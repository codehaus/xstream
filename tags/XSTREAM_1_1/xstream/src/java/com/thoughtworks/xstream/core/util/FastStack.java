package com.thoughtworks.xstream.core.util;

public final class FastStack {

    private Object[] stack;
    private int pointer;

    public FastStack(int initialCapacity) {
        stack = new Object[initialCapacity];
    }

    public void push(Object value) {
        if (pointer + 1 >= stack.length) {
            resizeStack(stack.length * 2);
        }
        stack[pointer++] = value;
    }

    public void popSilently() {
        pointer--;
    }

    public Object pop() {
        return stack[--pointer];
    }

    public Object peek() {
        return pointer == 0 ? null : stack[pointer - 1];
    }

    public int size() {
        return pointer;
    }

    public Object get(int i) {
        return stack[i];
    }

    private void resizeStack(int newCapacity) {
        Object[] newStack = new Object[newCapacity];
        System.arraycopy(stack, 0, newStack, 0, Math.min(stack.length, newCapacity));
        stack = newStack;
    }

}
