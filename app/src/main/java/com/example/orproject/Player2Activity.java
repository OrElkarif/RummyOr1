package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

public class Player2Activity extends AppCompatActivity {
    // מופע של לוח המשחק עבור שחקן 2
    public BoardGame boardGame;
    // מודול פיירבייס
    private FbModule fbModule;
    // לייאאוט של מסך המשחק
    private LinearLayout gameLayout;
    // הצבע הנוכחי
    private String currentColor = "Blue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            // מציאת הלייאאוט הלינארי עבור לוח המשחק
            LinearLayout ll = findViewById(R.id.ll);
            // מציאת הלייאאוט של המשחק עבור צבע הרקע
            gameLayout = findViewById(R.id.gameLayout);

            // יצירת מופע חדש של לוח משחק עבור שחקן 2
            boardGame = new BoardGame(this, false); // false עבור שחקן 2
            // הוספת תצוגת לוח המשחק ללייאאוט
            ll.addView(boardGame);

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
            Log.e("Player2Activity", "שגיאה באתחול האקטיביטי: " + e.getMessage());
        }
    }

    // שיטה להגדרת צבע הרקע של האקטיביטי
    public void setBackgroundColor(String color) {
        // שמירת מצב הצבע האחרון
        currentColor = color;

        // שינוי צבע הרקע של המסך
        int colorValue;
        switch (color) {
            case "Blue": colorValue = Color.BLUE; break;
            case "Red": colorValue = Color.RED; break;
            case "Pink": colorValue = 0xFFF2ACB9; break;
            case "Yellow": colorValue = Color.YELLOW; break;
            default: colorValue = Color.WHITE;
        }

        // עדכון הלייאאוט
        gameLayout.setBackgroundColor(colorValue);

        // עדכון לוח המשחק (אך בלי שהלוח יעדכן שוב את הצבע ב-Firebase)
        if (boardGame != null) {
            boardGame.setBackgroundColor(color);
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