package org.dicio.numbers.parser.param;

import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Number;

import java.util.function.Supplier;

public class ExtractNumberParams extends NumberParserParams<Number> {
    // TODO documentation
    // default values
    private boolean shortScale = true;
    private boolean preferOrdinal = false;

    public ExtractNumberParams(final NumberParser numberParser, final String utterance) {
        super(numberParser, utterance);
    }

    public ExtractNumberParams shortScale(final boolean shortScale) {
        this.shortScale = shortScale;
        return this;
    }

    public ExtractNumberParams preferOrdinal(final boolean preferOrdinal) {
        this.preferOrdinal = preferOrdinal;
        return this;
    }

    @Override
    protected Supplier<Number> getExtractorAtCurrentPosition(final TokenStream tokenStream) {
        return numberParser.extractNumber(tokenStream, shortScale, preferOrdinal);
    }
}
