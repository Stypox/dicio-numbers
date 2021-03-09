package org.dicio.numbers.parser.lexer;

import java.util.Set;

public class MatchedToken extends Token {
    private final Set<String> categories;

    protected MatchedToken(final String value,
                           final String spacesFollowing,
                           final Set<String> categories) {
        super(value, spacesFollowing);
        this.categories = categories;
    }

    @Override
    public boolean hasCategory(final String category) {
        return categories.contains(category);
    }
}
