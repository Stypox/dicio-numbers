package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.NumberFormatter;

import java.time.LocalDate;

/**
 * Note: this class is useless, since the niceYear function only takes one mandatory argument, but
 * is used anyway in NumberParserFormatter to keep consistency with other functions.
 */
public class NiceYearParameters {
    // TODO documentation

    private final NumberFormatter numberFormatter;
    private final LocalDate date;

    public NiceYearParameters(final NumberFormatter numberFormatter, final LocalDate date) {
        this.numberFormatter = numberFormatter;
        this.date = date;
    }

    public String get() {
        return numberFormatter.niceYear(date);
    }
}
