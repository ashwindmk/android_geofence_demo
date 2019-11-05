package com.ashwin.android.geofencedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (Utils.getGeofencesAdded(context)) {
                GeofenceRegisterJobIntentService.enqueueWork(context, Constants.ACTION_ADD);
            }
        }
    }
}
