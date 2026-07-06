package com.example.q5digitalmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        // Bind input and button views matching your layout design
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // 1. Validation check for blank input fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Authenticate credentials with the SQLite database
            boolean loginSuccess = dbHelper.checkUser(email, password);

            if (loginSuccess) {
                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                // 3. Navigate to MainActivity and embed the user email as a dynamic session parameter
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}