package org.dicio.numbers

import org.dicio.numbers.formatter.Formatter
import org.dicio.numbers.formatter.param.NiceDateParameters
import org.dicio.numbers.formatter.param.NiceDateTimeParameters
import org.dicio.numbers.formatter.param.NiceDurationParameters
import org.dicio.numbers.formatter.param.NiceNumberParameters
import org.dicio.numbers.formatter.param.NiceTimeParameters
import org.dicio.numbers.formatter.param.NiceYearParameters
import org.dicio.numbers.formatter.param.PronounceNumberParameters
import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.param.ExtractDateTimeParams
import org.dicio.numbers.parser.param.ExtractDurationParams
import org.dicio.numbers.parser.param.ExtractNumberParams
import org.dicio.numbers.unit.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale

/**
 * A class that wraps a [Formatter] and a [Parser] for a particular language
 * and provides convenience methods to call the available functions without having to provide all of
 * the default parameters.
 */
class ParserFormatter {
    private val formatter: Formatter?
    private val parser: Parser?

    /**
     * Constructs a [ParserFormatter] that wraps the provided [Formatter]
     * and [Parser]. Note: do not use this manually, prefer
     * [ParserFormatter]. This is mostly used for tests.
     *
     * @param formatter the formatter to wrap
     * @param parser the parser to wrap
     */
    constructor(formatter: Formatter?, parser: Parser?) {
        this.formatter = formatter
        this.parser = parser
    }

    /**
     * Constructs a [ParserFormatter] for the language of the provided locale.
     *
     * @param locale the locale containing the language to use
     * @throws IllegalArgumentException if the provided locale is not supported
     */
    constructor(locale: Locale) {
        val parserFormatterPair = ParserFormatterBuilder.parserFormatterPairForLocale(locale)
        this.formatter = parserFormatterPair.formatter
        this.parser = parserFormatterPair.parser
    }


    /**
     * Used to format the provided number to a human readable representation of the corresponding
     * mixed fraction, if possible, or of the number itself otherwise. For example, 5.75 would be
     * formatted into "five and three quarters" for English.
     *
     * @param number the number to format
     * @return an instance of a utility class that enables customizing various parameters before
     * calling [Formatter.niceNumber]. See
     * [NiceNumberParameters].
     */
    fun niceNumber(number: Double): NiceNumberParameters {
        return NiceNumberParameters(formatter!!, number)
    }

    /**
     * Used to format the provided number to a pronounceable representation. For example, -4000619
     * would be formatted into "minus four million, six hundred and nineteen" for English.
     *
     * @param number the number to format
     * @return an instance of a utility class that enables customizing various parameters before
     * calling [Formatter.pronounceNumber]. See [PronounceNumberParameters].
     */
    fun pronounceNumber(number: Double): PronounceNumberParameters {
        return PronounceNumberParameters(formatter!!, number)
    }

    /**
     * Used to format the provided date to a pronounceable representation. For example, 2021/4/28
     * would be formatted as "wednesday, april twenty-eighth, twenty twenty one" for English.
     *
     * @param date the date to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     * calling [Formatter.niceDate]. See [         ].
     */
    fun niceDate(date: LocalDate): NiceDateParameters {
        return NiceDateParameters(formatter!!, date)
    }

    /**
     * Used to format the year from the provided date to a pronounceable year. For example, year
     * 1984 would be formatted as "nineteen eighty four" for English.
     *
     * @param date the date containing the year to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     * calling [Formatter.niceYear]. See [NiceYearParameters].
     */
    fun niceYear(date: LocalDate): NiceYearParameters {
        // note: useless encapsulation, since niceYear has only the mandatory date parameter, but
        // keep for consistency
        return NiceYearParameters(formatter!!, date)
    }

    /**
     * Used to format the provided time to a human readable representation. For example, 5:30 would
     * be formatted as "five thirty" for English.
     *
     * @param time the time to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     * calling [Formatter.niceTime]. See
     * [NiceTimeParameters].
     */
    fun niceTime(time: LocalTime): NiceTimeParameters {
        return NiceTimeParameters(formatter!!, time)
    }

    /**
     * Used to format the provided date time to a pronounceable date and time. For example,
     * 2021/4/28 5:30 would be formatted as "wednesday, april twenty-eighth, twenty twenty one at
     * five thirty" for English.
     *
     * @param dateTime the date time to format (assumes already in local timezone)
     * @return an instance of a utility class that enables customizing various parameters before
     * calling [Formatter.niceDateTime]. See [NiceDateTimeParameters].
     */
    fun niceDateTime(dateTime: LocalDateTime): NiceDateTimeParameters {
        return NiceDateTimeParameters(formatter!!, dateTime)
    }

    /**
     * Used to format the provided duration to a human readable representation. For example, 12 days
     * 3:23:01 would be formatted as "twelve days three hours twenty three minutes one second".
     *
     * @param duration the duration to format
     * @return an instance of a utility class that enables customizing various parameters before
     * calling [Formatter.niceDuration]. See [NiceDurationParameters].
     */
    fun niceDuration(duration: Duration): NiceDurationParameters {
        return NiceDurationParameters(formatter!!, duration)
    }

    /**
     * Used to extract numbers from a string. For example, "I am twenty three years old" would be
     * parsed as "I am ", 23, " years old".
     *
     * @param utterance the text to extract numbers from
     * @return an instance of a utility class that enables customizing various parameters and then
     * allows calling [Parser.extractNumber] in
     * multiple ways. See [ExtractNumberParams].
     */
    fun extractNumber(utterance: String): ExtractNumberParams {
        return ExtractNumberParams(parser!!, utterance)
    }

    /**
     * Used to extract a duration from a string. For example, "Set a timer for three minutes and
     * five seconds" would be parsed as "Set a timer for ", 185 seconds.
     *
     * @param utterance the text to extract a duration from
     * @return an instance of a utility class that enables customizing various parameters and then
     * allows calling [Parser.extractDuration] in multiple
     * ways. See [ExtractDurationParams].
     */
    fun extractDuration(utterance: String): ExtractDurationParams {
        return ExtractDurationParams(parser!!, utterance)
    }

    /**
     * Used to extract a date&time from a string. For example, "Set an alarm at five p.m." would be
     * parsed as "Set an alarm ", today at 5 PM.
     *
     * @param utterance the text to extract a date&time from
     * @return an instance of a utility class that enables customizing various parameters and then
     * allows calling [Parser.extractDateTime] in
     * multiple ways. See [ExtractDateTimeParams].
     */
    fun extractDateTime(utterance: String): ExtractDateTimeParams {
        return ExtractDateTimeParams(parser!!, utterance)
    }
}
