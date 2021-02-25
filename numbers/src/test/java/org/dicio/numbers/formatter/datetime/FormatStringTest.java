package org.dicio.numbers.formatter.datetime;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FormatStringTest {

    private static final Map<String, String> substitutionTable = new HashMap<String, String>() {{
        put("a", "A");
        put("bbb", "B1");
        put("empty", "");
        put("space", " ");
        put("never used", "unreachable");
    }};

    private static void assertFormat(final String expected, final String formatString) {
        assertEquals(expected, new FormatString(formatString).format(substitutionTable));
    }


    @Test
    public void noFormatParts() {
        assertFormat("hello", "hello");
        assertFormat("    ", "    ");
        assertFormat(" { {{ ", " { {{ ");
        assertFormat("} {k127{", "} {k127{");
    }

    @Test
    public void onlyOneFormatPart() {
        assertFormat("A", "{a}");
        assertFormat("B1", "{bbb}");
        assertFormat("", "{empty}");
        assertFormat(" ", "{space}");
    }

    @Test
    public void onlyFormatParts() {
        assertFormat("B1A ", "{bbb}{a}{space}");
        assertFormat("AA AB1A", "{a}{empty}{a}{space}{empty}{a}{bbb}{empty}{a}{empty}");
        assertFormat("    ", "{space}{space}{empty}{space}{space}");
        assertFormat("", "{empty}{empty}{empty}");
    }

    @Test
    public void mixedParts() {
        assertFormat("A, B1 A", "{a}, {bbb}{space}{empty}{a}");
        assertFormat("    ciAoB1 ", "  {space}{space}ci{a}o{bbb} ");
        assertFormat("  }B1} A{a", "{space} }{bbb}} {a}{a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails() {
        new FormatString(" {a} {empty}{bbba}{space}").format(substitutionTable);
    }

    @Test
    public void emptyFormatString() {
        final FormatString fs = new FormatString("");
        assertEquals("", fs.format(Collections.emptyMap()));
        assertEquals("", fs.format(substitutionTable));
    }
}
