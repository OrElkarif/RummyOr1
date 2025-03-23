package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingActivity extends AppCompatActivity {

    private Spinner colorSpinner;
    private Button btnSaveColor;
    private String selectedColor = "Blue"; // Default color
    private DatabaseReference gameStateRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // יצירת גישה ישירות ל-Firebase
        gameStateRef = FirebaseDatabase.getInstance().getReference("gameState");

        // Initialize views
        colorSpinner = findViewById(R.id.spinnerBG);
        btnSaveColor = findViewById(R.id.btnSaveColor);

        // Set up the spinner with color options
        String[] colors = {"Blue", "Red", "Pink", "Yellow"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, colors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(adapter);

        // Get the currently selected color from intent if available
        String currentColor = getIntent().getStringExtra("currentColor");
        if (currentColor != null && !currentColor.isEmpty()) {
            for (int i = 0; i < colors.length; i++) {
                if (colors[i].equals(currentColor)) {
                    colorSpinner.setSelection(i);
                    selectedColor = currentColor;
                    break;
                }
            }
        }

        // Handle spinner selection
        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedColor = colors[position];
                updateBackgroundColor(selectedColor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Handle save button click
        btnSaveColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // שמירת הצבע הנבחר ישירות ב-Firebase
                gameStateRef.child("backgroundColor").setValue(selectedColor);

                // החזרת הצבע הנבחר לאקטיביטי הקודמת
                Intent resultIntent = new Intent();
                resultIntent.putExtra("color", selectedColor);
                setResult(RESULT_OK, resultIntent);

                // הצגת הודעת הצלחה
                Toast.makeText(SettingActivity.this, "Color saved: " + selectedColor, Toast.LENGTH_SHORT).show();

                // סיום האקטיביטי
                finish();
            }
        });
    }

    private void updateBackgroundColor(String color) {
        View rootView = findViewById(android.R.id.content);

        switch (color) {
            case "Blue":
                rootView.setBackgroundColor(Color.BLUE);
                break;
            case "Red":
                rootView.setBackgroundColor(Color.RED);
                break;
            case "Pink":
                rootView.setBackgroundColor(0xFFF2ACB9);
                break;
            case "Yellow":
                rootView.setBackgroundColor(Color.YELLOW);
                break;
            default:
                rootView.setBackgroundColor(Color.WHITE);
                break;
        }
    }
}