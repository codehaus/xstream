package com.thoughtworks.acceptance;

public class EncodingTest extends AbstractAcceptanceTest {

    public void testCanDealWithUtfText() {
        String input = "J�rg";

        String expected = "<string>J�rg</string>";

        assertBothWays(input, expected);

    }
}
