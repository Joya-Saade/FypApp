package com.example.halo_test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView welcomeText;
    private Button logoutButton, emergencyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Link UI Elements
        welcomeText = findViewById(R.id.textWelcome);
        logoutButton = findViewById(R.id.buttonLogout);
        emergencyButton = findViewById(R.id.buttonEmergency); // Emergency button


        // Display Welcome Message
        if (user != null) {
            String welcomeMessage = "Welcome, " + user.getEmail() + "!";
            welcomeText.setText(welcomeMessage);
        } else {
            Toast.makeText(this, "Error: No user logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity
        }

        // Logout Button Click
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(HomeActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, SignInActivity.class));
                finish(); // Close HomeActivity
            }
        });

        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, EmergencyContactActivity.class);
                startActivity(intent);
            }
        });
    }
}
