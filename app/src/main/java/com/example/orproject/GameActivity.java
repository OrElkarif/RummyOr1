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

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

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

    }


    @Override
    public void onClick(View v) {

    }
}
