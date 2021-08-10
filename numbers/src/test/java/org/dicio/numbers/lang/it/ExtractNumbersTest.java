package org.dicio.numbers.lang.it;

import org.dicio.numbers.NumberParserFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.ExtractNumbersTestBase;
import org.dicio.numbers.util.Number;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ExtractNumbersTest extends ExtractNumbersTestBase {

    @Override
    public String configFolder() {
        return "config/it-it";
    }

    private interface NumberFunction {
        Number call(final ItalianNumberExtractor enp);
    }


    private void assertNumberFunction(final String s,
                                      final Number value,
                                      final int finalTokenStreamPosition,
                                      final NumberFunction numberFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Number number = numberFunction.call(new ItalianNumberExtractor(ts, false));
        assertEquals("wrong value for string " + s, value, number);
        assertEquals("wrong final token position for number " + value, finalTokenStreamPosition, ts.getPosition());
    }

    private void assertNumberFunctionNull(final String s,
                                          final NumberFunction numberFunction) {
        assertNumberFunction(s, null, 0, numberFunction);
    }

    private void assertNumberLessThan1000(final String s, final boolean allowOrdinal, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).setOrdinal(isOrdinal), finalTokenStreamPosition,
                (enp) -> enp.numberLessThan1000(allowOrdinal));
    }

    private void assertNumberLessThan1000Null(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberLessThan1000(allowOrdinal));
    }

    private void assertNumberGroup(final String s, final boolean allowOrdinal, final long lastMultiplier, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).setOrdinal(isOrdinal), finalTokenStreamPosition,
                enp -> enp.numberGroup(allowOrdinal, lastMultiplier));
    }

    private void assertNumberGroupNull(final String s, final boolean allowOrdinal, final long lastMultiplier) {
        assertNumberFunctionNull(s, enp -> enp.numberGroup(allowOrdinal, lastMultiplier));
    }

    private void assertNumberInteger(final String s, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, numberDeduceType(value).setOrdinal(isOrdinal), finalTokenStreamPosition,
                (enp) -> enp.numberInteger(allowOrdinal));
    }

    private void assertNumberIntegerNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberInteger(allowOrdinal));
    }

    private void assertNumberPoint(final String s, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, numberDeduceType(value).setOrdinal(isOrdinal),
                finalTokenStreamPosition, (enp) -> enp.numberPoint(allowOrdinal));
    }

    private void assertNumberPointNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberPoint(allowOrdinal));
    }

    private void assertNumberSignPoint(final String s, final boolean allowOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, numberDeduceType(value).setOrdinal(isOrdinal),
                finalTokenStreamPosition, (enp) -> enp.numberSignPoint(allowOrdinal));
    }

    private void assertNumberSignPointNull(final String s, final boolean allowOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberSignPoint(allowOrdinal));
    }

    private void assertDivideByDenominatorIfPossible(final String s, final Number startingNumber, final Number value, final int finalTokenStreamPosition) {
        assertNumberFunction(s, value, finalTokenStreamPosition,
                (enp) -> enp.divideByDenominatorIfPossible(startingNumber));
    }

    private void assertExtractNumbers(final String s, final boolean preferOrdinal, final Object... results) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final List<Object> objects = new ItalianNumberExtractor(ts, preferOrdinal)
                .extractNumbers();
        assertArrayEquals("Invalid result array: " + objects.toString(), results, objects.toArray());
        assertTrue(ts.finished());
    }


    @Test
    public void testNumberLessThan1000() {
        assertNumberLessThan1000("zero",                T, 0,   F, 1);
        assertNumberLessThan1000("uno",                 F, 1,   F, 1);
        assertNumberLessThan1000("un",                  F, 1,   F, 1);
        assertNumberLessThan1000("cinque",              T, 5,   F, 1);
        assertNumberLessThan1000("diciannove",          F, 19,  F, 1);
        assertNumberLessThan1000("cento",               T, 100, F, 1);
        assertNumberLessThan1000("tre cento",           T, 300, F, 2);
        assertNumberLessThan1000("venti sei",           F, 26,  F, 2);
        assertNumberLessThan1000("trenta sette",        T, 37,  F, 2);
        assertNumberLessThan1000("sette cento sei",     F, 706, F, 3);
        assertNumberLessThan1000("otto cento diciotto", T, 818, F, 3);
    }

    @Test
    public void testNumberLessThan1000Digits() {
        assertNumberLessThan1000("0",              F, 0,   F, 1);
        assertNumberLessThan1000("1",              T, 1,   F, 1);
        assertNumberLessThan1000("6",              F, 6,   F, 1);
        assertNumberLessThan1000("15",             T, 15,  F, 1);
        assertNumberLessThan1000("100 diciannove", F, 100, F, 1);
        assertNumberLessThan1000("3 cento 8",      T, 308, F, 3);
        assertNumberLessThan1000("72",             F, 72,  F, 1);
        assertNumberLessThan1000("912",            T, 912, F, 1);
        assertNumberLessThan1000("8 cento e 18",   F, 818, F, 4);
        assertNumberLessThan1000("7 cento 3 9",    T, 703, F, 3);
        assertNumberLessThan1000("cento 4 7",      F, 104, F, 2);
        assertNumberLessThan1000("19 cento",       T, 19,  F, 1);
        assertNumberLessThan1000("sessanta 7",     F, 67,  F, 2);
        assertNumberLessThan1000("30 6",           T, 30,  F, 1);
    }

    @Test
    public void testNumberLessThan1000EdgeCases() {
        assertNumberLessThan1000("quattro cinque",           T, 4,   F, 1);
        assertNumberLessThan1000("un due e",                 F, 1,   F, 1);
        assertNumberLessThan1000("uno tredici",              T, 1,   F, 1);
        assertNumberLessThan1000("sedici otto",              F, 16,  F, 1);
        assertNumberLessThan1000("diciotto cento",           T, 18,  F, 1);
        assertNumberLessThan1000("zero cento",               F, 0,   F, 1);
        assertNumberLessThan1000("sessanta zero",            T, 60,  F, 1);
        assertNumberLessThan1000("un cento",                 F, 100, F, 2);
        assertNumberLessThan1000("uno e cento",              T, 100, F, 3);
        assertNumberLessThan1000("sette cento e sei",        F, 706, F, 4);
        assertNumberLessThan1000("cento novanta uno",        T, 191, F, 3);
        assertNumberLessThan1000("otto e cento e quindici",  F, 815, F, 5);
        assertNumberLessThan1000("e e cento e e undici e e", T, 111, F, 6);
    }

    @Test
    public void testNumberLessThan1000Ordinal() {
        assertNumberLessThan1000("quinto",                T, 5,   T, 1);
        assertNumberLessThan1000("venti seiesimo",        T, 26,  T, 2);
        assertNumberLessThan1000("settanta ottavo",       F, 70,  F, 1);
        assertNumberLessThan1000("cinquantesimo nono",    T, 50,  T, 1);
        assertNumberLessThan1000("cento tredicesimo",     T, 113, T, 2);
        assertNumberLessThan1000("primo cento",           T, 1,   T, 1);
        assertNumberLessThan1000("sette centesimo dieci", T, 700, T, 2);
        assertNumberLessThan1000("nove centesimo",        F, 9,   F, 1);
        assertNumberLessThan1000("620ª",                  T, 620, T, 2);
        assertNumberLessThan1000("987esime",              T, 987, T, 2);
        assertNumberLessThan1000("23°",                   T, 23,  T, 2);
        assertNumberLessThan1000("8 primo",               T, 8,   F, 1);
        assertNumberLessThan1000("1ª cento",              T, 1,   T, 2);
        assertNumberLessThan1000Null("settantesima", F);
        assertNumberLessThan1000Null("101°",         F);
    }

    @Test
    public void testNumberLessThan1000Null() {
        assertNumberLessThan1000Null("",               F);
        assertNumberLessThan1000Null("ciao",           T);
        assertNumberLessThan1000Null("ciao come stai", F);
        assertNumberLessThan1000Null("ciao due e",     T);
        assertNumberLessThan1000Null("milione",        T);
        assertNumberLessThan1000Null(" venti",         F);
    }

    @Test
    public void testNumberGroup() {
        assertNumberGroup("cento venti milioni",   F, 1000000000, 120000000, F, 3);
        assertNumberGroup("mille e sei",           T, 1000000000, 1000,      F, 1);
        assertNumberGroup("sei cento mila",        F, 1000000,    600000,    F, 3);
        assertNumberGroup("cento 70 migliaia",     T, 1000000,    170000,    F, 3);
        assertNumberGroup("572 milioni",           F, 1000000000, 572000000, F, 2);
        assertNumberGroup("1 milione",             T, 1000000000, 1000000,   F, 2);
        assertNumberGroup(", cento e novanta uno", F, 1000,       191,       F, 5);
    }

    @Test
    public void testNumberGroupOrdinal() {
        assertNumberGroup("sette cento sessanta quattro milionesimo", T, 1000000000, 764000000, T, 5);
        assertNumberGroup("sette cento sessanta quattro milionesimo", F, 1000000000, 764,       F, 4);
        assertNumberGroup("sette cento sessanta quattro milionesimo", F, 1000,       764,       F, 4);
        assertNumberGroup("quinto miliardesimo",                      T, 1000000000, 5,         T, 1);
        assertNumberGroup("diciannove centesimo",                     T, 1000000000, 19,        F, 1);
        assertNumberGroupNull("sette cento sessanta quattro milionesimo", T, 1000);
        assertNumberGroupNull("dodicesimo millesimo",                     F, 1000000000);
    }

    @Test
    public void testNumberGroupNull() {
        assertNumberGroupNull("",                        T, 1000000000);
        assertNumberGroupNull("ciao",                    F, 1000000);
        assertNumberGroupNull("ciao come stai",          T, 1000);
        assertNumberGroupNull("129000",                  F, 1000000000);
        assertNumberGroupNull("5000000",                 T, 1000000000);
        assertNumberGroupNull("cento sei",               F, 999);
        assertNumberGroupNull("dodici",                  T, 0);
        assertNumberGroupNull("sette miliardi",          F, 1000);
        assertNumberGroupNull("nove mila uno",           T, 1000);
        assertNumberGroupNull("otto milioni di persone", F, 1000000);
        assertNumberGroupNull(" dieci ",                 T, 1000000);
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
        assertNumberInteger("12°tempo",                T, 12,   T, 2);
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
        final NumberParserFormatter npf = new NumberParserFormatter(new ItalianFormatter(), null);
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
        final NumberParserFormatter npf = new NumberParserFormatter(new ItalianFormatter(), null);
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

    @Test
    public void testExtractNumbers() {
        assertExtractNumbers("un miliardesimo e mille sei cento novanta quattro",  F, n(1.0 / 1000000000.0, F), " e ", n(1694, F));
        assertExtractNumbers("è mille nove cento sessanta quattro trilionesimi", F, "è ", n(1964e-18, F));
        assertExtractNumbers(" hello  ciao!, 3/5 o quattro settimi?", F, " hello  ciao!, ", n(3.0 / 5.0, F), " o ", n(4.0 / 7.0, F), "?");
        assertExtractNumbers(" hello  ciao!, quattro settimi o 3/5?", T, " hello  ciao!, ", n(4.0 / 7.0, F), " o ", n(3.0 / 5.0, F), "?");
        assertExtractNumbers("tre miliardesimo piu due",              T, n(3000000000L, T), " ", n(2, F));
        assertExtractNumbers("due miliardesimi meno cinquanta otto",  T, n(2000000000L, T), " ", n(-58, F));
        assertExtractNumbers("nove miliardesimi per undici",          F, n(9.0 / 1000000000.0, F), " per ", n(11, F));
        assertExtractNumbers("tre mezzi, non undici quarti",          T, n(3.0 / 2.0, F), ", non ", n(11.0 / 4.0, F));
        assertExtractNumbers("sei paia equivale a una dozzina ",      T, n(12, F), " equivale a ", n(12, F), " ");
        assertExtractNumbers("6 trilionesimi di una torta",           T, n(6000000000000000000L, T), " di ", n(1, F), " torta");
    }

    @Test
    public void testNumberParserExtractNumbers() {
        final NumberParserFormatter npf
                = new NumberParserFormatter(null, new ItalianParser()); // mille non più mille // l'italia ha vinto sei a zero
        assertArrayEquals(new Object[] {"Ho ", new Number(23), " anni."}, npf.extractNumbers("Ho venti tre anni.").get().toArray());
        assertArrayEquals(new Object[] {"Il ", new Number(1000000000000000000L).setOrdinal(true)}, npf.extractNumbers("Il trilionesimo").get().toArray());
        assertArrayEquals(new Object[] {new Number(1e-18)}, npf.extractNumbers("Un trilionesimo").get().toArray());
        assertArrayEquals(new Object[] {new Number(1000000000L)}, npf.extractNumbers("Un miliardo").preferOrdinal(true).get().toArray());
        assertArrayEquals(new Object[] {new Number(1000), " non ", new Number(1000)}, npf.extractNumbers("Mille non più mille").get().toArray());
        assertArrayEquals(new Object[] {"Vince ", new Number(6), " a ", new Number(0), " "}, npf.extractNumbers("Vince sei a zero ").get().toArray());
    }
}
