package org.dicio.numbers.formatter.param

import org.dicio.numbers.formatter.Formatter
import java.time.LocalDate

/**
 * TODO documentation
 */
class NiceDateParameters(
    private val formatter: Formatter, private val date: LocalDate
) {
    // default values
    private var now: LocalDate? = null

    fun now(now: LocalDate?): NiceDateParameters {
        this.now = now
        return this
    }

    fun get(): String {
        return formatter.niceDate(date, now)
    }
}
