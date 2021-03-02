package org.dicio.numbers.parser.normalize;

import java.util.List;

public class Normalizer {

    private final NormalizeConfig config;

    public Normalizer(final NormalizeConfig config) {
        this.config = config;
    }

    public String expandContractions(final String utterance) {
        return Token.replaceTokensMatchingMap(config.contractions, tokenize(utterance));
    }

    public String numbersToDigits(final String utterance) {
        return Token.replaceTokensMatchingMap(config.numberReplacements, tokenize(utterance));
    }

    public String removeArticles(final String utterance) {
        return Token.removeTokensMatchingList(config.articles, tokenize(utterance));
    }

    public String removeStopWords(final String utterance) {
        return Token.removeTokensMatchingList(config.stopWords, tokenize(utterance));
    }

    public List<Token> tokenize(final String utterance) {
        // TODO improve to separate % and # (maybe also - )
        return Token.tokenize(utterance);
    }
}
