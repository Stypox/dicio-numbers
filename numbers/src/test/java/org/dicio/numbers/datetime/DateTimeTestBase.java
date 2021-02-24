package org.dicio.numbers.datetime;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.dicio.numbers.NumberFormatter;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class DateTimeTestBase {

    private JsonObject root;
    protected NumberFormatter nf;

    public abstract String configFolder();
    public abstract NumberFormatter buildNumberFormatter();

    @Before
    public void setup() throws JsonParserException {
        root = JsonParser.object().from(ClassLoader.getSystemClassLoader()
                .getResourceAsStream(configFolder() + "/date_time_test.json"));
        nf = buildNumberFormatter();
    }

    @Test
    public void testConfigLoadedCorrectly() {
        final DateTimeConfig config = new DateTimeConfig(configFolder());

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

        assertEquals(7, config.weekdays.length);
        assertEquals(31, config.days.length);
        assertEquals(12, config.months.length);
        assertTrue("numbers list size " + config.numbers.size() + " is not at least 20",
                config.numbers.size() > 20);

        for (int i = 0; i < 20; ++i) {
            assertNotNull(config.numbers.get(i));
            assertFalse(config.numbers.get(i).isEmpty());
            assertNotEquals(String.valueOf(i), config.getNumber(i));
        }
    }

    @Test
    public void testNiceYear() {
        final JsonObject tests = root.getObject("test_nice_year");
        for (int i = 1; tests.has(String.valueOf(i)); ++i) {
            final JsonObject test = tests.getObject(String.valueOf(i));
            assertEquals("test " + i + " fails", test.getString("assertEqual"), nf.niceYear(
                    Objects.requireNonNull(localDateFrom(test.getString("datetime_param"),
                            "True".equals(test.getString("bc"))))));
        }
    }

    @Test
    public void testNiceDate() {
        final JsonObject tests = root.getObject("test_nice_date");
        for (int i = 1; tests.has(String.valueOf(i)); ++i) {
            final JsonObject test = tests.getObject(String.valueOf(i));
            assertEquals("test " + i + " fails", test.getString("assertEqual"), nf.niceDate(
                    localDateFrom(test.getString("datetime_param"), false),
                    localDateFrom(test.getString("now"), false)));
        }
    }

    @Test
    public void testNiceDateTime() {
        final JsonObject tests = root.getObject("test_nice_date_time");
        for (int i = 1; tests.has(String.valueOf(i)); ++i) {
            final JsonObject test = tests.getObject(String.valueOf(i));
            final LocalDateTime dateTimeParam
                    = localDateTimeFrom(test.getString("datetime_param"), false);
            assertNotNull("The json test file did not provide date time param", dateTimeParam);

            assertEquals("test " + i + " fails", test.getString("assertEqual"), nf.niceDateTime(
                    dateTimeParam.toLocalDate(),
                    localDateFrom(test.getString("now"), false),
                    dateTimeParam.toLocalTime(),
                    "True".equals(test.getString("use_24hour")),
                    "True".equals(test.getString("use_ampm"))));
        }
    }

    private LocalDate localDateFrom(final String string, final boolean bc) {
        final LocalDateTime localDateTime = localDateTimeFrom(string, bc);
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toLocalDate();
    }

    private LocalDateTime localDateTimeFrom(final String string, final boolean bc) {
        if (string.equals("None")) {
            return null;
        }

        final String[] dateTimeParts = string.split(", ");
        return LocalDateTime.of(
                Integer.parseInt(dateTimeParts[0]) * (bc ? -1 : 1),
                Integer.parseInt(dateTimeParts[1]),
                Integer.parseInt(dateTimeParts[2]),
                Integer.parseInt(dateTimeParts[3]),
                Integer.parseInt(dateTimeParts[4]),
                Integer.parseInt(dateTimeParts[5]));
    }
}
