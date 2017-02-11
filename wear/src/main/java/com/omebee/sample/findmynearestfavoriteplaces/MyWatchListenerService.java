package com.omebee.sample.findmynearestfavoriteplaces;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import com.google.android.gms.wearable.Node;

/**
 * Created by phannguyen on 8/12/16.
 */

public class MyWatchListenerService  extends WearableListenerService {

    public static final String TAG = "MyWatchListenerService";
    public static final String WEARABLE_DATA_PATH = "/wearable/data/find_my_nearest_favorite_places";


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        DataMap dataMap;
        for(DataEvent dataEvent:dataEvents){
            if(dataEvent.getType()==DataEvent.TYPE_CHANGED){
                String path = dataEvent.getDataItem().getUri().getPath();
                if(path.equalsIgnoreCase(WEARABLE_DATA_PATH)){
                    dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                    Log.v(TAG,"DataMap received on Wearable Device"+dataMap);

                    List<String>places = new ArrayList<>();
                    places.add(dataMap.getString("place1"));
                    places.add(dataMap.getString("place2"));
                    places.add(dataMap.getString("place3"));
                    places.add(dataMap.getString("place4"));

                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor mEditor = sp.edit();
                    Set<String> set = new HashSet<>();
                    set.addAll(places);
                    mEditor.putStringSet("places_data", set);
                    mEditor.apply();//save immediately to memory and save async to disk

                    //restart activity for updating new data
                    Intent startIntent = new Intent(this,MainActivity.class);
                    startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startIntent);

                }
            }
        }

    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.v(TAG,"Peer is Connected");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.v(TAG,"Peer is Disconnected");
    }
}
