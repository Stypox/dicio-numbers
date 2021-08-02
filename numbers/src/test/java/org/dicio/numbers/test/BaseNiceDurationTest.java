package org.dicio.numbers.test;

import org.dicio.numbers.NumberParserFormatter;
import org.dicio.numbers.formatter.NumberFormatter;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class BaseNiceDurationTest {

    protected final NumberParserFormatter pf;

    protected BaseNiceDurationTest(final NumberFormatter numberFormatter) {
        pf = new NumberParserFormatter(numberFormatter, null);
    }

    protected void assertDuration(final String expected,
                                  final boolean speech,
                                  final long days,
                                  final long hours,
                                  final long minutes,
                                  final long seconds) {
        // just make sure given parameters are valid
        assertTrue(days >= 0);
        assertTrue(hours >= 0 && hours < 24);
        assertTrue(minutes >= 0 && minutes < 60);
        assertTrue(seconds >= 0 && seconds < 60);

        assertEquals(expected, pf.niceDuration(
                        Duration.ofSeconds(seconds + 60 * (minutes + 60 * (hours + 24 * days))))
                .speech(speech).get());
    }
}
