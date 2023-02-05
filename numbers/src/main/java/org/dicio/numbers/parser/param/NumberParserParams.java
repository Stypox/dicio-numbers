package org.dicio.numbers.parser.param;

import org.dicio.numbers.parser.Parser;
import org.dicio.numbers.parser.lexer.TokenStream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class NumberParserParams<T> {
    // TODO add documentation
    protected final Parser parser;
    private final String utterance;

    protected NumberParserParams(final Parser parser, final String utterance) {
        this.parser = parser;
        this.utterance = utterance;
    }


    protected abstract Supplier<T> getExtractorAtCurrentPosition(TokenStream tokenStream);


    public T getFirst() {
        final TokenStream ts = parser.tokenize(utterance);
        final Supplier<T> extractorAtCurrentPosition = getExtractorAtCurrentPosition(ts);

        while (!ts.finished()) {
            final T result = extractorAtCurrentPosition.get();
            if (result != null) {
                // found the first match here, return it
                return result;
            }

            // nothing found here, try next position
            ts.movePositionForwardBy(1);
        }

        // nothing was found at any position
        return null;
    }

    public List<Object> getMixedWithText() {
        final TokenStream ts = parser.tokenize(utterance);
        final Supplier<T> extractorAtCurrentPosition = getExtractorAtCurrentPosition(ts);
        final List<Object> textAndObjects = new ArrayList<>();
        final StringBuilder currentText = new StringBuilder();

        while (!ts.finished()) {
            final Object object = extractorAtCurrentPosition.get();

            if (object == null) {
                // no object here, add text and spaces of the current token to currentText instead
                currentText.append(ts.get(0).getValue());
                currentText.append(ts.get(0).getSpacesFollowing());
                ts.movePositionForwardBy(1);
            } else {
                if (currentText.length() != 0) {
                    textAndObjects.add(currentText.toString()); // add the text before the object
                    currentText.setLength(0); // clear the string builder efficiently
                }
                textAndObjects.add(object);
                currentText.append(ts.get(-1).getSpacesFollowing()); // spaces after the object
            }
        }

        if (currentText.length() != 0) {
            // add leftover text
            textAndObjects.add(currentText.toString());
        }

        return textAndObjects;
    }
}
