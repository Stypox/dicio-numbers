@file:Suppress("ReplaceSubstringWithDropLast", "ReplaceSubstringWithTake") // substring() is cleaner

package org.dicio.numbers.parser.lexer

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.dicio.numbers.unit.Duration
import org.dicio.numbers.unit.Number
import org.dicio.numbers.util.ResourceOpener
import org.dicio.numbers.util.Utils
import java.io.FileNotFoundException
import java.text.Normalizer
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.regex.Pattern

class Tokenizer(configFolder: String) {
    private val spaces: String
    private val charactersAsWord: String
    private val rawNumberCategories: Set<String>
    private val pluralEndings: MutableList<String>

    private val wordMatches: MutableMap<String, Set<String>>
    private val numberMappings: MutableMap<String, Mapping>
    private val compoundWordPieces: MutableList<String>
    private val durationMappings: MutableMap<String, DurationMapping>


    init {
        try {
            val root = JsonParser.`object`().from(
                ResourceOpener.getResourceAsStream("$configFolder/tokenizer.json")
            )

            spaces = root.getString("spaces")
            charactersAsWord = root.getString("characters_as_word")

            val compoundWordPieceCategory = root.getString("compound_word_piece_category", null)
            compoundWordPieces = ArrayList()

            rawNumberCategories = readCategories(root.getArray("raw_number_categories"))

            pluralEndings = ArrayList()
            for (o in root.getArray("plural_endings")) {
                if (o !is String) {
                    throw RuntimeException(
                        "Content of plural_endings array is not string: $o"
                    )
                }
                pluralEndings.add(o)
            }

            wordMatches = HashMap()
            for (match in root.getArray("word_matches")) {
                if (match !is JsonObject) {
                    throw RuntimeException("Content of word_matches array is not object: $match")
                }

                val categories = readCategories(match.getArray("categories"))

                for (v in match.getArray("values")) {
                    if (v !is String) {
                        throw RuntimeException("Content of values array is not string: $v")
                    }
                    wordMatches[v] = categories

                    if (categories.contains(compoundWordPieceCategory)) {
                        compoundWordPieces.add(v)
                    }
                }
            }

            numberMappings = HashMap()
            for (mapping in root.getArray("number_mappings")) {
                if (mapping !is JsonObject) {
                    throw RuntimeException(
                        "Content of number_mappings array is not an object: $mapping"
                    )
                }

                val categories = readCategories(mapping.getArray("categories"))
                val values = mapping.getObject("values")
                    ?: throw RuntimeException("Missing values object in mapping: $mapping")

                for ((key, value) in values) {
                    numberMappings[key] = Mapping(categories, Number.fromObject(value))
                    if (categories.contains(compoundWordPieceCategory)) {
                        compoundWordPieces.add(key)
                    }
                }
            }

            durationMappings = HashMap()
            for ((key, value) in root.getObject("duration_words")) {
                val parts = AT_SPACES_SPLITTER.split(key)
                if (parts.size != 2) {
                    throw RuntimeException(
                        "Duration \"" + key + "\" is not valid, it"
                                + " should be made of an integer number followed by a unit"
                    )
                }

                val multiplier = try {
                    Number(parts[0].toLong())
                } catch (e: NumberFormatException) {
                    throw RuntimeException(
                        "Multiplier \"" + parts[0] + "\" of duration \""
                                + key + "\" is not an integer", e
                    )
                }

                val chronoUnit = try {
                    ChronoUnit.valueOf(parts[1])
                } catch (e: IllegalArgumentException) {
                    throw RuntimeException(
                        "Unit \"" + parts[1] + "\" of duration \""
                                + key + "\" is not a valid unit; valid units are: "
                                + ChronoUnit::class.java.enumConstants.contentToString(),
                        e
                    )
                }

                if (value !is JsonArray) {
                    throw RuntimeException(
                        "Value corresponding to duration \"" + key
                                + "\" is not an array: " + value
                    )
                }
                for (w in value) {
                    if (w !is String) {
                        throw RuntimeException(
                            "Entry in array for duration \"" + key +
                                    "\" is not a string: " + w
                        )
                    }
                    // make sure to create a new DurationMapping object each time, since their
                    // restrictedAfterNumber value is changed in the for below
                    durationMappings[w] = DurationMapping(
                        key,
                        Duration().plus(multiplier, chronoUnit)
                    )
                }
            }
            for (o in root.getArray("duration_restrict_after_number")) {
                if (o !is String || !durationMappings.containsKey(o)) {
                    throw RuntimeException(
                        "Found entry in duration_restrict_after_number array"
                                + " that was not in duration_words: " + o
                    )
                }
                durationMappings[o]!!.restrictedAfterNumber = true
            }
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        } catch (e: JsonParserException) {
            throw RuntimeException(e)
        }
    }


