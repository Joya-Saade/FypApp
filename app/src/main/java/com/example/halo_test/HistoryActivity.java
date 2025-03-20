package com.example.halo_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Initialize Navigation Buttons
        ImageButton homeButton = findViewById(R.id.nav_home);
        ImageButton locationButton = findViewById(R.id.nav_location);
        ImageButton recordButton = findViewById(R.id.nav_record);
        ImageButton historyButton = findViewById(R.id.nav_history);
        ImageButton profileButton = findViewById(R.id.nav_profile);

        // ✅ Home Button - Navigates to HomeActivity
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // ✅ Location Button - Navigates to MapsActivity
        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        // ✅ Record Button - Navigates to RecordActivity
        recordButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, RecordActivity.class);
            startActivity(intent);
        });

        // ✅ History Button - This is the current activity (No need to navigate)

        // ✅ Profile Button - Navigates to ProfileActivity
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, EmergencyContactActivity.class);
            startActivity(intent);
        });
    }
}
