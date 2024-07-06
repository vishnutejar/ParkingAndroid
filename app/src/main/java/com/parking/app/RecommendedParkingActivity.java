package com.parking.app;

import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.maps.model.Marker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecommendedParkingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Bulgaria is located in the Eastern Europe region at latitude 42.733883 and longitude 25.48583
    // Coordinates for Bulgaria (adjust to your specific location)
    private static final LatLng BULGARIA_CENTER = new LatLng(42.7339, 25.4858);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_parking);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadRecommendedParkingSlots(); // Load recommended parking slots when map is ready

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // You can add handling for marker click if needed
                return false;
            }
        });
    }

    private void loadRecommendedParkingSlots() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("RecommendedParkingSlots");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String slotName = snapshot.child("Name").getValue(String.class);
                    Double latitude = snapshot.child("Latitude").getValue(Double.class);
                    Double longitude = snapshot.child("Longitude").getValue(Double.class);

                    if (slotName != null && latitude != null && longitude != null) {
                        LatLng location = new LatLng(latitude, longitude);
                        MarkerOptions options = new MarkerOptions().position(location).title(slotName);
                        mMap.addMarker(options);
                    }
                }

                // Center the map around Bulgaria
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BULGARIA_CENTER, 7)); // Adjust zoom level as needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error loading recommended parking slots: " + databaseError.getMessage());
            }
        });
    }
}
