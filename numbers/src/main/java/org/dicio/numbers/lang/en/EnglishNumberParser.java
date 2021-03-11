package org.dicio.numbers.lang.en;

import org.dicio.numbers.parser.lexer.NumberToken;
import org.dicio.numbers.parser.lexer.Token;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.util.Number;

public class EnglishNumberParser {

    private final TokenStream ts;

    EnglishNumberParser(final TokenStream tokenStream) {
        this.ts = tokenStream;
    }

    Number numberSignPoint(final boolean preferOrdinal) {
        if (ts.get(0).hasCategory("sign")) {
            // parse sign from e.g. "minus twelve"

            boolean negative = ts.get(0).hasCategory("negative");
            ts.movePositionForwardBy(1);

            final Number n = numberPoint(preferOrdinal);
            if (n == null) {
                ts.movePositionForwardBy(-1); // rewind
                return null;
            } else {
                return n.multiply(negative ? -1 : 1);
            }

        }
        return numberPoint(preferOrdinal);
    }

    Number numberPoint(final boolean preferOrdinal) {
        Number n = numberShortScale(preferOrdinal);
        if (n != null && n.isOrdinal()) {
            return n; // no point or fraction separator can appear after an ordinal number
        }

        if (ts.get(0).hasCategory("point")) {
            // parse point indicator from e.g. "twenty one point four five three"

            if (!ts.get(1).hasCategory("digit_after_point") && !isRawNumber(ts.get(1))) {
                return n; // there is an only point at the end of the number: it is not part of it
            }

            ts.movePositionForwardBy(1);
            if (n == null) {
                n = new Number(0.0); // numbers can start with just "point"
            }

            double magnitude = 0.1;
            if (ts.get(0).getValue().length() > 1 && isRawNumber(ts.get(0))) {
                // handle sequence of raw digits after point, e.g. .0123
                // value.length > 1 since multiple single-digits are handled below, e.g. . 0 1 2 3
                for (int i = 0; i < ts.get(0).getValue().length(); ++i) {
                    n = n.plus((ts.get(0).getValue().charAt(i) - '0') * magnitude);
                    magnitude /= 10;
                }
                ts.movePositionForwardBy(1);

            } else {
                // read as many digits as possible, e.g. point one six 5 one 0 three
                while (true) {
                    if (ts.get(0).hasCategory("digit_after_point")
                            || (ts.get(0).getValue().length() == 1 && isRawNumber(ts.get(0)))) {
                        n = n.plus(ts.get(0).getNumber().multiply(magnitude));
                        magnitude /= 10;
                    } else {
                        break; // reached a word that is not a digit
                    }
                    ts.movePositionForwardBy(1);
                }
            }

        } else if (n != null && ts.get(0).hasCategory("fraction_separator")) {
            // parse fraction from e.g. "twenty divided by one hundred"

            int separatorLength = 1;
            if (ts.get(1).hasCategory("fraction_separator_secondary")) {
                separatorLength = 2; // also remove "by" after "divided by"
            }

            ts.movePositionForwardBy(separatorLength);
            final Number denominator = numberShortScale(false);
            if (denominator == null) {
                ts.movePositionForwardBy(-separatorLength); // not a fraction, reset
            } else {
                return n.divide(denominator);
            }
        }

        return n;
    }

    Number numberShortScale(final boolean preferOrdinal) {
        if (ts.get(0).hasCategory("ignore")
                && (!ts.get(0).isValue("a") || ts.get(1).hasCategory("ignore"))) {
            return null; // do not eat ignored words at the beginning, expect a (see e.g. a hundred)
        }

        // read as many groups as possible (e.g. 123 billion + 45 million + 6 thousand + 78)
        Number groups = null;
        long lastMultiplier = Long.MAX_VALUE;
        while (true) {
            final Number group = numberGroupShortScale(preferOrdinal, lastMultiplier);
            if (group == null) {
                break; // either nothing else was found or next multiplier is bigger than last one
            } else if (groups == null) {
                groups = group; // first group
            } else {
                groups = groups.plus(group); // e.g. seven hundred thousand + thirteen
            }

            if (group.isOrdinal()) {
                groups.setOrdinal(true);
                break; // ordinal numbers terminate at the ordinal group
            }
            lastMultiplier = group.integerValue();
        }

        if (groups == null) {
            // try to parse big raw numbers (bigger than 999), e.g. 1207
            final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
            if (isRawNumber(ts.get(nextNotIgnore))) {
                ts.movePositionForwardBy(nextNotIgnore + 1);
                groups = ts.get(-1).getNumber(); // just a big number in raw form, e.g. 1250067
                if (ts.get(0).hasCategory("ordinal_suffix")) {
                    ts.movePositionForwardBy(1);
                    return groups.setOrdinal(true);
                }
                return groups;
            } else {
                return null; // nothing was found
            }
        }
        // groups != null from here on

        if (groups.isOrdinal()) {
            return groups; // no more checks, as the ordinal word comes last, e.g. million twelfth
        }

        if (groups.lessThan(21) && groups.moreThan(9)) {
            // parse years (from 1001 to 2099) in the particular forms (but hundred is below)
            // use nextNotIgnore to skip -, e.g. nineteen-oh-two
            final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);

            if (ts.get(nextNotIgnore).isNumberEqualTo(0)) {
                final int digitIndex = ts.indexOfWithoutCategory("ignore", nextNotIgnore + 1);
                if (ts.get(digitIndex) instanceof NumberToken
                        && ts.get(digitIndex).getNumber().lessThan(10)) {
                    // n + o/oh/nought/zero/0 + digit, e.g. sixteen oh one -> 1601
                    ts.movePositionForwardBy(digitIndex + 1);
                    return groups.multiply(100).plus(ts.get(-1).getNumber());
                }

            } else if (ts.get(nextNotIgnore).hasCategory("teen")
                    || (isRawNumber(ts.get(nextNotIgnore))
                    && ts.get(0).getNumber().lessThan(100))) {
                // n + teen, e.g. twenty thirteen -> 2013
                ts.movePositionForwardBy(nextNotIgnore + 1);
                return groups.multiply(100).plus(ts.get(-1).getNumber());

            } else if (ts.get(nextNotIgnore).hasCategory("tens")) {
                // n + tens (+ digit), e.g. nineteen eighty four -> 1984
                groups = groups.multiply(100).plus(ts.get(nextNotIgnore).getNumber());
                ts.movePositionForwardBy(nextNotIgnore + 1);

                final int digitIndex = ts.indexOfWithoutCategory("ignore", 0);
                if (ts.get(digitIndex).hasCategory("digit")) {
                    // digit is optional (e.g. seventeen fifty -> 1750 would do as well)
                    groups = groups.plus(ts.get(digitIndex).getNumber());
                    ts.movePositionForwardBy(digitIndex + 1);
                }
                return groups;
            }
        }

