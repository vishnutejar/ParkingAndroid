package com.parking.app.views;

import static com.parking.app.AppContants.Recommended;
import static com.parking.app.AppContants.Reserved;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parking.app.AppContants;
import com.parking.app.R;
import com.parking.app.models.ParkingSlot;

import java.util.Map;

public class FindParkingDetailsActivity extends AppCompatActivity {

    private RadioGroup priceRadioGroup;
    private TextView txt_title;
    private ImageView img_back;
    Button bt_recommend, bt_reserve, bt_contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_parking_details);
        Intent intent = getIntent();

        String name = intent.getExtras().getString(AppContants.SlotName);
        String status = intent.getExtras().getString(AppContants.SlotStatus);
        double latitude = intent.getExtras().getDouble(AppContants.SlotLatitude, 0.0);
        double longitude = intent.getExtras().getDouble(AppContants.SlotLongitude, 0.0);
        Map<String, Integer> price = (Map<String, Integer>) intent.getExtras().getSerializable(AppContants.SlotMapPrices);
        priceRadioGroup = findViewById(R.id.priceRadioGroup);
        txt_title = findViewById(R.id.txt_title);
        img_back = findViewById(R.id.img_back);
        bt_recommend = findViewById(R.id.bt_recommend);
        bt_reserve = findViewById(R.id.bt_reserve);
        bt_contact = findViewById(R.id.bt_contact);
        // Add radio buttons dynamically
        Map<String, Integer> pricesMap = price;
        if (pricesMap != null) {
            for (Map.Entry<String, Integer> entry : pricesMap.entrySet()) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(entry.getKey() + ": $" + entry.getValue());
                radioButton.setId(View.generateViewId());
                priceRadioGroup.addView(radioButton);
            }
        }
        txt_title.setText("Name : " + name);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ParkingSlot slot = new ParkingSlot(name, status, latitude, longitude, price);
        bt_recommend.setOnClickListener(view -> {
            handlePriceSelection(priceRadioGroup, slot, Recommended);
            onBackPressed();
        });
        bt_reserve.setOnClickListener(view -> {
            handlePriceSelection(priceRadioGroup, slot, Reserved);
            onBackPressed();

        });
    }

    private void handlePriceSelection(RadioGroup priceRadioGroup, ParkingSlot slot, String status) {
        // Check if the slot is already recommended or reserved
        if (Recommended.equals(slot.getStatus()) || Reserved.equals(slot.getStatus())) {
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


}