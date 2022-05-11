package org.dicio.numbers.parser;

import org.dicio.numbers.parser.lexer.Tokenizer;
import org.dicio.numbers.unit.Duration;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public abstract class NumberParser {

    protected final Tokenizer tokenizer;

    protected NumberParser(final String configFolder) {
        tokenizer = new Tokenizer(configFolder);
    }


    public abstract List<Object> extractNumbers(String utterance,
                                                boolean shortScale,
                                                boolean preferOrdinal);

    public abstract Duration extractDuration(String utterance, boolean shortScale);

    public abstract LocalDateTime extractDateTime(String utterance,
                                                  boolean anchorDate,
                                                  LocalTime defaultTime);
}
