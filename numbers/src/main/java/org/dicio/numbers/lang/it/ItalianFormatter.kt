package org.dicio.numbers.lang.it

import org.dicio.numbers.formatter.Formatter
import org.dicio.numbers.unit.MixedFraction
import org.dicio.numbers.util.Utils
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class ItalianFormatter : Formatter("config/it-it") {

    override fun niceNumber(mixedFraction: MixedFraction, speech: Boolean): String {
        if (speech) {
            val sign = if (mixedFraction.negative) "meno " else ""
            if (mixedFraction.numerator == 0) {
                return sign + pronounceNumber(mixedFraction.whole.toDouble(), 0, true, false, false)
            }

            var denominatorString = if (mixedFraction.denominator == 2) {
                "mezzo"
            } else {
                // use ordinal: only mezzo is exceptional
                pronounceNumber(mixedFraction.denominator.toDouble(), 0, true, false, true)
            }

            val numeratorString = if (mixedFraction.numerator == 1) {
                "un"
            } else {
                denominatorString =
                    denominatorString.substring(0, denominatorString.length - 1) + "i"
                pronounceNumber(mixedFraction.numerator.toDouble(), 0, true, false, false)
            }

            return if (mixedFraction.whole == 0L) {
                "$sign$numeratorString $denominatorString"
            } else {
                (sign + pronounceNumber(mixedFraction.whole.toDouble(), 0, true, false, false)
                        + " e " + numeratorString + " " + denominatorString)
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
        // for italian shortScale is completely ignored

        if (number == Double.POSITIVE_INFINITY) {
            return "infinito"
        } else if (number == Double.NEGATIVE_INFINITY) {
            return "meno infinito"
        } else if (java.lang.Double.isNaN(number)) {
            return "non un numero"
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
                    "%s per dieci alla %s",
                    pronounceNumber(n, places, shortScale, false, false),
                    pronounceNumber(power, places, shortScale, false, false)
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
                result.append("meno ")
            }
        }

        val realPlaces = Utils.decimalPlacesNoFinalZeros(varNumber, places)
        val numberIsWhole = realPlaces == 0
        val realOrdinal = ordinal && numberIsWhole
        // if no decimal places to be printed, numberLong should be the rounded number
        val numberLong = varNumber.toLong() + (if (varNumber % 1 >= 0.5 && numberIsWhole) 1 else 0)

        if (realOrdinal && ORDINAL_NAMES.containsKey(numberLong)) {
            result.append(ORDINAL_NAMES[numberLong])
        } else if (!realOrdinal && NUMBER_NAMES.containsKey(numberLong)) {
            if (varNumber > 1000) {
                result.append("un ")
            }
            result.append(NUMBER_NAMES[numberLong])
        } else {
            val groups = Utils.splitByModulus(numberLong, 1000)
            val groupNames: MutableList<String> = ArrayList()
            for (i in groups.indices) {
                val z = groups[i]
                if (z == 0L) {
                    continue  // skip 000 groups
                }
                var groupName = subThousand(z)

                if (i == 1) {
                    if (z == 1L) {
                        groupName = "mille"
                    } else {
                        // use mila instead of mille
                        groupName += " mila"
                    }
                } else if (i != 0) {
                    // magnitude > 1000, so un is always there
                    if (z == 1L) {
                        groupName = "un"
                    }

                    val magnitude = Utils.longPow(1000, i)
                    groupName += " " + NUMBER_NAMES[magnitude]
                    if (z != 1L) {
                        groupName = groupName.substring(0, groupName.length - 1) + "i"
                    }
                }

                groupNames.add(groupName)
            }

            if (groupNames.isEmpty()) {
                result.append("zero")
            } else {
                appendSplitGroups(result, groupNames)
            }

            if (ordinal && numberIsWhole) { // not ordinal if not whole
                if (result.lastIndexOf("dieci") == result.length - 5) {
                    result.deleteCharAt(result.length - 4)
                    result.append("mo")
                } else {
                    if (result.lastIndexOf("tre") != result.length - 3
                        && result.lastIndexOf("sei") != result.length - 3
                    ) {
                        result.deleteCharAt(result.length - 1)
                        if (result.lastIndexOf("mil") == result.length - 3) {
                            result.append("l")
                        }
                    }
                    result.append("esimo")
                }
            }
        }

        if (realPlaces > 0) {
            if (varNumber < 1.0 && (result.isEmpty() || "meno ".contentEquals(result))) {
                result.append("zero") // nothing was written before
            }
            result.append(" virgola")

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
            val result = StringBuilder()
            if (time.minute == 45) {
                when (val newHour = (time.hour + 1) % 24) {
                    0 -> result.append("un quarto a mezzanotte")
                    12 -> result.append("un quarto a mezzogiorno")
                    else -> {
                        result.append("un quarto alle ")
                        result.append(getHourName(newHour, use24Hour))
                    }
                }
            } else {
                result.append(getHourName(time.hour, use24Hour))

                when (time.minute) {
                    0 -> result.append(" in punto")
                    15 -> result.append(" e un quarto")
                    30 -> result.append(" e mezza")
                    else -> {
                        result.append(" e ")
                        if (time.minute < 10) {
                            result.append("zero ")
                        }
                        result.append(pronounceNumberDuration(time.minute.toLong()))
                    }
                }
            }

            if (!use24Hour && showAmPm && result.indexOf("mezzanotte") == -1 && result.indexOf("mezzogiorno") == -1) {
                if (time.hour >= 19) {
                    result.append(" di sera")
                } else if (time.hour >= 12) {
                    result.append(" di pomeriggio")
                } else if (time.hour >= 4) {
                    result.append(" di mattina")
                } else {
                    result.append(" di notte")
                }
            }
            return result.toString()
        } else {
            if (use24Hour) {
                return time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN))
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

    private fun getHourName(hour: Int, use24Hour: Boolean): String? {
        if (hour == 0) {
            return "mezzanotte"
        } else if (hour == 12) {
            return "mezzogiorno"
        }
        val normalizedHour = if (use24Hour) {
            hour
        } else {
            hour % 12
        }

        return if (normalizedHour == 1) {
            "una"
        } else {
            pronounceNumberDuration(normalizedHour.toLong())
        }
    }

    override fun pronounceNumberDuration(number: Long): String {
        if (number == 1L) {
            return "un"
        }
        return super.pronounceNumberDuration(number)
    }


    /**
     * @param n must be 0 <= n <= 999
     * @return the string representation of a number smaller than 1000
     */
    private fun subThousand(n: Long): String {
        val builder = StringBuilder()
        var requiresSpace = false // whether a space needs to be added before the content
        if (n >= 100) {
            val hundred = n / 100
            if (hundred > 1) {
                builder.append(NUMBER_NAMES[hundred])
                builder.append(" ")
            }
            builder.append("cento")
            requiresSpace = true
        }

        val lastTwoDigits = n % 100
        if (lastTwoDigits != 0L && NUMBER_NAMES.containsKey(lastTwoDigits)) {
            if (requiresSpace) {
                // this is surely true, but let's keep the space for consistency
                builder.append(" ")
            }
            builder.append(NUMBER_NAMES[lastTwoDigits])
        } else {
            val ten = (n % 100) / 10
            if (ten > 0) {
                if (requiresSpace) {
                    builder.append(" ")
                }
                builder.append(NUMBER_NAMES[ten * 10])
                requiresSpace = true
            }

            val unit = n % 10
            if (unit > 0) {
                if (requiresSpace) {
                    builder.append(" ")
                }
                builder.append(NUMBER_NAMES[unit])
            }
        }

        return builder.toString()
    }

    /**
     * @param result the string builder to append the comma-separated group names to
     * @param groupNames the group names
     */
    private fun appendSplitGroups(result: StringBuilder, groupNames: List<String>) {
        if (groupNames.isNotEmpty()) {
            result.append(groupNames[groupNames.size - 1])
        }

        for (i in groupNames.size - 2 downTo 0) {
            result.append(", ")
            result.append(groupNames[i])
        }
    }

    companion object {
        private val NUMBER_NAMES = mapOf(
            0L to "zero",
            1L to "uno",
            2L to "due",
            3L to "tre",
            4L to "quattro",
            5L to "cinque",
            6L to "sei",
            7L to "sette",
            8L to "otto",
            9L to "nove",
            10L to "dieci",
            11L to "undici",
            12L to "dodici",
            13L to "tredici",
            14L to "quattordici",
            15L to "quindici",
            16L to "sedici",
            17L to "diciassette",
            18L to "diciotto",
            19L to "diciannove",
            20L to "venti",
            30L to "trenta",
            40L to "quaranta",
            50L to "cinquanta",
            60L to "sessanta",
            70L to "settanta",
            80L to "ottanta",
            90L to "novanta",
            100L to "cento",
            1000L to "mille",
            1000000L to "milione",
            1000000000L to "miliardo",
            1000000000000L to "bilione",
            1000000000000000L to "biliardo",
            1000000000000000000L to "trilione",
        )

        private val ORDINAL_NAMES = mapOf(
            1L to "primo",
            2L to "secondo",
            3L to "terzo",
            4L to "quarto",
            5L to "quinto",
            6L to "sesto",
            7L to "settimo",
            8L to "ottavo",
            9L to "nono",
            10L to "decimo",
            11L to "undicesimo",
            12L to "dodicesimo",
            13L to "tredicesimo",
            14L to "quattordicesimo",
            15L to "quindicesimo",
            16L to "sedicesimo",
            17L to "diciassettesimo",
            18L to "diciottesimo",
            19L to "diciannovesimo",
            1000L to "millesimo",
            1000000L to "milionesimo",
            1000000000L to "miliardesimo",
            1000000000000L to "bilionesimo",
            1000000000000000L to "biliardesimo",
            1000000000000000000L to "trilionesimo",
        )
    }
}
