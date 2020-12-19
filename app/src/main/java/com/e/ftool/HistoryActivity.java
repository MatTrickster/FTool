package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    HistoryAdapter adapter;
    String uId,user;
    List<HashMap<String,String>> items;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        items = new ArrayList<>();
        uId = getIntent().getStringExtra("uId");
        user = getIntent().getStringExtra("user");
        recyclerView = findViewById(R.id.history_recyclerview);
        adapter = new HistoryAdapter(items);

         layoutManager = new LinearLayoutManager(HistoryActivity.this, LinearLayoutManager.VERTICAL,
                 false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        DatabaseReference reference;

        if(user.equals("c")) {
            reference = FirebaseDatabase.getInstance().getReference("customers/"+uId+"/history/");
        }else{
            reference = FirebaseDatabase.getInstance().getReference("drivers/"+uId+"/history/");
        }

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot snap : snapshot.getChildren()){

                    HashMap<String,String> map = new HashMap<>();
                    map.put("time_date",snap.getKey());
                    map.put("service",snap.child("service").getValue().toString());
                    //map.put("amount",snap.child("amount").getValue().toString());

                    items.add(map);

                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}