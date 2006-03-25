package com.thoughtworks.acceptance;

import com.thoughtworks.acceptance.objects.Software;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.testutil.DynamicSecurityManager;

import junit.framework.TestCase;

import java.io.File;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.CodeSource;
import java.security.Policy;


/**
 * Test XStream with an active SecurityManager. Note, that it is intentional, that this test is not
 * derived from AbstractAcceptanceTest to avoid loaded classes before the SecurityManager is in
 * action.
 * 
 * @author J&ouml;rg Schaible
 */
public class SecurityManagerTest extends TestCase {

    private XStream xstream;
    private DynamicSecurityManager securityManager;
    private CodeSource defaultCodeSource;
    private File mainClasses;
    private File testClasses;
    private File libs;

    protected void setUp() throws Exception {
        super.setUp();
        System.setSecurityManager(null);
        defaultCodeSource = new CodeSource(null, null);
        mainClasses = new File(
                new File(new File(System.getProperty("user.dir"), "build"), "java"), "-");
        testClasses = new File(
                new File(new File(System.getProperty("user.dir"), "build"), "test"), "-");
        libs = new File(new File(System.getProperty("user.dir"), "lib"), "*");
        securityManager = new DynamicSecurityManager();
        Policy policy = Policy.getPolicy();
        securityManager.setPermissions(defaultCodeSource, policy.getPermissions(defaultCodeSource));
        securityManager.addPermission(
                defaultCodeSource, new RuntimePermission("setSecurityManager"));
    }

    protected void tearDown() throws Exception {
        System.setSecurityManager(null);
        super.tearDown();
    }

    public void testSerializeWithXpp3DriverAndSun14ReflectionProviderAndActiveSecurityManager() {
        securityManager.addPermission(defaultCodeSource, new FilePermission(
                mainClasses.toString(), "read"));
        securityManager.addPermission(defaultCodeSource, new FilePermission(
                testClasses.toString(), "read"));
        securityManager.addPermission(
                defaultCodeSource, new FilePermission(libs.toString(), "read"));
        securityManager.addPermission(defaultCodeSource, new RuntimePermission(
                "accessDeclaredMembers"));
        securityManager.addPermission(defaultCodeSource, new RuntimePermission(
                "accessClassInPackage.sun.reflect"));
        securityManager.addPermission(defaultCodeSource, new RuntimePermission(
                "accessClassInPackage.sun.misc"));
        securityManager
                .addPermission(defaultCodeSource, new RuntimePermission("createClassLoader"));
        securityManager.addPermission(defaultCodeSource, new RuntimePermission(
                "reflectionFactoryAccess"));
        securityManager.addPermission(defaultCodeSource, new ReflectPermission(
                "suppressAccessChecks"));
        securityManager.setReadOnly();
        System.setSecurityManager(securityManager);

        xstream = new XStream(new Sun14ReflectionProvider());

        assertBothWays();
    }

    public void testSerializeWithXpp3DriverAndPureJavaReflectionProviderAndActiveSecurityManager() {
        securityManager.addPermission(defaultCodeSource, new FilePermission(
                mainClasses.toString(), "read"));
        securityManager.addPermission(defaultCodeSource, new FilePermission(
                testClasses.toString(), "read"));
        securityManager.addPermission(
                defaultCodeSource, new FilePermission(libs.toString(), "read"));
        securityManager.addPermission(defaultCodeSource, new RuntimePermission(
                "accessDeclaredMembers"));
        securityManager
                .addPermission(defaultCodeSource, new RuntimePermission("createClassLoader"));
        securityManager.addPermission(defaultCodeSource, new ReflectPermission(
                "suppressAccessChecks"));
        securityManager.setReadOnly();
        System.setSecurityManager(securityManager);

        xstream = new XStream(new PureJavaReflectionProvider());

        assertBothWays();
    }

    public void testSerializeWithDomDriverAndPureJavaReflectionProviderAndActiveSecurityManager() {
        securityManager.addPermission(defaultCodeSource, new FilePermission(
                mainClasses.toString(), "read"));
        securityManager.addPermission(defaultCodeSource, new FilePermission(
                testClasses.toString(), "read"));
        securityManager.addPermission(
                defaultCodeSource, new FilePermission(libs.toString(), "read"));
        securityManager.addPermission(defaultCodeSource, new RuntimePermission(
                "accessDeclaredMembers"));
        securityManager
                .addPermission(defaultCodeSource, new RuntimePermission("createClassLoader"));
        securityManager.addPermission(defaultCodeSource, new ReflectPermission(
                "suppressAccessChecks"));
        securityManager.setReadOnly();
        System.setSecurityManager(securityManager);

        // uses implicit PureJavaReflectionProvider, since Sun14ReflectionProvider cannot be loaded
        xstream = new XStream(new DomDriver());

        assertBothWays();
    }

    private void assertBothWays() {

        xstream.alias("software", Software.class);

        final Software sw = new Software("jw", "xstr");
        final String xml = "<software>\n"
                + "  <vendor>jw</vendor>\n"
                + "  <name>xstr</name>\n"
                + "</software>";

        String resultXml = xstream.toXML(sw);
        assertEquals(xml, resultXml);
        Object resultRoot = xstream.fromXML(resultXml);
        if (!sw.equals(resultRoot)) {
            assertEquals("Object deserialization failed", "DESERIALIZED OBJECT\n"
                    + xstream.toXML(sw), "DESERIALIZED OBJECT\n" + xstream.toXML(resultRoot));
        }
    }
}
