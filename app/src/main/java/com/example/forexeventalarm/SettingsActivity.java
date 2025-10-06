package com.example.forexeventalarm;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AppSettings";
    public static final String LEAD_TIME_KEY = "NotificationLeadTime";
    public static final String SLEEP_START_KEY = "SleepStart";
    public static final String SLEEP_END_KEY = "SleepEnd";
    public static final String SELECTED_CURRENCIES_KEY = "SelectedCurrencies";
    public static final String SELECTED_IMPACTS_KEY = "SelectedImpacts";

    private EditText etCustomLeadTime;
    private Spinner spinnerUnits;
    private Button btnSave;
    private TextView tvSleepStart, tvSleepEnd;

    // Currency checkboxes
    private CheckBox checkUSD, checkEUR, checkGBP, checkJPY, checkAUD, checkCAD, checkCHF, checkCNY, checkNZD;

    // Impact checkboxes
    private CheckBox checkHigh, checkMedium, checkLow;

    private String sleepStartTime = "";
    private String sleepEndTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etCustomLeadTime = findViewById(R.id.et_custom_lead_time);
        spinnerUnits = findViewById(R.id.spinner_units);
        btnSave = findViewById(R.id.btn_save_all_settings);

        tvSleepStart = findViewById(R.id.tv_sleep_start);
        tvSleepEnd = findViewById(R.id.tv_sleep_end);

        checkUSD = findViewById(R.id.checkUSD);
        checkEUR = findViewById(R.id.checkEUR);
        checkGBP = findViewById(R.id.checkGBP);
        checkJPY = findViewById(R.id.checkJPY);
        checkAUD = findViewById(R.id.checkAUD);
        checkCAD = findViewById(R.id.checkCAD);
        checkCHF = findViewById(R.id.checkCHF);
        checkCNY = findViewById(R.id.checkCNY);
        checkNZD = findViewById(R.id.checkNZD);

        checkHigh = findViewById(R.id.checkHigh);
        checkMedium = findViewById(R.id.checkMedium);
        checkLow = findViewById(R.id.checkLow);

        btnSave.setOnClickListener(v -> saveSettings());
        tvSleepStart.setOnClickListener(v -> showTimePickerDialog(true));
        tvSleepEnd.setOnClickListener(v -> showTimePickerDialog(false));

        loadSettings();
    }

    private void showTimePickerDialog(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String formattedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
            if (isStartTime) {
                sleepStartTime = formattedTime;
                tvSleepStart.setText("Sleep starts at: " + sleepStartTime);
            } else {
                sleepEndTime = formattedTime;
                tvSleepEnd.setText("Sleep ends at: " + sleepEndTime);
            }
        }, hour, minute, true);
        dialog.show();
    }

    private void saveSettings() {
        String input = etCustomLeadTime.getText().toString().trim();
        int value = 10;
        try { value = Integer.parseInt(input); } catch (NumberFormatException ignored) {}

        if (spinnerUnits.getSelectedItemPosition() == 1) value *= 60; // hours to minutes
        value = Math.max(0, Math.min(value, 1440));

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(LEAD_TIME_KEY, value);
        editor.putString(SLEEP_START_KEY, sleepStartTime);
        editor.putString(SLEEP_END_KEY, sleepEndTime);

        // Save currencies
        Set<String> currencies = new HashSet<>();
        if (checkUSD.isChecked()) currencies.add("USD");
        if (checkEUR.isChecked()) currencies.add("EUR");
        if (checkGBP.isChecked()) currencies.add("GBP");
        if (checkJPY.isChecked()) currencies.add("JPY");
        if (checkAUD.isChecked()) currencies.add("AUD");
        if (checkCAD.isChecked()) currencies.add("CAD");
        if (checkCHF.isChecked()) currencies.add("CHF");
        if (checkCNY.isChecked()) currencies.add("CNY");
        if (checkNZD.isChecked()) currencies.add("NZD");
        editor.putStringSet(SELECTED_CURRENCIES_KEY, currencies);

        // Save impacts
        Set<String> impacts = new HashSet<>();
        if (checkHigh.isChecked()) impacts.add("High");
        if (checkMedium.isChecked()) impacts.add("Medium");
        if (checkLow.isChecked()) impacts.add("Low");
        editor.putStringSet(SELECTED_IMPACTS_KEY, impacts);

        editor.apply();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> savedCurrencies = prefs.getStringSet(SELECTED_CURRENCIES_KEY, new HashSet<>());
        Set<String> savedImpacts = prefs.getStringSet(SELECTED_IMPACTS_KEY, new HashSet<>());

        checkUSD.setChecked(savedCurrencies.contains("USD"));
        checkEUR.setChecked(savedCurrencies.contains("EUR"));
        checkGBP.setChecked(savedCurrencies.contains("GBP"));
        checkJPY.setChecked(savedCurrencies.contains("JPY"));
        checkAUD.setChecked(savedCurrencies.contains("AUD"));
        checkCAD.setChecked(savedCurrencies.contains("CAD"));
        checkCHF.setChecked(savedCurrencies.contains("CHF"));
        checkCNY.setChecked(savedCurrencies.contains("CNY"));
        checkNZD.setChecked(savedCurrencies.contains("NZD"));

        checkHigh.setChecked(savedImpacts.contains("High"));
        checkMedium.setChecked(savedImpacts.contains("Medium"));
        checkLow.setChecked(savedImpacts.contains("Low"));
    }
}
