package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity {

    TextView temp, city, wind, humidity, driversAround;
    ImageView icon;
    CardView weatherCard, driversAroundCard;
    ProgressBar progressBar1, progressBar2;
    FloatingActionButton book;
    ImageView downGif;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    String uId;
    DatabaseReference cRef, dRef;
    ValueEventListener v1;

    LinearLayout currentDriver;
    TextView noOrder;

    String orderStatus, myDriverKey;
    Driver myDriver;

    TextView driverName, driverNumber, status;

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
        driversAround = findViewById(R.id.drivers_around);
        downGif = findViewById(R.id.down_gif);
        uId = getIntent().getStringExtra("uId");
        currentDriver = findViewById(R.id.current_driver);
        noOrder = findViewById(R.id.no_order);
        driverName = findViewById(R.id.driver_name);
        driverNumber = findViewById(R.id.driver_number);
        status = findViewById(R.id.status);

        cRef = FirebaseDatabase.getInstance().getReference("customers/" + uId + "/");
        v1 = cRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("request").exists()) {

                    noOrder.setVisibility(View.GONE);
                    currentDriver.setVisibility(View.VISIBLE);

                    myDriverKey = snapshot.child("request").child("driver_key").getValue().toString();
                    orderStatus = snapshot.child("request").child("status").getValue().toString();

                    dRef = FirebaseDatabase.getInstance().getReference("drivers/" + myDriverKey + "/");

                    dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            myDriver = new Driver(snapshot.child("name").getValue().toString(),
                                    snapshot.child("number").getValue().toString(), "", "");

                            driverName.setText(myDriver.getName());
                            driverNumber.setText(myDriver.getNumber());
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

                    } else if (orderStatus.equals("accepted")){

                        status.setText("Accepted");
                        status.setTextColor(Color.parseColor("#1CCF11"));

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Glide.with(this).load(R.drawable.down_gif).into(downGif);

        checkGPS();

        book.setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, CustomerMapActivity.class);
            intent.putExtra("uId", uId);
            startActivity(intent);

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

    public void getDriverAround() {

        DatabaseReference loc = FirebaseDatabase.getInstance().getReference("drivers_available_loc");
        GeoFire geoFire = new GeoFire(loc);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLongitude(), currentLocation.getLatitude())
                , 10000);
        geoQuery.removeAllListeners();

        final int[] count = {0};

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                count[0]++;
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                driversAround.setText("" + count[0]);

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

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
}