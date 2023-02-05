package org.dicio.numbers.unit;

import static org.dicio.numbers.util.Utils.remainderFromRoundingToLong;
import static org.dicio.numbers.util.Utils.roundToLong;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * TODO add documentation and tests
 */
public class Duration {

    private static final long SECONDS_IN_DAY = ChronoUnit.DAYS.getDuration().getSeconds();
    private static final double DAYS_IN_MONTH
            = (double) ChronoUnit.MONTHS.getDuration().getSeconds()
            / (double) ChronoUnit.DAYS.getDuration().getSeconds(); // ~30.436875
    
    private final long nanos; // basic well-defined unit of time, fits precisely up to ~10^3 hours
    private final long days; // not stored as nanos because of e.g. summer/winter time changes
    private final long months; // not stored as days because months have different numbers of days
    private final long years; // not stored as months because of e.g. historical changes


    public Duration(final long nanos, final long days, final long months, final long years) {
        this.nanos = nanos;
        this.days = days;
        this.months = months;
        this.years = years;
    }

    public Duration() {
        this(0, 0, 0, 0);
    }

    public Duration(final Duration duration) {
        this(duration.nanos, duration.days, duration.months, duration.years);
    }

    public Duration(final java.time.Duration javaDuration) {
        this(javaDuration.getNano() + (javaDuration.getSeconds() % SECONDS_IN_DAY) * 1000000000L,
                javaDuration.getSeconds() / SECONDS_IN_DAY, 0, 0);
    }


    public Duration plusNanos(final long number) {
        return new Duration(this.nanos + number, this.days, this.months, this.years);
    }

    public Duration plusDays(final long number) {
        return new Duration(this.nanos, this.days + number, this.months, this.years);
    }

    public Duration plusMonths(final long number) {
        return new Duration(this.nanos, this.days, this.months + number, this.years);
    }

    public Duration plusYears(final long number) {
        return new Duration(this.nanos, this.days, this.months, this.years + number);
    }


    public Duration plus(final long number, final ChronoUnit chronoUnit) {
        switch (chronoUnit) {
            case NANOS:      return plusNanos(number);
            case MICROS:     return plusNanos(number * 1000L);
            case MILLIS:     return plusNanos(number * 1000000L);
            case SECONDS:    return plusNanos(number * 1000000000L);
            case MINUTES:    return plusNanos(number * 60000000000L);
            case HOURS:      return plusNanos(number * 3600000000000L);
            case HALF_DAYS:  return plusNanos(number * 43200000000000L); // 12 hours

            case DAYS:       return plusDays(number);
            case WEEKS:      return plusDays(number * 7L);

            case MONTHS:     return plusMonths(number);

            case YEARS:      return plusYears(number);
            case DECADES:    return plusYears(number * 10L);
            case CENTURIES:  return plusYears(number * 100L);
            case MILLENNIA:  return plusYears(number * 1000L);
            case ERAS:       return plusYears(number * 1000000000L);

            case FOREVER:
            default:
                throw new RuntimeException("Invalid chrono unit: " + chronoUnit);
        }
    }

    public Duration plus(final double number, final ChronoUnit chronoUnit) {
        final Duration result = plus(roundToLong(number), chronoUnit);
        switch (chronoUnit) {
            case NANOS:
            case MICROS:
            case MILLIS:
            case SECONDS:
            case MINUTES:
            case HOURS:
            case HALF_DAYS:
                return result;

            case DAYS:
            case WEEKS:
                // Convert to nanos directly, assuming 1 * day = DAY.getDuration().toNanos() * ns,
                // even though that's not always the case because of e.g. summer/winter time changes
                return result.plusNanos(
                        roundToLong(
                                remainderFromRoundingToLong(number)
                                        * chronoUnit.getDuration().toNanos()
                        )
                );

            case MONTHS:
                // Convert months to days using DAYS_IN_MONTH, which was obtained using the built-in
                // ChronoUnit estimations for month and day's average durations (in seconds)
                return result.plus(remainderFromRoundingToLong(number) * DAYS_IN_MONTH,
                        ChronoUnit.DAYS);

            case YEARS:
            case DECADES:
            case CENTURIES:
            case MILLENNIA:
            case ERAS:
                // According to the built-in ChronoUnit estimations, a year is exactly 12 months
                // (even though that's not always the case because e.g. of leap years), so
                // monthsInYearUnit is surely an integer
                final long monthsInYearUnit = chronoUnit.getDuration().getSeconds()
                        / ChronoUnit.MONTHS.getDuration().getSeconds();
                return result.plus(remainderFromRoundingToLong(number) * monthsInYearUnit,
                        ChronoUnit.MONTHS);

            case FOREVER:
            default:
                throw new RuntimeException("Invalid chrono unit: " + chronoUnit);
        }
    }

    public Duration plus(final Number number, final ChronoUnit chronoUnit) {
        return number.isInteger()
                ? plus(number.integerValue(), chronoUnit)
                : plus(number.decimalValue(), chronoUnit);
    }

    public Duration plus(final Duration duration) {
        return new Duration(
                nanos + duration.nanos,
                days + duration.days,
                months + duration.months,
                years + duration.years
        );
    }


    public Duration multiply(final long number) {
        return new Duration(nanos * number, days * number, months * number, years * number);
    }

    public Duration multiply(final double number) {
        return new Duration(roundToLong(nanos * number), 0, 0, 0)
                .plus(days * number, ChronoUnit.DAYS)
                .plus(months * number, ChronoUnit.MONTHS)
                .plus(years * number, ChronoUnit.YEARS);
    }

    public Duration multiply(final Number number) {
        return number.isInteger()
                ? multiply(number.integerValue())
                : multiply(number.decimalValue());
    }


    public long getNanos() {
        return nanos;
    }

    public long getDays() {
        return days;
    }

    public long getMonths() {
        return months;
    }

    public long getYears() {
        return years;
    }


    @Override
    public String toString() {
        return toJavaDuration().toString();
    }

    public java.time.Duration toJavaDuration() {
        return java.time.Duration.ofNanos(nanos)
                .plusDays(days)
                .plus(multiplyDurationBy(months, ChronoUnit.MONTHS))
                .plus(multiplyDurationBy(years, ChronoUnit.YEARS));
    }

    public LocalDateTime applyAsOffsetToDateTime(final LocalDateTime original) {
        return original.plusNanos(nanos)
                .plusDays(days)
                .plusMonths(months)
                .plusYears(years);
    }

    private java.time.Duration multiplyDurationBy(final long number,
                                                  final ChronoUnit chronoUnit) {
        // This operation would not be doable directly with Duration.plus(), since it throws an
        // exception with units with an estimated duration
        return chronoUnit.getDuration().multipliedBy(number);
    }
}
