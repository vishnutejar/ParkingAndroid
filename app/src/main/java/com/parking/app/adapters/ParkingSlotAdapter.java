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
        holder.slotNameTextView.setText("Name : "+slot.getName());
        holder.txt_prise.setText("Price : " + slot.getSelectedPrice());
        holder.txt_contact.setText("Contact : " + "9739393939");
        holder.txt_city.setText("City : " + slot.getCity());
        holder.txt_rating.setText(String.valueOf(slot.getSelectedRating()));
    }

    @Override
    public int getItemCount() {
        return parkingSlotList.size();
    }

    public static class ParkingSlotViewHolder extends RecyclerView.ViewHolder {
        TextView slotNameTextView, txt_prise, txt_contact,txt_city,txt_rating;

        public ParkingSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_city = itemView.findViewById(R.id.txt_city);
            slotNameTextView = itemView.findViewById(R.id.slot_name);
            txt_prise = itemView.findViewById(R.id.txt_prise);
            txt_rating = itemView.findViewById(R.id.txt_rating);
            txt_contact = itemView.findViewById(R.id.txt_contact);
        }
    }
}
