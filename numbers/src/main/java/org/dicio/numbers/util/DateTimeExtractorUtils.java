package org.dicio.numbers.util;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DateTimeExtractorUtils {
    public static final int HOURS_IN_DAY = 24;
    public static final int DAYS_IN_WEEK = DayOfWeek.values().length; // 7
    public static final int MONTHS_IN_YEAR = Month.values().length; // 12

    private final TokenStream ts;
    private final LocalDateTime now;
    private final BiFunction<Integer, Integer, Integer> extractIntegerInRange;

    /**
     * This class should work well at least for european languages (I don't know the structure of
     * other languages though). Requires the token stream to have been tokenized with the same rules
     * as in the English language.
     *
     * @param tokenStream the token stream from which to obtain information
     */
    public DateTimeExtractorUtils(
            final TokenStream tokenStream,
            final LocalDateTime now,
            final BiFunction<Integer, Integer, Integer> extractIntegerInRange
    ) {
        this.ts = tokenStream;
        this.now = now;
        this.extractIntegerInRange = extractIntegerInRange;
    }

    public Boolean ampm() {
        return bcadOrAmpm("ampm");
    }

    public Boolean bcad() {
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


    public static Boolean isMomentOfDayPm(final Integer momentOfDay) {
        if (momentOfDay == null) {
            return null;
        }
        return momentOfDay >= 12;
    }

    public Integer monthName() {
        if (ts.get(0).hasCategory("month_name")) {
            ts.movePositionForwardBy(1);
            return (int) ts.get(-1).getNumber().integerValue();
        } else {
            return null;
        }
    }

    public Integer dayOfWeek() {
        if (ts.get(0).hasCategory("day_of_week")) {
            ts.movePositionForwardBy(1);
            return (int) ts.get(-1).getNumber().integerValue();
        } else {
            return null;
        }
    }


    public Integer second() {
        return minuteOrSecond("1 SECONDS");
    }

    public Integer minute() {
        return minuteOrSecond("1 MINUTES");
    }

    public Integer minuteOrSecond(final String durationCategory) {
        final Integer number = extractIntegerInRange.apply(0, 59);
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


    public Integer relativeToday() {
        if (ts.get(0).hasCategory("today")) {
            ts.movePositionForwardBy(1);
            return 0; // no offset
        } else {
            return null;
        }
    }

    public Integer relativeDayOfWeekDuration() {
        return relativeIndicatorDuration(() -> {
            Integer number = extractIntegerInRange.apply(1, Integer.MAX_VALUE);
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

    public Duration relativeMonthDuration() {
        final Long months = relativeIndicatorDuration(() -> {
            if (ts.get(0).hasCategory("month_name")) {
                final long monthsDifference
                        = ts.get(0).getNumber().integerValue() - now.getMonth().getValue();
                final long monthsOffset = (monthsDifference + MONTHS_IN_YEAR) % MONTHS_IN_YEAR
                        // add a year if the two months coincide
                        + (monthsDifference == 0 ? MONTHS_IN_YEAR : 0);
                ts.movePositionForwardBy(1);
                return monthsOffset;
            }
            return null;

        }, monthsOffset -> monthsOffset == MONTHS_IN_YEAR
                // the congruency modulo MONTHS_IN_YEAR is 0: just use a minus to maintain it
                ? -MONTHS_IN_YEAR
                // keep congruency modulo MONTHS_IN_YEAR
                : monthsOffset - MONTHS_IN_YEAR);

        return months == null ? null : new Duration(0, 0, months, 0);
    }

    public <T> T relativeIndicatorDuration(final Supplier<T> durationExtractor,
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
        if (relativeIndicator == 0
                && ts.get(nextNotIgnore).hasCategory("post_relative_indicator")) {
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
