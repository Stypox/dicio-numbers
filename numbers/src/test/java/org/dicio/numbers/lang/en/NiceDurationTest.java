package org.dicio.numbers.lang.en;

import org.dicio.numbers.formatter.NumberFormatter;
import org.dicio.numbers.test.BaseNiceDurationTest;
import org.junit.Test;

public class NiceDurationTest extends BaseNiceDurationTest {

    @Override
    public NumberFormatter buildNumberFormatter() {
        return new EnglishFormatter();
    }

    @Test
    public void zero() {
        assertDuration("zero seconds", true, 0, 0, 0, 0);
        assertDuration("0:00", false, 0, 0, 0, 0);
    }

    @Test
    public void speechOne() {
        assertDuration("one second", true, 0, 0, 0, 1);
        assertDuration("one minute", true, 0, 0, 1, 0);
        assertDuration("one hour", true, 0, 1, 0, 0);
        assertDuration("one day", true, 1, 0, 0, 0);
    }

    @Test
    public void speechMany() {
        assertDuration("five seconds", true, 0, 0, 0, 5);
        assertDuration("two minutes", true, 0, 0, 2, 0);
        assertDuration("seventeen hours", true, 0, 17, 0, 0);
        assertDuration("eighty four days", true, 84, 0, 0, 0);
    }

    @Test
    public void speech() {
        assertDuration("six days twenty three hours fifty nine minutes thirty two seconds", true, 6, 23, 59, 32);
        assertDuration("nineteen days fifty two minutes", true, 19, 0, 52, 0);
        assertDuration("one hour six seconds", true, 0, 1, 0, 6);
        assertDuration("sixty three days forty four seconds", true, 63, 0, 0, 44);
        assertDuration("one day one hour one minute one second", true, 1, 1, 1, 1);
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
