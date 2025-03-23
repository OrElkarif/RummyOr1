package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private ArrayList card[];
    private Button btnAgainstPlayerCards;
    private LinearLayout linearLayout1;
    // מודול פיירבייס
    private FbModule fbModule;
    // מופע של לוח המשחק
    private BoardGame boardGame;
    // הצבע הנוכחי
    private String currentColor = "Blue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            // יצירת מופע חדש של לוח המשחק
            boardGame = new BoardGame(this, true);

            // מציאת הלייאאוט הלינארי עבור לוח המשחק
            LinearLayout ll = findViewById(R.id.ll);
            ll.addView(boardGame);

            // מציאת הלייאאוט של המשחק עבור צבע הרקע
            linearLayout1 = findViewById(R.id.gameLayout);
            linearLayout1.setBackgroundColor(Color.WHITE);

            // אתחול מודול פיירבייס עם האקטיביטי הנוכחית
            fbModule = new FbModule(this);

            // בדיקה אם יש צבע שמור בפיירבייס
            fbModule.gameStateRef.child("backgroundColor").get().addOnSuccessListener(snapshot -> {
                String color = snapshot.getValue(String.class);
                if (color != null) {
                    // עדכון הצבע הנוכחי
                    currentColor = color;
                    // עדכון הלייאאוט
                    setBackgroundColor(color);
                    // עדכון ה-Canvas
                    if (boardGame != null) {
                        boardGame.setBackgroundColor(color);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("GameActivity", "שגיאה באתחול האקטיביטי: " + e.getMessage());
        }
    }

    // שיטה להגדרת צבע הרקע של האקטיביטי
    public void setBackgroundColor(String color) {
        try {
            // שמירת הצבע הנוכחי
            currentColor = color;

            // הגדרת צבע הרקע של הלייאאוט
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

            // עדכון ה-Canvas
            if (boardGame != null) {
                boardGame.setBackgroundColor(color);
            }
        } catch (Exception e) {
            Log.e("GameActivity", "שגיאה בהגדרת צבע רקע: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // עדכון צבע הרקע בעת חזרה לאקטיביטי
        if (boardGame != null) {
            boardGame.setBackgroundColor(currentColor);
        }
    }
}