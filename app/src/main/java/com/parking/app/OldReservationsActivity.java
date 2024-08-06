package com.parking.app;

import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OldReservationsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private ParkingSlotAdapter parkingSlotAdapter;
    private List<ParkingSlot> parkingSlotList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_reservations);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("old_reservations");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view); // Ensure this ID matches the one in XML
        parkingSlotList = new ArrayList<>();
        parkingSlotAdapter = new ParkingSlotAdapter(parkingSlotList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(parkingSlotAdapter);

        loadReservedSlots();
    }

    private void loadReservedSlots() {
        databaseReference.orderByChild("Status").equalTo("Reserved").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingSlotList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String slotName = snapshot.child("Name").getValue(String.class);
                    String slotStatus = snapshot.child("Status").getValue(String.class);
                    Double latitude = snapshot.child("Latitude").getValue(Double.class);
                    Double longitude = snapshot.child("Longitude").getValue(Double.class);
                    Map<String, Integer> slotPrices = (Map<String, Integer>) snapshot.child("Prices").getValue();

                    if (slotName != null && slotStatus != null && latitude != null && longitude != null) {
                        ParkingSlot slot = new ParkingSlot(slotName, slotStatus, latitude, longitude, slotPrices);
                        parkingSlotList.add(slot);
                    }
                }
                parkingSlotAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OldReservationsActivity.this, "Failed to load reserved slots.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
