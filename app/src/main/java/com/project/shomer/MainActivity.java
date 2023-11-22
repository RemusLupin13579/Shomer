package com.project.shomer;

// MainActivity.java

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.project.shomer.CircularProgressView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    //private CircularProgressView circularProgressView;
    //private ProgressBar progressBar;
    //private CountDownTimer countDownTimer;
    //private long startTimeMillis;
    private Button startButton;
    private TextView startTimeTextView;
    private Spinner durationSpinner;

    private static long START_TIME_IN_MILLIS =  21600000;
    private TextView mTextViewCountDown;
    private Button mButtonReset;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;

    private long startTimeMillis;

    private long currentMinhag = 21600000;

    private static final String PREF_NAME = "TimerPrefs";
    private static final String KEY_START_TIME = "StartTime";
    private static final String KEY_ESTIMATED_FINISH_TIME = "EstimatedFinishTime";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        durationSpinner = findViewById(R.id.durationSpinner);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);

        startButton = findViewById(R.id.startButton);
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
                if(position == 2){
                    currentMinhag = 21600000;
                }
                if(position == 1){
                    currentMinhag =  10800000;
                }
                if(position == 0){
                    currentMinhag =  3600000;
                }
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

    private void startTimer() {
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
                startButton.setVisibility(View.INVISIBLE);
                mButtonReset.setVisibility(View.VISIBLE);
            }
        }.start();
        mTimerRunning = true;
        startButton.setText("Reset");
        // Start the service with time left
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra("time_left", mTimeLeftInMillis);
        startService(intent);
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
        // Convert milliseconds to a formatted time string (HH:mm)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date = new Date(millis);
        return sdf.format(date);
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        startButton.setText("Start");
        mButtonReset.setVisibility(View.VISIBLE);
    }

    private void updateStartTimeTextView() {
        String startTime = getTimeFromMillis(startTimeMillis);
        startTimeTextView.setText("הטיימר התחיל ב-" + startTime);
    }

    private void resetTimer() {
        stopService(new Intent(this, TimerService.class));
        mCountDownTimer.cancel();
        mTimerRunning = false;
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        startButton.setText("Start");
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private String getTimeFromMillis(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date date = new Date(millis);
        return sdf.format(date);
    }

    private long getMillisFromDuration(String duration) {
        int hours = Integer.parseInt(duration.split(" ")[0]);
        return TimeUnit.HOURS.toMillis(hours);
    }

    private void spinnerSettings() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.duration_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        // Set default duration to 6 hours
        int defaultDurationPosition = adapter.getPosition("6 שעות");
        durationSpinner.setSelection(defaultDurationPosition);
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

}
