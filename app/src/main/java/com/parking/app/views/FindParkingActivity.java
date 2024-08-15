package com.parking.app.views;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.parking.app.AppContants;
import com.parking.app.adapters.ParkingSlotAdapter;
import com.parking.app.R;
import com.parking.app.models.ParkingSlot;
import com.parking.app.models.Reviews;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindParkingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private RecyclerView recyclerView;
    private ParkingSlotAdapter adapter;
    private List<ParkingSlot> parkingSlotList;
    private EditText searchEditText;
    private GoogleMap mMap;
    private ConstraintLayout constraintLayout;
    private static final LatLng BULGARIA_CENTER = new LatLng(42.7339, 25.4858);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_parking);

        FirebaseApp.initializeApp(this);

        constraintLayout = findViewById(R.id.container_layout);
        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        parkingSlotList = new ArrayList<>();
        adapter = new ParkingSlotAdapter(parkingSlotList);
        recyclerView.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(locationName)) {
                    searchParkingSlots(locationName);
                } else {
                    Toast.makeText(FindParkingActivity.this, "Please enter a location name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadParkingSlots(); // Load all parking slots initially
    }

    private void loadParkingSlots() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingSlotList.clear();
                mMap.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String valueKey = snapshot.getKey();
                    String City = snapshot.child("City").getValue(String.class);
                    String slotName = snapshot.child("Name").getValue(String.class);
                    String status = snapshot.child("Status").getValue(String.class);
                    ArrayList<Map<String, Integer>> reviews = (ArrayList<Map<String, Integer>>) snapshot.child("Reviews").getValue();
                    Double latitude = null;
                    Double longitude = null;
                    Map<String, Integer> prices = null;

                    try {
                        latitude = snapshot.child("Latitude").getValue(Double.class);
                        longitude = snapshot.child("Longitude").getValue(Double.class);
                    } catch (Exception e) {
                        String latStr = snapshot.child("Latitude").getValue(String.class);
                        String lonStr = snapshot.child("Longitude").getValue(String.class);

                        if (latStr != null && lonStr != null) {
                            try {
                                latitude = Double.parseDouble(latStr);
                                longitude = Double.parseDouble(lonStr);
                            } catch (NumberFormatException ex) {
                                Toast.makeText(FindParkingActivity.this, "Invalid latitude/longitude for slot: " + slotName, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    prices = (Map<String, Integer>) snapshot.child("Prices").getValue();

                    if (slotName != null && status != null && latitude != null && longitude != null && prices != null) {
                        ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices, reviews);
                        parkingSlot.setCity(City);
                        parkingSlot.setValueKey(valueKey);
                        parkingSlotList.add(parkingSlot);
                        LatLng location = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(slotName).snippet("Status: " + status));
                        marker.setTag(parkingSlot);
                    }
                }

                adapter.notifyDataSetChanged();
                if (!parkingSlotList.isEmpty()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BULGARIA_CENTER, 7));
                }
                Log.e("count ->", "" + parkingSlotList.size());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindParkingActivity.this, "Failed to load parking slots.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchParkingSlots(String locationName) {
        Query query = FirebaseDatabase.getInstance().getReference().child("ParkingSlots").orderByChild("Name").startAt(locationName).endAt(locationName + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingSlotList.clear();
                mMap.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String slotName = snapshot.child("Name").getValue(String.class);
                    String status = snapshot.child("Status").getValue(String.class);
                    Double latitude = null;
                    Double longitude = null;
                    Map<String, Integer> prices = null;
                    ArrayList<Map<String, Integer>> reviews = null;

                    try {
                        latitude = snapshot.child("Latitude").getValue(Double.class);
                        longitude = snapshot.child("Longitude").getValue(Double.class);
                    } catch (Exception e) {
                        String latStr = snapshot.child("Latitude").getValue(String.class);
                        String lonStr = snapshot.child("Longitude").getValue(String.class);

                        if (latStr != null && lonStr != null) {
                            try {
                                latitude = Double.parseDouble(latStr);
                                longitude = Double.parseDouble(lonStr);
                            } catch (NumberFormatException ex) {
                                Toast.makeText(FindParkingActivity.this, "Invalid latitude/longitude for slot: " + slotName, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    prices = (Map<String, Integer>) snapshot.child("Prices").getValue();
                    reviews = (ArrayList<Map<String, Integer>>) snapshot.child("Reviews").getValue();

                    if (slotName != null && status != null && latitude != null && longitude != null && prices != null) {
                        ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices, reviews);
                        parkingSlotList.add(parkingSlot);

                        LatLng location = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(slotName).snippet("Status: " + status));
                        marker.setTag(parkingSlot);
                    }
                }

                adapter.notifyDataSetChanged();
                if (!parkingSlotList.isEmpty()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BULGARIA_CENTER, 7));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindParkingActivity.this, "Failed to search parking slots.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ParkingSlot slot = (ParkingSlot) marker.getTag();
                if (slot != null) {
                    //showParkingSlotDetails(slot);

                    Intent intent = new Intent(FindParkingActivity.this, FindParkingDetailsActivity.class);
                    intent.putExtra("obj", slot);
                    intent.putExtra(AppContants.SlotMapPrices, (Serializable) slot.getPrices());
                    intent.putExtra(AppContants.SlotReviews, (Serializable) slot.getReviews());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        loadParkingSlots();
    }
}
