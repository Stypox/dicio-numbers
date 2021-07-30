package org.dicio.numbers.lang.it;

import org.dicio.numbers.NumberParserFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NiceDurationTest {

    private static NumberParserFormatter pf;

    @BeforeClass
    public static void setup() {
        pf = new NumberParserFormatter(new ItalianFormatter(), null);
    }

    private static void assertDuration(final String expected,
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

    @Test
    public void zero() {
        assertDuration("zero secondi", true, 0, 0, 0, 0);
        assertDuration("0:00", false, 0, 0, 0, 0);
    }

    @Test
    public void speechOne() {
        assertDuration("un secondo", true, 0, 0, 0, 1);
        assertDuration("un minuto", true, 0, 0, 1, 0);
        assertDuration("un ora", true, 0, 1, 0, 0);
        assertDuration("un giorno", true, 1, 0, 0, 0);
    }

    @Test
    public void speechMany() {
        assertDuration("cinque secondi", true, 0, 0, 0, 5);
        assertDuration("due minuti", true, 0, 0, 2, 0);
        assertDuration("diciassette ore", true, 0, 17, 0, 0);
        assertDuration("ottanta quattro giorni", true, 84, 0, 0, 0);
    }

    @Test
    public void speech() {
        assertDuration("sei giorni venti tre ore cinquanta nove minuti trenta due secondi", true, 6, 23, 59, 32);
        assertDuration("diciannove giorni cinquanta due minuti", true, 19, 0, 52, 0);
        assertDuration("un ora sei secondi", true, 0, 1, 0, 6);
        assertDuration("sessanta tre giorni quaranta quattro secondi", true, 63, 0, 0, 44);
        assertDuration("un giorno un ora un minuto un secondo", true, 1, 1, 1, 1);
    }

    @Test
    public void noSpeechOne() {
        assertDuration("0:01", false, 0, 0, 0, 1);
        assertDuration("1:00", false, 0, 0, 1, 0);
        assertDuration("1:00:00", false, 0, 1, 0, 0);
        assertDuration("1d 0:00:00", false, 1, 0, 0, 0);
    }

    @Test
    public void noSpeechMany() {
        assertDuration("0:39", false, 0, 0, 0, 39);
        assertDuration("24:00", false, 0, 0, 24, 0);
        assertDuration("3:00:00", false, 0, 3, 0, 0);
        assertDuration("76d 0:00:00", false, 76, 0, 0, 0);
    }

    @Test
    public void noSpeech() {
        assertDuration("6d 23:59:32", false, 6, 23, 59, 32);
        assertDuration("19d 0:52:00", false, 19, 0, 52, 0);
        assertDuration("1:00:06", false, 0, 1, 0, 6);
        assertDuration("63d 0:00:44", false, 63, 0, 0, 44);
        assertDuration("1d 1:01:01", false, 1, 1, 1, 1);
    }
}
