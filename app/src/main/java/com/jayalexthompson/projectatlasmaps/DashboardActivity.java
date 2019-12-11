package com.jayalexthompson.projectatlasmaps;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.graphics.Color.BLUE;
import static com.jayalexthompson.projectatlasmaps.R.*;
import static com.jayalexthompson.projectatlasmaps.R.drawable.button_inactive;



public class DashboardActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //declaring all variables to be user
    private GoogleMap currentMap;
    private Marker destinationMarker;
    List<Marker> routePointList = new ArrayList<Marker>();
    LatLngBounds.Builder Foundbuilder;
    LatLngBounds.Builder routeBuilder;
    //google's api fragmemt
    private AutocompleteSupportFragment search_bar;
    private boolean mLocationPermissionGranted = false;
    private static final String TAG = DashboardActivity.class.getSimpleName();
    //sets permissions
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private static final float DEFAULT_ZOOM = 21;
    CameraUpdate cu;
    CameraUpdate goToFound;
    public GeoApiContext mGeoApiContext;
    Button route;
    Button save;
    String email;
    TextView information;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    Polyline polyline;
    Button logout;
    Button profile;
    Button history;
    String style;
    String mode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(layout.activity_dashboard);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(id.map);
        mapFragment.getMapAsync(this);
        getLocationPermission();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            mode = "car";
            style = "metric";

            //locate specific user details for email and mode saving
            DocumentReference docRef = database.collection("app_users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            document.getData();

                            //check if user has filled in all metrics
                            if (document.get("email") != null) {
                                email = (document.get("email").toString());
                                style = (document.get("metrics").toString());
                                mode = (document.get("mode").toString());

                            }
                        }
                    }
                }
            });
        }
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //hides all labels and buttons that aren't shown until a trip is created
        route = findViewById(id.routeDisplay);
        save = findViewById(id.saveRoute);
        information = findViewById(id.routeInformation);
        save.setVisibility(View.GONE);
        route.setVisibility(View.GONE);
        information.setVisibility(View.GONE);
        logout = findViewById(id.logOutAction);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            //signs out user from the app
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent home = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(home);
            }
        });
        //opens profile page
        profile = findViewById(id.updateProfileAction);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent update = new Intent(DashboardActivity.this, UpdateProfileActivity.class);
                startActivity(update);
            }
        });
        //opens history page
        history = findViewById(id.viewHistroyAction);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewHistory = new Intent(DashboardActivity.this, HistroyActivity.class);
                startActivity(viewHistory);
            }
        });

        //starts map components
        explorerMap();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //clears and creates a map
        currentMap = googleMap;
        currentMap.clear();
        Log.d("mylog", "Added Markers");

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        if(mGeoApiContext == null){
            //adds server side key needed important
            mGeoApiContext = new GeoApiContext.Builder().apiKey("AIzaSyCvLPUHprLcxcQVMC0R0E4ESZBV5oem8lw").build();

        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                //gets
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {

        if (currentMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                currentMap.setMyLocationEnabled(true);
                currentMap.getUiSettings().setMyLocationButtonEnabled(true);
                currentMap.getUiSettings().setZoomControlsEnabled(true);
            } else {
                //boolean compass = false;
                currentMap.setMyLocationEnabled(false);
                currentMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                currentMap.getUiSettings().setZoomControlsEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = ((Location) task.getResult());
                            currentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            //set default location here
                            currentMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void explorerMap(){

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),"AIzaSyATuPpOtSm7rzJMMHXAfjk6-nNlSe0872s");
        }

        search_bar = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(id.findPlaces);
        search_bar.setPlaceFields(Arrays.asList(Place.Field.ADDRESS,Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = ((Location) task.getResult());
                            int searchArea = 1;
                            if(!search_bar.equals(null)){
                                search_bar.setLocationRestriction(RectangularBounds.newInstance(
                                        new LatLng(mLastKnownLocation.getLatitude() - searchArea, mLastKnownLocation.getLongitude() - 1),
                                        new LatLng(mLastKnownLocation.getLatitude() + searchArea, mLastKnownLocation.getLongitude() + 1)
                                ));
                            }else{
                                ++searchArea;

                            }

                            search_bar.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                                @Override
                                public void onPlaceSelected(@NonNull Place place) {

                                    save.setVisibility(View.GONE);
                                    route.setVisibility(View.GONE);
                                    information.setVisibility(View.GONE);

                                    if (destinationMarker != null) {
                                        destinationMarker.remove();
                                    }
                                    Log.d("Maps", "Place selected: " + place.getName());
                                    Toast.makeText(DashboardActivity.this, place.getName().toString(), Toast.LENGTH_SHORT).show();
                                    destinationMarker = currentMap.addMarker(new MarkerOptions().position(place.getLatLng()).
                                            title("Name: " + place.getName()));

                                    Foundbuilder = new LatLngBounds.Builder();
                                    Foundbuilder.include(place.getLatLng());
                                    int mapPadding = 80;
                                    LatLngBounds FoundBounds = Foundbuilder.build();
                                    goToFound = CameraUpdateFactory.newLatLngBounds(FoundBounds, mapPadding);
                                    currentMap.animateCamera(goToFound);


                                    route.setVisibility(View.VISIBLE);
                                    route.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            routeBuilder = new LatLngBounds.Builder();
                                            routeBuilder .include(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                                            routeBuilder .include(place.getLatLng());
                                            int padding = 80;
                                            LatLngBounds bounds = routeBuilder .build();
                                            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                            currentMap.animateCamera(cu);
                                            routeDirections(destinationMarker);
                                            information.setVisibility(View.VISIBLE);
                                            save.setVisibility(View.VISIBLE);

                                        }
                                    });
                                    currentMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker) {

                                            routePointList.add(destinationMarker);

                                            Toast.makeText(DashboardActivity.this, "It works", Toast.LENGTH_SHORT).show();
                                            return false;
                                        }
                                    });

                                }

                                @Override
                                public void onError(@NonNull Status status) {
                                    Log.d("Maps", "An error occurred: " + status);

                                }
                            });
                        }
                    }
                });
            }
            } catch(SecurityException e)  {
            Toast.makeText(DashboardActivity.this, "broken premissions", Toast.LENGTH_SHORT).show();
                Log.e("Exception: %s", e.getMessage());
            }

    }




    @Override
    public boolean onMarkerClick(Marker marker) {
        //routeDirections(destinationMarker);
        return false;
    }

    private void routeDirections(Marker marker){
        Log.w(TAG, "working ");



        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );

        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        //boolean imperial = false;


        if(style != null){
        if(style.equals("imperial")) {
            directions.units(Unit.IMPERIAL);
        }else{
            directions.units(Unit.METRIC);
            }
        }else{
            directions.units(Unit.METRIC);
        }



        if(mode != null) {
            if (mode.equals("car")) {
                directions.mode(TravelMode.DRIVING);
            } else if (mode.equals("walking")) {
                directions.mode(TravelMode.WALKING);
            } else if (mode.equals("public transport")) {
                directions.mode(TravelMode.TRANSIT);
            } else if (mode.equals("cycle")) {
                directions.mode(TravelMode.BICYCLING);
            } else {
                directions.mode(TravelMode.DRIVING);
            }
        }else {
            directions.mode(TravelMode.DRIVING);
        }

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());

        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
               Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
             Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                Log.d(TAG, "onResult: successfully retrieved directions.");
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }


    private void addPolylinesToMap(final DirectionsResult result){

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                information.setBackgroundColor(Color.BLACK);
                information.setBackgroundColor(Color.WHITE);
                information.setText("Route Distance : " + result.routes[0].legs[0].distance.humanReadable + "\nTravel Time : " + result.routes[0].legs[0].duration.humanReadable + "\n Travel Mode:" + mode);

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        double start = mLastKnownLocation.getLatitude() + mLastKnownLocation.getLongitude();
                        Map<String, Object> route = new HashMap<>();
                        route.put("email", email);
                        route.put("start", result.routes[0].legs[0].startAddress.toString());
                        route.put("end",result.routes[0].legs[0].endAddress.toString());
                        route.put("distance", result.routes[0].legs[0].distance.toString());
                        route.put("time",result.routes[0].legs[0].duration.toString());
                        route.put("date",new Date());
                        route.put("travel mode", mode);
                        route.put("measurement", style);

                        database.collection("app_trips").document()
                        .set(route).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(DashboardActivity.this, "Saved trip", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DashboardActivity.this, "Cannot save trip", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                 Log.d(TAG, "run: latlng: " + latLng.toString());
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }

                    if( polyline != null){
                        polyline.remove();
                    }

                    polyline = currentMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), color.atlasEcho));
                    polyline.setClickable(true);
                }
            }
        });
    }
}
