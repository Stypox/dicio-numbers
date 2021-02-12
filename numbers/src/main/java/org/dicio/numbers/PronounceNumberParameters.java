package org.dicio.numbers;

public class PronounceNumberParameters {

    private final NumberParseFormat numberParseFormat;
    private final double number;

    // default values
    private int places = 2;
    private boolean shortScale = true;
    private boolean scientific = false;
    private boolean ordinals = false;

    PronounceNumberParameters(final NumberParseFormat numberParseFormat, final double number) {
        this.numberParseFormat = numberParseFormat;
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
        return numberParseFormat.pronounceNumber(number, places, shortScale, scientific, ordinals);
    }
}
