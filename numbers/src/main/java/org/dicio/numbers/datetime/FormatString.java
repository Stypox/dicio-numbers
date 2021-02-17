package org.dicio.numbers.datetime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormatString {

    private interface Part {
        void format(Map<String, String> substitution, StringBuilder stringBuilder);
    }

    private static class StringPart implements Part {
        private final String value;
        StringPart(final String value) {
            this.value = value;
        }

        @Override
        public void format(final Map<String, String> substitution,
                           final StringBuilder stringBuilder) {
            stringBuilder.append(value);
        }
    }

    private static class FormatPart implements Part {
        private final String key;
        FormatPart(final String key) {
            this.key = key;
        }

        @Override
        public void format(final Map<String, String> substitutionTable,
                           final StringBuilder stringBuilder) {
            if (!substitutionTable.containsKey(key)){
                throw new IllegalArgumentException("Missing key " + key);
            }
            stringBuilder.append(substitutionTable.get(this.key));
        }
    }


    final List<Part> parts;

    public FormatString(final String stringToParse) {
        parts = new ArrayList<>();
        int prevIndex = 0;
        while (prevIndex < stringToParse.length()) {
            final int beginIndex = stringToParse.indexOf('{', prevIndex);
            if (beginIndex < 0) {
                parts.add(new StringPart(stringToParse.substring(prevIndex)));
                break;
            }

            final int endIndex = stringToParse.indexOf('}', beginIndex + 1);
            if (endIndex < 0) {
                parts.add(new StringPart(stringToParse.substring(prevIndex)));
                break;
            }

            if (beginIndex != prevIndex) {
                parts.add(new StringPart(stringToParse.substring(prevIndex, beginIndex)));
            }
            parts.add(new FormatPart(stringToParse.substring(beginIndex + 1, endIndex)));
            prevIndex = endIndex + 1;
        }
    }

    public String format(final Map<String, String> substitutionTable) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final Part part : parts) {
            part.format(substitutionTable, stringBuilder);
        }
        return stringBuilder.toString();
    }
}
