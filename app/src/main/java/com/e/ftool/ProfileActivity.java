package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    TextView noService;
    EditText name, number;
    String uId;
    Button addService;
    RecyclerView recyclerView;
    ArrayList<HashMap<String, String>> services = new ArrayList<>();
    DataSnapshot snap;
    DatabaseReference reference;
    String user;
    LinearLayout servicesLayout;
    ServicesAdapter adapter;
    CircleImageView image;
    TextView changePhoto;
    Uri photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uId = getIntent().getStringExtra("uId");
        user = getIntent().getStringExtra("user");

        recyclerView = findViewById(R.id.services_recycler);
        addService = findViewById(R.id.add_service);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        noService = findViewById(R.id.no_service);
        servicesLayout = findViewById(R.id.services);
        image = findViewById(R.id.image);
        changePhoto = findViewById(R.id.change_photo);

        if (user.equals("d")) {
            reference = FirebaseDatabase.getInstance().getReference("drivers/" + uId + "/");

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new ServicesAdapter(ProfileActivity.this, services, reference,"d");
            recyclerView.setAdapter(adapter);

            addService.setOnClickListener(view -> {

                View dView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.add_service, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Select Service")
                        .setView(dView)
                        .setPositiveButton("ADD", (dialogInterface, i) -> {
                        })
                        .setNeutralButton("Cancel", (dialogInterface, i) -> {
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {

                    EditText priceEdit = dView.findViewById(R.id.service_price);
                    Spinner spinner = dView.findViewById(R.id.service_spinner);

                    if (priceEdit.getText().length() == 0) {
                        priceEdit.setError("Field is Empty");
                    } else if (priceEdit.getText().toString().equals("0")) {
                        priceEdit.setError("Price Can't Be 0");
                    } else {

                        String text = spinner.getSelectedItem().toString();

                        for (DataSnapshot s1 : snap.child("services").getChildren()) {
                            if (s1.getKey().equals(text)) {
                                Toast.makeText(ProfileActivity.this, "Service Already Available",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        Map<String, Object> map = new HashMap<>();
                        map.put("charge", priceEdit.getText().toString());
                        reference.child("services").child(text).setValue(map);

                        Toast.makeText(ProfileActivity.this, "Service Added Successfully",
                                Toast.LENGTH_SHORT).show();

                        dialog.dismiss();


                    }

                });

            });
        } else {
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

                if (snapshot.child("photo_url").exists()) {
                    photoUrl = Uri.parse(snapshot.child("photo_url").getValue().toString());

                    Glide.with(getApplicationContext()).load(photoUrl)
                            .placeholder(R.drawable.ic_person).into(image);
                }

                if (user.equals("d")) {

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

        image.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

            ImageView imageView = new ImageView(ProfileActivity.this);
            Glide.with(ProfileActivity.this).load(photoUrl).into(imageView);

            LinearLayout linearLayout = new LinearLayout(ProfileActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(params);
            linearLayout.setLayoutParams(params);
            linearLayout.addView(imageView);
            builder.setView(linearLayout);

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        changePhoto.setOnClickListener(view -> {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image ..."),
                    100);

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            photoUrl = data.getData();

            if (photoUrl != null) {

                ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
                progressDialog.setTitle("Uploading ...");
                progressDialog.show();

                Bitmap bmp = null;
                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                byte[] imgData = baos.toByteArray();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference("Users Photo/" +
                        uId + "/");

                storageReference.putBytes(imgData).addOnSuccessListener(taskSnapshot ->

                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                            reference.child("photo_url").setValue(uri.toString());
                            progressDialog.dismiss();

                            Toast.makeText(ProfileActivity.this, "Photo Updated Successfully!", Toast.LENGTH_SHORT).show();

                        }).addOnFailureListener(e -> {

                            Toast.makeText(ProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                            progressDialog.dismiss();

                        })

                ).addOnFailureListener(e -> {

                    Toast.makeText(ProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }).addOnProgressListener(taskSnapshot -> {

                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Completed - " + (int) progress + " %");

                });

            } else {
                Toast.makeText(ProfileActivity.this, "No Photo Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (user.equals("d")) {
            if (services.size() < 1)
                Toast.makeText(ProfileActivity.this, "Atleast 1 Service Required", Toast.LENGTH_SHORT).show();
            else
                super.onBackPressed();
        }
        else
            super.onBackPressed();

    }
}