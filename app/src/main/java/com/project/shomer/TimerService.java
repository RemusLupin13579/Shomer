package com.project.shomer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class TimerService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to interact with bound clients.
        return null;
    }

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
                // Build and show a notification when the timer finishes
                buildAndShowEndNotification();
                mNotificationManager.cancel(1);
                stopSelf();
            }
        }.start();

        startForeground(1, buildNotification());
        return START_STICKY;
    }

    private void sendCountdownUpdate(long timeLeftInMillis) {
        Intent intent = new Intent("com.project.shomer.COUNTDOWN_UPDATE");
        intent.putExtra("timeLeftInMillis", timeLeftInMillis);
        sendBroadcast(intent);
    }

    private void updateNotification(String text) {
        // Create an Intent to launch your desired activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create a PendingIntent with FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );
        SharedPreferences preferences = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        // Retrieve estimated finish time from shared preferences
        String estimatedFinishTimeMillis = preferences.getString("endtime", "00:00");

        mNotificationBuilder.setContentTitle(text)
                .setContentText("Estimated hour: " + estimatedFinishTimeMillis)
                .setContentIntent(pendingIntent)  // Set the PendingIntent
                .setAutoCancel(true);  // Auto-cancel the notification when clicked
        // Notify with a unique ID
        mNotificationManager.notify(1, mNotificationBuilder.build());

        // Send countdown update to MainActivity
        sendCountdownUpdate(mTimeLeftInMillis);
    }

    private void buildAndShowEndNotification() {
        // Build a notification for the end of the timer
        NotificationCompat.Builder endNotificationBuilder = new NotificationCompat.Builder(this, "timer_channel_id")
                .setContentTitle("הגיע הזמן")
                .setContentText("קדימה שוקולד!")
                .setSmallIcon(R.drawable.pizzatime)
                .setAutoCancel(true);

        // Create an Intent to launch your desired activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create a PendingIntent with FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        endNotificationBuilder.setContentIntent(pendingIntent);

        // Notify with a unique ID
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(2, endNotificationBuilder.build());
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
