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

import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.param.NumberParserParamsTestBase;
import org.junit.Test;

public class ParserParamsTest extends NumberParserParamsTestBase {

    @Override
    protected Parser numberParser() {
        return new EnglishParser();
    }


    @Test
    public void testNumberFirst() {
        assertNumberFirst("it is nineteen sixty four trillionths", T, F, n(1964e-12, F));
        assertNumberFirst("36 twelfths of apple",                  F, T, n(3, F));
        assertNumberFirst("I'm really one hundred and eighth",     F, F, n(100, F));
        assertNumberFirst("I'm really one hundred and eighth",     T, T, n(108, T));
    }

    @Test
    public void testNumberMixedWithText() {
        assertNumberMixedWithText(" hello  ciao!, 3/5 or four sevenths?", T, F, " hello  ciao!, ", n(3.0 / 5.0, F), " or ", n(4.0 / 7.0, F), "?");
        assertNumberMixedWithText(" hello  ciao!, four sevenths or 3/5?", T, T, " hello  ciao!, ", n(4.0 / 7.0, F), " or ", n(3.0 / 5.0, F), "?");
        assertNumberMixedWithText("three billionth plus two",             T, T, n(3000000000L, T), " ", n(2, F));
        assertNumberMixedWithText("one billionth and sixteen sixty four", T, F, n(1.0 / 1000000000.0, F), " and ", n(1664, F));
        assertNumberMixedWithText("two billionths minus fifty eight",     F, T, n(2000000000000L, T), " ", n(-58, F));
        assertNumberMixedWithText("nine billionths times eleven",         F, F, n(9.0 / 1000000000000.0, F), " times ", n(11, F));
        assertNumberMixedWithText("three halves, not eleven quarters",    F, T, n(3.0 / 2.0, F), ", not ", n(11.0 / 4.0, F));
        assertNumberMixedWithText("six pairs equals a dozen ",            F, T, n(12, F), " equals ", n(12, F), " ");
        assertNumberMixedWithText("a dozen scores is not a gross",        F, T, n(240, F), " is not ", n(144, F));
        assertNumberMixedWithText("6 quadrillionths of a cake",           F, T, n(6e24, T), " of a cake");
        assertNumberMixedWithText("is nineteen sixty four quadrillionth", F, F, "is ", n(1964e-24, F));
        assertNumberMixedWithText("I'm twenty three years old.",          T, F, "I'm ", n(23, F), " years old.");
        assertNumberMixedWithText("The quintillionth",                    F, F, "The ", n(1e30, T));
        assertNumberMixedWithText("One quintillionth",                    T, F, n(1e-18, F));
        assertNumberMixedWithText("One quintillionth",                    T, T, n(1000000000000000000L, T));
        assertNumberMixedWithText("One billion",                          F, T, n(1000000000000L, F));
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
