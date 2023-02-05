package org.dicio.numbers.lang.it;

import static org.dicio.numbers.test.TestUtils.DAY;
import static org.dicio.numbers.test.TestUtils.MONTH;
import static org.dicio.numbers.test.TestUtils.t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.test.WithTokenizerTestBase;
import org.junit.Test;

public class ExtractDurationTest extends WithTokenizerTestBase {
    @Override
    public String configFolder() {
        return "config/it-it";
    }

    @Test
    public void testNumberParserExtractDuration() {
        final ParserFormatter npf = new ParserFormatter(null, new ItalianParser());
        assertNull(npf.extractDuration("ciao come stai?").getFirst());
        assertNull(npf.extractDuration("un miliardo di euro").shortScale(true).getFirst());
        assertNull(npf.extractDuration("un milione").shortScale(false).getFirst());
        assertEquals(t(DAY), npf.extractDuration("ventiquattro ore non sono due giorni").getFirst().toJavaDuration());
        assertEquals(t(2 * DAY), npf.extractDuration("due giorni non sono ventiquattro ore").getFirst().toJavaDuration());
        assertEquals(t(3 * MONTH + 2 * DAY), npf.extractDuration("tre mesi e due giorni").getFirst().toJavaDuration());
    }
}
