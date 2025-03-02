package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class PreGameActivity extends AppCompatActivity {
    private Button btnPlayer1;
    private Button btnPlayer2;
    private FbModule fbModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_game);

        btnPlayer1 = findViewById(R.id.btnPlayer1);
        btnPlayer2 = findViewById(R.id.btnPlayer2);

        fbModule = new FbModule(null);

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
}