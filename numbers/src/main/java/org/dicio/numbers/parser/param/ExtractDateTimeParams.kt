package org.dicio.numbers.parser.param

import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.lexer.TokenStream
import java.time.LocalDateTime

class ExtractDateTimeParams(parser: Parser, utterance: String) :
    NumberParserParams<LocalDateTime>(parser, utterance) {
    // TODO documentation
    // default values
    private var now: LocalDateTime = LocalDateTime.now()
    private var shortScale = true // shouldn't make a big difference anyway
    private var preferMonthBeforeDay = false

    fun now(now: LocalDateTime): ExtractDateTimeParams {
        this.now = now
        return this
    }

    fun shortScale(shortScale: Boolean): ExtractDateTimeParams {
        this.shortScale = shortScale
        return this
    }

    fun preferMonthBeforeDay(preferMonthBeforeDay: Boolean): ExtractDateTimeParams {
        this.preferMonthBeforeDay = preferMonthBeforeDay
        return this
    }

    override fun getExtractorAtCurrentPosition(tokenStream: TokenStream): () -> LocalDateTime? {
        return parser.extractDateTime(tokenStream, shortScale, preferMonthBeforeDay, now)
    }
}
