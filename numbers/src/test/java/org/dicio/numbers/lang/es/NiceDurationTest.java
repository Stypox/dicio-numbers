package org.dicio.numbers.lang.es;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.test.NiceDurationTestBase;
import org.junit.Test;

public class NiceDurationTest extends NiceDurationTestBase {

    @Override
    public Formatter buildNumberFormatter() {
        return new SpanishFormatter();
    }

    @Test
    public void zero() {
        assertDuration("cero segundos", T, 0, 0, 0, 0);
        assertDuration("0:00",         F, 0, 0, 0, 0);
    }

    @Test
    public void speechOne() {
        assertDuration("un segundo", T, 0, 0, 0, 1);
        assertDuration("un minuto", T, 0, 0, 1, 0);
        assertDuration("una hora",   T, 0, 1, 0, 0);
        assertDuration("un día",    T, 1, 0, 0, 0);
    }

    @Test
    public void speechMany() {
        assertDuration("cinco segundos",     T,  0,  0, 0, 5);
        assertDuration("dos minutos",      T,  0,  0, 2, 0);
        assertDuration("diecisiete horas",  T,  0, 17, 0, 0);
        assertDuration("ochenta y cuatro horas", T, 84,  0, 0, 0);
    }

    //TODO Spanish translation

    @Test
    public void speech() {
        assertDuration("seis días veintitrés horas cincuenta y nueve minutos treinta y dos segundos", T,  6, 23, 59, 32);
        assertDuration("diecinueve días cincuenta y dos minutos",                                     T, 19,  0, 52,  0);
        assertDuration("una hora seis segundos",                                                      T,  0,  1,  0,  6);
        assertDuration("sesenta y tres días cuarenta y cuatro segundos",                              T, 63,  0,  0, 44);
        assertDuration("un día una hora un minuto un segundo",                                        T,  1,  1,  1,  1);
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
        assertDuration("1d 1:01:01",  F , 1,  1,  1,  1);
    }
}
