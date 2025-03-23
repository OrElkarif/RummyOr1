package com.example.orproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialog {
    private Dialog dialog; // הצהרה על משתנה מסוג Dialog שישמש כדיאלוג המותאם
    private Context context; // הצהרה על משתנה מסוג Context לקישור עם האקטיביטי המארחת

    // בנאי המחלקה - מקבל קונטקסט של האקטיביטי המארחת
    public CustomDialog(Context context) {
        this.context = context; // שמירת הקונטקסט שהתקבל במשתנה של המחלקה
        dialog = new Dialog(context); // יצירת דיאלוג חדש עם הקונטקסט שהתקבל
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // בקשה להציג את הדיאלוג ללא כותרת
        dialog.setContentView(R.layout.activity_custom_dialog); // הגדרת הלייאאוט של הדיאלוג
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // הגדרת רקע שקוף לחלון הדיאלוג
        dialog.setCancelable(false); // מניעת סגירת הדיאלוג בלחיצה מחוץ לו
    }

    // פונקציה להצגת דיאלוג המנצח - מקבלת שם המנצח והניקוד
    public void showWinnerDialog(String winner, int score) {
        TextView tvWinner = dialog.findViewById(R.id.tvWinner); // מציאת רכיב הטקסט שיציג את המנצח
        TextView tvScore = dialog.findViewById(R.id.tvScore); // מציאת רכיב הטקסט שיציג את הניקוד
        Button btnFinishGame = dialog.findViewById(R.id.btnFinishGame); // מציאת כפתור הסיום

        tvWinner.setText(winner + " Wins!"); // הגדרת הטקסט של המנצח
        tvScore.setText("Final Score: " + score); // הגדרת הטקסט של הניקוד הסופי

        // הגדרת מאזין לחיצה לכפתור הסיום
        btnFinishGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // סגירת הדיאלוג

                // חזרה לתפריט הראשי
                Intent intent = new Intent(context, MainActivity.class); // יצירת כוונה (Intent) לפתיחת המסך הראשי
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // הגדרת דגל לניקוי כל המסכים הקודמים במחסנית
                context.startActivity(intent); // הפעלת האקטיביטי החדשה

                // סגירת האקטיביטי הנוכחית
                if (context instanceof GameActivity) { // בדיקה אם הקונטקסט הוא מסוג GameActivity
                    ((GameActivity) context).finish(); // סגירת האקטיביטי של המשחק
                } else if (context instanceof Player1Activity) { // בדיקה אם הקונטקסט הוא מסוג Player1Activity
                    ((Player1Activity) context).finish(); // סגירת האקטיביטי של שחקן 1
                } else if (context instanceof Player2Activity) { // בדיקה אם הקונטקסט הוא מסוג Player2Activity
                    ((Player2Activity) context).finish(); // סגירת האקטיביטי של שחקן 2
                }
            }
        });

        dialog.show(); // הצגת הדיאלוג
    }
}