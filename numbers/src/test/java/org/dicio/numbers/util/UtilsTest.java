package org.dicio.numbers.util;

import org.junit.Test;

import static org.dicio.numbers.util.Utils.WHOLE_NUMBER_ACCURACY;
import static org.dicio.numbers.util.Utils.containsCodePoint;
import static org.dicio.numbers.util.Utils.decimalPlacesNoFinalZeros;
import static org.dicio.numbers.util.Utils.firstNotNull;
import static org.dicio.numbers.util.Utils.isWhole;
import static org.dicio.numbers.util.Utils.longPow;
import static org.dicio.numbers.util.Utils.removeRedundantSpaces;
import static org.dicio.numbers.util.Utils.roundToLong;
import static org.dicio.numbers.util.Utils.splitByModulus;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void testIsWhole() {
        assertTrue(isWhole(0.0, 0.0000001));
        assertTrue(isWhole(-0.0, 0.0000001));
        assertTrue(isWhole(5.0, 0.0000001));
        assertTrue(isWhole(5.1, 0.11));
        assertTrue(isWhole(0.1, 0.11));
        assertTrue(isWhole(-0.1, 0.11));
        assertTrue(isWhole(5.000001, WHOLE_NUMBER_ACCURACY));
        assertTrue(isWhole(30000.99991, WHOLE_NUMBER_ACCURACY));
        assertTrue(isWhole(0.0000001, WHOLE_NUMBER_ACCURACY));

        assertFalse(isWhole(0.0, 0.0));
        assertFalse(isWhole(-0.0, 0.0));
        assertFalse(isWhole(5.0, 0.0));
        assertFalse(isWhole(0.5, WHOLE_NUMBER_ACCURACY));
        assertFalse(isWhole(31.0 / 7.0, WHOLE_NUMBER_ACCURACY));
        assertFalse(isWhole(0.001, WHOLE_NUMBER_ACCURACY));
        assertFalse(isWhole(0.89999, 0.1)); // floating point errors
        assertFalse(isWhole(8.89999, 0.1));
    }

    @Test
    public void testDecimalPlacesNoFinalZeros() {
        assertEquals(2, decimalPlacesNoFinalZeros(0.22912, 2));
        assertEquals(5, decimalPlacesNoFinalZeros(90.10293, 8));
        assertEquals(3, decimalPlacesNoFinalZeros(4.9980, 3));
        assertEquals(0, decimalPlacesNoFinalZeros(24.9980, 2));
        assertEquals(0, decimalPlacesNoFinalZeros(16, 800));

        assertEquals(0, decimalPlacesNoFinalZeros(0.0, 200));
        assertEquals(0, decimalPlacesNoFinalZeros(-0.0, 400));
        assertEquals(0, decimalPlacesNoFinalZeros(-16, 400));
    }

    @Test
    public void testLongPow() {
        assertEquals(1, longPow(1000000, 0));
        assertEquals(1, longPow(0, 0));
        assertEquals(0, longPow(0, 10000));
        assertEquals(1, longPow(1, 10000));
        assertEquals(1000000000, longPow(1000, 3));
        assertEquals(1000000000, longPow(10, 9));
        assertEquals(10000000000L, longPow(100, 5));
    }

    @Test
    public void testRoundToLong() {
        assertEquals(5, roundToLong(5.0));
        assertEquals(2, roundToLong(2.4));
        assertEquals(11, roundToLong(10.6));
        assertEquals(1, roundToLong(0.5));
        assertEquals(-5, roundToLong(-5.0));
        assertEquals(-2, roundToLong(-2.4));
        assertEquals(-11, roundToLong(-10.6));
        assertEquals(-1, roundToLong(-0.5));
    }

    private void assertSplitByModulus(final long number,
                                      final int splitModulus,
                                      final Long... splits) {
        assertArrayEquals(splits, splitByModulus(number, splitModulus).toArray());
    }

    @Test
    public void testSplitByModulus() {
        assertSplitByModulus(0, 10);
        assertSplitByModulus(0, 1000000);
        assertSplitByModulus(10, 1000000, 10L);
        assertSplitByModulus(1234, 10, 4L, 3L, 2L, 1L);
        assertSplitByModulus(101220300040L, 1000, 40L, 300L, 220L, 101L);
        assertSplitByModulus(100001L, 100, 1L, 0L, 10L);
    }

    @Test
    public void testRemoveRedundantSpaces() {
        assertEquals("hello hi", removeRedundantSpaces("hello hi"));
        assertEquals("hello hi", removeRedundantSpaces("hello hi   "));
        assertEquals("hello hi", removeRedundantSpaces(" hello hi"));
        assertEquals("hello hi", removeRedundantSpaces("    hello hi  "));
        assertEquals("hello hi", removeRedundantSpaces("  hello    hi    "));
        assertEquals("eight hundred b.c.", removeRedundantSpaces("eight  hundred b.c."));
        assertEquals("eight hundred", removeRedundantSpaces(" eight hundred "));
    }

    @Test
    public void testContainsCodePointTrue() {
        assertTrue(containsCodePoint("abcde", "a".codePointAt(0)));
        assertTrue(containsCodePoint("625346", "5".codePointAt(0)));
        assertTrue(containsCodePoint("ç!?'\"", "\"".codePointAt(0)));
        assertTrue(containsCodePoint("àèìéç", "ì".codePointAt(0)));
    }

    @Test
    public void testContainsCodePointFalse() {
        assertFalse(containsCodePoint("", "@".codePointAt(0)));
        assertFalse(containsCodePoint("hello", "0".codePointAt(0)));
        assertFalse(containsCodePoint("ç!?\"", "'".codePointAt(0)));
        assertFalse(containsCodePoint("@òç", "c".codePointAt(0)));
        assertFalse(containsCodePoint("àèç", "e".codePointAt(0)));
    }

    @Test
    public void testFirstNotNull() {
        assertNull(firstNotNull());
        assertNull(firstNotNull(() -> null));
        assertNull(firstNotNull(() -> null, () -> null, () -> null));
        assertEquals(Integer.valueOf(5), firstNotNull(() -> 5));
        assertEquals(Integer.valueOf(-2), firstNotNull(() -> -2, () -> 5, () -> null));
        assertEquals(Integer.valueOf(5), firstNotNull(() -> null, () -> 5));
    }
}
