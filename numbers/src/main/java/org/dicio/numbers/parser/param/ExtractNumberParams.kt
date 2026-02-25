package org.dicio.numbers.parser.param

import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.unit.Number

class ExtractNumberParams(parser: Parser, utterance: String) :
    ParserParams<Number?>(parser, utterance) {
    // default values
    private var shortScale = true
    private var preferOrdinal = false
    private var integerOnly = false

    /**
     * @param shortScale consider large numbers (e.g. the "one billion") as short scale (true) or
     * long scale (false) numbers (see
     * [Names of large numbers](https://en.wikipedia.org/wiki/Names_of_large_numbers)). **The
     * default is `true`.** This setting only affects languages where both short and long scale
     * numbers exist, e.g. English, and is otherwise ignored.
     * @return this
     */
    fun shortScale(shortScale: Boolean): ExtractNumberParams {
        this.shortScale = shortScale
        return this
    }

    /**
     * @param preferOrdinal whether to resolve ambiguities between ordinal numbers and fractions in
     * favor of ordinal numbers. **The default is `false`.** E.g. if [preferOrdinal]`=true`, then
     * "hundred eighths` will be parsed as "108th", and otherwise as "12.5" (i.e. 100/8). This
     * parameter has no effect when [integerOnly]`=true`, because there can't be fractions then,
     * so all non-ambiguities are obviously resolved in favor of ordinal numbers.
     * @return this
     */
    fun preferOrdinal(preferOrdinal: Boolean): ExtractNumberParams {
        this.preferOrdinal = preferOrdinal
        return this
    }

    /**
     * @param integerOnly whether to ignore all decimal number indicators (e.g. "point", ".",
     * fractions, ...) and only look for integer values even when those could be part of a decimal
     * number or a fraction. **The default is `false`.** E.g. if [integerOnly]`=true`, then "one
     * point two" will be parsed as [[1, "point", 2]]. The returned [Number]s will generally have
     * [Number.isInteger]`=true` (except in edge cases like very big integer numbers that fit in a
     * [Double] but not in a [Long]).
     * @return this
     */
    fun integerOnly(integerOnly: Boolean): ExtractNumberParams {
        this.integerOnly = integerOnly
        return this
    }

    /**
     * @return Like [parseFirst], but returns [Number.integerValue]`()`, and does so only if
     * [Number.isInteger] is `true`. When parsing a string like "one point two", the result will be
     * `null` if [integerOnly]`=false` (since `1.2` is parsed as a floating point and then discarded
     * by this function since it's not an integer), and will be `1` if [integerOnly]`=true` (since
     * `1` is parsed as an integer).
     */
    fun parseFirstIfInteger(): Long? {
        return parseFirst()?.takeIf { it.isInteger }?.integerValue()
    }

    override fun getExtractorAtCurrentPosition(tokenStream: TokenStream): () -> Number? {
        return parser.extractNumber(tokenStream, shortScale, preferOrdinal, integerOnly)
    }
}
