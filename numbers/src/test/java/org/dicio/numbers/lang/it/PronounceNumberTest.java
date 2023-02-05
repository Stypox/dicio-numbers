package org.dicio.numbers.lang.it;

import org.dicio.numbers.ParserFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.T;
import static org.junit.Assert.assertEquals;

public class PronounceNumberTest {

    private static ParserFormatter pf;

    @BeforeClass
    public static void setup() {
        pf = new ParserFormatter(new ItalianFormatter(), null);
    }

    @Test
    public void smallIntegers() {
        assertEquals("zero", pf.pronounceNumber(0).get());
        assertEquals("uno", pf.pronounceNumber(1).get());
        assertEquals("dieci", pf.pronounceNumber(10).get());
        assertEquals("quindici", pf.pronounceNumber(15).get());
        assertEquals("venti", pf.pronounceNumber(20).get());
        assertEquals("venti sette", pf.pronounceNumber(27).get());
        assertEquals("trenta", pf.pronounceNumber(30).get());
        assertEquals("trenta tre", pf.pronounceNumber(33).get());
    }

    @Test
    public void negativeSmallIntegers() {
        assertEquals("meno uno", pf.pronounceNumber(-1).get());
        assertEquals("meno dieci", pf.pronounceNumber(-10).get());
        assertEquals("meno quindici", pf.pronounceNumber(-15).get());
        assertEquals("meno venti", pf.pronounceNumber(-20).get());
        assertEquals("meno venti sette", pf.pronounceNumber(-27).get());
        assertEquals("meno trenta", pf.pronounceNumber(-30).get());
        assertEquals("meno trenta tre", pf.pronounceNumber(-33).get());
    }

    @Test
    public void decimals() {
        assertEquals("zero virgola zero cinque", pf.pronounceNumber(0.05).get());
        assertEquals("meno zero virgola zero cinque", pf.pronounceNumber(-0.05).get());
        assertEquals("uno virgola due tre", pf.pronounceNumber(1.234).get());
        assertEquals("venti uno virgola due sei quattro", pf.pronounceNumber(21.264).places(5).get());
        assertEquals("venti uno virgola due sei quattro", pf.pronounceNumber(21.264).places(4).get());
        assertEquals("venti uno virgola due sei quattro", pf.pronounceNumber(21.264).places(3).get());
        assertEquals("venti uno virgola due sei", pf.pronounceNumber(21.264).places(2).get());
        assertEquals("venti uno virgola tre", pf.pronounceNumber(21.264).places(1).get());
        assertEquals("venti uno", pf.pronounceNumber(21.264).places(0).get());
        assertEquals("meno venti uno virgola due sei quattro", pf.pronounceNumber(-21.264).places(5).get());
        assertEquals("meno venti uno virgola due sei quattro", pf.pronounceNumber(-21.264).places(4).get());
        assertEquals("meno venti uno virgola due sei quattro", pf.pronounceNumber(-21.264).places(3).get());
        assertEquals("meno venti uno virgola due sei", pf.pronounceNumber(-21.264).places(2).get());
        assertEquals("meno venti uno virgola tre", pf.pronounceNumber(-21.264).places(1).get());
        assertEquals("meno venti uno", pf.pronounceNumber(-21.264).places(0).get());
    }

