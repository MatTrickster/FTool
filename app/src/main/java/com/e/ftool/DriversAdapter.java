package com.e.ftool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriversAdapter extends RecyclerView.Adapter<DriversAdapter.ViewHolder> {

    GoogleMap map;
    Context context;
    View v;
    List<Driver> drivers;
    BottomSheetBehavior sheet;

    DriversAdapter(Context context, List<Driver> drivers, GoogleMap map, BottomSheetBehavior sheet){
        this.context = context;
        this.drivers = drivers;
        this.map = map;
        this.sheet = sheet;
    }

    @NonNull
    @Override
    public DriversAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_item, parent, false);
        return new DriversAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriversAdapter.ViewHolder holder, int position) {

        Glide.with(context).load(drivers.get(position).getImgUrl()).placeholder(R.drawable.ic_person)
                .into(holder.imageView);
        holder.name.setText(drivers.get(position).getName());
        holder.number.setText(""+drivers.get(position).getNumber());
        holder.charge.setText("Rs. "+drivers.get(position).getServiceCharge() + " /Hr");
        holder.rating.setText(drivers.get(position).getRating());
        holder.distance.setText((String.format("%.2f",drivers.get(position).getDist()/1000))+" Km");

        holder.itemView.setOnClickListener(view -> {

            map.moveCamera(CameraUpdateFactory.newLatLng(drivers.get(position).getLocation()));
            sheet.setState(BottomSheetBehavior.STATE_COLLAPSED);

        });

    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imageView;
        TextView name,number,charge,rating,distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = v.findViewById(R.id.driver_img);
            name = v.findViewById(R.id.driver_name);
            number = v.findViewById(R.id.driver_number);
            charge = v.findViewById(R.id.service_charge);
            rating = v.findViewById(R.id.driver_rating);
            distance = v.findViewById(R.id.distance);
        }
    }

}
