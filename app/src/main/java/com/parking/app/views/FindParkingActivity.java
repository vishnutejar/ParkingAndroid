package com.parking.app.views;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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

    private void showParkingSlotDetails(ParkingSlot slot) {
        // Check if the slot is already recommended or reserved
        if ("Recommended".equals(slot.getStatus()) || "Reserved".equals(slot.getStatus())) {
            Toast.makeText(this, "This slot is already " + slot.getStatus().toLowerCase(), Toast.LENGTH_SHORT).show();
            return; // Exit the method, preventing further interaction
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(slot.getName());

        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_price_selection, null);
        builder.setView(dialogView);

        RadioGroup priceRadioGroup = dialogView.findViewById(R.id.priceRadioGroup);

        // Add radio buttons dynamically
        Map<String, Integer> pricesMap = slot.getPrices();
        if (pricesMap != null) {
            for (Map.Entry<String, Integer> entry : pricesMap.entrySet()) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(entry.getKey() + ": $" + entry.getValue());
                radioButton.setId(View.generateViewId());
                priceRadioGroup.addView(radioButton);
            }
        }

        builder.setPositiveButton("Recommend", (dialog, which) -> {
            handlePriceSelection(priceRadioGroup, slot, "Recommended");
        });

        builder.setNegativeButton("Reserve", (dialog, which) -> {
            handlePriceSelection(priceRadioGroup, slot, "Reserved");
        });

        builder.create().show();
    }

    private void handlePriceSelection(RadioGroup priceRadioGroup, ParkingSlot slot, String status) {
        // Check if the slot is already recommended or reserved
        if ("Recommended".equals(slot.getStatus()) || "Reserved".equals(slot.getStatus())) {
            Toast.makeText(this, "This slot is already " + slot.getStatus().toLowerCase(), Toast.LENGTH_SHORT).show();
            return; // Prevent any further action
        }

        int selectedId = priceRadioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            // Find the selected RadioButton by its ID
            RadioButton selectedRadioButton = priceRadioGroup.findViewById(selectedId);
            if (selectedRadioButton != null) {
                String selectedPrice = selectedRadioButton.getText().toString();
                slot.setSelectedPrice(selectedPrice);

                // Update the slot status and selected price in Firebase
                DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots").child(slot.getName());
                slotRef.child("Status").setValue(status);
                slotRef.child("SelectedPrice").setValue(selectedPrice);

                // Create notification for the action
                createNotification("You have " + status.toLowerCase() + " the parking slot: " + slot.getName() + " at " + selectedPrice);
                Toast.makeText(this, "Parking slot " + status.toLowerCase() + " successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error finding selected price option.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select a price", Toast.LENGTH_SHORT).show();
        }
    }


    private String buildMessage(ParkingSlot slot) {
        StringBuilder message = new StringBuilder();
        message.append("Status: ").append(slot.getStatus()).append("\n");
        if (slot.getPrices() != null) {
            message.append("Prices:\n");
            for (Map.Entry<String, Integer> entry : slot.getPrices().entrySet()) {
                message.append(entry.getKey()).append(": $").append(entry.getValue()).append("\n");
            }
        } else {
            message.append("No pricing information available.\n");
        }
        return message.toString();
    }

    private void updateParkingSlotStatus(ParkingSlot slot, String newStatus) {
        DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots").child(slot.getName()).child("Status");
        slotRef.setValue(newStatus);

        String notificationMessage = newStatus.equals("Recommended") ? "You have recommended the parking slot: " : "You have reserved the parking slot: ";
        createNotification(notificationMessage + slot.getName() + " at " + slot.getSelectedPrice());
    }

    private void createNotification(String message) {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "parking_app_channel")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Parking Slot Status Updated")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ParkingAppChannel";
            String description = "Channel for Parking App notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("parking_app_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void loadParkingSlots() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots");

        databaseRef.addValueEventListener(new ValueEventListener() {
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
                        ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices);
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
                        ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices);
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
                    intent.putExtra(AppContants.SlotName, slot.getName());
                    intent.putExtra(AppContants.SlotLatitude, slot.getLatitude());
                    intent.putExtra(AppContants.SlotLongitude, slot.getLongitude());
                    intent.putExtra(AppContants.SlotStatus, slot.getStatus());
                    intent.putExtra(AppContants.SlotMapPrices, (Serializable) slot.getPrices());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        loadParkingSlots();
    }
}
