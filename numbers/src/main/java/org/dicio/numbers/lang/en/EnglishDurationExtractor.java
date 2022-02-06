package org.dicio.numbers.lang.en;

import org.dicio.numbers.parser.lexer.DurationToken;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.util.Number;

import java.time.Duration;

public class EnglishDurationExtractor {
    private final TokenStream ts;
    private final EnglishNumberExtractor numberExtractor;

    EnglishDurationExtractor(final TokenStream tokenStream, final boolean shortScale) {
        this.ts = tokenStream;
        this.numberExtractor = new EnglishNumberExtractor(ts, shortScale, false);
    }

    public Duration extractDuration() {
        Duration result;
        while (true) {
            final int originalPosition = ts.getPosition();
            final Number number = numberExtractor.extractOneNumberNoOrdinal();
            result = durationAfterNullableNumber(number);

            if (result != null) {
                break; // found a duration, exit the loop and try to expand it
            }

            ts.setPosition(originalPosition + 1); // advance by one token w/ respect to beginning
            if (ts.finished()) {
                // no duration found in all of the token stream
                return null;
            }
        }

        while (!ts.finished()) {
            final Number number = numberExtractor.extractOneNumberNoOrdinal();
            final Duration duration = durationAfterNullableNumber(number);

            if (number == null && duration == null && ts.get(0).hasCategory("ignore")) {
                ts.movePositionForwardBy(1); // skip this ignorable word and continue
            } else if (duration == null) {
                break;
            } else {
                result = result.plus(duration); // found another duration group, add and continue
            }
        }

        return result;
    }

    Duration durationAfterNullableNumber(final Number number) {
        if (number == null) {
            if (ts.get(0).isDurationToken()) {
                final DurationToken durationToken = ts.get(0).asDurationToken();
                if (durationToken.isRestrictedAfterNumber()) {
                    // found duration token that requires a number before it, but there is not one,
                    // e.g. s, ms, h
                    return null;
                } else {
                    // found valid duration token at current position, without a number before,
                    // e.g. a second (since "a" is not considered a number)
                    ts.movePositionForwardBy(1);
                    return durationToken.getDurationMultiplier();
                }
            } else {
                // found neither a number, nor a duration token, just a random token
                return null;
            }

        } else {
            int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
            if (ts.get(nextNotIgnore).hasCategory("duration_separator")) {
                // found a duration separator (like "of") after the number, e.g. a couple of hours
                nextNotIgnore = ts.indexOfWithoutCategory("ignore", nextNotIgnore + 1);
            }

            if (ts.get(nextNotIgnore).isDurationToken()) {
                // found a number followed by a duration multiplier,
                // e.g. two seconds, a couple of hours
                final DurationToken durationToken = ts.get(nextNotIgnore).asDurationToken();
                ts.movePositionForwardBy(nextNotIgnore + 1);
                return durationToken.getDurationMultipliedBy(number);
            } else {
                // the number that was found was actually followed by a duration multiplier,
                // e.g. fifteen people, a couple of houses
                return null;
            }
        }
    }
}
