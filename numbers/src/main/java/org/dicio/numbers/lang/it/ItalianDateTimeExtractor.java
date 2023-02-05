package org.dicio.numbers.lang.it;

import static org.dicio.numbers.util.DateTimeExtractorUtils.HOURS_IN_DAY;
import static org.dicio.numbers.util.DateTimeExtractorUtils.isMomentOfDayPm;
import static org.dicio.numbers.util.NumberExtractorUtils.signBeforeNumber;
import static org.dicio.numbers.util.Utils.firstNotNull;
import static org.dicio.numbers.util.Utils.roundToInt;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;
import org.dicio.numbers.util.DateTimeExtractorUtils;
import org.dicio.numbers.util.DurationExtractorUtils;
import org.dicio.numbers.util.NumberExtractorUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class ItalianDateTimeExtractor {

    private final TokenStream ts;
    private final LocalDateTime now;
    private final ItalianNumberExtractor numberExtractor;
    private final DurationExtractorUtils durationExtractor;
    public final DateTimeExtractorUtils dateTimeExtractor;

    ItalianDateTimeExtractor(final TokenStream tokenStream, final LocalDateTime now) {
        this.ts = tokenStream;
        this.now = now;
        this.numberExtractor = new ItalianNumberExtractor(ts);
        this.durationExtractor = new DurationExtractorUtils(ts, numberExtractor::numberNoOrdinal);
        this.dateTimeExtractor = new DateTimeExtractorUtils(ts, now, this::extractIntegerInRange);
    }

    private Integer extractIntegerInRange(final int fromInclusive, final int toInclusive) {
        // disallow fraction as / should be treated as a day/month/year separator
        return NumberExtractorUtils.extractOneIntegerInRange(ts, fromInclusive, toInclusive,
                () -> signBeforeNumber(ts, () -> numberExtractor.numberInteger(false)));
    }


    LocalDateTime dateTime() {
        // first try preferring having a date first, then try with time first
        return ts.firstWhichUsesMostTokens(() -> dateTime(false), () -> dateTime(true));
    }

    private LocalDateTime dateTime(final boolean timeFirst) {
        LocalDate date = null;
        LocalTime time = null;

        if (!timeFirst) {
            // first try with special days, since duration-related words might be used
            date = relativeSpecialDay();

            if (date == null) {
                // then try with duration, since otherwise numbers would be interpreted as date days
                final Duration duration = firstNotNull(
                        this::relativeDuration, dateTimeExtractor::relativeMonthDuration);
                if (duration == null) {
                    // no normal relative duration found: finally try extracting a date normally
                    date = date();
                } else if (duration.getNanos() == 0 && duration.getDays() != 0) {
                    // duration contains a specified day and no specified time, so a time can follow
                    date = duration.applyAsOffsetToDateTime(now).toLocalDate();
                } else if (duration.getNanos() != 0 && duration.getDays() == 0
                        && duration.getMonths() == 0 && duration.getYears() == 0) {
                    // duration contains a specified time, so a date could follow
                    time = duration.applyAsOffsetToDateTime(now).toLocalTime();
                } else {
                    // duration contains mixed date&time, or has units >=month, nothing can follow
                    return duration.applyAsOffsetToDateTime(now);
                }
            }
        }

        if (time == null) {
            time = ts.tryOrSkipDateTimeIgnore(date != null, this::timeWithAmpm);
        }

        if (date == null && time != null) {
            // try to extract a date after the time
            final int originalPosition = ts.getPosition();
            final Duration duration = ts.tryOrSkipDateTimeIgnore(true, this::relativeDuration);
            if (duration == null) {
                date = ts.tryOrSkipDateTimeIgnore(true,
                        () -> firstNotNull(this::relativeSpecialDay, this::date));
            } else if (duration.getNanos() == 0 && duration.getDays() != 0) {
                date = duration.applyAsOffsetToDateTime(now).toLocalDate();
            } else {
                ts.setPosition(originalPosition);
            }
        }

        if (date == null) {
            return time == null ? null : time.atDate(now.toLocalDate());
        } else {
            return time == null ? date.atTime(now.toLocalTime()) : date.atTime(time);
        }
    }

    LocalTime timeWithAmpm() {
        LocalTime time = time();
        Boolean pm;
        if (time == null) {
            // if there is no time, maybe there is a moment of day (not am/pm though) preceding?
            final Integer momentOfDay = momentOfDay();
            if (momentOfDay == null) {
                return null;
            }

            time = ts.tryOrSkipDateTimeIgnore(true, this::time);
            if (time == null) {
                // found moment of day without a specific time
                return LocalTime.of(momentOfDay, 0);
            } else {
                // use moment of day before time to determine am/pm
                pm = isMomentOfDayPm(momentOfDay);
            }
        } else {
            // found a time, now look for am/pm or a moment of day
            pm = ts.tryOrSkipDateTimeIgnore(true, () -> firstNotNull(
                    dateTimeExtractor::ampm, () -> isMomentOfDayPm(momentOfDay())));
        }

        if (pm != null && pm && !isMomentOfDayPm(time.getHour())) {
            // time must be in the afternoon, but time is not already in the afternoon, correct it
            time = time.withHour((time.getHour() + 12) % HOURS_IN_DAY);
        }
        return time;
    }

    LocalTime time() {
        // try both with a normal hour and with "mezzogiorno"/"mezzanotte"
        final Integer hour = firstNotNull(this::noonMidnightLike, this::hour);
        if (hour == null) {
            return null;
        }
        LocalTime result = LocalTime.of(hour, 0);

        final Integer minute = ts.tryOrSkipDateTimeIgnore(true,
                () -> firstNotNull(this::specialMinute, dateTimeExtractor::minute));
        if (minute == null) {
            return result;
        }
        result = result.withMinute(minute);

        final Integer second = ts.tryOrSkipDateTimeIgnore(true, dateTimeExtractor::second);
        if (second == null) {
            return result;
        }
        return result.withSecond(second);
    }

    LocalDate date() {
        LocalDate result = now.toLocalDate();

        final Integer dayOfWeek = dayOfWeek();
        final Integer day = ts.tryOrSkipDateTimeIgnore(dayOfWeek != null,
                () -> extractIntegerInRange(1, 31));

        if (day == null) {
            if (dayOfWeek != null) {
                // TODO maybe enforce the date to be in the future?
                return result.plus(dayOfWeek - result.getDayOfWeek().ordinal(), ChronoUnit.DAYS);
            }
            result = result.withDayOfMonth(1);
        } else {
            result = result.withDayOfMonth(day);
        }

        final Integer month = ts.tryOrSkipDateTimeIgnore(day != null, () ->
                firstNotNull(dateTimeExtractor::monthName, () -> extractIntegerInRange(1, 12)));
        if (month == null) {
            if (day != null) {
                return result;
            }
            result = result.withMonth(1);
        } else {
            result = result.withMonth(month);
        }

        // if month is null then day is also null, otherwise we would have returned above
        final Integer year = ts.tryOrSkipDateTimeIgnore(month != null,
                () -> extractIntegerInRange(0, 999999999));
        if (year == null) {
            if (month != null) {
                return result;
            }
            return null;
        }

        final Boolean bcad = dateTimeExtractor.bcad();
        return result.withYear(year * (bcad == null || bcad ? 1 : -1));
    }


    Integer dayOfWeek() {
        if (ts.get(0).isValue("mar")) {
            ts.movePositionForwardBy(1);
            return 1; // special case, since mar already used for march
        } else {
            return dateTimeExtractor.dayOfWeek();
        }
    }

    public Integer specialMinute() {
        final int originalPosition = ts.getPosition();

        final Number number = numberExtractor.numberNoOrdinal();
        if (number != null && number.isDecimal()
                && number.decimalValue() > 0.0 && number.decimalValue() < 1.0) {
            // e.g. alle due e tre quarti
            return roundToInt(number.decimalValue() * 60);
        }

        ts.setPosition(originalPosition);
        return null;
    }

    Integer noonMidnightLike() {
        return noonMidnightLikeOrMomentOfDay("noon_midnight_like");
    }

    Integer momentOfDay() {
        // noon_midnight_like is a part of moment_of_day, so noon and midnight are included
        return noonMidnightLikeOrMomentOfDay("moment_of_day");
    }

    private Integer noonMidnightLikeOrMomentOfDay(final String category) {
        int originalPosition = ts.getPosition();

        int relativeIndicator = 0; // 0 = not found, otherwise the sign, +1 or -1
        if (ts.get(0).hasCategory("pre_special_hour")) {
            // found a word that usually comes before special hours, e.g. questo, dopo
            if (ts.get(0).hasCategory("pre_relative_indicator")) {
                relativeIndicator = ts.get(0).hasCategory("negative") ? -1 : 1;
                // only move to next not ignore if we got a relative indicator
                ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1));
            } else {
                ts.movePositionForwardBy(1);
            }
        }

        if (ts.get(0).hasCategory(category)) {
            // special hour found, e.g. mezzanotte, sera, pranzo
            ts.movePositionForwardBy(1);
            return ((int) ts.get(-1).getNumber().integerValue() + HOURS_IN_DAY + relativeIndicator)
                    % HOURS_IN_DAY;
        }

        // noon/midnight have both the categores noon_midnight_like and moment_of_day, always try
        if (ts.get(0).getValue().startsWith("mezz")) {
            // sometimes e.g. "mezzogiorno" is split into "mezzo giorno"
            if (ts.get(1).getValue().startsWith("giorn")) {
                ts.movePositionForwardBy(2);
                return 12 + relativeIndicator;
            } else if (ts.get(1).getValue().startsWith("nott")) {
                ts.movePositionForwardBy(2);
                return (HOURS_IN_DAY + relativeIndicator) % HOURS_IN_DAY;
            }
        }

        // no special hour found
        ts.setPosition(originalPosition);
        return null;
    }

    Integer hour() {
        int originalPosition = ts.getPosition();

        // skip words that usually come before hours, e.g. alle, ore
        ts.movePositionForwardBy(ts.indexOfWithoutCategory("pre_hour", 0));

        final Integer number = extractIntegerInRange(0, HOURS_IN_DAY);
        if (number == null) {
            // no number found, or the number is not a valid hour, e.g. le ventisei
            ts.setPosition(originalPosition);
            return null;
        }

        // found hour, e.g. alle diciannove
        return number % HOURS_IN_DAY; // transform 24 into 0
    }


    private LocalDate relativeSpecialDay() {
        final Integer days = firstNotNull(this::relativeYesterday, dateTimeExtractor::relativeToday,
                this::relativeTomorrow, dateTimeExtractor::relativeDayOfWeekDuration);
        if (days == null) {
            return null;
        }
        return now.toLocalDate().plusDays(days);
    }

    Integer relativeYesterday() {
        int originalPosition = ts.getPosition();

        // collect as many adders ("altro") preceding yesterday ("ieri") as possible
        int dayCount = 0;
        while (ts.get(0).hasCategory("yesterday_adder")) {
            ++dayCount;
            ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1));
        }

        // collect the actual yesterday ("ieri") and exit if it is not found
        if (!ts.get(0).hasCategory("yesterday")) {
            ts.setPosition(originalPosition);
            return null;
        }
        ts.movePositionForwardBy(1);
        ++dayCount;

        // if no adders were collected before yesterday, try to collect only one at the end
        int nextNotIgnore = ts.indexOfWithoutCategory("date_time_ignore", 0);
        if (dayCount == 1 && ts.get(nextNotIgnore).hasCategory("yesterday_adder")) {
            ++dayCount;
            ts.movePositionForwardBy(nextNotIgnore + 1);
        }

        // found relative yesterday, e.g. altro altro ieri, ieri l'altro
        return -dayCount;
    }

    Integer relativeTomorrow() {
        int originalPosition = ts.getPosition();

        // collect as many "dopo" preceding "domani" as possible
        int dayCount = 0;
        while (ts.get(0).hasCategory("tomorrow_adder")) {
            ++dayCount;
            ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1));
        }

        // collect the actual "domani" and exit if it is not found
        if (!ts.get(0).hasCategory("tomorrow")) {
            ts.setPosition(originalPosition);
            return null;
        }
        ts.movePositionForwardBy(1);
        ++dayCount;

        // found relative tomorrow, e.g. domani, dopo dopo domani
        return dayCount;
    }

    Duration relativeDuration() {
        return dateTimeExtractor.relativeIndicatorDuration(durationExtractor::duration,
                duration -> duration.multiply(-1));
    }
}
