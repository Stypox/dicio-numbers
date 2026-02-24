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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.param.ExtractNumberParams;
import org.dicio.numbers.parser.param.ParserParams;
import org.dicio.numbers.parser.param.ParserParamsTestBase;
import org.dicio.numbers.unit.Number;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ParserParamsTest extends ParserParamsTestBase {

    @Override
    protected Parser numberParser() {
        return new ItalianParser();
    }

    protected void assertNumberFirst(final String s, final boolean preferOrdinal, final boolean integerOnly, final Number expectedResult) {
        assertNumberFirst(s, true, preferOrdinal, integerOnly, expectedResult);
    }

    protected void assertNumberFirstIfInteger(final String s, final boolean preferOrdinal, final boolean integerOnly, final Long expectedResult) {
        assertNumberFirstIfInteger(s, true, preferOrdinal, integerOnly, expectedResult);
    }

    protected void assertNumberMixedWithText(final String s, final boolean preferOrdinal, final boolean integerOnly, final Object... expectedResults) {
        assertNumberMixedWithText(s, true, preferOrdinal, integerOnly, expectedResults);
    }

    @SuppressWarnings("unchecked")
    protected void assertNumberPossibleIntervals(final String s, final boolean preferOrdinal, final boolean integerOnly, final ParserParams.MatchedRange<Number>... expectedRanges) {
        assertNumberPossibleIntervals(s, true, preferOrdinal, integerOnly, expectedRanges);
    }

    protected void assertDurationFirst(final String s, final java.time.Duration expectedResult) {
        assertDurationFirst(s, true, expectedResult);
    }

    protected void assertDurationMixedWithText(final String s, final Object... expectedResults) {
        assertDurationMixedWithText(s, true, expectedResults);
    }

    @SuppressWarnings("unchecked")
    protected void assertDurationPossibleIntervals(final String s, final ParserParams.MatchedRange<java.time.Duration>... expectedRanges) {
        assertDurationPossibleIntervals(s, true, expectedRanges);
    }


    // used in timeout tests below: they are built separately so they don't contribute to timeout
    static String longNumberMixedWithText;
    static int partsOfLongNumberMixedWithText;

    @BeforeClass
    public static void setupLongNumberMixedWithText() {
        final ParserFormatter npf = new ParserFormatter(new ItalianFormatter(), null);
        final List<String> strings = new ArrayList<>();
        for (int i = 0; i < 1100000000;) {
            if (i < 2200) {
                ++i; // test all numbers from 0 to 200 (also tests years!)
            } else if (i < 1000000) {
                i += 1207;
            } else {
                i += 299527; // roughly 10000 iterations
            }

            final double num = (i % 4 == 0) ? (1.0 / i) : i;
            strings.add(npf.pronounceNumber(num).places(0).get()); // not ordinal
            strings.add(npf.pronounceNumber(num).places(0).ordinal(T).get()); // ordinal
            strings.add(npf.niceNumber(num).speech(false).get()); // not speech
            strings.add(npf.niceNumber(num).speech(true).get()); // speech
            strings.add(String.valueOf(num));
            strings.add(i % 2 == 0 ? " ciao " : " di ");
            strings.add(i % 2 == 0 ? "invalido" : "un centesimo");
            strings.add(i % 2 == 0 ? " e " : " un ");
            strings.add(i % 2 == 0 ? "," : " ; ");
            strings.add("-++-+--+-+-");
            strings.add(i % 2 == 0 ? " più " : " meno ");
        }
        Collections.shuffle(strings, new Random(42));
        partsOfLongNumberMixedWithText = strings.size();
        longNumberMixedWithText = String.join("", strings);
    }


    @Test
    public void testNumberFirst() {
        assertNumberFirst("36 dodicesimi di mela",     T, F, n(3, F));
        assertNumberFirst("36 dodicesimi di mela",     T, T, n(36, F));
        assertNumberFirst("sono proprio centottesimo", F, F, n(12.5, F));
        assertNumberFirst("sono proprio centottesimo", F, T, n(108, T));
        assertNumberFirst("sono proprio centottesimo", T, F, n(108, T));
        assertNumberFirst("sono proprio centottesimo", T, T, n(108, T));
        assertNumberFirst("fanno 10/20",               F, F, n(0.5, F));
        assertNumberFirst("fanno 10/20",               F, T, n(10, F));
        assertNumberFirst("è mezzo",                   T, F, n(0.5, F));
        assertNumberFirst("è mezzo",                   T, T, null);
        assertNumberFirst("un",                        F, F, n(1, F));
        assertNumberFirst("whatever",                  F, F, null);
        assertNumberFirst("e whatever",                T, F, null);

        // edge case, "mille nove cento sessanta quattro trilioni" wouldn't be a valid short scale
        // integer, and so "mille" and "nove cento sessanta quattro trilioni" are considered two
        // different numbers
        assertNumberFirst("è mille nove cento sessanta quattro trilionesimi", F, F, n(1964e-18, F));
        assertNumberFirst("è mille nove cento sessanta quattro trilionesimi", F, T, n(1000, F));
        assertNumberFirst("è mille nove cento sessanta quattro trilionesimi", T, F, n(1000 / 964e18, F));
        assertNumberFirst("è mille nove cento sessanta quattro trilioni",     T, F, n(1000, F));
    }

    @Test
    public void testNumberFirstIfInteger() {
        assertNumberFirstIfInteger("fanno dieci centesimi ok?", F, F, null);
        assertNumberFirstIfInteger("fanno dieci centesimi ok?", F, T, 10L);
        assertNumberFirstIfInteger("è mezzo",                   F, F, null);
        assertNumberFirstIfInteger("è mezzo",                   T, T, null);
        assertNumberFirstIfInteger("due mezzi",                 T, F, 1L);
        assertNumberFirstIfInteger("due mezzi",                 T, T, 2L);
    }

    @Test
    public void testNumberMixedWithText() {
        assertNumberMixedWithText("un miliardesimo e mille sei cento novanta quattro",  F, F, n(1.0 / 1000000000.0, F), " e ", n(1694, F));
        assertNumberMixedWithText("un miliardesimo e mille sei cento novanta quattro",  F, T, n(1000000000, T), " e ", n(1694, F));
        assertNumberMixedWithText(" hello  ciao!, 3/5 o quattro settimi?", F, F, " hello  ciao!, ", n(3.0 / 5.0, F), " o ", n(4.0 / 7.0, F), "?");
        assertNumberMixedWithText(" hello  ciao!, quattro settimi o 3/5?", T, F, " hello  ciao!, ", n(4.0 / 7.0, F), " o ", n(3.0 / 5.0, F), "?");
        assertNumberMixedWithText(" hello  ciao!, quattro settimi o 3/5?", T, T, " hello  ciao!, ", n(4, F), " ", n(7, T), " o ", n(3, F), "/", n(5, F), "?");
        assertNumberMixedWithText("tre miliardesimo piu due",              T, F, n(3000000000L, T), " ", n(2, F));
        assertNumberMixedWithText("due miliardesimi meno cinquanta otto",  T, F, n(2000000000L, T), " ", n(-58, F));
        assertNumberMixedWithText("nove miliardesimi per undici",          F, F, n(9.0 / 1000000000.0, F), " per ", n(11, F));
        assertNumberMixedWithText("nove miliardesimi per undici",          F, T, n(9000000000L, T), " per ", n(11, F));
        assertNumberMixedWithText("tre mezzi, non undici quarti",          T, F, n(3.0 / 2.0, F), ", non ", n(11.0 / 4.0, F));
        assertNumberMixedWithText("tre mezzi, non undici quarti",          T, T, n(3, F), " mezzi, non ", n(11, F), " ", n(4, T));
        assertNumberMixedWithText("sei paia equivale a una dozzina ",      T, F, n(12, F), " equivale a ", n(12, F), " ");
        assertNumberMixedWithText("sei paia equivale a una dozzina ",      T, T, n(12, F), " equivale a ", n(12, F), " ");
        assertNumberMixedWithText("6 trilionesimi di una torta",           T, F, n(6000000000000000000L, T), " di ", n(1, F), " torta");
        assertNumberMixedWithText("6 trilionesimi di una torta",           F, T, n(6000000000000000000L, T), " di ", n(1, F), " torta");
        assertNumberMixedWithText("6 trilionesimi di una torta",           F, F, n(6e-18, F), " di ", n(1, F), " torta");
        assertNumberMixedWithText("Il trilionesimo",                       F, F, "Il ", n(1000000000000000000L, T));
        assertNumberMixedWithText("Un trilionesimo",                       F, F, n(1e-18, F));
        assertNumberMixedWithText("Un trilionesimo",                       F, T, n(1000000000000000000L, T));
        assertNumberMixedWithText("Un miliardo",                           T, F, n(1000000000L, F));
        assertNumberMixedWithText("Mille non più mille",                   F, F, n(1000, F), " non ", n(1000, F));
        assertNumberMixedWithText("Vince sei a zero ",                     T, T, "Vince ", n(6, F), " a ", n(0, F), " ");
        assertNumberMixedWithText("due mezzi",                             F, F, n(1, F));
        assertNumberMixedWithText("due mezzi",                             T, T, n(2, F), " mezzi");
    }

    @Test
    public void testNumberMixedWithTextCompound() {
        assertNumberMixedWithText("millemiliardesimi e milleseicento novantaquattro",              F, F, n(1000.0 / 1000000000.0, F), " e ", n(1694, F));
        assertNumberMixedWithText("è millenovecento sessantaquattro novanta quattro trilionesimi", F, F, "è ", n(1964.0 / 94e18, F));
        assertNumberMixedWithText("ventitreesimo meno cinquantotto ventinovesimi",                 T, F, n(23, T), " ", n(-2, F));
        assertNumberMixedWithText("novantasei trentaseiesimi più centosedici",                     F, F, n(96.0 / 36.0, F), " ", n(116, F));
        assertNumberMixedWithText("novantanove virgola unounozeroquattrotre virgola zerouno",      T, F, n(99.11043, F), " virgola ", n(0, F), n(1, F));
        assertNumberMixedWithText("venticinque dozzine trequarti virgola ventidueciao",            T, F, n(300, F), " ", n(3.0 / 4.0, F), " virgola ventidueciao");
        assertNumberMixedWithText("mezze coppie",                                                  T, F, n(0.5, F), " ", n(2, F));
        assertNumberMixedWithText("Ho ventitre anni",                                              F, F, "Ho ", n(23, F), " anni");
    }

    @Test(timeout = 5000) // ~300000 number parses take <3s, use 6s timeout just for slower PCs
    public void testNumberMixedWithTextPerformance() {
        // make sure there are a lot of strings indeed (these numbers are just used to test that the
        // input data makes sense, if the input data changes feel free to also change these)
        assertEquals(73667, partsOfLongNumberMixedWithText);
        assertEquals(1267337, longNumberMixedWithText.length());

        for (int i = 0; i < (1 << 2); ++i) {
            final List<Object> objects = new ExtractNumberParams(numberParser(), longNumberMixedWithText)
                    .integerOnly(i%2 == 1).preferOrdinal((i/2)%2 == 1)
                    .parseMixedWithText();
            //System.out.println(objects.size() + " - " + partsOfLongNumberMixedWithText);
            // make sure the number of numbers that was actually parsed is roughly the same as those
            // in the input (the 0.8 is just a magic value, so feel free to decrease it if needed)
            assertTrue(objects.size() / ((double) partsOfLongNumberMixedWithText) > 0.8);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNumberPossibleIntervals() {
        assertNumberPossibleIntervals("non ha nessun numero", F, F);
        assertNumberPossibleIntervals(" & uno virgola due tre % ", T, F,
                range(3, 22, n(1.23), T), range(3, 18, n(1.2), F), range(3, 6, n(1), F),
                // range(7, 22, n(0.23), F), range(7, 18, n(0.2), F), <- these are only for English, in Italian we don't say "virgola due tre" to indicate 0.23
                range(15, 18, n(2), F),
                range(19, 24, n(3 / 100.0), T), range(19, 22, n(3), F),
                range(23, 24, n(1 / 100.0), F));

        // edge case combining fractions, "a", long/short scale, ordinal, onlyInteger
        // "a thousand trillionth" is not a valid number with short scale!
        assertNumberPossibleIntervals("mille trilionesimi", F, F,
                range(0, 18, n(1e-15), T), range(0, 5, n(1000), F),
                range(6, 18, n(1000000000000000000L, T), F));
        assertNumberPossibleIntervals("mille trilionesimi", T, F,
                range(0, 18, n(1e-15, F), T), range(0, 5, n(1000), F),
                range(6, 18, n(1000000000000000000L, T), F));
        assertNumberPossibleIntervals("mille trilionesimi", F, T,
                range(0, 5, n(1000), T),
                range(6, 18, n(1000000000000000000L, T), T));
    }

    @Test(timeout = 10000) // ~3500000 number parses take <5s, use 10s timeout just for slower PCs
    public void testNumberPossibleIntervalsPerformance() {
        for (int i = 0; i < (1 << 2); ++i) {
            final List<ParserParams.MatchedRange<Number>> objects = new ExtractNumberParams(numberParser(), longNumberMixedWithText)
                    .integerOnly(i%2 == 1).preferOrdinal((i/2)%2 == 1)
                    .parsePossibleIntervals();
            //System.out.println(objects.size() + " - " + partsOfLongNumberMixedWithText);
            // make sure the number of intervals is significantly larger than the number of numbers
            // originating from roughly the same as those
            // in the input (the 10 is just a magic value, so feel free to decrease it if needed)
            assertTrue(objects.size() / ((double) partsOfLongNumberMixedWithText) > 10);
        }
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

    @SuppressWarnings("unchecked")
    @Test
    public void testDurationPossibleIntervals() {
        assertDurationPossibleIntervals("un paio di minuti e ventisette secondi fa",
                range(0, 38, t(147), T), range(0, 17, t(120), F),
                range(3, 38, t(147), F), range(3, 17, t(120), F),
                range(11, 38, t(87), F), range(11, 17, t(60), F),
                range(20, 38, t(27), F), range(25, 38, t(7), F), range(31, 38, t(1), F));
        assertDurationPossibleIntervals("2ns e quattro ore mentre sei millisecondi.",
                range(0, 17, t(4 * HOUR, 2), T), range(0, 3, t(0, 2), F),
                range(6, 17, t(4 * HOUR), F), range(14, 17, t(HOUR), F),
                range(25, 41, t(0, 6 * MILLIS), T), range(29, 41, t(0, MILLIS), F));
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

    @SuppressWarnings("unchecked")
    @Test
    public void testDateTimePossibleIntervals() {
        assertDateTimePossibleIntervals("nel 1612, non dieci anni fa!", LocalDateTime.of(1, 2, 3, 4, 5, 6),
                range(4,  8,  LocalDateTime.of(1612, 1,  1, 4, 5, 6), T),
                range(14, 27, LocalDateTime.of(-9,   2,  3, 4, 5, 6), T),
                range(14, 19, LocalDateTime.of(1,    2, 10, 4, 5, 6), F), // this is not exactly the expected behavior but I guess it makes sense
                range(20, 27, LocalDateTime.of(0,    2,  3, 4, 5, 6), F));
        assertDateTimePossibleIntervals("ven 14 lug del 2017 alle 3:32:00 era dopo pranzo", LocalDateTime.of(2026, 2, 24, 6, 5, 4),
                range(0,  32, LocalDateTime.of(2017, 7, 14,  3, 32, 0), T),
                range(0,  29, LocalDateTime.of(2017, 7, 14,  3, 32, 0), F),
                range(0,  26, LocalDateTime.of(2017, 7, 14,  3,  0, 0), F),
                range(0,  19, LocalDateTime.of(2017, 7, 14,  6,  5, 4), F),
                range(0,  10, LocalDateTime.of(2026, 7, 14,  6,  5, 4), F),
                range(0,  6,  LocalDateTime.of(2026, 2, 14,  6,  5, 4), F),
                range(0,  3,  LocalDateTime.of(2026, 2, 27,  6,  5, 4), F),
                range(4,  32, LocalDateTime.of(2017, 7, 14,  3, 32, 0), F),
                range(4,  29, LocalDateTime.of(2017, 7, 14,  3, 32, 0), F),
                range(4,  26, LocalDateTime.of(2017, 7, 14,  3,  0, 0), F),
                range(4,  19, LocalDateTime.of(2017, 7, 14,  6,  5, 4), F),
                range(4,  10, LocalDateTime.of(2026, 7, 14,  6,  5, 4), F),
                range(4,  6,  LocalDateTime.of(2026, 2, 14,  6,  5, 4), F),
                range(7,  32, LocalDateTime.of(2017, 7,  1,  3, 32, 0), F),
                range(7,  29, LocalDateTime.of(2017, 7,  1,  3, 32, 0), F),
                range(7,  26, LocalDateTime.of(2017, 7,  1,  3,  0, 0), F),
                range(7,  19, LocalDateTime.of(2017, 7,  1,  6,  5, 4), F),
                range(7,  10, LocalDateTime.of(2026, 7,  1,  6,  5, 4), F),
                range(15, 32, LocalDateTime.of(2017, 1,  1,  3, 32, 0), F),
                range(15, 29, LocalDateTime.of(2017, 1,  1,  3, 32, 0), F),
                range(15, 26, LocalDateTime.of(2017, 1,  1,  3,  0, 0), F),
                range(15, 19, LocalDateTime.of(2017, 1,  1,  6,  5, 4), F),
                range(20, 32, LocalDateTime.of(2026, 2, 24,  3, 32, 0), F),
                range(20, 29, LocalDateTime.of(2026, 2, 24,  3, 32, 0), F),
                range(20, 26, LocalDateTime.of(2026, 2, 24,  3,  0, 0), F),
                range(25, 32, LocalDateTime.of(2026, 2, 24,  3, 32, 0), F),
                range(25, 29, LocalDateTime.of(2026, 2, 24,  3, 32, 0), F),
                // the next four are not exactly the expected behavior but I guess it makes sense
                range(25, 26, LocalDateTime.of(2026, 2,  3,  6,  5, 4), F),
                range(27, 32, LocalDateTime.of(32,   1,  1,  0,  0, 0), F),
                range(27, 29, LocalDateTime.of(32,   1,  1,  6,  5, 4), F),
                range(30, 32, LocalDateTime.of(0,    1,  1,  6,  5, 4), F),
                range(37, 48, LocalDateTime.of(2026, 2, 24, 13,  0, 0), T),
                range(42, 48, LocalDateTime.of(2026, 2, 24, 12,  0, 0), F));
    }
}
