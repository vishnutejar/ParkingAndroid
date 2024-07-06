package com.parking.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ParkingSlotAdapter extends RecyclerView.Adapter<ParkingSlotAdapter.ParkingSlotViewHolder> {

    private List<ParkingSlot> parkingSlotList;

    public ParkingSlotAdapter(List<ParkingSlot> parkingSlotList) {
        this.parkingSlotList = parkingSlotList;
    }

    @NonNull
    @Override
    public ParkingSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parking_slot_item, parent, false);
        return new ParkingSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingSlotViewHolder holder, int position) {
        ParkingSlot parkingSlot = parkingSlotList.get(position);
        holder.slotNameTextView.setText(parkingSlot.getName());
        holder.slotStatusTextView.setText(parkingSlot.getStatus());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prices = "Prices: \n1 hour: " + parkingSlot.getPrices().get("1_hour") + " euro\n" +
                        "5 hours: " + parkingSlot.getPrices().get("5_hours") + " euro\n" +
                        "1 day: " + parkingSlot.getPrices().get("1_day") + " euro\n" +
                        "1 week: " + parkingSlot.getPrices().get("1_week") + " euro\n" +
                        "1 year: " + parkingSlot.getPrices().get("1_year") + " euro";
                Toast.makeText(v.getContext(), prices, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return parkingSlotList.size();
    }

    public static class ParkingSlotViewHolder extends RecyclerView.ViewHolder {
        TextView slotNameTextView, slotStatusTextView;

        public ParkingSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            slotNameTextView = itemView.findViewById(R.id.slotNameTextView);
            slotStatusTextView = itemView.findViewById(R.id.slotStatusTextView);
        }
    }
}
