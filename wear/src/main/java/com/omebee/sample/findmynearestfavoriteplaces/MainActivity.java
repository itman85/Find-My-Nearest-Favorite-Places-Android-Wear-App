package com.omebee.sample.findmynearestfavoriteplaces;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableRecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends WearableActivity implements PlacesListAdapter.ItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private TextView mTextView;
    ArrayList<String> places = new ArrayList<String>() {{
        add("cinema");
        add("cafe");
        add("restaurant");
        add("pub");
    }};
    private GoogleApiClient googleApiClient;
    private WearableRecyclerView wearableRecyclerView;
    String currentLat = "", currentLng = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customlist_layout);
        wearableRecyclerView = (WearableRecyclerView) findViewById(R.id.wearable_list);
        wearableRecyclerView.setHasFixedSize(true);
        /*WearableRecyclerView.OffsettingHelper offsettingHelper = new WearableRecyclerView.OffsettingHelper() {
            @Override
            public void updateChild(View view, WearableRecyclerView wearableRecyclerView) {

            }
        };
        wearableRecyclerView.setOffsettingHelper(offsettingHelper);*/
        //load data from shared preference
        getPlacesDataFromSharedPref();
        PlacesListAdapter exampleAdapter = new PlacesListAdapter(MainActivity.this, places);
        wearableRecyclerView.setAdapter(exampleAdapter);

        exampleAdapter.setListener(this);

        if(!hasGPSHardware()){
            Toast.makeText(getApplicationContext(), "Android watch hardware does not have GPS, so it will using gps from phone", Toast.LENGTH_SHORT).show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 2911);
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public void onItemSelected(int position) {
        //Toast.makeText(this, places.get(position), Toast.LENGTH_LONG).show();
        if(currentLng.isEmpty() || currentLng.isEmpty()){
            Toast.makeText(this,"Cannot get current location",Toast.LENGTH_LONG).show();
        }else {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            intent.putExtra("name", places.get(position));
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(googleApiClient.isConnected()){
            stopLocationUpdates();
        }
        googleApiClient.disconnect();

    }

    private boolean hasGPSHardware() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, MainActivity.this);
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.getStatus().isSuccess()) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed in requesting location updates", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this,"No permission to get location",Toast.LENGTH_LONG).show();
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        String locationMsg = "Location is null";
        if(location!=null){
            currentLat = Double.toString(location.getLatitude());
            currentLng = Double.toString(location.getLongitude());
            locationMsg = "Update location : "+ currentLat+","+currentLng;
        }
        Log.d("Location Update",locationMsg);
        //Toast.makeText(this,locationMsg,Toast.LENGTH_LONG).show();
    }

    private void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        startLocationUpdates();
    }

    private void getPlacesDataFromSharedPref(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> set = sp.getStringSet("places_data", null);
        if(set!=null && set.size()>0) {
            places.clear();
            places.addAll(set);
        }else {
            places = new ArrayList<String>() {{
                add("cinema");
                add("cafe");
                add("restaurant");
                add("pub");
            }};
        }
    }


}
