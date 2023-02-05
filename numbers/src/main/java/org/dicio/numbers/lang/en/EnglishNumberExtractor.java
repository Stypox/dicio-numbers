package org.dicio.numbers.lang.en;

import org.dicio.numbers.parser.lexer.NumberToken;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Number;
import org.dicio.numbers.util.NumberExtractorUtils;

import static org.dicio.numbers.util.NumberExtractorUtils.*;

public class EnglishNumberExtractor {

    private final TokenStream ts;
    private final boolean shortScale;

    EnglishNumberExtractor(final TokenStream tokenStream,
                           final boolean shortScale) {
        this.ts = tokenStream;
        this.shortScale = shortScale;
    }

    Number numberPreferOrdinal() {
        // first try with suffix multiplier, e.g. dozen
        Number number = numberSuffixMultiplier();
        if (number == null) {
            number = numberSignPoint(true); // then try with normal number
        }

        // maybe there is a valid denominator? (note: number could be null, e.g. a tenth)
        return divideByDenominatorIfPossible(number);
    }

    Number numberPreferFraction() {
        // first try with suffix multiplier, e.g. dozen
        Number number = numberSuffixMultiplier();
        if (number == null) {
            number = numberSignPoint(false); // then try without ordinal
        }

        // maybe there is a valid denominator? (note: number could be null, e.g. a tenth)
        // note that e.g. "a couple halves" ends up here, but that's valid
        number = divideByDenominatorIfPossible(number);

        if (number == null) {
            // maybe an ordinal number?
            number = numberSignPoint(true);
        }
        return number;
    }

    Number numberNoOrdinal() {
        // for now this function is used internally just for duration parsing, but maybe it could
        // be exposed to library users, giving more control over how ordinals are handled.

        // first try with suffix multiplier, e.g. dozen
        Number number = numberSuffixMultiplier();
        if (number == null) {
            number = numberSignPoint(false); // then try without ordinal
        }

        // maybe there is a valid denominator? (note: number could be null, e.g. a tenth)
        // note that e.g. "a couple halves" ends up here, but that's valid
        number = divideByDenominatorIfPossible(number);

        return number;
    }


