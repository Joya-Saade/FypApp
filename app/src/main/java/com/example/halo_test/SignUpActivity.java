package com.example.halo_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private EditText fullName, email, password, confirmPassword;
    private Button signUpButton, signInButton; // Added Sign-In Button
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Link UI Elements
        fullName = findViewById(R.id.editTextFullName);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        confirmPassword = findViewById(R.id.editTextConfirmPassword);
        signUpButton = findViewById(R.id.buttonSignUp);
        signInButton = findViewById(R.id.buttonSignIn); // Link the Sign-In Button

        // Handle Sign Up Button Click
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Handle Sign-In Button Click
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        String userFullName = fullName.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userConfirmPassword = confirmPassword.getText().toString().trim();

        // Validate input
        if (userFullName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty() || userConfirmPassword.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!userPassword.equals(userConfirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassword.length() < 6) {
            Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create User in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Store user data in Firebase Realtime Database
                            User newUser = new User(userFullName, userEmail);
                            databaseReference.child(userId).setValue(newUser)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Log.e("SignUpActivity", "Registration Failed: " + errorMessage);

                        if (errorMessage.contains("The email address is already in use")) {
                            // Redirect user to SignInActivity if email is already registered
                            Toast.makeText(SignUpActivity.this, "Account already exists. Redirecting to login...", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
