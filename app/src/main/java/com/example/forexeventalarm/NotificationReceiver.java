// In NotificationReceiver.java

package com.example.forexeventalarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.provider.Settings;
import android.content.SharedPreferences; // <-- Add this import
import java.util.Calendar; // <-- Add this import
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    // In NotificationReceiver.java

    @Override
    public void onReceive(Context context, Intent intent) {
        // --- ADD THE SLEEP MODE CHECK AT THE VERY TOP ---
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String sleepStart = prefs.getString(SettingsActivity.SLEEP_START_KEY, "");
        String sleepEnd = prefs.getString(SettingsActivity.SLEEP_END_KEY, "");

        // Only check if both start and end times are set
        if (!sleepStart.isEmpty() && !sleepEnd.isEmpty()) {
            try {
                Calendar now = Calendar.getInstance();
                int currentHour = now.get(Calendar.HOUR_OF_DAY);
                int currentMinute = now.get(Calendar.MINUTE);
                int currentTimeInMinutes = currentHour * 60 + currentMinute;

                int startHour = Integer.parseInt(sleepStart.split(":")[0]);
                int startMinute = Integer.parseInt(sleepStart.split(":")[1]);
                int startTimeInMinutes = startHour * 60 + startMinute;

                int endHour = Integer.parseInt(sleepEnd.split(":")[0]);
                int endMinute = Integer.parseInt(sleepEnd.split(":")[1]);
                int endTimeInMinutes = endHour * 60 + endMinute;

                boolean isSleeping = false;
                // This logic handles overnight sleep periods (e.g., 22:00 to 06:00)
                if (startTimeInMinutes > endTimeInMinutes) {
                    if (currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes < endTimeInMinutes) {
                        isSleeping = true;
                    }
                } else { // This handles same-day sleep periods (e.g., 13:00 to 15:00)
                    if (currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes < endTimeInMinutes) {
                        isSleeping = true;
                    }
                }

                if (isSleeping) {
                    Log.d("NotificationReceiver", "Sleep mode is active. Silencing alarm.");
                    return; // Exit without showing the alarm
                }

            } catch (Exception e) {
                Log.e("NotificationReceiver", "Error parsing sleep time", e);
            }
        }
        // --- END OF SLEEP MODE CHECK ---
        Log.d("NotificationReceiver", "Alarm received for: " + intent.getStringExtra("eventTitle"));

        // First, check if we have the overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
            // PERMISSION GRANTED: Launch the AlarmActivity directly.
            Intent activityIntent = new Intent(context, AlarmActivity.class);
            activityIntent.putExtras(intent.getExtras()); // Copy all data
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);

        } else {
            // PERMISSION DENIED: Fall back to the reliable foreground service + notification method.
            Intent serviceIntent = new Intent(context, AlarmService.class);
            serviceIntent.putExtras(intent.getExtras()); // Copy all data

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}