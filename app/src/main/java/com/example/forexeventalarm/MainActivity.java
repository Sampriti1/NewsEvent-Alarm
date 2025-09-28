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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.widget.ImageView;
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;

    // --- NEW: Modern way to handle asking for permissions ---
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action.
                    Log.d(TAG, "Notification permission granted.");
                    fetchEvents();
                } else {
                    // Explain to the user that the feature is unavailable.
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
            // When clicked, open the SettingsActivity
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        // 1. Create the notification channel (must be done early)
        createNotificationChannel();

        // 2. Setup the RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Ask for notification permission, which will then trigger fetchEvents()
        askForNotificationPermission();
        testNotification();
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

    // --- NEW: Handles asking for POST_NOTIFICATIONS on Android 13+ ---
    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted
                Log.d(TAG, "Notification permission already granted.");
                fetchEvents();
            } else {
                // Directly ask for the permission
                Log.d(TAG, "Requesting notification permission.");
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // No runtime permission needed for older versions
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

                    if (!events.isEmpty()) {
                        Event firstEvent = events.get(0);
                        // We are logging the raw date and time strings from the API
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

        // --- FIXED: Check for exact alarm permission before scheduling ---
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


    private boolean scheduleNotification(Event event, int requestCode, AlarmManager alarmManager) {
        try {
            // --- 1. Get the user's saved preference ---
            SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);

            int leadTime = prefs.getInt(SettingsActivity.LEAD_TIME_KEY, 30);

            // Combine date and time strings.
            String dateTimeString = event.getDate() + " " + event.getTime();
            // TO THIS (New format that matches your API):
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy h:mmaa", Locale.getDefault());
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTime(sdf.parse(dateTimeString));


            // The value will be negative because we are subtracting.
            eventCal.add(Calendar.MINUTE, -leadTime);

            // Only schedule if the calculated alarm time is still in the future
            if (eventCal.getTimeInMillis() > System.currentTimeMillis()) {
                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("eventTitle", event.getTitle());
                intent.putExtra("eventTime", event.getTime());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        requestCode, // Unique request code
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        eventCal.getTimeInMillis(),
                        pendingIntent
                );
                return true; // Successfully scheduled
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date/time for event: " + event.getTitle(), e);
        } catch (Exception e) {
            Log.e(TAG, "An unexpected error occurred while scheduling notification for: " + event.getTitle(), e);
        }
        return false; // Failed to schedule
    }
    private void testNotification() {
        //  It is optional and been done for testing
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // First, check if we have permission to schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please grant exact alarm permission first.", Toast.LENGTH_SHORT).show();
               ;
                return;
            }
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("eventTitle", "🔔 This is a Test Event");
        intent.putExtra("eventTime", "In 10 seconds");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                999, // Using a unique request code for the test
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set the alarm to fire in 10 seconds (10000 milliseconds)
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10000,
                pendingIntent
        );

        Log.d("MainActivity", "Test notification has been scheduled.");
        Toast.makeText(this, "Test notification will fire in 10 seconds.", Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This creates the menu icon in the toolbar
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // This handles the click on the menu icon
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
