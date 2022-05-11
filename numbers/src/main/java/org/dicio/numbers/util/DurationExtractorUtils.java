package org.dicio.numbers.util;

import org.dicio.numbers.parser.lexer.DurationToken;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Number;

import java.time.Duration;
import java.util.function.Supplier;

public class DurationExtractorUtils {
    private final TokenStream ts;
    private final Supplier<Number> extractOneNumberNoOrdinal;

    private DurationExtractorUtils(final TokenStream tokenStream,
                                   final Supplier<Number> extractOneNumberNoOrdinal) {
        this.ts = tokenStream;
        this.extractOneNumberNoOrdinal = extractOneNumberNoOrdinal;
    }

    /**
     * Extract a duration from the provided token stream (assuming the token stream has been
     * tokenized with the same rules as those in the English language). This class should work
     * well at least for european languages (I don't know the structure of other languages though).
     * @param tokenStream the token stream from which to obtain information
     * @param extractOneNumberNoOrdinal tries to extract a non-ordinal number at the current token
     *                                  stream position. Will be called multiple times. Should not
     *                                  prefer ordinal numbers (i.e. preferOrdinal should be false).
     * @return the found duration, or null if no duration was found
     */
    public static Duration extractDuration(final TokenStream tokenStream,
                                           final Supplier<Number> extractOneNumberNoOrdinal) {
        return new DurationExtractorUtils(tokenStream, extractOneNumberNoOrdinal).extractDuration();
    }

    private Duration extractDuration() {
        Duration result;
        while (true) {
            final int originalPosition = ts.getPosition();
            final Number number = extractOneNumberNoOrdinal.get();
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
            final Number number = extractOneNumberNoOrdinal.get();
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

    private Duration durationAfterNullableNumber(final Number number) {
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
