package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.NumberFormatter;

public class PronounceNumberParameters {

    private final NumberFormatter numberFormatter;
    private final double number;

    // default values
    private int places = 2;
    private boolean shortScale = true;
    private boolean scientific = false;
    private boolean ordinals = false;

    public PronounceNumberParameters(final NumberFormatter numberFormatter, final double number) {
        this.numberFormatter = numberFormatter;
        this.number = number;
    }


    /**
     * @param places the number of decimal places to round decimal numbers to. The default is 2.
     * @return this
     */
    public PronounceNumberParameters places(final int places) {
        this.places = places;
        return this;
    }

    /**
     * @param shortScale use short (true) or long (false) scale for large numbers (see
     *                   <a href="https://en.wikipedia.org/wiki/Names_of_large_numbers">
     *                       Names of large numbers</a>). The default is true.
     * @return this
     */
    public PronounceNumberParameters shortScale(final boolean shortScale) {
        this.shortScale = shortScale;
        return this;
    }

    /**
     * @param scientific if true convert and pronounce in scientific notation. The default is false.
     * @return this
     */
    public PronounceNumberParameters scientific(final boolean scientific) {
        this.scientific = scientific;
        return this;
    }

    /**
     * @param ordinals if true pronounce in the ordinal form (e.g. "first" instead of "one" for
     *                 English). The default is false.
     * @return this
     */
    public PronounceNumberParameters ordinals(final boolean ordinals) {
        this.ordinals = ordinals;
        return this;
    }

    /**
     * Calls {@link NumberFormatter#pronounceNumber(double, int, boolean, boolean, boolean)} with
     * the stored parameters.
     *
     * @return the formatted number as a string
     */
    public String get() {
        return numberFormatter.pronounceNumber(number, places, shortScale, scientific, ordinals);
    }
}
