package org.dicio.numbers.parser.lexer;

import java.util.List;

public class TokenStream {
    private final List<Token> tokens;
    private int position = 0;

    public TokenStream(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public Token get(final int aheadBy) {
        final int index = position + aheadBy;
        if (index < 0 || index >= tokens.size()) {
            return Token.emptyToken(); // empty token to allow reducing checks
        }

        return tokens.get(index);
    }

    /**
     * Finds the first token without the provided category and returns the aheadBy offset
     * @param category the category that tokens have to match to end the search
     * @param startFromAheadBy start the search from the current position plus this value
     * @return the aheadBy index of the found token, or -position-1 if none could be found
     */
    public int indexOfWithoutCategory(final String category, final int startFromAheadBy) {
        for (int i = Math.max(position + startFromAheadBy, 0); i < tokens.size(); ++i) {
            if (!tokens.get(i).hasCategory(category)) {
                return i-position;
            }
        }
        return -1-position;
    }

    /**
     * Finds the first token (looking backward) without the provided category and returns the
     * (negative) aheadBy offset.
     * @param category the category that tokens have to match to end the search
     * @param startFromAheadBy start the search from the token at the current position plus this
     *                         value minus one and go backward from there
     * @return the aheadBy index of the found token, always negative, or -position-1 if none could
     *         be found
     */
    public int indexOfWithoutCategoryBackward(final String category, final int startFromAheadBy) {
        for (int i = Math.min(position + startFromAheadBy, tokens.size()) - 1; i >= 0; --i) {
            if (!tokens.get(i).hasCategory(category)) {
                return i-position;
            }
        }
        return -1-position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    public void movePositionForwardBy(final int delta) {
        position += delta;
    }

    public boolean finished() {
        return position >= tokens.size();
    }
}
