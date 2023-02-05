package org.dicio.numbers.test;

import org.dicio.numbers.parser.lexer.Tokenizer;
import org.junit.Before;

public abstract class WithTokenizerTestBase {
    protected Tokenizer tokenizer;

    public abstract String configFolder();

    @Before
    public void setup() {
        tokenizer = new Tokenizer(configFolder());
    }
}
