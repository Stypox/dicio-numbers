package org.dicio.numbers.parser.lexer;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.dicio.numbers.util.Number;
import org.dicio.numbers.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tokenizer {

    private final String spaces;
    private final String charactersAsWord;
    private final Set<String> rawNumberCategories;

    private final Map<String, Set<String>> wordMatches;
    private final Map<String, Mapping> numberMappings;


    public Tokenizer(final String configFolder) {
        try {
            final JsonObject root = JsonParser.object().from(ClassLoader.getSystemClassLoader()
                    .getResourceAsStream(configFolder + "/tokenizer.json"));

            spaces = root.getString("spaces");
            charactersAsWord = root.getString("characters_as_word");
            rawNumberCategories = readCategories(root.getArray("raw_number_categories"));

            wordMatches = new HashMap<>();
            for (final Object o : root.getArray("word_matches")) {
                if (!(o instanceof JsonObject)) {
                    throw new RuntimeException("Content of word_matches array is not object: " + o);
                }

                final JsonObject match = (JsonObject) o;
                final Set<String> categories = readCategories(match.getArray("categories"));
                final JsonArray values = match.getArray("values");
                if (values == null) {
                    throw new RuntimeException("Missing values array in match: " + match);
                }

                for (final Object v : values) {
                    if (!(v instanceof String)) {
                        throw new RuntimeException("Content of values array is not string: " + v);
                    }
                    wordMatches.put((String) v, categories);
                }
            }

            numberMappings = new HashMap<>();
            for (final Object o : root.getArray("number_mappings")) {
                if (!(o instanceof JsonObject)) {
                    throw new RuntimeException(
                            "Content of number_mappings array is not object: " + o);
                }

                final JsonObject mapping = (JsonObject) o;
                final Set<String> categories = readCategories(mapping.getArray("categories"));

                final JsonObject values = mapping.getObject("values");
                if (values == null) {
                    throw new RuntimeException("Missing values object in mapping: " + mapping);
                }

                for (final Map.Entry<String, Object> v : values.entrySet()) {
                    if (v.getValue() instanceof Short || v.getValue() instanceof Integer
                            || v.getValue() instanceof Long) {
                        numberMappings.put(v.getKey(), new Mapping(categories,
                                new Number(((java.lang.Number) v.getValue()).longValue())));
                    } else if (v.getValue() instanceof Float || v.getValue() instanceof Double) {
                        numberMappings.put(v.getKey(), new Mapping(categories,
                                new Number(((java.lang.Number) v.getValue()).doubleValue())));
                    } else {
                        throw new RuntimeException("Content of values array is neither an integer "
                                + "nor a decimal number: " + v);
                    }
                }
            }

        } catch (final JsonParserException e) {
            throw new RuntimeException(e);
        }
    }


    public List<Token> tokenize(final String s) {
        final List<Token> tokens = new ArrayList<>();

        // the spaces at the beginning are put in a separate token
        int i = 0;
        while (i < s.length() && Utils.containsCodePoint(spaces, s.codePointAt(i))) {
            ++i;
        }
        if (i != 0) {
            tokens.add(new Token("", s.substring(0, i)));
        }

        int begin = i;
        while (i < s.length()) {
            // token values are either a 1-char-long string from the charactersAsWord array,
            // or another arbitrary string not containing any spaces
            boolean tokenIsDigits = false;
            while (i < s.length()) {
                if (Utils.containsCodePoint(spaces, s.codePointAt(i))) {
                    break; // current character is a space
                } else if (Utils.containsCodePoint(charactersAsWord, s.codePointAt(i))) {
                    if (i == begin) {
                        ++i; // found character to be considered as a separate word at the beginning
                    }
                    break; // current character is to be considered as a separate word, stop
                } else if (Character.isDigit(s.codePointAt(i))) {
                    if (i == begin) {
                        tokenIsDigits = true; // found a digit at the beginning, get others
                        ++i;
                        while (i < s.length() && Character.isDigit(s.codePointAt(i))) {
                            ++i; // collect as many digits as possible
                        }
                    }
                    break; // found a digit, stop
                }
                ++i;
            }
            final String value = s.substring(begin, i);
            begin = i;

            while (i < s.length() && Utils.containsCodePoint(spaces, s.codePointAt(i))) {
                ++i; // collect as many spaces as possible
            }
            final String spacesFollowing = s.substring(begin, i);
            begin = i;

            tokens.add(tokenFromValue(value, spacesFollowing, tokenIsDigits));
        }
        return tokens;
    }


    private Token tokenFromValue(final String value,
                                 final String spacesFollowing,
                                 final boolean tokenIsDigits) {
        if (tokenIsDigits) {
            return new NumberToken(value, spacesFollowing, rawNumberCategories,
                    new Number(Long.parseLong(value)));
        }

        final String clean = cleanValue(value);

        final Mapping mapping = numberMappings.get(clean);
        if (mapping != null) {
            return new NumberToken(value, spacesFollowing, mapping.categories, mapping.number);
        }

        final Set<String> match = wordMatches.get(clean);
        if (match != null) {
            return new MatchedToken(value, spacesFollowing, match);
        }

        return new Token(value, spacesFollowing);
    }

    private String cleanValue(final String value) {
        // TODO remove accents
        return value.toLowerCase();
    }


    private static Set<String> readCategories(final JsonArray categoriesArray) {
        if (categoriesArray == null) {
            throw new RuntimeException(
                    "Missing categories array in match, mapping or raw_number_categories");
        }

        final Set<String> categories = new HashSet<>();
        for (final Object o : categoriesArray) {
            if (!(o instanceof String)) {
                throw new RuntimeException("Content of categories array is not string: " + o);
            }
            categories.add((String) o);
        }

        return categories;
    }


    private static final class Mapping {
        final Set<String> categories;
        final Number number;

        private Mapping(final Set<String> categories, final Number number) {
            this.categories = categories;
            this.number = number;
        }
    }
}
