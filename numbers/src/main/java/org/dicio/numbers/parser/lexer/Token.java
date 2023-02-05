package org.dicio.numbers.parser.lexer;

import org.dicio.numbers.unit.Number;

public class Token {

    private static final Token EMPTY_TOKEN = new Token("", "");


    private final String value;
    private String spacesFollowing;

    Token(final String value, final String spacesFollowing) {
        this.value = value;
        this.spacesFollowing = spacesFollowing;
    }

    void setSpacesFollowing(final String spacesFollowing) {
        this.spacesFollowing = spacesFollowing;
    }


    public final String getValue() {
        return value;
    }

    public final String getSpacesFollowing() {
        return spacesFollowing;
    }

    public final boolean isValue(final String value) {
        return this.value.equalsIgnoreCase(value);
    }

    public Number getNumber() {
        throw new UnsupportedOperationException(); // throw by default, overridden
    }

    public boolean isNumberEqualTo(final long integer) {
        return false; // false by default, overridden
    }

    public boolean hasCategory(final String category) {
        return false; // false by default, overridden
    }

    public boolean isDurationToken() {
        return false; // false by default, overridden
    }

    public DurationToken asDurationToken() {
        throw new UnsupportedOperationException(); // throw by default, overridden
    }


    public static Token emptyToken() {
        return EMPTY_TOKEN;
    }
}
