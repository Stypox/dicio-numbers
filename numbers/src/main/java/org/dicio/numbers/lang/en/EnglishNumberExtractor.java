package org.dicio.numbers.lang.en;

import org.dicio.numbers.parser.lexer.NumberToken;
import org.dicio.numbers.parser.lexer.Token;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.util.Number;

import java.util.ArrayList;
import java.util.List;

public class EnglishNumberExtractor {

    private final TokenStream ts;
    private final boolean shortScale;
    private final boolean preferOrdinal;

    EnglishNumberExtractor(final TokenStream tokenStream,
                           final boolean shortScale,
                           final boolean preferOrdinal) {
        this.ts = tokenStream;
        this.shortScale = shortScale;
        this.preferOrdinal = preferOrdinal;
    }

    public List<Object> extractNumbers() {
        List<Object> textAndNumbers = new ArrayList<>();
        final StringBuilder currentText = new StringBuilder();

        // the called functions will add objects to textAndNumbers and reuse currentText for strings
        if (preferOrdinal) {
            extractNumbersPreferOrdinal(textAndNumbers, currentText);
        } else {
            extractNumbersPreferFraction(textAndNumbers, currentText);
        }

        if (currentText.length() != 0) {
            // add leftover text (this can be done here since the functions above reuse currentText)
            textAndNumbers.add(currentText.toString());
        }

        return textAndNumbers;
    }

    void extractNumbersPreferOrdinal(final List<Object> textAndNumbers,
                                     final StringBuilder currentText) {
        while (!ts.finished()) {
            // first try with suffix multiplier, e.g. dozen
            Number number = numberSuffixMultiplier();
            if (number == null) {
                number = numberSignPoint(true); // then try with normal number
            }

            if (number != null) {
                // a number was found, maybe it has a valid denominator?
                number = divideByDenominatorIfPossible(number);
            }
            addNumberOrText(number, textAndNumbers, currentText);
        }
    }

    void extractNumbersPreferFraction(final List<Object> textAndNumbers,
                                      final StringBuilder currentText) {
        while (!ts.finished()) {
            // first try with suffix multiplier, e.g. dozen
            Number number = numberSuffixMultiplier();
            if (number == null) {
                number = numberSignPoint(false); // then try without ordinal
            }

            if (number == null) {
                // maybe an ordinal number?
                number = numberSignPoint(true);
            } else {
                // a number was found, maybe it has a valid denominator?
                // note that e.g. "a couple halves" ends up here, but that's valid
                number = divideByDenominatorIfPossible(number);
            }
            addNumberOrText(number, textAndNumbers, currentText);
        }
    }

    Number divideByDenominatorIfPossible(final Number numberToEdit) {
        // if numberToEdit is directly followed by an ordinal number then it is a fraction (only if
        // numberToEdit is not ordinal or already decimal). Note: a big long scale integer (i.e.
        // 10^24) would be decimal, here we are assuming that such a number will never have a
        // fraction after it for simplicity.

        if (!numberToEdit.isOrdinal() && !numberToEdit.isDecimal()
                && !ts.get(0).hasCategory("ignore")) {
            final int originalPosition = ts.getPosition();
            final Number denominator = numberInteger(true);
            if (denominator == null) {
                // no denominator found: maybe a custom multiplier? e.g. half (=0.5), dozen (=12)
                if (ts.get(0).hasCategory("suffix_multiplier")) {
                    ts.movePositionForwardBy(1);

                    final Number multiplier = ts.get(-1).getNumber();
                    if (multiplier.isDecimal() && ((long) (1 / multiplier.decimalValue()))
                            == (1 / multiplier.decimalValue())) {
                        // the multiplier is an exact fraction, divide by the denominator converted
                        // to long to possibly preserve the integerness of numberToEdit, e.g.
                        // sixteen quarters should be 4, not 4.0
                        return numberToEdit.divide((long) (1 / multiplier.decimalValue()));
                    }

                    return numberToEdit.multiply(multiplier);
                }
            } else if (denominator.isOrdinal() && denominator.moreThan(2)) {
                return numberToEdit.divide(denominator); // valid denominator, e.g. one fifth
            } else {
                // invalid denominator, e.g. three two, four second
                ts.setPosition(originalPosition); // restore to original position
            }
        }
        return numberToEdit;
    }

