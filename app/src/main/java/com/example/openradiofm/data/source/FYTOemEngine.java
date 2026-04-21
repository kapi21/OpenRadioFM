package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * FYT/Teyes (com.syu.radio): motor OEM por intents.
 *
 * Este motor no accede al hardware de radio directamente; delega en la app OEM de FYT.
 * Funciona como “control remoto” (tune por deep-link, next/prev vía MyService).
 */
public final class FYTOemEngine implements RadioEngine {

    private static final String TAG = "FYTOemEngine";

    private static final String PKG_SYU_RADIO = "com.syu.radio";
    private static final String CLS_MYSERVICE = "com.syu.broadcast.MyService";

    private static final String ACTION_SYU_RADIO = "com.syu.radio";
    private static final String ACTION_PREV = "com.syu.radio.prevservice";
    private static final String ACTION_NEXT = "com.syu.radio.nextservice";

    // FYT OEM deep-link (observado en ActRadio: getQueryParameter("freq")).
    private static final String RADIO_URI_TEMPLATE = "radio://tune?freq=%d";

    private Context mAppContext;
    private RadioEngineCallback mCallback;
    private int mCurrentFreqKhz = 0;
    private int mCurrentBand = 0; // FM1 por defecto
    private boolean mScanning = false;

    @Override
    public boolean init(Context context) {
        try {
            if (context == null) return false;
            mAppContext = context.getApplicationContext();
            boolean ok = isFytOemAvailable(mAppContext);
            Log.i(TAG, "init(): com.syu.radio disponible=" + ok);
            return ok;
        } catch (Exception e) {
            Log.w(TAG, "init(): excepción", e);
            return false;
        }
    }

    @Override
    public void release() {
        mCallback = null;
        mAppContext = null;
        mScanning = false;
    }

    @Override
    public void closeDevice() {
        // No hay dispositivo propio que cerrar: el hardware vive en la app OEM.
    }

    @Override
    public String getEngineName() {
        return "FYT/OEM (com.syu.radio)";
    }

