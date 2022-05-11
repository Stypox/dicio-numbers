package org.dicio.numbers.parser.lexer;

import org.dicio.numbers.unit.Number;

import java.util.Set;

public class NumberToken extends MatchedToken {

    private final Number number;

    NumberToken(final String value,
                final String spacesFollowing,
                final Set<String> categories,
                final Number number) {
        super(value, spacesFollowing, categories);
        this.number = number;
    }

    public Number getNumber() {
        return number;
    }

    @Override
    public boolean isNumberEqualTo(final long integer) {
        return number.equals(integer);
    }
}
