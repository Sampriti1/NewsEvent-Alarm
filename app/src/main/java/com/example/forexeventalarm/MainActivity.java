package com.example.forexeventalarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> currentEvents;
    private AlarmScheduler alarmScheduler; // <-- The new scheduler

    // A single launcher to handle returning from the Settings screen
    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // When we return from settings, the user might have changed their preferences.
                // It's a good practice to reschedule all alarms to reflect the new settings.
                Log.d(TAG, "Returned from settings. Rescheduling alarms if necessary...");
                if (currentEvents != null && !currentEvents.isEmpty()) {
                    scheduleAlarmsForEvents(currentEvents);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) fetchEvents();
                else Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the scheduler
        alarmScheduler = new AlarmScheduler(this);

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
        askForNotificationPermission();
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                fetchEvents();
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
                    currentEvents = response.body();
                    eventAdapter = new EventAdapter(currentEvents);
                    recyclerView.setAdapter(eventAdapter);
                    // Schedule alarms with the freshly fetched data
                    scheduleAlarmsForEvents(currentEvents);
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

    /**
     * This method is now very simple. It just tells the AlarmScheduler what to do.
     */
    private void scheduleAlarmsForEvents(List<Event> events) {
        // First, cancel all previously scheduled alarms to prevent duplicates
        alarmScheduler.cancelAllAlarms();
        // Then, schedule new alarms based on the latest data and settings
        int scheduledCount = alarmScheduler.scheduleGroupedAlarms(events);
        Toast.makeText(this, scheduledCount + " events scheduled.", Toast.LENGTH_SHORT).show();
    }

    private void showFilterDialog() {
        // Your existing filter dialog code (no changes needed)
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
        // Your existing notification channel code (no changes needed)
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