    Number divideByDenominatorIfPossible(final Number numberToEdit) {
        if (numberToEdit == null) {
            if (ts.get(0).isValue("a")) {
                // handle cases where
                final int originalPosition = ts.getPosition();
                ts.movePositionForwardBy(1);

                final Number denominator = numberInteger(true);
                if (denominator != null && denominator.isOrdinal() && denominator.moreThan(2)) {
                    return new Number(1).divide(denominator); // valid denominator, e.g. a tenth
                } else {
                    // missing or invalid denominator, e.g. a hello, a four
                    ts.setPosition(originalPosition); // restore to original position
                }
            }
            return null;
        }

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

    Number numberSignPoint(final boolean allowOrdinal) {
        return signBeforeNumber(ts, () -> numberPoint(allowOrdinal));
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

            int originalPosition = ts.getPosition();
            ts.movePositionForwardBy(1);
            if (ts.get(0).hasCategory("fraction_separator_secondary")) {
                ts.movePositionForwardBy(1); // also remove "by" after "divided by"
            }

            final Number denominator = numberInteger(false);
            if (denominator == null
                    || (denominator.isInteger() && denominator.integerValue() == 0)
                    || (denominator.isDecimal() && denominator.decimalValue() == 0.0)) {
                ts.setPosition(originalPosition); // not a fraction or division by zero, reset
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

        Number n = numberMadeOfGroups(ts, allowOrdinal, shortScale ? NumberExtractorUtils::numberGroupShortScale
                : EnglishNumberExtractor::numberGroupLongScale);
        if (n == null) {
            return numberBigRaw(ts, allowOrdinal); // try to parse big raw numbers (>=1000), e.g. 1207
        } else if (n.isOrdinal()) {
            return n; // no more checks, as the ordinal word comes last, e.g. million twelfth
        }
        // n != null from here on

        if (n.lessThan(21) && n.moreThan(9) && !ts.get(-1).hasCategory("raw")) {
            // parse years (1001 to 2099) in the particular forms (but xx-hundred is handled below)
            final Number secondGroup = numberYearSecondGroup(allowOrdinal);
            if (secondGroup != null) {
                return n.multiply(100).plus(secondGroup).withOrdinal(secondGroup.isOrdinal());
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
                    return n.multiply(100).withOrdinal(ordinal);
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
                        return n.withOrdinal(true); // ordinal number, e.g. 20,056,789th
                    } else {
                        ts.setPosition(originalPosition);
                        return null; // found ordinal number, revert since allowOrdinal is false
                    }
                }
            }
        }

        return n; // e.g. six million, three hundred and twenty seven
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
                return ts.get(-1).getNumber().withOrdinal(ordinal);
            }

        } else if (ts.get(nextNotIgnore).hasCategory("teen")) {
            // teen, e.g. (twenty) thirteen -> (20)13
            final boolean ordinal = ts.get(nextNotIgnore).hasCategory("ordinal");
            if (!allowOrdinal && ordinal) {
                return null; // do not allow ordinal number if allowOrdinal is false
            } else {
                ts.movePositionForwardBy(nextNotIgnore + 1);
                return ts.get(-1).getNumber().withOrdinal(ordinal);
            }

        } else if (ts.get(nextNotIgnore).getValue().length() == 2
                && isRawNumber(ts.get(nextNotIgnore))) {
            // raw number with two digits, e.g. (twenty) 41 -> (20)41, (12) 05 th -> (12)05th
            final boolean ordinal = ts.get(nextNotIgnore + 1).hasCategory("ordinal_suffix");
            if (!allowOrdinal && ordinal) {
                return null; // do not allow raw number + st/nd/rd/th if allowOrdinal is false
            } else {
                ts.movePositionForwardBy(nextNotIgnore + (ordinal ? 2 : 1));
                return ts.get(ordinal ? -2 : -1).getNumber().withOrdinal(ordinal);
            }

        } else if (ts.get(nextNotIgnore).hasCategory("tens")) {
            // tens (+ digit), e.g. (nineteen) eighty four -> (19)84
            final Number tens = ts.get(nextNotIgnore).getNumber();
            if (ts.get(nextNotIgnore).hasCategory("ordinal")) {
                if (allowOrdinal) {
                    // nothing follows an ordinal number, e.g. (twenty) twentieth -> 2020th
                    ts.movePositionForwardBy(nextNotIgnore + 1);
                    return tens.withOrdinal(true);
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
                return tens.plus(ts.get(-1).getNumber()).withOrdinal(ordinal);
            } else {
                return tens; // digit is optional, e.g. (seventeen) fifty -> (17)50
            }
        }

        return null; // invalid second year group
    }

    static Number numberGroupLongScale(final TokenStream ts, final boolean allowOrdinal, final double lastMultiplier) {
        if (lastMultiplier < 1000000) {
            return null; // prevent two numbers smaller than 1000000 to be one after another
        }

        final int originalPosition = ts.getPosition();
        Number first = numberGroupShortScale(ts, allowOrdinal, 1000000);
        if (first == null) {
            // there is no number or the number is followed by a multiplier which is not thousand
            first = numberLessThan1000(ts, allowOrdinal);
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
                        return ts.get(-2).getNumber().withOrdinal(true);
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

            final Number second = numberLessThan1000(ts, allowOrdinal);
            if (second != null) {
                first = first.plus(second);
                if (second.isOrdinal()) {
                    return first.withOrdinal(true); // nothing else follows an ordinal number
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
                    return multiplier.withOrdinal(ordinal);
                } else {
                    // number smaller than 1000000 followed by a multiplier,
                    // e.g. thirteen thousand billion
                    return multiplier.multiply(first).withOrdinal(ordinal);
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

    static Number shortMultiplierToLongScale(final Number shortScaleMultiplier) {
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
}
