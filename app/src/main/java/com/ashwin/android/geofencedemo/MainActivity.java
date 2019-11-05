package com.ashwin.android.geofencedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = Constants.TAG;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private String mPendingGeofenceTask = null;

    private Button mGeofencesButton = null;

    private LocalBroadcastManager mLocalBroadcastManager = null;

    // Geofence add/remove on-complete listener
    private BroadcastReceiver completeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setButtonState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mGeofencesButton = (Button) findViewById(R.id.geofences_button);

        mGeofencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean areGeofencesAdded = Utils.getGeofencesAdded(getApplicationContext());
                if (!Utils.checkPermissions(getApplicationContext())) {
                    if (areGeofencesAdded) {
                        mPendingGeofenceTask = Constants.ACTION_REMOVE;
                    } else {
                        mPendingGeofenceTask = Constants.ACTION_ADD;
                    }
                    requestPermissions();
                } else {
                    if (areGeofencesAdded) {
                        removeGeofences();
                    } else {
                        addGeofences();
                    }
                }
            }
        });

        setButtonState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocalBroadcastManager.registerReceiver(completeListener, new IntentFilter(Constants.GEOFENCE_ACTION_COMPLETE));

        if (!Utils.checkPermissions(getApplicationContext())) {
            requestPermissions();
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
        if (Utils.getGeofencesAdded(getApplicationContext())) {
            mGeofencesButton.setText(Constants.ACTION_REMOVE);
        } else {
            mGeofencesButton.setText(Constants.ACTION_ADD);
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

    private void performPendingGeofenceTask() {
        if (Constants.ACTION_ADD.equals(mPendingGeofenceTask)) {
            addGeofences();
        } else if (Constants.ACTION_REMOVE.equals(mPendingGeofenceTask)) {
            removeGeofences();
        }
    }

    private void addGeofences() {
        GeofenceRegisterJobIntentService.enqueueWork(getApplicationContext(), Constants.ACTION_ADD);
    }

    private void removeGeofences() {
        GeofenceRegisterJobIntentService.enqueueWork(getApplicationContext(), Constants.ACTION_REMOVE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocalBroadcastManager.unregisterReceiver(completeListener);
    }
}
