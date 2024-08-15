package com.parking.app.views;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parking.app.adapters.ParkingSlotAdapter;
import com.parking.app.R;
import com.parking.app.models.ParkingSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendedParkingActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private ParkingSlotAdapter parkingSlotAdapter;
    private List<ParkingSlot> parkingSlotList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_parking);

        // Initialize Firebase
        //databaseReference = FirebaseDatabase.getInstance().getReference("RecommendedParkingSlots");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("MainPages").child("Pages").child("RecommendedParking").child("RecommendedParkings");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view); // Ensure this ID matches the one in XML
        /*parkingSlotList = new ArrayList<>();
        parkingSlotAdapter = new ParkingSlotAdapter(parkingSlotList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(parkingSlotAdapter);*/

        //  loadRecommendedParkingSlots();
        loadParkingSlots();
    }

    List<String> listOfValues = new ArrayList<>();

    private void loadRecommendedParkingSlots() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingSlotList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String value = snapshot.getValue(String.class);
                    listOfValues.add(value);
                    assert value != null;
                    // loadParkingSlots("/".concat(value));
                    /*String slotName = snapshot.child("Name").getValue(String.class);
                    String slotStatus = snapshot.child("Status").getValue(String.class);
                    Double latitude = snapshot.child("Latitude").getValue(Double.class);
                    Double longitude = snapshot.child("Longitude").getValue(Double.class);
                    Map<String, Integer> slotPrices = (Map<String, Integer>) snapshot.child("Prices").getValue();

                    if (slotName != null && slotStatus != null && latitude != null && longitude != null && slotPrices != null) {
                        ParkingSlot slot = new ParkingSlot(slotName, slotStatus, latitude, longitude, slotPrices);
                        parkingSlotList.add(slot);
                    }*/
                }

                //  parkingSlotAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RecommendedParkingActivity.this, "Failed to load recommended parking slots.", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error loading recommended parking slots: " + databaseError.getMessage());
            }
        });

        parkingSlotList = new ArrayList<>();
        parkingSlotAdapter = new ParkingSlotAdapter(parkingSlotList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(parkingSlotAdapter);
    }

    private void loadParkingSlots() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("Status").getValue(String.class);

                    if (status != null)
                        if (status.equals("Recommended")) {


                            String slotName = snapshot.child("Name").getValue(String.class);
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
                                        Toast.makeText(RecommendedParkingActivity.this, "Invalid latitude/longitude for slot: " + slotName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            prices = (Map<String, Integer>) snapshot.child("Prices").getValue();
                            reviews = (ArrayList<Map<String, Integer>>) snapshot.child("Reviews").getValue();

                            if (slotName != null && status != null && latitude != null && longitude != null && prices != null) {
                                ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices, reviews);
                                parkingSlotList.add(parkingSlot);

                      /*  LatLng location = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(slotName).snippet("Status: " + status));
                        marker.setTag(parkingSlot);*/
                            }
                        }
                }
                if (!parkingSlotList.isEmpty()) {
                    //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BULGARIA_CENTER, 7));
                    parkingSlotAdapter = new ParkingSlotAdapter(parkingSlotList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(parkingSlotAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RecommendedParkingActivity.this, "Failed to load parking slots.", Toast.LENGTH_SHORT).show();
            }

        });

    }

}
