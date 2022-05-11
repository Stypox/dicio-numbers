package org.dicio.numbers.unit;

import org.dicio.numbers.util.Utils;

import java.util.Arrays;
import java.util.List;

public class MixedFraction {

    public static final List<Integer> DEFAULT_DENOMINATORS
            = Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

    public final boolean negative; // true if original number is negative
    public final long whole; // always positive
    public final int numerator; // always positive
    public final int denominator; // if numerator = 0, then = 1, otherwise > 1

    private MixedFraction(final boolean negative,
                          final long whole,
                          final int numerator,
                          final int denominator) {
        this.negative = negative;
        this.whole = whole;
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Convert a double to components of a mixed fraction representation.
     * @see #DEFAULT_DENOMINATORS
     *
     * @param number the number to convert
     * @param denominators the denominators to use, each has to be > 1
     * @return the closest fractional representation using the provided denominators. Returns {@code
     *         null} if none of the provided denominators yielded a close enough approximation.
     *         E.g. {@code 4.500002} would become the whole number {@code 4}, the numerator {@code
     *         1} and the denominator {@code 2}.
     */
    public static MixedFraction of(final double number, final List<Integer> denominators) {
        if ((long) Math.abs(number) == Long.MAX_VALUE) {
            return null; // number is too large to fit
        } else if (Utils.isWhole(number, Utils.WHOLE_FRACTION_ACCURACY)) {
            return new MixedFraction(number < 0, Utils.roundToLong(Math.abs(number)), 0, 1);
        }

        final double numberFraction = Math.abs(number % 1);
        for (final int denominator : denominators) {
            final double numerator = numberFraction * denominator;
            if (Utils.isWhole(numerator, Utils.WHOLE_FRACTION_ACCURACY)) {
                return new MixedFraction(number < 0, Math.abs((long) number),
                        (int) Utils.roundToLong(numerator), denominator);
            }
        }

        return null;
    }
}
