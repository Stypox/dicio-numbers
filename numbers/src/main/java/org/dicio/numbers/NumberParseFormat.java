package org.dicio.numbers;

import org.dicio.numbers.param.Gender;
import org.dicio.numbers.param.NiceNumberParameters;
import org.dicio.numbers.param.PronounceNumberParameters;
import org.dicio.numbers.util.MixedFraction;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public abstract class NumberParseFormat {

    // FORMAT

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


    // PARSE

    public abstract double extractNumber(String text, boolean shortScale, boolean ordinals);

    public abstract List<Double> extractNumbers(String text, boolean shortScale, boolean ordinals);

    public abstract long extractDuration(String text);

    public abstract LocalDateTime extractDateTime(String text, boolean anchorDate,
                                                  LocalTime defaultTime);

    public abstract String normalize(String text, boolean removeArticles);


    public abstract Gender getGender(String text, String context);

    public abstract boolean isFractional(String text, boolean shortScale, boolean spoken);

    public abstract boolean isOrdinal(String text, boolean shortScale, boolean spoken);


    // FORMAT DEFAULT PARAMETERS

    public final NiceNumberParameters niceNumber(final double number) {
        return new NiceNumberParameters(this, number);
    }

    public final PronounceNumberParameters pronounceNumber(final double number) {
        return new PronounceNumberParameters(this, number);
    }
}
