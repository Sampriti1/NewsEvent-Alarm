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

    // ✅ New method: convert UTC -> device local
    public String getLocalTime() {
        try {
            String input = date + " " + time; // Example: "09-27-2025 3:30am"

            // Parse incoming UTC time
            SimpleDateFormat sdfUtc = new SimpleDateFormat("MM-dd-yyyy h:mma", Locale.US);
            sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date parsedDate = sdfUtc.parse(input);

            // Convert to device local timezone
            SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            sdfLocal.setTimeZone(TimeZone.getDefault());

            return sdfLocal.format(parsedDate);

        } catch (Exception e) {
            e.printStackTrace();
            return time; // fallback: return raw time
        }
    }
}
