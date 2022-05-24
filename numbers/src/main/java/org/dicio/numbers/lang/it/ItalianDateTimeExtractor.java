package org.dicio.numbers.lang.it;

import static org.dicio.numbers.util.Utils.firstNotNull;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.unit.Number;
import org.dicio.numbers.util.DurationExtractorUtils;
import org.dicio.numbers.util.NumberExtractorUtils;

import java.time.DayOfWeek;
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

    Number specialMinute() {
        final int originalPosition = ts.getPosition();

        final Number number = numberExtractor.extractOneNumberNoOrdinal();
        if (number != null && number.isDecimal()
                && number.decimalValue() > 0.0 && number.decimalValue() < 1.0) {
            return number.multiply(60);
        }

        ts.setPosition(originalPosition);
        return null;
    }

    Number minute() {
        final Number number = NumberExtractorUtils.numberLessThan1000InRange(ts, false, 0, 59);
        if (number == null) {
            return null;
        }

        if (ts.get(0).isDurationToken()
                && ts.get(0).asDurationToken().getDurationCategory().equals("1 MINUTES")) {
            // skip "minuti" said after a minute count, e.g. ventiquattro minuti
            ts.movePositionForwardBy(1);
        }

        return number;
    }


    Number specialHour() {
        int originalPosition = ts.getPosition();

        if (ts.get(0).hasCategory("pre_special_hour")) {
            // found a word that usually comes before special hours, e.g. questo
            ts.movePositionForwardBy(1);
        }

        if (ts.get(0).hasCategory("special_hour")) {
            // special hour found, e.g. mezzanotte, sera, pranzo
            ts.movePositionForwardBy(1);
            return ts.get(-1).getNumber();
        }

        if (ts.get(0).getValue().startsWith("mezz")) {
            // sometimes e.g. "mezzogiorno" is split into "mezzo giorno"
            if (ts.get(1).getValue().startsWith("giorn")) {
                ts.movePositionForwardBy(2);
                return new Number(12);
            } else if (ts.get(1).getValue().startsWith("nott")) {
                ts.movePositionForwardBy(2);
                return new Number(24);
            }
        }

        // no special hour found
        ts.setPosition(originalPosition);
        return null;
    }

    Number hour() {
        int originalPosition = ts.getPosition();

        if (ts.get(0).hasCategory("pre_hour")) {
            // found a word that usually comes before hours, e.g. alle
            ts.movePositionForwardBy(1);
            // ^ numberLessThan1000 takes care of ignoring the "ignore" category, so only move by 1
        }

        final Number number = NumberExtractorUtils.numberLessThan1000InRange(ts, false, 0, 24);
        if (number == null) {
            // no number found, or the number is not a valid hour, e.g. le ventisei
            ts.setPosition(originalPosition);
            return null;
        }

        // found hour, e.g. alle diciannove
        return number;
    }


    Duration relativeSpecialDay() {
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
            Number number = numberExtractor.extractOneNumberNoOrdinal();
            if (number == null) {
                // there does not need to be a number, e.g. giovedì prossimo
                number = new Number(1);
            } else {
                // found a number, e.g. fra due
                ts.movePositionForwardBy(ts.indexOfWithoutCategory("date_time_ignore", 0));
                if (!number.isInteger() || number.lessThan(1)) {
                    // invalid number for relative week duration, e.g. fra 1.5 martedì
                    return null;
                }
            }

            if (ts.get(0).hasCategory("day_of_week")) {
                // found a day of week, e.g. giovedì
                final long daysDifference
                        = ts.get(0).getNumber().integerValue() - now.getDayOfWeek().ordinal();
                final long daysOffset = (daysDifference + DAYS_IN_WEEK) % DAYS_IN_WEEK
                        // add a week if the two days coincide
                        + (daysDifference == 0 ? DAYS_IN_WEEK : 0)
                        // sum some additional weeks if the input says so
                        + (number.integerValue() - 1) * DAYS_IN_WEEK;
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
