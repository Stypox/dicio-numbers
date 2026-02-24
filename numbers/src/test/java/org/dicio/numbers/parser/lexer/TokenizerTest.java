package org.dicio.numbers.parser.lexer;

import org.dicio.numbers.test.WithTokenizerTestBase;
import org.dicio.numbers.unit.Number;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenizerTest extends WithTokenizerTestBase {

    @Override
    public String configFolder() {
        return "config/en-us";
    }


    private static void assertBaseToken(final Token token,
                                        final String value,
                                        final String spacesFollowing,
                                        final int positionInOriginalString) {
        assertEquals(value, token.value);
        assertEquals(spacesFollowing, token.spacesFollowing);
        assertEquals(positionInOriginalString, token.positionInOriginalString);
        assertTrue(token.isValue(value));
        assertFalse(token.isValue(spacesFollowing));
    }

    private static void assertToken(final Token token,
                                    final String value,
                                    final String spacesFollowing,
                                    final int positionInOriginalString) {
        assertEquals("token is not an unmatched Token: "
                + token.getClass().getSimpleName(), Token.class, token.getClass());
        assertBaseToken(token, value, spacesFollowing, positionInOriginalString);
    }

    private static void assertToken(final Token token,
                                    final String value,
                                    final String spacesFollowing,
                                    final int positionInOriginalString,
                                    final String[] categories,
                                    final String[] notCategories) {
        assertTrue("token is not a MatchedToken: " + token.getClass().getSimpleName(),
                token instanceof MatchedToken);
        assertBaseToken(token, value, spacesFollowing, positionInOriginalString);
        for (final String category : categories) {
            assertTrue("Missing category " + category, token.hasCategory(category));
        }
        for (final String category : notCategories) {
            assertFalse("Unexpected category " + category, token.hasCategory(category));
        }
    }

    private static void assertToken(final Token token,
                                    final String value,
                                    final String spacesFollowing,
                                    final int positionInOriginalString,
                                    final String[] categories,
                                    final String[] notCategories,
                                    final Number number) {
        assertTrue("token is not a NumberToken: " + token.getClass().getSimpleName(),
                token instanceof NumberToken);
        assertToken(token, value, spacesFollowing, positionInOriginalString, categories, notCategories);
        assertEquals(number, token.getNumber());
    }

    private static String[] cat(final String... categories) {
        return categories;
    }


    @Test
    public void everything() {
        final List<Token> tokens = tokenizer.tokenize("   Hello, twenTy second; plus 1928point \"half\" a-million");
        assertToken(tokens.get(0),  "",        "   ", 0);
        assertToken(tokens.get(1),  "Hello",   "",    3);
        assertToken(tokens.get(2),  ",",       " ",   8,  cat("ignore", "thousand_separator"), cat("point"));
        assertToken(tokens.get(3),  "twenTy",  " ",   10, cat("number", "tens"),               cat("ordinal"),    new Number(20));
        assertToken(tokens.get(4),  "second",  "; ",  17, cat("number", "ordinal", "digit"),   cat("tens"),       new Number(2));
        assertToken(tokens.get(5),  "plus",    " ",   25, cat("sign", "positive"),             cat("number"));
        assertToken(tokens.get(6),  "1928",    "",    30, cat("raw", "number"),                cat("digit"),      new Number(1928));
        assertToken(tokens.get(7),  "point",   " \"", 34, cat("point"),                        cat("ignore"));
        assertToken(tokens.get(8),  "half",    "\" ", 41, cat("number", "suffix_multiplier"),  cat("multiplier"), new Number(0.5));
        assertToken(tokens.get(9),  "a",       "",    47, cat("ignore"),                       cat("sign"));
        assertToken(tokens.get(10), "-",       "",    48, cat("ignore", "sign", "negative"),   cat("positive"));
        assertToken(tokens.get(11), "million", "",    49, cat("number", "multiplier"),         cat("tens"),       new Number(1000000));
    }

    @Test
    public void plurals() {
        final List<Token> tokens = tokenizer.tokenize(",heLLos plus twoS Fifths ");
        assertToken(tokens.get(0),  ",",       "",  0,  cat("ignore", "thousand_separator"), cat("point"));
        assertToken(tokens.get(1),  "heLLos",  " ", 1);
        assertToken(tokens.get(2),  "plus",    " ", 8,  cat("sign", "positive"),             cat("number"));
        assertToken(tokens.get(3),  "twoS",    " ", 13, cat("number", "digit"),              cat("ordinal"), new Number(2));
        assertToken(tokens.get(4),  "Fifths",  " ", 18, cat("number", "digit", "ordinal"),   cat("sign"),    new Number(5));
    }

    @Test
    public void accents() {
        final List<Token> tokens = tokenizer.tokenize("twò ThréèS çòÙplé mInùs");
        assertToken(tokens.get(0),  "twò",    " ", 0,  cat("number", "digit"),               cat("ordinal"),    new Number(2));
        assertToken(tokens.get(1),  "ThréèS", " ", 4,  cat("number", "digit"),               cat("sign"),       new Number(3));
        assertToken(tokens.get(2),  "çòÙplé", " ", 11, cat("number", "suffix_multiplier"),   cat("multiplier"), new Number(2));
        assertToken(tokens.get(3),  "mInùs",  "",  18, cat("sign", "negative"),              cat("number"));
    }

    @Test
    public void compound() {
        final List<Token> tokens = new Tokenizer("config/it-it").tokenize("z; centoventottesimo; z;");
        assertToken(tokens.get(0),  "z",        "; ", 0);
        assertToken(tokens.get(1),  "cento",    "",   3,  cat("number", "hundred", "compound_word_piece"),          cat("digit"),      new Number(100));
        assertToken(tokens.get(2),  "vent",     "",   8,  cat("number", "tens", "compound_word_piece"),             cat("ordinal"),    new Number(20));
        assertToken(tokens.get(3),  "ottesimo", "; ", 12, cat("number", "digit", "ordinal", "compound_word_piece"), cat("multiplier"), new Number(8));
        assertToken(tokens.get(4),  "z",        ";",  22);
    }

    @Test
    public void hugeNumbers() {
        final String doubleMax = "179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        final List<Token> tokens = tokenizer.tokenize(Long.MAX_VALUE + " 1" + Long.MAX_VALUE + " " +
                Long.MIN_VALUE + " " + (Long.MIN_VALUE + 1) + " " + Long.MIN_VALUE + "1 " +
                doubleMax + " 1" + doubleMax + " " + doubleMax.repeat(10));
        assertToken(tokens.get(0),  "9223372036854775807",  " ", 0,   cat("number", "raw"),    cat("digit"),  new Number(9223372036854775807L));
        assertToken(tokens.get(1),  "19223372036854775807", " ", 20,  cat("number", "raw"),    cat("digit"),  new Number(19223372036854775807.0));
        assertToken(tokens.get(2),  "-",                    "",  41,  cat("sign", "negative"), cat("number"));
        assertToken(tokens.get(3),  "9223372036854775808",  " ", 42,  cat("number", "raw"),    cat("digit"),  new Number(9223372036854775808.0));
        assertToken(tokens.get(4),  "-",                    "",  62,  cat("sign", "negative"), cat("number"));
        assertToken(tokens.get(5),  "9223372036854775807",  " ", 63,  cat("number", "raw"),    cat("digit"),  new Number(9223372036854775807L));
        assertToken(tokens.get(6),  "-",                    "",  83,  cat("sign", "negative"), cat("number"));
        assertToken(tokens.get(7),  "92233720368547758081", " ", 84,  cat("number", "raw"),    cat("digit"),  new Number(92233720368547758081.0));
        assertToken(tokens.get(8),  doubleMax,              " ", 105, cat("number", "raw"),    cat("digit"),  new Number(Double.MAX_VALUE));
        assertToken(tokens.get(9),  "1" + doubleMax,        " ", 415);
        assertToken(tokens.get(10), doubleMax.repeat(10),   "",  726);
    }
}
