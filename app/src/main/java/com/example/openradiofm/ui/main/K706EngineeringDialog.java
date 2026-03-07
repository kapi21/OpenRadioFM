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
import androidx.appcompat.widget.SwitchCompat;
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
    private SwitchCompat swLogosOnline;

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
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }

        bindViews();
        setupButtons();

        mIsRunning = true;

        // V15.6: Aplicar tipografía global al diálogo de ingeniería
        mActivity.applyRecursiveFont(getWindow().getDecorView(), mActivity.getSystemTypeface());

        logEvent("SYS", "K706 DEV MODE INITIALIZED");
        logEvent("SYS", "MCU BRIDGE: " + (getK706Manager() != null ? "CONNECTED" : "DISCONNECTED"));
        startUpdateLoop();
    }

    private void bindViews() {
        tvMonitor = findViewById(R.id.tvK706Monitor);
        tvLog = findViewById(R.id.tvK706Log);
        scrollLog = findViewById(R.id.scrollK706Log);
        swLogosOnline = findViewById(R.id.swK706LogosOnline);

        if (swLogosOnline != null) {
            swLogosOnline.setChecked(mActivity.mPrefs.getBoolean("pref_logos_online", false));
            swLogosOnline.setOnCheckedChangeListener((v, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_logos_online", checked).apply();
                logEvent("SET", "LOGOS_ONLINE > " + (checked ? "ON" : "OFF"));
            });
        }

        // Close buttons
        View btnClose = findViewById(R.id.btnCloseK706);
        if (btnClose != null)
            btnClose.setOnClickListener(v -> dismiss());
        View btnExit = findViewById(R.id.btnK706Exit);
        if (btnExit != null)
            btnExit.setOnClickListener(v -> dismiss());

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
        btnEngine.setText(getContext().getString(R.string.radio_engine_label, name));
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
        if (mActivity.mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
            return ((com.example.openradiofm.data.source.K706Engine) mActivity.mEngine).getManager();
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
                case 1:
                    resId = R.id.btnK706Ch1;
                    break;
                case 2:
                    resId = R.id.btnK706Ch2;
                    break;
                case 3:
                    resId = R.id.btnK706Ch3;
                    break;
                case 4:
                    resId = R.id.btnK706Ch4;
                    break;
            }
            final int channel = ch;
            setupButton(resId, null, () -> {
                setAudioChannel(channel);
                logEvent("AUD", "SET CHANNEL: " + channel);
            });
        }

        // === RDS TEST TOOLS (V16.2) ===
        Button btnInject = findViewById(R.id.btnInjectRt);
        android.widget.EditText etRt = findViewById(R.id.etRdsTest);
        if (btnInject != null && etRt != null) {
            btnInject.setOnClickListener(v -> {
                String testText = etRt.getText().toString();
                if (!testText.isEmpty() && mActivity.mRdsManager != null) {
                    mActivity.mRdsManager.onRdsText(testText);
                    logEvent("TEST", "INJECTED RDS RT: " + testText);
                }
            });
        }
    }

    private void setupButton(int resId, String label, Runnable action) {
        Button btn = findViewById(resId);
        if (btn != null) {
            if (label != null)
                btn.setText(label);
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
     * 
     * @return true si se ejecutó correctamente, false si QFTunerManager no
     *         disponible
     */
    private boolean invokeQFTuner(String methodName, Class<?> paramType, Object arg) {
        Object mgr = getQFTunerManager();
        if (mgr == null)
            return false;
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
        if (mgr == null)
            return false;
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
                if (!mIsRunning || !isShowing())
                    return;
                updateMonitor();
                mHandler.postDelayed(this, 500);
            }
        };
        mHandler.post(mUpdateRunnable);
    }

    private void updateMonitor() {
        if (mActivity == null || mActivity.mRadioService == null) {
            if (tvMonitor != null) {
                tvMonitor.setText(getContext().getString(R.string.freq_no_service));
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

            // K706-specific info (V15.7: Telemetría Real)
            K706RadioManager mgr = getK706Manager();
            boolean scanning = mgr != null && mgr.isScanning();
            byte[] signalData = (mgr != null) ? mgr.getLastSignalData() : null;

            int rssiRaw = -1;
            int snrRaw = -1;
            String rawHex = "NONE";

            if (signalData != null && signalData.length >= 8) {
                rssiRaw = signalData[5] & 0xFF; // Estación 96.9 -> 0x76
                snrRaw = signalData[7] & 0xFF; // Estación 96.9 -> 0xCF (0x01?)
                rawHex = mgr.bytesToHex(signalData);
            }

            // Generación de SQI y RSSI (V15.7: Basado en Telemetría si existe)
            int sqi;
            if (rssiRaw != -1) {
                // Mapeo experimental: rssi 0x76 (118) es señal muy fuerte.
                sqi = Math.min(100, (rssiRaw * 100) / 118);
            } else {
                boolean rdsLock = mActivity.mHasRdsLock;
                if (rdsLock && stereo)
                    sqi = 100;
                else if (rdsLock)
                    sqi = 75;
                else if (stereo)
                    sqi = 60;
                else
                    sqi = 30;
            }

            int bars = sqi / 10;
            StringBuilder rssiBar = new StringBuilder("[");
            for (int i = 0; i < 10; i++)
                rssiBar.append(i < bars ? "█" : "░");
            rssiBar.append("]");

            // dBM estimado basado en RSSI si existe
            int dbm = (rssiRaw != -1) ? (-120 + rssiRaw) : (-100 + (sqi / 2));

            // Build monitor text
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.US, "FREQ.....: %.2f MHz\n", freq / 1000.0));
            sb.append(String.format("BAND.....: B%d (%s)\n", band, getBandName(band)));
            sb.append(String.format("STEREO...: %s\n", stereo ? "YES (19kHz pilot)" : "NO (mono)"));
            sb.append(String.format("LOC/DX...: %s\n", isLocal ? "LOCAL" : "DX"));
            sb.append(String.format("SCANNING.: %s\n", scanning ? "▶ ACTIVE" : "IDLE"));
            sb.append(String.format("REAL RSSI: 0x%02X (%d) %s\n", rssiRaw, rssiRaw, rssiBar.toString()));
            sb.append(String.format("REAL SNR.: 0x%02x\n", snrRaw));
            sb.append(String.format("SIG. EST.: %ddBm\n", dbm));
            sb.append(String.format("RAW 0x41.: %s\n", rawHex));
            sb.append(String.format("PTY......: %s\n", pty != null ? pty : "---"));
            sb.append(String.format("DEVICE...: %s / %s\n",
                    android.os.Build.MODEL, android.os.Build.DEVICE));
            sb.append(String.format("BOARD....: %s", android.os.Build.BOARD));

            if (tvMonitor != null) {
                tvMonitor.setText(sb.toString());
            }

        } catch (Exception e) {
            if (tvMonitor != null) {
                tvMonitor.setText(getContext().getString(R.string.freq_error_read, e.getMessage()));
            }
        }
    }

    private String getBandName(int band) {
        switch (band) {
            case 0:
                return "FM1";
            case 1:
                return "FM2";
            case 2:
                return "FM3";
            case 3:
                return "AM1";
            case 4:
                return "AM2";
            default:
                return "UNK";
        }
    }

    private void logEvent(String tag, String msg) {
        if (tvLog == null)
            return;
        String time = timeFormat.format(new Date());
        String entry = String.format("[%s] %s: %s\n", time, tag, msg);

        mHandler.post(() -> {
            tvLog.append(entry);
            if (scrollLog != null)
                scrollLog.fullScroll(View.FOCUS_DOWN);
        });
    }

    public void addRdsLog(String msg) {
        if (msg != null && msg.startsWith("B7")) {
            logEvent("RDS_RT", msg);
        } else if (msg != null && msg.startsWith("B6")) {
            logEvent("RDS_PS", msg);
        } else {
            logEvent("RDS_RAW", msg);
        }
    }

    public void updateSignalQuality(int rssi, int snr) {
        // En un dispositivo no-K706 recibimos RSSI desde el motor en la app y podemos
        // loguearlo
        logEvent("SIG", "UPDATED RSSI: " + rssi + ", SNR: " + snr);
    }

    @Override
    public void dismiss() {
        mIsRunning = false;
        mHandler.removeCallbacks(mUpdateRunnable);
        super.dismiss();
    }
}
