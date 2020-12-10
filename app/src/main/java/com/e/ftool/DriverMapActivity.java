package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    TextView back;
    Polyline polyline = null;
    String uId, cId;
    DatabaseReference cRef, ref, reference;
    ValueEventListener v1;
    Customer customer;
    Boolean occupied = false;
    AlertDialog dialog;
    String[] time_distance = new String[2];
    FloatingActionButton completeRide;
    Boolean arrivedToCustomer = false;
    Driver driver;
    LinearLayout bottomSheet;
    Circle dCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        uId = getIntent().getStringExtra("uId");
        ref = FirebaseDatabase.getInstance().getReference("drivers").child(uId);
        cRef = FirebaseDatabase.getInstance().getReference("customers");
        reference = FirebaseDatabase.getInstance().getReference("drivers_available_loc");
        completeRide = findViewById(R.id.complete_ride);

        completeRide.setOnClickListener(view -> {
            if (arrivedToCustomer) {
                endRide();
            } else {
                Toast.makeText(DriverMapActivity.this, "Destination Not Arrived Yet",
                        Toast.LENGTH_SHORT).show();
            }
        });

        v1 = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("services").getChildrenCount() == 0) {

                    Toast.makeText(DriverMapActivity.this, "Add Atleast 1 Service",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(DriverMapActivity.this, ProfileActivity.class);
                    intent.putExtra("uId", uId);
                    startActivity(intent);

                }

                if (snapshot.child("customer_request").exists()) {

                    completeRide.setVisibility(View.VISIBLE);

                    driver = new Driver(snapshot.child("name").getValue().toString(), snapshot.child("number").getValue()
                            .toString(), snapshot.child("customer_request").child("service").getValue().toString(), "0");

                    cId = snapshot.child("customer_request").child("uId").getValue().toString();

                    if (snapshot.child("customer_request").child("status").getValue().toString().equals("requested")) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(DriverMapActivity.this)
                                .setTitle("New Customer!")
                                .setCancelable(false)
                                .setMessage("Name \t\t:\t\t" + snapshot.child("customer_request").child("name")
                                        .getValue().toString() + "\nService \t\t:\t\t" + snapshot.child("customer_request")
                                        .child("service").getValue().toString() + "\nContact \t:\t\t" + snapshot
                                        .child("customer_request").child("contact").getValue().toString())
                                .setPositiveButton("Accept", (dialogInterface, i) -> {

                                    cRef.child(cId).child("request").child("status").setValue("accepted");
                                    ref.child("customer_request").child("status").setValue("accepted");

                                })
                                .setNegativeButton("Decline", (dialogInterface, i) -> {

                                    cRef.child(cId).child("request").child("status").setValue("declined");
                                    ref.child("customer_request").removeValue();

                                    Toast.makeText(DriverMapActivity.this, "Request Declined",
                                            Toast.LENGTH_SHORT).show();

                                });
                        builder.create().show();

                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(DriverMapActivity.this)
                                .setMessage("Fetching Customer Details ...")
                                .setCancelable(false);
                        dialog = builder.create();
                        dialog.show();

                        customer = new Customer(snapshot.child("customer_request").child("name").getValue().toString(),
                                snapshot.child("customer_request").child("contact").getValue().toString(), new
                                LatLng(Double.parseDouble(snapshot.child("customer_request").child("c_lat").getValue().toString()),
                                Double.parseDouble(snapshot.child("customer_request").child("c_lng").getValue().toString())),
                                snapshot.child("customer_request").child("service").getValue().toString(), "0");
                        occupied = true;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        back = findViewById(R.id.back);

        checkGPS();
    }

    public void endRide() {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String time = timestamp.toString();
        time = time.substring(0, time.indexOf("."));

        ref.child("customer_request").removeValue();
        cRef.child(cId).child("current_ride").child(time).setValue(driver);
        cRef.child(cId).child("request").child("status").setValue("completed");
        ref.child("history").child(time).setValue(customer);

        Toast.makeText(DriverMapActivity.this,"Ride Completed!",Toast.LENGTH_SHORT).show();

        map.clear();

        bottomSheet.setVisibility(View.GONE);
        completeRide.setVisibility(View.GONE);
        occupied = false;
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();

            back.setVisibility(View.INVISIBLE);

            if (occupied) {
                dialog.dismiss();
                drawRoute();
                showCustomerDetails();

                if (time_distance[0] != null)
                    if (!time_distance[0].contains("km"))
                        arrivedToCustomer = true;

            } else {

                LatLng l1 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(l1);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_blue_dot));
                markerOptions.anchor(0.5f,0.5f);
                map.clear();
                map.addMarker(markerOptions);

                CircleOptions co = new CircleOptions();
                co.center(l1);
                co.radius(30);
                co.fillColor(0x154D2EFF);
                co.strokeColor(0xee4D2EFF);
                co.strokeWidth(1.0f);
                dCircle = map.addCircle(co);

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(l1, 19));
            }

            GeoFire geoFire = new GeoFire(reference);
            geoFire.setLocation(uId, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
        }
    };

    public void showCustomerDetails() {

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheet.setVisibility(View.VISIBLE);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetBehavior.setHideable(false);

        ImageView imageView = findViewById(R.id.click);
        imageView.setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        });

        TextView customerName = findViewById(R.id.user_sheet_name);
        TextView customerNumber = findViewById(R.id.user_sheet_number);
        TextView customerDistance = findViewById(R.id.user_sheet_distance);

        customerName.setText("Customer Name\t\t\t:\t\t" + customer.getName());
        customerNumber.setText("Customer Number\t\t:\t\t" + customer.getContact());

        if (time_distance[0] == null)
            customerDistance.setText("Fetching Time and Distance ...");
        else
            customerDistance.setText("Distance : " + time_distance[0] + "\t\tETA : " + time_distance[1]);

        Button callCustomer = findViewById(R.id.call_user);
        callCustomer.setOnClickListener(view -> {

            if (ContextCompat.checkSelfPermission(DriverMapActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DriverMapActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 100);

            } else {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + customer.getContact()));
                startActivity(callIntent);
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + customer.getContact()));
            startActivity(callIntent);
        }
    }


    public void checkGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(DriverMapActivity.this, R.raw.map_style));
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
                    ActivityCompat.requestPermissions(DriverMapActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            10);
                    return;
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(v1);
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

    public void drawRoute() {

        map.clear();

        LatLng dLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng cLatLng = customer.getLatLng();

        Location loc1 = new Location("");
        loc1.setLatitude(cLatLng.latitude);
        loc1.setLongitude(cLatLng.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(dLatLng.latitude);
        loc2.setLongitude(dLatLng.longitude);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        builder.include(cLatLng);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

        map.addMarker(new MarkerOptions().position(cLatLng).title("Customer")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup))).setAnchor(0.5f, 0.5f);
        map.addMarker(new MarkerOptions().position(dLatLng).title("Me")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tractor))).setAnchor(0.5f, 0.5f);

        try {
            new TaskDirectionRequest().execute(buildRequestUrl(new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            ), cLatLng)).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.profile) {

            Intent intent = new Intent(DriverMapActivity.this, ProfileActivity.class);
            intent.putExtra("uId", uId);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
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
                    } else if (x == 2) {
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
}