package org.dicio.numbers.test;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.util.ResourceOpener;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class DateTimeTestBase {

    private JsonObject root;
    protected Formatter nf;
    protected ParserFormatter pf; // useful for overriding classes

    public abstract String configFolder();
    public abstract Formatter buildNumberFormatter();

    @Before
    public void setup() throws FileNotFoundException, JsonParserException {
        root = JsonParser.object().from(
                ResourceOpener.getResourceAsStream(configFolder() + "/date_time_test.json"));
        nf = buildNumberFormatter();
        pf = new ParserFormatter(nf, null);
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
