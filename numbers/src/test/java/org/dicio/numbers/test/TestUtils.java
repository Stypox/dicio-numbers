package org.dicio.numbers.test;

import org.dicio.numbers.lang.en.EnglishFormatter;
import org.dicio.numbers.unit.Number;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class TestUtils {
    public static final boolean T = true;
    public static final boolean F = false;

    // expressed in nanoseconds
    public static final int MICROS = ChronoUnit.MICROS.getDuration().getNano();
    public static final int MILLIS = ChronoUnit.MILLIS.getDuration().getNano();

    // expressed in seconds
    public static final long MINUTE = ChronoUnit.MINUTES.getDuration().getSeconds();
    public static final long HOUR = ChronoUnit.HOURS.getDuration().getSeconds();
    public static final long DAY = ChronoUnit.DAYS.getDuration().getSeconds();
    public static final long WEEK = ChronoUnit.WEEKS.getDuration().getSeconds();
    public static final long YEAR = ChronoUnit.YEARS.getDuration().getSeconds();
    public static final long MONTH = ChronoUnit.MONTHS.getDuration().getSeconds();

    public static Number numberDeduceType(final double value) {
        if (((long) value) == value) {
            return new Number((long) value);
        } else {
            return new Number(value);
        }
    }

    public static Number n(final long value, final boolean ordinal) {
        return new Number(value, ordinal);
    }

    public static Number n(final double value, final boolean ordinal) {
        return new Number(value, ordinal);
    }

    public static Number n(final long value) {
        return new Number(value);
    }

    public static Number n(final double value) {
        return new Number(value);
    }

    public static Duration t(final long seconds, final int nanoseconds) {
        return Duration.ofSeconds(seconds, nanoseconds);
    }

    public static Duration t(final long seconds) {
        return Duration.ofSeconds(seconds);
    }

    public static Duration t(final double seconds) {
        return Duration.ofSeconds((long) seconds, (int)((seconds % 1.0) * 1e9));
    }

    public static org.dicio.numbers.unit.Duration t(final double number, final ChronoUnit chronoUnit) {
        return new org.dicio.numbers.unit.Duration().plus(number, chronoUnit);
    }

    public static String niceDuration(final org.dicio.numbers.unit.Duration duration) {
        return niceDuration(duration.toJavaDuration());
    }

    public static String niceDuration(final Duration duration) {
        if (duration.isNegative()) {
            return "minus " + niceDuration(duration.negated());
        } else {
            return new EnglishFormatter().niceDuration(
                    new org.dicio.numbers.unit.Duration(duration), true);
        }
    }
}
