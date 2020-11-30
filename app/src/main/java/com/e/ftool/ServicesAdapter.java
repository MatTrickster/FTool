package com.e.ftool;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ViewHolder> {

    DatabaseReference ref;
    Context context;
    ArrayList<HashMap<String,String>> list;

    public ServicesAdapter(Context context, ArrayList<HashMap<String,String>> list, DatabaseReference ref){
        this.context = context;
        this.list = list;
        this.ref = ref;
    }

    @NonNull
    @Override
    public ServicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicesAdapter.ViewHolder holder, int position) {

        String temp = list.get(position).get("title");

        holder.title.setText(temp);

        if(temp.equals("Tractor"))
            holder.icon.setImageResource(R.mipmap.ic_tractor1);
        else if(temp.equals("JCB"))
            holder.icon.setImageResource(R.mipmap.ic_jcb_loader);
        else if(temp.equals("Rotavator"))
            holder.icon.setImageResource(R.mipmap.ic_rotavator);

        holder.removeService.setOnClickListener(view -> {

            if(list.size()>1){
                ref.child("services").child(temp).removeValue();
                Toast.makeText(context,"Service Removed Successfully",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context,"Atleast 1 Service Required",Toast.LENGTH_SHORT).show();
            }

        });

        holder.itemView.setOnClickListener(view -> {

            View dView = LayoutInflater.from(context).inflate(R.layout.service_detail, null);
            EditText charge = dView.findViewById(R.id.service_charge);

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(temp)
                    .setView(dView)
                    .setPositiveButton("Done", (dialogInterface, i) -> {

                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    })
                    .setNeutralButton("Edit", (dialogInterface, i) -> {

                    });
            AlertDialog dialog = builder.create();
            charge.setText(list.get(position).get("charge"));
            dialog.show();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                if(charge.isEnabled()){

                    if(charge.getText().toString().equals("0")){
                        charge.setError("Price Can't Be 0");
                    }else {
                        ref.child("services").child(temp).child("charge").setValue(charge.getText().toString());
                        Toast.makeText(context, "Service Details Updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });

            Button button1 = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            button1.setOnClickListener(view12 -> {
                charge.setEnabled(true);
            });

        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title,removeService;

        ViewHolder(View view) {
            super(view);

            icon = view.findViewById(R.id.service_icon);
            title = view.findViewById(R.id.service_title);
            removeService = view.findViewById(R.id.remove_service);
        }

    }
}
