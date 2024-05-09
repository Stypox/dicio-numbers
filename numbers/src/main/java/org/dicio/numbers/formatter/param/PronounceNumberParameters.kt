package org.dicio.numbers.formatter.param

import org.dicio.numbers.formatter.Formatter

class PronounceNumberParameters(private val formatter: Formatter, private val number: Double) {
    // default values
    private var places = 2
    private var shortScale = true
    private var scientific = false
    private var ordinal = false


    /**
     * @param places the number of decimal places to round decimal numbers to. The default is 2.
     * @return this
     */
    fun places(places: Int): PronounceNumberParameters {
        this.places = places
        return this
    }

    /**
     * @param shortScale use short (true) or long (false) scale for large numbers (see
     * [Names of large numbers](https://en.wikipedia.org/wiki/Names_of_large_numbers)).
     * The default is true.
     * @return this
     */
    fun shortScale(shortScale: Boolean): PronounceNumberParameters {
        this.shortScale = shortScale
        return this
    }

    /**
     * @param scientific if true convert and pronounce in scientific notation. The default is false.
     * @return this
     */
    fun scientific(scientific: Boolean): PronounceNumberParameters {
        this.scientific = scientific
        return this
    }

    /**
     * @param ordinal if true pronounce in the ordinal form (e.g. "first" instead of "one" for
     * English). The default is false.
     * @return this
     */
    fun ordinal(ordinal: Boolean): PronounceNumberParameters {
        this.ordinal = ordinal
        return this
    }

    /**
     * Calls [Formatter.pronounceNumber] with
     * the stored parameters.
     *
     * @return the formatted number as a string
     */
    fun get(): String {
        return formatter.pronounceNumber(number, places, shortScale, scientific, ordinal)
    }
}