    Number numberSuffixMultiplier() {
        if (ts.get(0).hasCategory("suffix_multiplier")) {
            ts.movePositionForwardBy(1);
            return ts.get(-1).getNumber(); // a suffix multiplier, e.g. dozen, half, score, percent
        } else if (ts.get(0).isValue("a") && ts.get(1).hasCategory("suffix_multiplier")) {
            ts.movePositionForwardBy(2); // also skip "a" before the suffix, e.g. a dozen
            return ts.get(-1).getNumber(); // a suffix multiplier preceded by "a", e.g. a quarter
        } else {
            return null;
        }
    }

    void addNumberOrText(final Number number,
                         final List<Object> textAndNumbers,
                         final StringBuilder currentText) {
        if (number == null) {
            // no number here, add the text of the current token to currentText instead
            currentText.append(ts.get(0).getValue()).append(ts.get(0).getSpacesFollowing());
            ts.movePositionForwardBy(1);
        } else {
            if (currentText.length() != 0) {
                textAndNumbers.add(currentText.toString()); // add the text before the number
                currentText.setLength(0); // clear the string builder efficiently
            }
            textAndNumbers.add(number);
            currentText.append(ts.get(-1).getSpacesFollowing()); // spaces after the number
        }
    }

    Number numberSignPoint(final boolean allowOrdinal) {
        if (ts.get(0).hasCategory("sign")) {
            // parse sign from e.g. "minus twelve"

            boolean negative = ts.get(0).hasCategory("negative");
            ts.movePositionForwardBy(1);

            final Number n = numberPoint(allowOrdinal);
            if (n == null) {
                ts.movePositionForwardBy(-1); // rewind
                return null;
            } else {
                return n.multiply(negative ? -1 : 1).setOrdinal(n.isOrdinal());
            }

        }
        return numberPoint(allowOrdinal);
    }

