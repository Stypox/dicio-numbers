package org.dicio.numbers.parser.param;

import org.dicio.numbers.parser.NumberParser;

import java.util.List;

public class ExtractNumbersParams {
    // TODO documentation
    private final NumberParser numberParser;
    private final String utterance;

    // default values
    private boolean shortScale = true;
    private boolean preferOrdinal = false;

    public ExtractNumbersParams(final NumberParser numberParser, final String utterance) {
        this.numberParser = numberParser;
        this.utterance = utterance;
    }

    public ExtractNumbersParams shortScale(final boolean shortScale) {
        this.shortScale = shortScale;
        return this;
    }

    public ExtractNumbersParams preferOrdinal(final boolean preferOrdinal) {
        this.preferOrdinal = preferOrdinal;
        return this;
    }

    public List<Object> get() {
        return numberParser.extractNumbers(utterance, shortScale, preferOrdinal);
    }
}
