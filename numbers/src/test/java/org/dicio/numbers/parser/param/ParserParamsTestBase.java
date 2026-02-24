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

@SuppressWarnings({"unchecked", "DataFlowIssue"})
public abstract class ParserParamsTestBase {

    protected abstract Parser numberParser();

    protected final <T> ParserParams.MatchedRange<T> range(final int start,
                                                           final int end,
                                                           final T parsedData,
                                                           final boolean isLargestPossible) {
        return new ParserParams.MatchedRange<>(start, end, parsedData, isLargestPossible);
    }

    private <T, R> void assertPpFirst(final ParserParams<T> pp,
                                      final Function<T, R> transformActualResult,
                                      final R expectedResult) {
        assertEquals(expectedResult, transformActualResult.apply(pp.parseFirst()));
    }

    private <T, R> void assertPpFirstIfInteger(final ExtractNumberParams pp,
                                               final Long expectedResult) {
        assertEquals(expectedResult, pp.parseFirstIfInteger());
    }

    private <T, R> void assertPpMixedWithText(final ParserParams<T> pp,
                                              final Class<T> ppClass,
                                              final Function<T, R> transformActualResult,
                                              final Object... expectedResults) {
        final List<Object> actualResults = pp.parseMixedWithText();
        final String actualResultsString = actualResults.toString();

        assertEquals("Wrong results size: " + actualResultsString, expectedResults.length, actualResults.size());
        for (int i = 0; i < expectedResults.length; i++) {
            if (actualResults.get(i) instanceof String) {
                assertEquals("Wrong string at position " + i + ": " + actualResultsString,
                        expectedResults[i], actualResults.get(i));
            } else if (ppClass.isInstance(actualResults.get(i))) {
                assertEquals("Wrong object at position " + i + ": " + actualResultsString,
                        expectedResults[i], transformActualResult.apply(ppClass.cast(actualResults.get(i))));
            } else {
                fail("Wrong object type at position " + i + ": " + actualResultsString);
            }
        }
    }

    private <T, R> void assertPpPossibleIntervals(final ParserParams<T> pp,
                                                  final Function<T, R> transformActualResult,
                                                  final ParserParams.MatchedRange<R>... expectedRanges) {
        final List<ParserParams.MatchedRange<T>> actualResults = pp.parsePossibleIntervals();
        final ParserParams.MatchedRange<R>[] actualResultsMapped = actualResults.stream()
                .map(e -> new ParserParams.MatchedRange<>(e.getStart(), e.getEnd(),
                        transformActualResult.apply(e.getParsedData()), e.isLargestPossible()))
                .<ParserParams.MatchedRange<R>>toArray(ParserParams.MatchedRange[]::new);
        assertArrayEquals(expectedRanges, actualResultsMapped);
    }

    protected void assertNumberFirst(final String s, final boolean shortScale, final boolean preferOrdinal, final boolean integerOnly, final Number expectedResult) {
        assertPpFirst(new ExtractNumberParams(numberParser(), s)
                .shortScale(shortScale).integerOnly(integerOnly)
                .preferOrdinal(preferOrdinal), Function.identity(), expectedResult);
    }

    protected void assertNumberFirstIfInteger(final String s, final boolean shortScale, final boolean preferOrdinal, final boolean integerOnly, final Long expectedResult) {
        assertPpFirstIfInteger(new ExtractNumberParams(numberParser(), s)
                .shortScale(shortScale).integerOnly(integerOnly)
                .preferOrdinal(preferOrdinal), expectedResult);
    }

    protected void assertNumberMixedWithText(final String s, final boolean shortScale, final boolean preferOrdinal, final boolean integerOnly, final Object... expectedResults) {
        assertPpMixedWithText(new ExtractNumberParams(numberParser(), s)
                .shortScale(shortScale).integerOnly(integerOnly)
                .preferOrdinal(preferOrdinal), Number.class, Function.identity(), expectedResults);
    }

    protected void assertNumberPossibleIntervals(final String s, final boolean shortScale, final boolean preferOrdinal, final boolean integerOnly, final ParserParams.MatchedRange<Number>... expectedRanges) {
        assertPpPossibleIntervals(new ExtractNumberParams(numberParser(), s)
                .shortScale(shortScale).integerOnly(integerOnly)
                .preferOrdinal(preferOrdinal), Function.identity(), expectedRanges);
    }

    protected void assertDurationFirst(final String s, final boolean shortScale, final java.time.Duration expectedResult) {
        assertPpFirst(new ExtractDurationParams(numberParser(), s).shortScale(shortScale),
                Duration::toJavaDuration, expectedResult);
    }

    protected void assertDurationMixedWithText(final String s, final boolean shortScale, final Object... expectedResults) {
        assertPpMixedWithText(new ExtractDurationParams(numberParser(), s).shortScale(shortScale),
                Duration.class, Duration::toJavaDuration, expectedResults);
    }

    protected void assertDurationPossibleIntervals(final String s, final boolean shortScale, final ParserParams.MatchedRange<java.time.Duration>... expectedRanges) {
        assertPpPossibleIntervals(new ExtractDurationParams(numberParser(), s).shortScale(shortScale),
                Duration::toJavaDuration, expectedRanges);
    }

    protected void assertDateTimeFirst(final String s, final LocalDateTime now, final LocalDateTime expectedResult) {
        assertPpFirst(new ExtractDateTimeParams(numberParser(), s).now(now),
                Function.identity(), expectedResult);
    }

    protected void assertDateTimeMixedWithText(final String s, final LocalDateTime now, final Object... expectedResults) {
        assertPpMixedWithText(new ExtractDateTimeParams(numberParser(), s).now(now),
                LocalDateTime.class, Function.identity(), expectedResults);
    }

    protected void assertDateTimePossibleIntervals(final String s, final LocalDateTime now, final ParserParams.MatchedRange<LocalDateTime>... expectedRanges) {
        assertPpPossibleIntervals(new ExtractDateTimeParams(numberParser(), s).now(now),
                Function.identity(), expectedRanges);
    }
}
