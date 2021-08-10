package org.dicio.numbers.test;

import org.dicio.numbers.parser.lexer.Tokenizer;
import org.dicio.numbers.util.Number;
import org.junit.Before;

public abstract class ExtractNumbersTestBase {
    protected static final boolean T = true, F = false;

    protected Tokenizer tokenizer;

    public abstract String configFolder();

    @Before
    public void setup() {
        tokenizer = new Tokenizer(configFolder());
    }


    protected static Number numberDeduceType(final double value) {
        if (((long) value) == value) {
            return new Number((long) value);
        } else {
            return new Number(value);
        }
    }

    protected static Number n(final long value, final boolean ordinal) {
        return new Number(value).setOrdinal(ordinal);
    }

    protected static Number n(final double value, final boolean ordinal) {
        return new Number(value).setOrdinal(ordinal);
    }
}
