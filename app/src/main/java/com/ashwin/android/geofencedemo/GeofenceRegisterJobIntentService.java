package com.ashwin.android.geofencedemo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Map;

public class GeofenceRegisterJobIntentService extends JobIntentService {
    private static final int JOB_ID = 560;

    static void enqueueWork(Context context, String action) {
        Intent intent = new Intent(action);
        enqueueWork(context, GeofenceRegisterJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Context context = getApplicationContext();

        if (!Utils.checkPermissions(context)) {
            Log.e(Constants.TAG, "Insufficient permission to add/remove geofences");
            return;
        }

        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);

        Intent i = new Intent(context, GeofenceBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        String action = intent.getAction();
        if (Constants.ACTION_ADD.equals(action)) {
            addGeofences(geofencingClient, pendingIntent);
        } else if (Constants.ACTION_REMOVE.equals(action)) {
            removeGeofences(geofencingClient, pendingIntent);
        } else {
            // This should never happen
            Log.e(Constants.TAG, "Invalid action received!");
        }
    }

    private void addGeofences(GeofencingClient geofencingClient, PendingIntent pendingIntent) {
        ArrayList<Geofence> geofenceList = new ArrayList<>();
        for (Map.Entry<String, LatLng> entry : Constants.GEOFENCE_LANDMARKS.entrySet()) {
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        GeofencingRequest geofencingRequest = builder.build();

        Log.w(Constants.TAG, "Adding " + geofenceList.size() + " geofences...");
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Context context = getApplicationContext();
                    if (task.isSuccessful()) {
                        Utils.updateGeofencesAdded(context, true);
                        Log.d(Constants.TAG, "Geofences added successfully");
                    } else {
                        String errorMessage = Utils.getErrorString(task.getException());
                        Log.e(Constants.TAG, errorMessage);
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.GEOFENCE_ACTION_COMPLETE));
                }
            });
    }

    private void removeGeofences(GeofencingClient geofencingClient, PendingIntent pendingIntent) {
        Log.w(Constants.TAG, "Removing all geofences...");
        geofencingClient.removeGeofences(pendingIntent)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Context context = getApplicationContext();
                    if (task.isSuccessful()) {
                        Utils.updateGeofencesAdded(context, false);
                        Log.d(Constants.TAG, "Geofences removed successfully");
                    } else {
                        String errorMessage = Utils.getErrorString(task.getException());
                        Log.e(Constants.TAG, errorMessage);
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.GEOFENCE_ACTION_COMPLETE));                }
            });
    }
}
