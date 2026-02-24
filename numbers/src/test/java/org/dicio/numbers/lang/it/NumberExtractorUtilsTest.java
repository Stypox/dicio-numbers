package org.dicio.numbers.lang.it;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;

import org.dicio.numbers.test.NumberExtractorUtilsTestBase;
import org.junit.Test;

public class NumberExtractorUtilsTest extends NumberExtractorUtilsTestBase {

    @Override
    public String configFolder() {
        return "config/it-it";
    }


    @Test
    public void testNumberLessThan1000() {
        assertNumberLessThan1000("zero",                T, 0,   F, 1);
        assertNumberLessThan1000("uno",                 F, 1,   F, 1);
        assertNumberLessThan1000("un",                  F, 1,   F, 1);
        assertNumberLessThan1000("cinque",              T, 5,   F, 1);
        assertNumberLessThan1000("diciannove",          F, 19,  F, 1);
        assertNumberLessThan1000("cento",               T, 100, F, 1);
        assertNumberLessThan1000("tre cento",           T, 300, F, 2);
        assertNumberLessThan1000("venti sei",           F, 26,  F, 2);
        assertNumberLessThan1000("trenta sette",        T, 37,  F, 2);
        assertNumberLessThan1000("sette cento sei",     F, 706, F, 3);
        assertNumberLessThan1000("otto cento diciotto", T, 818, F, 3);
    }

    @Test
    public void testNumberLessThan1000Digits() {
        assertNumberLessThan1000("0",              F, 0,   F, 1);
        assertNumberLessThan1000("1",              T, 1,   F, 1);
        assertNumberLessThan1000("6",              F, 6,   F, 1);
        assertNumberLessThan1000("15",             T, 15,  F, 1);
        assertNumberLessThan1000("100 diciannove", F, 100, F, 1);
        assertNumberLessThan1000("3 cento 8",      T, 308, F, 3);
        assertNumberLessThan1000("72",             F, 72,  F, 1);
        assertNumberLessThan1000("912",            T, 912, F, 1);
        assertNumberLessThan1000("8 cento e 18",   F, 818, F, 4);
        assertNumberLessThan1000("7 cento 3 9",    T, 703, F, 3);
        assertNumberLessThan1000("cento 4 7",      F, 104, F, 2);
        assertNumberLessThan1000("19 cento",       T, 19,  F, 1);
        assertNumberLessThan1000("sessanta 7",     F, 67,  F, 2);
        assertNumberLessThan1000("30 6",           T, 30,  F, 1);
    }

    @Test
    public void testNumberLessThan1000EdgeCases() {
        assertNumberLessThan1000("quattro cinque",           T, 4,   F, 1);
        assertNumberLessThan1000("un due e",                 F, 1,   F, 1);
        assertNumberLessThan1000("uno tredici",              T, 1,   F, 1);
        assertNumberLessThan1000("sedici otto",              F, 16,  F, 1);
        assertNumberLessThan1000("diciotto cento",           T, 18,  F, 1);
        assertNumberLessThan1000("zero cento",               F, 0,   F, 1);
        assertNumberLessThan1000("sessanta zero",            T, 60,  F, 1);
        assertNumberLessThan1000("un cento",                 F, 100, F, 2);
        assertNumberLessThan1000("uno e cento",              T, 100, F, 3);
        assertNumberLessThan1000("sette cento e sei",        F, 706, F, 4);
        assertNumberLessThan1000("cento novanta uno",        T, 191, F, 3);
        assertNumberLessThan1000("otto e cento e quindici",  F, 815, F, 5);
        assertNumberLessThan1000("cento e e e undici e e e", T, 111, F, 5);
    }

