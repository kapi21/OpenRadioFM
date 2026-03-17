package com.example.openradiofm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Bootstrap OEM: asegura que los MEDIA_BUTTON arrancan el servicio de medios
 * incluso con arranque en frío (sin proceso/UI).
 */
public class MediaButtonBootstrapReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaBtnBootstrap";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        try {
            Intent serviceIntent = new Intent(context, RadioMediaService.class);
            serviceIntent.setAction(intent.getAction());
            serviceIntent.putExtras(intent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error arrancando RadioMediaService desde MEDIA_BUTTON", e);
        }
    }
}

