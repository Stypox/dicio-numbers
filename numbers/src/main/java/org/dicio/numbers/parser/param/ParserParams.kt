package org.dicio.numbers.parser.param

import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.lexer.TokenStream

/**
 * TODO add documentation
 */
abstract class ParserParams<T> protected constructor(
    protected val parser: Parser, private val utterance: String
) {
    protected abstract fun getExtractorAtCurrentPosition(tokenStream: TokenStream): () -> T?

    fun parseFirst(): T? {
        val ts = parser.tokenize(utterance)
        val extractorAtCurrentPosition = getExtractorAtCurrentPosition(ts)

        while (!ts.finished()) {
            val result: T? = extractorAtCurrentPosition()
            if (result != null) {
                // found the first match here, return it
                return result
            }

            // nothing found here, try next position
            ts.movePositionForwardBy(1)
        }

        // nothing was found at any position
        return null
    }

    fun parseMixedWithText(): List<Any> {
        val ts = parser.tokenize(utterance)
        val extractorAtCurrentPosition = getExtractorAtCurrentPosition(ts)
        val textAndObjects = ArrayList<Any>()
        val currentText = StringBuilder()

        while (!ts.finished()) {
            val o: T? = extractorAtCurrentPosition()

            if (o == null) {
                // no object here, add text and spaces of the current token to currentText instead
                currentText.append(ts[0].value)
                currentText.append(ts[0].spacesFollowing)
                ts.movePositionForwardBy(1)
            } else {
                if (currentText.isNotEmpty()) {
                    textAndObjects.add(currentText.toString()) // add the text before the object
                    currentText.setLength(0) // clear the string builder efficiently
                }
                textAndObjects.add(o)
                currentText.append(ts[-1].spacesFollowing) // spaces after the object
            }
        }

        if (currentText.isNotEmpty()) {
            // add leftover text
            textAndObjects.add(currentText.toString())
        }

        return textAndObjects
    }

    /**
     * [[start], [end]) is an inclusive-exclusive interval representing the range of characters in
     * [utterance] that correspond to [parsedData]. [isLargestPossible] is `true` only if this range
     * is not contained in any other range returned by [parsePossibleIntervals].
     */
    data class MatchedRange<T>(
        val start: Int,
        val end: Int, // exclusive
        val parsedData: T,
        val isLargestPossible: Boolean,
    )

    /**
     * This function is `O(n * k²)`, where `n` is the length of [utterance], and `k` is the maximum
     * length of any object of type [T] that can be parsed in [utterance]. Since [T] represents
     * numbers, durations or dates, `k` is generally small (e.g. just a few words).
     *
     * @return a list of ranges, one for every interval where an object of type [T] could be parsed,
     * sorted first according to [MatchedRange.start] and then by reversed [MatchedRange.end].
     */
    fun parsePossibleIntervals(): List<MatchedRange<T>> {
        val ts = parser.tokenize(utterance)
        val tokenCount = ts.tokenCount
        val extractorAtCurrentPosition = getExtractorAtCurrentPosition(ts)
        val ranges = ArrayList<MatchedRange<T>>()

        var maxEndSoFar = 0
        for (start in 0..<tokenCount) {
            ts.tokenCount = tokenCount // parse up to the whole string (set before accessing ts[0]!)
            ts.position = start // set this so `ts[0].positionInOriginalString` points to start
            val startPositionInOriginalString = ts[0].positionInOriginalString

            // this loops tries to parse all possible lengths of `extractorAtCurrentPosition()`
            // starting from the starting token stream position `start`
            while (true) {
                ts.position = start // start parsing from `start` at every cycle
                val parsedData: T = extractorAtCurrentPosition()
                    ?: break // nothing found, so there also won't be anything if we restrict the interval further
                assert(ts.position != start) // something was matched, so the token stream surely advanced

                ranges.add(
                    MatchedRange(
                        start = startPositionInOriginalString,
                        end = ts[-1].positionInOriginalString + ts[-1].value.length,
                        parsedData = parsedData,
                        // If this is the longest range starting from here, and it reaches further
                        // right than ever observed before, then this range is not contained in any
                        // other range for sure.
                        isLargestPossible = ts.tokenCount == tokenCount && ts.position > maxEndSoFar,
                    )
                )

                maxEndSoFar = maxOf(maxEndSoFar, ts.position)
                ts.tokenCount = ts.position - 1 // next time try to parse a smaller token stream!
            }
        }

        return ranges
    }
}
