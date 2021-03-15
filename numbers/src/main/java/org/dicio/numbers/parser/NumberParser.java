package org.dicio.numbers.parser;

import org.dicio.numbers.parser.lexer.Tokenizer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public abstract class NumberParser {

    protected final Tokenizer tokenizer;

    protected NumberParser(final String configFolder) {
        tokenizer = new Tokenizer(configFolder);
    }


    public abstract List<Object> extractNumbers(String text, boolean shortScale, boolean ordinal);

    public abstract long extractDuration(String text);

    public abstract LocalDateTime extractDateTime(String text,
                                                  boolean anchorDate,
                                                  LocalTime defaultTime);
}
