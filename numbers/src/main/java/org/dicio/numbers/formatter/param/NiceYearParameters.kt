package org.dicio.numbers.formatter.param

import org.dicio.numbers.formatter.Formatter
import java.time.LocalDate

/**
 * Note: this class is useless, since the niceYear function only takes one mandatory argument, but
 * is used anyway in NumberParserFormatter to keep consistency with other functions.
 *
 * TODO documentation
 */
class NiceYearParameters(
    private val formatter: Formatter, private val date: LocalDate
) {
    fun get(): String {
        return formatter.niceYear(date)
    }
}
