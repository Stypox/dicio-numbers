package org.dicio.numbers.lang.it;

import static org.dicio.numbers.test.TestUtils.DAY;
import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.HOUR;
import static org.dicio.numbers.test.TestUtils.MILLIS;
import static org.dicio.numbers.test.TestUtils.MINUTE;
import static org.dicio.numbers.test.TestUtils.T;
import static org.dicio.numbers.test.TestUtils.YEAR;
import static org.dicio.numbers.test.TestUtils.n;
import static org.dicio.numbers.test.TestUtils.t;

import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.param.NumberParserParamsTestBase;
import org.dicio.numbers.unit.Number;
import org.junit.Test;

import java.time.LocalDateTime;

public class ParserParamsTest extends NumberParserParamsTestBase {

    @Override
    protected Parser numberParser() {
        return new ItalianParser();
    }

    protected void assertNumberFirst(final String s, final boolean preferOrdinal, final Number expectedResult) {
        assertNumberFirst(s, true, preferOrdinal, expectedResult);
    }

    protected void assertNumberMixedWithText(final String s, final boolean preferOrdinal, final Object... expectedResults) {
        assertNumberMixedWithText(s, true, preferOrdinal, expectedResults);
    }

    protected void assertDurationFirst(final String s, final java.time.Duration expectedResult) {
        assertDurationFirst(s, true, expectedResult);
    }

    protected void assertDurationMixedWithText(final String s, final Object... expectedResults) {
        assertDurationMixedWithText(s, true, expectedResults);
    }


    @Test
    public void testNumberFirst() {
        assertNumberFirst("è mille nove cento sessanta quattro trilionesimi", F, n(1964e-18, F));
        assertNumberFirst("36 dodicesimi di mela",                            T, n(3, F));
        assertNumberFirst("sono proprio centottesimo",                        F, n(12.5, F));
        assertNumberFirst("sono proprio centottesimo",                        T, n(108, T));
    }

    @Test
    public void testNumberMixedWithText() {
        assertNumberMixedWithText("un miliardesimo e mille sei cento novanta quattro",  F, n(1.0 / 1000000000.0, F), " e ", n(1694, F));
        assertNumberMixedWithText(" hello  ciao!, 3/5 o quattro settimi?", F, " hello  ciao!, ", n(3.0 / 5.0, F), " o ", n(4.0 / 7.0, F), "?");
        assertNumberMixedWithText(" hello  ciao!, quattro settimi o 3/5?", T, " hello  ciao!, ", n(4.0 / 7.0, F), " o ", n(3.0 / 5.0, F), "?");
        assertNumberMixedWithText("tre miliardesimo piu due",              T, n(3000000000L, T), " ", n(2, F));
        assertNumberMixedWithText("due miliardesimi meno cinquanta otto",  T, n(2000000000L, T), " ", n(-58, F));
        assertNumberMixedWithText("nove miliardesimi per undici",          F, n(9.0 / 1000000000.0, F), " per ", n(11, F));
        assertNumberMixedWithText("tre mezzi, non undici quarti",          T, n(3.0 / 2.0, F), ", non ", n(11.0 / 4.0, F));
        assertNumberMixedWithText("sei paia equivale a una dozzina ",      T, n(12, F), " equivale a ", n(12, F), " ");
        assertNumberMixedWithText("6 trilionesimi di una torta",           T, n(6000000000000000000L, T), " di ", n(1, F), " torta");
        assertNumberMixedWithText("Il trilionesimo",                       F, "Il ", n(1000000000000000000L, T));
        assertNumberMixedWithText("Un trilionesimo",                       F, n(1e-18, F));
        assertNumberMixedWithText("Un miliardo",                           T, n(1000000000L, F));
        assertNumberMixedWithText("Mille non più mille",                   F, n(1000, F), " non ", n(1000, F));
        assertNumberMixedWithText("Vince sei a zero ",                     T, "Vince ", n(6, F), " a ", n(0, F), " ");
    }

    @Test
    public void testNumberMixedWithTextCompound() {
        assertNumberMixedWithText("millemiliardesimi e milleseicento novantaquattro",              F, n(1000.0 / 1000000000.0, F), " e ", n(1694, F));
        assertNumberMixedWithText("è millenovecento sessantaquattro novanta quattro trilionesimi", F, "è ", n(1964.0 / 94e18, F));
        assertNumberMixedWithText("ventitreesimo meno cinquantotto ventinovesimi",                 T, n(23, T), " ", n(-2, F));
        assertNumberMixedWithText("novantasei trentaseiesimi più centosedici",                     F, n(96.0 / 36.0, F), " ", n(116, F));
        assertNumberMixedWithText("novantanove virgola unounozeroquattrotre virgola zerouno",      T, n(99.11043, F), " virgola ", n(0, F), n(1, F));
        assertNumberMixedWithText("venticinque dozzine trequarti virgola ventidueciao",            T, n(300, F), " ", n(3.0 / 4.0, F), " virgola ventidueciao");
        assertNumberMixedWithText("mezze coppie",                                                  T, n(0.5, F), " ", n(2, F));
        assertNumberMixedWithText("Ho ventitre anni",                                              F, "Ho ", n(23, F), " anni");
    }

    @Test
    public void testDurationFirst() {
        assertDurationFirst("Metti un timer di due minuti e trenta secondi test", t(2 * MINUTE + 30));
        assertDurationFirst("sai due anni fa non sono 750gg", t(2 * YEAR));
    }

    @Test
    public void testDurationMixedWithText() {
        assertDurationMixedWithText("sai due anni fa non sono 750gg", "sai ", t(2 * YEAR), " fa non sono ", t(750 * DAY));
        assertDurationMixedWithText("due ns e quattro ore mentre sei millisecondi.", t(4 * HOUR, 2), " mentre ", t(0, 6 * MILLIS), ".");
    }

    @Test
    public void testDateTimeFirst() {
        assertDateTimeFirst("quando nel millenovecento ottanta c'era", LocalDateTime.of(1, 2, 3, 4, 5, 6), LocalDateTime.of(1980, 1, 1, 4, 5, 6));
        assertDateTimeFirst("due giorni fa era aprile?", LocalDateTime.of(2015, 5, 4, 3, 2, 1), LocalDateTime.of(2015, 5, 2, 3, 2, 1));
        assertDateTimeFirst("quando ven 14 lug del 2017 alle 3:32:00 e dopo pranzo.", LocalDateTime.of(1, 2, 3, 4, 5, 6), LocalDateTime.of(2017, 7, 14, 15, 32, 0));
    }

    @Test
    public void testDateTimeMixedWithText() {
        assertDateTimeMixedWithText("nel 1612, non dieci anni fa!", LocalDateTime.of(1, 2, 3, 4, 5, 6), "nel ", LocalDateTime.of(1612, 1, 1, 4, 5, 6), ", non ", LocalDateTime.of(-9, 2, 3, 4, 5, 6), "!");
        assertDateTimeMixedWithText("ven 14 lug del 2017 alle 3:32:00 era dopo pranzo", LocalDateTime.of(9, 8, 7, 6, 5, 4), LocalDateTime.of(2017, 7, 14, 3, 32, 0), " era ", LocalDateTime.of(9, 8, 7, 13, 0, 0));
    }
}
