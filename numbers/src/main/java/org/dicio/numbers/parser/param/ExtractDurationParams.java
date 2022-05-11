package org.dicio.numbers.parser.param;

import org.dicio.numbers.parser.NumberParser;
import org.dicio.numbers.unit.Duration;

public class ExtractDurationParams {
    private final NumberParser numberParser;
    private final String utterance;

    // default values
    private boolean shortScale = true;

    public ExtractDurationParams(final NumberParser numberParser, final String utterance) {
        this.numberParser = numberParser;
        this.utterance = utterance;
    }

    /**
     * @param shortScale consider large numbers that make up durations (e.g. the "one billion" in
     *                   "one billion milliseconds") as short scale (true) or long scale (false)
     *                   numbers (see <a
     *                   href="https://en.wikipedia.org/wiki/Names_of_large_numbers">Names of large
     *                   numbers</a>). The default is true.
     * @return this
     */
    public ExtractDurationParams shortScale(final boolean shortScale) {
        this.shortScale = shortScale;
        return this;
    }

    /**
     * Calls {@link NumberParser#extractDuration(String, boolean)} with
     * the stored parameters.
     *
     * @return the duration contained in the utterance, or null if there is no duration
     */
    public Duration get() {
        return numberParser.extractDuration(utterance, shortScale);
    }
}
