package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PreGameActivity extends AppCompatActivity {
    // אלמנטי ממשק המשתמש
    private Button btnPlayer1;
    private Button btnPlayer2;
    // לייאאוט של מסך טרום המשחק
    private LinearLayout preGameLayout;
    // הפניה ישירה למסד הנתונים של פיירבייס
    private DatabaseReference gameStateRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // טעינת הלייאאוט של מסך טרום המשחק
        setContentView(R.layout.activity_pre_game);

        try {
            // מציאת הלייאאוטים והכפתורים
            preGameLayout = findViewById(R.id.preGameLayout);
            btnPlayer1 = findViewById(R.id.btnPlayer1);
            btnPlayer2 = findViewById(R.id.btnPlayer2);

            // קבלת צבע רקע מהאינטנט אם זמין
            String backgroundColor = getIntent().getStringExtra("backgroundColor");
            if (backgroundColor != null) {
                // אם יש צבע רקע שהועבר, הגדר אותו
                setBackgroundColor(backgroundColor);
            }

            // יצירת הפניה ישירה לצומת gameState
            gameStateRef = FirebaseDatabase.getInstance().getReference("gameState");

            // האזנה לצבע רקע
            gameStateRef.child("backgroundColor").get().addOnSuccessListener(snapshot -> {
                String color = snapshot.getValue(String.class);
                if (color != null) {
                    setBackgroundColor(color);
                }
            });

            // הגדרת כפתור שחקן 1
            btnPlayer1.setOnClickListener(v -> {
                try {
                    // איפוס מצב המשחק לפני ההתחלה - ללא שימוש ב-FbModule
                    gameStateRef.child("player1Cards").setValue(null);
                    gameStateRef.child("player2Cards").setValue(null);
                    gameStateRef.child("packet").setValue(null);
                    gameStateRef.child("playerScores").child("player1").setValue(0);
                    gameStateRef.child("playerScores").child("player2").setValue(0);

                    // הגדרת התור ההתחלתי לשחקן 1
                    gameStateRef.child("currentTurn").setValue("player1");

                    // מתן זמן קצר לפיירבייס לעדכן
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // התעלם משגיאות השהייה
                    }

                    // התחלת אקטיביטי של שחקן 1
                    Intent intent = new Intent(PreGameActivity.this, Player1Activity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("PreGameActivity", "שגיאה בלחיצה על כפתור שחקן 1: " + e.getMessage());
                }
            });

            // הגדרת כפתור שחקן 2
            btnPlayer2.setOnClickListener(v -> {
                try {
                    // התחלת אקטיביטי של שחקן 2
                    Intent intent = new Intent(PreGameActivity.this, Player2Activity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("PreGameActivity", "שגיאה בלחיצה על כפתור שחקן 2: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("PreGameActivity", "שגיאה כללית באתחול האקטיביטי: " + e.getMessage());
        }
    }

    // שיטה להגדרת צבע הרקע
    public void setBackgroundColor(String color) {
        try {
            // בדיקה שהלייאאוט לא null
            if (preGameLayout != null) {
                // בחירת הצבע המתאים לפי הערך שהתקבל
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
        } catch (Exception e) {
            Log.e("PreGameActivity", "שגיאה בהגדרת צבע רקע: " + e.getMessage());
        }
    }
}