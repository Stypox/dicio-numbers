package org.dicio.numbers.lang.it

import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.unit.Duration
import org.dicio.numbers.unit.Number
import org.dicio.numbers.util.DurationExtractorUtils
import java.time.LocalDateTime

class ItalianParser : Parser("config/it-it") {
    override fun extractNumber(
        tokenStream: TokenStream,
        shortScale: Boolean,
        preferOrdinal: Boolean
    ): () -> Number? {
        val numberExtractor = ItalianNumberExtractor(tokenStream)
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
        val numberExtractor = ItalianNumberExtractor(tokenStream)
        return DurationExtractorUtils(tokenStream, numberExtractor::numberNoOrdinal)::duration
    }

    override fun extractDateTime(
        tokenStream: TokenStream,
        shortScale: Boolean,
        preferMonthBeforeDay: Boolean,
        now: LocalDateTime
    ): () -> LocalDateTime? {
        return ItalianDateTimeExtractor(tokenStream, now)::dateTime
    }
}
