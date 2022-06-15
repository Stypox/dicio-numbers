package org.dicio.numbers.parser.param;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public abstract class NumberParserParamsTestBase {

    protected abstract Parser numberParser();

    private <T, R> void assertNppFirst(final NumberParserParams<T> npp,
                                       final Function<T, R> transformActualResult,
                                       final R expectedResult) {
        assertEquals(expectedResult, transformActualResult.apply(npp.getFirst()));
    }

    private <T, R> void assertNppMixedWithText(final NumberParserParams<T> npp,
                                               final Class<T> nppClass,
                                               final Function<T, R> transformActualResult,
                                               final Object... expectedResults) {
        final List<Object> actualResults = npp.getMixedWithText();
        final String actualResultsString = actualResults.toString();

        assertEquals("Wrong results size: " + actualResultsString, expectedResults.length, actualResults.size());
        for (int i = 0; i < expectedResults.length; i++) {
            if (actualResults.get(i) instanceof String) {
                assertEquals("Wrong string at position " + i + ": " + actualResultsString,
                        expectedResults[i], actualResults.get(i));
            } else if (nppClass.isInstance(actualResults.get(i))) {
                assertEquals("Wrong object at position " + i + ": " + actualResultsString,
                        expectedResults[i], transformActualResult.apply(nppClass.cast(actualResults.get(i))));
            } else {
                fail("Wrong object type at position " + i + ": " + actualResultsString);
            }
        }
    }

    protected void assertNumberFirst(final String s, final boolean shortScale, final boolean preferOrdinal, final Number expectedResult) {
        assertNppFirst(new ExtractNumberParams(numberParser(), s).shortScale(shortScale)
                .preferOrdinal(preferOrdinal), Function.identity(), expectedResult);
    }

    protected void assertNumberMixedWithText(final String s, final boolean shortScale, final boolean preferOrdinal, final Object... expectedResults) {
        assertNppMixedWithText(new ExtractNumberParams(numberParser(), s).shortScale(shortScale)
                .preferOrdinal(preferOrdinal), Number.class, Function.identity(), expectedResults);
    }

    protected void assertDurationFirst(final String s, final boolean shortScale, final java.time.Duration expectedResult) {
        assertNppFirst(new ExtractDurationParams(numberParser(), s).shortScale(shortScale),
                Duration::toJavaDuration, expectedResult);
    }

    protected void assertDurationMixedWithText(final String s, final boolean shortScale, final Object... expectedResults) {
        assertNppMixedWithText(new ExtractDurationParams(numberParser(), s).shortScale(shortScale),
                Duration.class, Duration::toJavaDuration, expectedResults);
    }

    protected void assertDateTimeFirst(final String s, final LocalDateTime now, final LocalDateTime expectedResult) {
        assertNppFirst(new ExtractDateTimeParams(numberParser(), s).now(now),
                Function.identity(), expectedResult);
    }

    protected void assertDateTimeMixedWithText(final String s, final LocalDateTime now, final Object... expectedResults) {
        assertNppMixedWithText(new ExtractDateTimeParams(numberParser(), s).now(now),
                LocalDateTime.class, Function.identity(), expectedResults);
    }
}
