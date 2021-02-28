package org.dicio.numbers.formatter.datetime;

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
        assertFalse(config.bc.isEmpty());
        assertFalse(config.today.isEmpty());
        assertFalse(config.tomorrow.isEmpty());
        assertFalse(config.yesterday.isEmpty());
        assertFalse(config.dayWord.isEmpty());
        assertFalse(config.daysWord.isEmpty());
        assertFalse(config.hourWord.isEmpty());
        assertFalse(config.hoursWord.isEmpty());
        assertFalse(config.minuteWord.isEmpty());
        assertFalse(config.minutesWord.isEmpty());
        assertFalse(config.secondWord.isEmpty());
        assertFalse(config.secondsWord.isEmpty());
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
