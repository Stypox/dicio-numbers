package org.dicio.numbers.util

import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.unit.Duration
import org.dicio.numbers.unit.Number

/**
 * This class should work well at least for european languages (I don't know the structure of
 * other languages though). Requires the token stream to have been tokenized with the same rules
 * as in the English language.
 *
 * @param ts the token stream from which to obtain information
 * @param extractOneNumberNoOrdinal tries to extract a non-ordinal number at the current token
 * stream position. Will be called multiple times. Should not
 * prefer ordinal numbers (i.e. preferOrdinal should be false).
 */
class DurationExtractorUtils(
    private val ts: TokenStream,
    private val extractOneNumberNoOrdinal: () -> Number?
) {
    /**
     * Extract a duration at the current position (i.e. no words will be skipped, not even ignorable
     * words) in the token stream provided in the constructor
     * @return the found duration, or null if no duration was found
     */
    fun duration(): Duration? {
        val originalPosition = ts.position
        val firstNumber = extractOneNumberNoOrdinal()
        var result = durationAfterNullableNumber(firstNumber)

        if (result == null) {
            // duration not found at current position
            ts.position = originalPosition
            return null
        }

        // found a duration, try to expand it
        var positionLastDurationFound = ts.position
        while (!ts.finished()) {
            val number = extractOneNumberNoOrdinal()
            val duration = durationAfterNullableNumber(number)

            if (number == null && duration == null && ts[0].hasCategory("ignore")) {
                ts.movePositionForwardBy(1) // skip this ignorable word and continue
            } else if (duration == null) {
                break
            } else {
                positionLastDurationFound = ts.position
                result = result!!.plus(duration) // found another duration group, add and continue
            }
        }

        ts.position = positionLastDurationFound
        return result
    }

    private fun durationAfterNullableNumber(number: Number?): Duration? {
        if (number == null) {
            val durationToken = ts[0].asDurationToken ?: return null
            if (durationToken.isRestrictedAfterNumber) {
                // found duration token that requires a number before it, but there is not one,
                // e.g. s, ms, h
                return null
            } else {
                // found valid duration token at current position, without a number before,
                // e.g. a second (since "a" is not considered a number)
                ts.movePositionForwardBy(1)
                return durationToken.durationMultiplier
            }
        } else {
            var nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0)
            if (ts[nextNotIgnore].hasCategory("duration_separator")) {
                // found a duration separator (like "of") after the number, e.g. a couple of hours
                nextNotIgnore = ts.indexOfWithoutCategory("ignore", nextNotIgnore + 1)
            }

            val durationToken = ts[nextNotIgnore].asDurationToken ?:
                // the number that was found was not followed by a duration multiplier,
                // e.g. fifteen people, a couple of houses
                return null

            // found a number followed by a duration multiplier,
            // e.g. two seconds, a couple of hours
            ts.movePositionForwardBy(nextNotIgnore + 1)
            return durationToken.durationMultiplier.multiply(number)
        }
    }
}
