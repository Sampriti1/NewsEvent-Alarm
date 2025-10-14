package com.example.forexeventalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class EventReceiver extends BroadcastReceiver {

    private static final String TAG = "EventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the JSON string containing the list of events for this alarm time.
        String eventsJson = intent.getStringExtra("eventsJson");

        // A safety check in case the intent is empty.
        if (eventsJson == null || eventsJson.isEmpty()) {
            Log.e(TAG, "Received an alarm trigger but eventsJson was null or empty.");
            return;
        }

        Log.d(TAG, "Alarm triggered for an event group. Starting AlarmService...");

        // Create an intent to start the AlarmService.
        Intent alarmServiceIntent = new Intent(context, AlarmService.class);

        // Pass the JSON string containing all event data to the service.
        alarmServiceIntent.putExtra("eventsJson", eventsJson);

        // Start the service. It will become a foreground service to play the alarm.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(alarmServiceIntent);
        } else {
            context.startService(alarmServiceIntent);
        }
    }
}
