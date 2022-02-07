package org.dicio.numbers.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.dicio.numbers.lang.en.EnglishFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;

import java.time.Duration;

public abstract class DurationExtractorUtilsTestBase extends WithTokenizerTestBase {

    public abstract Duration extractDuration(TokenStream ts, boolean shortScale);

    public void assertDuration(final String s,
                               final TokenStream ts,
                               final boolean shortScale,
                               final Duration expectedDuration) {
        final Duration duration = extractDuration(ts, shortScale);
        assertNotNull(duration);
        assertEquals("wrong duration in seconds for string \"" + s + "\": expected \""
                        + new EnglishFormatter().niceDuration(expectedDuration, true)
                        + "\" but got \"" + new EnglishFormatter().niceDuration(duration, true)
                        + "\"",
                expectedDuration, duration);
    }

    public void assertDuration(final String s,
                               final boolean shortScale,
                               final Duration expectedDuration) {
        assertDuration(s, new TokenStream(tokenizer.tokenize(s)), shortScale, expectedDuration);
    }

    public void assertNoDuration(final String s, final boolean shortScale) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Duration duration = extractDuration(ts, shortScale);
        assertNull(duration);
    }
}
