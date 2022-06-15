package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.unit.MixedFraction;
import org.dicio.numbers.util.Utils;

import java.util.List;

import static org.dicio.numbers.unit.MixedFraction.DEFAULT_DENOMINATORS;

public class NiceNumberParameters {

    private final Formatter formatter;
    private final double number;

    // default values
    private boolean speech = true;
    private List<Integer> denominators = DEFAULT_DENOMINATORS;

    public NiceNumberParameters(final Formatter formatter, final double number) {
        this.formatter = formatter;
        this.number = number;
    }


    /**
     * @param speech format for speech (true) or display (false). The default is true.
     * @return this
     */
    public NiceNumberParameters speech(final boolean speech) {
        this.speech = speech;
        return this;
    }

    /**
     * @param denominators the denominators to use to extract the mixed fraction. The default is
     *                     {@link MixedFraction#DEFAULT_DENOMINATORS}, i.e. a list of all of the
     *                     numbers from 2 to 20 inclusive.
     * @return this
     */
    public NiceNumberParameters denominators(final List<Integer> denominators) {
        this.denominators = denominators;
        return this;
    }

    /**
     * Tries to extract a mixed fraction from the number provided at the beginning, using the stored
     * denominators, and calls {@link Formatter#niceNumber(MixedFraction, boolean)} on it,
     * also providing the stored speech value. If a fraction could not approximate the original
     * number close enough, the number is instead formatted using {@link
     * Formatter#pronounceNumber(double, int, boolean, boolean, boolean)} if the stored speech
     * is true, otherwise it is converted to a string using "%f".
     *
     * @return the formatted mixed fraction as a string
     */
    public String get() {
        final MixedFraction mixedFraction = MixedFraction.of(number, denominators);
        if (mixedFraction == null) {
            // unable to convert to fraction
            if (speech) {
                return formatter.pronounceNumber(number, 2, true, false, false);
            } else {
                final int realPlaces = Utils.decimalPlacesNoFinalZeros(number, 2);
                return String.format("%." + realPlaces + "f", number);
            }
        } else {
            return formatter.niceNumber(mixedFraction, speech);
        }
    }
}
