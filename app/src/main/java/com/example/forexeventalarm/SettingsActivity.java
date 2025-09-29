package com.example.forexeventalarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AppSettings";
    public static final String LEAD_TIME_KEY = "NotificationLeadTime";

    private RadioGroup radioGroupLeadTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        radioGroupLeadTime = findViewById(R.id.radioGroup_leadTime);
        loadPreference(); // Set the radio button to the currently saved value

        // Add a listener that saves the new value whenever the user clicks a different option
        radioGroupLeadTime.setOnCheckedChangeListener((group, checkedId) -> {
            int leadTime = 30; // Default
            if (checkedId == R.id.radio_5_min) {
                leadTime = 5;
            } else if (checkedId == R.id.radio_10_min) {
                leadTime = 10;
            } else if (checkedId == R.id.radio_20_min) {
                leadTime = 20;
            }

            else if (checkedId == R.id.radio_30_min) {
                leadTime = 30;
            }
            savePreference(leadTime);
        });
    }

    private void savePreference(int leadTime) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(LEAD_TIME_KEY, leadTime);
        editor.apply();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadPreference() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int leadTime = settings.getInt(LEAD_TIME_KEY, 30); // Default to 30 if nothing is saved

        if (leadTime == 5) {
            radioGroupLeadTime.check(R.id.radio_5_min);
        } else if (leadTime == 10) {
            radioGroupLeadTime.check(R.id.radio_10_min);
        } else if (leadTime == 20) {
            radioGroupLeadTime.check(R.id.radio_20_min);
        } else {
            radioGroupLeadTime.check(R.id.radio_30_min);
        }
    }
}