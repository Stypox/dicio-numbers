package org.dicio.numbers.lang.en;

import org.dicio.numbers.ParserFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;
import static org.junit.Assert.assertEquals;

public class PronounceNumberTest {

    private static ParserFormatter pf;

    @BeforeClass
    public static void setup() {
        pf = new ParserFormatter(new EnglishFormatter(), null);
    }

    @Test
    public void smallIntegers() {
        assertEquals("zero", pf.pronounceNumber(0).get());
        assertEquals("one", pf.pronounceNumber(1).get());
        assertEquals("ten", pf.pronounceNumber(10).get());
        assertEquals("fifteen", pf.pronounceNumber(15).get());
        assertEquals("twenty", pf.pronounceNumber(20).get());
        assertEquals("twenty seven", pf.pronounceNumber(27).get());
        assertEquals("thirty", pf.pronounceNumber(30).get());
        assertEquals("thirty three", pf.pronounceNumber(33).get());
    }

    @Test
    public void negativeSmallIntegers() {
        assertEquals("minus one", pf.pronounceNumber(-1).get());
        assertEquals("minus ten", pf.pronounceNumber(-10).get());
        assertEquals("minus fifteen", pf.pronounceNumber(-15).get());
        assertEquals("minus twenty", pf.pronounceNumber(-20).get());
        assertEquals("minus twenty seven", pf.pronounceNumber(-27).get());
        assertEquals("minus thirty", pf.pronounceNumber(-30).get());
        assertEquals("minus thirty three", pf.pronounceNumber(-33).get());
    }

    @Test
    public void decimals() {
        assertEquals("zero point zero five", pf.pronounceNumber(0.05).get());
        assertEquals("minus zero point zero five", pf.pronounceNumber(-0.05).get());
        assertEquals("one point two three", pf.pronounceNumber(1.234).get());
        assertEquals("twenty one point two six four", pf.pronounceNumber(21.264).places(5).get());
        assertEquals("twenty one point two six four", pf.pronounceNumber(21.264).places(4).get());
        assertEquals("twenty one point two six four", pf.pronounceNumber(21.264).places(3).get());
        assertEquals("twenty one point two six", pf.pronounceNumber(21.264).places(2).get());
        assertEquals("twenty one point three", pf.pronounceNumber(21.264).places(1).get());
        assertEquals("twenty one", pf.pronounceNumber(21.264).places(0).get());
        assertEquals("minus twenty one point two six four", pf.pronounceNumber(-21.264).places(5).get());
        assertEquals("minus twenty one point two six four", pf.pronounceNumber(-21.264).places(4).get());
        assertEquals("minus twenty one point two six four", pf.pronounceNumber(-21.264).places(3).get());
        assertEquals("minus twenty one point two six", pf.pronounceNumber(-21.264).places(2).get());
        assertEquals("minus twenty one point three", pf.pronounceNumber(-21.264).places(1).get());
        assertEquals("minus twenty one", pf.pronounceNumber(-21.264).places(0).get());
    }

    @Test
    public void roundingDecimals() {
        assertEquals("zero", pf.pronounceNumber(0.05).places(0).get());
        assertEquals("zero", pf.pronounceNumber(-0.4).places(0).get());
        assertEquals("minus twenty two", pf.pronounceNumber(-21.7).places(0).get());
        assertEquals("eighty nine", pf.pronounceNumber(89.2).places(0).get());
        assertEquals("ninety", pf.pronounceNumber(89.9).places(0).get());
        assertEquals("minus one", pf.pronounceNumber(-0.5).places(0).get());
        assertEquals("zero", pf.pronounceNumber(-0.4).places(0).get());
        assertEquals("six point three", pf.pronounceNumber(6.28).places(1).get());
        assertEquals("minus three point one", pf.pronounceNumber(-3.14).places(1).get());
        // note: 3.15 does not yield "three point two" because of floating point errors
        assertEquals("three point two", pf.pronounceNumber(3.150001).places(1).get());
        assertEquals("zero point three", pf.pronounceNumber(0.25).places(1).get());
        assertEquals("minus zero point three", pf.pronounceNumber(-0.25).places(1).get());
        assertEquals("nineteen", pf.pronounceNumber(19.004).get());
    }

