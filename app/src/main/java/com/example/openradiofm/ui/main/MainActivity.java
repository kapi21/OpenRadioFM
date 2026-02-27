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
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.hcn.autoradio.IRadioServiceAPI;
import com.hcn.autoradio.IRadioCallBack;
import com.example.openradiofm.data.source.HiddenRadioPlayer;
import com.example.openradiofm.data.source.RadioEngine;
import com.example.openradiofm.data.source.RadioEngineCallback;
import com.example.openradiofm.data.source.K706Engine;
import com.example.openradiofm.data.source.K706RadioManager;
import com.example.openradiofm.data.source.QS6Engine;
import com.example.openradiofm.ui.theme.ThemeManager;
import com.example.openradiofm.utils.PtyManager;
import com.example.openradiofm.utils.MetadataUtils;
import com.example.openradiofm.R;

/**
 * Pantalla principal de la radio FM.
 *
 * Responsabilidades:
 * - Conectarse al servicio de radio del coche (IRadioServiceAPI).
 * - Mostrar frecuencia, nombre RDS y texto RDS.
 * - Gestionar presets, logos locales y botones de control.
 *
 * Notas de diseño:
 * - El sondeo del estado de la radio se hace en un hilo de fondo mediante
 * Timer,
 * y solo las actualizaciones de UI pasan por runOnUiThread() para no bloquear
 * el hilo principal.
 * - Los recursos de hardware (servicio, proceso root, listener RDS oculto) se
 * liberan explícitamente en onDestroy() para evitar fugas de memoria.
 */
public class MainActivity extends AppCompatActivity implements RadioEngineCallback {

