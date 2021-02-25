package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.NumberFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class NiceDateTimeParameters {

    private final NumberFormatter numberFormatter;
    private final LocalDate date;
    private final LocalTime time;

    // default values
    private LocalDate now = null;
    private boolean use24Hour = false;
    private boolean showAmPm = false;

    public NiceDateTimeParameters(final NumberFormatter numberFormatter,
                                  final LocalDateTime dateTime) {
        this.numberFormatter = numberFormatter;
        this.date = dateTime.toLocalDate();
        this.time = dateTime.toLocalTime();
    }

    /**
     *
     * @param now nullable
     * @return
     */
    public NiceDateTimeParameters now(final LocalDate now) {
        this.now = now;
        return this;
    }

    public NiceDateTimeParameters use24Hour(final boolean use24Hour) {
        this.use24Hour = use24Hour;
        return this;
    }

    public NiceDateTimeParameters showAmPm(final boolean showAmPm) {
        this.showAmPm = showAmPm;
        return this;
    }

    public String get() {
        return numberFormatter.niceDateTime(date, now, time, use24Hour, showAmPm);
    }
}
