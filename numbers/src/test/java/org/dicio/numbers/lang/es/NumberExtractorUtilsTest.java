package org.dicio.numbers.lang.es;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;

import org.dicio.numbers.test.NumberExtractorUtilsTestBase;
import org.junit.Test;

public class NumberExtractorUtilsTest extends NumberExtractorUtilsTestBase {

    @Override
    public String configFolder() {
        return "config/es-es";
    }
    
    //TODO Spanish translation

    @Test
    public void testNumberLessThan1000() {
        assertNumberLessThan1000("zero",                   T, 0,   F, 1);
        assertNumberLessThan1000("one",                    F, 1,   F, 1);
        assertNumberLessThan1000("five",                   T, 5,   F, 1);
        assertNumberLessThan1000("nineteen",               F, 19,  F, 1);
        assertNumberLessThan1000("hundred",                T, 100, F, 1);
        assertNumberLessThan1000("one hundred",            F, 100, F, 2);
        assertNumberLessThan1000("three hundred",          T, 300, F, 2);
        assertNumberLessThan1000("twenty six",             F, 26,  F, 2);
        assertNumberLessThan1000("thirty-seven",           T, 37,  F, 3);
        assertNumberLessThan1000("seven hundred six",      F, 706, F, 3);
        assertNumberLessThan1000("eight hundred eighteen", T, 818, F, 3);
    }

    @Test
    public void testNumberLessThan1000Digits() {
        assertNumberLessThan1000("0",                F, 0,   F, 1);
        assertNumberLessThan1000("1",                T, 1,   F, 1);
        assertNumberLessThan1000("6",                F, 6,   F, 1);
        assertNumberLessThan1000("15",               T, 15,  F, 1);
        assertNumberLessThan1000("100 nineteen",     F, 100, F, 1);
        assertNumberLessThan1000("3 hundred 8",      T, 308, F, 3);
        assertNumberLessThan1000("72",               F, 72,  F, 1);
        assertNumberLessThan1000("912",              T, 912, F, 1);
        assertNumberLessThan1000("8 hundred and 18", F, 818, F, 4);
        assertNumberLessThan1000("7 hundred 3 9",    T, 703, F, 3);
        assertNumberLessThan1000("hundred 4 7",      F, 104, F, 2);
        assertNumberLessThan1000("19 hundred",       T, 19,  F, 1);
        assertNumberLessThan1000("sixty 7",          F, 67,  F, 2);
        assertNumberLessThan1000("30 6",             T, 30,  F, 1);
    }

    @Test
    public void testNumberLessThan1000EdgeCases() {
        assertNumberLessThan1000("four five",                          T, 4,   F, 1);
        assertNumberLessThan1000("a two and",                          F, 2,   F, 2);
        assertNumberLessThan1000("one thirteen",                       T, 1,   F, 1);
        assertNumberLessThan1000("sixteen eight",                      F, 16,  F, 1);
        assertNumberLessThan1000("eighteen hundred",                   T, 18,  F, 1);
        assertNumberLessThan1000("zero hundred",                       F, 0,   F, 1);
        assertNumberLessThan1000("sixty nought",                       T, 60,  F, 1);
        assertNumberLessThan1000("a hundred",                          F, 100, F, 2);
        assertNumberLessThan1000("one, and a hundred",                 T, 100, F, 5);
        assertNumberLessThan1000("seven hundred and six",              F, 706, F, 4);
        assertNumberLessThan1000("one hundred and ninety one",         T, 191, F, 5);
        assertNumberLessThan1000("eight and a hundred and fifteen",    F, 815, F, 6);
        assertNumberLessThan1000("a a one a a hundred a a eleven a a", T, 111, F, 9);
    }

