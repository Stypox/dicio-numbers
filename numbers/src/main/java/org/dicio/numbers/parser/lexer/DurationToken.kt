package org.dicio.numbers.parser.lexer

import org.dicio.numbers.unit.Duration

class DurationToken internal constructor(
    value: String,
    spacesFollowing: String,
    // this is basically the duration multiplier, but in plain text
    val durationCategory: String,
    val durationMultiplier: Duration,
    val isRestrictedAfterNumber: Boolean
) : Token(value, spacesFollowing) {
    override val asDurationToken = this
}
