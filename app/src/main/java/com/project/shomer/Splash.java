package com.project.shomer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    private static int SPLASH_TIMER=2300;

    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.project.shomer.R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        videoView = findViewById(R.id.videoView);

        // Set the path of the video file
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.bg;

        // Set the Uri of the video file
        Uri videoUri = Uri.parse(videoPath);

        // Set up a media controller for video playback controls (optional)
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Set the Uri as the video source and start playing
        videoView.setVideoURI(videoUri);
        videoView.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent=new Intent(Splash.this,MainActivity.class);
                startActivity(intent);
                finish();


            }
        },SPLASH_TIMER);

    }
}