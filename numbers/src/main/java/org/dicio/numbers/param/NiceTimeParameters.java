package org.dicio.numbers.param;

import org.dicio.numbers.NumberParseFormat;

import java.time.LocalDateTime;

public class NiceTimeParameters {

    private final NumberParseFormat numberParseFormat;
    private final LocalDateTime dateTime;

    // default values
    private boolean speech = true;
    private boolean use24Hour = false;
    private boolean showAmPm = false;

    public NiceTimeParameters(final NumberParseFormat numberParseFormat,
                              final LocalDateTime dateTime) {
        this.numberParseFormat = numberParseFormat;
        this.dateTime = dateTime;
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
        return numberParseFormat.niceTime(dateTime, speech, use24Hour, showAmPm);
    }
}
