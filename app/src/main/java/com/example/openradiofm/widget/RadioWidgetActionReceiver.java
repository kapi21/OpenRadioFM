package com.example.openradiofm.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.example.openradiofm.service.RadioMediaService;

/**
 * Recibe pulsaciones del widget y arranca {@link RadioMediaService} en foreground (Android 8+).
 */
public class RadioWidgetActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;
        if (!RadioMediaService.ACTION_WIDGET_PREV_PRESET.equals(action)
                && !RadioMediaService.ACTION_WIDGET_NEXT_PRESET.equals(action)
                && !RadioMediaService.ACTION_WIDGET_SEEK_DOWN.equals(action)
                && !RadioMediaService.ACTION_WIDGET_SEEK_UP.equals(action)
                && !RadioMediaService.ACTION_WIDGET_TOGGLE_MUTE.equals(action)) {
            return;
        }
        Intent svc = new Intent(context, RadioMediaService.class);
        svc.setAction(action);
        try {
            ContextCompat.startForegroundService(context, svc);
        } catch (Exception e) {
            try {
                context.startService(svc);
            } catch (Exception ignored) {
            }
        }
    }
}
