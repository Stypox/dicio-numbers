package org.dicio.numbers.test;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Number;

import static org.dicio.numbers.util.NumberExtractorUtils.numberGroupShortScale;
import static org.dicio.numbers.util.NumberExtractorUtils.numberLessThan1000;
import static org.junit.Assert.assertEquals;

import java.util.function.Function;

public abstract class NumberExtractorUtilsTestBase extends WithTokenizerTestBase {

    protected void assertNumberFunction(final String s,
                                        final Number value,
                                        final int finalTokenStreamPosition,
                                        final Function<TokenStream, Number> numberFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Number number = numberFunction.apply(ts);
        assertEquals("wrong value for string " + s, value, number);
        assertEquals("wrong final token position for number " + value, finalTokenStreamPosition, ts.getPosition());
    }

    protected void assertNumberFunctionNull(final String s,
                                            final Function<TokenStream, Number> numberFunction) {
        assertNumberFunction(s, null, 0, numberFunction);
    }

    protected void assertNumberLessThan1000(final String s, final boolean allowOrdinal, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).withOrdinal(isOrdinal), finalTokenStreamPosition,
                ts -> numberLessThan1000(ts, allowOrdinal));
    }

    protected void assertNumberLessThan1000Null(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, ts -> numberLessThan1000(ts, allowOrdinal));
    }

    protected void assertNumberGroupShortScale(final String s, final boolean allowOrdinal, final long lastMultiplier, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).withOrdinal(isOrdinal), finalTokenStreamPosition,
                ts -> numberGroupShortScale(ts, allowOrdinal, lastMultiplier));
    }

    protected void assertNumberGroupShortScaleNull(final String s, final boolean allowOrdinal, final long lastMultiplier) {
        assertNumberFunctionNull(s, ts -> numberGroupShortScale(ts, allowOrdinal, lastMultiplier));
    }
}