    fun tokenize(s: String): List<Token> {
        val tokens: MutableList<Token> = ArrayList()

        // the spaces at the beginning are put in a separate token
        var i = 0
        while (i < s.length && Utils.containsCodePoint(spaces, s.codePointAt(i))) {
            ++i
        }
        if (i != 0) {
            tokens.add(Token("", s.substring(0, i), 0))
        }

        var begin = i
        while (i < s.length) {
            // token values are either a 1-char-long string from the charactersAsWord array,
            // or another arbitrary string not containing any spaces
            var tokenIsDigits = false
            var valueNeedsCleaning = true
            while (i < s.length) {
                if (Utils.containsCodePoint(spaces, s.codePointAt(i))) {
                    break // current character is a space
                } else if (Utils.containsCodePoint(charactersAsWord, s.codePointAt(i))) {
                    if (i == begin) {
                        valueNeedsCleaning = false // do not normalize single characters
                        ++i // found character to be considered as a separate word at the beginning
                    }
                    break // current character is to be considered as a separate word, stop
                } else if (Character.isDigit(s.codePointAt(i))) {
                    if (i == begin) {
                        tokenIsDigits = true // found a digit at the beginning, get others
                        ++i
                        while (i < s.length && Character.isDigit(s.codePointAt(i))) {
                            ++i // collect as many digits as possible
                        }
                    }
                    break // found a digit, stop
                }
                ++i
            }
            val positionInOriginalString = begin
            val value = s.substring(begin, i)
            begin = i

            while (i < s.length && Utils.containsCodePoint(spaces, s.codePointAt(i))) {
                ++i // collect as many spaces as possible
            }
            val spacesFollowing = s.substring(begin, i)
            begin = i

            addTokenFromValue(tokens, value, positionInOriginalString, spacesFollowing, tokenIsDigits, valueNeedsCleaning)
        }
        return tokens
    }


    private fun addTokenFromValue(
        tokens: MutableList<Token>,
        value: String,
        positionInOriginalString: Int,
        spacesFollowing: String,
        tokenIsDigits: Boolean,
        valueNeedsCleaning: Boolean
    ) {
        if (tokenIsDigits) {
            // `value` might be too big to fit in a Long, in that case first try parsing it as
            // Double, and if even that does not work, don't consider this a number
            val number = runCatching { Number(value.toLong()) }.getOrNull()
                ?: Number(value.toDouble()).takeIf { it.decimalValue().isFinite() }
            if (number != null) {
                tokens.add(NumberToken(value, spacesFollowing, positionInOriginalString,
                                       rawNumberCategories, number))
                return
            }
        }

        val clean = if (valueNeedsCleaning) cleanValue(value) else value
        var token = tokenFromValueExact(clean, value, positionInOriginalString, spacesFollowing)
        if (token == null) {
            val removedPluralEndings = removePluralEndings(clean)
            if (removedPluralEndings != null) {
                token = tokenFromValueExact(removedPluralEndings, value, positionInOriginalString, spacesFollowing)
            }
        }

        if (token == null) {
            // try to parse compound word
            val compoundWord: List<Token>? = tokenizeCompoundWord(clean, spacesFollowing, positionInOriginalString)
            if (!compoundWord.isNullOrEmpty()) {
                // results from tokenizeCompoundWord are reversed
                tokens.addAll(compoundWord.reversed())
                return
            }
        }

        tokens.add(token ?: Token(value, spacesFollowing, positionInOriginalString))
    }

