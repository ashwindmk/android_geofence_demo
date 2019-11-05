package com.ashwin.android.geofencedemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.preference.PreferenceManager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.GeofenceStatusCodes;

class Utils {
    private Utils() { }

    static boolean checkPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            return checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    static boolean checkPermission(Context context, String permission) {
        int value = context.getPackageManager().checkPermission(permission, context.getPackageName());
        return value == PackageManager.PERMISSION_GRANTED;
    }

    static boolean getGeofencesAdded(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
    }

    static void updateGeofencesAdded(Context context, boolean added) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    // Geofence error messages
    static String getErrorString(Exception e) {
        if (e instanceof ApiException) {
            return getErrorString(((ApiException) e).getStatusCode());
        } else {
            return "Unknown error: the Geofence service is not available now";
        }
    }

    static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence service is not available now";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Your app has registered too many geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "You have provided too many PendingIntents to the addGeofences() call";
            default:
                return "Unknown error: the Geofence service is not available now";
        }
    }
}
