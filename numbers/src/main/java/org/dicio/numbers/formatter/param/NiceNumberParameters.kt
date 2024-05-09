package org.dicio.numbers.formatter.param

import org.dicio.numbers.formatter.Formatter
import org.dicio.numbers.unit.MixedFraction
import org.dicio.numbers.util.Utils

class NiceNumberParameters(private val formatter: Formatter, private val number: Double) {
    // default values
    private var speech = true
    private var denominators: List<Int> = MixedFraction.DEFAULT_DENOMINATORS


    /**
     * @param speech format for speech (true) or display (false). The default is true.
     * @return this
     */
    fun speech(speech: Boolean): NiceNumberParameters {
        this.speech = speech
        return this
    }

    /**
     * @param denominators the denominators to use to extract the mixed fraction. The default is
     * [MixedFraction.DEFAULT_DENOMINATORS], i.e. a list of all of the
     * numbers from 2 to 20 inclusive.
     * @return this
     */
    fun denominators(denominators: List<Int>): NiceNumberParameters {
        this.denominators = denominators
        return this
    }

    /**
     * Tries to extract a mixed fraction from the number provided at the beginning, using the stored
     * denominators, and calls [Formatter.niceNumber] on it,
     * also providing the stored speech value. If a fraction could not approximate the original
     * number close enough, the number is instead formatted using [Formatter.pronounceNumber] if the
     * stored speech is true, otherwise it is converted to a string using "%f".
     *
     * @return the formatted mixed fraction as a string
     */
    fun get(): String {
        val mixedFraction = MixedFraction.of(number, denominators)
        if (mixedFraction == null) {
            // unable to convert to fraction
            if (speech) {
                return formatter.pronounceNumber(number, 2, true, false, false)
            } else {
                val realPlaces = Utils.decimalPlacesNoFinalZeros(
                    number, 2
                )
                return String.format("%." + realPlaces + "f", number)
            }
        } else {
            return formatter.niceNumber(mixedFraction, speech)
        }
    }
}
