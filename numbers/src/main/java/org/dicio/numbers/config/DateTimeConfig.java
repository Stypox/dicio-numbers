package org.dicio.numbers.config;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import java.util.HashMap;
import java.util.Map;

public class DateTimeConfig {

    public final FormatStringCollection decadeFormat;
    public final FormatStringCollection hundredFormat;
    public final FormatStringCollection thousandFormat;
    public final FormatStringCollection yearFormat;

    public final String bc;

    public final FormatString dateFormatFull;
    public final FormatString dateFormatFullNoYear;
    public final FormatString dateFormatFullNoYearMonth;

    public final FormatString dateTimeFormat;

    public final String today;
    public final String tomorrow;
    public final String yesterday;

    public final String[] weekdays;
    public final String[] days;
    public final String[] months;
    public final Map<Integer, String> numbers;

    public DateTimeConfig(final String jsonConfigPath) {
        try {
            final JsonObject root = JsonParser.object().from(
                    ClassLoader.getSystemClassLoader().getResourceAsStream(jsonConfigPath));

            final JsonObject year = root.getObject("year_format");
            final JsonObject date = root.getObject("date_format");
            final JsonObject dateTime = root.getObject("date_time_format");
            final JsonObject weekday = root.getObject("weekday");
            final JsonObject day = root.getObject("date");
            final JsonObject month = root.getObject("month");
            final JsonObject number = root.getObject("number");

            decadeFormat = new FormatStringCollection(root.getObject("decade_format"));
            hundredFormat = new FormatStringCollection(root.getObject("hundreds_format"));
            thousandFormat = new FormatStringCollection(root.getObject("thousand_format"));
            yearFormat = new FormatStringCollection(year);

            bc = year.getString("bc");

            dateFormatFull = new FormatString(date.getString("date_full"));
            dateFormatFullNoYear = new FormatString(date.getString("date_full_no_year"));
            dateFormatFullNoYearMonth
                    = new FormatString(date.getString("date_full_no_year_month"));

            dateTimeFormat = new FormatString(dateTime.getString("date_time"));

            today = date.getString("today");
            tomorrow = date.getString("tomorrow");
            yesterday = date.getString("yesterday");

            weekdays = new String[7];
            for (int i = 0; i < 7; ++i) {
                weekdays[i] = weekday.getString(String.valueOf(i));
            }

            days = new String[31];
            for (int i = 0; i < 31; ++i) {
                days[i] = day.getString(String.valueOf(i + 1));
            }

            months = new String[12];
            for (int i = 0; i < 12; ++i) {
                months[i] = month.getString(String.valueOf(i + 1));
            }

            numbers = new HashMap<>();
            for (final Map.Entry<String, Object> entry : number.entrySet()) {
                numbers.put(Integer.valueOf(entry.getKey()), (String) entry.getValue());
            }
        } catch (final JsonParserException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNumber(final int number) {
        if (numbers.containsKey(number)) {
            return numbers.get(number);
        }
        return String.valueOf(number);
    }
}
