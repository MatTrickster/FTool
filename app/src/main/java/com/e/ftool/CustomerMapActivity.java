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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
    TextView back,driversAroundText;
    List<LatLng> t = new ArrayList<LatLng>();
    List<Marker> driversAround = new ArrayList<>();
    Button request;
    String uId;
    Marker cMarker;
    DatabaseReference cRef;
    static Polyline polyline = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        back = findViewById(R.id.back);
        driversAroundText = findViewById(R.id.drivers_around);
        request = findViewById(R.id.request);
        uId = getIntent().getStringExtra("uId");

        cRef = FirebaseDatabase.getInstance().getReference("customers/" + uId + "/");

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (request.getText().equals("Request")) {

                    request.setText("Getting Your Driver");
                    cRef.child("request").setValue("requested");
                    //getClosestDriver();
                }

            }
        });

        checkGPS();

    }

    Boolean driverFound = false;
    String driverId = null;

    public void getClosestDriver() {

        final DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("drivers_available_loc");

        GeoFire geoFire = new GeoFire(driversRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(),
                currentLocation.getLongitude()), 25);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                if (!driverFound) {

                    final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("drivers/" +
                            key + "/");

                    dRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.child("occupied").getValue().toString().equals("false")) {

                                if(driverFound)
                                    return;

                                for(Marker temp : driversAround){
                                    temp.remove();
                                }

                                driversAroundText.setVisibility(View.GONE);

                                driverFound = true;
                                driverId = snapshot.getKey();

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("u_id", uId);
                                map.put("d_lat", currentLocation.getLatitude());
                                map.put("d_lng", currentLocation.getLongitude());
                                dRef.child("customer_request").updateChildren(map);
                                dRef.child("occupied").setValue(false);

                                cRef.child("request").setValue("accepted");

                                getDriverLocation();
                                //getDriverInfo();
                                //getHasRideEnded();
                                request.setText("Looking for Driver Location....");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

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

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private LatLng driverLatLng;

    public void getDriverLocation() {

        driverLocationRef = FirebaseDatabase.getInstance().getReference("drivers_available_loc/" +
                driverId + "/").child("l");

        driverLocationRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double locationLat = Double.parseDouble(snapshot.child("0").getValue().toString());
                double locationLng = Double.parseDouble(snapshot.child("1").getValue().toString());

                driverLatLng = new LatLng(locationLat, locationLng);
                if (mDriverMarker != null) {
                    mDriverMarker.remove();
                }

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

                float distance = loc1.distanceTo(loc2);

                if (distance < 100) {
                    request.setText("Driver's Here");
                } else {
                    request.setText("Driver Found: " + String.valueOf(distance) + " meters");
                }

                mDriverMarker = map.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tractor)));

                try {
                    new TaskDirectionRequest().execute(buildRequestUrl(new LatLng(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude()
                    ), driverLatLng)).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        driverLocationRef.addValueEventListener(driverLocationRefListener);

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

    public static class TaskDirectionRequest extends AsyncTask<String, Void, String> {

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

    public static class TaskParseDirection extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
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
                    if (x <= 2) {
                        Log.i("TAG", "" + point);
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    points.add(new LatLng(lat, lng));
                }
                polylineOptions.addAll(points);
                polylineOptions.width(14f);
                polylineOptions.color(Color.BLACK);
                polylineOptions.geodesic(true);
            }
            if (polyline != null)
                polyline.remove();
            if (polylineOptions != null) {
                polyline = map.addPolyline(polylineOptions);
            }
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
        cMarker = map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        cMarker.setTag("customer");

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

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();

            LatLng l1 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(l1);
            markerOptions.title("Im here");

            if (cMarker.getId() != null)
                cMarker.remove();

            cMarker = map.addMarker(markerOptions);
            cMarker.setTag("customer");
            if (!driverFound) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(l1, 17));
                getDriversAround();
                addDrivers();
            }

            back.setVisibility(View.INVISIBLE);

        }
    };

    public void addDrivers(){

        for(Marker temp : driversAround){
            temp.remove();
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));

        for(int i=0;i<t.size();i++) {

            builder.include(t.get(i));
            MarkerOptions options = new MarkerOptions();
            options.position(t.get(i));
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tractor));
            Marker marker = map.addMarker(options);
            driversAround.add(marker);

        }

        if(t.size()!=0)
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),100));

        driversAroundText.setText("Drivers Around : "+t.size());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
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

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                if (!t.contains(driverLocation))
                    t.add(driverLocation);

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

}




