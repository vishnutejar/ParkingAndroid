package com.parking.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parking.app.R;
import com.parking.app.models.ParkingSlot;

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
        ParkingSlot slot = parkingSlotList.get(position);
        holder.slotNameTextView.setText(slot.getName());
        holder.latitudeTextView.setText(String.valueOf(slot.getLatitude()));
        holder.longitudeTextView.setText(String.valueOf(slot.getLongitude()));
    }

    @Override
    public int getItemCount() {
        return parkingSlotList.size();
    }

    public static class ParkingSlotViewHolder extends RecyclerView.ViewHolder {
        TextView slotNameTextView, latitudeTextView, longitudeTextView;

        public ParkingSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            slotNameTextView = itemView.findViewById(R.id.slot_name);
            latitudeTextView = itemView.findViewById(R.id.latitude);
            longitudeTextView = itemView.findViewById(R.id.longitude);
        }
    }
}
