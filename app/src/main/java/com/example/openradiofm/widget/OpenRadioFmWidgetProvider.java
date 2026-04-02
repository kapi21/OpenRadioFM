package com.example.openradiofm.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;

import com.example.openradiofm.R;
import com.example.openradiofm.service.RadioMediaService;
import com.example.openradiofm.ui.main.MainActivity;

/**
 * Widget de escritorio: logo (icono de app por defecto), frecuencia, RDS PS y memorias anterior/siguiente.
 * Los datos se actualizan desde {@link MainActivity} cuando cambia la sintonía (misma cadencia que otros widgets OEM).
 */
public class OpenRadioFmWidgetProvider extends AppWidgetProvider {

    private static final String PREFS = "OpenRadioFmWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int freq = p.getInt("freq_khz", 0);
        int band = p.getInt("band", 0);
        String ps = p.getString("ps", "");
        for (int id : appWidgetIds) {
            updateOne(context, appWidgetManager, id, freq, band, ps);
        }
    }

    private static void updateOne(Context context, AppWidgetManager awm, int appWidgetId,
            int freqKhz, int band, String ps) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_openradio);

        String freqText;
        if (freqKhz > 0) {
            if (band == 3 || band == 4) {
                freqText = String.format(java.util.Locale.US, "%d kHz", freqKhz);
            } else {
                freqText = String.format(java.util.Locale.US, "%.2f MHz", freqKhz / 1000.0);
            }
        } else {
            freqText = "—";
        }
        rv.setTextViewText(R.id.widget_freq, freqText);

        String psLine = (ps != null && !ps.trim().isEmpty()) ? ps.trim() : "—";
        rv.setTextViewText(R.id.widget_ps, psLine);

        rv.setImageViewResource(R.id.widget_logo, R.mipmap.ic_launcher);

        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piOpen = PendingIntent.getActivity(context, 0, open, piFlags);
        rv.setOnClickPendingIntent(R.id.widget_root, piOpen);
        rv.setOnClickPendingIntent(R.id.widget_logo, piOpen);

        Intent prev = new Intent(context, RadioWidgetActionReceiver.class);
        prev.setAction(RadioMediaService.ACTION_WIDGET_PREV_PRESET);
        PendingIntent piPrev = PendingIntent.getBroadcast(context, 1, prev, piFlags);
        rv.setOnClickPendingIntent(R.id.widget_btn_prev, piPrev);

        Intent next = new Intent(context, RadioWidgetActionReceiver.class);
        next.setAction(RadioMediaService.ACTION_WIDGET_NEXT_PRESET);
        PendingIntent piNext = PendingIntent.getBroadcast(context, 2, next, piFlags);
        rv.setOnClickPendingIntent(R.id.widget_btn_next, piNext);

        awm.updateAppWidget(appWidgetId, rv);
    }

    /** Llamar desde MainActivity cuando cambien frecuencia / RDS. */
    public static void updateStationDisplay(Context context, int freqKhz, int band, String ps) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("freq_khz", freqKhz)
                .putInt("band", band)
                .putString("ps", ps != null ? ps : "")
                .apply();

        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, OpenRadioFmWidgetProvider.class);
        int[] ids = awm.getAppWidgetIds(cn);
        if (ids.length == 0) return;
        for (int id : ids) {
            updateOne(context, awm, id, freqKhz, band, ps);
        }
    }
}
