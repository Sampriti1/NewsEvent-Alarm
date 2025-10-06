package com.example.forexeventalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Stop the AlarmService when stop button is pressed
        Intent stopIntent = new Intent(context, AlarmService.class);
        context.stopService(stopIntent);
    }
}
