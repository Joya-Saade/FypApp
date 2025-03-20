package com.example.halo_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private EditText email, password;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private TextView signUpText; // "Sign up now!" clickable text

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Link UI Elements
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.button);
        signUpText = findViewById(R.id.textView8); // "Sign up now!" text

        // Handle Sign In Button Click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Handle Click on "Sign up now!" Text
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpPage();
            }
        });
    }

    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate user in Firebase
        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(SignInActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            Log.d("SignInActivity", "User logged in: " + user.getEmail());

                            // Navigate to HomeActivity (or another page)
                            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // Close SignInActivity
                        }
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Log.e("SignInActivity", "Login Failed: " + errorMessage);
                        Toast.makeText(SignInActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openSignUpPage() {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
}
