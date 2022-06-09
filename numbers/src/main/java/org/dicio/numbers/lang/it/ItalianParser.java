package org.dicio.numbers.lang.it;

import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;
import org.dicio.numbers.util.DurationExtractorUtils;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class ItalianParser extends NumberParser {

    public ItalianParser() {
        super("config/it-it");
    }


    @Override
    public Supplier<Number> extractNumber(final TokenStream tokenStream,
                                          final boolean shortScale,
                                          final boolean preferOrdinal) {
        final ItalianNumberExtractor numberExtractor = new ItalianNumberExtractor(tokenStream);
        if (preferOrdinal) {
            return numberExtractor::numberPreferOrdinal;
        } else {
            return numberExtractor::numberPreferFraction;
        }
    }

    @Override
    public Supplier<Duration> extractDuration(final TokenStream tokenStream,
                                              final boolean shortScale) {
        final ItalianNumberExtractor numberExtractor = new ItalianNumberExtractor(tokenStream);
        return new DurationExtractorUtils(tokenStream, numberExtractor::numberNoOrdinal)::duration;
    }

    @Override
    public Supplier<LocalDateTime> extractDateTime(final TokenStream tokenStream,
                                                   final LocalDateTime now) {
        return new ItalianDateTimeExtractor(tokenStream, now)::dateTime;
    }
}
