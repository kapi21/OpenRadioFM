package com.example.openradiofm.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.Target;
import com.example.openradiofm.R;
import com.example.openradiofm.service.RadioMediaService;
import com.example.openradiofm.ui.main.MainActivity;

/**
 * Widget de escritorio: logo (icono de app por defecto), frecuencia, RDS PS y memorias anterior/siguiente.
 * Los datos se actualizan desde {@link MainActivity} cuando cambia la sintonía (misma cadencia que otros widgets OEM).
 */
public class OpenRadioFmWidgetProvider extends AppWidgetProvider {

    private static final String PREFS = "OpenRadioFmWidget";
    public static final String EXTRA_WIDGET_SHOW_INFO = "extra_widget_show_info";
    public static final String EXTRA_WIDGET_OPEN_FAVORITES_DIALOG = "extra_widget_open_favorites_dialog";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int freq = p.getInt("freq_khz", 0);
        int band = p.getInt("band", 0);
        String ps = p.getString("ps", "");
        String logo = p.getString("logo_url", null);
        for (int id : appWidgetIds) {
            updateOne(context, appWidgetManager, id, freq, band, ps, logo);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int freq = p.getInt("freq_khz", 0);
        int band = p.getInt("band", 0);
        String ps = p.getString("ps", "");
        String logo = p.getString("logo_url", null);
        updateOne(context, appWidgetManager, appWidgetId, freq, band, ps, logo);
    }

    private static void updateOne(Context context, AppWidgetManager awm, int appWidgetId,
            int freqKhz, int band, String ps, String logoUrl) {
        boolean expanded = isExpanded(awm, appWidgetId);
        int layout = expanded ? R.layout.widget_openradio_expanded : R.layout.widget_openradio_compact;
        RemoteViews rv = new RemoteViews(context.getPackageName(), layout);

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

        if (expanded) {
            rv.setTextViewText(R.id.widget_band, bandLabel(band));
        }

        // Placeholder por defecto (solo si no tenemos logo de emisora)
        if (TextUtils.isEmpty(logoUrl)) {
            rv.setImageViewResource(R.id.widget_logo, R.drawable.ic_launcher);
        }

        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piOpen = PendingIntent.getActivity(context, 0, open, piFlags);
        rv.setOnClickPendingIntent(R.id.widget_root, piOpen);
        rv.setOnClickPendingIntent(R.id.widget_logo, piOpen);

        // Click en PS: abrir app y mostrar info rápida (diálogo).
        Intent info = new Intent(context, MainActivity.class);
        info.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        info.putExtra(EXTRA_WIDGET_SHOW_INFO, true);
        info.putExtra("freq_khz", freqKhz);
        info.putExtra("band", band);
        info.putExtra("ps", ps != null ? ps : "");
        PendingIntent piInfo = PendingIntent.getActivity(context, 10, info, piFlags);
        rv.setOnClickPendingIntent(R.id.widget_ps, piInfo);

        // Launchers OEM a menudo no permiten redimensionar: el "widget simple" debe hacer SEEK (como antes).
        String prevAction = expanded ? RadioMediaService.ACTION_WIDGET_PREV_PRESET : RadioMediaService.ACTION_WIDGET_SEEK_DOWN;
        String nextAction = expanded ? RadioMediaService.ACTION_WIDGET_NEXT_PRESET : RadioMediaService.ACTION_WIDGET_SEEK_UP;

        Intent prev = new Intent(context, RadioWidgetActionReceiver.class);
        prev.setAction(prevAction);
        PendingIntent piPrev = PendingIntent.getBroadcast(context, 1, prev, piFlags);
        rv.setOnClickPendingIntent(R.id.widget_btn_prev, piPrev);

        Intent next = new Intent(context, RadioWidgetActionReceiver.class);
        next.setAction(nextAction);
        PendingIntent piNext = PendingIntent.getBroadcast(context, 2, next, piFlags);
        rv.setOnClickPendingIntent(R.id.widget_btn_next, piNext);

        if (expanded) {
            Intent mute = new Intent(context, RadioWidgetActionReceiver.class);
            mute.setAction(RadioMediaService.ACTION_WIDGET_TOGGLE_MUTE);
            PendingIntent piMute = PendingIntent.getBroadcast(context, 5, mute, piFlags);
            rv.setOnClickPendingIntent(R.id.widget_btn_mute, piMute);

            // Favoritos (opcional): abre el diálogo Save/Load favorites.
            Intent fav = new Intent(context, MainActivity.class);
            fav.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            fav.putExtra(EXTRA_WIDGET_OPEN_FAVORITES_DIALOG, true);
            PendingIntent piFav = PendingIntent.getActivity(context, 11, fav, piFlags);
            rv.setOnClickPendingIntent(R.id.widget_btn_fav, piFav);
        }

        // Accesibilidad: describir contenido principal.
        String cd = bandLabel(band) + " " + freqText + ". " + psLine;
        try {
            rv.setContentDescription(R.id.widget_root, cd);
            rv.setContentDescription(R.id.widget_logo, cd);
            rv.setContentDescription(R.id.widget_freq, cd);
            rv.setContentDescription(R.id.widget_ps, cd);
        } catch (Exception ignored) {}

        awm.updateAppWidget(appWidgetId, rv);

        // Cargar logo dinámico si existe (Glide + AppWidgetTarget).
        // Importante: RemoteViews no conserva el bitmap si re-renderizamos el widget, así que
        // debemos recargar aunque la URL no cambie (evita volver al placeholder).
        if (!TextUtils.isEmpty(logoUrl)) {
            Object model = normalizeLogoModel(logoUrl);
            AppWidgetTarget target = new AppWidgetTarget(context.getApplicationContext(),
                    R.id.widget_logo, rv, appWidgetId);
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(model)
                    .apply(new RequestOptions()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .dontTransform())
                    .error(R.drawable.ic_launcher)
                    .into(target);
        }
    }

