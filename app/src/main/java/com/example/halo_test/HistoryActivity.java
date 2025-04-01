package com.example.halo_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private RecyclerView rideRecyclerView;
    private RideAdapter rideAdapter;

    private List<LatLng> savedPath = new ArrayList<>();

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


        // Initialize Firebase and User
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        rideRecyclerView = findViewById(R.id.rideRecyclerView);
        rideRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideAdapter = new RideAdapter(new ArrayList<>(), this);
        rideRecyclerView.setAdapter(rideAdapter);

        // Load and display saved rides
        if (currentUser != null) {
            loadSavedRides();
        }
    }

    private void updateSummaryBox(List<List<LatLng>> rideDataList) {
        int totalRides = rideDataList.size();
        double totalDistanceKm = 0;
        int estimatedTimeMin = 0;

        for (List<LatLng> path : rideDataList) {
            for (int i = 1; i < path.size(); i++) {
                totalDistanceKm += distanceBetween(path.get(i - 1), path.get(i));
            }
            estimatedTimeMin += path.size() * 2; // assume 1 point every 2 mins
        }

        TextView totalRidesText = findViewById(R.id.totalRides);
        TextView totalDistanceText = findViewById(R.id.totalDistance);
        TextView totalTimeText = findViewById(R.id.totalTime);

        totalRidesText.setText("Rides: " + totalRides);
        totalDistanceText.setText(String.format("Distance: %.1f km", totalDistanceKm));
        totalTimeText.setText("Time: " + estimatedTimeMin + " min");
    }

    private double distanceBetween(LatLng start, LatLng end) {
        float[] result = new float[1];
        android.location.Location.distanceBetween(
                start.latitude, start.longitude,
                end.latitude, end.longitude,
                result
        );
        return result[0] / 1000.0; // meters to kilometers
    }


    private void loadSavedRides() {

        String userId = currentUser.getUid();

        db.collection("Users").document(userId)
                .collection("Rides")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<List<LatLng>> rides = new ArrayList<>();
                    double totalDistanceKm = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        List<Map<String, Double>> path = (List<Map<String, Double>>) doc.get("path");
                        List<LatLng> latLngPath = new ArrayList<>();

                        if (path != null) {
                            for (Map<String, Double> point : path) {
                                Double lat = point.get("latitude");
                                Double lng = point.get("longitude");
                                if (lat != null && lng != null) {
                                    latLngPath.add(new LatLng(lat, lng));
                                }
                            }
                            if (!latLngPath.isEmpty()) {
                                rides.add(latLngPath);
                                totalDistanceKm += calculateDistance(latLngPath);

                            }
                        }
                    }



                    if (rides.isEmpty()) {
                        findViewById(R.id.rideHistoryTitle).setVisibility(View.GONE);
                        findViewById(R.id.summaryBox).setVisibility(View.GONE);
                        findViewById(R.id.rideRecyclerView).setVisibility(View.GONE);
                        findViewById(R.id.noRecordedRidesText).setVisibility(View.VISIBLE);
                        findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                    } else {
                        // 🚀 Update Summary Box
                        ((TextView) findViewById(R.id.totalRides)).setText("Rides: " + rides.size());
                        ((TextView) findViewById(R.id.totalDistance)).setText(String.format("Distance: %.2f km", totalDistanceKm));
                        ((TextView) findViewById(R.id.totalTime)).setText("Time: -- min"); // Optional
                        // Show rides
                        RideAdapter adapter = new RideAdapter(rides, this);
                        RecyclerView recyclerView = findViewById(R.id.rideRecyclerView);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setVisibility(View.VISIBLE);

                        findViewById(R.id.rideHistoryTitle).setVisibility(View.VISIBLE);
                        findViewById(R.id.summaryBox).setVisibility(View.VISIBLE);
                        findViewById(R.id.noRecordedRidesText).setVisibility(View.GONE);
                        findViewById(R.id.imageView).setVisibility(View.GONE);

                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Error fetching saved rides.", e));
    }

    // 🧮 Helper to calculate total distance for a ride
    private double calculateDistance(List<LatLng> path) {
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            LatLng start = path.get(i);
            LatLng end = path.get(i + 1);
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    start.latitude, start.longitude,
                    end.latitude, end.longitude,
                    results
            );
            totalDistance += results[0]; // in meters
        }
        return totalDistance / 1000.0; // convert to km
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        if (savedPath.isEmpty()) {
            Toast.makeText(this, "No saved rides found.", Toast.LENGTH_SHORT).show();
        } else {
            // Zoom to the first saved location if any
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(savedPath.get(0), 12));
        }
    }


}
