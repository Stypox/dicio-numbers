package org.dicio.numbers.util

import org.dicio.numbers.parser.lexer.Token
import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.unit.Number

object NumberExtractorUtils {
    @JvmStatic
    fun extractOneIntegerInRange(
        ts: TokenStream,
        fromInclusive: Int,
        toInclusive: Int,
        numberSupplier: () -> Number?
    ): Int? {
        val originalPosition = ts.position
        val number = numberSupplier()

        if (number == null || !number.isInteger || number.integerValue() < fromInclusive || number.integerValue() > toInclusive) {
            ts.position = originalPosition
            return null
        }

        return number.integerValue().toInt()
    }


    @JvmStatic
    fun signBeforeNumber(
        ts: TokenStream,
        numberSupplier: () -> Number?
    ): Number? {
        if (ts[0].hasCategory("sign")) {
            // parse sign from e.g. "minus twelve"

            val negative = ts[0].hasCategory("negative")
            ts.movePositionForwardBy(1)

            val n = numberSupplier()
            if (n == null) {
                ts.movePositionForwardBy(-1) // rewind
                return null
            } else {
                return n.multiply((if (negative) -1 else 1).toLong()).withOrdinal(n.isOrdinal)
            }
        }
        return numberSupplier()
    }

    fun numberBigRaw(ts: TokenStream, allowOrdinal: Boolean): Number? {
        // try to parse big raw numbers (bigger than 999), e.g. 1207, 57378th
        if (isRawNumber(ts[0])) {
            val ordinal = ts[1].hasCategory("ordinal_suffix")
            if (!allowOrdinal && ordinal) {
                return null // do not allow ordinal if allowOrdinal is false
            } else {
                // a big number in raw form, e.g. 1250067, 5839th
                ts.movePositionForwardBy(if (ordinal) 2 else 1)
                return ts[if (ordinal) -2 else -1].number!!.withOrdinal(ordinal)
            }
        } else {
            return null // nothing was found
        }
    }

    fun numberMadeOfGroups(
        ts: TokenStream,
        getNumberGroup: (ts: TokenStream, lastMultiplier: Double) -> Number?
    ): Number? {
        // read as many groups as possible (e.g. 123 billion + 45 million + 6 thousand + 78)
        var groups: Number? = null
        var lastMultiplier = Double.MAX_VALUE
        while (true) {
            val group = ts.tryOrSkipCategory("ignore", groups != null) {
                getNumberGroup(ts, lastMultiplier)
            }

            groups = if (group == null) {
                break // either nothing else was found or next multiplier is bigger than last one
            } else if (groups == null) {
                group // first group
            } else {
                groups.plus(group) // e.g. seven hundred thousand + thirteen
            }

            if (group.isOrdinal) {
                groups = groups.withOrdinal(true)
                break // ordinal numbers terminate at the ordinal group
            }
            lastMultiplier =
                if (group.isDecimal) group.decimalValue() else group.integerValue().toDouble()
        }
        return groups
    }

    @JvmStatic
    fun numberGroupShortScale(
        ts: TokenStream,
        allowOrdinal: Boolean,
        lastMultiplier: Double
    ): Number? {
        if (lastMultiplier < 1000) {
            return null // prevent two numbers smaller than 1000 to be one after another
        }

        val originalPosition = ts.position
        val groupValue = numberLessThan1000(ts, allowOrdinal) // e.g. one hundred and twelve
        if (groupValue != null && groupValue.isOrdinal) {
            // ordinal numbers can't be followed by a multiplier
            return groupValue
        }

        val nextNotIgnore = if (groupValue == null)
            0 // do not skip ahead if nothing was matched so far
        else
            ts.indexOfWithoutCategory("ignore", 0)
        val ordinal = ts[nextNotIgnore].hasCategory("ordinal")
        if (ts[nextNotIgnore].hasCategory("multiplier") && (allowOrdinal || !ordinal)) {
            // prevent ordinal multiplier if allowOrdinal is false
            val multiplier = ts[nextNotIgnore].number
            if (multiplier!!.lessThan(lastMultiplier)) {
                ts.movePositionForwardBy(nextNotIgnore + 1)
                return if (groupValue == null) {
                    // the multiplier alone, e.g. a million
                    multiplier.withOrdinal(ordinal)
                } else {
                    // number smaller than 1000 followed by a multiplier, e.g. thirteen billion
                    multiplier.multiply(groupValue).withOrdinal(ordinal)
                }
            }
        } else {
            // no multiplier for this last number group, e.g. one hundred and two
            // also here if the multiplier is ordinal, but allowOrdinal is false
            return groupValue
        }

        // multiplier is too big, reset to previous position
        ts.position = originalPosition
        return null
    }

