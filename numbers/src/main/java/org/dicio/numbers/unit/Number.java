package org.dicio.numbers.unit;

import java.util.Objects;

/**
 * TODO add documentation
 */
public class Number {

    private final boolean isDecimal;
    private final long integerValue;
    private final double decimalValue;
    private final boolean isOrdinal;


    public Number(final long integerValue) {
        this(integerValue, false);
    }

    public Number(final double decimalValue) {
        this(decimalValue, false);
    }

    public Number(final long integerValue, final boolean isOrdinal) {
        this(false, integerValue, Double.NaN, isOrdinal);
    }

    public Number(final double decimalValue, final boolean isOrdinal) {
        this(true, 0, decimalValue, isOrdinal);
    }

    private Number(final boolean isDecimal,
                   final long integerValue,
                   final double decimalValue,
                   final boolean isOrdinal) {
        this.isDecimal = isDecimal;
        this.integerValue = integerValue;
        this.decimalValue = decimalValue;
        this.isOrdinal = isOrdinal;
    }


    public static Number fromObject(final Object object) {
        if (object instanceof Short || object instanceof Integer || object instanceof Long) {
            return new Number(((java.lang.Number) object).longValue());
        } else if (object instanceof Float || object instanceof Double) {
            return new Number(((java.lang.Number) object).doubleValue());
        } else {
            throw new IllegalArgumentException(
                    "object is neither an integer nor a decimal number: " + object);
        }
    }


    public boolean isDecimal() {
        return isDecimal;
    }

    public boolean isInteger() {
        return !isDecimal;
    }

    public long integerValue() {
        return integerValue;
    }

    public double decimalValue() {
        return decimalValue;
    }

    public boolean isOrdinal() {
        return isOrdinal;
    }

    public Number withOrdinal(final boolean isOrdinal) {
        return new Number(this.isDecimal, this.integerValue, this.decimalValue, isOrdinal);
    }


    public Number multiply(final long integer) {
        if (isDecimal) {
            return new Number(decimalValue * integer, isOrdinal);
        } else {
            try {
                return new Number(Math.multiplyExact(integerValue, integer), isOrdinal);
            } catch (final ArithmeticException e) {
                return new Number((double) integerValue * integer, isOrdinal);
            }
        }
    }

    public Number multiply(final double decimal) {
        return new Number((isDecimal ? decimalValue : integerValue) * decimal, isOrdinal);
    }

    public Number multiply(final Number number) {
        return (number.isDecimal ? multiply(number.decimalValue) : multiply(number.integerValue));
    }

    public Number plus(final long integer) {
        if (isDecimal) {
            return new Number(decimalValue + integer, isOrdinal);
        } else {
            try {
                return new Number(Math.addExact(integerValue, integer), isOrdinal);
            } catch (final ArithmeticException e) {
                return new Number((double) integerValue + integer, isOrdinal);
            }
        }
    }

    public Number plus(final double decimal) {
        return new Number((isDecimal ? decimalValue : integerValue) + decimal, isOrdinal);
    }

    public Number plus(final Number number) {
        return number.isDecimal ? plus(number.decimalValue) : plus(number.integerValue);
    }

    public Number divide(final long integer) {
        if (isDecimal) {
            return new Number(decimalValue / integer, isOrdinal);
        } else if (integerValue % integer == 0) {
            return new Number(integerValue / integer, isOrdinal);
        } else {
            return new Number(((double) integerValue) / integer, isOrdinal);
        }
    }

    public Number divide(final double decimal) {
        return new Number((isDecimal ? decimalValue : integerValue) / decimal, isOrdinal);
    }

    public Number divide(final Number number) {
        return number.isDecimal ? divide(number.decimalValue) : divide(number.integerValue);
    }

    public boolean lessThan(final long integer) {
        return isDecimal ? (decimalValue < integer) : (integerValue < integer);
    }

    public boolean lessThan(final double decimal) {
        return isDecimal ? (decimalValue < decimal) : (integerValue < decimal);
    }

    public boolean moreThan(final long integer) {
        return isDecimal ? (decimalValue > integer) : (integerValue > integer);
    }


    public boolean equals(final long integer) {
        return !isDecimal && integerValue == integer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final Number number = (Number) o;
            return isDecimal == number.isDecimal && isOrdinal == number.isOrdinal && (isDecimal
                    ? decimalValue == number.decimalValue : integerValue == number.integerValue);
        }
    }

    @Override
    public int hashCode() {
        return isDecimal ? Objects.hash(true, decimalValue) : Objects.hash(false, integerValue);
    }

    @Override
    public String toString() {
        return (isDecimal ? String.valueOf(decimalValue) : String.valueOf(integerValue))
                + (isOrdinal ? "th" : "");
    }
}
