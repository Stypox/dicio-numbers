package org.dicio.numbers.parser.lexer;

import java.util.Set;

public class MatchedToken extends Token {
    private final Set<String> categories;
    private DurationToken durationTokenMatch = null;

    MatchedToken(final String value,
                 final String spacesFollowing,
                 final Set<String> categories) {
        super(value, spacesFollowing);
        this.categories = categories;
    }

    public void setDurationTokenMatch(final DurationToken durationTokenMatch) {
        this.durationTokenMatch = durationTokenMatch;
    }

    @Override
    public boolean hasCategory(final String category) {
        return categories.contains(category);
    }

    @Override
    public boolean isDurationToken() {
        return durationTokenMatch != null;
    }

    @Override
    public DurationToken asDurationToken() {
        if (durationTokenMatch == null) {
            return super.asDurationToken();
        } else {
            return durationTokenMatch;
        }
    }
}
