package com.example.q5digitalmarketplace;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        MaterialButton btnLogin = findViewById(R.id.btnWelcomeLogin);
        MaterialButton btnSignup = findViewById(R.id.btnWelcomeSignup);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}