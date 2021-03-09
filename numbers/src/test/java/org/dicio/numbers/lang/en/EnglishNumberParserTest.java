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

    private static void assertNumberLessThan1000(final String s, final long value, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value), finalTokenStreamPosition, EnglishNumberParser::numberLessThan1000);
    }

    private static void assertNumberLessThan1000Null(final String s) {
        assertNumberFunctionNull(s, EnglishNumberParser::numberLessThan1000);
    }

    private static void assertNumberGroupShortScale(final String s, final long lastMultiplier, final long value, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value), finalTokenStreamPosition, enp -> enp.numberGroupShortScale(lastMultiplier));
    }

    private static void assertNumberGroupShortScaleNull(final String s, final long lastMultiplier) {
        assertNumberFunctionNull(s, enp -> enp.numberGroupShortScale(lastMultiplier));
    }

    private static void assertNumberShortScale(final String s, final long value, final int finalTokenStreamPosition) {
        assertNumberFunction(s, new Number(value), finalTokenStreamPosition,EnglishNumberParser::numberShortScale);
    }

    private static void assertNumberShortScaleNull(final String s) {
        assertNumberFunctionNull(s, EnglishNumberParser::numberShortScale);
    }

    private static void assertNumberPoint(final String s, final double value, final int finalTokenStreamPosition) {
        assertNumberFunction(s, ((long) value) == value ? new Number((long) value) : new Number(value),
                finalTokenStreamPosition, EnglishNumberParser::numberPoint);
    }

    private static void assertNumberPointNull(final String s) {
        assertNumberFunctionNull(s, EnglishNumberParser::numberPoint);
    }

    private static void assertNumberSignPoint(final String s, final double value, final int finalTokenStreamPosition) {
        assertNumberFunction(s, ((long) value) == value ? new Number((long) value) : new Number(value),
                finalTokenStreamPosition, EnglishNumberParser::numberSignPoint);
    }

    private static void assertNumberSignPointNull(final String s) {
        assertNumberFunctionNull(s, EnglishNumberParser::numberSignPoint);
    }


    @Test
    public void testNumberLessThan1000() {
        assertNumberLessThan1000("zero",                   0,   1);
        assertNumberLessThan1000("one",                    1,   1);
        assertNumberLessThan1000("five",                   5,   1);
        assertNumberLessThan1000("nineteen",               19,  1);
        assertNumberLessThan1000("hundred",                100, 1);
        assertNumberLessThan1000("one hundred",            100, 2);
        assertNumberLessThan1000("three hundred",          300, 2);
        assertNumberLessThan1000("twenty six",             26,  2);
        assertNumberLessThan1000("thirty-seven",           37,  3);
        assertNumberLessThan1000("seven hundred six",      706, 3);
        assertNumberLessThan1000("eight hundred eighteen", 818, 3);
    }

    @Test
    public void testNumberLessThan1000Digits() {
        assertNumberLessThan1000("0",                0,   1);
        assertNumberLessThan1000("1",                1,   1);
        assertNumberLessThan1000("6",                6,   1);
        assertNumberLessThan1000("15",               15,  1);
        assertNumberLessThan1000("100 nineteen",     100, 1);
        assertNumberLessThan1000("3 hundred 8",      308, 3);
        assertNumberLessThan1000("72",               72,  1);
        assertNumberLessThan1000("912",              912, 1);
        assertNumberLessThan1000("8 hundred and 18", 818, 4);
        assertNumberLessThan1000("7 hundred 3 9",    703, 3);
        assertNumberLessThan1000("hundred 4 7",      104, 2);
        assertNumberLessThan1000("19 hundred",       19,  1);
        assertNumberLessThan1000("sixty 7",          67,  2);
    }

    @Test
    public void testNumberLessThan1000EdgeCases() {
        assertNumberLessThan1000("four five",                          4,   1);
        assertNumberLessThan1000("a two and",                          2,   2);
        assertNumberLessThan1000("one thirteen",                       1,   1);
        assertNumberLessThan1000("sixteen eight",                      16,  1);
        assertNumberLessThan1000("eighteen hundred",                   18,  1);
        assertNumberLessThan1000("zero hundred",                       0,   1);
        assertNumberLessThan1000("sixty nought",                       60,  1);
        assertNumberLessThan1000("a hundred",                          100, 2);
        assertNumberLessThan1000("one, and a hundred",                 100, 5);
        assertNumberLessThan1000("seven hundred and six",              706, 4);
        assertNumberLessThan1000("one hundred and ninety one",         191, 5);
        assertNumberLessThan1000("eight and a hundred and fifteen",    815, 6);
        assertNumberLessThan1000("a a one a a hundred a a eleven a a", 111, 9);
    }

    @Test
    public void testNumberLessThan1000Null() {
        assertNumberLessThan1000Null("");
        assertNumberLessThan1000Null("hello");
        assertNumberLessThan1000Null("hello how are you");
        assertNumberLessThan1000Null("a hello two and");
        assertNumberLessThan1000Null("a car and a half,");
        assertNumberLessThan1000Null("a million");
        assertNumberLessThan1000Null(" twenty");
    }

    @Test
    public void testNumberGroupShortScale() {
        assertNumberGroupShortScale("one hundred and twenty million", 1000000000, 120000000, 5);
        assertNumberGroupShortScale("three thousand and six",         1000000000, 3000,      2);
        assertNumberGroupShortScale("a hundred thousand",             1000000,    100000,    3);
        assertNumberGroupShortScale("hundred 70 thousand",            1000000,    170000,    3);
        assertNumberGroupShortScale("572 million",                    1000000000, 572000000, 2);
        assertNumberGroupShortScale("3 million",                      1000000000, 3000000,   2);
        assertNumberGroupShortScale(", one hundred and ninety one",   1000,       191,       6);
    }

    @Test
    public void testNumberGroupShortScaleNull() {
        assertNumberGroupShortScaleNull("",                      1000000000);
        assertNumberGroupShortScaleNull("hello",                 1000000);
        assertNumberGroupShortScaleNull("hello how are you",     1000);
        assertNumberGroupShortScaleNull("129000",                1000000000);
        assertNumberGroupShortScaleNull("5000000",               1000000000);
        assertNumberGroupShortScaleNull("one hundred and six",   999);
        assertNumberGroupShortScaleNull("twelve",                0);
        assertNumberGroupShortScaleNull("seven billion",         1000);
        assertNumberGroupShortScaleNull("nine thousand and one", 1000);
        assertNumberGroupShortScaleNull("eight million people",  1000000);
        assertNumberGroupShortScaleNull(" ten ",  1000000);
    }

    @Test
    public void testNumberShortScale() {
        assertNumberShortScale("twenty 5 billion, 1 hundred and sixty four million, seven thousand and nineteen", 25164007019L, 15);
        assertNumberShortScale("two thousand, one hundred and ninety one", 2191, 8);
        assertNumberShortScale("nine hundred and ten",         910,            4);
        assertNumberShortScale("two million",                  2000000,        2);
        assertNumberShortScale("one thousand and ten",         1010,           4);
        assertNumberShortScale("1234567890123",                1234567890123L, 1);
        assertNumberShortScale("654 and",                      654,            1);
        assertNumberShortScale("a hundred four,",              104,            3);
        assertNumberShortScale("nine thousand, three million", 9000,           2);
    }

    @Test
    public void testNumberShortScaleThousandSeparator() {
        assertNumberShortScale("23,001",               23001,      3);
        assertNumberShortScale("a 167,42",             167,        2);
        assertNumberShortScale("1,234,023,054, hello", 1234023054, 7);
        assertNumberShortScale("23,001, a 500",        23001,      3);
        assertNumberShortScale("5,030,two",            5030,       3);
        assertNumberShortScale("67,104,23",            67104,      3);
    }

    @Test
    public void testNumberShortScaleYear() {
        assertNumberShortScale("two twenty-one",                 2,    1);
        assertNumberShortScale("nineteen 745",                   19,   1);
        assertNumberShortScale("ten 21",                         1021, 2);
        assertNumberShortScale("nineteen oh 6 and two",          1906, 3);
        assertNumberShortScale("twenty-nought-oh",               2000, 5);
        assertNumberShortScale("eleven zero 0",                  1100, 3);
        assertNumberShortScale("seventeen 0 0",                  1700, 3);
        assertNumberShortScale("sixty-four-hundred",             6400, 5);
        assertNumberShortScale("two hundred and twelve hundred", 212,  4);
        assertNumberShortScale("58 hundred",                     5800, 2);
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
            assertNumberShortScale(formatted, i, tokensInFormatted);
        }
    }

    @Test
    public void testNumberShortScaleNull() {
        assertNumberShortScaleNull("");
        assertNumberShortScaleNull("a hello how are you");
        assertNumberShortScaleNull(", and");
        assertNumberShortScaleNull("oh two");
        assertNumberShortScaleNull(", 123485 and");
        assertNumberShortScaleNull("and 123");
        assertNumberShortScaleNull(" one thousand ");
    }

    @Test
    public void testNumberPoint() {
        assertNumberPoint("one thousand, five hundred and seventy four point nine one two oh nought o zero", 1574.912, 16);
        assertNumberPoint("twenty three point nought 1 oh 2 three, five hundred", 23.01023, 8);
        assertNumberPoint("twenty three thousand point sixteen", 23000, 3);
        assertNumberPoint("fifteen-oh-nine point eight four five", 1509.845, 9);
        assertNumberPoint("3645.7183",                  3645.7183, 3);
        assertNumberPoint("twenty five.2",              25.2,      4);
        assertNumberPoint("eighty point 6745",          80.6745,   3);
        assertNumberPoint("4 point 67 45",              4.67,      3);
        assertNumberPoint("4000 point 6 63",            4000.6,    3);
        assertNumberPoint("74567 point six",            74567.6,   3);
        assertNumberPoint("nought . 6 8 2 zero twenty", 0.682,     6);
        assertNumberPoint("74567 point six",            74567.6,   3);
        assertNumberPoint("point 800",                  .8,        2);
        assertNumberPoint("one point twenty",           1,         1);
    }

    @Test
    public void testNumberPointFraction() {
        assertNumberPoint("twenty three million, one hundred thousand and sixty four over sixteen", 1443754, 12);
        assertNumberPoint("sixteen over twenty three million, one hundred thousand and sixty four", 1.0 / 1443754.0, 12);
        assertNumberPoint("8 thousand and, 192 divided by 4 thousand 96 eight", 2, 10);
        assertNumberPoint("ninety eight hundred / one hundred", 98, 6);
        assertNumberPoint("twenty four over sixty five", 24.0 / 65.0,       5);
        assertNumberPoint("one over five and a half",    1.0 / 5.0,         3);
        assertNumberPoint("twenty six divided by seven", 26.0 / 7.0,        5);
        assertNumberPoint("47328 over 12093",            47328.0 / 12093.0, 3);
        assertNumberPoint("five / six nine two",         5.0 / 6.0,         3);
        assertNumberPoint("nine over, two",              9,                 1);
        assertNumberPoint("eight divided five",          8.0 / 5.0,         3);
        assertNumberPoint("six by nineteen",             6,                 1);
    }

    @Test
    public void testNumberPointNull() {
        assertNumberPointNull("");
        assertNumberPointNull("hello world");
        assertNumberPointNull("point");
        assertNumberPointNull("point twenty");
        assertNumberPointNull("point, 1 2 3 4");
        assertNumberPointNull(". and six four eight");
        assertNumberPointNull("over two");
        assertNumberPointNull(" one divided by five");
    }

    @Test
    public void testNumberSignPoint() {
        assertNumberSignPoint("minus seventy six thousand, three hundred and fifty six over 23", -76356.0 / 23.0, 12);
        assertNumberSignPoint("minus twelve",        -12, 2);
        assertNumberSignPoint("plus million",        1000000, 2);
        assertNumberSignPoint("-1843",               -1843, 2);
        assertNumberSignPoint("+573976",             573976, 2);
        assertNumberSignPoint("minus 42903.5",       -42903.5, 4);
        assertNumberSignPoint("minus point oh four", -.04, 4);
    }

    @Test
    public void testNumberSignPointNull() {
        assertNumberSignPointNull("");
        assertNumberSignPointNull("hello how are you");
        assertNumberSignPointNull("minus minus 1 hundred and sixty");
        assertNumberSignPointNull(" plus million");
        assertNumberSignPointNull(" +- 5");
    }
}
