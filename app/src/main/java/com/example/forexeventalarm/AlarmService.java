package com.example.forexeventalarm;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.forexeventalarm.model.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AlarmService extends Service {

    private MediaPlayer mediaPlayer;
    public static final String STOP_ACTION = "com.example.forexeventalarm.STOP_ALARM";
    private static final int FOREGROUND_ID = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Handle the stop action from the notification
        // Handle the stop action from the notification
        if (intent != null && STOP_ACTION.equals(intent.getAction())) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        String eventsJson = intent.getStringExtra("eventsJson");
        if (eventsJson == null) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        // 1. Prepare data and default values
        Gson gson = new Gson();
        Type type = new TypeToken<List<Event>>() {}.getType();
        List<Event> events = gson.fromJson(eventsJson, type);

        String notificationTitle = "Forex Event Alarm";
        String notificationDetails = "An event is occurring.";
        NotificationCompat.Style notificationStyle = null;

        if (events != null && !events.isEmpty()) {
            if (events.size() > 1) {
                // --- For MULTIPLE events ---
                notificationTitle = events.size() + " Events Occurring Now!";
                String time = events.get(0).getTime();
                notificationDetails = "Time: " + time; // Short summary text

                // Build the full, multi-line text for the expanded view
                StringBuilder bigTextBuilder = new StringBuilder();
                bigTextBuilder.append("Time: ").append(time).append("\n\n"); // Add time at the top

                for (Event event : events) {
                    bigTextBuilder.append("• ") // Bullet point
                            .append(event.getTitle())
                            .append(" (")
                            .append(event.getCurrency())
                            .append(" - ")
                            .append(event.getImpact())
                            .append(")\n"); // New line for the next event
                }
                // Remove the final new line character
                if (bigTextBuilder.length() > 0) {
                    bigTextBuilder.setLength(bigTextBuilder.length() - 1);
                }

                // Set the notification style to be expandable
                notificationStyle = new NotificationCompat.BigTextStyle().bigText(bigTextBuilder.toString());

            } else {
                // --- For a SINGLE event ---
                Event event = events.get(0);
                notificationTitle = event.getTitle();
                notificationDetails = "Time: " + event.getTime() + " | Impact: " + event.getImpact();
            }
        }

        // 2. Start playing the alarm sound
        if (mediaPlayer == null) {
            Uri alarmSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
            }
            mediaPlayer = MediaPlayer.create(this, alarmSound);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }

        // 3. Create and show the foreground notification
        Intent stopIntent = new Intent(this, AlarmService.class);
        stopIntent.setAction(STOP_ACTION);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "eventChannel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDetails)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .setOngoing(true);

        // Apply the expandable style if we created one
        if (notificationStyle != null) {
            builder.setStyle(notificationStyle);
        }

        startForeground(FOREGROUND_ID, builder.build());

        return START_STICKY;
    }

    private void stopAlarm() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
