package org.dicio.numbers.unit;

import org.dicio.numbers.unit.Number;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NumberTest {
    @Test
    public void internalType() {
        assertTrue(new Number(0.1).isDecimal());
        assertTrue(new Number(-1).isInteger());
        assertFalse(new Number(-1.0).isInteger());
        assertFalse(new Number(423789).isDecimal());
        assertEquals(-1.72, new Number(-1.72).decimalValue(), 0.0);
        assertEquals(654, new Number(654).integerValue());
        assertEquals(Double.NaN, new Number(5).decimalValue(), 0.0);
        assertEquals(0, new Number(2.0).integerValue());
    }

    @Test
    public void plus() {
        assertEquals(new Number(423 + 536), new Number(423).plus(536));
        assertEquals(new Number(1.0 - 76), new Number(1.0).plus(new Number(-76)));
        assertEquals(new Number(3567 + 23e56), new Number(3567).plus(23e56));
        assertEquals(new Number(0.243 + 12.96), new Number(0.243).plus(new Number(12.96)));
        assertEquals(new Number(Long.MAX_VALUE + 1.0), new Number(Long.MAX_VALUE).plus(1));
    }

    @Test
    public void multiply() {
        assertEquals(new Number(423 * 536), new Number(423).multiply(536));
        assertEquals(new Number(1.0 * 76), new Number(1.0).multiply(new Number(76)));
        assertEquals(new Number(-3567 * 23e56), new Number(-3567).multiply(23e56));
        assertEquals(new Number(0.243 * 12.96), new Number(0.243).multiply(new Number(12.96)));
        assertEquals(new Number(Long.MIN_VALUE * 2.0), new Number(Long.MIN_VALUE).multiply(2));
    }

    @Test
    public void divide() {
        assertEquals(new Number(43), new Number(43 * 78).divide(new Number(78)));
        assertEquals(new Number(-43.0), new Number(43.0 * 78).divide(new Number(-78)));
        assertEquals(new Number(1.5), new Number(3).divide(new Number(2)));
    }

    @Test
    public void lessThan() {
        assertTrue(new Number(483).lessThan(1000));
        assertTrue(new Number(5833).lessThan(5834));
        assertFalse(new Number(6).lessThan(6));
        assertTrue(new Number(-654).lessThan(-653));
        assertTrue(new Number(483.2).lessThan(1000));
        assertTrue(new Number(5833.999).lessThan(5834));
        assertFalse(new Number(6.0).lessThan(6));
        assertTrue(new Number(-653.0001).lessThan(-653));
    }

    @Test
    public void moreThan() {
        assertTrue(new Number(1000).moreThan(624));
        assertTrue(new Number(9348).moreThan(9347));
        assertFalse(new Number(6).moreThan(6));
        assertTrue(new Number(-975).moreThan(-976));
        assertTrue(new Number(1000.5345).moreThan(624));
        assertTrue(new Number(9347.0001).moreThan(9347));
        assertFalse(new Number(6.0).moreThan(6));
        assertTrue(new Number(-975.999).moreThan(-976));
    }

    @Test
    public void equalityNumber() {
        assertEquals(new Number(149), new Number(149));
        assertEquals(new Number(-53), new Number(-53));
        assertEquals(new Number(2.45), new Number(2.45));
        assertNotEquals(new Number(76.4), new Number(76.5));
        assertNotEquals(new Number(21), new Number(23));
        assertNotEquals(new Number(58.0), new Number(58));
        assertNotEquals(new Number(97), new Number(97.0));
        assertNotEquals(null, new Number(-8.0));
        assertNotEquals("", new Number(-56.1));
    }

    @Test
    public void equalityInteger() {
        assertTrue(new Number(645).equals(645));
        assertTrue(new Number(-6).equals(-6));
        assertFalse(new Number(294).equals(-294));
        assertFalse(new Number(45234.254).equals(767));
        assertFalse(new Number(234.0).equals(234));
    }
}
