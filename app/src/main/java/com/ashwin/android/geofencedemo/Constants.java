package com.ashwin.android.geofencedemo;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

final class Constants {
    private Constants() { }

    static final String TAG = "geofence-debug";

    static final String GEOFENCES_ADDED_KEY = "GEOFENCES_ADDED";

    static final float GEOFENCE_RADIUS_IN_METERS = 500f;

    static final HashMap<String, LatLng> GEOFENCE_LANDMARKS = new HashMap<>();
    static {
        GEOFENCE_LANDMARKS.put("bandra", new LatLng(19.0607, 72.8416));
        GEOFENCE_LANDMARKS.put("vakola", new LatLng(19.0816,72.8556));
        GEOFENCE_LANDMARKS.put("santacruz", new LatLng(19.0817, 72.8415));
        GEOFENCE_LANDMARKS.put("vile_parle", new LatLng(19.0995, 72.8439));
        GEOFENCE_LANDMARKS.put("andheri", new LatLng(19.1189, 72.8472));
        GEOFENCE_LANDMARKS.put("jogeshwari", new LatLng(19.1361, 72.8488));
        GEOFENCE_LANDMARKS.put("ram_mandir", new LatLng(19.1516, 72.8501));
        GEOFENCE_LANDMARKS.put("lotus_corporate_park", new LatLng(19.1447, 72.8533));
        GEOFENCE_LANDMARKS.put("goregaon", new LatLng(19.1648, 72.8493));
    }

    // Button texts
    static final String ADD_GEOFENCES = "ADD GEOFENCES";
    static final String REMOVE_GEOFENCES = "REMOVE GEOFENCES";
}
