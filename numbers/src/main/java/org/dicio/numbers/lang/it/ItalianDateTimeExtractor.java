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
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItalianDateTimeExtractor {
    
    private static final long DAYS_IN_WEEK = DayOfWeek.values().length;
    private static final long MONTHS_IN_YEAR = Month.values().length;

    private final TokenStream ts;
    private final LocalDateTime now;
    private final ItalianNumberExtractor numberExtractor;
    private final DurationExtractorUtils durationExtractor;

    ItalianDateTimeExtractor(final TokenStream tokenStream, final LocalDateTime now) {
        this.ts = tokenStream;
        this.now = now;
        this.numberExtractor = new ItalianNumberExtractor(ts, false);
        this.durationExtractor = new DurationExtractorUtils(ts,
                numberExtractor::extractOneNumberNoOrdinal);
    }

    private Integer extractIntegerInRange(final int fromInclusive, final int toInclusive) {
        // disallow fraction as / should be treated as a day/month/year separator
        return NumberExtractorUtils.extractOneIntegerInRange(ts, fromInclusive, toInclusive,
                () -> numberExtractor.numberSignPoint(false, false));
    }


    LocalDate date() {
        LocalDate result = now.toLocalDate();

        final Integer dayOfWeek = dayOfWeek();
        final Integer day = tryOrSkipIgnore("date_time_ignore", dayOfWeek == null,
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

        final Integer month = tryOrSkipIgnore("date_time_ignore", day == null,
                () -> {
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
        final Integer year = tryOrSkipIgnore("date_time_ignore", month == null,
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

    <T> T tryOrSkipIgnore(final String skipCategory,
                          final boolean disallowSkippingIgnore,
                          final Supplier<T> function) {
        if (disallowSkippingIgnore) {
            return function.get();
        }

        final int originalPosition = ts.getPosition();
        do {
            final T result = function.get();
            if (result != null) {
                return result;
            }
            ts.movePositionForwardBy(1);
        } while (ts.get(-1).hasCategory(skipCategory) && !ts.finished());

        // found nothing, restore position
        ts.setPosition(originalPosition);
        return null;
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
        return bcadOrAmpm("ampm_before", "ampm_after", "ampm_identifier");
    }

    Boolean bcad() {
        return bcadOrAmpm("bcad_before", "bcad_after", "bcad_identifier");
    }

    /**
     * @return false if before+identifier matches, true if after+identifier matches, null otherwise
     */
    private Boolean bcadOrAmpm(final String before, final String after, final String identifier) {
        final boolean result;
        if (ts.get(0).hasCategory(before)) {
            result = false;
        } else if (ts.get(0).hasCategory(after)) {
            result = true;
        } else if (ts.get(0).hasCategory(identifier)) {
            // identifier without no preceding before/after -> return "before" (a.m. or B.C.)
            ts.movePositionForwardBy(1);
            return false;
        } else {
            return null;
        }

        // we can't use ts.indexOfWithoutCategory, since some ignore words might be identifiers
        final int originalPosition = ts.getPosition();
        do {
            ts.movePositionForwardBy(1);
        } while (!ts.get(0).hasCategory(identifier) && ts.get(0).hasCategory("ignore"));

        if (ts.get(0).hasCategory(identifier)) {
            ts.movePositionForwardBy(1);
            return result;
        } else {
            ts.setPosition(originalPosition);
            return null;
        }
    }


    Integer second() {
        return minuteOrSecond("1 SECONDS");
    }

    Integer specialMinute() {
        final int originalPosition = ts.getPosition();

        final Number number = numberExtractor.extractOneNumberNoOrdinal();
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


    Integer specialHour() {
        int originalPosition = ts.getPosition();

        if (ts.get(0).hasCategory("pre_special_hour")) {
            // found a word that usually comes before special hours, e.g. questo
            ts.movePositionForwardBy(1);
        }

        if (ts.get(0).hasCategory("special_hour")) {
            // special hour found, e.g. mezzanotte, sera, pranzo
            ts.movePositionForwardBy(1);
            return (int) ts.get(-1).getNumber().integerValue();
        }

        if (ts.get(0).getValue().startsWith("mezz")) {
            // sometimes e.g. "mezzogiorno" is split into "mezzo giorno"
            if (ts.get(1).getValue().startsWith("giorn")) {
                ts.movePositionForwardBy(2);
                return 12;
            } else if (ts.get(1).getValue().startsWith("nott")) {
                ts.movePositionForwardBy(2);
                return 24;
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

        final Integer number = extractIntegerInRange(0, 24);
        if (number == null) {
            // no number found, or the number is not a valid hour, e.g. le ventisei
            ts.setPosition(originalPosition);
            return null;
        }

        // found hour, e.g. alle diciannove
        return number;
    }


    private Duration relativeSpecialDay() {
        return firstNotNull(this::relativeYesterday, this::relativeToday, this::relativeTomorrow);
    }

    Duration relativeYesterday() {
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
        return new Duration().plus(new Number(-dayCount), ChronoUnit.DAYS);
    }

    Duration relativeToday() {
        if (ts.get(0).hasCategory("today")) {
            ts.movePositionForwardBy(1);
            return new Duration(); // no offset
        } else {
            return null;
        }
    }

    Duration relativeTomorrow() {
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
        return new Duration().plus(new Number(dayCount), ChronoUnit.DAYS);
    }

    Duration relativeDayOfWeekDuration() {
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
                final long daysDifference
                        = ts.get(0).getNumber().integerValue() - now.getDayOfWeek().ordinal();
                final long daysOffset = (daysDifference + DAYS_IN_WEEK) % DAYS_IN_WEEK
                        // add a week if the two days coincide
                        + (daysDifference == 0 ? DAYS_IN_WEEK : 0)
                        // sum some additional weeks if the input says so
                        + (number - 1) * DAYS_IN_WEEK;
                ts.movePositionForwardBy(1);
                return new Duration().plus(new Number(daysOffset), ChronoUnit.DAYS);
            } else {
                return null;
            }

        }, duration -> {
            final long daysOffset = duration.getDays().integerValue();
            final long newDaysOffset = daysOffset % DAYS_IN_WEEK == 0
                    // the congruency modulo DAYS_IN_WEEK is 0: just use a minus to maintain it
                    ? -daysOffset
                    // keep congruency modulo DAYS_IN_WEEK, taking care of additional weeks
                    : 2 * (daysOffset % DAYS_IN_WEEK) - DAYS_IN_WEEK - daysOffset;
            return new Duration().plus(new Number(newDaysOffset), ChronoUnit.DAYS);
        });
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
        return relativeIndicatorDuration(durationExtractor::extractDurationAtCurrentPosition,
                duration -> duration.multiply(new Number(-1)));
    }

    private Duration relativeIndicatorDuration(
            final Supplier<Duration> durationExtractor,
            final Function<Duration, Duration> oppositeDuration) {
        final int originalTsPosition = ts.getPosition();

        int relativeIndicator = 0; // 0 = not found, otherwise the sign, +1 or -1
        if (ts.get(0).hasCategory("pre_relative_indicator")) {
            // there is a relative indicator before, e.g. fra
            relativeIndicator = ts.get(0).hasCategory("negative") ? -1 : 1;
            ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 1));
        }

        final Duration result = durationExtractor.get();
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
