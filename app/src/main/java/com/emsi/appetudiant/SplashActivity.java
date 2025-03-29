package com.emsi.appetudiant;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Duration of the splash screen in milliseconds
    private static final int SPLASH_DURATION = 4000; // 4 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide the action bar for fullscreen experience
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Find views to animate
        ImageView logoImageView = findViewById(R.id.splashLogo);
        TextView appNameTextView = findViewById(R.id.splashAppName);
        TextView taglineTextView = findViewById(R.id.splashTagline);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        // Apply animations
        logoImageView.startAnimation(fadeIn);
        appNameTextView.startAnimation(slideInRight);
        taglineTextView.startAnimation(slideUp);

        // Delay for SPLASH_DURATION and then start MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the splash activity so it's not in the back stack

            // Add a transition animation
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, SPLASH_DURATION);
    }
}
