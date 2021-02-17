package org.dicio.numbers;

import org.dicio.numbers.datetime.FormatString;
import org.dicio.numbers.datetime.DateTimeConfig;
import org.dicio.numbers.util.MixedFraction;
import org.dicio.numbers.util.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.HashMap;

public abstract class NumberFormatter {

    protected final DateTimeConfig config;

    protected NumberFormatter(final String configFolder) {
        config = new DateTimeConfig(configFolder + "/date_time.json");
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
        final int year = Math.abs(date.getYear());
        // TODO maybe add tests for this table
        final HashMap<String, String> substitutionTable = new HashMap<String, String>() {{
            put("x", config.getNumber(year % 10));

            put("xx", config.getNumber(year % 100));
            put("x0", config.getNumber(year % 100 - year % 10));
            put("x_in_x0", config.getNumber(year % 100 / 10));

            put("xxx", config.getNumber(year % 1000));
            put("x00", config.getNumber(year % 1000 - year % 100));
            put("x_in_x00", config.getNumber(year % 1000 / 100));
            // duplicate of x_in_x00, keep for compatibility with lingua-franca json files
            put("x_in_0x00", config.getNumber(year % 1000 / 100));

            put("xx00", config.getNumber(year % 10000 - year % 100));
            put("xx_in_xx00", config.getNumber(year % 10000 / 100));
            put("x000", config.getNumber(year % 10000 - year % 1000));
            put("x_in_x000", config.getNumber(year % 10000 / 1000));
            put("x0_in_x000", config.getNumber(year % 10000 / 1000 * 10));
        }};

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

    // TODO maybe now should be LocalDate
    public abstract String niceDateTime(LocalDateTime dateTime,
                                        LocalDateTime now,
                                        boolean use24Hour,
                                        boolean showAmPm);

    public abstract String niceDuration(int seconds, boolean speech);

}
