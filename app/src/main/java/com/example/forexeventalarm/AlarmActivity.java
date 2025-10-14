package com.example.forexeventalarm;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.forexeventalarm.model.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AlarmActivity extends AppCompatActivity {

    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // --- This code wakes up the screen and shows the activity over the lock screen ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        // --- Find UI elements ---
        TextView tvTitle = findViewById(R.id.tv_event_title);
        //TextView tvDetails = findViewById(R.id.tv_event_time); // Repurposing this TextView for details
        //TextView tvImpactLabel = findViewById(R.id.tv_event_impact); // We will hide this
        // AFTER
        TextView tvDetails = findViewById(R.id.tv_event_details);
        Button btnDismiss = findViewById(R.id.btn_dismiss);

        // --- NEW: Logic to handle a group of events ---
        String eventsJson = getIntent().getStringExtra("eventsJson");
        if (eventsJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Event>>() {}.getType();
            List<Event> events = gson.fromJson(eventsJson, type);

            if (events != null && !events.isEmpty()) {
                // If there's more than one event, show a summary title
                if (events.size() > 1) {
                    tvTitle.setText(events.size() + " Events Occurring Now");
                } else {
                    // If there's only one event, use its title
                    tvTitle.setText(events.get(0).getTitle());
                }

                // Build a detailed string with information for all events
                StringBuilder detailsBuilder = new StringBuilder();
                for (Event event : events) {
                    detailsBuilder.append("• ") // Bullet point
                            .append(event.getCurrency())
                            .append(" | Impact: ")
                            .append(event.getImpact())
                            .append("\n")
                            .append(event.getTitle())
                            .append("\n\n"); // Add extra space between events
                }

                // Set the combined details and hide the now-unnecessary impact label
                tvDetails.setText(detailsBuilder.toString());
               // tvImpactLabel.setVisibility(View.GONE);
            }
        } else {
            // Fallback if something goes wrong
            tvTitle.setText("An event is occurring.");
        }


        // --- This is your existing code to play the sound ---
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(this, alarmSound);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- This is your existing code to stop the alarm ---
        btnDismiss.setOnClickListener(v -> {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
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