    private fun tokenFromValueExact(
        clean: String,
        value: String,
        positionInOriginalString: Int,
        spacesFollowing: String
    ): Token? {
        var matchedToken: MatchedToken? = null
        val mapping = numberMappings[clean]
        if (mapping == null) {
            val wordMatch = wordMatches[clean]
            if (wordMatch != null) {
                matchedToken = MatchedToken(
                    value, spacesFollowing, positionInOriginalString, wordMatch
                )
            }
        } else {
            matchedToken = NumberToken(
                value, spacesFollowing, positionInOriginalString, mapping.categories, mapping.number
            )
        }

        val dur = durationMappings[clean]
        if (dur != null) {
            val durationToken = DurationToken(
                value, spacesFollowing, positionInOriginalString,
                dur.durationCategory, dur.durationMultiplier, dur.restrictedAfterNumber
            )
            if (matchedToken == null) {
                return durationToken
            } else {
                matchedToken.setDurationTokenMatch(durationToken)
            }
        }

        return matchedToken
    }

    private fun removePluralEndings(value: String): String? {
        for (pluralEnding in pluralEndings) {
            if (value.endsWith(pluralEnding)) {
                return value.substring(0, value.length - pluralEnding.length)
            }
        }
        return null
    }

    private fun cleanValue(value: String): String {
        // nfkd normalize (i.e. remove accents) and make lowercase
        val normalized =
            Normalizer.normalize(value.lowercase(Locale.getDefault()), Normalizer.Form.NFKD)
        return DIACRITICAL_MARKS_REMOVER.matcher(normalized).replaceAll("")
    }

    /**
     * Tokenizes a compound word (e.g. twentytwo is parsed into two tokens: twewnty and two)
     * @param clean the clean word
     * @return a list of tokens in reverse order (e.g. [two, twenty] for input twentytwo)
     */
    private fun tokenizeCompoundWord(
        clean: String,
        spacesFollowing: String,
        positionInOriginalString: Int,
    ): MutableList<Token>? {
        if (clean.isEmpty()) {
            return ArrayList()
        }

        for (compoundPiece in compoundWordPieces) {
            if (clean.startsWith(compoundPiece)) {
                val nextTokens = tokenizeCompoundWord(
                    clean = clean.substring(compoundPiece.length),
                    spacesFollowing = spacesFollowing,
                    positionInOriginalString = positionInOriginalString + compoundPiece.length,
                )
                if (nextTokens != null) {
                    nextTokens.add(
                        tokenFromValueExact(
                            clean = compoundPiece,
                            value = compoundPiece,
                            spacesFollowing = if (nextTokens.isEmpty()) spacesFollowing else "",
                            positionInOriginalString = positionInOriginalString,
                        )!!
                    )
                    return nextTokens // will be in reverse order, since first matches are added last
                }
            }
        }

        return null
    }


    private class Mapping(val categories: Set<String>, val number: Number)

    private class DurationMapping(val durationCategory: String, val durationMultiplier: Duration) {
        var restrictedAfterNumber: Boolean = false
    }

    companion object {
        private val DIACRITICAL_MARKS_REMOVER: Pattern =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        private val AT_SPACES_SPLITTER: Pattern = Pattern.compile(" ")


        private fun readCategories(categoriesArray: JsonArray?): Set<String> {
            if (categoriesArray == null) {
                throw RuntimeException(
                    "Missing categories array in match, mapping or raw_number_categories"
                )
            }

            val categories: MutableSet<String> = HashSet()
            for (o in categoriesArray) {
                if (o !is String) {
                    throw RuntimeException("Content of categories array is not string: $o")
                }
                categories.add(o)
            }

            return categories
        }
    }
}
