package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DriverProfileActivity extends AppCompatActivity {

    TextView noService;
    EditText driverName, driverNumber;
    String uId;
    Button addService;
    RecyclerView recyclerView;
    ArrayList<HashMap<String,String>> services = new ArrayList<>();
    DataSnapshot snap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        uId = getIntent().getStringExtra("uId");
        recyclerView = findViewById(R.id.services_recycler);
        addService = findViewById(R.id.add_service);
        driverName = findViewById(R.id.driver_name);
        driverNumber = findViewById(R.id.driver_number);
        noService = findViewById(R.id.no_service);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("drivers/" + uId + "/");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        ServicesAdapter adapter = new ServicesAdapter(DriverProfileActivity.this,services,reference);
        recyclerView.setAdapter(adapter);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                services.clear();

                snap = snapshot;

                driverName.setText(snapshot.child("name").getValue().toString());
                driverNumber.setText(snapshot.child("number").getValue().toString());

                if (snapshot.child("services").getChildrenCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    noService.setVisibility(View.VISIBLE);
                }else{

                    for(DataSnapshot snap : snapshot.child("services").getChildren()){

                        HashMap<String, String> hash = new HashMap<>();
                        hash.put("charge",snap.child("charge").getValue().toString());
                        hash.put("title",snap.getKey());

                        services.add(hash);

                    }

                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    noService.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        addService.setOnClickListener(view -> {

            View dView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.add_service, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(DriverProfileActivity.this)
                    .setTitle("Select Service")
                    .setView(dView)
                    .setPositiveButton("ADD", (dialogInterface, i) -> { })
                    .setNeutralButton("Cancel", (dialogInterface, i) -> { });

            AlertDialog dialog = builder.create();
            dialog.show();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {

                EditText priceEdit = dView.findViewById(R.id.service_price);
                Spinner spinner = dView.findViewById(R.id.service_spinner);

                if (priceEdit.getText().length() == 0) {
                    priceEdit.setError("Field is Empty");
                }else if(priceEdit.getText().toString().equals("0")) {
                    priceEdit.setError("Price Can't Be 0");
                }
                else
                {

                    String text = spinner.getSelectedItem().toString();

                    for(DataSnapshot s1 : snap.child("services").getChildren()){
                        if(s1.getKey().equals(text)){
                            Toast.makeText(DriverProfileActivity.this,"Service Already Available",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("charge", priceEdit.getText().toString());
                    reference.child("services").child(text).setValue(map);

                    Toast.makeText(DriverProfileActivity.this,"Service Added Successfully",
                            Toast.LENGTH_SHORT).show();

                    dialog.dismiss();


                }

            });

        });
    }

    @Override
    public void onBackPressed() {

        if(services.size()<1){

            Toast.makeText(DriverProfileActivity.this,"Atleast 1 Service Required",Toast.LENGTH_SHORT).show();

        }else{
            super.onBackPressed();
        }
    }
}