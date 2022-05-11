package org.dicio.numbers.parser.lexer;

import org.dicio.numbers.unit.Duration;

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
}
