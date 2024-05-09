package org.dicio.numbers.parser.lexer

import kotlin.math.max

class TokenStream(private val tokens: List<Token>) {
    @JvmField
    var position: Int = 0

    operator fun get(aheadBy: Int): Token {
        val index = position + aheadBy
        if (index < 0 || index >= tokens.size) {
            return Token.emptyToken() // empty token to allow reducing checks
        }

        return tokens[index]
    }

    /**
     * Moves the position of the token stream ahead by the provided delta. The delta can be
     * negative, i.e. rewinding the stream.
     * @param delta the offset of the wanted position with respect to the current
     */
    fun movePositionForwardBy(delta: Int) {
        position += delta
    }

    fun finished(): Boolean {
        return position >= tokens.size
    }


    /**
     * Finds the first token without the provided category and returns the aheadBy offset
     * @param category the category that tokens have to match to end the search
     * @param startFromAheadBy start the search from the current position plus this value
     * @return the aheadBy offset of the found token, or the aheadBy offset of one past the last
     * token in the token stream if no token was found without the provided category
     */
    fun indexOfWithoutCategory(category: String, startFromAheadBy: Int): Int {
        for (i in max(position + startFromAheadBy, 0) until tokens.size) {
            if (!tokens[i].hasCategory(category)) {
                return i - position
            }
        }
        return tokens.size - position
    }

    fun <T> tryOrSkipCategory(
        category: String,
        doTrySkipping: Boolean,
        function: () -> T
    ): T? {
        if (!doTrySkipping) {
            return function()
        }

        val originalPosition = position
        do {
            val result: T? = function()
            if (result != null) {
                return result
            }
            movePositionForwardBy(1)
        } while (get(-1).hasCategory(category) && !finished())

        // found nothing, restore position
        position = originalPosition
        return null
    }

    fun <T> tryOrSkipDateTimeIgnore(doTrySkipping: Boolean, function: () -> T): T? {
        return tryOrSkipCategory("date_time_ignore", doTrySkipping, function)
    }

    @SafeVarargs
    fun <T> firstWhichUsesMostTokens(vararg suppliers: () -> T): T? {
        val originalPosition = position
        var bestResult: T? = null
        var bestPosition = originalPosition

        for (supplier in suppliers) {
            position = originalPosition
            val result: T? = supplier()
            if (result != null && position > bestPosition) {
                bestResult = result
                bestPosition = position
            }
        }

        position = bestPosition
        return bestResult
    }
}
