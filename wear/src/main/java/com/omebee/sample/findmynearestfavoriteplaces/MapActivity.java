package com.omebee.sample.findmynearestfavoriteplaces;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.omebee.sample.findmynearestfavoriteplaces.FindPlacesHelper.downloadUrl;

/**
 * Created by phannguyen on 8/11/16.
 */

public class MapActivity extends WearableActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener {
    private static final LatLng SYDNEY = new LatLng(-33.85704, 151.21522);
    private DismissOverlayView mDismissOverlay;
    private GoogleMap mMap;
    private MapFragment mMapFragment;
    List<HashMap<String, String>> nearestPlaces = new ArrayList<>();
    int currentPlaceIndex = 0;
    String currentLat = "", currentLng = "", name = "";
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if(getIntent().getExtras()!=null){
            currentLat = getIntent().getExtras().getString("lat");
            currentLng = getIntent().getExtras().getString("lng");
            name = getIntent().getExtras().getString("name");
        }else{
            //set default value to find restaurants in Sydney
            currentLat = "-33.8670522";
            currentLng = "151.1957362";
            name = "restaurant";
        }
        setContentView(R.layout.map_activity);

        setAmbientEnabled();

        // Retrieve the containers for the root of the layout and the map. Margins will need to be
        // set on them to account for the system window insets.
        final FrameLayout topFrameLayout = (FrameLayout) findViewById(R.id.root_container);
        final FrameLayout mapFrameLayout = (FrameLayout) findViewById(R.id.map_container);
        Button nextPlace = (Button)findViewById(R.id.next);
        nextPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPlaceIndex==nearestPlaces.size()-1){
                    currentPlaceIndex = 0;
                }else{
                    currentPlaceIndex++;
                }
                HashMap<String, String> nearestPlace = nearestPlaces.get(currentPlaceIndex);
                double lat = Double.parseDouble(nearestPlace.get("lat"));
                double lng = Double.parseDouble(nearestPlace.get("lng"));
                Log.d("Nearest Place",lat+"-"+lng+"-"+nearestPlace.get("place_name"));
                LatLng location = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(location)
                        .title(nearestPlace.get("place_name"))).showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17.0f));
            }
        });
        // Set the system view insets on the containers when they become available.
        topFrameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Call through to super implementation and apply insets
                insets = topFrameLayout.onApplyWindowInsets(insets);

                FrameLayout.LayoutParams params =
                        (FrameLayout.LayoutParams) mapFrameLayout.getLayoutParams();

                // Add Wearable insets to FrameLayout container holding map as margins
                params.setMargins(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom());
                mapFrameLayout.setLayoutParams(params);

                return insets;
            }
        });

        // Obtain the DismissOverlayView and display the intro help text.
        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlay.setIntroText("Long click to exit the map.");
        mDismissOverlay.showIntroIfNecessary();

        // Obtain the MapFragment and set the async listener to be notified when the map is ready.
        mMapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        StringBuilder url = new StringBuilder(FindPlacesHelper.getFindPlacesURL(currentLat,currentLng,name));
        FindPlacesTask placesTask = new FindPlacesTask();
        placesTask.execute(url.toString());

    }

    /**
     * Starts ambient mode on the map.
     * The API swaps to a non-interactive and low-color rendering of the map when the user is no
     * longer actively using the app.
     */
    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        mMapFragment.onEnterAmbient(ambientDetails);
    }

    /**
     * Exits ambient mode on the map.
     * The API swaps to the normal rendering of the map when the user starts actively using the app.
     */
    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        mMapFragment.onExitAmbient();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Map is ready to be used.
        mMap = googleMap;

        // Set the long click listener as a way to exit the map.
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // Display the dismiss overlay with a button to exit this activity.
        mDismissOverlay.show();
    }

    private class FindPlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
                while (mMap==null){
                    //waiting map ready
                    Log.d("FindPlacesTask", "waiting map ready...");
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                Log.d("FindPlacesTask Error:", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            Log.d("Result",result);
            nearestPlaces.clear();
            nearestPlaces =  FindPlacesHelper.parseJsonPlacesResults(result);
            if(nearestPlaces.size()>0){
                HashMap<String, String> nearestPlace = nearestPlaces.get(0);
                double lat = Double.parseDouble(nearestPlace.get("lat"));
                double lng = Double.parseDouble(nearestPlace.get("lng"));
                Log.d("Nearest Place",lat+"-"+lng+"-"+nearestPlace.get("place_name"));
                LatLng location = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(location)
                        .title(nearestPlace.get("place_name"))).showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17.0f));
            }
        }
    }
}
