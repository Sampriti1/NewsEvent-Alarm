package com.example.forexeventalarm;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView; // <-- Added this import
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AppSettings";
    public static final String LEAD_TIME_KEY = "NotificationLeadTime";
    // ADDED KEYS FOR SLEEP MODE
    public static final String SLEEP_START_KEY = "SleepStart";
    public static final String SLEEP_END_KEY = "SleepEnd";

    private EditText etCustomLeadTime;
    private Spinner spinnerUnits;
    private Button btnSave;
    // ADDED TEXTVIEWS FOR SLEEP MODE UI
    private TextView tvSleepStart;
    private TextView tvSleepEnd;

    // ADDED STRINGS TO HOLD THE SELECTED TIMES
    private String sleepStartTime = "";
    private String sleepEndTime = "";

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
        // ADDED findViewById for new TextViews
        tvSleepStart = findViewById(R.id.tv_sleep_start);
        tvSleepEnd = findViewById(R.id.tv_sleep_end);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.lead_time_units, R.layout.spinner_item_white);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnits.setAdapter(adapter);

        loadSettings(); // Renamed from loadPreference
        btnSave.setOnClickListener(v -> saveSettings()); // Renamed from saveCustomLeadTime

        // ADDED LISTENERS for sleep time selectors
        tvSleepStart.setOnClickListener(v -> showTimePickerDialog(true));
        tvSleepEnd.setOnClickListener(v -> showTimePickerDialog(false));
    }

    // NEW METHOD to show the time picker dialog
    private void showTimePickerDialog(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String formattedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
            if (isStartTime) {
                sleepStartTime = formattedTime;
                tvSleepStart.setText("Sleep starts at: " + sleepStartTime);
                tvSleepStart.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                sleepEndTime = formattedTime;
                tvSleepEnd.setText("Sleep ends at: " + sleepEndTime);
                tvSleepEnd.setTextColor(getResources().getColor(android.R.color.white));
            }
        }, hour, minute, true); // true for 24-hour format

        timePickerDialog.show();
    }

    // RENAMED AND UPDATED to save all settings
    private void saveSettings() {
        // --- Save Lead Time (your existing logic) ---
        String input = etCustomLeadTime.getText().toString().trim();
        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid lead time number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerUnits.getSelectedItemPosition() == 1) { // Hours
            value *= 60;
        }
        if (value > 1440) value = 1440;
        if (value < 0) value = 0;

        // --- Save All Settings ---
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(LEAD_TIME_KEY, value);
        // ADDED logic to save sleep times
        editor.putString(SLEEP_START_KEY, sleepStartTime);
        editor.putString(SLEEP_END_KEY, sleepEndTime);
        editor.apply();

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    // RENAMED AND UPDATED to load all settings
    private void loadSettings() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // --- Load Lead Time (your existing logic) ---
        int leadTimeInMinutes = settings.getInt(LEAD_TIME_KEY, 10);
        if (leadTimeInMinutes >= 60 && leadTimeInMinutes % 60 == 0) {
            etCustomLeadTime.setText(String.valueOf(leadTimeInMinutes / 60));
            spinnerUnits.setSelection(1);
        } else {
            etCustomLeadTime.setText(String.valueOf(leadTimeInMinutes));
            spinnerUnits.setSelection(0);
        }

        // --- ADDED logic to load and display sleep times ---
        sleepStartTime = settings.getString(SLEEP_START_KEY, "");
        sleepEndTime = settings.getString(SLEEP_END_KEY, "");

        if (!sleepStartTime.isEmpty()) {
            tvSleepStart.setText("Sleep starts at: " + sleepStartTime);
            tvSleepStart.setTextColor(getResources().getColor(android.R.color.white));
        }
        if (!sleepEndTime.isEmpty()) {
            tvSleepEnd.setText("Sleep ends at: " + sleepEndTime);
            tvSleepEnd.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
}

