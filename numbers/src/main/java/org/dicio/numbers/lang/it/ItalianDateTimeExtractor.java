package org.dicio.numbers.lang.it;

import static org.dicio.numbers.util.Utils.firstNotNull;
import static org.dicio.numbers.util.Utils.roundToInt;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;
import org.dicio.numbers.util.DurationExtractorUtils;
import org.dicio.numbers.util.NumberExtractorUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItalianDateTimeExtractor {

    private static final int HOURS_IN_DAY = 24;
    private static final int DAYS_IN_WEEK = DayOfWeek.values().length; // 7
    private static final int MONTHS_IN_YEAR = Month.values().length; // 12

    private final TokenStream ts;
    private final LocalDateTime now;
    private final ItalianNumberExtractor numberExtractor;
    private final DurationExtractorUtils durationExtractor;

    ItalianDateTimeExtractor(final TokenStream tokenStream, final LocalDateTime now) {
        this.ts = tokenStream;
        this.now = now;
        this.numberExtractor = new ItalianNumberExtractor(ts);
        this.durationExtractor = new DurationExtractorUtils(ts, numberExtractor::numberNoOrdinal);
    }

    private Integer extractIntegerInRange(final int fromInclusive, final int toInclusive) {
        // disallow fraction as / should be treated as a day/month/year separator
        return NumberExtractorUtils.extractOneIntegerInRange(ts, fromInclusive, toInclusive,
                () -> numberExtractor.numberSignPoint(false, false));
    }


    LocalDateTime dateTime() {
        // first try preferring having a date first, then try with time first
        return ts.firstWhichUsesMostTokens(() -> dateTime(false), () -> dateTime(true));
    }

    private LocalDateTime dateTime(final boolean timeFirst) {
        LocalDate date = null;
        LocalTime time = null;

        if (!timeFirst) {
            final Duration duration
                    = firstNotNull(this::relativeDuration, this::relativeMonthDuration);
            if (duration == null) {
                // no normal relative duration found: start extracting a date normally
                date = firstNotNull(this::relativeSpecialDay, this::date);
            } else if (duration.getNanos().equals(0) && !duration.getDays().equals(0)) {
                // duration contains a specified day and no specified time, so a time can follow
                date = duration.applyAsOffsetToDateTime(now).toLocalDate();
            } else if (!duration.getNanos().equals(0) && duration.getDays().equals(0)
                    && duration.getMonths().equals(0) && duration.getYears().equals(0)) {
                // duration contains a specified time, so a date could follow
                time = duration.applyAsOffsetToDateTime(now).toLocalTime();
            } else if (duration.getNanos().equals(0)) {
                // duration contains mixed date&time, or specifies units >=month, nothing can follow
                return duration.applyAsOffsetToDateTime(now);
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
            } else if (duration.getNanos().equals(0) && !duration.getDays().equals(0)) {
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
            pm = ts.tryOrSkipDateTimeIgnore(true,
                    () -> firstNotNull(this::ampm, () -> isMomentOfDayPm(momentOfDay())));
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
                () -> firstNotNull(this::specialMinute, this::minute));
        if (minute == null) {
            return result;
        }
        result = result.withMinute(minute);

        final Integer second = ts.tryOrSkipDateTimeIgnore(true, this::second);
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

        final Integer month = ts.tryOrSkipDateTimeIgnore(day != null, () -> {
            final Integer number = monthName();
            if (number != null) {
                return number + 1;
            }
            return extractIntegerInRange(1, 12);
        });

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
                () -> extractIntegerInRange(0, Integer.MAX_VALUE));
        if (year == null) {
            if (month != null) {
                return result;
            }
            return null;
        }

        final Boolean bcad = bcad();
        return result.withYear(year * (bcad == null || bcad ? 1 : -1));
    }


    Integer monthName() {
        if (ts.get(0).hasCategory("month_name")) {
            ts.movePositionForwardBy(1);
            return (int) ts.get(-1).getNumber().integerValue();
        } else {
            return null;
        }
    }

    Integer dayOfWeek() {
        if (ts.get(0).hasCategory("day_of_week")) {
            ts.movePositionForwardBy(1);
            return (int) ts.get(-1).getNumber().integerValue();
        } else if (ts.get(0).isValue("mar")) {
            ts.movePositionForwardBy(1);
            return 1; // special case, since mar already used for march
        } else {
            return null;
        }
    }


    Boolean ampm() {
        return bcadOrAmpm("ampm");
    }

    Boolean bcad() {
        return bcadOrAmpm("bcad");
    }

    /**
     * @param prefix either "bcad" or "ampm", i.e. the prefix to use for the following categories:
     *               _before, _after, _identifier, _before_combined, _after_combined
     * @return false if before+identifier matches, true if after+identifier matches, null otherwise
     */
    private Boolean bcadOrAmpm(final String prefix) {
        final boolean result;
        ts.movePositionForwardBy(1);
        if (ts.get(-1).hasCategory(prefix + "_before")) {
            result = false;
        } else if (ts.get(-1).hasCategory(prefix + "_after")) {
            result = true;
        } else if (ts.get(-1).hasCategory(prefix + "_before_combined")) {
            // found am or bc in a single word -> return "before"
            return false;
        } else if (ts.get(-1).hasCategory(prefix + "_after_combined")) {
            // found pm or ad in a single word -> return "after"
            return true;
        } else {
            // nothing related to bc/ad/am/pm here (even if the identifier might match on its own)
            ts.movePositionForwardBy(-1);
            return null;
        }

        // we can't use ts.indexOfWithoutCategory, since some ignore words might be identifiers
        final Boolean foundIdentifier = ts.tryOrSkipCategory("ignore", true,
                () -> ts.get(0).hasCategory(prefix + "_identifier") ? true : null);
        if (foundIdentifier != null) {
            ts.movePositionForwardBy(1);
            return result;
        } else {
            ts.movePositionForwardBy(-1);
            return null;
        }
    }


    Integer second() {
        return minuteOrSecond("1 SECONDS");
    }

    Integer specialMinute() {
        final int originalPosition = ts.getPosition();

        final Number number = numberExtractor.numberNoOrdinal();
        if (number != null && number.isDecimal()
                && number.decimalValue() > 0.0 && number.decimalValue() < 1.0) {
            return roundToInt(number.decimalValue() * 60);
        }

        ts.setPosition(originalPosition);
        return null;
    }

    Integer minute() {
        return minuteOrSecond("1 MINUTES");
    }

    private Integer minuteOrSecond(final String durationCategory) {
        final Integer number = extractIntegerInRange(0, 59);
        if (number == null) {
            return null;
        }

        if (ts.get(0).isDurationToken()
                && ts.get(0).asDurationToken().getDurationCategory().equals(durationCategory)) {
            // skip "minuti"/"secondi" said after a minute/second count, e.g. ventiquattro minuti
            ts.movePositionForwardBy(1);
        }

        return number;
    }


    static Boolean isMomentOfDayPm(final Integer momentOfDay) {
        if (momentOfDay == null) {
            return null;
        }
        return momentOfDay >= 12;
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
        final Integer days = firstNotNull(this::relativeYesterday, this::relativeToday,
                this::relativeTomorrow, this::relativeDayOfWeekDuration);
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

    Integer relativeToday() {
        if (ts.get(0).hasCategory("today")) {
            ts.movePositionForwardBy(1);
            return 0; // no offset
        } else {
            return null;
        }
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

    Integer relativeDayOfWeekDuration() {
        return relativeIndicatorDuration(() -> {
            Integer number = extractIntegerInRange(1, Integer.MAX_VALUE);
            if (number == null) {
                // there does not need to be a number, e.g. giovedì prossimo
                number = 1;
            } else {
                // found a number, e.g. fra due
                ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 0));
            }

            if (ts.get(0).hasCategory("day_of_week")) {
                // found a day of week, e.g. giovedì
                final int daysDifference
                        = (int) ts.get(0).getNumber().integerValue() - now.getDayOfWeek().ordinal();
                final int daysOffset = (daysDifference + DAYS_IN_WEEK) % DAYS_IN_WEEK
                        // add a week if the two days coincide
                        + (daysDifference == 0 ? DAYS_IN_WEEK : 0)
                        // sum some additional weeks if the input says so
                        + (number - 1) * DAYS_IN_WEEK;
                ts.movePositionForwardBy(1);
                return daysOffset;
            } else {
                return null;
            }

        }, daysOffset -> daysOffset % DAYS_IN_WEEK == 0
                // the congruency modulo DAYS_IN_WEEK is 0: just use a minus to maintain it
                ? -daysOffset
                // keep congruency modulo DAYS_IN_WEEK, taking care of additional weeks
                : 2 * (daysOffset % DAYS_IN_WEEK) - DAYS_IN_WEEK - daysOffset);
    }

    Duration relativeMonthDuration() {
        return relativeIndicatorDuration(() -> {
            if (ts.get(0).hasCategory("month_name")) {
                final long monthsDifference
                        = ts.get(0).getNumber().integerValue() - now.getMonth().ordinal();
                final long monthsOffset = (monthsDifference + MONTHS_IN_YEAR) % MONTHS_IN_YEAR
                        // add a year if the two months coincide
                        + (monthsDifference == 0 ? MONTHS_IN_YEAR : 0);
                ts.movePositionForwardBy(1);
                return new Duration().plus(new Number(monthsOffset), ChronoUnit.MONTHS);
            }
            return null;

        }, duration -> {
            final long monthsOffset = duration.getMonths().integerValue();
            final long newMonthsOffset = monthsOffset == MONTHS_IN_YEAR
                    // the congruency modulo MONTHS_IN_YEAR is 0: just use a minus to maintain it
                    ? -MONTHS_IN_YEAR
                    // keep congruency modulo MONTHS_IN_YEAR
                    : monthsOffset - MONTHS_IN_YEAR;
            return new Duration().plus(new Number(newMonthsOffset), ChronoUnit.MONTHS);
        });
    }

    Duration relativeDuration() {
        return relativeIndicatorDuration(durationExtractor::duration,
                duration -> duration.multiply(new Number(-1)));
    }

    private <T> T relativeIndicatorDuration(final Supplier<T> durationExtractor,
                                            final Function<T, T> oppositeDuration) {
        final int originalTsPosition = ts.getPosition();

        int relativeIndicator = 0; // 0 = not found, otherwise the sign, +1 or -1
        if (ts.get(0).hasCategory("pre_relative_indicator")) {
            // there is a relative indicator before, e.g. fra
            relativeIndicator = ts.get(0).hasCategory("negative") ? -1 : 1;
            ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1));
        }

        final T result = durationExtractor.get();
        if (result == null) {
            // no duration found, e.g. tra sei ciao
            ts.setPosition(originalTsPosition);
            return null;
        }

        final int nextNotIgnore = ts.indexOfWithoutCategory("date_time_ignore", 0);
        if (relativeIndicator == 0 && ts.get(nextNotIgnore).hasCategory("post_relative_indicator")) {
            // there is a relative indicator after, e.g. due settimane fa
            relativeIndicator = ts.get(nextNotIgnore).hasCategory("negative") ? -1 : 1;
            ts.movePositionForwardBy(nextNotIgnore + 1);
        }

        if (relativeIndicator == 0) {
            // no relative indicator found, this is not a relative duration, e.g. sei mesi
            ts.setPosition(originalTsPosition);
            return null;
        } else {
            // found relative duration, e.g. tra due minuti
            return relativeIndicator == -1
                    ? oppositeDuration.apply(result)
                    : result;
        }
    }
}
