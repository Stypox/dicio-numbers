package org.dicio.numbers.lang.en;

import org.dicio.numbers.formatter.Formatter;
import org.dicio.numbers.unit.MixedFraction;
import org.dicio.numbers.util.Utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnglishFormatter extends Formatter {

    final Map<Long, String> NUMBER_NAMES = new HashMap<Long, String>() {{
        put(0L, "zero");
        put(1L, "one");
        put(2L, "two");
        put(3L, "three");
        put(4L, "four");
        put(5L, "five");
        put(6L, "six");
        put(7L, "seven");
        put(8L, "eight");
        put(9L, "nine");
        put(10L, "ten");
        put(11L, "eleven");
        put(12L, "twelve");
        put(13L, "thirteen");
        put(14L, "fourteen");
        put(15L, "fifteen");
        put(16L, "sixteen");
        put(17L, "seventeen");
        put(18L, "eighteen");
        put(19L, "nineteen");
        put(20L, "twenty");
        put(30L, "thirty");
        put(40L, "forty");
        put(50L, "fifty");
        put(60L, "sixty");
        put(70L, "seventy");
        put(80L, "eighty");
        put(90L, "ninety");
        put(100L, "hundred");
        put(1000L, "thousand");
        put(1000000L, "million");
    }};

    final Map<Long, String> NUMBER_NAMES_SHORT_SCALE = new HashMap<Long, String>(NUMBER_NAMES) {{
        put(1000000000L, "billion");
        put(1000000000000L, "trillion");
        put(1000000000000000L, "quadrillion");
        put(1000000000000000000L, "quintillion");
    }};

    final Map<Long, String> NUMBER_NAMES_LONG_SCALE = new HashMap<Long, String>(NUMBER_NAMES) {{
        put(1000000000000L, "billion");
        put(1000000000000000000L, "trillion");
    }};


    final Map<Long, String> ORDINAL_NAMES = new HashMap<Long, String>() {{
        put(1L, "first");
        put(2L, "second");
        put(3L, "third");
        put(4L, "fourth");
        put(5L, "fifth");
        put(6L, "sixth");
        put(7L, "seventh");
        put(8L, "eighth");
        put(9L, "ninth");
        put(10L, "tenth");
        put(11L, "eleventh");
        put(12L, "twelfth");
        put(13L, "thirteenth");
        put(14L, "fourteenth");
        put(15L, "fifteenth");
        put(16L, "sixteenth");
        put(17L, "seventeenth");
        put(18L, "eighteenth");
        put(19L, "nineteenth");
        put(20L, "twentieth");
        put(30L, "thirtieth");
        put(40L, "fortieth");
        put(50L, "fiftieth");
        put(60L, "sixtieth");
        put(70L, "seventieth");
        put(80L, "eightieth");
        put(90L, "ninetieth");
        put(100L, "hundredth");
        put(1000L, "thousandth");
        put(1000000L, "millionth");
    }};

    final Map<Long, String> ORDINAL_NAMES_SHORT_SCALE = new HashMap<Long, String>(ORDINAL_NAMES) {{
        put(1000000000L, "billionth");
        put(1000000000000L, "trillionth");
        put(1000000000000000L, "quadrillionth");
        put(1000000000000000000L, "quintillionth");
    }};

    final Map<Long, String> ORDINAL_NAMES_LONG_SCALE = new HashMap<Long, String>(ORDINAL_NAMES) {{
        put(1000000000000L, "billionth");
        put(1000000000000000000L, "trillionth");
    }};


    public EnglishFormatter() {
        super("config/en-us");
    }

    @Override
    public String niceNumber(final MixedFraction mixedFraction, final boolean speech) {
        if (speech) {
            final String sign = mixedFraction.negative ? "minus " : "";
            if (mixedFraction.numerator == 0) {
                return sign + pronounceNumber(mixedFraction.whole, 0, true, false, false);
            }

            String denominatorString;
            if (mixedFraction.denominator == 2) {
                denominatorString = "half";
            } else if (mixedFraction.denominator == 4) {
                denominatorString = "quarter";
            } else {
                // use ordinal: only half and quarter are exceptions
                denominatorString
                        = pronounceNumber(mixedFraction.denominator, 0, true, false, true);
            }

            final String numeratorString;
            if (mixedFraction.numerator == 1) {
                numeratorString = "a";
            } else {
                numeratorString = pronounceNumber(mixedFraction.numerator, 0, true, false, false);
                denominatorString += "s";
            }

            if (mixedFraction.whole == 0) {
                return sign + numeratorString + " " + denominatorString;
            } else {
                return sign + pronounceNumber(mixedFraction.whole, 0, true, false, false)
                        + " and " + numeratorString + " " + denominatorString;
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

        if (number == Double.POSITIVE_INFINITY) {
            return "infinity";
        } else if (number == Double.NEGATIVE_INFINITY) {
            return "negative infinity";
        } else if (Double.isNaN(number)) {
            return "not a number";
        }

        // also using scientific mode if the number is too big to be spoken fully. Checking against
        // the biggest double smaller than 10^21 = 1000 * 10^18, which is the biggest pronounceable
        // number, since e.g. 999.99 * 10^18 can be pronounced correctly.
        if (scientific || Math.abs(number) > 999999999999999934463d) {
            final String scientificFormatted = String.format(Locale.ENGLISH, "%E", number);
            final String[] parts = scientificFormatted.split("E", 2);
            final double power = Integer.parseInt(parts[1]);

            if (power != 0) {
                // This handles negatives of powers separately from the normal
                // handling since each call disables the scientific flag
                final double n = Double.parseDouble(parts[0]);
                return String.format("%s%s times ten to the power of %s%s",
                        n < 0 ? "negative " : "",
                        pronounceNumber(Math.abs(n), places, shortScale, false, false),
                        power < 0 ? "negative " : "",
                        pronounceNumber(Math.abs(power), places, shortScale, false, false));
            }
        }

        final StringBuilder result = new StringBuilder();
        if (number < 0) {
            number = -number;
            // from here on number is always positive
            if (places != 0 || number >= 0.5) {
                // do not add minus if number will be rounded to 0
                result.append(scientific ? "negative " : "minus ");
            }
        }

        final int realPlaces = Utils.decimalPlacesNoFinalZeros(number, places);
        final boolean numberIsWhole = realPlaces == 0;
        // if no decimal places to be printed, numberLong should be the rounded number
        final long numberLong = (long) number + (number % 1 >= 0.5 && numberIsWhole ? 1 : 0);

        if (!ordinal && numberIsWhole && numberLong > 1000 && numberLong < 2000) {
            // deal with 4 digits that can be said like a date, i.e. 1972 => nineteen seventy two

            result.append(NUMBER_NAMES.get(numberLong / 100));
            result.append(" ");
            if (numberLong % 100 == 0) {
                // 1900 => nineteen hundred
                result.append(NUMBER_NAMES.get(100L));
            } else if (numberLong % 100 < 10 && numberLong % 100 != 0) {
                // 1906 => nineteen oh six
                result.append("oh ");
                result.append(NUMBER_NAMES.get(numberLong % 10));
            } else if (numberLong % 10 == 0 || numberLong % 100 < 20) {
                // 1960 => nineteen sixty; 1911 => nineteen eleven
                result.append(NUMBER_NAMES.get(numberLong % 100));
            } else {
                // 1961 => nineteen sixty one
                result.append(NUMBER_NAMES.get(numberLong % 100 - numberLong % 10));
                result.append(" ");
                result.append(NUMBER_NAMES.get(numberLong % 10));
            }

            return result.toString();
        }

        if (!ordinal && NUMBER_NAMES.containsKey(numberLong)) {
            if (number > 90) {
                result.append("one ");
            }
            result.append(NUMBER_NAMES.get(numberLong));

        } else if (shortScale) {
            boolean ordi = ordinal && numberIsWhole; // not ordinal if not whole
            final List<Long> groups = Utils.splitByModulus(numberLong, 1000);
            final List<String> groupNames = new ArrayList<>();
            for (int i = 0; i < groups.size(); ++i) {
                final long z = groups.get(i);
                if (z == 0) {
                    continue; // skip 000 groups
                }
                String groupName = subThousand(z, i == 0 && ordi);

                if (i != 0) {
                    final long magnitude = Utils.longPow(1000, i);
                    if (ordi) {
                        // ordi can be true only for the first group (i.e. at the end of the number)
                        if (z == 1) {
                            // remove "one" from first group (e.g. "one billion, millionth")
                            groupName = ORDINAL_NAMES_SHORT_SCALE.get(magnitude);
                        } else {
                            groupName += " " + ORDINAL_NAMES_SHORT_SCALE.get(magnitude);
                        }
                    } else {
                        groupName += " " + NUMBER_NAMES_SHORT_SCALE.get(magnitude);
                    }
                }

                groupNames.add(groupName);
                ordi = false;
            }

            appendSplitGroups(result, groupNames);

        } else {
            boolean ordi = ordinal && numberIsWhole; // not ordinal if not whole
            final List<Long> groups = Utils.splitByModulus(numberLong, 1000000);
            final List<String> groupNames = new ArrayList<>();
            for (int i = 0; i < groups.size(); ++i) {
                final long z = groups.get(i);
                if (z == 0) {
                    continue; // skip 000000 groups
                }

                String groupName;
                if (z < 1000) {
                    groupName = subThousand(z, i == 0 && ordi);
                } else {
                    groupName = subThousand(z / 1000, false) + " thousand";
                    if (z % 1000 != 0) {
                        groupName += (i == 0 ? ", " : " ") + subThousand(z % 1000, i == 0 && ordi);
                    } else if (i == 0 && ordi) {
                        if (z / 1000 == 1) {
                            groupName = "thousandth"; // remove "one" from "one thousandth"
                        } else {
                            groupName += "th";
                        }
                    }
                }

                if (i != 0) {
                    final long magnitude = Utils.longPow(1000000, i);
                    if (ordi) {
                        // ordi can be true only for the first group (i.e. at the end of the number)
                        if (z == 1) {
                            // remove "one" from first group (e.g. "one billion, millionth")
                            groupName = ORDINAL_NAMES_LONG_SCALE.get(magnitude);
                        } else {
                            groupName += " " + ORDINAL_NAMES_LONG_SCALE.get(magnitude);
                        }
                    } else {
                        groupName += " " + NUMBER_NAMES_LONG_SCALE.get(magnitude);
                    }
                }

                groupNames.add(groupName);
                ordi = false;
            }

            appendSplitGroups(result, groupNames);
        }

        if (realPlaces > 0) {
            if (number < 1.0 && (result.length() == 0 || "minus ".contentEquals(result))) {
                result.append("zero"); // nothing was written before
            }
            result.append(" point");

            final String fractionalPart = String.format("%." + realPlaces + "f", number % 1);
            for (int i = 2; i < fractionalPart.length(); ++i) {
                result.append(" ");
                result.append(NUMBER_NAMES.get((long) (fractionalPart.charAt(i) - '0')));
            }
        }

        return result.toString();
    }

    @Override
    public String niceTime(final LocalTime time,
                           final boolean speech,
                           final boolean use24Hour,
                           final boolean showAmPm) {
        if (speech) {
            if (use24Hour) {
                final StringBuilder result = new StringBuilder();
                if (time.getHour() < 10) {
                    result.append("zero ");
                }
                result.append(pronounceNumberDuration(time.getHour()));

                result.append(" ");
                if (time.getMinute() == 0) {
                    result.append("hundred");
                } else {
                    if (time.getMinute() < 10) {
                        result.append("zero ");
                    }
                    result.append(pronounceNumberDuration(time.getMinute()));
                }

                return result.toString();
            } else {
                if (time.getHour() == 0 && time.getMinute() == 0) {
                    return "midnight";
                } else if (time.getHour() == 12 && time.getMinute() == 0) {
                    return "noon";
                }

                final int normalizedHour = (time.getHour() + 11) % 12 + 1; // 1 to 12
                final StringBuilder result = new StringBuilder();
                if (time.getMinute() == 15) {
                    result.append("quarter past ");
                    result.append(pronounceNumberDuration(normalizedHour));
                } else if (time.getMinute() == 30) {
                    result.append("half past ");
                    result.append(pronounceNumberDuration(normalizedHour));
                } else if (time.getMinute() == 45) {
                    result.append("quarter to ");
                    result.append(pronounceNumberDuration(normalizedHour % 12 + 1));
                } else {
                    result.append(pronounceNumberDuration(normalizedHour));

                    if (time.getMinute() == 0) {
                        if (!showAmPm) {
                            return result + " o'clock";
                        }
                    } else {
                        if (time.getMinute() < 10) {
                            result.append(" oh");
                        }
                        result.append(" ");
                        result.append(pronounceNumberDuration(time.getMinute()));
                    }
                }

                if (showAmPm) {
                    result.append(time.getHour() >= 12 ? " p.m." : " a.m.");
                }
                return result.toString();
            }

        } else {
            if (use24Hour) {
                return time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH));
            } else {
                final String result = time.format(DateTimeFormatter.ofPattern(
                        showAmPm ? "K:mm a" : "K:mm", Locale.ENGLISH));
                if (result.startsWith("0:")) {
                    return "12:" + result.substring(2);
                } else {
                    return result;
                }
            }
        }
    }


    /**
     * @param n must be 0 <= n <= 999
     * @param ordinal whether to return an ordinal number (usually with -th)
     * @return the string representation of a number smaller than 1000
     */
    private String subThousand(final long n, final boolean ordinal) {
        // this function calls itself inside if branches to make sure `ordinal` is respected
        if (ordinal && ORDINAL_NAMES.containsKey(n)) {
            return ORDINAL_NAMES.get(n);
        } else if (n < 100) {
            if (!ordinal && NUMBER_NAMES.containsKey(n)) {
                return NUMBER_NAMES.get(n);
            }
            // n is surely => 20 from here on, since all n < 20 are in (ORDINAL|NUMBER)_NAMES

            return NUMBER_NAMES.get(n - n % 10)
                    + (n % 10 > 0 ? " " + subThousand(n % 10, ordinal) : "");
        } else {
            return NUMBER_NAMES.get(n / 100) + " hundred"
                    + (n % 100 > 0 ? " and " + subThousand(n % 100, ordinal)
                    : (ordinal ? "th" : ""));
        }
    }

    /**
     * @param result the string builder to append the comma-separated group names to
     * @param groupNames the group names
     */
    private void appendSplitGroups(final StringBuilder result, final List<String> groupNames) {
        if (!groupNames.isEmpty()) {
            result.append(groupNames.get(groupNames.size() - 1));
        }

        for (int i = groupNames.size() - 2; i >= 0; --i) {
            result.append(", ");
            result.append(groupNames.get(i));
        }
    }
}
