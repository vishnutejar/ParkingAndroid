package com.parking.app.adapters;

import static com.parking.app.AppContants.RecommendAndReserve;
import static com.parking.app.AppContants.Recommended;
import static com.parking.app.AppContants.Reserved;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parking.app.R;
import com.parking.app.models.ParkingSlot;
import com.parking.app.views.FindParkingDetailsActivity;

import java.util.List;

public class ParkingSlotAdapter extends RecyclerView.Adapter<ParkingSlotAdapter.ParkingSlotViewHolder> {

    private List<ParkingSlot> parkingSlotList;
    String actiontype;

    public ParkingSlotAdapter(List<ParkingSlot> parkingSlotList, String actiontype) {
        this.actiontype = actiontype;
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
        holder.slotNameTextView.setText("Name : " + slot.getName());
        holder.txt_prise.setText("Price : " + slot.getSelectedPrice());
        holder.txt_contact.setText("Contact : " + "9739393939");
        holder.txt_city.setText("City : " + slot.getCity());
        holder.txt_rating.setText(String.valueOf(slot.getSelectedRating()));
        if (actiontype.equals(Recommended)) {
            holder.bt_recommend_reserve.setText(R.string.reserve);
            holder.bt_recommend_reserve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else if (actiontype.equals(Reserved)) {
            holder.bt_recommend_reserve.setText(R.string.recommend);
        } else {
            holder.bt_recommend_reserve.setVisibility(View.GONE);
        }
        if (slot.getStatus().equals(RecommendAndReserve)) {
            holder.bt_recommend_reserve.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return parkingSlotList.size();
    }

    public static class ParkingSlotViewHolder extends RecyclerView.ViewHolder {
        TextView slotNameTextView, txt_prise, txt_contact, txt_city, txt_rating;
        AppCompatButton bt_recommend_reserve;

        public ParkingSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_city = itemView.findViewById(R.id.txt_city);
            slotNameTextView = itemView.findViewById(R.id.slot_name);
            txt_prise = itemView.findViewById(R.id.txt_prise);
            txt_rating = itemView.findViewById(R.id.txt_rating);
            txt_contact = itemView.findViewById(R.id.txt_contact);
            bt_recommend_reserve = itemView.findViewById(R.id.bt_recommend_reserve);
        }
    }
}
