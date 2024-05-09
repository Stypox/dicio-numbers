package org.dicio.numbers.parser

import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.parser.lexer.Tokenizer
import org.dicio.numbers.unit.Duration
import org.dicio.numbers.unit.Number
import java.time.LocalDateTime

abstract class Parser protected constructor(configFolder: String) {
    protected val tokenizer: Tokenizer = Tokenizer(configFolder)


    abstract fun extractNumber(
        tokenStream: TokenStream,
        shortScale: Boolean,
        preferOrdinal: Boolean
    ): () -> Number?

    abstract fun extractDuration(
        tokenStream: TokenStream,
        shortScale: Boolean
    ): () -> Duration?

    abstract fun extractDateTime(
        tokenStream: TokenStream,
        shortScale: Boolean,
        preferMonthBeforeDay: Boolean,
        now: LocalDateTime
    ): () -> LocalDateTime?


    fun tokenize(utterance: String): TokenStream {
        return TokenStream(tokenizer.tokenize(utterance))
    }
}
