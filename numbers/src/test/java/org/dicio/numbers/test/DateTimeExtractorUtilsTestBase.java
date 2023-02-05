package org.dicio.numbers.test;

import static org.dicio.numbers.test.TestUtils.niceDuration;
import static org.dicio.numbers.util.DateTimeExtractorUtils.isMomentOfDayPm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.dicio.numbers.lang.it.ItalianDateTimeExtractor;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.util.DateTimeExtractorUtils;
import org.junit.Test;

import java.util.function.Function;

public abstract class DateTimeExtractorUtilsTestBase extends WithTokenizerTestBase {

    public abstract DateTimeExtractorUtils build(final TokenStream ts);

    public void assertRelativeDurationFunction(final String s,
                                               final Duration expectedDuration,
                                               final int finalTokenStreamPosition,
                                               final Function<DateTimeExtractorUtils, Duration> durationFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Duration actualDuration = durationFunction.apply(build(ts));
        assertNotNull("null relative duration for string \"" + s + "\"", actualDuration);
        assertEquals("wrong final token position for string \"" + s + "\"",
                finalTokenStreamPosition, ts.getPosition());
        assertTrue("wrong relative duration for string \"" + s + "\": expected \""
                        + niceDuration(expectedDuration) + "\" but got \""
                        + niceDuration(actualDuration) + "\"",
                expectedDuration.getNanos() == actualDuration.getNanos()
                        && expectedDuration.getDays() == actualDuration.getDays()
                        && expectedDuration.getMonths() == actualDuration.getMonths()
                        && expectedDuration.getYears() == actualDuration.getYears());
    }

    public void assertRelativeDurationFunctionNull(final String s,
                                                   final Function<DateTimeExtractorUtils, Duration> durationFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Duration duration = durationFunction.apply(build(ts));

        if (duration != null) {
            fail("expected no relative duration (null), but got \"" + niceDuration(duration) + "\"");
        }
    }

    public <T> void assertFunction(final String s,
                                   final T expectedResult,
                                   int finalTokenStreamPosition,
                                   final Function<DateTimeExtractorUtils, T> function) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        assertEquals("wrong result for string \"" + s + "\"",
                expectedResult, function.apply(build(ts)));
        assertEquals("wrong final token position for string \"" + s + "\"",
                finalTokenStreamPosition, ts.getPosition());
    }

    public <T> void assertFunctionNull(final String s,
                                       final Function<DateTimeExtractorUtils, T> numberFunction) {
        assertFunction(s, null, 0, numberFunction);
    }

    public void assertRelativeMonthDuration(final String s, final Duration expectedDuration, int finalTokenStreamPosition) {
        assertRelativeDurationFunction(s, expectedDuration, finalTokenStreamPosition, DateTimeExtractorUtils::relativeMonthDuration);
    }

    public void assertRelativeMonthDurationNull(final String s) {
        assertRelativeDurationFunctionNull(s, DateTimeExtractorUtils::relativeMonthDuration);
    }

    public void assertRelativeDayOfWeekDuration(final String s, final int expectedDuration, int finalTokenStreamPosition) {
        assertFunction(s, expectedDuration, finalTokenStreamPosition, DateTimeExtractorUtils::relativeDayOfWeekDuration);
    }

    public void assertRelativeDayOfWeekDurationNull(final String s) {
        assertFunctionNull(s, DateTimeExtractorUtils::relativeDayOfWeekDuration);
    }

    public void assertRelativeToday(final String s) {
        assertFunction(s, 0, 1, DateTimeExtractorUtils::relativeToday);
    }

    public void assertRelativeTodayNull(final String s) {
        assertFunctionNull(s, DateTimeExtractorUtils::relativeToday);
    }

    public void assertMinute(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, DateTimeExtractorUtils::minute);
    }

    public void assertMinuteNull(final String s) {
        assertFunctionNull(s, DateTimeExtractorUtils::minute);
    }

    public void assertSecond(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, DateTimeExtractorUtils::second);
    }

    public void assertSecondNull(final String s) {
        assertFunctionNull(s, DateTimeExtractorUtils::second);
    }

    public void assertBcad(final String s, final Boolean expectedAd, int finalTokenStreamPosition) {
        assertFunction(s, expectedAd, finalTokenStreamPosition, DateTimeExtractorUtils::bcad);
    }

    public void assertBcadNull(final String s) {
        assertFunctionNull(s, DateTimeExtractorUtils::bcad);
    }

    public void assertAmpm(final String s, final Boolean expectedPm, int finalTokenStreamPosition) {
        assertFunction(s, expectedPm, finalTokenStreamPosition, DateTimeExtractorUtils::ampm);
    }

    public void assertAmpmNull(final String s) {
        assertFunctionNull(s, DateTimeExtractorUtils::ampm);
    }

    public void assertMonthName(final String s, final int expected) {
        assertFunction(s, expected, 1, DateTimeExtractorUtils::monthName);
    }

    public void assertMonthNameNull(final String s) {
        assertFunctionNull(s, DateTimeExtractorUtils::monthName);
    }


    @Test
    public void testIsMomentOfDayPm() {
        assertEquals(Boolean.FALSE, isMomentOfDayPm(0));
        assertEquals(Boolean.FALSE, isMomentOfDayPm(5));
        assertEquals(Boolean.FALSE, isMomentOfDayPm(11));
        assertEquals(Boolean.TRUE, isMomentOfDayPm(12));
        assertEquals(Boolean.TRUE, isMomentOfDayPm(18));
        assertEquals(Boolean.TRUE, isMomentOfDayPm(24));
        //noinspection ConstantConditions
        assertNull(isMomentOfDayPm(null));
    }
}
