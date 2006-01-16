package com.thoughtworks.acceptance;

import com.thoughtworks.acceptance.someobjects.X;
import com.thoughtworks.xstream.alias.CannotResolveClassException;

/**
 * @author Paul Hammant
 */
public class AliasTest extends AbstractAcceptanceTest {

    public void testBarfsIfAliasDoesNotExist() {

        String xml = "" +
                "<X-array>\n" +
                "  <X>\n" +
                "    <anInt>0</anInt>\n" +
                "  </X>\n" +
                "</X-array>";

        // now change the alias
        xstream.alias("Xxxxxxxx", X.class);
        try {
            xstream.fromXML(xml);
            fail("ShouldCannotResolveClassException expected");
        } catch (CannotResolveClassException expectedException) {
            // expected
        }
    }

    public void testAliasWithUnderscore() {
        String xml = "" +
                "<X_alias>\n" +
                "  <anInt>0</anInt>\n" +
                "</X_alias>";

        // now change the alias
        xstream.alias("X_alias", X.class);
        X x = new X(0);
        try {
            assertBothWays(x, xml);
            fail("CannotResolveClassException expected");
        } catch (CannotResolveClassException e) {
            //expected - marshalling works but not unmarshalling
        }
    }

}
