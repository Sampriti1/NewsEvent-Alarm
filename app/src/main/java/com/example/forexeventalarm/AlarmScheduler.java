package com.example.forexeventalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.example.forexeventalarm.model.Event;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";
    private final Context context;
    private final AlarmManager alarmManager;
    private final SharedPreferences prefs;

    public AlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Groups events by time, and schedules a single alarm for each group.
     * @param events The full list of events from the API.
     * @return The total number of events that were successfully scheduled.
     */
    public int scheduleGroupedAlarms(List<Event> events) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Cannot schedule exact alarms, permission not granted.");
            return 0;
        }

        // 1. Load user's filter preferences
        Set<String> selectedCurrencies = prefs.getStringSet(SettingsActivity.SELECTED_CURRENCIES_KEY, new HashSet<>());
        Set<String> selectedImpacts = prefs.getStringSet(SettingsActivity.SELECTED_IMPACTS_KEY, new HashSet<>());
        int leadTime = prefs.getInt(SettingsActivity.LEAD_TIME_KEY, 10);

        // 2. Create a Map to group events by their trigger time
        Map<Long, List<Event>> groupedEvents = new HashMap<>();

        // 3. Loop through all events to filter and group them
        for (Event event : events) {
            // Skip if event doesn't match currency or impact filters
            if (!selectedCurrencies.contains(event.getCurrency()) || !selectedImpacts.contains(event.getImpact())) {
                continue;
            }

            try {
                long triggerTime = calculateTriggerTime(event, leadTime);

                if (triggerTime > System.currentTimeMillis()) {
                    if (!groupedEvents.containsKey(triggerTime)) {
                        groupedEvents.put(triggerTime, new ArrayList<>());
                    }
                    groupedEvents.get(triggerTime).add(event);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Failed to parse date for event: " + event.getTitle(), e);
            }
        }

        // 4. Schedule one alarm for each group of events
        Gson gson = new Gson();
        int scheduledCount = 0;
        for (Map.Entry<Long, List<Event>> entry : groupedEvents.entrySet()) {
            long triggerTime = entry.getKey();
            List<Event> eventGroup = entry.getValue();

            Intent intent = new Intent(context, EventReceiver.class);
            String eventsJson = gson.toJson(eventGroup);
            intent.putExtra("eventsJson", eventsJson);

            int requestCode = (int) (triggerTime / 1000);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            scheduledCount += eventGroup.size();
        }

        Log.d(TAG, "Scheduling complete. Groups: " + groupedEvents.size() + ". Total events: " + scheduledCount);
        return scheduledCount;
    }

    /**
     * Cancels all previously scheduled alarms.
     * It's good practice to call this before scheduling new alarms.
     */
    public void cancelAllAlarms() {
        // To cancel, we need to recreate the same PendingIntent that was used to set the alarm.
        // A robust way to do this is to keep track of the request codes used.
        // A simpler, brute-force way for now is to assume a max number of alarms.
        Log.d(TAG, "Canceling all previous alarms...");
        for (int i = 0; i < 500; i++) { // Assuming max 500 potential alarms
            Intent intent = new Intent(context, EventReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, i, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * Helper method to calculate the precise trigger time for an event.
     */
    private long calculateTriggerTime(Event event, int leadTime) throws ParseException {
        String date = event.getDate();
        String time = event.getTime();
        String normalizedTime = time.replace(" ", "").replace(".", "").toLowerCase();
        String finalDateTimeString = date + " " + normalizedTime;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mma", Locale.US);
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(sdf.parse(finalDateTimeString));
        eventCal.add(Calendar.MINUTE, -leadTime);
        return eventCal.getTimeInMillis();
    }
}
