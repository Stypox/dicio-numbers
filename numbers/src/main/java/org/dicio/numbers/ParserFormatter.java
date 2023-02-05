package org.dicio.numbers;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.formatter.param.NiceDateParameters;
import org.dicio.numbers.formatter.param.NiceDateTimeParameters;
import org.dicio.numbers.formatter.param.NiceDurationParameters;
import org.dicio.numbers.formatter.param.NiceNumberParameters;
import org.dicio.numbers.formatter.param.NiceTimeParameters;
import org.dicio.numbers.formatter.param.NiceYearParameters;
import org.dicio.numbers.formatter.param.PronounceNumberParameters;
import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.parser.param.ExtractDateTimeParams;
import org.dicio.numbers.parser.param.ExtractDurationParams;
import org.dicio.numbers.parser.param.ExtractNumberParams;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.MixedFraction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

/**
 * A class that wraps a {@link Formatter} and a {@link Parser} for a particular language
 * and provides convenience methods to call the available functions without having to provide all of
 * the default parameters.
 */
public final class ParserFormatter {
    private final Formatter formatter;
    private final Parser parser;

    /**
     * Constructs a {@link ParserFormatter} that wraps the provided {@link Formatter}
     * and {@link Parser}. Note: do not use this manually, prefer
     * {@link ParserFormatter}. This is mostly used for tests.
     *
     * @param formatter the formatter to wrap
     * @param parser the parser to wrap
     */
    public ParserFormatter(final Formatter formatter, final Parser parser) {
        this.formatter = formatter;
        this.parser = parser;
    }

    /**
     * Constructs a {@link ParserFormatter} for the language of the provided locale.
     *
     * @param locale the locale containing the language to use
     * @throws IllegalArgumentException if the provided locale is not supported
     */
    public ParserFormatter(final Locale locale) throws IllegalArgumentException {
        final ParserFormatterBuilder.ParserFormatterPair parserFormatterPair
                = ParserFormatterBuilder.parserFormatterPairForLocale(locale);
        this.formatter = parserFormatterPair.formatter;
        this.parser = parserFormatterPair.parser;
    }


    /**
     * Used to format the provided number to a human readable representation of the corresponding
     * mixed fraction, if possible, or of the number itself otherwise. For example, 5.75 would be
     * formatted into "five and three quarters" for English.
     *
     * @param number the number to format
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link Formatter#niceNumber(MixedFraction, boolean)}. See
     *         {@link NiceNumberParameters}.
     */
    public NiceNumberParameters niceNumber(final double number) {
        return new NiceNumberParameters(formatter, number);
    }

    /**
     * Used to format the provided number to a pronounceable representation. For example, -4000619
     * would be formatted into "minus four million, six hundred and nineteen" for English.
     *
     * @param number the number to format
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link Formatter#pronounceNumber(double, int, boolean, boolean,
     *         boolean)}. See {@link PronounceNumberParameters}.
     */
    public PronounceNumberParameters pronounceNumber(final double number) {
        return new PronounceNumberParameters(formatter, number);
    }

    /**
     * Used to format the provided date to a pronounceable representation. For example, 2021/4/28
     * would be formatted as "wednesday, april twenty-eighth, twenty twenty one" for English.
     *
     * @param date the date to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link Formatter#niceDate(LocalDate, LocalDate)}. See {@link
     *         NiceDateParameters}.
     */
    public NiceDateParameters niceDate(final LocalDate date) {
        return new NiceDateParameters(formatter, date);
    }

    /**
     * Used to format the year from the provided date to a pronounceable year. For example, year
     * 1984 would be formatted as "nineteen eighty four" for English.
     *
     * @param date the date containing the year to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link Formatter#niceYear(LocalDate)}. See {@link NiceYearParameters}.
     */
    public NiceYearParameters niceYear(final LocalDate date) {
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
     *         calling {@link Formatter#niceTime(LocalTime, boolean, boolean, boolean)}. See
     *         {@link NiceTimeParameters}.
     */
    public NiceTimeParameters niceTime(final LocalTime time) {
        return new NiceTimeParameters(formatter, time);
    }

    /**
     * Used to format the provided date time to a pronounceable date and time. For example,
     * 2021/4/28 5:30 would be formatted as "wednesday, april twenty-eighth, twenty twenty one at
     * five thirty" for English.
     *
     * @param dateTime the date time to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link Formatter#niceDateTime(LocalDate, LocalDate, LocalTime, boolean,
     *         boolean)}. See {@link NiceDateTimeParameters}.
     */
    public NiceDateTimeParameters niceDateTime(final LocalDateTime dateTime) {
        return new NiceDateTimeParameters(formatter, dateTime);
    }

    /**
     * Used to format the provided duration to a human readable representation. For example, 12 days
     * 3:23:01 would be formatted as "twelve days three hours twenty three minutes one second".
     *
     * @param duration the duration to format
     * @return an instance of a utility class that enables customizing various parameters before
     *         calling {@link Formatter#niceDuration(Duration, boolean)}. See {@link
     *         NiceDurationParameters}.
     */
    public NiceDurationParameters niceDuration(final Duration duration) {
        return new NiceDurationParameters(formatter, duration);
    }

    /**
     * Used to extract numbers from a string. For example, "I am twenty three years old" would be
     * parsed as "I am ", 23, " years old".
     *
     * @param utterance the text to extract numbers from
     * @return an instance of a utility class that enables customizing various parameters and then
     *         allows calling {@link Parser#extractNumber(TokenStream, boolean, boolean)} in
     *         multiple ways. See {@link ExtractNumberParams}.
     */
    public ExtractNumberParams extractNumber(final String utterance) {
        return new ExtractNumberParams(parser, utterance);
    }

    /**
     * Used to extract a duration from a string. For example, "Set a timer for three minutes and
     * five seconds" would be parsed as "Set a timer for ", 185 seconds.
     *
     * @param utterance the text to extract a duration from
     * @return an instance of a utility class that enables customizing various parameters and then
     *         allows calling {@link Parser#extractDuration(TokenStream, boolean)} in multiple
     *         ways. See {@link ExtractDurationParams}.
     */
    public ExtractDurationParams extractDuration(final String utterance) {
        return new ExtractDurationParams(parser, utterance);
    }

    /**
     * Used to extract a date&time from a string. For example, "Set an alarm at five p.m." would be
     * parsed as "Set an alarm ", today at 5 PM.
     *
     * @param utterance the text to extract a date&time from
     * @return an instance of a utility class that enables customizing various parameters and then
     *         allows calling {@link Parser#extractDateTime(TokenStream, LocalDateTime)} in
     *         multiple ways. See {@link ExtractDateTimeParams}.
     */
    public ExtractDateTimeParams extractDateTime(final String utterance) {
        return new ExtractDateTimeParams(parser, utterance);
    }
}
