package org.dicio.numbers.parser.param;

import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;

import java.util.function.Supplier;

public class ExtractDurationParams extends NumberParserParams<Duration> {
    // TODO documentation
    // default values
    private boolean shortScale = true;

    public ExtractDurationParams(final Parser parser, final String utterance) {
        super(parser, utterance);
    }

    /**
     * @param shortScale consider large numbers that make up durations (e.g. the "one billion" in
     *                   "one billion milliseconds") as short scale (true) or long scale (false)
     *                   numbers (see <a
     *                   href="https://en.wikipedia.org/wiki/Names_of_large_numbers">Names of large
     *                   numbers</a>). The default is true.
     * @return this
     */
    public ExtractDurationParams shortScale(final boolean shortScale) {
        this.shortScale = shortScale;
        return this;
    }

    @Override
    protected Supplier<Duration> getExtractorAtCurrentPosition(final TokenStream tokenStream) {
        return parser.extractDuration(tokenStream, shortScale);
    }
}
