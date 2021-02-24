package org.dicio.numbers;

import org.dicio.numbers.param.NiceDateParameters;
import org.dicio.numbers.param.NiceDurationParameters;
import org.dicio.numbers.param.NiceNumberParameters;
import org.dicio.numbers.param.NiceTimeParameters;
import org.dicio.numbers.param.NiceYearParameters;
import org.dicio.numbers.param.PronounceNumberParameters;

import java.time.Duration;
import java.time.LocalDate;
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

    public final NiceDateParameters niceDate(final LocalDate date) {
        return new NiceDateParameters(formatter, date);
    }

    public final NiceYearParameters niceYear(final LocalDate date) {
        // note: useless encapsulation, since niceYear has only the mandatory date parameter, but
        // keep for consistency
        return new NiceYearParameters(formatter, date);
    }

    public final NiceTimeParameters niceTime(final LocalTime time) {
        return new NiceTimeParameters(formatter, time);
    }

    public final NiceDurationParameters niceDuration(final Duration duration) {
        return new NiceDurationParameters(formatter, duration);
    }
}
