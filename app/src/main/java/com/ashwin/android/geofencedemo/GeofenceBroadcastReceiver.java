package com.ashwin.android.geofencedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.TAG, "Received geofence broadcast");
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent);
    }
}
