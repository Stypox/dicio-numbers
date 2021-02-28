package org.dicio.numbers.parser.normalize;

import org.junit.Test;

public class NormalizeConfigDefaultTest extends NormalizeConfigTestBase {
    @Override
    public String configFolder() {
        return null;
    }

    @Test(expected = RuntimeException.class)
    public void invalidConfigFolder() {
        new NormalizeConfig("Hello, how are you?");
    }
}
