package org.dicio.numbers.param;

import org.dicio.numbers.NumberFormatter;

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

    public PronounceNumberParameters places(final int places) {
        this.places = places;
        return this;
    }

    public PronounceNumberParameters shortScale(final boolean shortScale) {
        this.shortScale = shortScale;
        return this;
    }

    public PronounceNumberParameters scientific(final boolean scientific) {
        this.scientific = scientific;
        return this;
    }

    public PronounceNumberParameters ordinals(final boolean ordinals) {
        this.ordinals = ordinals;
        return this;
    }

    public String get() {
        return numberFormatter.pronounceNumber(number, places, shortScale, scientific, ordinals);
    }
}
