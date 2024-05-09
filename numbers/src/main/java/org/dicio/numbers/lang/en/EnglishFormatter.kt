package org.dicio.numbers.lang.en

import org.dicio.numbers.formatter.Formatter
import org.dicio.numbers.unit.MixedFraction
import org.dicio.numbers.util.Utils
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class EnglishFormatter : Formatter("config/en-us") {

    override fun niceNumber(mixedFraction: MixedFraction, speech: Boolean): String {
        if (speech) {
            val sign = if (mixedFraction.negative) "minus " else ""
            if (mixedFraction.numerator == 0) {
                return sign + pronounceNumber(mixedFraction.whole.toDouble(), 0, true, false, false)
            }

            var denominatorString = when (mixedFraction.denominator) {
                2 -> "half"
                4 -> "quarter"
                else -> {
                    // use ordinal: only half and quarter are exceptions
                    pronounceNumber(mixedFraction.denominator.toDouble(), 0, true, false, true)
                }
            }

            val numeratorString: String
            if (mixedFraction.numerator == 1) {
                numeratorString = "a"
            } else {
                numeratorString =
                    pronounceNumber(mixedFraction.numerator.toDouble(), 0, true, false, false)
                denominatorString += "s"
            }

            return if (mixedFraction.whole == 0L) {
                "$sign$numeratorString $denominatorString"
            } else {
                (sign + pronounceNumber(mixedFraction.whole.toDouble(), 0, true, false, false)
                        + " and " + numeratorString + " " + denominatorString)
            }
        } else {
            return niceNumberNotSpeech(mixedFraction)
        }
    }

    override fun pronounceNumber(
        number: Double,
        places: Int,
        shortScale: Boolean,
        scientific: Boolean,
        ordinal: Boolean
    ): String {
        if (number == Double.POSITIVE_INFINITY) {
            return "infinity"
        } else if (number == Double.NEGATIVE_INFINITY) {
            return "negative infinity"
        } else if (java.lang.Double.isNaN(number)) {
            return "not a number"
        }

        // also using scientific mode if the number is too big to be spoken fully. Checking against
        // the biggest double smaller than 10^21 = 1000 * 10^18, which is the biggest pronounceable
        // number, since e.g. 999.99 * 10^18 can be pronounced correctly.
        if (scientific || abs(number) > 999999999999999934463.0) {
            val scientificFormatted = String.format(Locale.ENGLISH, "%E", number)
            val parts = scientificFormatted.split("E".toRegex(), limit = 2).toTypedArray()
            val power = parts[1].toInt().toDouble()

            if (power != 0.0) {
                // This handles negatives of powers separately from the normal
                // handling since each call disables the scientific flag
                val n = parts[0].toDouble()
                return String.format(
                    "%s%s times ten to the power of %s%s",
                    if (n < 0) "negative " else "",
                    pronounceNumber(abs(n), places, shortScale, false, false),
                    if (power < 0) "negative " else "",
                    pronounceNumber(abs(power), places, shortScale, false, false)
                )
            }
        }

        val result = StringBuilder()
        var varNumber = number
        if (varNumber < 0) {
            varNumber = -varNumber
            // from here on number is always positive
            if (places != 0 || varNumber >= 0.5) {
                // do not add minus if number will be rounded to 0
                result.append(if (scientific) "negative " else "minus ")
            }
        }

        val realPlaces = Utils.decimalPlacesNoFinalZeros(varNumber, places)
        val numberIsWhole = realPlaces == 0
        // if no decimal places to be printed, numberLong should be the rounded number
        val numberLong = varNumber.toLong() + (if (varNumber % 1 >= 0.5 && numberIsWhole) 1 else 0)

        if (!ordinal && numberIsWhole && numberLong > 1000 && numberLong < 2000) {
            // deal with 4 digits that can be said like a date, i.e. 1972 => nineteen seventy two

            result.append(NUMBER_NAMES[numberLong / 100])
            result.append(" ")
            if (numberLong % 100 == 0L) {
                // 1900 => nineteen hundred
                result.append(NUMBER_NAMES[100L])
            } else if (numberLong % 100 < 10 && numberLong % 100 != 0L) {
                // 1906 => nineteen oh six
                result.append("oh ")
                result.append(NUMBER_NAMES[numberLong % 10])
            } else if (numberLong % 10 == 0L || numberLong % 100 < 20) {
                // 1960 => nineteen sixty; 1911 => nineteen eleven
                result.append(NUMBER_NAMES[numberLong % 100])
            } else {
                // 1961 => nineteen sixty one
                result.append(NUMBER_NAMES[numberLong % 100 - numberLong % 10])
                result.append(" ")
                result.append(NUMBER_NAMES[numberLong % 10])
            }

            return result.toString()
        }

        if (!ordinal && NUMBER_NAMES.containsKey(numberLong)) {
            if (varNumber > 90) {
                result.append("one ")
            }
            result.append(NUMBER_NAMES[numberLong])
        } else if (shortScale) {
            var ordi = ordinal && numberIsWhole // not ordinal if not whole
            val groups = Utils.splitByModulus(numberLong, 1000)
            val groupNames: MutableList<String?> = ArrayList()
            for (i in groups.indices) {
                val z = groups[i]
                if (z == 0L) {
                    continue  // skip 000 groups
                }
                var groupName = subThousand(z, i == 0 && ordi)

                if (i != 0) {
                    val magnitude = Utils.longPow(1000, i)
                    if (ordi) {
                        // ordi can be true only for the first group (i.e. at the end of the number)
                        if (z == 1L) {
                            // remove "one" from first group (e.g. "one billion, millionth")
                            groupName = ORDINAL_NAMES_SHORT_SCALE[magnitude]
                        } else {
                            groupName += " " + ORDINAL_NAMES_SHORT_SCALE[magnitude]
                        }
                    } else {
                        groupName += " " + NUMBER_NAMES_SHORT_SCALE[magnitude]
                    }
                }

                groupNames.add(groupName)
                ordi = false
            }

            appendSplitGroups(result, groupNames)
        } else {
            var ordi = ordinal && numberIsWhole // not ordinal if not whole
            val groups = Utils.splitByModulus(numberLong, 1000000)
            val groupNames: MutableList<String?> = ArrayList()
            for (i in groups.indices) {
                val z = groups[i]
                if (z == 0L) {
                    continue  // skip 000000 groups
                }

                var groupName: String?
                if (z < 1000) {
                    groupName = subThousand(z, i == 0 && ordi)
                } else {
                    groupName = subThousand(z / 1000, false) + " thousand"
                    if (z % 1000 != 0L) {
                        groupName += (if (i == 0) ", " else " ") + subThousand(
                            z % 1000,
                            i == 0 && ordi
                        )
                    } else if (i == 0 && ordi) {
                        if (z / 1000 == 1L) {
                            groupName = "thousandth" // remove "one" from "one thousandth"
                        } else {
                            groupName += "th"
                        }
                    }
                }

                if (i != 0) {
                    val magnitude = Utils.longPow(1000000, i)
                    if (ordi) {
                        // ordi can be true only for the first group (i.e. at the end of the number)
                        if (z == 1L) {
                            // remove "one" from first group (e.g. "one billion, millionth")
                            groupName = ORDINAL_NAMES_LONG_SCALE[magnitude]
                        } else {
                            groupName += " " + ORDINAL_NAMES_LONG_SCALE[magnitude]
                        }
                    } else {
                        groupName += " " + NUMBER_NAMES_LONG_SCALE[magnitude]
                    }
                }

                groupNames.add(groupName)
                ordi = false
            }

            appendSplitGroups(result, groupNames)
        }

        if (realPlaces > 0) {
            if (varNumber < 1.0 && (result.isEmpty() || "minus ".contentEquals(result))) {
                result.append("zero") // nothing was written before
            }
            result.append(" point")

            val fractionalPart = String.format("%." + realPlaces + "f", varNumber % 1)
            for (i in 2 until fractionalPart.length) {
                result.append(" ")
                result.append(NUMBER_NAMES[(fractionalPart[i].code - '0'.code).toLong()])
            }
        }

        return result.toString()
    }

    override fun niceTime(
        time: LocalTime,
        speech: Boolean,
        use24Hour: Boolean,
        showAmPm: Boolean
    ): String {
        if (speech) {
            if (use24Hour) {
                val result = StringBuilder()
                if (time.hour < 10) {
                    result.append("zero ")
                }
                result.append(pronounceNumberDuration(time.hour.toLong()))

                result.append(" ")
                if (time.minute == 0) {
                    result.append("hundred")
                } else {
                    if (time.minute < 10) {
                        result.append("zero ")
                    }
                    result.append(pronounceNumberDuration(time.minute.toLong()))
                }

                return result.toString()
            } else {
                if (time.hour == 0 && time.minute == 0) {
                    return "midnight"
                } else if (time.hour == 12 && time.minute == 0) {
                    return "noon"
                }

                val normalizedHour = (time.hour + 11) % 12 + 1 // 1 to 12
                val result = StringBuilder()
                if (time.minute == 15) {
                    result.append("quarter past ")
                    result.append(pronounceNumberDuration(normalizedHour.toLong()))
                } else if (time.minute == 30) {
                    result.append("half past ")
                    result.append(pronounceNumberDuration(normalizedHour.toLong()))
                } else if (time.minute == 45) {
                    result.append("quarter to ")
                    result.append(pronounceNumberDuration((normalizedHour % 12 + 1).toLong()))
                } else {
                    result.append(pronounceNumberDuration(normalizedHour.toLong()))

                    if (time.minute == 0) {
                        if (!showAmPm) {
                            return "$result o'clock"
                        }
                    } else {
                        if (time.minute < 10) {
                            result.append(" oh")
                        }
                        result.append(" ")
                        result.append(pronounceNumberDuration(time.minute.toLong()))
                    }
                }

                if (showAmPm) {
                    result.append(if (time.hour >= 12) " p.m." else " a.m.")
                }
                return result.toString()
            }
        } else {
            if (use24Hour) {
                return time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH))
            } else {
                val result = time.format(
                    DateTimeFormatter.ofPattern(
                        if (showAmPm) "K:mm a" else "K:mm", Locale.ENGLISH
                    )
                )
                return if (result.startsWith("0:")) {
                    "12:" + result.substring(2)
                } else {
                    result
                }
            }
        }
    }


    /**
     * @param n must be 0 <= n <= 999
     * @param ordinal whether to return an ordinal number (usually with -th)
     * @return the string representation of a number smaller than 1000
     */
    private fun subThousand(n: Long, ordinal: Boolean): String? {
        // this function calls itself inside if branches to make sure `ordinal` is respected
        if (ordinal && ORDINAL_NAMES.containsKey(n)) {
            return ORDINAL_NAMES[n]
        } else if (n < 100) {
            if (!ordinal && NUMBER_NAMES.containsKey(n)) {
                return NUMBER_NAMES[n]
            }

            // n is surely => 20 from here on, since all n < 20 are in (ORDINAL|NUMBER)_NAMES
            return (NUMBER_NAMES[n - n % 10]
                    + (if (n % 10 > 0) " " + subThousand(n % 10, ordinal) else ""))
        } else {
            return (NUMBER_NAMES[n / 100] + " hundred"
                    + (if (n % 100 > 0) " and " + subThousand(n % 100, ordinal)
            else (if (ordinal) "th" else "")))
        }
    }

    /**
     * @param result the string builder to append the comma-separated group names to
     * @param groupNames the group names
     */
    private fun appendSplitGroups(result: StringBuilder, groupNames: List<String?>) {
        if (groupNames.isNotEmpty()) {
            result.append(groupNames[groupNames.size - 1])
        }

        for (i in groupNames.size - 2 downTo 0) {
            result.append(", ")
            result.append(groupNames[i])
        }
    }

    companion object {
        val NUMBER_NAMES = mapOf(
            0L to "zero",
            1L to "one",
            2L to "two",
            3L to "three",
            4L to "four",
            5L to "five",
            6L to "six",
            7L to "seven",
            8L to "eight",
            9L to "nine",
            10L to "ten",
            11L to "eleven",
            12L to "twelve",
            13L to "thirteen",
            14L to "fourteen",
            15L to "fifteen",
            16L to "sixteen",
            17L to "seventeen",
            18L to "eighteen",
            19L to "nineteen",
            20L to "twenty",
            30L to "thirty",
            40L to "forty",
            50L to "fifty",
            60L to "sixty",
            70L to "seventy",
            80L to "eighty",
            90L to "ninety",
            100L to "hundred",
            1000L to "thousand",
            1000000L to "million",
        )

        val NUMBER_NAMES_SHORT_SCALE = NUMBER_NAMES + mapOf(
            1000000000L to "billion",
            1000000000000L to "trillion",
            1000000000000000L to "quadrillion",
            1000000000000000000L to "quintillion",
        )

        val NUMBER_NAMES_LONG_SCALE = NUMBER_NAMES + mapOf(
            1000000000000L to "billion",
            1000000000000000000L to "trillion",
        )


        val ORDINAL_NAMES = mapOf(
            1L to "first",
            2L to "second",
            3L to "third",
            4L to "fourth",
            5L to "fifth",
            6L to "sixth",
            7L to "seventh",
            8L to "eighth",
            9L to "ninth",
            10L to "tenth",
            11L to "eleventh",
            12L to "twelfth",
            13L to "thirteenth",
            14L to "fourteenth",
            15L to "fifteenth",
            16L to "sixteenth",
            17L to "seventeenth",
            18L to "eighteenth",
            19L to "nineteenth",
            20L to "twentieth",
            30L to "thirtieth",
            40L to "fortieth",
            50L to "fiftieth",
            60L to "sixtieth",
            70L to "seventieth",
            80L to "eightieth",
            90L to "ninetieth",
            100L to "hundredth",
            1000L to "thousandth",
            1000000L to "millionth",
        )

        val ORDINAL_NAMES_SHORT_SCALE = ORDINAL_NAMES + mapOf(
            1000000000L to "billionth",
            1000000000000L to "trillionth",
            1000000000000000L to "quadrillionth",
            1000000000000000000L to "quintillionth",
        )

        val ORDINAL_NAMES_LONG_SCALE = ORDINAL_NAMES + mapOf(
            1000000000000L to "billionth",
            1000000000000000000L to "trillionth",
        )
    }
}
