package org.dicio.numbers.lang.en;

import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.WithTokenizerTestBase;
import org.dicio.numbers.unit.Number;
import org.junit.Test;

import java.util.function.BiFunction;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;
import static org.dicio.numbers.test.TestUtils.n;
import static org.dicio.numbers.test.TestUtils.numberDeduceType;
import static org.junit.Assert.*;

public class ExtractNumbersTest extends WithTokenizerTestBase {

    @Override
    public String configFolder() {
        return "config/en-us";
    }


    private void assertNumberFunction(final String s,
                                      final boolean shortScale,
                                      final Number value,
                                      final int finalTokenStreamPosition,
                                      final BiFunction<EnglishNumberExtractor, TokenStream, Number> numberFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Number number = numberFunction.apply(new EnglishNumberExtractor(ts, shortScale), ts);
        assertEquals("wrong value for string " + s, value, number);
        assertEquals("wrong final token position for number " + value, finalTokenStreamPosition, ts.getPosition());
    }

    private void assertNumberFunctionNull(final String s,
                                          final boolean shortScale,
                                          final BiFunction<EnglishNumberExtractor, TokenStream, Number> numberFunction) {
        assertNumberFunction(s, shortScale, null, 0, numberFunction);
    }

    private void assertNumberGroupLongScale(final String s, final boolean allowOrdinal, final double lastMultiplier, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, true, numberDeduceType(value).withOrdinal(isOrdinal), finalTokenStreamPosition,
                (enp, ts) -> EnglishNumberExtractor.numberGroupLongScale(ts, allowOrdinal, lastMultiplier));
    }

    private void assertNumberGroupLongScaleNull(final String s, final boolean allowOrdinal, final double lastMultiplier) {
        assertNumberFunctionNull(s, true, (enp, ts) -> EnglishNumberExtractor.numberGroupLongScale(ts, allowOrdinal, lastMultiplier));
    }

    private void assertNumberInteger(final String s, final boolean shortScale, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, shortScale, numberDeduceType(value).withOrdinal(isOrdinal), finalTokenStreamPosition,
                (enp, ts) -> enp.numberInteger(allowOrdinal));
    }

    private void assertNumberIntegerNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, true, (enp, ts) -> enp.numberInteger(allowOrdinal));
        assertNumberFunctionNull(s, false, (enp, ts) -> enp.numberInteger(allowOrdinal));
    }

    private void assertNumberPoint(final String s, final boolean shortScale, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, shortScale, numberDeduceType(value).withOrdinal(isOrdinal),
                finalTokenStreamPosition, (enp, ts) -> enp.numberPoint(allowOrdinal));
    }

    private void assertNumberPointNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, true, (enp, ts) -> enp.numberPoint(allowOrdinal));
        assertNumberFunctionNull(s, false, (enp, ts) -> enp.numberPoint(allowOrdinal));
    }

    private void assertNumberSignPoint(final String s, final boolean shortScale, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, shortScale, numberDeduceType(value).withOrdinal(isOrdinal),
                finalTokenStreamPosition, (enp, ts) -> enp.numberSignPoint(allowOrdinal));
    }

    private void assertNumberSignPointNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, true, (enp, ts) -> enp.numberSignPoint(allowOrdinal));
        assertNumberFunctionNull(s, false, (enp, ts) -> enp.numberSignPoint(allowOrdinal));
    }

    private void assertDivideByDenominatorIfPossible(final String s, final Number startingNumber, final Number value, final int finalTokenStreamPosition) {
        assertNumberFunction(s, true, value, finalTokenStreamPosition,
                (enp, ts) -> enp.divideByDenominatorIfPossible(startingNumber));
    }


    @Test
    public void testNumberGroupLongScale() {
        assertNumberGroupLongScale("one hundred and twenty million", F, 1e9,  120e6,     F, 5);
        assertNumberGroupLongScale("sixty three quadrillion",        F, 1e28, 63e24,     F, 3);
        assertNumberGroupLongScale("three thousand and six",         T, 1e9,  3006,      F, 4);
        assertNumberGroupLongScale("a hundred thousand",             F, 1e6,  100000,    F, 3);
        assertNumberGroupLongScale("hundred 70 thousand",            T, 1e6,  170000,    F, 3);
        assertNumberGroupLongScale("572 million",                    F, 1e9,  572e6,     F, 2);
        assertNumberGroupLongScale("572012 billion",                 F, 1e18, 572012e12, F, 2);
        assertNumberGroupLongScale("3 million",                      T, 1e9,  3e6,       F, 2);
        assertNumberGroupLongScale(", one hundred and ninety one",   F, 1e6,  191,       F, 6);
    }

    @Test
    public void testNumberGroupLongScaleOrdinal() {
        assertNumberGroupLongScale("seven hundred and sixty four millionth", T, 1e9, 764e6, T, 6);
        assertNumberGroupLongScale("seven hundred and sixty four millionth", F, 1e9, 764,   F, 5);
        assertNumberGroupLongScale("seven hundred and sixty four millionth", F, 1e6, 764,   F, 5);
        assertNumberGroupLongScale("fifth billionth",                        T, 1e9, 5,     T, 1);
        assertNumberGroupLongScale("nineteen hundredth",                     T, 1e9, 19,    F, 1);
        assertNumberGroupLongScaleNull("seven hundred and sixty four millionth", T, 1000);
        assertNumberGroupLongScaleNull("twelfth thousandth",                     F, 1e9);
    }

    @Test
    public void testNumberGroupLongScaleNull() {
        assertNumberGroupLongScaleNull("",                      T, 1e9);
        assertNumberGroupLongScaleNull("hello",                 F, 1e6);
        assertNumberGroupLongScaleNull("hello how are you",     T, 1e6);
        assertNumberGroupLongScaleNull("5000000",               T, 1e9);
        assertNumberGroupLongScaleNull("one hundred and six",   F, 999);
        assertNumberGroupLongScaleNull("twelve",                T, 0);
        assertNumberGroupLongScaleNull("seven billion",         F, 1e6);
        assertNumberGroupLongScaleNull("nine thousand and one", T, 1000);
        assertNumberGroupLongScaleNull("eight million people",  F, 1e6);
        assertNumberGroupLongScaleNull(" ten ",                 T, 1e6);
    }

    @Test
    public void testNumberInteger() {
        assertNumberInteger("one hundred and four thousand, six hundred quadrillion, sixty four thousand and one trillion, one hundred thousand billion", F, F, 104600064001100000e12, F, 21);
        assertNumberInteger("twenty 5 billion, 1 hundred and sixty four million, seven thousand and nineteen", T, T, 25164007019L, F, 15);
        assertNumberInteger("twenty 5 billion, 1 hundred and sixty four million, seven billion", T, T, 25164000000L, F, 10);
        assertNumberInteger("two thousand, one hundred and ninety one", T, F, 2191, F, 8);
        assertNumberInteger("nine hundred and ten",         F, T, 910,            F, 4);
        assertNumberInteger("two million",                  F, F, 2000000,        F, 2);
        assertNumberInteger("one thousand and ten",         T, T, 1010,           F, 4);
        assertNumberInteger("1234567890123",                T, F, 1234567890123L, F, 1);
        assertNumberInteger("654 and",                      F, T, 654,            F, 1);
        assertNumberInteger("a hundred four,",              F, F, 104,            F, 3);
        assertNumberInteger("nine thousand, three million", T, T, 9000,           F, 2);
    }

    @Test
    public void testNumberIntegerOrdinal() {
        assertNumberInteger("one hundred and four thousand, six hundred quadrillion, sixty four thousand and one trillion, one hundred thousand billionth", F, T, 104600064001100000e12, T, 21);
        assertNumberInteger("one hundred and four thousand, six hundred quadrillion, sixty four thousand and one trillionth, one hundred thousand billion", F, T, 104600064001e18, T, 16);
        assertNumberInteger("one hundred and four thousand, six hundred quadrillion, sixty four thousand and one trillionth, one hundred thousand billion", F, F, 104600e24, F, 15);
        assertNumberInteger("twenty 5 billion, 1 hundred and sixty four million, seven thousand and nineteenth", T, T, 25164007019L,  T, 15);
        assertNumberInteger("73 billion, twenty three millionth, seven thousand and nineteen",                   T, T, 73023000000L,  T, 6);
        assertNumberInteger("one hundred and 6 billion, twenty one million, one billionth",                      T, T, 106021000000L, F, 9);
        assertNumberInteger("one hundred and 6 billion, twenty one million, one thousandth",                     T, F, 106021000001L, F, 11);
        assertNumberInteger("nineteen hundredth",    T, T, 1900,     T, 2);
        assertNumberInteger("twenty oh first",       F, T, 2001,     T, 3);
        assertNumberInteger("twenty oh first",       F, F, 20,       F, 1);
        assertNumberInteger("nineteen 09th",         T, T, 1909,     T, 3);
        assertNumberInteger("nineteen 09th",         T, F, 19,       F, 1);
        assertNumberInteger("eleven sixteenth",      F, T, 1116,     T, 2);
        assertNumberInteger("eleven sixteenth",      F, F, 11,       F, 1);
        assertNumberInteger("eighteen twenty first", T, T, 1821,     T, 3);
        assertNumberInteger("eighteen twenty first", T, F, 1820,     F, 2);
        assertNumberInteger("thirteen sixtieth",     F, T, 1360,     T, 2);
        assertNumberInteger("thirteen sixtieth",     F, F, 13,       F, 1);
        assertNumberInteger("sixteenth hundred",     T, T, 16,       T, 1);
        assertNumberInteger("sixteenth oh four",     T, T, 16,       T, 1);
        assertNumberInteger("543789th",              F, T, 543789,   T, 2);
        assertNumberInteger("75,483,543 rd",         F, T, 75483543, T, 6);
        assertNumberIntegerNull("2938th",               F);
        assertNumberIntegerNull("102,321th",            F);
        assertNumberIntegerNull("thirteenth hundredth", F);
    }

    @Test
    public void testNumberIntegerThousandSeparator() {
        // independent of short/long scale and of ordinal mode
        assertNumberInteger("23,001",               T, F, 23001,      F, 3);
        assertNumberInteger("19,123",               T, T, 19123,      F, 3);
        assertNumberInteger("a 167,42",             F, T, 167,        F, 2);
        assertNumberInteger("1,234,023,054, hello", F, F, 1234023054, F, 7);
        assertNumberInteger("23,001, a 500",        T, T, 23001,      F, 3);
        assertNumberInteger("5,030,two",            F, F, 5030,       F, 3);
        assertNumberInteger("67,104,23",            F, T, 67104,      F, 3);
    }

    @Test
    public void testNumberIntegerYear() {
        // independent of short/long scale and of ordinal mode
        assertNumberInteger("two twenty-one",                 T, T, 2,    F, 1);
        assertNumberInteger("nineteen 745",                   F, F, 19,   F, 1);
        assertNumberInteger("nineteen 25",                    F, F, 1925, F, 2);
        assertNumberInteger("19 twenty five",                 F, F, 19,   F, 1);
        assertNumberInteger("19 25",                          F, F, 19,   F, 1);
        assertNumberInteger("nineteenth twenty five",         F, T, 19,   T, 1);
        assertNumberInteger("ten 21",                         F, T, 1021, F, 2);
        assertNumberInteger("nineteen oh 6 and two",          T, F, 1906, F, 3);
        assertNumberInteger("twenty-nought-oh",               T, T, 2000, F, 5);
        assertNumberInteger("eleven zero 0",                  F, F, 1100, F, 3);
        assertNumberInteger("seventeen 0 0",                  F, T, 1700, F, 3);
        assertNumberInteger("sixty-four-hundred",             T, F, 6400, F, 5);
        assertNumberInteger("two hundred and twelve hundred", T, T, 212,  F, 4);
        assertNumberInteger("58 hundred",                     F, F, 5800, F, 2);
        assertNumberInteger("nineteen hundred",               F, T, 1900, F, 2);
        assertNumberInteger("eighteen 1",                     T, F, 18,   F, 1);
    }

    private int tokensInFormattedString(final String formatted) {
        int tokensInFormatted = 1;
        for (int j = 0; j < formatted.length(); ++j) {
            if (formatted.charAt(j) == ' ' || formatted.charAt(j) == ',') {
                ++tokensInFormatted;
            }
        }
        return tokensInFormatted;
    }

    @Test
    public void testNumberIntegerWithFormatter() {
        final ParserFormatter npf = new ParserFormatter(new EnglishFormatter(), null);
        for (int i = 0; i < 1100000000;) {
            if (i < 2200) {
                ++i; // test all numbers from 0 to 200 (also tests years!)
            } else if (i < 1000000) {
                i += 1207;
            } else {
                i += 299527;
            }

            // not ordinal
            String formatted = npf.pronounceNumber(i).places(0).get();
            int tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, T, T, i, F, tokensInFormatted);

            // ordinal
            formatted = npf.pronounceNumber(i).places(0).ordinal(T).get();
            tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, T, T, i, T, tokensInFormatted);

            // long scale not ordinal
            formatted = npf.pronounceNumber(i).places(0).shortScale(false).get();
            tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, F, T, i, F, tokensInFormatted);

            // long scale ordinal
            formatted = npf.pronounceNumber(i).places(0).shortScale(false).ordinal(true).get();
            tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, F, T, i, T, tokensInFormatted);
        }
    }

    @Test(timeout = 4000) // 40000 formats + parses take <2s, use 4s timeout just for slower PCs
    public void testNumberIntegerPerformanceWithFormatter() {
        final ParserFormatter npf = new ParserFormatter(new EnglishFormatter(), null);
        final long startingValue = 54378960497L;
        for (long i = startingValue; i < startingValue + 10000; ++i) {
            // short scale not ordinal
            String formatted = npf.pronounceNumber(i).places(0).get();
            int tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, T, T, i, F, tokensInFormatted);

            // short scale ordinal
            formatted = npf.pronounceNumber(i).places(0).ordinal(true).get();
            tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, T, T, i, T, tokensInFormatted);

            // long scale not ordinal
            formatted = npf.pronounceNumber(i).places(0).shortScale(false).get();
            tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, F, T, i, F, tokensInFormatted);

            // long scale ordinal
            formatted = npf.pronounceNumber(i).places(0).shortScale(false).ordinal(true).get();
            tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, F, T, i, T, tokensInFormatted);
        }
    }

    @Test
    public void testNumberIntegerNull() {
        assertNumberIntegerNull("",                    T);
        assertNumberIntegerNull("a hello how are you", F);
        assertNumberIntegerNull(", and",               T);
        assertNumberIntegerNull("oh two",              F);
        assertNumberIntegerNull(", 123485 and",        T);
        assertNumberIntegerNull("and 123",             F);
        assertNumberIntegerNull(" one thousand ",      T);
    }

    @Test
    public void testNumberPoint() {
        assertNumberPoint("one thousand, five hundred and seventy four point nine one two oh nought o zero", T, T, 1574.912, F, 16);
        assertNumberPoint("twenty three point nought 1 oh 2 three, five hundred", F, T, 23.01023, F, 8);
        assertNumberPoint("fifteen-oh-nine point eight four five", F, F, 1509.845, F, 9);
        assertNumberPoint("twenty three thousand point sixteen", T, T, 23000, F, 3);
        assertNumberPoint("3645.7183",                  T, F, 3645.7183, F, 3);
        assertNumberPoint("twenty five.2",              F, T, 25.2,      F, 4);
        assertNumberPoint("eighty point 6745",          F, F, 80.6745,   F, 3);
        assertNumberPoint("4 point 67 45",              T, T, 4.67,      F, 3);
        assertNumberPoint("4000 point 6 63",            T, F, 4000.6,    F, 3);
        assertNumberPoint("74567 point six",            F, T, 74567.6,   F, 3);
        assertNumberPoint("nought . 6 8 2 zero twenty", F, F, 0.682,     F, 6);
        assertNumberPoint("74567 point six",            T, T, 74567.6,   F, 3);
        assertNumberPoint("point 800",                  T, F, .8,        F, 2);
        assertNumberPoint("one point twenty",           F, T, 1,         F, 1);
    }

    @Test
    public void testNumberPointFraction() {
        assertNumberPoint("twenty three million, one hundred thousand and sixty four over sixteen", F, F, 1443754, F, 12);
        assertNumberPoint("sixteen over twenty three million, one hundred thousand and sixty four", T, T, 1.0 / 1443754.0, F, 12);
        assertNumberPoint("8 thousand and, 192 divided by 4 thousand 96 eight", T, F, 2, F, 10);
        assertNumberPoint("ninety eight hundred / one hundred", F, T, 98, F, 6);
        assertNumberPoint("twenty four over sixty five", F, T, 24.0 / 65.0,       F, 5);
        assertNumberPoint("one over five and a half",    T, F, 1.0 / 5.0,         F, 3);
        assertNumberPoint("twenty six divided by seven", T, T, 26.0 / 7.0,        F, 5);
        assertNumberPoint("47328 over 12093",            F, F, 47328.0 / 12093.0, F, 3);
        assertNumberPoint("five / six nine two",         F, T, 5.0 / 6.0,         F, 3);
        assertNumberPoint("nine over, two",              T, F, 9,                 F, 1);
        assertNumberPoint("eight divided five",          T, T, 8.0 / 5.0,         F, 3);
        assertNumberPoint("six by nineteen",             F, F, 6,                 F, 1);
    }

    @Test
    public void testNumberPointOrdinal() {
        assertNumberPoint("fifth point six",                     T, T, 5,     T, 1);
        assertNumberPoint("3 thousand 7 hundred tenth over six", F, T, 3710,  T, 5);
        assertNumberPoint("3 thousand 7 hundred tenth over six", T, F, 3700,  F, 4);
        assertNumberPoint("eight point one second",              F, F, 8.1,   F, 3);
        assertNumberPoint("eight point one third",               T, T, 8.1,   F, 3);
        assertNumberPoint("six over fifth",                      F, T, 6,     F, 1);
        assertNumberPoint("nine over thirty ninth",              T, T, 0.3,   F, 3);
        assertNumberPoint("nine over thirty ninth",              F, F, 0.3,   F, 3);
        assertNumberPoint("thirteen point 1 2 3 th",             T, T, 13.12, F, 4);
    }

    @Test
    public void testNumberPointNull() {
        assertNumberPointNull("",                     F);
        assertNumberPointNull("hello world",          T);
        assertNumberPointNull("point",                F);
        assertNumberPointNull("point twenty",         T);
        assertNumberPointNull("point, 1 2 3 4",       F);
        assertNumberPointNull(". and six four eight", T);
        assertNumberPointNull("over two",             F);
        assertNumberPointNull(" one divided by five", T);
    }

    @Test
    public void testNumberSignPoint() {
        assertNumberSignPoint("minus seventy six thousand, three hundred and fifty six over 23", T, T, -76356.0 / 23.0, F, 12);
        assertNumberSignPoint("minus twelve",        T, F, -12,      F, 2);
        assertNumberSignPoint("plus million",        F, T, 1000000,  F, 2);
        assertNumberSignPoint("-1843",               F, F, -1843,    F, 2);
        assertNumberSignPoint("+573,976",            T, T, 573976,   F, 4);
        assertNumberSignPoint("minus 42903.5",       T, F, -42903.5, F, 4);
        assertNumberSignPoint("minus point oh four", F, T, -.04,     F, 4);
    }

    @Test
    public void testNumberSignPointOrdinal() {
        assertNumberSignPoint("minus twelfth",      T, T, -12,      T, 2);
        assertNumberSignPoint("-one hundredth",     F, F, -1,       F, 2);
        assertNumberSignPoint("plus millionth ten", F, T, 1000000,  T, 2);
        assertNumberSignPoint("-1843th",            T, T, -1843,    T, 3);
        assertNumberSignPoint("+573,976rd",         T, T, 573976,   T, 5);
        assertNumberSignPointNull("minus first", F);
        assertNumberSignPointNull("-1843th",     F);
    }

    @Test
    public void testNumberSignPointNull() {
        assertNumberSignPointNull("",                                F);
        assertNumberSignPointNull("hello how are you",               T);
        assertNumberSignPointNull("minus minus 1 hundred and sixty", F);
        assertNumberSignPointNull(" plus million",                   T);
        assertNumberSignPointNull(" +- 5",                           F);
    }

    @Test
    public void testDivideByDenominatorIfPossible() {
        assertDivideByDenominatorIfPossible("fifths",    n(5, F),    n(1, F),   1);
        assertDivideByDenominatorIfPossible("dozen two", n(3, F),    n(36, F),  1);
        assertDivideByDenominatorIfPossible("halves a",  n(19, F),   n(9.5, F), 1);
        assertDivideByDenominatorIfPossible("%",         n(50, F),   n(0.5, F), 1);
        assertDivideByDenominatorIfPossible("‰",         n(1000, F), n(1, F),   1);
        assertDivideByDenominatorIfPossible("quarter",   n(16, F),   n(4, F),   1);
        assertDivideByDenominatorIfPossible("quarter",   n(4.4, F),  n(4.4, F), 0);
        assertDivideByDenominatorIfPossible("people",    n(98, F),   n(98, F),  0);

        // "a" could be the numerator of fractions but not really a number, so handled here
        assertDivideByDenominatorIfPossible("a tenth",   null,       n(0.1, F), 2);
        assertDivideByDenominatorIfPossible("a ten",     null,       null,      0);
        assertDivideByDenominatorIfPossible("a people",  null,       null,      0);
        assertDivideByDenominatorIfPossible("a tenth",   n(2.8, F),  n(2.8, F), 0);
    }
}
