package com.example.halo_test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {
    private List<List<LatLng>> rides;
    private Context context;

    public RideAdapter(List<List<LatLng>> rides, Context context) {
        this.rides = rides;
        this.context = context;
    }

    public void setRides(List<List<LatLng>> rides) {
        this.rides = rides;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ride_map, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        List<LatLng> path = rides.get(position);

        holder.mapView.onCreate(null);
        holder.mapView.onResume();

        holder.mapView.getMapAsync(googleMap -> {
            if (path != null && !path.isEmpty()) {
                PolylineOptions options = new PolylineOptions().addAll(path).width(5).color(Color.BLUE);
                googleMap.addPolyline(options);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), 12));
            }
        });

        holder.overlay.setOnClickListener(v -> {
            Log.d("RideAdapter", "Clicked ride at position: " + position);
            Intent intent = new Intent(context, FullMapActivity.class);
            ArrayList<LatLng> pathArray = new ArrayList<>(path);
            intent.putExtra("ridePath", pathArray);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        MapView mapView;
        View overlay;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            mapView = itemView.findViewById(R.id.miniMap);
            overlay = itemView.findViewById(R.id.overlay);
        }
    }
}

