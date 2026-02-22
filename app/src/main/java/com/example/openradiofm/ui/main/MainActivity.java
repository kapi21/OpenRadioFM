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
import com.example.openradiofm.utils.PtyManager; // V5.0
import com.example.openradiofm.utils.MetadataUtils;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.hcn.autoradio.IRadioServiceAPI;
import com.hcn.autoradio.IRadioCallBack;
import com.example.openradiofm.data.source.HiddenRadioPlayer;
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
public class MainActivity extends AppCompatActivity {

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
     * - FM_COMPLETO: dispositivo con root + servicio especial del coche disponible.
     * - FM_BASICO: sin root o sin servicio; solo frecuencia + logos en SD (y, más
     * adelante, nombres manuales).
     */
    private enum FmMode {
        FM_MT8163, // Ahora usamos FM_MT8163 en lugar de FM_COMPLETO para claridad
        FM_BASICO,
        FM_K706
    }

    IRadioServiceAPI mRadioService;
    com.example.openradiofm.data.repository.RadioRepository mRepository;
    android.content.SharedPreferences mPrefs;
    HiddenRadioPlayer mHiddenPlayer;

    // V3.0: Caché de logos por banda
    int mLastFreq = -1;
    boolean mHasRdsLock = false;
    String mCurrentPty = null; // V5.2: Live PTY persistence
    private String mLastLogoUrl = "";
    private java.util.Map<String, String> mLogoCachePerBand = new java.util.HashMap<>();

    // V5.0: UI Elements (Fixing Compilation Errors)
    private TextView tvPty;
    private ImageView ivSignalLevel;
    private ImageView ivPtyIcon; // V5.0: Categorical Icon
    private ImageView ivAfIcon, ivTaIcon, ivTpIcon; // RDS Status Icons

    // V5.0: State & Presets
    private boolean mMuteState = false;
    private com.example.openradiofm.ui.theme.ThemeManager.Skin mCurrentSkin = com.example.openradiofm.ui.theme.ThemeManager.Skin.CLASSIC_GRAY;
    private int[] mPresets = new int[PRESETS_COUNT]; // Using constant for size

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

    // V3.0: Background personalizado
    private android.view.View mRootLayout;

    private TextView tvFrequency, tvRdsName, tvRdsInfo;
    private android.view.View boxFrequency;
    private ImageView ivBandIndicator, ivUnitLabel, ivFavoriteIndicator, ivStereoIcon;
    private ImageButton btnLocDx, btnBand;

    private final android.view.View[] cardPresets = new android.view.View[PRESETS_COUNT];
    private final TextView[] tvPresets = new TextView[PRESETS_COUNT];
    private final ImageView[] ivPresets = new ImageView[PRESETS_COUNT];

    private int mCurrentBand = 0;

    private FmMode mMode = FmMode.FM_BASICO;
    private boolean mIsScanning = false; // V9.5: Estado de AutoScan toggle
    private boolean mIsV3 = false; // V5.4: Track Layout 3 active

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
                mRadioService.registerRadioCallback(mCallback);

                // Fix v4.5.1: Obtener banda real INMEDIATAMENTE antes de cargar presets
                try {
                    mCurrentBand = mRadioService.getCurrentBand();
                } catch (Exception ignored) {
                }

                startStatusPolling();
                showToast("Conexión Establecida");

                // V5.7: Immediate UI refresh after recreation if we have a stored frequency
                refreshPresetsCache();
                runOnUiThread(() -> {
                    refreshPresetButtons();
                    if (mLastFreq != -1) {
                        updateFrequencyDisplay(mLastFreq);
                    }
                });

                // Solo inicializamos el listener oculto de RDS en modo MT8163 (completo).
                if (mMode == FmMode.FM_MT8163) {
                    initHiddenPlayer();
                }
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
                
