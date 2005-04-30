package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.testutil.CallLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationCallbackOrderTest extends AbstractAcceptanceTest {

    // static so it can be accessed by objects under test, without them needing a reference back to the testcase
    private static CallLog log = new CallLog();

    // --- Sample class hiearchy

    public static class Base implements Serializable{

        private void writeObject(ObjectOutputStream out) throws IOException {
            log.actual("Base.writeObject() start");
            out.defaultWriteObject();
            log.actual("Base.writeObject() end");
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            log.actual("Base.readObject() start");
            in.defaultReadObject();
            log.actual("Base.readObject() end");
        }

        private Object writeReplace() {
            log.actual("Base.writeReplace()");
            return this;
        }

        private Object readResolve() {
            log.actual("Base.readResolve()");
            return this;
        }
    }

    public static class Child extends Base implements Serializable{

        private void writeObject(ObjectOutputStream out) throws IOException {
            log.actual("Child.writeObject() start");
            out.defaultWriteObject();
            log.actual("Child.writeObject() end");
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            log.actual("Child.readObject() start");
            in.defaultReadObject();
            log.actual("Child.readObject() end");
        }

        private Object writeReplace() {
            log.actual("Child.writeReplace()");
            return this;
        }

        private Object readResolve() {
            log.actual("Child.readResolve()");
            return this;
        }
    }

    // --- Convenience wrappers around Java Object Serialization

    private byte[] javaSerialize(Object object) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(bytes);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return bytes.toByteArray();
    }

    private Object javaDeserialize(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
        return objectInputStream.readObject();
    }

    // --- Tests

    public void testJavaSerialization() throws IOException {
        // expectations
        log.expect("Child.writeReplace()");
        log.expect("Base.writeObject() start");
        log.expect("Base.writeObject() end");
        log.expect("Child.writeObject() start");
        log.expect("Child.writeObject() end");

        // execute
        javaSerialize(new Child());

        // verify
        log.verify();
    }

    public void testXStreamSerialization() throws IOException {
        // expectations
        log.expect("Child.writeReplace()");
        log.expect("Base.writeObject() start");
        log.expect("Base.writeObject() end");
        log.expect("Child.writeObject() start");
        log.expect("Child.writeObject() end");

        // execute
        xstream.toXML(new Child());

        // verify
        log.verify();
    }

    public void testJavaDeserialization() throws IOException, ClassNotFoundException {
        // setup
        byte[] data = javaSerialize(new Child());
        log.reset();

        // expectations
        log.expect("Base.readObject() start");
        log.expect("Base.readObject() end");
        log.expect("Child.readObject() start");
        log.expect("Child.readObject() end");
        log.expect("Child.readResolve()");

        // execute
        javaDeserialize(data);

        // verify
        log.verify();
    }

    public void testXStreamDeserialization() throws IOException, ClassNotFoundException {
        // setup
        String data = xstream.toXML(new Child());
        log.reset();

        // expectations
        log.expect("Base.readObject() start");
        log.expect("Base.readObject() end");
        log.expect("Child.readObject() start");
        log.expect("Child.readObject() end");
        log.expect("Child.readResolve()");

        // execute
        xstream.fromXML(data);

        // verify
        log.verify();
    }


    public static class ParentNotTransient implements Serializable {

        public int somethingNotTransient;

        public ParentNotTransient(int somethingNotTransient) {
            this.somethingNotTransient = somethingNotTransient;
        }

    }

    public static class ChildWithTransient extends ParentNotTransient implements Serializable {

        public transient int somethingTransient;

        public ChildWithTransient(int somethingNotTransient, int somethingTransient) {
            super(somethingNotTransient);
            this.somethingTransient = somethingTransient;
        }

        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            somethingTransient = 99999;
        }
    }

    public void testCallsReadObjectEvenWithoutNonTransientFields() {
        xstream.alias("parent", ParentNotTransient.class);
        xstream.alias("child", ChildWithTransient.class);

        Object in = new ChildWithTransient(10, 22222);
        String expectedXml = ""
                + "<child serialization=\"custom\">\n"
                + "  <parent>\n"
                + "    <default>\n"
                + "      <somethingNotTransient>10</somethingNotTransient>\n"
                + "    </default>\n"
                + "  </parent>\n"
                + "  <child>\n"
                + "    <default/>\n"
                + "  </child>\n"
                + "</child>";

        String xml = xstream.toXML(in);
        assertEquals(expectedXml, xml);

        ChildWithTransient childWithTransient = (ChildWithTransient) xstream.fromXML(xml);

        assertEquals(10, childWithTransient.somethingNotTransient);
        assertEquals(99999, childWithTransient.somethingTransient);
    }


    public static class SomethingThatValidates implements Serializable {

        private void readObject(ObjectInputStream s) throws IOException {

            final int LOW_PRIORITY = -5;
            final int MEDIUM_PRIORITY = 0;
            final int HIGH_PRIORITY = 5;

            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() {
                    log.actual("validateObject() medium priority 1");
                }
            }, MEDIUM_PRIORITY);

            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() {
                    log.actual("validateObject() high priority");
                }
            }, HIGH_PRIORITY);

            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() {
                    log.actual("validateObject() low priority");
                }
            }, LOW_PRIORITY);

            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() {
                    log.actual("validateObject() medium priority 2");
                }
            }, MEDIUM_PRIORITY);
        }
    }

    public void testJavaSerializationValidatesObjectIsCalledInPriorityOrder() throws IOException, ClassNotFoundException {
        // expect
        log.expect("validateObject() high priority");
        log.expect("validateObject() medium priority 2");
        log.expect("validateObject() medium priority 1");
        log.expect("validateObject() low priority");

        // execute
        javaDeserialize(javaSerialize(new SomethingThatValidates()));

        // verify
        log.verify();
    }

    public void testXStreamSerializationValidatesObjectIsCalledInPriorityOrder() throws IOException, ClassNotFoundException {
        // expect
        log.expect("validateObject() high priority");
        log.expect("validateObject() medium priority 2");
        log.expect("validateObject() medium priority 1");
        log.expect("validateObject() low priority");

        // execute
        xstream.fromXML(xstream.toXML(new SomethingThatValidates()));

        // verify
        log.verify();
    }

}
