package com.example.halo_test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EmergencyContactActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private EditText inputName, inputEmail, inputPhone;
    private TextView contactName, contactEmail, contactPhone, contactTitle;
    private Button btnSaveContact, btnEditContact, btnSendEmergencyEmail;
    private LinearLayout inputLayout;
    private CardView contactCard;

    private String emergencyEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // UI Element Initialization
        inputLayout = findViewById(R.id.inputLayout);
        contactCard = findViewById(R.id.contactCard);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        contactName = findViewById(R.id.contactName);
        contactEmail = findViewById(R.id.contactEmail);
        contactPhone = findViewById(R.id.contactPhone);
        contactTitle = findViewById(R.id.contactTitle);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnEditContact = findViewById(R.id.btnEditContact);
        btnSendEmergencyEmail = findViewById(R.id.btnSendEmergencyEmail);

        // Load stored contact details & listen for emergency triggers
        loadEmergencyContact();
        listenForEmergencyTrigger();

        btnSaveContact.setOnClickListener(v -> saveEmergencyContact());
        btnEditContact.setOnClickListener(v -> enableEditing());
        btnSendEmergencyEmail.setOnClickListener(v -> sendEmergencyEmail());
    }

    /*** 🔥 Method to Load Emergency Contact from Firebase ***/
    private void loadEmergencyContact() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        DocumentReference contactRef = db.collection("EmergencyContacts").document(userId);

        contactRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                emergencyEmail = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone");

                contactName.setText("Name: " + name);
                contactEmail.setText("Email: " + emergencyEmail);
                contactPhone.setText("Phone: " + phone);
                contactTitle.setVisibility(View.VISIBLE);

                contactCard.setVisibility(View.VISIBLE);
                btnEditContact.setVisibility(View.VISIBLE);
                btnSendEmergencyEmail.setVisibility(View.VISIBLE);
                inputLayout.setVisibility(View.GONE);
            } else {
                contactTitle.setVisibility(View.GONE);
                contactCard.setVisibility(View.GONE);
                btnEditContact.setVisibility(View.GONE);
                btnSendEmergencyEmail.setVisibility(View.GONE);
                inputLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    /*** 🔥 Method to Save Emergency Contact to Firebase ***/
    private void saveEmergencyContact() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        // Validate input fields
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> contact = new HashMap<>();
        contact.put("name", name);
        contact.put("email", email);
        contact.put("phone", phone);

        db.collection("EmergencyContacts").document(userId).set(contact)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Emergency Contact Saved!", Toast.LENGTH_SHORT).show();
                    loadEmergencyContact(); // Refresh UI
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error Saving Contact!", Toast.LENGTH_SHORT).show());
    }

    /*** 🔥 Method to Enable Editing of Emergency Contact ***/
    private void enableEditing() {
        contactCard.setVisibility(View.GONE);
        btnEditContact.setVisibility(View.GONE);
        btnSendEmergencyEmail.setVisibility(View.GONE);
        inputLayout.setVisibility(View.VISIBLE);

        inputName.setText(contactName.getText().toString().replace("Name: ", ""));
        inputEmail.setText(contactEmail.getText().toString().replace("Email: ", ""));
        inputPhone.setText(contactPhone.getText().toString().replace("Phone: ", ""));
    }

    /*** 🔥 Method to Listen for Emergency Trigger in Firestore ***/
    private void listenForEmergencyTrigger() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        DocumentReference docRef = db.collection("EmergencyEvents").document(userId);

        docRef.addSnapshotListener((document, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }

            if (document != null && document.exists()) {
                Boolean emergencyTrigger = document.getBoolean("emergency_trigger");

                if (Boolean.TRUE.equals(emergencyTrigger)) {
                    Log.d("Firestore", "🚨 Emergency detected! Sending email...");
                    sendEmergencyEmail();
                    resetEmergencyTrigger();
                }
            }
        });
    }

    /*** 🔥 Method to Send Emergency Email ***/
    private void sendEmergencyEmail() {
        if (emergencyEmail == null || emergencyEmail.isEmpty()) {
            Log.e("Email", "❌ No emergency contact email found!");
            return;
        }

        String subject = "🚨 Emergency Alert: Immediate Assistance Required!";
        String message = "Dear Emergency Contact,\n\n" +
                "🚑 Your friend has been in a serious accident and needs immediate assistance.\n\n" +
                "📍 Last Known Location: [Include GPS Coordinates Here]\n\n" +
                "Please take immediate action or contact the authorities.\n\n" +
                "**This is an automated emergency alert from the HALO Smart Helmet System.**\n\n" +
                "Best regards,\nHALO Emergency System";

        new JavaMailAPI(emergencyEmail, subject, message).execute();
        Toast.makeText(this, "Emergency email sent!", Toast.LENGTH_SHORT).show();
    }

    /*** 🔥 Method to Reset Emergency Trigger in Firebase ***/
    private void resetEmergencyTrigger() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        Map<String, Object> resetData = new HashMap<>();
        resetData.put("emergency_trigger", false);

        db.collection("EmergencyEvents").document(userId)
                .set(resetData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "✅ Emergency trigger reset."));
    }
}
