package org.dicio.numbers.lang.it;

import static org.dicio.numbers.lang.it.ItalianDateTimeExtractor.isMomentOfDayPm;
import static org.dicio.numbers.test.TestUtils.niceDuration;
import static org.dicio.numbers.test.TestUtils.numberDeduceType;
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

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.WithTokenizerTestBase;
import org.dicio.numbers.unit.Duration;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

public class ExtractDateTimeTest extends WithTokenizerTestBase {

    // Tuesday the 10th of May, 2022, 19:38:36
    private static final LocalDateTime NOW = LocalDateTime.of(2022, 5, 10, 19, 38, 36, 295834726);

    @Override
    public String configFolder() {
        return "config/it-it";
    }


    private Duration t(final double number, final ChronoUnit chronoUnit) {
        return new Duration().plus(numberDeduceType(number), chronoUnit);
    }

    private void assertRelativeDurationFunction(final String s,
                                                final Duration expectedDuration,
                                                final int finalTokenStreamPosition,
                                                final Function<ItalianDateTimeExtractor, Duration> durationFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Duration actualDuration = durationFunction.apply(new ItalianDateTimeExtractor(ts, NOW));
        assertNotNull("null relative duration for string \"" + s + "\"", actualDuration);
        assertEquals("wrong final token position for string \"" + s + "\"",
                finalTokenStreamPosition, ts.getPosition());
        assertTrue("wrong relative duration for string \"" + s + "\": expected \""
                        + niceDuration(expectedDuration) + "\" but got \""
                        + niceDuration(actualDuration) + "\"",
                expectedDuration.getNanos().equals(actualDuration.getNanos())
                        && expectedDuration.getDays().equals(actualDuration.getDays())
                        && expectedDuration.getMonths().equals(actualDuration.getMonths())
                        && expectedDuration.getYears().equals(actualDuration.getYears()));
    }

    private void assertRelativeDurationFunctionNull(final String s,
                                                    final Function<ItalianDateTimeExtractor, Duration> durationFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Duration duration = durationFunction.apply(new ItalianDateTimeExtractor(ts, NOW));

        if (duration != null) {
            fail("expected no relative duration (null), but got \"" + niceDuration(duration)
                    + "\"");
        }
    }

