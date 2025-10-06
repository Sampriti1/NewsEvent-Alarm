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

    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        TextView tvTitle = findViewById(R.id.tv_event_title);
        TextView tvTime = findViewById(R.id.tv_event_time);
        TextView tvImpact = findViewById(R.id.tv_event_impact);
        Button btnDismiss = findViewById(R.id.btn_dismiss);

        String title = getIntent().getStringExtra("eventTitle");
        String time = getIntent().getStringExtra("eventTime");
        String impact = getIntent().getStringExtra("eventImpact");

        tvTitle.setText(title);
        tvTime.setText(time);
        tvImpact.setText("Impact: " + impact);

        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(this, alarmSound);
            ringtone.play();
        } catch (Exception e) { e.printStackTrace(); }

        btnDismiss.setOnClickListener(v -> {
            if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
            finish();
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
