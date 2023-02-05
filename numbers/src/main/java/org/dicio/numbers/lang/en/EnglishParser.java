package org.dicio.numbers.lang.en;

import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;
import org.dicio.numbers.util.DurationExtractorUtils;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class EnglishParser extends Parser {

    public EnglishParser() {
        super("config/en-us");
    }


    @Override
    public Supplier<Number> extractNumber(final TokenStream tokenStream,
                                          final boolean shortScale,
                                          final boolean preferOrdinal) {
        final EnglishNumberExtractor numberExtractor
                = new EnglishNumberExtractor(tokenStream, shortScale);
        if (preferOrdinal) {
            return numberExtractor::numberPreferOrdinal;
        } else {
            return numberExtractor::numberPreferFraction;
        }
    }

    @Override
    public Supplier<Duration> extractDuration(final TokenStream tokenStream,
                                              final boolean shortScale) {
        final EnglishNumberExtractor numberExtractor
                = new EnglishNumberExtractor(tokenStream, shortScale);
        return new DurationExtractorUtils(tokenStream, numberExtractor::numberNoOrdinal)::duration;
    }

    @Override
    public Supplier<LocalDateTime> extractDateTime(final TokenStream tokenStream,
                                                   final boolean shortScale,
                                                   final boolean preferMonthBeforeDay,
                                                   final LocalDateTime now) {
        return new EnglishDateTimeExtractor(tokenStream, shortScale, preferMonthBeforeDay, now)
                ::dateTime;
    }
}
