package com.thoughtworks.acceptance;

public class EncodingTest extends AbstractAcceptanceTest {

    public void testCanDealWithUtfText() {
        assertBothWays("J�rg", "<string>J�rg</string>");
    }

    public void testEscapesXmlUnfriendlyChars() {
        assertBothWays("<", "<string>&lt;</string>");
        assertBothWays(">", "<string>&gt;</string>");
        assertBothWays("<>", "<string>&lt;&gt;</string>");
        assertBothWays("<=", "<string>&lt;=</string>");
        assertBothWays(">=", "<string>&gt;=</string>");
    }

}
