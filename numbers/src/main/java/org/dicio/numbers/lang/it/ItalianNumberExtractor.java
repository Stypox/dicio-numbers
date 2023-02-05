package org.dicio.numbers.lang.it;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Number;
import org.dicio.numbers.util.NumberExtractorUtils;

import static org.dicio.numbers.util.NumberExtractorUtils.*;

public class ItalianNumberExtractor {

    private final TokenStream ts;

    ItalianNumberExtractor(final TokenStream tokenStream) {
        this.ts = tokenStream;
    }

    Number numberPreferOrdinal() {
        // first try with suffix multiplier, e.g. dozen
        Number number = numberSuffixMultiplier();
        if (number == null) {
            number = numberSignPoint(true); // then try with normal number
        }

        if (number != null) {
            // a number was found, maybe it has a valid denominator?
            number = divideByDenominatorIfPossible(number);
        }
        return number;
    }

    Number numberPreferFraction() {
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

        if (number != null) {
            // a number was found, maybe it has a valid denominator?
            // note that e.g. "una mezza coppia" ends up here, but that's valid
            number = divideByDenominatorIfPossible(number);
        }

        return number;
    }

    Number divideByDenominatorIfPossible(final Number numberToEdit) {
        // if numberToEdit is directly followed by an ordinal number then it is a fraction (only if numberToEdit is not
        // ordinal or already decimal). Note: a big integer (i.e. 10^24) would be decimal, here we are assuming that
        // such a number will never have a fraction after it for simplicity.

        if (!numberToEdit.isOrdinal() && !numberToEdit.isDecimal()
                && !ts.get(0).hasCategory("ignore")) {
            final int originalPosition = ts.getPosition();
            final Number denominator = numberInteger(true);
            if (denominator == null) {
                // no denominator found: maybe a custom multiplier? e.g. mezzo (=0.5), dozzina (=12)
                if (ts.get(0).hasCategory("suffix_multiplier")) {
                    ts.movePositionForwardBy(1);

                    final Number multiplier = ts.get(-1).getNumber();
                    if (multiplier.isDecimal() && ((long) (1 / multiplier.decimalValue()))
                            == (1 / multiplier.decimalValue())) {
                        // the multiplier is an exact fraction, divide by the denominator converted
                        // to long to possibly preserve the integerness of numberToEdit, e.g.
                        // sedici mezzi should be 8, not 8.0
                        return numberToEdit.divide((long) (1 / multiplier.decimalValue()));
                    }

                    return numberToEdit.multiply(multiplier);
                }
            } else if (denominator.isOrdinal() && denominator.moreThan(2)) {
                return numberToEdit.divide(denominator); // valid denominator, e.g. un quinto
            } else {
                // invalid denominator, e.g. sei primi
                ts.setPosition(originalPosition); // restore to original position
            }
        }
        return numberToEdit;
    }

    Number numberSuffixMultiplier() {
        if (ts.get(0).hasCategory("suffix_multiplier")) {
            ts.movePositionForwardBy(1);
            return ts.get(-1).getNumber(); // a suffix multiplier, e.g. dozen, half, score, percent
        } else {
            return null;
        }
    }

    Number numberSignPoint(final boolean allowOrdinal) {
        return signBeforeNumber(ts, () -> numberPoint(allowOrdinal));
    }

    Number numberPoint(final boolean allowOrdinal) {
        Number n = numberInteger(allowOrdinal);
        if (n == null || n.isOrdinal()) {
            // numbers can not start with just "virgola"
            // no point or fraction separator can appear after an ordinal number
            return n;
        }

        if (ts.get(0).hasCategory("point")) {
            // parse point indicator from e.g. "twenty one point four five three"

            if (!ts.get(1).hasCategory("digit_after_point")
                    && (!isRawNumber(ts.get(1)) || ts.get(2).hasCategory("ordinal_suffix"))) {
                // also return if next up is an ordinal raw number, i.e. followed by °/esimo
                return n; // there is an only comma at the end of the number: it is not part of it
            }
            ts.movePositionForwardBy(1);

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

        } else if (ts.get(0).hasCategory("fraction_separator")) {
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
        if (ts.get(0).hasCategory("ignore")) {
            return null; // do not eat ignored words at the beginning
        }

        Number n = numberMadeOfGroups(ts, allowOrdinal, NumberExtractorUtils::numberGroupShortScale);
        if (n == null) {
            return numberBigRaw(ts, allowOrdinal); // try to parse big raw numbers (>=1000), e.g. 1207
        } else if (n.isOrdinal()) {
            return n; // no more checks, as the ordinal word comes last, e.g. million twelfth
        }
        // n != null from here on

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
}
