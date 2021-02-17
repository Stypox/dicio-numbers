package org.dicio.numbers.datetime;

import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FormatStringCollectionTest {

    private static final String defaultJson = "{" +
            "\"1\": {\"match\": \"^\\\\d$\", \"format\": \"{x}\"}," +
            "\"2\": {\"match\": \"^1\\\\d$\", \"format\": \"{xx}\"}," +
            "\"3\": {\"match\": \"^\\\\d0$\", \"format\": \"{x0}\"}," +
            "\"4\": {\"match\": \"^[2-9]\\\\d$\", \"format\": \"{x0} {x}\"}," +
            "\"default\": \"{number}\"" +
            "}";

    private static final Map<String, String> substitutionTable = new HashMap<String, String>() {{
        put("x", "five");
        put("xx", "65");
        put("x0", "sixty");
        put("number", "1965");
    }};

    private static FormatStringCollection fsc;

    private static void assertFormattedTo(final String expected, final int number) {
        assertEquals(number + " is not formatted correctly", expected, fsc.getMostSuitableFormatString(number).format(substitutionTable));
    }


    @BeforeClass
    public static void setup() throws JsonParserException {
        fsc = new FormatStringCollection(JsonParser.object().from(defaultJson));
    }

    @Test
    public void singleDigit() {
        for (int i = 0; i < 10; ++i) {
            assertFormattedTo("five", i);
        }
    }

    @Test
    public void twoDigitLessThanTwenty() {
        for (int i = 10; i < 20; ++i) {
            assertFormattedTo("65", i);
        }
    }

    @Test
    public void twoDigitMoreThanTwenty() {
        for (int i = 20; i < 100; ++i) {
            if (i % 10 == 0) {
                assertFormattedTo("sixty", i);
            } else {
                assertFormattedTo("sixty five", i);
            }
        }
    }

    @Test
    public void otherDigitCount() {
        for (final int i : new int[] {100, 4387, 192, 448, 37482, Integer.MAX_VALUE}) {
            assertFormattedTo("1965", i);
        }
    }

    @Test
    public void negative() {
        for (final int i : new int[] {-1, -8, -32, -123, -53798, Integer.MIN_VALUE}) {
            assertFormattedTo("1965", i);
        }
    }
}
