package com.example.forexeventalarm.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Event {
    private String date;
    private String time;
    private String title;
    private String impact;
    private String currency; // <-- NEW FIELD

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time; // raw UTC time
    }

    public String getTitle() {
        return title;
    }

    public String getImpact() {
        return impact;
    }

    public String getCurrency() {
        return currency;
    }

    // ✅ UTC → Local conversion
    public String getLocalTime() {
        try {
            String input = date + " " + time; // Example: "09-27-2025 3:30am"

            SimpleDateFormat sdfUtc = new SimpleDateFormat("MM-dd-yyyy h:mma", Locale.US);
            sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date parsedDate = sdfUtc.parse(input);

            SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            sdfLocal.setTimeZone(TimeZone.getDefault());

            return sdfLocal.format(parsedDate);

        } catch (Exception e) {
            e.printStackTrace();
            return time;
        }
    }
}
