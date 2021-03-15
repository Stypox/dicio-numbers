package org.dicio.numbers.lang.en;

import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.parser.lexer.TokenStream;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class EnglishParser extends NumberParser {

    public EnglishParser() {
        super("config/en-us");
    }


    @Override
    public List<Object> extractNumbers(final String text,
                                       final boolean shortScale,
                                       final boolean preferOrdinal) {
        return new EnglishNumberParser(new TokenStream(tokenizer.tokenize(text)),
                shortScale, preferOrdinal).extractNumbers();
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
