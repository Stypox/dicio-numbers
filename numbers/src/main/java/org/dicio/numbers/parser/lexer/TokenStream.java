package org.dicio.numbers.parser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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

    public int getPosition() {
        return position;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    /**
     * Moves the position of the token stream ahead by the provided delta. The delta can be
     * negative, i.e. rewinding the stream.
     * @param delta the offset of the wanted position with respect to the current
     */
    public void movePositionForwardBy(final int delta) {
        position += delta;
    }

    public boolean finished() {
        return position >= tokens.size();
    }


    /**
     * Finds the first token without the provided category and returns the aheadBy offset
     * @param category the category that tokens have to match to end the search
     * @param startFromAheadBy start the search from the current position plus this value
     * @return the aheadBy offset of the found token, or the aheadBy offset of one past the last
     *         token in the token stream if no token was found without the provided category
     */
    public int indexOfWithoutCategory(final String category, final int startFromAheadBy) {
        for (int i = Math.max(position + startFromAheadBy, 0); i < tokens.size(); ++i) {
            if (!tokens.get(i).hasCategory(category)) {
                return i-position;
            }
        }
        return tokens.size()-position;
    }

    public <T> T tryOrSkipCategory(final String category,
                                   final boolean doTrySkipping,
                                   final Supplier<T> function) {
        if (!doTrySkipping) {
            return function.get();
        }

        final int originalPosition = getPosition();
        do {
            final T result = function.get();
            if (result != null) {
                return result;
            }
            movePositionForwardBy(1);
        } while (get(-1).hasCategory(category) && !finished());

        // found nothing, restore position
        setPosition(originalPosition);
        return null;
    }

    public <T> T tryOrSkipDateTimeIgnore(final boolean doTrySkipping, final Supplier<T> function) {
        return tryOrSkipCategory("date_time_ignore", doTrySkipping, function);
    }

    @SafeVarargs
    public final <T> T firstWhichUsesMostTokens(final Supplier<T>... suppliers) {
        final int originalPosition = getPosition();
        T bestResult = null;
        int bestPosition = originalPosition;

        for (final Supplier<T> supplier : suppliers) {
            setPosition(originalPosition);
            final T result = supplier.get();
            if (result != null && getPosition() > bestPosition) {
                bestResult = result;
                bestPosition = getPosition();
            }
        }

        setPosition(bestPosition);
        return bestResult;
    }
}
