package org.dicio.numbers.parser.param;

import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.lexer.TokenStream;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class ExtractDateTimeParams extends NumberParserParams<LocalDateTime> {
    // TODO documentation
    // default values
    private LocalDateTime now = LocalDateTime.now();
    private boolean shortScale = true; // shouldn't make a big difference anyway
    private boolean preferMonthBeforeDay = false;

    public ExtractDateTimeParams(final Parser parser, final String utterance) {
        super(parser, utterance);
    }

    public ExtractDateTimeParams now(final LocalDateTime now) {
        this.now = now;
        return this;
    }

    public ExtractDateTimeParams shortScale(final boolean shortScale) {
        this.shortScale = shortScale;
        return this;
    }

    public ExtractDateTimeParams preferMonthBeforeDay(final boolean preferMonthBeforeDay) {
        this.preferMonthBeforeDay = preferMonthBeforeDay;
        return this;
    }

    @Override
    protected Supplier<LocalDateTime> getExtractorAtCurrentPosition(final TokenStream tokenStream) {
        return parser.extractDateTime(tokenStream, shortScale, preferMonthBeforeDay, now);
    }
}
