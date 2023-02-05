package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.Formatter;

import java.time.LocalDate;

public class NiceDateParameters {
    // TODO documentation

    private final Formatter formatter;
    private final LocalDate date;

    // default values
    private LocalDate now = null;

    public NiceDateParameters(final Formatter formatter, final LocalDate date) {
        this.formatter = formatter;
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
        return formatter.niceDate(date, now);
    }
}
