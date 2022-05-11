package org.dicio.numbers.lang.it;

import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.util.DurationExtractorUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ItalianParser extends NumberParser {

    public ItalianParser() {
        super("config/it-it");
    }


    @Override
    public List<Object> extractNumbers(final String utterance,
                                       final boolean shortScale,
                                       final boolean preferOrdinal) {
        return new ItalianNumberExtractor(new TokenStream(tokenizer.tokenize(utterance)), preferOrdinal)
                .extractNumbers();
    }

    @Override
    public Duration extractDuration(final String utterance, final boolean shortScale) {
        final TokenStream tokenStream = new TokenStream(tokenizer.tokenize(utterance));
        final ItalianNumberExtractor numberExtractor
                = new ItalianNumberExtractor(tokenStream, false);
        return new DurationExtractorUtils(tokenStream, numberExtractor::extractOneNumberNoOrdinal)
                .extractDuration();
    }

    @Override
    public LocalDateTime extractDateTime(final String utterance,
                                         final boolean anchorDate,
                                         final LocalTime defaultTime) {
        return null;
    }
}
