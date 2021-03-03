package com.intelligentsoftwaresdev.bankapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;

import android.util.Log;
import android.widget.ProgressBar;

import com.intelligentsoftwaresdev.bankapp.R;

public class SplashActivity extends AppCompatActivity {

    ProgressBar splashProgress;
    int SPLASH_TIME = 1000; //This is 1 seconds
    private static final String TAG = "Splash";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.e(TAG, "onCreate: " );
        //This is additional feature, used to run a progress bar
        splashProgress = findViewById(R.id.splashProgress);
        playProgress();

        //Code to start timer and take action after the timer ends
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do any action here. Now we are moving to next page
                Intent mySuperIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mySuperIntent);

                //This 'finish()' is for exiting the app when back button pressed from Home page which is ActivityHome
                finish();

            }
        }, SPLASH_TIME);
    }

    //Method to run progress bar for 5 seconds
    private void playProgress() {
        ObjectAnimator.ofInt(splashProgress, "progress", 100)
                .setDuration(5000)
                .start();
    }
}