                if (mRadioService != null && mRadioService instanceof com.example.openradiofm.data.source.K706RadioManager) {
                    com.example.openradiofm.data.source.K706RadioManager k706Manager = 
                        (com.example.openradiofm.data.source.K706RadioManager) mRadioService;
                    
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

    private void initHiddenPlayer() {
        mHiddenPlayer = new HiddenRadioPlayer(new HiddenRadioPlayer.Listener() {
            @Override
            public void onRdsText(String text) {
                String cleanedText = MetadataUtils.cleanRdsText(text);
                runOnUiThread(() -> {
                    if (tvRdsInfo != null) {
                        String current = tvRdsInfo.getText().toString();
                        if (!current.equals(cleanedText)) {
                            tvRdsInfo.setText(cleanedText);
            tvRdsInfo.setSelected(true); // V9: Enable marquee
            if (cleanedText == null || cleanedText.trim().isEmpty()) {
                                // V5.0: Keep visible in V2 to prevent shifts
                                tvRdsInfo.setVisibility(mIsV3 ? View.GONE : View.VISIBLE);
                            } else {
                                tvRdsInfo.setVisibility(View.VISIBLE);
                            }
                        }
                        mHasRdsLock = (cleanedText != null && !cleanedText.isEmpty());
                    }
                });
            }

            @Override
            public void onRdsName(String name) {
                runOnUiThread(() -> {
                    if (tvRdsName != null && name != null && !name.isEmpty()) {
                        tvRdsName.setText(name);
                        tvRdsName.setVisibility(View.VISIBLE);
                        mHasRdsLock = true;
                    }
                });
            }

            @Override
            public void onRdsPty(String pty) {
                runOnUiThread(() -> {
                    mCurrentPty = pty;
                    updatePtyUI(pty); // V5.0: Update Icon & Text
                });
            }

            @Override
            public void onRawEvent(int code, Object info, String str) {
                // Posibilidad de loguear eventos desconocidos para depuración
                if (code == HiddenRadioPlayer.EVENT_PS_DONE) {
                    mHasRdsLock = true;
                }
            }
        });
        if (!mHiddenPlayer.init()) {
            Log.e(TAG, "Error RDS Hardware Init");
        } else {
            // V5.0: Forzar estéreo nada más iniciar para mejorar sensibilidad
            mHiddenPlayer.setStereo(true);
        }
    }

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

    // Callback mínimo para eventos del servicio de radio. De momento no se usa,
    // pero es importante desregistrarlo en onDestroy() para no filtrar la Activity.
    // Callback mínimo para eventos del servicio de radio.
    private final IRadioCallBack mCallback = new IRadioCallBack.Stub() {
        @Override
        public void onEvent(int code, String data) {
            runOnUiThread(() -> {
                switch(code) {
                    case 34: // Legacy PTY (0x22)
                    case 102: // New PTY from K706Manager
                        mCurrentPty = data;
                        updatePtyUI(data);
                        // V9.9: Persist PTY in repository
                        if (mRepository != null && mLastFreq > 0) {
                            mRepository.saveRdsPty(mLastFreq, data);
                        }
                        break;
                    case 100: // RDS PS Name
                        if (tvRdsName != null) {
                            String rdsName = data.trim();
                            tvRdsName.setText(rdsName);
                            tvRdsName.setVisibility(View.VISIBLE);
                            mHasRdsLock = true;
                            
                            // V9: Persist RDS name for presets and refresh if needed
                            try {
                                if (mRadioService != null && mRepository != null) {
                                    int freq = mRadioService.getCurrentFreq();
                                    mRepository.saveRdsName(freq, rdsName);
                                    // If this frequency is in presets, refresh visuals
                                    for (int i = 0; i < PRESETS_COUNT; i++) {
                                        String key = "P" + (i + 1) + "_B" + mCurrentBand;
                                        if (mPrefs.getInt(key, 0) == freq) {
                                            updateCardVisuals(i, freq);
                                        }
                                    }
                                }
                            } catch (Exception e) {}
                        }
                        break;
                    case 101: // RDS RT
                        if (tvRdsInfo != null) {
                    String cleaned = MetadataUtils.cleanRdsText(data);
                    tvRdsInfo.setText(cleaned);
                    tvRdsInfo.setSelected(true); // V9: Enable marquee
                    tvRdsInfo.setVisibility(View.VISIBLE);
                }
                        break;
                    case 103: // Stereo Debug
                        boolean isStereo = "1".equals(data);
                        if (ivSignalLevel != null) {
                            // Update signal color based on stereo flag ("1" or "0")
                            int color = isStereo ? Color.parseColor("#00E676") : Color.parseColor("#FFD600");
                            ivSignalLevel.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                        if (ivStereoIcon != null) {
                            ivStereoIcon.setVisibility(isStereo ? View.VISIBLE : View.GONE);
                        }
                        break;
                    case 110: // Raw RDS byte dumping
                        if (mEngineeringDialog != null && mEngineeringDialog.isShowing()) {
                            mEngineeringDialog.addRdsLog(data);
                        }
                        break;
                    case 104: // Band ID from MCU
                        try {
                            int mcuBand = Integer.parseInt(data);
                            if (mcuBand != mCurrentBand) {
                                mCurrentBand = mcuBand;
                                refreshPresetsCache();
                                refreshPresetButtons();
                            }
                        } catch (NumberFormatException ignored) {}
                        break;
                    case 105: // V7.2: Frecuencia instantánea desde MCU callback (ya normalizada ×1000)
                        try {
                            int mcuFreq = Integer.parseInt(data);
                            // V7.2: Valor ya viene en formato ×1000 desde K706RadioManager
                            if (mcuFreq != mLastFreq) {
                                mLastFreq = mcuFreq;
                                updateFrequencyDisplay(mcuFreq);
                                Log.d(TAG, "MCU Freq Update: " + mcuFreq + " (" + String.format("%.2f", mcuFreq / 1000.0) + " MHz)");
                            }
                        } catch (NumberFormatException ignored) {}
                        break;
                    case 106: // V7.1: DX/Local toggle desde MCU
                        if (btnLocDx != null) {
                    boolean isLocal = "1".equals(data);
                    btnLocDx.setSelected(isLocal);
                    // V9: LOCAL=radio_loc_p (active/filled), DX=radio_loc_n (normal/outline)
                    btnLocDx.setImageResource(isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
                }
                        break;
                    case 111: // V9.9: RDS AF/TA/TP Status (0xB3/0xB4 parse)
                        if (data != null && !data.isEmpty()) {
                            // Example formats: "AF:1", "TA:1", "TP:0"
                            String[] parts = data.split(",");
                            for (String part : parts) {
                                String[] kv = part.split(":");
                                if (kv.length == 2) {
                                    String key = kv[0].trim();
                                    boolean active = "1".equals(kv[1].trim());

                                    if ("AF".equals(key) && ivAfIcon != null) {
                                        ivAfIcon.setAlpha(active ? 1.0f : 0.2f);
                                    } else if ("TA".equals(key) && ivTaIcon != null) {
                                        ivTaIcon.setAlpha(active ? 1.0f : 0.2f);
                                    } else if ("TP".equals(key) && ivTpIcon != null) {
                                        ivTpIcon.setAlpha(active ? 1.0f : 0.2f);
                                    }
                                }
                            }
                        }
                        break;
                }
            });
        }
    };

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

        if (mIsV3) {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // Optional: If we want to ensure it's not translucent
            // getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        setContentView(mIsV3 ? R.layout.activity_main_v3 : R.layout.activity_main);

        // V3.8: Premium Background Binding
        ivDynamicBackground = findViewById(R.id.ivDynamicBackground);

        if (checkSelfPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
        }

        // V2.0: Crear carpeta RadioLogos si no existe
        createRadioLogosFolder();

        // V2.0: Cargar fondo personalizado si existe
        loadCustomBackground();
        loadCarLogo(); // V3.9: Cargar logo marca coche

        // Determinar modo de funcionamiento (FM completo vs básico) antes de crear el
        // repositorio.
        mMode = detectMode();
        Log.d(TAG, "Modo de funcionamiento: " + mMode);

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
        if (ivAfIcon != null) ivAfIcon.setAlpha(0.2f);
        if (ivTaIcon != null) ivTaIcon.setAlpha(0.2f);
        if (ivTpIcon != null) ivTpIcon.setAlpha(0.2f);

        android.view.View boxLogo = findViewById(R.id.boxLogo);

        if (boxLogo != null) {
            boxLogo.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = new com.example.openradiofm.ui.theme.ThemeManager(
                        this).cycleSkin();
                applySkin(next);
                showToast("Skin: " + next.displayName);
            });
            boxLogo.setOnLongClickListener(v -> {
                showHistoryDialog();
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
                showHistoryDialog();
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

        // Presets Binding
        bindPresetViews();
        // Initial Refresh (will be updated again when band is fetched)
        refreshPresetButtons();

        setupRdsText();
        applyFonts();

        // V10: Custom User Names
        // V10: Custom User Names
        setupCustomNameEditing();

        // V8.5: Easter Egg (Credits) - Restored
        setupCreditsEasterEgg();

        // Conectamos con el servicio de radio del coche.
        conectarRadio();
    }

    /**
     * Configura los botones de control (EQ, Mute, Test, AutoScan, LOC/DX).
     */
    private void setupControlButtons() {
        // EQ Logic (V9.3: Direct DSP Intent)
        ImageButton btnEq = findViewById(R.id.btnSettings);
        if (btnEq != null) {
            btnEq.setOnClickListener(v -> {
                if (mMode == FmMode.FM_MT8163) {
                    sendMcuKey(0x134); // Keycode 308 for DSP in MT8163 (from v4.5)
                } else {
                    try {
                        // V9.4d: Abrir QF Sound Effect del K706 directamente
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.qf.soundeffect");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);
                        } else {
                            // Fallback: abrir ajustes de sonido de Android
                            Intent intent = new Intent("android.intent.action.MAIN");
                            intent.setClassName("com.android.settings", "com.android.settings.Settings$SoundSettingsActivity");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        showToast("No se pudo abrir el DSP");
                    }
                }
            });

            // Long Click para abrir selector de skins
            btnEq.setOnLongClickListener(v -> {
                showPremiumSettingsDialog();
                return true;
            });
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
        }

        // LOC/DX Switch
        btnLocDx = findViewById(R.id.btnLocDx);
        if (btnLocDx != null) {
            btnLocDx.setOnClickListener(v -> {
                if (mMode == FmMode.FM_MT8163) {
                    execRemote(IRadioServiceAPI::onLocDxEvent);
                } else {
                    // V9.8: Función LOC/DX desactivada temporalmente para K706 (Standby)
                    showToast("LOC / DX (Standby en K706 - En investigación)");
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

        // V4.0: Extra Button 1 - Android Settings
        ImageButton btnExtra1 = findViewById(R.id.btnExtra1);
        if (btnExtra1 != null) {
            btnExtra1.setOnClickListener(v -> {
                try {
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settingsIntent);
                } catch (Exception e) {
                    showStyledToast(getString(R.string.error_opening_settings));
                }
            });
        }

        // V4.0: Extra Button 2 - Save/Load Favorites
        ImageButton btnExtra2 = findViewById(R.id.btnExtra2);
        if (btnExtra2 != null) {
            btnExtra2.setOnClickListener(v -> {
                showSaveLoadFavoritesDialog();
            });
        }
    }

    private void setupCustomNameEditing() {
        // Permitir editar el nombre al mantener pulsado el texto del nombre RDS
        if (tvRdsName != null) {
            // Click normal: Mostrar historial
            tvRdsName.setOnClickListener(v -> showHistoryDialog());

            // Long click: Editar nombre
            tvRdsName.setOnLongClickListener(v -> {
                showEditNameDialog();
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
                showHistoryDialog();
                return true;
            });
        }
    }

    private void showEditNameDialog() {
        if (mRadioService == null)
            return;
        try {
            int currentFreq = mRadioService.getCurrentFreq();

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Editar nombre de emisora");
            builder.setMessage("Frecuencia: " + String.format("%.1f MHz", currentFreq / 1000.0));

            final android.widget.EditText input = new android.widget.EditText(this);
            input.setSingleLine(true);

            // Pre-llenar con el nombre actual (si es custom o RDS)
            com.example.openradiofm.data.model.RadioStation s = mRepository.getStationInfo(currentFreq, null);
            if (s.getName() != null) {
                input.setText(s.getName());
                input.setSelectAllOnFocus(true);
            }

            builder.setView(input);

            builder.setPositiveButton("Guardar", (dialog, which) -> {
                String newName = input.getText().toString();
                // Guardar en repositorio (SharedPreferences)
                mRepository.setCustomName(currentFreq, newName);
                showToast("Nombre guardado");
                // Forzar refresco inmediato de UI
                refreshRadioStatus();
            });

            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

            // Botón para borrar nombre personalizado y volver al original/RDS
            builder.setNeutralButton("Restaurar Original", (dialog, which) -> {
                mRepository.setCustomName(currentFreq, null); // Null borra la entrada custom
                showToast("Nombre restaurado");
                refreshRadioStatus();
            });

            builder.show();
            input.requestFocus(); // Focus automático

        } catch (RemoteException e) {
            e.printStackTrace();
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

        // 2) Apagar el subsistema de hardware de radio (evitar que siga sonando de fondo)
        if (mRadioService != null) {
            if (mRadioService instanceof com.example.openradiofm.data.source.K706RadioManager) {
                try {
                    ((com.example.openradiofm.data.source.K706RadioManager) mRadioService).closeDevice();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        // 1. Prioridad: K706 (mcu_service nativo)
        if (isK706()) {
            return FmMode.FM_K706;
        }

        // 2. Prioridad: MT8163 (Servicio de radio del sistema)
        if (hasCarRadioService()) {
            return FmMode.FM_MT8163;
        }
        
        // 3. Fallback: Modo básico
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
            // Fallback: Check system property usually present on these units
            /*
            String platform = android.os.SystemProperties.get("ro.board.platform", "");
            if (platform.contains("mt8163")) return true;
            */
        } catch (Exception e) {}
        return false;
    }

    /**
     * V3.8: Aplica la tipografía seleccionada a los elementos clave.
     * Busca los archivos en res/font (ej: bebas.ttf, digital.ttf, inter.ttf)
     */
    private void applyFonts() {
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
        for (int i = 0; i < tvPresets.length; i++) {
            if (tvPresets[i] != null)
                tvPresets[i].setTypeface(typeface);
        }
    }

    private void setupRdsText() {
        // V8.3: Enable Marquee on RDS Info
        if (tvRdsInfo != null) {
            tvRdsInfo.setText(""); // Start Empty
            tvRdsInfo.setSelected(true); // Required for Marquee
            tvRdsInfo.setSingleLine(true);
            tvRdsInfo.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
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
                    showCreditsDialog();
                }
            });
        }
    }

    private void showCreditsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("About OpenRadioFM");
        builder.setMessage("OpenRadioFM v4.0\\n\\nDesarrollada por Jimmy80\\n(Febrero 2026)");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // V3.0: Toggle Layout Button REMOVED (Moved to LOC/DX Long Press)
        /*
         * builder.setNeutralButton("Switch Layout (v3)", (dialog, which) -> {
         * boolean current = mPrefs.getBoolean("pref_layout_v3", false);
         * mPrefs.edit().putBoolean("pref_layout_v3", !current).apply();
         * showToast("Layout cambiado. Reiniciando...");
         * recreate();
         * });
         */

        android.app.AlertDialog dialog = builder.create();

        // V5.0: Apply premium styling
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.7f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
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
    private void loadCustomBackground() {
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
     * V3.9: Carga el logo de la marca del coche si existe en
     * /sdcard/RadioLogos/car_logo.png
     * Se coloca en el hueco derecho del layout V3 (ivCarLogo) y en el central del
     * V2 (ivMainLogo).
     */
    private void loadCarLogo() {
        // Layout V3
        ImageView ivCarLogo = findViewById(R.id.ivCarLogo);
        // Layout V2
        ImageView ivMainLogo = findViewById(R.id.ivMainLogo);

        java.io.File logoFile = new java.io.File("/sdcard/RadioLogos/car_logo.png");
        boolean logoExists = logoFile.exists();

        // Logic for Layout V3
        if (ivCarLogo != null) {
            if (logoExists) {
                ivCarLogo.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(logoFile)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivCarLogo);
            } else {
                ivCarLogo.setVisibility(View.INVISIBLE);
            }
        }

        // Logic for Layout V2 (Standardization)
        if (ivMainLogo != null) {
            if (logoExists) {
                // If custom car logo exists, use it
                Glide.with(this)
                        .load(logoFile)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivMainLogo);
            } else {
                // If no custom logo, revert to app icon
                ivMainLogo.setImageResource(R.mipmap.ic_launcher);
            }
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
        ImageButton btnSeekDownV3 = findViewById(R.id.btnSeekDown); // LEFT (<)
        ImageButton btnSeekUpV3 = findViewById(R.id.btnSeekUp); // RIGHT (>)

        // Left Button (<) -> Step Down / Seek Down (Long Press)
        if (btnSeekDownV3 != null) {
            btnSeekDownV3.setOnClickListener(v -> stepFreqDown());
            // Standard: Long Left = Seek Down
            btnSeekDownV3.setOnLongClickListener(v -> {
                onSeekDownEvent();
                return true;
            });
        }

        // Right Button (>) -> Step Up / Seek Up (Long Press)
        if (btnSeekUpV3 != null) {
            btnSeekUpV3.setOnClickListener(v -> stepFreqUp());
            // Standard: Long Right = Seek Up
            btnSeekUpV3.setOnLongClickListener(v -> {
                onSeekUpEvent();
                return true;
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
                execRemote(IRadioServiceAPI::onBandEvent);
            });
        }
    }

    private void bindPresetViews() {
        // V4: Dynamic binding for 15 presets (P1-P15)
        for (int i = 0; i < PRESETS_COUNT; i++) {
            int index = i + 1;
            int cardId = getResources().getIdentifier("cardP" + index, "id", getPackageName());
            int tvId = getResources().getIdentifier("tvP" + index, "id", getPackageName());
            int ivId = getResources().getIdentifier("ivP" + index, "id", getPackageName());

            if (cardId != 0)
                cardPresets[i] = findViewById(cardId);
            if (tvId != 0)
                tvPresets[i] = findViewById(tvId);
            if (ivId != 0)
                ivPresets[i] = findViewById(ivId);
        }
    }

    public void refreshPresetButtons() {
        for (int i = 0; i < PRESETS_COUNT; i++) {
            String key = "P" + (i + 1) + "_B" + mCurrentBand;
            setupPresetCard(i, key);
        }
    }

    private void setupPresetCard(int index, String key) {
        if (cardPresets[index] == null)
            return; // Safety check
        int savedFreq = mPrefs.getInt(key, 0);
        updateCardVisuals(index, savedFreq);

        cardPresets[index].setOnClickListener(v -> {
            animateButton(v); // V4
            int freq = mPrefs.getInt(key, 0);
            if (freq > 0) {
                execRemote(s -> s.gotoFreq(freq));
            } else {
                showToast("Vacío - Mantén para guardar");
            }
        });

        cardPresets[index].setOnLongClickListener(v -> {
            animateButton(v); // V4
            if (mRadioService != null) {
                try {
                    int current = mRadioService.getCurrentFreq();
                    mPrefs.edit().putInt(key, current).apply();
                    updateCardVisuals(index, current);
                    // showToast("Guardado en B" + (mCurrentBand + 1)); // V8.4: Disabled toast
                } catch (RemoteException e) {
                }
            }
            return true;
        });
    }

    private int getSkinDrawableId() {
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

    private void updateCardVisuals(int index, int freq) {
        if (freq == 0) {
            if (tvPresets[index] != null) {
                tvPresets[index].setText("Empty");
                tvPresets[index].setVisibility(View.VISIBLE);
            }
            if (ivPresets[index] != null) {
                ivPresets[index].setImageDrawable(null);
                ivPresets[index].setBackground(null); 
            }
            // Restore borders/background for the container (Preserving skin)
            if (cardPresets[index] != null) cardPresets[index].setBackgroundResource(getSkinDrawableId());
            return;
        }

        // V9: No placeholder/background behind logos
        if (ivPresets[index] != null) {
            ivPresets[index].setImageDrawable(null); // Truly empty if no logo
            ivPresets[index].setBackground(null); 
        }
        // Restore borders/background for the container (Preserving skin)
        if (cardPresets[index] != null) cardPresets[index].setBackgroundResource(getSkinDrawableId());

        // V9: Logic for Name/Freq display
        if (tvPresets[index] != null) {
            tvPresets[index].setVisibility(View.VISIBLE);
            
            // Priority: getStationInfo handles Custom > RDS PS > RDS Root
            com.example.openradiofm.data.model.RadioStation s = mRepository.getStationInfo(freq, null);
            String displayName = s.getName();
            
            if (displayName != null && !displayName.isEmpty()) {
                tvPresets[index].setText(displayName);
            } else {
                tvPresets[index].setText(String.format(java.util.Locale.US, "%.1f", freq / 1000.0));
            }
        }

        // Async Fetch Logo / Custom Info
        mRepository.getStationInfo(freq, logoUrl -> {
            runOnUiThread(() -> {
                if (logoUrl != null && ivPresets[index] != null) {
                    Glide.with(MainActivity.this)
                            .load(logoUrl)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(ivPresets[index]);
                }
            });
        });
    }

    private interface RemoteAction {
        void run(IRadioServiceAPI s) throws RemoteException;
    }

    private void execRemote(RemoteAction action) {
        if (mRadioService == null)
            return;
        try {
            action.run(mRadioService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void conectarRadio() {
        // V6.0: K706 Direct Connection
        if (mMode == FmMode.FM_K706) {
            try {
                mRadioService = new com.example.openradiofm.data.source.K706RadioManager(this);
                mRadioService.registerRadioCallback(mCallback);
                mCurrentBand = mRadioService.getCurrentBand();
                startStatusPolling();
                showToast("Motor K706 Activado (Directo)");
                
                // V6.1: Restore state to avoid reset to 87.5 on rotation/layout change
                if (mRadioService instanceof com.example.openradiofm.data.source.K706RadioManager) {
                     int safeFreq = (mLastFreq > 0) ? mLastFreq : 87500; // Default if first run
                     // If band is unknown or 0 (FM1), keep it.
                     ((com.example.openradiofm.data.source.K706RadioManager)mRadioService).syncState(safeFreq, mCurrentBand);
                }

                // Init UI immediatelly
                refreshPresetsCache();
                refreshPresetButtons();
                refreshRadioStatus();
                return;
            } catch (Exception e) {
                showToast("Error iniciando K706: " + e.getMessage());
                // Fallback a detección de servicio si falla la instancia local
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
    private void refreshRadioStatus() {
        if (mLastFreq != -1) {
            runOnUiThread(() -> updateFrequencyDisplay(mLastFreq));
        }

        if (mRadioService == null)
            return;
        execRemote(s -> {
            int freq = s.getCurrentFreq();
            int band = s.getCurrentBand();
            boolean isStereo = s.IsStereo();
            boolean isLocal = s.IsDxLocal();

            // Fix v4.5.1: SIEMPRE sincronizar mCurrentBand y refrescar presets
            if (band != mCurrentBand) {
                mCurrentBand = band;
                refreshPresetsCache();
                runOnUiThread(() -> refreshPresetButtons());
            }

            // V4.3: Hardware Toggle for AM
            boolean amEnabled = mPrefs.getBoolean("pref_enable_am", true);
            boolean isAm = (band == BAND_AM1 || band == BAND_AM2);
            if (isAm && !amEnabled) {
                // Auto skip to next band
                execRemote(IRadioServiceAPI::onBandEvent);
                return;
            }

            // V5.0: Composite Signal Quality Logic
            // Raw SNR/RSSI is not available in SDK, so we infer quality
            // mCurrentQuality = calculateSignalQuality(isStereo, isLocal, mHasRdsLock); //
            // Legacy removed

            String bandCacheKey = band + "_" + freq;

            if (freq != mLastFreq) {
                mLastFreq = freq;
                mHasRdsLock = false; // Reset lock on manual change

                // V4.0 / V5.1: Clear RDS UI on Tune aggressively (User request)
                // When frequency changes, blank out RDS data until new data arrives
                mCurrentPty = null; // Clear live PTY
                
                runOnUiThread(() -> {
                    if (tvRdsName != null) {
                        tvRdsName.setText("");
                        // Keep visible in Layout 2 to prevent shift, GONE in V3
                        tvRdsName.setVisibility(mIsV3 ? View.GONE : View.VISIBLE);
                    }
                    if (tvRdsInfo != null) {
                        tvRdsInfo.setText("");
                        // Keep visible in Layout 2 to prevent shift, GONE in V3
                        tvRdsInfo.setVisibility(mIsV3 ? View.GONE : View.VISIBLE);
                    }
                    if (tvPty != null) {
                        tvPty.setText("Sin PTY");
                        if (ivPtyIcon != null) ivPtyIcon.setVisibility(View.GONE);
                    }
                });

                if (mPrefs.getBoolean("pref_save_history", true)) {
                    addToHistory(freq);
                }
            }

            com.example.openradiofm.data.model.RadioStation station = mRepository.getStationInfo(freq, null);
            String rdsName = station.getName();

            runOnUiThread(() -> {
                // Quality Indicator
                // updateQualityUI(mCurrentQuality); // Legacy removed

                // FIXED LOGIC: Freq formatting (MHz for FM, kHz for AM)
                if (isAm) {
                    tvFrequency.setText(String.valueOf(freq));
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
                    tvFrequency.setText(String.format(java.util.Locale.US, "%.2f", freq / 1000.0));
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

                // RDS Display logic moved to updateFrequencyDisplay
                updateFrequencyDisplay(freq);

                // V4.0: Logo & Background Logic (Independent for V3 support)
                String cachedLogo = mLogoCachePerBand.get(bandCacheKey);
                ImageView ivMainLogo = findViewById(R.id.ivMainLogo);

                if (cachedLogo != null) {
                    if (!cachedLogo.equals(mLastLogoUrl)) {
                        mLastLogoUrl = cachedLogo;
                        if (ivMainLogo != null && !mIsV3) {
                            ivMainLogo.setVisibility(View.VISIBLE);
                            Glide.with(MainActivity.this)
                                    .load(cachedLogo)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(ivMainLogo);
                        }
                        updateDynamicBackground(cachedLogo);
                    }
                } else {
                    mRepository.getStationInfo(freq, url -> {
                        runOnUiThread(() -> {
                            if (url != null) {
                                if (!url.equals(mLastLogoUrl)) {
                                    mLastLogoUrl = url;
                                    mLogoCachePerBand.put(bandCacheKey, url);
                                    if (ivMainLogo != null && !mIsV3) {
                                        ivMainLogo.setVisibility(View.VISIBLE);
                                        Glide.with(MainActivity.this)
                                                .load(url)
                                                .transition(DrawableTransitionOptions.withCrossFade())
                                                .into(ivMainLogo);
                                    }
                                    updateDynamicBackground(url);
                                }
                            } else {
                                mLastLogoUrl = "";
                                mLogoCachePerBand.remove(bandCacheKey);
                                if (ivMainLogo != null && !mIsV3) {
                                    ivMainLogo.setImageResource(R.mipmap.ic_launcher);
                                    ivMainLogo.setVisibility(View.VISIBLE);
                                }
                                updateDynamicBackground(null);
                            }
                        });
                    });
                }

                // Hide logo always in V3 if it somehow exists
                if (mIsV3 && ivMainLogo != null) {
                    ivMainLogo.setVisibility(View.GONE);
                }

                updateBandImage(band);
                if (btnLocDx != null) {
                    btnLocDx.setSelected(isLocal);
                    // V9: LOCAL=radio_loc_p, DX=radio_loc_n
                    btnLocDx.setImageResource(isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
                }
            });
            
            // V5.2: Send update intent to K706 Native system Widget
            sendWidgetUpdateIntent(freq, band, rdsName);
        });
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
    private void showStyledToast(String msg) {
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

    private void showToast(String msg) {
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

    // V3.0: Premium "Radio Interface" Dialog
    private void showPremiumSettingsDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium_settings);

        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        android.view.View cardTheme = dialog.findViewById(R.id.cardTheme);
        android.view.View cardFonts = dialog.findViewById(R.id.cardFonts);
        android.view.View cardBackground = dialog.findViewById(R.id.cardBackground);

        android.view.View viewColorPreview = dialog.findViewById(R.id.viewColorPreview);
        TextView tvFontPreview = dialog.findViewById(R.id.tvFontPreview);
        TextView tvBackgroundStatus = dialog.findViewById(R.id.tvBackgroundStatus);

        androidx.appcompat.widget.SwitchCompat swLogosOnline = dialog.findViewById(R.id.switchLogosOnline);
        androidx.appcompat.widget.SwitchCompat swNight = dialog.findViewById(R.id.switchNightMode);
        androidx.appcompat.widget.SwitchCompat swHistory = dialog.findViewById(R.id.switchSaveHistory);
        androidx.appcompat.widget.SwitchCompat swGestures = dialog.findViewById(R.id.switchSwipeGestures); // Changed
                                                                                                           // from
                                                                                                           // swSwipe to
                                                                                                           // swGestures

        // V4.0: Language Selector
        android.widget.LinearLayout rowLanguage = dialog.findViewById(R.id.rowLanguage);
        TextView tvCurrentLanguage = dialog.findViewById(R.id.tvCurrentLanguage);
        updateCurrentLanguageText(tvCurrentLanguage);

        if (rowLanguage != null) {
            rowLanguage.setOnClickListener(v -> {
                showLanguageSelector();
                dialog.dismiss();
            });
        }

        android.view.View btnClose = dialog.findViewById(R.id.btnCloseSettings);

        // Previews
        updateSettingsPreviews(viewColorPreview, tvFontPreview);
        if (tvBackgroundStatus != null) {
            int bgIdx = mPrefs.getInt("pref_bg_mode", 1);
            String[] modes = { getString(R.string.bg_pure_black), getString(R.string.bg_fixed_image),
                    getString(R.string.bg_dynamic_logo) };
            if (bgIdx >= 0 && bgIdx < modes.length)
                tvBackgroundStatus.setText(modes[bgIdx]);
        }

        // Logos Online Switch
        if (swLogosOnline != null) {
            swLogosOnline.setChecked(mPrefs.getBoolean("pref_logos_online", false));
            swLogosOnline.setOnCheckedChangeListener((bv, checked) -> {
                mPrefs.edit().putBoolean("pref_logos_online", checked).apply();
                showToast(checked ? "Logos Online: Activado" : "Logos Online: Desactivado");
            });
        }

        // Night Mode Switch
        android.view.View rowNightSchedule = dialog.findViewById(R.id.rowNightSchedule);
        TextView tvNightStart = dialog.findViewById(R.id.tvNightStart);
        TextView tvNightEnd = dialog.findViewById(R.id.tvNightEnd);

        // V4.5: Initialize schedule display
        int nightStart = mPrefs.getInt("pref_night_start", 19);
        int nightEnd = mPrefs.getInt("pref_night_end", 7);
        if (tvNightStart != null) tvNightStart.setText(String.format(java.util.Locale.US, "%02d:00", nightStart));
        if (tvNightEnd != null) tvNightEnd.setText(String.format(java.util.Locale.US, "%02d:00", nightEnd));

        if (swNight != null) {
            boolean nightEnabled = mPrefs.getBoolean("pref_night_mode_auto", false);
            swNight.setChecked(nightEnabled);
            if (rowNightSchedule != null) rowNightSchedule.setVisibility(nightEnabled ? View.VISIBLE : View.GONE);

            swNight.setOnCheckedChangeListener((bv, checked) -> {
                mPrefs.edit().putBoolean("pref_night_mode_auto", checked).apply();
                if (rowNightSchedule != null) rowNightSchedule.setVisibility(checked ? View.VISIBLE : View.GONE);
                if (checked) {
                    checkAndApplyNightMode();
                    showToast("Modo Noche Automático: Activado");
                } else {
                    showToast("Modo Noche Automático: Desactivado - Puedes elegir skin manualmente");
                }
            });
        }

        // V4.5: Night Schedule TimePickers
        if (tvNightStart != null) {
            tvNightStart.setOnClickListener(v -> {
                int curStart = mPrefs.getInt("pref_night_start", 19);
                new android.app.TimePickerDialog(this, (view, h, m) -> {
                    mPrefs.edit().putInt("pref_night_start", h).apply();
                    tvNightStart.setText(String.format(java.util.Locale.US, "%02d:00", h));
                    checkAndApplyNightMode();
                    String startStr = String.format(java.util.Locale.US, "%02d:00", h);
                    String endStr = tvNightEnd != null ? tvNightEnd.getText().toString() : "07:00";
                    showToast(getString(R.string.night_schedule_updated, startStr, endStr));
                }, curStart, 0, true).show();
            });
        }
        if (tvNightEnd != null) {
            tvNightEnd.setOnClickListener(v -> {
                int curEnd = mPrefs.getInt("pref_night_end", 7);
                new android.app.TimePickerDialog(this, (view, h, m) -> {
                    mPrefs.edit().putInt("pref_night_end", h).apply();
                    tvNightEnd.setText(String.format(java.util.Locale.US, "%02d:00", h));
                    checkAndApplyNightMode();
                    String startStr = tvNightStart != null ? tvNightStart.getText().toString() : "19:00";
                    String endStr = String.format(java.util.Locale.US, "%02d:00", h);
                    showToast(getString(R.string.night_schedule_updated, startStr, endStr));
                }, curEnd, 0, true).show();
            });
        }

        // History Switch
        if (swHistory != null) {
            swHistory.setChecked(mPrefs.getBoolean("pref_save_history", true));
            swHistory.setOnCheckedChangeListener((bv, checked) -> {
                mPrefs.edit().putBoolean("pref_save_history", checked).apply();
                showToast(checked ? "Historial: Activado" : "Historial: Desactivado");
            });
        }

        // Swipe Gestures Switch
        if (swGestures != null) {
            swGestures.setChecked(mPrefs.getBoolean("pref_swipe_gestures", true));
            swGestures.setOnCheckedChangeListener((bv, checked) -> {
                mPrefs.edit().putBoolean("pref_swipe_gestures", checked).apply();
                showToast(checked ? "Gestos: Activados" : "Gestos: Desactivados");
            });
        }

        // V4.3: Hardware Settings - AM Toggle
        androidx.appcompat.widget.SwitchCompat swAm = dialog.findViewById(R.id.switchEnableAm);
        if (swAm != null) {
            swAm.setChecked(mPrefs.getBoolean("pref_enable_am", true));
            swAm.setOnCheckedChangeListener((bv, checked) -> {
                mPrefs.edit().putBoolean("pref_enable_am", checked).apply();
                showToast(checked ? getString(R.string.am_band_enabled) : getString(R.string.am_band_disabled));
            });
        }

        // V4.3: Hardware Settings - Engine
        android.view.View rowEngine = dialog.findViewById(R.id.rowEngine);
        TextView tvCurrentEngine = dialog.findViewById(R.id.tvCurrentEngine);
        if (tvCurrentEngine != null) {
            updateCurrentEngineText(tvCurrentEngine);
        }
        if (rowEngine != null) {
            rowEngine.setOnClickListener(v -> {
                showEngineSelector();
                dialog.dismiss();
            });
        }

        cardTheme.setOnClickListener(v -> {
            // Check if auto night mode is enabled
            if (mPrefs.getBoolean("pref_night_mode_auto", false)) {
                showToast("Desactiva Modo Noche Automático para elegir skin manualmente");
                return;
            }
            showThemeSelector(dialog, viewColorPreview, tvFontPreview);
        });
        cardFonts.setOnClickListener(v -> showFontSelector(dialog, tvFontPreview));
        cardBackground.setOnClickListener(v -> showBackgroundSelector(dialog, tvBackgroundStatus));

        // V4.1: Unified About Button Listener
        android.view.View btnAbout = dialog.findViewById(R.id.btnAbout);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                showAboutDialog();
                // Optional: keep settings open or close it? usually keep open or close both?
                // Let's keep settings open for now, or maybe dismiss.
                // The standard is usually stacking dialogs is fine.
            });
        }

        // V4.5: RADIO DATA SYSTEM (RDS) Switches en Premium Settings
        androidx.appcompat.widget.SwitchCompat swPremiumRds = dialog.findViewById(R.id.swPremiumRds);
        if (swPremiumRds != null) {
            swPremiumRds.setOnCheckedChangeListener((bv, checked) -> {
                if (!invokeQFTuner("setRdsSwitch", int.class, checked ? 1 : 0)) {
                    sendMcuTunerCmd((byte) 0x15, (byte) (checked ? 0x01 : 0x00), (byte) 0x00);
                }
                showToast("RDS Subsystem: " + (checked ? "Activado" : "Desactivado"));
            });
        }

        androidx.appcompat.widget.SwitchCompat swPremiumAf = dialog.findViewById(R.id.swPremiumAf);
        androidx.appcompat.widget.SwitchCompat swPremiumTa = dialog.findViewById(R.id.swPremiumTa);

        if (swPremiumAf != null) {
            swPremiumAf.setOnCheckedChangeListener((bv, checked) -> {
                boolean taChecked = swPremiumTa != null && swPremiumTa.isChecked();
                if (mRadioService instanceof com.example.openradiofm.data.source.K706RadioManager) {
                    ((com.example.openradiofm.data.source.K706RadioManager) mRadioService).enableSilentlyRdsFeatures(checked, taChecked);
                }
                showToast("Alternative Frequencies (AF): " + (checked ? "Activado" : "Desactivado"));
            });
        }

        if (swPremiumTa != null) {
            swPremiumTa.setOnCheckedChangeListener((bv, checked) -> {
                boolean afChecked = swPremiumAf != null && swPremiumAf.isChecked();
                if (mRadioService instanceof com.example.openradiofm.data.source.K706RadioManager) {
                    ((com.example.openradiofm.data.source.K706RadioManager) mRadioService).enableSilentlyRdsFeatures(afChecked, checked);
                }
                showToast("Traffic Announcements (TA): " + (checked ? "Activado" : "Desactivado"));
            });
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateSettingsPreviews(android.view.View colorView, TextView fontView) {
        if (colorView != null) {
            com.example.openradiofm.ui.theme.ThemeManager tm = new com.example.openradiofm.ui.theme.ThemeManager(this);
            int color = tm.getAccentColor();
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(color);
            colorView.setBackground(shape);
        }
        if (fontView != null) {
            int fontIdx = mPrefs.getInt("pref_font_type", 0);
            applyFontToView(fontView, fontIdx);
        }
    }

    private void applyFontToView(TextView tv, int fontType) {
        android.graphics.Typeface typeface = android.graphics.Typeface.DEFAULT_BOLD;
        try {
            if (fontType == 1)
                typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.id.tvFrequency); // Dummy but we
                                                                                                      // need res
            // Actually better use the same logic as applyFonts
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
        }
        tv.setTypeface(typeface);
    }

    private void updateSettingsDialogStates(TextView tvTheme, TextView tvFont, TextView tvBg) {
        // Theme
        tvTheme.setText("Active");

        // Font
        int fontIdx = mPrefs.getInt("pref_font_type", 0);
        String[] fonts = { getString(R.string.font_default), getString(R.string.font_bebas), getString(R.string.font_digital), getString(R.string.font_modern), getString(R.string.font_orbitron), getString(R.string.font_formula1) };
        if (fontIdx >= 0 && fontIdx < fonts.length)
            tvFont.setText(fonts[fontIdx]);

        // BG
        int bgIdx = mPrefs.getInt("pref_bg_mode", 1);
        String[] modes = { getString(R.string.bg_pure_black), getString(R.string.bg_fixed_image),
                getString(R.string.bg_dynamic_logo) };
        if (bgIdx >= 0 && bgIdx < modes.length)
            tvBg.setText(modes[bgIdx]);
    }

    private void updateCurrentEngineText(TextView tv) {
        int idx = mPrefs.getInt("pref_radio_engine", 0);
        String[] engines = { getString(R.string.engine_auto), "HCN", "MTK", "Standard", "TS" };
        if (idx >= 0 && idx < engines.length)
            tv.setText(engines[idx]);
    }

    private void showEngineSelector() {
        String[] options = {
                getString(R.string.engine_auto),
                getString(R.string.engine_hcn),
                getString(R.string.engine_mtk),
                getString(R.string.engine_ts),
                getString(R.string.engine_standard)
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.radio_engine)
                .setItems(options, (dialog, which) -> {
                    mPrefs.edit().putInt("pref_radio_engine", which).apply();
                    showToast(String.format(getString(R.string.engine_changed), options[which]));
                    conectarRadio();
                })
                .show();
    }

    // V4.0: Saved Preset Indicator & Color Logic (Unified)
    private void updateFrequencyDisplay(int freq) {
        if (tvFrequency != null) {
            if (mCurrentBand == BAND_AM1 || mCurrentBand == BAND_AM2) {
                tvFrequency.setText(String.valueOf(freq));
            } else {
                tvFrequency.setText(String.format(java.util.Locale.US, "%.2f", freq / 1000.0f));
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
                    && !rdsNameValue.equals("STATION NAME")) {
                tvRdsName.setText(rdsNameValue);
                tvRdsName.setVisibility(View.VISIBLE);
            } else {
                String current = tvRdsName.getText().toString();
                // V4.5.1: Si ya hay un nombre recibido por callback (no es el default), NO lo borramos ni lo ocultamos
                if (!current.isEmpty() && !current.equals("STATION") && !current.equals("STATION NAME")
                        && !current.equals("STATION ") && !current.equals(" NAME")) {
                    tvRdsName.setVisibility(View.VISIBLE);
                } else {
                    // V4.0: Keep visible in V2 to prevent shifts
                    tvRdsName.setVisibility(mIsV3 ? View.GONE : View.VISIBLE);
                    if (!mIsV3)
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
                    // V4.0: Keep visible in V2 to prevent shifts
                    tvRdsInfo.setVisibility(mIsV3 ? View.GONE : View.VISIBLE);
                    if (!mIsV3)
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
                try {
                    if (mRadioService != null)
                        hasStereo = mRadioService.IsStereo();
                } catch (Exception ignored) {
                }

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
                try {
                    boolean hasStereo = mRadioService != null && mRadioService.IsStereo();
                    ivStereoIcon.setVisibility(hasStereo ? View.VISIBLE : View.GONE);
                } catch (Exception ignored) {
                }
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

    private void showThemeSelector(android.app.Dialog parentDialog, android.view.View colorPreview,
            TextView fontPreview) {
        // IMPORTANT: This array MUST match the order of Skin enum in ThemeManager.java
        String[] skins = { "Night Mode", "Classic", "Orange", "Blue", "Green", "Purple", "Red", "Yellow", "Cyan",
                "Pink", "White" };
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.select_skin)
                .setItems(skins, (d, w) -> {
                    com.example.openradiofm.ui.theme.ThemeManager.Skin[] skinValues = com.example.openradiofm.ui.theme.ThemeManager.Skin
                            .values();
                    if (w < skinValues.length) {
                        com.example.openradiofm.ui.theme.ThemeManager themeManager = new com.example.openradiofm.ui.theme.ThemeManager(
                                this);
                        themeManager.setSkin(skinValues[w]);
                        applySkin(skinValues[w]);
                        updateSettingsPreviews(colorPreview, fontPreview);
                    }
                })
                .create();

        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.7f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void showFontSelector(android.app.Dialog parentDialog, TextView fontPreview) {
        String[] fonts = { getString(R.string.font_default), getString(R.string.font_bebas), getString(R.string.font_digital), getString(R.string.font_modern), getString(R.string.font_orbitron), getString(R.string.font_formula1) };
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.select_typography)
                .setItems(fonts, (d, w) -> {
                    mPrefs.edit().putInt("pref_font_type", w).apply();
                    applyFonts();
                    updateSettingsPreviews(null, fontPreview);
                })
                .create();

        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.7f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void showBackgroundSelector(android.app.Dialog parentDialog, TextView tvStatus) {
        String[] modes = { getString(R.string.bg_pure_black), getString(R.string.bg_fixed_image),
                getString(R.string.bg_dynamic_logo) };
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.select_bg_mode)
                .setItems(modes, (d, w) -> {
                    mPrefs.edit().putInt("pref_bg_mode", w).apply();
                    loadCustomBackground();
                    loadCarLogo();
                    updateDynamicBackground(mLastLogoUrl);
                    if (tvStatus != null)
                        tvStatus.setText(modes[w]);
                })
                .create();

        // V3.0: Apply premium styling
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.7f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    // V4.2: New Language Selector to ensure correct preference key usage
    private void showNewLanguageSelector() {
        String[] languages = { "Español (ES)", "English (EN)", "Français (FR)", "Deutsch (DE)", "Português (PT)",
                "Italiano (IT)", "Русский (RU)" };
        final String[] codes = { "es", "en", "fr", "de", "pt", "it", "ru" };

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setItems(languages, (d, w) -> {
                    if (w < codes.length) {
                        mPrefs.edit().putString("pref_app_language", codes[w]).apply();
                        // Recreate activity to apply new context
                        recreate();
                    }
                })
                .create();

        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.7f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    /**
     * Aplica el skin seleccionado a todos los elementos de la interfaz.
     */
    private void applySkin(com.example.openradiofm.ui.theme.ThemeManager.Skin skin) {
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

        // Iconos en azul noche (tint)
        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnBand, R.id.btnAutoScan,
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
        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnBand, R.id.btnAutoScan,
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

    /**
     * V3.8: Actualiza el fondo de la aplicación con el logo de la radio
     * (difuminado).
     */
    private void updateDynamicBackground(String logoUrl) {
        if (ivDynamicBackground == null)
            return;

        // V4.0: Layout 3 always allows dynamic background if enabled in prefs
        int bgMode = mPrefs.getInt("pref_bg_mode", 1); // 1: Classic, 2: Dynamic

        if (bgMode == 2) {
            ivDynamicBackground.setVisibility(View.VISIBLE);
            if (logoUrl != null && !logoUrl.isEmpty()) {
                Glide.with(this)
                        .load(logoUrl)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivDynamicBackground);
            } else {
                // If no logo but dynamic enabled, we could show a default or keep last
                // For now, let's keep it visible with the overlay
            }
        } else {
            ivDynamicBackground.setVisibility(View.GONE);
            loadCustomBackground();
        }
    }

    // V4: Frequency Step Helpers (Manual Tuning)
    private void stepFreqUp() {
        mCurrentPty = null; // V5.2: Reset PTY on tune
        if (mRadioService == null)
            return;
        try {
            int current = mRadioService.getCurrentFreq();
            int band = mRadioService.getCurrentBand();
            boolean isAm = (band == BAND_AM1 || band == BAND_AM2);

            int newFreq;
            if (isAm) {
                newFreq = current + 9;
                if (newFreq > 1620)
                    newFreq = 522; // AM Europe range
            } else {
                newFreq = current + 50;
                if (newFreq > 108000)
                    newFreq = 87500;
            }
            mRadioService.gotoFreq(newFreq);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void stepFreqDown() {
        mCurrentPty = null; // V5.2: Reset PTY on tune
        if (mRadioService == null)
            return;
        try {
            int current = mRadioService.getCurrentFreq();
            int band = mRadioService.getCurrentBand();
            boolean isAm = (band == BAND_AM1 || band == BAND_AM2);

            int newFreq;
            if (isAm) {
                newFreq = current - 9;
                if (newFreq < 522)
                    newFreq = 1620;
            } else {
                newFreq = current - 50;
                if (newFreq < 87500)
                    newFreq = 108000;
            }
            mRadioService.gotoFreq(newFreq);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
    private void checkAndApplyNightMode() {
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

    private void showHistoryDialog() {
        String historyStr = mPrefs.getString("pref_station_history", "");
        if (historyStr.isEmpty()) {
            showStyledToast(getString(R.string.history_empty));
            return;
        }

        String[] freqs = historyStr.split(",");
        String[] displayNames = new String[freqs.length];

        for (int i = 0; i < freqs.length; i++) {
            int f = Integer.parseInt(freqs[i]);
            displayNames[i] = String.format("%.2f MHz", f / 1000.0f);
            // V4: Opcional, añadir nombre RDS si está en caché...
        }

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.station_history))
                .setItems(displayNames, (d, w) -> {
                    if (mRadioService != null) {
                        try {
                            mRadioService.gotoFreq(Integer.parseInt(freqs[w]));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .create();

        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.7f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    /**
     * V4.0: Muestra diálogo para guardar/cargar favoritos
     */
    /**
     * V4.1: Muestra diálogo para guardar/cargar favoritos usando diseño unificado
     */
    private void showSaveLoadFavoritesDialog() {
        if (!checkStoragePermissions()) {
            requestStoragePermissions();
            return;
        }

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_save_load);

        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        android.view.View btnSave = dialog.findViewById(R.id.btnSave);
        android.view.View btnLoad = dialog.findViewById(R.id.btnLoad);
        android.view.View btnDeleteAllFavs = dialog.findViewById(R.id.btnDeleteAllFavs);
        android.view.View btnClearHistory = dialog.findViewById(R.id.btnClearHistory);
        android.view.View btnClose = dialog.findViewById(R.id.btnClose);

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                saveFavoritesToFile();
                dialog.dismiss();
            });
        }

        if (btnLoad != null) {
            btnLoad.setOnClickListener(v -> {
                loadFavoritesFromFile();
                dialog.dismiss();
            });
        }

        if (btnDeleteAllFavs != null) {
            btnDeleteAllFavs.setOnClickListener(v -> {
                if (mPrefs != null) {
                    mPrefs.edit().clear().apply();
                    showToast("Todos los favoritos han sido borrados");
                    refreshPresetButtons();
                }
                dialog.dismiss();
            });
        }

        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> {
                if (mPrefs != null) {
                    mPrefs.edit().remove("pref_station_history").apply();
                    showToast("El historial ha sido borrado");
                }
                dialog.dismiss();
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * V4.1: Muestra diálogo "Acerca de" unificado
     */
    private void showAboutDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_about);

        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        // Update version text dynamically if needed
        try {
            TextView tvVersion = dialog.findViewById(R.id.tvAppVersion);
            if (tvVersion != null) {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                tvVersion.setText("Versión " + versionName);
            }
        } catch (Exception e) {
        }

        android.view.View btnClose = dialog.findViewById(R.id.btnClose);
        if (btnClose != null)
            btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Guarda los favoritos actuales en un archivo .fav en la carpeta RadioLogos
     */
    private void saveFavoritesToFile() {
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
    private void loadFavoritesFromFile() {
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
                getString(R.string.language_russian)
        };

        String[] languageCodes = { "es", "en", "ru" };
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
        builder.show();
    }

    /**
     * V4.0: Actualiza el texto del idioma actual en el menú Premium
     */
    private void updateCurrentLanguageText(TextView tvCurrentLanguage) {
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
            default:
                langName = getString(R.string.language_spanish);
                break;
        }

        tvCurrentLanguage.setText(langName);
    }

    // PERMISSION HANDLING
    private boolean checkStoragePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int write = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            return write == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    read == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestStoragePermissions() {
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
                showSaveLoadFavoritesDialog();
                showToast("Se requieren permisos de almacenamiento para guardar favoritos.");
            }
        }
    }

    // V5.0: Helper Methods to fix compilation
    private void setMute(boolean mute) {
        mMuteState = mute;
        
        // V6.2: Support for K706 Native Mute
        if (mRadioService instanceof com.example.openradiofm.data.source.K706RadioManager) {
            try {
                ((com.example.openradiofm.data.source.K706RadioManager) mRadioService).setMute(mute);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
        if (mMode == FmMode.FM_MT8163) {
            // Punto 3: Lógica invertida en MT8163
            execRemote(IRadioServiceAPI::onSeekDownEvent);
        } else {
            execRemote(IRadioServiceAPI::onSeekUpEvent);
        }
    }

    private void onSeekDownEvent() {
        if (mMode == FmMode.FM_MT8163) {
            // Punto 3: Lógica invertida en MT8163
            execRemote(IRadioServiceAPI::onSeekUpEvent);
        } else {
            execRemote(IRadioServiceAPI::onSeekDownEvent);
        }
    }

    private void gotoFreq(int freq) {
        mCurrentPty = null; // V5.2: Reset PTY on tune
        if (mRadioService == null)
            return;
        execRemote(s -> s.gotoFreq(freq));
    }

    private void refreshPresetsCache() {
        // Fix v4.5.1: Usar mCurrentBand directamente (ya actualizado en refreshRadioStatus)
        int band = mCurrentBand;
        if (mRadioService != null) {
            try {
                band = mRadioService.getCurrentBand();
                mCurrentBand = band; // Sincronizar siempre
            } catch (RemoteException ignored) {}
        }
        for (int i = 0; i < PRESETS_COUNT; i++) {
            String key = "P" + (i + 1) + "_B" + band;
            mPresets[i] = mPrefs.getInt(key, 0);
        }
    }

    /**
     * V4.3: Helper to check if a frequency is stored in presets
     */
    private boolean isStationMemorized(int freq) {
        if (mPresets == null)
            return false;
        for (int p : mPresets) {
            if (p == freq)
                return true;
        }
        return false;
    }

    /**
     * V4.3: Helper to get the 1-based index of a preset frequency
     */
    private int getPresetIndex(int freq) {
        if (mPresets == null)
            return 0;
        for (int i = 0; i < mPresets.length; i++) {
            if (mPresets[i] == freq)
                return i + 1;
        }
        return 0;
    }

    /**
     * V9.5: AutoScan Toggle — click 1 inicia, click 2 detiene.
     */
    private void toggleAutoScan(ImageButton btn) {
        if (!mIsScanning) {
            // Iniciar AutoScan (0x08)
            execRemote(IRadioServiceAPI::onScanEvent);
            mIsScanning = true;
            if (btn != null) {
                btn.setColorFilter(android.graphics.Color.parseColor("#00E676"),
                    android.graphics.PorterDuff.Mode.SRC_IN); // Verde = escaneando
            }
            showToast("AutoScan iniciado...");
        } else {
            // Detener AutoScan (0x09 = onPSEvent/stopScan)
            execRemote(IRadioServiceAPI::onPSEvent);
            mIsScanning = false;
            if (btn != null) {
                btn.clearColorFilter(); // Restaurar color original
            }
            showToast("AutoScan detenido");
        }
    }

    /**
     * V4.3: Auto Scan Wrapper (legacy, ahora usa toggleAutoScan)
     */
    private void promptAutoScan() {
        execRemote(IRadioServiceAPI::onScanEvent);
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
        if (mRadioService instanceof com.example.openradiofm.data.source.K706RadioManager) {
            try {
                java.lang.reflect.Method sendCmd = com.example.openradiofm.data.source.K706RadioManager.class.getDeclaredMethod(
                    "sendCmd", byte.class, byte.class, byte.class);
                sendCmd.setAccessible(true);
                sendCmd.invoke(mRadioService, subCmd, param1, param2);
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "sendMcuTunerCmd error", e);
            }
        }
    }
}
