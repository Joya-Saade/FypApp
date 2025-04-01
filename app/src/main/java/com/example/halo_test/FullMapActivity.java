package com.example.halo_test;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class FullMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap fullMap;
    private List<LatLng> ridePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fullMapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ridePath = getIntent().getParcelableArrayListExtra("ridePath");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        fullMap = googleMap;
        if (ridePath != null && !ridePath.isEmpty()) {
            fullMap.addPolyline(new PolylineOptions().addAll(ridePath).width(8).color(Color.RED));
            fullMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ridePath.get(0), 14));
        }
    }
}

