package com.example.q5digitalmarketplace;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPhone, etPassword;
    private MaterialButton btnSignUp;
    private DatabaseHelper dbHelper;

    // Executor for running the database insertion in the background
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler bound to the main looper for safe UI updates
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // Single Toast instance to prevent notification queue flooding
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        // Bind layout views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnSignUp.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // 1. Validate complete field inputs
            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                showToast("Please fill all fields");
            } else {
                // Disable button immediately to prevent double-clicks during processing
                btnSignUp.setEnabled(false);

                // Push the database transaction to a background thread
                executorService.execute(() -> {
                    // 2. Inserts into the database via your helper method
                    boolean success = dbHelper.addUser(username, email, phone, password);

                    // Post the result and UI modifications back to the Main (UI) Thread
                    mainHandler.post(() -> {
                        // Re-enable the button once processing is complete
                        btnSignUp.setEnabled(true);

                        if (success) {
                            showToast("Registration Successful");
                            // Clean navigation back to Login screen context
                            finish();
                        } else {
                            showToast("Registration Failed or Email Already Taken");
                        }
                    });
                });
            }
        });

        tvLogin.setOnClickListener(v -> {
            // Dismisses signup activity context and reverts interface display to LoginActivity
            finish();
        });
    }

    // Helper method to safely manage and clear Toast notifications
    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown executor when activity is destroyed to avoid memory leaks
        executorService.shutdown();
    }
}