    @Test
    public void hundred() {
        assertEquals("one hundred", pf.pronounceNumber(100).get());
        assertEquals("six hundred and seventy eight", pf.pronounceNumber(678).get());

        assertEquals("one hundred and three million, two hundred and fifty four thousand, six hundred and fifty four",
                pf.pronounceNumber(103254654).get());
        assertEquals("one million, five hundred and twelve thousand, four hundred and fifty seven",
                pf.pronounceNumber(1512457).get());
        assertEquals("two hundred and nine thousand, nine hundred and ninety six",
                pf.pronounceNumber(209996).get());
    }

    @Test
    public void year() {
        assertEquals("fourteen fifty six", pf.pronounceNumber(1456).get());
        assertEquals("nineteen eighty four", pf.pronounceNumber(1984).get());
        assertEquals("eighteen oh one", pf.pronounceNumber(1801).get());
        assertEquals("eleven hundred", pf.pronounceNumber(1100).get());
        assertEquals("twelve oh one", pf.pronounceNumber(1201).get());
        assertEquals("fifteen ten", pf.pronounceNumber(1510).get());
        assertEquals("ten oh six", pf.pronounceNumber(1006).get());
        assertEquals("one thousand", pf.pronounceNumber(1000).get());
        assertEquals("two thousand", pf.pronounceNumber(2000).get());
        assertEquals("two thousand, fifteen", pf.pronounceNumber(2015).get());
        assertEquals("four thousand, eight hundred and twenty seven", pf.pronounceNumber(4827).get());
    }

    @Test
    public void scientificNotation() {
        assertEquals("zero", pf.pronounceNumber(0.0).scientific(T).get());
        assertEquals("three point three times ten to the power of one",
                pf.pronounceNumber(33).scientific(T).get());
        assertEquals("two point nine nine times ten to the power of eight",
                pf.pronounceNumber(299492458).scientific(T).get());
        assertEquals("two point nine nine seven nine two five times ten to the power of eight",
                pf.pronounceNumber(299792458).scientific(T).places(6).get());
        assertEquals("one point six seven two times ten to the power of negative twenty seven",
                pf.pronounceNumber(1.672e-27).scientific(T).places(3).get());

        // auto scientific notation when number is too big to be pronounced
        assertEquals("two point nine five times ten to the power of twenty four",
                pf.pronounceNumber(2.9489e24).get());
    }

    private void assertShortLongScale(final double number,
                                      final String shortScale,
                                      final String longScale) {
        assertEquals(shortScale, pf.pronounceNumber(number).shortScale(T).get());
        assertEquals(longScale, pf.pronounceNumber(number).shortScale(F).get());
    }

