package org.dicio.numbers.unit;

import org.dicio.numbers.unit.MixedFraction;
import org.dicio.numbers.util.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MixedFractionTest {

    private void assertMixedFraction(final double number,
                                     final long whole,
                                     final int numerator,
                                     final int denominator) {
        final MixedFraction mixedFraction
                = MixedFraction.of(number, MixedFraction.DEFAULT_DENOMINATORS);
        assertNotNull(mixedFraction);

        assertTrue(mixedFraction.whole >= 0);
        assertTrue(mixedFraction.numerator >= 0);
        if (mixedFraction.numerator == 0) {
            assertEquals(1, mixedFraction.denominator);
        } else {
            assertTrue(mixedFraction.denominator > 1);
        }

        assertEquals(number < 0, mixedFraction.negative);
        assertEquals(whole, mixedFraction.whole);
        assertEquals(numerator, mixedFraction.numerator);
        assertEquals(denominator, mixedFraction.denominator);

        Assert.assertEquals(number, (number < 0 ? -1 : 1) * (mixedFraction.whole
                        + (double) mixedFraction.numerator / mixedFraction.denominator),
                Utils.WHOLE_FRACTION_ACCURACY);
    }

    private void assertMixedFraction(final boolean negative,
                                     final long whole,
                                     final int numerator,
                                     final int denominator) {
        assertMixedFraction((negative ? -1 : 1) * (whole + (double) numerator / denominator),
                whole, numerator, denominator);
    }

    @Test
    public void testConvertAndBack() {
        assertMixedFraction(true, 0, 0, 1);
        assertMixedFraction(false, 0, 0, 1);
        assertMixedFraction(true, 1, 0, 1);
        assertMixedFraction(false, 1, 0, 1);
        assertMixedFraction(true, 1, 1, 2);
        assertMixedFraction(false, 8, 7, 19);
        assertMixedFraction(true, 2354, 2, 3);
        assertMixedFraction(false, 12, 9, 10);
        assertMixedFraction(true, 664, 4, 7);
        assertMixedFraction(false, 735, 4, 9);
    }

    @Test
    public void testWholeNumbers() {
        assertMixedFraction(0.0, 0, 0, 1);
        assertMixedFraction(-0.0, 0, 0, 1);
        assertMixedFraction(1.0, 1, 0, 1);
        assertMixedFraction(-2.0, 2, 0, 1);
        assertMixedFraction(5.00001, 5, 0, 1);
        assertMixedFraction(-18.00001, 18, 0, 1);
    }

    @Test
    public void testFractionalNumbers() {
        assertMixedFraction(2.5003, 2, 1, 2);
        assertMixedFraction(-192.66667, 192, 2, 3);
        assertMixedFraction(42.25, 42, 1, 4);
        assertMixedFraction(-0.4005, 0, 2, 5);
        assertMixedFraction(0.8333, 0, 5, 6);
        assertMixedFraction(-6112.714285, 6112, 5, 7);
        assertMixedFraction(745.266666667, 745, 4, 15);
    }

    @Test
    public void testOtherValidNumbers() {
        assertNotNull(MixedFraction.of(1e18d, MixedFraction.DEFAULT_DENOMINATORS));
        assertNotNull(MixedFraction.of(-1e18d, MixedFraction.DEFAULT_DENOMINATORS));
    }

    @Test
    public void testInvalidNumbers() {
        assertNull(MixedFraction.of(1e20, MixedFraction.DEFAULT_DENOMINATORS));
        assertNull(MixedFraction.of(1.0 / 25, MixedFraction.DEFAULT_DENOMINATORS));
        assertNull(MixedFraction.of(1.5, Arrays.asList(3, 9, 27)));
    }

    @Test
    public void testDefaultDenominators() {
        assertEquals(19, MixedFraction.DEFAULT_DENOMINATORS.size());
        for (int i = 0; i < 19; ++i) {
            assertEquals(i + 2, (int) MixedFraction.DEFAULT_DENOMINATORS.get(i));
        }
    }
}
