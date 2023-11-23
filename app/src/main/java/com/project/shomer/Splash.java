package com.project.shomer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    private static int SPLASH_TIMER=2300;

    private VideoView videoView;

    private Handler handler = new Handler(Looper.getMainLooper());

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.project.shomer.R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        videoView = findViewById(R.id.videoView);

        // Set the path of the video file
        String videoPath = "android.resource://" + getPackageName() + "/raw/bg";

        // Set the Uri of the video file
        Uri videoUri = Uri.parse(videoPath);

        /*// Set up a media controller for video playback controls (optional)
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);*/

        // Set the Uri as the video source and start playing
        videoView.setVideoURI(videoUri);
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "Video Path: " + videoPath);
                Log.e(TAG, "Error during video playback. What: " + what + ", Extra: " + extra);
                return false; // Returning false indicates that the error was not handled and playback should not be stopped.
            }
        });
        videoView.start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToMainActivity();
                finish();
            }
        },SPLASH_TIMER);

    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(Splash.this, MainActivity.class);
        startActivity(intent);
        finishAfterTransition();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video playback when the activity is paused
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video playback when the activity is resumed
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop video playback and release resources when the activity is destroyed
        if (videoView != null) {
            videoView.stopPlayback();
        }
        // Remove any pending callbacks to avoid memory leaks
        handler.removeCallbacksAndMessages(null);
    }
}