package com.thoughtworks.acceptance;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.mapper.Mapper;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class XmlFriendlyTest extends AbstractAcceptanceTest {

    
    protected XStream createXStream() {
        return new XStream(null, (Mapper)null, createDriver(), new XmlFriendlyReplacer("_-", "__", "default"));
    }

    public static class WithDollarCharField extends StandardObject {
        String $field;
        String field$;
        String fi$eld;
        String fi$$eld;
    }

    public void testSupportsFieldsWithDollarChar() {
        xstream.alias("dollar", WithDollarCharField.class);

        WithDollarCharField in = new WithDollarCharField();
        in.$field = "a";
        in.field$ = "b";
        in.fi$eld = "c";
        in.fi$$eld = "d";

        String expected = "" +
                "<dollar>\n" +
                "  <_-field>a</_-field>\n" +
                "  <field_->b</field_->\n" +
                "  <fi_-eld>c</fi_-eld>\n" +
                "  <fi_-_-eld>d</fi_-_-eld>\n" +
                "</dollar>";
        assertBothWays(in, expected);
    }

    public static class WithUnderscoreCharField extends StandardObject {
        String _field;
        String field_;
        String fi_eld;
        String fi__eld;
    }

    public void testSupportsFieldsWithUnderscoreChar() {
        xstream.alias("underscore", WithUnderscoreCharField.class);

        WithUnderscoreCharField in = new WithUnderscoreCharField();
        in._field = "a";
        in.field_ = "b";
        in.fi_eld = "c";
        in.fi__eld = "d";

        String expected = "" +
                "<underscore>\n" +
                "  <__field>a</__field>\n" +
                "  <field__>b</field__>\n" +
                "  <fi__eld>c</fi__eld>\n" +
                "  <fi____eld>d</fi____eld>\n" +
                "</underscore>";
        assertBothWays(in, expected);
    }

    public static class A_B extends StandardObject {
        private int x;

        public A_B(int x) {
            this.x = x;
        }

    }

    public void testSupportsUnderscoreInShortClassName() {
        assertBothWays(new A_B(3), ""
                + "<com.thoughtworks.acceptance.XmlFriendlyTest_-A__B>\n"
                + "  <x>3</x>\n"
                + "</com.thoughtworks.acceptance.XmlFriendlyTest_-A__B>");
    }

    public void testSlashRSlashSlashSlashN() {
        String before = "\r\\\n";
        String xml = xstream.toXML(before);
        assertEquals(before, xstream.fromXML(xml));
    }

    public void testCanDealWithUtfText() {
        assertBothWays("J\u00F6rg", "<string>J\u00F6rg</string>");
    }

    public void testCanDealWithNullCharactersInText() {
        assertBothWays("X\0Y", "<string>X&#x0;Y</string>");
    }

    public void testEscapesXmlUnfriendlyChars() {
        assertBothWays("<", "<string>&lt;</string>");
        assertBothWays(">", "<string>&gt;</string>");
        assertBothWays("<>", "<string>&lt;&gt;</string>");
        assertBothWays("<=", "<string>&lt;=</string>");
        assertBothWays(">=", "<string>&gt;=</string>");
        assertBothWays("&", "<string>&amp;</string>");
        assertBothWays("'", "<string>&apos;</string>");
        assertBothWays("\"", "<string>&quot;</string>");
    }

    public void testDecimalFormatSymbols() {
        final String xml;
        if (!JVM.is14()) {
            xml =
                    "<java.text.DecimalFormatSymbols serialization=\"custom\">\n"
                + "  <java.text.DecimalFormatSymbols>\n"
                + "    <default>\n"
                + "      <decimalSeparator>,</decimalSeparator>\n"
                + "      <digit>#</digit>\n"
                + "      <exponential>E</exponential>\n"
                + "      <groupingSeparator>.</groupingSeparator>\n"
                + "      <minusSign>-</minusSign>\n"
                + "      <monetarySeparator>,</monetarySeparator>\n"
                + "      <patternSeparator>;</patternSeparator>\n"
                + "      <perMill>\u2030</perMill>\n"
                + "      <percent>%</percent>\n"
                + "      <serialVersionOnStream>1</serialVersionOnStream>\n"
                + "      <zeroDigit>0</zeroDigit>\n"
                + "      <NaN>\ufffd</NaN>\n"
                + "      <currencySymbol>DM</currencySymbol>\n"
                + "      <infinity>\u221e</infinity>\n"
                + "      <intlCurrencySymbol>DEM</intlCurrencySymbol>\n"
                + "    </default>\n"
                + "  </java.text.DecimalFormatSymbols>\n"
                + "</java.text.DecimalFormatSymbols>";
        } else {
            xml =
                    "<java.text.DecimalFormatSymbols serialization=\"custom\">\n"
                + "  <java.text.DecimalFormatSymbols>\n"
                + "    <default>\n"
                + "      <decimalSeparator>,</decimalSeparator>\n"
                + "      <digit>#</digit>\n"
                + "      <exponential>E</exponential>\n"
                + "      <groupingSeparator>.</groupingSeparator>\n"
                + "      <minusSign>-</minusSign>\n"
                + "      <monetarySeparator>,</monetarySeparator>\n"
                + "      <patternSeparator>;</patternSeparator>\n"
                + "      <perMill>\u2030</perMill>\n"
                + "      <percent>%</percent>\n"
                + "      <serialVersionOnStream>2</serialVersionOnStream>\n"
                + "      <zeroDigit>0</zeroDigit>\n"
                + "      <NaN>\ufffd</NaN>\n"
                + "      <currencySymbol>\u20ac</currencySymbol>\n"
                + "      <infinity>\u221e</infinity>\n"
                + "      <intlCurrencySymbol>EUR</intlCurrencySymbol>\n"
                + "      <locale>de_DE</locale>\n"
                + "    </default>\n"
                + "  </java.text.DecimalFormatSymbols>\n"
                + "</java.text.DecimalFormatSymbols>";
        }
        final DecimalFormatSymbols format = new DecimalFormatSymbols(Locale.GERMANY);
        assertBothWays(format, xml);
    }

}
