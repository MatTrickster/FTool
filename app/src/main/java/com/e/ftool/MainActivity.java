package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.gesture.GestureLibraries;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    TextView temp, city, wind, humidity, driversAround;
    ImageView icon;
    CardView weatherCard, driversAroundCard;
    FloatingActionButton book;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    String uId;
    DatabaseReference cRef, dRef;
    ValueEventListener v1;
    LinearLayout currentOrder;
    TextView noOrder;
    String orderStatus, myDriverKey;
    Driver myDriver;
    TextView driverName, driverNumber, driverRating, status;
    TextView callDriver, cancelOrder;
    TextView service,land,charge;
    RelativeLayout viewDriver;
    String driverImgUrl = "null";
    CircleImageView driverImage;
    LocationRequest locationRequest;
    LocationCallback mLocationCallback;
    ImageView info;
    ProgressBar progressBar;
    LinearLayout serviceBottomSheet;
    Button go,hide;
    View dim;
    BottomSheetBehavior bottomSheetBehavior;
    ChipGroup chipGroup;
    String desiredService;
    ImageView statusIcon;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("login",MODE_PRIVATE);
        temp = findViewById(R.id.temp);
        city = findViewById(R.id.city);
        wind = findViewById(R.id.wind);
        humidity = findViewById(R.id.humidity);
        icon = findViewById(R.id.icon);
        weatherCard = findViewById(R.id.weather_card);
        book = findViewById(R.id.book);
        driversAroundCard = findViewById(R.id.drivers_around_card);
        driversAround = findViewById(R.id.drivers_around);
        uId = getIntent().getStringExtra("uId");
        currentOrder = findViewById(R.id.current_order);
        noOrder = findViewById(R.id.no_order);
        driverName = findViewById(R.id.driver_name);
        driverNumber = findViewById(R.id.driver_number);
        driverRating = findViewById(R.id.driver_rating);
        status = findViewById(R.id.status);
        callDriver = findViewById(R.id.call_driver);
        cancelOrder = findViewById(R.id.cancel_order);
        driverImage = findViewById(R.id.driver_img);
        viewDriver = findViewById(R.id.view_driver);
        info = findViewById(R.id.info);
        service = findViewById(R.id.service);
        land = findViewById(R.id.land);
        charge = findViewById(R.id.charge);
        progressBar = findViewById(R.id.progress);
        serviceBottomSheet = findViewById(R.id.services_bottom_sheet);
        go = findViewById(R.id.go);
        hide = findViewById(R.id.hide);
        dim = findViewById(R.id.dim);
        chipGroup = findViewById(R.id.chip_group);
        statusIcon = findViewById(R.id.status_icon);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId != -1) {
                Chip chip = group.findViewById(checkedId);
                desiredService = "" + chip.getText().toString();
            }else
                desiredService = null;

        });

        bottomSheetBehavior = BottomSheetBehavior.from(serviceBottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    book.setImageResource(R.drawable.cancel);
                    dim.setVisibility(View.VISIBLE);
                }else {
                    book.setImageResource(R.drawable.book);
                    dim.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        cRef = FirebaseDatabase.getInstance().getReference("customers/" + uId + "/");
        v1 = cRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                orderStatus = null;

                if (snapshot.child("request").exists()) {

                    noOrder.setVisibility(View.GONE);
                    currentOrder.setVisibility(View.VISIBLE);

                    myDriverKey = snapshot.child("request").child("driver_key").getValue().toString();
                    orderStatus = snapshot.child("request").child("status").getValue().toString();

                    if(orderStatus.equals("requested"))
                        statusIcon.setImageResource(R.drawable.ic_requested);
                    else if(orderStatus.equals("accepted"))
                        statusIcon.setImageResource(R.drawable.ic_accepted);

                    dRef = FirebaseDatabase.getInstance().getReference("drivers/" + myDriverKey + "/");

                    dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.child("photo_url").exists())
                                driverImgUrl = snapshot.child("photo_url").getValue().toString();

                            myDriver = new Driver(snapshot.child("name").getValue().toString(),
                                    snapshot.child("number").getValue().toString(), "",
                                    snapshot.child("rating").getValue().toString(), driverImgUrl,
                                    snapshot.child("history").getChildrenCount());

                            if(orderStatus.equals("requested") || orderStatus.equals("accepted")) {
                                String s = snapshot.child("customer_request").child("service").getValue().toString();
                                land.setText(snapshot.child("customer_request").child("land").getValue().toString());
                                charge.setText(snapshot.child("services").child(s).child("charge").getValue().toString());
                                service.setText(s);
                            }

                            driverName.setText(myDriver.getName());
                            driverNumber.setText(myDriver.getNumber());
                            driverRating.setText(myDriver.getRating());

                            if (!driverImgUrl.equals("null"))
                                Glide.with(getApplicationContext()).load(driverImgUrl).into(driverImage);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    if (orderStatus.equals("requested")) {

                        status.setText("Requested");
                        status.setTextColor(Color.parseColor("#ED9410"));

                    } else if (orderStatus.equals("declined")) {

                        status.setText("Declined");
                        status.setTextColor(Color.parseColor("#DC1F1F"));

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Request Declined!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", (dialogInterface, i) -> {
                                    cRef.child("request").removeValue();

                                    noOrder.setVisibility(View.VISIBLE);
                                    currentOrder.setVisibility(View.GONE);
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } else if (orderStatus.equals("accepted")) {

                        status.setText("Accepted");
                        status.setTextColor(Color.parseColor("#1CCF11"));

                    } else if (orderStatus.equals("completed")) {

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                        RatingBar ratingBar = new RatingBar(MainActivity.this);
                        ratingBar.setLayoutParams(lp);
                        ratingBar.setNumStars(5);
                        ratingBar.setRating(3);
                        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                        linearLayout.addView(ratingBar);
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Ride Completed!")
                                .setMessage("Please Rate Driver!")
                                .setCancelable(false)
                                .setView(linearLayout)
                                .setPositiveButton("Rate", (dialogInterface, i) -> {

                                    String time = " ";
                                    for (DataSnapshot temp : snapshot.child("current_ride").getChildren()) {
                                        time = temp.getKey();
                                    }

                                    dRef.child("history").child(time).child("rating").setValue(String.valueOf(ratingBar.getRating()));

                                    double rat = Float.parseFloat(myDriver.getRating()) * myDriver.getrCount();
                                    rat = rat + ratingBar.getRating();
                                    rat = rat / (myDriver.getrCount() + 1);
                                    dRef.child("rating").setValue(rat);

                                    cRef.child("request").removeValue();
                                    cRef.child("history").child(time).setValue(snapshot.child("current_ride").child(time).getValue());
                                    cRef.child("history").child(time).child("rating").setValue(String.valueOf(ratingBar.getRating()));
                                    cRef.child("current_ride").removeValue();

                                    noOrder.setVisibility(View.VISIBLE);
                                    currentOrder.setVisibility(View.GONE);
                                })
                                .setNegativeButton("No", (dialogInterface, i) -> {

                                    String time = " ";
                                    for (DataSnapshot temp : snapshot.child("current_ride").getChildren()) {
                                        time = temp.getKey();
                                    }

                                    cRef.child("request").removeValue();
                                    cRef.child("history").child(time).setValue(snapshot.child("current_ride").child(time).getValue());
                                    cRef.child("current_ride").removeValue();

                                    noOrder.setVisibility(View.VISIBLE);
                                    currentOrder.setVisibility(View.GONE);
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        checkGPS();
        attachServices();

        info.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("It is the total number of drivers available providing all types of services" +
                    " in the range of 50 KM.")
                    .setPositiveButton("OK", (dialogInterface, i) -> { });
            builder.create().show();

        });

        go.setOnClickListener(view -> {
            if (desiredService == null) {

                Toast.makeText(MainActivity.this, "Select Desired Service", Toast.LENGTH_SHORT).show();

            } else {

                Intent intent = new Intent(MainActivity.this, CustomerMapActivity.class);
                intent.putExtra("uId", uId);
                intent.putExtra("requested", orderStatus != null);
                intent.putExtra("service", desiredService);
                startActivity(intent);
            }

        });

        hide.setOnClickListener(view -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        book.setOnClickListener(view -> {

            if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        callDriver.setOnClickListener(view -> {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 100);

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Confirm Call?");
                builder.setPositiveButton("Call", (dialogInterface, i) -> {

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + myDriver.getNumber()));
                    startActivity(callIntent);

                }).setNegativeButton("No", (dialogInterface, i) -> { });
                builder.create().show();
            }

        });

        cancelOrder.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Confirm Cancel?");
            builder.setPositiveButton("Yes", (dialogInterface, i) -> {

                if (orderStatus.equals("requested")) {

                    cRef.child("request").removeValue();

                    noOrder.setVisibility(View.VISIBLE);
                    currentOrder.setVisibility(View.GONE);
                } else {
                    Toast.makeText(MainActivity.this, "Order is Accepted, Cannot be cancelled Now!",
                            Toast.LENGTH_SHORT).show();
                }

            }).setNegativeButton("No", (dialogInterface, i) -> { });
            builder.create().show();

        });

        viewDriver.setOnClickListener(view -> {

            if (orderStatus.equals("accepted")) {

                Intent intent = new Intent(MainActivity.this, CustomerMapActivity.class);
                intent.putExtra("uId", uId);
                intent.putExtra("riding", true);
                intent.putExtra("driver_key", myDriverKey);
                startActivity(intent);

            } else
                Toast.makeText(MainActivity.this, "Order Not Accepted Yet", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.customer_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.logout){

            sp.edit().putBoolean("logged",false).apply();

            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkGPS() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response =
                        task.getResult(ApiException.class);

                if(response.getLocationSettingsStates().isGpsPresent()){

                    fetchLocation();
                }
            } catch (ApiException ex) {
                switch (ex.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException =
                                    (ResolvableApiException) ex;
                            resolvableApiException
                                    .startResolutionForResult(MainActivity.this,
                                            100);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    public void attachServices() {

        FirebaseDatabase.getInstance().getReference("services_available/")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Chip chip = new Chip(MainActivity.this);
                            ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(MainActivity.this,
                                    null,
                                    0,
                                    R.style.Widget_MaterialComponents_Chip_Choice);
                            chip.setChipDrawable(chipDrawable);
                            chip.setText(snap.getValue().toString());
                            chipGroup.addView(chip);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void getDriverAround() {

        final int[] count = {0};

        DatabaseReference loc = FirebaseDatabase.getInstance().getReference("drivers_available_loc");
        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {

                    double lat = Double.parseDouble(snap.child("lat").getValue().toString());
                    double lng = Double.parseDouble(snap.child("lng").getValue().toString());

                    Location location = new Location("");
                    location.setLatitude(lat);
                    location.setLongitude(lng);

                    float dis = currentLocation.distanceTo(location);
                    if (dis < 50000) {
                        count[0]++;
                    }
                }

                driversAround.setText("" + count[0]);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        driversAroundCard.setVisibility(View.VISIBLE);
    }

    public void updateWeather(Double lat, Double lng) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://api.weatherapi.com/v1/current.json?key=388fc46389584136a5b92028200912&q=" + lat + "," + lng;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {

            weatherCard.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            try {
                JSONObject jsonObject = new JSONObject(response);

                JSONObject location = jsonObject.getJSONObject("location");
                city.setText(location.getString("name"));

                JSONObject current = jsonObject.getJSONObject("current");

                String t = current.getString("temp_c");
                String is_day = current.getString("is_day");

                temp.setText(t.substring(0, t.indexOf(".")) + "°");
                wind.setText(current.get("wind_kph") + " km/h");
                humidity.setText(current.get("humidity") + " %");

                if (is_day.equals("1"))
                    icon.setImageResource(R.drawable.ic_day);
                else
                    icon.setImageResource(R.drawable.ic_night);


            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("TAG", "y " + e);
            }

        }, error -> {
        });

        queue.add(request);
    }

    public void fetchLocation(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10);

            return;
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult.getLastLocation() == null)
                    return;
                currentLocation = locationResult.getLastLocation();

                fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

                updateWeather(currentLocation.getLatitude(), currentLocation.getLongitude());

                getDriverAround();
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback , Looper.myLooper());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, "Location Enabled", Toast.LENGTH_SHORT).show();

            fetchLocation();
        }
        else if(requestCode == 100 && resultCode == RESULT_CANCELED){
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 10 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            fetchLocation();
        }

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + myDriver.getNumber()));
            startActivity(callIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(v1 != null)
            cRef.removeEventListener(v1);
    }

    public void History(View v){

        Intent intent = new Intent(MainActivity.this,HistoryActivity.class);
        intent.putExtra("uId",uId);
        intent.putExtra("user","c");
        startActivity(intent);

    }

    public void Profile(View v){

        Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
        intent.putExtra("uId",uId);
        intent.putExtra("user","c");
        startActivity(intent);

    }
}