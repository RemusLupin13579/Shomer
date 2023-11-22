package com.project.shomer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TimerService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to interact with bound clients.
        return null;
    }

    private static final String TAG = "TimerService";
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this, "TIMER_CHANNEL_ID");



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mTimeLeftInMillis = intent.getLongExtra("time_left", 21600000); // 6 hours in milliseconds

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateNotification("Time remaining: " + formatTime(mTimeLeftInMillis));
            }

            @Override
            public void onFinish() {
                mNotificationManager.cancel(1);
                stopSelf();
            }
        }.start();

        startForeground(1, buildNotification());
        return START_STICKY;
    }

    private void updateNotification(String text) {

        SharedPreferences preferences = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        // Retrieve estimated finish time from shared preferences
        String estimatedFinishTimeMillis = preferences.getString("endtime", "00:00");

        mNotificationBuilder.setContentTitle(text);
        mNotificationBuilder.setContentText("Estimated hour: " + estimatedFinishTimeMillis);
        mNotificationManager.notify(1, mNotificationBuilder.build());
    }

    private Notification buildNotification() {

        return mNotificationBuilder
                .setContentTitle("Timer Running")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setOngoing(true)
                .build();
    }

    private String formatTime(long millis) {
        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int seconds = (int) ((millis / 1000) % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

}
