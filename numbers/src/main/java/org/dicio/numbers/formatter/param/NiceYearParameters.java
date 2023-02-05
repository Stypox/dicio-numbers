package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.Formatter;

import java.time.LocalDate;

/**
 * Note: this class is useless, since the niceYear function only takes one mandatory argument, but
 * is used anyway in NumberParserFormatter to keep consistency with other functions.
 */
public class NiceYearParameters {
    // TODO documentation

    private final Formatter formatter;
    private final LocalDate date;

    public NiceYearParameters(final Formatter formatter, final LocalDate date) {
        this.formatter = formatter;
        this.date = date;
    }

    public String get() {
        return formatter.niceYear(date);
    }
}
