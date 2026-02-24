package org.dicio.numbers.parser.lexer

import org.dicio.numbers.unit.Number

/**
 * @param positionInOriginalString index in the original string at which `value` starts
 */
open class Token internal constructor(
    @JvmField val value: String,
    @JvmField val spacesFollowing: String,
    @JvmField val positionInOriginalString: Int,
) {

    fun isValue(value: String?): Boolean {
        return this.value.equals(value, ignoreCase = true)
    }

    open fun isNumberEqualTo(integer: Long): Boolean {
        return false // false by default, overridden
    }

    open fun hasCategory(category: String): Boolean {
        return false // false by default, overridden
    }

    open val number: Number? = null

    open val asDurationToken: DurationToken? = null // null by default, but overridden


    companion object {
        private val EMPTY_TOKEN = Token("", "", -1)

        @JvmStatic
        fun emptyToken(): Token {
            return EMPTY_TOKEN
        }
    }
}
