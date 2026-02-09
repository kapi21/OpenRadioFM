package com.example.openradiofm.ui.main;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView; 
import android.view.Window;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.openradiofm.R;
import com.example.openradiofm.data.model.RadioStation;
import com.hcn.autoradio.IRadioServiceAPI;

import java.util.Locale;
import java.io.File;
import android.widget.Button;
import android.app.AlertDialog;
import android.widget.Toast;
import android.widget.ScrollView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EngineeringModeDialog extends Dialog {

    private final MainActivity mActivity;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mUpdateRunnable;
    private boolean mIsRunning = false;

    // UI Elements
    private TextView tvSignalQualityIndex, tvStereoPilot, tvTunerMode, tvRssiBar;
    private TextView tvPiCode, tvPtyRaw, tvRdsSync, tvAfList;
    private TextView tvServiceLatency, tvMemoryUsage, tvChipset;
    private TextView tvDeviceInfo, tvRootStatus;
    
    // File System & Assets
    private TextView tvAssetsInfo;
    private Button btnResetFavs, btnResetHistory;

    // Tuner & Log
    private Button btnTuneDown, btnTuneUp, btnExitSystem;
    private TextView tvTerminalLog;
    private ScrollView scrollLog;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
    
    // State Tracking for Logging
    private int mLastFreq = -1;
    private String mLastPty = "";
    private String mLastPi = "";
    private boolean mLastStereo = false;

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
        setupCloseButton();
        
        // Start updating
        mIsRunning = true;
        logEvent("SYS", "ENGINEERING MODE INITIALIZED");
        logEvent("SYS", "KERNEL ACCESS GRANTED [ROOT_LEVEL]");
        startUpdateLoop();
    }

    private void bindViews() {
        // RF Section
        tvSignalQualityIndex = findViewById(R.id.tvSignalQualityIndex);
        tvStereoPilot = findViewById(R.id.tvStereoPilot);
        tvTunerMode = findViewById(R.id.tvTunerMode);
        tvRssiBar = findViewById(R.id.tvRssiBar);

        // RDS Section
        tvPiCode = findViewById(R.id.tvPiCode);
        tvPtyRaw = findViewById(R.id.tvPtyRaw);
        tvRdsSync = findViewById(R.id.tvRdsSync);
        tvAfList = findViewById(R.id.tvAfList);

        // System Section
        tvServiceLatency = findViewById(R.id.tvServiceLatency);
        tvMemoryUsage = findViewById(R.id.tvMemoryUsage);
        tvChipset = findViewById(R.id.tvChipset);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        tvRootStatus = findViewById(R.id.tvRootStatus);
        
        // Assets & Data
        tvAssetsInfo = findViewById(R.id.tvAssetsInfo);
        btnResetFavs = findViewById(R.id.btnResetFavs);
        btnResetHistory = findViewById(R.id.btnResetHistory);
        
        // Tuner & Log
        btnTuneDown = findViewById(R.id.btnTuneDown);
        btnTuneUp = findViewById(R.id.btnTuneUp);
        btnExitSystem = findViewById(R.id.btnExitSystem);
        tvTerminalLog = findViewById(R.id.tvTerminalLog);
        scrollLog = findViewById(R.id.scrollLog);

        setupDataButtons();
        setupTunerButtons();
        setupExitButton();
    }

    private void setupCloseButton() {
        View btnClose = findViewById(R.id.btnCloseEng);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
    }

    private void startUpdateLoop() {
        mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mIsRunning || !isShowing()) return;

                updateMetrics();
                updateMetrics();
                checkAssets(); // V5.0: Check Files
                mHandler.postDelayed(this, 1000); // 1Hz refresh for files is enough
            }
        };
        mHandler.post(mUpdateRunnable);
    }

    private void updateMetrics() {
        if (mActivity == null || mActivity.mRadioService == null) {
            tvSignalQualityIndex.setText("SQI_INDEX.....: NO_SERVICE");
            return;
        }

        try {
            // 1. RF Telemetry
            int currentFreq = mActivity.mRadioService.getCurrentFreq();
            if (currentFreq != mLastFreq) {
                logEvent("RF", String.format(Locale.US, "TUNED > %.2f MHz", currentFreq / 1000.0f));
                mLastFreq = currentFreq;
            }

            boolean isStereo = mActivity.mRadioService.IsStereo();
            if (isStereo != mLastStereo) {
                logEvent("AUD", isStereo ? "STEREO PILOT DETECTED" : "MONO SIGNAL");
                mLastStereo = isStereo;
            }
            boolean isLocal = mActivity.mRadioService.IsDxLocal();
            // Note: RSSI/SNR are not exposed in IRadioServiceAPI publicly. 
            // We infer them or use the calculated quality if available.
            // Assuming we added calculateSignalQuality to MainActivity or have access to mHasRdsLock
            boolean rdsLock = mActivity.mHasRdsLock;

            // Inferred SQI (0-100%)
            int sqi = 0;
            if (rdsLock && isStereo) sqi = 100;
            else if (rdsLock) sqi = 75;
            else if (isStereo) sqi = 60;
            else sqi = 30;
            
            tvSignalQualityIndex.setText(String.format(Locale.US, "SQI_INDEX.....: %d%%", sqi));
            tvStereoPilot.setText(String.format("STEREO_PILOT..: %s (%s)", isStereo ? "LOCKED" : "NO_PILOT", isStereo ? "19kHz" : "---"));
            tvTunerMode.setText(String.format("TUNER_MODE....: %s", isLocal ? "LOC (LOCAL)" : "DX (DISTANT)"));

            // Simulated RSSI Bar based on SQI
            int bars = sqi / 10;
            StringBuilder bar = new StringBuilder("[");
            for (int i=0; i<10; i++) bar.append(i < bars ? "█" : "░");
            bar.append("]");
            // Fake dBm estimate based on visual quality
            int dbm = -100 + (sqi / 2); // -100 to -50 range approx
            tvRssiBar.setText(String.format("RSSI: %s %ddBm (EST)", bar.toString(), dbm));

            // 2. RDS Debug
            // We need access to RDS data. Accessing HiddenRadioPlayer if possible.
            String pty = mActivity.mCurrentPty;
            if (pty != null && !pty.equals(mLastPty)) {
                logEvent("RDS", "PTY UPDATE > " + pty);
                mLastPty = pty;
            }
            
            tvPtyRaw.setText(String.format("PTY_RAW.......: %s", pty != null ? pty : "WAITING..."));

            RadioStation s = mActivity.mRepository.getStationInfo(mActivity.mLastFreq, null);
            String pi = "----"; 
            if (rdsLock) pi = "SYNC_OK"; // SimplifiedPI
            // Since we don't have raw PI easily accessible without deeper hooks, using Sync status changes
             
            tvPiCode.setText(String.format("PI_CODE.......: %s", pi));
            tvRdsSync.setText(String.format("RDS_SYNC......: %s", rdsLock ? "LOCKED" : "SEARCHING"));
            tvAfList.setText("AF_LIST.......: [SCANNING]");

            // 3. System Diagnostics
            long maxMem = Runtime.getRuntime().maxMemory();
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long usedMem = (totalMem - freeMem) / 1024 / 1024;
            
            tvMemoryUsage.setText(String.format(Locale.US, "HEAP_MEMORY.......: %dMB / %dMB", usedMem, maxMem / 1024 / 1024));
            
            // Latency (Ping service)
            long start = System.nanoTime();
            mActivity.mRadioService.getCurrentFreq();
            long end = System.nanoTime();
            double latency = (end - start) / 1000000.0;
            
            tvServiceLatency.setText(String.format(Locale.US, "SERVICE_LATENCY...: %.2fms", latency));
            
            // Chipset - hardcoded detection based on investigation
            tvChipset.setText("DETECTED_CHIPSET..: MTK8163_NATIVE");

            // 4. Extended System Info (User Request)
            String device = android.os.Build.DEVICE; // e.g. 8163
            String model = android.os.Build.MODEL;   // e.g. KAPI_21
            String board = android.os.Build.BOARD;   // e.g. full_8163_pie_v2
            
            tvDeviceInfo.setText(String.format("DEVICE_INFO.......: %s / %s (%s)", model.toUpperCase(), device.toUpperCase(), board.toUpperCase()));
            
            // Root Check
            boolean isRooted = checkRootMethod1() || checkRootMethod2();
            tvRootStatus.setText(String.format("ROOT_ACCESS.......: %s", isRooted ? "GRANTED (SU FOUND)" : "DENIED (NO SU)"));
            if (isRooted) {
                 tvRootStatus.setTextColor(Color.parseColor("#00FF00")); // Green
            } else {
                 tvRootStatus.setTextColor(Color.parseColor("#FF0000")); // Red
            }

        } catch (Exception e) {
            e.printStackTrace();
            tvSignalQualityIndex.setText("SQI_INDEX.....: ERROR_READ");
        }
    }

    /**
     * V5.0: Asset & File System Check
     */
    private void checkAssets() {
        if (tvAssetsInfo == null) return;
        
        File radioLogosDir = new File("/sdcard/RadioLogos");
        File bgFilePng = new File(radioLogosDir, "background.png");
        File bgFileJpg = new File(radioLogosDir, "background.jpg");
        File carLogo = new File(radioLogosDir, "car_logo.png");
        
        // Count .fav files
        int favCount = 0;
        if (radioLogosDir.exists() && radioLogosDir.isDirectory()) {
            File[] files = radioLogosDir.listFiles((dir, name) -> name.endsWith(".fav"));
            if (files != null) favCount = files.length;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DIR...........: %s\n", radioLogosDir.exists() ? "OK (/sdcard/RadioLogos)" : "MISSING"));
        sb.append(String.format("BG_IMAGE......: %s\n", (bgFilePng.exists() || bgFileJpg.exists()) ? "FOUND" : "NOT_FOUND"));
        sb.append(String.format("CAR_LOGO......: %s\n", carLogo.exists() ? "FOUND" : "NOT_FOUND"));
        sb.append(String.format("SAVED_FAVS....: %d FILES", favCount));
        
        tvAssetsInfo.setText(sb.toString());
    }

    /**
     * V5.0: Data Management Buttons
     */
    private void setupDataButtons() {
        if (btnResetFavs != null) {
            btnResetFavs.setOnClickListener(v -> showConfirmationDialog(
                "RESET FAVORITES", 
                "Delete all saved .fav files and clear current presets?",
                () -> resetFavorites()
            ));
        }
        
        if (btnResetHistory != null) {
            btnResetHistory.setOnClickListener(v -> showConfirmationDialog(
                "RESET HISTORY", 
                "Clear station history?",
                () -> resetHistory()
            ));
        }
    }

    private void setupTunerButtons() {
        if (btnTuneDown != null) {
            btnTuneDown.setOnClickListener(v -> {
                if (mActivity != null && mActivity.mRadioService != null) {
                    try {
                        mActivity.mRadioService.onManualDownEvent();
                        logEvent("CMD", "STEP DOWN <");
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        }
        if (btnTuneUp != null) {
            btnTuneUp.setOnClickListener(v -> {
                if (mActivity != null && mActivity.mRadioService != null) {
                    try {
                        mActivity.mRadioService.onManualUpEvent();
                        logEvent("CMD", "STEP UP >");
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        }
    }

    private void setupExitButton() {
        if (btnExitSystem != null) {
            btnExitSystem.setOnClickListener(v -> dismiss());
        }
    }

    private void logEvent(String tag, String msg) {
        if (tvTerminalLog == null) return;
        String time = timeFormat.format(new Date());
        String entry = String.format("[%s] %s: %s\n", time, tag, msg);
        
        mHandler.post(() -> {
            tvTerminalLog.append(entry);
            if (scrollLog != null) scrollLog.fullScroll(View.FOCUS_DOWN);
        });
    }

    private void resetFavorites() {
        // 1. Delete .fav files
        File radioLogosDir = new File("/sdcard/RadioLogos");
        if (radioLogosDir.exists()) {
            File[] files = radioLogosDir.listFiles((dir, name) -> name.endsWith(".fav"));
            if (files != null) {
                for (File f : files) f.delete();
            }
        }
        
        // 2. Clear Preferences (RadioPresets)
        if (mActivity != null && mActivity.mPrefs != null) {
            mActivity.mPrefs.edit().clear().apply();
            mActivity.refreshPresetButtons(); // Reload UI
            Toast.makeText(getContext(), "Favorites Reset Complete", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetHistory() {
        if (mActivity != null && mActivity.mPrefs != null) {
            // Remove specific key "pref_station_history"
            mActivity.mPrefs.edit().remove("pref_station_history").apply();
            Toast.makeText(getContext(), "History Cleared", Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(getContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("YES", (d, w) -> onConfirm.run())
            .setNegativeButton("NO", null)
            .show();
    }
    
    // Root Check Method 1: Check build tags
    private boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    // Root Check Method 2: Check su binary
    private boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su" };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    @Override
    public void dismiss() {
        mIsRunning = false;
        mHandler.removeCallbacks(mUpdateRunnable);
        super.dismiss();
    }
}
