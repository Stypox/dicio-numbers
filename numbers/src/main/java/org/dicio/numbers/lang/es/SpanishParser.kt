package org.dicio.numbers.lang.es

import org.dicio.numbers.parser.NumberParser
import java.util.*

class SpanishParser : NumberParser() {
    override fun getLanguage(): String = "es"

    override fun getNumberWords(): Map<String, Long> {
        return mapOf(
            "cero" to 0,
            "uno" to 1,
            "dos" to 2,
            "tres" to 3,
            "cuatro" to 4,
            "cinco" to 5,
            "seis" to 6,
            "siete" to 7,
            "ocho" to 8,
            "nueve" to 9,
            "diez" to 10,
            "once" to 11,
            "doce" to 12,
            "trece" to 13,
            "catorce" to 14,
            "quince" to 15,
            "dieciséis" to 16,
            "diecisiete" to 17,
            "dieciocho" to 18,
            "diecinueve" to 19,
            "veinte" to 20,
            "treinta" to 30,
            "cuarenta" to 40,
            "cincuenta" to 50,
            "sesenta" to 60,
            "setenta" to 70,
            "ochenta" to 80,
            "noventa" to 90,
            "cien" to 100,
            "ciento" to 100,
            "doscientos" to 200,
            "trescientos" to 300,
            "cuatrocientos" to 400,
            "quinientos" to 500,
            "seiscientos" to 600,
            "setecientos" to 700,
            "ochocientos" to 800,
            "novecientos" to 900,
            "mil" to 1000,
            "millón" to 1_000_000,
            "millones" to 1_000_000
        )
    }

    override fun getOrdinalWords(): Map<String, Long> {
        return mapOf(
            "primero" to 1,
            "segundo" to 2,
            "tercero" to 3,
            "cuarto" to 4,
            "quinto" to 5,
            "sexto" to 6,
            "séptimo" to 7,
            "octavo" to 8,
            "noveno" to 9,
            "décimo" to 10
        )
    }
}