    Number numberPoint(final boolean allowOrdinal) {
        Number n = numberInteger(allowOrdinal);
        if (n != null && n.isOrdinal()) {
            return n; // no point or fraction separator can appear after an ordinal number
        }

        if (ts.get(0).hasCategory("point")) {
            // parse point indicator from e.g. "twenty one point four five three"

            if (!ts.get(1).hasCategory("digit_after_point")
                    && (!isRawNumber(ts.get(1)) || ts.get(2).hasCategory("ordinal_suffix"))) {
                // also return if next up is an ordinal raw number, i.e. followed by st/nd/rd/th
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
                            || (ts.get(0).getValue().length() == 1 && isRawNumber(ts.get(0))
                            && !ts.get(1).hasCategory("ordinal_suffix"))) {
                        // do not allow ordinal raw numbers, i.e. followed by st/nd/rd/th

                        n = n.plus(ts.get(0).getNumber().multiply(magnitude));
                        magnitude /= 10;
                    } else {
                        break; // reached a word that is not a valid digit
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
            final Number denominator = numberInteger(false);
            if (denominator == null) {
                ts.movePositionForwardBy(-separatorLength); // not a fraction, reset
            } else {
                return n.divide(denominator);
            }
        }

        return n;
    }

    Number numberInteger(final boolean allowOrdinal) {
        if (ts.get(0).hasCategory("ignore")
                && (!ts.get(0).isValue("a") || ts.get(1).hasCategory("ignore"))) {
            return null; // do not eat ignored words at the beginning, expect a (see e.g. a hundred)
        }

        Number n = numberShortLongScale(allowOrdinal);
        if (n == null) {
            return numberBigRaw(allowOrdinal); // try to parse big raw numbers (>=1000), e.g. 1207
        } else if (n.isOrdinal()) {
            return n; // no more checks, as the ordinal word comes last, e.g. million twelfth
        }
        // n != null from here on

        if (n.lessThan(21) && n.moreThan(9)) {
            // parse years (1001 to 2099) in the particular forms (but xx-hundred is handled below)
            final Number secondGroup = numberYearSecondGroup(allowOrdinal);
            if (secondGroup != null) {
                return n.multiply(100).plus(secondGroup).setOrdinal(secondGroup.isOrdinal());
            }
        }

        if (n.lessThan(100)) {
            final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
            if (ts.get(nextNotIgnore).hasCategory("hundred")) {
                // parse numbers suffixed by hundred, e.g. twenty six hundred -> 2600
                final boolean ordinal = ts.get(nextNotIgnore).hasCategory("ordinal");
                if (allowOrdinal || !ordinal) {
                    // prevent ordinal numbers if allowOrdinal is false
                    ts.movePositionForwardBy(nextNotIgnore + 1);
                    return n.multiply(100).setOrdinal(ordinal);
                }
            }
        }

        if (n.lessThan(1000)) {
            // parse raw number n separated by comma, e.g. 123,045,006
            // assuming current position is at the first comma
            if (isRawNumber(ts.get(-1)) && ts.get(0).hasCategory("thousand_separator")
                    && ts.get(1).getValue().length() == 3 && isRawNumber(ts.get(1))) {
                final int originalPosition = ts.getPosition() - 1;

                while (ts.get(0).hasCategory("thousand_separator")
                        && ts.get(1).getValue().length() == 3 && isRawNumber(ts.get(1))) {
                    n = n.multiply(1000).plus(ts.get(1).getNumber());
                    ts.movePositionForwardBy(2); // do not allow ignored words in between
                }

                if (ts.get(0).hasCategory("ordinal_suffix")) {
                    if (allowOrdinal) {
                        ts.movePositionForwardBy(1);
                        return n.setOrdinal(true); // ordinal number, e.g. 20,056,789th
                    } else {
                        ts.setPosition(originalPosition);
                        return null; // found ordinal number, revert since allowOrdinal is false
                    }
                }
            }
        }

        return n; // e.g. six million, three hundred and twenty seven
    }

    Number numberBigRaw(final boolean allowOrdinal) {
        // try to parse big raw numbers (bigger than 999), e.g. 1207, 57378th
        final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
        if (isRawNumber(ts.get(nextNotIgnore))) {
            final boolean ordinal = ts.get(nextNotIgnore + 1).hasCategory("ordinal_suffix");
            if (!allowOrdinal && ordinal) {
                return null; // do not allow ordinal if allowOrdinal is false
            } else {
                // a big number in raw form, e.g. 1250067, 5839th
                ts.movePositionForwardBy(nextNotIgnore + (ordinal ? 2 : 1));
                return ts.get(ordinal ? -2 : -1).getNumber().setOrdinal(ordinal);
            }

        } else {
            return null; // nothing was found
        }
    }

    Number numberYearSecondGroup(final boolean allowOrdinal) {
        // parse the last two digits of a year, e.g. oh five -> 05, nineteen -> 19, eighty two -> 82

        // use nextNotIgnore to skip -, e.g. (nineteen)-oh-two
        final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);

        if (ts.get(nextNotIgnore).isNumberEqualTo(0)) {
            final int digitIndex = ts.indexOfWithoutCategory("ignore", nextNotIgnore + 1);
            final boolean ordinal = ts.get(digitIndex).hasCategory("ordinal");
            if (ts.get(digitIndex) instanceof NumberToken
                    && ts.get(digitIndex).getNumber().lessThan(10)
                    && (allowOrdinal || !ordinal)) {
                // o/oh/nought/zero/0 + digit, e.g. (sixteen) oh one -> (16)01
                // prevent ordinal number if allowOrdinal is false, e.g. (eighteen) oh second
                ts.movePositionForwardBy(digitIndex + 1);
                return ts.get(-1).getNumber().setOrdinal(ordinal);
            }

        } else if (ts.get(nextNotIgnore).hasCategory("teen")) {
            // teen, e.g. (twenty) thirteen -> (20)13
            final boolean ordinal = ts.get(nextNotIgnore).hasCategory("ordinal");
            if (!allowOrdinal && ordinal) {
                return null; // do not allow ordinal number if allowOrdinal is false
            } else {
                ts.movePositionForwardBy(nextNotIgnore + 1);
                return ts.get(-1).getNumber().setOrdinal(ordinal);
            }

        } else if (ts.get(nextNotIgnore).getValue().length() == 2
                && isRawNumber(ts.get(nextNotIgnore))) {
            // raw number with two digits, e.g. (twenty) 41 -> (20)41, (12) 05 th -> (12)05th
            final boolean ordinal = ts.get(nextNotIgnore + 1).hasCategory("ordinal_suffix");
            if (!allowOrdinal && ordinal) {
                return null; // do not allow raw number + st/nd/rd/th if allowOrdinal is false
            } else {
                ts.movePositionForwardBy(nextNotIgnore + (ordinal ? 2 : 1));
                return ts.get(ordinal ? -2 : -1).getNumber().setOrdinal(ordinal);
            }

        } else if (ts.get(nextNotIgnore).hasCategory("tens")) {
            // tens (+ digit), e.g. (nineteen) eighty four -> (19)84
            final Number tens = ts.get(nextNotIgnore).getNumber();
            if (ts.get(nextNotIgnore).hasCategory("ordinal")) {
                if (allowOrdinal) {
                    // nothing follows an ordinal number, e.g. (twenty) twentieth -> 2020th
                    ts.movePositionForwardBy(nextNotIgnore + 1);
                    return tens.setOrdinal(true);
                } else {
                    return null; // prevent ordinal numbers if allowOrdinal is false
                }
            }
            ts.movePositionForwardBy(nextNotIgnore + 1);

            final int digitIndex = ts.indexOfWithoutCategory("ignore", 0);
            final boolean ordinal = ts.get(digitIndex).hasCategory("ordinal");
            if (ts.get(digitIndex).hasCategory("digit") && (allowOrdinal || !ordinal)) {
                // do not consider ordinal digit if allowOrdinal is false
                ts.movePositionForwardBy(digitIndex + 1);
                return tens.plus(ts.get(-1).getNumber()).setOrdinal(ordinal);
            } else {
                return tens; // digit is optional, e.g. (seventeen) fifty -> (17)50
            }
        }

        return null; // invalid second year group
    }

