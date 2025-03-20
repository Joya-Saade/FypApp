package com.example.halo_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Bottom Navigation Buttons
        ImageButton homeButton = findViewById(R.id.nav_home);
        ImageButton locationButton = findViewById(R.id.nav_location);
        ImageButton recordButton = findViewById(R.id.nav_record);
        ImageButton historyButton = findViewById(R.id.nav_history);
        ImageButton profileButton = findViewById(R.id.nav_profile);

        // Display Welcome Message
        if (user == null) {
            Toast.makeText(this, "Error: No user logged in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, SignInActivity.class));
            finish(); // Close this activity
        }

        // 🔹 Bottom Navigation Bar Click Listeners
        homeButton.setOnClickListener(v -> {
            // Do nothing as we are already in HomeActivity
        });

        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        recordButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RecordActivity.class);
            startActivity(intent);
        });

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EmergencyContactActivity.class);
            startActivity(intent);
        });
    }
}
