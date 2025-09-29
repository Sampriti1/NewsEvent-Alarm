package com.example.forexeventalarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AppSettings";
    public static final String LEAD_TIME_KEY = "NotificationLeadTime";

    private EditText etCustomLeadTime;
    private Spinner spinnerUnits;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        etCustomLeadTime = findViewById(R.id.et_custom_lead_time);
        spinnerUnits = findViewById(R.id.spinner_units);
        btnSave = findViewById(R.id.btn_save);

        // Setup Spinner Adapter
        // In SettingsActivity.java -> onCreate()

// Setup Spinner Adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.lead_time_units, R.layout.spinner_item_white); // Use our custom layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnits.setAdapter(adapter);


        loadPreference();
        btnSave.setOnClickListener(v -> saveCustomLeadTime());
    }

    private void saveCustomLeadTime() {
        String input = etCustomLeadTime.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
            return;
        }

        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }

        // 0 position is "Minutes", 1 position is "Hours"
        if (spinnerUnits.getSelectedItemPosition() == 1) { // Hours
            value *= 60; // convert hours to minutes
        }

        // Optional: Cap the lead time to 24 hours (1440 minutes)
        if (value > 1440) {
            value = 1440;
            Toast.makeText(this, "Maximum lead time is 24 hours.", Toast.LENGTH_SHORT).show();
        }
        if (value < 0) {
            value = 0;
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(LEAD_TIME_KEY, value);
        editor.apply();

        Toast.makeText(this, "Lead time saved: " + value + " minutes", Toast.LENGTH_SHORT).show();
        finish(); // Close the settings screen after saving
    }

    private void loadPreference() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int leadTimeInMinutes = settings.getInt(LEAD_TIME_KEY, 10); // default 30 min

        // If the value is a perfect hour and is 60 or more
        if (leadTimeInMinutes >= 60 && leadTimeInMinutes % 60 == 0) {
            etCustomLeadTime.setText(String.valueOf(leadTimeInMinutes / 60));
            spinnerUnits.setSelection(1); // Set spinner to "Hours"
        } else {
            etCustomLeadTime.setText(String.valueOf(leadTimeInMinutes));
            spinnerUnits.setSelection(0); // Set spinner to "Minutes"
        }
    }
}

