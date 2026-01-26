package com.example.openradiofm.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.hcn.autoradio.IRadioServiceAPI;
import com.hcn.autoradio.IRadioCallBack;
import com.example.openradiofm.data.source.HiddenRadioPlayer;
import com.example.openradiofm.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "OpenRadioFm";
    private IRadioServiceAPI mRadioService;
    private HiddenRadioPlayer mHiddenPlayer;
    private int mLastFreq = 0;
    private String mLastLogoUrl = ""; // V8.4: Caching Logic
    
    // Repositorio
    private com.example.openradiofm.data.repository.RadioRepository mRepository;

    // UI Views V4
    private TextView tvFrequency;
    private TextView tvRdsName; // V5 New
    private TextView tvRdsInfo;
    private ImageButton btnLocDx; 
    private ImageButton btnBand;  
    
    // Smart Cards
    private ImageView[] ivPresets = new ImageView[6];
    private TextView[] tvPresets = new TextView[6];
    private View[] cardPresets = new View[6]; // Changed from Button[] to View[]

    // Settings Indicators (TextViews now)
    private TextView tvIndTa, tvIndAf, tvIndTp;

    private android.content.SharedPreferences mPrefs;
    private int mCurrentBand = 0; // Cache for band-specific presets
    
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRadioService = IRadioServiceAPI.Stub.asInterface(service);
            try {
                mRadioService.registerRadioCallback(mCallback);
                startStatusPolling();
                showToast("Conexión Establecida");
                initHiddenPlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stopStatusPolling();
            mRadioService = null;
        }
    };

    private void initHiddenPlayer() {
        mHiddenPlayer = new HiddenRadioPlayer(new HiddenRadioPlayer.Listener() {
            @Override
            public void onRdsText(String text) {
                runOnUiThread(() -> {
                    if (tvRdsInfo != null) {
                        String current = tvRdsInfo.getText().toString();
                        if (!current.equals(text)) {
                            tvRdsInfo.setText(text);
                        }
                    }
                });
            }
            @Override
            public void onRdsName(String name) {
                // UI update via polling
            }
            @Override
            public void onRawEvent(int code, Object info, String str) {}
        });
        if (!mHiddenPlayer.init()) Log.e(TAG, "Error RDS Hardware Init");
    }

    private java.util.Timer mTimer;
    private void startStatusPolling() {
        stopStatusPolling();
        mTimer = new java.util.Timer();
        mTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> refreshRadioStatus());
            }
        }, 500, 500);
    }
    private void stopStatusPolling() {
        if (mTimer != null) { mTimer.cancel(); mTimer = null; }
    }

    private final IRadioCallBack mCallback = new IRadioCallBack.Stub() {
        @Override public void onEvent(int code, String data) {}
    };

    private int mTestClickCount = 0;
    private long mTestStartTime = 0;

    private void sendMcuKey(int key) {
        try {
            Class<?> mcuClass = Class.forName("android.carsource.McuManager");
            java.lang.reflect.Method getInstance = mcuClass.getMethod("getsInstance");
            Object instance = getInstance.invoke(null);
            java.lang.reflect.Method injectKey = mcuClass.getMethod("injectKeyEventTimeout", int.class, int.class);
            injectKey.invoke(instance, key, 0x32);
            Log.d(TAG, "MCU Key injected: " + key);
        } catch (Exception e) {
            Log.e(TAG, "Error injecting MCU key: " + e.getMessage());
            showToast("Hardware EQ not supported on this device");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_OpenRadioFm);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
        }

        mRepository = new com.example.openradiofm.data.repository.RadioRepository();
        mPrefs = getSharedPreferences("RadioPresets", MODE_PRIVATE);

        // Bind Views
        tvFrequency = findViewById(R.id.tvFrequency);
        tvRdsName = findViewById(R.id.tvRdsName); // V5
        tvRdsInfo = findViewById(R.id.tvRdsInfo);
        
        btnLocDx = findViewById(R.id.btnLocDx);
        btnBand = findViewById(R.id.btnBand);
        
        // Indicators Binding
        tvIndTa = findViewById(R.id.tvIndTa);
        tvIndAf = findViewById(R.id.tvIndAf);
        tvIndTp = findViewById(R.id.tvIndTp);

        // EQ Logic (V8: MCU Injection)
        ImageButton btnEq = findViewById(R.id.btnSettings);
        btnEq.setOnClickListener(v -> sendMcuKey(0x134)); // Keycode 308 for DSP

        // Mute Logic (System Audio)
        ImageButton btnMute = findViewById(R.id.btnMute);
        android.media.AudioManager am = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
        btnMute.setOnClickListener(v -> {
            boolean isSelected = !v.isSelected();
            v.setSelected(isSelected);
            if (isSelected) {
                am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_MUTE, 0);
                btnMute.setImageResource(R.drawable.radio_mute_p); // Active
            } else {
                am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_UNMUTE, 0);
                btnMute.setImageResource(R.drawable.radio_mute_n); // Inactive
            }
        });

        // Test/Hidden Menu (V8: 5 Clicks logic)
        findViewById(R.id.btnTest).setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (mTestClickCount == 0 || (now - mTestStartTime) > 15000) {
                mTestClickCount = 1;
                mTestStartTime = now;
            } else {
                mTestClickCount++;
            }

            if (mTestClickCount >= 5) {
                mTestClickCount = 0; // Reset
                Intent intent = new Intent();
                intent.setComponent(new android.content.ComponentName("com.hcn.changedapp", "com.hcn.changedapp.TestActivity"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    showToast("Error al abrir TestActivity");
                }
            } else {
                showToast("Faltan " + (5 - mTestClickCount) + " clics");
            }
        });

        // Auto Scan
        findViewById(R.id.btnAutoScan).setOnClickListener(v -> execRemote(IRadioServiceAPI::onScanEvent));
        
        

        
        // Status Indicators logic (Toggle Click)
        View.OnClickListener toggleListener = v -> {
            boolean isChecked = !v.isSelected();
            v.setSelected(isChecked);
            updateStatusIndicator((TextView)v, isChecked);
            
            // Save state
            if (v.getId() == R.id.tvIndTa) mPrefs.edit().putBoolean("TA_ENABLED", isChecked).apply();
            if (v.getId() == R.id.tvIndAf) mPrefs.edit().putBoolean("AF_ENABLED", isChecked).apply();
            if (v.getId() == R.id.tvIndTp) mPrefs.edit().putBoolean("TP_ENABLED", isChecked).apply();
        };

        tvIndTa.setOnClickListener(toggleListener);
        tvIndAf.setOnClickListener(toggleListener);
        tvIndTp.setOnClickListener(toggleListener);
        
        // Restore Saved States
        boolean ta = mPrefs.getBoolean("TA_ENABLED", false);
        boolean af = mPrefs.getBoolean("AF_ENABLED", false);
        boolean tp = mPrefs.getBoolean("TP_ENABLED", false);
        
        tvIndTa.setSelected(ta); updateStatusIndicator(tvIndTa, ta);
        tvIndAf.setSelected(af); updateStatusIndicator(tvIndAf, af);
        tvIndTp.setSelected(tp); updateStatusIndicator(tvIndTp, tp);

        // Switches Logic (LOC/DX)
        btnLocDx.setOnClickListener(v -> execRemote(IRadioServiceAPI::onLocDxEvent));
        // Icon update handled in refreshRadioStatus
        
        // Seeking Logic
        setupSeekButtons();

        // Presets Binding
        bindPresetViews();
        // Initial Refresh (will be updated again when band is fetched)
        refreshPresetButtons();
        
        setupRdsText();
        applyFonts();
        conectarRadio();
    }

    private void applyFonts() {
        try {
            android.graphics.Typeface orbitron = android.graphics.Typeface.createFromAsset(getAssets(), "fonts/orbitron_bold.ttf");
            tvFrequency.setTypeface(orbitron);
            // V8.3: Apply to all main text views
            tvRdsName.setTypeface(orbitron); 
            tvRdsInfo.setTypeface(orbitron); 
            
            // Apply to preset numbers too
            for (TextView tv : tvPresets) {
                if (tv != null) tv.setTypeface(orbitron);
            }
        } catch (Exception e) {
            Log.w(TAG, "Orbitron font not found in assets/fonts. Skipping.");
        }
    }
    
    private void setupRdsText() {
         // V8.3: Enable Marquee on RDS Info
         tvRdsInfo.setText(""); // Start Empty
         tvRdsInfo.setSelected(true); // Required for Marquee
         tvRdsInfo.setSingleLine(true);
         tvRdsInfo.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
    }

    private void setupSeekButtons() {
        ImageButton btnSeekUp = findViewById(R.id.btnSeekUp);     // VISUALLY LEFT (<) -> DOWN (Decrement)
        ImageButton btnSeekDown = findViewById(R.id.btnSeekDown); // VISUALLY RIGHT (>) -> UP (Increment)
        
        // V8.4: Custom Seek Step (0.1 MHz)
        // We use gotoFreq(current +/- 100) instead of onManualUp/Down because default step is 0.05
        
        btnSeekUp.setOnClickListener(v -> {
             if (mRadioService == null) return;
             try {
                 int current = mRadioService.getCurrentFreq();
                 int newFreq = current - 100; // -0.1 MHz
                 if (newFreq < 87500) newFreq = 108000; // Wrap Around
                 mRadioService.gotoFreq(newFreq);
             } catch (RemoteException e) { e.printStackTrace(); }
             // Old: execRemote(IRadioServiceAPI::onManualDownEvent);
        });
        
        btnSeekUp.setOnLongClickListener(v -> {
            execRemote(IRadioServiceAPI::onSeekUpEvent); // V9: Swapped
            return true;
        });

        // Right Button (>) -> Increment
        btnSeekDown.setOnClickListener(v -> {
             if (mRadioService == null) return;
             try {
                 int current = mRadioService.getCurrentFreq();
                 int newFreq = current + 100; // +0.1 MHz
                 if (newFreq > 108000) newFreq = 87500; // Wrap Around
                 mRadioService.gotoFreq(newFreq);
             } catch (RemoteException e) { e.printStackTrace(); }
             // Old: execRemote(IRadioServiceAPI::onManualUpEvent);
        });
        
        btnSeekDown.setOnLongClickListener(v -> {
            execRemote(IRadioServiceAPI::onSeekDownEvent); // V9: Swapped
            return true;
        });

        // Loop Band Logic
        btnBand.setOnClickListener(v -> {
             execRemote(IRadioServiceAPI::onBandEvent);
        });
    }

    private void bindPresetViews() {
        int[] cardIds = {R.id.cardP1, R.id.cardP2, R.id.cardP3, R.id.cardP4, R.id.cardP5, R.id.cardP6};
        int[] tvIds = {R.id.tvP1, R.id.tvP2, R.id.tvP3, R.id.tvP4, R.id.tvP5, R.id.tvP6};
        int[] ivIds = {R.id.ivP1, R.id.ivP2, R.id.ivP3, R.id.ivP4, R.id.ivP5, R.id.ivP6};

        for(int i=0; i<6; i++) {
            cardPresets[i] = findViewById(cardIds[i]);
            tvPresets[i] = findViewById(tvIds[i]);
            ivPresets[i] = findViewById(ivIds[i]);
        }
    }

    private void refreshPresetButtons() {
        for(int i=0; i<6; i++) {
            // Key is now P1_B0, P1_B1, etc.
            String key = "P" + (i+1) + "_B" + mCurrentBand;
            setupPresetCard(i, key);
        }
    }

    private void setupPresetCard(int index, String key) {
        int savedFreq = mPrefs.getInt(key, 0);
        updateCardVisuals(index, savedFreq);

        cardPresets[index].setOnClickListener(v -> {
            int freq = mPrefs.getInt(key, 0);
            if (freq > 0) {
                execRemote(s -> s.gotoFreq(freq));
            } else {
                showToast("Vacío - Mantén para guardar");
            }
        });

        cardPresets[index].setOnLongClickListener(v -> {
            if (mRadioService != null) {
                try {
                    int current = mRadioService.getCurrentFreq();
                    mPrefs.edit().putInt(key, current).apply();
                    updateCardVisuals(index, current);
                    // showToast("Guardado en B" + (mCurrentBand + 1)); // V8.4: Disabled toast
                } catch (RemoteException e) {}
            }
            return true;
        });
    }

    private void updateCardVisuals(int index, int freq) {
        if (freq == 0) {
            tvPresets[index].setText("Empty");
            ivPresets[index].setImageDrawable(null);
            tvPresets[index].setVisibility(View.VISIBLE);
            return;
        }

        // Default: Show Freq
        tvPresets[index].setText(String.format("%.1f", freq / 1000.0));
        ivPresets[index].setImageDrawable(null);
        tvPresets[index].setVisibility(View.VISIBLE);

        // Async Fetch Logo
        mRepository.getStationInfo(freq, logoUrl -> {
            runOnUiThread(() -> {
                if (logoUrl != null) {
                    Glide.with(MainActivity.this)
                         .load(logoUrl)
                         .transition(DrawableTransitionOptions.withCrossFade())
                         .into(ivPresets[index]);
                    // User Request: Logo Left, Name/Freq Right. Both Visible.
                    // Keep tvPresets VISIBLE but maybe update text to Name?
                }
            });
        });
        
        com.example.openradiofm.data.model.RadioStation s = mRepository.getStationInfo(freq, null);
        if (s.getName() != null && !s.getName().isEmpty()) {
             tvPresets[index].setText(s.getName());
        }
    }

    private interface RemoteAction { void run(IRadioServiceAPI s) throws RemoteException; }
    private void execRemote(RemoteAction action) {
        if (mRadioService == null) return;
        try { action.run(mRadioService); } catch (RemoteException e) { e.printStackTrace(); }
    }

    private void conectarRadio() {
        Intent intent = new Intent("com.hcn.autoradio.FM_PLUG_SERVICE");
        intent.setPackage("com.hcn.autoradio");
        try { bindService(intent, mConnection, Context.BIND_AUTO_CREATE); } catch (Exception e) {}
    }

    private void refreshRadioStatus() {
        if (mRadioService == null) return;
        execRemote(s -> {
            int freq = s.getCurrentFreq();
            int band = s.getCurrentBand();
            boolean isStereo = s.IsStereo();
            boolean isLocal = s.IsDxLocal();
            
            // Check if band changed -> Refresh presets
            if (band != mCurrentBand) {
                mCurrentBand = band;
                runOnUiThread(() -> refreshPresetButtons());
            }

            if (freq != mLastFreq) {
                mLastFreq = freq;
            }
            
            // SMART MAIN DISPLAY LOGIC
            mRepository.getStationInfo(freq, logoUrl -> {}); // Prefetch

            com.example.openradiofm.data.model.RadioStation station = mRepository.getStationInfo(freq, null);
            String rdsName = station.getName();
            
            runOnUiThread(() -> {
                // FIXED LOGIC: Always show Frequency in Big Box
                tvFrequency.setText(String.format("%.1f", freq / 1000.0));
                // tvFrequency.setTextSize(110); // V8.4: Moved to XML
                
                if (rdsName != null && !rdsName.isEmpty()) {
                    tvRdsName.setText(rdsName);
                    // tvRdsInfo contains scrolling text (RT)
                } else {
                    tvRdsName.setText(""); 
                }
                
                // Ensure Sintonizando isn't blocking RDS Name if we have one
                if (rdsName != null && !rdsName.isEmpty()) {
                     if (!tvRdsName.getText().toString().equals(rdsName)) {
                         tvRdsName.setText(rdsName);
                     }
                }
                

                
                // CONDITIONAL LOGO VISIBILITY: Only show if RDS Name is present
                ImageView ivMainLogo = findViewById(R.id.ivMainLogo);
                if (rdsName != null && !rdsName.isEmpty()) {
                    ivMainLogo.setVisibility(View.VISIBLE);
                    mRepository.getStationInfo(freq, url -> {
                        runOnUiThread(() -> {
                            // V8.4: Logo Caching to prevent flickering
                            if (url != null) {
                                if (url.equals(mLastLogoUrl)) return; // Skip if same
                                mLastLogoUrl = url;
                                Glide.with(MainActivity.this)
                                     .load(url)
                                     .transition(DrawableTransitionOptions.withCrossFade())
                                     .into(ivMainLogo);
                            } else {
                                mLastLogoUrl = "";
                                // V9: Default Logo
                                ivMainLogo.setImageResource(R.mipmap.ic_launcher);
                            }
                        });
                    });
                } else {
                    // V9: Show Default Logo instead of Invisible? Or keep invisible?
                    // User said: "default if no logo... or until there is". 
                    // So if NO RDS Name found yet (Sintonizando...), show default?
                    ivMainLogo.setVisibility(View.VISIBLE);
                    ivMainLogo.setImageResource(R.mipmap.ic_launcher);
                }
                
                updateBandImage(band);
                btnLocDx.setSelected(isLocal);
                btnLocDx.setImageResource(isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
            });
        });
    }

    private void updateBandImage(int band) {
        int resId = R.drawable.radio_fm1; // Default
        if (band == 0) resId = R.drawable.radio_fm1;
        else if (band == 1) resId = R.drawable.radio_fm2;
        else if (band == 2) resId = R.drawable.radio_fm3;
        else if (band == 3) resId = R.drawable.radio_fm1; 
        else if (band == 4) resId = R.drawable.radio_fm2;
        
        btnBand.setImageResource(resId);
    }

    private void updateStatusIndicator(TextView tv, boolean active) {
        if (tv == null) return;
        if (active) {
            tv.setTextColor(Color.parseColor("#FF8C00")); // Orange for Active
            tv.setAlpha(1.0f);
        } else {
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setAlpha(0.3f);
        }
    }

    private String getBandLabel(int bandCode) {
        if (bandCode == 0) return "FM 1";
        if (bandCode == 1) return "FM 2";
        if (bandCode == 2) return "FM 3";
        if (bandCode == 3) return "AM 1";
        if (bandCode == 4) return "AM 2";
        return "B" + bandCode;
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void launchExternalApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            showToast("App no instalada: " + packageName);
        }
    }
}