    @JvmStatic
    fun numberLessThan1000(ts: TokenStream, allowOrdinal: Boolean): Number? {
        var hundred: Long = -1
        var ten: Long = -1
        var digit: Long = -1
        var ordinal = false
        var firstIteration = true
        while (true) {
            val nextNotIgnore = if (firstIteration) {
                firstIteration = false
                0 // do not skip ahead if nothing was matched so far
            } else {
                ts.indexOfWithoutCategory("ignore", 0)
            }

            if (!allowOrdinal && ts[nextNotIgnore].hasCategory("ordinal")) {
                // prevent ordinal numbers if allowOrdinal is false
                break
            }

            if (ts[nextNotIgnore].hasCategory("digit")) {
                if (digit < 0 && (!ts[nextNotIgnore].isNumberEqualTo(0)
                            || (ten < 0 && hundred < 0))
                ) {
                    // do not allow zero after ten or hundred, e.g. twenty zero or hundred nought
                    digit = ts[nextNotIgnore].number!!.integerValue()
                } else {
                    break // unexpected double digit
                }
            } else if (ts[nextNotIgnore].hasCategory("teen")) {
                if (ten < 0 && digit < 0) {
                    ten = ts[nextNotIgnore].number!!.integerValue()
                    digit = 0 // ten contains also the digit, but set to 0 to prevent double digit
                } else {
                    break // unexpected double ten or ten after digit
                }
            } else if (ts[nextNotIgnore].hasCategory("tens")) {
                if (ten < 0 && digit < 0) {
                    ten = ts[nextNotIgnore].number!!.integerValue()
                } else {
                    break // unexpected double ten or ten after digit
                }
            } else if (ts[nextNotIgnore].hasCategory("hundred")) {
                if (hundred < 0 && ten < 0) {
                    if (digit < 0) {
                        hundred = 100 // e.g. a hundred
                    } else if (digit == 0L) {
                        break // do not allow e.g. zero hundred
                    } else {
                        hundred = digit * 100 // e.g. three hundred
                        digit = -1 // reset digit for e.g. four hundred and nine
                    }
                } else {
                    break // unexpected double hundred
                }
            } else if (isRawNumber(ts[nextNotIgnore])) {
                // raw number, e.g. 192
                val rawNumber = ts[nextNotIgnore].number
                if (rawNumber!!.isDecimal) {
                    // this can happen only for numbers really big, like with 50 digits
                    // so they surely are not less than 1000
                    break
                }

                if (!allowOrdinal && ts[nextNotIgnore + 1].hasCategory("ordinal_suffix")) {
                    break // do not allow ordinal if allowOrdinal is false
                }

                if (rawNumber.lessThan(10)) {
                    if (digit < 0) {
                        digit = rawNumber.integerValue()
                    } else {
                        break // unexpected double digit
                    }
                } else if (rawNumber.lessThan(100)) {
                    if (ten < 0 && digit < 0) {
                        ten = rawNumber.integerValue()
                        // ten contains also the digit, but set to 0 to prevent double digit
                        digit = 0
                    } else {
                        break // unexpected double ten or ten after digit
                    }
                } else if (rawNumber.lessThan(1000)) {
                    if (hundred < 0 && ten < 0 && digit < 0) {
                        hundred = rawNumber.integerValue()
                        // hundred contains also the digit, but set to 0 to prevent double digit/ten
                        ten = 0
                        digit = 0
                    } else {
                        break // unexpected double hundred or hundred after digit or ten
                    }
                } else {
                    break // raw number is too big, not smaller than 1000
                }

                // this point is reached only if the raw number was accepted
                ordinal = ts[nextNotIgnore + 1].hasCategory("ordinal_suffix")
                if (ordinal) {
                    ts.movePositionForwardBy(nextNotIgnore + 2)
                    break // raw number followed by st/nd/rd/th, nothing else allowed, e.g. 407th
                }
            } else {
                break // random token encountered, number is terminated
            }

            ts.movePositionForwardBy(nextNotIgnore + 1)
            if (ts[-1].hasCategory("ordinal")) {
                // ordinal number encountered, nothing else can follow, e.g. two hundredth
                ordinal = true
                break
            }
        }

        return if (hundred < 0 && ten < 0 && digit < 0) {
            null
        } else {
            Number(
                (if (hundred < 0) 0 else hundred) + (if (ten < 0) 0 else ten)
                        + (if (digit < 0) 0 else digit), ordinal
            ) // e.g. one hundred and twelve
        }
    }

    fun isRawNumber(token: Token): Boolean {
        return token.hasCategory("number") && token.hasCategory("raw")
    }
}
