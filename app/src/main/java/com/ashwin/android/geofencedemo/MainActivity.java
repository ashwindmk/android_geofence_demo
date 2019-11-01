package com.ashwin.android.geofencedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<Void> {
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    private static final String TAG = Constants.TAG;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private GeofencingClient mGeofencingClient;

    private PendingIntent mGeofencePendingIntent;

    private ArrayList<Geofence> mGeofenceList;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    private Button mGeofencesButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;

        mGeofencesButton = (Button) findViewById(R.id.geofences_button);
        mGeofencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermissions()) {
                    if (getGeofencesAdded()) {
                        mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
                    } else {
                        mPendingGeofenceTask = PendingGeofenceTask.ADD;
                    }
                    requestPermissions();
                } else {
                    if (getGeofencesAdded()) {
                        removeGeofences();
                    } else {
                        addGeofences();
                    }
                }
            }
        });

        setButtonState();

        populateGeofenceList();

        mGeofencingClient = LocationServices.getGeofencingClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    private boolean checkPermission(String permission) {
        int permissionState = ActivityCompat.checkSelfPermission(this, permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startPermissionRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION});
        } else {
            startPermissionRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void setButtonState() {
        if (getGeofencesAdded()) {
            mGeofencesButton.setText(Constants.REMOVE_GEOFENCES);
        } else {
            mGeofencesButton.setText(Constants.ADD_GEOFENCES);
        }
    }

    private void startPermissionRequest(String[] permissions) {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "on-request-permission-results: " + Arrays.toString(grantResults));
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
                Toast.makeText(MainActivity.this, "Permission cancelled", Toast.LENGTH_LONG).show();
            } else {
                boolean allPermissionsGranted = false;

                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = true;
                    } else {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (allPermissionsGranted) {
                    Log.i(TAG, "All permissions granted");
                    Toast.makeText(MainActivity.this, "All permissions granted", Toast.LENGTH_LONG).show();
                    performPendingGeofenceTask();
                } else {
                    Log.i(TAG, "Insufficient permission");
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.GEOFENCE_LANDMARKS.entrySet()) {
            mGeofenceList.add(new Geofence.Builder()
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
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            Log.e(TAG, "Insufficient permissions");
            Toast.makeText(MainActivity.this, "Insufficient permissions", Toast.LENGTH_LONG).show();
            return;
        }

        Log.w(TAG, "Adding " + mGeofenceList.size() + " geofences...");
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (!checkPermissions()) {
            Log.e(TAG, "Insufficient permissions");
            Toast.makeText(MainActivity.this, "Insufficient permissions", Toast.LENGTH_LONG).show();
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());
            setButtonState();

            String message = getGeofencesAdded() ? "Geofences added" : "Geofences removed";
            Log.d(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.e(TAG, errorMessage);
        }
    }

    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
    }

    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    // Check if location update is working
    private void updateLocation(Context context) {
        FusedLocationProviderClient fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d(TAG, "Updated location: latitude: " + latitude + ", longitude: " + longitude);
                } else {
                    Log.e(TAG, "Location is NULL");
                }
            }
        });
    }
}
