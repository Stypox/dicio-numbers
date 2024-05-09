package org.dicio.numbers.formatter.param

import org.dicio.numbers.formatter.Formatter
import java.time.LocalTime

/**
 * TODO documentation
 */
class NiceTimeParameters(
    private val formatter: Formatter, private val time: LocalTime
) {
    // default values
    private var speech = true
    private var use24Hour = false
    private var showAmPm = false

    fun speech(speech: Boolean): NiceTimeParameters {
        this.speech = speech
        return this
    }

    fun use24Hour(use24Hour: Boolean): NiceTimeParameters {
        this.use24Hour = use24Hour
        return this
    }

    fun showAmPm(showAmPm: Boolean): NiceTimeParameters {
        this.showAmPm = showAmPm
        return this
    }

    fun get(): String {
        return formatter.niceTime(time, speech, use24Hour, showAmPm)
    }
}
