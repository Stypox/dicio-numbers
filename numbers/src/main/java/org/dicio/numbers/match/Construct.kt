package org.dicio.numbers.match

import org.dicio.numbers.parser.lexer.TokenStream

interface Construct<T> {
    fun matchToEnd(memToEnd: Array<Score>, ts: TokenStream)
}
