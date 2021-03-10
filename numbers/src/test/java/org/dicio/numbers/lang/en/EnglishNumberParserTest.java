package org.dicio.numbers.lang.en;

import org.dicio.numbers.NumberParserFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.parser.lexer.Tokenizer;
import org.dicio.numbers.util.Number;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnglishNumberParserTest {

    private static Tokenizer tokenizer;

    @BeforeClass
    public static void setup() {
        tokenizer = new Tokenizer("config/en-us");
    }

    private interface NumberFunction {
        Number call(final EnglishNumberParser enp);
    }

    private static void assertNumberFunction(final String s,
                                             final Number value,
                                             final int finalTokenStreamPosition,
                                             final NumberFunction numberFunction) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Number number = numberFunction.call(new EnglishNumberParser(ts));
        assertEquals("wrong value for string " + s, value, number);
        assertEquals("wrong final token position for number " + value, finalTokenStreamPosition, ts.getPosition());
    }

    private static void assertNumberFunctionNull(final String s,
                                                 final NumberFunction numberFunction) {
        assertNumberFunction(s, null, 0, numberFunction);
    }

    private static void assertNumberLessThan1000(final String s, final boolean preferOrdinal, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).setOrdinal(isOrdinal), finalTokenStreamPosition,
                (enp) -> enp.numberLessThan1000(preferOrdinal));
    }

    private static void assertNumberLessThan1000Null(final String s, final boolean preferOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberLessThan1000(preferOrdinal));
    }

    private static void assertNumberGroupShortScale(final String s, final boolean preferOrdinal, final long lastMultiplier, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).setOrdinal(isOrdinal), finalTokenStreamPosition,
                enp -> enp.numberGroupShortScale(preferOrdinal, lastMultiplier));
    }

    private static void assertNumberGroupShortScaleNull(final String s, final boolean preferOrdinal, final long lastMultiplier) {
        assertNumberFunctionNull(s, enp -> enp.numberGroupShortScale(preferOrdinal, lastMultiplier));
    }

    private static void assertNumberShortScale(final String s, final boolean preferOrdinal, final long value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value).setOrdinal(isOrdinal), finalTokenStreamPosition,
                (enp) -> enp.numberShortScale(preferOrdinal));
    }

    private static void assertNumberShortScaleNull(final String s, final boolean preferOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberShortScale(preferOrdinal));
    }

    private static void assertNumberPoint(final String s, final boolean preferOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, (((long) value) == value ? new Number((long) value) : new Number(value)).setOrdinal(isOrdinal),
                finalTokenStreamPosition, (enp) -> enp.numberPoint(preferOrdinal));
    }

    private static void assertNumberPointNull(final String s, final boolean preferOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberPoint(preferOrdinal));
    }

    private static void assertNumberSignPoint(final String s, final boolean preferOrdinal, final double value, final boolean isOrdinal, final int finalTokenStreamPosition) {
        assertNumberFunction(s, (((long) value) == value ? new Number((long) value) : new Number(value)).setOrdinal(isOrdinal),
                finalTokenStreamPosition, (enp) -> enp.numberSignPoint(preferOrdinal));
    }

    private static void assertNumberSignPointNull(final String s, final boolean preferOrdinal) {
        assertNumberFunctionNull(s, (enp) -> enp.numberSignPoint(preferOrdinal));
    }


    @Test
    public void testNumberLessThan1000() {
        assertNumberLessThan1000("zero",                   false, 0,   false, 1);
        assertNumberLessThan1000("one",                    false, 1,   false, 1);
        assertNumberLessThan1000("five",                   false, 5,   false, 1);
        assertNumberLessThan1000("nineteen",               false, 19,  false, 1);
        assertNumberLessThan1000("hundred",                false, 100, false, 1);
        assertNumberLessThan1000("one hundred",            false, 100, false, 2);
        assertNumberLessThan1000("three hundred",          false, 300, false, 2);
        assertNumberLessThan1000("twenty six",             false, 26,  false, 2);
        assertNumberLessThan1000("thirty-seven",           false, 37,  false, 3);
        assertNumberLessThan1000("seven hundred six",      false, 706, false, 3);
        assertNumberLessThan1000("eight hundred eighteen", false, 818, false, 3);
    }

    @Test
    public void testNumberLessThan1000Digits() {
        assertNumberLessThan1000("0",                false, 0,   false, 1);
        assertNumberLessThan1000("1",                false, 1,   false, 1);
        assertNumberLessThan1000("6",                false, 6,   false, 1);
        assertNumberLessThan1000("15",               false, 15,  false, 1);
        assertNumberLessThan1000("100 nineteen",     false, 100, false, 1);
        assertNumberLessThan1000("3 hundred 8",      false, 308, false, 3);
        assertNumberLessThan1000("72",               false, 72,  false, 1);
        assertNumberLessThan1000("912",              false, 912, false, 1);
        assertNumberLessThan1000("8 hundred and 18", false, 818, false, 4);
        assertNumberLessThan1000("7 hundred 3 9",    false, 703, false, 3);
        assertNumberLessThan1000("hundred 4 7",      false, 104, false, 2);
        assertNumberLessThan1000("19 hundred",       false, 19,  false, 1);
        assertNumberLessThan1000("sixty 7",          false, 67,  false, 2);
    }

    @Test
    public void testNumberLessThan1000EdgeCases() {
        assertNumberLessThan1000("four five",                          false, 4,   false, 1);
        assertNumberLessThan1000("a two and",                          false, 2,   false, 2);
        assertNumberLessThan1000("one thirteen",                       false, 1,   false, 1);
        assertNumberLessThan1000("sixteen eight",                      false, 16,  false, 1);
        assertNumberLessThan1000("eighteen hundred",                   false, 18,  false, 1);
        assertNumberLessThan1000("zero hundred",                       false, 0,   false, 1);
        assertNumberLessThan1000("sixty nought",                       false, 60,  false, 1);
        assertNumberLessThan1000("a hundred",                          false, 100, false, 2);
        assertNumberLessThan1000("one, and a hundred",                 false, 100, false, 5);
        assertNumberLessThan1000("seven hundred and six",              false, 706, false, 4);
        assertNumberLessThan1000("one hundred and ninety one",         false, 191, false, 5);
        assertNumberLessThan1000("eight and a hundred and fifteen",    false, 815, false, 6);
        assertNumberLessThan1000("a a one a a hundred a a eleven a a", false, 111, false, 9);
    }

    @Test
    public void testNumberLessThan1000Null() {
        assertNumberLessThan1000Null("", false);
        assertNumberLessThan1000Null("hello", false);
        assertNumberLessThan1000Null("hello how are you", false);
        assertNumberLessThan1000Null("a hello two and", false);
        assertNumberLessThan1000Null("a car and a half,", false);
        assertNumberLessThan1000Null("a million", false);
        assertNumberLessThan1000Null(" twenty", false);
    }

    @Test
    public void testNumberGroupShortScale() {
        assertNumberGroupShortScale("one hundred and twenty million", false, 1000000000, 120000000, false, 5);
        assertNumberGroupShortScale("three thousand and six",         false, 1000000000, 3000,      false, 2);
        assertNumberGroupShortScale("a hundred thousand",             false, 1000000,    100000,    false, 3);
        assertNumberGroupShortScale("hundred 70 thousand",            false, 1000000,    170000,    false, 3);
        assertNumberGroupShortScale("572 million",                    false, 1000000000, 572000000, false, 2);
        assertNumberGroupShortScale("3 million",                      false, 1000000000, 3000000,   false, 2);
        assertNumberGroupShortScale(", one hundred and ninety one",   false, 1000,       191,       false, 6);
    }

    @Test
    public void testNumberGroupShortScaleNull() {
        assertNumberGroupShortScaleNull("",                      false, 1000000000);
        assertNumberGroupShortScaleNull("hello",                 false, 1000000);
        assertNumberGroupShortScaleNull("hello how are you",     false, 1000);
        assertNumberGroupShortScaleNull("129000",                false, 1000000000);
        assertNumberGroupShortScaleNull("5000000",               false, 1000000000);
        assertNumberGroupShortScaleNull("one hundred and six",   false, 999);
        assertNumberGroupShortScaleNull("twelve",                false, 0);
        assertNumberGroupShortScaleNull("seven billion",         false, 1000);
        assertNumberGroupShortScaleNull("nine thousand and one", false, 1000);
        assertNumberGroupShortScaleNull("eight million people",  false, 1000000);
        assertNumberGroupShortScaleNull(" ten ",                 false, 1000000);
    }

    @Test
    public void testNumberShortScale() {
        assertNumberShortScale("twenty 5 billion, 1 hundred and sixty four million, seven thousand and nineteen", false, 25164007019L, false, 15);
        assertNumberShortScale("two thousand, one hundred and ninety one", false, 2191, false, 8);
        assertNumberShortScale("nine hundred and ten",         false, 910,            false, 4);
        assertNumberShortScale("two million",                  false, 2000000,        false, 2);
        assertNumberShortScale("one thousand and ten",         false, 1010,           false, 4);
        assertNumberShortScale("1234567890123",                false, 1234567890123L, false, 1);
        assertNumberShortScale("654 and",                      false, 654,            false, 1);
        assertNumberShortScale("a hundred four,",              false, 104,            false, 3);
        assertNumberShortScale("nine thousand, three million", false, 9000,           false, 2);
    }

    @Test
    public void testNumberShortScaleThousandSeparator() {
        assertNumberShortScale("23,001",               false, 23001,      false, 3);
        assertNumberShortScale("a 167,42",             false, 167,        false, 2);
        assertNumberShortScale("1,234,023,054, hello", false, 1234023054, false, 7);
        assertNumberShortScale("23,001, a 500",        false, 23001,      false, 3);
        assertNumberShortScale("5,030,two",            false, 5030,       false, 3);
        assertNumberShortScale("67,104,23",            false, 67104,      false, 3);
    }

    @Test
    public void testNumberShortScaleYear() {
        assertNumberShortScale("two twenty-one",                 false, 2,    false, 1);
        assertNumberShortScale("nineteen 745",                   false, 19,   false, 1);
        assertNumberShortScale("ten 21",                         false, 1021, false, 2);
        assertNumberShortScale("nineteen oh 6 and two",          false, 1906, false, 3);
        assertNumberShortScale("twenty-nought-oh",               false, 2000, false, 5);
        assertNumberShortScale("eleven zero 0",                  false, 1100, false, 3);
        assertNumberShortScale("seventeen 0 0",                  false, 1700, false, 3);
        assertNumberShortScale("sixty-four-hundred",             false, 6400, false, 5);
        assertNumberShortScale("two hundred and twelve hundred", false, 212,  false, 4);
        assertNumberShortScale("58 hundred",                     false, 5800, false, 2);
    }

    @Test
    public void testNumberShortScaleWithFormatter() {
        final NumberParserFormatter npf = new NumberParserFormatter(new EnglishFormatter(), null);
        for (int i = 0; i < 1100000000;) {
            if (i < 2200) {
                ++i; // test all numbers from 0 to 200 (also tests years!)
            } else if (i < 1000000) {
                i += 1207;
            } else {
                i += 299527;
            }

            final String formatted = npf.pronounceNumber(i).places(0).get();
            int tokensInFormatted = 1;
            for (int j = 0; j < formatted.length(); ++j) {
                if (formatted.charAt(j) == ' ' || formatted.charAt(j) == ',') {
                    ++tokensInFormatted;
                }
            }
            assertNumberShortScale(formatted, false, i, false, tokensInFormatted);
        }
    }

    @Test
    public void testNumberShortScaleNull() {
        assertNumberShortScaleNull("", false);
        assertNumberShortScaleNull("a hello how are you", false);
        assertNumberShortScaleNull(", and", false);
        assertNumberShortScaleNull("oh two", false);
        assertNumberShortScaleNull(", 123485 and", false);
        assertNumberShortScaleNull("and 123", false);
        assertNumberShortScaleNull(" one thousand ", false);
    }

    @Test
    public void testNumberPoint() {
        assertNumberPoint("one thousand, five hundred and seventy four point nine one two oh nought o zero", false, 1574.912, false, 16);
        assertNumberPoint("twenty three point nought 1 oh 2 three, five hundred", false, 23.01023, false, 8);
        assertNumberPoint("twenty three thousand point sixteen", false, 23000, false, 3);
        assertNumberPoint("fifteen-oh-nine point eight four five", false, 1509.845, false, 9);
        assertNumberPoint("3645.7183",                  false, 3645.7183, false, 3);
        assertNumberPoint("twenty five.2",              false, 25.2,      false, 4);
        assertNumberPoint("eighty point 6745",          false, 80.6745,   false, 3);
        assertNumberPoint("4 point 67 45",              false, 4.67,      false, 3);
        assertNumberPoint("4000 point 6 63",            false, 4000.6,    false, 3);
        assertNumberPoint("74567 point six",            false, 74567.6,   false, 3);
        assertNumberPoint("nought . 6 8 2 zero twenty", false, 0.682,     false, 6);
        assertNumberPoint("74567 point six",            false, 74567.6,   false, 3);
        assertNumberPoint("point 800",                  false, .8,        false, 2);
        assertNumberPoint("one point twenty",           false, 1,         false, 1);
    }

    @Test
    public void testNumberPointFraction() {
        assertNumberPoint("twenty three million, one hundred thousand and sixty four over sixteen", false, 1443754, false, 12);
        assertNumberPoint("sixteen over twenty three million, one hundred thousand and sixty four", false, 1.0 / 1443754.0, false, 12);
        assertNumberPoint("8 thousand and, 192 divided by 4 thousand 96 eight", false, 2, false, 10);
        assertNumberPoint("ninety eight hundred / one hundred", false, 98, false, 6);
        assertNumberPoint("twenty four over sixty five", false, 24.0 / 65.0,       false, 5);
        assertNumberPoint("one over five and a half",    false, 1.0 / 5.0,         false, 3);
        assertNumberPoint("twenty six divided by seven", false, 26.0 / 7.0,        false, 5);
        assertNumberPoint("47328 over 12093",            false, 47328.0 / 12093.0, false, 3);
        assertNumberPoint("five / six nine two",         false, 5.0 / 6.0,         false, 3);
        assertNumberPoint("nine over, two",              false, 9,                 false, 1);
        assertNumberPoint("eight divided five",          false, 8.0 / 5.0,         false, 3);
        assertNumberPoint("six by nineteen",             false, 6,                 false, 1);
    }

    @Test
    public void testNumberPointNull() {
        assertNumberPointNull("", false);
        assertNumberPointNull("hello world", false);
        assertNumberPointNull("point", false);
        assertNumberPointNull("point twenty", false);
        assertNumberPointNull("point, 1 2 3 4", false);
        assertNumberPointNull(". and six four eight", false);
        assertNumberPointNull("over two", false);
        assertNumberPointNull(" one divided by five", false);
    }

    @Test
    public void testNumberSignPoint() {
        assertNumberSignPoint("minus seventy six thousand, three hundred and fifty six over 23", false, -76356.0 / 23.0, false, 12);
        assertNumberSignPoint("minus twelve",        false, -12,      false, 2);
        assertNumberSignPoint("plus million",        false, 1000000,  false, 2);
        assertNumberSignPoint("-1843",               false, -1843,    false, 2);
        assertNumberSignPoint("+573976",             false, 573976,   false, 2);
        assertNumberSignPoint("minus 42903.5",       false, -42903.5, false, 4);
        assertNumberSignPoint("minus point oh four", false, -.04,     false, 4);
    }

    @Test
    public void testNumberSignPointNull() {
        assertNumberSignPointNull("", false);
        assertNumberSignPointNull("hello how are you", false);
        assertNumberSignPointNull("minus minus 1 hundred and sixty", false);
        assertNumberSignPointNull(" plus million", false);
        assertNumberSignPointNull(" +- 5", false);
    }
}
