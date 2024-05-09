package org.dicio.numbers.formatter.datetime

class FormatString(stringToParse: String) {
    interface Part {
        fun format(substitution: Map<String, String>, stringBuilder: StringBuilder)
    }

    private class StringPart(private val value: String) : Part {
        override fun format(
            substitution: Map<String, String>,
            stringBuilder: StringBuilder
        ) {
            stringBuilder.append(value)
        }
    }

    private class FormatPart(private val key: String) : Part {
        override fun format(
            substitution: Map<String, String>,
            stringBuilder: StringBuilder
        ) {
            require(substitution.containsKey(key)) { "Missing key $key" }
            stringBuilder.append(substitution[key])
        }
    }


    private val parts: MutableList<Part> = ArrayList()

    init {
        var prevIndex = 0
        while (prevIndex < stringToParse.length) {
            val beginIndex = stringToParse.indexOf('{', prevIndex)
            if (beginIndex < 0) {
                parts.add(StringPart(stringToParse.substring(prevIndex)))
                break
            }

            val endIndex = stringToParse.indexOf('}', beginIndex + 1)
            if (endIndex < 0) {
                parts.add(StringPart(stringToParse.substring(prevIndex)))
                break
            }

            if (beginIndex != prevIndex) {
                parts.add(StringPart(stringToParse.substring(prevIndex, beginIndex)))
            }
            parts.add(FormatPart(stringToParse.substring(beginIndex + 1, endIndex)))
            prevIndex = endIndex + 1
        }
    }

    fun format(substitutionTable: Map<String, String>): String {
        val stringBuilder = StringBuilder()
        for (part in parts) {
            part.format(substitutionTable, stringBuilder)
        }
        return stringBuilder.toString()
    }
}
