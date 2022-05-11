package org.dicio.numbers.test;

import static org.dicio.numbers.test.TestUtils.niceDuration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.dicio.numbers.lang.en.EnglishFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;

public abstract class DurationExtractorUtilsTestBase extends WithTokenizerTestBase {

    public abstract org.dicio.numbers.unit.Duration extractDuration(TokenStream ts, boolean shortScale);

    public void assertDuration(final String s,
                               final TokenStream ts,
                               final boolean shortScale,
                               final java.time.Duration expectedDuration) {
        final Duration duration = extractDuration(ts, shortScale);
        assertNotNull(duration);
        assertEquals("wrong duration for string \"" + s + "\": expected \""
                        + niceDuration(expectedDuration) + "\" but got \""
                        + niceDuration(duration) + "\"",
                expectedDuration, duration.toJavaDuration());
    }

    public void assertDuration(final String s,
                               final boolean shortScale,
                               final java.time.Duration expectedDuration) {
        assertDuration(s, new TokenStream(tokenizer.tokenize(s)), shortScale, expectedDuration);
    }

    public void assertNoDuration(final String s, final boolean shortScale) {
        final TokenStream ts = new TokenStream(tokenizer.tokenize(s));
        final Duration duration = extractDuration(ts, shortScale);
        assertNull(duration);
    }
}
