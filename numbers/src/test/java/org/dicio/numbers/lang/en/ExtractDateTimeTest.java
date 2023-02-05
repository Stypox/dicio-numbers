package org.dicio.numbers.lang.en;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;
import static org.dicio.numbers.test.TestUtils.niceDuration;
import static org.dicio.numbers.test.TestUtils.t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.WithTokenizerTestBase;
import org.dicio.numbers.unit.Duration;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

public class ExtractDateTimeTest extends WithTokenizerTestBase {

    // Sunday the 5th of February, 2023, 9:41:12
    private static final LocalDateTime NOW = LocalDateTime.of(2023, 2, 5, 9, 41, 12, 759274821);

    @Override
    public String configFolder() {
        return "config/en-us";
    }


    private void assertRelativeDurationFunction(final String s,
                                                final Duration expectedDuration,
                                                final int finalTokenStreamPosition,
                                                final Function<EnglishDateTimeExtractor, Duration> durationFunction) {
        // some random but deterministic values: we don't actually use big numbers here so it
        // shouldn't make a difference, and preferMonthBeforeDay only affects date and dateTime
        final boolean shortScale = (s.hashCode() % 2) == 0;
        final boolean preferMonthBeforeDay = ((s.hashCode() / 2) % 2) == 0;

        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Duration actualDuration = durationFunction.apply(new EnglishDateTimeExtractor(ts, shortScale, preferMonthBeforeDay, NOW));
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

    private void assertRelativeDurationFunctionNull(final String s,
                                                    final Function<EnglishDateTimeExtractor, Duration> durationFunction) {
        // some random but deterministic values: we don't actually use big numbers here so it
        // shouldn't make a difference, and preferMonthBeforeDay only affects date and dateTime
        final boolean shortScale = (s.hashCode() % 2) == 0;
        final boolean preferMonthBeforeDay = ((s.hashCode() / 2) % 2) == 0;

        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Duration duration = durationFunction.apply(new EnglishDateTimeExtractor(ts, shortScale, preferMonthBeforeDay, NOW));

        if (duration != null) {
            fail("expected no relative duration (null), but got \"" + niceDuration(duration)
                    + "\"");
        }
    }

    private <T> void assertFunction(final String s,
                                    final boolean preferMonthBeforeDay,
                                    final T expectedResult,
                                    int finalTokenStreamPosition,
                                    final Function<EnglishDateTimeExtractor, T> function) {
        // some random but deterministic value: we don't actually use big numbers here so it
        // shouldn't make a difference
        final boolean shortScale = (s.hashCode() % 2) == 0;

        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        assertEquals("wrong result for string \"" + s + "\"",
                expectedResult, function.apply(new EnglishDateTimeExtractor(ts, shortScale, preferMonthBeforeDay, NOW)));
        assertEquals("wrong final token position for string \"" + s + "\"",
                finalTokenStreamPosition, ts.getPosition());
    }

    private <T> void assertFunctionNull(final String s,
                                        final boolean preferMonthBeforeDay,
                                        final Function<EnglishDateTimeExtractor, T> numberFunction) {
        assertFunction(s, preferMonthBeforeDay, null, 0, numberFunction);
    }

    private void assertRelativeDuration(final String s, final Duration expectedDuration, int finalTokenStreamPosition) {
        assertRelativeDurationFunction(s, expectedDuration, finalTokenStreamPosition, EnglishDateTimeExtractor::relativeDuration);
    }

    private void assertRelativeDurationNull(final String s) {
        assertRelativeDurationFunctionNull(s, EnglishDateTimeExtractor::relativeDuration);
    }

    private void assertRelativeTomorrow(final String s, final int expectedDuration, int finalTokenStreamPosition) {
        assertFunction(s, false, expectedDuration, finalTokenStreamPosition, EnglishDateTimeExtractor::relativeTomorrow);
    }

    private void assertRelativeTomorrowNull(final String s) {
        assertFunctionNull(s, false, EnglishDateTimeExtractor::relativeTomorrow);
    }

    private void assertRelativeYesterday(final String s, final int expectedDuration, int finalTokenStreamPosition) {
        assertFunction(s, false, expectedDuration, finalTokenStreamPosition, EnglishDateTimeExtractor::relativeYesterday);
    }

    private void assertRelativeYesterdayNull(final String s) {
        assertFunctionNull(s, false, EnglishDateTimeExtractor::relativeYesterday);
    }

    private void assertHour(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, false, expected, finalTokenStreamPosition, EnglishDateTimeExtractor::hour);
    }

