package org.dicio.numbers.unit

import org.dicio.numbers.util.Utils
import kotlin.math.abs

class MixedFraction private constructor(// true if original number is negative
    @JvmField val negative: Boolean,
    // always positive
    @JvmField val whole: Long,
    // always positive
    @JvmField val numerator: Int,
    // if numerator = 0, then = 1, otherwise > 1
    @JvmField val denominator: Int
) {
    companion object {
        @JvmField
        val DEFAULT_DENOMINATORS
                : List<Int> =
            mutableListOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)

        /**
         * Convert a double to components of a mixed fraction representation.
         * @see .DEFAULT_DENOMINATORS
         *
         *
         * @param number the number to convert
         * @param denominators the denominators to use, each has to be > 1
         * @return the closest fractional representation using the provided denominators. Returns `null` if none of the provided denominators yielded a close enough approximation.
         * E.g. `4.500002` would become the whole number `4`, the numerator `1` and the denominator `2`.
         */
        fun of(number: Double, denominators: List<Int>): MixedFraction? {
            if (abs(number).toLong() == Long.MAX_VALUE) {
                return null // number is too large to fit
            } else if (Utils.isWhole(number, Utils.WHOLE_FRACTION_ACCURACY)) {
                return MixedFraction(number < 0, Utils.roundToLong(abs(number)), 0, 1)
            }

            val numberFraction = abs(number % 1)
            for (denominator in denominators) {
                val numerator = numberFraction * denominator
                if (Utils.isWhole(numerator, Utils.WHOLE_FRACTION_ACCURACY)) {
                    return MixedFraction(
                        number < 0, abs(number.toLong().toDouble())
                            .toLong(),
                        Utils.roundToLong(numerator).toInt(), denominator
                    )
                }
            }

            return null
        }
    }
}
