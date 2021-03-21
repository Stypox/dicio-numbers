package org.dicio.numbers.util;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResourceOpener {
    public static InputStream getResourceAsStream(final String path) throws FileNotFoundException {
        final String normalizedPath = path.startsWith("/") ? path : "/" + path;
        final InputStream inputStream = ResourceOpener.class.getResourceAsStream(normalizedPath);

        if (inputStream == null) {
            throw new FileNotFoundException();
        }
        return inputStream;
    }
}
