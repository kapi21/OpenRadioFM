package com.example.openradiofm.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.Window;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.QS6Engine;
import com.example.openradiofm.engine.NWDTunerAdapter;

import java.util.Locale;
import java.io.File;
import android.widget.Button;
import android.widget.ScrollView;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.widget.SwitchCompat;
import android.widget.SeekBar;

/**
 * Menú de ingeniería «Technical Matrix» para plataforma QS6 / NWD (mismo acceso que K706/MT8163:
 * pulsación larga en GPS).
 */
public class QS6EngineeringDialog extends Dialog {

    private static final Object SHARED_KERNEL_LOCK = new Object();
    private static volatile Qs6KernelMcuClient sSharedKernelClient;

    private final MainActivity mActivity;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mUpdateRunnable;
    private boolean mIsRunning = false;

    private TextView tvQs6NwdBind;
    private TextView tvQs6RdsFlags;
    private TextView tvQs6Rssi;
    private TextView tvQs6StereoUi;
    private TextView tvQs6StereoRaw;
    private TextView tvQs6BandInfo;
    private TextView tvQs6Scan;
    private TextView tvQs6DxLocal;

    private TextView tvQs6Ps;
    private TextView tvQs6Rt;
    private TextView tvQs6Pi;
    private TextView tvQs6Pty;
    private TextView tvQs6RdsLock;

    private TextView tvQs6OemFocusEvent;
    private TextView tvQs6OemFlags;

    private TextView tvQs6Assets;
    private TextView tvQs6Latency;
    private TextView tvQs6Memory;
    private TextView tvQs6Device;
    private TextView tvQs6Root;

    private TextView tvQs6Log;
    private ScrollView scrollQs6Log;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    // QS6 MCU direct (KernelService) - experimental
    private TextView tvQs6KernelBind;
    private TextView tvQs6KernelLastCmd;
    private Qs6KernelMcuClient mKernelClient;
    /** Máscara RDS experimental (OEM envía un byte en dataType=0x08). */
    private int mKernelRdsMask = 0;
    /** PTY experimental para dataType=0x09 (0..31 típico). */
    private int mKernelPtyIdx = 0;
    private NWDTunerAdapter mNwdAdapter;

    private int mLastFreq = -1;
    private SwitchCompat swDevDayModeEnabled;
    private SwitchCompat swDevReliefHdEnabled;
    private SwitchCompat swDevFileLoggingEnabled;
    private SeekBar sbDevFileLogProfile;
    private TextView tvDevFileLogProfileValue;

    public QS6EngineeringDialog(MainActivity activity) {
        super(activity);
        this.mActivity = activity;
    }

    private static Qs6KernelMcuClient acquireSharedKernelClient(android.content.Context ctx) {
        synchronized (SHARED_KERNEL_LOCK) {
            if (sSharedKernelClient == null) {
                sSharedKernelClient = new Qs6KernelMcuClient(ctx);
            }
            return sSharedKernelClient;
        }
    }

    /**
     * Libera el bind al KernelService (si existía). Llamar desde {@code Activity.onDestroy()}
     * cuando la app ya no necesita el canal experimental.
     */
    public static void releaseSharedKernelClient() {
        synchronized (SHARED_KERNEL_LOCK) {
            if (sSharedKernelClient != null) {
                try { sSharedKernelClient.setListener(null); } catch (Exception ignored) {}
                try { sSharedKernelClient.disconnect(); } catch (Exception ignored) {}
                sSharedKernelClient = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_qs6_engineering);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }

        bindViews();
        setupControls();

        mKernelClient = acquireSharedKernelClient(getContext());
        mKernelClient.setListener(bound -> mHandler.post(() -> {
            if (tvQs6KernelBind != null) tvQs6KernelBind.setText(bound ? "CONNECTED" : "DISCONNECTED");
            logEvent("MCU", "KERNEL_BIND=" + (bound ? "CONNECTED" : "DISCONNECTED"));
        }));
        // Mantener sesión: reconectar al abrir el menú (no depende de pulsar BIND_KERNEL cada vez).
        try { mKernelClient.connect(); } catch (Exception ignored) {}

        mNwdAdapter = NWDTunerAdapter.getInstance(getContext());
        try { mNwdAdapter.connect(); } catch (Exception ignored) {}

        mIsRunning = true;
        logEvent("SYS", "QS6_NWD_ENGINEERING_MATRIX_V1");
        logEvent("SYS", "NWD_AIDL=" + (getQs6Engine() != null && getQs6Engine().isNwdServiceBound()
                ? "BOUND" : "DISCONNECTED"));
        startUpdateLoop();
    }

