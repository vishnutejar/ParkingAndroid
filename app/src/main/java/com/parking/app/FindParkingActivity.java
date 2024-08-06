package com.parking.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindParkingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private RecyclerView recyclerView;
    private ParkingSlotAdapter adapter;
    private List<ParkingSlot> parkingSlotList;
    private EditText searchEditText;
    private GoogleMap mMap;

    private static final LatLng BULGARIA_CENTER = new LatLng(42.7339, 25.4858);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_parking);

        FirebaseApp.initializeApp(this);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(slot.getName());
        builder.setMessage(buildMessage(slot));
        builder.setPositiveButton("Recommend", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                updateParkingSlotStatus(slot, "Recommended");
            }
        });
        builder.setNegativeButton("Reserve", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                updateParkingSlotStatus(slot, "Reserved");
            }
        });
        builder.create().show();
    }

    private String buildMessage(ParkingSlot slot) {
        StringBuilder message = new StringBuilder();
        message.append("Status: ").append(slot.getStatus()).append("\n");
        message.append("Prices:\n");
        for (Map.Entry<String, Integer> entry : slot.getPrices().entrySet()) {
            message.append(entry.getKey()).append(": $").append(entry.getValue()).append("\n");
        }
        return message.toString();
    }

    private void updateParkingSlotStatus(ParkingSlot slot, String newStatus) {
        DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots").child(slot.getName()).child("Status");
        slotRef.setValue(newStatus);

        String notificationMessage = newStatus.equals("Recommended") ? "You have recommended the parking slot: " : "You have reserved the parking slot: ";
        createNotification(notificationMessage + slot.getName());
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
                        // If parsing fails, try getting the values as strings and converting them
                        String latStr = snapshot.child("Latitude").getValue(String.class);
                        String lonStr = snapshot.child("Longitude").getValue(String.class);

                        if (latStr != null && lonStr != null) {
                            try {
                                latitude = Double.parseDouble(latStr);
                                longitude = Double.parseDouble(lonStr);
                            } catch (NumberFormatException ex) {
                                // Log error or handle the case where conversion fails
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
                Toast.makeText(FindParkingActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchParkingSlots(String locationName) {
        LatLng searchedLocation = BULGARIA_CENTER;

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots");
        Query query = databaseRef.orderByChild("Latitude").equalTo(searchedLocation.latitude);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ParkingSlot> filteredList = new ArrayList<>();

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
                        // If parsing fails, try getting the values as strings and converting them
                        String latStr = snapshot.child("Latitude").getValue(String.class);
                        String lonStr = snapshot.child("Longitude").getValue(String.class);

                        if (latStr != null && lonStr != null) {
                            try {
                                latitude = Double.parseDouble(latStr);
                                longitude = Double.parseDouble(lonStr);
                            } catch (NumberFormatException ex) {
                                // Log error or handle the case where conversion fails
                                Toast.makeText(FindParkingActivity.this, "Invalid latitude/longitude for slot: " + slotName, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    prices = (Map<String, Integer>) snapshot.child("Prices").getValue();

                    if (slotName != null && status != null && latitude != null && longitude != null && prices != null) {
                        ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices);
                        filteredList.add(parkingSlot);
                    }
                }

                parkingSlotList.clear();
                parkingSlotList.addAll(filteredList);
                adapter.notifyDataSetChanged();
                mMap.clear();
                for (ParkingSlot slot : filteredList) {
                    LatLng location = new LatLng(slot.getLatitude(), slot.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(slot.getName()).snippet("Status: " + slot.getStatus()));
                    marker.setTag(slot);
                }

                if (!filteredList.isEmpty()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 10));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindParkingActivity.this, "Failed to search data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadParkingSlots(); // Load all parking slots initially when the map is ready

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ParkingSlot slot = (ParkingSlot) marker.getTag();
                if (slot != null) {
                    showParkingSlotDetails(slot);
                }
                return false;
            }
        });
    }
}
