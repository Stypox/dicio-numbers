package org.dicio.numbers;

import org.dicio.numbers.formatter.NumberFormatter;
import org.dicio.numbers.lang.en.EnglishFormatter;
import org.dicio.numbers.lang.en.EnglishParser;
import org.dicio.numbers.lang.it.ItalianFormatter;
import org.dicio.numbers.lang.it.ItalianParser;
import org.dicio.numbers.parser.NumberParser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class NumberParserFormatterBuilder {

    private static class ParserFormatterClasses {
        final Class<? extends NumberFormatter> formatter;
        final Class<? extends NumberParser> parser;

        private ParserFormatterClasses(final Class<? extends NumberFormatter> formatter,
                                       final Class<? extends NumberParser> parser) {
            this.parser = parser;
            this.formatter = formatter;
        }
    }

    static class ParserFormatterPair {
        final NumberFormatter formatter;
        final NumberParser parser;

        ParserFormatterPair(final NumberFormatter formatter, final NumberParser parser) {
            this.parser = parser;
            this.formatter = formatter;
        }
    }

    private static final Map<String, ParserFormatterClasses> PARSER_FORMATTER_CLASSES_MAP
            = new HashMap<String, ParserFormatterClasses>() {{
        put("en", new ParserFormatterClasses(EnglishFormatter.class, EnglishParser.class));
        put("it", new ParserFormatterClasses(ItalianFormatter.class, ItalianParser.class));
    }};


    private NumberParserFormatterBuilder() {
    }


    static ParserFormatterPair parserFormatterPairForLocale(final Locale locale)
            throws IllegalArgumentException {
        final String localeString
                = resolveLocaleString(locale, PARSER_FORMATTER_CLASSES_MAP.keySet());
        final ParserFormatterClasses classes = PARSER_FORMATTER_CLASSES_MAP.get(localeString);

        try {
            return new ParserFormatterPair(classes.formatter.newInstance(),
                    classes.parser.newInstance());
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("The formatter and parser for the provided supported"
                    + " locale \"" + locale + "\" could not be instantiated", e);
        }
    }


    // copied from dicio-android
    private static String resolveLocaleString(final Locale locale,
                                              final Collection<String> supportedLocales)
            throws IllegalArgumentException {
        // first try with full locale name (e.g. en-US)
        String localeString = (locale.getLanguage() + "-" + locale.getCountry()).toLowerCase();
        if (supportedLocales.contains(localeString)) {
            return localeString;
        }

        // then try with only base language (e.g. en)
        localeString = locale.getLanguage().toLowerCase();
        if (supportedLocales.contains(localeString)) {
            return localeString;
        }

        // then try with children languages of locale base language (e.g. en-US, en-GB, en-UK, ...)
        for (final String supportedLocalePlus : supportedLocales) {
            for (final String supportedLocale : supportedLocalePlus.split("\\+")) {
                if (supportedLocale.split("-", 2)[0].equals(localeString)) {
                    return supportedLocalePlus;
                }
            }
        }

        // fail
        throw new IllegalArgumentException("Unsupported locale: " + locale);
    }
}
