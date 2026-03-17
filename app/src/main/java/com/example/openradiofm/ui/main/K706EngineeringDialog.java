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

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.K706RadioManager;
import java.util.Locale;
import java.io.File;
import android.widget.Button;
import android.widget.ScrollView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class K706EngineeringDialog extends Dialog {

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

    // Log & Controls
    private TextView tvK706Log;
    private ScrollView scrollK706Log;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
    
    private int mLastFreq = -1;

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
        logEvent("SYS", "K706_ENGINEERING_MATRIX_V10.0");
        logEvent("SYS", "MCU_CONNECTION: " + (getK706Manager() != null ? "ACTIVE" : "NONE"));
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
            for(int i=0; i<10; i++) bar.append(i < bars ? "█" : "░");
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

            // 2.5 OEM Media / AudioFocus status (vía SharedPreferences escrito por RadioMediaService)
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
        File dir = new File("/sdcard/RadioLogos");
        StringBuilder sb = new StringBuilder();
        sb.append("PATH....: /sdcard/RadioLogos\n");
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
    public void dismiss() {
        mIsRunning = false;
        mHandler.removeCallbacks(mUpdateRunnable);
        super.dismiss();
    }
}
