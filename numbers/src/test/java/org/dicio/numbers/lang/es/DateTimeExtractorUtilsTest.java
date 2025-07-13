package org.dicio.numbers.lang.es;

import static org.dicio.numbers.test.TestUtils.t;
import static org.dicio.numbers.util.NumberExtractorUtils.signBeforeNumber;
import static java.time.temporal.ChronoUnit.MONTHS;

import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.DateTimeExtractorUtilsTestBase;
import org.dicio.numbers.util.DateTimeExtractorUtils;
import org.dicio.numbers.util.NumberExtractorUtils;
import org.junit.Test;

import java.time.LocalDateTime;

public class DateTimeExtractorUtilsTest extends DateTimeExtractorUtilsTestBase {

    // Saturday the 4th of February, 2023, 22:03:47
    private static final LocalDateTime NOW = LocalDateTime.of(2023, 2, 4, 22, 3, 47, 482175927);

    @Override
    public String configFolder() {
        return "config/es-es";
    }

    @Override
    public DateTimeExtractorUtils build(final TokenStream ts) {
        final SpanishNumberExtractor numberExtractor = new SpanishNumberExtractor(ts, false);
        return new DateTimeExtractorUtils(ts, NOW, (fromInclusive, toInclusive) ->
            NumberExtractorUtils.extractOneIntegerInRange(ts, fromInclusive, toInclusive,
                    () -> signBeforeNumber(ts, () -> numberExtractor.numberInteger(false)))
        );
    }
    @Test
    public void testRelativeMonthDuration() {
        assertRelativeMonthDuration("septiembre que viene",             t(7, MONTHS),   2);
        assertRelativeMonthDuration("próximo abril y de", t(2, MONTHS),   3);
        assertRelativeMonthDuration("último abril y de",      t(-10, MONTHS), 3);
        assertRelativeMonthDuration("febrero que vendrá",          t(12, MONTHS),  2);
        assertRelativeMonthDuration("febrero que pasó",              t(-12, MONTHS), 2);
        assertRelativeMonthDuration("enero pasado",                t(-1, MONTHS),  2);
    }

    @Test
    public void testRelativeMonthDurationNull() {
        assertRelativeMonthDurationNull("hola cómo estás");
        assertRelativeMonthDurationNull("en noviembre ocurrirá");
        assertRelativeMonthDurationNull("octubre");
        assertRelativeMonthDurationNull("en dos octubres");
        assertRelativeMonthDurationNull("en dos meses");
    }

    @Test
    public void testRelativeToday() {
        assertRelativeToday("hoy");
        assertRelativeToday("hoy ahora mismo");
        assertRelativeToday("hoy prueba");
        assertRelativeToday("hoy y");
    }

    @Test
    public void testRelativeTodayNull() {
        assertRelativeTodayNull("hola cómo estás");
        assertRelativeTodayNull("el mismo hoy");
        assertRelativeTodayNull("el día de hoy");
        assertRelativeTodayNull("ayer");
        assertRelativeTodayNull("mañana");
    }

    @Test
    public void testRelativeDayOfWeekDuration() {
        assertRelativeDayOfWeekDuration("el siguiente jueves",                    5,   2);
        assertRelativeDayOfWeekDuration("el anterior jueves",                    -2,  2);
        assertRelativeDayOfWeekDuration("los dos domingos pasados sí",             -13, 3);
        assertRelativeDayOfWeekDuration("tres y jueves y siguientes", 17,  5);
        assertRelativeDayOfWeekDuration("cuatro martes antes y",       -26, 4);
        assertRelativeDayOfWeekDuration("siguiente domingo",                7,   2);
        assertRelativeDayOfWeekDuration("este sábado",                     -7,  2);
    }

    @Test
    public void testRelativeDayOfWeekDurationNull() {
        assertRelativeDayOfWeekDurationNull("hola cómo estás");
        assertRelativeDayOfWeekDurationNull("lunes");
        assertRelativeDayOfWeekDurationNull("este lunes");
        assertRelativeDayOfWeekDurationNull("dos viernes");
        assertRelativeDayOfWeekDurationNull("en dos días");
        assertRelativeDayOfWeekDurationNull("en dos sábados");
        assertRelativeDayOfWeekDurationNull("un lunes anterior");
        assertRelativeDayOfWeekDurationNull("ayes y mañana");
    }

    @Test
    public void testMinute() {
        assertMinute("cero a b c",         0,  1);
        assertMinute("cincuenta y nueve horas",   59, 2);
        assertMinute("quince y",        15, 1);
        assertMinute("veintiocho s", 28, 3);
        assertMinute("seis mins prueba",      6,  2);
        assertMinute("treinta y seis de min",  36, 2);
        assertMinute("44m de",             44, 2);
    }

    @Test
    public void testMinuteNull() {
        assertMinuteNull("hola cómo estás");
        assertMinuteNull("sesenta minutos");
        assertMinuteNull("ciento y veinte");
        assertMinuteNull("menos dieciséis");
        assertMinuteNull("12000 minutos");
        assertMinuteNull("y dos de");
    }

    @Test
    public void testSecond() {
        assertSecond("cero a b c",         0,  1);
        assertSecond("ciento nueve horas",   59, 2);
        assertSecond("quince y",        15, 1);
        assertSecond("veinto y ocho h", 28, 3);
        assertSecond("seis segs test",      6,  2);
        assertSecond("treinta seise de seg",  36, 2);
        assertSecond("44s de",             44, 2);
    }

    @Test
    public void testSecondNull() {
        assertSecondNull("hola cómo estás");
        assertSecondNull("sesenta segundos");
        assertSecondNull("ciento y veinte");
        assertSecondNull("menos dieciseis");
        assertSecondNull("12000 segundos");
        assertSecondNull("y dos de");
    }

    @Test
    public void testBcad() {
        assertBcad("a.C. prueba",     false, 3);
        assertBcad("d.C. and",      true,  3);
        assertBcad("adc prueba y",    true,  1);
        assertBcad("antes de Cristo", false, 2);
        assertBcad("d y Domini",  true,  3);
        assertBcad("ace",           false, 1);
        assertBcad("d current",     false, 2);

        // there is a workaround for this in spanishDateTimeExtractor
        assertBcad("a.c.e.",        false, 3);
    }

    @Test
    public void testBcadNull() {
        assertBcadNull("a.m.");
        assertBcadNull("después prueba Cristo");
        assertBcadNull("y antes Cristo");
        assertBcadNull("prueba c");
        assertBcadNull("m");
        assertBcadNull("c prueba");
    }

    @Test
    public void testAmpm() {
        assertAmpm("a.m. prueba",      false, 3);
        assertAmpm("p.m. y",       true,  3);
        assertAmpm("am y prueba",    false, 1);
        assertAmpm("post meridiano",  true,  2);
        assertAmpm("p y meridiem", true,  3);
    }

    @Test
    public void testAmpmNull() {
        assertAmpmNull("A.C.");
        assertAmpmNull("ante prueba meridiem");
        assertAmpmNull("y post m");
        assertAmpmNull("prueba m");
        assertAmpmNull("c");
        assertAmpmNull("aym");
        assertAmpmNull("meridiano prueba");
    }

    @Test
    public void testMonthName() {
        assertMonthName("enero",    1);
        assertMonthName("dic e",      12);
        assertMonthName("sept ember", 9);
        assertMonthName("mar",        3);
    }

    @Test
    public void testMonthNameNull() {
        assertMonthNameNull("lunes");
        assertMonthNameNull("jaguar");
        assertMonthNameNull("hola feb");
        assertMonthNameNull("y dic de");
    }
}
