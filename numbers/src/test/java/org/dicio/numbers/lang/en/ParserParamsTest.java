package org.dicio.numbers.lang.en;

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
import org.dicio.numbers.parser.param.NumberParserParamsTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ParserParamsTest extends NumberParserParamsTestBase {

    @Override
    protected Parser numberParser() {
        return new EnglishParser();
    }


    @Test
    public void testNumberFirst() {
        assertNumberFirst("36 twelfths of apple",              F, T, F, n(3, F));
        assertNumberFirst("36 twelfths of apple",              T, T, T, n(36, F));
        assertNumberFirst("I'm really one hundred and eighth", F, F, F, n(100, F));
        assertNumberFirst("I'm really one hundred and eighth", T, T, F, n(108, T));
        assertNumberFirst("I'm really one hundred and eighth", T, F, T, n(108, T));
        assertNumberFirst("I'm really one hundred and eighth", F, T, T, n(108, T));
        assertNumberFirst("it's 10/20",                        T, F, F, n(0.5, F));
        assertNumberFirst("it's 10/20",                        F, F, T, n(10, F));
        assertNumberFirst("does half",                         T, T, F, n(0.5, F));
        assertNumberFirst("does half",                         F, T, T, null);
        assertNumberFirst("a",                                 T, F, F, null);
        assertNumberFirst("whatever",                          F, F, F, null);
        assertNumberFirst("and whatever",                      T, T, F, null);

        // edge case, "nineteen sixty four trillions" wouldn't be a valid integer, and so
        // "nineteen sixty four" and "trillions" are considered two different numbers
        assertNumberFirst("it is nineteen sixty four trillionths", T, F, F, n(1964e-12, F));
        assertNumberFirst("it is nineteen sixty four trillionths", T, F, T, n(1964, F));
        assertNumberFirst("it is nineteen sixty four trillionths", T, T, F, n(1964e-12, F));
        assertNumberFirst("it is nineteen sixty four trillions",   T, T, F, n(1964, F));
        assertNumberFirst("it is nineteen sixty four trillionths", F, F, F, n(1964e-18, F));
        assertNumberFirst("it is nineteen sixty four trillionths", F, F, T, n(1964, F));
        assertNumberFirst("it is nineteen sixty four trillionths", F, T, F, n(1964e-18, F));
        assertNumberFirst("it is nineteen sixty four trillions",   F, T, F, n(1964, F));

        // edge case, "one thousand nine hundred sixty four trillions" wouldn't be a valid short
        // scale integer, and so "one thousand" and "nine hundred sixty four trillions" are
        // considered two different numbers
        assertNumberFirst("it is one thousand nine hundred sixty four trillionths", T, F, F, n(1964e-12, F));
        assertNumberFirst("it is one thousand nine hundred sixty four trillionths", T, F, T, n(1000, F));
        assertNumberFirst("it is one thousand nine hundred sixty four trillionths", T, T, F, n(1000 / 964e12, F));
        assertNumberFirst("it is one thousand nine hundred sixty four trillions",   T, T, F, n(1000, F));
        assertNumberFirst("it is one thousand nine hundred sixty four trillionths", F, F, F, n(1964e-18, F));
        assertNumberFirst("it is one thousand nine hundred sixty four trillionths", F, F, T, n(1964e18, T));
        assertNumberFirst("it is one thousand nine hundred sixty four trillionths", F, T, F, n(1964e18, T));
        assertNumberFirst("it is one thousand nine hundred sixty four trillions",   F, T, F, n(1964e18, F));
    }

    @Test
    public void testNumberFirstIfInteger() {
        assertNumberFirstIfInteger("it's two twelfths ok?", F, T, F, null);
        assertNumberFirstIfInteger("it's two twelfths ok?", T, T, T, 2L);
        assertNumberFirstIfInteger("it's half",             F, T, F, null);
        assertNumberFirstIfInteger("it's half",             T, T, T, null);
        assertNumberFirstIfInteger("two halves",            F, T, F, 1L);
        assertNumberFirstIfInteger("two halves",            T, T, T, 2L);
    }

    @Test
    public void testNumberMixedWithText() {
        assertNumberMixedWithText(" hello  ciao!, 3/5 or four sevenths?", T, F, F, " hello  ciao!, ", n(3.0 / 5.0, F), " or ", n(4.0 / 7.0, F), "?");
        assertNumberMixedWithText(" hello  ciao!, four sevenths or 3/5?", T, T, F, " hello  ciao!, ", n(4.0 / 7.0, F), " or ", n(3.0 / 5.0, F), "?");
        assertNumberMixedWithText(" hello  ciao!, four sevenths or 3/5?", T, T, T, " hello  ciao!, ", n(4, F), " ", n(7, T), " or ", n(3, F), "/", n(5, F), "?");
        assertNumberMixedWithText("three billionth plus two",             T, T, F, n(3000000000L, T), " ", n(2, F));
        assertNumberMixedWithText("one billionth and sixteen sixty four", T, F, F, n(1.0 / 1000000000.0, F), " and ", n(1664, F));
        assertNumberMixedWithText("two billionths minus fifty eight",     F, T, F, n(2000000000000L, T), " ", n(-58, F));
        assertNumberMixedWithText("nine billionths times eleven",         F, F, F, n(9.0 / 1000000000000.0, F), " times ", n(11, F));
        assertNumberMixedWithText("nine billionths times eleven",         F, F, T, n(9000000000000L, T), " times ", n(11, F));
        assertNumberMixedWithText("three halves, not eleven quarters",    F, T, F, n(3.0 / 2.0, F), ", not ", n(11.0 / 4.0, F));
        assertNumberMixedWithText("three halves, not eleven fifths",      F, T, T, n(3, F), " halves, not ", n(11, F), " ", n(5, T));
        assertNumberMixedWithText("six pairs equals a dozen ",            F, T, F, n(12, F), " equals ", n(12, F), " ");
        assertNumberMixedWithText("six pairs equals a dozen ",            F, T, T, n(12, F), " equals ", n(12, F), " ");
        assertNumberMixedWithText("a dozen scores is not a gross",        F, T, F, n(240, F), " is not ", n(144, F));
        assertNumberMixedWithText("6 quadrillionths of a cake",           F, T, F, n(6e24, T), " of a cake");
        assertNumberMixedWithText("6 quadrillionths of a cake",           F, F, T, n(6e24, T), " of a cake");
        assertNumberMixedWithText("6 quadrillionths of a cake",           F, F, F, n(6e-24, F), " of a cake");
        assertNumberMixedWithText("is nineteen sixty four quadrillionth", F, F, F, "is ", n(1964e-24, F));
        assertNumberMixedWithText("I'm twenty three years old.",          T, F, F, "I'm ", n(23, F), " years old.");
        assertNumberMixedWithText("The quintillionth",                    F, F, F, "The ", n(1e30, T));
        assertNumberMixedWithText("One quintillionth",                    T, F, F, n(1e-18, F));
        assertNumberMixedWithText("One quintillionth",                    T, T, F, n(1000000000000000000L, T));
        assertNumberMixedWithText("One quintillionth",                    T, F, T, n(1000000000000000000L, T));
        assertNumberMixedWithText("One billion",                          F, T, F, n(1000000000000L, F));
        assertNumberMixedWithText("two halves",                           F, F, F, n(1, F));
        assertNumberMixedWithText("two halves",                           F, T, T, n(2, F), " halves");
    }

    // built for usage below, but the building shouldn't count in the test timeout
    static String longNumberMixedWithText;
    static int partsOfLongNumberMixedWithText;
    @BeforeClass
    public static void setupLongNumberMixedWithText() {
        final ParserFormatter npf = new ParserFormatter(new EnglishFormatter(), null);
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
            strings.add(npf.pronounceNumber(num).places(0).shortScale(false).get()); // long scale not ordinal
            strings.add(npf.pronounceNumber(num).places(0).shortScale(false).ordinal(true).get()); // long scale ordinal
            strings.add(npf.niceNumber(num).speech(false).get()); // not speech
            strings.add(npf.niceNumber(num).speech(true).get()); // speech
            strings.add(String.valueOf(num));
            strings.add(i % 2 == 0 ? " hello " : " of ");
            strings.add(i % 2 == 0 ? "invalid" : "one hundredth");
            strings.add(i % 2 == 0 ? " and " : " a ");
            strings.add(i % 2 == 0 ? "," : " ; ");
            strings.add("-++-+--+-+-");
            strings.add(i % 2 == 0 ? " plus " : " minus ");
        }
        partsOfLongNumberMixedWithText = strings.size();
        longNumberMixedWithText = String.join("", strings);
    }
    @Test(timeout = 12000) // ~160000 number parses take <6s, use 12s timeout just for slower PCs
    public void testNumberMixedWithTextPerformance() {
        // make sure there are a lot of strings indeed (these numbers are just used to test that the
        // input data makes sense, if the input data changes feel free to also change these)
        assertEquals(87061, partsOfLongNumberMixedWithText);
        assertEquals(2120319, longNumberMixedWithText.length());

        for (int i = 0; i < (1 << 3); ++i) {
            final List<Object> objects = new ExtractNumberParams(numberParser(), longNumberMixedWithText)
                .shortScale(i%2 == 1).integerOnly((i/2)%2 == 1).preferOrdinal((i/4)%2 == 1)
                .parseMixedWithText();
            // make sure the number of numbers that was actually parsed is roughly the same as those
            // in the input (the 0.8 is just a magic value, so feel free to decrease it if needed)
            assertTrue(objects.size() / ((double) partsOfLongNumberMixedWithText) > 0.8);
        }
    }

    @Test
    public void testDurationFirst() {
        assertDurationFirst("Set a two minute and two billion nanosecond timer", F, t(2 * MINUTE + 2000L));
        assertDurationFirst("you know two years ago are not billions of days", T, t(2 * YEAR));
    }

    @Test
    public void testDurationMixedWithText() {
        assertDurationMixedWithText("2ns and four hours while six milliseconds.", F, t(4 * HOUR, 2), " while ", t(0, 6 * MILLIS), ".");
        assertDurationMixedWithText("you know two years ago are not billions of day", T, "you know ", t(2 * YEAR), " ago are not ", t(1000000000L * DAY));
    }
}
