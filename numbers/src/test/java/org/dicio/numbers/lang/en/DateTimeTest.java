package org.dicio.numbers.lang.en;

import org.dicio.numbers.NumberFormatter;
import org.dicio.numbers.config.DateTimeTestBase;

public class DateTimeTest extends DateTimeTestBase {
    @Override
    public String configFolder() {
        return "config/en-us";
    }

    @Override
    public NumberFormatter buildNumberFormatter() {
        return new EnglishFormatter();
    }
}