    /** Llamar desde MainActivity cuando cambien frecuencia / RDS. */
    public static void updateStationDisplay(Context context, int freqKhz, int band, String ps) {
        updateStationDisplay(context, freqKhz, band, ps, null);
    }

    /** Variante que también persiste el logo actual (URL o ruta local). */
    public static void updateStationDisplay(Context context, int freqKhz, int band, String ps, String logoUrl) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("freq_khz", freqKhz)
                .putInt("band", band)
                .putString("ps", ps != null ? ps : "")
                .putString("logo_url", (logoUrl != null && !logoUrl.trim().isEmpty()) ? logoUrl.trim() : "")
                .apply();

        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, OpenRadioFmWidgetProvider.class);
        int[] ids = awm.getAppWidgetIds(cn);
        if (ids.length == 0) return;
        for (int id : ids) {
            updateOne(context, awm, id, freqKhz, band, ps, logoUrl);
        }
    }

    private static Object normalizeLogoModel(String raw) {
        String s = raw != null ? raw.trim() : "";
        if (s.isEmpty()) return null;
        // Aceptamos URLs, file://, content:// y rutas locales absolutas.
        if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("content://") || s.startsWith("file://")) {
            return s;
        }
        if (s.startsWith("/")) {
            return "file://" + s;
        }
        return s;
    }

    private static boolean isExpanded(AppWidgetManager awm, int appWidgetId) {
        try {
            Bundle o = awm.getAppWidgetOptions(appWidgetId);
            if (o == null) return false;
            int minH = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
            // Umbral simple: > ~90dp muestra controles extra.
            return minH >= 96;
        } catch (Exception e) {
            return false;
        }
    }

    private static String bandLabel(int band) {
        if (band == 0) return "FM1";
        if (band == 1) return "FM2";
        if (band == 2) return "FM3";
        if (band == 3) return "AM1";
        if (band == 4) return "AM2";
        return "FM1";
    }

}
