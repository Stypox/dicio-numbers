package org.dicio.numbers;

import org.junit.Test;

import java.util.Locale;

import static org.dicio.numbers.ParserFormatterBuilder.ParserFormatterPair;
import static org.dicio.numbers.ParserFormatterBuilder.parserFormatterPairForLocale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParserFormatterBuilderTest {

    @Test
    public void parserFormatterPairForLocaleEnglish() {
        final ParserFormatterPair pfp = parserFormatterPairForLocale(Locale.ENGLISH);
        assertNotNull(pfp);
        assertNotNull(pfp.formatter);
        assertNotNull(pfp.parser);
    }

    @Test
    public void numberParserFormatterConstructorEnglish() {
        final ParserFormatter npf = new ParserFormatter(Locale.ENGLISH);
        final ParserFormatterPair pfp = parserFormatterPairForLocale(Locale.ENGLISH);

        assertEquals(pfp.formatter.pronounceNumber(42534, 0, true, false, false),
                npf.pronounceNumber(42534).places(0).shortScale(true).scientific(false).ordinal(false).get());
        assertEquals(pfp.parser.extractNumber(pfp.parser.tokenize("first twenty four three point two"), true, false).get(),
                npf.extractNumber("hello first twenty four three point two").shortScale(true).preferOrdinal(false).getFirst());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parserFormatterPairForLocaleUnsupported() {
        parserFormatterPairForLocale(Locale.ROOT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void numberParserFormatterConstructorUnsupported() {
        new ParserFormatter(Locale.ROOT);
    }
}
