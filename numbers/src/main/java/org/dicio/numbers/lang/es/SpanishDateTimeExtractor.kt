package org.dicio.numbers.lang.es

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

class SpanishDateTimeExtractor internal constructor(
    private val ts: TokenStream,
    private val now: LocalDateTime
) {
    private val numberExtractor = SpanishNumberExtractor(ts)
    private val durationExtractor = DurationExtractorUtils(ts, numberExtractor::numberNoOrdinal)
    private val dateTimeExtractor = DateTimeExtractorUtils(ts, now, this::extractIntegerInRange)

    private fun extractIntegerInRange(fromInclusive: Int, toInclusive: Int): Int? {
        // disallow fraction as / should be treated as a day/month/year separator
        return NumberExtractorUtils.extractOneIntegerInRange(
            ts, fromInclusive, toInclusive
        ) { NumberExtractorUtils.signBeforeNumber(ts) { numberExtractor.numberInteger(false) } }
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
            // AM/PM should not do anything after 0 (e.g. 0pm or 24 di sera)

            if (pm && !DateTimeExtractorUtils.isMomentOfDayPm(time.hour)) {
                // time must be in the afternoon, but time is not already, correct it
                time = time.withHour((time.hour + 12) % DateTimeExtractorUtils.HOURS_IN_DAY)
            }
        }
        return time
    }

    fun time(): LocalTime? {
        // try both with a normal hour and with "mezzogiorno"/"mezzanotte"
        val hour = Utils.firstNotNull(this::noonMidnightLike, this::hour) ?: return null
        var result = LocalTime.of(hour, 0)

        val minute = ts.tryOrSkipDateTimeIgnore(
            true
        ) {
            Utils.firstNotNull(this::specialMinute, dateTimeExtractor::minute)
        }
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

        val dayOfWeek = dayOfWeek()
        val day = ts.tryOrSkipDateTimeIgnore(
            dayOfWeek != null
        ) { extractIntegerInRange(1, 31) }

        if (day == null) {
            if (dayOfWeek != null) {
                // TODO maybe enforce the date to be in the future?
                return result.plus((dayOfWeek - result.dayOfWeek.ordinal).toLong(), ChronoUnit.DAYS)
            }
            result = result.withDayOfMonth(1)
        } else {
            result = result.withDayOfMonth(day)
        }

        val month = ts.tryOrSkipDateTimeIgnore(day != null) {
            Utils.firstNotNull(dateTimeExtractor::monthName, { extractIntegerInRange(1, 12) })
        }
        if (month == null) {
            if (day != null) {
                return result
            }
            result = result.withMonth(1)
        } else {
            result = result.withMonth(month)
        }

        // if month is null then day is also null, otherwise we would have returned above
        val year = ts.tryOrSkipDateTimeIgnore(
            month != null
        ) { extractIntegerInRange(0, 999999999) }
        if (year == null) {
            if (month != null) {
                return result
            }
            return null
        }

        val bcad = dateTimeExtractor.bcad()
        return result.withYear(year * (if (bcad == null || bcad) 1 else -1))
    }


    fun dayOfWeek(): Int? {
        if (ts[0].isValue("mar")) {
            ts.movePositionForwardBy(1)
            return 1 // special case, since mar already used for march
        } else {
            return dateTimeExtractor.dayOfWeek()
        }
    }

    fun specialMinute(): Int? {
        val originalPosition = ts.position

        val number = numberExtractor.numberNoOrdinal()
        if (number != null && number.isDecimal && number.decimalValue() > 0.0 && number.decimalValue() < 1.0) {
            // e.g. alle due e tre quarti
            return Utils.roundToInt(number.decimalValue() * 60)
        }

        ts.position = originalPosition
        return null
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
            // found a word that usually comes before special hours, e.g. questo, dopo
            if (ts[0].hasCategory("pre_relative_indicator")) {
                relativeIndicator = if (ts[0].hasCategory("negative")) -1 else 1
                // only move to next not ignore if we got a relative indicator
                ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1))
            } else {
                ts.movePositionForwardBy(1)
            }
        }

        if (ts[0].hasCategory(category)) {
            // special hour found, e.g. mezzanotte, sera, pranzo
            ts.movePositionForwardBy(1)
            return ((ts[-1].number!!.integerValue().toInt() +
                    DateTimeExtractorUtils.HOURS_IN_DAY + relativeIndicator)
                    % DateTimeExtractorUtils.HOURS_IN_DAY)
        }

        // noon/midnight have both the categores noon_midnight_like and moment_of_day, always try
        if (ts[0].value.startsWith("mezz")) {
            // sometimes e.g. "mezzogiorno" is split into "mezzo giorno"
            if (ts[1].value.startsWith("giorn")) {
                ts.movePositionForwardBy(2)
                return 12 + relativeIndicator
            } else if (ts[1].value.startsWith("nott")) {
                ts.movePositionForwardBy(2)
                return (DateTimeExtractorUtils.HOURS_IN_DAY + relativeIndicator) % DateTimeExtractorUtils.HOURS_IN_DAY
            }
        }

        // no special hour found
        ts.position = originalPosition
        return null
    }

    fun hour(): Int? {
        val originalPosition = ts.position

        // skip words that usually come before hours, e.g. alle, ore
        ts.movePositionForwardBy(ts.indexOfWithoutCategory("pre_hour", 0))

        val number = extractIntegerInRange(0, DateTimeExtractorUtils.HOURS_IN_DAY)
        if (number == null) {
            // no number found, or the number is not a valid hour, e.g. le ventisei
            ts.position = originalPosition
            return null
        }

        // found hour, e.g. alle diciannove
        return number % DateTimeExtractorUtils.HOURS_IN_DAY // transform 24 into 0
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
        val originalPosition = ts.position

        // collect as many adders ("altro") preceding yesterday ("ieri") as possible
        var dayCount = 0
        while (ts[0].hasCategory("yesterday_adder")) {
            ++dayCount
            ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1))
        }

        // collect the actual yesterday ("ieri") and exit if it is not found
        if (!ts[0].hasCategory("yesterday")) {
            ts.position = originalPosition
            return null
        }
        ts.movePositionForwardBy(1)
        ++dayCount

        // if no adders were collected before yesterday, try to collect only one at the end
        val nextNotIgnore = ts.indexOfWithoutCategory("date_time_ignore", 0)
        if (dayCount == 1 && ts[nextNotIgnore].hasCategory("yesterday_adder")) {
            ++dayCount
            ts.movePositionForwardBy(nextNotIgnore + 1)
        }

        // found relative yesterday, e.g. altro altro ieri, ieri l'altro
        return -dayCount
    }

    fun relativeTomorrow(): Int? {
        val originalPosition = ts.position

        // collect as many "dopo" preceding "domani" as possible
        var dayCount = 0
        while (ts[0].hasCategory("tomorrow_adder")) {
            ++dayCount
            ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1))
        }

        // collect the actual "domani" and exit if it is not found
        if (!ts[0].hasCategory("tomorrow")) {
            ts.position = originalPosition
            return null
        }
        ts.movePositionForwardBy(1)
        ++dayCount

        // found relative tomorrow, e.g. domani, dopo dopo domani
        return dayCount
    }

    fun relativeDuration(): Duration? {
        return dateTimeExtractor.relativeIndicatorDuration(
            { durationExtractor.duration() },
            { duration -> duration.multiply(-1) }
        )
    }
}
