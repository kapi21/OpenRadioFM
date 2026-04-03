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

import java.util.Locale;
import java.io.File;
import android.widget.Button;
import android.widget.ScrollView;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.widget.SwitchCompat;

/**
 * Menú de ingeniería «Technical Matrix» para plataforma QS6 / NWD (mismo acceso que K706/MT8163:
 * pulsación larga en GPS).
 */
public class QS6EngineeringDialog extends Dialog {

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

    private int mLastFreq = -1;

    public QS6EngineeringDialog(MainActivity activity) {
        super(activity);
        this.mActivity = activity;
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

        DevAutoscanToggleHelper.bind((SwitchCompat) findViewById(R.id.swDevAutoScanEnabled), mActivity);
        DevAutoscanToggleHelper.bindThresholdSeekBar(
                (SeekBar) findViewById(R.id.sbDevAutoScanThreshold),
                findViewById(R.id.tvDevAutoScanThresholdValue),
                mActivity);
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

            int sqi = mActivity.mHasRdsLock ? 80 : 25;
            StringBuilder bar = new StringBuilder("[");
            int bars = sqi / 10;
            for (int i = 0; i < 10; i++) bar.append(i < bars ? "█" : "░");
            bar.append("]");
            if (tvQs6Rssi != null) {
                tvQs6Rssi.setText(String.format(Locale.US, "[est] %s RDS_LOCK=%s", bar, mActivity.mHasRdsLock));
            }

            TextView mainRdsName = mActivity.findViewById(R.id.tvRdsName);
            TextView mainRdsInfo = mActivity.findViewById(R.id.tvRdsInfo);

            if (tvQs6Ps != null) tvQs6Ps.setText(mainRdsName != null ? mainRdsName.getText() : "NONE");
            String rt = (mainRdsInfo != null) ? mainRdsInfo.getText().toString() : "WAITING...";
            if (tvQs6Rt != null) tvQs6Rt.setText(rt.length() > 36 ? rt.substring(0, 33) + "..." : rt);
            if (tvQs6Pi != null) tvQs6Pi.setText(mActivity.mCurrentPi != null ? mActivity.mCurrentPi : "—");
            if (tvQs6Pty != null) tvQs6Pty.setText(mActivity.mCurrentPty != null ? mActivity.mCurrentPty : "00");
            if (tvQs6RdsLock != null) tvQs6RdsLock.setText(mActivity.mHasRdsLock ? "YES" : "NO");

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
        super.dismiss();
    }
}
