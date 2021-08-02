package org.dicio.numbers.test;

import org.dicio.numbers.formatter.datetime.DateTimeConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class DateTimeConfigTestBase {

    protected DateTimeConfig config;

    public abstract String configFolder();

    @Before
    public void setup() {
        config = new DateTimeConfig(configFolder());
    }

    private void assertNotEmpty(final String string) {
        assertFalse("String is empty", string.isEmpty());
    }

    private void assertTrimmed(final String string) {
        assertEquals("String is not trimmed: \"" + string + "\"", string, string.trim());
    }

    @Test
    public void notNull() {
        assertNotNull(config.decadeFormat);
        assertNotNull(config.hundredFormat);
        assertNotNull(config.thousandFormat);
        assertNotNull(config.yearFormat);
        assertNotNull(config.bc);
        assertNotNull(config.dateFormatFull);
        assertNotNull(config.dateFormatFullNoYear);
        assertNotNull(config.dateFormatFullNoYearMonth);
        assertNotNull(config.dateTimeFormat);
        assertNotNull(config.today);
        assertNotNull(config.tomorrow);
        assertNotNull(config.yesterday);
        assertNotNull(config.weekdays);
        assertNotNull(config.days);
        assertNotNull(config.months);
        assertNotNull(config.numbers);
        assertNotNull(config.dayWord);
        assertNotNull(config.daysWord);
        assertNotNull(config.hourWord);
        assertNotNull(config.hoursWord);
        assertNotNull(config.minuteWord);
        assertNotNull(config.minutesWord);
        assertNotNull(config.secondWord);
        assertNotNull(config.secondsWord);
    }

    @Test
    public void empty() {
        assertNotEmpty(config.bc);
        assertNotEmpty(config.today);
        assertNotEmpty(config.tomorrow);
        assertNotEmpty(config.yesterday);
        assertNotEmpty(config.dayWord);
        assertNotEmpty(config.daysWord);
        assertNotEmpty(config.hourWord);
        assertNotEmpty(config.hoursWord);
        assertNotEmpty(config.minuteWord);
        assertNotEmpty(config.minutesWord);
        assertNotEmpty(config.secondWord);
        assertNotEmpty(config.secondsWord);
    }

    @Test
    public void trimmed() {
        assertTrimmed(config.bc);
        assertTrimmed(config.today);
        assertTrimmed(config.tomorrow);
        assertTrimmed(config.yesterday);
        assertTrimmed(config.dayWord);
        assertTrimmed(config.daysWord);
        assertTrimmed(config.hourWord);
        assertTrimmed(config.hoursWord);
        assertTrimmed(config.minuteWord);
        assertTrimmed(config.minutesWord);
        assertTrimmed(config.secondWord);
        assertTrimmed(config.secondsWord);
    }

    @Test
    public void correctLengths() {
        assertEquals(7, config.weekdays.length);
        assertEquals(31, config.days.length);
        assertEquals(12, config.months.length);
        assertTrue("numbers list size " + config.numbers.size() + " is not at least 20",
                config.numbers.size() > 20);
    }

    @Test
    public void numbers() {
        for (int i = 0; i < 20; ++i) {
            assertNotNull(config.numbers.get(i));
            assertFalse(config.numbers.get(i).isEmpty());
            assertNotEquals(String.valueOf(i), config.getNumber(i));
        }
    }
}
