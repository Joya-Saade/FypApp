package com.example.halo_test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    private List<LatLng> recordedPath = new ArrayList<>();
    private Polyline polyline;
    private Button buttonStopRecording;
    private TextView timerText;

    private Handler handler = new Handler();
    private long startTime = 0L;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        timerText = findViewById(R.id.timerText);
        buttonStopRecording = findViewById(R.id.buttonStopRecording);
        buttonStopRecording.setOnClickListener(v -> stopRecording());

        requestLocationPermissions();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        if (hasLocationPermissions()) {
            startRecording();
        } else {
            requestLocationPermissions();
        }
    }

    private void requestLocationPermissions() {
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);

            // 🔥 Get last known location to zoom in instantly
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            zoomToLocation(location); // Instant zoom-in
                        }
                    });

            // 🔥 Request **faster** location updates
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 2000) // Updates every 2 sec
                    .setMinUpdateIntervalMillis(1000) // Fastest interval 1 sec
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) return;

                    for (Location location : locationResult.getLocations()) {
                        recordLocation(location);
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            // Start Timer
            startTime = SystemClock.uptimeMillis();
            isRecording = true;
            handler.post(updateTimerRunnable);
        }
    }

    // 🚀 New method to instantly zoom to last known location
    private void zoomToLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18), 1000, null);
    }

    private void recordLocation(Location location) {
        if (gMap == null || location == null) return;

        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
        recordedPath.add(newPoint);

        // Remove previous polyline before updating
        if (polyline != null) {
            polyline.remove();
        }

        polyline = gMap.addPolyline(new PolylineOptions()
                .addAll(recordedPath)
                .width(8)
                .color(0xFFFF0000)); // Red Lines


        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 12), 800, null);
    }

    private void stopRecording() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        isRecording = false;
        handler.removeCallbacks(updateTimerRunnable);

        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        finish(); // Return to previous activity
    }

    private Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                long elapsedMillis = SystemClock.uptimeMillis() - startTime;
                int seconds = (int) (elapsedMillis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                timerText.setText(timeFormatted);

                handler.postDelayed(this, 1000); // Update every second
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        handler.removeCallbacks(updateTimerRunnable);
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
                startRecording();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
