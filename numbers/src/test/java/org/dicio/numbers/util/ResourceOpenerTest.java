package org.dicio.numbers.util;

import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertNotNull;

public class ResourceOpenerTest {

    @Test
    public void validResourcePath() throws FileNotFoundException {
        assertNotNull(ResourceOpener.getResourceAsStream("config/en-us/tokenizer.json"));
        assertNotNull(ResourceOpener.getResourceAsStream("./config/en-us/date_time.json"));
        assertNotNull(ResourceOpener.getResourceAsStream("/config/./en-us/date_time_test.json"));
    }

    @Test(expected = FileNotFoundException.class)
    public void invalidResourcePath() throws FileNotFoundException {
        ResourceOpener.getResourceAsStream("invalid");
    }

    @Test
    public void folderResourcePath() throws FileNotFoundException {
        // note: the returned stream for folders contains the `\n`-separated list of children
        assertNotNull(ResourceOpener.getResourceAsStream("config/en-us/"));
        assertNotNull(ResourceOpener.getResourceAsStream("./config/en-us"));
        assertNotNull(ResourceOpener.getResourceAsStream("/config/en-us/./"));
    }
}
