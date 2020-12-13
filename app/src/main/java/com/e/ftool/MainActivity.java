package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    static int selectedServicePos = -1;
    TextView temp, city, wind, humidity, driversAround;
    ImageView icon;
    CardView weatherCard, driversAroundCard;
    ProgressBar progressBar1, progressBar2, progressBar3;
    FloatingActionButton book;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    String uId;
    DatabaseReference cRef, dRef;
    ValueEventListener v1;

    LinearLayout currentDriver;
    TextView noOrder;

    String orderStatus, myDriverKey;
    Driver myDriver;

    TextView driverName, driverNumber, driverRating, status;
    TextView callDriver, cancelOrder;

    RelativeLayout viewDriver;
    String driverImgUrl = "null";
    CircleImageView driverImage;

    ArrayList<HashMap<String,String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temp = findViewById(R.id.temp);
        city = findViewById(R.id.city);
        wind = findViewById(R.id.wind);
        humidity = findViewById(R.id.humidity);
        icon = findViewById(R.id.icon);
        weatherCard = findViewById(R.id.weather_card);
        progressBar1 = findViewById(R.id.progress1);
        book = findViewById(R.id.book);
        driversAroundCard = findViewById(R.id.drivers_around_card);
        progressBar2 = findViewById(R.id.progress2);
        progressBar3 = findViewById(R.id.progress3);
        driversAround = findViewById(R.id.drivers_around);
        uId = getIntent().getStringExtra("uId");
        currentDriver = findViewById(R.id.current_driver);
        noOrder = findViewById(R.id.no_order);
        driverName = findViewById(R.id.driver_name);
        driverNumber = findViewById(R.id.driver_number);
        driverRating = findViewById(R.id.driver_rating);
        status = findViewById(R.id.status);
        callDriver = findViewById(R.id.call_driver);
        cancelOrder = findViewById(R.id.cancel_order);
        driverImage = findViewById(R.id.driver_img);
        viewDriver = findViewById(R.id.view_driver);

        attachServices();

        cRef = FirebaseDatabase.getInstance().getReference("customers/" + uId + "/");
        v1 = cRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                orderStatus = null;

                if (snapshot.child("request").exists()) {

                    noOrder.setVisibility(View.GONE);
                    currentDriver.setVisibility(View.VISIBLE);

                    myDriverKey = snapshot.child("request").child("driver_key").getValue().toString();
                    orderStatus = snapshot.child("request").child("status").getValue().toString();

                    dRef = FirebaseDatabase.getInstance().getReference("drivers/" + myDriverKey + "/");

                    dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.child("photo_url").exists())
                                driverImgUrl = snapshot.child("photo_url").getValue().toString();

                            myDriver = new Driver(snapshot.child("name").getValue().toString(),
                                    snapshot.child("number").getValue().toString(), "",
                                    snapshot.child("rating").getValue().toString(),driverImgUrl);

                            driverName.setText(myDriver.getName());
                            driverNumber.setText(myDriver.getNumber());
                            driverRating.setText(myDriver.getRating());

                            if(!driverImgUrl.equals("null"))
                                Glide.with(getApplicationContext()).load(driverImgUrl).into(driverImage);

                            progressBar3.setVisibility(View.GONE);
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
                                    currentDriver.setVisibility(View.GONE);
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
                                    cRef.child("request").removeValue();
                                    cRef.child("history").child(time).setValue(snapshot.child("current_ride").child(time).getValue());
                                    cRef.child("history").child(time).child("rating").setValue(String.valueOf(ratingBar.getRating()));
                                    cRef.child("current_ride").removeValue();

                                    noOrder.setVisibility(View.VISIBLE);
                                    currentDriver.setVisibility(View.GONE);
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
                                    currentDriver.setVisibility(View.GONE);
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                } else {
                    progressBar3.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        checkGPS();

        book.setOnClickListener(view -> {

            if(selectedServicePos == -1){

                Toast.makeText(MainActivity.this,"Select Desired Service",Toast.LENGTH_SHORT).show();

            }else {

                Intent intent = new Intent(MainActivity.this, CustomerMapActivity.class);
                intent.putExtra("uId", uId);
                intent.putExtra("requested", orderStatus != null);
                intent.putExtra("service",list.get(selectedServicePos).get("title"));
                startActivity(intent);
            }

        });

        callDriver.setOnClickListener(view -> {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 100);

            } else {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + myDriver.getNumber()));
                startActivity(callIntent);
            }

        });

        cancelOrder.setOnClickListener(view -> {

            if (orderStatus.equals("requested")) {

                cRef.child("request").removeValue();

                noOrder.setVisibility(View.VISIBLE);
                currentDriver.setVisibility(View.GONE);
            } else {
                Toast.makeText(MainActivity.this, "Order is Accepted, Cannot be cancelled Now!",
                        Toast.LENGTH_SHORT).show();
            }

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

    public void attachServices(){

        list = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.services_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        ServicesAdapter services = new ServicesAdapter(MainActivity.this,list,null,"c");
        recyclerView.setAdapter(services);

        FirebaseDatabase.getInstance().getReference("services_available/")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot snap : snapshot.getChildren()){

                            HashMap<String,String> map = new HashMap<>();
                            map.put("title",snap.getValue().toString());
                            list.add(map);

                        }

                        services.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();

            updateWeather(currentLocation.getLatitude(), currentLocation.getLongitude());

            getDriverAround();
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + myDriver.getNumber()));
            startActivity(callIntent);
        }
    }

    public void getDriverAround() {

        final int[] count = {0};

        DatabaseReference loc = FirebaseDatabase.getInstance().getReference("drivers_available_loc");
        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot snap : snapshot.getChildren()){

                    double lat = Double.parseDouble(snap.child("lat").getValue().toString());
                    double lng = Double.parseDouble(snap.child("lng").getValue().toString());

                    Location location = new Location("");
                    location.setLatitude(lat);
                    location.setLongitude(lng);

                    Log.i("TAG","x "+snap.getKey());

                    float dis = currentLocation.distanceTo(location);
                    if(dis<10000){
                        count[0]++;
                    }
                }

                driversAround.setText("" + count[0]);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        progressBar2.setVisibility(View.GONE);
        driversAroundCard.setVisibility(View.VISIBLE);
    }

    public void updateWeather(Double lat, Double lng) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://api.weatherapi.com/v1/current.json?key=388fc46389584136a5b92028200912&q=" + lat + "," + lng;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {

            progressBar1.setVisibility(View.GONE);
            weatherCard.setVisibility(View.VISIBLE);

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

    public void checkGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, Enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) ->
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("No", (dialog, id) -> finish());
            final AlertDialog alert = builder.create();
            alert.show();

        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(this, "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(60000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            10);
                    return;
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cRef.removeEventListener(v1);
    }

    public void Profile(View v){

        Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
        intent.putExtra("uId",uId);
        intent.putExtra("user","c");
        startActivity(intent);

    }
}