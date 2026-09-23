package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.openradiofm.util.SpdDiagLogger;
import com.spd.radio.IRadioAidlCallback;
import com.spd.radio.IRadioAidlInterface;
import com.spd.radio.entity.aidl.RadioFreqInfo;
import com.spd.radio.entity.aidl.RadioRdsSettings;
import com.spd.radio.entity.aidl.RadioStatus;

import java.util.Locale;

/**
 * SPD Engine (Junsun V9 Plus): control por AIDL {@code com.spd.radio} con comandos directos a MCU.
 *
 * Requisito del proyecto: usar “modo directo a MCU” siempre que sea posible.
 * - Tune/step/seek/band/local/rds: vía setCommand (MCU) o getters AIDL.
 * - AutoScan: lo hace ScanManager propio de la app (NO usamos CMD_PREVIEW_SCAN).
 */
public class SpdEngine implements RadioEngine {
    private static final String TAG = "SpdEngine";

    private static final String ACTION_RADIO_SERVICE = "com.spd.radio.service";
    private static final String PACKAGE_RADIO_SERVICE = "com.spd.radio";
    /**
     * En ROMs SPD, el "SourceManager" del sistema puede rechazar paquetes terceros en enter/exitSource
     * (se ve en logcat como "SpdService-SourcManager: error package=...").
     * Usamos el identificador del UI OEM de radio para que el sistema acepte el handoff de fuente.
     */
    private static final String SPD_OEM_RADIO_UI_PKG = "com.spd.spdradio";

    private static final int CMD_SEEK_DOWN = 0x1000;
    private static final int CMD_SEEK_UP = 0x1001;
    private static final int CMD_STOP_SEARCH = 0x1002;
    private static final int CMD_TUNER_DOWN = 0x1003;
    private static final int CMD_TUNER_UP = 0x1004;
    private static final int CMD_PRESET_DOWN = 0x1005;
    private static final int CMD_PRESET_UP = 0x1006;

    private static final int CMD_SET_BAND = 0x2000;
    private static final int CMD_SET_LOCAL = 0x2002;
    private static final int CMD_SET_PLAY_STATE = 0x2008;
    private static final int CMD_ENTER = 0x200B;
    private static final int CMD_EXIT = 0x200C;
    /** Direct frequency set (kHz). */
    private static final int CMD_SET_FM_FREQ_DIRECT = 0x200E;

    private Context mContext;
    private String mClientType;
    private IRadioAidlInterface mService;
    private boolean mBound;

    private final android.os.Handler mMainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean mPollingEnabled;

    private int mCurrentFreq = 87500;
    private int mCurrentBand = 0; // 0..4
    private boolean mIsScanning;
    private boolean mIsStereo;
    private boolean mIsAfEnabled;
    private boolean mIsTaEnabled;
    private boolean mIsTpEnabled;
    private boolean mIsDxLocal;
    private boolean mOnlineStreamingActive;

    private int mLastLoggedFreq;
    private String mLastLoggedPs = "";
    private String mLastLoggedRt = "";
    private int mLastLoggedBand = -1;
    private boolean mLastLoggedScan;

    private RadioEngineCallback mCallback;

