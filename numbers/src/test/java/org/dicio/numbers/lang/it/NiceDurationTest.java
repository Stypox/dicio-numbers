package org.dicio.numbers.lang.it;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.test.NiceDurationTestBase;
import org.junit.Test;

public class NiceDurationTest extends NiceDurationTestBase {

    @Override
    public Formatter buildNumberFormatter() {
        return new ItalianFormatter();
    }

    @Test
    public void zero() {
        assertDuration("zero secondi", T, 0, 0, 0, 0);
        assertDuration("0:00",         F, 0, 0, 0, 0);
    }

    @Test
    public void speechOne() {
        assertDuration("un secondo", T, 0, 0, 0, 1);
        assertDuration("un minuto",  T, 0, 0, 1, 0);
        assertDuration("un ora",     T, 0, 1, 0, 0);
        assertDuration("un giorno",  T, 1, 0, 0, 0);
    }

    @Test
    public void speechMany() {
        assertDuration("cinque secondi",         T,  0,  0, 0, 5);
        assertDuration("due minuti",             T,  0,  0, 2, 0);
        assertDuration("diciassette ore",        T,  0, 17, 0, 0);
        assertDuration("ottanta quattro giorni", T, 84,  0, 0, 0);
    }

    @Test
    public void speech() {
        assertDuration("sei giorni venti tre ore cinquanta nove minuti trenta due secondi", T,  6, 23, 59, 32);
        assertDuration("diciannove giorni cinquanta due minuti",                            T, 19,  0, 52,  0);
        assertDuration("un ora sei secondi",                                                T,  0,  1,  0,  6);
        assertDuration("sessanta tre giorni quaranta quattro secondi",                      T, 63,  0,  0, 44);
        assertDuration("un giorno un ora un minuto un secondo",                             T,  1,  1,  1,  1);
    }

    @Test
    public void noSpeechOne() {
        assertDuration("0:01",       F, 0, 0, 0, 1);
        assertDuration("1:00",       F, 0, 0, 1, 0);
        assertDuration("1:00:00",    F, 0, 1, 0, 0);
        assertDuration("1d 0:00:00", F, 1, 0, 0, 0);
    }

    @Test
    public void noSpeechMany() {
        assertDuration("0:39",        F,  0, 0,  0, 39);
        assertDuration("24:00",       F,  0, 0, 24,  0);
        assertDuration("3:00:00",     F,  0, 3,  0,  0);
        assertDuration("76d 0:00:00", F, 76, 0,  0,  0);
    }

    @Test
    public void noSpeech() {
        assertDuration("6d 23:59:32", F,  6, 23, 59, 32);
        assertDuration("19d 0:52:00", F, 19,  0, 52,  0);
        assertDuration("1:00:06",     F,  0,  1,  0,  6);
        assertDuration("63d 0:00:44", F, 63,  0,  0, 44);
        assertDuration("1d 1:01:01",  F,  1,  1,  1,  1);
    }
}
