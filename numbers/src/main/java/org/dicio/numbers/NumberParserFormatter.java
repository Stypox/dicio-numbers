package org.dicio.numbers;

import org.dicio.numbers.param.NiceDurationParameters;
import org.dicio.numbers.param.NiceNumberParameters;
import org.dicio.numbers.param.NiceTimeParameters;
import org.dicio.numbers.param.PronounceNumberParameters;

import java.time.Duration;
import java.time.LocalTime;

public final class NumberParserFormatter {
    private final NumberFormatter formatter;
    private final NumberParser parser;

    public NumberParserFormatter(final NumberFormatter formatter, final NumberParser parser) {
        this.formatter = formatter;
        this.parser = parser;
    }

    public final NiceNumberParameters niceNumber(final double number) {
        return new NiceNumberParameters(formatter, number);
    }

    public final PronounceNumberParameters pronounceNumber(final double number) {
        return new PronounceNumberParameters(formatter, number);
    }

    public final NiceTimeParameters niceTime(final LocalTime time) {
        return new NiceTimeParameters(formatter, time);
    }

    public final NiceDurationParameters niceDuration(final Duration duration) {
        return new NiceDurationParameters(formatter, duration);
    }
}