    @Test
    public void testNumberLessThan1000Ordinal() {
        assertNumberLessThan1000("quinto",                T, 5,   T, 1);
        assertNumberLessThan1000("venti seiesimo",        T, 26,  T, 2);
        assertNumberLessThan1000("settanta ottavo",       F, 70,  F, 1);
        assertNumberLessThan1000("cinquantesimo nono",    T, 50,  T, 1);
        assertNumberLessThan1000("cento tredicesimo",     T, 113, T, 2);
        assertNumberLessThan1000("primo cento",           T, 1,   T, 1);
        assertNumberLessThan1000("sette centesimo dieci", T, 700, T, 2);
        assertNumberLessThan1000("nove centesimo",        F, 9,   F, 1);
        assertNumberLessThan1000("620ª",                  T, 620, T, 2);
        assertNumberLessThan1000("987esime",              T, 987, T, 2);
        assertNumberLessThan1000("23°",                   T, 23,  T, 2);
        assertNumberLessThan1000("8 primo",               T, 8,   F, 1);
        assertNumberLessThan1000("1ª cento",              T, 1,   T, 2);
        assertNumberLessThan1000Null("settantesima", F);
        assertNumberLessThan1000Null("101°",         F);
    }

    @Test
    public void testNumberLessThan1000Null() {
        assertNumberLessThan1000Null("",               F);
        assertNumberLessThan1000Null("ciao",           T);
        assertNumberLessThan1000Null("ciao come stai", F);
        assertNumberLessThan1000Null("ciao due e",     T);
        assertNumberLessThan1000Null("milione",        T);
        assertNumberLessThan1000Null(" venti",         F);
    }

    // in italian there is no short/long scale difference

    @Test
    public void testNumberGroup() {
        assertNumberGroupShortScale("cento venti milioni",   F, 1000000000, 120000000, F, 3);
        assertNumberGroupShortScale("mille e sei",           T, 1000000000, 1000,      F, 1);
        assertNumberGroupShortScale("sei cento mila",        F, 1000000,    600000,    F, 3);
        assertNumberGroupShortScale("cento 70 migliaia",     T, 1000000,    170000,    F, 3);
        assertNumberGroupShortScale("572 milioni",           F, 1000000000, 572000000, F, 2);
        assertNumberGroupShortScale("1 milione",             T, 1000000000, 1000000,   F, 2);
        assertNumberGroupShortScale("cento e novanta uno, ", F, 1000,       191,       F, 4);
    }

    @Test
    public void testNumberGroupOrdinal() {
        assertNumberGroupShortScale("sette cento sessanta quattro milionesimo", T, 1000000000, 764000000, T, 5);
        assertNumberGroupShortScale("sette cento sessanta quattro milionesimo", F, 1000000000, 764,       F, 4);
        assertNumberGroupShortScale("sette cento sessanta quattro milionesimo", F, 1000,       764,       F, 4);
        assertNumberGroupShortScale("quinto miliardesimo",                      T, 1000000000, 5,         T, 1);
        assertNumberGroupShortScale("diciannove centesimo",                     T, 1000000000, 19,        F, 1);
        assertNumberGroupShortScaleNull("sette cento sessanta quattro milionesimo", T, 1000);
        assertNumberGroupShortScaleNull("dodicesimo millesimo",                     F, 1000000000);
    }

    @Test
    public void testNumberGroupNull() {
        assertNumberGroupShortScaleNull("",                        T, 1000000000);
        assertNumberGroupShortScaleNull("ciao",                    F, 1000000);
        assertNumberGroupShortScaleNull("ciao come stai",          T, 1000);
        assertNumberGroupShortScaleNull("129000",                  F, 1000000000);
        assertNumberGroupShortScaleNull("5000000",                 T, 1000000000);
        assertNumberGroupShortScaleNull("cento sei",               F, 999);
        assertNumberGroupShortScaleNull("dodici",                  T, 0);
        assertNumberGroupShortScaleNull("sette miliardi",          F, 1000);
        assertNumberGroupShortScaleNull("nove mila uno",           T, 1000);
        assertNumberGroupShortScaleNull("otto milioni di persone", F, 1000000);
        assertNumberGroupShortScaleNull(" dieci ",                 T, 1000000);
        assertNumberGroupShortScaleNull("e dieci",                 F, 1000000);
        assertNumberGroupShortScaleNull("e milleseicento novanta", T, 1000000000);
    }
}
