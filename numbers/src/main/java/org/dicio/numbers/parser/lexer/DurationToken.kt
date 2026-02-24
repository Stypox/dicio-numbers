package org.dicio.numbers.parser.lexer

import org.dicio.numbers.unit.Duration

class DurationToken internal constructor(
    value: String,
    spacesFollowing: String,
    positionInOriginalString: Int,
    // this is basically the duration multiplier, but in plain text
    val durationCategory: String,
    val durationMultiplier: Duration,
    val isRestrictedAfterNumber: Boolean
) : Token(value, spacesFollowing, positionInOriginalString) {
    override val asDurationToken = this
}
