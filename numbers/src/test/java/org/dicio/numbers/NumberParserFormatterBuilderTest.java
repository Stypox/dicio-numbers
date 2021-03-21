package org.dicio.numbers;

import org.junit.Test;

import java.util.Locale;

import static org.dicio.numbers.NumberParserFormatterBuilder.ParserFormatterPair;
import static org.dicio.numbers.NumberParserFormatterBuilder.parserFormatterPairForLocale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NumberParserFormatterBuilderTest {

    @Test
    public void parserFormatterPairForLocaleEnglish() {
        final ParserFormatterPair pfp = parserFormatterPairForLocale(Locale.ENGLISH);
        assertNotNull(pfp);
        assertNotNull(pfp.formatter);
        assertNotNull(pfp.parser);
    }

    @Test
    public void numberParserFormatterConstructorEnglish() {
        final NumberParserFormatter npf = new NumberParserFormatter(Locale.ENGLISH);
        final ParserFormatterPair pfp = parserFormatterPairForLocale(Locale.ENGLISH);

        assertEquals(pfp.formatter.pronounceNumber(42534, 0, true, false, false),
                npf.pronounceNumber(42534).places(0).shortScale(true).scientific(false).ordinal(false).get());
        assertEquals(pfp.parser.extractNumbers("hello first twenty four three point two", true, false),
                npf.extractNumbers("hello first twenty four three point two").shortScale(true).preferOrdinal(false).get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parserFormatterPairForLocaleUnsupported() {
        parserFormatterPairForLocale(Locale.ROOT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void numberParserFormatterConstructorUnsupported() {
        new NumberParserFormatter(Locale.ROOT);
    }
}
