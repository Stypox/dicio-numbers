package org.dicio.numbers.datetime;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NiceYearSubstitutionTableBuilderTest {

    private static DateTimeConfig config;

    @BeforeClass
    public static void setup() {
        config = new DateTimeConfig("config/en-us/date_time.json");
    }

    private static void assertSubstitutionTableValue(final Map<String, String> substitutionTable,
                                                     final String key,
                                                     final String value) {
        assertEquals(value, substitutionTable.get(key));
    }

    private static void assertSame_x_in_x00_x_in_0x00(final int year) {
        final Map<String, String> st = NiceYearSubstitutionTableBuilder.build(config, year);
        assertEquals(st.get("x_in_x00"), st.get("x_in_0x00"));
    }

    @Test
    public void yearZero() {
        final Map<String, String> st = NiceYearSubstitutionTableBuilder.build(config, 0);
        assertSubstitutionTableValue(st, "x", "zero");
        assertSubstitutionTableValue(st, "xx", "zero");
        assertSubstitutionTableValue(st, "x0", "zero");
        assertSubstitutionTableValue(st, "x_in_x0", "zero");
        assertSubstitutionTableValue(st, "xxx", "zero");
        assertSubstitutionTableValue(st, "x00", "zero");
        assertSubstitutionTableValue(st, "x_in_x00", "zero");
        assertSubstitutionTableValue(st, "x_in_0x00", "zero");
        assertSubstitutionTableValue(st, "xx00", "zero");
        assertSubstitutionTableValue(st, "xx_in_xx00", "zero");
        assertSubstitutionTableValue(st, "x000", "zero");
        assertSubstitutionTableValue(st, "x_in_x000", "zero");
        assertSubstitutionTableValue(st, "x0_in_x000", "zero");
    }

    @Test
    public void oneDigitYear() {
        final Map<String, String> st = NiceYearSubstitutionTableBuilder.build(config, 6);
        assertSubstitutionTableValue(st, "x", "six");
        assertSubstitutionTableValue(st, "xx", "six");
        assertSubstitutionTableValue(st, "x0", "zero");
        assertSubstitutionTableValue(st, "x_in_x0", "zero");
        assertSubstitutionTableValue(st, "xxx", "six");
        assertSubstitutionTableValue(st, "x00", "zero");
        assertSubstitutionTableValue(st, "x_in_x00", "zero");
        assertSubstitutionTableValue(st, "x_in_0x00", "zero");
        assertSubstitutionTableValue(st, "xx00", "zero");
        assertSubstitutionTableValue(st, "xx_in_xx00", "zero");
        assertSubstitutionTableValue(st, "x000", "zero");
        assertSubstitutionTableValue(st, "x_in_x000", "zero");
        assertSubstitutionTableValue(st, "x0_in_x000", "zero");
    }

    @Test
    public void twoDigitYear() {
        final Map<String, String> st = NiceYearSubstitutionTableBuilder.build(config, 42);
        assertSubstitutionTableValue(st, "x", "two");
        assertSubstitutionTableValue(st, "xx", "42");
        assertSubstitutionTableValue(st, "x0", "forty");
        assertSubstitutionTableValue(st, "x_in_x0", "four");
        assertSubstitutionTableValue(st, "xxx", "42");
        assertSubstitutionTableValue(st, "x00", "zero");
        assertSubstitutionTableValue(st, "x_in_x00", "zero");
        assertSubstitutionTableValue(st, "x_in_0x00", "zero");
        assertSubstitutionTableValue(st, "xx00", "zero");
        assertSubstitutionTableValue(st, "xx_in_xx00", "zero");
        assertSubstitutionTableValue(st, "x000", "zero");
        assertSubstitutionTableValue(st, "x_in_x000", "zero");
        assertSubstitutionTableValue(st, "x0_in_x000", "zero");
    }

    @Test
    public void threeDigitYear() {
        final Map<String, String> st = NiceYearSubstitutionTableBuilder.build(config, 470);
        assertSubstitutionTableValue(st, "x", "zero");
        assertSubstitutionTableValue(st, "xx", "seventy");
        assertSubstitutionTableValue(st, "x0", "seventy");
        assertSubstitutionTableValue(st, "x_in_x0", "seven");
        assertSubstitutionTableValue(st, "xxx", "470");
        assertSubstitutionTableValue(st, "x00", "400");
        assertSubstitutionTableValue(st, "x_in_x00", "four");
        assertSubstitutionTableValue(st, "x_in_0x00", "four");
        assertSubstitutionTableValue(st, "xx00", "400");
        assertSubstitutionTableValue(st, "xx_in_xx00", "four");
        assertSubstitutionTableValue(st, "x000", "zero");
        assertSubstitutionTableValue(st, "x_in_x000", "zero");
        assertSubstitutionTableValue(st, "x0_in_x000", "zero");
    }

    @Test
    public void fourDigitYear() {
        final Map<String, String> st = NiceYearSubstitutionTableBuilder.build(config, 1983);
        assertSubstitutionTableValue(st, "x", "three");
        assertSubstitutionTableValue(st, "xx", "83");
        assertSubstitutionTableValue(st, "x0", "eighty");
        assertSubstitutionTableValue(st, "x_in_x0", "eight");
        assertSubstitutionTableValue(st, "xxx", "983");
        assertSubstitutionTableValue(st, "x00", "900");
        assertSubstitutionTableValue(st, "x_in_x00", "nine");
        assertSubstitutionTableValue(st, "x_in_0x00", "nine");
        assertSubstitutionTableValue(st, "xx00", "1900");
        assertSubstitutionTableValue(st, "xx_in_xx00", "nineteen");
        assertSubstitutionTableValue(st, "x000", "1000");
        assertSubstitutionTableValue(st, "x_in_x000", "one");
        assertSubstitutionTableValue(st, "x0_in_x000", "ten");
    }

    @Test
    public void fiveDigitYear() {
        final Map<String, String> st = NiceYearSubstitutionTableBuilder.build(config, 29809);
        assertSubstitutionTableValue(st, "x", "nine");
        assertSubstitutionTableValue(st, "xx", "nine");
        assertSubstitutionTableValue(st, "x0", "zero");
        assertSubstitutionTableValue(st, "x_in_x0", "zero");
        assertSubstitutionTableValue(st, "xxx", "809");
        assertSubstitutionTableValue(st, "x00", "800");
        assertSubstitutionTableValue(st, "x_in_x00", "eight");
        assertSubstitutionTableValue(st, "x_in_0x00", "eight");
        assertSubstitutionTableValue(st, "xx00", "9800");
        assertSubstitutionTableValue(st, "xx_in_xx00", "98");
        assertSubstitutionTableValue(st, "x000", "9000");
        assertSubstitutionTableValue(st, "x_in_x000", "nine");
        assertSubstitutionTableValue(st, "x0_in_x000", "ninety");
    }

    @Test
    public void bcYear() {
        final Map<String, String> st = NiceYearSubstitutionTableBuilder.build(config, -2021);
        assertSubstitutionTableValue(st, "x", "one");
        assertSubstitutionTableValue(st, "xx", "21");
        assertSubstitutionTableValue(st, "x0", "twenty");
        assertSubstitutionTableValue(st, "x_in_x0", "two");
        assertSubstitutionTableValue(st, "xxx", "21");
        assertSubstitutionTableValue(st, "x00", "zero");
        assertSubstitutionTableValue(st, "x_in_x00", "zero");
        assertSubstitutionTableValue(st, "x_in_0x00", "zero");
        assertSubstitutionTableValue(st, "xx00", "2000");
        assertSubstitutionTableValue(st, "xx_in_xx00", "twenty");
        assertSubstitutionTableValue(st, "x000", "2000");
        assertSubstitutionTableValue(st, "x_in_x000", "two");
        assertSubstitutionTableValue(st, "x0_in_x000", "twenty");
    }

    @Test
    public void x_in_x00_shouldEqual_x_in_0x00() {
        assertSame_x_in_x00_x_in_0x00(0);
        assertSame_x_in_x00_x_in_0x00(2);
        assertSame_x_in_x00_x_in_0x00(65);
        assertSame_x_in_x00_x_in_0x00(327);
        assertSame_x_in_x00_x_in_0x00(4283);
        assertSame_x_in_x00_x_in_0x00(93578);
        assertSame_x_in_x00_x_in_0x00(-5);
        assertSame_x_in_x00_x_in_0x00(-23);
        assertSame_x_in_x00_x_in_0x00(-987);
        assertSame_x_in_x00_x_in_0x00(-2355);
        assertSame_x_in_x00_x_in_0x00(-64435);
    }
}
