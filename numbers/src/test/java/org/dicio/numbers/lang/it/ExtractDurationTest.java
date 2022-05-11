package org.dicio.numbers.lang.it;

import static org.dicio.numbers.test.TestUtils.DAY;
import static org.dicio.numbers.test.TestUtils.MONTH;
import static org.dicio.numbers.test.TestUtils.t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.dicio.numbers.NumberParserFormatter;
import org.dicio.numbers.lang.en.EnglishParser;
import org.dicio.numbers.test.WithTokenizerTestBase;
import org.junit.Test;

public class ExtractDurationTest extends WithTokenizerTestBase {
    @Override
    public String configFolder() {
        return "config/it-it";
    }

    @Test
    public void testNumberParserExtractDuration() {
        final NumberParserFormatter npf
                = new NumberParserFormatter(null, new ItalianParser());
        assertNull(npf.extractDuration("ciao come stai?").get());
        assertNull(npf.extractDuration("un miliardo di euro").shortScale(true).get());
        assertNull(npf.extractDuration("un milione").shortScale(false).get());
        assertEquals(t(DAY), npf.extractDuration("ventiquattro ore non sono due giorni").get().toJavaDuration());
        assertEquals(t(2 * DAY), npf.extractDuration("due giorni non sono ventiquattro ore").get().toJavaDuration());
        assertEquals(t(3 * MONTH + 2 * DAY), npf.extractDuration("tre mesi e due giorni").get().toJavaDuration());
    }
}
