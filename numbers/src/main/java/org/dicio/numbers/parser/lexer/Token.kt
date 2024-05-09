package org.dicio.numbers.parser.lexer

import org.dicio.numbers.unit.Number

open class Token internal constructor(@JvmField val value: String, @JvmField var spacesFollowing: String) {

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
        private val EMPTY_TOKEN = Token("", "")

        @JvmStatic
        fun emptyToken(): Token {
            return EMPTY_TOKEN
        }
    }
}
