package com.example.q5digitalmarketplace;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Initialize Splash Screen API
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 2. Identify UI elements
        View logo = findViewById(R.id.iv_logo);
        View brandName = findViewById(R.id.tv_brand_name);
        View brandTagline = findViewById(R.id.tv_brand_tagline);
        View loadingIndicator = findViewById(R.id.loading_indicator);
        View footerNote = findViewById(R.id.tv_footer_note);

        // 3. Simple Sequential Entrance Animations
        
        // Logo Entrance: Fade and Scale Up
        if (logo != null) {
            logo.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(800)
                    .setInterpolator(new AnticipateOvershootInterpolator())
                    .setStartDelay(200)
                    .start();
        }

        // Brand Name: Fade In
        if (brandName != null) {
            brandName.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(500)
                    .start();
        }

        // Tagline: Fade In
        if (brandTagline != null) {
            brandTagline.animate()
                    .alpha(0.8f)
                    .setDuration(800)
                    .setStartDelay(800)
                    .start();
        }

        // Loading Indicator: Fade In
        if (loadingIndicator != null) {
            loadingIndicator.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .setStartDelay(1000)
                    .start();
        }

        // Footer: Fade In
        if (footerNote != null) {
            footerNote.animate()
                    .alpha(0.5f)
                    .setDuration(800)
                    .setStartDelay(1200)
                    .start();
        }

        // 4. Navigate to Login after a branded delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); 
        }, 3000);
    }
}
