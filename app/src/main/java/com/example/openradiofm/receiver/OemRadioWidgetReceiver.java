package com.example.openradiofm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.openradiofm.service.RadioMediaService;

/**
 * K706/QuickFish launcher widgets suelen mandar broadcasts "customize/radio/*" a la radio OEM.
 * Este receiver los intercepta para que OpenRadioFM pueda reaccionar (seek/mute) incluso si
 * el launcher no envía ACTION_MEDIA_BUTTON a nuestra MediaSession.
 */
public class OemRadioWidgetReceiver extends BroadcastReceiver {
    private static final String TAG = "OemRadioWidgetRx";

    private static final String ACT_PRE = "/customize/radio/pre";
    private static final String ACT_NEXT = "/customize/radio/next";
    private static final String ACT_SEEK_UP = "/customize/radio/seek_up";
    private static final String ACT_SEEK_DOWN = "/customize/radio/seek_down";
    private static final String ACT_CLOSE = "/customize/radio/close";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        final String action = intent.getAction();

        // Log de diagnóstico: nos permite ver exactamente qué manda el launcher (extras incluidos).
        try {
            Log.i(TAG, "onReceive action=" + action + " extras=" + intent.getExtras());
        } catch (Exception ignored) {}

        if (action == null) return;

        String svcAction = null;
        switch (action) {
            case ACT_NEXT:
            case ACT_SEEK_UP:
                // “Siguiente” del widget OEM: en OpenRadioFM se modela como seek arriba (o preset según preferencia en el servicio).
                svcAction = RadioMediaService.ACTION_WIDGET_SEEK_UP;
                break;
            case ACT_PRE:
            case ACT_SEEK_DOWN:
                svcAction = RadioMediaService.ACTION_WIDGET_SEEK_DOWN;
                break;
            case ACT_CLOSE:
                // “Cerrar” OEM suele equivaler a pause/mute.
                svcAction = RadioMediaService.ACTION_WIDGET_TOGGLE_MUTE;
                break;
        }

        if (svcAction == null) return;

        try {
            Intent svc = new Intent(context, RadioMediaService.class);
            svc.setAction(svcAction);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(svc);
            } else {
                context.startService(svc);
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudo arrancar RadioMediaService (" + svcAction + ")", e);
        }
    }
}

