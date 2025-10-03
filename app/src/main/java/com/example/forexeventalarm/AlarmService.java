// Create this new file: AlarmService.java

package com.example.forexeventalarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get event details from the intent
        String eventTitle = intent.getStringExtra("eventTitle");
        String eventTime = intent.getStringExtra("eventTime");
        String eventImpact = intent.getStringExtra("eventImpact");

        // Create the same full-screen intent and notification as before
        Intent fullScreenIntent = new Intent(this, AlarmActivity.class);
        fullScreenIntent.putExtra("eventTitle", eventTitle);
        fullScreenIntent.putExtra("eventTime", eventTime);
        fullScreenIntent.putExtra("eventImpact", eventImpact);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "eventChannel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Upcoming Event!")
                .setContentText(eventTitle)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true);

        // This is the key line: we start the service in the foreground
        // which tells the system this is a high-priority task.
        startForeground((int) System.currentTimeMillis(), builder.build());

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
