package org.dicio.numbers.test;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.util.Number;

import static org.dicio.numbers.util.NumberExtractorUtils.numberGroupShortScale;
import static org.dicio.numbers.util.NumberExtractorUtils.numberLessThan1000;
import static org.junit.Assert.assertEquals;

public abstract class NumberExtractorUtilsTestBase extends ExtractNumbersTestBase {

    protected interface NumberFunction {
        Number call(final TokenStream ts);
    }


    protected void assertNumberFunction(final String s,
                                        final Number value,
                                        final int finalTokenStreamPosition,
                                        final NumberFunction numberFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Number number = numberFunction.call(ts);
        assertEquals("wrong value for string " + s, value, number);
        assertEquals("wrong final token position for number " + value, finalTokenStreamPosition, ts.getPosition());
    }

    protected void assertNumberFunctionNull(final String s,
                                            final NumberFunction numberFunction) {
        assertNumberFunction(s, null, 0, numberFunction);
    }

    protected void assertNumberLessThan1000(final String s, final boolean allowOrdinal, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).setOrdinal(isOrdinal), finalTokenStreamPosition,
                ts -> numberLessThan1000(ts, allowOrdinal));
    }

    protected void assertNumberLessThan1000Null(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, ts -> numberLessThan1000(ts, allowOrdinal));
    }

    protected void assertNumberGroupShortScale(final String s, final boolean allowOrdinal, final long lastMultiplier, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).setOrdinal(isOrdinal), finalTokenStreamPosition,
                ts -> numberGroupShortScale(ts, allowOrdinal, lastMultiplier));
    }

    protected void assertNumberGroupShortScaleNull(final String s, final boolean allowOrdinal, final long lastMultiplier) {
        assertNumberFunctionNull(s, ts -> numberGroupShortScale(ts, allowOrdinal, lastMultiplier));
    }
}
