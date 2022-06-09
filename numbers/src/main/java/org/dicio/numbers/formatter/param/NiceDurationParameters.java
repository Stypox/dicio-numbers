package org.dicio.numbers.formatter.param;

import org.dicio.numbers.formatter.NumberFormatter;
import org.dicio.numbers.unit.Duration;

public class NiceDurationParameters {
    // TODO documentation

    private final NumberFormatter numberFormatter;
    private final Duration duration;

    // default values
    private boolean speech = true;

    public NiceDurationParameters(final NumberFormatter numberFormatter, final Duration duration) {
        this.numberFormatter = numberFormatter;
        this.duration = duration;
    }

    public NiceDurationParameters speech(final boolean speech) {
        this.speech = speech;
        return this;
    }

    public String get() {
        return numberFormatter.niceDuration(duration, speech);
    }
}