    private void bindViews() {
        tvQs6NwdBind = findViewById(R.id.tvQs6NwdBind);
        tvQs6RdsFlags = findViewById(R.id.tvQs6RdsFlags);
        tvQs6Rssi = findViewById(R.id.tvQs6Rssi);
        tvQs6StereoUi = findViewById(R.id.tvQs6StereoUi);
        tvQs6StereoRaw = findViewById(R.id.tvQs6StereoRaw);
        tvQs6BandInfo = findViewById(R.id.tvQs6BandInfo);
        tvQs6Scan = findViewById(R.id.tvQs6Scan);
        tvQs6DxLocal = findViewById(R.id.tvQs6DxLocal);

        tvQs6Ps = findViewById(R.id.tvQs6Ps);
        tvQs6Rt = findViewById(R.id.tvQs6Rt);
        tvQs6Pi = findViewById(R.id.tvQs6Pi);
        tvQs6Pty = findViewById(R.id.tvQs6Pty);
        tvQs6RdsLock = findViewById(R.id.tvQs6RdsLock);

        tvQs6OemFocusEvent = findViewById(R.id.tvQs6OemFocusEvent);
        tvQs6OemFlags = findViewById(R.id.tvQs6OemFlags);

        tvQs6Assets = findViewById(R.id.tvQs6Assets);
        tvQs6Latency = findViewById(R.id.tvQs6Latency);
        tvQs6Memory = findViewById(R.id.tvQs6Memory);
        tvQs6Device = findViewById(R.id.tvQs6Device);
        tvQs6Root = findViewById(R.id.tvQs6Root);

        tvQs6Log = findViewById(R.id.tvQs6Log);
        scrollQs6Log = findViewById(R.id.scrollQs6Log);

        tvQs6KernelBind = findViewById(R.id.tvQs6KernelBind);
        tvQs6KernelLastCmd = findViewById(R.id.tvQs6KernelLastCmd);

        sbDevFileLogProfile = findViewById(R.id.sbDevFileLogProfile);
        tvDevFileLogProfileValue = findViewById(R.id.tvDevFileLogProfileValue);
    }

