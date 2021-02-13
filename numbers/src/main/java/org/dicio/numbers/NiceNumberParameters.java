package org.dicio.numbers;

import java.util.List;

import static org.dicio.numbers.MixedFraction.DEFAULT_DENOMINATORS;

public class NiceNumberParameters {

    private final NumberParseFormat numberParseFormat;
    private final double number;

    // default values
    private boolean speech = true;
    private List<Integer> denominators = DEFAULT_DENOMINATORS;

    NiceNumberParameters(final NumberParseFormat numberParseFormat, final double number) {
        this.numberParseFormat = numberParseFormat;
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
                return numberParseFormat.pronounceNumber(number, 2, true, false, false);
            } else {
                final int realPlaces = Utils.decimalPlacesNoFinalZeros(number, 2);
                return String.format("%." + realPlaces + "f", number);
            }
        } else {
            return numberParseFormat.niceNumber(mixedFraction, speech);
        }
    }
}
