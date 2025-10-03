package com.example.forexeventalarm;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    private TextView tvEventTitle;
    private TextView tvEventTime;
    private TextView tvEventImpact;
    private Button btnDismiss;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // These flags are crucial for showing the activity over the lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        tvEventTitle = findViewById(R.id.tv_event_title);
        tvEventTime = findViewById(R.id.tv_event_time);
        tvEventImpact = findViewById(R.id.tv_event_impact);
        btnDismiss = findViewById(R.id.btn_dismiss);

        // Get the event title from the intent
        String eventTitle = getIntent().getStringExtra("eventTitle");
        String eventTime = getIntent().getStringExtra("eventTime");
        String eventImpact = getIntent().getStringExtra("eventImpact");
        if (eventTitle != null) {
            tvEventTitle.setText(eventTitle);
        }
        if (eventTime != null) {
            tvEventTime.setText(eventTime);
        }
        if (eventImpact != null) {
            tvEventImpact.setText("Impact: " + eventImpact);
        }
        // Play the default alarm sound
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                // If alarm sound is null, try notification sound
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(this, alarmSound);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnDismiss.setOnClickListener(v -> {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
            finish(); // Close the alarm screen
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}