    private void setupControls() {
        if (findViewById(R.id.btnCloseQs6) != null) {
            findViewById(R.id.btnCloseQs6).setOnClickListener(v -> dismiss());
        }
        if (findViewById(R.id.btnQs6Exit) != null) {
            findViewById(R.id.btnQs6Exit).setOnClickListener(v -> dismiss());
        }

        setupTunerButton(R.id.btnQs6SeekDown, () -> {
            try {
                if (mActivity.mEngine != null) mActivity.mEngine.seekDown();
            } catch (Exception ignored) {}
            logEvent("NWD", "CMD_SEEK_DOWN");
        });
        setupTunerButton(R.id.btnQs6SeekUp, () -> {
            try {
                if (mActivity.mEngine != null) mActivity.mEngine.seekUp();
            } catch (Exception ignored) {}
            logEvent("NWD", "CMD_SEEK_UP");
        });
        setupTunerButton(R.id.btnQs6FineDown, () -> {
            try {
                if (mActivity.mEngine != null) mActivity.mEngine.stepDown();
            } catch (Exception ignored) {}
            logEvent("NWD", "CMD_STEP_DOWN");
        });
        setupTunerButton(R.id.btnQs6FineUp, () -> {
            try {
                if (mActivity.mEngine != null) mActivity.mEngine.stepUp();
            } catch (Exception ignored) {}
            logEvent("NWD", "CMD_STEP_UP");
        });

        Button btnEq = findViewById(R.id.btnQs6OpenEq);
        if (btnEq != null) {
            btnEq.setOnClickListener(v -> {
                try {
                    if (mActivity.mEngine != null) {
                        mActivity.mEngine.openEq(mActivity);
                        logEvent("NWD", "OPEN_EQ_OEM");
                    }
                } catch (Exception e) {
                    logEvent("ERR", e.getMessage());
                }
            });
        }

        Button btnWake = findViewById(R.id.btnQs6WakeNwd);
        if (btnWake != null) {
            btnWake.setOnClickListener(v -> {
                try {
                    QS6Engine eng = getQs6Engine();
                    if (eng != null) {
                        eng.wakeNwdRadioFromEngineeringMenu();
                        logEvent("NWD", "WAKE_BROADCAST_SENT");
                    }
                } catch (Exception e) {
                    logEvent("ERR", e.getMessage());
                }
            });
        }

        // QS6 KernelService -> MCU (botonera de verificación)
        setupTunerButton(R.id.btnQs6KernelBind, () -> {
            try {
                if (mKernelClient != null) {
                    // Rebind "duro"
                    mKernelClient.disconnect();
                    mKernelClient.connect();
                }
                setKernelLastCmd("BIND_KERNEL");
            } catch (Exception e) {
                logEvent("MCU_ERR", String.valueOf(e.getMessage()));
            }
        });
        setupTunerButton(R.id.btnQs6KernelInfo, () -> sendKernelCmd("RADIO_INFO",
                Qs6KernelMcuClient.buildFmRequestRadioInfo()));
        setupTunerButton(R.id.btnQs6KernelBackOn, () -> sendKernelCmd("BACK_SERVICE_ON",
                Qs6KernelMcuClient.buildFmSetBackServiceOn(true)));
        setupTunerButton(R.id.btnQs6KernelBackOff, () -> sendKernelCmd("BACK_SERVICE_OFF",
                Qs6KernelMcuClient.buildFmSetBackServiceOn(false)));

        // ACTION: type=0x03 dataType=0x01 payload[actionType, actionValue]
        // Valores confirmados en dump OEM (RadioManager.smali):
        // (1,1)/(2,1) = acción OEM "seek", (3,1)/(4,1) = acción OEM "search".
        // En campo, (1/2) se percibe como paso fino ~0.5 y (3/4) como salto de emisora.
        setupTunerButton(R.id.btnQs6KernelSeekDown, () -> sendKernelCmd("MCU_STEP_0_5_DOWN",
                Qs6KernelMcuClient.buildFmSeekDown()));
        setupTunerButton(R.id.btnQs6KernelSeekUp, () -> sendKernelCmd("MCU_STEP_0_5_UP",
                Qs6KernelMcuClient.buildFmSeekUp()));
        setupTunerButton(R.id.btnQs6KernelSearchDown, () -> sendKernelCmd("MCU_SEEK_STATION_DOWN",
                Qs6KernelMcuClient.buildFmSearchDown()));
        setupTunerButton(R.id.btnQs6KernelSearchUp, () -> sendKernelCmd("MCU_SEEK_STATION_UP",
                Qs6KernelMcuClient.buildFmSearchUp()));
        setupTunerButton(R.id.btnQs6KernelBand, () -> sendKernelCmd("MCU_BAND_CYCLE",
                Qs6KernelMcuClient.buildFmBandCycle()));
        setupTunerButton(R.id.btnQs6KernelAms, () -> sendKernelCmd("MCU_AMS",
                Qs6KernelMcuClient.buildFmAms()));

        // Tune rápido (freq en unidades NWD: 10kHz en FM)
        setupTunerButton(R.id.btnQs6KernelTune875, () -> sendKernelCmd("MCU_TUNE_87_5",
                Qs6KernelMcuClient.buildFmTune(8750, (byte) 0x00, /*prefab*/ 0)));
        setupTunerButton(R.id.btnQs6KernelTune1015, () -> sendKernelCmd("MCU_TUNE_101_5",
                Qs6KernelMcuClient.buildFmTune(10150, (byte) 0x00, /*prefab*/ 0)));

        // Más pruebas directas (KernelProtocal / RadioProtocalUtil)
        setupTunerButton(R.id.btnQs6KernelIntro, () -> sendKernelCmd("MCU_INTRO",
                Qs6KernelMcuClient.buildFmIntro()));
        setupTunerButton(R.id.btnQs6KernelNearDx, () -> sendKernelCmd("MCU_NEAR_DX",
                Qs6KernelMcuClient.buildFmNearOn(true)));
        setupTunerButton(R.id.btnQs6KernelNearLoc, () -> sendKernelCmd("MCU_NEAR_LOC",
                Qs6KernelMcuClient.buildFmNearOn(false)));

        setupTunerButton(R.id.btnQs6KernelRdsShow, () -> sendKernelCmd("RDS_SHOW_STATE",
                Qs6KernelMcuClient.buildFmRequestRadioRdsShowState()));
        setupTunerButton(R.id.btnQs6KernelRds0, () -> sendKernelRdsMask("RDS_SET", 0));
        setupTunerButton(R.id.btnQs6KernelRds7, () -> sendKernelRdsMask("RDS_SET", 7));
        setupTunerButton(R.id.btnQs6KernelRdsXor1, () -> sendKernelRdsMask("RDS_XOR", mKernelRdsMask ^ 0x01));
        setupTunerButton(R.id.btnQs6KernelRdsXor2, () -> sendKernelRdsMask("RDS_XOR", mKernelRdsMask ^ 0x02));
        setupTunerButton(R.id.btnQs6KernelRdsXor4, () -> sendKernelRdsMask("RDS_XOR", mKernelRdsMask ^ 0x04));

        setupTunerButton(R.id.btnQs6KernelPtyDown, () -> {
            mKernelPtyIdx = Math.max(0, mKernelPtyIdx - 1);
            sendKernelCmd("PTY_SET_" + mKernelPtyIdx, Qs6KernelMcuClient.buildFmSetPtyIndex(mKernelPtyIdx));
        });
        setupTunerButton(R.id.btnQs6KernelPtyUp, () -> {
            mKernelPtyIdx = Math.min(31, mKernelPtyIdx + 1);
            sendKernelCmd("PTY_SET_" + mKernelPtyIdx, Qs6KernelMcuClient.buildFmSetPtyIndex(mKernelPtyIdx));
        });
        setupTunerButton(R.id.btnQs6KernelSaveP1, () -> sendKernelSavePreset(1));
        setupTunerButton(R.id.btnQs6KernelSaveP2, () -> sendKernelSavePreset(2));
        setupTunerButton(R.id.btnQs6KernelSaveP3, () -> sendKernelSavePreset(3));
        setupTunerButton(R.id.btnQs6KernelSaveP4, () -> sendKernelSavePreset(4));
        setupTunerButton(R.id.btnQs6KernelSaveP5, () -> sendKernelSavePreset(5));
        setupTunerButton(R.id.btnQs6KernelSaveP6, () -> sendKernelSavePreset(6));
        setupTunerButton(R.id.btnQs6KernelSaveP7, () -> sendKernelSavePreset(7));
        setupTunerButton(R.id.btnQs6KernelSaveP8, () -> sendKernelSavePreset(8));
        setupTunerButton(R.id.btnQs6KernelSaveP9, () -> sendKernelSavePreset(9));
        setupTunerButton(R.id.btnQs6KernelSaveP10, () -> sendKernelSavePreset(10));
        setupTunerButton(R.id.btnQs6KernelSaveP11, () -> sendKernelSavePreset(11));
        setupTunerButton(R.id.btnQs6KernelSaveP12, () -> sendKernelSavePreset(12));
        setupTunerButton(R.id.btnQs6KernelSaveP13, () -> sendKernelSavePreset(13));
        setupTunerButton(R.id.btnQs6KernelSaveP14, () -> sendKernelSavePreset(14));
        setupTunerButton(R.id.btnQs6KernelSaveP15, () -> sendKernelSavePreset(15));
        setupTunerButton(R.id.btnQs6KernelSaveP16, () -> sendKernelSavePreset(16));
        setupTunerButton(R.id.btnQs6KernelSavePAll, this::sendKernelSaveAllPresetsSweep);

        // AIDL RadioFeature (NWD)
        setupTunerButton(R.id.btnQs6AidlRefresh, () -> {
            if (mNwdAdapter == null) mNwdAdapter = NWDTunerAdapter.getInstance(getContext());
            String st = (mNwdAdapter != null) ? mNwdAdapter.getDebugStatus() : "AIDL=NULL";
            logEvent("AIDL", st);
        });
        setupTunerButton(R.id.btnQs6AidlIntro, () -> {
            try {
                if (mNwdAdapter != null) mNwdAdapter.intro();
                logEvent("AIDL", "INTRO()");
            } catch (Throwable t) { logEvent("AIDL_ERR", t.getClass().getSimpleName()); }
        });
        setupTunerButton(R.id.btnQs6AidlStereoOn, () -> {
            try {
                if (mNwdAdapter != null) mNwdAdapter.setStereoOn(true);
                logEvent("AIDL", "STEREO_ON");
            } catch (Throwable t) { logEvent("AIDL_ERR", t.getClass().getSimpleName()); }
        });
        setupTunerButton(R.id.btnQs6AidlStereoOff, () -> {
            try {
                if (mNwdAdapter != null) mNwdAdapter.setStereoOn(false);
                logEvent("AIDL", "STEREO_OFF");
            } catch (Throwable t) { logEvent("AIDL_ERR", t.getClass().getSimpleName()); }
        });
        setupTunerButton(R.id.btnQs6AidlPrefPrev, () -> {
            try {
                if (mNwdAdapter != null) mNwdAdapter.prefeb(false);
                logEvent("AIDL", "PREFEB<<");
            } catch (Throwable t) { logEvent("AIDL_ERR", t.getClass().getSimpleName()); }
        });
        setupTunerButton(R.id.btnQs6AidlPrefNext, () -> {
            try {
                if (mNwdAdapter != null) mNwdAdapter.prefeb(true);
                logEvent("AIDL", "PREFEB>>");
            } catch (Throwable t) { logEvent("AIDL_ERR", t.getClass().getSimpleName()); }
        });

        // Shadow / broadcasts
        setupTunerButton(R.id.btnQs6ShadowStopSearch, () -> {
            try {
                if (mNwdAdapter != null) mNwdAdapter.stopScan();
                logEvent("SHADOW", "ACTION_STOP_SEARCH");
            } catch (Throwable t) { logEvent("SHADOW_ERR", t.getClass().getSimpleName()); }
        });

        DevAutoscanToggleHelper.bind((SwitchCompat) findViewById(R.id.swDevAutoScanEnabled), mActivity);
        DevAutoscanToggleHelper.bindThresholdSeekBar(
                (SeekBar) findViewById(R.id.sbDevAutoScanThreshold),
                findViewById(R.id.tvDevAutoScanThresholdValue),
                mActivity);

        // Kill-switch para Modo Día (UI)
        swDevDayModeEnabled = findViewById(R.id.swDevDayModeEnabled);
        if (swDevDayModeEnabled != null) {
            android.content.SharedPreferences prefs =
                    mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
            boolean on = true;
            try { on = prefs.getBoolean("pref_dev_day_mode_enabled", true); } catch (Exception ignored) {}
            swDevDayModeEnabled.setChecked(on);
            swDevDayModeEnabled.setOnCheckedChangeListener((btn, checked) -> {
                try { prefs.edit().putBoolean("pref_dev_day_mode_enabled", checked).apply(); } catch (Exception ignored) {}
                logEvent("DEV", "pref_dev_day_mode_enabled=" + checked);
                try {
                    if (!checked && mActivity.mThemeManager != null
                            && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.DAY_MODE) {
                        mActivity.mThemeManager.setSkin(com.example.openradiofm.ui.theme.ThemeManager.Skin.CLASSIC);
                        mActivity.applySkin(com.example.openradiofm.ui.theme.ThemeManager.Skin.CLASSIC);
                    }
                } catch (Exception ignored2) {}
            });
        }

        // Kill-switch: mostrar/ocultar Relieve HD en Ajustes premium
        swDevReliefHdEnabled = findViewById(R.id.swDevReliefHdEnabled);
        if (swDevReliefHdEnabled != null) {
            android.content.SharedPreferences prefs =
                    mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
            boolean on = true;
            try { on = prefs.getBoolean("pref_dev_relief_hd_enabled", true); } catch (Exception ignored) {}
            swDevReliefHdEnabled.setChecked(on);
            swDevReliefHdEnabled.setOnCheckedChangeListener((btn, checked) -> {
                try { prefs.edit().putBoolean("pref_dev_relief_hd_enabled", checked).apply(); } catch (Exception ignored) {}
                logEvent("DEV", "pref_dev_relief_hd_enabled=" + checked);
                if (!checked) {
                    try {
                        prefs.edit().putBoolean("pref_relief_hd", false).apply();
                        mActivity.applyReliefHd(false);
                    } catch (Exception ignored2) {}
                }
            });
        }

        // Dev: log a fichero en RadioLogos
        swDevFileLoggingEnabled = findViewById(R.id.swDevFileLoggingEnabled);
        if (swDevFileLoggingEnabled != null) {
            android.content.SharedPreferences prefs =
                    mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
            boolean on = false;
            try { on = prefs.getBoolean(com.example.openradiofm.utils.RadioActivityFileLogger.PREF_DEV_FILE_LOG_ENABLED, false); } catch (Exception ignored) {}
            swDevFileLoggingEnabled.setChecked(on);
            swDevFileLoggingEnabled.setOnCheckedChangeListener((btn, checked) -> {
                com.example.openradiofm.utils.RadioActivityFileLogger.onToggleChanged(mActivity, checked);
                logEvent("DEV", com.example.openradiofm.utils.RadioActivityFileLogger.PREF_DEV_FILE_LOG_ENABLED + "=" + checked);
            });
        }

        // Perfil (0..2): BASIC / MEDIUM / FULL
        if (sbDevFileLogProfile != null) {
            android.content.SharedPreferences prefs =
                    mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
            int prof = com.example.openradiofm.utils.RadioActivityFileLogger.PROFILE_BASIC;
            try { prof = prefs.getInt(com.example.openradiofm.utils.RadioActivityFileLogger.PREF_DEV_FILE_LOG_PROFILE,
                    com.example.openradiofm.utils.RadioActivityFileLogger.PROFILE_BASIC); } catch (Exception ignored) {}
            if (prof < 0) prof = 0;
            if (prof > 2) prof = 2;
            sbDevFileLogProfile.setProgress(prof);
            if (tvDevFileLogProfileValue != null) tvDevFileLogProfileValue.setText(profileLabel(prof));
            sbDevFileLogProfile.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int p = Math.max(0, Math.min(progress, 2));
                    try { prefs.edit().putInt(com.example.openradiofm.utils.RadioActivityFileLogger.PREF_DEV_FILE_LOG_PROFILE, p).apply(); } catch (Exception ignored) {}
                    if (tvDevFileLogProfileValue != null) tvDevFileLogProfileValue.setText(profileLabel(p));
                    logEvent("DEV", com.example.openradiofm.utils.RadioActivityFileLogger.PREF_DEV_FILE_LOG_PROFILE + "=" + p);
                    try { com.example.openradiofm.utils.RadioActivityFileLogger.logBasic(mActivity, "DEV", "FILE_LOG_PROFILE=" + profileLabel(p)); } catch (Exception ignored2) {}
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    private void setKernelLastCmd(String s) {
        if (tvQs6KernelLastCmd != null) tvQs6KernelLastCmd.setText(s != null ? s : "-");
    }

    private void sendKernelCmd(String label, byte[] frame) {
        try {
            if (mKernelClient == null) {
                logEvent("MCU_ERR", "Kernel client NULL");
                return;
            }
            if (!mKernelClient.isBound()) {
                mKernelClient.connect();
                logEvent("MCU", "AUTO_BIND (sending " + label + ")");
            }
            mKernelClient.requestRaw(frame);
            setKernelLastCmd(label);
            logEvent("MCU", "TX " + label + " (" + frame.length + "B)");
        } catch (Throwable t) {
            setKernelLastCmd("ERR:" + label);
            logEvent("MCU_ERR", label + " -> " + t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage()));
        }
    }

    private void sendKernelRdsMask(String labelPrefix, int mask8) {
        int m = mask8 & 0xFF;
        mKernelRdsMask = m;
        String label = labelPrefix + "_0b" + Integer.toBinaryString(m);
        sendKernelCmd(label, Qs6KernelMcuClient.buildFmSetRdsState(m));
    }

    private int kernelFrequencyUnitsForCurrentTuner() {
        if (mActivity == null || mActivity.mEngine == null) return 0;
        int khz = mActivity.mEngine.getCurrentFreq();
        int band = mActivity.mEngine.getCurrentBand();
        if (band < 3) {
            // FM en NWD: unidades de 10kHz
            return Math.max(0, khz / 10);
        }
        // AM: kHz directos (según adaptador/NWD en QS6)
        return Math.max(0, khz);
    }

    private void sendKernelSavePreset(int presetIndex) {
        try {
            if (mActivity == null || mActivity.mEngine == null) {
                logEvent("MCU_ERR", "SAVE_PRESET: engine NULL");
                return;
            }
            int band = mActivity.mEngine.getCurrentBand();
            int freqUnits = kernelFrequencyUnitsForCurrentTuner();
            if (freqUnits <= 0) {
                logEvent("MCU_ERR", "SAVE_PRESET: freqUnits=0");
                return;
            }
            int idx = Math.max(0, Math.min(presetIndex, 15));
            sendKernelCmd("SAVE_P" + idx + "_B" + band + "_F" + freqUnits,
                    Qs6KernelMcuClient.buildFmSaveCurrentFrequency((byte) band, idx, freqUnits));
        } catch (Throwable t) {
            logEvent("MCU_ERR", "SAVE_PRESET -> " + t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage()));
        }
    }

