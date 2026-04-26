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
 *
 * IMPORTANTE (K706): este puente es crítico para el control desde el widget OEM.
 * - Las acciones y su mapeo varían por ROM/launcher (pre/prev/previous/seek_up/seek_down/mute/pause/close).
 * - Modificar el intent-filter del manifest o este switch puede romper el control del widget.
 * Antes de cambiarlo, notificar al desarrollador y validar en hardware K706/QuickFish.
 */
public class OemRadioWidgetReceiver extends BroadcastReceiver {
    private static final String TAG = "OemRadioWidgetRx";

    // Algunas ROMs envían el action con o sin "/" inicial.
    private static final String ACT_PRE = "customize/radio/pre";
    private static final String ACT_PREV = "customize/radio/prev";
    private static final String ACT_PREVIOUS = "customize/radio/previous";
    private static final String ACT_NEXT = "customize/radio/next";
    private static final String ACT_SEEK_UP = "customize/radio/seek_up";
    private static final String ACT_SEEK_DOWN = "customize/radio/seek_down";
    private static final String ACT_CLOSE = "customize/radio/close";
    private static final String ACT_MUTE = "customize/radio/mute";
    private static final String ACT_PAUSE = "customize/radio/pause";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        final String rawAction = intent.getAction();
        final String action = normalizeAction(rawAction);

        // Log de diagnóstico: nos permite ver exactamente qué manda el launcher (extras incluidos).
        try {
            Log.i(TAG, "onReceive action=" + rawAction + " normalized=" + action + " extras=" + intent.getExtras());
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
            case ACT_PREV:
            case ACT_PREVIOUS:
            case ACT_SEEK_DOWN:
                svcAction = RadioMediaService.ACTION_WIDGET_SEEK_DOWN;
                break;
            case ACT_CLOSE:
            case ACT_MUTE:
            case ACT_PAUSE:
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

    private static String normalizeAction(String action) {
        if (action == null) return null;
        // El intent-filter permite acciones con "/" inicial; normalizamos para comparar.
        while (action.startsWith("/")) action = action.substring(1);
        return action;
    }
}

