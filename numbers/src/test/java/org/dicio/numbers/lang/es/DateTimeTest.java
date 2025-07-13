package org.dicio.numbers.lang.es;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.test.DateTimeTestBase;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class DateTimeTest extends DateTimeTestBase {

    @Override
    public String configFolder() {
        return "config/es-es";
    }

    @Override
    public Formatter buildNumberFormatter() {
        return new SpanishFormatter();
    }

    @Test
    public void testNiceDate() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("jueves, abril veintiocho, dos mil veintiuno",
                pf.niceDate(LocalDate.of(2021, 4, 28)).get());
        assertEquals("domingo, agosto trece",
                pf.niceDate(LocalDate.of(-84, 8, 13)).now(LocalDate.of(-84, 8, 23)).get());
    }

    @Test
    public void testNiceYear() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("mil novecientos ochenta y cuatro", pf.niceYear(LocalDate.of(1984, 4, 28)).get());
        assertEquals("ochocientos diez a.C.", pf.niceYear(LocalDate.of(-810, 8, 13)).get());
    }

    @Test
    public void testNiceDateTime() {
        // just check that the NumberParserFormatter functions do their job
        assertEquals("miércoles, veintiuno de septiembre, mil setiesientos sesenta y cuatro al mediodía", pf.niceDateTime(LocalDateTime.of(1764, 9, 12, 12, 0)).get());
        assertEquals("jueves, tres de noviembre, trescientos veintiocho a.C. a las ocho y siete", pf.niceDateTime(LocalDateTime.of(-328, 11, 3, 5, 7)).get());
    }
}
