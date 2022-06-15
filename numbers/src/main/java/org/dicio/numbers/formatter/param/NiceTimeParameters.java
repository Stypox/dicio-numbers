package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.Formatter;

import java.time.LocalTime;

public class NiceTimeParameters {
    // TODO documentation

    private final Formatter formatter;
    private final LocalTime time;

    // default values
    private boolean speech = true;
    private boolean use24Hour = false;
    private boolean showAmPm = false;

    public NiceTimeParameters(final Formatter formatter, final LocalTime time) {
        this.formatter = formatter;
        this.time = time;
    }

    public NiceTimeParameters speech(final boolean speech) {
        this.speech = speech;
        return this;
    }

    public NiceTimeParameters use24Hour(final boolean use24Hour) {
        this.use24Hour = use24Hour;
        return this;
    }

    public NiceTimeParameters showAmPm(final boolean showAmPm) {
        this.showAmPm = showAmPm;
        return this;
    }

    public String get() {
        return formatter.niceTime(time, speech, use24Hour, showAmPm);
    }
}
