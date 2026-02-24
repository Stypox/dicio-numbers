package org.dicio.numbers.lang.en;

import static org.dicio.numbers.test.TestUtils.DAY;
import static org.dicio.numbers.test.TestUtils.t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.test.WithTokenizerTestBase;
import org.junit.Test;

public class ExtractDurationTest extends WithTokenizerTestBase {
    @Override
    public String configFolder() {
        return "config/en-us";
    }

    @Test
    public void testNumberParserExtractDuration() {
        final ParserFormatter npf = new ParserFormatter(null, new EnglishParser());
        assertNull(npf.extractDuration("hello how are you").parseFirst());
        assertNull(npf.extractDuration("one billion euros").shortScale(true).parseFirst());
        assertNull(npf.extractDuration("a million").shortScale(false).parseFirst());
        assertEquals(t(DAY), npf.extractDuration("twenty four hours is not two days").parseFirst().toJavaDuration());
        assertEquals(t(2 * DAY), npf.extractDuration("two days are not twenty four hours").parseFirst().toJavaDuration());
    }
}
