package org.dicio.numbers.lang.it;

import static org.dicio.numbers.test.TestUtils.DAY;
import static org.dicio.numbers.test.TestUtils.HOUR;
import static org.dicio.numbers.test.TestUtils.MICROS;
import static org.dicio.numbers.test.TestUtils.MILLIS;
import static org.dicio.numbers.test.TestUtils.MINUTE;
import static org.dicio.numbers.test.TestUtils.MONTH;
import static org.dicio.numbers.test.TestUtils.WEEK;
import static org.dicio.numbers.test.TestUtils.YEAR;
import static org.dicio.numbers.test.TestUtils.t;
import static org.junit.Assert.assertTrue;

import org.dicio.numbers.ParserFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.DurationExtractorUtilsTestBase;
import org.dicio.numbers.unit.Duration;
import org.dicio.numbers.util.DurationExtractorUtils;
import org.junit.Test;

/**
 * TODO also test extractDurationAtCurrentPosition
 */
public class DurationExtractorUtilsTest extends DurationExtractorUtilsTestBase {

    @Override
    public String configFolder() {
        return "config/it-it";
    }

    @Override
    public Duration extractDuration(final TokenStream ts, final boolean shortScale) {
        final ItalianNumberExtractor numberExtractor = new ItalianNumberExtractor(ts);
        return new DurationExtractorUtils(ts, numberExtractor::numberNoOrdinal).duration();
    }

    private void assertDuration(final String s, final java.time.Duration duration) {
        assertDuration(s, true, duration); // short scale is unused in italian
    }

    private void assertNoDuration(final String s) {
        assertNoDuration(s, true); // short scale is unused in italian
    }


    @Test
    public void testDurationNumberAndUnit() {
        assertDuration("un miliardo nanosecondi",      t(1));
        assertDuration("mille settecentoventotto μs",  t(0, 1728 * MICROS));
        assertDuration("un decimo millisecondo",       t(0, 100 * MICROS));
        assertDuration("18s",                          t(18));
        assertDuration("un sec",                       t(1));
        assertDuration("59 minuti s",                  t(59 * MINUTE));
        assertDuration("venti tre ore",                t(23 * HOUR));
        assertDuration("mezz'ora",                     t(HOUR / 2));
        assertDuration("uno virgola due giorni",       t(1.2 * DAY));
        assertDuration("metà giorno",                  t(DAY / 2));
        assertDuration("quattro giornate",             t(4 * DAY));
        assertDuration("dieci e settimane e",          t(10 * WEEK));
        assertDuration("6 mese",                       t(6 * MONTH));
        assertDuration("tre miliardi anni fa",         t(3e9 * YEAR));
        assertDuration("quindici decenni",             t(150 * YEAR));
        assertDuration("un miliardesimo secolo",       t(1e-9 * 100 * YEAR));
        assertDuration("1 millennio",                  t(1000 * YEAR));
        assertNoDuration("quattro tre millenni quattro");
        assertNoDuration("dammi un sec");
        assertNoDuration("e dieci e settimane e");
        assertNoDuration("cento tests");
        assertNoDuration("virgola tre quattro grammi");
    }

    @Test
    public void testDurationOnlyUnit() {
        assertDuration("ora minuto millennio",                      t(1000 * YEAR + HOUR + MINUTE));
        assertDuration("millisecondo e secondo, microsecondo", t(1, MILLIS + MICROS));
        assertDuration("secondi secondo s",                         t(2));
        assertDuration("minuto ore ms",                             t(MINUTE + HOUR));
        assertNoDuration("ciao millisecondo");
        assertNoDuration("la lettera h");
        assertNoDuration("ns μs ms s m h");
    }

    @Test
    public void testDurationOf() {
        assertDuration("un miliardo di nanosecondi",    t(1));
        assertDuration("tre ventesimi di millisecondo", t(0, 150 * MICROS));
        assertDuration("due decimi di secondo",         t(0, 200 * MILLIS));
        assertDuration("un paio d'ore",                 t(2 * HOUR));
        assertDuration("tre miliardi anni fa",          t(3e9 * YEAR));
        assertDuration("un miliardesimo di secolo",     t(1e-9 * 100 * YEAR));
        assertNoDuration("tanti secondi");
        assertNoDuration("decine di linee di test");
        assertNoDuration("ciao duecento di ciao");
        assertNoDuration("ciao di s");
    }

    @Test
    public void testMultipleDurationGroups() {
        assertDuration("venti minuti e trentasei e secondi perchè", t(20 * MINUTE + 36));
        assertDuration("sette giorni, 21 ore e dodici minuti per raggiungerti", t(7 * DAY + 21 * HOUR + 12 * MINUTE));
        assertDuration("minuto, secondi e millisecondo, microsecondi nanosecondo test", t(MINUTE + 1, MILLIS + MICROS + 1));
        assertDuration("5 ns ns", t(0, 5));
        assertNoDuration("ms 5 ns ns");
    }

    @Test(timeout = 4000) // 1024 formats + parses take <2s, use 4s timeout just for slower PCs
    public void testPerformanceWithFormatter() {
        // TODO there are no fractions of second here since the formatter does not support them
        final java.time.Duration[] alternatives = {
                t(1), t(5 * MINUTE), t(2 * HOUR), t(16 * DAY), t(WEEK), t(3 * MONTH), t(5 * YEAR),
                t(1e8 * YEAR), t(17 * WEEK), t(45)
        };

        final ParserFormatter npf = new ParserFormatter(new ItalianFormatter(), null);
        for (int i = 0; i < (1 << alternatives.length); ++i) {
            java.time.Duration durationToTest = java.time.Duration.ZERO;
            for (int j = 0; j < alternatives.length; ++j) {
                if ((i & (1 << j)) != 0) {
                    durationToTest = durationToTest.plus(alternatives[j]);
                }
            }

            final String formatted = npf.niceDuration(new Duration(durationToTest)).get();
            final TokenStream ts = new TokenStream(tokenizer.tokenize(formatted));
            assertDuration(formatted, ts, true, durationToTest); // short scale is unused in italian
            assertTrue(ts.finished());
        }
    }
}
