package org.dicio.numbers.lang.en;

import org.dicio.numbers.NumberParserFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class NiceTimeTest {

    private static NumberParserFormatter pf;

    @BeforeClass
    public static void setup() {
        pf = new NumberParserFormatter(new EnglishFormatter(), null);
    }

    @Test
    public void random() {
        final LocalDateTime dt = LocalDateTime.of(2017, 1, 31, 13, 22, 3);
        assertEquals("one twenty two", pf.niceTime(dt).get());
        assertEquals("one twenty two p.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("thirteen twenty two", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("thirteen twenty two", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("1:22", pf.niceTime(dt).speech(false).get());
        assertEquals("1:22 PM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("13:22", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("13:22", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void oClock() {
        final LocalDateTime dt = LocalDateTime.of(2021, 6, 17, 15, 0, 32);
        assertEquals("three o'clock", pf.niceTime(dt).get());
        assertEquals("three p.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("fifteen hundred", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("fifteen hundred", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("3:00", pf.niceTime(dt).speech(false).get());
        assertEquals("3:00 PM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("15:00", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("15:00", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void afterMidnight() {
        final LocalDateTime dt = LocalDateTime.of(2019, 4, 23, 0, 2, 9);
        assertEquals("twelve oh two", pf.niceTime(dt).get());
        assertEquals("twelve oh two a.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("zero zero zero two", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("zero zero zero two", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("12:02", pf.niceTime(dt).speech(false).get());
        assertEquals("12:02 AM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("00:02", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("00:02", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void quarterPast() {
        final LocalDateTime dt = LocalDateTime.of(2018, 2, 8, 1, 15, 33);
        assertEquals("quarter past one", pf.niceTime(dt).get());
        assertEquals("quarter past one a.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("zero one fifteen", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("zero one fifteen", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("1:15", pf.niceTime(dt).speech(false).get());
        assertEquals("1:15 AM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("01:15", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("01:15", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void half() {
        final LocalDateTime dt = LocalDateTime.of(2045, 11, 30, 12, 30, 59);
        assertEquals("half past twelve", pf.niceTime(dt).get());
        assertEquals("half past twelve p.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("twelve thirty", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("twelve thirty", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("12:30", pf.niceTime(dt).speech(false).get());
        assertEquals("12:30 PM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("12:30", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("12:30", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void quarterTo() {
        final LocalDateTime dt = LocalDateTime.of(2019, 7, 16, 23, 45, 7);
        assertEquals("quarter to twelve", pf.niceTime(dt).get());
        assertEquals("quarter to twelve p.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("twenty three forty five", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("twenty three forty five", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("11:45", pf.niceTime(dt).speech(false).get());
        assertEquals("11:45 PM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("23:45", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("23:45", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }

    @Test
    public void tenAm() {
        final LocalDateTime dt = LocalDateTime.of(2038, 9, 28, 10, 3, 44);
        assertEquals("ten oh three", pf.niceTime(dt).get());
        assertEquals("ten oh three a.m.", pf.niceTime(dt).showAmPm(true).get());
        assertEquals("ten zero three", pf.niceTime(dt).use24Hour(true).get());
        assertEquals("ten zero three", pf.niceTime(dt).use24Hour(true).showAmPm(true).get());
        assertEquals("10:03", pf.niceTime(dt).speech(false).get());
        assertEquals("10:03 AM", pf.niceTime(dt).speech(false).showAmPm(true).get());
        assertEquals("10:03", pf.niceTime(dt).speech(false).use24Hour(true).get());
        assertEquals("10:03", pf.niceTime(dt).speech(false).use24Hour(true).showAmPm(true).get());
    }
}
