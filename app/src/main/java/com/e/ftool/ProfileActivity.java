package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class ProfileActivity extends AppCompatActivity {

    TextView noService;
    EditText name, number;
    String uId;
    Button addService;
    RecyclerView recyclerView;
    ArrayList<HashMap<String,String>> services = new ArrayList<>();
    DataSnapshot snap;
    DatabaseReference reference;
    String user;
    LinearLayout servicesLayout;
    ServicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uId = getIntent().getStringExtra("uId");
        recyclerView = findViewById(R.id.services_recycler);
        addService = findViewById(R.id.add_service);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        noService = findViewById(R.id.no_service);
        servicesLayout = findViewById(R.id.services);

        user = getIntent().getStringExtra("user");
        if(user.equals("d")) {
            reference = FirebaseDatabase.getInstance().getReference("drivers/" + uId + "/");

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new ServicesAdapter(ProfileActivity.this,services,reference);
            recyclerView.setAdapter(adapter);
        }
        else {
            reference = FirebaseDatabase.getInstance().getReference("customers/" + uId + "/");
            servicesLayout.setVisibility(View.GONE);
        }

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                services.clear();

                snap = snapshot;

                name.setText(snapshot.child("name").getValue().toString());
                number.setText(snapshot.child("number").getValue().toString());

                if(user.equals("d")) {

                    if (snapshot.child("services").getChildrenCount() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        noService.setVisibility(View.VISIBLE);
                    } else {

                        for (DataSnapshot snap : snapshot.child("services").getChildren()) {

                            HashMap<String, String> hash = new HashMap<>();
                            hash.put("charge", snap.child("charge").getValue().toString());
                            hash.put("title", snap.getKey());

                            services.add(hash);

                        }

                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);
                        noService.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        addService.setOnClickListener(view -> {

            View dView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.add_service, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this)
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
                            Toast.makeText(ProfileActivity.this,"Service Already Available",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("charge", priceEdit.getText().toString());
                    reference.child("services").child(text).setValue(map);

                    Toast.makeText(ProfileActivity.this,"Service Added Successfully",
                            Toast.LENGTH_SHORT).show();

                    dialog.dismiss();


                }

            });

        });
    }

    @Override
    public void onBackPressed() {

        if(user.equals("d")) {

            if (services.size() < 1) {

                Toast.makeText(ProfileActivity.this, "Atleast 1 Service Required", Toast.LENGTH_SHORT).show();

            } else {
                super.onBackPressed();
            }
        }

        super.onBackPressed();
    }
}