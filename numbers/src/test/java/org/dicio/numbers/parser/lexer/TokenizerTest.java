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
                                        final String spacesFollowing) {
        assertEquals(value, token.getValue());
        assertEquals(spacesFollowing, token.getSpacesFollowing());
        assertTrue(token.isValue(value));
        assertFalse(token.isValue(spacesFollowing));
    }

    private static void assertToken(final Token token,
                                    final String value,
                                    final String spacesFollowing) {
        assertEquals("token is not an unmatched Token: "
                + token.getClass().getSimpleName(), token.getClass(), Token.class);
        assertBaseToken(token, value, spacesFollowing);
    }

    private static void assertToken(final Token token,
                                    final String value,
                                    final String spacesFollowing,
                                    final String[] categories,
                                    final String[] notCategories) {
        assertTrue("token is not a MatchedToken: " + token.getClass().getSimpleName(),
                token instanceof MatchedToken);
        assertBaseToken(token, value, spacesFollowing);
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
                                    final String[] categories,
                                    final String[] notCategories,
                                    final Number number) {
        assertTrue("token is not a NumberToken: " + token.getClass().getSimpleName(),
                token instanceof NumberToken);
        assertToken(token, value, spacesFollowing, categories, notCategories);
        assertEquals(number, token.getNumber());
    }

    private static String[] cat(final String... categories) {
        return categories;
    }


    @Test
    public void everything() {
        final List<Token> tokens = tokenizer.tokenize("   Hello, twenTy second; plus 1928point \"half\" a-million");
        assertToken(tokens.get(0),  "",        "   ");
        assertToken(tokens.get(1),  "Hello",   "");
        assertToken(tokens.get(2),  ",",       " ",   cat("ignore", "thousand_separator"), cat("point"));
        assertToken(tokens.get(3),  "twenTy",  " ",   cat("number", "tens"),               cat("ordinal"),    new Number(20));
        assertToken(tokens.get(4),  "second",  "; ",  cat("number", "ordinal", "digit"),   cat("tens"),       new Number(2));
        assertToken(tokens.get(5),  "plus",    " ",   cat("sign", "positive"),             cat("number"));
        assertToken(tokens.get(6),  "1928",    "",    cat("raw", "number"),                cat("digit"),      new Number(1928));
        assertToken(tokens.get(7),  "point",   " \"", cat("point"),                        cat("ignore"));
        assertToken(tokens.get(8),  "half",    "\" ", cat("number", "suffix_multiplier"),  cat("multiplier"), new Number(0.5));
        assertToken(tokens.get(9),  "a",       "",    cat("ignore"),                       cat("sign"));
        assertToken(tokens.get(10), "-",       "",    cat("ignore", "sign", "negative"),   cat("positive"));
        assertToken(tokens.get(11), "million", "",    cat("number", "multiplier"),         cat("tens"),       new Number(1000000));
    }

    @Test
    public void plurals() {
        final List<Token> tokens = tokenizer.tokenize(",heLLos plus twoS Fifths ");
        assertToken(tokens.get(0),  ",",       "",  cat("ignore", "thousand_separator"), cat("point"));
        assertToken(tokens.get(1),  "heLLos",  " ");
        assertToken(tokens.get(2),  "plus",    " ", cat("sign", "positive"),             cat("number"));
        assertToken(tokens.get(3),  "twoS",    " ", cat("number", "digit"),              cat("ordinal"), new Number(2));
        assertToken(tokens.get(4),  "Fifths",  " ", cat("number", "digit", "ordinal"),   cat("sign"),    new Number(5));
    }

    @Test
    public void accents() {
        final List<Token> tokens = tokenizer.tokenize("twò ThréèS çòÙplé mInùs");
        assertToken(tokens.get(0),  "twò",    " ", cat("number", "digit"),               cat("ordinal"),    new Number(2));
        assertToken(tokens.get(1),  "ThréèS", " ", cat("number", "digit"),               cat("sign"),       new Number(3));
        assertToken(tokens.get(2),  "çòÙplé", " ", cat("number", "suffix_multiplier"),   cat("multiplier"), new Number(2));
        assertToken(tokens.get(3),  "mInùs",  "",  cat("sign", "negative"),              cat("number"));
    }
}
