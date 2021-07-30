package org.dicio.numbers.lang.it;

import org.dicio.numbers.formatter.NumberFormatter;
import org.dicio.numbers.util.MixedFraction;
import org.dicio.numbers.util.Utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItalianFormatter extends NumberFormatter {

    final Map<Long, String> NUMBER_NAMES = new HashMap<Long, String>() {{
        put(0L, "zero");
        put(1L, "uno");
        put(2L, "due");
        put(3L, "tre");
        put(4L, "quattro");
        put(5L, "cinque");
        put(6L, "sei");
        put(7L, "sette");
        put(8L, "otto");
        put(9L, "nove");
        put(10L, "dieci");
        put(11L, "undici");
        put(12L, "dodici");
        put(13L, "tredici");
        put(14L, "quattordici");
        put(15L, "quindici");
        put(16L, "sedici");
        put(17L, "diciassette");
        put(18L, "diciotto");
        put(19L, "diciannove");
        put(20L, "venti");
        put(30L, "trenta");
        put(40L, "quaranta");
        put(50L, "cinquanta");
        put(60L, "sessanta");
        put(70L, "settanta");
        put(80L, "ottanta");
        put(90L, "novanta");
        put(100L, "cento");
        put(1000L, "mille");
        put(1000000L, "milione");
        put(1000000000L, "miliardo");
        put(1000000000000L, "bilione");
        put(1000000000000000L, "biliardo");
        put(1000000000000000000L, "trilione");
    }};

    final Map<Long, String> ORDINAL_NAMES = new HashMap<Long, String>() {{
        put(1L, "primo");
        put(2L, "secondo");
        put(3L, "terzo");
        put(4L, "quarto");
        put(5L, "quinto");
        put(6L, "sesto");
        put(7L, "settimo");
        put(8L, "ottavo");
        put(9L, "nono");
        put(10L, "decimo");
        put(11L, "undicesimo");
        put(12L, "dodicesimo");
        put(13L, "tredicesimo");
        put(14L, "quattordicesimo");
        put(15L, "quindicesimo");
        put(16L, "sedicesimo");
        put(17L, "diciassettesimo");
        put(18L, "diciottesimo");
        put(19L, "diciannovesimo");
        put(20L, "ventesimo");
        put(30L, "trentesimo");
        put(40L, "quarantesimo");
        put(50L, "cinquantesimo");
        put(60L, "sessantesimo");
        put(70L, "settantesimo");
        put(80L, "ottantesimo");
        put(90L, "novantesimo");
        put(100L, "centesimo");
        put(1000L, "millesimo");
        put(1000000L, "milionesimo");
        put(1000000000L, "miliardesimo");
        put(1000000000000L, "bilionesimo");
        put(1000000000000000L, "biliardesimo");
        put(1000000000000000000L, "trilionesimo");
    }};


    public ItalianFormatter() {
        super("config/it-it");
    }

    @Override
    public String niceNumber(final MixedFraction mixedFraction, final boolean speech) {
        if (speech) {
            final String sign = mixedFraction.negative ? "meno " : "";
            if (mixedFraction.numerator == 0) {
                return sign + pronounceNumber(mixedFraction.whole, 0, true, false, false);
            }

            String denominatorString;
            if (mixedFraction.denominator == 2) {
                denominatorString = "mezzo";
            } else if (mixedFraction.denominator == 4) {
                denominatorString = "quarto";
            } else {
                // use ordinal: only mezzo and quarto are exceptions
                denominatorString
                        = pronounceNumber(mixedFraction.denominator, 0, true, false, true);
            }

            final String numeratorString;
            if (mixedFraction.numerator == 1) {
                numeratorString = "un"; // TODO check
            } else {
                numeratorString = pronounceNumber(mixedFraction.numerator, 0, true, false, false);
                denominatorString = denominatorString.substring(0, denominatorString.length() - 1) + "i";
            }

            if (mixedFraction.whole == 0) {
                return sign + numeratorString + " " + denominatorString;
            } else {
                return sign + pronounceNumber(mixedFraction.whole, 0, true, false, false)
                        + " e " + numeratorString + " " + denominatorString;
            }

        } else {
            return niceNumberNotSpeech(mixedFraction);
        }
    }

    @Override
    public String pronounceNumber(double number,
                                  final int places,
                                  final boolean shortScale,
                                  final boolean scientific,
                                  final boolean ordinal) {
        return "";
    }

    @Override
    public String niceTime(final LocalTime time,
                           final boolean speech,
                           final boolean use24Hour,
                           final boolean showAmPm) {
        return "";
    }
}
