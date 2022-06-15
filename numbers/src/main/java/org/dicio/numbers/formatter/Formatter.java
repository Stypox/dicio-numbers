package org.dicio.numbers.formatter;

import org.dicio.numbers.formatter.datetime.DateTimeConfig;
import org.dicio.numbers.formatter.datetime.FormatString;
import org.dicio.numbers.formatter.datetime.NiceYearSubstitutionTableBuilder;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.MixedFraction;
import org.dicio.numbers.util.Utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public abstract class Formatter {

    protected final DateTimeConfig config;

    protected Formatter(final String configFolder) {
        config = new DateTimeConfig(configFolder);
    }


    /**
     * Format a mixed fraction to a human readable representation. For example, 5 + 3/4 would be
     * formatted into "five and three quarters" for English.
     *
     * @param mixedFraction the mixed fraction to format
     * @param speech format for speech (true) or display (false)
     * @return the formatted mixed fraction as a string
     */
    public abstract String niceNumber(MixedFraction mixedFraction, boolean speech);

    protected final String niceNumberNotSpeech(MixedFraction mixedFraction) {
        final String sign = mixedFraction.negative ? "-" : "";
        if (mixedFraction.numerator == 0) {
            return sign + mixedFraction.whole;
        } else if (mixedFraction.whole == 0) {
            return sign + mixedFraction.numerator + "/" + mixedFraction.denominator;
        } else {
            return sign + mixedFraction.whole + " "
                    + mixedFraction.numerator + "/" + mixedFraction.denominator;
        }
    }

    /**
     * Format a number to a pronounceable representation. For example, -4000619 would be formatted
     * into "minus four million, six hundred and nineteen" for English.
     *
     * @param number the number to pronounce
     * @param places the number of decimal places to round decimal numbers to
     * @param shortScale use short (true) or long (false) scale for large numbers (see
     *                   <a href="https://en.wikipedia.org/wiki/Names_of_large_numbers">
     *                       Names of large numbers</a>)
     * @param scientific if true convert and pronounce in scientific notation
     * @param ordinal if true pronounce in the ordinal form (e.g. "first" instead of "one" for
     *                 English)
     * @return the formatted number as a string
     */
    public abstract String pronounceNumber(double number,
                                           int places,
                                           boolean shortScale,
                                           boolean scientific,
                                           boolean ordinal);

    /**
     * Format a date to a pronounceable representation. For example, 2021/4/28 would be formatted
     * as "wednesday, april twenty-eighth, twenty twenty one" for English.
     *
     * @param date the date to format (assumes already in local timezone)
     * @param now the current date or null. If not null, the returned date for speech will be
     *            shortened accordingly: no year is returned if now is in the same year as date, no
     *            month is returned if now is in the same year and in the same month as date.
     *            Yesterday, today and tomorrow translations are also used.
     * @return the formatted date string
     */
    public String niceDate(final LocalDate date, final LocalDate now) {
        FormatString formatString = config.dateFormatFull;
        if (now != null) {
            // try to remove redundant information based on the current date
            final int daysDifference = Period.between(date, now).getDays();
            if (daysDifference == 1) {
                return config.yesterday;
            } else if (daysDifference == 0) {
                return config.today;
            } else if (daysDifference == -1) {
                return config.tomorrow;
            } else if (date.getYear() == now.getYear()) {
                if (date.getMonth() == now.getMonth()
                        && date.getDayOfMonth() > now.getDayOfMonth()) {
                    formatString = config.dateFormatFullNoYearMonth;
                } else {
                    formatString = config.dateFormatFullNoYear;
                }
            }
        }

        return formatString.format(new HashMap<String, String>() {{
            put("day", config.days[date.getDayOfMonth() - 1]);
            put("weekday", config.weekdays[date.getDayOfWeek().getValue() - 1]);
            put("month", config.months[date.getMonth().getValue() - 1]);
            put("formatted_year", niceYear(date));
        }});
    }

    /**
     * Format the year from a date to a pronounceable year. For example, year 1984 would be
     * formatted as "nineteen eighty four" for English.
     *
     * @param date the date containing the year to format (assumes already in local timezone)
     * @return the formatted year string
     */
    public String niceYear(final LocalDate date) {
        final Map<String, String> substitutionTable
                = NiceYearSubstitutionTableBuilder.build(config, date.getYear());
        final int year = Math.abs(date.getYear());

        substitutionTable.put("number", String.valueOf(year % 100));
        substitutionTable.put("formatted_decade", config.decadeFormat
                .getMostSuitableFormatString(year % 100).format(substitutionTable));

        substitutionTable.put("number", String.valueOf(year % 1000));
        substitutionTable.put("formatted_hundreds", config.hundredFormat
                .getMostSuitableFormatString(year % 1000).format(substitutionTable));

        substitutionTable.put("number", String.valueOf(year % 10000));
        substitutionTable.put("formatted_thousand", config.thousandFormat
                .getMostSuitableFormatString(year % 10000).format(substitutionTable));

        substitutionTable.put("number", String.valueOf(year));
        substitutionTable.put("bc", date.getYear() >= 0 ? "" : config.bc);

        final String formattedYear =
                config.yearFormat.getMostSuitableFormatString(year).format(substitutionTable);
        return Utils.removeRedundantSpaces(formattedYear);
    }

    /**
     * Format a time to a human readable representation. For example, 5:30 would be formatted as
     * "five thirty" for English.
     *
     * @param time the time to format (assumes already in local timezone)
     * @param speech format for speech (true) or display (false)
     * @param use24Hour output in 24-hour/military (true) or 12-hour (false) format
     * @param showAmPm if true include the am/pm for 12-hour format
     * @return the formatted time string
     */
    public abstract String niceTime(LocalTime time,
                                    boolean speech,
                                    boolean use24Hour,
                                    boolean showAmPm);

    /**
     * Format a date time to a pronounceable date and time. For example, 2021/4/28 5:30 would be
     * formatted as "wednesday, april twenty-eighth, twenty twenty one at five thirty" for English.
     *
     * @param date the date to format (assumes already in local timezone)
     * @param now the current date or null. If not null, the returned date for speech will be
     *            shortened accordingly: no year is returned if now is in the same year as date, no
     *            month is returned if now is in the same year and in the same month as date.
     *            Yesterday, today and tomorrow translations are also used.
     * @param time the time to format (assumes already in local timezone)
     * @param use24Hour output in 24-hour/military (true) or 12-hour (false) format
     * @param showAmPm if true include the am/pm for 12-hour format
     * @return the formatted date time string
     */
    public String niceDateTime(final LocalDate date,
                               final LocalDate now,
                               final LocalTime time,
                               final boolean use24Hour,
                               final boolean showAmPm) {
        return config.dateTimeFormat.format(new HashMap<String, String>() {{
            put("formatted_date", niceDate(date, now));
            put("formatted_time", niceTime(time, true, use24Hour, showAmPm));
        }});
    }

    /**
     * Format a duration to a human readable representation. For example, 12 days 3:23:01 would be
     * formatted as "twelve days three hours twenty three minutes one second".
     *
     * @param duration the duration to format
     * @param speech format for speech (true) or display (false)
     * @return the formatted time span string
     */
    public String niceDuration(final Duration duration, final boolean speech) {
        final java.time.Duration javaDuration = duration.toJavaDuration();
        final long days = javaDuration.toDays();
        final long hours = javaDuration.toHours() % 24;
        final long minutes = javaDuration.toMinutes() % 60;
        final long seconds = javaDuration.getSeconds() % 60;

        final StringBuilder result = new StringBuilder();
        if (speech) {
            if (days > 0) {
                result.append(pronounceNumberDuration(days));
                result.append(" ");
                result.append(days == 1 ? config.dayWord : config.daysWord);
            }

            if (hours > 0) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append(pronounceNumberDuration(hours));
                result.append(" ");
                result.append(hours == 1 ? config.hourWord : config.hoursWord);
            }

            if (minutes > 0) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append(pronounceNumberDuration(minutes));
                result.append(" ");
                result.append(minutes == 1 ? config.minuteWord : config.minutesWord);
            }

            // if the duration is zero also write "zero seconds"
            if (seconds > 0 || javaDuration.getSeconds() == 0) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append(pronounceNumberDuration(seconds));
                result.append(" ");
                result.append(seconds == 1 ? config.secondWord : config.secondsWord);
            }

        } else {
            if (days > 0) {
                result.append(days);
                result.append("d ");
            }

            if (hours > 0 || days > 0) {
                result.append(hours);
                result.append(":");
            }

            if (minutes < 10 && (hours > 0 || days > 0)) {
                result.append("0");
            }
            result.append(minutes);
            result.append(":");

            if (seconds < 10) {
                result.append("0");
            }
            result.append(seconds);

        }

        return result.toString();
    }

    protected String pronounceNumberDuration(final long number) {
        return pronounceNumber(number, 0, true, false, false);
    }
}
