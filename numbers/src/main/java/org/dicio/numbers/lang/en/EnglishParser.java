package org.dicio.numbers.lang.en;

import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.parser.param.Gender;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class EnglishParser extends NumberParser {
    @Override
    public double extractNumber(final String text,
                                final boolean shortScale,
                                final boolean ordinals) {
        return 0;
    }

    @Override
    public List<Double> extractNumbers(final String text,
                                       final boolean shortScale,
                                       final boolean ordinals) {
        return null;
    }

    @Override
    public long extractDuration(final String text) {
        return 0;
    }

    @Override
    public LocalDateTime extractDateTime(final String text,
                                         final boolean anchorDate,
                                         final LocalTime defaultTime) {
        return null;
    }

    @Override
    public String normalize(final String text, final boolean removeArticles) {
        return null;
    }

    @Override
    public Gender getGender(final String text, final String context) {
        return Gender.UNKNOWN;
    }

    @Override
    public boolean isFractional(final String text, final boolean shortScale, final boolean spoken) {
        return false;
    }

    @Override
    public boolean isOrdinal(final String text, final boolean shortScale, final boolean spoken) {
        return false;
    }
}
