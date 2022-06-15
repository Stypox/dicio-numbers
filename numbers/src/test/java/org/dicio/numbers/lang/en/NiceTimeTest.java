package org.dicio.numbers.lang.en;

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
        pf = new ParserFormatter(new EnglishFormatter(), null);
    }

    @Test
    public void random() {
        final LocalTime dt = LocalTime.of(13, 22, 3);
        assertEquals("one twenty two", pf.niceTime(dt).get());
        assertEquals("one twenty two p.m.", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("thirteen twenty two", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("thirteen twenty two", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("1:22", pf.niceTime(dt).speech(F).get());
        assertEquals("1:22 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("13:22", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("13:22", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void oClock() {
        final LocalTime dt = LocalTime.of(15, 0, 32);
        assertEquals("three o'clock", pf.niceTime(dt).get());
        assertEquals("three p.m.", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("fifteen hundred", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("fifteen hundred", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("3:00", pf.niceTime(dt).speech(F).get());
        assertEquals("3:00 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("15:00", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("15:00", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void afterMidnight() {
        final LocalTime dt = LocalTime.of(0, 2, 9);
        assertEquals("twelve oh two", pf.niceTime(dt).get());
        assertEquals("twelve oh two a.m.", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("zero zero zero two", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("zero zero zero two", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("12:02", pf.niceTime(dt).speech(F).get());
        assertEquals("12:02 AM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("00:02", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("00:02", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void quarterPast() {
        final LocalTime dt = LocalTime.of(1, 15, 33);
        assertEquals("quarter past one", pf.niceTime(dt).get());
        assertEquals("quarter past one a.m.", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("zero one fifteen", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("zero one fifteen", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("1:15", pf.niceTime(dt).speech(F).get());
        assertEquals("1:15 AM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("01:15", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("01:15", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void half() {
        final LocalTime dt = LocalTime.of(12, 30, 59);
        assertEquals("half past twelve", pf.niceTime(dt).get());
        assertEquals("half past twelve p.m.", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("twelve thirty", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("twelve thirty", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("12:30", pf.niceTime(dt).speech(F).get());
        assertEquals("12:30 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("12:30", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("12:30", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void quarterTo() {
        final LocalTime dt = LocalTime.of(23, 45, 7);
        assertEquals("quarter to twelve", pf.niceTime(dt).get());
        assertEquals("quarter to twelve p.m.", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("twenty three forty five", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("twenty three forty five", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("11:45", pf.niceTime(dt).speech(F).get());
        assertEquals("11:45 PM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("23:45", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("23:45", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }

    @Test
    public void tenAm() {
        final LocalTime dt = LocalTime.of(10, 3, 44);
        assertEquals("ten oh three", pf.niceTime(dt).get());
        assertEquals("ten oh three a.m.", pf.niceTime(dt).showAmPm(T).get());
        assertEquals("ten zero three", pf.niceTime(dt).use24Hour(T).get());
        assertEquals("ten zero three", pf.niceTime(dt).use24Hour(T).showAmPm(T).get());
        assertEquals("10:03", pf.niceTime(dt).speech(F).get());
        assertEquals("10:03 AM", pf.niceTime(dt).speech(F).showAmPm(T).get());
        assertEquals("10:03", pf.niceTime(dt).speech(F).use24Hour(T).get());
        assertEquals("10:03", pf.niceTime(dt).speech(F).use24Hour(T).showAmPm(T).get());
    }
}
