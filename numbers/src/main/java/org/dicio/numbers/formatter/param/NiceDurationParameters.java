package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.unit.Duration;

public class NiceDurationParameters {
    // TODO documentation

    private final Formatter formatter;
    private final Duration duration;

    // default values
    private boolean speech = true;

    public NiceDurationParameters(final Formatter formatter, final Duration duration) {
        this.formatter = formatter;
        this.duration = duration;
    }

    public NiceDurationParameters speech(final boolean speech) {
        this.speech = speech;
        return this;
    }

    public String get() {
        return formatter.niceDuration(duration, speech);
    }
}
