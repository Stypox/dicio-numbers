package org.dicio.numbers;

import org.dicio.numbers.formatter.NumberFormatter;
import org.dicio.numbers.formatter.param.NiceDateParameters;
import org.dicio.numbers.formatter.param.NiceDateTimeParameters;
import org.dicio.numbers.formatter.param.NiceDurationParameters;
import org.dicio.numbers.formatter.param.NiceNumberParameters;
import org.dicio.numbers.formatter.param.NiceTimeParameters;
import org.dicio.numbers.formatter.param.NiceYearParameters;
import org.dicio.numbers.formatter.param.PronounceNumberParameters;
import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.parser.param.ExtractNumbersParams;
import org.dicio.numbers.util.MixedFraction;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A class that wraps a {@link NumberFormatter} and a {@link NumberParser} for a particular language
 * and provides convenience methods to call the available functions without having to provide all of
 * the default parameters.
 */
public final class NumberParserFormatter {
    private final NumberFormatter formatter;
    private final NumberParser parser;

    /**
     * Constructs a {@link NumberParserFormatter} that wraps the provided {@link NumberFormatter}
     * and {@link NumberParser}.
     *
     * @param formatter the formatter to wrap
     * @param parser the parser to wrap
     */
    public NumberParserFormatter(final NumberFormatter formatter, final NumberParser parser) {
        this.formatter = formatter;
        this.parser = parser;
    }


    /**
     * Used to format the provided number to a human readable representation of the corresponding
     * mixed fraction, if possible, or of the number itself otherwise. For example, 5.75 would be
     * formatted into "five and three quarters" for English.
     *
     * @param number the number to format
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link NumberFormatter#niceNumber(MixedFraction, boolean)}. See
     *         {@link NiceNumberParameters}.
     */
    public final NiceNumberParameters niceNumber(final double number) {
        return new NiceNumberParameters(formatter, number);
    }

    /**
     * Used to format the provided number to a pronounceable representation. For example, -4000619
     * would be formatted into "minus four million, six hundred and nineteen" for English.
     *
     * @param number the number to format
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link NumberFormatter#pronounceNumber(double, int, boolean, boolean,
     *         boolean)}. See {@link PronounceNumberParameters}.
     */
    public final PronounceNumberParameters pronounceNumber(final double number) {
        return new PronounceNumberParameters(formatter, number);
    }

    /**
     * Used to format the provided date to a pronounceable representation. For example, 2021/4/28
     * would be formatted as "wednesday, april twenty-eighth, twenty twenty one" for English.
     *
     * @param date the date to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link NumberFormatter#niceDate(LocalDate, LocalDate)}. See {@link
     *         NiceDateParameters}.
     */
    public final NiceDateParameters niceDate(final LocalDate date) {
        return new NiceDateParameters(formatter, date);
    }

    /**
     * Used to format the year from the provided date to a pronounceable year. For example, year
     * 1984 would be formatted as "nineteen eighty four" for English.
     *
     * @param date the date containing the year to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link NumberFormatter#niceYear(LocalDate)}. See {@link NiceYearParameters}.
     */
    public final NiceYearParameters niceYear(final LocalDate date) {
        // note: useless encapsulation, since niceYear has only the mandatory date parameter, but
        // keep for consistency
        return new NiceYearParameters(formatter, date);
    }

    /**
     * Used to format the provided time to a human readable representation. For example, 5:30 would
     * be formatted as "five thirty" for English.
     *
     * @param time the time to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link NumberFormatter#niceTime(LocalTime, boolean, boolean, boolean)}. See
     *         {@link NiceTimeParameters}.
     */
    public final NiceTimeParameters niceTime(final LocalTime time) {
        return new NiceTimeParameters(formatter, time);
    }

    /**
     * Used to format the provided date time to a pronounceable date and time. For example,
     * 2021/4/28 5:30 would be formatted as "wednesday, april twenty-eighth, twenty twenty one at
     * five thirty" for English.
     *
     * @param dateTime the date time to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link NumberFormatter#niceDateTime(LocalDate, LocalDate, LocalTime, boolean,
     *         boolean)}. See {@link NiceDateTimeParameters}.
     */
    public final NiceDateTimeParameters niceDateTime(final LocalDateTime dateTime) {
        return new NiceDateTimeParameters(formatter, dateTime);
    }

    /**
     * Used to format the provided duration to a human readable representation. For example, 12 days
     * 3:23:01 would be formatted as "twelve days three hours twenty three minutes one second".
     *
     * @param duration the duration to format
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link NumberFormatter#niceDuration(Duration, boolean)}. See {@link
     *         NiceDurationParameters}.
     */
    public final NiceDurationParameters niceDuration(final Duration duration) {
        return new NiceDurationParameters(formatter, duration);
    }

    /**
     * Used to extract numbers from a string. For example, "I am twenty three years old" would be
     * parsed as "I am ", 23, " years old".
     *
     * @param utterance the text to extract numbers from
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link NumberParser#extractNumbers(String, boolean, boolean)}. See {@link
     *         ExtractNumbersParams}.
     */
    public final ExtractNumbersParams extractNumbers(final String utterance) {
        return new ExtractNumbersParams(parser, utterance);
    }
}
