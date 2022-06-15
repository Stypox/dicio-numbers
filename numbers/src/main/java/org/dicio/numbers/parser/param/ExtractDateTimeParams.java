package org.dicio.numbers.parser.param;

import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.lexer.TokenStream;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class ExtractDateTimeParams extends NumberParserParams<LocalDateTime> {
    // TODO documentation
    // default values
    private LocalDateTime now = LocalDateTime.now();

    public ExtractDateTimeParams(final Parser parser, final String utterance) {
        super(parser, utterance);
    }

    public ExtractDateTimeParams now(final LocalDateTime now) {
        this.now = now;
        return this;
    }

    @Override
    protected Supplier<LocalDateTime> getExtractorAtCurrentPosition(final TokenStream tokenStream) {
        return parser.extractDateTime(tokenStream, now);
    }
}
