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
    private Dialog dialog;
    private Context context;

    public CustomDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_custom_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false); // Prevent dismissing by tapping outside
    }

    public void showWinnerDialog(String winner, int score) {
        TextView tvWinner = dialog.findViewById(R.id.tvWinner);
        TextView tvScore = dialog.findViewById(R.id.tvScore);
        Button btnFinishGame = dialog.findViewById(R.id.btnFinishGame);

        tvWinner.setText(winner + " Wins!");
        tvScore.setText("Final Score: " + score);

        btnFinishGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Return to the main menu
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);

                // Close the current activity
                if (context instanceof GameActivity) {
                    ((GameActivity) context).finish();
                } else if (context instanceof Player1Activity) {
                    ((Player1Activity) context).finish();
                } else if (context instanceof Player2Activity) {
                    ((Player2Activity) context).finish();
                }
            }
        });

        dialog.show();
    }
}