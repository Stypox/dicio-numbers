package org.dicio.numbers.unit;

import org.dicio.numbers.util.Utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * TODO add documentation and tests
 */
public class Duration {
    
    private Number nanos; // fits ~10^6 hours as a long before turning into a double
    private Number days;
    private Number months;
    private Number years;


    public Duration(final Duration duration) {
        this(duration.nanos, duration.days, duration.months, duration.years);
    }

    public Duration() {
        this.nanos = new Number(0);
        this.days = new Number(0);
        this.months = new Number(0);
        this.years = new Number(0);
    }

    public Duration(final java.time.Duration javaDuration) {
        final long seconds = javaDuration.getSeconds();
        final long secondsInADay = ChronoUnit.DAYS.getDuration().getSeconds();
        this.nanos = new Number(
                javaDuration.getNano() + (seconds % secondsInADay) * 1000000000L);
        this.days = new Number(seconds / secondsInADay);
        this.months = new Number(0);
        this.years = new Number(0);
    }

    private Duration(final Number nanos,
                     final Number days,
                     final Number months,
                     final Number years) {
        this.nanos = new Number(nanos);
        this.days = new Number(days);
        this.months = new Number(months);
        this.years = new Number(years);
    }

    
    public Duration plus(final Number number, final ChronoUnit chronoUnit) {
        final Duration result = new Duration(this);
        switch (chronoUnit) {
            case NANOS:
                result.nanos = result.nanos.plus(number);
                break;
            case MICROS:
                result.nanos = result.nanos.plus(number.multiply(1000L));
                break;
            case MILLIS:
                result.nanos = result.nanos.plus(number.multiply(1000000L));
                break;
            case SECONDS:
                result.nanos = result.nanos.plus(number.multiply(1000000000L));
                break;
            case MINUTES:
                result.nanos = result.nanos.plus(number.multiply(60000000000L));
                break;
            case HOURS:
                result.nanos = result.nanos.plus(number.multiply(3600000000000L));
                break;
            case HALF_DAYS:
                result.days = result.days.plus(number.multiply(0.5));
                break;
            case DAYS:
                result.days = result.days.plus(number);
                break;
            case WEEKS:
                result.days = result.days.plus(number.multiply(7L));
                break;
            case MONTHS:
                result.months = result.months.plus(number);
                break;
            case YEARS:
                result.years = result.years.plus(number);
                break;
            case DECADES:
                result.years = result.years.plus(number.multiply(10L));
                break;
            case CENTURIES:
                result.years = result.years.plus(number.multiply(100L));
                break;
            case MILLENNIA:
                result.years = result.years.plus(number.multiply(1000L));
                break;
            case ERAS:
                result.years = result.years.plus(number.multiply(1000000000L));
                break;
            case FOREVER:
                throw new RuntimeException("FOREVER is not a valid temporal unit");
        }
        return result;
    }

    public Duration plus(final Duration duration) {
        return new Duration(
                nanos.plus(duration.nanos),
                days.plus(duration.days),
                months.plus(duration.months),
                years.plus(duration.years)
        );
    }

    public Duration multiply(final Number number) {
        return new Duration(
                nanos.multiply(number),
                days.multiply(number),
                months.multiply(number),
                years.multiply(number)
        );
    }


    public Number getNanos() {
        return nanos;
    }

    public Number getDays() {
        return days;
    }

    public Number getMonths() {
        return months;
    }

    public Number getYears() {
        return years;
    }


    @Override
    public String toString() {
        return toJavaDuration().toString();
    }

    public java.time.Duration toJavaDuration() {
        return multiplyDurationBy(ChronoUnit.NANOS, nanos)
                .plus(multiplyDurationBy(ChronoUnit.DAYS, days))
                .plus(multiplyDurationBy(ChronoUnit.MONTHS, months))
                .plus(multiplyDurationBy(ChronoUnit.YEARS, years));
    }

    public LocalDateTime applyAsOffsetToDateTime(final LocalDateTime original) {
        final long yearsFloor = floorNumber(years);

        final Number actualMonths = adjustTimeByRemainder(
                months, ChronoUnit.MONTHS, years, yearsFloor, ChronoUnit.YEARS);
        final long monthsFloor = floorNumber(actualMonths);

        final Number actualDays = adjustTimeByRemainder(
                days, ChronoUnit.DAYS, actualMonths, monthsFloor, ChronoUnit.MONTHS);
        final long daysFloor = floorNumber(actualDays);

        Number actualNanos = nanos;
        if (!actualDays.isInteger()) {
            final double daysRemainder = actualDays.decimalValue() - daysFloor;
            if (daysRemainder != 0.0) {
                actualNanos = nanos.plus(daysRemainder
                        * ChronoUnit.DAYS.getDuration().getSeconds() * 1000000000L); // s to ns
            }
        }
        final long nanosRound = actualNanos.isInteger()
                ? actualNanos.integerValue() : Utils.roundToLong(actualNanos.decimalValue());

        return original.plusNanos(nanosRound)
                .plusDays(daysFloor)
                .plusMonths(monthsFloor)
                .plusYears(yearsFloor);
    }

    private long floorNumber(final Number number) {
        return (number.isInteger() ? number.integerValue() : (long) number.decimalValue());
    }

    private Number adjustTimeByRemainder(final Number timeB,
                                         final ChronoUnit unitB,
                                         final Number timeA,
                                         final long timeAFloor,
                                         final ChronoUnit unitA) {
        if (timeA.isInteger()) {
            return timeB;
        }

        final double timeARemainder = timeA.decimalValue() - timeAFloor;
        if (timeARemainder == 0.0) {
            // do not make calculations to preserve timeB's integer-ness
            return timeB;
        }

        // assumes unitA and unitB only have the "seconds" field set
        return timeB.plus(timeARemainder // <- the remainder
                * ((double) unitA.getDuration().getSeconds()
                / unitB.getDuration().getSeconds()));
    }


    private java.time.Duration multiplyDurationBy(final ChronoUnit chronoUnit,
                                                  final Number number) {
        if (number.isInteger()) {
            return chronoUnit.getDuration().multipliedBy(number.integerValue());
        } else {
            final BigDecimal allNanos = BigDecimal.valueOf(chronoUnit.getDuration().getNano())
                    .add(BigDecimal.valueOf(chronoUnit.getDuration().getSeconds(), -9))
                    .multiply(BigDecimal.valueOf(number.decimalValue()));
            final long seconds = allNanos.multiply(BigDecimal.valueOf(1, 9)).longValue();
            final long nanos = allNanos.subtract(BigDecimal.valueOf(seconds, -9)).longValue();

            return java.time.Duration.ofSeconds(seconds, nanos);
        }
    }
}