    @Test
    public void largeNumbers() {
        assertShortLongScale(1001892,
                "one million, one thousand, eight hundred and ninety two",
                "one million, one thousand, eight hundred and ninety two");
        assertShortLongScale(299792458,
                "two hundred and ninety nine million, seven hundred and ninety two thousand, four hundred and fifty eight",
                "two hundred and ninety nine million, seven hundred and ninety two thousand, four hundred and fifty eight");
        assertShortLongScale(-100202133440.0,
                "minus one hundred billion, two hundred and two million, one hundred and thirty three thousand, four hundred and forty",
                "minus one hundred thousand two hundred and two million, one hundred and thirty three thousand, four hundred and forty");
        assertShortLongScale(20102000987000.0,
                "twenty trillion, one hundred and two billion, nine hundred and eighty seven thousand",
                "twenty billion, one hundred and two thousand million, nine hundred and eighty seven thousand");
        assertShortLongScale(-2061000560007060.0,
                "minus two quadrillion, sixty one trillion, five hundred and sixty million, seven thousand, sixty",
                "minus two thousand sixty one billion, five hundred and sixty million, seven thousand, sixty");
        assertShortLongScale(9111202032999999488.0, // floating point errors
                "nine quintillion, one hundred and eleven quadrillion, two hundred and two trillion, thirty two billion, nine hundred and ninety nine million, nine hundred and ninety nine thousand, four hundred and eighty eight",
                "nine trillion, one hundred and eleven thousand two hundred and two billion, thirty two thousand nine hundred and ninety nine million, nine hundred and ninety nine thousand, four hundred and eighty eight");

        assertShortLongScale(29000.0, "twenty nine thousand", "twenty nine thousand");
        assertShortLongScale(301000.0, "three hundred and one thousand", "three hundred and one thousand");
        assertShortLongScale(4000000.0, "four million", "four million");
        assertShortLongScale(50000000.0, "fifty million", "fifty million");
        assertShortLongScale(630000000.0, "six hundred and thirty million", "six hundred and thirty million");
        assertShortLongScale(7000000000.0, "seven billion", "seven thousand million");
        assertShortLongScale(16000000000.0, "sixteen billion", "sixteen thousand million");
        assertShortLongScale(923000000000.0, "nine hundred and twenty three billion", "nine hundred and twenty three thousand million");
        assertShortLongScale(1000000000000.0, "one trillion", "one billion");
        assertShortLongScale(29000000000000.0, "twenty nine trillion", "twenty nine billion");
        assertShortLongScale(308000000000000.0, "three hundred and eight trillion", "three hundred and eight billion");
        assertShortLongScale(4000000000000000.0, "four quadrillion", "four thousand billion");
        assertShortLongScale(52000000000000000.0, "fifty two quadrillion", "fifty two thousand billion");
        assertShortLongScale(640000000000000000.0, "six hundred and forty quadrillion", "six hundred and forty thousand billion");
        assertShortLongScale(7000000000000000000.0, "seven quintillion", "seven trillion");

        // TODO maybe improve this
        assertShortLongScale(1000001, "one million, one", "one million, one");
        assertShortLongScale(-2000000029, "minus two billion, twenty nine", "minus two thousand million, twenty nine");
    }

