package org.dicio.numbers.parser.normalize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Token {
    public int index;
    public String value;

    private Token(final int index, final String value) {
        this.index = index;
        this.value = value;
    }

    public static List<Token> tokenize(final String utterance) {
        final List<Token> result = new ArrayList<>();
        int i = 0;
        for (final String part : utterance.split(" ")) {
            if (!part.isEmpty()) {
                result.add(new Token(i, part));
                ++i;
            }
        }
        return result;
    }

    public static String joinTokens(final List<Token> tokens) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final Token token : tokens) {
            if (!token.value.isEmpty()
                    && stringBuilder.length() != 0
                    && stringBuilder.charAt(stringBuilder.length() - 1) != ' ') {
                stringBuilder.append(" "); // separator is space
            }
            stringBuilder.append(token.value);
        }
        return stringBuilder.toString();
    }

    public static String replaceTokensMatchingMap(final Map<String, String> replacementMap,
                                                  final List<Token> tokens) {
        for (final Token token : tokens) {
            token.value = replacementMap.getOrDefault(token.value, token.value);
        }
        return Token.joinTokens(tokens);
    }

    public static String removeTokensMatchingList(final List<String> removalList,
                                                  final List<Token> tokens) {
        for (final Token token : tokens) {
            if (removalList.contains(token.value)) {
                token.value = "";
            }
        }
        return Token.joinTokens(tokens);
    }
}
