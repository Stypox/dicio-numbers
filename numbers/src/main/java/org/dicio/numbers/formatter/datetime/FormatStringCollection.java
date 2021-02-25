package org.dicio.numbers.formatter.datetime;

import com.grack.nanojson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FormatStringCollection {

    private static class PatternFormatPair {
        final Pattern pattern;
        final FormatString formatString;

        private PatternFormatPair(final JsonObject jsonObject) {
            pattern = Pattern.compile(jsonObject.getString("match"));
            formatString = new FormatString(jsonObject.getString("format"));
        }
    }

    private final FormatString defaultFormat;
    private final List<PatternFormatPair> patternFormats;

    public FormatStringCollection(final JsonObject jsonObject) {
        defaultFormat = new FormatString(jsonObject.getString("default"));
        patternFormats = new ArrayList<>();
        for (int i = 1; jsonObject.has(String.valueOf(i)); ++i) {
            patternFormats.add(new PatternFormatPair(jsonObject.getObject(String.valueOf(i))));
        }
    }

    public FormatString getMostSuitableFormatString(final int number) {
        final String numberString = String.valueOf(number);
        for (final PatternFormatPair patternFormat : patternFormats) {
            if (patternFormat.pattern.matcher(numberString).matches()) {
                return patternFormat.formatString;
            }
        }
        return defaultFormat;
    }
}
