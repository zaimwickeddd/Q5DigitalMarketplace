package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private DatabaseHelper dbHelper;

    // Executor for running the database query in the background
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler bound to the main looper for safe UI updates
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // Single Toast instance to prevent notification queue flooding
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please enter email and password");
            } else {
                // Disable button immediately to prevent double-clicks during processing
                btnLogin.setEnabled(false);

                // Push the database verification to a background thread
                executorService.execute(() -> {
                    boolean isValid = dbHelper.checkUser(email, password);

                    // Post the result and UI modifications back to the Main (UI) Thread
                    mainHandler.post(() -> {
                        // Re-enable the button once processing is complete
                        btnLogin.setEnabled(true);

                        if (isValid) {
                            // Save email to SharedPreferences for use in fragments
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                            prefs.edit().putString("user_email", email).apply();

                            showToast("Login Successful");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showToast("Invalid email or password");
                        }
                    });
                });
            }
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    // Helper method to safely manage and clear Toast notifications
    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown executor when activity is destroyed to avoid memory leaks
        executorService.shutdown();
    }
}