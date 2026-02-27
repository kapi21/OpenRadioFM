package com.example.openradiofm.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.K706RadioManager;
import com.hcn.autoradio.IRadioServiceAPI;
import com.hcn.autoradio.IRadioCallBack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * V9.5: Menú de desarrollo específico para hardware K706.
 * Expone todas las funciones MCU del informe de ingeniería inversa
 * para testing directo de comandos sin pasar por la UI principal.
 */
public class K706EngineeringDialog extends Dialog {

    private static final String TAG = "K706EngDialog";

    private final MainActivity mActivity;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mUpdateRunnable;
    private boolean mIsRunning = false;

    // Monitor
    private TextView tvMonitor, tvLog;
    private ScrollView scrollLog;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    // Track state changes for logging
    private int mLastLogFreq = -1;
    private String mLastLogPty = "";
    private boolean mLastLogStereo = false;

    public K706EngineeringDialog(MainActivity activity) {
        super(activity);
        this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_k706_engineering);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        bindViews();
        setupButtons();

        mIsRunning = true;
        logEvent("SYS", "K706 DEV MODE INITIALIZED");
        logEvent("SYS", "MCU BRIDGE: " + (getK706Manager() != null ? "CONNECTED" : "DISCONNECTED"));
        startUpdateLoop();
    }

    private void bindViews() {
        tvMonitor = findViewById(R.id.tvK706Monitor);
        tvLog = findViewById(R.id.tvK706Log);
        scrollLog = findViewById(R.id.scrollK706Log);

        // Close buttons
        View btnClose = findViewById(R.id.btnCloseK706);
        if (btnClose != null) btnClose.setOnClickListener(v -> dismiss());
        View btnExit = findViewById(R.id.btnK706Exit);
        if (btnExit != null) btnExit.setOnClickListener(v -> dismiss());

        // V4.6: Selector de motor de radio (movido desde Settings)
        setupEngineSelector();
    }

    /**
     * V4.6: Selector de motor de radio (movido desde Settings al Engineering Menu)
     */
    private void setupEngineSelector() {
        Button btnEngine = new Button(getContext());
        int idx = mActivity.mPrefs.getInt("pref_radio_engine", 0);
        String[] engines = { "Auto", "HCN", "MTK", "Standard", "TS" };
        String name = (idx >= 0 && idx < engines.length) ? engines[idx] : "Auto";
        btnEngine.setText("⚙ RADIO ENGINE: " + name);
        btnEngine.setTextColor(Color.parseColor("#00FF00"));
        btnEngine.setBackgroundColor(Color.parseColor("#1a1a2e"));
        btnEngine.setAllCaps(false);
        btnEngine.setTextSize(12);
        btnEngine.setOnClickListener(v -> {
            mActivity.showEngineSelector();
            dismiss();
        });

        View exitBtn = findViewById(R.id.btnK706Exit);
        if (exitBtn != null && exitBtn.getParent() instanceof android.view.ViewGroup) {
            android.view.ViewGroup parent = (android.view.ViewGroup) exitBtn.getParent();
            int exitIdx = parent.indexOfChild(exitBtn);
            parent.addView(btnEngine, exitIdx);
        }
    }

    private K706RadioManager getK706Manager() {
        if (mActivity.mRadioService instanceof K706RadioManager) {
            return (K706RadioManager) mActivity.mRadioService;
        }
        return null;
    }

    private void setupButtons() {
        // === TUNER ===
        setupButton(R.id.btnK706FineDown, "FINE ▼", () -> {
            execService(s -> s.onManualDownEvent());
            logEvent("CMD", "FINE DOWN (0x0E)");
        });

        setupButton(R.id.btnK706FineUp, "FINE ▲", () -> {
            execService(s -> s.onManualUpEvent());
            logEvent("CMD", "FINE UP (0x0F)");
        });

        setupButton(R.id.btnK706SeekDown, "SEEK ◀", () -> {
            execService(s -> s.onSeekDownEvent());
            logEvent("CMD", "SEEK DOWN (0x0C)");
        });

        setupButton(R.id.btnK706SeekUp, "SEEK ▶", () -> {
            execService(s -> s.onSeekUpEvent());
            logEvent("CMD", "SEEK UP (0x0D)");
        });

        setupButton(R.id.btnK706AutoScan, null, () -> {
            execService(s -> s.onScanEvent());
            logEvent("CMD", "AUTOSCAN START (0x08)");
        });

        setupButton(R.id.btnK706StopScan, null, () -> {
            execService(s -> s.onPSEvent());
            logEvent("CMD", "STOP SCAN (0x09)");
        });

        setupButton(R.id.btnK706SavePreset, null, () -> {
            // SUB_SAVE (0x04) no está en la AIDL, enviar directamente via MCU
            sendMcuTunerCmd((byte) 0x04, (byte) 0x00, (byte) 0x00);
            logEvent("CMD", "SAVE PRESET (0x04)");
        });

        // === BAND / LOC ===
        setupButton(R.id.btnK706Band, null, () -> {
            execService(s -> s.onBandEvent());
            logEvent("CMD", "BAND CYCLE (0x06)");
        });

        setupButton(R.id.btnK706LocDx, null, () -> {
            execService(s -> s.onLocDxEvent());
            logEvent("CMD", "LOC/DX TOGGLE (0x0A)");
        });

        setupButton(R.id.btnK706Area0, null, () -> {
            sendMcuTunerCmd((byte) 0x0A, (byte) 0x00, (byte) 0x00);
            logEvent("CMD", "RADIO AREA: 0 (0x0A 0x00)");
        });

        setupButton(R.id.btnK706Area1, null, () -> {
            sendMcuTunerCmd((byte) 0x0A, (byte) 0x01, (byte) 0x00);
            logEvent("CMD", "RADIO AREA: 1 (0x0A 0x01)");
        });

        // === RDS CONTROLS (MOVED TO PREMIUM SETTINGS) ===

        setupButton(R.id.btnK706PtyReset, null, () -> {
            if (!invokeQFTuner("setRdsPtyType", int.class, 0)) {
                sendMcuRdsCmd((byte) 0x00);
                // V9.8: Enviar también FF para limpiar rastro del filtro
                mHandler.postDelayed(() -> sendMcuRdsCmd((byte) 0xFF), 100);
            }
            logEvent("RDS", "PTY FILTER RESET (0x00+0xFF)");
        });

        setupButton(R.id.btnK706PtyNews, null, () -> {
            if (!invokeQFTuner("setRdsPtyType", int.class, 1)) {
                sendMcuRdsCmd((byte) 0x01);
            }
            logEvent("RDS", "PTY FILTER: NEWS");
        });

        // === AUDIO CHANNEL ===
        for (int ch = 1; ch <= 4; ch++) {
            int resId = 0;
            switch (ch) {
                case 1: resId = R.id.btnK706Ch1; break;
                case 2: resId = R.id.btnK706Ch2; break;
                case 3: resId = R.id.btnK706Ch3; break;
                case 4: resId = R.id.btnK706Ch4; break;
            }
            final int channel = ch;
            setupButton(resId, null, () -> {
                setAudioChannel(channel);
                logEvent("AUD", "SET CHANNEL: " + channel);
            });
        }
    }

    private void setupButton(int resId, String label, Runnable action) {
        Button btn = findViewById(resId);
        if (btn != null) {
            if (label != null) btn.setText(label);
            btn.setOnClickListener(v -> action.run());
        }
    }

    // === MCU Command Helpers ===

    /**
     * Envía un comando al MCU Tuner (prefijo 0xA0) vía K706RadioManager.
     */
    private void sendMcuTunerCmd(byte subCmd, byte param1, byte param2) {
        K706RadioManager mgr = getK706Manager();
        if (mgr == null) {
            logEvent("ERR", "K706Manager no disponible");
            return;
        }
        try {
            // Usar reflection para acceder a sendCmd privado
            java.lang.reflect.Method sendCmd = K706RadioManager.class.getDeclaredMethod(
                "sendCmd", byte.class, byte.class, byte.class);
            sendCmd.setAccessible(true);
            sendCmd.invoke(mgr, subCmd, param1, param2);
        } catch (Exception e) {
            logEvent("ERR", "sendCmd failed: " + e.getMessage());
            Log.e(TAG, "sendMcuTunerCmd error", e);
        }
    }

    /**
     * Envía un comando RDS (prefijo 0xA2) para PTY filter.
     */
    private void sendMcuRdsCmd(byte ptyType) {
        K706RadioManager mgr = getK706Manager();
        if (mgr == null) {
            logEvent("ERR", "K706Manager no disponible");
            return;
        }
        try {
            java.lang.reflect.Method sendRdsCmd = K706RadioManager.class.getDeclaredMethod(
                "sendRdsCmd", byte.class);
            sendRdsCmd.setAccessible(true);
            sendRdsCmd.invoke(mgr, ptyType);
        } catch (Exception e) {
            logEvent("ERR", "sendRdsCmd failed: " + e.getMessage());
            Log.e(TAG, "sendMcuRdsCmd error", e);
        }
    }

    /**
     * Cambia el canal de audio vía RPC_SetChannel.
     */
    private void setAudioChannel(int channel) {
        K706RadioManager mgr = getK706Manager();
        if (mgr == null) {
            logEvent("ERR", "K706Manager no disponible");
            return;
        }
        try {
            // mSetChannel es privado, accedemos via reflection
            java.lang.reflect.Field fSetChannel = K706RadioManager.class.getDeclaredField("mSetChannel");
            fSetChannel.setAccessible(true);
            java.lang.reflect.Method setChannel = (java.lang.reflect.Method) fSetChannel.get(mgr);
            
            java.lang.reflect.Field fMcuManager = K706RadioManager.class.getDeclaredField("mMcuManager");
            fMcuManager.setAccessible(true);
            Object mcuManager = fMcuManager.get(mgr);
            
            if (setChannel != null && mcuManager != null) {
                try {
                    // Primero intentar con byte (signatura correcta del K706)
                    setChannel.invoke(mcuManager, (byte) channel);
                } catch (IllegalArgumentException e) {
                    // Fallback: intentar con int
                    setChannel.invoke(mcuManager, channel);
                }
                logEvent("AUD", "RPC_SetChannel(" + channel + ") OK");
            } else {
                logEvent("ERR", "SetChannel method or MCU not available");
            }
        } catch (Exception e) {
            logEvent("ERR", "setChannel failed: " + e.getMessage());
            Log.e(TAG, "setAudioChannel error", e);
        }
    }

    private interface ServiceAction {
        void run(com.hcn.autoradio.IRadioServiceAPI s) throws RemoteException;
    }

    private void execService(ServiceAction action) {
        if (mActivity.mRadioService == null) {
            logEvent("ERR", "RadioService no disponible");
            return;
        }
        try {
            action.run(mActivity.mRadioService);
        } catch (RemoteException e) {
            logEvent("ERR", "RemoteException: " + e.getMessage());
        }
    }

    // === V9.6: QFTunerManager Helpers ===

    private Object mCachedQFTunerManager;
    private boolean mQFTunerChecked = false;

    private Object getQFTunerManager() {
        if (!mQFTunerChecked) {
            mQFTunerChecked = true;
            try {
                Class<?> clazz = Class.forName("com.qf.clientsdk.QFTunerManager");
                java.lang.reflect.Method getInstance = clazz.getMethod("getInstance");
                mCachedQFTunerManager = getInstance.invoke(null);
                logEvent("SYS", "QFTunerManager: CONNECTED");
            } catch (Exception e) {
                logEvent("SYS", "QFTunerManager: NOT AVAILABLE");
            }
        }
        return mCachedQFTunerManager;
    }

    /**
     * Invoca un método del QFTunerManager con un argumento int.
     * @return true si se ejecutó correctamente, false si QFTunerManager no disponible
     */
    private boolean invokeQFTuner(String methodName, Class<?> paramType, Object arg) {
        Object mgr = getQFTunerManager();
        if (mgr == null) return false;
        try {
            java.lang.reflect.Method m = mgr.getClass().getMethod(methodName, paramType);
            m.invoke(mgr, arg);
            logEvent("QF", methodName + "(" + arg + ") OK");
            return true;
        } catch (NoSuchMethodException e) {
            logEvent("QF", methodName + " NOT FOUND");
            return false;
        } catch (Exception e) {
            logEvent("QF", methodName + " FAILED: " + e.getMessage());
            return false;
        }
    }

    /**
     * Invoca un método del QFTunerManager sin argumentos.
     */
    private boolean invokeQFTunerNoArg(String methodName) {
        Object mgr = getQFTunerManager();
        if (mgr == null) return false;
        try {
            java.lang.reflect.Method m = mgr.getClass().getMethod(methodName);
            m.invoke(mgr);
            logEvent("QF", methodName + "() OK");
            return true;
        } catch (NoSuchMethodException e) {
            logEvent("QF", methodName + " NOT FOUND");
            return false;
        } catch (Exception e) {
            logEvent("QF", methodName + " FAILED: " + e.getMessage());
            return false;
        }
    }

    // === Monitor Loop ===

    private void startUpdateLoop() {
        mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mIsRunning || !isShowing()) return;
                updateMonitor();
                mHandler.postDelayed(this, 500);
            }
        };
        mHandler.post(mUpdateRunnable);
    }

    private void updateMonitor() {
        if (mActivity == null || mActivity.mRadioService == null) {
            if (tvMonitor != null) {
                tvMonitor.setText("FREQ: NO_SERVICE\nBAND: ---\nFLAGS: ---");
            }
            return;
        }

        try {
            int freq = mActivity.mRadioService.getCurrentFreq();
            int band = mActivity.mRadioService.getCurrentBand();
            boolean stereo = mActivity.mRadioService.IsStereo();
            boolean isLocal = mActivity.mRadioService.IsDxLocal();

            // Log state changes
            if (freq != mLastLogFreq) {
                logEvent("RF", String.format(Locale.US, "TUNED > %.2f MHz", freq / 1000.0));
                mLastLogFreq = freq;
            }
            if (stereo != mLastLogStereo) {
                logEvent("AUD", stereo ? "STEREO DETECTED" : "MONO SIGNAL");
                mLastLogStereo = stereo;
            }

            String pty = mActivity.mCurrentPty;
            if (pty != null && !pty.equals(mLastLogPty)) {
                logEvent("RDS", "PTY > " + pty);
                mLastLogPty = pty;
            }

            // K706-specific info
            K706RadioManager mgr = getK706Manager();
            boolean scanning = mgr != null && mgr.isScanning();

            // Generación de SQI y RSSI Extrapolado
            boolean rdsLock = mActivity.mHasRdsLock;
            int sqi = 0;
            if (rdsLock && stereo) sqi = 100;
            else if (rdsLock) sqi = 75;
            else if (stereo) sqi = 60;
            else sqi = 30;

            int bars = sqi / 10;
            StringBuilder rssiBar = new StringBuilder("[");
            for (int i=0; i<10; i++) rssiBar.append(i < bars ? "█" : "░");
            rssiBar.append("]");
            int dbm = -100 + (sqi / 2); // Estimación empírica

            // Build monitor text
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.US, "FREQ.....: %.2f MHz\n", freq / 1000.0));
            sb.append(String.format("BAND.....: B%d (%s)\n", band, getBandName(band)));
            sb.append(String.format("STEREO...: %s\n", stereo ? "YES (19kHz pilot)" : "NO (mono)"));
            sb.append(String.format("LOC/DX...: %s\n", isLocal ? "LOCAL" : "DX"));
            sb.append(String.format("SCANNING.: %s\n", scanning ? "▶ ACTIVE" : "IDLE"));
            sb.append(String.format("SQI LOG..: %d%% %s\n", sqi, rssiBar.toString()));
            sb.append(String.format("SIG. EST.: %ddBm\n", dbm));
            sb.append(String.format("PTY......: %s\n", pty != null ? pty : "---"));
            sb.append(String.format("DEVICE...: %s / %s\n",
                android.os.Build.MODEL, android.os.Build.DEVICE));
            sb.append(String.format("BOARD....: %s", android.os.Build.BOARD));

            if (tvMonitor != null) {
                tvMonitor.setText(sb.toString());
            }

        } catch (Exception e) {
            if (tvMonitor != null) {
                tvMonitor.setText("FREQ: ERROR_READ\nBAND: ---\n" + e.getMessage());
            }
        }
    }

    private String getBandName(int band) {
        switch (band) {
            case 0: return "FM1";
            case 1: return "FM2";
            case 2: return "FM3";
            case 3: return "AM1";
            case 4: return "AM2";
            default: return "UNK";
        }
    }

    private void logEvent(String tag, String msg) {
        if (tvLog == null) return;
        String time = timeFormat.format(new Date());
        String entry = String.format("[%s] %s: %s\n", time, tag, msg);

        mHandler.post(() -> {
            tvLog.append(entry);
            if (scrollLog != null) scrollLog.fullScroll(View.FOCUS_DOWN);
        });
    }

    public void addRdsLog(String msg) {
        logEvent("RDS_RAW", msg);
    }

    @Override
    public void dismiss() {
        mIsRunning = false;
        mHandler.removeCallbacks(mUpdateRunnable);
        super.dismiss();
    }
}
