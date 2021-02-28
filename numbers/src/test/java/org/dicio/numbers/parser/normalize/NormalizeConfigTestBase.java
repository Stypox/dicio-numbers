package org.dicio.numbers.parser.normalize;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public abstract class NormalizeConfigTestBase {

    protected NormalizeConfig config;

    /**
     * @return the config folder or null if you want to test the default config
     */
    public abstract String configFolder();

    @Before
    public void setup() {
        if (configFolder() == null) {
            config = NormalizeConfig.DEFAULT_CONFIG;
        } else {
            config = new NormalizeConfig(configFolder());
        }
    }

    @Test
    public void notNull() {
        assertNotNull(config.contractions);
        assertNotNull(config.wordReplacements);
        assertNotNull(config.numberReplacements);
        assertNotNull(config.accents);
        assertNotNull(config.stopWords);
        assertNotNull(config.articles);
        assertNotNull(config.symbols);
    }
}
