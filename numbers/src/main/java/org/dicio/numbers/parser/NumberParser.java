package org.dicio.numbers.parser;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.parser.lexer.Tokenizer;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

public abstract class NumberParser {

    protected final Tokenizer tokenizer;

    protected NumberParser(final String configFolder) {
        tokenizer = new Tokenizer(configFolder);
    }


    public abstract Supplier<Number> extractNumber(TokenStream tokenStream,
                                                   boolean shortScale,
                                                   boolean preferOrdinal);

    public abstract Supplier<Duration> extractDuration(TokenStream tokenStream, boolean shortScale);

    public abstract Supplier<LocalDateTime> extractDateTime(TokenStream tokenStream,
                                                            LocalDateTime now);


    public TokenStream tokenize(final String utterance) {
        return new TokenStream(tokenizer.tokenize(utterance));
    }
}
