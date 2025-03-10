package com.example.halo_test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EmergencyContactActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private EditText inputName, inputEmail, inputPhone;
    private TextView contactName, contactEmail, contactPhone;
    private Button btnSaveContact, btnEditContact;
    private LinearLayout inputLayout;
    private CardView contactCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        // Firebase initialization
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // UI Elements
        inputLayout = findViewById(R.id.inputLayout);
        contactCard = findViewById(R.id.contactCard);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        contactName = findViewById(R.id.contactName);
        contactEmail = findViewById(R.id.contactEmail);
        contactPhone = findViewById(R.id.contactPhone);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnEditContact = findViewById(R.id.btnEditContact);

        // Load contact info if it exists
        loadEmergencyContact();

        // Save contact button
        btnSaveContact.setOnClickListener(v -> saveEmergencyContact());

        // Edit contact button
        btnEditContact.setOnClickListener(v -> enableEditing());
    }

    private void loadEmergencyContact() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        DocumentReference contactRef = db.collection("EmergencyContacts").document(userId);

        contactRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Contact found, display it
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone");

                contactName.setText("Name: " + name);
                contactEmail.setText("Email: " + email);
                contactPhone.setText("Phone: " + phone);

                // Show contact card & edit button, hide input fields
                contactCard.setVisibility(View.VISIBLE);
                btnEditContact.setVisibility(View.VISIBLE);
                inputLayout.setVisibility(View.GONE);
            } else {
                // No contact found, show input fields
                contactCard.setVisibility(View.GONE);
                btnEditContact.setVisibility(View.GONE);
                inputLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void saveEmergencyContact() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        Map<String, Object> contact = new HashMap<>();
        contact.put("name", inputName.getText().toString().trim());
        contact.put("email", inputEmail.getText().toString().trim());
        contact.put("phone", inputPhone.getText().toString().trim());

        db.collection("EmergencyContacts").document(userId).set(contact)
                .addOnSuccessListener(aVoid -> loadEmergencyContact());
    }

    private void enableEditing() {
        contactCard.setVisibility(View.GONE);
        btnEditContact.setVisibility(View.GONE);
        inputLayout.setVisibility(View.VISIBLE);

        inputName.setText(contactName.getText().toString().replace("Name: ", ""));
        inputEmail.setText(contactEmail.getText().toString().replace("Email: ", ""));
        inputPhone.setText(contactPhone.getText().toString().replace("Phone: ", ""));
    }
}
