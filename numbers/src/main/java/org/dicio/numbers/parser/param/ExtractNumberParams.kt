package org.dicio.numbers.parser.param

import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.unit.Number

class ExtractNumberParams(parser: Parser, utterance: String) :
    NumberParserParams<Number?>(parser, utterance) {
    // TODO documentation
    // default values
    private var shortScale = true
    private var preferOrdinal = false
    private var integerOnly = false

    fun shortScale(shortScale: Boolean): ExtractNumberParams {
        this.shortScale = shortScale
        return this
    }

    fun preferOrdinal(preferOrdinal: Boolean): ExtractNumberParams {
        this.preferOrdinal = preferOrdinal
        return this
    }

    fun integerOnly(integerOnly: Boolean): ExtractNumberParams {
        this.integerOnly = integerOnly
        return this
    }

    val firstIfInteger: Long?
        get() = first?.takeIf { it.isInteger }?.integerValue()

    override fun getExtractorAtCurrentPosition(tokenStream: TokenStream): () -> Number? {
        return parser.extractNumber(tokenStream, shortScale, preferOrdinal, integerOnly)
    }
}
