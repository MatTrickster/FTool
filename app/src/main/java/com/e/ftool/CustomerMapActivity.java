package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    static GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    ArrayList<Driver> drivers = new ArrayList<>();
    List<Marker> driversMarkers = new ArrayList<>();
    String uId;
    Marker cMarker;
    DatabaseReference cRef, driversRef, driverLocationRef;
    DataSnapshot driversSnap, customerSnap;
    static Polyline polyline = null, polyline1 = null, polyline2 = null;
    String driverSelectedKey;
    Boolean riding;
    ValueEventListener v1, v2;
    private LatLng driverLatLng;
    String[] time_distance = new String[2];
    Boolean alreadyRequested = false;
    String desiredService;
    LocationCallback mLocationCallback;
    TextView driversAround;
    Spinner rangeSpinner;
    String range = "5";
    Circle cCircle;
    AlertDialog.Builder builder;
    Dialog dialog;
    MaterialCardView filter;
    LinearLayout bottomSheet;
    CardView driversBottomCard;
    RecyclerView driversList;
    ChipGroup chipGroup;
    DriversAdapter driversAdapter;
    RecyclerView.LayoutManager layoutManager;
    BottomSheetBehavior driversBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        uId = getIntent().getStringExtra("uId");
        riding = getIntent().getBooleanExtra("riding", false);
        driverSelectedKey = getIntent().getStringExtra("driver_key");
        alreadyRequested = getIntent().getBooleanExtra("requested", false);
        desiredService = getIntent().getStringExtra("service");
        driversAround = findViewById(R.id.drivers_around);
        rangeSpinner = findViewById(R.id.range_spinner);
        filter = findViewById(R.id.filter);
        driversBottomCard = findViewById(R.id.drivers_card);
        driversList = findViewById(R.id.drivers_list);
        chipGroup = findViewById(R.id.chip_group);
        layoutManager = new LinearLayoutManager(CustomerMapActivity.this, LinearLayoutManager.VERTICAL,
                false);
        driversBottomSheet = BottomSheetBehavior.from(driversBottomCard);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if(checkedId == R.id.l_to_h){
                drivers.sort((driver, t1) -> {

                    int charge1 = Integer.parseInt(driver.getServiceCharge());
                    int charge2 = Integer.parseInt(t1.getServiceCharge());

                    return charge1-charge2;
                });

            }else if(checkedId == R.id.h_rating){

                drivers.sort((driver, t1) -> {

                    int charge1 = Integer.parseInt(driver.getServiceCharge());
                    int charge2 = Integer.parseInt(t1.getServiceCharge());

                    return charge2-charge1;
                });

            }

            driversBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

            driversAdapter = new DriversAdapter(CustomerMapActivity.this,drivers,map,driversBottomSheet);
            driversList.setAdapter(driversAdapter);

        });

        rangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                range = rangeSpinner.getSelectedItem().toString();
                range = range.substring(0, range.indexOf(" "));

                builder = new AlertDialog.Builder(CustomerMapActivity.this);

                if (riding) {
                    filter.setVisibility(View.GONE);
                    builder.setMessage("Getting Driver Location ...");
                } else {
                    builder.setMessage("Getting Drivers ...");
                }

                dialog = builder.create();
                dialog.show();
                fetchLocation(range);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
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

                if (snapshot.child("request").exists())
                    if (snapshot.child("request").child("status").getValue().toString().equals("completed"))
                        finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void request(String service, String key, String land) {

        if (driversSnap.child(key).child("services").child(service).getChildrenCount() == 0) {
            Toast.makeText(CustomerMapActivity.this, "Service Not Available For Selected Driver",
                    Toast.LENGTH_SHORT).show();

        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapActivity.this);
            builder.setTitle("Confirm Request?");
            builder.setPositiveButton("Request", (dialogInterface, i) -> {

                HashMap<String, Object> map = new HashMap<>();
                map.put("c_lat", currentLocation.getLatitude());
                map.put("c_lng", currentLocation.getLongitude());
                map.put("service", service);
                map.put("land", land);
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
                finish();

            }).setNegativeButton("Cancel", (dialogInterface, i) -> {
            });

        }
    }

    public void getDriversAround(String range) {

        drivers.clear();
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
                    if (dis < Integer.parseInt(range) * 1000) {

                        boolean desiredServiceMatches = false;
                        String serviceCharge = "";

                        LatLng driverLocation = new LatLng(lat, lng);

                        for (DataSnapshot snap1 : driversSnap.child(snap.getKey()).child("services").getChildren()) {

                            if (snap1.getKey().toString().equals(desiredService)) {
                                desiredServiceMatches = true;
                                serviceCharge = driversSnap.child(snap.getKey()).child("services").child(desiredService)
                                        .child("charge").getValue().toString();
                            }
                        }

                        if (!desiredServiceMatches) {
                            continue;
                        }

                        Driver driver = new Driver(driverLocation, serviceCharge,
                                driversSnap.child(snap.getKey()).child("name").getValue().toString(),
                                driversSnap.child(snap.getKey()).child("number").getValue().toString(), snap.getKey(),
                                driversSnap.child(snap.getKey()).child("photo_url").getValue().toString());

                        drivers.add(driver);

                    }

                }

                driversAround.setText("" + drivers.size());
                addDriversToMap();

                driversBottomCard.setVisibility(View.VISIBLE);

                driversAdapter = new DriversAdapter(CustomerMapActivity.this,drivers,map,driversBottomSheet);
                driversList.setLayoutManager(layoutManager);
                driversList.setAdapter(driversAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void addDriversToMap() {

        for (Marker m : driversMarkers) {
            m.remove();
        }
        driversMarkers.clear();

        LatLngBounds.Builder builderB = new LatLngBounds.Builder();
        builderB.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

        for (int i = 0; i < drivers.size(); i++) {

            builderB.include(drivers.get(i).getLocation());
            MarkerOptions options = new MarkerOptions();
            options.position(drivers.get(i).getLocation());
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tractor));
            Marker marker = map.addMarker(options);
            marker.setTag(drivers.get(i).getKey());
            driversMarkers.add(marker);
        }

        dialog.dismiss();

        if (drivers.size() != 0)
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builderB.build(), 100));
        else {
            builder = new AlertDialog.Builder(CustomerMapActivity.this);
            builder.setMessage("No Driver Found!\nTry Increasing range.")
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                    });
            dialog = builder.create();
            dialog.show();
        }

    }

    public void fetchLocation(String range) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CustomerMapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10);

            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLastLocation() == null)
                    return;
                currentLocation = locationResult.getLastLocation();

                if (riding) {
                    drawRoute();
                } else {

                    fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

                    if (cMarker != null)
                        cMarker.remove();
                    if (cCircle != null)
                        cCircle.remove();

                    LatLng l1 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(l1);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_blue_dot));
                    markerOptions.anchor(0.5f, 0.5f);

                    cMarker = map.addMarker(markerOptions);
                    cMarker.setTag("Customer");

                    CircleOptions co = new CircleOptions();
                    co.center(l1);
                    co.radius(30);
                    co.fillColor(0x154D2EFF);
                    co.strokeColor(0xee4D2EFF);
                    co.strokeWidth(1.0f);
                    cCircle = map.addCircle(co);
                    cCircle.setTag("Customer");

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(l1, 19));
                    getDriversAround(range);

                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

    }

    public void showDriverDetails() {

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheet.setVisibility(View.VISIBLE);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(false);
        ImageView imageView = findViewById(R.id.click);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    imageView.setImageResource(R.drawable.ic_down);
                else
                    imageView.setImageResource(R.drawable.ic_up);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        imageView.setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        });

        TextView driverName = findViewById(R.id.user_sheet_name);
        TextView driverNumber = findViewById(R.id.user_sheet_number);
        TextView driverDistance = findViewById(R.id.user_sheet_distance);

        driverName.setText("Customer Name\t\t\t:\t\t"
                + driversSnap.child(driverSelectedKey).child("name").getValue().toString());
        driverNumber.setText("Customer Number\t\t:\t\t"
                + driversSnap.child(driverSelectedKey).child("number").getValue().toString());

        if (time_distance[0] == null)
            driverDistance.setText("Fetching Time and Distance ...");
        else
            driverDistance.setText("Distance : " + time_distance[0] + "\t\tETA : " + time_distance[1]);

        Button callCustomer = findViewById(R.id.call_user);
        callCustomer.setOnClickListener(view -> {

            if (ContextCompat.checkSelfPermission(CustomerMapActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CustomerMapActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 100);

            } else {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"
                        + driversSnap.child(driverSelectedKey).child("number").getValue().toString()));
                startActivity(callIntent);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(CustomerMapActivity.this, R.raw.map_style));

        map.setOnMarkerClickListener(marker -> {

            if (marker.getTag().equals("Customer"))
                return false;

            AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapActivity.this);
            View v = getLayoutInflater().inflate(R.layout.driver_details, null);
            TextView dName = v.findViewById(R.id.driver_name);
            TextView dNumber = v.findViewById(R.id.driver_number);
            CircleImageView dImg = v.findViewById(R.id.driver_img);
            TextView service = v.findViewById(R.id.service);
            TextView sCharge = v.findViewById(R.id.service_charge);
            TextInputEditText land = v.findViewById(R.id.land);
            Button request = v.findViewById(R.id.request);

            for (int i = 0; i < drivers.size(); i++) {

                if (drivers.get(i).getKey().equals(marker.getTag())) {

                    driverSelectedKey = drivers.get(i).getKey();
                    dName.setText(drivers.get(i).getName());
                    dNumber.setText(drivers.get(i).getNumber());
                    service.setText(desiredService);
                    sCharge.setText(drivers.get(i).getServiceCharge());
                    Glide.with(CustomerMapActivity.this).load(drivers.get(i).getImgUrl())
                            .placeholder(R.drawable.ic_person)
                            .into(dImg);

                    break;
                }
            }

            builder.setView(v);
            builder.create().show();

            request.setOnClickListener(view -> {

                if (alreadyRequested) {
                    Toast.makeText(CustomerMapActivity.this, "Already One Ongoing Order",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (land.getText().length() == 0)
                        land.setError("Fill Detail");
                    else if (land.getText().toString().equals("0"))
                        land.setError("Field Can't be zero");
                    else if (Integer.parseInt(land.getText().toString()) > 10)
                        land.setError("Land Can't be more than 10");
                    else
                        request(desiredService, driverSelectedKey, land.getText().toString());
                }

            });

            return false;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + driversSnap
                    .child(driverSelectedKey).child("number").getValue().toString()));
            startActivity(callIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (v1 != null)
            cRef.removeEventListener(v1);

        if (v2 != null)
            driverLocationRef.removeEventListener(v2);

        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    public void drawRoute() {

        map.clear();
        if (dialog != null)
            dialog.dismiss();

        driverLocationRef = FirebaseDatabase.getInstance().getReference("drivers_available_loc/" +
                driverSelectedKey + "/");

        v2 = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double locationLat = Double.parseDouble(snapshot.child("lat").getValue().toString());
                double locationLng = Double.parseDouble(snapshot.child("lng").getValue().toString());

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
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 140));

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
                    Log.i("TAG", "" + s);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
            PolylineOptions polylineOptions = null, polylineOptions1 = null, polylineOptions2 = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                polylineOptions1 = new PolylineOptions();
                polylineOptions2 = new PolylineOptions();
                int x = 0;
                for (HashMap<String, String> point : path) {

                    Log.i("TAG", point + "\nx " + time_distance[0]);
                    x++;
                    if (x == 1) {
                        time_distance[0] = point.get("distance");
                        continue;
                    } else if (x == 2) {
                        time_distance[1] = point.get("duration");

                        double sLat = Double.parseDouble(point.get("s_lat"));
                        double sLng = Double.parseDouble(point.get("s_lng"));
                        double eLat = Double.parseDouble(point.get("e_lat"));
                        double eLng = Double.parseDouble(point.get("e_lng"));

                        if (sLat == eLat && sLng == eLng)
                            continue;

                        LatLng l1 = new LatLng(sLat, sLng);
                        LatLng l2 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                        LatLng l3 = new LatLng(eLat, eLng);
                        LatLng l4 = new LatLng(driverLatLng.latitude, driverLatLng.longitude);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(l1);
                        builder.include(l2);
                        builder.include(l3);
                        builder.include(l4);
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 140));

                        polylineOptions2.add(l3);
                        polylineOptions2.add(l4);
                        polylineOptions2.width(10f);
                        polylineOptions2.color(Color.parseColor("#72bcd4"));
                        polylineOptions2.geodesic(true);

                        polylineOptions1.add(l1);
                        polylineOptions1.add(l2);
                        polylineOptions1.width(10f);
                        polylineOptions1.color(Color.parseColor("#72bcd4"));
                        polylineOptions1.geodesic(true);

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

                showDriverDetails();
            }

            if (polyline != null)
                polyline.remove();
            if (polyline1 != null)
                polyline1.remove();
            if (polyline2 != null)
                polyline2.remove();
            if (polylineOptions != null) {
                polyline = map.addPolyline(polylineOptions);
            }

            List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(10));

            if (polylineOptions1 != null) {
                polyline1 = map.addPolyline(polylineOptions1);
                polyline1.setPattern(pattern);
            }
            if (polylineOptions2 != null) {
                polyline2 = map.addPolyline(polylineOptions2);
                polyline2.setPattern(pattern);
            }
        }
    }
}
