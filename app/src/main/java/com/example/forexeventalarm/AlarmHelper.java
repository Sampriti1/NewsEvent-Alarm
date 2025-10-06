package com.example.forexeventalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AlarmHelper {

    public static void scheduleEventAlarm(Context context, String currency, String impact, String eventTitle, String eventTime) {

        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);

        Set<String> selectedCurrencies = prefs.getStringSet(SettingsActivity.SELECTED_CURRENCIES_KEY, new HashSet<>());
        Set<String> selectedImpacts = prefs.getStringSet(SettingsActivity.SELECTED_IMPACTS_KEY, new HashSet<>());

        if (!selectedCurrencies.contains(currency)) return; // ignore this currency
        if (!selectedImpacts.contains(impact)) return; // ignore this impact

        int leadTime = prefs.getInt(SettingsActivity.LEAD_TIME_KEY, 10); // in minutes

        // Convert eventTime string "HH:mm" to millis
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        Calendar eventCal = Calendar.getInstance();
        try {
            Date eventDate = sdf.parse(eventTime);
            Calendar now = Calendar.getInstance();
            eventCal.setTime(eventDate);
            eventCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
            eventCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
            eventCal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            eventCal.add(Calendar.MINUTE, -leadTime); // fire alarm leadTime minutes before
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("eventTitle", eventTitle);
        intent.putExtra("eventTime", eventTime);
        intent.putExtra("eventImpact", impact);

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, eventCal.getTimeInMillis(), pendingIntent);
        }
    }
}