    @Test
    public void roundingDecimals() {
        assertEquals("zero", pf.pronounceNumber(0.05).places(0).get());
        assertEquals("zero", pf.pronounceNumber(-0.4).places(0).get());
        assertEquals("meno venti due", pf.pronounceNumber(-21.7).places(0).get());
        assertEquals("ottanta nove", pf.pronounceNumber(89.2).places(0).get());
        assertEquals("novanta", pf.pronounceNumber(89.9).places(0).get());
        assertEquals("meno uno", pf.pronounceNumber(-0.5).places(0).get());
        assertEquals("zero", pf.pronounceNumber(-0.4).places(0).get());
        assertEquals("sei virgola tre", pf.pronounceNumber(6.28).places(1).get());
        assertEquals("meno tre virgola uno", pf.pronounceNumber(-3.14).places(1).get());
        // note: 3.15 does not yield "tre virgola due" because of floating point errors
        assertEquals("tre virgola due", pf.pronounceNumber(3.150001).places(1).get());
        assertEquals("zero virgola tre", pf.pronounceNumber(0.25).places(1).get());
        assertEquals("meno zero virgola tre", pf.pronounceNumber(-0.25).places(1).get());
        assertEquals("diciannove", pf.pronounceNumber(19.004).get());
    }

    @Test
    public void cento() {
        assertEquals("cento", pf.pronounceNumber(100).get());
        assertEquals("sei cento settanta otto", pf.pronounceNumber(678).get());

        assertEquals("cento tre milioni, due cento cinquanta quattro mila, sei cento cinquanta quattro",
                pf.pronounceNumber(103254654).get());
        assertEquals("un milione, cinque cento dodici mila, quattro cento cinquanta sette",
                pf.pronounceNumber(1512457).get());
        assertEquals("due cento nove mila, nove cento novanta sei",
                pf.pronounceNumber(209996).get());
    }

    @Test
    public void year() {
        assertEquals("mille, quattro cento cinquanta sei", pf.pronounceNumber(1456).get());
        assertEquals("mille, nove cento ottanta quattro", pf.pronounceNumber(1984).get());
        assertEquals("mille, otto cento uno", pf.pronounceNumber(1801).get());
        assertEquals("mille, cento", pf.pronounceNumber(1100).get());
        assertEquals("mille, due cento uno", pf.pronounceNumber(1201).get());
        assertEquals("mille, cinque cento dieci", pf.pronounceNumber(1510).get());
        assertEquals("mille, sei", pf.pronounceNumber(1006).get());
        assertEquals("mille", pf.pronounceNumber(1000).get());
        assertEquals("due mila", pf.pronounceNumber(2000).get());
        assertEquals("due mila, quindici", pf.pronounceNumber(2015).get());
        assertEquals("quattro mila, otto cento venti sette", pf.pronounceNumber(4827).get());
    }

    @Test
    public void scientificNotation() {
        assertEquals("zero", pf.pronounceNumber(0.0).scientific(T).get());
        assertEquals("tre virgola tre per dieci alla uno",
                pf.pronounceNumber(33).scientific(T).get());
        assertEquals("due virgola nove nove per dieci alla otto",
                pf.pronounceNumber(299492458).scientific(T).get());
        assertEquals("due virgola nove nove sette nove due cinque per dieci alla otto",
                pf.pronounceNumber(299792458).scientific(T).places(6).get());
        assertEquals("uno virgola sei sette due per dieci alla meno venti sette",
                pf.pronounceNumber(1.672e-27).scientific(T).places(3).get());

        // auto scientific notation when number is too big to be pronounced
        assertEquals("due virgola nove cinque per dieci alla venti quattro",
                pf.pronounceNumber(2.9489e24).get());
    }

    private void assertShortLongScale(final double number,
                                      final String result) {
        assertEquals(result, pf.pronounceNumber(number).shortScale(T).get());
        assertEquals(result, pf.pronounceNumber(number).shortScale(F).get());
    }