    // V4.0: Language Context Wrapper (CORRECTED)
    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = "es"; // Default
        try {
            // Use "RadioPresets" to match onCreate
            android.content.SharedPreferences prefs = newBase.getSharedPreferences("RadioPresets",
                    Context.MODE_PRIVATE);
            lang = prefs.getString("app_language", "es");
        } catch (Exception e) {
        }
        super.attachBaseContext(MyContextWrapper.wrap(newBase, lang));
    }

    private static final String TAG = "OpenRadioFm";
    private static final int PRESETS_COUNT = 15; // Updated to match V4 max presets

    // Band Constants
    private static final int BAND_FM1 = 0;
    private static final int BAND_FM2 = 1;
    private static final int BAND_FM3 = 2;
    private static final int BAND_AM1 = 3;
    private static final int BAND_AM2 = 4;

    /**
     * Modos de funcionamiento de la app:
     * - FM_MT8163: dispositivo con root + servicio especial del coche disponible.
     * - FM_BASICO: sin root o sin servicio.
     * - FM_K706: sintonizador K706 (MTK).
     * - FM_QS6: sintonizador QS6.
     * - AM: modo AM.
     */
    public enum FmMode {
        FM_MT8163, 
        FM_BASICO,
        FM_K706,
        FM_QS6,
        AM
    }

    public FmMode mMode = FmMode.FM_BASICO;

    public IRadioServiceAPI mRadioService;
    public com.example.openradiofm.data.repository.RadioRepository mRepository;
    public android.content.SharedPreferences mPrefs;
    public HiddenRadioPlayer mHiddenPlayer;

    // V5.0: Capa de abstracción de hardware
    public RadioEngine mEngine;
    boolean mIsScanning = false;
    public java.util.List<ScannedStation> mCapturedList = new java.util.ArrayList<>();
    public StationAdapter mStationAdapter;
    public DialogManager mDialogManager;
    public LogoManager mLogoManager;

    // V11: RDS PI Database Identification
    private com.example.openradiofm.data.source.RdsDatabase mRdsDb;
    private String mCurrentPi = null;

    // V13: Gestor de Presets (Reducción de MainActivity)
    public PresetManager mPresetManager;
    public int mLastFreq = -1;
    public boolean mHasRdsLock = false;
    public String mCurrentPty = null;
    public String mLastLogoUrl = "";
    public java.util.Map<String, String> mLogoCachePerBand = new java.util.HashMap<>();

    // V5.0: UI Elements (Fixing Compilation Errors)
    private TextView tvPty;
    private ImageView ivSignalLevel;
    private ImageView ivPtyIcon; // V5.0: Categorical Icon
    private ImageView ivAfIcon, ivTaIcon, ivTpIcon; // RDS Status Icons

    public boolean mMuteState = false;
    public ThemeManager.Skin mCurrentSkin = ThemeManager.Skin.CLASSIC_GRAY;

    // V5.0: Signal Quality Logic
    public enum SignalQuality {
        EXCELLENT("Excellent", "#00FF00", 4),
        GOOD("Good", "#ADFF2F", 3),
        FAIR("Fair", "#FFFF00", 2),
        POOR("Poor", "#FF4500", 1),
        NO_SIGNAL("No Signal", "#FF0000", 0);

        public final String label;
        public final String color;
        public final int bars;

        SignalQuality(String label, String color, int bars) {
            this.label = label;
            this.color = color;
            this.bars = bars;
        }
    }

    private SignalQuality mCurrentQuality = SignalQuality.NO_SIGNAL;
    
    // V9.9: RDS Debugging Tracker
    public K706EngineeringDialog mEngineeringDialog = null;

    public int mCurrentBand = 0;
    public int getCurrentBand() { return mCurrentBand; }
    public boolean mIsV3 = false; // V5.4: Track Layout 3 active

    // --- Clases de Soporte para Escaneo Selectivo (V12.1: Reubicadas para estabilidad) ---
    public static class ScannedStation {
        public int frequency;
        public String name = "Buscando RDS...";
        public ScannedStation(int f) { this.frequency = f; }
    }

    public class StationViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView freq, name;
        android.widget.Button[] presets = new android.widget.Button[12];
        StationViewHolder(android.view.View root) {
            super(root);
            freq = root.findViewById(R.id.tvFreq);
            name = root.findViewById(R.id.tvName);
            for(int i=0; i<12; i++) {
                int resId = getResources().getIdentifier("btnP" + (i+1), "id", getPackageName());
                presets[i] = root.findViewById(resId);
            }
        }
    }

    public class StationAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<StationViewHolder> {
        @Override
        public StationViewHolder onCreateViewHolder(android.view.ViewGroup p, int t) {
            return new StationViewHolder(getLayoutInflater().inflate(R.layout.item_scanned_station, p, false));
        }
        @Override
        public void onBindViewHolder(StationViewHolder h, int p) {
            ScannedStation s = mCapturedList.get(p);
            h.freq.setText(String.format("%.2f MHz", s.frequency / 1000.0f));
            h.name.setText(s.name);
            for(int i=0; i<12; i++) {
                final int slot = i;
                h.presets[i].setOnClickListener(v -> {
                    if (mPresetManager != null) {
                        mPresetManager.savePreset(mCurrentBand, slot, s.frequency, s.name.equals("Buscando RDS...") ? "" : s.name);
                        showToast("Guardado en Slot " + (slot + 1));
                        refreshPresetButtons();
                    }
                });
            }
        }
        @Override public int getItemCount() { return mCapturedList.size(); }
    }

    // Métodos delegados al PresetManager para compatibilidad con código existente
    public void gotoPreset(int index) {
        if (mPresetManager != null) {
            int freq = mPresetManager.getFreq(index);
            if (freq > 0) {
                gotoFreq(freq);
            }
        }
    }

    public void savePreset(int index) {
        if (mEngine != null && mPresetManager != null) {
            int current = mEngine.getCurrentFreq();
            String currentRds = "";
            
            // V13.5: Capturar nombre RDS actual si existe para que el preset lo use
            if (tvRdsName != null) {
                currentRds = tvRdsName.getText().toString().trim();
                // Si el nombre es la frecuencia (ej: "96.9"), ignorarlo
                if (currentRds.matches("\\d+\\.\\d+")) {
                    currentRds = "";
                }
            }

            mPresetManager.savePreset(mCurrentBand, index, current, currentRds);
        }
    }

    /**
     * V14.0: Salta al siguiente favorito guardado en la banda actual.
     * V14.1: Prioriza el comando de hardware del motor.
     */
    private void gotoNextFavorite() {
        if (mEngine == null || mPresetManager == null) return;
        
        int currentFreq = mEngine.getCurrentFreq();
        int nextFreq = mPresetManager.getNextFavorite(currentFreq);
        
        if (nextFreq != -1) {
            Log.d(TAG, "Saltando a SIGUIENTE favorito (Software): " + nextFreq);
            gotoFreq(nextFreq);
        } else {
            showToast("No hay otros favoritos guardados");
        }
    }

    /**
     * V14.0: Salta al favorito anterior guardado en la banda actual.
     * V14.1: Prioriza el comando de hardware del motor.
     */
    private void gotoPreviousFavorite() {
        if (mEngine == null || mPresetManager == null) return;

        int currentFreq = mEngine.getCurrentFreq();
        int prevFreq = mPresetManager.getPreviousFavorite(currentFreq);
        
        if (prevFreq != -1) {
            Log.d(TAG, "Saltando a ANTERIOR favorito (Software): " + prevFreq);
            gotoFreq(prevFreq);
        } else {
            showToast("No hay otros favoritos guardados");
        }
    }

    // V3.0: Background personalizado
    private android.view.View mRootLayout;

    private TextView tvFrequency, tvRdsName, tvRdsInfo;
    private android.view.View boxFrequency;
    private ImageView ivBandIndicator, ivUnitLabel, ivFavoriteIndicator, ivStereoIcon;
    private ImageButton btnLocDx, btnBand;

    // UI Arrays for Presets - REMOVED (Managed by PresetManager)

    private void animateButton(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 0.9f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 0.9f, 1.0f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRadioService = IRadioServiceAPI.Stub.asInterface(service);
            try {
                // V12.4: NO registrar mCallback directamente. El RadioEngine se encarga de los callbacks.
                // Esto evita duplicidades y conflictos de códigos de evento (flickering).
                // mRadioService.registerRadioCallback(mCallback); 

                try {
                    mCurrentBand = mRadioService.getCurrentBand();
                } catch (Exception ignored) {
                }

                startStatusPolling();
                showToast("Conexión Establecida");

                refreshPresetsCache();
                runOnUiThread(() -> {
                    if (mPresetManager != null) {
                        mPresetManager.refreshPresetsCache(mCurrentBand);
                        mPresetManager.refreshButtons(mCurrentBand);
                    }
                    if (mLastFreq != -1) {
                        updateFrequencyDisplay(mLastFreq);
                    }
                });
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

    // V9.9: Hack para el problema del K706 donde MediaFocusControl "roba" 
    // el canal pero no nos envía OnAudioFocusChange (solo abandona customAudioFocus).
    private BroadcastReceiver mBtStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.qf.action.BT_STATE".equals(intent.getAction())) {
                // state=0 (Desconectado), state=1 (Conectando), state=2 (Conectado)
                int state = intent.getIntExtra("state", -1);
                Log.d(TAG, "BT_STATE Broadcast Received: " + state);
                
                if (mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
                    com.example.openradiofm.data.source.K706RadioManager k706Manager = 
                        ((com.example.openradiofm.data.source.K706Engine) mEngine).getManager();
                    
                    if (state == 0) {
                        Log.d(TAG, "Bluetooth Desconectado: Forzando recuperación de audio FM (SetChannel 2)");
                        // Tras unos milisegundos para dejar que el sistema asimile la desconexión
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                k706Manager.enforceAudioChannelRecovery(); // Need to add this helper in K706RadioManager
                                showToast("Recuperando Audio FM...");
                            } catch (Exception e) {
                                Log.e(TAG, "Error recuperando canal FM tras BT", e);
                            }
                        }, 500); 
                    } else if (state == 2) {
                        Log.d(TAG, "Bluetooth Conectado: Nos silenciamos preventivamente");
                        try {
                            k706Manager.setMute(true);
                            k706Manager.returnAudioChannel(); // Helper para RPC_SetChannel(4)
                        } catch (Exception e) {
                            Log.e(TAG, "Error cediendo canal FM tras BT connect", e);
                        }
                    }
                }
            }
        }
    };


    // ScheduledExecutorService para sondear el estado de la radio en segundo plano.
    // Más robusto que Timer y evita fugas de memoria.
    private java.util.concurrent.ScheduledExecutorService mPollingExecutor;

    /**
     * Inicia el sondeo periódico del estado de la radio.
     *
     * IMPORTANTE:
     * - El trabajo pesado (llamadas AIDL y acceso al repositorio/root) se ejecuta
     * en el hilo del ScheduledExecutorService (en segundo plano).
     * - Solo el pintado de la interfaz se hace dentro de runOnUiThread().
     */
    private void startStatusPolling() {
        stopStatusPolling();
        mPollingExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        mPollingExecutor.scheduleAtFixedRate(() -> {
            // Ejecutamos la lógica de refresco directamente en el hilo del executor.
            // Dentro de refreshRadioStatus() se usa runOnUiThread() solo para la UI.
            refreshRadioStatus();
        }, 500, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Detiene el sondeo de estado si está activo.
     */
    private void stopStatusPolling() {
        if (mPollingExecutor != null) {
            mPollingExecutor.shutdownNow();
            mPollingExecutor = null;
        }
    }

    private final com.hcn.autoradio.IRadioCallBack mCallback = new com.hcn.autoradio.IRadioCallBack.Stub() {
        @Override
        public void onEvent(int code, String data) {
            runOnUiThread(() -> {
                switch(code) {
                    case 110: // Debug RDS
                        if (mEngineeringDialog != null && mEngineeringDialog.isShowing()) {
                            mEngineeringDialog.addRdsLog(data);
                        }
                        break;
                    default:
                        // Los eventos estándar (Frecuencia, RDS, Stereo, etc.) 
                        // se manejan ahora vía RadioEngineCallback unificado.
                        break;
                }
            });
        }
    };

    // V5.0: Callbacks unificados del RadioEngine (MainActivity implementa RadioEngineCallback)
    @Override public void onFrequencyChanged(int freqKhz) {
        mCurrentPi = null; // Reset PI on tune
        runOnUiThread(() -> updateFrequencyDisplay(freqKhz));
    }
    @Override public void onBandChanged(int band) {
        runOnUiThread(() -> { 
            mCurrentBand = band; 
            if (mPresetManager != null) {
                mPresetManager.refreshPresetsCache(band);
                mPresetManager.refreshButtons(band);
            }
        });
    }
    @Override public void onStereoChanged(boolean stereo) {
        runOnUiThread(() -> { 
            if (ivStereoIcon != null) ivStereoIcon.setVisibility(stereo ? android.view.View.VISIBLE : android.view.View.GONE); 
            if (ivSignalLevel != null) {
                // V12.4: Actualizar color de señal según estado Stereo (Verde=Stereo, Amarillo=Mono)
                int color = stereo ? android.graphics.Color.parseColor("#00E676") : android.graphics.Color.parseColor("#FFD600");
                ivSignalLevel.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        });
    }
    @Override public void onRdsName(final String name) {
        runOnUiThread(() -> { 
            if (tvRdsName != null && name != null && !name.isEmpty()) {
                tvRdsName.setText(name);
                tvRdsName.setVisibility(View.VISIBLE);
                mHasRdsLock = true;
                
                // V13.6: Persistir en repositorio por frecuencia para logos y futuros presets
                if (mRepository != null && mEngine != null) {
                    mRepository.saveRdsName(mEngine.getCurrentFreq(), name);
                }

                // V11: Aprendizaje automático por PI
                if (mCurrentPi != null && mRdsDb != null) {
                    mRdsDb.savePiName(mCurrentPi, name);
                }

                // V13.6: Si esta frecuencia es un favorito, actualizar su botón para mostrar nombre/logo
                if (mPresetManager != null && mEngine != null) {
                    mPresetManager.updateCardVisuals(-1, mEngine.getCurrentFreq(), mCurrentBand);
                }
            }
        });
    }
    @Override public void onRdsText(String text) {
        String cleanedText = MetadataUtils.cleanRdsText(text);
        runOnUiThread(() -> {
            if (tvRdsInfo != null) {
                String current = tvRdsInfo.getText().toString();
                if (!current.equals(cleanedText)) {
                    tvRdsInfo.setText(cleanedText);
                    tvRdsInfo.setSelected(true); // Enable marquee
                    if (cleanedText == null || cleanedText.trim().isEmpty()) {
                        tvRdsInfo.setVisibility(mIsV3 ? View.GONE : View.VISIBLE);
                    } else {
                        tvRdsInfo.setVisibility(View.VISIBLE);
                    }
                }
                mHasRdsLock = (cleanedText != null && !cleanedText.isEmpty());
            }
        });
    }
    @Override public void onRdsPty(String pty) {
        runOnUiThread(() -> {
            mCurrentPty = pty;
            updatePtyUI(pty);
        });
    }
    @Override public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
        runOnUiThread(() -> {
            if (ivAfIcon != null) ivAfIcon.setAlpha(afEnabled ? 1.0f : 0.2f);
            if (ivTaIcon != null) ivTaIcon.setAlpha(taEnabled ? 1.0f : 0.2f);
            if (ivTpIcon != null) ivTpIcon.setAlpha(tpEnabled ? 1.0f : 0.2f);
            Log.d(TAG, "Engine RDS Status: AF=" + afEnabled + " TA=" + taEnabled + " TP=" + tpEnabled);
        });
    }
    @Override public void onRdsPi(String piCode) {
        mCurrentPi = piCode;
        runOnUiThread(() -> {
            Log.d(TAG, "RDS PI Code received from engine: " + piCode);
            
            // V11: Identificación instantánea
            if (mRdsDb != null) {
                String savedName = mRdsDb.getNameForPi(piCode);
                if (savedName != null && tvRdsName != null) {
                    tvRdsName.setText(savedName);
                    tvRdsName.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Instant Identification via PI: " + savedName);

                    // V13.6: También guardar en repositorio por frecuencia para logos
                    if (mRepository != null && mEngine != null) {
                        mRepository.saveRdsName(mEngine.getCurrentFreq(), savedName);
                        // Refrescar favoritos si aplica
                        if (mPresetManager != null) {
                            mPresetManager.updateCardVisuals(-1, mEngine.getCurrentFreq(), mCurrentBand);
                        }
                    }
                }
            }

            // V11: Posibilidad de mostrar el código PI en Engineering Dialog si está abierto
            if (mEngineeringDialog != null && mEngineeringDialog.isShowing()) {
                mEngineeringDialog.addRdsLog("PI: " + piCode);
            }
        });
    }
    @Override public void onDxLocalChanged(boolean isLocal) {
        runOnUiThread(() -> { 
            if (btnLocDx != null) {
                btnLocDx.setSelected(isLocal);
                btnLocDx.setAlpha(1.0f);
                // V9: LOCAL=radio_loc_p (active/filled), DX=radio_loc_n (normal/outline)
                btnLocDx.setImageResource(isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
            }
        });
    }
    @Override public void onScanStatusChanged(boolean scanning) {
        runOnUiThread(() -> {
            if (!scanning && mStationAdapter != null) {
                // Si el escaneo terminó automáticamente, podemos actualizar algún indicador si existiera
                Log.d(TAG, "Scan finished callback received");
            }
        });
    }
    @Override public void onRawEvent(int code, String data) {
        // Forward to engineering dialog if open
        if (mEngineeringDialog != null && mEngineeringDialog.isShowing()) {
            mEngineeringDialog.addRdsLog(data);
        }
    }

    private int mTestClickCount = 0;
    private long mTestStartTime = 0;

    // V8.5: Credits Easter Egg Variables (Restored)
    private int mCreditsClickCount = 0;
    private long mCreditsStartTime = 0;

    /**
     * Envía una tecla al MCU del coche usando la API interna
     * android.carsource.McuManager.
     * Todo el acceso va envuelto en try/catch para que en dispositivos sin esta
     * clase
     * simplemente se muestre un Toast y no se cierre la app.
     */
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
            showToast("Hardware EQ no soportado en este dispositivo");
        }
    }

    private ImageView ivDynamicBackground;

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mLastFreq", mLastFreq);
        outState.putBoolean("mIsV3", mIsV3);
        Log.d(TAG, "State Saved: Freq=" + mLastFreq);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_OpenRadioFm);
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLastFreq = savedInstanceState.getInt("mLastFreq", -1);
            mIsV3 = savedInstanceState.getBoolean("mIsV3", false);
            Log.d(TAG, "State Restored: Freq=" + mLastFreq);
        }

        // Registrar receiver para las desconexiones Bluetooth de la placa QF (K706)
        IntentFilter filter = new IntentFilter("com.qf.action.BT_STATE");
        registerReceiver(mBtStateReceiver, filter);

        // V3.0: Layout Selection
        mPrefs = getSharedPreferences("RadioPresets", MODE_PRIVATE); // Init prefs early
        mIsV3 = mPrefs.getBoolean("pref_layout_v3", false);

        // V4.7: Manejo de Barra de Estado (Fullscreen condicional)
        boolean showStatusBarV2 = mPrefs.getBoolean("pref_show_status_bar_v2", false);
        if (mIsV3 || (!mIsV3 && showStatusBarV2)) {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            // El Layout 2 por defecto es pantalla completa
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(mIsV3 ? R.layout.activity_main_v3 : R.layout.activity_main);

        // V3.8: Premium Background Binding
        ivDynamicBackground = findViewById(R.id.ivDynamicBackground);

        if (checkSelfPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
        }

        // V13: Inicializar Managers
        mPresetManager = new PresetManager(this, mRepository, mPrefs, PRESETS_COUNT);
        mDialogManager = new DialogManager(this);
        mLogoManager = new LogoManager(this);

        // V2.0: Cargar fondo personalizado si existe
        mLogoManager.loadCustomBackground();
        mLogoManager.loadCarLogo(); // V3.9: Cargar logo marca coche

        // Determinar modo de funcionamiento (FM completo vs básico) antes de crear el
        // repositorio.
        mMode = detectMode();

        // V11: Database RDS PI
        mRdsDb = new com.example.openradiofm.data.source.RdsDatabase(this);
        Log.d(TAG, "Modo de funcionamiento: " + mMode);

        // V13: Cargar última frecuencia guardada antes de conectar
        if (mLastFreq == -1) {
            mLastFreq = mPrefs.getInt("pref_last_freq", 87500); // 87.5 MHz default
            Log.d(TAG, "Freq cargada de SharedPreferences: " + mLastFreq);
        }

        if (mMode == FmMode.FM_BASICO) {
            showToast("Modo Básico: Sin Motor Detectado");
        } else if (mMode == FmMode.FM_K706) {
             showToast("Motor K706: Hardware Nativo Detectado");
        } else {
            showToast("Motor MT8163: Servicio de Sistema Detectado");
        }

        if (mMode == FmMode.FM_MT8163) {
            // Repositorio de datos (nombres RDS por root + logos locales/cloud).
            mRepository = new com.example.openradiofm.data.repository.RadioRepository(this, true);
        } else {
            // En modo K706 o básico, el RDS Name ya lo gestiona el Manager o se saca de metadatos.
            mRepository = new com.example.openradiofm.data.repository.RadioRepository(this, false);
        }
        // Preferencias para presets y estados de indicadores (TA/AF/TP, etc.).
        mPrefs = getSharedPreferences("RadioPresets", MODE_PRIVATE);

        // V13: Inicializar PresetManager y DialogManager
        mPresetManager = new PresetManager(this, mRepository, mPrefs, PRESETS_COUNT);
        mDialogManager = new DialogManager(this);

        // Bind Views
        tvFrequency = findViewById(R.id.tvFrequency);
        tvRdsName = findViewById(R.id.tvRdsName); // V5
        tvRdsInfo = findViewById(R.id.tvRdsInfo);

        // V4.3: New UI Elements
        tvPty = findViewById(R.id.tvPty);
        ivSignalLevel = findViewById(R.id.ivSignalLevel);
        ivPtyIcon = findViewById(R.id.ivPtyIcon); // V5.0

        btnLocDx = findViewById(R.id.btnLocDx);
        btnBand = findViewById(R.id.btnBand);

        ivBandIndicator = findViewById(R.id.ivBandIndicator);
        ivUnitLabel = findViewById(R.id.ivUnitLabel);
        ivFavoriteIndicator = findViewById(R.id.ivFavoriteIndicator);
        ivStereoIcon = findViewById(R.id.ivStereoIcon);
        ivAfIcon = findViewById(R.id.ivAfIcon);
        ivTaIcon = findViewById(R.id.ivTaIcon);
        ivTpIcon = findViewById(R.id.ivTpIcon);
        
        // V9.9: RDS Icons must be dimmed by default, not gone.
        // V5.0: RDS Icons - Ahora usan mEngine (sin bifurcación por modo)
        if (ivAfIcon != null) {
            ivAfIcon.setAlpha(0.2f);
            ivAfIcon.setOnClickListener(v -> {
                animateButton(ivAfIcon);
                if (mEngine != null) mEngine.toggleRdsFeature(1); // AF
            });
        }
        if (ivTaIcon != null) {
            ivTaIcon.setAlpha(0.2f);
            ivTaIcon.setOnClickListener(v -> {
                animateButton(ivTaIcon);
                if (mEngine != null) mEngine.toggleRdsFeature(2); // TA
            });
        }
        if (ivTpIcon != null) {
            ivTpIcon.setAlpha(0.2f);
            ivTpIcon.setOnClickListener(v -> {
                animateButton(ivTpIcon);
                if (mEngine != null) mEngine.toggleRdsFeature(0); // RDS global
            });
        }

        android.view.View boxLogo = findViewById(R.id.boxLogo);

        if (boxLogo != null) {
            boxLogo.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = new com.example.openradiofm.ui.theme.ThemeManager(
                        this).cycleSkin();
                applySkin(next);
                showToast("Skin: " + next.displayName);
            });
            boxLogo.setOnLongClickListener(v -> {
                mDialogManager.showHistoryDialog();
                return true;
            });
        }
        if (tvRdsName != null) {
            tvRdsName.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = new com.example.openradiofm.ui.theme.ThemeManager(
                        this).cycleSkin();
                applySkin(next);
                showToast("Skin: " + next.displayName);
            });
        }

        android.view.View ivCarLogo = findViewById(R.id.ivCarLogo);
        if (ivCarLogo != null) {
            ivCarLogo.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = new com.example.openradiofm.ui.theme.ThemeManager(
                        this).cycleSkin();
                applySkin(next);
                showToast("Skin: " + next.displayName);
            });
            ivCarLogo.setOnLongClickListener(v -> {
                mDialogManager.showHistoryDialog();
                return true;
            });
        }

        // Indicators Binding - REMOVED

        // Configurar controles (EQ, Mute, Test, AutoScan, LOC/DX)
        setupControlButtons();

        // Configurar indicadores de estado (Eliminados)
        // setupIndicators();

        // Aplicar Skin guardado
        com.example.openradiofm.ui.theme.ThemeManager themeManager = new com.example.openradiofm.ui.theme.ThemeManager(
                this);
        applySkin(themeManager.getCurrentSkin());
        checkAndApplyNightMode(); // V4: Automatic Night Mode

        // Seeking Logic
        setupSeekButtons();
 
        // V13: Presets via PresetManager
        mPresetManager.bindViews(findViewById(android.R.id.content), mIsV3);
        mPresetManager.refreshPresetsCache(mCurrentBand);
        mPresetManager.refreshButtons(mCurrentBand);

        setupRdsText();
        applyFonts();

        // V10: Custom User Names
        setupCustomNameEditing();

        // V8.5: Easter Egg (Credits) - Restored
        setupCreditsEasterEgg();

        // Conectamos con el servicio de radio del coche.
        conectarRadio();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: Nueva configuración detectada");
        
        // V10: Manejar cambio de modo noche sin recrear la Activity
        if (mPrefs != null && mPrefs.getBoolean("pref_night_mode_auto", false)) {
            checkAndApplyNightMode();
        }
    }

    /**
     * Configura los botones de control (EQ, Mute, Test, AutoScan, LOC/DX).
     */
    private void setupControlButtons() {
        // EQ Logic (V10: Delegated to RadioEngine)
        ImageButton btnEq = findViewById(R.id.btnSettings);
        if (btnEq != null) {
            btnEq.setOnClickListener(v -> {
                if (mEngine != null) {
                    mEngine.openEq(MainActivity.this);
                } else {
                    // Fallback para modo básico o inicio temprano
                    showToast("Ecualizador no disponible (Motor no iniciado)");
                }
            });
            // V4.6: Se elimina la pulsación larga para el menú premium aquí, ahora está en btnExtra1
        }

        // Mute Logic (System Audio)
        ImageButton btnMute = findViewById(R.id.btnMute);
        if (btnMute != null) {
            btnMute.setOnClickListener(v -> {
                boolean newState = !mMuteState;
                setMute(newState);
                // V4.3: Mute Icon Logic (Selected = Mute On = Speaker+X)
                btnMute.setSelected(newState);
                btnMute.setImageResource(newState ? R.drawable.radio_mute_p : R.drawable.radio_mute_n);
            });
        }

        // V3.8: GPS Button with Hidden Test Menu
        android.view.View btnGps = findViewById(R.id.btnGps);
        if (btnGps != null) {
            btnGps.setOnClickListener(v -> {
                long now = System.currentTimeMillis();

                // Track clicks for hidden menu
                if (mTestClickCount == 0 || (now - mTestStartTime) > 3000) {
                    mTestClickCount = 1;
                    mTestStartTime = now;
                } else {
                    mTestClickCount++;
                }

                if (mTestClickCount >= 5) {
                    mTestClickCount = 0; // Reset
                    // V9.5: Abrir menú de desarrollo correcto según hardware
                    if (mMode == FmMode.FM_K706) {
                        mEngineeringDialog = new K706EngineeringDialog(MainActivity.this);
                        mEngineeringDialog.setOnDismissListener(dialog -> mEngineeringDialog = null);
                        mEngineeringDialog.show();
                    } else if (mMode == FmMode.FM_MT8163) {
                        new EngineeringModeDialog(MainActivity.this).show();
                    } else {
                        showToast("Modo básico: Menú de ingeniería no disponible");
                    }
                } else {
                    // Single click action: Open GPS
                    // If it's the first click or still haven't reached 5
                    if (mTestClickCount == 1) {
                        try {
                            // Try to open Google Maps as default, or any maps app
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q="));
                            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mapIntent);
                        } catch (Exception e) {
                            showToast("No se encontró aplicación de GPS");
                        }
                    }
                }
            });
        }

        ImageButton btnAutoScan = findViewById(R.id.btnAutoScan);
    if (btnAutoScan != null) {
        btnAutoScan.setImageResource(R.drawable.radio_scan_icon_f); // V4.3 Corrected to _f
        btnAutoScan.setOnClickListener(v -> toggleAutoScan(btnAutoScan));
        
        // V11.7: Auto Store Selectivo (Solo K706) en Pulsación Larga
        btnAutoScan.setOnLongClickListener(v -> {
            if (mMode == FmMode.FM_K706) {
                mDialogManager.showSelectiveScanDialog();
                return true;
            } else {
                showToast("Escaneo selectivo solo disponible en motor K706");
                return false;
            }
        });
    }
    

        // LOC/DX Switch — V5.0: Via RadioEngine
        btnLocDx = findViewById(R.id.btnLocDx);
        if (btnLocDx != null) {
            btnLocDx.setOnClickListener(v -> {
                if (mEngine != null) {
                    mEngine.toggleDxLocal();
                }
            });

            // V3.5: Layout Toggle on Long Press
            btnLocDx.setOnLongClickListener(v -> {
                boolean current = mPrefs.getBoolean("pref_layout_v3", false);
                mPrefs.edit().putBoolean("pref_layout_v3", !current).apply();
                showToast("Layout: " + (!current ? "V3 (Horizontal)" : "V2 (Vertical)"));
                recreate();
                return true;
            });
        }

        // V4.0: Extra Button 1 - Android Settings (V4.6: Now opens Premium Menu on short click)
        ImageButton btnExtra1 = findViewById(R.id.btnExtra1);
        if (btnExtra1 != null) {
            btnExtra1.setOnClickListener(v -> {
                mDialogManager.showPremiumSettingsDialog();
            });

            // V4.6: Long click opens Android Settings
            btnExtra1.setOnLongClickListener(v -> {
                try {
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settingsIntent);
                } catch (Exception e) {
                    showStyledToast(getString(R.string.error_opening_settings));
                }
                return true;
            });
        }

        // V4.0: Extra Button 2 - Save/Load Favorites
        ImageButton btnExtra2 = findViewById(R.id.btnExtra2);
        if (btnExtra2 != null) {
            btnExtra2.setOnClickListener(v -> {
                mDialogManager.showSaveLoadFavoritesDialog();
            });
        }
    }

    private void setupCustomNameEditing() {
        // Permitir editar el nombre al mantener pulsado el texto del nombre RDS
        if (tvRdsName != null) {
            // Click normal: Mostrar historial
            tvRdsName.setOnClickListener(v -> mDialogManager.showHistoryDialog());

            // Long click: Editar nombre
            tvRdsName.setOnLongClickListener(v -> {
                mDialogManager.showEditNameDialog();
                return true;
            });
        }

        // También en el logo principal, por si tvRdsName está vacío
        android.view.View ivMainLogo = findViewById(R.id.ivMainLogo);
        if (ivMainLogo != null) {
            // Click normal: Cambiar Color (Ciclar Skin)
            ivMainLogo.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = new com.example.openradiofm.ui.theme.ThemeManager(
                        this).cycleSkin();
                applySkin(next);
                showToast("Skin: " + next.displayName);
            });

            // Long click: Historial
            ivMainLogo.setOnLongClickListener(v -> {
                mDialogManager.showHistoryDialog();
                return true;
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // --------------------------------------------------------------------------------
        // LIMPIEZA DE RECURSOS (CRÍTICO PARA EVITAR FUGAS DE MEMORIA)
        // --------------------------------------------------------------------------------

        // 1) Detener el Timer de sondeo.
        stopStatusPolling();

        // 2) Apagar el subsistema de hardware de radio
        if (mEngine != null) {
            mEngine.release();
        }

        // 3) Desconectar del Servicio de Radio del Coche.
        try {
            unregisterReceiver(mBtStateReceiver);
        } catch (Exception e) {}
        
        try {
            if (mRadioService != null) {
                mRadioService.unRegisterRadioCallback(mCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            // Si ya estaba desregistrado, ignoramos.
        }

        // 3) Liberar el listener de la API oculta de radio.
        // HiddenRadioPlayer mantiene una referencia a nuestros callbacks. Al liberarlo,
        // rompemos esa referencia circular.
        if (mHiddenPlayer != null) {
            mHiddenPlayer.release();
            mHiddenPlayer = null;
        }

        // 4) Cerrar procesos Root (Shell).
        // RootRDSSource abre un proceso "su" persistente. Si no lo cerramos con "exit",
        // ese proceso se quedaría huérfano en el sistema consumiendo RAM y CPU.
        if (mRepository != null) {
            mRepository.shutdown();
            mRepository = null;
        }
    }

    /**
     * Detecta el modo de funcionamiento de la app.
     * 
     * MODO_FM_COMPLETO:
     * - Se activa solo si tenemos ROOT ("su") y el SERVICIO HCN
     * ("com.hcn.autoradio").
     * - Permite leer nombres RDS avanzados directamente de archivos del sistema.
     * 
     * MODO_FM_BASICO:
     * - Se usa en dispositivos normales o si falta el servicio del coche.
     * - Desactiva totalmente el código Root para evitar permisos denegados o
     * crashes.
     */
    private FmMode detectMode() {
        // 1. QS6 G5 (NWD) - New Hardware
        if (isQS6()) {
            return FmMode.FM_QS6;
        }

        // 2. Prioridad: K706 (mcu_service nativo)
        if (isK706()) {
            return FmMode.FM_K706;
        }

        // 3. Prioridad: MT8163 (Servicio de radio del sistema)
        if (hasCarRadioService()) {
            return FmMode.FM_MT8163;
        }
        
        // 4. Fallback: Modo básico
        return FmMode.FM_BASICO;
    }

    /**
     * Comprueba si el sistema anuncia algún servicio de radio compatible.
     * Soporta varios fabricantes (HCN, MTK, TopWay, Generic).
     */
    private boolean hasCarRadioService() {
        int engineIdx = mPrefs.getInt("pref_radio_engine", 0); // 0: Auto, 1: HCN, 2: MTK, 3: TS, 4: Standard

        String[][] allProviders = {
                { "com.hcn.autoradio", "com.hcn.autoradio.FM_PLUG_SERVICE" },
                { "com.mediatek.fmradio", "com.mediatek.fmradio.IFmRadioService" },
                { "com.android.fmradio", "com.android.fmradio.IFmRadioService" },
                { "com.android.fmradio", "com.android.fmradio.FmRadioService" },
                { "com.ts.mainui", "com.ts.mainui.radio.IRadioService" },
                { "com.syu.radio", "com.syu.radio.IRadioService" }
        };

        android.content.pm.PackageManager pm = getPackageManager();

        // If not Auto, pick only the specific one
        if (engineIdx > 0) {
            int target = engineIdx - 1;
            if (target < allProviders.length) {
                String[] provider = allProviders[target];
                return checkProvider(pm, provider);
            }
        }

        // Auto mode: scan all
        for (String[] provider : allProviders) {
            if (checkProvider(pm, provider))
                return true;
        }
        return false;
    }

    private boolean checkProvider(android.content.pm.PackageManager pm, String[] provider) {
        try {
            Intent intent = new Intent(provider[1]);
            intent.setPackage(provider[0]);
            java.util.List<android.content.pm.ResolveInfo> list = pm.queryIntentServices(intent, 0);
            if (list != null && !list.isEmpty()) {
                Log.d(TAG, "Detector: Encontrado servicio en " + provider[0]);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Comprobación simple de root: verifica si existe un binario su
     * en rutas típicas del sistema. No ejecuta su, por lo que es segura.
     */
    private boolean hasRootBinary() {
        String[] paths = {
                "/system/xbin/su",
                "/system/bin/su",
                "/system/su",
                "/sbin/su"
        };
        for (String path : paths) {
            try {
                java.io.File f = new java.io.File(path);
                if (f.exists()) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * V6.0: Detecta si existe el servicio MCU específico del K706.
     */
    private boolean isK706() {
        try {
            // Check if mcu_service exists
            Object service = getSystemService("mcu_service");
            if (service != null && service.getClass().getName().contains("McuManager")) {
                Log.d(TAG, "K706 Detectado: mcu_service encontrado.");
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    /**
     * V12.0: Detecta si es una plataforma NWD (QS6 G5).
     */
    private boolean isQS6() {
        try {
            android.content.pm.PackageManager pm = getPackageManager();
            android.content.Intent intent = new android.content.Intent("com.nwd.radio.service.ACTION_RADIO_SERVICE");
            intent.setPackage("com.nwd.radio.service");
            java.util.List<android.content.pm.ResolveInfo> list = pm.queryIntentServices(intent, 0);
            if (list != null && !list.isEmpty()) {
                Log.d(TAG, "QS6 G5 Detectado: Servicio NWD encontrado.");
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    /**
     * V3.8: Aplica la tipografía seleccionada a los elementos clave.
     * Busca los archivos en res/font (ej: bebas.ttf, digital.ttf, inter.ttf)
     */
    public void applyFonts() {
        int fontType = mPrefs.getInt("pref_font_type", 0); // 0: Default, 1: Bebas, 2: Digital, 3: Inter

        android.graphics.Typeface typeface = null;
        try {
            if (fontType == 1)
                typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.bebas);
            else if (fontType == 2)
                typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.digital);
            else if (fontType == 3)
                typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.inter);
            else if (fontType == 4)
                typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.orbitron);
            else if (fontType == 5)
                typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.formula1);
        } catch (Exception e) {
            // Si no existen los archivos en res/font todavía, usamos el por defecto
            // (Orbitron en assets o System)
            try {
                typeface = android.graphics.Typeface.createFromAsset(getAssets(), "fonts/orbitron_bold.ttf");
            } catch (Exception ex) {
                typeface = android.graphics.Typeface.DEFAULT_BOLD;
            }
        }

        if (typeface == null)
            typeface = android.graphics.Typeface.DEFAULT_BOLD;

        if (tvFrequency != null)
            tvFrequency.setTypeface(typeface);
        if (tvRdsName != null)
            tvRdsName.setTypeface(typeface);
        if (tvRdsInfo != null)
            tvRdsInfo.setTypeface(typeface);

        // V2.1: Use traditional loop for safety with tvPresets array
        if (mPresetManager != null) {
            mPresetManager.applyFonts(typeface);
        }
    }

    public void refreshPresetButtons() {
        if (mPresetManager != null) {
            mPresetManager.refreshButtons(mCurrentBand);
        }
    }

    public int getSkinDrawableId() {
        if (mCurrentSkin == null) return R.drawable.bg_glass_card_premium;
        switch (mCurrentSkin) {
            case NIGHT_MODE: return R.drawable.bg_glass_card_night;
            case ORANGE:     return R.drawable.bg_glass_card_orange;
            case BLUE:       return R.drawable.bg_glass_card_blue;
            case GREEN:      return R.drawable.bg_glass_card_green;
            case PURPLE:     return R.drawable.bg_glass_card_purple;
            case RED:        return R.drawable.bg_glass_card_red;
            case YELLOW:     return R.drawable.bg_glass_card_yellow;
            case CYAN:       return R.drawable.bg_glass_card_cyan;
            case PINK:       return R.drawable.bg_glass_card_pink;
            case WHITE:      return R.drawable.bg_glass_card_white;
            default:         return R.drawable.bg_glass_card_premium;
        }
    }

    private void setupRdsText() {
        // V8.3: Enable Marquee on RDS Info
        if (tvRdsInfo != null) {
            tvRdsInfo.setText(""); // Start Empty
            tvRdsInfo.setSelected(true); // Required for Marquee
            tvRdsInfo.setSingleLine(true);
            tvRdsInfo.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
            tvRdsInfo.setMarqueeRepeatLimit(-1);
            tvRdsInfo.setSelected(true);
        }
    }

    /**
     * Configura el Easter Egg de créditos al pulsar la frecuencia. (Restaurado)
     */
    private void setupCreditsEasterEgg() {
        if (tvFrequency != null) {
            tvFrequency.setOnClickListener(v -> {
                long now = System.currentTimeMillis();
                // Reset si han pasado más de 3 segundos desde el primer clic
                if (mCreditsClickCount == 0 || (now - mCreditsStartTime) > 3000) {
                    mCreditsClickCount = 1;
                    mCreditsStartTime = now;
                } else {
                    mCreditsClickCount++;
                }

                if (mCreditsClickCount >= 5) {
                    mCreditsClickCount = 0;
                    if (mDialogManager != null) mDialogManager.showCreditsDialog();
                }
            });
        }
    }


    /**
     * V2.0: Crea la carpeta /sdcard/RadioLogos/ si no existe.
     * Esto soluciona el bug reportado donde la app no creaba la carpeta
     * automáticamente.
     */
    private void createRadioLogosFolder() {
        try {
            java.io.File radioLogosDir = new java.io.File("/sdcard/RadioLogos/");
            if (!radioLogosDir.exists()) {
                boolean created = radioLogosDir.mkdirs();
                if (created) {
                    Log.d(TAG, "Carpeta RadioLogos creada exitosamente");
                } else {
                    Log.e(TAG, "Error al crear carpeta RadioLogos");
                }
            } else {
                Log.d(TAG, "Carpeta RadioLogos ya existe");
            }
        } catch (Exception e) {
            Log.e(TAG, "Excepción al crear carpeta RadioLogos: " + e.getMessage());
        }
    }

    /**
     * V3.8: Carga el fondo según la preferencia del usuario (pref_bg_mode).
     * 0: Negro Puro, 1: background.png personal, 2: Logo Dinámico.
     */
    public void loadCustomBackground() {
        int bgMode = mPrefs.getInt("pref_bg_mode", 1); // Por defecto Imagen si existe

        // Reset backgrounds first
        if (ivDynamicBackground != null)
            ivDynamicBackground.setVisibility(View.GONE);
        View root = findViewById(R.id.rootLayout); // assuming id is set, or find by type
        if (root == null) {
            android.view.View decor = getWindow().getDecorView().findViewById(android.R.id.content);
            if (decor instanceof android.view.ViewGroup)
                root = ((android.view.ViewGroup) decor).getChildAt(0);
        }

        if (root == null)
            return;

        if (bgMode == 0) {
            // Negro Puro
            root.setBackgroundColor(android.graphics.Color.BLACK);
        } else if (bgMode == 1) {
            // Imagen Fija background.png
            try {
                java.io.File bgJpg = new java.io.File("/sdcard/RadioLogos/background.jpg");
                java.io.File bgPng = new java.io.File("/sdcard/RadioLogos/background.png");
                java.io.File backgroundFile = bgJpg.exists() ? bgJpg : (bgPng.exists() ? bgPng : null);

                if (backgroundFile != null) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory
                            .decodeFile(backgroundFile.getAbsolutePath());
                    if (bitmap != null) {
                        root.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), bitmap));
                    }
                } else {
                    root.setBackgroundResource(R.drawable.bg_grainy_dark);
                }
            } catch (Exception e) {
                root.setBackgroundResource(R.drawable.bg_grainy_dark);
            }
        } else {
            // Logo Dinámico (El fondo base es negro, el logo se superpone en
            // ivDynamicBackground)
            root.setBackgroundColor(android.graphics.Color.BLACK);
            // El refresco real ocurre en updateDynamicBackground
        }
    }

    /**
     * Configura los botones de búsqueda manual y automática de frecuencias.
     *
     * - Los pasos manuales (+/- 0.1 MHz) se hacen mediante llamadas AIDL al
     * servicio,
     * que se ejecutan fuera del hilo principal cuando es posible.
     * - Las búsquedas largas (seek up/down) usan los eventos específicos del
     * servicio.
     */
    private void setupSeekButtons() {
        ImageButton btnSeekDown = findViewById(R.id.btnSeekDown); // LEFT (<)
        ImageButton btnSeekUp = findViewById(R.id.btnSeekUp); // RIGHT (>)
        ImageButton btnFavPrev = findViewById(R.id.btnFavPrev);
        ImageButton btnFavNext = findViewById(R.id.btnFavNext);

        // Left Button (<) -> Step Down / Seek Down (Long Press)
        if (btnSeekDown != null) {
            btnSeekDown.setOnClickListener(v -> {
                animateButton(btnSeekDown);
                stepFreqDown();
            });
            // Standard: Long Left = Seek Down
            btnSeekDown.setOnLongClickListener(v -> {
                onSeekDownEvent();
                return true;
            });
        }

        // Right Button (>) -> Step Up / Seek Up (Long Press)
        if (btnSeekUp != null) {
            btnSeekUp.setOnClickListener(v -> {
                animateButton(btnSeekUp);
                stepFreqUp();
            });
            // Standard: Long Right = Seek Up
            btnSeekUp.setOnLongClickListener(v -> {
                onSeekUpEvent();
                return true;
            });
        }

        // Navigation Buttons (Fav Prev / Fav Next)
        if (btnFavPrev != null) {
            btnFavPrev.setOnClickListener(v -> {
                animateButton(btnFavPrev);
                gotoPreviousFavorite();
            });
        }

        if (btnFavNext != null) {
            btnFavNext.setOnClickListener(v -> {
                animateButton(btnFavNext);
                gotoNextFavorite();
            });
        }

        // V4: Bind Frequency Box for gestures (Fluid Drag)
        boxFrequency = findViewById(R.id.boxFrequency);
        if (boxFrequency != null) {
            boxFrequency.setOnTouchListener(new OnSwipeTouchListener(this) {
                private float scrollAccumulator = 0;
                private static final int SCROLL_SENSITIVITY = 30; // Pixels per step

                @Override
                public void onSwipeLeft() {
                    stepFreqDown();
                }

                @Override
                public void onSwipeRight() {
                    stepFreqUp();
                }

                @Override
                public void onScrollEvent(float deltaX) {
                    if (!mPrefs.getBoolean("pref_swipe_gestures", true))
                        return;

                    scrollAccumulator += deltaX;
                    if (Math.abs(scrollAccumulator) > SCROLL_SENSITIVITY) {
                        if (scrollAccumulator > 0) {
                            stepFreqUp();
                        } else {
                            stepFreqDown();
                        }
                        scrollAccumulator = 0;
                    }
                }
            });
        }

        // Loop Band Logic
        if (btnBand != null) {
            btnBand.setOnClickListener(v -> {
                if (mEngine != null) mEngine.bandCycle();
            });
        }
    }

    public void refreshPresetsCache() {
        if (mPresetManager != null) {
            mPresetManager.refreshPresetsCache(mCurrentBand);
        }
    }

    public void conectarRadio() {
        // V6.0: K706 Direct Connection
        if (mMode == FmMode.FM_K706) {
            try {
                // V5.0: Usar RadioEngine como capa de abstracción
                K706Engine k706Engine = new K706Engine();
                if (k706Engine.init(this)) {
                    mEngine = k706Engine;
                    mRadioService = k706Engine.asAidl(); // Compatibilidad temporal con execRemote()
                    
                    // V12.2: Eliminamos mRadioService.registerRadioCallback(mCallback) 
                    // para evitar conflictos con el callback unificado del Engine.
                    
                    mEngine.setCallback(this);

                    mCurrentBand = mEngine.getCurrentBand();
                    startStatusPolling();
                    showToast("Motor K706 Activado (Engine)");

                    // V6.1: Restore state
                    if (mRadioService instanceof K706RadioManager) {
                        int safeFreq = (mLastFreq > 0) ? mLastFreq : 87500;
                        ((K706RadioManager) mRadioService).syncState(safeFreq, mCurrentBand);
                    }

                    refreshPresetsCache();
                    refreshPresetButtons();
                    refreshRadioStatus();
                    return;
                }
            } catch (Exception e) {
                showToast("Error iniciando K706: " + e.getMessage());
                // Fallback a detección de servicio si falla la instancia local
            }
        }

        if (mMode == FmMode.FM_QS6) {
            try {
                QS6Engine qs6Engine = new QS6Engine();
                if (qs6Engine.init(this)) {
                    mEngine = qs6Engine;
                    // V12.2: Ya no usamos mCallback AIDL directo si hay Engine
                    mEngine.setCallback(this);

                    mCurrentBand = mEngine.getCurrentBand();
                    startStatusPolling();
                    showToast("Motor QS6 Activado (Engine)");

                    refreshPresetsCache();
                    refreshPresetButtons();
                    refreshRadioStatus();
                    return;
                }
            } catch (Exception e) {
                showToast("Error iniciando QS6: " + e.getMessage());
            }
        }

        if (mMode == FmMode.FM_MT8163) {
            try {
                com.example.openradiofm.data.source.MT8163Engine mt8163Engine = new com.example.openradiofm.data.source.MT8163Engine();
                if (mt8163Engine.init(this)) {
                    mEngine = mt8163Engine;
                    mEngine.setCallback(this);

                    mCurrentBand = mEngine.getCurrentBand();
                    startStatusPolling();
                    showToast("Motor MT8163 Activado (Engine)");

                    refreshPresetsCache();
                    refreshPresetButtons();
                    refreshRadioStatus();
                    return;
                }
            } catch (Exception e) {
                showToast("Error iniciando MT8163: " + e.getMessage());
            }
        }

        int engineIdx = mPrefs.getInt("pref_radio_engine", 0);

        String[][] allProviders = {
                { "com.hcn.autoradio", "com.hcn.autoradio.FM_PLUG_SERVICE" },
                { "com.mediatek.fmradio", "com.mediatek.fmradio.IFmRadioService" },
                { "com.android.fmradio", "com.android.fmradio.IFmRadioService" },
                { "com.android.fmradio", "com.android.fmradio.FmRadioService" },
                { "com.ts.mainui", "com.ts.mainui.radio.IRadioService" },
                { "com.syu.radio", "com.syu.radio.IRadioService" }
        };

        // If already connected, maybe unbind? (Optional, let's keep it simple for now)

        if (engineIdx > 0) {
            int target = engineIdx - 1;
            if (target < allProviders.length) {
                bindToProvider(allProviders[target]);
                return;
            }
        }

        for (String[] provider : allProviders) {
            if (bindToProvider(provider))
                return;
        }
    }

    private boolean bindToProvider(String[] provider) {
        Intent intent = new Intent(provider[1]);
        intent.setPackage(provider[0]);
        try {
            if (bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
                Log.d(TAG, "Conector: Vinculando a " + provider[0]);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Lee el estado actual de la radio desde el servicio remoto y lo refleja en la
     * UI.
     *
     * IMPORTANTE:
     * - Este método puede ser llamado desde el hilo del Timer (segundo plano).
     * - Cualquier acceso a vistas se encapsula en runOnUiThread().
     */
    public void refreshRadioStatus() {
        if (mLastFreq != -1) {
            runOnUiThread(() -> updateFrequencyDisplay(mLastFreq));
        }

        if (mEngine == null)
            return;

        int freq = mEngine.getCurrentFreq();
        if (freq <= 0) return; // V11.7: No actualizar si el servicio aún no reporta frecuencia

        int band = mEngine.getCurrentBand();
        boolean isStereo = mEngine.isStereo();
        boolean isLocal = mEngine.isDxLocal();

        // Fix v4.5.1: SIEMPRE sincronizar mCurrentBand y refrescar presets
        if (band != mCurrentBand) {
            mCurrentBand = band;
            if (mPresetManager != null) {
                mPresetManager.refreshPresetsCache(band);
                runOnUiThread(() -> mPresetManager.refreshButtons(band));
            }
        }

        // V4.3: Hardware Toggle for AM
        boolean amEnabled = mPrefs.getBoolean("pref_enable_am", true);
        boolean isAm = (band == BAND_AM1 || band == BAND_AM2);
        if (isAm && !amEnabled) {
            mEngine.bandCycle();
            return;
        }

        String bandCacheKey = band + "_" + freq;

        if (freq != mLastFreq) {
            mLastFreq = freq;
            mHasRdsLock = false;
            mCurrentPty = null;
            mLastLogoUrl = null; // V12.2: Limpiar referencia de logo inmediatamente
            
            runOnUiThread(() -> {
                if (tvRdsName != null) {
                    tvRdsName.setText("");
                    tvRdsName.setVisibility(View.VISIBLE); // V12.2: Siempre visible para evitar "saltos" en UI
                }
                if (tvRdsInfo != null) {
                    tvRdsInfo.setText("");
                    tvRdsInfo.setVisibility(View.VISIBLE);
                }
                
                // V12.2: Limpieza inmediata de imágenes
                ImageView ivMainLogo = findViewById(R.id.ivMainLogo);
                if (ivMainLogo != null && mLogoManager != null) {
                    mLogoManager.applyFallbackLogo(ivMainLogo);
                }

                // V13.7: Limpieza de fondo dinámico al sintonizar
                if (mLogoManager != null) mLogoManager.updateDynamicBackground(null);
                if (tvPty != null) {
                    tvPty.setText("Sin PTY");
                    if (ivPtyIcon != null) ivPtyIcon.setVisibility(View.GONE);
                }
            });

            if (mPrefs.getBoolean("pref_save_history", true)) {
                addToHistory(freq);
            }

            // V13: Guardar última frecuencia persistentemente
            mPrefs.edit().putInt("pref_last_freq", freq).apply();
            Log.d(TAG, "Ultima frecuencia guardada: " + freq);
        }

        com.example.openradiofm.data.model.RadioStation station = mRepository.getStationInfo(freq, null);
        String rdsName = station.getName();
        final int fFreq = freq;
        final int fBand = band;
        final boolean fIsAm = isAm;
        final boolean fIsLocal = isLocal;

        runOnUiThread(() -> {
            // FIXED LOGIC: Freq formatting (MHz for FM, kHz for AM)
            if (fIsAm) {
                tvFrequency.setText(String.valueOf(fFreq));
                if (ivUnitLabel != null) {
                    ivUnitLabel.setImageResource(R.drawable.radio_khz);
                    if (mCurrentSkin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
                        ivUnitLabel.setColorFilter(getResources().getColor(R.color.night_blue_primary, null),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        ivUnitLabel.clearColorFilter();
                    }
                }
            } else {
                tvFrequency.setText(String.format(java.util.Locale.US, "%.2f", fFreq / 1000.0));
                if (ivUnitLabel != null) {
                    ivUnitLabel.setImageResource(R.drawable.radio_mhz);
                    if (mCurrentSkin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
                        ivUnitLabel.setColorFilter(getResources().getColor(R.color.night_blue_primary, null),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        ivUnitLabel.clearColorFilter();
                    }
                }
            }

            updateFrequencyDisplay(fFreq);

            // V4.0: Logo & Background Logic (Handled by LogoManager)
            if (mLogoManager != null) {
                String cachedLogo = mLogoCachePerBand.get(bandCacheKey);
                mLogoManager.updateStationLogo(fFreq, fBand, cachedLogo);
            }

            updateBandImage(fBand);
            if (btnLocDx != null) {
                btnLocDx.setSelected(fIsLocal);
                btnLocDx.setImageResource(fIsLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
            }
        });
        
        sendWidgetUpdateIntent(freq, band, rdsName);
    }

    /**
     * V5.2: Broadcast to K706 Launcher Widget
     */
    private void sendWidgetUpdateIntent(int freq, int band, String rdsName) {
        try {
            android.content.Intent intent = new android.content.Intent("com.qf.radio.update_action");
            
            // Format frequency string (e.g., "92.20") exactly as K706 native FmUtils.formatStation does.
            String freqStr;
            int nativeFreqInt;
            if (band == BAND_AM1 || band == BAND_AM2) {
                freqStr = String.valueOf(freq);
                nativeFreqInt = freq;
            } else {
                java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
                java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols(java.util.Locale.US);
                df.setDecimalFormatSymbols(dfs);
                freqStr = df.format(freq / 1000.0f);
                nativeFreqInt = freq / 10; // K706 native uses 8750 for 87.5 MHz (our freq is 87500)
            }
            
            intent.putExtra("com.qf.radio.update_action_key", freqStr);
            intent.putExtra("com.qf.radio.update_action_freq_key", nativeFreqInt);
            intent.putExtra("com.qf.radio.update_action_band_key", band);
            intent.putExtra("com.qf.radio.update_action_preset_key", getPresetIndex(freq)); // Send 0 if not a preset
            intent.putExtra("com.qf.radio.update_action_searching_key", false); // We don't track search status globally here yet
            
            String widgetName = (rdsName != null && !rdsName.isEmpty() && !rdsName.equals("STATION NAME") && !rdsName.equals("STATION")) ? rdsName : "";
            intent.putExtra("com.qf.radio.update_action_name_key", widgetName);
            
            // Try to make the intent explicit to bypass background implicit broadcast restrictions
            intent.setPackage("com.android.auto.autohome");
            
            // Send standard broadcast (system apps or apps with correct permission will receive it)
            sendBroadcast(intent);
        } catch (Exception ex) {
            Log.e(TAG, "Error sending widget broadcast: " + ex.getMessage());
        }
    }

    /**
     * Calcula una calidad de señal estimada basándose en flags disponibles.
     * Algoritmo basado en CHIP_RADIO_SNR_RSSI.md
     */

    private void updateBandImage(int band) {
        int resId = R.drawable.radio_fm1;
        if (band == BAND_FM1)
            resId = R.drawable.radio_fm1;
        else if (band == BAND_FM2)
            resId = R.drawable.radio_fm2;
        else if (band == BAND_FM3)
            resId = R.drawable.radio_fm3;
        else if (band == BAND_AM1) {
            // Placeholder until assets are provided, fallback to FM1 or a generic icon
            resId = getResources().getIdentifier("radio_am1", "drawable", getPackageName());
            if (resId == 0)
                resId = R.drawable.radio_fm1;
        } else if (band == BAND_AM2) {
            resId = getResources().getIdentifier("radio_am2", "drawable", getPackageName());
            if (resId == 0)
                resId = R.drawable.radio_fm2;
        }

        if (ivBandIndicator != null) {
            ivBandIndicator.setImageResource(resId);
            if (btnBand != null)
                btnBand.setImageResource(R.drawable.radio_band_n);
        } else if (btnBand != null) {
            btnBand.setImageResource(resId);
        }
    }

    private void updateStatusIndicator(TextView tv, boolean active) {
        if (tv == null)
            return;
        if (active) {
            tv.setTextColor(Color.parseColor("#FF8C00")); // Orange for Active
            tv.setAlpha(1.0f);
        } else {
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setAlpha(0.3f);
        }
    }

    /**
     * V5.0: Updates PTY Label and Icon based on RDS data.
     * V4.5.1: Shows localized category name instead of raw PTY number.
     */
    private void updatePtyUI(String pty) {
        String displayLabel = pty;
        int ptyCode = -1;
        
        if (pty == null || pty.trim().isEmpty()) {
            ptyCode = 0;
            displayLabel = "Sin PTY";
        } else {
            // V4.5.1: Try to parse as number and show localized label
            try {
                ptyCode = Integer.parseInt(pty.trim());
                if (ptyCode > 0 && ptyCode <= 31) {
                    displayLabel = PtyManager.getPtyLabel(this, ptyCode);
                } else {
                    ptyCode = 0;
                    displayLabel = "Sin PTY";
                }
            } catch (NumberFormatException ignored) {
                // Not a number, use as-is
                if ("0".equals(pty.trim())) {
                    ptyCode = 0;
                    displayLabel = "Sin PTY";
                }
            }
        }

        // V9.6: Siempre mantenemos el texto visible para no descolocar el layout central. Si no hay valor, mostramos "Sin PTY"
        if (tvPty != null) {
            tvPty.setText(displayLabel);
            tvPty.setVisibility(View.VISIBLE);
        }

        if (ivPtyIcon != null) {
            int iconRes = (ptyCode > 0) ? PtyManager.getPtyIconResource(ptyCode) : 0;
            if (iconRes != 0) {
                ivPtyIcon.setImageResource(iconRes);
                ivPtyIcon.setVisibility(View.VISIBLE);
            } else {
                // Ocultamos el icono para no desperdiciar espacio, pero la TV_PTY mantendrá su zona
                ivPtyIcon.setVisibility(View.GONE);
            }
        }
    }

    private String getBandLabel(int bandCode) {
        if (bandCode == 0)
            return "FM 1";
        if (bandCode == 1)
            return "FM 2";
        if (bandCode == 2)
            return "FM 3";
        return "B" + bandCode;
    }

    // V4: Custom Toasts
    public void showStyledToast(String msg) {
        runOnUiThread(() -> {
            try {
                android.view.View layout = getLayoutInflater().inflate(R.layout.toast_custom, null);
                TextView text = layout.findViewById(R.id.toastText);
                text.setText(msg);

                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            } catch (Exception e) {
                // Fallback
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showToast(String msg) {
        showStyledToast(msg);
    }

    private void launchExternalApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            showToast("App no instalada: " + packageName);
        }
    }

    // V4.0: Saved Preset Indicator & Color Logic (Unified)
    private void updateFrequencyDisplay(int freq) {
        if (freq <= 0) return; // V11.7: Evitar mostrar 00.0/0
        if (tvFrequency != null) {
            if (mCurrentBand == BAND_AM1 || mCurrentBand == BAND_AM2) {
                tvFrequency.setText(String.valueOf(freq));
            } else {
                // V12.3: Usar double para evitar errores de precisión de punto flotante (+/- 0.05)
                tvFrequency.setText(String.format(java.util.Locale.US, "%.2f", (double)freq / 1000.0));
            }

            // Get State
            boolean isNight = (mCurrentSkin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
            boolean isFavorite = isStationMemorized(freq);
            int idx = getPresetIndex(freq);

            // Colors
            int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
            int gold = android.graphics.Color.parseColor("#FFD700");
            int white = android.graphics.Color.WHITE;

            // 1. Dial Color & Unit Label
            if (isNight) {
                // V5.6: Always Night Blue in Night Mode as requested
                tvFrequency.setTextColor(nightBlue);
                if (ivUnitLabel != null)
                    ivUnitLabel.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                if (tvRdsName != null)
                    tvRdsName.setTextColor(nightBlue);
                if (tvRdsInfo != null)
                    tvRdsInfo.setTextColor(nightBlue);
                if (tvPty != null)
                    tvPty.setTextColor(nightBlue);
            } else {
                // Normal Mode -> Always White
                tvFrequency.setTextColor(white);
                if (ivUnitLabel != null)
                    ivUnitLabel.clearColorFilter();
                if (tvRdsName != null)
                    tvRdsName.setTextColor(white);
                if (tvRdsInfo != null)
                    tvRdsInfo.setTextColor(white);
                if (tvPty != null)
                    tvPty.setTextColor(white);
            }

            // 2. Favorite Icon
            if (ivFavoriteIndicator != null) {
                if (isFavorite && idx > 0) {
                    ivFavoriteIndicator.setVisibility(View.VISIBLE);
                    int resId = getResources().getIdentifier("radio_icon_p" + String.format("%02d", idx), "drawable",
                            getPackageName());
                    ivFavoriteIndicator.setImageResource(resId != 0 ? resId : R.drawable.radio_icon_p01);

                    // Tint logic
                    if (isNight) {
                        ivFavoriteIndicator.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        // Normal Mode: White
                        ivFavoriteIndicator.setColorFilter(white, android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                } else {
                    ivFavoriteIndicator.setVisibility(View.GONE);
                }
            }

            // RDS Visibility Logic (V4.0 Finalization)
            com.example.openradiofm.data.model.RadioStation station = mRepository.getStationInfo(freq, null);
            String rdsNameValue = (station != null) ? station.getName() : null;

            if (rdsNameValue != null && !rdsNameValue.isEmpty() && !rdsNameValue.equals("STATION")
                    && !rdsNameValue.equals("STATION NAME") && !rdsNameValue.matches("\\d+")) {
                tvRdsName.setText(rdsNameValue);
                tvRdsName.setVisibility(View.VISIBLE);
            } else {
                String current = tvRdsName.getText().toString();
                // V4.5.1: Si ya hay un nombre recibido por callback (no es el default), NO lo borramos ni lo ocultamos
                if (!current.isEmpty() && !current.equals("STATION") && !current.equals("STATION NAME")
                        && !current.equals("STATION ") && !current.equals(" NAME")) {
                    tvRdsName.setVisibility(View.VISIBLE);
                } else {
                // V11.7.1: Mantener siempre visible para permitir clic y edición manual
                tvRdsName.setVisibility(View.VISIBLE);
                tvRdsName.setText("");
            }
        }

            if (tvRdsInfo != null) {
                String rdsText = tvRdsInfo.getText().toString();
                // V4.5.1: Si ya hay texto RDS recibido, no lo ocultamos bajo ninguna circunstancia en este ciclo
                if (!rdsText.isEmpty() && !rdsText.equals("RDS TEXT INFO") && !rdsText.equals("RDS Info Text")) {
                    tvRdsInfo.setVisibility(View.VISIBLE);
                    tvRdsInfo.setTextColor(isNight ? nightBlue : white);
                } else {
                // V11.7.1: Mantener visible para consistencia visual
                tvRdsInfo.setVisibility(View.VISIBLE);
                tvRdsInfo.setText("");
            }
        }

            // 3. PTY UI (Priorizando vivo sobre la persistencia)
            if (tvPty != null) {
                String storedPty = (station != null) ? station.getPty() : null;
                // Si tenemos PTY en vivo (mCurrentPty), prevalece
                String ptyToDisplay = (mCurrentPty != null) ? mCurrentPty : storedPty;
                
                updatePtyUI(ptyToDisplay);
                tvPty.setTextColor(isNight ? nightBlue : white);
            }

            // 4. Signal Level Coloring (V4.2 Refinement)
            if (ivSignalLevel != null) {
                int signalColor;
                boolean hasStereo = false;
                if (mEngine != null) hasStereo = mEngine.isStereo();

                if (mHasRdsLock && hasStereo) {
                    signalColor = android.graphics.Color.parseColor("#00E676"); // Green
                } else if (hasStereo) {
                    signalColor = android.graphics.Color.parseColor("#FFD600"); // Yellow (Solo si hay estéreo mínimo, lo consideramos emisión válida normal en FM)
                } else if (mCurrentBand == BAND_AM1 || mCurrentBand == BAND_AM2) {
                    // AM bands don't usually have stereo, maybe we show yellow if we are tuned?
                    // We assume that the tune locked, so we provide default AM color
                    signalColor = android.graphics.Color.parseColor("#FFD600"); // Yellow for AM
                } else {
                    signalColor = android.graphics.Color.parseColor("#FF5252"); // Red (No emisión reconocible)
                }
                ivSignalLevel.setColorFilter(signalColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }
            
            // Re-apply stereo visibility based on immediate hardware state
            if (ivStereoIcon != null) {
                boolean hasStereo = mEngine != null && mEngine.isStereo();
                ivStereoIcon.setVisibility(hasStereo ? View.VISIBLE : View.GONE);
            }
        }
    }

    private int getPresetIndexForFreq(int freq) {
        for (int i = 1; i <= 15; i++) {
            String key = "P" + i + "_B" + mCurrentBand;
            if (mPrefs.getInt(key, 0) == freq)
                return i;
        }
        return -1;
    }

    private boolean isFrequencySaved(int freq) {
        // Check current band presets
        for (int i = 1; i <= 12; i++) {
            String key = "P" + i + "_B" + mCurrentBand;
            if (mPrefs.getInt(key, 0) == freq)
                return true;
        }
        return false;
    }


    /**
     * Aplica el skin seleccionado a todos los elementos de la interfaz.
     */
    public void applySkin(com.example.openradiofm.ui.theme.ThemeManager.Skin skin) {
        this.mCurrentSkin = skin; // Update global state
        int drawableId = getSkinDrawableId();

        // V3.0: Detect current layout
        boolean isLayoutV3 = mPrefs.getBoolean("pref_layout_v3", false);

        // Apply skin borders to main controls ONLY in Layout V2
        if (!isLayoutV3) {
            int[] viewIds = {
                    R.id.boxFrequency, R.id.btnSeekUp, R.id.btnSeekDown,
                    R.id.tvRdsName, R.id.tvRdsInfo,
                    R.id.btnBand, R.id.btnAutoScan,
                    R.id.boxLogo,
                    R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                    R.id.btnExtra1, R.id.btnExtra2 // V4.0: New extra buttons
            };

            for (int id : viewIds) {
                android.view.View v = findViewById(id);
                if (v != null) {
                    // Save current padding
                    int pL = v.getPaddingLeft();
                    int pT = v.getPaddingTop();
                    int pR = v.getPaddingRight();
                    int pB = v.getPaddingBottom();

                    v.setBackgroundResource(drawableId);

                    // Restore padding (setBackgroundResource resets it)
                    v.setPadding(pL, pT, pR, pB);
                }
            }
        } else {
            // V4.0: Layout V3 specific skinning for the new RDS boxes (BORDERLESS)
            int borderlessId = R.drawable.bg_glass_card_borderless;
            int[] v3BoxIds = { R.id.tvRdsName, R.id.tvRdsInfo };
            for (int id : v3BoxIds) {
                android.view.View v = findViewById(id);
                if (v != null) {
                    int pL = v.getPaddingLeft();
                    int pT = v.getPaddingTop();
                    int pR = v.getPaddingRight();
                    int pB = v.getPaddingBottom();
                    v.setBackground(null);
                    v.setPadding(pL, pT, pR, pB);
                }
            }
        }

        // V2.1: Apply to Presets P1-P12
        for (int i = 1; i <= 12; i++) {
            int id = getResources().getIdentifier("cardP" + i, "id", getPackageName());
            android.view.View v = findViewById(id);
            if (v != null)
                v.setBackgroundResource(drawableId);
        }

        // V4.0: Apply Night Mode Colors (Android Auto style)
        if (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
            applyNightModeColors();
        } else {
            resetNightModeColors();
        }
    }

    /**
     * Aplica colores azul noche a textos e iconos (estilo Android Auto / Google
     * Maps)
     */
    private void applyNightModeColors() {
        int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
        int nightBlueAccent = getResources().getColor(R.color.night_blue_accent, null);

        // Textos principales en azul noche
        TextView tvFrequency = findViewById(R.id.tvFrequency);
        TextView tvRdsName = findViewById(R.id.tvRdsName);
        TextView tvRdsInfo = findViewById(R.id.tvRdsInfo);

        // V5.1: Logic moved to updateFrequencyDisplay for freq color to respect
        // favorites
        // But we refresh it here to be immediate
        int currentFreq = mLastFreq;
        if (currentFreq != -1)
            updateFrequencyDisplay(currentFreq);

        if (tvRdsName != null)
            tvRdsName.setTextColor(nightBlue);
        if (tvRdsInfo != null)
            tvRdsInfo.setTextColor(nightBlue);
        if (tvPty != null)
            tvPty.setTextColor(nightBlue);

        // V4.0: Icono de banda FM (Layout V3) y texto MHz
        ImageView ivBandIndicator = findViewById(R.id.ivBandIndicator);
        if (ivBandIndicator != null) {
            ivBandIndicator.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // Logic for UnitLabel, FavoriteIndicator, SignalLevel moved to
        // updateFrequencyDisplay/refreshRadioStatus

        // V14.3: Tintado de iconos de estado y PTY
        if (ivStereoIcon != null) {
            ivStereoIcon.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (ivPtyIcon != null) {
            ivPtyIcon.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (ivAfIcon != null) {
            ivAfIcon.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (ivTaIcon != null) {
            ivTaIcon.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (ivTpIcon != null) {
            ivTpIcon.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // Iconos en azul noche (tint)
        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnFavPrev, R.id.btnFavNext, R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2
        };

        for (int id : buttonIds) {
            ImageButton btn = findViewById(id);
            if (btn != null) {
                btn.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        // Presets en azul noche
        for (int i = 1; i <= 12; i++) {
            int tvId = getResources().getIdentifier("tvP" + i, "id", getPackageName());
            TextView tv = findViewById(tvId);
            if (tv != null)
                tv.setTextColor(nightBlue);
        }
    }

    /**
     * Restaura colores originales cuando se sale del modo nocturno
     */
    private void resetNightModeColors() {
        int white = getResources().getColor(R.color.white, null);

        // Restaurar textos a blanco
        TextView tvFrequency = findViewById(R.id.tvFrequency);
        TextView tvRdsName = findViewById(R.id.tvRdsName);
        TextView tvRdsInfo = findViewById(R.id.tvRdsInfo);

        // V5.1 refresh freq color
        int currentFreq = mLastFreq;
        if (currentFreq != -1)
            updateFrequencyDisplay(currentFreq);

        if (tvRdsName != null)
            tvRdsName.setTextColor(white);
        if (tvRdsInfo != null)
            tvRdsInfo.setTextColor(white);
        if (tvPty != null)
            tvPty.setTextColor(white);

        // V4.0: Restaurar icono de banda FM y texto MHz
        ImageView ivBandIndicator = findViewById(R.id.ivBandIndicator);
        if (ivBandIndicator != null) {
            ivBandIndicator.clearColorFilter();
        }

        // V5.1: Restaurar icono favorito
        ImageView ivFavoriteIndicator = findViewById(R.id.ivFavoriteIndicator);
        if (ivFavoriteIndicator != null) {
            ivFavoriteIndicator.clearColorFilter();
        }

        if (ivUnitLabel != null) {
            ivUnitLabel.clearColorFilter();
        }

        // Quitar tint de iconos
        if (ivStereoIcon != null) ivStereoIcon.clearColorFilter();
        if (ivPtyIcon != null) ivPtyIcon.clearColorFilter();
        if (ivAfIcon != null) ivAfIcon.clearColorFilter();
        if (ivTaIcon != null) ivTaIcon.clearColorFilter();
        if (ivTpIcon != null) ivTpIcon.clearColorFilter();

        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnFavPrev, R.id.btnFavNext, R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2
        };

        for (int id : buttonIds) {
            ImageButton btn = findViewById(id);
            if (btn != null) {
                btn.clearColorFilter();
            }
        }

        // Restaurar presets a blanco
        for (int i = 1; i <= 12; i++) {
            int tvId = getResources().getIdentifier("tvP" + i, "id", getPackageName());
            TextView tv = findViewById(tvId);
            if (tv != null)
                tv.setTextColor(white);
        }
    }


    // V4: Frequency Step Helpers (Manual Tuning)
    private void stepFreqUp() {
        mCurrentPty = null;
        if (mEngine == null) return;
        int current = mEngine.getCurrentFreq();
        int band = mEngine.getCurrentBand();
        boolean isAm = (band == BAND_AM1 || band == BAND_AM2);

        int newFreq;
        if (isAm) {
            newFreq = current + 9;
            if (newFreq > 1620) newFreq = 522;
        } else {
            // V12.3: Paso de 100 kHz (0.1 MHz) estándar en Europa, evita el parpadeo de 0.05
            newFreq = current + 100;
            if (newFreq > 108000) newFreq = 87500;
        }
        mEngine.tune(newFreq);
    }

    private void stepFreqDown() {
        mCurrentPty = null;
        if (mEngine == null) return;
        int current = mEngine.getCurrentFreq();
        int band = mEngine.getCurrentBand();
        boolean isAm = (band == BAND_AM1 || band == BAND_AM2);

        int newFreq;
        if (isAm) {
            newFreq = current - 9;
            if (newFreq < 522) newFreq = 1620;
        } else {
            // V12.3: Paso de 100 kHz (0.1 MHz)
            newFreq = current - 100;
            if (newFreq < 87500) newFreq = 108000;
        }
        mEngine.tune(newFreq);
    }

    // V4: Swipe Listener Class
    private static class OnSwipeTouchListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final int SWIPE_THRESHOLD = 80;
            private static final int SWIPE_VELOCITY_THRESHOLD = 80;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0)
                            onSwipeRight();
                        else
                            onSwipeLeft();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                onScrollEvent(-distanceX); // Negative because swipe left is positive distanceX
                return true;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onScrollEvent(float distanceX) {
        }
    }

    // V4: Automatic Night Mode
    public void checkAndApplyNightMode() {
        boolean autoNight = mPrefs.getBoolean("pref_night_mode_auto", false);
        if (!autoNight)
            return;

        if (isNightTime()) {
            com.example.openradiofm.ui.theme.ThemeManager themeManager = new com.example.openradiofm.ui.theme.ThemeManager(
                    this);
            themeManager.setSkin(com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
            applySkin(com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
        }
    }

    private boolean isNightTime() {
        // 1. Check System UI Mode
        int nightModeFlags = getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            return true;
        } else {
            // 2. Fallback: Time Based (configurable, default 19h-7h)
            int startHour = mPrefs.getInt("pref_night_start", 19);
            int endHour = mPrefs.getInt("pref_night_end", 7);
            int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
            if (startHour > endHour) {
                // Overnight range (e.g. 19-7)
                return (hour >= startHour || hour < endHour);
            } else {
                // Same-day range (e.g. 22-23)
                return (hour >= startHour && hour < endHour);
            }
        }
    }

    private void addToHistory(int freq) {
        if (freq <= 0)
            return;
        String historyStr = mPrefs.getString("pref_station_history", "");
        java.util.List<String> history = new java.util.ArrayList<>();

        if (!historyStr.isEmpty()) {
            history.addAll(java.util.Arrays.asList(historyStr.split(",")));
        }

        String freqStr = String.valueOf(freq);
        if (history.contains(freqStr)) {
            history.remove(freqStr);
        }
        history.add(0, freqStr);

        // Limit to 15
        if (history.size() > 15) {
            history = history.subList(0, 15);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(history.get(i));
            if (i < history.size() - 1)
                sb.append(",");
        }
        mPrefs.edit().putString("pref_station_history", sb.toString()).apply();
        Log.d(TAG, "History updated: " + sb.toString());
    }


    /**
     * V4.0: Muestra diálogo para guardar/cargar favoritos
     */
    /**
     * V4.1: Muestra diálogo para guardar/cargar favoritos usando diseño unificado
     */


    /**
     * Guarda los favoritos actuales en un archivo .fav en la carpeta RadioLogos
     */
    public void saveFavoritesToFile() {
        try {
            java.io.File radioLogosDir = new java.io.File(android.os.Environment.getExternalStorageDirectory(),
                    "RadioLogos");
            if (!radioLogosDir.exists()) {
                radioLogosDir.mkdirs();
            }

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            java.io.File favFile = new java.io.File(radioLogosDir, "favoritos_" + timestamp + ".fav");

            org.json.JSONObject jsonRoot = new org.json.JSONObject();
            org.json.JSONArray presetsArray = new org.json.JSONArray();

            for (int band = 0; band < 3; band++) {
                for (int i = 1; i <= 12; i++) {
                    String key = "P" + i + "_B" + band;
                    int freq = mPrefs.getInt(key, 0);

                    if (freq > 0) {
                        org.json.JSONObject presetObj = new org.json.JSONObject();
                        presetObj.put("preset", i);
                        presetObj.put("band", band);
                        presetObj.put("frequency", freq);

                        // Obtener nombre custom del repositorio (que usa RadioStationNames prefs)
                        com.example.openradiofm.data.model.RadioStation s = mRepository.getStationInfo(freq, null);
                        if (s != null && s.getName() != null && !s.getName().isEmpty()) {
                            presetObj.put("custom_name", s.getName());
                        }

                        presetsArray.put(presetObj);
                    }
                }
            }

            jsonRoot.put("presets", presetsArray);
            jsonRoot.put("version", "1.0");
            jsonRoot.put("timestamp", timestamp);

            java.io.FileWriter writer = new java.io.FileWriter(favFile);
            writer.write(jsonRoot.toString(2));
            writer.close();

            showStyledToast(String.format(getString(R.string.favorites_saved), favFile.getName()));

        } catch (Exception e) {
            showStyledToast(String.format(getString(R.string.error_saving_favorites), e.getMessage()));
            android.util.Log.e("SaveFavorites", "Error", e);
        }
    }

    /**
     * Carga favoritos desde un archivo .fav en la carpeta RadioLogos
     */
    public void loadFavoritesFromFile() {
        try {
            java.io.File radioLogosDir = new java.io.File(android.os.Environment.getExternalStorageDirectory(),
                    "RadioLogos");
            if (!radioLogosDir.exists() || !radioLogosDir.isDirectory()) {
                showStyledToast(getString(R.string.folder_not_found));
                return;
            }

            java.io.File[] favFiles = radioLogosDir.listFiles((dir, name) -> name.endsWith(".fav"));

            if (favFiles == null || favFiles.length == 0) {
                showStyledToast(getString(R.string.no_favorites_files));
                return;
            }

            java.util.Arrays.sort(favFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            String[] fileNames = new String[favFiles.length];
            for (int i = 0; i < favFiles.length; i++) {
                fileNames[i] = favFiles[i].getName();
            }

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.select_favorites_file));

            final java.io.File[] finalFavFiles = favFiles;
            builder.setItems(fileNames, (dialog, which) -> {
                loadFavoritesFromSpecificFile(finalFavFiles[which]);
            });

            builder.setNegativeButton(getString(R.string.cancel), null);
            builder.show();

        } catch (Exception e) {
            showStyledToast(String.format(getString(R.string.error_loading_favorites), e.getMessage()));
            android.util.Log.e("LoadFavorites", "Error", e);
        }
    }

    /**
     * Carga favoritos desde un archivo específico
     */
    private void loadFavoritesFromSpecificFile(java.io.File favFile) {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(favFile));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            org.json.JSONObject jsonRoot = new org.json.JSONObject(jsonString.toString());
            org.json.JSONArray presetsArray = jsonRoot.getJSONArray("presets");

            android.content.SharedPreferences.Editor editor = mPrefs.edit();
            for (int band = 0; band < 3; band++) {
                for (int i = 1; i <= 12; i++) {
                    editor.remove("P" + i + "_B" + band);
                }
            }

            int loadedCount = 0;
            for (int i = 0; i < presetsArray.length(); i++) {
                org.json.JSONObject presetObj = presetsArray.getJSONObject(i);
                int presetNum = presetObj.getInt("preset");
                int freq = presetObj.getInt("frequency");
                int band = presetObj.optInt("band", 0);

                editor.putInt("P" + presetNum + "_B" + band, freq);

                if (presetObj.has("custom_name")) {
                    String customName = presetObj.getString("custom_name");
                    mRepository.setCustomName(freq, customName);
                }

                loadedCount++;
            }

            editor.apply();
            refreshPresetButtons();

            showStyledToast(String.format(getString(R.string.favorites_loaded), loadedCount, favFile.getName()));

        } catch (Exception e) {
            showStyledToast(String.format(getString(R.string.error_loading_favorites), e.getMessage()));
            android.util.Log.e("LoadFavorites", "Error", e);
        }
    }

    /**
     * V4.0: Aplica el idioma seleccionado por el usuario.
     * Ya no necesitamos actualizar Resources manualmente aquí,
     * MyContextWrapper lo hace en attachBaseContext al recrear.
     */
    private void setLocale(String languageCode) {
        mPrefs.edit().putString("app_language", languageCode).apply();
    }

    /**
     * V4.0: Muestra diálogo para seleccionar idioma
     */
    private void showLanguageSelector() {
        String[] languages = {
                getString(R.string.language_spanish),
                getString(R.string.language_english),
                getString(R.string.language_russian),
                getString(R.string.language_romanian),
                getString(R.string.language_ukrainian),
                getString(R.string.language_serbian),
                getString(R.string.language_french),
                getString(R.string.language_chinese),
                getString(R.string.language_japanese)
        };

        String[] languageCodes = { "es", "en", "ru", "ro", "uk", "sr", "fr", "zh", "ja" };
        String currentLang = mPrefs.getString("app_language", "es");

        int selectedIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                selectedIndex = i;
                break;
            }
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_language));
        builder.setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
            String selectedLang = languageCodes[which];
            String selectedLangName = languages[which];

            setLocale(selectedLang);
            showStyledToast(String.format(getString(R.string.language_changed), selectedLangName));

            dialog.dismiss();
            recreate();
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        
        android.app.AlertDialog dialog = builder.create();
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.7f);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#E6121212")));
        }
        dialog.show();
    }

    /**
     * V4.0: Actualiza el texto del idioma actual en el menú Premium
     */
    public void updateCurrentLanguageText(TextView tvCurrentLanguage) {
        if (tvCurrentLanguage == null)
            return;

        String currentLang = mPrefs.getString("app_language", "es");
        String langName;

        switch (currentLang) {
            case "en":
                langName = getString(R.string.language_english);
                break;
            case "ru":
                langName = getString(R.string.language_russian);
                break;
            case "ro":
                langName = getString(R.string.language_romanian);
                break;
            case "uk":
                langName = getString(R.string.language_ukrainian);
                break;
            case "sr":
                langName = getString(R.string.language_serbian);
                break;
            default:
                langName = getString(R.string.language_spanish);
                break;
        }

        tvCurrentLanguage.setText(langName);
    }

    // PERMISSION HANDLING
    public boolean checkStoragePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int write = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            return write == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    read == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void requestStoragePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[] {
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                showToast("Permisos concedidos. Probando acceso...");
                if (mDialogManager != null) mDialogManager.showSaveLoadFavoritesDialog();
                showToast("Se requieren permisos de almacenamiento para guardar favoritos.");
            }
        }
    }

    // V15: Wrappers de compatibilidad para diálogos llamados desde otras clases
    public void showEngineSelector() {
        if (mDialogManager != null) mDialogManager.showEngineSelector();
    }

    private void setMute(boolean mute) {
        mMuteState = mute;
        
        // V11: Via RadioEngine
        if (mEngine != null) {
            mEngine.setMute(mute);
        }

        android.media.AudioManager am = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            // Using setStreamMute (deprecated but effective for simple needs) or
            // adjustStreamVolume
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        mute ? android.media.AudioManager.ADJUST_MUTE : android.media.AudioManager.ADJUST_UNMUTE, 0);
            } else {
                am.setStreamMute(android.media.AudioManager.STREAM_MUSIC, mute);
            }
        }
    }

    private void onSeekUpEvent() {
        if (mEngine != null) mEngine.seekUp();
    }

    private void onSeekDownEvent() {
        if (mEngine != null) mEngine.seekDown();
    }

    private void gotoFreq(int freq) {
        mCurrentPty = null; // V5.2: Reset PTY on tune
        if (mEngine != null) mEngine.tune(freq);
    }

    /**
     * V4.3: Helper to check if a frequency is stored in presets
     */
    private boolean isStationMemorized(int freq) {
        if (mPresetManager == null) return false;
        
        for (int i = 0; i < PRESETS_COUNT; i++) {
            if (mPresetManager.getFreq(i) == freq) return true;
        }
        return false;
    }

    /**
     * V4.3: Helper to get the 1-based index of a preset frequency
     */
    private int getPresetIndex(int freq) {
        if (mPresetManager == null) return 0;
        
        for (int i = 0; i < PRESETS_COUNT; i++) {
            if (mPresetManager.getFreq(i) == freq) return i + 1;
        }
        return 0;
    }


    /**
     * V11.7: Muestra el diálogo de Escaneo Selectivo (Solo K706).
     */
    /**
     * V9.5: AutoScan Toggle — click 1 inicia, click 2 detiene.
     */
    private void toggleAutoScan(ImageButton btn) {
        if (mEngine == null) return;
        if (!mIsScanning) {
            mEngine.scan();
            mIsScanning = true;
            if (btn != null) {
                btn.setColorFilter(android.graphics.Color.parseColor("#00E676"),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            }
            showToast("AutoScan iniciado...");
        } else {
            mEngine.stopScan();
            mIsScanning = false;
            if (btn != null) {
                btn.clearColorFilter();
            }
            showToast("AutoScan detenido");
        }
    }

    private void promptAutoScan() {
        if (mEngine != null) mEngine.scan();
    }

    // === V4.5: QFTunerManager & MCU Helpers para Settings Premium ===

    private Object mCachedQFTunerManager;
    private boolean mQFTunerChecked = false;

    private Object getQFTunerManager() {
        if (!mQFTunerChecked) {
            mQFTunerChecked = true;
            try {
                Class<?> clazz = Class.forName("com.qf.clientsdk.QFTunerManager");
                java.lang.reflect.Method getInstance = clazz.getMethod("getInstance");
                mCachedQFTunerManager = getInstance.invoke(null);
            } catch (Exception e) {
                // Not available
            }
        }
        return mCachedQFTunerManager;
    }

    private boolean invokeQFTuner(String methodName, Class<?> paramType, Object arg) {
        Object mgr = getQFTunerManager();
        if (mgr == null) return false;
        try {
            java.lang.reflect.Method m = mgr.getClass().getMethod(methodName, paramType);
            m.invoke(mgr, arg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean invokeQFTunerNoArg(String methodName) {
        Object mgr = getQFTunerManager();
        if (mgr == null) return false;
        try {
            java.lang.reflect.Method m = mgr.getClass().getMethod(methodName);
            m.invoke(mgr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendMcuTunerCmd(byte subCmd, byte param1, byte param2) {
        if (mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
            com.example.openradiofm.data.source.K706RadioManager mgr = 
                ((com.example.openradiofm.data.source.K706Engine) mEngine).getManager();
            if (mgr != null) {
                try {
                    java.lang.reflect.Method sendCmd = com.example.openradiofm.data.source.K706RadioManager.class.getDeclaredMethod(
                        "sendCmd", byte.class, byte.class, byte.class);
                    sendCmd.setAccessible(true);
                    sendCmd.invoke(mgr, subCmd, param1, param2);
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "sendMcuTunerCmd error", e);
                }
            }
        }
    }

}
