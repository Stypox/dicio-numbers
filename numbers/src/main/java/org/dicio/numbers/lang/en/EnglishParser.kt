package org.dicio.numbers.lang.en

import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.unit.Duration
import org.dicio.numbers.unit.Number
import org.dicio.numbers.util.DurationExtractorUtils
import java.time.LocalDateTime

class EnglishParser : Parser("config/en-us") {
    override fun extractNumber(
        tokenStream: TokenStream,
        shortScale: Boolean,
        preferOrdinal: Boolean
    ): () -> Number? {
        val numberExtractor = EnglishNumberExtractor(tokenStream, shortScale)
        return if (preferOrdinal) {
            numberExtractor::numberPreferOrdinal
        } else {
            numberExtractor::numberPreferFraction
        }
    }

    override fun extractDuration(
        tokenStream: TokenStream,
        shortScale: Boolean
    ): () -> Duration? {
        val numberExtractor = EnglishNumberExtractor(tokenStream, shortScale)
        return DurationExtractorUtils(tokenStream, numberExtractor::numberNoOrdinal)::duration
    }

    override fun extractDateTime(
        tokenStream: TokenStream,
        shortScale: Boolean,
        preferMonthBeforeDay: Boolean,
        now: LocalDateTime
    ): () -> LocalDateTime? {
        return EnglishDateTimeExtractor(tokenStream, shortScale, preferMonthBeforeDay, now)::dateTime
    }
}
