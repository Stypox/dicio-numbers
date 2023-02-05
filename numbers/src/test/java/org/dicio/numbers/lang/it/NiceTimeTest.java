package org.dicio.numbers.lang.it;

import org.dicio.numbers.ParserFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalTime;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;
import static org.junit.Assert.assertEquals;

public class NiceTimeTest {

    private static ParserFormatter pf;

    @BeforeClass
    public static void setup() {
        pf = new ParserFormatter(new ItalianFormatter(), null);
    }

    @Test
    public void random() {
        final LocalTime dt = LocalTime.of(13, 22, 3);
        assertEquals("una e venti due", pf.niceTime(dt).get());
        assertEquals("una e venti due di pomeriggio", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("tredici e venti due", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("tredici e venti due", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("1:22", pf.niceTime(dt).speech(F).get());
        assertEquals("1:22 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("13:22", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("13:22", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void oClock() {
        final LocalTime dt = LocalTime.of(15, 0, 32);
        assertEquals("tre in punto", pf.niceTime(dt).get());
        assertEquals("tre in punto di pomeriggio", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("quindici in punto", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("quindici in punto", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("3:00", pf.niceTime(dt).speech(F).get());
        assertEquals("3:00 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("15:00", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("15:00", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void afterMidnight() {
        final LocalTime dt = LocalTime.of(0, 2, 9);
        assertEquals("mezzanotte e zero due", pf.niceTime(dt).get());
        assertEquals("mezzanotte e zero due", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("mezzanotte e zero due", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("mezzanotte e zero due", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("12:02", pf.niceTime(dt).speech(F).get());
        assertEquals("12:02 AM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("00:02", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("00:02", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void quarterPast() {
        final LocalTime dt = LocalTime.of(1, 15, 33);
        assertEquals("una e un quarto", pf.niceTime(dt).get());
        assertEquals("una e un quarto di notte", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("una e un quarto", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("una e un quarto", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("1:15", pf.niceTime(dt).speech(F).get());
        assertEquals("1:15 AM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("01:15", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("01:15", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void half() {
        final LocalTime dt = LocalTime.of(12, 30, 59);
        assertEquals("mezzogiorno e mezza", pf.niceTime(dt).get());
        assertEquals("mezzogiorno e mezza", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("mezzogiorno e mezza", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("mezzogiorno e mezza", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("12:30", pf.niceTime(dt).speech(F).get());
        assertEquals("12:30 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("12:30", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("12:30", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void quarterTo() {
        final LocalTime dt = LocalTime.of(23, 45, 7);
        assertEquals("un quarto a mezzanotte", pf.niceTime(dt).get());
        assertEquals("un quarto a mezzanotte", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("un quarto a mezzanotte", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("un quarto a mezzanotte", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("11:45", pf.niceTime(dt).speech(F).get());
        assertEquals("11:45 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("23:45", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("23:45", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void tenAm() {
        final LocalTime dt = LocalTime.of(10, 3, 44);
        assertEquals("dieci e zero tre", pf.niceTime(dt).get());
        assertEquals("dieci e zero tre di mattina", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("dieci e zero tre", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("dieci e zero tre", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("10:03", pf.niceTime(dt).speech(F).get());
        assertEquals("10:03 AM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("10:03", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("10:03", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void tenPm() {
        final LocalTime dt = LocalTime.of(22, 3, 44);
        assertEquals("dieci e zero tre", pf.niceTime(dt).get());
        assertEquals("dieci e zero tre di sera", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("venti due e zero tre", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("venti due e zero tre", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("10:03", pf.niceTime(dt).speech(F).get());
        assertEquals("10:03 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("22:03", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("22:03", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }
}