    private final IRadioAidlCallback mAidlCallback = new IRadioAidlCallback.Stub() {
        @Override
        public void onRadioBandChanged(String band) {
            mCurrentBand = bandToIndex(band);
            logInfo("cb_band", "band=" + band + " idx=" + mCurrentBand);
            post(() -> {
                if (mCallback != null) mCallback.onBandChanged(mCurrentBand);
            });
        }

        @Override
        public void onRadioCurrentStatusChanged(RadioStatus status) {
            if (status == null) return;
            mIsStereo = status.stereo;
            mIsTpEnabled = status.rdsTPInfo;
            mIsTaEnabled = status.rdsTAInfo;
            mIsScanning = status.isSeeking || status.isPreviewScaning || status.isAutoSearching;
            mIsDxLocal = status.local == 1;
            maybeLogStatus("cb_status", status.signalLevel);
            post(() -> {
                if (mCallback != null) {
                    mCallback.onStereoChanged(mIsStereo);
                    mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                    mCallback.onDxLocalChanged(mIsDxLocal);
                    mCallback.onScanStatusChanged(mIsScanning);
                    if (status.rdsPTY >= 0) {
                        mCallback.onRdsPty(String.valueOf(status.rdsPTY));
                    }
                    mCallback.onSignalUpdate(status.signalLevel, 0);
                }
            });
        }

        @Override
        public void onRadioFreqChanged(int freq) {
            int norm = normalizeFreq(freq);
            if (norm > 0) {
                mCurrentFreq = norm;
                if (mCurrentFreq != mLastLoggedFreq) {
                    mLastLoggedFreq = mCurrentFreq;
                    logInfo("cb_freq", "freq=" + mCurrentFreq + " raw=" + freq);
                }
            } else {
                logInfo("cb_freq", "ignored raw=" + freq);
            }
            post(() -> {
                if (mCallback != null && mCurrentFreq > 0) mCallback.onFrequencyChanged(mCurrentFreq);
            });
        }

        @Override
        public void onRadioListChanged(String listType, java.util.List<RadioFreqInfo> list, int focusIndex) {
            // Not needed for core flow.
        }

        @Override
        public void onRadioRdsPSChanged(String ps) {
            final String value = ps == null ? "" : ps.trim();
            if (!value.equals(mLastLoggedPs)) {
                mLastLoggedPs = value;
                logInfo("cb_rds_ps", "ps=" + value);
            }
            post(() -> {
                if (mCallback != null) mCallback.onRdsName(value);
            });
        }

        @Override
        public void onRadioRdsRTChanged(String rt) {
            final String value = rt == null ? "" : rt.trim();
            if (!value.equals(mLastLoggedRt)) {
                mLastLoggedRt = value;
                logInfo("cb_rds_rt", "rt=" + value);
            }
            post(() -> {
                if (mCallback != null) mCallback.onRdsText(value);
            });
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IRadioAidlInterface.Stub.asInterface(service);
            mBound = true;
            Log.d(TAG, "SPD radio service connected: " + name.flattenToShortString());
            logInfo("svc_connected", "name=" + name.flattenToShortString() + " type=" + mClientType);
            try {
                if (mService != null) {
                    mService.registerCallback(mClientType, mAidlCallback);
                    logInfo("svc_register_callback", "ok type=" + mClientType);
                    // enterSource puede ser validado por el sistema; evitar rechazo usando id OEM.
                    mService.enterSource(SPD_OEM_RADIO_UI_PKG);
                    logInfo("svc_enter_source", "type=" + SPD_OEM_RADIO_UI_PKG + " (clientType=" + mClientType + ")");
                    ensureRdsEnabled();
                    refreshSnapshot();
                    startPolling();
                }
            } catch (Exception e) {
                Log.w(TAG, "onServiceConnected init path failed", e);
                SpdDiagLogger.e("svc_connected_init_fail", "type=" + mClientType, e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "SPD radio service disconnected: " + name.flattenToShortString());
            mBound = false;
            stopPolling();
            mService = null;
            logInfo("svc_disconnected", "name=" + name.flattenToShortString());
        }
    };

    @Override
    public boolean init(Context context) {
        try {
            mContext = context != null ? context.getApplicationContext() : null;
            if (mContext == null) return false;
            SpdDiagLogger.init(mContext);
            mClientType = mContext.getPackageName();
            logInfo("init", "clientType=" + mClientType + " logPath=" + SpdDiagLogger.getLogPath());

            Intent intent = new Intent(ACTION_RADIO_SERVICE);
            intent.setPackage(PACKAGE_RADIO_SERVICE);
            mBound = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "bindService(SPD) -> " + mBound);
            logInfo("bind_service", "result=" + mBound);
            return mBound;
        } catch (Exception e) {
            Log.e(TAG, "init failed", e);
            SpdDiagLogger.e("init_fail", "", e);
            return false;
        }
    }

    @Override
    public void release() {
        release(false);
    }

    @Override
    public void release(boolean isChangingConfigurations) {
        if (isChangingConfigurations) {
            logInfo("release", "changingConfig=true");
            return;
        }
        try {
            if (mService != null) {
                try {
                    mService.unregisterCallback(mClientType);
                    logInfo("svc_unregister_callback", "type=" + mClientType);
                } catch (Exception ignored) {}
                try {
                    mService.exitSource(SPD_OEM_RADIO_UI_PKG);
                    logInfo("svc_exit_source", "type=" + SPD_OEM_RADIO_UI_PKG + " (clientType=" + mClientType + ")");
                } catch (Exception ignored) {}
            }
            if (mBound && mContext != null) {
                mContext.unbindService(mConnection);
            }
        } catch (Exception e) {
            Log.w(TAG, "release failed", e);
            SpdDiagLogger.e("release_fail", "", e);
        } finally {
            stopPolling();
            mBound = false;
            mService = null;
            mCallback = null;
            com.example.openradiofm.ui.main.RadioServiceController.clearSharedLocalEngineIfSame(this);
        }
    }

