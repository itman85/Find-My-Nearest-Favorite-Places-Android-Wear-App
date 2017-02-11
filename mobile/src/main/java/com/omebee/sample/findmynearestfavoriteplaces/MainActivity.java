package com.omebee.sample.findmynearestfavoriteplaces;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleApiClient = null;
    public static final String TAG = "MainActivity_PHONE";
    public static final String WEARABLE_DATA_PATH = "/wearable/data/find_my_nearest_favorite_places";
    EditText place1;
    EditText place2;
    EditText place3;
    EditText place4;

    ArrayList<String> places = new ArrayList<String>() {{
        add("cinema");
        add("cafe");
        add("restaurant");
        add("pub");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        place1 = (EditText) findViewById(R.id.place1);
        place2 = (EditText) findViewById(R.id.place2);
        place3 = (EditText) findViewById(R.id.place3);
        place4 = (EditText) findViewById(R.id.place4);

        Button updateBtn = (Button) findViewById(R.id.update);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(place1.getText().toString().isEmpty() || place2.getText().toString().isEmpty()
                        || place3.getText().toString().isEmpty() || place4.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Please input your 4 favorite places",Toast.LENGTH_LONG).show();
                }else {
                    places.clear();
                    places.add(place1.getText().toString());
                    places.add(place2.getText().toString());
                    places.add(place3.getText().toString());
                    places.add(place4.getText().toString());
                    //save to shared preference
                    savePlacesDataToSharedPref();
                    //send to wear for updating
                    sendDataMapToDataLayer();
                }
            }
        });
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(Wearable.API);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        googleApiClient = builder.build();
        //load data from shared preference
        getPlacesDataFromSharedPref();
        //
        place1.setText(places.get(0));
        place2.setText(places.get(1));
        place3.setText(places.get(2));
        place4.setText(places.get(3));
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if(googleApiClient!=null && googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    private DataMap createDataMap(){
        DataMap dataMap = new DataMap();
        dataMap.putString("place1",places.get(0));
        dataMap.putString("place2",places.get(1));
        dataMap.putString("place3",places.get(2));
        dataMap.putString("place4",places.get(3));
        return dataMap;
    }
    public void sendDataMapToDataLayer(){
        if(googleApiClient.isConnected()){
            DataMap dataMap = createDataMap();
            new ProcessSendingDataMapToDataLayer(WEARABLE_DATA_PATH,dataMap).start();
        }
        else{
            Log.v(TAG,"Connection is closed");
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
       // sendDataMapToDataLayer();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class ProcessSendingDataMapToDataLayer extends  Thread {
        String path;
        DataMap dataMap;

        public ProcessSendingDataMapToDataLayer(String path, DataMap dataMap) {
            this.path = path;
            this.dataMap = dataMap;
        }

        @Override
        public void run() {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(WEARABLE_DATA_PATH);
            putDataMapRequest.getDataMap().putAll(dataMap);

            PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
            DataApi.DataItemResult dataItemResult = Wearable.DataApi.putDataItem(googleApiClient,putDataRequest).await();
            if (dataItemResult.getStatus().isSuccess()) {
                //print success log
                Log.v(TAG, "DataItem: successfully sent");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Update data to watch successfully",Toast.LENGTH_LONG).show();
                    }
                });

            } else {
                // print failure log
                Log.v(TAG, "Error while sending the DataItem");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Update data to watch failed",Toast.LENGTH_LONG).show();
                    }
                });

            }
        }
    }

    private void savePlacesDataToSharedPref(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEditor = sp.edit();
        Set<String> set = new HashSet<>();
        set.addAll(places);
        mEditor.putStringSet("places_data", set);
        mEditor.apply();
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
