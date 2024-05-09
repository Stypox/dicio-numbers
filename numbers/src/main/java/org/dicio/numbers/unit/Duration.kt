package org.dicio.numbers.unit

import org.dicio.numbers.util.Utils
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * TODO add documentation and tests
 */
class Duration @JvmOverloads constructor(// basic well-defined unit of time, fits precisely up to ~10^3 hours
    @JvmField val nanos: Long = 0, // not stored as nanos because of e.g. summer/winter time changes
    @JvmField val days: Long = 0, // not stored as days because months have different numbers of days
    @JvmField val months: Long = 0, // not stored as months because of e.g. historical changes
    @JvmField val years: Long = 0
) {
    constructor(duration: Duration) : this(
        duration.nanos,
        duration.days,
        duration.months,
        duration.years
    )

    constructor(javaDuration: java.time.Duration) : this(
        javaDuration.nano + (javaDuration.seconds % SECONDS_IN_DAY) * 1000000000L,
        javaDuration.seconds / SECONDS_IN_DAY, 0, 0
    )


    fun plusNanos(number: Long): Duration {
        return Duration(this.nanos + number, this.days, this.months, this.years)
    }

    fun plusDays(number: Long): Duration {
        return Duration(this.nanos, this.days + number, this.months, this.years)
    }

    fun plusMonths(number: Long): Duration {
        return Duration(this.nanos, this.days, this.months + number, this.years)
    }

    fun plusYears(number: Long): Duration {
        return Duration(this.nanos, this.days, this.months, this.years + number)
    }


    fun plus(number: Long, chronoUnit: ChronoUnit): Duration {
        return when (chronoUnit) {
            ChronoUnit.NANOS -> plusNanos(number)
            ChronoUnit.MICROS -> plusNanos(number * 1000L)
            ChronoUnit.MILLIS -> plusNanos(number * 1000000L)
            ChronoUnit.SECONDS -> plusNanos(number * 1000000000L)
            ChronoUnit.MINUTES -> plusNanos(number * 60000000000L)
            ChronoUnit.HOURS -> plusNanos(number * 3600000000000L)
            ChronoUnit.HALF_DAYS -> plusNanos(number * 43200000000000L) // 12 hours

            ChronoUnit.DAYS -> plusDays(number)
            ChronoUnit.WEEKS -> plusDays(number * 7L)

            ChronoUnit.MONTHS -> plusMonths(number)

            ChronoUnit.YEARS -> plusYears(number)
            ChronoUnit.DECADES -> plusYears(number * 10L)
            ChronoUnit.CENTURIES -> plusYears(number * 100L)
            ChronoUnit.MILLENNIA -> plusYears(number * 1000L)
            ChronoUnit.ERAS -> plusYears(number * 1000000000L)

            ChronoUnit.FOREVER -> throw RuntimeException("Invalid chrono unit: $chronoUnit")
            else -> throw RuntimeException("Invalid chrono unit: $chronoUnit")
        }
    }

    fun plus(number: Double, chronoUnit: ChronoUnit): Duration {
        val result = plus(Utils.roundToLong(number), chronoUnit)
        when (chronoUnit) {
            ChronoUnit.NANOS, ChronoUnit.MICROS, ChronoUnit.MILLIS, ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS -> return result

            ChronoUnit.DAYS, ChronoUnit.WEEKS ->                 // Convert to nanos directly, assuming 1 * day = DAY.getDuration().toNanos() * ns,
                // even though that's not always the case because of e.g. summer/winter time changes
                return result.plusNanos(
                    Utils.roundToLong(
                        Utils.remainderFromRoundingToLong(number)
                                * chronoUnit.duration.toNanos()
                    )
                )

            ChronoUnit.MONTHS ->                 // Convert months to days using DAYS_IN_MONTH, which was obtained using the built-in
                // ChronoUnit estimations for month and day's average durations (in seconds)
                return result.plus(
                    Utils.remainderFromRoundingToLong(number) * DAYS_IN_MONTH,
                    ChronoUnit.DAYS
                )

            ChronoUnit.YEARS, ChronoUnit.DECADES, ChronoUnit.CENTURIES, ChronoUnit.MILLENNIA, ChronoUnit.ERAS -> {
                // According to the built-in ChronoUnit estimations, a year is exactly 12 months
                // (even though that's not always the case because e.g. of leap years), so
                // monthsInYearUnit is surely an integer
                val monthsInYearUnit = (chronoUnit.duration.seconds
                        / ChronoUnit.MONTHS.duration.seconds)
                return result.plus(
                    Utils.remainderFromRoundingToLong(number) * monthsInYearUnit,
                    ChronoUnit.MONTHS
                )
            }

            ChronoUnit.FOREVER -> throw RuntimeException("Invalid chrono unit: $chronoUnit")
            else -> throw RuntimeException("Invalid chrono unit: $chronoUnit")
        }
    }

    fun plus(number: Number, chronoUnit: ChronoUnit): Duration {
        return if (number.isInteger
        ) plus(number.integerValue(), chronoUnit)
        else plus(number.decimalValue(), chronoUnit)
    }

    fun plus(duration: Duration): Duration {
        return Duration(
            nanos + duration.nanos,
            days + duration.days,
            months + duration.months,
            years + duration.years
        )
    }


    fun multiply(number: Long): Duration {
        return Duration(nanos * number, days * number, months * number, years * number)
    }

    fun multiply(number: Double): Duration {
        return Duration(Utils.roundToLong(nanos * number), 0, 0, 0)
            .plus(days * number, ChronoUnit.DAYS)
            .plus(months * number, ChronoUnit.MONTHS)
            .plus(years * number, ChronoUnit.YEARS)
    }

    fun multiply(number: Number): Duration {
        return if (number.isInteger
        ) multiply(number.integerValue())
        else multiply(number.decimalValue())
    }


    override fun toString(): String {
        return toJavaDuration().toString()
    }

    fun toJavaDuration(): java.time.Duration {
        return java.time.Duration.ofNanos(nanos)
            .plusDays(days)
            .plus(multiplyDurationBy(months, ChronoUnit.MONTHS))
            .plus(multiplyDurationBy(years, ChronoUnit.YEARS))
    }

    fun applyAsOffsetToDateTime(original: LocalDateTime): LocalDateTime {
        return original.plusNanos(nanos)
            .plusDays(days)
            .plusMonths(months)
            .plusYears(years)
    }

    private fun multiplyDurationBy(
        number: Long,
        chronoUnit: ChronoUnit
    ): java.time.Duration {
        // This operation would not be doable directly with Duration.plus(), since it throws an
        // exception with units with an estimated duration
        return chronoUnit.duration.multipliedBy(number)
    }

    companion object {
        private val SECONDS_IN_DAY = ChronoUnit.DAYS.duration.seconds
        private val DAYS_IN_MONTH =
            ChronoUnit.MONTHS.duration.seconds.toDouble() / ChronoUnit.DAYS.duration.seconds.toDouble() // ~30.436875
    }
}