    @Override
    public void closeDevice() {
        logInfo("close_device", "cmd=EXIT");
        sendCommand(CMD_EXIT, 0, 0, null);
    }

    @Override
    public String getEngineName() {
        return "SPD";
    }

    @Override
    public void tune(int freqKhz) {
        mCurrentFreq = normalizeFreq(freqKhz);
        logInfo("tune", "req=" + freqKhz + " norm=" + mCurrentFreq);
        post(() -> {
            if (mCallback != null) mCallback.onFrequencyChanged(mCurrentFreq);
        });
        // “Direct MCU” path: set frequency directly.
        sendCommand(CMD_SET_FM_FREQ_DIRECT, mCurrentFreq, 0, null);
        requestRefresh(220);
        requestRefresh(800);
    }

    @Override
    public int getCurrentFreq() {
        if (mService != null) {
            try {
                RadioFreqInfo info = mService.getFreqInfo();
                if (info != null) {
                    mCurrentFreq = normalizeFreq(info.freq);
                    if (info.band != null) mCurrentBand = bandToIndex(info.band);
                }
            } catch (Exception ignored) {}
        }
        return mCurrentFreq;
    }

    @Override
    public int getCurrentBand() {
        return mCurrentBand;
    }

    @Override
    public void setBand(int band) {
        // SPD uses setCommand + bundle string.
        String target = indexToBand(Math.max(0, Math.min(4, band)));
        Bundle b = new Bundle();
        b.putString("string", target);
        logInfo("set_band", "band=" + band + " str=" + target);
        sendCommand(CMD_SET_BAND, 0, 0, b);
        requestRefresh(220);
        requestRefresh(900);
    }

    @Override
    public void seekUp() {
        logInfo("seek_up", "");
        mIsScanning = true;
        post(() -> { if (mCallback != null) mCallback.onScanStatusChanged(true); });
        sendCommand(CMD_SEEK_UP, 0, 0, null);
        requestRefresh(500);
        requestRefresh(1500);
    }

    @Override
    public void seekDown() {
        logInfo("seek_down", "");
        mIsScanning = true;
        post(() -> { if (mCallback != null) mCallback.onScanStatusChanged(true); });
        sendCommand(CMD_SEEK_DOWN, 0, 0, null);
        requestRefresh(500);
        requestRefresh(1500);
    }

    @Override
    public void stepUp() {
        logInfo("step_up", "");
        sendCommand(CMD_TUNER_UP, 0, 0, null);
        requestRefresh(180);
        requestRefresh(500);
    }

    @Override
    public void stepDown() {
        logInfo("step_down", "");
        sendCommand(CMD_TUNER_DOWN, 0, 0, null);
        requestRefresh(180);
        requestRefresh(500);
    }

    @Override
    public void scan() {
        // Requisito: AutoScan lo gestiona ScanManager propio (software).
        logInfo("scan", "no-op (ScanManager propio)");
    }

    @Override
    public void stopScan() {
        // Si ScanManager está en curso, él mismo controla la secuencia; aquí no detenemos OEM.
        logInfo("scan_stop", "no-op (ScanManager propio)");
    }

    @Override
    public void bandCycle() {
        int next = (mCurrentBand + 1) % 5;
        setBand(next);
    }

    @Override
    public boolean isScanning() {
        return mIsScanning;
    }

    @Override
    public boolean isStereo() {
        return mIsStereo;
    }

    @Override
    public void setStereo(boolean enable) {
        Log.d(TAG, "setStereo not exposed by SPD service");
    }

    @Override
    public void setMute(boolean mute) {
        logInfo("mute", "mute=" + mute);
        sendCommand(CMD_SET_PLAY_STATE, mute ? 0 : 1, 0, null);
    }

    @Override
    public void openEq(Context context) {
        // No EQ API SPD conocida; fallback a Settings.
        try {
            Intent settingsIntent = context.getPackageManager().getLaunchIntentForPackage("com.android.settings");
            if (settingsIntent != null) {
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
            }
        } catch (Exception e) {
            Log.w(TAG, "openEq fallback failed", e);
        }
    }

