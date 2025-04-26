package com.example.orproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AchievementsActivity extends AppCompatActivity {

    private TextView tvRummiesCompleted;
    private TextView tvGamesPlayed;
    private TextView tvTotalScore;
    private RadioButton radioButtonYes;
    private RadioButton radioButtonNo;
    private Button btnAction;
    private FirebaseAuth mAuth;
    private DatabaseReference userStatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // No user is signed in, redirect to login
            Toast.makeText(this, "Please log in to view achievements", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AchievementsActivity.this, LoginActivity.class));
            finish();
            return;
        }

        userStatsRef = FirebaseDatabase.getInstance().getReference("userStats").child(currentUser.getUid());

        // Initialize UI elements
        tvRummiesCompleted = findViewById(R.id.tvRummiesCompleted);
        tvGamesPlayed = findViewById(R.id.tvGamesPlayed);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        radioButtonYes = findViewById(R.id.radioButtonYes);
        radioButtonNo = findViewById(R.id.radioButtonNo);
        btnAction = findViewById(R.id.btnAction);

        // Set button click listener
        btnAction.setOnClickListener(v -> {
            if (radioButtonYes.isChecked()) {
                // Start a new game
                startActivity(new Intent(AchievementsActivity.this, PreGameActivity.class));
            } else {
                // Go back to main menu
                startActivity(new Intent(AchievementsActivity.this, MainActivity.class));
            }
            finish();
        });

        // Load user stats
        loadUserStats();
    }

    private void loadUserStats() {
        userStatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get user stats
                    Integer completedQuartets = dataSnapshot.child("completedQuartets").getValue(Integer.class);
                    Integer gamesPlayed = dataSnapshot.child("gamesPlayed").getValue(Integer.class);
                    Integer totalScore = dataSnapshot.child("totalScore").getValue(Integer.class);

                    // Update UI
                    if (completedQuartets != null) {
                        tvRummiesCompleted.setText("Amount of Rummies completed: " + completedQuartets);
                    }

                    if (gamesPlayed != null) {
                        tvGamesPlayed.setText("Amount of turns: " + gamesPlayed);
                    }

                    if (totalScore != null) {
                        tvTotalScore.setText("Score: " + totalScore);
                    }
                } else {
                    // User stats don't exist yet, initialize them
                    initializeUserStats();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AchievementsActivity.this, "Failed to load achievements", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeUserStats() {
        userStatsRef.child("completedQuartets").setValue(0);
        userStatsRef.child("gamesPlayed").setValue(0);
        userStatsRef.child("totalScore").setValue(0);

        // Show default values
        tvRummiesCompleted.setText("Amount of Rummies completed: 0");
        tvGamesPlayed.setText("Amount of turns: 0");
        tvTotalScore.setText("Score: 0");
    }
}