package org.dicio.numbers.formatter.param

import org.dicio.numbers.formatter.Formatter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * TODO documentation
 */
class NiceDateTimeParameters(
    private val formatter: Formatter,
    dateTime: LocalDateTime
) {
    private val date: LocalDate = dateTime.toLocalDate()
    private val time: LocalTime = dateTime.toLocalTime()

    // default values
    private var now: LocalDate? = null
    private var use24Hour = false
    private var showAmPm = false

    /**
     *
     * @param now nullable
     * @return
     */
    fun now(now: LocalDate?): NiceDateTimeParameters {
        this.now = now
        return this
    }

    fun use24Hour(use24Hour: Boolean): NiceDateTimeParameters {
        this.use24Hour = use24Hour
        return this
    }

    fun showAmPm(showAmPm: Boolean): NiceDateTimeParameters {
        this.showAmPm = showAmPm
        return this
    }

    fun get(): String {
        return formatter.niceDateTime(date, now, time, use24Hour, showAmPm)
    }
}
