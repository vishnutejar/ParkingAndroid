package com.parking.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OldReservationsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private GoogleMap mMap; // Declare GoogleMap variable

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_reservations);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("old_reservations");

        // Load map fragment (example)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Assign GoogleMap object to mMap variable
        mMap = googleMap;

        // Example: Add a marker in Bulgaria and move the camera
        LatLng bulgaria = new LatLng(42.7339, 25.4858);
        mMap.addMarker(new MarkerOptions().position(bulgaria).title("Marker in Bulgaria"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bulgaria));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10)); // Adjust zoom level as needed
    }
}
