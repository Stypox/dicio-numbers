package org.dicio.numbers.parser.lexer

import org.dicio.numbers.unit.Number

class NumberToken internal constructor(
    value: String,
    spacesFollowing: String,
    positionInOriginalString: Int,
    categories: Set<String>,
    override val number: Number
) : MatchedToken(value, spacesFollowing, positionInOriginalString, categories) {
    override fun isNumberEqualTo(integer: Long): Boolean {
        return number.equals(integer)
    }
}
