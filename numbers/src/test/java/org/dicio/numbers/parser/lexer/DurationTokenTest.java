package org.dicio.numbers.parser.lexer;

import static org.dicio.numbers.test.TestUtils.DAY;
import static org.dicio.numbers.test.TestUtils.HOUR;
import static org.dicio.numbers.test.TestUtils.MICROS;
import static org.dicio.numbers.test.TestUtils.MILLIS;
import static org.dicio.numbers.test.TestUtils.MINUTE;
import static org.dicio.numbers.test.TestUtils.YEAR;
import static org.dicio.numbers.test.TestUtils.n;
import static org.dicio.numbers.test.TestUtils.t;
import static org.junit.Assert.assertEquals;

import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;
import org.junit.Test;

public class DurationTokenTest {

    private static void assertDurationMultipliedBy(final java.time.Duration tokenDuration,
                                                   final Number number,
                                                   final java.time.Duration expectedDuration) {
        assertEquals(expectedDuration, new Duration(tokenDuration).multiply(number).toJavaDuration());
    }

    @Test
    public void testGetDurationMultipliedByInteger() {
        assertDurationMultipliedBy(t(1),              n(18),         t(18));
        assertDurationMultipliedBy(t(0, 1),           n(46),         t(0, 46));
        assertDurationMultipliedBy(t(6),              n(2),          t(12));
        assertDurationMultipliedBy(t(0, 15),          n(3),          t(0, 45));
        assertDurationMultipliedBy(t(HOUR),           n(24),         t(DAY));
        assertDurationMultipliedBy(t(0, MICROS),      n(100),        t(0, 100 * MICROS));
        assertDurationMultipliedBy(t(0, 10 * MILLIS), n(6000),       t(MINUTE));
        assertDurationMultipliedBy(t(1, 1),           n(1000000000), t(1000000001));
    }

    @Test
    public void testGetDurationMultipliedByDecimalLikeInteger() {
        // exactly the same as in testGetDurationMultipliedByInteger but with double instead of long
        assertDurationMultipliedBy(t(1),              n(18.0), t(18));
        assertDurationMultipliedBy(t(0, 1),           n(46.0), t(0, 46));
        assertDurationMultipliedBy(t(6),              n(2.0),  t(12));
        assertDurationMultipliedBy(t(0, 15),          n(3.0),  t(0, 45));
        assertDurationMultipliedBy(t(HOUR),           n(24.0), t(DAY));
        assertDurationMultipliedBy(t(0, MICROS),      n(1e2),  t(0, 100 * MICROS));
        assertDurationMultipliedBy(t(0, 10 * MILLIS), n(6e3),  t(MINUTE));
        assertDurationMultipliedBy(t(1, 1),           n(1e9),  t(1000000001));
    }

    @Test
    public void testGetDurationMultipliedByDecimal() {
        // tests with actual decimal numbers (i.e. numbers with a fractional part different from 0)
        assertDurationMultipliedBy(t(DAY),                          n(1.5),     t(36 * HOUR));
        assertDurationMultipliedBy(t(4),                            n(1.2),     t(4, 800 * MILLIS));
        assertDurationMultipliedBy(t(6 * YEAR),                     n(2.5),     t(15 * YEAR));
        assertDurationMultipliedBy(t(10),                           n(1e-4),    t(0, MILLIS));
        assertDurationMultipliedBy(t(0, MILLIS),                    n(0.1),     t(0, 100 * MICROS));
        assertDurationMultipliedBy(t(0, 400 * MILLIS),              n(7.5),     t(3, 0));
        assertDurationMultipliedBy(t(0, 1),                         n(0.6),     t(0, 1)); // rounds
        assertDurationMultipliedBy(t(12 * MINUTE, 800 * MILLIS),    n(1.25),    t(15 * MINUTE + 1, 0));
        assertDurationMultipliedBy(t(10, 100),                      n(0.01),    t(0, 100 * MILLIS + 1));
        assertDurationMultipliedBy(t(14 * MINUTE + 3, 81 * MILLIS), n(0.9),     t(12 * MINUTE + 38, 772 * MILLIS + 900 * MICROS));

        // Floating point errors occur below here, hence the +1 at the end... Strangely enough
        // 292.8-293 is not even close to -0.2, as there are 408 other doubles in between the two,
        // as this Python 3 code proves (where 2.7755575615628914E-17 = Math.ulp(0.2)):
        //
        //     for i in range(409):
        //         assert (292.8-293-2.7755575615628914E-17*i) != (292.8-293-2.7755575615628914E-17*(i+1))
        //
        assertDurationMultipliedBy(t(16 * DAY, 414 * MILLIS),       n(18.3),    t(292 * DAY + 19 * HOUR + 12 * MINUTE + 7, 576 * MILLIS + 200 * MICROS + 1));

        // Floating point errors here, too: the correct result would be t(152999947232L, 760004608)
        assertDurationMultipliedBy(t(389570574000L, 67017),         n(0.39274), t(152999947232L, 760022001));
    }
}
