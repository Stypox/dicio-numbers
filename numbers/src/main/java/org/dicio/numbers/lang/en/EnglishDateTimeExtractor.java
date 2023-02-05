package org.dicio.numbers.lang.en;

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

public class EnglishDateTimeExtractor {

    private final TokenStream ts;
    private final boolean preferMonthBeforeDay;
    private final LocalDateTime now;
    private final EnglishNumberExtractor numberExtractor;
    private final DurationExtractorUtils durationExtractor;
    public final DateTimeExtractorUtils dateTimeExtractor; // TODO private

    EnglishDateTimeExtractor(final TokenStream tokenStream,
                             final boolean shortScale,
                             final boolean preferMonthBeforeDay,
                             final LocalDateTime now) {
        this.ts = tokenStream;
        this.preferMonthBeforeDay = preferMonthBeforeDay;
        this.now = now;
        this.numberExtractor = new EnglishNumberExtractor(ts, shortScale);
        this.durationExtractor = new DurationExtractorUtils(ts, numberExtractor::numberNoOrdinal);
        this.dateTimeExtractor = new DateTimeExtractorUtils(ts, now, this::extractIntegerInRange);
    }

    private Integer extractIntegerInRange(final int fromInclusive, final int toInclusive) {
        return extractIntegerInRange(fromInclusive, toInclusive, false);
    }

    private Integer extractIntegerInRange(final int fromInclusive,
                                          final int toInclusive,
                                          final boolean allowOrdinal) {
        // disallow fraction as / should be treated as a day/month/year separator
        return NumberExtractorUtils.extractOneIntegerInRange(ts, fromInclusive, toInclusive,
                () -> signBeforeNumber(ts, () -> numberExtractor.numberInteger(allowOrdinal)));
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
        final int originalPosition = ts.getPosition();
        final Integer specialMinute = specialMinute();

        // try both with a normal hour and with "mezzogiorno"/"mezzanotte"
        final Integer hour = firstNotNull(this::noonMidnightLike, this::hour);
        if (hour == null) {
            ts.setPosition(originalPosition);
            return null;
        } else if (specialMinute != null) {
            // we can't use special minute on its own, but only when there is a hour
            if (specialMinute < 0) {
                return LocalTime.of((hour + HOURS_IN_DAY - 1) % HOURS_IN_DAY, 60 + specialMinute); // e.g. quarter to six
            } else {
                return LocalTime.of(hour, specialMinute); // e.g. half past seven
            }
        }
        LocalTime result = LocalTime.of(hour, 0);

        if (oClock()) {
            return result; // e.g. ten o'clock
        }

        final Integer minute = ts.tryOrSkipDateTimeIgnore(true, dateTimeExtractor::minute);
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

        final Integer dayOfWeek = dateTimeExtractor.dayOfWeek();
        final Integer firstNum = ts.tryOrSkipDateTimeIgnore(dayOfWeek != null,
                () -> extractIntegerInRange(1, 31, true));

        if (firstNum == null && dayOfWeek != null) {
            // TODO maybe enforce the date to be in the future?
            return result.plus(dayOfWeek - result.getDayOfWeek().ordinal(), ChronoUnit.DAYS);
        }

        final Integer monthName = ts.tryOrSkipDateTimeIgnore(firstNum != null,
                dateTimeExtractor::monthName);
        if (monthName == null) {
            if (firstNum == null) {
                result = result.withDayOfMonth(1).withMonth(1);

            } else {
                final int secondNumMax = firstNum <= 12 ? 31 : 12;
                final Integer secondNum = ts.tryOrSkipDateTimeIgnore(true,
                        () -> extractIntegerInRange(1, secondNumMax, true));

                if (secondNum == null) {
                    if (preferMonthBeforeDay && firstNum <= 12) {
                        return result.withDayOfMonth(1).withMonth(firstNum);
                    } else {
                        return result.withDayOfMonth(firstNum);
                    }

                } else {
                    if ((preferMonthBeforeDay || secondNum > 12) && firstNum <= 12) {
                        result = result.withDayOfMonth(secondNum).withMonth(firstNum);
                    } else {
                        // secondNum is surely <= 12 here because of secondNumMax
                        result = result.withDayOfMonth(firstNum).withMonth(secondNum);
                    }
                }
            }

        } else {
            result = result.withMonth(monthName);

            if (firstNum == null) {
                final Integer secondNum = ts.tryOrSkipDateTimeIgnore(true,
                        () -> extractIntegerInRange(1, 31, true));
                if (secondNum == null) {
                    result = result.withDayOfMonth(1);
                } else {
                    result = result.withDayOfMonth(secondNum);
                }
            } else {
                result = result.withDayOfMonth(firstNum);
            }
        }
        final boolean dayOrMonthFound = firstNum != null || monthName != null;

        // we might have AD before the year, too
        Boolean bcad = ts.tryOrSkipDateTimeIgnore(dayOrMonthFound, this::bcad);

        // if month is null then day is also null, otherwise we would have returned above
        final Integer year = ts.tryOrSkipDateTimeIgnore(dayOrMonthFound && bcad == null,
                () -> extractIntegerInRange(0, 999999999));
        if (year == null) {
            if (dayOrMonthFound) {
                return result;
            }
            return null;
        }

        if (bcad == null) {
            bcad = bcad();
        }
        return result.withYear(year * (bcad == null || bcad ? 1 : -1));
    }


