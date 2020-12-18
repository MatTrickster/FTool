package com.e.ftool;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    View v;

    HistoryAdapter(){
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.i("TAG","Cons");
        holder.orderId.setText("Order "+position);

    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView orderId, serviceDate, status;
        CardView statusCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderId = v.findViewById(R.id.order_id);
            serviceDate = v.findViewById(R.id.service_date);
            status = v.findViewById(R.id.status);
            statusCard = v.findViewById(R.id.status_card);
        }
    }
}
