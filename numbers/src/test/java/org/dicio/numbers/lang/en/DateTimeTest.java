package org.dicio.numbers.lang.en;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.test.DateTimeTestBase;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class DateTimeTest extends DateTimeTestBase {

    @Override
    public String configFolder() {
        return "config/en-us";
    }

    @Override
    public Formatter buildNumberFormatter() {
        return new EnglishFormatter();
    }

    @Test
    public void testNiceDate() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("wednesday, april twenty-eighth, twenty twenty one",
                pf.niceDate(LocalDate.of(2021, 4, 28)).get());
        assertEquals("sunday, august thirteenth",
                pf.niceDate(LocalDate.of(-84, 8, 13)).now(LocalDate.of(-84, 8, 23)).get());
    }

    @Test
    public void testNiceYear() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("nineteen eighty four", pf.niceYear(LocalDate.of(1984, 4, 28)).get());
        assertEquals("eight hundred ten b.c.", pf.niceYear(LocalDate.of(-810, 8, 13)).get());
    }

    @Test
    public void testNiceDateTime() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("wednesday, september twelfth, seventeen sixty four at noon", pf.niceDateTime(LocalDateTime.of(1764, 9, 12, 12, 0)).get());
        assertEquals("thursday, november third, three hundred twenty eight b.c. at five oh seven", pf.niceDateTime(LocalDateTime.of(-328, 11, 3, 5, 7)).get());
    }
}