    @Test
    public void largeNumbers() {
        assertShortLongScale(1001892, "un milione, mille, otto cento novanta due");
        assertShortLongScale(299792458, "due cento novanta nove milioni, sette cento novanta due mila, quattro cento cinquanta otto");
        assertShortLongScale(-100202133440.0, "meno cento miliardi, due cento due milioni, cento trenta tre mila, quattro cento quaranta");
        assertShortLongScale(20102000987000.0, "venti bilioni, cento due miliardi, nove cento ottanta sette mila");
        assertShortLongScale(-2061000560007060.0, "meno due biliardi, sessanta uno bilioni, cinque cento sessanta milioni, sette mila, sessanta");
        // floating point errors
        assertShortLongScale(9111202032999999488.0, "nove trilioni, cento undici biliardi, due cento due bilioni, trenta due miliardi, nove cento novanta nove milioni, nove cento novanta nove mila, quattro cento ottanta otto");

        assertShortLongScale(29000.0, "venti nove mila");
        assertShortLongScale(301000.0, "tre cento uno mila");
        assertShortLongScale(4000000.0, "quattro milioni");
        assertShortLongScale(50000000.0, "cinquanta milioni");
        assertShortLongScale(630000000.0, "sei cento trenta milioni");
        assertShortLongScale(7000000000.0, "sette miliardi");
        assertShortLongScale(16000000000.0, "sedici miliardi");
        assertShortLongScale(923000000000.0, "nove cento venti tre miliardi");
        assertShortLongScale(1000000000000.0, "un bilione");
        assertShortLongScale(29000000000000.0, "venti nove bilioni");
        assertShortLongScale(308000000000000.0, "tre cento otto bilioni");
        assertShortLongScale(4000000000000000.0, "quattro biliardi");
        assertShortLongScale(52000000000000000.0, "cinquanta due biliardi");
        assertShortLongScale(640000000000000000.0, "sei cento quaranta biliardi");
        assertShortLongScale(7000000000000000000.0, "sette trilioni");

        // TODO maybe improve this
        assertShortLongScale(1000001, "un milione, uno");
        assertShortLongScale(-2000000029, "meno due miliardi, venti nove");
    }