    private <T> void assertFunction(final String s,
                                    final T expectedResult,
                                    int finalTokenStreamPosition,
                                    final Function<ItalianDateTimeExtractor, T> function) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        assertEquals("wrong result for string \"" + s + "\"",
                expectedResult, function.apply(new ItalianDateTimeExtractor(ts, NOW)));
        assertEquals("wrong final token position for string \"" + s + "\"",
                finalTokenStreamPosition, ts.getPosition());
    }

    private <T> void assertFunctionNull(final String s,
                                        final Function<ItalianDateTimeExtractor, T> numberFunction) {
        assertFunction(s, null, 0, numberFunction);
    }

    private void assertRelativeDuration(final String s, final Duration expectedDuration, int finalTokenStreamPosition) {
        assertRelativeDurationFunction(s, expectedDuration, finalTokenStreamPosition, ItalianDateTimeExtractor::relativeDuration);
    }

    private void assertRelativeDurationNull(final String s) {
        assertRelativeDurationFunctionNull(s, ItalianDateTimeExtractor::relativeDuration);
    }

    private void assertRelativeMonthDuration(final String s, final Duration expectedDuration, int finalTokenStreamPosition) {
        assertRelativeDurationFunction(s, expectedDuration, finalTokenStreamPosition, ItalianDateTimeExtractor::relativeMonthDuration);
    }

    private void assertRelativeMonthDurationNull(final String s) {
        assertRelativeDurationFunctionNull(s, ItalianDateTimeExtractor::relativeMonthDuration);
    }

    private void assertRelativeDayOfWeekDuration(final String s, final int expectedDuration, int finalTokenStreamPosition) {
        assertFunction(s, expectedDuration, finalTokenStreamPosition, ItalianDateTimeExtractor::relativeDayOfWeekDuration);
    }

    private void assertRelativeDayOfWeekDurationNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::relativeDayOfWeekDuration);
    }

    private void assertRelativeTomorrow(final String s, final int expectedDuration, int finalTokenStreamPosition) {
        assertFunction(s, expectedDuration, finalTokenStreamPosition, ItalianDateTimeExtractor::relativeTomorrow);
    }

    private void assertRelativeTomorrowNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::relativeTomorrow);
    }

    private void assertRelativeToday(final String s) {
        assertFunction(s, 0, 1, ItalianDateTimeExtractor::relativeToday);
    }

    private void assertRelativeTodayNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::relativeToday);
    }

    private void assertRelativeYesterday(final String s, final int expectedDuration, int finalTokenStreamPosition) {
        assertFunction(s, expectedDuration, finalTokenStreamPosition, ItalianDateTimeExtractor::relativeYesterday);
    }

    private void assertRelativeYesterdayNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::relativeYesterday);
    }

    private void assertHour(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::hour);
    }

    private void assertHourNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::hour);
    }

    private void assertMomentOfDay(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::momentOfDay);
    }

    private void assertMomentOfDayNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::momentOfDay);
    }

    private void assertNoonMidnightLike(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::noonMidnightLike);
    }

    private void assertNoonMidnightLikeNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::noonMidnightLike);
    }

    private void assertMinute(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::minute);
    }

    private void assertMinuteNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::minute);
    }

    private void assertSpecialMinute(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::specialMinute);
    }

    private void assertSpecialMinuteNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::specialMinute);
    }

    private void assertSecond(final String s, final int expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::second);
    }

    private void assertSecondNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::second);
    }

    private void assertBcad(final String s, final Boolean expectedAd, int finalTokenStreamPosition) {
        assertFunction(s, expectedAd, finalTokenStreamPosition, ItalianDateTimeExtractor::bcad);
    }

    private void assertBcadNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::bcad);
    }

    private void assertAmpm(final String s, final Boolean expectedAd, int finalTokenStreamPosition) {
        assertFunction(s, expectedAd, finalTokenStreamPosition, ItalianDateTimeExtractor::ampm);
    }

    private void assertAmpmNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::ampm);
    }

    private void assertDayOfWeek(final String s, final int expected) {
        assertFunction(s, expected, 1, ItalianDateTimeExtractor::dayOfWeek);
    }

    private void assertDayOfWeekNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::dayOfWeek);
    }

    private void assertMonthName(final String s, final int expected) {
        assertFunction(s, expected, 1, ItalianDateTimeExtractor::monthName);
    }

    private void assertMonthNameNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::monthName);
    }

    private void assertDate(final String s, final LocalDate expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::date);
    }

    private void assertDateNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::date);
    }

    private void assertTime(final String s, final LocalTime expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::time);
    }

    private void assertTimeNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::time);
    }

    private void assertTimeWithAmpm(final String s, final LocalTime expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::timeWithAmpm);
    }

    private void assertTimeWithAmpmNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::timeWithAmpm);
    }

    private void assertDateTime(final String s, final LocalDateTime expected, int finalTokenStreamPosition) {
        assertFunction(s, expected, finalTokenStreamPosition, ItalianDateTimeExtractor::dateTime);
    }

    private void assertDateTimeNull(final String s) {
        assertFunctionNull(s, ItalianDateTimeExtractor::dateTime);
    }


    @Test
    public void testRelativeDuration() {
        assertRelativeDuration("tra due settimane vado al mare", t(2, WEEKS),   3);
        assertRelativeDuration("in quattro mesi scorsi",         t(4, MONTHS),  3);
        assertRelativeDuration("dopo secondi è caduto",          t(1, SECONDS), 2);
        assertRelativeDuration("in un paio di decenni",          t(20, YEARS),  5);
        assertRelativeDuration("nove e giorni è fa e",           t(-9, DAYS),   5);
        assertRelativeDuration("settant'anni a questa parte",    t(-70, YEARS), 5);
        assertRelativeDuration("tre mesi e due giorni prima",    t(-3, MONTHS).plus(t(-2, DAYS)), 6);
        assertRelativeDuration("sessantadue secoli passati sono seimiladuecento anni fa", t(-6200, YEARS), 4);
    }

    @Test
    public void testRelativeDurationNull() {
        assertRelativeDurationNull("ciao come va");
        assertRelativeDurationNull("quattro semestri");
        assertRelativeDurationNull("sai che tra una settimana");
        assertRelativeDurationNull("e due mesi fa");
        assertRelativeDurationNull("la giornata scorsa");
    }

    @Test
    public void testRelativeMonthDuration() {
        assertRelativeMonthDuration("settembre che viene",   t(4, MONTHS),   3);
        assertRelativeMonthDuration("aprile e prossimo e a", t(11, MONTHS),  3);
        assertRelativeMonthDuration("scorso e aprile e a",   t(-1, MONTHS),  3);
        assertRelativeMonthDuration("maggio che verrà",      t(12, MONTHS),  3);
        assertRelativeMonthDuration("maggio è passato",      t(-12, MONTHS), 3);
        assertRelativeMonthDuration("in gennaio",            t(8, MONTHS),   2);
    }

    @Test
    public void testRelativeMonthDurationNull() {
        assertRelativeMonthDurationNull("ciao come va");
        assertRelativeMonthDurationNull("questo novembre fa");
        assertRelativeMonthDurationNull("ottobre");
        assertRelativeMonthDurationNull("tra due ottobre");
        assertRelativeMonthDurationNull("tra due mesi");
    }

    @Test
    public void testRelativeDayOfWeekDuration() {
        assertRelativeDayOfWeekDuration("giovedì prossimo",     2,   2);
        assertRelativeDayOfWeekDuration("giovedi scorso",       -5,  2);
        assertRelativeDayOfWeekDuration("tra due domeniche si", 12,  3);
        assertRelativeDayOfWeekDuration("due e domenica e fa",  -9,  5);
        assertRelativeDayOfWeekDuration("tre lunedì e prima e", -15, 4);
        assertRelativeDayOfWeekDuration("martedì prossimo",     7,   2);
        assertRelativeDayOfWeekDuration("un martedì fa",        -7,  3);
    }

    @Test
    public void testRelativeDayOfWeekDurationNull() {
        assertRelativeDayOfWeekDurationNull("ciao come va");
        assertRelativeDayOfWeekDurationNull("lunedi");
        assertRelativeDayOfWeekDurationNull("due venerdì");
        assertRelativeDayOfWeekDurationNull("tra due giorni");
        assertRelativeDayOfWeekDurationNull("e tra due domeniche");
        assertRelativeDayOfWeekDurationNull("ieri e domani");
    }

    @Test
    public void testRelativeTomorrow() {
        assertRelativeTomorrow("domani andiamo",            1, 1);
        assertRelativeTomorrow("dopodomani e",              2, 2);
        assertRelativeTomorrow("dopo di domani test",       2, 3);
        assertRelativeTomorrow("dopo e dopodomani e",       3, 4);
        assertRelativeTomorrow("dopo dopo dopo dopodomani", 5, 5);
    }

    @Test
    public void testRelativeTomorrowNull() {
        assertRelativeTomorrowNull("ciao come va");
        assertRelativeTomorrowNull("e domani");
        assertRelativeTomorrowNull("il dopo domani");
        assertRelativeTomorrowNull("ieri");
        assertRelativeTomorrowNull("oggi");
    }

    @Test
    public void testRelativeToday() {
        assertRelativeToday("oggi");
        assertRelativeToday("oggi proprio oggi");
        assertRelativeToday("oggi test");
        assertRelativeToday("oggi e");
    }

    @Test
    public void testRelativeTodayNull() {
        assertRelativeTodayNull("ciao come va");
        assertRelativeTodayNull("proprio oggi");
        assertRelativeTodayNull("l'oggi");
        assertRelativeTodayNull("e oggi");
        assertRelativeTodayNull("ieri");
        assertRelativeTodayNull("domani");
    }

    @Test
    public void testRelativeYesterday() {
        assertRelativeYesterday("ieri sono stato",            -1, 1);
        assertRelativeYesterday("altro l'ieri test",          -2, 3);
        assertRelativeYesterday("ieri l'altro e",             -2, 3);
        assertRelativeYesterday("ieri l'altro l'altro",       -2, 3);
        assertRelativeYesterday("altro ieri altro",           -2, 2);
        assertRelativeYesterday("altroieri",                  -2, 2);
        assertRelativeYesterday("altro l'altro l'altro ieri", -4, 6);
    }

    @Test
    public void testRelativeYesterdayNull() {
        assertRelativeYesterdayNull("ciao come va");
        assertRelativeYesterdayNull("e ieri");
        assertRelativeYesterdayNull("l'altro ieri");
        assertRelativeYesterdayNull("altri ieri");
        assertRelativeYesterdayNull("oggi");
        assertRelativeYesterdayNull("domani");
    }

    @Test
    public void testHour() {
        assertHour("ventuno test",     21, 2);
        assertHour("le zero e",        0,  2);
        assertHour("l'una e ventisei", 1,  2);
        assertHour("dodici in punto",  12, 1);
        assertHour("alle diciassette", 17, 2);
        assertHour("all'ora alle tre", 3,  4);
        assertHour("alle ore tredici", 13, 3);
    }

    @Test
    public void testHourNull() {
        assertHourNull("ciao come va");
        assertHourNull("venticinque");
        assertHourNull("le meno due");
        assertHourNull("alle cento cinquanta quattro");
        assertHourNull("il sette");
        assertHourNull("alle ore");
        assertHourNull("le e zero e");
        assertHourNull("e venti quattro");
        assertHourNull("l'un milione");
    }

    @Test
    public void testNoonMidnightLike() {
        assertNoonMidnightLike("a mezzanotte",       0,  2);
        assertNoonMidnightLike("mezzo giorno",       12, 2);
        assertNoonMidnightLike("queste mezze notti", 0,  3);
    }

    @Test
    public void testNoonMidnightLikeNull() {
        assertNoonMidnightLikeNull("ciao come va");
        assertNoonMidnightLikeNull("questa sera e");
        assertNoonMidnightLikeNull("stanotte test");
        assertNoonMidnightLikeNull("dopo cena");
        assertNoonMidnightLikeNull("prima del pranzo");
        assertNoonMidnightLikeNull("e a mezzogiorno");
        assertNoonMidnightLikeNull("mezza è notte");
        assertNoonMidnightLikeNull("la mezzanotte");
        assertNoonMidnightLikeNull("alle ore mezzogiorno");
        assertNoonMidnightLikeNull("fra mezza notte");
    }

    @Test
    public void testMomentOfDay() {
        assertMomentOfDay("a mezzanotte",       0,  2);
        assertMomentOfDay("mezzo giorno",       12, 2);
        assertMomentOfDay("queste mezze notti", 0,  3);
        assertMomentOfDay("questa sera e",      21, 2);
        assertMomentOfDay("stanotte test",      3,  1);
        assertMomentOfDay("dopo cena",          21, 2);
        assertMomentOfDay("prima del pranzo",   11, 3);
    }

    @Test
    public void testMomentOfDayNull() {
        assertMomentOfDayNull("ciao come va");
        assertMomentOfDayNull("e a mezzogiorno");
        assertMomentOfDayNull("mezza è notte");
        assertMomentOfDayNull("la cena");
        assertMomentOfDayNull("alle ore cena");
        assertMomentOfDayNull("fra cena");
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

    @Test
    public void testMinute() {
        assertMinute("zero a b c",        0,  1);
        assertMinute("cinquantanove ore", 59, 2);
        assertMinute("quindici e",        15, 1);
        assertMinute("venti e otto s",    28, 3);
        assertMinute("sei minuti test",   6,  2);
        assertMinute("trentasei e min",   36, 2);
        assertMinute("44m e",             44, 2);
    }

    @Test
    public void testMinuteNull() {
        assertMinuteNull("ciao come va");
        assertMinuteNull("sessanta minuti");
        assertMinuteNull("cento venti");
        assertMinuteNull("meno sedici");
        assertMinuteNull("12000 minuti");
        assertMinuteNull("e due e");
    }

    @Test
    public void testSpecialMinute() {
        assertSpecialMinute("un quarto e",              15, 2);
        assertSpecialMinute("mezza test",               30, 1);
        assertSpecialMinute("un mezzo",                 30, 2);
        assertSpecialMinute("zero virgola due",         12, 3);
        assertSpecialMinute("tredici ottantasettesimi", 9,  3); // 13/87*60 is 8.97 -> rounded to 9
    }

    @Test
    public void testSpecialMinuteNull() {
        assertSpecialMinuteNull("ciao come va");
        assertSpecialMinuteNull("due");
        assertSpecialMinuteNull("cento dodici");
        assertSpecialMinuteNull("meno un quarto");
        assertSpecialMinuteNull("quattro quarti");
        assertSpecialMinuteNull("zero mezzi");
        assertSpecialMinuteNull("zero e virgola due");
        assertSpecialMinuteNull("tredici e ottantasettesimi");
    }

    @Test
    public void testSecond() {
        assertSecond("zero a b c",        0,  1);
        assertSecond("cinquantanove ore", 59, 2);
        assertSecond("quindici e",        15, 1);
        assertSecond("venti e otto m",    28, 3);
        assertSecond("sei secondo test",  6,  2);
        assertSecond("trentasei e sec",   36, 2);
        assertSecond("44s e",             44, 2);
    }

    @Test
    public void testSecondNull() {
        assertSecondNull("ciao come va");
        assertSecondNull("sessanta secondi");
        assertSecondNull("cento venti");
        assertSecondNull("meno sedici");
        assertSecondNull("12000 secondi");
        assertSecondNull("dodici mila");
        assertSecondNull("e due e");
    }

    @Test
    public void testBcad() {
        assertBcad("a.C. test",   false, 3);
        assertBcad("d.C. e",      true,  3);
        assertBcad("dc test e",   true,  1);
        assertBcad("dopo Cristo", true,  2);
        assertBcad("c test",      false, 1);
        assertBcad("a e Cristo",  false, 3);
    }

    @Test
    public void testBcadNull() {
        assertBcadNull("a.m.");
        assertBcadNull("dopo test Cristo");
        assertBcadNull("e avanti Cristo");
        assertBcadNull("test c");
        assertBcadNull("m");
    }

    @Test
    public void testAmpm() {
        assertAmpm("a.m. test",      false, 3);
        assertAmpm("p.m. e",         true,  3);
        assertAmpm("am e test",      false, 1);
        assertAmpm("post meridiano", true,  2);
        assertAmpm("meridian test",  false, 1);
        assertAmpm("p e meridiem",   true,  3);
    }

    @Test
    public void testAmpmNull() {
        assertAmpmNull("a.C.");
        assertAmpmNull("ante test meridiem");
        assertAmpmNull("e post m");
        assertAmpmNull("test m");
        assertAmpmNull("c");
        assertAmpmNull("aem");
    }

    @Test
    public void testDayOfWeek() {
        assertDayOfWeek("lunedì",         0);
        assertDayOfWeek("domeniche test", 6);
        assertDayOfWeek("sab e",          5);
        assertDayOfWeek("mar",            1);
    }

    @Test
    public void testDayOfWeekNull() {
        assertDayOfWeekNull("gennaio");
        assertDayOfWeekNull("vendro");
        assertDayOfWeekNull("ciao martedì");
        assertDayOfWeekNull("e ven to");
    }

    @Test
    public void testMonthName() {
        assertMonthName("gennaio",    0);
        assertMonthName("dic e",      11);
        assertMonthName("sett embre", 8);
        assertMonthName("mar",        2);
    }

    @Test
    public void testMonthNameNull() {
        assertMonthNameNull("lunedì");
        assertMonthNameNull("genner");
        assertMonthNameNull("ciao feb");
        assertMonthNameNull("e dic to");
    }

    @Test
    public void testDate() {
        assertDate("04/09-4096",                                  LocalDate.of(4096,  9,  4),  5);
        assertDate("giovedì 26 del maggio 2022",                  LocalDate.of(2022,  5,  26), 5);
        assertDate("lun dodici giu duemila dodici avanti cristo", LocalDate.of(-2012, 6,  12), 8);
        assertDate("quattrocento settanta sei d.C.",              LocalDate.of(476,   1,  1),  7);
        assertDate("quattromila avanti cristo",                   LocalDate.of(-4000, 1,  1),  4);
        assertDate("quattromila dell'avanti cristo",              LocalDate.of(4000,  1,  1),  2);
        assertDate("martedì e ventisette",                        LocalDate.of(2022,  5,  27), 4);
        assertDate("duemila dodici",                              LocalDate.of(2012,  1,  1),  3);
        assertDate("novembre e",                                  LocalDate.of(2022,  11, 1),  1);
        assertDate("martedì test",                                LocalDate.of(2022,  5,  10), 1);
        assertDate("domenica duemilatredici",                     LocalDate.of(2022,  5,  15), 1);
        assertDate("lunedì novembre",                             LocalDate.of(2022,  5,  9),  1);
    }

    @Test
    public void testDateNull() {
        assertDateNull("ciao come va");
        assertDateNull("sono martedì");
        assertDateNull("e duemilaquindici");
        assertDateNull("del due maggio");
        assertDateNull("domani");
    }

    @Test
    public void testTime() {
        assertTime("13:28.33 test",                             LocalTime.of(13, 28, 33), 4);
        assertTime("mezzogiorno e mezzo",                       LocalTime.of(12, 30, 0),  3);
        assertTime("alle quattordici e",                        LocalTime.of(14, 0,  0),  2);
        assertTime("le ventitre e cinquantun min e 17 secondi", LocalTime.of(23, 51, 17), 10);
        assertTime("mezzanotte del dodici",                     LocalTime.of(0,  12, 0),  3);
        assertTime("ventiquattro e zero",                       LocalTime.of(0,  0,  0),  4);
    }

    @Test
    public void testTimeNull() {
        assertTimeNull("ciao come va");
        assertTimeNull("sessantuno");
        assertTimeNull("30:59");
        assertTimeNull("meno sedici");
        assertTimeNull("quattro milioni");
        assertTimeNull("sera");
    }

    @Test
    public void testTimeWithAmpm() {
        assertTimeWithAmpm("11:28.33 pm test",                          LocalTime.of(23, 28, 33), 5);
        assertTimeWithAmpm("mezzogiorno e mezzo dopo pranzo",           LocalTime.of(12, 30, 0),  5);
        assertTimeWithAmpm("alle due di notte",                         LocalTime.of(2,  0,  0),  4);
        assertTimeWithAmpm("le tre e trentotto di pomeriggio",          LocalTime.of(15, 38, 0),  7);
        assertTimeWithAmpm("18:29:02 e am",                             LocalTime.of(18, 29, 2),  5);
        assertTimeWithAmpm("sera",                                      LocalTime.of(21, 0,  0),  1);
        assertTimeWithAmpm("pomeriggio alle quattro e tre e sei",       LocalTime.of(16, 3,  6),  7);
        // this turns out wrong, but it is a corner case
        assertTimeWithAmpm("le ventiquattro di sera",                   LocalTime.of(12, 0,  0),  5);
    }

    @Test
    public void testTimeWithAmpmNull() {
        assertTimeWithAmpmNull("ciao come va");
        assertTimeWithAmpmNull("sessantuno");
        assertTimeWithAmpmNull("30:59");
        assertTimeWithAmpmNull("meno sedici");
        assertTimeWithAmpmNull("quattro milioni");
    }

    @Test
    public void testDateTime() {
        assertDateTime("domani alle 12:45",                                LocalDateTime.of(2022, 5,  11, 12, 45, 0),  4);
        assertDateTime("26/12/2003 19:18:59",                              LocalDateTime.of(2003, 12, 26, 19, 18, 59), 8);
        assertDateTime("19:18:59 26/12/2003",                              LocalDateTime.of(2003, 12, 26, 19, 18, 59), 8);
        assertDateTime("lunedì prossimo alle ventidue",                    LocalDateTime.of(2022, 5,  16, 22, 0,  0),  5);
        assertDateTime("le 6 post meridiem di martedì prossimo",           LocalDateTime.of(2022, 5,  17, 18, 0,  0),  7);
        assertDateTime("ventisette luglio alle nove e trentanove di sera", LocalDateTime.of(2022, 7,  27, 21, 39, 0),  10);
        assertDateTime("ieri sera alle 5 e mezza",                         LocalDateTime.of(2022, 5,  9,  17, 30, 0),  6);
        assertDateTime("domani sera alle undici",                          LocalDateTime.of(2022, 5,  11, 23, 0,  0),  4);
        assertDateTime("ieri e mattina test",                              LocalDateTime.of(2022, 5,  9,  9,  0,  0),  3);
        assertDateTime("domenica alle 2:45 p.m.",                          LocalDateTime.of(2022, 5,  15, 14, 45, 0),  7);
        assertDateTime("ventun gennaio dopo la cena",                      LocalDateTime.of(2022, 1,  21, 21, 0,  0),  6);
        assertDateTime("fra due giorni alle quattro e 40 di pomeriggio",   LocalDateTime.of(2022, 5,  12, 16, 40, 0),  9);
        assertDateTime("ventitre millisecondi",     NOW.withDayOfMonth(23),  2);
        assertDateTime("fra tre mesi",              NOW.plusMonths(3),       3);
        assertDateTime("in quindici gg",            NOW.plusDays(15),        3);
        assertDateTime("trenta due nanosecondi fa", NOW.minusNanos(32),      4);
        assertDateTime("sette novembre del 193 a.C.", NOW.withYear(-193).withMonth(11).withDayOfMonth(7), 7);
    }

    @Test
    public void testDateTimeNull() {
        assertDateTimeNull("ciao come va");
        assertDateTimeNull("il ventun gennaio dopo cena");
        assertDateTimeNull("meno centotre millisecondi");
    }
}
