package com.example.halo_test;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Marker userMarker;
    private LatLng currentLocation; // Store current location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Search Bar Acting as Navigate Button
        TextView searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> showDestinationPopup());

        // Record Button → Opens RecordActivity
        ImageButton buttonRecord = findViewById(R.id.nav_record);
        buttonRecord.setOnClickListener(v -> {
            if (currentLocation != null && gMap != null) {
                // Zoom in before switching activities
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

                // Delay opening activity to allow zoom animation to complete
                buttonRecord.postDelayed(() -> {
                    Intent intent = new Intent(MapsActivity.this, RecordActivity.class);
                    intent.putExtra("latitude", currentLocation.latitude);
                    intent.putExtra("longitude", currentLocation.longitude);
                    startActivity(intent);
                }, 800); // 800ms delay for smooth transition
            } else {
                Toast.makeText(MapsActivity.this, "Location not available yet!", Toast.LENGTH_SHORT).show();
            }
        });

        // Home Button → Opens HomeActivity
        ImageButton buttonHome = findViewById(R.id.nav_home);
        buttonHome.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close MapsActivity
        });

        // Request Location Permissions
        checkAndRequestPermissions();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        if (hasLocationPermissions()) {
            enableUserLocation();
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void checkAndRequestPermissions() {
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        } else {
            return new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            updateMapWithLocation(location);
                        } else {
                            Toast.makeText(this, "Unable to fetch current location. Ensure GPS is ON.", Toast.LENGTH_LONG).show();
                        }
                    });

            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setMinUpdateIntervalMillis(2000)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        updateMapWithLocation(location);
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateMapWithLocation(Location location) {
        if (gMap == null || location == null) {
            return; // Ensure the map and location are not null
        }

        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // Remove the old marker if it exists
        if (userMarker != null) {
            userMarker.remove();
        }

        // Add a new marker at the current location
        userMarker = gMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("You are here"));

        // Ensure auto-zoom works when location updates
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
    }

    private void showDestinationPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Destination");

        final EditText input = new EditText(this);
        input.setHint("Enter destination (e.g., Borj Al Barajne, Beirut)");
        builder.setView(input);

        builder.setPositiveButton("Navigate", (dialog, which) -> {
            String destination = input.getText().toString();
            if (!destination.isEmpty() && currentLocation != null) {
                startNavigation(destination);
            } else {
                Toast.makeText(this, "Please enter a destination and ensure location is available.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void startNavigation(String destination) {
        String uri = "google.navigation:q=" + destination + "&mode=d"; // 'd' mode = driving
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean permissionGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                    break;
                }
            }

            if (permissionGranted) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Enable it in settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