    @Test
    public void ordinal() {
        // small numbers
        assertEquals("primo", pf.pronounceNumber(1).shortScale(T).ordinal(T).get());
        assertEquals("primo", pf.pronounceNumber(1).shortScale(F).ordinal(T).get());
        assertEquals("decimo", pf.pronounceNumber(10).shortScale(T).ordinal(T).get());
        assertEquals("decimo", pf.pronounceNumber(10).shortScale(F).ordinal(T).get());
        assertEquals("quindicesimo", pf.pronounceNumber(15).shortScale(T).ordinal(T).get());
        assertEquals("quindicesimo", pf.pronounceNumber(15).shortScale(F).ordinal(T).get());
        assertEquals("ventesimo", pf.pronounceNumber(20).shortScale(T).ordinal(T).get());
        assertEquals("ventesimo", pf.pronounceNumber(20).shortScale(F).ordinal(T).get());
        assertEquals("venti settesimo", pf.pronounceNumber(27).shortScale(T).ordinal(T).get());
        assertEquals("venti settesimo", pf.pronounceNumber(27).shortScale(F).ordinal(T).get());
        assertEquals("trentesimo", pf.pronounceNumber(30).shortScale(T).ordinal(T).get());
        assertEquals("trentesimo", pf.pronounceNumber(30).shortScale(F).ordinal(T).get());
        assertEquals("trenta treesimo", pf.pronounceNumber(33).shortScale(T).ordinal(T).get());
        assertEquals("trenta treesimo", pf.pronounceNumber(33).shortScale(F).ordinal(T).get());
        assertEquals("centesimo", pf.pronounceNumber(100).shortScale(T).ordinal(T).get());
        assertEquals("centesimo", pf.pronounceNumber(100).shortScale(F).ordinal(T).get());
        assertEquals("cento decimo", pf.pronounceNumber(110).shortScale(F).ordinal(T).get());
        assertEquals("sei cento seiesimo", pf.pronounceNumber(606).shortScale(T).ordinal(T).get());
        assertEquals("millesimo", pf.pronounceNumber(1000).shortScale(T).ordinal(T).get());
        assertEquals("millesimo", pf.pronounceNumber(1000).shortScale(F).ordinal(T).get());
        assertEquals("dieci millesimo", pf.pronounceNumber(10000).shortScale(T).ordinal(T).get());
        assertEquals("dieci millesimo", pf.pronounceNumber(10000).shortScale(F).ordinal(T).get());
        assertEquals("due centesimo", pf.pronounceNumber(200).shortScale(T).ordinal(T).get());
        assertEquals("due centesimo", pf.pronounceNumber(200).shortScale(F).ordinal(T).get());
        assertEquals("diciotto mila, sei cento novanta unesimo", pf.pronounceNumber(18691).ordinal(T).shortScale(T).get());
        assertEquals("diciotto mila, sei cento novanta unesimo", pf.pronounceNumber(18691).ordinal(T).shortScale(F).get());
        assertEquals("mille, cinque cento sessanta settesimo", pf.pronounceNumber(1567).ordinal(T).shortScale(T).get());
        assertEquals("mille, cinque cento sessanta settesimo", pf.pronounceNumber(1567).ordinal(T).shortScale(F).get());

        // big numbers
        assertEquals("milionesimo", pf.pronounceNumber(1000000).ordinal(T).get());
        assertEquals("diciotto milionesimo", pf.pronounceNumber(18000000).ordinal(T).get());
        assertEquals("diciotto milioni, centesimo", pf.pronounceNumber(18000100).ordinal(T).get());
        assertEquals("cento venti sette miliardesimo", pf.pronounceNumber(127000000000.0).ordinal(T).shortScale(T).get());
        assertEquals("due cento uno miliardesimo", pf.pronounceNumber(201000000000.0).ordinal(T).shortScale(F).get());
        assertEquals("nove cento tredici miliardi, ottanta milioni, sei cento mila, sessanta quattresimo", pf.pronounceNumber(913080600064.0).ordinal(T).shortScale(T).get());
        assertEquals("nove cento tredici miliardi, ottanta milioni, sei cento mila, sessanta quattresimo", pf.pronounceNumber(913080600064.0).ordinal(T).shortScale(F).get());
        assertEquals("un bilione, due milionesimo", pf.pronounceNumber(1000002000000.0).ordinal(T).shortScale(T).get());
        assertEquals("un bilione, due milionesimo", pf.pronounceNumber(1000002000000.0).ordinal(T).shortScale(F).get());
        assertEquals("quattro bilioni, un milionesimo", pf.pronounceNumber(4000001000000.0).ordinal(T).shortScale(T).get());
        assertEquals("quattro bilioni, un milionesimo", pf.pronounceNumber(4000001000000.0).ordinal(T).shortScale(F).get());

        // decimal numbers and scientific notation: the behaviour should be the same as with ordinal=F
        assertEquals("due virgola sette otto", pf.pronounceNumber(2.78).ordinal(T).get());
        assertEquals("terzo", pf.pronounceNumber(2.78).places(0).ordinal(T).get());
        assertEquals("diciannovesimo", pf.pronounceNumber(19.004).ordinal(T).get());
        assertEquals("otto cento trenta milioni, quattro cento trenta otto mila, novanta due virgola uno otto tre", pf.pronounceNumber(830438092.1829).places(3).ordinal(T).get());
        assertEquals("due virgola cinque quattro per dieci alla sei", pf.pronounceNumber(2.54e6).ordinal(T).scientific(T).get());
    }

    @Test
    public void edgeCases() {
        assertEquals("zero", pf.pronounceNumber(0.0).get());
        assertEquals("zero", pf.pronounceNumber(-0.0).get());
        assertEquals("infinito", pf.pronounceNumber(Double.POSITIVE_INFINITY).get());
        assertEquals("meno infinito", pf.pronounceNumber(Double.NEGATIVE_INFINITY).scientific(F).get());
        assertEquals("meno infinito", pf.pronounceNumber(Double.NEGATIVE_INFINITY).scientific(T).get());
        assertEquals("non un numero", pf.pronounceNumber(Double.NaN).get());
    }
}
