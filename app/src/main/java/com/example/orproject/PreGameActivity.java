package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class PreGameActivity extends AppCompatActivity {
    private Button btnPlayer1;
    private Button btnPlayer2;
    private FbModule fbModule;
    private LinearLayout preGameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_game);

        // Find views
        preGameLayout = findViewById(R.id.preGameLayout); // Make sure this ID exists in your layout
        btnPlayer1 = findViewById(R.id.btnPlayer1);
        btnPlayer2 = findViewById(R.id.btnPlayer2);

        // Get background color from intent if available
        String backgroundColor = getIntent().getStringExtra("backgroundColor");
        if (backgroundColor != null) {
            setBackgroundColor(backgroundColor);
        }

        fbModule = new FbModule(new FbModule.GameStateListener() {
            @Override
            public void onPlayer1CardsChanged(ArrayList<Card> cards) {
                // Not needed for this activity
            }

            @Override
            public void onPlayer2CardsChanged(ArrayList<Card> cards) {
                // Not needed for this activity
            }

            @Override
            public void onPacketChanged(ArrayList<Card> cards) {
                // Not needed for this activity
            }

            @Override
            public void onTurnChanged(String currentPlayer) {
                // Not needed for this activity
            }

            @Override
            public void onPlayerScoreUpdated(String player, int score) {
                // Not needed for this activity
            }

            @Override
            public void onBackgroundColorChanged(String color) {
                setBackgroundColor(color);
            }
        });

        btnPlayer1.setOnClickListener(v -> {
            // Reset game state before starting
            fbModule.resetGame();

            // Set initial turn to player 1
            FbModule.updateTurn("player1");

            Intent intent = new Intent(PreGameActivity.this, Player1Activity.class);
            startActivity(intent);
        });

        btnPlayer2.setOnClickListener(v -> {
            Intent intent = new Intent(PreGameActivity.this, Player2Activity.class);
            startActivity(intent);
        });
    }

    // Method to set background color
    public void setBackgroundColor(String color) {
        if (preGameLayout != null) {
            switch (color) {
                case "Blue":
                    preGameLayout.setBackgroundColor(Color.BLUE);
                    break;
                case "Red":
                    preGameLayout.setBackgroundColor(Color.RED);
                    break;
                case "Pink":
                    preGameLayout.setBackgroundColor(0xFFF2ACB9);
                    break;
                case "Yellow":
                    preGameLayout.setBackgroundColor(Color.YELLOW);
                    break;
                default:
                    preGameLayout.setBackgroundColor(Color.WHITE);
            }
        }
    }
}