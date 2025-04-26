package com.example.orproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin, btnRegister;
    private TextView txtLoginStatus;
    private ProgressBar loginProgressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference userStatsRef;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        userStatsRef = FirebaseDatabase.getInstance().getReference("userStats");

        // Initialize UI elements
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        txtLoginStatus = findViewById(R.id.txtLoginStatus);
        loginProgressBar = findViewById(R.id.loginProgressBar);

        // Set click listeners
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, go to MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) return;

        // Show progress
        loginProgressBar.setVisibility(View.VISIBLE);
        txtLoginStatus.setText("Logging in...");

        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        txtLoginStatus.setText("Authentication failed");
                        if (task.getException() != null) {
                            txtLoginStatus.setText("Authentication failed: " + task.getException().getMessage());
                        }
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) return;

        // Show progress
        loginProgressBar.setVisibility(View.VISIBLE);
        txtLoginStatus.setText("Registering...");

        // Create user with Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Initialize user stats in Firebase
                        if (user != null) {
                            initializeUserStats(user.getUid());
                        }

                        Toast.makeText(LoginActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user
                        txtLoginStatus.setText("Registration failed");
                        if (task.getException() != null) {
                            txtLoginStatus.setText("Registration failed: " + task.getException().getMessage());
                        }
                        Toast.makeText(LoginActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        // Reset errors
        txtLoginStatus.setText("");

        // Check for empty fields
        if (TextUtils.isEmpty(email)) {
            txtLoginStatus.setText("Email cannot be empty");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            txtLoginStatus.setText("Password cannot be empty");
            return false;
        }

        // Validate email format
        if (!email.endsWith("@gmail.com")) {
            txtLoginStatus.setText("Email must end with @gmail.com");
            return false;
        }

        // Validate password length
        if (password.length() < 6) {
            txtLoginStatus.setText("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void initializeUserStats(String userId) {
        // Create initial stats for new user
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("completedQuartets", 0);
        userStats.put("gamesPlayed", 0);
        userStats.put("totalScore", 0);

        // Save to Firebase
        userStatsRef.child(userId).setValue(userStats);
    }
}