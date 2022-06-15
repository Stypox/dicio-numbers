package org.dicio.numbers.lang.it;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.test.DateTimeTestBase;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class DateTimeTest extends DateTimeTestBase {

    @Override
    public String configFolder() {
        return "config/it-it";
    }

    @Override
    public Formatter buildNumberFormatter() {
        return new ItalianFormatter();
    }

    @Test
    public void testNiceDate() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("mercoledì, ventotto aprile, duemila ventuno",
                pf.niceDate(LocalDate.of(2021, 4, 28)).get());
        assertEquals("domenica, tredici agosto",
                pf.niceDate(LocalDate.of(-84, 8, 13)).now(LocalDate.of(-84, 8, 23)).get());
    }

    @Test
    public void testNiceYear() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("mille novecento ottantaquattro", pf.niceYear(LocalDate.of(1984, 4, 28)).get());
        assertEquals("ottocento dieci a.C.", pf.niceYear(LocalDate.of(-810, 8, 13)).get());
    }

    @Test
    public void testNiceDateTime() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("mercoledì, dodici settembre, mille settecento sessantaquattro alle mezzogiorno in punto", pf.niceDateTime(LocalDateTime.of(1764, 9, 12, 12, 0)).get());
        assertEquals("giovedì, tre novembre, trecento ventotto a.C. alle cinque e zero sette", pf.niceDateTime(LocalDateTime.of(-328, 11, 3, 5, 7)).get());
    }
}
