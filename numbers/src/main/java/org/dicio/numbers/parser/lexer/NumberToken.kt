package org.dicio.numbers.parser.lexer

import org.dicio.numbers.unit.Number

class NumberToken internal constructor(
    value: String,
    spacesFollowing: String,
    categories: Set<String>,
    override val number: Number
) : MatchedToken(value, spacesFollowing, categories) {
    override fun isNumberEqualTo(integer: Long): Boolean {
        return number.equals(integer)
    }
}
