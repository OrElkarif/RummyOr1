package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private ArrayList card[];
    private Button btnAgainstPlayerCards;
    private LinearLayout linearLayout1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        BoardGame boardGame = new BoardGame(this, true);

        LinearLayout ll = findViewById(R.id.ll);

        ll.addView(boardGame);
        linearLayout1 = findViewById(R.id.gameLayout);

        linearLayout1.setBackgroundColor(Color.WHITE);

        FbModule fbModule = new FbModule(new FbModule.GameStateListener() {
            // Implement other required methods...

            @Override
            public void onPlayer1CardsChanged(ArrayList<Card> cards) {

            }

            @Override
            public void onPlayer2CardsChanged(ArrayList<Card> cards) {

            }

            @Override
            public void onPacketChanged(ArrayList<Card> cards) {

            }

            @Override
            public void onTurnChanged(String currentPlayer) {

            }

            @Override
            public void onPlayerScoreUpdated(String player, int score) {

            }

            @Override
            public void onBackgroundColorChanged(String color) {
                setBackgroundColor(color);
            }
        });

    }
    // Add this to GameActivity.java
    public void setBackgroundColor(String color) {
        switch (color) {
            case "Blue":
                linearLayout1.setBackgroundColor(Color.BLUE);
                break;
            case "Red":
                linearLayout1.setBackgroundColor(Color.RED);
                break;
            case "Pink":
                linearLayout1.setBackgroundColor(0xFFF2ACB9);
                break;
            case "Yellow":
                linearLayout1.setBackgroundColor(Color.YELLOW);
                break;
            default:
                linearLayout1.setBackgroundColor(Color.WHITE);
        }
    }


    }