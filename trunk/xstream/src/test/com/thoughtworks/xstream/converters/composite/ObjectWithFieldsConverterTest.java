package com.thoughtworks.xstream.converters.composite;

import com.thoughtworks.xstream.XStream;
import junit.framework.TestCase;

/**
 * [Marcos Tarruella, tarruella@email.com] Joe, could you please send me the link to the code
 * so I may have a chance to have another look at it.
 * Cheers!
 *
 * P.S. I was about to use xmlUnit but... a bit dipsy and decided not to (perhaps next GeekNight :-)
 */
public class ObjectWithFieldsConverterTest extends TestCase {

    public class World {
        int anInt = 1;
        Integer anInteger = new Integer(2);
        char anChar = 'a';
        Character anCharacter = new Character('w');
        boolean anBool = true;
        Boolean anBoolean = new Boolean(false);
        byte aByte = 4;
        Byte aByteClass = new Byte("5");
        short aShort = 6;
        Short aShortClass = new Short("7");
        float aFloat = 8f;
        Float aFloatClass = new Float("9");
        long aLong = 10;
        Long aLongClass = new Long("11");
        String anString = new String("XStream programming!");
    }

    public void testPrimitiveTypes() {
        World world = new World();

        XStream xstream = new XStream();
        xstream.alias("world", World.class);

        String expected =
                "<world>\n" +
                "  <anInt>1</anInt>\n" +
                "  <anInteger>2</anInteger>\n" +
                "  <anChar>a</anChar>\n" +
                "  <anCharacter>w</anCharacter>\n" +
                "  <anBool>true</anBool>\n" +
                "  <anBoolean>false</anBoolean>\n" +
                "  <aByte>4</aByte>\n" +
                "  <aByteClass>5</aByteClass>\n" +
                "  <aShort>6</aShort>\n" +
                "  <aShortClass>7</aShortClass>\n" +
                "  <aFloat>8.0</aFloat>\n" +
                "  <aFloatClass>9.0</aFloatClass>\n" +
                "  <aLong>10</aLong>\n" +
                "  <aLongClass>11</aLongClass>\n" +
                "  <anString>XStream programming!</anString>\n" +
                "</world>";

        assertEquals(expected, xstream.toXML(world));
    }
}
