package org.dicio.numbers.test;

import org.dicio.numbers.util.Number;

public class TestUtils {
    public static final boolean T = true;
    public static final boolean F = false;

    public static Number numberDeduceType(final double value) {
        if (((long) value) == value) {
            return new Number((long) value);
        } else {
            return new Number(value);
        }
    }

    public static Number n(final long value, final boolean ordinal) {
        return new Number(value).setOrdinal(ordinal);
    }

    public static Number n(final double value, final boolean ordinal) {
        return new Number(value).setOrdinal(ordinal);
    }
}