    @Override
    public void tune(int freqKhz) {
        mCurrentFreqKhz = freqKhz;
        if (mCallback != null) mCallback.onFrequencyChanged(freqKhz);

        if (mAppContext == null) return;
        if (!isFytOemAvailable(mAppContext)) {
            if (mCallback != null) mCallback.onRawEvent(9001, "com.syu.radio no disponible");
            return;
        }

        int oemFreq = toOemFreqFromKhz(freqKhz);
        Uri uri = Uri.parse(String.format(RADIO_URI_TEMPLATE, oemFreq));
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, uri).setPackage(PKG_SYU_RADIO);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mAppContext.startActivity(i);
            if (mCallback != null) mCallback.onRawEvent(9000, "ACTION_VIEW " + uri);
        } catch (Exception e) {
            Log.w(TAG, "tune(): no se pudo lanzar ACTION_VIEW " + uri, e);
            if (mCallback != null) mCallback.onRawEvent(9002, "tune error: " + e.getClass().getSimpleName());
        }
    }

    @Override
    public void setBand(int band) {
        mCurrentBand = band;
        if (mCallback != null) mCallback.onBandChanged(band);
    }

    @Override
    public int getCurrentFreq() {
        return mCurrentFreqKhz;
    }

    @Override
    public int getCurrentBand() {
        return mCurrentBand;
    }

    @Override
    public void seekUp() {
        sendMyService(ACTION_NEXT);
        // Best-effort: avanzar 100 kHz para refrescar UI si el OEM no notifica de vuelta.
        optimisticStep(+100);
    }

    @Override
    public void seekDown() {
        sendMyService(ACTION_PREV);
        optimisticStep(-100);
    }

    @Override
    public void stepUp() {
        seekUp();
    }

    @Override
    public void stepDown() {
        seekDown();
    }

    @Override
    public void scan() {
        // No hay API pública para auto-scan OEM sin IPC propietario.
        mScanning = true;
        if (mCallback != null) mCallback.onScanStatusChanged(true);
        // Parar inmediatamente para evitar UI bloqueada en “scanning”.
        stopScan();
        if (mCallback != null) mCallback.onRawEvent(9010, "scan: no soportado en FYT/OEM");
    }

    @Override
    public void stopScan() {
        mScanning = false;
        if (mCallback != null) mCallback.onScanStatusChanged(false);
    }

    @Override
    public void bandCycle() {
        // FYT OEM maneja banda internamente; aquí solo alternamos estado local.
        mCurrentBand = (mCurrentBand == 0) ? 1 : 0;
        if (mCallback != null) mCallback.onBandChanged(mCurrentBand);
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

    @Override
    public boolean isStereo() {
        return false;
    }

    @Override
    public void setStereo(boolean enable) {
        // No expuesto por intents.
    }

    @Override
    public void setMute(boolean mute) {
        // No control de audio directo aquí (evitar pelearse con reproductores como AIMP).
    }

    @Override
    public void openEq(Context context) {
        // No-op.
    }

    @Override
    public boolean requestPlayAudio() {
        if (mAppContext == null) return false;
        try {
            Intent i = new Intent(ACTION_SYU_RADIO).setPackage(PKG_SYU_RADIO);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mAppContext.startActivity(i);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "requestPlayAudio(): no se pudo abrir radio OEM", e);
            return false;
        }
    }

    @Override
    public void enforceAudioRecovery() {
        // No-op (no control MCU).
    }

    @Override
    public void switchToAndroidAudio() {
        // No-op.
    }

    @Override
    public void switchToFmAudio() {
        // No-op.
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {
        // No-op.
    }

    @Override
    public void toggleRdsFeature(int type) {
        // No expuesto por intents.
    }

    @Override
    public boolean isAfEnabled() {
        return false;
    }

    @Override
    public boolean isTaEnabled() {
        return false;
    }

    @Override
    public boolean isTpEnabled() {
        return false;
    }

    @Override
    public void toggleDxLocal() {
        // No-op.
    }

    @Override
    public boolean isDxLocal() {
        return false;
    }

    @Override
    public void gotoPreset(int index) {
        // No disponible sin IPC propietario.
    }

    @Override
    public void nextFavorite() {
        seekUp();
    }

    @Override
    public void prevFavorite() {
        seekDown();
    }

    @Override
    public void setCallback(RadioEngineCallback cb) {
        mCallback = cb;
    }

    private void optimisticStep(int deltaKhz) {
        if (mCurrentFreqKhz <= 0) return;
        int next = mCurrentFreqKhz + deltaKhz;
        // Clamp FM típico (65-108MHz) para no descontrolar display.
        if (next < 65000) next = 65000;
        if (next > 108000) next = 108000;
        mCurrentFreqKhz = next;
        if (mCallback != null) mCallback.onFrequencyChanged(next);
    }

    private void sendMyService(String action) {
        if (mAppContext == null) return;
        try {
            Intent i = new Intent(action).setComponent(new ComponentName(PKG_SYU_RADIO, CLS_MYSERVICE));
            i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            ComponentName cn = mAppContext.startService(i);
            if (mCallback != null) mCallback.onRawEvent(9003, "startService " + action + " -> " + cn);
        } catch (Exception e) {
            Log.w(TAG, "sendMyService(): fallo " + action, e);
            if (mCallback != null) mCallback.onRawEvent(9004, "startService error: " + e.getClass().getSimpleName());
        }
    }

    private static int toOemFreqFromKhz(int freqKhz) {
        // Observado en campo: OEM acepta MHz*100 (ej. 10170 = 101.70MHz). En kHz: 101700/10=10170.
        if (freqKhz <= 0) return 0;
        if (freqKhz < 2000) return freqKhz; // ya parece OEM (953, 964…)
        return Math.round(freqKhz / 10f);
    }

    public static boolean isFytOemAvailable(Context context) {
        if (context == null) return false;
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(PKG_SYU_RADIO, 0);
        } catch (Exception e) {
            return false;
        }

        // Señal FYT: sys.fyt.platform no vacío (si accesible).
        String platform = systemPropertyGet("sys.fyt.platform");
        if (platform != null && !platform.trim().isEmpty()) return true;

        // Fallback: si puede resolver el deep-link radio:// (aunque no sea FYT “puro”).
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("radio://tune?freq=10170")).setPackage(PKG_SYU_RADIO);
            return i.resolveActivity(context.getPackageManager()) != null;
        } catch (Exception ignored) {
            return true; // ya sabemos que el paquete existe
        }
    }

    private static String systemPropertyGet(String key) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            Object v = get.invoke(null, key);
            return v != null ? v.toString() : "";
        } catch (Exception e) {
            // Android 9+ puede restringir; no forzar.
            return "";
        }
    }
}