    @Override
    public boolean requestPlayAudio() {
        logInfo("request_play_audio", "");
        sendCommand(CMD_ENTER, 0, 0, null);
        return true;
    }

    @Override
    public void enforceAudioRecovery() {
        logInfo("enforce_audio_recovery", "");
        sendCommand(CMD_ENTER, 0, 0, null);
    }

    @Override
    public void switchToAndroidAudio() {
        logInfo("switch_android_audio", "");
        sendCommand(CMD_EXIT, 0, 0, null);
    }

    @Override
    public void switchToFmAudio() {
        logInfo("switch_fm_audio", "");
        sendCommand(CMD_ENTER, 0, 0, null);
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {
        mOnlineStreamingActive = active;
    }

    @Override
    public boolean isOnlineStreamingActive() {
        return mOnlineStreamingActive;
    }

    @Override
    public void toggleRdsFeature(int type) {
        logInfo("toggle_rds", "type=" + type);
        if (mService == null) return;
        try {
            RadioRdsSettings settings = mService.getRdsSettings();
            if (settings == null) settings = new RadioRdsSettings();
            if (type == 1) {
                settings.rdsAF = !settings.rdsAF;
            } else if (type == 2) {
                settings.rdsTA = !settings.rdsTA;
            } else {
                settings.rdsType = settings.rdsType == 0 ? 1 : 0;
            }
            mService.setRdsSettings(settings);
            mIsAfEnabled = settings.rdsAF;
            mIsTaEnabled = settings.rdsTA;
            post(() -> {
                if (mCallback != null) {
                    mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                }
            });
            requestRefresh(250);
        } catch (Exception e) {
            Log.w(TAG, "toggleRdsFeature failed", e);
            SpdDiagLogger.e("toggle_rds_fail", "type=" + type, e);
        }
    }

    @Override public boolean isAfEnabled() { return mIsAfEnabled; }
    @Override public boolean isTaEnabled() { return mIsTaEnabled; }
    @Override public boolean isTpEnabled() { return mIsTpEnabled; }

    @Override
    public void toggleDxLocal() {
        mIsDxLocal = !mIsDxLocal;
        logInfo("toggle_dx_local", "local=" + mIsDxLocal);
        sendCommand(CMD_SET_LOCAL, mIsDxLocal ? 1 : 0, 0, null);
        post(() -> { if (mCallback != null) mCallback.onDxLocalChanged(mIsDxLocal); });
        requestRefresh(200);
    }

    @Override public boolean isDxLocal() { return mIsDxLocal; }

    @Override public void gotoPreset(int index) { Log.d(TAG, "gotoPreset: app-side presets, idx=" + index); }

    @Override
    public void nextFavorite() {
        Bundle b = new Bundle();
        b.putString("string", "FAVORITES");
        sendCommand(CMD_PRESET_UP, 0, 0, b);
    }

    @Override
    public void prevFavorite() {
        Bundle b = new Bundle();
        b.putString("string", "FAVORITES");
        sendCommand(CMD_PRESET_DOWN, 0, 0, b);
    }

    @Override
    public void setCallback(RadioEngineCallback cb) {
        mCallback = cb;
        logInfo("set_callback", "cb=" + (cb != null));
        if (cb != null) {
            requestRefresh(120);
            startPolling();
        }
    }

    private void sendCommand(int cmd, int arg0, int arg1, Bundle bundle) {
        if (mService == null) return;
        try {
            logInfo("send_cmd", "cmd=0x" + Integer.toHexString(cmd) + " a0=" + arg0 + " a1=" + arg1 + " b=" + bundleToString(bundle));
            mService.setCommand(cmd, arg0, arg1, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "setCommand failed cmd=0x" + Integer.toHexString(cmd), e);
            SpdDiagLogger.e("send_cmd_fail", "cmd=0x" + Integer.toHexString(cmd), e);
        }
    }

    private void ensureRdsEnabled() {
        if (mService == null) return;
        try {
            RadioRdsSettings settings = mService.getRdsSettings();
            if (settings == null) settings = new RadioRdsSettings();
            if (settings.rdsType == 0) {
                settings.rdsType = 1;
                mService.setRdsSettings(settings);
                logInfo("rds_enabled", "forced rdsType=1");
            }
            mIsAfEnabled = settings.rdsAF;
            mIsTaEnabled = settings.rdsTA;
        } catch (Exception e) {
            Log.w(TAG, "ensureRdsEnabled failed", e);
            SpdDiagLogger.e("ensure_rds_fail", "", e);
        }
    }

    private void refreshSnapshot() {
        if (mService == null) return;
        try {
            RadioFreqInfo fi = mService.getFreqInfo();
            if (fi != null) {
                int norm = normalizeFreq(fi.freq);
                if (norm > 0) {
                    mCurrentFreq = norm;
                }
                if (fi.band != null) mCurrentBand = bandToIndex(fi.band);
                if ((norm > 0 && mCurrentFreq != mLastLoggedFreq) || mCurrentBand != mLastLoggedBand) {
                    mLastLoggedFreq = mCurrentFreq;
                    mLastLoggedBand = mCurrentBand;
                    logInfo("snap_freq", "freq=" + mCurrentFreq + " raw=" + fi.freq + " band=" + fi.band + " idx=" + mCurrentBand);
                }
            }
            try {
                String ps = mService.getRadioRdsPS();
                if (ps != null) mLastLoggedPs = ps.trim();
                String rt = mService.getRadioRdsRT();
                if (rt != null) mLastLoggedRt = rt.trim();
            } catch (Exception ignored) {}
            post(() -> {
                if (mCallback != null) {
                    if (mCurrentFreq > 0) mCallback.onFrequencyChanged(mCurrentFreq);
                    mCallback.onBandChanged(mCurrentBand);
                    mCallback.onRdsName(mLastLoggedPs);
                    mCallback.onRdsText(mLastLoggedRt);
                }
            });
        } catch (Exception e) {
            SpdDiagLogger.e("snap_fail", "", e);
        }
    }

    private void startPolling() {
        if (mPollingEnabled) return;
        mPollingEnabled = true;
        mMainHandler.postDelayed(mPollingRunnable, 800);
    }

    private void stopPolling() {
        mPollingEnabled = false;
        try { mMainHandler.removeCallbacks(mPollingRunnable); } catch (Exception ignored) {}
    }

    private final Runnable mPollingRunnable = new Runnable() {
        @Override public void run() {
            if (!mPollingEnabled) return;
            refreshSnapshot();
            mMainHandler.postDelayed(this, 900);
        }
    };

    private void requestRefresh(long delayMs) {
        mMainHandler.postDelayed(this::refreshSnapshot, Math.max(0, delayMs));
    }

    private void post(Runnable r) {
        if (r == null) return;
        mMainHandler.post(r);
    }

    private static int normalizeFreq(int value) {
        // SPD suele dar kHz (>= 10000). Si viniese “MHz x100” o similar, intentamos normalizar de forma conservadora.
        if (value <= 0) return 0;
        if (value < 2000) return value * 100; // fallback extremo
        if (value < 20000) return value * 10;
        return value;
    }

    private static int bandToIndex(String band) {
        if (band == null) return 0;
        String b = band.trim().toUpperCase(Locale.US);
        if (b.contains("FM2")) return 1;
        if (b.contains("FM3")) return 2;
        if (b.contains("AM1")) return 3;
        if (b.contains("AM2")) return 4;
        return 0;
    }

    private static String indexToBand(int idx) {
        switch (idx) {
            case 1: return "FM2";
            case 2: return "FM3";
            case 3: return "AM1";
            case 4: return "AM2";
            default: return "FM1";
        }
    }

    private static String bundleToString(Bundle b) {
        if (b == null) return "";
        try {
            return b.keySet().toString();
        } catch (Exception e) {
            return "bundle";
        }
    }

    private void maybeLogStatus(String event, int signal) {
        boolean scan = mIsScanning;
        if (scan != mLastLoggedScan || mCurrentBand != mLastLoggedBand) {
            mLastLoggedScan = scan;
            mLastLoggedBand = mCurrentBand;
            logInfo(event, "scan=" + scan + " band=" + mCurrentBand + " sig=" + signal);
        }
    }

    private void logInfo(String event, String msg) {
        try {
            SpdDiagLogger.i(event, msg);
        } catch (Exception ignored) {}
        // También a logcat para facilitar diagnósticos del tester.
        try {
            Log.d(TAG, event + (msg != null && !msg.isEmpty() ? (": " + msg) : ""));
        } catch (Exception ignored) {}
    }
}

