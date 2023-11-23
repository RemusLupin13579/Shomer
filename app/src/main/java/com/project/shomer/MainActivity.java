package com.project.shomer;

// MainActivity.java

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private TextView startTimeTextView;
    private Spinner durationSpinner;

    private static long START_TIME_IN_MILLIS =  21600000;
    private TextView mTextViewCountDown;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;

    private long startTimeMillis;

    private long currentMinhag = 21600000;

    private static final String PREF_NAME = "TimerPrefs";
    private static final String KEY_START_TIME = "StartTime";
    private static final String KEY_ESTIMATED_FINISH_TIME = "EstimatedFinishTime";

    private BroadcastReceiver countdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null &&
                    intent.getAction().equals("com.project.shomer.COUNTDOWN_UPDATE")) {
                mTimerRunning = true;
                startButton.setText("Reset");
                long timeLeftInMillis = intent.getLongExtra("timeLeftInMillis", 21600000);
                // Update your UI with the new countdown value
                updateCountdownUI(timeLeftInMillis);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        IntentFilter filter = new IntentFilter("com.project.shomer.COUNTDOWN_UPDATE");
        registerReceiver(countdownReceiver, filter);

        durationSpinner = findViewById(R.id.durationSpinner);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);

        startButton = findViewById(R.id.startButton);
        // Set the background color of the button
        startButton.setBackgroundResource(R.drawable.roundedbutton); // Replace with your drawable resource
        startButton.getBackground().setColorFilter(getResources().getColor(R.color.my_light_button_background), PorterDuff.Mode.SRC_ATOP);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    resetTimer();
                } else {
                    startTimer();
                }
            }
        });

        spinnerSettings();
        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                // Update the timer duration when a spinner item is selected
                updateTimerDuration();

                if(position == 2){
                    currentMinhag = 21600000;
                }
                if(position == 1){
                    currentMinhag =  10800000;
                }
                if(position == 0){
                    currentMinhag =  3600000;
                }
                // Reset the timer with the new duration
                resetTimer();

                // Update UI
                mTimeLeftInMillis = currentMinhag;
                updateCountDownText();
                displayStartAndFinishTimes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        mTimeLeftInMillis = currentMinhag;
        updateCountDownText();
        displayStartAndFinishTimes();
    }

    private void updateCountdownUI(long timeLeftInMillis) {
        int hours = (int) (timeLeftInMillis / (1000 * 60 * 60));
        int minutes = (int) ((timeLeftInMillis / (1000 * 60)) % 60);
        int seconds = (int) ((timeLeftInMillis / 1000) % 60);

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        // Update your UI element (e.g., a TextView) with the new countdown value
        mTextViewCountDown.setText(timeLeftFormatted);
    }


    private void startTimer() {
        Toast.makeText(getApplicationContext(), "לא לשכוח ברכה אחרונה", Toast.LENGTH_SHORT).show();
        saveStartTime();
        saveEstimatedFinishTime();
        displayStartAndFinishTimes();
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                startTimeMillis = System.currentTimeMillis();
                //updateStartTimeTextView();
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                mTimerRunning = false;
                startButton.setText("Start");
                // Show a notification
                showNotification("הגיע הזמן", "קדימה שוקולד!");
                Toast.makeText(getApplicationContext(), "FINISHED", Toast.LENGTH_SHORT).show();
            }
        }.start();
        mTimerRunning = true;
        startButton.setText("Reset");
        // Start the service with time left
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra("time_left", mTimeLeftInMillis);
        startService(intent);

    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if notification channels are supported (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("timer_channel_id", "Timer Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "timer_channel_id")
                .setSmallIcon(R.drawable.pizzatime)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

    private void saveStartTime() {
        // Save the current time as the start time in shared preferences
        long currentTimeMillis = System.currentTimeMillis();
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_START_TIME, currentTimeMillis);
        editor.apply();
    }

    private void saveEstimatedFinishTime() {
        // Save the estimated finish time (start time + 6 hours) in shared preferences
        long estimatedFinishTimeMillis = System.currentTimeMillis() + 21600000;
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_ESTIMATED_FINISH_TIME, estimatedFinishTimeMillis);
        editor.apply();
    }

    private void displayStartAndFinishTimes() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Retrieve start time from shared preferences
        long startTimeMillis = preferences.getLong(KEY_START_TIME, 0);

        // Retrieve estimated finish time from shared preferences
        long estimatedFinishTimeMillis = preferences.getLong(KEY_ESTIMATED_FINISH_TIME, 0);

        // Format the times for display
        String startTime = getTimeFromMillis(startTimeMillis);
        String estimatedFinishTime = getTimeFromMillisForDisplay(estimatedFinishTimeMillis);

        SharedPreferences endgame = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("endtime", estimatedFinishTime);
        editor.apply();

        // Display the times in the TextView
        startTimeTextView.setText("סיימת לאכול בשעה: " + startTime +
                "\nתהיו חלביים בשעה: " + estimatedFinishTime);
    }

    private String getTimeFromMillisForDisplay(long millis) {
        // Convert milliseconds to a formatted time string (hh:mm a)
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date date = new Date(millis);
        return sdf.format(date);
    }

    private void resetTimer() {
        stopService(new Intent(this, TimerService.class));
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mTimerRunning = false;
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        startButton.setText("Start");
    }

    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / (1000 * 60 * 60));
        int minutes = (int) ((mTimeLeftInMillis / (1000 * 60)) % 60);
        int seconds = (int) ((mTimeLeftInMillis / 1000) % 60);

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void updateTimerDuration() {
        // Retrieve the selected duration from the spinner
        String selectedDuration = durationSpinner.getSelectedItem().toString();

        // Convert the selected duration to milliseconds
        mTimeLeftInMillis = getMillisFromDuration(selectedDuration);

        // Update UI elements (e.g., text views) accordingly
        updateCountDownText();
    }

    private String getTimeFromMillis(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date date = new Date(millis);
        return sdf.format(date);
    }

    private void spinnerSettings() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.duration_options,
                R.layout.spinner_item_layout
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        // Set default duration to 6 hours
        int defaultDurationPosition = adapter.getPosition("6 שעות");
        durationSpinner.setSelection(defaultDurationPosition);

        // Update the timer duration based on the selected item
        updateTimerDuration();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "TIMER_CHANNEL_ID",
                    "TimerChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for displaying timer updates");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private long getMillisFromDuration(String duration) { //"6 שעות">millis
        int hours = Integer.parseInt(duration.split(" ")[0]);
        return TimeUnit.HOURS.toMillis(hours);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(countdownReceiver);
    }


}
