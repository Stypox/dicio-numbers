package org.dicio.numbers.lang.en;

import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.util.DurationExtractorUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class EnglishParser extends NumberParser {

    public EnglishParser() {
        super("config/en-us");
    }


    @Override
    public List<Object> extractNumbers(final String utterance,
                                       final boolean shortScale,
                                       final boolean preferOrdinal) {
        return new EnglishNumberExtractor(new TokenStream(tokenizer.tokenize(utterance)),
                shortScale, preferOrdinal).extractNumbers();
    }

    @Override
    public Duration extractDuration(final String utterance, final boolean shortScale) {
        final TokenStream tokenStream = new TokenStream(tokenizer.tokenize(utterance));
        final EnglishNumberExtractor numberExtractor
                = new EnglishNumberExtractor(tokenStream, shortScale, false);
        return DurationExtractorUtils.extractDuration(tokenStream,
                numberExtractor::extractOneNumberNoOrdinal);
    }

    @Override
    public LocalDateTime extractDateTime(final String utterance,
                                         final boolean anchorDate,
                                         final LocalTime defaultTime) {
        return null;
    }
}
