package org.dicio.numbers.formatter.param

import org.dicio.numbers.formatter.Formatter
import org.dicio.numbers.unit.Duration

/**
 * TODO documentation
 */
class NiceDurationParameters(
    private val formatter: Formatter, private val duration: Duration
) {
    // default values
    private var speech = true

    fun speech(speech: Boolean): NiceDurationParameters {
        this.speech = speech
        return this
    }

    fun get(): String {
        return formatter.niceDuration(duration, speech)
    }
}
