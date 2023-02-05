package org.dicio.numbers.util;

import org.dicio.numbers.parser.lexer.Token;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Number;

import java.util.function.Supplier;

public class NumberExtractorUtils {

    private NumberExtractorUtils() {
    }

    public interface NumberGroupGetter {
        Number get(TokenStream ts, boolean allowOrdinal, double lastMultiplier);
    }


    public static Integer extractOneIntegerInRange(final TokenStream ts,
                                                   final int fromInclusive,
                                                   final int toInclusive,
                                                   final Supplier<Number> numberSupplier) {
        final int originalPosition = ts.getPosition();
        final Number number = numberSupplier.get();

        if (number == null || !number.isInteger()
                || number.integerValue() < fromInclusive || number.integerValue() > toInclusive) {
            ts.setPosition(originalPosition);
            return null;
        }

        return (int) number.integerValue();
    }


    public static Number signBeforeNumber(final TokenStream ts,
                                          final Supplier<Number> numberSupplier) {
        if (ts.get(0).hasCategory("sign")) {
            // parse sign from e.g. "minus twelve"

            boolean negative = ts.get(0).hasCategory("negative");
            ts.movePositionForwardBy(1);

            final Number n = numberSupplier.get();
            if (n == null) {
                ts.movePositionForwardBy(-1); // rewind
                return null;
            } else {
                return n.multiply(negative ? -1 : 1).withOrdinal(n.isOrdinal());
            }

        }
        return numberSupplier.get();
    }

    public static Number numberBigRaw(final TokenStream ts, final boolean allowOrdinal) {
        // try to parse big raw numbers (bigger than 999), e.g. 1207, 57378th
        final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
        if (isRawNumber(ts.get(nextNotIgnore))) {
            final boolean ordinal = ts.get(nextNotIgnore + 1).hasCategory("ordinal_suffix");
            if (!allowOrdinal && ordinal) {
                return null; // do not allow ordinal if allowOrdinal is false
            } else {
                // a big number in raw form, e.g. 1250067, 5839th
                ts.movePositionForwardBy(nextNotIgnore + (ordinal ? 2 : 1));
                return ts.get(ordinal ? -2 : -1).getNumber().withOrdinal(ordinal);
            }

        } else {
            return null; // nothing was found
        }
    }

    public static Number numberMadeOfGroups(final TokenStream ts,
                                            final boolean allowOrdinal,
                                            final NumberGroupGetter getNumberGroup) {
        // read as many groups as possible (e.g. 123 billion + 45 million + 6 thousand + 78)
        Number groups = null;
        double lastMultiplier = Double.MAX_VALUE;
        while (true) {
            final Number group = getNumberGroup.get(ts, allowOrdinal, lastMultiplier);

            if (group == null) {
                break; // either nothing else was found or next multiplier is bigger than last one
            } else if (groups == null) {
                groups = group; // first group
            } else {
                groups = groups.plus(group); // e.g. seven hundred thousand + thirteen
            }

            if (group.isOrdinal()) {
                groups = groups.withOrdinal(true);
                break; // ordinal numbers terminate at the ordinal group
            }
            lastMultiplier = group.isDecimal() ? group.decimalValue() : group.integerValue();
        }
        return groups;
    }

    public static Number numberGroupShortScale(final TokenStream ts,
                                               final boolean allowOrdinal,
                                               final double lastMultiplier) {
        if (lastMultiplier < 1000) {
            return null; // prevent two numbers smaller than 1000 to be one after another
        }

        final int originalPosition = ts.getPosition();
        final Number groupValue = numberLessThan1000(ts, allowOrdinal); // e.g. one hundred and twelve
        if (groupValue != null && groupValue.isOrdinal()) {
            // ordinal numbers can't be followed by a multiplier
            return groupValue;
        }

        final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
        final boolean ordinal = ts.get(nextNotIgnore).hasCategory("ordinal");
        if (ts.get(nextNotIgnore).hasCategory("multiplier") && (allowOrdinal || !ordinal)) {
            // prevent ordinal multiplier if allowOrdinal is false
            final Number multiplier = ts.get(nextNotIgnore).getNumber();
            if (multiplier.lessThan(lastMultiplier)) {
                ts.movePositionForwardBy(nextNotIgnore + 1);
                if (groupValue == null) {
                    // the multiplier alone, e.g. a million
                    return multiplier.withOrdinal(ordinal);
                } else {
                    // number smaller than 1000 followed by a multiplier, e.g. thirteen billion
                    return multiplier.multiply(groupValue).withOrdinal(ordinal);
                }
            }
        } else {
            // no multiplier for this last number group, e.g. one hundred and two
            // also here if the multiplier is ordinal, but allowOrdinal is false
            return groupValue;
        }

        // multiplier is too big, reset to previous position
        ts.setPosition(originalPosition);
        return null;
    }

