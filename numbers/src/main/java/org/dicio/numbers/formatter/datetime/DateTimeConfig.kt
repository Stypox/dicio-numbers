package org.dicio.numbers.formatter.datetime

import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.dicio.numbers.util.ResourceOpener
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class DateTimeConfig(configFolder: String) {
    @JvmField
    val decadeFormat: FormatStringCollection
    @JvmField
    val hundredFormat: FormatStringCollection
    @JvmField
    val thousandFormat: FormatStringCollection
    @JvmField
    val yearFormat: FormatStringCollection

    @JvmField
    val bc: String

    @JvmField
    val dateFormatFull: FormatString
    @JvmField
    val dateFormatFullNoYear: FormatString
    @JvmField
    val dateFormatFullNoYearMonth: FormatString

    @JvmField
    val dateTimeFormat: FormatString

    @JvmField
    val today: String
    @JvmField
    val tomorrow: String
    @JvmField
    val yesterday: String

    @JvmField
    val weekdays: Array<String>
    @JvmField
    val days: Array<String>
    @JvmField
    val months: Array<String>
    @JvmField
    val numbers: Map<Int, String>

    @JvmField
    val dayWord: String
    @JvmField
    val daysWord: String
    @JvmField
    val hourWord: String
    @JvmField
    val hoursWord: String
    @JvmField
    val minuteWord: String
    @JvmField
    val minutesWord: String
    @JvmField
    val secondWord: String
    @JvmField
    val secondsWord: String

    init {
        try {
            val root = JsonParser.`object`().from(
                ResourceOpener.getResourceAsStream("$configFolder/date_time.json")
            )

            val year = root.getObject("year_format")
            val date = root.getObject("date_format")
            val dateTime = root.getObject("date_time_format")
            val weekday = root.getObject("weekday")
            val day = root.getObject("date")
            val month = root.getObject("month")
            val number = root.getObject("number")

            decadeFormat = FormatStringCollection(root.getObject("decade_format"))
            hundredFormat = FormatStringCollection(root.getObject("hundreds_format"))
            thousandFormat = FormatStringCollection(root.getObject("thousand_format"))
            yearFormat = FormatStringCollection(year)

            bc = year.getString("bc")

            dateFormatFull = FormatString(date.getString("date_full"))
            dateFormatFullNoYear = FormatString(date.getString("date_full_no_year"))
            dateFormatFullNoYearMonth = FormatString(date.getString("date_full_no_year_month"))

            dateTimeFormat = FormatString(dateTime.getString("date_time"))

            today = date.getString("today")
            tomorrow = date.getString("tomorrow")
            yesterday = date.getString("yesterday")

            weekdays = (0..6).map { weekday.getString(it.toString()) }.toTypedArray()
            days = (0..30).map { day.getString((it + 1).toString()) }.toTypedArray()
            months = (0..11).map { month.getString((it + 1).toString()) }.toTypedArray()
            numbers = number.map { (key, value) -> Pair(key.toInt(), value as String) }.toMap()

            dayWord = readWordFromFile(configFolder, "day")
            daysWord = readWordFromFile(configFolder, "days")
            hourWord = readWordFromFile(configFolder, "hour")
            hoursWord = readWordFromFile(configFolder, "hours")
            minuteWord = readWordFromFile(configFolder, "minute")
            minutesWord = readWordFromFile(configFolder, "minutes")
            secondWord = readWordFromFile(configFolder, "second")
            secondsWord = readWordFromFile(configFolder, "seconds")
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: JsonParserException) {
            throw RuntimeException(e)
        }
    }

    fun getNumber(number: Int): String {
        return numbers[number] ?: number.toString()
    }

    @Throws(IOException::class)
    private fun readWordFromFile(
        configFolder: String,
        word: String
    ): String {
        val inputStream = ResourceOpener.getResourceAsStream("$configFolder/$word.word")

        val result = ByteArrayOutputStream()
        val buffer = ByteArray(16)
        var length: Int
        while ((inputStream.read(buffer).also { length = it }) != -1) {
            result.write(buffer, 0, length)
        }
        return result.toString(StandardCharsets.UTF_8.name()).trim { it <= ' ' }
    }
}