    private void assertHourNull(final String s) {
        assertFunctionNull(s, false, EnglishDateTimeExtractor::hour);
    }

    private void assertMomentOfDay(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, false, expected, finalTokenStreamPosition, EnglishDateTimeExtractor::momentOfDay);
    }

    private void assertMomentOfDayNull(final String s) {
        assertFunctionNull(s, false, EnglishDateTimeExtractor::momentOfDay);
    }

    private void assertNoonMidnightLike(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, false, expected, finalTokenStreamPosition, EnglishDateTimeExtractor::noonMidnightLike);
    }

    private void assertNoonMidnightLikeNull(final String s) {
        assertFunctionNull(s, false, EnglishDateTimeExtractor::noonMidnightLike);
    }

    private void assertSpecialMinute(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, false, expected, finalTokenStreamPosition, EnglishDateTimeExtractor::specialMinute);
    }

    private void assertSpecialMinuteNull(final String s) {
        assertFunctionNull(s, false, EnglishDateTimeExtractor::specialMinute);
    }

    private void assertOClock(final String s, int finalTokenStreamPosition) {
        assertFunction(s, false, true, finalTokenStreamPosition, EnglishDateTimeExtractor::oClock);
    }

    private void assertOClockFalse(final String s) {
        assertFunction(s, false, false, 0, EnglishDateTimeExtractor::oClock);
    }

    // TODO bcad, o clock
    private void assertDate(final String s, final boolean preferMonthBeforeDay, final LocalDate expected, int finalTokenStreamPosition) {
        assertFunction(s, preferMonthBeforeDay, expected, finalTokenStreamPosition, EnglishDateTimeExtractor::date);
    }

    private void assertDate(final String s, final LocalDate expected, int finalTokenStreamPosition) {
        assertDate(s, false, expected, finalTokenStreamPosition);
        assertDate(s, true, expected, finalTokenStreamPosition);
    }

    private void assertDateNull(final String s) {
        assertFunctionNull(s, true, EnglishDateTimeExtractor::date);
        assertFunctionNull(s, false, EnglishDateTimeExtractor::date);
    }

    private void assertBcad(final String s, final Boolean expectedAd, int finalTokenStreamPosition) {
        assertFunction(s, false, expectedAd, finalTokenStreamPosition, EnglishDateTimeExtractor::bcad);
    }

    private void assertTime(final String s, final LocalTime expected, int finalTokenStreamPosition) {
        assertFunction(s, false, expected, finalTokenStreamPosition, EnglishDateTimeExtractor::time);
    }

    private void assertTimeNull(final String s) {
        assertFunctionNull(s, false, EnglishDateTimeExtractor::time);
    }

    private void assertTimeWithAmpm(final String s, final LocalTime expected, int finalTokenStreamPosition) {
        assertFunction(s, false, expected, finalTokenStreamPosition, EnglishDateTimeExtractor::timeWithAmpm);
    }

    private void assertTimeWithAmpmNull(final String s) {
        assertFunctionNull(s, false, EnglishDateTimeExtractor::timeWithAmpm);
    }

    private void assertDateTime(final String s, final boolean preferMonthBeforeDay, final LocalDateTime expected, int finalTokenStreamPosition) {
        assertFunction(s, preferMonthBeforeDay, expected, finalTokenStreamPosition, EnglishDateTimeExtractor::dateTime);
    }

    private void assertDateTime(final String s, final LocalDateTime expected, int finalTokenStreamPosition) {
        assertDateTime(s, false, expected, finalTokenStreamPosition);
        assertDateTime(s, true, expected, finalTokenStreamPosition);
    }

    private void assertDateTimeNull(final String s) {
        assertFunctionNull(s, true, EnglishDateTimeExtractor::dateTime);
        assertFunctionNull(s, false, EnglishDateTimeExtractor::dateTime);
    }


    @Test
    public void testRelativeDuration() {
        assertRelativeDuration("in two weeks I'll go",   t(2, WEEKS),   3);
        assertRelativeDuration("in four months ago",     t(4, MONTHS),  3);
        assertRelativeDuration("after seconds he fell",  t(1, SECONDS), 2);
        assertRelativeDuration("in a couple of decades", t(20, YEARS),  5);
        assertRelativeDuration("nine a days a ago a",    t(-9, DAYS),   5);
        assertRelativeDuration("seventy years ago",      t(-70, YEARS), 3);
        assertRelativeDuration("three months and two days before", t(-3, MONTHS).plus(t(-2, DAYS)), 6);
        assertRelativeDuration("last sixty seven centuries started six thousand seven hundred years ago", t(-6700, YEARS), 4);
    }

    @Test
    public void testRelativeDurationNull() {
        assertRelativeDurationNull("hello how are you");
        assertRelativeDurationNull("four semesters");
        assertRelativeDurationNull("you know in a week");
        assertRelativeDurationNull("and two months ago");
        assertRelativeDurationNull("the past day");
    }

    @Test
    public void testRelativeTomorrow() {
        assertRelativeTomorrow("tomorrow we go",             1, 1);
        assertRelativeTomorrow("the day after tomorrow and", 2, 4);
        assertRelativeTomorrow("day after morrow and",       2, 3);
        assertRelativeTomorrow("morrow the day after",       1, 1);
    }

    @Test
    public void testRelativeTomorrowNull() {
        assertRelativeTomorrowNull("hello how are you");
        assertRelativeTomorrowNull("the tomorrow");
        assertRelativeTomorrowNull("of day after tomorrow");
        assertRelativeTomorrowNull("yesterday");
        assertRelativeTomorrowNull("today");
        assertRelativeTomorrowNull("day after the tomorrow");
        assertRelativeTomorrowNull("the day of after tomorrow");
    }

    @Test
    public void testRelativeYesterday() {
        assertRelativeYesterday("yesterday I've been",          -1, 1);
        assertRelativeYesterday("the day before yesterday and", -2, 4);
        assertRelativeYesterday("day before yesterday test",    -2, 3);
        assertRelativeYesterday("yesterday the day before",     -1, 1);
    }

    @Test
    public void testRelativeYesterdayNull() {
        assertRelativeYesterdayNull("hello how are you");
        assertRelativeYesterdayNull("and yesterday");
        assertRelativeYesterdayNull("of day before tomorrow");
        assertRelativeYesterdayNull("today");
        assertRelativeYesterdayNull("morrow");
        assertRelativeYesterdayNull("day before the tomorrow");
        assertRelativeYesterdayNull("the day of before tomorrow");
    }

    @Test
    public void testHour() {
        assertHour("8:36 test",         8,  1);
        assertHour("16:44 test",        16, 1);
        assertHour("twenty one test",   21, 2);
        assertHour("the zero and",      0,  2);
        assertHour("at one twenty six", 1,  2);
        assertHour("twelve o clock",    12, 1);
        assertHour("at seventeen the",  17, 2);
        assertHour("at hour at three",  3,  4);
        assertHour("at hours thirteen", 13, 3);
        assertHour("the seven test",    7,  2);
    }

    @Test
    public void testHourNull() {
        assertHourNull("hello how are you");
        assertHourNull("twenty five");
        assertHourNull("the minus two");
        assertHourNull("at one hundred and fifty four");
        assertHourNull("at hour");
        assertHourNull("the and zero and");
        assertHourNull("and twenty four");
        assertHourNull("the one million");
    }

    @Test
    public void testNoonMidnightLike() {
        assertNoonMidnightLike("at midnight", 0,  2);
        assertNoonMidnightLike("middays",     12, 1);
        assertNoonMidnightLike("this noon",   12, 2);
    }

    @Test
    public void testNoonMidnightLikeNull() {
        assertNoonMidnightLikeNull("hello how are you");
        assertNoonMidnightLikeNull("this evening and");
        assertNoonMidnightLikeNull("tonight test");
        assertNoonMidnightLikeNull("after dinner");
        assertNoonMidnightLikeNull("before the lunch");
        assertNoonMidnightLikeNull("and at midday");
        assertNoonMidnightLikeNull("and midnight");
        assertNoonMidnightLikeNull("at hour noon");
        assertNoonMidnightLikeNull("in midnight");
        assertNoonMidnightLikeNull("at the midday");
    }

    @Test
    public void testMomentOfDay() {
        assertMomentOfDay("at midnight",      0,  2);
        assertMomentOfDay("noon",             12, 1);
        assertMomentOfDay("these midnights",  0,  2);
        assertMomentOfDay("this evening and", 21, 2);
        assertMomentOfDay("at tonight test",  23, 2);
        assertMomentOfDay("nighttime test",   3,  1);
        assertMomentOfDay("after dinner",     21, 2);
        assertMomentOfDay("before the lunch", 11, 3);
        assertMomentOfDay("the dinner",       20, 2);
    }

    @Test
    public void testMomentOfDayNull() {
        assertMomentOfDayNull("hello how are you");
        assertMomentOfDayNull("and at midday");
        assertMomentOfDayNull("mid night");
        assertMomentOfDayNull("at hour dinner");
        assertMomentOfDayNull("in dinner");
    }

    @Test
    public void testSpecialMinute() {
        assertSpecialMinute("a quarter to",            -15, 3);
        assertSpecialMinute("half of past test",       30,  3);
        assertSpecialMinute("a half to eleven",        -30, 3);
        assertSpecialMinute("zero point two of past",  12,  5);
        assertSpecialMinute("thirteen fourteenths to", -56, 3); // 13/14*60 is 55.7 -> rounded to 56
        assertSpecialMinute("at twenty the past",      20,  4);
        assertSpecialMinute("the fifty and nine to",   -59, 5);
        assertSpecialMinute("fifteen past twelve",     15,  2);
    }

    @Test
    public void testSpecialMinuteNull() {
        assertSpecialMinuteNull("hello how are you");
        assertSpecialMinuteNull("two");
        assertSpecialMinuteNull("one hundred and twelve to");
        assertSpecialMinuteNull("minus a quarter to five");
        assertSpecialMinuteNull("four quarters to nine");
        assertSpecialMinuteNull("zero halfs to");
        assertSpecialMinuteNull("zero and comma two past");
        assertSpecialMinuteNull("thirteen and fourteenths past");
        assertSpecialMinuteNull("and fifteen past twelve");
    }

    @Test
    public void testOClock() {
        assertOClock("o clock",    2);
        assertOClock("o'clock",    2);
        assertOClock("oclock",     1);
        assertOClock("o,clock",    3);
        assertOClock("exact",      1);
        assertOClock("on the dot", 3);
    }

    @Test
    public void testOClockFalse() {
        assertOClockFalse("hello");
        assertOClockFalse("by the clock");
        assertOClockFalse("clock o");
        assertOClockFalse("clock");
        assertOClockFalse("on");
    }

    @Test
    public void testDate() {
        assertDate("04/09-4096",                                  F, LocalDate.of(4096,  9,  4),  5);
        assertDate("04/09-4096",                                  T, LocalDate.of(4096,  4,  9),  5);
        assertDate("4 13 2023",                                      LocalDate.of(2023,  4,  13), 3);
        assertDate("13.4.2023",                                      LocalDate.of(2023,  4,  13), 5);
        assertDate("six of seven of nineteen ninety five",        F, LocalDate.of(1995,  7,  6),  7);
        assertDate("six of seven of nineteen ninety five",        T, LocalDate.of(1995,  6,  7),  7);
        assertDate("thursday 26 of may 2022",                        LocalDate.of(2022,  5,  26), 5);
        assertDate("august the second, two",                         LocalDate.of(2,     8,  2),  5);
        assertDate("2nd january, two b.c.",                          LocalDate.of(-2,    1,  2),  8);
        assertDate("mon twelve jun two thousand twelve b.C.",        LocalDate.of(-2012, 6,  12), 9);
        assertDate("four hundred seventy six AD",                    LocalDate.of(476,   1,  1),  5);
        assertDate("four thousand before common era",                LocalDate.of(-4000, 1,  1),  5);
        assertDate("four thousand of before Christ",                 LocalDate.of(4000,  1,  1),  2);
        assertDate("tuesday and twenty seven",                       LocalDate.of(2023,  2,  27), 4);
        assertDate("tuesday and twelve",                          F, LocalDate.of(2023,  2,  12), 3);
        assertDate("tuesday and twelve",                          T, LocalDate.of(2023,  12, 1),  3); // a bit strange
        assertDate("november e",                                     LocalDate.of(2023,  11, 1),  1);
        assertDate("wednesday test eight",                           LocalDate.of(2023,  2,  1),  1);
        assertDate("monday november",                                LocalDate.of(2023,  1,  30), 1);
        assertDate("october two thousand and twelve",                LocalDate.of(2012,  10, 1),  5);
        assertDate("999999999",                                      LocalDate.of(999999999,1,1), 1);
        // the following work thanks to special case in number extractor!
        assertDate("twenty twelve",                                  LocalDate.of(2012,  1,  1),  2);
        assertDate("sunday twenty thirteen",                         LocalDate.of(2023,  2,  5),  1);
    }

    @Test
    public void testDateNull() {
        assertDateNull("hello how are you");
        assertDateNull("am tuedsay");
        assertDateNull("and two thousand and fifteen");
        assertDateNull("of may two");
        assertDateNull("tomorrow");
        assertDateNull("1000000000");
    }

    @Test
    public void testBcad() {
        // b.c.e special case, not covered by DateTimeExtractorUtils.bcad()
        assertBcad("bce",                false, 1);
        assertBcad("b.c.e.",             false, 5);
        assertBcad("before current era", false, 3);
        assertBcad("current era",        true,  2);
    }

    @Test
    public void testTime() {
        assertTime("13:28.33 test",        LocalTime.of(13, 28, 33), 4);
        assertTime("half past noon",       LocalTime.of(12, 30, 0),  3);
        assertTime("at fourteen and",      LocalTime.of(14, 0,  0),  2);
        assertTime("midnight of twelve",   LocalTime.of(0,  12, 0),  3);
        assertTime("twenty four and zero", LocalTime.of(0,  0,  0),  4);
        assertTime("the twenty three and fifty one min and 17 seconds", LocalTime.of(23, 51, 17), 10);
    }

    @Test
    public void testTimeNull() {
        assertTimeNull("hello how are you");
        assertTimeNull("sixty one");
        assertTimeNull("30:59");
        assertTimeNull("minus sixteen");
        assertTimeNull("four million");
        assertTimeNull("evening");
    }

    @Test
    public void testTimeWithAmpm() {
        assertTimeWithAmpm("11:28.33 pm test",                    LocalTime.of(23, 28, 33), 5);
        assertTimeWithAmpm("half past noon and a quarter",        LocalTime.of(12, 30, 0),  3);
        assertTimeWithAmpm("at two o'clock in the morning",       LocalTime.of(2,  0,  0),  7);
        assertTimeWithAmpm("three thirty eight in the afternoon", LocalTime.of(15, 38, 0),  6);
        assertTimeWithAmpm("18:29:02 and am",                     LocalTime.of(18, 29, 2),  5);
        assertTimeWithAmpm("evening",                             LocalTime.of(21, 0,  0),  1);
        assertTimeWithAmpm("afternoon at four and three and six", LocalTime.of(16, 3,  6),  7);
        // this turns out wrong, but it is a corner case
        assertTimeWithAmpm("twenty four in the evening",          LocalTime.of(12, 0,  0),  5);
    }

    @Test
    public void testTimeWithAmpmNull() {
        assertTimeWithAmpmNull("hello how are you");
        assertTimeWithAmpmNull("sixty one");
        assertTimeWithAmpmNull("30:59");
        assertTimeWithAmpmNull("minus sixteen");
        assertTimeWithAmpmNull("four million");
    }

    @Test
    public void testDateTime() {
        assertDateTime("tomorrow at 12:45",                      LocalDateTime.of(2023, 2,  6,  12, 45, 0),  4);
        assertDateTime("26/12/2003 19:18:59",                    LocalDateTime.of(2003, 12, 26, 19, 18, 59), 8);
        assertDateTime("19:18:59 26/12/2003 test",               LocalDateTime.of(2003, 12, 26, 19, 18, 59), 8);
        assertDateTime("12/26/2003 19:18:59 and",                LocalDateTime.of(2003, 12, 26, 19, 18, 59), 8);
        assertDateTime("19:18:59 12/26/2003",                    LocalDateTime.of(2003, 12, 26, 19, 18, 59), 8);
        assertDateTime("7/5/2003 1:2:3 test",                 F, LocalDateTime.of(2003, 5,  7,  1,  2,  3),  8);
        assertDateTime("7/5/2003 1:2:3",                      T, LocalDateTime.of(2003, 7,  5,  1,  2,  3),  8);
        assertDateTime("1:2:3 7/5/2003 and",                  F, LocalDateTime.of(2003, 5,  7,  1,  2,  3),  8);
        assertDateTime("1:2:3 7/5/2003",                      T, LocalDateTime.of(2003, 7,  5,  1,  2,  3),  8);
        assertDateTime("next friday at twenty two o clock",      LocalDateTime.of(2023, 2,  10, 22, 0,  0),  7);
        assertDateTime("the 6 post meridiem of next tuesday",    LocalDateTime.of(2023, 2,  7,  18, 0,  0),  7);
        assertDateTime("yesterday evening at twenty to 5",       LocalDateTime.of(2023, 2,  4,  16, 40, 0),  6);
        assertDateTime("in three days evening at eleven",        LocalDateTime.of(2023, 2,  8,  23, 0,  0),  6);
        assertDateTime("day after morrow and morning test",      LocalDateTime.of(2023, 2,  7,  9,  0,  0),  5);
        assertDateTime("sunday at 2:45 p.m.",                    LocalDateTime.of(2023, 2,  5,  14, 45, 0),  7);
        assertDateTime("twenty first of jan after a dinner",     LocalDateTime.of(2023, 1,  21, 21, 0,  0),  7);
        assertDateTime("two days ago at four 40 at dusk",        LocalDateTime.of(2023, 2,  3,  16, 40, 0),  8);
        assertDateTime("twenty seventh of july at nine thirty nine in the evening", LocalDateTime.of(2023, 7,  27, 21, 39, 0), 11);
        assertDateTime("twenty three milliseconds",           NOW.withDayOfMonth(23),  2);
        assertDateTime("next three months on the dot",        NOW.plusMonths(3),       3);
        assertDateTime("in fifteen d",                        NOW.plusDays(15),        3);
        assertDateTime("thirty two nanoseconds ago",          NOW.minusNanos(32),      4);
        assertDateTime("two days and seven milliseconds ago", NOW.minusNanos(7000000).minusDays(2), 6);
        assertDateTime("seventh of november, 193 b.C.",       NOW.withYear(-193).withMonth(11).withDayOfMonth(7), 8);
    }

    @Test
    public void testDateTimeNull() {
        assertDateTimeNull("hello how are you");
        assertDateTimeNull("test twenty first of jan after a dinner");
        assertDateTimeNull("minus one millisecond");
    }

    @Test
    public void testNumberParserExtractDateTime() {
        final ParserFormatter npf = new ParserFormatter(null, new EnglishParser());
        assertNull(npf.extractDateTime("hello how are you").getFirst());
        assertEquals(NOW.minusDays(30).withHour(14).withMinute(39).withSecond(0).withNano(0),
                npf.extractDateTime("2:39 p.m., thirty days ago").now(NOW).getFirst());
        assertEquals(NOW.plusMinutes(3).plusSeconds(46),
                npf.extractDateTime("in three minutes forty six seconds").now(NOW).getFirst());
        assertEquals(NOW.withYear(3).withMonth(2).withDayOfMonth(1),
                npf.extractDateTime("1 2/3").preferMonthBeforeDay(false).now(NOW).getFirst());
        assertEquals(NOW.withYear(3).withMonth(1).withDayOfMonth(2),
                npf.extractDateTime("1.2,3").preferMonthBeforeDay(true).now(NOW).getFirst());
    }
}
