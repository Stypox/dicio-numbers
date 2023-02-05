package org.dicio.numbers.parser.lexer;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;
import org.dicio.numbers.util.ResourceOpener;
import org.dicio.numbers.util.Utils;

import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class Tokenizer {

    private static final Pattern DIACRITICAL_MARKS_REMOVER =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern AT_SPACES_SPLITTER = Pattern.compile(" ");


    private final String spaces;
    private final String charactersAsWord;
    private final Set<String> rawNumberCategories;
    private final List<String> pluralEndings;

    private final Map<String, Set<String>> wordMatches;
    private final Map<String, Mapping> numberMappings;
    private final List<String> compoundWordPieces;
    private final Map<String, DurationMapping> durationMappings;


    public Tokenizer(final String configFolder) {
        try {
            final JsonObject root = JsonParser.object().from(
                    ResourceOpener.getResourceAsStream(configFolder + "/tokenizer.json"));

            spaces = root.getString("spaces");
            charactersAsWord = root.getString("characters_as_word");

            final String compoundWordPieceCategory = root.getString("compound_word_piece_category", null);
            compoundWordPieces = new ArrayList<>();

            rawNumberCategories = readCategories(root.getArray("raw_number_categories"));

            pluralEndings = new ArrayList<>();
            for (final Object o : root.getArray("plural_endings")) {
                if (!(o instanceof String)) {
                    throw new RuntimeException(
                            "Content of plural_endings array is not string: " + o);
                }
                pluralEndings.add((String) o);
            }

            wordMatches = new HashMap<>();
            for (final Object o : root.getArray("word_matches")) {
                if (!(o instanceof JsonObject)) {
                    throw new RuntimeException("Content of word_matches array is not object: " + o);
                }

                final JsonObject match = (JsonObject) o;
                final Set<String> categories = readCategories(match.getArray("categories"));

                for (final Object v : match.getArray("values")) {
                    if (!(v instanceof String)) {
                        throw new RuntimeException("Content of values array is not string: " + v);
                    }
                    wordMatches.put((String) v, categories);

                    if (categories.contains(compoundWordPieceCategory)) {
                        compoundWordPieces.add((String) v);
                    }
                }
            }

            numberMappings = new HashMap<>();
            for (final Object o : root.getArray("number_mappings")) {
                if (!(o instanceof JsonObject)) {
                    throw new RuntimeException(
                            "Content of number_mappings array is not an object: " + o);
                }

                final JsonObject mapping = (JsonObject) o;
                final Set<String> categories = readCategories(mapping.getArray("categories"));

                final JsonObject values = mapping.getObject("values");
                if (values == null) {
                    throw new RuntimeException("Missing values object in mapping: " + mapping);
                }

                for (final Map.Entry<String, Object> v : values.entrySet()) {
                    numberMappings.put(v.getKey(),
                            new Mapping(categories, Number.fromObject(v.getValue())));

                    if (categories.contains(compoundWordPieceCategory)) {
                        compoundWordPieces.add(v.getKey());
                    }
                }
            }

            durationMappings = new HashMap<>();
            for (final Map.Entry<String, Object> o : root.getObject("duration_words").entrySet()) {
                final String[] parts = AT_SPACES_SPLITTER.split(o.getKey());
                if (parts.length != 2) {
                    throw new RuntimeException("Duration \"" + o.getKey() + "\" is not valid, it"
                            + " should be made of an integer number followed by a unit");
                }

                final Number multiplier;
                try {
                    multiplier = new Number(Integer.parseInt(parts[0]));
                } catch (final NumberFormatException e) {
                    throw new RuntimeException("Multiplier \"" + parts[0] + "\" of duration \""
                            + o.getKey() + "\" is not an integer", e);
                }

                final ChronoUnit chronoUnit;
                try {
                    chronoUnit = ChronoUnit.valueOf(parts[1]);
                } catch (final IllegalArgumentException e) {
                    throw new RuntimeException("Unit \"" + parts[1] + "\" of duration \""
                            + o.getKey() + "\" is not a valid unit; valid units are: "
                            + Arrays.toString(ChronoUnit.class.getEnumConstants()));
                }

                if (!(o.getValue() instanceof JsonArray)) {
                    throw new RuntimeException("Value corresponding to duration \"" + o.getKey()
                            + "\" is not an array: " + o.getValue());
                }
                for (final Object w : (JsonArray) o.getValue()) {
                    if (!(w instanceof String)) {
                        throw new RuntimeException("Entry in array for duration \"" + o.getKey() +
                                "\" is not a string: " + w);
                    }
                    // make sure to create a new DurationMapping object each time, since their
                    // restrictedAfterNumber value is changed in the for below
                    durationMappings.put((String) w, new DurationMapping(o.getKey(),
                            new Duration().plus(multiplier, chronoUnit)));
                }
            }
            for (final Object o : root.getArray("duration_restrict_after_number")) {
                if (!(o instanceof String) || !durationMappings.containsKey(o)) {
                    throw new RuntimeException("Found entry in duration_restrict_after_number array"
                            + " that was not in duration_words: " + o);
                }
                durationMappings.get(o).restrictedAfterNumber = true;
            }

        } catch (final FileNotFoundException | JsonParserException e) {
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
            boolean tokenIsDigits = false, valueNeedsCleaning = true;
            while (i < s.length()) {
                if (Utils.containsCodePoint(spaces, s.codePointAt(i))) {
                    break; // current character is a space
                } else if (Utils.containsCodePoint(charactersAsWord, s.codePointAt(i))) {
                    if (i == begin) {
                        valueNeedsCleaning = false; // do not normalize single characters
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

            addTokenFromValue(tokens, value, spacesFollowing, tokenIsDigits, valueNeedsCleaning);
        }
        return tokens;
    }


    private void addTokenFromValue(final List<Token> tokens,
                                   final String value,
                                   final String spacesFollowing,
                                   final boolean tokenIsDigits,
                                   final boolean valueNeedsCleaning) {
        if (tokenIsDigits) {
            tokens.add(new NumberToken(value, spacesFollowing, rawNumberCategories,
                    new Number(Long.parseLong(value))));
            return;
        }

        final String clean = valueNeedsCleaning ? cleanValue(value) : value;
        Token token = tokenFromValueExact(clean, value, spacesFollowing);
        if (token == null) {
            final String removedPluralEndings = removePluralEndings(clean);
            if (removedPluralEndings != null) {
                token = tokenFromValueExact(removedPluralEndings, value, spacesFollowing);
            }
        }

        if (token == null) {
            // try to parse compound word
            final List<Token> compoundWord = tokenizeCompoundWord(clean);
            if (compoundWord != null && !compoundWord.isEmpty()) {
                compoundWord.get(0).setSpacesFollowing(spacesFollowing);
                Collections.reverse(compoundWord); // results from tokenizeCompoundWord are reversed
                tokens.addAll(compoundWord);
                return;
            }
        }

        tokens.add(token == null ? new Token(value, spacesFollowing) : token);
    }

    private Token tokenFromValueExact(final String clean,
                                      final String value,
                                      final String spacesFollowing) {
        MatchedToken matchedToken = null;
        final Mapping mapping = numberMappings.get(clean);
        if (mapping == null) {
            final Set<String> wordMatch = wordMatches.get(clean);
            if (wordMatch != null) {
                matchedToken = new MatchedToken(value, spacesFollowing, wordMatch);
            }

        } else {
            matchedToken
                    = new NumberToken(value, spacesFollowing, mapping.categories, mapping.number);
        }

        final DurationMapping dur = durationMappings.get(clean);
        if (dur != null) {
            final DurationToken durationToken = new DurationToken(value, spacesFollowing,
                    dur.durationCategory, dur.durationMultiplier, dur.restrictedAfterNumber);
            if (matchedToken == null) {
                return durationToken;
            } else {
                matchedToken.setDurationTokenMatch(durationToken);
            }
        }

        return matchedToken;
    }

    private String removePluralEndings(final String value) {
        for (final String pluralEnding : pluralEndings) {
            if (value.endsWith(pluralEnding)) {
                return value.substring(0, value.length() - pluralEnding.length());
            }
        }
        return null;
    }

    private String cleanValue(final String value) {
        // nfkd normalize (i.e. remove accents) and make lowercase
        final String normalized = Normalizer.normalize(value.toLowerCase(), Normalizer.Form.NFKD);
        return DIACRITICAL_MARKS_REMOVER.matcher(normalized).replaceAll("");
    }

    /**
     * Tokenizes a compound word (e.g. twentytwo is parsed into two tokens: twewnty and two)
     * @param clean the clean word
     * @return a list of tokens in reverse order (e.g. [two, twenty] for input twentytwo)
     */
    private List<Token> tokenizeCompoundWord(final String clean) {
        if (clean.isEmpty()) {
            return new ArrayList<>();
        }

        for (final String compoundPiece : compoundWordPieces) {
            if (clean.startsWith(compoundPiece)) {
                final List<Token> nextTokens = tokenizeCompoundWord(clean.substring(compoundPiece.length()));
                if (nextTokens != null) {
                    nextTokens.add(tokenFromValueExact(compoundPiece, compoundPiece, ""));
                    return nextTokens; // will be in reverse order, since first matches are added last
                }
            }
        }

        return null;
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

    private static final class DurationMapping {
        final String durationCategory;
        final Duration durationMultiplier;
        boolean restrictedAfterNumber;

        private DurationMapping(final String durationCategory, final Duration durationMultiplier) {
            this.durationCategory = durationCategory;
            this.durationMultiplier = durationMultiplier;
            this.restrictedAfterNumber = false;
        }
    }
}
