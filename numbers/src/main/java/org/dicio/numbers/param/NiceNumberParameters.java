package org.dicio.numbers.param;

import org.dicio.numbers.NumberFormatter;
import org.dicio.numbers.util.MixedFraction;
import org.dicio.numbers.util.Utils;

import java.util.List;

import static org.dicio.numbers.util.MixedFraction.DEFAULT_DENOMINATORS;

public class NiceNumberParameters {

    private final NumberFormatter numberFormatter;
    private final double number;

    // default values
    private boolean speech = true;
    private List<Integer> denominators = DEFAULT_DENOMINATORS;

    public NiceNumberParameters(final NumberFormatter numberFormatter, final double number) {
        this.numberFormatter = numberFormatter;
        this.number = number;
    }

    public NiceNumberParameters speech(final boolean speech) {
        this.speech = speech;
        return this;
    }

    public NiceNumberParameters denominators(final List<Integer> denominators) {
        this.denominators = denominators;
        return this;
    }

    public String get() {
        final MixedFraction mixedFraction = MixedFraction.of(number, denominators);
        if (mixedFraction == null) {
            // unable to convert to fraction
            if (speech) {
                return numberFormatter.pronounceNumber(number, 2, true, false, false);
            } else {
                final int realPlaces = Utils.decimalPlacesNoFinalZeros(number, 2);
                return String.format("%." + realPlaces + "f", number);
            }
        } else {
            return numberFormatter.niceNumber(mixedFraction, speech);
        }
    }
}
