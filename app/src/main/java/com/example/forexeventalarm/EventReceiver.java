package com.example.forexeventalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class EventReceiver extends BroadcastReceiver {

    private static final String TAG = "EventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Get event data from PendingIntent
        String currency = intent.getStringExtra("eventCurrency");
        String impact = intent.getStringExtra("eventImpact");
        String eventTitle = intent.getStringExtra("eventTitle");
        String eventTime = intent.getStringExtra("eventTime");

        Log.d(TAG, "Received event -> Currency: " + currency + ", Impact: " + impact + ", Title: " + eventTitle);

        // Load user preferences
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> selectedCurrencies = prefs.getStringSet(SettingsActivity.SELECTED_CURRENCIES_KEY, new HashSet<>());
        Set<String> selectedImpacts = prefs.getStringSet(SettingsActivity.SELECTED_IMPACTS_KEY, new HashSet<>());

        Log.d(TAG, "User selected currencies: " + selectedCurrencies);
        Log.d(TAG, "User selected impacts: " + selectedImpacts);

        // Only trigger alarm if both match
        if (selectedCurrencies.contains(currency) && selectedImpacts.contains(impact)) {
            Log.d(TAG, "Event matches user settings. Starting AlarmService...");

            Intent alarmIntent = new Intent(context, AlarmService.class);
            alarmIntent.putExtra("eventTitle", eventTitle);
            alarmIntent.putExtra("eventTime", eventTime);
            alarmIntent.putExtra("eventImpact", impact);
            alarmIntent.putExtra("eventCurrency", currency);

            // This starts a foreground service that will play the alarm
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(alarmIntent);
            } else {
                context.startService(alarmIntent);
            }
        } else {
            Log.d(TAG, "Event does NOT match user settings. Alarm will not ring.");
        }
    }
}
