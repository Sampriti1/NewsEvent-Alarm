package com.example.forexeventalarm;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;
import com.example.forexeventalarm.adapter.EventAdapter;
import com.example.forexeventalarm.model.Event;
import com.example.forexeventalarm.network.ApiClient;
import com.example.forexeventalarm.network.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.SharedPreferences;
import android.provider.Settings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> currentEvents;

    // --- Permission launcher ---

    private final ActivityResultLauncher<Intent> overlayPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "Permission granted. Alarms will now appear full-screen.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Permission denied. Alarms will show as notifications instead.", Toast.LENGTH_LONG).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted.");
                    fetchEvents();
                } else {
                    Log.d(TAG, "Notification permission denied.");
                    Toast.makeText(this, "Permission denied. Notifications will not work.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        settingsIcon.setOnClickListener(v -> {
            // The new code calls the launcher you created earlier
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });

        // --- Filter button ---
        ImageView filterBtn = findViewById(R.id.filterBtn);
        filterBtn.setOnClickListener(v -> showFilterDialog());

        // --- Setup notifications & recyclerView ---
        createNotificationChannel();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkOverlayPermission();
        askForNotificationPermission();
        //testNotification();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "eventChannel",
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for Forex Event reminders");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created.");
            }
        }
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission already granted.");
                fetchEvents();
            } else {
                Log.d(TAG, "Requesting notification permission.");
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            fetchEvents();
        }
    }

    private void fetchEvents() {
        Log.d(TAG, "Fetching events from API...");


       ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Event>> call = apiService.getEvents();

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched " + response.body().size() + " events.");
                    List<Event> events = response.body();
                    MainActivity.this.currentEvents = events;
                    if (!events.isEmpty()) {
                        Event firstEvent = events.get(0);
                        Log.d("API_DATA_CHECK", "Date from API: '" + firstEvent.getDate() + "'");
                        Log.d("API_DATA_CHECK", "Time from API: '" + firstEvent.getTime() + "'");
                    }
                    eventAdapter = new EventAdapter(events);
                    recyclerView.setAdapter(eventAdapter);
                    scheduleAlarmsForEvents(events);
                } else {
                    Log.e(TAG, "API Response not successful. Code: " + response.code());
                    Toast.makeText(MainActivity.this, "No data found or server error.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e(TAG, "API call failed.", t);
                Toast.makeText(MainActivity.this, "Failed to load events: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    private void scheduleAlarmsForEvents(List<Event> events) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. Sending user to settings.");
                Toast.makeText(this, "Please grant permission to schedule alarms.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        int scheduledCount = 0;
        for (int i = 0; i < events.size(); i++) {
            if (scheduleNotification(events.get(i), i, alarmManager)) {
                scheduledCount++;
            }
        }
        Log.d(TAG, "Finished scheduling. Total alarms set: " + scheduledCount);
        Toast.makeText(this, "Events loaded and " + scheduledCount + " alarms set.", Toast.LENGTH_SHORT).show();
    }
    // In MainActivity.java

    private boolean scheduleNotification(Event event, int requestCode, AlarmManager alarmManager) {
        try {
            SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
            int leadTime = prefs.getInt(SettingsActivity.LEAD_TIME_KEY, 10);




            String date = event.getDate();
            String time = event.getTime();


            String normalizedTime = time
                    .replace(" ", "")
                    .replace(".", "")
                    .toLowerCase();



            String finalDateTimeString = date + " " + normalizedTime;


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mma", Locale.US);

            Calendar eventCal = Calendar.getInstance();


            eventCal.setTime(sdf.parse(finalDateTimeString));



            eventCal.add(Calendar.MINUTE, -leadTime);

            if (eventCal.getTimeInMillis() > System.currentTimeMillis()) {
                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("eventTitle", event.getTitle());
                intent.putExtra("eventTime", event.getTime());
                      // <-- ADD THIS LINE
                intent.putExtra("eventImpact", event.getImpact());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        eventCal.getTimeInMillis(),
                        pendingIntent
                );

                Log.d("AlarmScheduler", "SUCCESS: Alarm set for event: " + event.getTitle());
                return true;
            } else {

                Log.w("AlarmScheduler", "SKIPPED (In Past): Alarm for event '" + event.getTitle() + "' was not set because its calculated time is in the past.");
                return false;
            }

        } catch (ParseException e) {

            Log.e("AlarmScheduler", "PARSE FAILED for event: '" + event.getTitle() + "'. Raw DateTime: '" + event.getDate() + " " + event.getTime() + "'", e);
        } catch (Exception e) {
            Log.e("AlarmScheduler", "Unexpected error for event: " + event.getTitle(), e);
        }
        return false;
    }

    private void showFilterDialog() {
        final String[] options = {"All", "High", "Medium", "Low", "Holiday"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Filter by Impact")
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if (eventAdapter != null) {
                        eventAdapter.filterByImpact(selected);
                    }
                })
                .show();
    }

    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // This code runs when you return from SettingsActivity
                Log.d(TAG, "Returned from settings. Rescheduling alarms...");
                if (currentEvents != null && !currentEvents.isEmpty()) {
                    // Reschedule all alarms with the new lead time from SharedPreferences
                    scheduleAlarmsForEvents(currentEvents);
                   // Toast.makeText(this, "Settings updated. Alarms rescheduled.", Toast.LENGTH_SHORT).show();
                }
            });

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // If permission is not granted, show a dialog to explain why it's needed
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("To ensure alarms appear on screen immediately, please grant the 'Display over other apps' permission.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            overlayPermissionLauncher.launch(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(this, "Permission denied. Alarms will show as notifications.", Toast.LENGTH_LONG).show();
                        })
                        .show();
            }
        }
    }
    private void testNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please grant exact alarm permission first.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("eventTitle", "🔔 This is a Test Event");
        intent.putExtra("eventTime", "In 10 seconds");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10000,
                pendingIntent
        );

        Log.d("MainActivity", "Test notification scheduled.");
        Toast.makeText(this, "Test notification will fire in 10 seconds.", Toast.LENGTH_SHORT).show();
    }
}
