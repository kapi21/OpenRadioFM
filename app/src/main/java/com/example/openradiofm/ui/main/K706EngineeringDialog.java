package com.example.openradiofm.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.EditText;
import android.view.Window;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.K706RadioManager;
import java.util.Locale;
import java.io.File;
import android.widget.Button;
import android.widget.ScrollView;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.widget.SwitchCompat;
import android.widget.SeekBar;

public class K706EngineeringDialog extends Dialog implements K706RadioManager.RawMcuListener {

    private final MainActivity mActivity;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mUpdateRunnable;
    private boolean mIsRunning = false;

    // UI Elements - RF
    private TextView tvK706Rssi, tvK706RawSignal, tvK706Stereo, tvK706BandInfo, tvK706Scan;
    
    // UI Elements - RDS
    private TextView tvK706Ps, tvK706Rt, tvK706Pi, tvK706Pty;

    // UI Elements - OEM Media
    private TextView tvK706OemFocusEvent, tvK706OemFlags;
    
    // UI Elements - Assets & System
    private TextView tvK706Assets, tvK706McuRaw, tvK706Device;
    private TextView tvK706Rssi_Real, tvK706Snr_Real, tvK706Usn_Real;

    // Log & Controls
    private TextView tvK706Log;
    private ScrollView scrollK706Log;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
    
