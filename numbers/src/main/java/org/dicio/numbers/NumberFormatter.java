package org.dicio.numbers;

import org.dicio.numbers.param.NiceNumberParameters;
import org.dicio.numbers.param.NiceTimeParameters;
import org.dicio.numbers.param.PronounceNumberParameters;
import org.dicio.numbers.util.MixedFraction;

import java.time.LocalDateTime;

public abstract class NumberFormatter {

    public abstract String niceNumber(MixedFraction mixedFraction, boolean speech);

    public abstract String pronounceNumber(double number, int places, boolean shortScale,
                                           boolean scientific, boolean ordinals);

    public abstract String niceDate(LocalDateTime dateTime, LocalDateTime now);

    public abstract String niceYear(LocalDateTime dateTime);

    public abstract String niceTime(LocalDateTime dateTime, boolean speech, boolean use24Hour,
                                    boolean useAmPm);

    public abstract String niceDateTime(LocalDateTime dateTime, LocalDateTime now,
                                        boolean use24Hour, boolean useAmPm);

    public abstract String niceDuration(int seconds, boolean speech);

}
