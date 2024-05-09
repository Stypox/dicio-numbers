package org.dicio.numbers.parser.lexer

open class MatchedToken internal constructor(
    value: String,
    spacesFollowing: String,
    private val categories: Set<String>
) : Token(value, spacesFollowing) {
    private var durationTokenMatch: DurationToken? = null

    fun setDurationTokenMatch(durationTokenMatch: DurationToken?) {
        this.durationTokenMatch = durationTokenMatch
    }

    override fun hasCategory(category: String): Boolean {
        return categories.contains(category)
    }

    override val asDurationToken: DurationToken?
        get() = durationTokenMatch
}