    @Test
    public void ordinal() {
        // small numbers
        assertEquals("first", pf.pronounceNumber(1).shortScale(T).ordinal(T).get());
        assertEquals("first", pf.pronounceNumber(1).shortScale(F).ordinal(T).get());
        assertEquals("tenth", pf.pronounceNumber(10).shortScale(T).ordinal(T).get());
        assertEquals("tenth", pf.pronounceNumber(10).shortScale(F).ordinal(T).get());
        assertEquals("fifteenth", pf.pronounceNumber(15).shortScale(T).ordinal(T).get());
        assertEquals("fifteenth", pf.pronounceNumber(15).shortScale(F).ordinal(T).get());
        assertEquals("twentieth", pf.pronounceNumber(20).shortScale(T).ordinal(T).get());
        assertEquals("twentieth", pf.pronounceNumber(20).shortScale(F).ordinal(T).get());
        assertEquals("twenty seventh", pf.pronounceNumber(27).shortScale(T).ordinal(T).get());
        assertEquals("twenty seventh", pf.pronounceNumber(27).shortScale(F).ordinal(T).get());
        assertEquals("thirtieth", pf.pronounceNumber(30).shortScale(T).ordinal(T).get());
        assertEquals("thirtieth", pf.pronounceNumber(30).shortScale(F).ordinal(T).get());
        assertEquals("thirty third", pf.pronounceNumber(33).shortScale(T).ordinal(T).get());
        assertEquals("thirty third", pf.pronounceNumber(33).shortScale(F).ordinal(T).get());
        assertEquals("hundredth", pf.pronounceNumber(100).shortScale(T).ordinal(T).get());
        assertEquals("hundredth", pf.pronounceNumber(100).shortScale(F).ordinal(T).get());
        assertEquals("thousandth", pf.pronounceNumber(1000).shortScale(T).ordinal(T).get());
        assertEquals("thousandth", pf.pronounceNumber(1000).shortScale(F).ordinal(T).get());
        assertEquals("ten thousandth", pf.pronounceNumber(10000).shortScale(T).ordinal(T).get());
        assertEquals("ten thousandth", pf.pronounceNumber(10000).shortScale(F).ordinal(T).get());
        assertEquals("two hundredth", pf.pronounceNumber(200).shortScale(T).ordinal(T).get());
        assertEquals("two hundredth", pf.pronounceNumber(200).shortScale(F).ordinal(T).get());
        assertEquals("eighteen thousand, six hundred and ninety first", pf.pronounceNumber(18691).ordinal(T).shortScale(T).get());
        assertEquals("eighteen thousand, six hundred and ninety first", pf.pronounceNumber(18691).ordinal(T).shortScale(F).get());
        assertEquals("one thousand, five hundred and sixty seventh", pf.pronounceNumber(1567).ordinal(T).shortScale(T).get());
        assertEquals("one thousand, five hundred and sixty seventh", pf.pronounceNumber(1567).ordinal(T).shortScale(F).get());

        // big numbers
        assertEquals("eighteen millionth", pf.pronounceNumber(18000000).ordinal(T).get());
        assertEquals("eighteen million, hundredth", pf.pronounceNumber(18000100).ordinal(T).get());
        assertEquals("one hundred and twenty seven billionth", pf.pronounceNumber(127000000000.0).ordinal(T).shortScale(T).get());
        assertEquals("two hundred and one thousand millionth", pf.pronounceNumber(201000000000.0).ordinal(T).shortScale(F).get());
        assertEquals("nine hundred and thirteen billion, eighty million, six hundred thousand, sixty fourth", pf.pronounceNumber(913080600064.0).ordinal(T).shortScale(T).get());
        assertEquals("nine hundred and thirteen thousand eighty million, six hundred thousand, sixty fourth", pf.pronounceNumber(913080600064.0).ordinal(T).shortScale(F).get());
        assertEquals("one trillion, two millionth", pf.pronounceNumber(1000002000000.0).ordinal(T).shortScale(T).get());
        assertEquals("one billion, two millionth", pf.pronounceNumber(1000002000000.0).ordinal(T).shortScale(F).get());
        assertEquals("four trillion, millionth", pf.pronounceNumber(4000001000000.0).ordinal(T).shortScale(T).get());
        assertEquals("four billion, millionth", pf.pronounceNumber(4000001000000.0).ordinal(T).shortScale(F).get());

        // decimal numbers and scientific notation: the behaviour should be the same as with ordinal=F
        assertEquals("two point seven eight", pf.pronounceNumber(2.78).ordinal(T).get());
        assertEquals("third", pf.pronounceNumber(2.78).places(0).ordinal(T).get());
        assertEquals("nineteenth", pf.pronounceNumber(19.004).ordinal(T).get());
        assertEquals("eight hundred and thirty million, four hundred and thirty eight thousand, ninety two point one eight three", pf.pronounceNumber(830438092.1829).places(3).ordinal(T).get());
        assertEquals("two point five four times ten to the power of six", pf.pronounceNumber(2.54e6).ordinal(T).scientific(T).get());
    }

    @Test
    public void edgeCases() {
        assertEquals("zero", pf.pronounceNumber(0.0).get());
        assertEquals("zero", pf.pronounceNumber(-0.0).get());
        assertEquals("infinity", pf.pronounceNumber(Double.POSITIVE_INFINITY).get());
        assertEquals("negative infinity", pf.pronounceNumber(Double.NEGATIVE_INFINITY).scientific(F).get());
        assertEquals("negative infinity", pf.pronounceNumber(Double.NEGATIVE_INFINITY).scientific(T).get());
        assertEquals("not a number", pf.pronounceNumber(Double.NaN).get());
    }
}
