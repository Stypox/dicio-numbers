package org.dicio.numbers.lang.en;

import static org.dicio.numbers.test.TestUtils.t;
import static org.dicio.numbers.util.NumberExtractorUtils.signBeforeNumber;
import static java.time.temporal.ChronoUnit.MONTHS;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.DateTimeExtractorUtilsTestBase;
import org.dicio.numbers.util.DateTimeExtractorUtils;
import org.dicio.numbers.util.NumberExtractorUtils;
import org.junit.Test;

import java.time.LocalDateTime;

public class DateTimeExtractorUtilsTest extends DateTimeExtractorUtilsTestBase {

    // Saturday the 4th of February, 2023, 22:03:47
    private static final LocalDateTime NOW = LocalDateTime.of(2023, 2, 4, 22, 3, 47, 482175927);

    @Override
    public String configFolder() {
        return "config/en-us";
    }

    @Override
    public DateTimeExtractorUtils build(final TokenStream ts) {
        final EnglishNumberExtractor numberExtractor = new EnglishNumberExtractor(ts, false);
        return new DateTimeExtractorUtils(ts, NOW, (fromInclusive, toInclusive) ->
            NumberExtractorUtils.extractOneIntegerInRange(ts, fromInclusive, toInclusive,
                    () -> signBeforeNumber(ts, () -> numberExtractor.numberInteger(false)))
        );
    }
    @Test
    public void testRelativeMonthDuration() {
        assertRelativeMonthDuration("next september",             t(7, MONTHS),   2);
        assertRelativeMonthDuration("april the following and of", t(2, MONTHS),   3);
        assertRelativeMonthDuration("last of april and the",      t(-10, MONTHS), 3);
        assertRelativeMonthDuration("upcoming february",          t(12, MONTHS),  2);
        assertRelativeMonthDuration("past february",              t(-12, MONTHS), 2);
        assertRelativeMonthDuration("january ago",                t(-1, MONTHS),  2);
    }

    @Test
    public void testRelativeMonthDurationNull() {
        assertRelativeMonthDurationNull("hello how are you");
        assertRelativeMonthDurationNull("this november following");
        assertRelativeMonthDurationNull("october");
        assertRelativeMonthDurationNull("in two october");
        assertRelativeMonthDurationNull("in two months");
    }

    @Test
    public void testRelativeToday() {
        assertRelativeToday("today");
        assertRelativeToday("today right today");
        assertRelativeToday("today test");
        assertRelativeToday("today and");
    }

    @Test
    public void testRelativeTodayNull() {
        assertRelativeTodayNull("hello how are you");
        assertRelativeTodayNull("right today");
        assertRelativeTodayNull("the today");
        assertRelativeTodayNull("yesterday");
        assertRelativeTodayNull("tomorrow");
    }

    @Test
    public void testRelativeDayOfWeekDuration() {
        assertRelativeDayOfWeekDuration("next thursday",                    5,   2);
        assertRelativeDayOfWeekDuration("last thursday",                    -2,  2);
        assertRelativeDayOfWeekDuration("past two sundays yes",             -13, 3);
        assertRelativeDayOfWeekDuration("three and tuesdays and following", 17,  5);
        assertRelativeDayOfWeekDuration("four mondays of before and",       -26, 4);
        assertRelativeDayOfWeekDuration("upcoming saturday",                7,   2);
        assertRelativeDayOfWeekDuration("saturday ago",                     -7,  2);
    }

    @Test
    public void testRelativeDayOfWeekDurationNull() {
        assertRelativeDayOfWeekDurationNull("hello how are you");
        assertRelativeDayOfWeekDurationNull("monday");
        assertRelativeDayOfWeekDurationNull("ago monday");
        assertRelativeDayOfWeekDurationNull("two fridays");
        assertRelativeDayOfWeekDurationNull("in two days");
        assertRelativeDayOfWeekDurationNull("and in two sundays");
        assertRelativeDayOfWeekDurationNull("a last monday");
        assertRelativeDayOfWeekDurationNull("yesterday and tomorrow");
    }

    @Test
    public void testMinute() {
        assertMinute("zero a b c",         0,  1);
        assertMinute("fifty nine hours",   59, 2);
        assertMinute("fifteen and",        15, 1);
        assertMinute("twenty and eight s", 28, 3);
        assertMinute("six mins test",      6,  2);
        assertMinute("thirty six of min",  36, 2);
        assertMinute("44m of",             44, 2);
    }

    @Test
    public void testMinuteNull() {
        assertMinuteNull("hello how are you");
        assertMinuteNull("sixty minutes");
        assertMinuteNull("one hundred and twenty");
        assertMinuteNull("minus sixteen");
        assertMinuteNull("12000 minutes");
        assertMinuteNull("and two of");
    }

    @Test
    public void testSecond() {
        assertSecond("zero a b c",         0,  1);
        assertSecond("fifty nine hours",   59, 2);
        assertSecond("fifteen and",        15, 1);
        assertSecond("twenty and eight h", 28, 3);
        assertSecond("six secs test",      6,  2);
        assertSecond("thirty six of sec",  36, 2);
        assertSecond("44s of",             44, 2);
    }

    @Test
    public void testSecondNull() {
        assertSecondNull("hello how are you");
        assertSecondNull("sixty seconds");
        assertSecondNull("one hundred and twenty");
        assertSecondNull("minus sixteen");
        assertSecondNull("12000 seconds");
        assertSecondNull("and two of");
    }

    @Test
    public void testBcad() {
        assertBcad("b.C. test",     false, 3);
        assertBcad("A.D. and",      true,  3);
        assertBcad("ad test of",    true,  1);
        assertBcad("before Christ", false, 2);
        assertBcad("a and Domini",  true,  3);
        assertBcad("bce",           false, 1);
        assertBcad("b current",     false, 2);

        // there is a workaround for this in EnglishDateTimeExtractor
        assertBcad("b.c.e.",        false, 3);
    }

    @Test
    public void testBcadNull() {
        assertBcadNull("a.m.");
        assertBcadNull("anno test Domini");
        assertBcadNull("and before common");
        assertBcadNull("test c");
        assertBcadNull("m");
        assertBcadNull("c test");
    }

    @Test
    public void testAmpm() {
        assertAmpm("a.m. test",      false, 3);
        assertAmpm("p.m. and",       true,  3);
        assertAmpm("am and test",    false, 1);
        assertAmpm("post meridian",  true,  2);
        assertAmpm("p and meridiem", true,  3);
    }

    @Test
    public void testAmpmNull() {
        assertAmpmNull("A.D.");
        assertAmpmNull("ante test meridiem");
        assertAmpmNull("and post m");
        assertAmpmNull("test m");
        assertAmpmNull("c");
        assertAmpmNull("aandm");
        assertAmpmNull("meridian test");
    }

    @Test
    public void testMonthName() {
        assertMonthName("january",    1);
        assertMonthName("dec e",      12);
        assertMonthName("sept ember", 9);
        assertMonthName("mar",        3);
    }

    @Test
    public void testMonthNameNull() {
        assertMonthNameNull("monday");
        assertMonthNameNull("jaguar");
        assertMonthNameNull("hello feb");
        assertMonthNameNull("and dic to");
    }
}
