package org.dicio.numbers.lang.es

import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.unit.Duration
import org.dicio.numbers.unit.Number
import org.dicio.numbers.util.DurationExtractorUtils
import java.time.LocalDateTime

class SpanishParser : Parser("config/es-es") {
    override fun extractNumber(
        tokenStream: TokenStream,
        shortScale: Boolean,
        preferOrdinal: Boolean
    ): () -> Number? {
        val numberExtractor = SpanishNumberExtractor(tokenStream)
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
        val numberExtractor = SpanishNumberExtractor(tokenStream)
        return DurationExtractorUtils(tokenStream, numberExtractor::numberNoOrdinal)::duration
    }

    override fun extractDateTime(
        tokenStream: TokenStream,
        shortScale: Boolean,
        preferMonthBeforeDay: Boolean,
        now: LocalDateTime
    ): () -> LocalDateTime? {
        return SpanishDateTimeExtractor(tokenStream, now)::dateTime
    }
}
