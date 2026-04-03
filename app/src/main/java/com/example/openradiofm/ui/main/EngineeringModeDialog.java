package com.example.openradiofm.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.view.Window;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.Context;
import androidx.appcompat.widget.SwitchCompat;

import com.example.openradiofm.R;
import java.util.Locale;
import java.io.File;
import android.widget.Button;
import android.widget.ScrollView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EngineeringModeDialog extends Dialog {

    private final MainActivity mActivity;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mUpdateRunnable;
    private boolean mIsRunning = false;

    // UI Elements - RF
    private TextView tvSignalQualityIndex, tvStereoPilot, tvTunerMode, tvRssiBar, tvChipset;
    
    // UI Elements - RDS
    private TextView tvPsName, tvRtText, tvPiCode, tvPtyRaw, tvRdsSync, tvAfList;
    
    // UI Elements - System
    private TextView tvServiceLatency, tvMemoryUsage, tvDeviceInfo, tvRootStatus;
    
    // UI Elements - Assets
    private TextView tvAssetsInfo;

    // Log & Controls
    private TextView tvTerminalLog;
    private ScrollView scrollLog;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    // Dev toggles (MT8163)
    private SwitchCompat swMt8163GlobalStreamMute;
    // Dev toggle (MTK8259)
    private SwitchCompat swMtk8259V5StreamMixerCompat;
    private SwitchCompat swDevAutoScanEnabled;
    private android.content.SharedPreferences mPrefs;
    
    private int mLastFreq = -1;

    public EngineeringModeDialog(MainActivity activity) {
        super(activity);
        this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_engineering_mode);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }

        bindViews();
        setupControls();
        
        mIsRunning = true;
        logEvent("SYS", "ENGINEERING MATRIC INITIALIZED");
        String eng = (mActivity.mEngine != null && mActivity.mEngine.getEngineName() != null)
                ? mActivity.mEngine.getEngineName() : "?";
        logEvent("SYS", "MODE: " + mActivity.mMode + " | ENGINE: " + eng);
        startUpdateLoop();
    }

    private void bindViews() {
        mPrefs = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);

        // RF
        tvSignalQualityIndex = findViewById(R.id.tvSignalQualityIndex);
        tvStereoPilot = findViewById(R.id.tvStereoPilot);
        tvTunerMode = findViewById(R.id.tvTunerMode);
        tvRssiBar = findViewById(R.id.tvRssiBar);
        tvChipset = findViewById(R.id.tvChipset);

        // RDS
        tvPsName = findViewById(R.id.tvPsName);
        tvRtText = findViewById(R.id.tvRtText);
        tvPiCode = findViewById(R.id.tvPiCode);
        tvPtyRaw = findViewById(R.id.tvPtyRaw);
        tvRdsSync = findViewById(R.id.tvRdsSync);
        tvAfList = findViewById(R.id.tvAfList);

        // System
        tvServiceLatency = findViewById(R.id.tvServiceLatency);
        tvMemoryUsage = findViewById(R.id.tvMemoryUsage);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        tvRootStatus = findViewById(R.id.tvRootStatus);
        
        // Assets
        tvAssetsInfo = findViewById(R.id.tvAssetsInfo);
        
        // Log
        tvTerminalLog = findViewById(R.id.tvTerminalLog);
        scrollLog = findViewById(R.id.scrollLog);

        // Dev toggles
        swMt8163GlobalStreamMute = findViewById(R.id.swMt8163GlobalStreamMute);
        swMtk8259V5StreamMixerCompat = findViewById(R.id.swMtk8259V5StreamMixerCompat);
        swDevAutoScanEnabled = findViewById(R.id.swDevAutoScanEnabled);
    }

    private void setupControls() {
        findViewById(R.id.btnCloseEng).setOnClickListener(v -> dismiss());
        findViewById(R.id.btnExitSystem).setOnClickListener(v -> dismiss());
        
        findViewById(R.id.btnTuneDown).setOnClickListener(v -> {
            if (mActivity.mEngine != null) {
                mActivity.mEngine.stepDown();
                logEvent("RF", "STEP_DOWN_CMD");
            }
        });
        
        findViewById(R.id.btnTuneUp).setOnClickListener(v -> {
            if (mActivity.mEngine != null) {
                mActivity.mEngine.stepUp();
                logEvent("RF", "STEP_UP_CMD");
            }
        });

        if (swMt8163GlobalStreamMute != null) {
            boolean enabled = false;
            try { enabled = mPrefs.getBoolean("pref_mt8163_global_stream_mute", false); }
            catch (Exception ignored) {}
            swMt8163GlobalStreamMute.setChecked(enabled);
            swMt8163GlobalStreamMute.setOnCheckedChangeListener((btn, checked) -> {
                try {
                    mPrefs.edit().putBoolean("pref_mt8163_global_stream_mute", checked).apply();
                } catch (Exception ignored) {}
                logEvent("DEV", "pref_mt8163_global_stream_mute=" + checked);
                if (checked) {
                    logEvent("WARN", "STREAM_MUSIC mute ON: puede silenciar Spotify/BT/Android Auto");
                }
            });
        }

        if (swMtk8259V5StreamMixerCompat != null) {
            boolean enabled = false;
            try { enabled = mPrefs.getBoolean("pref_mtk8259_v5_stream_mixer_compat", false); }
            catch (Exception ignored) {}
            swMtk8259V5StreamMixerCompat.setChecked(enabled);
            swMtk8259V5StreamMixerCompat.setOnCheckedChangeListener((btn, checked) -> {
                try {
                    mPrefs.edit().putBoolean("pref_mtk8259_v5_stream_mixer_compat", checked).apply();
                } catch (Exception ignored) {}
                logEvent("DEV", "pref_mtk8259_v5_stream_mixer_compat=" + checked);
                if (checked) {
                    logEvent("WARN", "MTK8259 legacy mixer ON: solo Close/OpenRadioCh (tipo v5.0)");
                }
            });
        }

        DevAutoscanToggleHelper.bind(swDevAutoScanEnabled, mActivity);
        DevAutoscanToggleHelper.bindThresholdSeekBar(
                (android.widget.SeekBar) findViewById(R.id.sbDevAutoScanThreshold),
                findViewById(R.id.tvDevAutoScanThresholdValue),
                mActivity);
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
            // 1. RF
            int freq = (mActivity.mEngine != null) ? mActivity.mEngine.getCurrentFreq() : 0;
            if (freq != mLastFreq) {
                logEvent("RF", String.format(Locale.US, "FREQ_CHANGED -> %.2f MHz", freq / 1000.0f));
                mLastFreq = freq;
            }

            boolean isStereo = (mActivity.mEngine != null) && mActivity.mEngine.isStereo();
            boolean isDx = (mActivity.mEngine != null) && !mActivity.mEngine.isDxLocal();
            
            tvSignalQualityIndex.setText(mActivity.mHasRdsLock ? "85% (RDS_LOCK)" : "40% (LOW)");
            tvStereoPilot.setText(isStereo ? "LOCKED (19kHz)" : "NO_PILOT");
            tvTunerMode.setText(isDx ? "DX (DISTANT)" : "LOC (LOCAL)");
            
            // RSSI Simulado
            int rssi = mActivity.mHasRdsLock ? -60 : -95;
            StringBuilder bar = new StringBuilder("[");
            int bars = (rssi + 110) / 10;
            for(int i=0; i<10; i++) bar.append(i < bars ? "█" : "░");
            bar.append("]");
            tvRssiBar.setText(String.format(Locale.US, "[%ddBm] %s", rssi, bar.toString()));
            
            tvChipset.setText(mActivity.mEngine != null ? mActivity.mEngine.getEngineName().toUpperCase() : "N/A");

            // 2. RDS
            TextView mainRdsName = mActivity.findViewById(R.id.tvRdsName);
            TextView mainRdsInfo = mActivity.findViewById(R.id.tvRdsInfo);
            
            String ps = (mainRdsName != null) ? mainRdsName.getText().toString() : "N/A";
            String rt = (mainRdsInfo != null) ? mainRdsInfo.getText().toString() : "WAITING...";
            
            tvPsName.setText(ps);
            tvRtText.setText(rt.length() > 30 ? rt.substring(0, 27) + "..." : rt);
            tvPiCode.setText("WAITING...");
            tvPtyRaw.setText(mActivity.mCurrentPty != null ? mActivity.mCurrentPty : "00");
            tvRdsSync.setText(mActivity.mHasRdsLock ? "LOCKED" : "SEARCHING");
            tvAfList.setText("ENABLE: " + (mActivity.mEngine != null && mActivity.mEngine.isAfEnabled()));

            // 3. System
            long start = System.nanoTime();
            if (mActivity.mEngine != null) mActivity.mEngine.getCurrentFreq();
            double latency = (System.nanoTime() - start) / 1000000.0;
            tvServiceLatency.setText(String.format(Locale.US, "%.2f ms", latency));

            long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long used = (total - (Runtime.getRuntime().freeMemory() / 1024 / 1024));
            tvMemoryUsage.setText(used + "MB / " + total + "MB");
            
            tvDeviceInfo.setText(android.os.Build.MODEL.toUpperCase());
            tvRootStatus.setText(new File("/system/xbin/su").exists() ? "GRANTED" : "DENIED");

        } catch (Exception e) {
            logEvent("ERR", e.getMessage());
        }
    }

    private void checkAssets() {
        File appDir = new File(mActivity.getExternalFilesDir(null) != null ? mActivity.getExternalFilesDir(null) : mActivity.getFilesDir(), "RadioLogos");
        File legacyDir = new File("/sdcard/RadioLogos");
        File dir = (legacyDir.exists() && legacyDir.isDirectory()) ? legacyDir : appDir;
        StringBuilder sb = new StringBuilder();
        sb.append("PATH...........: ").append(dir.getAbsolutePath()).append("\n");
        sb.append("DIR_STATUS....: ").append(dir.exists() ? "OK" : "MISSING").append("\n");
        
        File bg = new File(dir, "background.png");
        if (!bg.exists()) bg = new File(dir, "background.jpg");
        sb.append("BG_FILE.......: ").append(bg.exists() ? "FOUND (" + (bg.length()/1024) + "KB)" : "NOT_FOUND").append("\n");
        
        File car = new File(dir, "car_logo.png");
        sb.append("CAR_LOGO......: ").append(car.exists() ? "FOUND" : "NOT_FOUND").append("\n");
        
        File[] favs = dir.listFiles((d, name) -> name.endsWith(".fav"));
        sb.append("FAV_OBJECTS...: ").append(favs != null ? favs.length : 0).append(" FILES");
        
        tvAssetsInfo.setText(sb.toString());
    }

    private void logEvent(String tag, String msg) {
        if (tvTerminalLog == null) return;
        String entry = String.format("[%s] %s: %s\n", timeFormat.format(new Date()), tag, msg);
        mHandler.post(() -> {
            tvTerminalLog.append(entry);
            if (scrollLog != null) scrollLog.fullScroll(View.FOCUS_DOWN);
        });
    }

    @Override
    public void dismiss() {
        mIsRunning = false;
        mHandler.removeCallbacks(mUpdateRunnable);
        super.dismiss();
    }
}
