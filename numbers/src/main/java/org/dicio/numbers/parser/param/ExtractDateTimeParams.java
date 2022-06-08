package org.dicio.numbers.parser.param;

import org.dicio.numbers.parser.NumberParser;

import java.time.LocalDateTime;

public class ExtractDateTimeParams {
    // TODO documentation
    private final NumberParser numberParser;
    private final String utterance;

    // default values
    private LocalDateTime now = LocalDateTime.now();

    public ExtractDateTimeParams(final NumberParser numberParser, final String utterance) {
        this.numberParser = numberParser;
        this.utterance = utterance;
    }

    public ExtractDateTimeParams now(final LocalDateTime now) {
        this.now = now;
        return this;
    }

    public LocalDateTime get() {
        return numberParser.extractDateTime(utterance, now);
    }
}
