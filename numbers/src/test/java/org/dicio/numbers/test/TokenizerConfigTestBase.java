package org.dicio.numbers.test;

import static org.junit.Assert.fail;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.dicio.numbers.util.ResourceOpener;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class TokenizerConfigTestBase {
    protected JsonObject root;

    public abstract String configFolder();

    private String fullConfigFilePath() {
        return configFolder() + "/tokenizer.json";
    }

    @Before
    public void setup() throws FileNotFoundException, JsonParserException {
        root = JsonParser.object().from(
                ResourceOpener.getResourceAsStream(fullConfigFilePath()));
    }


    @Test
    public void testNoDuplicateMatches() {
        final Set<String> allMatches = new HashSet<>();
        final Set<String> duplicateMatches = new HashSet<>();

        final Stream<String> wordMatchesStream = root.getArray("word_matches").stream()
                .map(JsonObject.class::cast)
                .flatMap(o -> o.getArray("values").stream())
                .map(String.class::cast);

        final Stream<String> numberMappingsStream = root.getArray("number_mappings").stream()
                .map(JsonObject.class::cast)
                .map(o -> o.getObject("values"))
                .flatMap(o -> o.keySet().stream());

        Stream.concat(wordMatchesStream, numberMappingsStream).forEach(match -> {
            if (allMatches.contains(match)) {
                duplicateMatches.add(match);
            } else {
                allMatches.add(match);
            }
        });

        if (!duplicateMatches.isEmpty()) {
            fail("Duplicate word matches found in tokenizer config at \"" + fullConfigFilePath()
                    + "\":\n\t" + duplicateMatches);
        }
    }
}
