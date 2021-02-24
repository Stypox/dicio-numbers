package org.dicio.numbers.lang.en;

import org.dicio.numbers.NumberFormatter;
import org.dicio.numbers.NumberParserFormatter;
import org.dicio.numbers.datetime.DateTimeTestBase;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class DateTimeTest extends DateTimeTestBase {

    private static NumberParserFormatter pf;

    @Override
    public String configFolder() {
        return "config/en-us";
    }

    @Override
    public NumberFormatter buildNumberFormatter() {
        final NumberFormatter numberFormatter = new EnglishFormatter();
        pf = new NumberParserFormatter(numberFormatter, null);
        return numberFormatter;
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
}
