package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class Player1Activity extends AppCompatActivity {
    private BoardGame boardGame;
    private FbModule fbModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        LinearLayout ll = findViewById(R.id.ll);
        boardGame = new BoardGame(this, true); // true for Player1
        ll.addView(boardGame);

        fbModule = new FbModule(new FbModule.GameStateListener() {
            @Override
            public void onPlayer1CardsChanged(ArrayList<Card> cards) {
                boardGame.updatePlayerCards(cards, true);

            }

            @Override
            public void onPlayer2CardsChanged(ArrayList<Card> cards) {
                boardGame.updatePlayerCards(cards, false);
            }

            @Override
            public void onPacketChanged(ArrayList<Card> cards) {
                boardGame.updatePacket(cards);
            }

            @Override
            public void onTurnChanged(String currentPlayer) {
                boardGame.setTurn(currentPlayer.equals("player1"));
            }

            @Override
            public void onPlayerScoreUpdated(String player, int score) {
                boardGame.updateScore(score);
            }

            @Override
            public void onBackgroundColorChanged(String color) {
                LinearLayout gameLayout = findViewById(R.id.gameLayout);
                switch (color) {
                    case "Blue":
                        gameLayout.setBackgroundColor(Color.BLUE);
                        break;
                    case "Red":
                        gameLayout.setBackgroundColor(Color.RED);
                        break;
                    case "Pink":
                        gameLayout.setBackgroundColor(0xFFF2ACB9);
                        break;
                    case "Yellow":
                        gameLayout.setBackgroundColor(Color.YELLOW);
                        break;
                    default:
                        gameLayout.setBackgroundColor(Color.WHITE);
                }
            }
        });
    }
}