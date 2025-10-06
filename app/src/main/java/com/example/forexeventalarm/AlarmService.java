package com.example.forexeventalarm;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {

    private MediaPlayer mediaPlayer;
    public static final String STOP_ACTION = "com.example.forexeventalarm.STOP_ALARM";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // If STOP action is received
        if (intent != null && STOP_ACTION.equals(intent.getAction())) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        String eventTitle = intent.getStringExtra("eventTitle");
        String eventImpact = intent.getStringExtra("eventImpact");

        // Prevent multiple alarms playing
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return START_NOT_STICKY;
        }

        // Get alarm sound URI
        Uri alarmSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
        if (alarmSound == null)
            alarmSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);

        mediaPlayer = MediaPlayer.create(this, alarmSound);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        // Intent for Stop action
        Intent stopIntent = new Intent(this, AlarmService.class);
        stopIntent.setAction(STOP_ACTION);

        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "eventChannel")
                .setSmallIcon(R.drawable.ic_stop) // ✅ your stop icon
                .setContentTitle(eventTitle != null ? eventTitle : "Forex Event Alarm")
                .setContentText("Impact: " + (eventImpact != null ? eventImpact : "N/A"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .setOngoing(true)
                .setAutoCancel(false);

        startForeground(1, builder.build());

        return START_STICKY;
    }

    private void stopAlarm() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
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
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
