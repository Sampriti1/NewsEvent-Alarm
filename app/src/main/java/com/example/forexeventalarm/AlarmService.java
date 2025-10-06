package com.example.forexeventalarm;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;

public class AlarmService extends Service {

    private MediaPlayer mediaPlayer;

    public static final String STOP_ACTION = "com.example.forexeventalarm.STOP_ALARM";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && STOP_ACTION.equals(intent.getAction())) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        String eventTitle = intent.getStringExtra("eventTitle");
        String eventImpact = intent.getStringExtra("eventImpact");

        // Play alarm sound with looping
        Uri alarmSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
        if (alarmSound == null)
            alarmSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);

        mediaPlayer = MediaPlayer.create(this, alarmSound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Stop button intent
        Intent stopIntent = new Intent(this, AlarmService.class);
        stopIntent.setAction(STOP_ACTION);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "eventChannel")
                .setSmallIcon(R.drawable.ic_notification) // Replace with valid icon
                .setContentTitle(eventTitle)
                .setContentText("Impact: " + eventImpact)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.ic_notification, "Stop", stopPendingIntent)
                .setAutoCancel(false); // Keep notification until stop

        startForeground(1, builder.build());

        return START_STICKY;
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
