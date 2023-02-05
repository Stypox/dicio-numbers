package org.dicio.numbers.lang.it;

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

    // Tuesday the 10th of May, 2022, 19:38:36
    private static final LocalDateTime NOW = LocalDateTime.of(2022, 5, 10, 19, 38, 36, 295834726);

    @Override
    public String configFolder() {
        return "config/it-it";
    }

    @Override
    public DateTimeExtractorUtils build(final TokenStream ts) {
        final ItalianNumberExtractor numberExtractor = new ItalianNumberExtractor(ts);
        return new DateTimeExtractorUtils(ts, NOW, (fromInclusive, toInclusive) ->
            NumberExtractorUtils.extractOneIntegerInRange(ts, fromInclusive, toInclusive,
                    () -> signBeforeNumber(ts, () -> numberExtractor.numberInteger(false)))
        );
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
        assertBcad("a e Cristo",  false, 3);
    }

    @Test
    public void testBcadNull() {
        assertBcadNull("a.m.");
        assertBcadNull("dopo test Cristo");
        assertBcadNull("e avanti Cristo");
        assertBcadNull("test c");
        assertBcadNull("m");
        assertBcadNull("c test");
    }

    @Test
    public void testAmpm() {
        assertAmpm("a.m. test",      false, 3);
        assertAmpm("p.m. e",         true,  3);
        assertAmpm("am e test",      false, 1);
        assertAmpm("post meridiano", true,  2);
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
        assertAmpmNull("meridian test");
    }

    @Test
    public void testMonthName() {
        assertMonthName("gennaio",    1);
        assertMonthName("dic e",      12);
        assertMonthName("sett embre", 9);
        assertMonthName("mar",        3);
    }

    @Test
    public void testMonthNameNull() {
        assertMonthNameNull("lunedì");
        assertMonthNameNull("genner");
        assertMonthNameNull("ciao feb");
        assertMonthNameNull("e dic to");
    }
}
