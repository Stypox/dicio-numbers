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
    
    @Test
    public void testNumberLessThan1000() {
        assertNumberLessThan1000("cero",                   T, 0,   F, 1);
        assertNumberLessThan1000("uno",                    F, 1,   F, 1);
        assertNumberLessThan1000("cinco",                  T, 5,   F, 1);
        assertNumberLessThan1000("diecinueve",             F, 19,  F, 1);
        assertNumberLessThan1000("cien",                   T, 100, F, 1);
        assertNumberLessThan1000("un ciento",              F, 100, F, 2);
        assertNumberLessThan1000("trescientos",            T, 300, F, 2);
        assertNumberLessThan1000("veintiséis",             F, 26,  F, 2);
        assertNumberLessThan1000("treinta y siete",        T, 37,  F, 3);
        assertNumberLessThan1000("setecientos seis",       F, 706, F, 3);
        assertNumberLessThan1000("ochocientos dieciocho",  T, 818, F, 3);
    }

    @Test
    public void testNumberLessThan1000Digits() {
        assertNumberLessThan1000("0",                F, 0,   F, 1);
        assertNumberLessThan1000("1",                T, 1,   F, 1);
        assertNumberLessThan1000("6",                F, 6,   F, 1);
        assertNumberLessThan1000("15",               T, 15,  F, 1);
        assertNumberLessThan1000("100 diecinueve",   F, 100, F, 1);
        assertNumberLessThan1000("3 cientos 8",      T, 308, F, 3);
        assertNumberLessThan1000("72",               F, 72,  F, 1);
        assertNumberLessThan1000("912",              T, 912, F, 1);
        assertNumberLessThan1000("8 cientos y 18",   F, 818, F, 4);
        assertNumberLessThan1000("7 cientos 3 9",    T, 703, F, 3);
        assertNumberLessThan1000("ciento 4 7",       F, 104, F, 2);
        assertNumberLessThan1000("19 ciento",        T, 19,  F, 1);
        assertNumberLessThan1000("sesenta 7",        F, 67,  F, 2);
        assertNumberLessThan1000("30 6",             T, 30,  F, 1);
    }

    @Test
    public void testNumberLessThan1000EdgeCases() {
        assertNumberLessThan1000("cuatro cinco",                       T, 4,   F, 1);
        assertNumberLessThan1000("dos y",                              F, 2,   F, 2);
        assertNumberLessThan1000("uno trece",                          T, 1,   F, 1);
        assertNumberLessThan1000("dieciséis ocho",                     F, 16,  F, 1);
        assertNumberLessThan1000("mil ochocientos",                    T, 18,  F, 1);
        assertNumberLessThan1000("cero cien",                          F, 0,   F, 1);
        assertNumberLessThan1000("sesenta cero",                       T, 60,  F, 1);
        assertNumberLessThan1000("cien",                               F, 100, F, 2);
        assertNumberLessThan1000("uno, y un ciento",                   T, 100, F, 5);
        assertNumberLessThan1000("setecientos seis",                   F, 706, F, 4);
        assertNumberLessThan1000("ciento noventa y uno",               T, 191, F, 5);
        assertNumberLessThan1000("ocho y ciento quince",               F, 815, F, 6);
        assertNumberLessThan1000("uno ciento once",                    T, 111, F, 9);
    }

    @Test
    public void testNumberLessThan1000Ordinal() {
        assertNumberLessThan1000("quinto",                     T, 5,          T, 1);
        assertNumberLessThan1000("vigésimo sexto",             T, 26,         T, 2);
        assertNumberLessThan1000("septuagésimo octavo",        F, 70,         F, 1);
        assertNumberLessThan1000("quincuagésimo octavo",       T, 50,         T, 1);
        assertNumberLessThan1000("centésimo decimotercero",    T, 113,        T, 4);
        assertNumberLessThan1000("primer centenar",            T, 1,          T, 1);
        assertNumberLessThan1000("septuagésimo diez",          T, 700,        T, 2);
        assertNumberLessThan1000("nueve centésimo",            F, 9,          F, 1);
        assertNumberLessThan1000("23 va",                      T, 23,         T, 2);
        assertNumberLessThan1000("620va",                      T, 620,        T, 2);
        assertNumberLessThan1000("6va",                        T, 6,          T, 2);
        assertNumberLessThan1000("8 primero",                  T, 8,          F, 1);
        assertNumberLessThan1000("1er ciento",                 T, 1,          T, 2);
        assertNumberLessThan1000Null("séptimo", F);
        assertNumberLessThan1000Null("96va",    F);
    }

    @Test
    public void testNumberLessThan1000Null() {
        assertNumberLessThan1000Null("",                  F);
        assertNumberLessThan1000Null("hola",              T);
        assertNumberLessThan1000Null("hola como estas",   F);
        assertNumberLessThan1000Null("un hola dos y",     T);
        assertNumberLessThan1000Null("un coche y medio,", F);
        assertNumberLessThan1000Null("un millón",         T);
        assertNumberLessThan1000Null(" veinte",           F);
    }

    @Test
    public void testNumberGroupShortScale() {
        assertNumberGroupShortScale("ciento veinte millones",         F, 1000000000, 120000000, F, 5);
        assertNumberGroupShortScale("tres mil seis",                  T, 1000000000, 3000,      F, 2);
        assertNumberGroupShortScale("un cien mil",                    F, 1000000,    100000,    F, 3);
        assertNumberGroupShortScale("ciento 70 mil",                  T, 1000000,    170000,    F, 3);
        assertNumberGroupShortScale("572 millones",                   F, 1000000000, 572000000, F, 2);
        assertNumberGroupShortScale("3 millones",                     T, 1000000000, 3000000,   F, 2);
        assertNumberGroupShortScale(", ciento noventa y uno",         F, 1000,       191,       F, 6);
    }

    @Test
    public void testNumberGroupShortScaleOrdinal() {
        assertNumberGroupShortScale("setecientos sesenta y cuatro millonésimas", T, 1000000000, 764000000, T, 6);
        assertNumberGroupShortScale("setecientos sesenta y cuatro millonésimas", F, 1000000000, 764,       F, 5);
        assertNumberGroupShortScale("setecientos sesenta y cuatro millonésimas", F, 1000,       764,       F, 5);
        assertNumberGroupShortScale("quinto milmillonésimo",                     T, 1000000000, 5,         T, 1);
        assertNumberGroupShortScale("mil novecientos",                           T, 1000000000, 19,        F, 1);
        assertNumberGroupShortScaleNull("setecientos sesenta y cuatro millones", T, 1000);
        assertNumberGroupShortScaleNull("duodécimo milésimo",                    F, 1000000000);
    }

    @Test
    public void testNumberGroupShortScaleNull() {
        assertNumberGroupShortScaleNull("",                      T, 1000000000);
        assertNumberGroupShortScaleNull("hola",                  F, 1000000);
        assertNumberGroupShortScaleNull("hola cómo estás",       T, 1000);
        assertNumberGroupShortScaleNull("129000",                F, 1000000000);
        assertNumberGroupShortScaleNull("5000000",               T, 1000000000);
        assertNumberGroupShortScaleNull("un ciento seis",        F, 999);
        assertNumberGroupShortScaleNull("doce",                  T, 0);
        assertNumberGroupShortScaleNull("site mil millones",     F, 1000);
        assertNumberGroupShortScaleNull("nueve mil uno",         T, 1000);
        assertNumberGroupShortScaleNull("ocho millones de personas",  F, 1000000);
        assertNumberGroupShortScaleNull(" diez ",                 T, 1000000);
    }
}