    @Test
    public void testNumberLessThan1000Ordinal() {
        assertNumberLessThan1000("fifth",                      T, 5,          T, 1);
        assertNumberLessThan1000("twenty sixth",               T, 26,         T, 2);
        assertNumberLessThan1000("seventy eighth",             F, 70,         F, 1);
        assertNumberLessThan1000("fiftieth eighth",            T, 50,         T, 1);
        assertNumberLessThan1000("one hundred and thirteenth", T, 113,        T, 4);
        assertNumberLessThan1000("first hundred",              T, 1,          T, 1);
        assertNumberLessThan1000("seven hundredth ten",        T, 700,        T, 2);
        assertNumberLessThan1000("nine hundredth",             F, 9,          F, 1);
        assertNumberLessThan1000("23 th",                      T, 23,         T, 2);
        assertNumberLessThan1000("620nd",                      T, 620,        T, 2);
        assertNumberLessThan1000("6st",                        T, 6,          T, 2);
        assertNumberLessThan1000("8 first",                    T, 8,          F, 1);
        assertNumberLessThan1000("1st hundred",                T, 1,          T, 2);
        assertNumberLessThan1000Null("seventh", F);
        assertNumberLessThan1000Null("96th",    F);
    }

    @Test
    public void testNumberLessThan1000Null() {
        assertNumberLessThan1000Null("",                  F);
        assertNumberLessThan1000Null("hello",             T);
        assertNumberLessThan1000Null("hello how are you", F);
        assertNumberLessThan1000Null("a hello two and",   T);
        assertNumberLessThan1000Null("a car and a half,", F);
        assertNumberLessThan1000Null("a million",         T);
        assertNumberLessThan1000Null(" twenty",           F);
    }

    @Test
    public void testNumberGroupShortScale() {
        assertNumberGroupShortScale("one hundred and twenty million", F, 1000000000, 120000000, F, 5);
        assertNumberGroupShortScale("three thousand and six",         T, 1000000000, 3000,      F, 2);
        assertNumberGroupShortScale("a hundred thousand",             F, 1000000,    100000,    F, 3);
        assertNumberGroupShortScale("hundred 70 thousand",            T, 1000000,    170000,    F, 3);
        assertNumberGroupShortScale("572 million",                    F, 1000000000, 572000000, F, 2);
        assertNumberGroupShortScale("3 million",                      T, 1000000000, 3000000,   F, 2);
        assertNumberGroupShortScale(", one hundred and ninety one",   F, 1000,       191,       F, 6);
    }

    @Test
    public void testNumberGroupShortScaleOrdinal() {
        assertNumberGroupShortScale("seven hundred and sixty four millionth", T, 1000000000, 764000000, T, 6);
        assertNumberGroupShortScale("seven hundred and sixty four millionth", F, 1000000000, 764,       F, 5);
        assertNumberGroupShortScale("seven hundred and sixty four millionth", F, 1000,       764,       F, 5);
        assertNumberGroupShortScale("fifth billionth",                        T, 1000000000, 5,         T, 1);
        assertNumberGroupShortScale("nineteen hundredth",                     T, 1000000000, 19,        F, 1);
        assertNumberGroupShortScaleNull("seven hundred and sixty four millionth", T, 1000);
        assertNumberGroupShortScaleNull("twelfth thousandth",                     F, 1000000000);
    }

    @Test
    public void testNumberGroupShortScaleNull() {
        assertNumberGroupShortScaleNull("",                      T, 1000000000);
        assertNumberGroupShortScaleNull("hello",                 F, 1000000);
        assertNumberGroupShortScaleNull("hello how are you",     T, 1000);
        assertNumberGroupShortScaleNull("129000",                F, 1000000000);
        assertNumberGroupShortScaleNull("5000000",               T, 1000000000);
        assertNumberGroupShortScaleNull("one hundred and six",   F, 999);
        assertNumberGroupShortScaleNull("twelve",                T, 0);
        assertNumberGroupShortScaleNull("seven billion",         F, 1000);
        assertNumberGroupShortScaleNull("nine thousand and one", T, 1000);
        assertNumberGroupShortScaleNull("eight million people",  F, 1000000);
        assertNumberGroupShortScaleNull(" ten ",                 T, 1000000);
    }
}