    private void sendKernelSaveAllPresetsSweep() {
        // Guarda P1..P16 con la frecuencia actual. Útil para validar que el MCU acepta todos los índices.
        try {
            for (int i = 1; i <= 16; i++) {
                sendKernelSavePreset(i);
            }
            logEvent("MCU", "SAVE_ALL_DONE");
        } catch (Throwable t) {
            logEvent("MCU_ERR", "SAVE_ALL -> " + t.getClass().getSimpleName());
        }
    }

    private static String profileLabel(int p) {
        switch (p) {
            case 1: return "MEDIUM";
            case 2: return "FULL";
            default: return "BASIC";
        }
    }

    private void setupTunerButton(int resId, Runnable action) {
        View v = findViewById(resId);
        if (v != null) v.setOnClickListener(v1 -> action.run());
    }

    private QS6Engine getQs6Engine() {
        if (mActivity.mEngine instanceof QS6Engine) {
            return (QS6Engine) mActivity.mEngine;
        }
        return null;
    }

    private void startUpdateLoop() {
        mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mIsRunning || !isShowing()) return;
                updateMetrics();
                checkAssets();
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.post(mUpdateRunnable);
    }

    private void updateMetrics() {
        if (mActivity == null) return;

        try {
            QS6Engine qs6 = getQs6Engine();

            int freq = (mActivity.mEngine != null) ? mActivity.mEngine.getCurrentFreq() : 0;
            if (freq != mLastFreq) {
                logEvent("RF", String.format(Locale.US, "FREQ_TUNED: %.2f MHz", freq / 1000.0f));
                mLastFreq = freq;
            }

            if (tvQs6NwdBind != null) {
                tvQs6NwdBind.setText(qs6 != null && qs6.isNwdServiceBound() ? "CONNECTED" : "DISCONNECTED");
            }

            if (tvQs6KernelBind != null && mKernelClient != null) {
                tvQs6KernelBind.setText(mKernelClient.isBound() ? "CONNECTED" : "DISCONNECTED");
            }

            if (tvQs6RdsFlags != null && mActivity.mEngine != null) {
                tvQs6RdsFlags.setText(String.format(Locale.US, "AF=%s TA=%s TP=%s",
                        mActivity.mEngine.isAfEnabled(),
                        mActivity.mEngine.isTaEnabled(),
                        mActivity.mEngine.isTpEnabled()));
            }

            boolean scanning = mActivity.mEngine != null && mActivity.mEngine.isScanning();
            boolean dx = mActivity.mEngine != null && !mActivity.mEngine.isDxLocal();
            int band = (mActivity.mEngine != null) ? mActivity.mEngine.getCurrentBand() : 0;

            if (tvQs6Scan != null) tvQs6Scan.setText(scanning ? "RUNNING..." : "IDLE");
            if (tvQs6DxLocal != null) tvQs6DxLocal.setText(dx ? "DX" : "LOC");
            if (tvQs6BandInfo != null) tvQs6BandInfo.setText(String.valueOf(band));

            if (qs6 != null) {
                boolean uiStereo = qs6.isStereo();
                if (tvQs6StereoUi != null) {
                    tvQs6StereoUi.setText(uiStereo ? "STEREO (UI)" : "MONO");
                }
                if (tvQs6StereoRaw != null) {
                    tvQs6StereoRaw.setText(String.format(Locale.US, "PILOT=%s DEC=%s",
                            qs6.isStereoPilotReported(), qs6.isStereoDecoderEnabled()));
                }
            } else {
                if (tvQs6StereoUi != null) tvQs6StereoUi.setText("N/A");
                if (tvQs6StereoRaw != null) tvQs6StereoRaw.setText("N/A");
            }

            int sqi = mActivity.mRdsLockUiTick.hasLock ? 80 : 25;
            StringBuilder bar = new StringBuilder("[");
            int bars = sqi / 10;
            for (int i = 0; i < 10; i++) bar.append(i < bars ? "█" : "░");
            bar.append("]");
            if (tvQs6Rssi != null) {
                tvQs6Rssi.setText(String.format(Locale.US, "[est] %s RDS_LOCK=%s", bar, mActivity.mRdsLockUiTick.hasLock));
            }

            TextView mainRdsName = mActivity.findViewById(R.id.tvRdsName);
            TextView mainRdsInfo = mActivity.findViewById(R.id.tvRdsInfo);

            if (tvQs6Ps != null) tvQs6Ps.setText(mainRdsName != null ? mainRdsName.getText() : "NONE");
            String rt = (mainRdsInfo != null) ? mainRdsInfo.getText().toString() : "WAITING...";
            if (tvQs6Rt != null) tvQs6Rt.setText(rt.length() > 36 ? rt.substring(0, 33) + "..." : rt);
            if (tvQs6Pi != null) tvQs6Pi.setText(mActivity.mCurrentPi != null ? mActivity.mCurrentPi : "—");
            if (tvQs6Pty != null) tvQs6Pty.setText(mActivity.mCurrentPty != null ? mActivity.mCurrentPty : "00");
            if (tvQs6RdsLock != null) tvQs6RdsLock.setText(mActivity.mRdsLockUiTick.hasLock ? "YES" : "NO");

            try {
                android.content.SharedPreferences prefs =
                        mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
                String lastFocus = prefs.getString("oem_last_focus_event", "N/A");
                boolean userPaused = prefs.getBoolean("oem_user_paused", false);
                boolean wasPlayingBeforeLoss = prefs.getBoolean("oem_was_playing_before_focus_loss", false);
                boolean isPlaying = prefs.getBoolean("oem_is_playing", false);

                if (tvQs6OemFocusEvent != null) tvQs6OemFocusEvent.setText(lastFocus);
                if (tvQs6OemFlags != null) {
                    tvQs6OemFlags.setText("playing=" + isPlaying + " userPaused=" + userPaused
                            + " resumeArmed=" + wasPlayingBeforeLoss);
                }
            } catch (Exception ignored) {}

            long startNs = System.nanoTime();
            if (mActivity.mEngine != null) mActivity.mEngine.getCurrentFreq();
            double latencyMs = (System.nanoTime() - startNs) / 1000000.0;
            if (tvQs6Latency != null) tvQs6Latency.setText(String.format(Locale.US, "%.2f ms", latencyMs));

            long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long used = total - (Runtime.getRuntime().freeMemory() / 1024 / 1024);
            if (tvQs6Memory != null) tvQs6Memory.setText(used + "MB / " + total + "MB");

            if (tvQs6Device != null) tvQs6Device.setText(android.os.Build.MODEL.toUpperCase(Locale.US));
            if (tvQs6Root != null) {
                tvQs6Root.setText(new File("/system/xbin/su").exists() ? "GRANTED" : "DENIED");
            }

        } catch (Exception e) {
            logEvent("ERR", e.getMessage());
        }
    }

    private void checkAssets() {
        File appDir = new File(mActivity.getExternalFilesDir(null) != null
                ? mActivity.getExternalFilesDir(null) : mActivity.getFilesDir(), "RadioLogos");
        File legacyDir = new File("/sdcard/RadioLogos");
        File dir = (legacyDir.exists() && legacyDir.isDirectory()) ? legacyDir : appDir;
        StringBuilder sb = new StringBuilder();
        sb.append("PATH....: ").append(dir.getAbsolutePath()).append("\n");
        sb.append("FOLDER..: ").append(dir.exists() ? "READY" : "MISSING").append("\n");

        File bg = new File(dir, "background.png");
        if (!bg.exists()) bg = new File(dir, "background.jpg");
        sb.append("BG_IMG..: ").append(bg.exists() ? "OK (" + (bg.length() / 1024) + "KB)" : "FAIL").append("\n");

        File car = new File(dir, "car_logo.png");
        sb.append("CAR_ICO.: ").append(car.exists() ? "OK" : "FAIL").append("\n");

        File[] favs = dir.listFiles((d, name) -> name.endsWith(".fav"));
        sb.append("FAVS....: ").append(favs != null ? favs.length : 0).append(" OBJECTS");

        if (tvQs6Assets != null) tvQs6Assets.setText(sb.toString());
    }

    private void logEvent(String tag, String msg) {
        if (tvQs6Log == null) return;
        String entry = String.format("[%s] %s: %s\n", timeFormat.format(new Date()), tag, msg);
        mHandler.post(() -> {
            tvQs6Log.append(entry);
            if (scrollQs6Log != null) scrollQs6Log.fullScroll(View.FOCUS_DOWN);
        });
    }

    public void addRdsLog(String msg) {
        logEvent("RDS_RAW", msg);
    }

    public void updateSignalQuality(int rssi, int snr) {
        logEvent("SIG", String.format(Locale.US, "RSSI=%d SNR=%d", rssi, snr));
    }

    @Override
    public void dismiss() {
        mIsRunning = false;
        mHandler.removeCallbacks(mUpdateRunnable);
        try {
            if (mKernelClient != null) mKernelClient.setListener(null);
        } catch (Exception ignored) {}
        super.dismiss();
    }
}