    private int mLastFreq = -1;
    private SwitchCompat swDevDayModeEnabled;
    private SwitchCompat swDevReliefHdEnabled;
    private SwitchCompat swDevFileLoggingEnabled;
    private SeekBar sbDevFileLogProfile, sbK706Sensitivity;
    private TextView tvDevFileLogProfileValue, tvK706SensitivityValue;

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
            getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }

        bindViews();
        setupControls();
        
        mIsRunning = true;
        logEvent("SYS", "K706_ENGINEERING_MATRIX_V24.3");
        
        K706RadioManager mgr = getK706Manager();
        if (mgr != null) {
            mgr.addRawMcuListener(this);
            logEvent("SYS", "MCU_LISTENER_ATTACHED");
        } else {
            logEvent("SYS", "MCU_CONNECTION: ERROR_NONE");
        }
        
        startUpdateLoop();
    }

    private void bindViews() {
        // RF
        tvK706Rssi = findViewById(R.id.tvK706Rssi);
        tvK706RawSignal = findViewById(R.id.tvK706RawSignal);
        tvK706Stereo = findViewById(R.id.tvK706Stereo);
        tvK706BandInfo = findViewById(R.id.tvK706BandInfo);
        tvK706Scan = findViewById(R.id.tvK706Scan);

        // RDS
        tvK706Ps = findViewById(R.id.tvK706Ps);
        tvK706Rt = findViewById(R.id.tvK706Rt);
        tvK706Pi = findViewById(R.id.tvK706Pi);
        tvK706Pty = findViewById(R.id.tvK706Pty);

        // OEM Media
        tvK706OemFocusEvent = findViewById(R.id.tvK706OemFocusEvent);
        tvK706OemFlags = findViewById(R.id.tvK706OemFlags);

        // System & Assets
        tvK706Assets = findViewById(R.id.tvK706Assets);
        tvK706McuRaw = findViewById(R.id.tvK706McuRaw);
        tvK706Device = findViewById(R.id.tvK706Device);
        
        // Log
        tvK706Log = findViewById(R.id.tvK706Log);
        scrollK706Log = findViewById(R.id.scrollK706Log);

        tvK706Rssi_Real = findViewById(R.id.tvK706Rssi);
        tvK706Snr_Real = findViewById(R.id.tvK706Snr);
        tvK706Usn_Real = findViewById(R.id.tvK706Usn);

        sbDevFileLogProfile = findViewById(R.id.sbDevFileLogProfile);
        tvDevFileLogProfileValue = findViewById(R.id.tvDevFileLogProfileValue);

        sbK706Sensitivity = findViewById(R.id.sbK706Sensitivity);
        tvK706SensitivityValue = findViewById(R.id.tvK706SensitivityValue);
    }

    private void setupControls() {
        if (findViewById(R.id.btnCloseK706) != null)
            findViewById(R.id.btnCloseK706).setOnClickListener(v -> dismiss());
        if (findViewById(R.id.btnK706Exit) != null)
            findViewById(R.id.btnK706Exit).setOnClickListener(v -> dismiss());
        
        // Tuner Buttons
        setupTunerButton(R.id.btnK706SeekDown, () -> {
            try {
                if (mActivity.mEngine != null) mActivity.mEngine.seekDown();
            } catch (Exception ignored) {}
            logEvent("MCU", "CMD_SEEK_DOWN");
        });
        setupTunerButton(R.id.btnK706SeekUp, () -> {
            try {
                if (mActivity.mEngine != null) mActivity.mEngine.seekUp();
            } catch (Exception ignored) {}
            logEvent("MCU", "CMD_SEEK_UP");
        });
        setupTunerButton(R.id.btnK706FineDown, () -> {
            try {
                if (mActivity.mEngine != null) mActivity.mEngine.stepDown();
            } catch (Exception ignored) {}
            logEvent("MCU", "CMD_STEP_DOWN");
        });
        setupTunerButton(R.id.btnK706FineUp, () -> {
            try {
                if (mActivity.mEngine != null) mActivity.mEngine.stepUp();
            } catch (Exception ignored) {}
            logEvent("MCU", "CMD_STEP_UP");
        });

        DevAutoscanToggleHelper.bind((SwitchCompat) findViewById(R.id.swDevAutoScanEnabled), mActivity);
        DevAutoscanToggleHelper.bindThresholdSeekBar(
                (SeekBar) findViewById(R.id.sbDevAutoScanThreshold),
                findViewById(R.id.tvDevAutoScanThresholdValue),
                mActivity);

        // Kill-switch para Modo D├¡a (UI)
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
                try { prefs.edit().putBoolean(com.example.openradiofm.utils.RadioActivityFileLogger.PREF_DEV_FILE_LOG_ENABLED, checked).apply(); } catch (Exception ignored) {}
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

        // Tuner Sensitivity Slider (V24.8)
        if (sbK706Sensitivity != null) {
            android.content.SharedPreferences prefs =
                    mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
            int currentSens = prefs.getInt("pref_k706_tuner_sensitivity", 0);
            sbK706Sensitivity.setProgress(currentSens);
            if (tvK706SensitivityValue != null) tvK706SensitivityValue.setText(String.valueOf(currentSens));
            sbK706Sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (tvK706SensitivityValue != null) tvK706SensitivityValue.setText(String.valueOf(progress));
                    K706RadioManager mgr = getK706Manager();
                    if (mgr != null) {
                        mgr.setTunerSensitivity(progress);
                    }
                    prefs.edit().putInt("pref_k706_tuner_sensitivity", progress).apply();
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // V24.5: HARDWARE_AUTOMATION_MODULE (K706 EXCLUSIVE)
        SwitchCompat swAutoNight = findViewById(R.id.swAutoNightHw);
        if (swAutoNight != null) {
            android.content.SharedPreferences prefs =
                    mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
            swAutoNight.setChecked(prefs.getBoolean("pref_hw_auto_night_unlocked", true)); // Default true en K706
            swAutoNight.setOnCheckedChangeListener((btn, checked) -> {
                prefs.edit().putBoolean("pref_hw_auto_night_unlocked", checked).apply();
                logEvent("HW_AUTO", "LIGHTS_AUTOMATION=" + checked);
            });
        }

        SwitchCompat swReverse = findViewById(R.id.swReverseMuteHw);
        if (swReverse != null) {
            android.content.SharedPreferences prefs =
                    mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
            swReverse.setChecked(prefs.getBoolean("pref_hw_reverse_mute_unlocked", true));
            swReverse.setOnCheckedChangeListener((btn, checked) -> {
                prefs.edit().putBoolean("pref_hw_reverse_mute_unlocked", checked).apply();
                logEvent("HW_AUTO", "REVERSE_SYNC=" + checked);
            });
        }

        SwitchCompat swHandbrake = findViewById(R.id.swHandbrakeHw);
        if (swHandbrake != null) {
            android.content.SharedPreferences prefs =
                    mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
            swHandbrake.setChecked(prefs.getBoolean("pref_hw_handbrake_unlocked", true));
            swHandbrake.setOnCheckedChangeListener((btn, checked) -> {
                prefs.edit().putBoolean("pref_hw_handbrake_unlocked", checked).apply();
                logEvent("HW_AUTO", "SAFETY_LOCK=" + checked);
            });
        }
    }

    private void setupQuickCmd(int resId, byte[] cmd, String tag) {
        View btn = findViewById(resId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                K706RadioManager mgr = getK706Manager();
                if (mgr != null) {
                    mgr.sendRawMcuCommand(cmd);
                    logEvent("POWER_CMD", tag + " SENT");
                }
            });
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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
            K706RadioManager mgr = getK706Manager();
            
            // 1. RF DATA
            int freq = (mActivity.mEngine != null) ? mActivity.mEngine.getCurrentFreq() : 0;
            if (freq != mLastFreq) {
                logEvent("RF", String.format(Locale.US, "FREQ_TUNED: %.2f MHz", freq / 1000.0f));
                mLastFreq = freq;
            }

            boolean stereo = (mActivity.mEngine != null) && mActivity.mEngine.isStereo();
            int band = (mActivity.mEngine != null) ? mActivity.mEngine.getCurrentBand() : 0;
            boolean scanning = (mgr != null) && mgr.isScanning();
            
            byte[] signalData = (mgr != null) ? mgr.getLastSignalData() : null;
            int rssiRaw = (signalData != null && signalData.length >= 6) ? (signalData[5] & 0xFF) : -1;
            int snrRaw = (signalData != null && signalData.length >= 8) ? (signalData[7] & 0xFF) : -1;

            tvK706RawSignal.setText(String.format(Locale.US, "0x%02X / 0x%02X", rssiRaw != -1 ? rssiRaw : 0, snrRaw != -1 ? snrRaw : 0));
            tvK706Stereo.setText(stereo ? "LOCKED (19KHZ)" : "MONO");
            tvK706BandInfo.setText("BAND_" + band);
            tvK706Scan.setText(scanning ? "RUNNING..." : "IDLE");

            // RSSI Bar
            int sqi = (rssiRaw != -1) ? Math.min(100, (rssiRaw * 100) / 120) : (mActivity.mHasRdsLock ? 80 : 20);
            StringBuilder bar = new StringBuilder("[");
            int bars = sqi / 10;
            for(int i=0; i<10; i++) bar.append(i < bars ? "Ôûê" : "Ôûæ");
            bar.append("]");
            String dbm = (rssiRaw != -1) ? String.valueOf(-120 + rssiRaw) : "N/A";
            tvK706Rssi.setText(String.format(Locale.US, "[%sdBm] %s", dbm, bar.toString()));

            // 2. RDS DATA
            TextView mainRdsName = mActivity.findViewById(R.id.tvRdsName);
            TextView mainRdsInfo = mActivity.findViewById(R.id.tvRdsInfo);
            
            tvK706Ps.setText((mainRdsName != null) ? mainRdsName.getText() : "NONE");
            String rt = (mainRdsInfo != null) ? mainRdsInfo.getText().toString() : "WAITING...";
            tvK706Rt.setText(rt.length() > 30 ? rt.substring(0, 27) + "..." : rt);
            tvK706Pi.setText(mActivity.mCurrentPi != null ? mActivity.mCurrentPi : "WAITING...");
            tvK706Pty.setText(mActivity.mCurrentPty != null ? mActivity.mCurrentPty : "00");

            // 2.5 OEM Media / AudioFocus status (v├¡a SharedPreferences escrito por RadioMediaService)
            try {
                android.content.SharedPreferences prefs =
                        mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE);
                String lastFocus = prefs.getString("oem_last_focus_event", "N/A");
                boolean userPaused = prefs.getBoolean("oem_user_paused", false);
                boolean wasPlayingBeforeLoss = prefs.getBoolean("oem_was_playing_before_focus_loss", false);
                boolean isPlaying = prefs.getBoolean("oem_is_playing", false);

                if (tvK706OemFocusEvent != null) tvK706OemFocusEvent.setText(lastFocus);
                if (tvK706OemFlags != null) {
                    tvK706OemFlags.setText("playing=" + isPlaying + " userPaused=" + userPaused + " resumeArmed=" + wasPlayingBeforeLoss);
                }
            } catch (Exception ignored) {}

            // 3. SYSTEM
            String mcuRaw = (mgr != null && signalData != null) ? mgr.bytesToHex(signalData) : "NONE";
            tvK706McuRaw.setText(mcuRaw.length() > 20 ? mcuRaw.substring(0, 17) + "..." : mcuRaw);
            tvK706Device.setText(android.os.Build.MODEL.toUpperCase());

        } catch (Exception e) {
            logEvent("ERR", e.getMessage());
        }
    }

    private void checkAssets() {
        File appDir = new File(mActivity.getExternalFilesDir(null) != null ? mActivity.getExternalFilesDir(null) : mActivity.getFilesDir(), "RadioLogos");
        File legacyDir = new File("/sdcard/RadioLogos");
        File dir = (legacyDir.exists() && legacyDir.isDirectory()) ? legacyDir : appDir;
        StringBuilder sb = new StringBuilder();
        sb.append("PATH....: ").append(dir.getAbsolutePath()).append("\n");
        sb.append("FOLDER..: ").append(dir.exists() ? "READY" : "MISSING").append("\n");
        
        File bg = new File(dir, "background.png");
        if (!bg.exists()) bg = new File(dir, "background.jpg");
        sb.append("BG_IMG..: ").append(bg.exists() ? "OK (" + (bg.length()/1024) + "KB)" : "FAIL").append("\n");
        
        File car = new File(dir, "car_logo.png");
        sb.append("CAR_ICO.: ").append(car.exists() ? "OK" : "FAIL").append("\n");
        
        File[] favs = dir.listFiles((d, name) -> name.endsWith(".fav"));
        sb.append("FAVS....: ").append(favs != null ? favs.length : 0).append(" OBJECTS");
        
        tvK706Assets.setText(sb.toString());
    }

    private K706RadioManager getK706Manager() {
        if (mActivity.mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
            return ((com.example.openradiofm.data.source.K706Engine) mActivity.mEngine).getManager();
        }
        return null;
    }

    private void logEvent(String tag, String msg) {
        if (tvK706Log == null) return;
        String entry = String.format("[%s] %s: %s\n", timeFormat.format(new Date()), tag, msg);
        mHandler.post(() -> {
            tvK706Log.append(entry);
            if (scrollK706Log != null) scrollK706Log.fullScroll(View.FOCUS_DOWN);
        });
    }

    public void addRdsLog(String msg) {
        logEvent("MCU_RDS", msg);
    }

    public void updateSignalQuality(int rssi, int snr) {
        logEvent("SIG", String.format(Locale.US, "UPDATE_RSSI: %d, SNR: %d", rssi, snr));
    }

    @Override
    public void onRawData(byte[] data) {
        if (!mIsRunning || data == null || data.length == 0) return;
        
        int type = data[0] & 0xFF;
        String hex = bytesToHex(data);
        
        mHandler.post(() -> {
            // Un throttle simple para no inundar si hay muchos paquetes
            if (type != 0x29) { // Ignorar latidos
                logEvent("MCU_HEX", hex);
            }
            
            if (type == 0x20 || type == 0x21) {
                logEvent("SW_KEY", "DETECTED: " + hex);
            } else if (type == 0x41) {
                // Telemetr├¡a de se├▒al (Investigaci├│n sugerida)
                parseSignalData(data);
            } else if (type == 0xB0) {
                // Status info
                updateStatusFlags(data);
            }
        });
    }

    private void parseSignalData(byte[] data) {
        if (data.length < 3) return;
        // Basado en el mapping de QF_Framework
        int rssi = data[1] & 0xFF; // Nivel de campo
        int snr = data[2] & 0xFF;  // Relaci├│n se├▒al/ruido
        int usn = (data.length > 3) ? (data[3] & 0xFF) : 0; // Multipath
        
        if (tvK706Rssi_Real != null) tvK706Rssi_Real.setText(String.format(Locale.US, "%d dB╬╝V", rssi));
        if (tvK706Snr_Real != null) tvK706Snr_Real.setText(String.format(Locale.US, "%d dB", snr));
        if (tvK706Usn_Real != null) tvK706Usn_Real.setText(String.format(Locale.US, "%d (RAW)", usn));
    }

    private void updateStatusFlags(byte[] data) {
        if (data.length < 2) return;
        int flags = data[1] & 0xFF;
        boolean stereo = (flags & 0x10) != 0;
        if (tvK706Stereo != null) tvK706Stereo.setText(stereo ? "STOCKED (19KHZ)" : "MONO_ONLY");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    @Override
    public void dismiss() {
        mIsRunning = false;
        K706RadioManager mgr = getK706Manager();
        if (mgr != null) {
            mgr.removeRawMcuListener(this);
        }
        mHandler.removeCallbacks(mUpdateRunnable);
        super.dismiss();
    }
}
