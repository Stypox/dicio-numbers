package org.dicio.numbers.formatter.datetime

import kotlin.math.abs

object NiceYearSubstitutionTableBuilder {
    /**
     * @param dateTimeConfig the date time config for the current language, used to get number names
     * @param yearWithSign the current year, can be in the past (i.e. the negative sign can be used)
     * @return the substitution table to use for nice year formatting
     */
    @JvmStatic
    fun build(
        dateTimeConfig: DateTimeConfig,
        yearWithSign: Int
    ): MutableMap<String, String> {
        val year = abs(yearWithSign.toDouble()).toInt()
        return mutableMapOf(
            "x" to dateTimeConfig.getNumber(year % 10),

            "xx" to dateTimeConfig.getNumber(year % 100),
            "x0" to dateTimeConfig.getNumber(year % 100 - year % 10),
            "x_in_x0" to dateTimeConfig.getNumber(year % 100 / 10),

            "xxx" to dateTimeConfig.getNumber(year % 1000),
            "x00" to dateTimeConfig.getNumber(year % 1000 - year % 100),
            "x_in_x00" to dateTimeConfig.getNumber(year % 1000 / 100),
            // duplicate of x_in_x00, keep for compatibility with lingua-franca json files
            "x_in_0x00" to dateTimeConfig.getNumber(year % 1000 / 100),

            "xx00" to dateTimeConfig.getNumber(year % 10000 - year % 100),
            "xx_in_xx00" to dateTimeConfig.getNumber(year % 10000 / 100),
            "x000" to dateTimeConfig.getNumber(year % 10000 - year % 1000),
            "x_in_x000" to dateTimeConfig.getNumber(year % 10000 / 1000),
            "x0_in_x000" to dateTimeConfig.getNumber(year % 10000 / 1000 * 10),
        )
    }
}