    Boolean bcad() {
        final Boolean bcad = dateTimeExtractor.bcad();
        if (bcad != null && !bcad) {
            // skip "era" in "before current era"
            final int nextNotIgnore = ts.indexOfWithoutCategory("date_time_ignore", 0);
            if (ts.get(nextNotIgnore).hasCategory("bcad_era")) {
                ts.movePositionForwardBy(nextNotIgnore + 1);
            }
        }
        return bcad;
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
            // found a word that usually comes before special hours, e.g. this, in
            if (ts.get(0).hasCategory("pre_relative_indicator")) {
                relativeIndicator = ts.get(0).hasCategory("negative") ? -1 : 1;
                // only move to next not ignore if we got a relative indicator, e.g. in the ...
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

        // no special hour found
        ts.setPosition(originalPosition);
        return null;
    }

    Integer hour() {
        int originalPosition = ts.getPosition();

        // skip words that usually come before hours, e.g. at, hour
        ts.movePositionForwardBy(ts.indexOfWithoutCategory("pre_hour", 0));

        final Integer number = extractIntegerInRange(0, HOURS_IN_DAY);
        if (number == null) {
            // no number found, or the number is not a valid hour, e.g. at twenty six
            ts.setPosition(originalPosition);
            return null;
        }

        // found hour, e.g. at nineteen
        return number % HOURS_IN_DAY; // transform 24 into 0
    }

    public Integer specialMinute() {
        final int originalPosition = ts.getPosition();

        // skip words that usually come before hours, e.g. at, hour
        ts.movePositionForwardBy(ts.indexOfWithoutCategory("pre_hour", 0));

        final Number number = numberExtractor.numberNoOrdinal();
        if (number != null) {
            final int minutes;
            if (number.isDecimal() && number.decimalValue() > 0.0 && number.decimalValue() < 1.0) {
                minutes = roundToInt(number.decimalValue() * 60); // e.g. three quarters past one
            } else if (number.isInteger()
                    && number.integerValue() > 1 && number.integerValue() < 60) {
                minutes = (int) number.integerValue(); // e.g. ten to eleven
            } else {
                ts.setPosition(originalPosition);
                return null;
            }

            final Integer result = ts.tryOrSkipDateTimeIgnore(true, () -> {
                if (ts.get(0).hasCategory("special_minute_after")) {
                    // e.g. half past twelve
                    ts.movePositionForwardBy(1);
                    return minutes;
                } else if (ts.get(0).hasCategory("special_minute_before")) {
                    // e.g. quarter to eleven
                    ts.movePositionForwardBy(1);
                    return -minutes;
                } else {
                    return null;
                }
            });
            if (result != null) {
                return result;
            }
        }

        ts.setPosition(originalPosition);
        return null;
    }

    public boolean oClock() {
        if (ts.get(0).hasCategory("pre_oclock")) {
            final int nextNotIgnore = ts.indexOfWithoutCategory("date_time_ignore", 1);
            if (ts.get(nextNotIgnore).hasCategory("post_oclock")) {
                ts.movePositionForwardBy(nextNotIgnore + 1);
                return true;
            }
        } else if (ts.get(0).hasCategory("oclock_combined")) {
            ts.movePositionForwardBy(1);
            return true;
        }
        return false;
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
        if (ts.get(0).hasCategory("day_adder_the")
                && ts.get(1).hasCategory("day_adder_day")
                && ts.get(2).hasCategory("day_adder_before")
                && ts.get(3).hasCategory("yesterday")) {
            ts.movePositionForwardBy(4);
            return -2; // e.g. the day before yesterday
        }

        // "the" is optional
        if (ts.get(0).hasCategory("day_adder_day")
                && ts.get(1).hasCategory("day_adder_before")
                && ts.get(2).hasCategory("yesterday")) {
            ts.movePositionForwardBy(3);
            return -2; // e.g. day before yesterday
        }

        if (ts.get(0).hasCategory("yesterday")) {
            ts.movePositionForwardBy(1);
            return -1; // e.g. yesterday
        } else {
            return null;
        }
    }

    Integer relativeTomorrow() {
        if (ts.get(0).hasCategory("day_adder_the")
                && ts.get(1).hasCategory("day_adder_day")
                && ts.get(2).hasCategory("day_adder_after")
                && ts.get(3).hasCategory("tomorrow")) {
            ts.movePositionForwardBy(4);
            return 2; // e.g. the day after tomorrow
        }

        // "the" is optional
        if (ts.get(0).hasCategory("day_adder_day")
                && ts.get(1).hasCategory("day_adder_after")
                && ts.get(2).hasCategory("tomorrow")) {
            ts.movePositionForwardBy(3);
            return 2; // e.g. day after tomorrow
        }

        if (ts.get(0).hasCategory("tomorrow")) {
            ts.movePositionForwardBy(1);
            return 1; // e.g. tomorrow
        } else {
            return null;
        }
    }

    Duration relativeDuration() {
        return dateTimeExtractor.relativeIndicatorDuration(durationExtractor::duration,
                duration -> duration.multiply(-1));
    }
}