    public static Number numberLessThan1000(final TokenStream ts, final boolean allowOrdinal) {
        long hundred = -1, ten = -1, digit = -1;
        boolean ordinal = false;
        while (true) {
            final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
            if (!allowOrdinal && ts.get(nextNotIgnore).hasCategory("ordinal")) {
                // prevent ordinal numbers if allowOrdinal is false
                break;
            }

            if (ts.get(nextNotIgnore).hasCategory("digit")) {
                if (digit < 0 && (!ts.get(nextNotIgnore).isNumberEqualTo(0)
                        || (ten < 0 && hundred < 0))) {
                    // do not allow zero after ten or hundred, e.g. twenty zero or hundred nought
                    digit = ts.get(nextNotIgnore).getNumber().integerValue();
                } else {
                    break; // unexpected double digit
                }

            } else if (ts.get(nextNotIgnore).hasCategory("teen")) {
                if (ten < 0 && digit < 0) {
                    ten = ts.get(nextNotIgnore).getNumber().integerValue();
                    digit = 0; // ten contains also the digit, but set to 0 to prevent double digit
                } else {
                    break; // unexpected double ten or ten after digit
                }

            } else if (ts.get(nextNotIgnore).hasCategory("tens")) {
                if (ten < 0 && digit < 0) {
                    ten = ts.get(nextNotIgnore).getNumber().integerValue();
                } else {
                    break; // unexpected double ten or ten after digit
                }

            } else if (ts.get(nextNotIgnore).hasCategory("hundred")) {
                if (hundred < 0 && ten < 0) {
                    if (digit < 0) {
                        hundred = 100; // e.g. a hundred
                    } else if (digit == 0) {
                        break; // do not allow e.g. zero hundred
                    } else {
                        hundred = digit * 100; // e.g. three hundred
                        digit = -1; // reset digit for e.g. four hundred and nine
                    }
                } else {
                    break; // unexpected double hundred
                }

            } else if (isRawNumber(ts.get(nextNotIgnore))) {
                // raw number, e.g. 192
                final Number rawNumber = ts.get(nextNotIgnore).getNumber();
                if (rawNumber.isDecimal()) {
                    // this can happen only for numbers really big, like with 50 digits
                    // so they surely are not less than 1000
                    break;
                }

                if (!allowOrdinal && ts.get(nextNotIgnore + 1).hasCategory("ordinal_suffix")) {
                    break; // do not allow ordinal if allowOrdinal is false
                }

                if (rawNumber.lessThan(10)) {
                    if (digit < 0) {
                        digit = rawNumber.integerValue();
                    } else {
                        break; // unexpected double digit
                    }
                } else if (rawNumber.lessThan(100)) {
                    if (ten < 0 && digit < 0) {
                        ten = rawNumber.integerValue();
                        // ten contains also the digit, but set to 0 to prevent double digit
                        digit = 0;
                    } else {
                        break; // unexpected double ten or ten after digit
                    }
                } else if (rawNumber.lessThan(1000)) {
                    if (hundred < 0 && ten < 0 && digit < 0) {
                        hundred = rawNumber.integerValue();
                        // hundred contains also the digit, but set to 0 to prevent double digit/ten
                        ten = 0;
                        digit = 0;
                    } else {
                        break; // unexpected double hundred or hundred after digit or ten
                    }
                } else {
                    break; // raw number is too big, not smaller than 1000
                }

                // this point is reached only if the raw number was accepted
                ordinal = ts.get(nextNotIgnore + 1).hasCategory("ordinal_suffix");
                if (ordinal) {
                    ts.movePositionForwardBy(nextNotIgnore + 2);
                    break; // raw number followed by st/nd/rd/th, nothing else allowed, e.g. 407th
                }

            } else {
                break; // random token encountered, number is terminated
            }

            ts.movePositionForwardBy(nextNotIgnore + 1);
            if (ts.get(-1).hasCategory("ordinal")) {
                // ordinal number encountered, nothing else can follow, e.g. two hundredth
                ordinal = true;
                break;
            }
        }

        if (hundred < 0 && ten < 0 && digit < 0) {
            return null;
        } else {
            return new Number((hundred < 0 ? 0 : hundred) + (ten < 0 ? 0 : ten)
                    + (digit < 0 ? 0 : digit), ordinal); // e.g. one hundred and twelve
        }
    }

    public static boolean isRawNumber(final Token token) {
        return token.hasCategory("number") && token.hasCategory("raw");
    }
}
