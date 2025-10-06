package com.example.forexeventalarm;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forexeventalarm.adapter.EventAdapter;
import com.example.forexeventalarm.model.Event;
import com.example.forexeventalarm.network.ApiClient;
import com.example.forexeventalarm.network.ApiService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> currentEvents;

    // ---------------- Permissions ----------------
    private final ActivityResultLauncher<Intent> overlayPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "Overlay permission granted.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permission denied. Alarm will show as notification.", Toast.LENGTH_LONG).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) fetchEvents();
                else Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Log.d(TAG, "Returned from settings. Rescheduling alarms...");
                if (currentEvents != null && !currentEvents.isEmpty()) {
                    scheduleAlarmsForEvents(currentEvents);
                }
            });

    // ---------------- OnCreate ----------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });

        ImageView filterBtn = findViewById(R.id.filterBtn);
        filterBtn.setOnClickListener(v -> showFilterDialog());

        createNotificationChannel();
        checkOverlayPermission();
        checkForegroundServicePermission();
        askForNotificationPermission();
    }

    // ---------------- Permissions ----------------

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("To show alarms immediately, please allow 'Display over other apps'.")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        overlayPermissionLauncher.launch(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) ->
                            Toast.makeText(this, "Alarms will show as notifications.", Toast.LENGTH_SHORT).show())
                    .show();
        }
    }

    private void checkForegroundServicePermission() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (checkSelfPermission(android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
                }, 200);
            }
        }
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                fetchEvents();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            fetchEvents();
        }
    }

    // ---------------- Fetch & Schedule ----------------

    private void fetchEvents() {
        Log.d(TAG, "Fetching events from API...");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Event>> call = apiService.getEvents();

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> events = response.body();
                    currentEvents = events;
                    eventAdapter = new EventAdapter(events);
                    recyclerView.setAdapter(eventAdapter);
                    scheduleAlarmsForEvents(events);
                    Log.d(TAG, "Fetched " + events.size() + " events.");
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    private void scheduleAlarmsForEvents(List<Event> events) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Please enable exact alarm permission.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            return;
        }

        int count = 0;
        for (int i = 0; i < events.size(); i++) {
            if (scheduleSingleAlarm(events.get(i), i, alarmManager)) count++;
        }
        Toast.makeText(this, count + " alarms set.", Toast.LENGTH_SHORT).show();
    }

    private boolean scheduleSingleAlarm(Event event, int requestCode, AlarmManager alarmManager) {
        try {
            SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
            int leadTime = prefs.getInt(SettingsActivity.LEAD_TIME_KEY, 10);

            String date = event.getDate();
            String time = event.getTime();
            String normalizedTime = time.replace(" ", "").replace(".", "").toLowerCase();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mma", Locale.US);
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTime(sdf.parse(date + " " + normalizedTime));
            eventCal.add(Calendar.MINUTE, -leadTime);

            if (eventCal.getTimeInMillis() <= System.currentTimeMillis()) return false;

            Intent intent = new Intent(this, EventReceiver.class);
            intent.putExtra("eventTitle", event.getTitle());
            intent.putExtra("eventTime", event.getTime());
            intent.putExtra("eventImpact", event.getImpact());
            intent.putExtra("eventCurrency", event.getCurrency());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, eventCal.getTimeInMillis(), pendingIntent);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling alarm", e);
            return false;
        }
    }

    // ---------------- Helpers ----------------

    private void showFilterDialog() {
        final String[] options = {"All", "High", "Medium", "Low", "Holiday"};
        new AlertDialog.Builder(this)
                .setTitle("Filter by Impact")
                .setItems(options, (dialog, which) -> {
                    if (eventAdapter != null)
                        eventAdapter.filterByImpact(options[which]);
                })
                .show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "eventChannel",
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for Forex Event Reminders");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
