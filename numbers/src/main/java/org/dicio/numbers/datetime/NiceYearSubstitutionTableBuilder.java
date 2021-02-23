package org.dicio.numbers.datetime;

import java.util.HashMap;
import java.util.Map;

public final class NiceYearSubstitutionTableBuilder {
    /**
     * @param dateTimeConfig the date time config for the current language, used to get number names
     * @param yearWithSign the current year, can be in the past (i.e. the negative sign can be used)
     * @return the substitution table to use for nice year formatting
     */
    public static Map<String, String> build(final DateTimeConfig dateTimeConfig,
                                            final int yearWithSign) {
        final int year = Math.abs(yearWithSign);
        return new HashMap<String, String>() {{
            put("x", dateTimeConfig.getNumber(year % 10));

            put("xx", dateTimeConfig.getNumber(year % 100));
            put("x0", dateTimeConfig.getNumber(year % 100 - year % 10));
            put("x_in_x0", dateTimeConfig.getNumber(year % 100 / 10));

            put("xxx", dateTimeConfig.getNumber(year % 1000));
            put("x00", dateTimeConfig.getNumber(year % 1000 - year % 100));
            put("x_in_x00", dateTimeConfig.getNumber(year % 1000 / 100));
            // duplicate of x_in_x00, keep for compatibility with lingua-franca json files
            put("x_in_0x00", dateTimeConfig.getNumber(year % 1000 / 100));

            put("xx00", dateTimeConfig.getNumber(year % 10000 - year % 100));
            put("xx_in_xx00", dateTimeConfig.getNumber(year % 10000 / 100));
            put("x000", dateTimeConfig.getNumber(year % 10000 - year % 1000));
            put("x_in_x000", dateTimeConfig.getNumber(year % 10000 / 1000));
            put("x0_in_x000", dateTimeConfig.getNumber(year % 10000 / 1000 * 10));
        }};
    }
}