        if (groups.lessThan(100)) {
            final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
            if (ts.get(nextNotIgnore).hasCategory("hundred")) {
                // parse numbers suffixed by hundred, e.g. twenty six hundred -> 2600
                ts.movePositionForwardBy(nextNotIgnore + 1);
                return groups.multiply(100);
            }
        }

        if (groups.lessThan(1000)) {
            // parse raw number groups separated by comma, e.g. 123,045,006
            // assuming current position is at the first comma
            if (isRawNumber(ts.get(-1)) && ts.get(0).hasCategory("thousand_separator")
                    && ts.get(1).getValue().length() == 3 && isRawNumber(ts.get(1))) {
                while (ts.get(0).hasCategory("thousand_separator") && isRawNumber(ts.get(1))
                        && ts.get(1).getNumber().lessThan(1000)
                        && ts.get(1).getValue().length() == 3) {
                    groups = groups.multiply(1000).plus(ts.get(1).getNumber());
                    ts.movePositionForwardBy(2); // do not allow ignored words in between
                }
            }
        }

        return groups; // e.g. six million, three hundred and twenty seven
    }

    Number numberGroupShortScale(final boolean preferOrdinal, final long lastMultiplier) {
        final int originalPosition = ts.getPosition();
        final Number groupValue = numberLessThan1000(preferOrdinal); // e.g. one hundred and twelve

        if (groupValue != null && groupValue.isOrdinal()) {
            // ordinal numbers can't be followed by a multiplier
            return groupValue;
        }

        final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
        if (ts.get(nextNotIgnore).hasCategory("multiplier")) {
            final Number multiplier = ts.get(nextNotIgnore).getNumber();
            final boolean ordinal = ts.get(nextNotIgnore).hasCategory("ordinal");
            if (multiplier.lessThan(lastMultiplier) && (preferOrdinal || !ordinal)) {
                // prevent ordinal numbers if preferOrdinal is false
                ts.movePositionForwardBy(nextNotIgnore + 1);
                if (groupValue == null) {
                    // the multiplier alone, e.g. a million
                    return multiplier.setOrdinal(ordinal);
                } else {
                    // number smaller than 1000 followed by a multiplier, e.g. thirteen billion
                    return multiplier.multiply(groupValue).setOrdinal(ordinal);
                }
            }
        } else if (lastMultiplier >= 1000) {
            return groupValue; // no multiplier for this last number group, e.g. one hundred and two
        }

        // invalid multiplier or missing multiplier with big group value, reset to previous position
        ts.setPosition(originalPosition);
        return null;
    }

    final Number numberLessThan1000(final boolean preferOrdinal) {
        long hundred = -1, ten = -1, digit = -1;
        boolean ordinal = false;
        while (true) {
            final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
            if (!preferOrdinal && ts.get(nextNotIgnore).hasCategory("ordinal")
                    && !ts.get(nextNotIgnore).isNumberEqualTo(1)
                    && !ts.get(nextNotIgnore).isNumberEqualTo(2)) {
                // prevent ordinal numbers if preferOrdinal is false, but allow first and second,
                // since those can't mean anything else than ordinal, e.g. twenty first
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

                if (!preferOrdinal && ts.get(nextNotIgnore + 1).hasCategory("ordinal_suffix")) {
                    break; // do not allow ordinals if preferOrdinal is false
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
                    break; // raw number followed by th/nd/st, nothing else allowed, e.g. 407th
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
                    + (digit < 0 ? 0 : digit)).setOrdinal(ordinal); // e.g. one hundred and twelve
        }
    }

    private static boolean isRawNumber(final Token token) {
        return token.hasCategory("number") && token.hasCategory("raw");
    }
}
