package org.dicio.numbers.lang.it;

import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.WithTokenizerTestBase;
import org.dicio.numbers.unit.Number;
import org.junit.Test;

import java.util.function.Function;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;
import static org.dicio.numbers.test.TestUtils.n;
import static org.dicio.numbers.test.TestUtils.numberDeduceType;
import static org.junit.Assert.*;

public class ExtractNumbersTest extends WithTokenizerTestBase {

    @Override
    public String configFolder() {
        return "config/it-it";
    }


    private void assertNumberFunction(final String s,
                                      final Number value,
                                      final int finalTokenStreamPosition,
                                      final Function<ItalianNumberExtractor, Number> numberFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Number number = numberFunction.apply(new ItalianNumberExtractor(ts));
        assertEquals("wrong value for string " + s, value, number);
        assertEquals("wrong final token position for number " + value, finalTokenStreamPosition, ts.getPosition());
    }

    private void assertNumberFunctionNull(final String s,
                                          final Function<ItalianNumberExtractor, Number> numberFunction) {
        assertNumberFunction(s, null, 0, numberFunction);
    }

    private void assertNumberInteger(final String s, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, numberDeduceType(value).withOrdinal(isOrdinal), finalTokenStreamPosition,
                (enp) -> enp.numberInteger(allowOrdinal));
    }

    private void assertNumberIntegerNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberInteger(allowOrdinal));
    }

    private void assertNumberPoint(final String s, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, numberDeduceType(value).withOrdinal(isOrdinal),
                finalTokenStreamPosition, (enp) -> enp.numberPoint(allowOrdinal));
    }

    private void assertNumberPointNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberPoint(allowOrdinal));
    }

    private void assertNumberSignPoint(final String s, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, numberDeduceType(value).withOrdinal(isOrdinal),
                finalTokenStreamPosition, (enp) -> enp.numberSignPoint(allowOrdinal));
    }

    private void assertNumberSignPointNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberSignPoint(allowOrdinal));
    }

    private void assertDivideByDenominatorIfPossible(final String s, final Number startingNumber, final Number value, final int finalTokenStreamPosition) {
        assertNumberFunction(s, value, finalTokenStreamPosition,
                (enp) -> enp.divideByDenominatorIfPossible(startingNumber));
    }


    @Test
    public void testNumberInteger() {
        assertNumberInteger("cento sessanta quattro trilioni e un biliardo e cento bilioni", F, 164001100e12, F, 10);
        assertNumberInteger("venti 5 miliardi, 1 cento e sessanta quattro milioni, sette mila diciannove", T, 25164007019L, F, 14);
        assertNumberInteger("venti 5 miliardi, 1 cento e sessanta quattro milioni, sette miliardi", T, 25164000000L, F, 10);
        assertNumberInteger("due mila e cento novanta uno", F, 2191,           F, 6);
        assertNumberInteger("nove cento dieci",             T, 910,            F, 3);
        assertNumberInteger("due milioni",                  F, 2000000,        F, 2);
        assertNumberInteger("mille dieci",                  T, 1010,           F, 2);
        assertNumberInteger("1234567890123",                F, 1234567890123L, F, 1);
        assertNumberInteger("654 e",                        T, 654,            F, 1);
        assertNumberInteger("un cento quattro, ",           F, 104,            F, 3);
        assertNumberInteger("nove mila, tre milioni",       T, 9000,           F, 2);
        assertNumberInteger("una di notte",                 F, 1,              F, 1);
    }

    @Test
    public void testNumberIntegerOrdinal() {
        assertNumberInteger("cento sessanta quattro trilioni, un biliardo, cento bilionesimo", T, 164001100e12, T, 10);
        assertNumberInteger("cento sessanta quattro trilioni, uno biliardesimo, cento bilionesimo", T, 164001e15, T, 7);
        assertNumberInteger("cento sessanta quattro trilioni, uno biliardesimo, cento bilionesimo", F, 164e18, F, 6);
        assertNumberInteger("venti 5 miliardi, 1 cento sessanta nove milioni, sette mila diciannovesimo", T, 25169007019L,  T, 13);
        assertNumberInteger("73 miliardi, venti tre milionesimo, sette mila diciannove", T, 73023000000L,  T, 6);
        assertNumberInteger("cento 6 miliardi, venti uno milioni, uno miliardesimo", T, 106021000000L, F, 7);
        assertNumberInteger("cento e 6 miliardi, venti uno milioni e un millesimo", F, 106021000001L, F, 10);
        assertNumberInteger("diciannove centesimo",    T, 19,       F, 1);
        assertNumberInteger("diciannovesimo",          T, 19,       T, 1);
        assertNumberInteger("due mila unesimo",        T, 2001,     T, 3);
        assertNumberInteger("due mila unesimo",        F, 2000,     F, 2);
        assertNumberInteger("mille due cento 09esime", T, 1209,     T, 5);
        assertNumberInteger("mille nove cento 7esimo", F, 1900,     F, 3);
        assertNumberInteger("tredici sedicesimi",      F, 13,       F, 1);
        assertNumberInteger("sedici tredicesimi",      T, 16,       F, 1);
        assertNumberInteger("543789ª",                 T, 543789,   T, 2);
        assertNumberInteger("12°tempo",                T, 12,       T, 2);
        assertNumberInteger("75.483.543ª",             T, 75483543, T, 6);
        assertNumberIntegerNull("2938°",    F);
        assertNumberIntegerNull("102.321ª", F);
    }

    @Test
    public void testNumberIntegerThousandSeparator() {
        // independent of short/long scale and of ordinal mode
        assertNumberInteger("23.001",               F, 23001,      F, 3);
        assertNumberInteger("167.42",               T, 167,        F, 1);
        assertNumberInteger("1.234.023.054. ciao",  F, 1234023054, F, 7);
        assertNumberInteger("23.001. un 500",       T, 23001,      F, 3);
        assertNumberInteger("5.030.due",            F, 5030,       F, 3);
        assertNumberInteger("67.104.23",            T, 67104,      F, 3);
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
        final ParserFormatter npf = new ParserFormatter(new ItalianFormatter(), null);
        for (int i = 0; i < 1100000000;) {
            if (i < 2200) {
                ++i; // test all numbers from 0 to 2200
            } else if (i < 1000000) {
                i += 1207;
            } else {
                i += 299527;
            }

            // not ordinal
            String formatted = npf.pronounceNumber(i).places(0).get();
            int tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, T, i, F, tokensInFormatted);

            // ordinal
            formatted = npf.pronounceNumber(i).places(0).ordinal(T).get();
            tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, T, i, T, tokensInFormatted);
        }
    }

    @Test(timeout = 2000) // 20000 formats + parses take <1s, use 2s timeout just for slower PCs
    public void testNumberIntegerPerformance() {
        final ParserFormatter npf = new ParserFormatter(new ItalianFormatter(), null);
        final long startingValue = 54378960497L;
        for (long i = startingValue; i < startingValue + 10000; ++i) {
            // short scale not ordinal
            String formatted = npf.pronounceNumber(i).places(0).get();
            int tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, T, i, F, tokensInFormatted);

            // short scale ordinal
            formatted = npf.pronounceNumber(i).places(0).ordinal(true).get();
            tokensInFormatted = tokensInFormattedString(formatted);
            assertNumberInteger(formatted, T, i, T, tokensInFormatted);
        }
    }

    @Test
    public void testNumberIntegerNull() {
        assertNumberIntegerNull("",               T);
        assertNumberIntegerNull("ciao come stai", F);
        assertNumberIntegerNull(", e",            T);
        assertNumberIntegerNull("e due",          F);
        assertNumberIntegerNull(", 123485 e",     T);
        assertNumberIntegerNull("e 123",          F);
        assertNumberIntegerNull(" mille ",        T);
    }

    @Test
    public void testNumberPoint() {
        assertNumberPoint("mille cinque cento settanta quattro virgola nove uno due zero", T, 1574.912, F, 10);
        assertNumberPoint("venti tre virgola zero 1 zero 2 tre, cinque cento", T, 23.01023, F, 8);
        assertNumberPoint("cinque cento nove virgola otto quattro cinque", F, 509.845, F, 7);
        assertNumberPoint("venti tre mila virgola sedici", T, 23000, F, 3);
        assertNumberPoint("cento due, uno una",      T, 102.1,     F, 4);
        assertNumberPoint("due punto tre quattro",   T, 2.34,      F, 4);
        assertNumberPoint("3645,7183",               F, 3645.7183, F, 3);
        assertNumberPoint("venti cinque ,2",         T, 25.2,      F, 4);
        assertNumberPoint("ottanta virgola 6745",    F, 80.6745,   F, 3);
        assertNumberPoint("4 virgola 67 45",         T, 4.67,      F, 3);
        assertNumberPoint("4000 virgola 6 63",       F, 4000.6,    F, 3);
        assertNumberPoint("74567 virgola sei un",    T, 74567.6,   F, 3);
        assertNumberPoint("zero , 6 8 2 zero venti", F, 0.682,     F, 6);
        assertNumberPoint("74567 virgola sei",       T, 74567.6,   F, 3);
        assertNumberPoint("uno virgola venti",       T, 1,         F, 1);
    }

    @Test
    public void testNumberPointFraction() {
        assertNumberPoint("venti tre milioni, cento mila sessanta quattro fratto sedici", F, 1443754, F, 10);
        assertNumberPoint("sedici diviso venti tre milioni, cento mila sessanta quattro", T, 1.0 / 1443754.0, F, 10);
        assertNumberPoint("8 mila e, 192 diviso 4 mila 96 otto",  F, 2,                 F, 9);
        assertNumberPoint("nove mila otto cento / cento",         T, 98,                F, 6);
        assertNumberPoint("venti quattro fratto sessanta cinque", T, 24.0 / 65.0,       F, 5);
        assertNumberPoint("uno diviso cinque e mezzo",            F, 1.0 / 5.0,         F, 3);
        assertNumberPoint("venti sei diviso sette",               T, 26.0 / 7.0,        F, 4);
        assertNumberPoint("47328 fratto 12093",                   F, 47328.0 / 12093.0, F, 3);
        assertNumberPoint("cinque / sei nove due",                T, 5.0 / 6.0,         F, 3);
        assertNumberPoint("nove fratto, due",                     F, 9,                 F, 1);
    }

    @Test
    public void testNumberPointOrdinal() {
        assertNumberPoint("quinto virgola sei",               T, 5,     T, 1);
        assertNumberPoint("3 mila 7 cento decimo fratto sei", T, 3710,  T, 5);
        assertNumberPoint("3 mila 7 cento decimo fratto sei", F, 3700,  F, 4);
        assertNumberPoint("otto virgola uno secondo",         F, 8.1,   F, 3);
        assertNumberPoint("otto virgola uno terzo",           T, 8.1,   F, 3);
        assertNumberPoint("otto virgola un terzo",            T, 8,     F, 1);
        assertNumberPoint("sei diviso quinti",                T, 6,     F, 1);
        assertNumberPoint("nove / trenta novesimo",           T, 0.3,   F, 3);
        assertNumberPoint("nove / trenta novesimo",           F, 0.3,   F, 3);
        assertNumberPoint("tredici virgola 1 2 3 °",          T, 13.12, F, 4);
    }

    @Test
    public void testNumberPointNull() {
        assertNumberPointNull("",                     F);
        assertNumberPointNull("ciao mondo",           T);
        assertNumberPointNull("virgola",              F);
        assertNumberPointNull(", venti",              T);
        assertNumberPointNull(".9",                   T);
        assertNumberPointNull(",, 1 2 3 4",           F);
        assertNumberPointNull(", e sei quattro otto", T);
        assertNumberPointNull("fratto due",           F);
        assertNumberPointNull(" uno diviso cinque",   T);
        assertNumberPointNull("virgola 800",          F);
        assertNumberPointNull("punto 9",              F);
        assertNumberPointNull(",1",                   F);
    }

    @Test
    public void testNumberSignPoint() {
        assertNumberSignPoint("meno settanta sei mila tre cento cinquanta sei fratto 23", T, -76356.0 / 23.0, F, 10);
        assertNumberSignPoint("meno dodici",               F, -12,      F, 2);
        assertNumberSignPoint("più milioni",               T, 1000000,  F, 2);
        assertNumberSignPoint("piu mille",                 T, 1000,     F, 2);
        assertNumberSignPoint("-1843",                     F, -1843,    F, 2);
        assertNumberSignPoint("+573.976",                  T, 573976,   F, 4);
        assertNumberSignPoint("meno 42903,5",              F, -42903.5, F, 4);
    }

    @Test
    public void testNumberSignPointOrdinal() {
        assertNumberSignPoint("meno dodicesimo",       T, -12,      T, 2);
        assertNumberSignPoint("-un centesimo",         F, -1,       F, 2);
        assertNumberSignPoint("piu milionesimo dieci", T, 1000000,  T, 2);
        assertNumberSignPoint("-1843ª",                T, -1843,    T, 3);
        assertNumberSignPoint("+573.976°",             T, 573976,   T, 5);
        assertNumberSignPointNull("meno primo", F);
        assertNumberSignPointNull("-1843ª",     F);
    }

    @Test
    public void testNumberSignPointNull() {
        assertNumberSignPointNull("",                          F);
        assertNumberSignPointNull("ciao come stai",            T);
        assertNumberSignPointNull("meno meno cento sessanta",  F);
        assertNumberSignPointNull(" piu un milione",           T);
        assertNumberSignPointNull("+- 5",                      F);
        assertNumberSignPointNull("meno virgola zero quattro", T);
    }

    @Test
    public void testDivideByDenominatorIfPossible() {
        assertDivideByDenominatorIfPossible("quinti",      n(5, F),    n(1, F),   1);
        assertDivideByDenominatorIfPossible("dozzina due", n(3, F),    n(36, F),  1);
        assertDivideByDenominatorIfPossible("mezzo e",     n(19, F),   n(9.5, F), 1);
        assertDivideByDenominatorIfPossible("%",           n(50, F),   n(0.5, F), 1);
        assertDivideByDenominatorIfPossible("‰",           n(1000, F), n(1, F),   1);
        assertDivideByDenominatorIfPossible("quarto",      n(16, F),   n(4, F),   1);
        assertDivideByDenominatorIfPossible("quarto",      n(4.4, F),  n(4.4, F), 0);
        assertDivideByDenominatorIfPossible("persone",     n(98, F),   n(98, F),  0);
    }
}
