package org.dicio.numbers;

import org.dicio.numbers.datetime.DateTimeConfig;
import org.dicio.numbers.datetime.FormatString;
import org.dicio.numbers.datetime.NiceYearSubstitutionTableBuilder;
import org.dicio.numbers.util.MixedFraction;
import org.dicio.numbers.util.Utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public abstract class NumberFormatter {

    protected final DateTimeConfig config;

    protected NumberFormatter(final String configFolder) {
        config = new DateTimeConfig(configFolder);
    }


    public abstract String niceNumber(MixedFraction mixedFraction, boolean speech);

    public abstract String pronounceNumber(double number,
                                           int places,
                                           boolean shortScale,
                                           boolean scientific,
                                           boolean ordinals);

    /**
     *
     * @param date
     * @param now nullable
     * @return
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
            put("weekday", config.weekdays[date.getDayOfWeek().getValue() - 1]);
            put("month", config.months[date.getMonth().getValue() - 1]);
            put("day", config.days[date.getDayOfMonth() - 1]);
            put("formatted_year", niceYear(date));
        }});
    }

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

    public abstract String niceTime(LocalTime time,
                                    boolean speech,
                                    boolean use24Hour,
                                    boolean showAmPm);

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

    public String niceDuration(final Duration duration, final boolean speech) {
        final long days = duration.toDays();
        final long hours = duration.toHours() % 24;
        final long minutes = duration.toMinutes() % 60;
        final long seconds = duration.getSeconds() % 60;

        final StringBuilder result = new StringBuilder();
        if (speech) {
            if (days > 0) {
                result.append(pronounceNumber(days, 0, true, false, false));
                result.append(" ");
                result.append(days == 1 ? config.dayWord : config.daysWord);
            }

            if (hours > 0) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append(pronounceNumber(hours, 0, true, false, false));
                result.append(" ");
                result.append(hours == 1 ? config.hourWord : config.hoursWord);
            }

            if (minutes > 0) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append(pronounceNumber(minutes, 0, true, false, false));
                result.append(" ");
                result.append(minutes == 1 ? config.minuteWord : config.minutesWord);
            }

            // if the duration is zero also write "zero seconds"
            if (seconds > 0 || duration.getSeconds() == 0) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append(pronounceNumber(seconds, 0, true, false, false));
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

}
