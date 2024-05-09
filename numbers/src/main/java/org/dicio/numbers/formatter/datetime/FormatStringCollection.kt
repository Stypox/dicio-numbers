package org.dicio.numbers.formatter.datetime

import com.grack.nanojson.JsonObject
import java.util.regex.Pattern

class FormatStringCollection(jsonObject: JsonObject) {
    private class PatternFormatPair(jsonObject: JsonObject) {
        val pattern: Pattern = Pattern.compile(jsonObject.getString("match"))
        val formatString: FormatString = FormatString(jsonObject.getString("format"))
    }

    private val defaultFormat = FormatString(jsonObject.getString("default"))
    private val patternFormats: MutableList<PatternFormatPair> = ArrayList()

    init {
        var i = 1
        while (jsonObject.has(i.toString())) {
            patternFormats.add(PatternFormatPair(jsonObject.getObject(i.toString())))
            ++i
        }
    }

    fun getMostSuitableFormatString(number: Int): FormatString {
        val numberString = number.toString()
        for (patternFormat in patternFormats) {
            if (patternFormat.pattern.matcher(numberString).matches()) {
                return patternFormat.formatString
            }
        }
        return defaultFormat
    }
}
