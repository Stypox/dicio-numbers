package org.dicio.numbers.test;

import com.grack.nanojson.JsonParserException;
import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.unit.Duration;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class NiceDurationTestBase {

    protected ParserFormatter pf;

    public abstract Formatter buildNumberFormatter();

    @Before
    public void setup() throws FileNotFoundException, JsonParserException {
        pf = new ParserFormatter(buildNumberFormatter(), null);
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

        assertEquals(expected, pf.niceDuration(new Duration()
                        .plus(seconds + 60 * (minutes + 60 * (hours + 24 * days)), ChronoUnit.SECONDS))
                .speech(speech).get());
    }
}
