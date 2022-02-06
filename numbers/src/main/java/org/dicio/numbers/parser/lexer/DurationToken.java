package org.dicio.numbers.parser.lexer;

import org.dicio.numbers.util.Number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;

public class DurationToken extends Token {
    private final Duration durationMultiplier;
    private final boolean restrictedAfterNumber;

    DurationToken(final String value,
                  final String spacesFollowing,
                  final Duration durationMultiplier,
                  final boolean restrictedAfterNumber) {
        super(value, spacesFollowing);
        this.durationMultiplier = durationMultiplier;
        this.restrictedAfterNumber = restrictedAfterNumber;
    }

    @Override
    public boolean isDurationToken() {
        return true;
    }

    @Override
    public DurationToken asDurationToken() {
        return this;
    }

    public Duration getDurationMultiplier() {
        return durationMultiplier;
    }

    public boolean isRestrictedAfterNumber() {
        return restrictedAfterNumber;
    }

    public Duration getDurationMultipliedBy(final Number number) {
        if (number.isInteger()) {
            return durationMultiplier.multipliedBy(number.integerValue());
        } else {
            final BigDecimal allNanos = BigDecimal.valueOf(durationMultiplier.getNano())
                    .add(BigDecimal.valueOf(durationMultiplier.getSeconds(), -9))
                    .multiply(BigDecimal.valueOf(number.decimalValue()));
            final long seconds = allNanos.multiply(BigDecimal.valueOf(1, 9)).longValue();
            final long nanos = allNanos.subtract(BigDecimal.valueOf(seconds, -9)).longValue();

            return Duration.ofSeconds(seconds, nanos);
        }
    }
}
