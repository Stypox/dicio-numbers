package org.dicio.numbers.lang.it;

import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.parser.lexer.TokenStream;

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
    public long extractDuration(final String text) {
        return 0;
    }

    @Override
    public LocalDateTime extractDateTime(final String text,
                                         final boolean anchorDate,
                                         final LocalTime defaultTime) {
        return null;
    }
}
