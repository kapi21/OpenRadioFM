package com.example.openradiofm.engine;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Adapter that accesses the OEM QF tuner SDK via reflection.
 * <p>
 *   The original SDK lives in the OEM firmware (class com.qf.clientsdk.QFTunerManager).
 *   This wrapper loads that class at runtime, obtains the singleton instance and
 *   exposes a small, type‑safe API for OpenRadioFM.
 * </p>
 */
public class QFTunerAdapter {
    private static final String TAG = "QFTunerAdapter";
    private static QFTunerAdapter sInstance;

    private final Object tunerMgr;      // instance of com.qf.clientsdk.QFTunerManager
    private final Method mOnTune;
    private final Method mMute;
    private final Method mUnMute;
    private final Method mOnSeek;
    private final Method mAutoScan;
    private final Method mStopScan;
    private final Method mOnBand;
    private final Method mOnLoc;
    private final Method mSetRdsSwitch;
    private final Method mSetRdsAFSwitch;
    private final Method mSetRdsTASwitch;
    private final Method mSetRdsPtyType;
    private final Method mSetTunerTool;

    private QFTunerAdapter(Context ctx) throws Exception {
        // Load the OEM class via reflection
        Class<?> cls = Class.forName("com.qf.clientsdk.QFTunerManager");
        Method getInst = cls.getMethod("getInstance");
        tunerMgr = getInst.invoke(null);

        // Retrieve the methods we need
        mOnTune = safeGetMethod(cls, "onTune", int.class);
        mMute   = safeGetMethod(cls, "mute");
        mUnMute = safeGetMethod(cls, "unMute");
        mOnSeek = safeGetMethod(cls, "onSeek", boolean.class);
        mAutoScan = safeGetMethod(cls, "autoScan");
        mStopScan = safeGetMethod(cls, "stopScan");
        mOnBand = safeGetMethod(cls, "onBand", int.class);
        mOnLoc  = safeGetMethod(cls, "onLoc", int.class);
        mSetRdsSwitch = safeGetMethod(cls, "setRdsSwitch", int.class);
        mSetRdsAFSwitch = safeGetMethod(cls, "setRdsAFSwitch");
        mSetRdsTASwitch = safeGetMethod(cls, "setRdsTASwitch");
        mSetRdsPtyType = safeGetMethod(cls, "setRdsPtyType", int.class);
        
        Method tempSetTool = null;
        try {
            Class<?> toolCls = Class.forName("com.qf.clientsdk.ITunerTool");
            tempSetTool = cls.getMethod("setTunerTool", toolCls);
        } catch (Exception ignored) {}
        mSetTunerTool = tempSetTool;
    }

    private Method safeGetMethod(Class<?> cls, String name, Class<?>... params) {
        try {
            return cls.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Method not found: " + name);
            return null;
        }
    }

    /**
     * Returns the singleton instance, creating it if necessary.
     * Returns null if the OEM SDK cannot be loaded.
     */
    public static synchronized QFTunerAdapter getInstance(Context ctx) {
        if (sInstance == null) {
            try {
                sInstance = new QFTunerAdapter(ctx.getApplicationContext());
            } catch (Exception e) {
                Log.e(TAG, "QF SDK not found or incompatible", e);
                return null;
            }
        }
        return sInstance;
    }

    /**
     * Change the FM frequency. The frequency is expressed in kHz (e.g. 1010 == 101.0 MHz).
     * Note: Some OEM versions use MHz * 10 or kHz. K706 usually expects kHz for onTune.
     */
    public void tune(int freqKHz) {
        invoke(mOnTune, freqKHz);
    }

    public void mute() {
        invoke(mMute);
    }

    public void unMute() {
        invoke(mUnMute);
    }

    public void seek(boolean up) {
        invoke(mOnSeek, up);
    }

    public void autoScan() {
        invoke(mAutoScan);
    }

    public void stopScan() {
        invoke(mStopScan);
    }

    public void setBand(int band) {
        invoke(mOnBand, band);
    }

    public void setLoc(int mode) {
        invoke(mOnLoc, mode);
    }

    public void setRdsEnabled(boolean enabled) {
        invoke(mSetRdsSwitch, enabled ? 1 : 0);
    }

    public void toggleAf() {
        invoke(mSetRdsAFSwitch);
    }

    public void toggleTa() {
        invoke(mSetRdsTASwitch);
    }

    public void setPty(int pty) {
        invoke(mSetRdsPtyType, pty);
    }

    public void setTunerTool(Object toolProxy) {
        invoke(mSetTunerTool, toolProxy);
    }

    private void invoke(Method m, Object... args) {
        if (m != null && tunerMgr != null) {
            try {
                m.invoke(tunerMgr, args);
            } catch (Exception e) {
                Log.e(TAG, "Error invoking " + m.getName(), e);
            }
        }
    }
}
