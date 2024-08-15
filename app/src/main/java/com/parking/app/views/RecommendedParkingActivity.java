package com.parking.app.views;

import static com.parking.app.AppContants.Recommended;

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
        databaseReference = FirebaseDatabase.getInstance().getReference().child("MainPages").child("Pages").child("RecommendedParking").child("RecommendedParkings");

        recyclerView = findViewById(R.id.recycler_view); // Ensure this ID matches the one in XML
        loadParkingSlots();
    }

    private void loadParkingSlots() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);

                    if (status != null)
                        if (status.equals(Recommended)) {
                            String slotName = snapshot.child("name").getValue(String.class);
                            String city = snapshot.child("city").getValue(String.class);
                            String selectedPrice = snapshot.child("selectedPrice").getValue(String.class);
                            String selectedRating = snapshot.child("selectedRating").getValue(String.class);
                            Double latitude = null;
                            Double longitude = null;
                            Map<String, Integer> prices = null;
                            ArrayList<Map<String, Integer>> reviews = null;

                            try {
                                latitude = snapshot.child("latitude").getValue(Double.class);
                                longitude = snapshot.child("longitude").getValue(Double.class);
                            } catch (Exception e) {
                                String latStr = snapshot.child("latitude").getValue(String.class);
                                String lonStr = snapshot.child("longitude").getValue(String.class);

                                if (latStr != null && lonStr != null) {
                                    try {
                                        latitude = Double.parseDouble(latStr);
                                        longitude = Double.parseDouble(lonStr);
                                    } catch (NumberFormatException ex) {
                                        Toast.makeText(RecommendedParkingActivity.this, "Invalid latitude/longitude for slot: " + slotName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            prices = (Map<String, Integer>) snapshot.child("prices").getValue();
                            reviews = (ArrayList<Map<String, Integer>>) snapshot.child("reviews").getValue();

                            if (slotName != null && status != null && latitude != null && longitude != null && prices != null) {
                                ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices, reviews);
                                parkingSlot.setCity(city);
                                parkingSlot.setValueKey(snapshot.getKey());
                                parkingSlot.setSelectedPrice(selectedPrice);
                                parkingSlot.setSelectedRating(selectedRating);
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
