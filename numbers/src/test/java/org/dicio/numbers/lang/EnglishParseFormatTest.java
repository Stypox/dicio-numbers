package org.dicio.numbers.lang;

import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class EnglishParseFormatTest {

    private static EnglishParseFormat pf;

    @BeforeClass
    public static void setup() {
        pf = new EnglishParseFormat();
    }

    @Test
    public void testPronounceNumberSmallIntegers() {
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
    public void testPronounceNumberNegativeSmallIntegers() {
        assertEquals("minus one", pf.pronounceNumber(-1).get());
        assertEquals("minus ten", pf.pronounceNumber(-10).get());
        assertEquals("minus fifteen", pf.pronounceNumber(-15).get());
        assertEquals("minus twenty", pf.pronounceNumber(-20).get());
        assertEquals("minus twenty seven", pf.pronounceNumber(-27).get());
        assertEquals("minus thirty", pf.pronounceNumber(-30).get());
        assertEquals("minus thirty three", pf.pronounceNumber(-33).get());
    }

    @Test
    public void testPronounceNumberDecimals() {
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
    public void testPronounceNumberRoundingDecimals() {
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
    public void testPronounceNumberHundred() {
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
    public void testPronounceNumberYear() {
        assertEquals("fourteen fifty six", pf.pronounceNumber(1456).get());
        assertEquals("nineteen eighty four", pf.pronounceNumber(1984).get());
        assertEquals("eighteen oh one", pf.pronounceNumber(1801).get());
        assertEquals("eleven hundred", pf.pronounceNumber(1100).get());
        assertEquals("twelve oh one", pf.pronounceNumber(1201).get());
        assertEquals("fifteen ten", pf.pronounceNumber(1510).get());
        assertEquals("ten oh six", pf.pronounceNumber(1006).get());
        assertEquals("one thousand", pf.pronounceNumber(1000).get());
        assertEquals("two thousand", pf.pronounceNumber(2000).get());
        assertEquals("two thousand, fifteen", pf.pronounceNumber(2015).get()); // TODO maybe "twenty fifteen"?
        assertEquals("four thousand, eight hundred and twenty seven", pf.pronounceNumber(4827).get());
    }

    @Test
    public void testPronounceNumberScientificNotation() {
        assertEquals("zero", pf.pronounceNumber(0.0).scientific(true).get());
        assertEquals("three point three times ten to the power of one",
                pf.pronounceNumber(33).scientific(true).get());
        assertEquals("two point nine nine times ten to the power of eight",
                pf.pronounceNumber(299492458).scientific(true).get());
        assertEquals("two point nine nine seven nine two five times ten to the power of eight",
                pf.pronounceNumber(299792458).scientific(true).places(6).get());
        assertEquals("one point six seven two times ten to the power of negative twenty seven",
                pf.pronounceNumber(1.672e-27).scientific(true).places(3).get());

        // auto scientific notation when number is too big to be pronounced
        assertEquals("two point nine five times ten to the power of twenty four",
                pf.pronounceNumber(2.9489e24).get());
    }

    private void assertShortLongScale(final double number,
                                      final String shortScale,
                                      final String longScale) {
        assertEquals(shortScale, pf.pronounceNumber(number).shortScale(true).get());
        assertEquals(longScale, pf.pronounceNumber(number).shortScale(false).get());
    }

    @Test
    public void testPronounceNumberLargeNumbers() {
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
    public void testPronounceNumberEdgeCases() {
        assertEquals("zero", pf.pronounceNumber(0.0).get());
        assertEquals("zero", pf.pronounceNumber(-0.0).get());
        assertEquals("infinity", pf.pronounceNumber(Double.POSITIVE_INFINITY).get());
        assertEquals("negative infinity", pf.pronounceNumber(Double.NEGATIVE_INFINITY).scientific(false).get());
        assertEquals("negative infinity", pf.pronounceNumber(Double.NEGATIVE_INFINITY).scientific(true).get());
        assertEquals("not a number", pf.pronounceNumber(Double.NaN).get());
    }

    @Test
    public void testPronounceNumberOrdinals() {
        // small numbers
        assertEquals("first", pf.pronounceNumber(1).ordinals(true).get());
        assertEquals("tenth", pf.pronounceNumber(10).ordinals(true).get());
        assertEquals("fifteenth", pf.pronounceNumber(15).ordinals(true).get());
        assertEquals("twentieth", pf.pronounceNumber(20).ordinals(true).get());
        assertEquals("twenty seventh", pf.pronounceNumber(27).ordinals(true).get());
        assertEquals("thirtieth", pf.pronounceNumber(30).ordinals(true).get());
        assertEquals("thirty third", pf.pronounceNumber(33).ordinals(true).get());
        assertEquals("hundredth", pf.pronounceNumber(100).ordinals(true).get());
        assertEquals("thousandth", pf.pronounceNumber(1000).ordinals(true).get());
        assertEquals("ten thousandth", pf.pronounceNumber(10000).ordinals(true).get());
        assertEquals("eighteen thousand, six hundred and ninety first", pf.pronounceNumber(18691).ordinals(true).get());
        assertEquals("one thousand, five hundred and sixty seventh", pf.pronounceNumber(1567).ordinals(true).get());

        // big numbers
        assertEquals("eighteen millionth", pf.pronounceNumber(18000000).ordinals(true).get());
        assertEquals("eighteen million, hundredth", pf.pronounceNumber(18000100).ordinals(true).get());
        assertEquals("one hundred and twenty seven billionth", pf.pronounceNumber(127000000000.0).ordinals(true).shortScale(true).get());
        assertEquals("two hundred and one thousand millionth", pf.pronounceNumber(201000000000.0).ordinals(true).shortScale(false).get());
        assertEquals("nine hundred and thirteen billion, eighty million, six hundred thousand, sixty fourth", pf.pronounceNumber(913080600064.0).ordinals(true).shortScale(true).get());
        assertEquals("nine hundred and thirteen thousand eighty million, six hundred thousand, sixty fourth", pf.pronounceNumber(913080600064.0).ordinals(true).shortScale(false).get());
        assertEquals("one trillion, two millionth", pf.pronounceNumber(1000002000000.0).ordinals(true).shortScale(true).get());
        assertEquals("one billion, two millionth", pf.pronounceNumber(1000002000000.0).ordinals(true).shortScale(false).get());
        assertEquals("four trillion, millionth", pf.pronounceNumber(4000001000000.0).ordinals(true).shortScale(true).get());
        assertEquals("four billion, millionth", pf.pronounceNumber(4000001000000.0).ordinals(true).shortScale(false).get());

        // decimal numbers and scientific notation: the behaviour should be the same as with ordinals=false
        assertEquals("two point seven eight", pf.pronounceNumber(2.78).ordinals(true).get());
        assertEquals("third", pf.pronounceNumber(2.78).places(0).ordinals(true).get());
        assertEquals("nineteenth", pf.pronounceNumber(19.004).ordinals(true).get());
        assertEquals("eight hundred and thirty million, four hundred and thirty eight thousand, ninety two point one eight three", pf.pronounceNumber(830438092.1829).places(3).ordinals(true).get());
        assertEquals("two point five four times ten to the power of six", pf.pronounceNumber(2.54e6).ordinals(true).scientific(true).get());
    }

    @Test
    public void testNiceNumberSpeech() {
        assertEquals("thirty four and a half", pf.niceNumber(34.5).get());
        assertEquals("minus eighteen and three fifths", pf.niceNumber(-18.6).get());
        assertEquals("ninety eight and eighteen nineteenths", pf.niceNumber(98.947368421).get());
        assertEquals("minus five and six elevenths", pf.niceNumber(-5.5454545).get());
        assertEquals("seven ninths", pf.niceNumber(7.0 / 9).get());
        assertEquals("minus two seventeenths", pf.niceNumber(-2.0 / 17).get());
        assertEquals("four hundred and sixty five", pf.niceNumber(465).get());
        assertEquals("minus ninety one", pf.niceNumber(-91).get());
        assertEquals("zero", pf.niceNumber(0).get());
    }

    @Test
    public void testNiceNumberNoSpeech() {
        assertEquals("34 1/2", pf.niceNumber(34.5).speech(false).get());
        assertEquals("-18 3/5", pf.niceNumber(-18.6).speech(false).get());
        assertEquals("98 18/19", pf.niceNumber(98.947368421).speech(false).get());
        assertEquals("-5 6/11", pf.niceNumber(-5.5454545).speech(false).get());
        assertEquals("7/9", pf.niceNumber(7.0 / 9).speech(false).get());
        assertEquals("-2/17", pf.niceNumber(-2.0 / 17).speech(false).get());
        assertEquals("465", pf.niceNumber(465).speech(false).get());
        assertEquals("-91", pf.niceNumber(-91).speech(false).get());
        assertEquals("0", pf.niceNumber(0).speech(false).get());
    }

    @Test
    public void testNiceNumberCustomDenominators() {
        assertEquals("minus four and four tenths", pf.niceNumber(-4.4).denominators(Arrays.asList(2, 3, 4, 6, 7, 8, 9, 10, 11)).get());
        assertEquals("-64 6/12", pf.niceNumber(-64.5).speech(false).denominators(Collections.singletonList(12)).get());
        assertEquals("minus three and five hundred thousand millionths", pf.niceNumber(-3.5).denominators(Arrays.asList(1000000, 2000000)).get());
        assertEquals("9 1000000/2000000", pf.niceNumber(9.5).speech(false).denominators(Arrays.asList(2000000, 1000000)).get());
        assertEquals("zero point eight", pf.niceNumber(4.0 / 5).denominators(Arrays.asList(2, 3, 4)).get());
    }

    @Test
    public void testNiceNumberInvalidFraction() {
        assertEquals("one point eight four", pf.niceNumber(1.837).get());
        assertEquals("minus thirty eight point one nine", pf.niceNumber(-38.192).get());
        assertEquals("3829.48", pf.niceNumber(3829.47832).speech(false).get());
        assertEquals("-7.19", pf.niceNumber(-7.1928).speech(false).get());
        assertEquals("-9322.38", pf.niceNumber(-9322 - 8.0 / 21).speech(false).get());
    }

    @Test
    public void testNiceTimeRandom() {
        final LocalDateTime dt = LocalDateTime.of(2017, 1, 31, 13, 22, 3);
        assertEquals("one twenty two", pf.niceTime(dt).get());
        assertEquals("one twenty two p.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("thirteen twenty two", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("thirteen twenty two", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("1:22", pf.niceTime(dt).speech(false).get());
        assertEquals("1:22 PM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("13:22", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("13:22", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void testNiceTimeOClock() {
        final LocalDateTime dt = LocalDateTime.of(2021, 6, 17, 15, 0, 32);
        assertEquals("three o'clock", pf.niceTime(dt).get());
        assertEquals("three p.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("fifteen hundred", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("fifteen hundred", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("3:00", pf.niceTime(dt).speech(false).get());
        assertEquals("3:00 PM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("15:00", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("15:00", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void testNiceTimeAfterMidnight() {
        final LocalDateTime dt = LocalDateTime.of(2019, 4, 23, 0, 2, 9);
        assertEquals("twelve oh two", pf.niceTime(dt).get());
        assertEquals("twelve oh two a.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("zero zero zero two", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("zero zero zero two", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("12:02", pf.niceTime(dt).speech(false).get());
        assertEquals("12:02 AM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("00:02", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("00:02", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void testNiceTimeQuarterPast() {
        final LocalDateTime dt = LocalDateTime.of(2018, 2, 8, 1, 15, 33);
        assertEquals("quarter past one", pf.niceTime(dt).get());
        assertEquals("quarter past one a.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("zero one fifteen", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("zero one fifteen", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("1:15", pf.niceTime(dt).speech(false).get());
        assertEquals("1:15 AM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("01:15", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("01:15", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void testNiceTimeHalf() {
        final LocalDateTime dt = LocalDateTime.of(2045, 11, 30, 12, 30, 59);
        assertEquals("half past twelve", pf.niceTime(dt).get());
        assertEquals("half past twelve p.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("twelve thirty", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("twelve thirty", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("12:30", pf.niceTime(dt).speech(false).get());
        assertEquals("12:30 PM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("12:30", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("12:30", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void testNiceTimeQuarterTo() {
        final LocalDateTime dt = LocalDateTime.of(2019, 7, 16, 23, 45, 7);
        assertEquals("quarter to twelve", pf.niceTime(dt).get());
        assertEquals("quarter to twelve p.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("twenty three forty five", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("twenty three forty five", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("11:45", pf.niceTime(dt).speech(false).get());
        assertEquals("11:45 PM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("23:45", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("23:45", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }
}
