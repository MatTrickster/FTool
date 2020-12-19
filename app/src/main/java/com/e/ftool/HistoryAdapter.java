package com.e.ftool;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    View v;
    List<HashMap<String,String>> items;

    HistoryAdapter(List<HashMap<String,String>> items){
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String date = items.get(position).get("time_date");
        date = date.substring(0,date.indexOf(" "));

        holder.service.setText(items.get(position).get("service"));
        holder.date.setText(date);
        holder.amount.setText("Rs. 210");
        holder.sno.setText(position+1+".");

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView service, date, amount, sno;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            service = v.findViewById(R.id.service);
            date = v.findViewById(R.id.date);
            amount = v.findViewById(R.id.amount);
            sno = v.findViewById(R.id.sno);
        }
    }
}
