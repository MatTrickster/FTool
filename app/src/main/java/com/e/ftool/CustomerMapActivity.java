package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    static GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    TextView back, driversAroundText, driverSelectedName;
    ArrayList<Driver> drivers = new ArrayList<>();
    List<Marker> driversMarkers = new ArrayList<>();
    Button request;
    String uId;
    Marker cMarker;
    Circle cCircle;
    DatabaseReference cRef, driversRef, driverLocationRef;
    DataSnapshot driversSnap, customerSnap;
    static Polyline polyline = null;
    String driverSelectedKey;
    LinearLayout requestLayout, driverSelectedLayout;
    ImageView removeSelectedDriver;
    AlertDialog dialog;
    Boolean riding = false;
    ValueEventListener v1, v2;
    private LatLng driverLatLng;
    String []time_distance = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        Objects.requireNonNull(this.getSupportActionBar()).hide();

        request = findViewById(R.id.request);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        back = findViewById(R.id.back);
        driversAroundText = findViewById(R.id.drivers_around);
        uId = getIntent().getStringExtra("uId");
        requestLayout = findViewById(R.id.request_layout);
        driverSelectedName = findViewById(R.id.driver_selected_name);
        driverSelectedLayout = findViewById(R.id.driver_selected_layout);
        removeSelectedDriver = findViewById(R.id.remove_selected_driver);

        request.setOnClickListener(view -> {

            RadioGroup radioGroup = findViewById(R.id.radioGroup);
            RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());

            String serviceRequested = radioButton.getText().toString();

            request(serviceRequested, driverSelectedKey);
        });

        removeSelectedDriver.setOnClickListener(view -> {
            driverSelectedLayout.setVisibility(View.GONE);
            requestLayout.setVisibility(View.GONE);
        });

        driversRef = FirebaseDatabase.getInstance().getReference("drivers/");
        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driversSnap = snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cRef = FirebaseDatabase.getInstance().getReference("customers/" + uId + "/");
        v1 = cRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                customerSnap = snapshot;

                if (snapshot.child("request").exists()) {
                    if (snapshot.child("request").child("status").getValue().toString().equals("requested")) {

                        if(dialog!=null)
                            dialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapActivity.this)
                                .setMessage("Request Sent To Driver.\nGetting Confirmation From Driver ...")
                                .setCancelable(false)
                                .setPositiveButton("Cancel", (dialogInterface, i) -> {
                                    driversRef.child(snapshot.child("request")
                                            .child("driver_key").getValue().toString())
                                            .child("customer_request").removeValue();
                                    cRef.child("request").removeValue();
                                    Toast.makeText(CustomerMapActivity.this, "Request Cancelled", Toast.LENGTH_SHORT)
                                            .show();

                                });
                        dialog = builder.create();
                        dialog.show();

                    } else if (snapshot.child("request").child("status").getValue().toString().equals("declined")) {

                        if(dialog!=null)
                            dialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapActivity.this)
                                .setMessage("Request Declined!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", (dialogInterface, i) -> {
                                    cRef.child("request").removeValue();
                                });
                        dialog = builder.create();
                        dialog.show();
                    } else if(snapshot.child("request").child("status").getValue().toString().equals("accepted")) {

                        checkGPS();

                        if(dialog!=null)
                            dialog.dismiss();

                        driverSelectedLayout.setVisibility(View.GONE);
                        requestLayout.setVisibility(View.GONE);
                        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapActivity.this)
                                .setMessage("Request Accepted!\nGetting Driver Details ...")
                                .setCancelable(false);
                        dialog = builder.create();
                        dialog.show();

                        driverSelectedKey = snapshot.child("request").child("driver_key").getValue().toString();
                        riding = true;
                    }else if(snapshot.child("request").child("status").getValue().toString().equals("completed")){

                        if(dialog!=null)
                            dialog.dismiss();

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        LinearLayout linearLayout = new LinearLayout(CustomerMapActivity.this);
                        RatingBar ratingBar = new RatingBar(CustomerMapActivity.this);
                        ratingBar.setLayoutParams(lp);
                        ratingBar.setNumStars(5);
                        ratingBar.setRating(3);
                        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                        linearLayout.addView(ratingBar);
                        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapActivity.this)
                                .setTitle("Ride Completed!")
                                .setMessage("Please Rate Driver!")
                                .setCancelable(false)
                                .setView(linearLayout)
                                .setPositiveButton("Rate", (dialogInterface, i) -> {

                                    String time = " ";
                                    for(DataSnapshot temp :snapshot.child("current_ride").getChildren()) {
                                        time = temp.getKey();
                                    }

                                    driversRef.child(snapshot.child("request").child("driver_key").getValue().toString())
                                            .child("history").child(time).child("rating")
                                            .setValue(String.valueOf(ratingBar.getRating()));
                                    cRef.child("request").removeValue();
                                    cRef.child("history").child(time).setValue(snapshot.child("current_ride").child(time).getValue());
                                    cRef.child("history").child(time).child("rating").setValue(String.valueOf(ratingBar.getRating()));
                                    cRef.child("current_ride").removeValue();

                                    map.clear();

                                })
                                .setNegativeButton("No", (dialogInterface, i) -> {

                                    String time = " ";
                                    for(DataSnapshot temp :snapshot.child("current_ride").getChildren()) {
                                        time = temp.getKey();
                                    }

                                    cRef.child("request").removeValue();
                                    cRef.child("history").child(time).setValue(snapshot.child("current_ride").child(time).getValue());
                                    cRef.child("current_ride").removeValue();

                                    map.clear();
                                });
                        dialog = builder.create();
                        dialog.show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        checkGPS();
    }

    public void request(String service, String key) {

        if (driversSnap.child(key).child("services").child(service).getChildrenCount() == 0) {
            Toast.makeText(CustomerMapActivity.this, "Service Not Available For Selected Driver",
                    Toast.LENGTH_SHORT).show();

        } else {

            HashMap<String, Object> map = new HashMap<>();
            map.put("c_lat", currentLocation.getLatitude());
            map.put("c_lng", currentLocation.getLongitude());
            map.put("service", service);
            map.put("contact", customerSnap.child("number").getValue().toString());
            map.put("name", customerSnap.child("name").getValue().toString());
            map.put("uId", uId);
            map.put("status", "requested");

            driversRef.child(key).child("customer_request").setValue(map);

            HashMap<String, String> map1 = new HashMap<>();
            map1.put("status", "requested");
            map1.put("driver_key", key);
            cRef.child("request").setValue(map1);

            Toast.makeText(CustomerMapActivity.this, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
        }

    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();

            back.setVisibility(View.INVISIBLE);

            if (riding) {
                driversAroundText.setVisibility(View.GONE);
                dialog.dismiss();
                drawRoute();
                showDriverDetails();
            } else {

                LatLng l1 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(l1);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_blue_dot));
                markerOptions.anchor(0.5f,0.5f);

                if (cMarker.getId() != null)
                    cMarker.remove();

                if(cCircle.getId() != null)
                    cCircle.remove();

                cMarker = map.addMarker(markerOptions);
                cMarker.setTag("customer");

                CircleOptions co = new CircleOptions();
                co.center(l1);
                co.radius(30);
                co.fillColor(0x154D2EFF);
                co.strokeColor(0xee4D2EFF);
                co.strokeWidth(1.0f);
                cCircle = map.addCircle(co);
                cCircle.setTag("customer");

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(l1, 19));
                getDriversAround();
                addDrivers();
                driversAroundText.setVisibility(View.VISIBLE);

            }


        }
    };

    public void showDriverDetails() {

        LinearLayout linearLayout = findViewById(R.id.bottom_sheet);
        linearLayout.setVisibility(View.VISIBLE);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);

        bottomSheetBehavior.setHideable(false);

        ImageView imageView = findViewById(R.id.click);
        imageView.setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                imageView.setImageResource(R.drawable.ic_down);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                imageView.setImageResource(R.drawable.ic_up);
            }
        });

        TextView driverName = findViewById(R.id.user_sheet_name);
        TextView driverNumber = findViewById(R.id.user_sheet_number);
        TextView driverDistance = findViewById(R.id.user_sheet_distance);

        if (time_distance[0] == null)
           driverDistance.setText("Fetching Time and Distance ...");
        else
            driverDistance.setText("Distance : " + time_distance[0] + "\t\tETA : " + time_distance[1]);

        driverName.setText("Driver Name\t\t\t:\t\t"+driversSnap
                .child(driverSelectedKey).child("name").getValue().toString());
        driverNumber.setText("Driver Number\t\t:\t\t"+driversSnap
                .child(driverSelectedKey).child("number").getValue().toString());

        Button callDriver = findViewById(R.id.call_user);
        callDriver.setOnClickListener(view -> {

            if (ContextCompat.checkSelfPermission(CustomerMapActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CustomerMapActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},100);

            }else {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + driversSnap
                        .child(driverSelectedKey).child("number").getValue().toString()));
                startActivity(callIntent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + driversSnap
                    .child(driverSelectedKey).child("number").getValue().toString()));
            startActivity(callIntent);
        }
    }

    public void getDriversAround() {

        final DatabaseReference loc = FirebaseDatabase.getInstance().getReference("drivers_available_loc");
        final GeoFire geoFire = new GeoFire(loc);
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLongitude(), currentLocation.getLatitude())
                , 10000);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for (Driver d : drivers) {
                    if (d.getKey().equals(key))
                        return;
                }

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);
                ArrayList<HashMap<String, String>> service = new ArrayList<>();

                for (DataSnapshot snap : driversSnap.child(key).child("services").getChildren()) {
                    HashMap<String, String> m = new HashMap<>();
                    m.put("title", snap.getKey());
                    m.put("charge", snap.child("charge").getValue().toString());
                    service.add(m);
                }

                Driver driver = new Driver(driverLocation, service, driversSnap.child(key).child("name").getValue().toString(),
                        driversSnap.child(key).child("number").getValue().toString(), key);

                drivers.add(driver);

            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });

    }

    public void addDrivers() {

        for (Marker temp : driversMarkers) {
            temp.remove();
        }
        driversMarkers.clear();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

        for (int i = 0; i < drivers.size(); i++) {

            builder.include(drivers.get(i).getLocation());
            MarkerOptions options = new MarkerOptions();
            options.position(drivers.get(i).getLocation());
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tractor));
            Marker marker = map.addMarker(options);
            marker.setTag(drivers.get(i).getKey());
            driversMarkers.add(marker);

        }

        CustomMarkerInfoWindowView markerWindowView = new CustomMarkerInfoWindowView(CustomerMapActivity.this, drivers);
        map.setInfoWindowAdapter(markerWindowView);

        map.setOnInfoWindowClickListener(marker -> {
            for (int i = 0; i < driversMarkers.size(); i++) {
                if (driversMarkers.get(i).getTag().equals(marker.getTag())) {
                    driverSelectedKey = drivers.get(i).getKey();
                    requestLayout.setVisibility(View.VISIBLE);
                    driverSelectedName.setText("Driver Selected : " + drivers.get(i).getName());
                    driverSelectedLayout.setVisibility(View.VISIBLE);
                    break;
                }
            }
        });

        if (drivers.size() != 0)
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

        driversAroundText.setText("Drivers Around : " + drivers.size());
    }

    public void drawRoute() {

        map.clear();

        driverLocationRef = FirebaseDatabase.getInstance().getReference("drivers_available_loc/" +
                driverSelectedKey + "/").child("l");

        v2 = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double locationLat = Double.parseDouble(snapshot.child("0").getValue().toString());
                double locationLng = Double.parseDouble(snapshot.child("1").getValue().toString());

                driverLatLng = new LatLng(locationLat, locationLng);

                Location loc1 = new Location("");
                loc1.setLatitude(currentLocation.getLatitude());
                loc1.setLongitude(currentLocation.getLongitude());

                Location loc2 = new Location("");
                loc2.setLatitude(driverLatLng.latitude);
                loc2.setLongitude(driverLatLng.longitude);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                builder.include(driverLatLng);
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

                map.addMarker(new MarkerOptions().position(driverLatLng).title("Driver")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tractor)));
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .title("Me").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

                try {
                    String s = new TaskDirectionRequest().execute(buildRequestUrl(new LatLng(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude()
                    ), driverLatLng)).get();
                    Log.i("TAG",""+s);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        cMarker = map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        cMarker.setTag("customer");

        cCircle = map.addCircle(new CircleOptions().center(new LatLng(0,0)));
        cCircle.setTag("customer");

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(CustomerMapActivity.this,R.raw.map_style));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(7000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CustomerMapActivity.this,
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

    private static String requestDirection(String requestedUrl) {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(requestedUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            responseString = stringBuffer.toString();
            bufferedReader.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        httpURLConnection.disconnect();
        return responseString;
    }

    private String buildRequestUrl(LatLng origin, LatLng destination) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDestination = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";

        String param = strOrigin + "&" + strDestination + "&" + sensor + "&" + mode;
        String output = "json";
        String APIKEY = getResources().getString(R.string.google_maps_key);

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param + "&key=" + APIKEY;
        Log.d("TAG", url);
        return url;
    }

    public class TaskDirectionRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String responseString) {

            super.onPostExecute(responseString);
            //Json object parsing
            TaskParseDirection parseResult = new TaskParseDirection();
            parseResult.execute(responseString);
        }
    }

    public class TaskParseDirection extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonString) {
            List<List<HashMap<String, String>>> routes = null;
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(jsonString[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);
            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                int x = 0;
                for (HashMap<String, String> point : path) {

                    x++;
                    if (x == 1) {
                        time_distance[0] = point.get("distance");
                        continue;
                    }
                    else if(x == 2){
                        time_distance[1] = point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    points.add(new LatLng(lat, lng));
                }
                polylineOptions.addAll(points);
                polylineOptions.width(14f);
                polylineOptions.color(Color.parseColor("#72bcd4"));
                polylineOptions.geodesic(true);
            }
            if (polyline != null)
                polyline.remove();
            if (polylineOptions != null) {
                polyline = map.addPolyline(polylineOptions);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cRef.removeEventListener(v1);
        driverLocationRef.removeEventListener(v2);
    }
}
