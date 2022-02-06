package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.NumberFormatter;

import java.time.LocalDate;

public class NiceDateParameters {
    // TODO documentation

    private final NumberFormatter numberFormatter;
    private final LocalDate date;

    // default values
    private LocalDate now = null;

    public NiceDateParameters(final NumberFormatter numberFormatter, final LocalDate date) {
        this.numberFormatter = numberFormatter;
        this.date = date;
    }

    /**
     *
     * @param now nullable
     * @return
     */
    public NiceDateParameters now(final LocalDate now) {
        this.now = now;
        return this;
    }

    public String get() {
        return numberFormatter.niceDate(date, now);
    }
}