    Number numberShortLongScale(final boolean allowOrdinal) {
        // read as many groups as possible (e.g. 123 billion + 45 million + 6 thousand + 78)
        Number groups = null;
        double lastMultiplier = Double.MAX_VALUE;
        while (true) {
            final Number group;
            if (shortScale) {
                group = numberGroupShortScale(allowOrdinal, lastMultiplier);
            } else {
                group = numberGroupLongScale(allowOrdinal, lastMultiplier);
            }

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
            lastMultiplier = group.isDecimal() ? group.decimalValue() : group.integerValue();
        }
        return groups;
    }

    Number numberGroupShortScale(final boolean allowOrdinal, final double lastMultiplier) {
        if (lastMultiplier < 1000) {
            return null; // prevent two numbers smaller than 1000 to be one after another
        }

        final int originalPosition = ts.getPosition();
        final Number groupValue = numberLessThan1000(allowOrdinal); // e.g. one hundred and twelve
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
                    return multiplier.setOrdinal(ordinal);
                } else {
                    // number smaller than 1000 followed by a multiplier, e.g. thirteen billion
                    return multiplier.multiply(groupValue).setOrdinal(ordinal);
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

    Number numberGroupLongScale(final boolean allowOrdinal, final double lastMultiplier) {
        if (lastMultiplier < 1000000) {
            return null; // prevent two numbers smaller than 1000000 to be one after another
        }

        final int originalPosition = ts.getPosition();
        Number first = numberGroupShortScale(allowOrdinal, 1000000);
        if (first == null) {
            // there is no number or the number is followed by a multiplier which is not thousand
            first = numberLessThan1000(allowOrdinal);
            if (first != null && first.isOrdinal()) {
                return first;
            }

            if (first == null) {
                final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
                if (isRawNumber(ts.get(nextNotIgnore))
                        && ts.get(nextNotIgnore).getNumber().lessThan(1000000)) {
                    // maybe a raw number smaller than 1000000, e.g. 785743
                    final boolean ordinal = ts.get(nextNotIgnore + 1).hasCategory("ordinal_suffix");
                    if (ordinal) {
                        if (!allowOrdinal) {
                            // do not allow raw number + st/nd/rd/th if allowOrdinal is false
                            return null;
                        }
                        ts.movePositionForwardBy(nextNotIgnore + 2);
                        return ts.get(-2).getNumber().setOrdinal(true);
                    }
                    ts.movePositionForwardBy(nextNotIgnore + 1);
                    first = ts.get(-1).getNumber(); // raw number group, e.g. 123042 million
                }
            }

        } else {
            if (first.isOrdinal() || first.lessThan(1000)) {
                // nothing else follows an ordinal number; the number does not end with thousand
                return first;
            }

            final Number second = numberLessThan1000(allowOrdinal);
            if (second != null) {
                first = first.plus(second);
                if (second.isOrdinal()) {
                    return first.setOrdinal(true); // nothing else follows an ordinal number
                }
            }
        }

        final int nextNotIgnore = ts.indexOfWithoutCategory("ignore", 0);
        final boolean ordinal = ts.get(nextNotIgnore).hasCategory("ordinal");
        if (ts.get(nextNotIgnore).hasCategory("multiplier") && (allowOrdinal || !ordinal)
                && ts.get(nextNotIgnore).getNumber().moreThan(1000)) {
            // prevent ordinal multiplier if allowOrdinal is false; prevent thousand multiplier
            final Number multiplier = shortMultiplierToLongScale(ts.get(nextNotIgnore).getNumber());
            if (multiplier.lessThan(lastMultiplier)) {
                ts.movePositionForwardBy(nextNotIgnore + 1);
                if (first == null) {
                    // the multiplier alone, e.g. a million
                    return multiplier.setOrdinal(ordinal);
                } else {
                    // number smaller than 1000000 followed by a multiplier,
                    // e.g. thirteen thousand billion
                    return multiplier.multiply(first).setOrdinal(ordinal);
                }
            }
        } else {
            // no multiplier for this last number group, e.g. one thousand, three hundred and two
            // also here if the multiplier is ordinal, but allowOrdinal is false
            // also here if there is a thousand multiplier (happens e.g. in one thousand thousand)
            return first;
        }

        // invalid multiplier or missing multiplier with big group value, reset to previous position
        ts.setPosition(originalPosition);
        return null;
    }

    final Number shortMultiplierToLongScale(final Number shortScaleMultiplier) {
        if (shortScaleMultiplier.integerValue() == 1000000000L) {
            return new Number(1000000000000L); // billion
        } else if (shortScaleMultiplier.integerValue() == 1000000000000L) {
            return new Number(1000000000000000000L); // trillion
        } else if (shortScaleMultiplier.integerValue() == 1000000000000000L) {
            return new Number(1e24d); // quadrillion
        } else if (shortScaleMultiplier.integerValue() == 1000000000000000000L) {
            return new Number(1e30d); // quintillion
        } else {
            return shortScaleMultiplier; // million
        }
    }

    final Number numberLessThan1000(final boolean allowOrdinal) {
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
                    + (digit < 0 ? 0 : digit)).setOrdinal(ordinal); // e.g. one hundred and twelve
        }
    }

    private static boolean isRawNumber(final Token token) {
        return token.hasCategory("number") && token.hasCategory("raw");
    }
}
