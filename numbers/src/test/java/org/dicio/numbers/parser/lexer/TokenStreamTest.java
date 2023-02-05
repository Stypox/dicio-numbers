package org.dicio.numbers.parser.lexer;

import static org.dicio.numbers.test.TestUtils.n;
import static org.dicio.numbers.test.TestUtils.t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class TokenStreamTest {
    private static final List<Token> TOKENS = Arrays.asList(
            new Token("", " "),
            new MatchedToken("a", "\n ", Collections.singleton("ignore")),
            new DurationToken("ms", "", "", t(1, ChronoUnit.MILLIS), false)
    );

    private static final List<Token> TOKENS_IGNORES = Arrays.asList(
            new MatchedToken("hello", "", Collections.singleton("ignore")),
            new Token(",", " "),
            new MatchedToken("HOW", "\n", new HashSet<>(Arrays.asList("ignore", "other"))),
            new NumberToken("is", " \t", new HashSet<>(Arrays.asList("ignore", "another")), n(5)),
            new NumberToken("2022", " ", Collections.emptySet(), n(2022)),
            new MatchedToken("going", " ", Collections.singleton("test")),
            new MatchedToken("?", " ", Collections.singleton("ignore")),
            new DurationToken("s", "", "", t(1, ChronoUnit.SECONDS), true),
            new NumberToken("?", " ", Collections.singleton("ignore"), n(-1))
    );

    @Test
    public void testPositionHandling() {
        final TokenStream ts = new TokenStream(TOKENS);

        assertEquals(0, ts.getPosition());
        assertFalse(ts.finished());

        ts.movePositionForwardBy(2);
        assertEquals(2, ts.getPosition());
        assertFalse(ts.finished());

        ts.movePositionForwardBy(-1);
        assertEquals(1, ts.getPosition());
        assertFalse(ts.finished());

        ts.movePositionForwardBy(2);
        assertEquals(3, ts.getPosition());
        assertTrue(ts.finished());

        ts.setPosition(-5);
        assertEquals(-5, ts.getPosition());
        assertFalse(ts.finished());
    }

    @Test
    public void testGet() {
        final TokenStream ts = new TokenStream(TOKENS);
        assertSame(Token.emptyToken(), ts.get(-1));
        assertSame(TOKENS.get(0), ts.get(0));

        ts.movePositionForwardBy(1);
        assertSame(TOKENS.get(0), ts.get(-1));
        assertSame(TOKENS.get(1), ts.get(0));

        ts.setPosition(TOKENS.size());
        assertSame(TOKENS.get(2), ts.get(-1));
        assertSame(Token.emptyToken(), ts.get(0));
    }

    @Test
    public void testIndexOfWithoutCategory() {
        final TokenStream ts = new TokenStream(TOKENS_IGNORES);
        final int[] results = {2, 1, 0, 2, 1, 0, 0, 1, 0, 1, 0}; // from -1 to tokens.size()
        assertEquals(TOKENS_IGNORES.size() + 2, results.length);

        // make sure that `results` contains the index delta from each index to the next not ignore
        for (int j = 0; j < results.length; ++j) {
            ts.setPosition(j - 1);
            assertEquals(
                    "Wrong value when j=" + j,
                    results[j],
                    ts.indexOfWithoutCategory("ignore", 0));
        }

        // test all token stream positions and aheadBy offsets from -1 to tokens.size()
        for (int i = 0; i < results.length; ++i) {
            ts.setPosition(i - 1);
            for (int j = 0; j < results.length; ++j) {
                assertEquals(j, ts.getPosition() + j - i + 1);
                assertEquals(
                        "Wrong value when i=" + i + " and j=" + j,
                        results[j] + j - i,
                        ts.indexOfWithoutCategory("ignore", j - i));
            }
        }
    }
}
