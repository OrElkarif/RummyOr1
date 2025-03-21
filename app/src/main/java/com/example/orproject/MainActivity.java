package com.example.orproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ObjectInputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStartGame, btnSetting, btnInstruction, btnAchivevements;
    private String backgroundColor = "Blue";
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private LinearLayout linearLayout;


    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        init();
    }

    public static MainActivity getContext() {
        return instance;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    private void init() {
        btnStartGame = findViewById(R.id.btnStartGame);
        btnStartGame.setOnClickListener(this);
        btnSetting = findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(this);
        btnInstruction = findViewById(R.id.btnInstruction);
        btnInstruction.setOnClickListener(this);
        btnAchivevements = findViewById(R.id.btnAchievements);
        btnAchivevements.setOnClickListener(this);

        linearLayout = findViewById(R.id.MainActivity);
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            String str = data.getStringExtra("color");
                            backgroundColor = str; // Update the local color variable

                            // Update Firebase
                            FbModule fbModule = new FbModule(null);
                            fbModule.updateBackgroundColor(str);

                            // Update the local background
                            setBackgroundColor(str);

                            Toast.makeText(MainActivity.this,
                                    "Background color set to " + str, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onClick(View v) {
        if (v == btnStartGame) {
            Intent intent = new Intent(this, PreGameActivity.class);
            startActivity(intent);
        }
        if (v == btnSetting) {
            Intent i = new Intent(this, SettingActivity.class);
            i.putExtra("currentColor", backgroundColor); // Pass the current color
            activityResultLauncher.launch(i);

        }
        if (v == btnInstruction) {
            Intent intent = new Intent(this, InstructionActivity.class);
            startActivity(intent);

        }
        if (v == btnAchivevements) {
            Intent intent = new Intent(this, AchievementsActivity.class);
            startActivity(intent);

        }
    }


    public void setBackgroundColor(String str) {
        backgroundColor = str;
        switch (str) {
            case "Blue": {
                linearLayout.setBackgroundColor(Color.BLUE);
                break;
            }

            case "Red": {
                linearLayout.setBackgroundColor(Color.RED);
                break;
            }
            case "Pink": {
                linearLayout.setBackgroundColor(0xFFF2ACB9);
                break;
            }
            case "Yellow": {

                linearLayout.setBackgroundColor(Color.YELLOW);
                break;
            }

            default:
        }
    }
}



