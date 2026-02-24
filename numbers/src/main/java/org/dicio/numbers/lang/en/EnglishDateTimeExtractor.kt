package org.dicio.numbers.lang.en

import org.dicio.numbers.parser.lexer.TokenStream
import org.dicio.numbers.unit.Duration
import org.dicio.numbers.util.DateTimeExtractorUtils
import org.dicio.numbers.util.DurationExtractorUtils
import org.dicio.numbers.util.NumberExtractorUtils
import org.dicio.numbers.util.Utils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class EnglishDateTimeExtractor internal constructor(
    private val ts: TokenStream,
    shortScale: Boolean,
    private val preferMonthBeforeDay: Boolean,
    private val now: LocalDateTime
) {
    private val numberExtractor = EnglishNumberExtractor(ts, shortScale)
    private val durationExtractor = DurationExtractorUtils(ts, numberExtractor::numberNoOrdinal)
    private val dateTimeExtractor = DateTimeExtractorUtils(ts, now, this::extractIntegerInRange)

    private fun extractIntegerInRange(
        fromInclusive: Int,
        toInclusive: Int,
        allowOrdinal: Boolean = false
    ): Int? {
        // disallow fraction as / should be treated as a day/month/year separator
        return NumberExtractorUtils.extractOneIntegerInRange(
            ts, fromInclusive, toInclusive
        ) { NumberExtractorUtils.signBeforeNumber(ts) { numberExtractor.numberInteger(allowOrdinal) } }
    }

    fun dateTime(): LocalDateTime? {
        // first try preferring having a date first, then try with time first
        return ts.firstWhichUsesMostTokens({ dateTime(false) }, { dateTime(true) })
    }

    private fun dateTime(timeFirst: Boolean): LocalDateTime? {
        var date: LocalDate? = null
        var time: LocalTime? = null

        if (!timeFirst) {
            // first try with special days, since duration-related words might be used
            date = relativeSpecialDay()

            if (date == null) {
                // then try with duration, since otherwise numbers would be interpreted as date days
                val duration = Utils.firstNotNull(
                    this::relativeDuration,
                    dateTimeExtractor::relativeMonthDuration
                )
                if (duration == null) {
                    // no normal relative duration found: finally try extracting a date normally
                    date = date()
                } else if (duration.nanos == 0L && duration.days != 0L) {
                    // duration contains a specified day and no specified time, so a time can follow
                    date = duration.applyAsOffsetToDateTime(now).toLocalDate()
                } else if (duration.nanos != 0L && duration.days == 0L && duration.months == 0L && duration.years == 0L) {
                    // duration contains a specified time, so a date could follow
                    time = duration.applyAsOffsetToDateTime(now).toLocalTime()
                } else {
                    // duration contains mixed date&time, or has units >=month, nothing can follow
                    return duration.applyAsOffsetToDateTime(now)
                }
            }
        }

        if (time == null) {
            time = ts.tryOrSkipDateTimeIgnore(date != null) { this.timeWithAmpm() }
        }

        if (date == null && time != null) {
            // try to extract a date after the time
            val originalPosition = ts.position
            val duration = ts.tryOrSkipDateTimeIgnore(true) { this.relativeDuration() }
            if (duration == null) {
                date = ts.tryOrSkipDateTimeIgnore(
                    true
                ) {
                    Utils.firstNotNull(this::relativeSpecialDay, this::date)
                }
            } else if (duration.nanos == 0L && duration.days != 0L) {
                date = duration.applyAsOffsetToDateTime(now).toLocalDate()
            } else {
                ts.position = originalPosition
            }
        }

        return if (date == null) {
            time?.atDate(now.toLocalDate())
        } else {
            if (time == null) date.atTime(now.toLocalTime()) else date.atTime(time)
        }
    }

    fun timeWithAmpm(): LocalTime? {
        var time = time()
        val pm: Boolean?
        if (time == null) {
            // if there is no time, maybe there is a moment of day (not am/pm though) preceding?
            val momentOfDay = momentOfDay() ?: return null

            time = ts.tryOrSkipDateTimeIgnore(true) { this.time() }
            if (time == null) {
                // found moment of day without a specific time
                return LocalTime.of(momentOfDay, 0)
            } else {
                // use moment of day before time to determine am/pm
                pm = DateTimeExtractorUtils.isMomentOfDayPm(momentOfDay)
            }
        } else {
            // found a time, now look for am/pm or a moment of day
            pm = ts.tryOrSkipDateTimeIgnore(true) {
                Utils.firstNotNull(
                    dateTimeExtractor::ampm,
                    { momentOfDay()?.let(DateTimeExtractorUtils::isMomentOfDayPm) }
                )
            }
        }

        if (time.hour != 0 && pm != null) {
            // AM/PM should not do anything after 0 (e.g. 0pm or 24 in the evening)

            if (!pm && time.hour == 12) {
                // AM was specified after 12 (e.g. 12AM), so the time is midnight
                time = time.withHour(0)
            } else if (pm && !DateTimeExtractorUtils.isMomentOfDayPm(time.hour)) {
                // time must be in the afternoon, but time is not already, correct it
                time = time.withHour((time.hour + 12) % DateTimeExtractorUtils.HOURS_IN_DAY)
            }
        }
        return time
    }

    fun time(): LocalTime? {
        val originalPosition = ts.position
        val specialMinute = specialMinute()

        // try both with a normal hour and with "mezzogiorno"/"mezzanotte"
        val hour = Utils.firstNotNull(this::noonMidnightLike, this::hour)
        if (hour == null) {
            ts.position = originalPosition
            return null
        } else if (specialMinute != null) {
            // we can't use special minute on its own, but only when there is a hour
            return if (specialMinute < 0) {
                LocalTime.of(
                    (hour + DateTimeExtractorUtils.HOURS_IN_DAY - 1) % DateTimeExtractorUtils.HOURS_IN_DAY,
                    60 + specialMinute
                ) // e.g. quarter to six
            } else {
                LocalTime.of(hour, specialMinute) // e.g. half past seven
            }
        }
        var result = LocalTime.of(hour, 0)

        if (oClock()) {
            return result // e.g. ten o'clock
        }

        val minute = ts.tryOrSkipDateTimeIgnore(true) { dateTimeExtractor.minute() }
        if (minute == null) {
            return result
        }
        result = result.withMinute(minute)

        val second = ts.tryOrSkipDateTimeIgnore(true) { dateTimeExtractor.second() }
        if (second == null) {
            return result
        }
        return result.withSecond(second)
    }

    fun date(): LocalDate? {
        var result = now.toLocalDate()

        val dayOfWeek = dateTimeExtractor.dayOfWeek()
        val firstNum = ts.tryOrSkipDateTimeIgnore(
            dayOfWeek != null
        ) { extractIntegerInRange(1, 31, true) }

        if (firstNum == null && dayOfWeek != null) {
            // TODO maybe enforce the date to be in the future?
            return result.plus((dayOfWeek - result.dayOfWeek.ordinal).toLong(), ChronoUnit.DAYS)
        }

        // below here, we do result.withMonth(1) because January has 31 days, so setting the
        // withDayOfMonth() below will always succeed, and then we overwrite/reset the month anyway
        val monthName = ts.tryOrSkipDateTimeIgnore(
            firstNum != null
        ) { dateTimeExtractor.monthName() }
        if (monthName == null) {
            result = if (firstNum == null) {
                result.withMonth(1).withDayOfMonth(1)
            } else {
                val secondNumMax = if (firstNum <= 12) 31 else 12
                val secondNum = ts.tryOrSkipDateTimeIgnore(
                    true
                ) { extractIntegerInRange(1, secondNumMax, true) }

                if (secondNum == null) {
                    return if (preferMonthBeforeDay && firstNum <= 12) {
                        result.withMonth(1).withDayOfMonth(1).withMonth(firstNum)
                    } else {
                        val originalMonth = result.month.value // just to avoid withDayOfMonth failing
                        result.withMonth(1).withDayOfMonth(firstNum).withMonth(originalMonth)
                    }
                } else {
                    if ((preferMonthBeforeDay || secondNum > 12) && firstNum <= 12) {
                        result.withMonth(1).withDayOfMonth(secondNum).withMonth(firstNum)
                    } else {
                        // secondNum is surely <= 12 here because of secondNumMax
                        result.withMonth(1).withDayOfMonth(firstNum).withMonth(secondNum)
                    }
                }
            }
        } else {
            result = result.withMonth(1)

            if (firstNum == null) {
                val secondNum = ts.tryOrSkipDateTimeIgnore(
                    true
                ) { extractIntegerInRange(1, 31, true) }
                result = if (secondNum == null) {
                    result.withDayOfMonth(1)
                } else {
                    result.withDayOfMonth(secondNum)
                }
            } else {
                result = result.withDayOfMonth(firstNum)
            }

            // do withMonth after setting the day, so it coerces the day of month within the number
            // of days in the month if needed
            result = result.withMonth(monthName)
        }
        val dayOrMonthFound = firstNum != null || monthName != null

        // we might have AD before the year, too
        var bcad = ts.tryOrSkipDateTimeIgnore(dayOrMonthFound) { this.bcad() }

        // if month is null then day is also null, otherwise we would have returned above
        val year = ts.tryOrSkipDateTimeIgnore(
            dayOrMonthFound && bcad == null
        ) { extractIntegerInRange(0, 999999999) }
        if (year == null) {
            if (dayOrMonthFound) {
                return result
            }
            return null
        }

        if (bcad == null) {
            bcad = bcad()
        }
        return result.withYear(year * (if (bcad == null || bcad) 1 else -1))
    }


    fun bcad(): Boolean? {
        val bcad = dateTimeExtractor.bcad()
        if (bcad != null && !bcad) {
            // skip "era" in "before current era"
            val nextNotIgnore = ts.indexOfWithoutCategory("date_time_ignore", 0)
            if (ts[nextNotIgnore].hasCategory("bcad_era")) {
                ts.movePositionForwardBy(nextNotIgnore + 1)
            }
        }
        return bcad
    }

    fun noonMidnightLike(): Int? {
        return noonMidnightLikeOrMomentOfDay("noon_midnight_like")
    }

    fun momentOfDay(): Int? {
        // noon_midnight_like is a part of moment_of_day, so noon and midnight are included
        return noonMidnightLikeOrMomentOfDay("moment_of_day")
    }

    private fun noonMidnightLikeOrMomentOfDay(category: String): Int? {
        val originalPosition = ts.position

        var relativeIndicator = 0 // 0 = not found, otherwise the sign, +1 or -1
        if (ts[0].hasCategory("pre_special_hour")) {
            // found a word that usually comes before special hours, e.g. this, in
            if (ts[0].hasCategory("pre_relative_indicator")) {
                relativeIndicator = if (ts[0].hasCategory("negative")) -1 else 1
                // only move to next not ignore if we got a relative indicator, e.g. in the ...
                ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1))
            } else {
                ts.movePositionForwardBy(1)
            }
        }

        if (ts[0].hasCategory(category)) {
            // special hour found, e.g. mezzanotte, sera, pranzo
            ts.movePositionForwardBy(1)
            return ((ts[-1].number!!.integerValue()
                .toInt() + DateTimeExtractorUtils.HOURS_IN_DAY + relativeIndicator)
                    % DateTimeExtractorUtils.HOURS_IN_DAY)
        }

        // no special hour found
        ts.position = originalPosition
        return null
    }

    fun hour(): Int? {
        val originalPosition = ts.position

        // skip words that usually come before hours, e.g. at, hour
        ts.movePositionForwardBy(ts.indexOfWithoutCategory("pre_hour", 0))

        val number = extractIntegerInRange(0, DateTimeExtractorUtils.HOURS_IN_DAY)
        if (number == null) {
            // no number found, or the number is not a valid hour, e.g. at twenty six
            ts.position = originalPosition
            return null
        }

        // found hour, e.g. at nineteen
        return number % DateTimeExtractorUtils.HOURS_IN_DAY // transform 24 into 0
    }

    fun specialMinute(): Int? {
        val originalPosition = ts.position

        // skip words that usually come before hours, e.g. at, hour
        ts.movePositionForwardBy(ts.indexOfWithoutCategory("pre_hour", 0))

        val number = numberExtractor.numberNoOrdinal()
        if (number != null) {
            val minutes: Int
            if (number.isDecimal && number.decimalValue() > 0.0 && number.decimalValue() < 1.0) {
                minutes =
                    Utils.roundToInt(number.decimalValue() * 60) // e.g. three quarters past one
            } else if (number.isInteger && number.integerValue() > 1 && number.integerValue() < 60) {
                minutes = number.integerValue().toInt() // e.g. ten to eleven
            } else {
                ts.position = originalPosition
                return null
            }

            val result = ts.tryOrSkipDateTimeIgnore(true) {
                if (ts[0]
                        .hasCategory("special_minute_after")
                ) {
                    // e.g. half past twelve
                    ts.movePositionForwardBy(1)
                    return@tryOrSkipDateTimeIgnore minutes
                } else if (ts[0].hasCategory("special_minute_before")) {
                    // e.g. quarter to eleven
                    ts.movePositionForwardBy(1)
                    return@tryOrSkipDateTimeIgnore -minutes
                } else {
                    return@tryOrSkipDateTimeIgnore null
                }
            }
            if (result != null) {
                return result
            }
        }

        ts.position = originalPosition
        return null
    }

    fun oClock(): Boolean {
        if (ts[0].hasCategory("pre_oclock")) {
            val nextNotIgnore = ts.indexOfWithoutCategory("date_time_ignore", 1)
            if (ts[nextNotIgnore].hasCategory("post_oclock")) {
                ts.movePositionForwardBy(nextNotIgnore + 1)
                return true
            }
        } else if (ts[0].hasCategory("oclock_combined")) {
            ts.movePositionForwardBy(1)
            return true
        }
        return false
    }


    private fun relativeSpecialDay(): LocalDate? {
        val days = Utils.firstNotNull(
            this::relativeYesterday,
            dateTimeExtractor::relativeToday,
            this::relativeTomorrow,
            dateTimeExtractor::relativeDayOfWeekDuration
        )
        if (days == null) {
            return null
        }
        return now.toLocalDate().plusDays(days.toLong())
    }

    fun relativeYesterday(): Int? {
        if (ts[0].hasCategory("day_adder_the")
            && ts[1].hasCategory("day_adder_day")
            && ts[2].hasCategory("day_adder_before")
            && ts[3].hasCategory("yesterday")
        ) {
            ts.movePositionForwardBy(4)
            return -2 // e.g. the day before yesterday
        }

        // "the" is optional
        if (ts[0].hasCategory("day_adder_day")
            && ts[1].hasCategory("day_adder_before")
            && ts[2].hasCategory("yesterday")
        ) {
            ts.movePositionForwardBy(3)
            return -2 // e.g. day before yesterday
        }

        if (ts[0].hasCategory("yesterday")) {
            ts.movePositionForwardBy(1)
            return -1 // e.g. yesterday
        } else {
            return null
        }
    }

    fun relativeTomorrow(): Int? {
        if (ts[0].hasCategory("day_adder_the")
            && ts[1].hasCategory("day_adder_day")
            && ts[2].hasCategory("day_adder_after")
            && ts[3].hasCategory("tomorrow")
        ) {
            ts.movePositionForwardBy(4)
            return 2 // e.g. the day after tomorrow
        }

        // "the" is optional
        if (ts[0].hasCategory("day_adder_day")
            && ts[1].hasCategory("day_adder_after")
            && ts[2].hasCategory("tomorrow")
        ) {
            ts.movePositionForwardBy(3)
            return 2 // e.g. day after tomorrow
        }

        if (ts[0].hasCategory("tomorrow")) {
            ts.movePositionForwardBy(1)
            return 1 // e.g. tomorrow
        } else {
            return null
        }
    }

    fun relativeDuration(): Duration? {
        return dateTimeExtractor.relativeIndicatorDuration(
            { durationExtractor.duration() },
            { duration -> duration.multiply(-1) }
        )
    }
}
