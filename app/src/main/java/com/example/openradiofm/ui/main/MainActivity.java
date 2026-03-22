package com.example.openradiofm.ui.main;

import androidx.appcompat.app.AlertDialog;
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
import android.graphics.Bitmap;
import android.Manifest;
import android.content.pm.PackageManager;


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
            // V13.9: Default to FALSE for online logos as requested for testing
            if (!prefs.contains("pref_logos_online")) {
                prefs.edit().putBoolean("pref_logos_online", true).apply();
            }
        } catch (Exception e) {
        }
        super.attachBaseContext(MyContextWrapper.wrap(newBase, lang));
    }

    private static final String TAG = "OpenRadioFm";
    private static final int PRESETS_COUNT = 18; // V21.2: ampliar memorias de presets
    /** Silenciar FM en llamadas (K706): {@link Manifest.permission#READ_PHONE_STATE} */
    private static final int REQ_READ_PHONE_STATE_K706 = 1003;

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
        FM_8259_8667,
        AM
    }

    public FmMode mMode = FmMode.FM_BASICO;

    public IRadioServiceAPI mRadioService;
    public com.example.openradiofm.data.repository.RadioRepository mRepository;
    public android.content.SharedPreferences mPrefs;
    public HiddenRadioPlayer mHiddenPlayer;

    // V5.0: Capa de abstracción de hardware
    public RadioEngine mEngine;
    public boolean mIsScanning = false;
    public ScanManager mScanManager;
    public DialogManager mDialogManager;
    public LogoManager mLogoManager;
    public RadioServiceController mServiceController;
    public RDSManager mRdsManager;
    public StandardLayoutManager mStandardLayoutManager;
    public SimpleLayoutManager mSimpleLayoutManager;
    public ControlPanelManager mControlPanelManager;

    public boolean mIsSimpleLayout = false;
    public boolean mIsV3 = false;
    public boolean mIsMinimal = false; // V19.2
    public boolean mControlsHidden = false;
    private android.view.View bottomControls;
    private android.os.Handler mAutoHideHandler;
    private Runnable mAutoHideRunnable;

    // V21.0: UI Controllers Refactor
    public BaseLayoutController mUiController;
    private final android.os.Handler mMainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    // V16: Managers de Modo Nocturno e Historial
    public NightModeManager mNightModeManager;
    public HistoryManager mHistoryManager;
    public MediaSessionManager mMediaSessionManager;
    public ThemeManager mThemeManager; // V16.2: Skin manager

    // V18.5: Reloj Digital
    private android.os.Handler mClockHandler;
    private Runnable mClockRunnable;
    private TextView tvDigitalClock;

    // V5.5: Managers de Audio y Dispositivo
    public PlaybackManager mPlaybackManager;
    public DeviceManager mDeviceManager;
    public HardwareManager mHardwareManager;
    public RadioSessionController mSessionController;


    // V11: RDS PI Database Identification
    private com.example.openradiofm.data.source.RdsDatabase mRdsDb;
    public String mCurrentPi = null;

    // V13: Gestor de Presets (Reducción de MainActivity)
    public PresetManager mPresetManager;
    public int mLastFreq = -1;
    public String mLastPs = ""; // V18.6: Almacena el nombre RDS/Custom actual
    public boolean mHasRdsLock = false;
    public String mCurrentPty = null;
    public String mLastLogoUrl = "";
    public java.util.Map<String, String> mLogoCachePerBand = new java.util.HashMap<>();
    /**
     * QS6/NWD y otros motores con callbacks rápidos: invalida cargas de logo asíncronas al cambiar
     * frecuencia o banda (evita que un Glide/getStationInfo tardío pinte logo de otra emisora).
     */
    public final java.util.concurrent.atomic.AtomicInteger mLogoUiGeneration = new java.util.concurrent.atomic.AtomicInteger(0);
    private com.example.openradiofm.data.source.SupabaseSyncManager mSupabaseSyncManager;
    private com.example.openradiofm.ui.main.OnlineStreamManager mOnlineStreamManager;

    // V5.0: UI Elements (Fixing Compilation Errors)
    private TextView tvPty;
    private ImageView ivSignalLevel;
    private ImageView ivAfIcon, ivTaIcon, ivTpIcon; // RDS Status Icons
    private android.widget.FrameLayout ivDataActivity; // V16.2: Cloud Data indicator (Wrapper)
    private ImageView ivDataActivityIcon; // El icono real que cambia de color
    private int mActiveDataOps = 0; // V16.2: Concurrent Supabase Operations
    private android.animation.ObjectAnimator mDataBlinkAnimator;
    private long mLastInternetCheckTime = 0;
    private boolean mLastInternetCache = false;

    // V2.5: Broadcast guards
    private int mLastBroadcastFreq = -1;
    private int mLastBroadcastBand = -1;
    private String mLastBroadcastPs = "";

    // V2.6: Master Guard for refreshRadioStatus
    private int mLastRefreshFreq = -1;
    private int mLastRefreshBand = -1;
    private long mLastFullRefreshTime = 0;
    
    // V21.1: Throttling de tareas UI no críticas (fluidez)
    private static final long NIGHT_MODE_CHECK_INTERVAL_MS = 5_000;
    private static final long DATA_ACTIVITY_UI_INTERVAL_MS = 1_000;
    private long mLastNightModeCheckTime = 0;
    private long mLastDataActivityUiTime = 0;
    
    // V21.1: Evitar crear hilos por cada refresh (coalescing de station info)
    private java.util.concurrent.ExecutorService mStationInfoExecutor;
    private final java.util.concurrent.atomic.AtomicInteger mStationInfoSeq = new java.util.concurrent.atomic.AtomicInteger(0);
    private volatile int mLastStationInfoRequestedSeq = 0;

    public boolean mMuteState = false;

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

    // V9.9: RDS Debugging Tracker (K706)
    public K706EngineeringDialog mEngineeringDialog = null;
    /** Menú ingeniería QS6 / NWD (mismo easter egg GPS ×5). */
    public QS6EngineeringDialog mQs6EngineeringDialog = null;

    public int mCurrentBand = 0;
    private boolean mIsRecreating = false; // V20.3: Flag to distinguish between Cold Start and Layout Switch

    public int getCurrentBand() {
        return mCurrentBand;
    }



    // V18.6: StationAdapter and ScannedStation moved to separate files

    // Métodos delegados al PresetManager para compatibilidad con código existente
    public void gotoFreq(int freq) {
        if (mEngine != null) {
            // V20.0: Limpieza inmediata de UI para evitar logos "pegados" si el hardware falla o es lento
            runOnUiThread(() -> {
                if (mLogoManager != null) mLogoManager.clearLogo();
            });
            mEngine.tune(freq);
            mLastFreq = freq;
            refreshRadioStatus();
        }
    }

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
    public void gotoNextFavorite() {
        if (mEngine == null || mPresetManager == null)
            return;

        int currentFreq = mEngine.getCurrentFreq();
        // V21.3: Cambiado de getNextFavorite (frecuencia) a getNextSequentialFavorite (slots 1-18)
        int nextFreq = mPresetManager.getNextSequentialFavorite(currentFreq);

        if (nextFreq != -1) {
            Log.d(TAG, "Saltando a SIGUIENTE favorito (Secuencial): " + nextFreq);
            gotoFreq(nextFreq);
        } else {
            showToast("No hay otros favoritos guardados");
        }
    }

    /**
     * V14.0: Salta al favorito anterior guardado en la banda actual.
     * V21.3: Ahora usa navegación secuencial por slots.
     */
    public void gotoPreviousFavorite() {
        if (mEngine == null || mPresetManager == null)
            return;

        int currentFreq = mEngine.getCurrentFreq();
        // V21.3: Cambiado de getPreviousFavorite (frecuencia) a getPreviousSequentialFavorite (slots 1-18)
        int prevFreq = mPresetManager.getPreviousSequentialFavorite(currentFreq);

        if (prevFreq != -1) {
            Log.d(TAG, "Saltando a ANTERIOR favorito (Secuencial): " + prevFreq);
            gotoFreq(prevFreq);
        } else {
            showToast("No hay otros favoritos guardados");
        }
    }

    // V3.0: Background personalizado

    private TextView tvFrequency, tvRdsName, tvRdsInfo;
    private android.view.View boxFrequency;
    private ImageView ivBandIndicator, ivUnitLabel, ivFavoriteIndicator, ivStereoIcon;
    private ImageButton btnLocDx, btnBand, btnPowerOff;

    // UI Arrays for Presets - REMOVED (Managed by PresetManager)

    public void animateButton(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 0.9f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 0.9f, 1.0f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    // V18.6: MCU and BT logic moved to HardwareManager

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

    // V5.0: Callbacks unificados del RadioEngine (MainActivity implementa
    // RadioEngineCallback)
    @Override
    public void onFrequencyChanged(int freqKhz) {
        if (mSessionController != null) {
            mSessionController.onFrequencyChanged(freqKhz);
        }
        handleFrequencyChange(freqKhz);
        runOnUiThread(() -> {
            if (mUiController != null) {
                mUiController.updateFrequency(freqKhz, null, mCurrentBand >= 3);
            } else {
                updateFrequencyDisplay(freqKhz, null);
            }
        });
    }

    @Override
    public void onBandChanged(int band) {
        if (mSessionController != null) {
            mSessionController.onBandChanged(band);
        }
        runOnUiThread(() -> {
            mLogoUiGeneration.incrementAndGet();
            mCurrentBand = band;
            if (mPresetManager != null) {
                mPresetManager.refreshPresetsCache(band);
                mPresetManager.refreshButtons(band);
            }
            if (mUiController != null) {
                mUiController.updateBandIndicator(band);
            } else {
                updateBandImage(band);
            }

            // Asegurar que el icono de unidad (MHz/KHz) se actualiza SIEMPRE al cambiar de banda,
            // independientemente de si usamos UiController o el fallback legacy.
            if (ivUnitLabel != null) {
                int unitResId = (band == BAND_AM1 || band == BAND_AM2)
                        ? R.drawable.radio_khz
                        : R.drawable.radio_mhz;
                setImageResourceIfChanged(ivUnitLabel, unitResId);
            }
            
            // V2.6: Re-asegurar tinte noche completo tras refrescar presets y unit label.
            // refreshButtons() pone nuevas imágenes/textos en blanco, y setImageResourceIfChanged
            // cambia la imagen de ivUnitLabel. Ambos necesitan re-tintado.
            boolean isNight = (mThemeManager != null && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
            if (isNight && mNightModeManager != null) {
                mNightModeManager.applyNightModeColors(mLastFreq);
            }
            
            // V5.2: Actualizar Widget al cambiar de banda
            if (mEngine != null) {
                sendWidgetUpdateIntent(mEngine.getCurrentFreq(), band, mLastPs);
            }
        });
    }

    @Override
    public void onStereoChanged(boolean stereo) {
        if (mSessionController != null) {
            mSessionController.onStereoChanged(stereo);
        }
        runOnUiThread(() -> {
            if (mUiController != null) {
                mUiController.updateStereo(stereo);
            } else if (ivStereoIcon != null) {
                ivStereoIcon.setVisibility(stereo ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
            }
            
            if (ivSignalLevel != null) {
                int color = stereo ? android.graphics.Color.parseColor("#00E676")
                        : android.graphics.Color.parseColor("#FFD600");
                ivSignalLevel.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        });
    }

    @Override
    public void onRdsName(final String name) {
        if (mSessionController != null) {
            mSessionController.onRdsName(name);
        }
        runOnUiThread(() -> {
            if (mRdsManager != null) {
                mRdsManager.onRdsName(name);
                mHasRdsLock = mRdsManager.hasRdsLock();
                if (mMediaSessionManager != null) {
                    String freqText = String.format(java.util.Locale.US, "%.2f MHz", (mEngine != null ? mEngine.getCurrentFreq() : 0) / 1000.0);
                    mMediaSessionManager.updateMetadata(name, freqText, null);
                }
            }
            if (mUiController != null) {
                mUiController.updateRDS(name);
            }
            
            // V5.2: Forzar actualización de Widget al recibir nombre RDS
            if (mEngine != null) {
                sendWidgetUpdateIntent(mEngine.getCurrentFreq(), mCurrentBand, name);
            }
        });
    }

    /**
     * V16.2: Actualiza el estado visual del icono de actividad de datos.
     * - Apagado si no hay internet.
     * - Fijo si hay internet.
     * - Parpadeando si hay actividad (download/upload).
     */
    private void updateDataActivityUI() {
        if (ivDataActivity == null) return;

        boolean onlineEnabled = mPrefs.getBoolean("pref_logos_online", false);
        
        // V2.5: Cache internet check for 10 seconds to avoid main thread jitter
        long now = System.currentTimeMillis();
        boolean isConnected;
        if (now - mLastInternetCheckTime < 10000) {
            isConnected = mLastInternetCache;
        } else {
            isConnected = isInternetAvailable();
            mLastInternetCache = isConnected;
            mLastInternetCheckTime = now;
        }
        
        if (!onlineEnabled) {
            setVisibilityIfChanged(ivDataActivity, View.INVISIBLE);
            stopDataBlink();
            return;
        }

        setVisibilityIfChanged(ivDataActivity, View.VISIBLE);

        if (mActiveDataOps > 0) {
            startDataBlink();
        } else {
            stopDataBlink();
        }

        // V17.0: Indicador visual de Streaming Online activo
        if (ivDataActivityIcon == null) ivDataActivityIcon = findViewById(R.id.ivDataActivityIcon);
        
        if (mOnlineStreamManager != null && (ivDataActivityIcon != null)) {
            if (mOnlineStreamManager.isPlaying()) {
                // Streaming active -> RED
                setVisibilityIfChanged(ivDataActivityIcon, View.VISIBLE);
                setColorFilterIfChanged(ivDataActivityIcon, android.graphics.Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (mOnlineStreamManager.isLoading()) {
                // Loading -> YELLOW
                setVisibilityIfChanged(ivDataActivityIcon, View.VISIBLE);
                setColorFilterIfChanged(ivDataActivityIcon, android.graphics.Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // Inactive -> Respect Night Mode or Clear
                if (mThemeManager != null && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
                    int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
                    setColorFilterIfChanged(ivDataActivityIcon, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    setColorFilterIfChanged(ivDataActivityIcon, null, null);
                }
            }
        }
    }

    /**
     * V17.0: Configura el toggle de Radio Online vs Radio FM.
     */
    private void setupOnlineStreaming() {
        mOnlineStreamManager = new com.example.openradiofm.ui.main.OnlineStreamManager(this, mPlaybackManager);
        mOnlineStreamManager.setListener(new com.example.openradiofm.ui.main.OnlineStreamManager.StreamListener() {
            @Override
            public void onStreamStatusChanged(boolean isLoading, boolean isPlaying) {
                runOnUiThread(() -> updateDataActivityUI());
            }

            @Override
            public void onStreamError(String message) {
                runOnUiThread(() -> showToast(message));
            }
        });

        if (ivDataActivity != null) {
            ivDataActivity.setOnClickListener(v -> {
                // V21.4: Permitir siempre detener el stream si ya está sonando, independientemente de si la radio nativa murió (freq <= 0)
                if (mOnlineStreamManager != null && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading())) {
                    mOnlineStreamManager.stopStream();
                    showToast("Volviendo a Radio FM...");
                    if (mRadioService == null && mMode == FmMode.FM_MT8163 && mServiceController != null) {
                        try {
                            android.content.Intent wakeIntent = new android.content.Intent("com.hcn.autoradio.FMRADIO_START");
                            wakeIntent.setPackage("com.hcn.autoradio");
                            wakeIntent.addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(wakeIntent);
                        } catch (Exception ignored) {}
                        mServiceController.start();
                    }
                    return;
                }

                int freq = (mEngine != null) ? mEngine.getCurrentFreq() : -1;
                if (freq <= 0) return;

                // getStationInfo + resolución Supabase en hilo de fondo (URL a menudo aún no en caché).
                new Thread(() -> {
                    try {
                        com.example.openradiofm.data.model.RadioStation station =
                                mRepository.getStationInfo(freq, null);
                        String url = (station != null) ? station.getStreamUrl() : null;
                        if (url == null || url.isEmpty()) {
                            runOnUiThread(() -> {
                                if (!isFinishing()) {
                                    showToast("Buscando enlace de streaming…");
                                }
                            });
                            url = mRepository.resolveStreamUrlForFrequency(freq);
                        }
                        final String streamUrl = url;
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            if (streamUrl != null && !streamUrl.isEmpty()) {
                                mOnlineStreamManager.startStream(streamUrl);
                                showToast("Iniciando Radio Online...");
                            } else {
                                showToast("Streaming no disponible para esta emisora");
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Streaming: getStationInfo falló", e);
                        runOnUiThread(() -> {
                            if (!isFinishing()) { showToast("Error al cargar datos de emisora"); }
                        });
                    }
                }, "OpenRadioFM-streamMeta").start();
            });

            // V17.1: Pulsación larga para forzar recarga (borrar caché) de Supabase
            ivDataActivity.setOnLongClickListener(v -> {
                int freq = (mEngine != null) ? mEngine.getCurrentFreq() : -1;
                if (freq > 0) {
                    showToast("Caché de emisora borrada. Sincronizando...");
                    mRepository.clearCacheForFrequency(freq);
                    
                    // Asegurar que forzamos también la recarga visual deteniendo el posible stream actual
                    if (mOnlineStreamManager != null && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading())) {
                        mOnlineStreamManager.stopStream();
                    }

                    // Forzar recarga en segundo plano
                    new Thread(() -> {
                        mRepository.getStationInfo(freq, logoUrl -> {
                            // V18.6.4: Preservar nombre RDS actual al recargar
                            String name = (mRdsManager != null) ? mRdsManager.getDisplayName(freq) : mLastPs;
                            runOnUiThread(() -> updateFrequencyDisplay(freq, name));
                        });
                    }).start();
                }
                return true;
            });
        }
    }

    private void startDataBlink() {
        if (mDataBlinkAnimator != null && mDataBlinkAnimator.isRunning()) return;
        
        mDataBlinkAnimator = android.animation.ObjectAnimator.ofFloat(ivDataActivity, "alpha", 1.0f, 0.2f);
        mDataBlinkAnimator.setDuration(500);
        mDataBlinkAnimator.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        mDataBlinkAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        mDataBlinkAnimator.start();
    }

    private void stopDataBlink() {
        if (mDataBlinkAnimator != null) {
            mDataBlinkAnimator.cancel();
            mDataBlinkAnimator = null;
        }
        if (ivDataActivity != null) ivDataActivity.setAlpha(1.0f);
    }

    private boolean isInternetAvailable() {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            // V18.6.4: Migrado de getActiveNetworkInfo() (deprecada API 29) a NetworkCapabilities
            android.net.Network net = cm.getActiveNetwork();
            if (net == null) return false;
            android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(net);
            return caps != null && caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            Log.e(TAG, "isInternetAvailable: Error checking connection", e);
            return false;
        }
    }


    @Override
    public void onRdsText(String text) {
        if (mSessionController != null) {
            mSessionController.onRdsText(text);
        }
        runOnUiThread(() -> {
            if (mRdsManager != null) {
                mRdsManager.onRdsText(text);
                mHasRdsLock = mRdsManager.hasRdsLock();
                if (mMediaSessionManager != null) {
                    mMediaSessionManager.updateRds(text);
                }
            }
            if (mUiController != null) {
                mUiController.updateRDSText(text);
            }
        });
    }


    @Override
    public void onRdsPty(String pty) {
        if (mSessionController != null) {
            mSessionController.onRdsPty(pty);
        }
        runOnUiThread(() -> {
            if (mRdsManager != null) {
                mRdsManager.onRdsPty(pty);
                mCurrentPty = mRdsManager.getCurrentPty();
            }
            if (mUiController != null) {
                mUiController.updatePTY(pty);
            }
        });
    }




    @Override
    public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
        if (mSessionController != null) {
            mSessionController.onRdsStatus(afEnabled, taEnabled, tpEnabled);
        }
        runOnUiThread(() -> {
            if (mUiController != null) {
                mUiController.updateRdsStatus(afEnabled, taEnabled, tpEnabled);
            } else {
                if (ivAfIcon != null)
                    ivAfIcon.setAlpha(afEnabled ? 1.0f : 0.2f);
                if (ivTaIcon != null)
                    ivTaIcon.setAlpha(taEnabled ? 1.0f : 0.2f);
                if (ivTpIcon != null)
                    ivTpIcon.setAlpha(tpEnabled ? 1.0f : 0.2f);
            }
            Log.d(TAG, "Engine RDS Status: AF=" + afEnabled + " TA=" + taEnabled + " TP=" + tpEnabled);
        });
    }

    @Override
    public void onRdsPi(String piCode) {
        if (mSessionController != null) {
            mSessionController.onRdsPi(piCode);
        }
        mCurrentPi = piCode;
        // V16.0: Persistir PI Code para búsqueda de logos en Supabase
        if (mRepository != null && mEngine != null) {
            int freq = mEngine.getCurrentFreq();
            mRepository.saveRdsPi(freq, piCode);
        }
        runOnUiThread(() -> {
            if (mRdsManager != null) {
                mRdsManager.onRdsPi(piCode);
            }
        });
    }

    private final RDSManager.RDSListener mRdsListener = new RDSManager.RDSListener() {
        @Override
        public void onRdsNameConfirmed(String name) {
            // V13.6: Persistir en repositorio por frecuencia para logos y favoritos
            if (mRepository != null && mEngine != null) {
                int freq = mEngine.getCurrentFreq();
                mRepository.saveRdsName(freq, name);

                // V5.3: RDS PS Substitution (La variable reside en mRdsManager)
                runOnUiThread(() -> updateFrequencyDisplay(freq, name));

                // V21.2: Recargar logo con PS actual (prefs RDS_* puede ir un frame detrás por .apply())
                if (mLogoManager != null) {
                    mLogoManager.updateStationLogo(freq, mCurrentBand, null);
                }

                if (mPresetManager != null) {
                    mPresetManager.updateCardVisuals(-1, freq, mCurrentBand);
                }
            }
        }

        @Override
        public void onRdsMetadataUpdated() {
            // Futuras acciones cuando cambien metadatos globales
        }

        @Override
        public int getCurrentFrequency() {
            return mEngine != null ? mEngine.getCurrentFreq() : 0;
        }

        @Override
        public int getCurrentBand() {
            return mCurrentBand;
        }
    };

    @Override
    public void onDxLocalChanged(boolean isLocal) {
        if (mSessionController != null) {
            mSessionController.onDxLocalChanged(isLocal);
        }
        runOnUiThread(() -> {
            if (btnLocDx != null) {
                btnLocDx.setSelected(isLocal);
                btnLocDx.setAlpha(1.0f);
                // V9: LOCAL=radio_loc_p (active/filled), DX=radio_loc_n (normal/outline)
                setImageResourceIfChanged(btnLocDx, isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
            }
        });
    }

    @Override
    public void onScanStatusChanged(boolean scanning) {
        if (mSessionController != null) {
            mSessionController.onScanStatusChanged(scanning);
        }
        runOnUiThread(() -> {
            mIsScanning = scanning; // V13.9: Track global scanning state
            if (mScanManager != null) {
                mScanManager.applyEngineScanState(scanning);
            }
            if (!scanning && mScanManager != null && mScanManager.getStationAdapter() != null) {
                // Si el escaneo terminó automáticamente, podemos actualizar algún indicador si
                // existiera
                Log.d(TAG, "Scan finished callback received");
            }
            
            // V13.9: Al terminar el escaneo, forzamos un refresco completo para cargar logos y nombres
            if (!scanning && mEngine != null) {
                int currentFreq = mEngine.getCurrentFreq();
                mLastFreq = -1; // Force trigger
                handleFrequencyChange(currentFreq);
            }
        });
    }

    @Override
    public void onRawEvent(int code, String data) {
        if (mSessionController != null) {
            mSessionController.onRawEvent(code, data);
        }
        // Forward to engineering dialog if open
        if (mEngineeringDialog != null && mEngineeringDialog.isShowing()) {
            mEngineeringDialog.addRdsLog(data);
        }
        if (mQs6EngineeringDialog != null && mQs6EngineeringDialog.isShowing()) {
            mQs6EngineeringDialog.addRdsLog(data);
        }
    }

    @Override
    public void onSignalUpdate(int rssi, int snr) {
        if (mSessionController != null) {
            mSessionController.onSignalUpdate(rssi, snr);
        }
        runOnUiThread(() -> {
            if (mEngineeringDialog != null && mEngineeringDialog.isShowing()) {
                mEngineeringDialog.updateSignalQuality(rssi, snr);
            }
            if (mQs6EngineeringDialog != null && mQs6EngineeringDialog.isShowing()) {
                mQs6EngineeringDialog.updateSignalQuality(rssi, snr);
            }
        });
    }

    private int mTestClickCount = 0;
    private long mTestStartTime = 0;

    // V8.5: Credits Easter Egg Variables (Restored)
    private int mCreditsClickCount = 0;
    private long mCreditsStartTime = 0;

    private void sendMcuKey(int key) {
        if (mHardwareManager != null) mHardwareManager.sendMcuKey(key);
    }

    private ImageView ivDynamicBackground;

    private final RadioServiceController.ServiceListener mServiceListener = new RadioServiceController.ServiceListener() {
        @Override
        public void onModeDetected(FmMode mode) {
            mMode = mode; // Se asigna sincronamente antes de volver a la cola de eventos
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                Log.d(TAG, "Modo de funcionamiento detectado: " + mMode);

                if (mMode == FmMode.FM_MT8163) {
                    mRepository = new com.example.openradiofm.data.repository.RadioRepository(MainActivity.this, true);
                } else {
                    mRepository = new com.example.openradiofm.data.repository.RadioRepository(MainActivity.this, false);
                }

                if (mRepository != null) {
                    mRepository.setDataActivityListener(active -> {
                        MainActivity.this.runOnUiThread(() -> {
                            // V16.3: El cambio de contador debe ser en el UI thread para evitar desincronización
                            if (active) mActiveDataOps++;
                            else if (mActiveDataOps > 0) mActiveDataOps--;
                            
                            updateDataActivityUI();
                        });
                    });
                }

                // Database RDS PI
                if (mRdsDb == null) {
                    mRdsDb = new com.example.openradiofm.data.source.RdsDatabase(MainActivity.this);
                }

                // Inicializar Managers que dependen del repo/db
                if (mPresetManager == null) {
                    mPresetManager = new PresetManager(MainActivity.this, mRepository, mPrefs, PRESETS_COUNT);
                    mPresetManager.bindViews(findViewById(android.R.id.content), mIsV3);
                    mPresetManager.refreshPresetsCache(mCurrentBand);
                    mPresetManager.refreshButtons(mCurrentBand);
                }

                if (mDialogManager == null) {
                    mDialogManager = new DialogManager(MainActivity.this);
                    setupCustomNameEditing();
                }

                if (mScanManager == null) {
                    mScanManager = new ScanManager(MainActivity.this);
                }

                if (mRdsManager == null) {
                    mRdsManager = new RDSManager(MainActivity.this, findViewById(android.R.id.content), mRdsDb,
                            mRdsListener);
                    setupRdsText();
                }
            });
        }

        @Override
        public void onEngineReady(RadioEngine engine) {
            mEngine = engine;
            // Si RadioMediaService ya registró callback en el motor compartido (QS6/K706), combinar (no perder metadata Auto).
            com.example.openradiofm.data.source.RadioEngineCallback existingCb = null;
            if (mEngine instanceof com.example.openradiofm.data.source.QS6Engine) {
                existingCb = ((com.example.openradiofm.data.source.QS6Engine) mEngine).getCallback();
            } else if (mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
                existingCb = ((com.example.openradiofm.data.source.K706Engine) mEngine).getCallback();
            }
            if (existingCb != null && existingCb != MainActivity.this) {
                mEngine.setCallback(new com.example.openradiofm.data.source.CompositeRadioEngineCallback(
                        MainActivity.this, existingCb));
            } else {
                mEngine.setCallback(MainActivity.this);
            }

            if (mEngine instanceof K706Engine) {
                runOnUiThread(() -> requestReadPhoneStateForK706IfNeeded());
            }

            // V5.5: Sincronizar managers con el nuevo motor
            if (mPlaybackManager != null) {
                mPlaybackManager.setEngine(engine);
            }
            if (mDeviceManager != null) {
                mDeviceManager.init(engine, mPlaybackManager, mMediaSessionManager,
                        mServiceController, mRdsManager, mRepository, mPollingExecutor);
            }

            // Inicializar controlador de sesión compartido usando el mismo motor y playback manager
            try {
                if (mSessionController == null) {
                    mSessionController = new RadioSessionController(
                            MainActivity.this,
                            mEngine,
                            mPlaybackManager,
                            mPrefs,
                            getSharedPreferences("RadioStationNames", Context.MODE_PRIVATE)
                    );
                    // Listener opcional: refrescar algunos elementos de UI cuando cambie el estado global
                    mSessionController.addListener(state -> {
                        // Por ahora solo sincronizamos banda/frecuencia básicos si el engine está listo
                        if (state != null && mEngine != null) {
                            mLastFreq = state.freqKhz > 0 ? state.freqKhz : mLastFreq;
                            mCurrentBand = state.band;
                        }
                    });
                }
            } catch (Exception e) {
                Log.w(TAG, "No se pudo inicializar RadioSessionController en MainActivity", e);
            }

            // Si el motor no se ha inicializado todavía (ej: K706), lo hacemos aquí
            if (mEngine.getCurrentFreq() <= 0) {
                mEngine.init(MainActivity.this);
            }

            mCurrentBand = mEngine.getCurrentBand();

            // V20.0: Encapsular lógica de post-inicialización para permitir retardo táctico
            final Runnable postInitAction = () -> {
                // V18.6.3: Asegurar sintonización al arranque de cero
                if (mEngine != null && mEngine.getCurrentFreq() <= 0 && mLastFreq > 0) {
                    Log.d(TAG, "Startup: Tuning to last frequency " + mLastFreq);
                    mEngine.tune(mLastFreq);
                }

                startStatusPolling();

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (mEngine != null) {
                        showToast("Hardware: " + mEngine.getEngineName());
                        if (mEngine.getEngineName() != null && mEngine.getEngineName().contains("QS6")
                                && mPrefs != null && !mPrefs.getBoolean("pref_qs6_firmware_notice_shown", false)) {
                            mPrefs.edit().putBoolean("pref_qs6_firmware_notice_shown", true).apply();
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                try {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.qs6_audio_firmware_notice_title)
                                            .setMessage(R.string.qs6_audio_firmware_notice)
                                            .setPositiveButton(R.string.close, null)
                                            .show();
                                } catch (Exception ignored) {}
                            }, 2800);
                        }
                        refreshPresetsCache();
                        refreshPresetButtons();
                        refreshRadioStatus();
                        
                        // V20.3/V21.1: Agresivo siempre que estemos en Cold Start. 
                        // En recreación (layout switch) delegar en el init del motor si ya hay audio, 
                        // pero si detectamos mute injustificado, forzar recuperación.
                        // V18.6.5: NO desmutear si LIVE streaming está activo (evita audio duplicado).
                        boolean liveActive = mOnlineStreamManager != null && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading());
                        if (mPlaybackManager != null && !liveActive) {
                            if (!mIsRecreating) {
                                Log.d(TAG, "Startup Audio Recovery (Cold Start): Forzando desmuteo");
                                mPlaybackManager.setMute(false);
                            } else if (mMuteState) {
                                Log.d(TAG, "Startup Audio Recovery (Recreation): Detectado mute previo, forzando recuperación");
                                mPlaybackManager.setMute(false);
                            }
                        } else if (liveActive) {
                            Log.d(TAG, "Startup Audio Recovery: LIVE activo, no se desmutea la radio FM");
                        }
                    }
                });
            };

            // V20.0: Retardo de estabilización de 500ms específico para QS6 (NWD) 
            // para evitar DeadObjectException durante la transición de layout/inflado.
            if (mEngine != null && mEngine.getEngineName().contains("QS6")) {
                Log.d(TAG, "QS6 Startup: Aplicando pausa de estabilización de 500ms...");
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(postInitAction, 500);
            } else {
                postInitAction.run();
            }
        }

        @Override
        public void onServiceConnected(IRadioServiceAPI service) {
            mRadioService = service;
            Log.d(TAG, "onServiceConnected: Servicio AIDL legado (HCN) recibido. Inicializando motor...");

            // Solo manejamos MT8163/HCN aquí, ya que QS6 se inicializa de forma asíncrona e
            // independiente.
            if (mMode == FmMode.FM_MT8163) {
                // V21.3: Si ya tenemos el motor MT8163 creado, reutilizarlo en vez de crear uno nuevo.
                // Esto previene la duplicidad de hilos de polling y fugas de recursos.
                if (mEngine instanceof com.example.openradiofm.data.source.MT8163Engine) {
                    Log.i(TAG, "onServiceConnected: Reutilizando instancia existente del motor MT8163.");
                    ((com.example.openradiofm.data.source.MT8163Engine) mEngine).updateService(mRadioService);
                    return;
                }

                RadioEngine engine = new com.example.openradiofm.data.source.MT8163Engine(mRadioService);
                if (engine.init(MainActivity.this)) {
                    onEngineReady(engine);
                } else {
                    Log.e(TAG, "Fallo al inicializar el motor con el servicio AIDL");
                }
            } else {
                Log.w(TAG, "Ignorando onServiceConnected legado porque mMode es " + mMode);
            }
        }

        @Override
        public void onServiceDisconnected() {
            stopStatusPolling();
            mRadioService = null;
        }
    };

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
        
        // V19.2: Forzar que el control de volumen por hardware afecte al stream de musica 
        // desde el inicio. Esto evita el bug de doble pulsacion en MTK.
        setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);

        if (savedInstanceState != null) {
            mLastFreq = savedInstanceState.getInt("mLastFreq", -1);
            mIsV3 = savedInstanceState.getBoolean("mIsV3", false);
            mIsRecreating = true;
            Log.d(TAG, "State Restored: Freq=" + mLastFreq + " (Recreation detected)");
        }

        // V18.6: MCU and BT logic controlled by HardwareManager
        mHardwareManager = new HardwareManager(this);
        mHardwareManager.registerReceivers();

        // V3.0: Layout Selection
        mPrefs = getSharedPreferences("RadioPresets", MODE_PRIVATE); // Init prefs early
        
        // V21.3: Forzar habilitación de banda AM para evitar inestabilidad en motores HW (MTK8259)
        // Se ha eliminado la opción de desactivarlo en Ajustes Premium.
        if (!mPrefs.getBoolean("pref_enable_am", true)) {
            mPrefs.edit().putBoolean("pref_enable_am", true).apply();
            Log.i(TAG, "AM Band forced to enabled for stability.");
        }
        
        mIsV3 = mPrefs.getBoolean("pref_layout_v3", false);
        mIsSimpleLayout = mPrefs.getBoolean("pref_layout_simple", false);

        // V4.8: Manejo de Barra de Estado (Fullscreen condicional)
        applyStatusBarVisibility();

        if (mIsSimpleLayout) {
            setContentView(R.layout.activity_simple_radio);
            mUiController = new SimpleLayoutController(this);
        } else if (mIsV3) {
            setContentView(R.layout.activity_main_v3);
            mUiController = new V3LayoutController(this);
        } else {
            setContentView(R.layout.activity_main);
            mUiController = new MainLayoutController(this);
        }

        // V21.0: Initialize the active UI Controller
        if (mUiController != null) {
            mUiController.initViews(findViewById(android.R.id.content));
        }

        // V15.6: Aplicar tipografía global inmediatamente tras cargar el layout
        applyFonts();

        // V3.8: Premium Background Binding
        ivDynamicBackground = findViewById(R.id.ivDynamicBackground);

        if (checkSelfPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
        }

        // V13: Inicializar Managers agnósticos
        mLogoManager = new LogoManager(this);
        if (mRepository != null) {
            mSupabaseSyncManager = new com.example.openradiofm.data.source.SupabaseSyncManager(this, mRepository.getSupabaseSource());
        }
        mServiceController = new RadioServiceController(this, mPrefs, mServiceListener);

        // V16: NightMode y History Managers
        mNightModeManager = new NightModeManager(this, mPrefs, freq -> {
            // V18.6.4: Pasar el nombre RDS actual para no perderlo al cambiar de skin
            String currentName = (mRdsManager != null) ? mRdsManager.getDisplayName(freq) : mLastPs;
            updateFrequencyDisplay(freq, currentName);
        });
        mHistoryManager = new HistoryManager(this, mPrefs);
        mMediaSessionManager = new MediaSessionManager(this);
        mMediaSessionManager.connect();
        
        mControlPanelManager = new ControlPanelManager(this);

        // V5.5: Inicializar PlaybackManager y DeviceManager
        mPlaybackManager = new PlaybackManager(this);
        mPlaybackManager.init(mEngine, new PlaybackManager.PlaybackListener() {
            @Override
            public void onMuteStateChanged(boolean isMuted) {
                mMuteState = isMuted;
                runOnUiThread(() -> {
                    if (mUiController != null) {
                        mUiController.updateMute(isMuted);
                    }
                    
                    ImageButton btnMute = findViewById(R.id.btnMute);
                    if (btnMute != null) {
                        btnMute.setSelected(isMuted);
                        // boolean isMTK = mEngine != null && mEngine.getEngineName().contains("MTK"); // Removed as per instruction

                        if (isMuted) {
                            setImageResourceIfChanged(btnMute, R.drawable.radio_mute_p);
                        } else {
                            setImageResourceIfChanged(btnMute, R.drawable.radio_mute_n);
                        }
                        // V2.5: Preservar tinte noche si activo
                        Object savedFilter = btnMute.getTag(R.id.tag_color_filter);
                        if (savedFilter instanceof Integer) {
                            btnMute.setColorFilter((Integer) savedFilter, android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                        btnMute.setAlpha(1.0f);
                        if (!isMuted) btnMute.setSelected(false);
                    }
                });
            }

            @Override
            public void onMediaCommand(String command) {
                runOnUiThread(() -> {
                    switch (command) {
                        case "ACTION_NEXT":
                            if (mPresetManager != null) mPresetManager.playNextPreset();
                            break;
                        case "ACTION_PREV":
                            if (mPresetManager != null) mPresetManager.playPrevPreset();
                            break;
                    }
                });
            }
        });
        mPlaybackManager.registerMediaReceiver();

        mDeviceManager = new DeviceManager(this);


        // V2.0: Cargar fondo personalizado si existe
        mLogoManager.loadCustomBackground();
        mLogoManager.loadCarLogo();

        mSimpleLayoutManager = new SimpleLayoutManager(this);

        // V13: Cargar última frecuencia guardada
        if (mLastFreq == -1) {
            mLastFreq = mPrefs.getInt("pref_last_freq", 87500);
        }

        if (mIsSimpleLayout) {
            mSimpleLayoutManager.initViews(findViewById(android.R.id.content));
        }

        // Bind Views
        tvFrequency = findViewById(R.id.tvFrequency);
        if (tvFrequency != null) {
            tvFrequency.setEllipsize(null);
            tvFrequency.setSingleLine(false); // Necesario para que el Autosizing no se confunda con ellipsize
            tvFrequency.setMaxLines(1);
        }
        tvRdsName = findViewById(R.id.tvRdsName); // V5
        tvRdsInfo = findViewById(R.id.tvRdsInfo);

        // V4.3: New UI Elements
        tvPty = findViewById(R.id.tvPty);
        ivSignalLevel = findViewById(R.id.ivSignalLevel);

        btnLocDx = findViewById(R.id.btnLocDx);
        btnBand = findViewById(R.id.btnBand);
        btnPowerOff = findViewById(R.id.btnPowerOff);

        ivBandIndicator = findViewById(R.id.ivBandIndicator);
        ivUnitLabel = findViewById(R.id.ivUnitLabel);
        tvDigitalClock = findViewById(R.id.tvDigitalClock);

        // V18.5: Inicializar Reloj Digital
        mClockHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mClockRunnable = new Runnable() {
            @Override
            public void run() {
                if (tvDigitalClock != null && tvDigitalClock.getVisibility() == View.VISIBLE) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                    tvDigitalClock.setText(sdf.format(new java.util.Date()));
                }
                mClockHandler.postDelayed(this, 10000); // 10 segs (suficiente para HH:mm)
            }
        };

        // Aplicar preferencias iniciales
        applyLogoModePreference();
        ivFavoriteIndicator = findViewById(R.id.ivFavoriteIndicator);
        ivStereoIcon = findViewById(R.id.ivStereoIcon);
        ivAfIcon = findViewById(R.id.ivAfIcon);
        ivTaIcon = findViewById(R.id.ivTaIcon);
        ivTpIcon = findViewById(R.id.ivTpIcon);
        ivDataActivity = findViewById(R.id.ivDataActivity);
        ivDataActivityIcon = findViewById(R.id.ivDataActivityIcon);
        setupOnlineStreaming();

        // El listener de mRepository se configura asíncronamente en onModeDetected

        // V9.9: RDS Icons must be dimmed by default, not gone.
        // V5.0: RDS Icons - Ahora usan mEngine (sin bifurcación por modo)
        if (ivAfIcon != null) {
            ivAfIcon.setAlpha(0.2f);
            ivAfIcon.setOnClickListener(v -> {
                animateButton(ivAfIcon);
                if (mEngine != null)
                    mEngine.toggleRdsFeature(1); // AF
            });
        }
        if (ivTaIcon != null) {
            ivTaIcon.setAlpha(0.2f);
            ivTaIcon.setOnClickListener(v -> {
                animateButton(ivTaIcon);
                if (mEngine != null)
                    mEngine.toggleRdsFeature(2); // TA
            });
        }
        if (ivTpIcon != null) {
            ivTpIcon.setAlpha(0.2f);
            ivTpIcon.setOnClickListener(v -> {
                animateButton(ivTpIcon);
                if (mEngine != null)
                    mEngine.toggleRdsFeature(0); // RDS global
            });
        }

        // V16.2: Skin cycling remains in Car Logo (as it's more visual)
        android.view.View ivCarLogo = findViewById(R.id.ivCarLogo);
        if (ivCarLogo != null) {
            ivCarLogo.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = mThemeManager.cycleSkin();
                applySkin(next);
                showToast("Skin: " + next.displayName);
            });
            ivCarLogo.setOnLongClickListener(v -> {
                if (mDialogManager != null) mDialogManager.showHistoryDialog();
                return true;
            });
        }

        // Indicators Binding - REMOVED

        // Configurar controles (Delegados a ControlPanelManager)
        if (mControlPanelManager != null) mControlPanelManager.initViews();

        // Configurar indicadores de estado (Eliminados)
        // setupIndicators();

        // V16.2: Inicializar ThemeManager
        mThemeManager = new com.example.openradiofm.ui.theme.ThemeManager(this);
        mThemeManager.setLayoutPrefs(mPrefs); 
        // V2.5: Eliminado SkinAppliedListener redundante. applySkin() ahora gestiona todo secuencialmente.
        applySkin(mThemeManager.getCurrentSkin());
        checkAndApplyNightMode(); // V4: Automatic Night Mode

        // V18.6: Auto-hide bottom controls initialization
        bottomControls = findViewById(R.id.bottomControls);
        mAutoHideHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mAutoHideRunnable = () -> hideBottomControls();

        // En Layout V3, interceptar toques en el fondo para mostrar controles
        if (mIsV3) {
            android.view.View root = findViewById(R.id.rootLayout);
            if (root != null) {
                root.setOnTouchListener((v, event) -> {
                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        if (mControlsHidden) {
                            showBottomControls();
                        } else {
                            resetAutoHideTimer();
                        }
                    }
                    return false; // Permitir que otros elementos reciban el toque
                });
            }
            resetAutoHideTimer();
        }
    
        // Seeking Logic (Delegated to ControlPanelManager)

        applyFonts();

        // V8.5: Easter Egg (Credits) - Restored
        setupCreditsEasterEgg();

        if (mServiceController != null)
            mServiceController.start();

        // V20.0: Ajuste automático por densidad (DPI)
        adjustLayoutForDPI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "onNewIntent: App ya activa, refrescando parámetros (Single Instance).");
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // V18.6.5: Si LIVE streaming está activo, NO restaurar el canal FM.
        // El ExoPlayer sigue emitiendo en segundo plano y forzar FM causa audio duplicado.
        boolean liveActive = mOnlineStreamManager != null && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading());
        android.util.Log.d(TAG, "onResume: liveActive=" + liveActive);
        
        if (liveActive) {
            // LIVE sigue sonando: no tocar el canal de audio ni el mute
            return;
        }
        
        // V21.4: Re-conectar si la app vuelve al frente y el servicio nativo fue matado (ej. por Music Player)
        if (mRadioService == null && mMode == FmMode.FM_MT8163 && mServiceController != null) {
            android.util.Log.w(TAG, "onResume: mRadioService nulo (posible force-stop). Reactivando servicio...");
            try {
                android.content.Intent wakeIntent = new android.content.Intent("com.hcn.autoradio.FMRADIO_START");
                wakeIntent.setPackage("com.hcn.autoradio");
                wakeIntent.addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(wakeIntent);
            } catch (Exception ignored) {}
            
            mServiceController.start();
            // La reconexión es asíncrona. onServiceConnected disparará updateService() y enforceAudioRecovery()
            return; 
        }
        
        // V4.8: En K706, es mejor dejar que el Engine gestione el foco y el canal.
        if (mPlaybackManager != null) {
            mPlaybackManager.resumeIfMutedBySystem();
            
            // Si NO está muteado, nos aseguramos de que el canal FM esté activo en el MCU
            if (!mPlaybackManager.isMuted() && mEngine != null) {
                mEngine.switchToFmAudio();
            }
        }

        // Botón AutoScan alineado con el HAL (evita estado “verde” si el escaneo terminó en segundo plano)
        if (mEngine != null && mScanManager != null) {
            boolean scanning = mEngine.isScanning();
            mIsScanning = scanning;
            mScanManager.applyEngineScanState(scanning);
        }
    }


    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        android.util.Log.d(TAG, "onConfigurationChanged: Nueva configuración detectada");
        if (mPresetManager != null) {
            mPresetManager.refreshButtons(mCurrentBand);
        }

        // V10: Manejar cambio de modo noche sin recrear la Activity
        if (mPrefs != null && mPrefs.getBoolean("pref_night_mode_auto", false)) {
            checkAndApplyNightMode();
        }
    }

    // setupControlButtons and setupSeekButtons moved to ControlPanelManager

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
        if (ivMainLogo != null && !mIsSimpleLayout) {
            // Click normal: Cambiar Color (Ciclar Skin)
            ivMainLogo.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = mThemeManager.cycleSkin();
                applySkin(next);
                showToast("Skin: " + next.displayName);
            });

            // Long click: Historial
            ivMainLogo.setOnLongClickListener(v -> {
                mDialogManager.showHistoryDialog();
                return true;
            });
        }

        // V18.6: Reloj Digital también permite ciclar skin
        tvDigitalClock = findViewById(R.id.tvDigitalClock);
        if (tvDigitalClock != null) {
            tvDigitalClock.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = mThemeManager.cycleSkin();
                applySkin(next);
                showToast("Skin: " + next.displayName);
            });
            // Long click: Mostrar diálogo de personalización directamente
            tvDigitalClock.setOnLongClickListener(v -> {
                mDialogManager.showPremiumSettingsDialog();
                return true;
            });
        }
    }

    @Override
    protected void onDestroy() {
        // V21.0: Cancel all pending UI tasks immediately
        mMainHandler.removeCallbacksAndMessages(null);
        if (mAutoHideHandler != null) mAutoHideHandler.removeCallbacksAndMessages(null);
        if (mClockHandler != null) mClockHandler.removeCallbacksAndMessages(null);

        // V20.0: Check more robust for recreation (recreate() or config change)
        // or just moving to background (not finishing).
        // This prevents muting MT8163 on layout change.
        boolean recreating = isChangingConfigurations() || !isFinishing();
        Log.d(TAG, "onDestroy: Limpiando recursos. recreating=" + recreating + " (isFinishing=" + isFinishing() + ")");
        
        // V5.5: Limpieza delegada a DeviceManager (Actualizado V20.0 con flag de persistencia)
        stopStatusPolling();
        
        if (mStationInfoExecutor != null) {
            try {
                mStationInfoExecutor.shutdownNow();
            } catch (Exception ignored) {}
            mStationInfoExecutor = null;
        }

        if (mMediaSessionManager != null) {
            mMediaSessionManager.disconnect();
        }

        if (mDeviceManager != null) {
            mDeviceManager.releaseAllResources(recreating);
        }

        // Recursos no gestionados por DeviceManager (legacy específico)
        try {
            if (mHardwareManager != null) {
                mHardwareManager.unregisterReceivers();
            }
        } catch (Exception e) {}

        if (mHiddenPlayer != null) {
            mHiddenPlayer.release();
            mHiddenPlayer = null;
        }

        if (mOnlineStreamManager != null) {
            mOnlineStreamManager.release();
            mOnlineStreamManager = null;
        }

        // V18.6.2: Explicit cleanup for all managers
        if (mPresetManager != null) mPresetManager.release();
        if (mLogoManager != null) mLogoManager.release();
        if (mRdsManager != null) mRdsManager.release();
        if (mUiController != null) mUiController.release(); // Assuming the controller follows the pattern

        super.onDestroy();
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
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * V15.6: Obtiene la fuente configurada actualmente en las preferencias.
     */
    public android.graphics.Typeface getSystemTypeface() {
        int fontIdx = mPrefs.getInt("pref_font_type", 0);
        try {
            int[] fontRes = { 0, R.font.bebas, R.font.digital, R.font.inter, R.font.orbitron, R.font.formula1 };
            if (fontIdx > 0 && fontIdx < fontRes.length) {
                return androidx.core.content.res.ResourcesCompat.getFont(this, fontRes[fontIdx]);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading font: " + fontIdx, e);
        }
        return android.graphics.Typeface.DEFAULT_BOLD;
    }

    /**
     * V15.6: Aplica la tipografía seleccionada a todos los elementos del layout de
     * forma recursiva.
     */
    public void applyFonts() {
        android.graphics.Typeface typeface = getSystemTypeface();
        applyRecursiveFont(findViewById(android.R.id.content), typeface);

        // V2.1: Especial para mPresetManager que maneja sus propios arrays
        if (mPresetManager != null) {
            mPresetManager.applyFonts(typeface);
        }

        // V18.5: Reloj Digital
        TextView tvDigitalClock = findViewById(R.id.tvDigitalClock);
        if (tvDigitalClock != null) {
            tvDigitalClock.setTypeface(typeface);
        }
    }

    /**
     * V15.6: Aplica una fuente de forma recursiva a todos los TextViews (y
     * derivados) en un árbol de vistas.
     */
    public void applyRecursiveFont(View v, android.graphics.Typeface tf) {
        if (v == null)
            return;
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyRecursiveFont(vg.getChildAt(i), tf);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(tf);
        }
    }

    public void seekUp() {
        if (mEngine != null) {
            runOnUiThread(() -> {
                if (mLogoManager != null) mLogoManager.clearLogo();
            });
            mEngine.seekUp();
        }
    }

    public void seekDown() {
        if (mEngine != null) {
            runOnUiThread(() -> {
                if (mLogoManager != null) mLogoManager.clearLogo();
            });
            mEngine.seekDown();
        }
    }

    public void refreshPresetButtons() {
        if (mPresetManager != null) {
            mPresetManager.refreshButtons(mCurrentBand);
        }
    }

    /** V16.2: Delegado a ThemeManager */
    public int getSkinDrawableId() {
        return mThemeManager != null ? mThemeManager.getSkinDrawableId() : R.drawable.bg_glass_card_premium;
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
     * V17.2: Acción centralizada para el Easter Egg de créditos.
     */
    private void handleCreditsClick() {
        long now = System.currentTimeMillis();
        if (mCreditsClickCount == 0 || (now - mCreditsStartTime) > 3000) {
            mCreditsClickCount = 1;
            mCreditsStartTime = now;
        } else {
            mCreditsClickCount++;
        }

        if (mCreditsClickCount >= 5) {
            mCreditsClickCount = 0;
            if (mDialogManager != null)
                mDialogManager.showCreditsDialog();
        } else if (mCreditsClickCount == 1) {
            // V17.4: Restaurar Historial en el primer click (comportamiento GPS)
            if (mDialogManager != null)
                mDialogManager.showHistoryDialog();
        }
    }

    private void setupCreditsEasterEgg() {
        if (tvFrequency != null) {
            tvFrequency.setOnClickListener(v -> handleCreditsClick());

            // V16.2: Pulsación larga para editar nombre (RDS PS)
            tvFrequency.setOnLongClickListener(v -> {
                if (mDialogManager != null) {
                    mDialogManager.showEditNameDialog();
                    return true;
                }
                return false;
            });
        }

        // V16.2: También en el contenedor para facilitar la interacción
        android.view.View boxFrequency = findViewById(R.id.boxFrequency);
        android.view.View boxIconsTop = findViewById(R.id.boxIconsTopLayout2);
        
        View.OnClickListener clickListener = v -> handleCreditsClick();
        View.OnLongClickListener longClickListener = v -> {
            if (mDialogManager != null) {
                mDialogManager.showEditNameDialog();
                return true;
            }
            return false;
        };

        if (boxFrequency != null) {
            boxFrequency.setOnClickListener(clickListener);
            boxFrequency.setOnLongClickListener(longClickListener);
        }
        if (boxIconsTop != null) {
            boxIconsTop.setOnClickListener(clickListener);
            boxIconsTop.setOnLongClickListener(longClickListener);
        }
    }

    /**
     * V2.0: Crea la carpeta /sdcard/RadioLogos/ si no existe.
     * Esto soluciona el bug reportado donde la app no creaba la carpeta
     * automáticamente.
     */
    private void createRadioLogosFolder() {
        try {
            // V21.1: Carpeta principal legacy para evitar confusión al usuario.
            // Fallback a app-specific solo si el sistema bloquea /sdcard.
            java.io.File legacyDir = new java.io.File("/sdcard/RadioLogos/");
            boolean legacyOk = (legacyDir.exists() || legacyDir.mkdirs()) && legacyDir.canWrite();
            if (!legacyOk) {
                java.io.File external = getExternalFilesDir(null);
                java.io.File appDir = new java.io.File((external != null ? external : getFilesDir()), "RadioLogos");
                if (!appDir.exists()) appDir.mkdirs();
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
        // V21.1: Unificar lógica con LogoManager (Glide/downsample + compat storage)
        if (mLogoManager != null) {
            mLogoManager.loadCustomBackground();
            return;
        }
    }

    public void refreshPresetsCache() {
        if (mPresetManager != null) {
            mPresetManager.refreshPresetsCache(mCurrentBand);
        }
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
        if (mEngine == null)
            return;

        // V16.2/V21.1: Refrescar UI no crítica con throttling para evitar jitter en el hilo principal
        long now = System.currentTimeMillis();
        final boolean shouldUpdateDataUi = (now - mLastDataActivityUiTime) >= DATA_ACTIVITY_UI_INTERVAL_MS;
        final boolean shouldCheckNight = (now - mLastNightModeCheckTime) >= NIGHT_MODE_CHECK_INTERVAL_MS;
        if (shouldUpdateDataUi) mLastDataActivityUiTime = now;
        if (shouldCheckNight) mLastNightModeCheckTime = now;
        if (shouldUpdateDataUi || shouldCheckNight) {
            runOnUiThread(() -> {
                if (shouldUpdateDataUi) updateDataActivityUI();
                if (shouldCheckNight) checkAndApplyNightMode(); // Transiciones por tiempo
            });
        }

        // V18.6: Si estamos reproduciendo streaming online, saltamos la interrogación síncrona al hardware.
        // El hardware en MT8163 se apaga (muere) al tomar el audio, por lo que consultarle congela la UI.
        boolean isStreaming = mOnlineStreamManager != null && mOnlineStreamManager.isPlaying();

        // V18.6: Sincronizar estado visual del Mute con el sistema real.
        // Importante: En MT8163 el mute es por HW (fm_mute) y NO debe depender de STREAM_MUSIC
        // salvo compatibilidad explícita (pref_mt8163_global_stream_mute).
        if (mPlaybackManager != null && mEngine != null &&
            ("MTK8259_8667".equals(mEngine.getEngineName()) ||
             ("MT8163".equals(mEngine.getEngineName()) && mPrefs.getBoolean("pref_mt8163_global_stream_mute", false)))) {
            android.media.AudioManager am = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                boolean isSystemMuted;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // Nota OEM: en algunas ROMs isStreamMute() no refleja correctamente el estado real.
                    // Usamos también volumen==0 como señal fiable.
                    boolean muteFlag = am.isStreamMute(android.media.AudioManager.STREAM_MUSIC);
                    boolean volumeZero = am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) == 0;
                    isSystemMuted = muteFlag || volumeZero;
                } else {
                    // Fallback para versiones antiguas o checking volume
                    isSystemMuted = am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) == 0;
                }
                
                // Si el sistema NO está muteado pero nuestra UI SI, sincronizamos hacia DESMUTEADO
                if (!isSystemMuted && mPlaybackManager.isMuted()) {
                    Log.d(TAG, "Mute sync: System unmuted, updating UI/Engine");
                    mPlaybackManager.setMute(false);
                }
            }
        }

        int freq = isStreaming ? mLastFreq : mEngine.getCurrentFreq();
        if (freq <= 0)
            return;

        int band = isStreaming ? mCurrentBand : mEngine.getCurrentBand();
        boolean isStereo = isStreaming || mEngine.isStereo();
        boolean isLocal = !isStreaming && mEngine.isDxLocal();

        // V2.6: MASTER GUARD - Bloquear refresco pesado si no hay cambios externos
        // V2.7: Desactivar timeout expired (SPRD jitter fix). Solo refrescar si cambia estado real.
        boolean stateChanged = (freq != mLastRefreshFreq || band != mLastRefreshBand);

        if (!stateChanged) {
            // Solo actualizamos visibilidades inmediatas (Mute/Stream) y salimos
            final boolean fIsNight = (mThemeManager != null && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
            runOnUiThread(() -> {
               if (ivStereoIcon != null) {
                   setVisibilityIfChanged(ivStereoIcon, isStereo ? View.VISIBLE : View.INVISIBLE);
                   // V2.6: Proteger tinte noche al actualizar visibilidad
                   if (fIsNight) {
                       int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
                       setColorFilterIfChanged(ivStereoIcon, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                   }
               }
               if (btnLocDx != null) btnLocDx.setSelected(isLocal);
               // V18.6.4: Actualizar color de señal en el path de polling (para MT8163 que no tiene callback activo)
               if (ivSignalLevel != null) {
                   int sigColor = isStereo ? android.graphics.Color.parseColor("#00E676")
                           : android.graphics.Color.parseColor("#FFD600");
                   ivSignalLevel.setColorFilter(sigColor, android.graphics.PorterDuff.Mode.SRC_IN);
               }
            });
            return;
        }

        // V4.8.6: Limpieza inmediata de UI al cambiar de sintonía (Sin esperar a carga asíncrona)
        runOnUiThread(() -> {
            if (mRdsManager != null) mRdsManager.reset(true);
            if (mUiController != null) {
                mUiController.updateLogo(null);
                mUiController.updateRDS("");
            } else if (mLogoManager != null) {
                mLogoManager.clearLogo();
            }
        });

        mLastRefreshFreq = freq;
        mLastRefreshBand = band;
        mLastFullRefreshTime = System.currentTimeMillis();

        // Fix v4.5.1: SIEMPRE sincronizar mCurrentBand y refrescar presets
        if (band != mCurrentBand) {
            String logMsg = "Band shift detected: " + mCurrentBand + " -> " + band;
            mCurrentBand = band;
            Log.d(TAG, logMsg);
            if (mPresetManager != null) {
                mPresetManager.refreshPresetsCache(band);
                runOnUiThread(() -> mPresetManager.refreshButtons(band));
            }
        }

        // V4.3: Hardware Toggle for AM (REMOVED v21.3)
        // boolean amEnabled = mPrefs.getBoolean("pref_enable_am", true);
        // boolean isAm = (band == BAND_AM1 || band == BAND_AM2);
        // if (isAm && !amEnabled) {
        //     mEngine.bandCycle();
        //     return;
        // }

        String bandCacheKey = band + "_" + freq;

        if (freq != mLastFreq) {
            Log.d(TAG, "Ultima frecuencia guardada: " + freq);
        }

        final int fFreq = freq;
        final int fBand = band;
        final boolean fIsAm = (band == BAND_AM1 || band == BAND_AM2);
        final boolean fIsLocal = isLocal;
        final boolean fIsStreaming = isStreaming;

        // V18.6/V21.1: Recuperación de info de emisora en executor único (sin crear hilos en loop)
        final int seq = mStationInfoSeq.incrementAndGet();
        mLastStationInfoRequestedSeq = seq;
        if (mStationInfoExecutor == null) {
            mStationInfoExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
        }
        mStationInfoExecutor.execute(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (seq != mLastStationInfoRequestedSeq) return;

            com.example.openradiofm.data.model.RadioStation station = null;
            if (mRepository != null && !mIsScanning) {
                // V21.2: PS en vivo (RDSManager) gana sobre RDS_* en prefs (histórico de otra emisora en la misma frecuencia).
                // Solo si la frecuencia pedida es la del sintonizador FM actual (no streaming).
                String livePs = null;
                if (!fIsStreaming && mRdsManager != null && mEngine != null && fFreq == mEngine.getCurrentFreq()) {
                    String cn = mRdsManager.getConfirmedName();
                    if (cn != null && !cn.trim().isEmpty()) {
                        livePs = cn.trim();
                    }
                }
                station = mRepository.getStationInfo(fFreq, null, livePs);
            }
            if (seq != mLastStationInfoRequestedSeq) return;

            final String rdsName = (station != null) ? station.getName() : "";
            mLastPs = rdsName; // Sincronizar campo para acceso externo

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (seq != mLastStationInfoRequestedSeq) return;

                boolean isNight = (mThemeManager != null && mThemeManager.getActiveSkin()
                        == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);

                if (mUiController != null) {
                    mUiController.updateFrequency(fFreq, rdsName, fIsAm);
                    mUiController.applySkin(isNight);
                    mUiController.updateBandIndicator(fBand);

                    if (mLogoManager != null) {
                        String cachedLogo = mLogoCachePerBand.get(fBand + "_" + fFreq);
                        mLogoManager.updateStationLogo(fFreq, fBand, cachedLogo);
                    }

                    boolean isFav = isStationMemorized(fFreq);
                    int pIndex = getPresetIndex(fFreq);
                    mUiController.updateFavoriteIndicator(isFav, pIndex, isNight);
                    
                    // V2.6: Re-asegurar tinte noche completo tras actualizaciones parciales.
                    // mUiController.applySkin() solo cubre textos (freq, rds, pty, unit).
                    // NightModeManager cubre TODO: botones, iconos RDS, presets, reloj.
                    if (isNight && mNightModeManager != null) {
                        mNightModeManager.applyNightModeColors(mLastFreq);
                    }
                } else {
                    int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
                    if (isNight) {
                        setColorFilterIfChanged(ivUnitLabel, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                        setTextColorIfChanged(tvFrequency, nightBlue);
                    } else {
                        setColorFilterIfChanged(ivUnitLabel, null, null);
                        setTextColorIfChanged(tvFrequency, android.graphics.Color.WHITE);
                    }
                    updateFrequencyDisplay(fFreq, rdsName);
                    updateBandImage(fBand);
                }

                if (mMediaSessionManager != null) {
                    float freqDisplay = fFreq / 1000.0f;
                    String freqStr = String.format(java.util.Locale.US, "%.1f MHz", freqDisplay);
                    mMediaSessionManager.updateMetadata(rdsName, freqStr, null);
                }

                if (btnLocDx != null) {
                    btnLocDx.setSelected(fIsLocal);
                    setImageResourceIfChanged(btnLocDx, fIsLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
                }

                sendWidgetUpdateIntent(fFreq, fBand, rdsName);
            });
        });
    }

    /**
     * V5.2: Broadcast to K706 Launcher Widget
     */
    private void sendWidgetUpdateIntent(int freq, int band, String rdsName) {
        // V5.2: Broadcast to K706/MTK/Topway Launcher Widgets
        
        // V2.5: Deep Guard to avoid "Permission Denial" Binder flood
        if (freq == mLastBroadcastFreq && band == mLastBroadcastBand && 
            ((rdsName == null && mLastBroadcastPs == null) || (rdsName != null && rdsName.equals(mLastBroadcastPs)))) {
            return;
        }

        mLastBroadcastFreq = freq;
        mLastBroadcastBand = band;
        mLastBroadcastPs = rdsName;

        try {
            // 1. BROADCAST ESTÁNDAR K706 / QUICKFISH
            android.content.Intent qfIntent = new android.content.Intent("com.qf.radio.update_action");
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
                nativeFreqInt = freq / 10; 
            }
            qfIntent.putExtra("com.qf.radio.update_action_key", freqStr);
            qfIntent.putExtra("com.qf.radio.update_action_freq_key", nativeFreqInt);
            qfIntent.putExtra("com.qf.radio.update_action_band_key", band);
            qfIntent.putExtra("com.qf.radio.update_action_preset_key", getPresetIndex(freq));
            qfIntent.putExtra("com.qf.radio.update_action_searching_key", false);
            String widgetName = (rdsName != null && !rdsName.isEmpty() && !rdsName.equals("STATION NAME")
                    && !rdsName.equals("STATION")) ? rdsName : "";
            qfIntent.putExtra("com.qf.radio.update_action_name_key", widgetName);
            qfIntent.setPackage("com.android.auto.autohome");
            sendBroadcast(qfIntent);

            // 2. BROADCAST ESTÁNDAR LAUNCHER MTK (Vista Radio Original)
            android.content.Intent mtkIntent = new android.content.Intent("com.android.launcher.action.UPDATE_RADIO");
            mtkIntent.putExtra("frequency", freqStr);
            mtkIntent.putExtra("name", widgetName);
            mtkIntent.putExtra("band", band < 3 ? "FM" : "AM");
            mtkIntent.putExtra("isRadio", true);
            mtkIntent.putExtra("stereo", mEngine != null && mEngine.isStereo());
            sendBroadcast(mtkIntent);

            // 3. BROADCAST ESPECÍFICO TOPWAY / TS
            android.content.Intent tsIntent = new android.content.Intent("com.ts.main.radio.update");
            tsIntent.putExtra("freq", nativeFreqInt);
            tsIntent.putExtra("band", band);
            tsIntent.putExtra("name", widgetName);
            tsIntent.putExtra("isRadio", true);
            sendBroadcast(tsIntent);

            // 4. ACTUALIZACIÓN DE FUENTE DEL SISTEMA (Para activar widget de Radio)
            android.content.Intent sourceIntent = new android.content.Intent("com.android.launcher.action.UPDATE_SOURCE");
            sourceIntent.putExtra("source", 1); // 1 suele ser Radio
            sourceIntent.putExtra("sourceName", "Radio");
            sendBroadcast(sourceIntent);

        } catch (Exception ex) {
            Log.e(TAG, "Error updating launcher widgets", ex);
        }
    }

    /**
     * Calcula una calidad de señal estimada basándose en flags disponibles.
     * Algoritmo basado en CHIP_RADIO_SNR_RSSI.md
     */

    private void updateBandImage(int band) {
        int resId;
        if (band == BAND_FM1) {
            resId = R.drawable.radio_fm1;
        } else if (band == BAND_FM2) {
            resId = R.drawable.radio_fm2;
        } else if (band == BAND_FM3) {
            resId = R.drawable.radio_fm3;
        } else if (band == BAND_AM1) {
            resId = R.drawable.radio_am1;
        } else if (band == BAND_AM2) {
            resId = R.drawable.radio_am2;
        } else {
            resId = R.drawable.radio_fm1; // Fallback
        }

        if (ivBandIndicator != null) {
            setImageResourceIfChanged(ivBandIndicator, resId);
            if (btnBand != null)
                setImageResourceIfChanged(btnBand, R.drawable.radio_band_n);
        } else if (btnBand != null) {
            setImageResourceIfChanged(btnBand, resId);
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
    // V4.8.6: Ahora acepta rdsName para evitar hilos redundantes si ya se obtuvo antes.
    void updateFrequencyDisplay(int freq, String rdsName) {
        if (freq <= 0) return;
        
        runOnUiThread(() -> {
            if (mUiController != null) {
                mUiController.updateFrequency(freq, rdsName, mCurrentBand >= 3);
                
                // Add RDS updates through controller
                if (mRdsManager != null) {
                    boolean isNight = (mThemeManager != null && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
                    mUiController.applySkin(isNight);
                }
                
                // Favorite indicator update
                boolean isFavorite = isStationMemorized(freq);
                int idx = getPresetIndex(freq);
                boolean isNight = (mThemeManager != null && mThemeManager.getActiveSkin() == ThemeManager.Skin.NIGHT_MODE);
                mUiController.updateFavoriteIndicator(isFavorite, idx, isNight);
                
            } else {
                // FALLBACK LEGACY
                legacy_updateFrequencyDisplay(freq, rdsName);
            }
        });
    }

    private void legacy_updateFrequencyDisplay(int freq, String rdsName) {
        if (tvFrequency == null) return;
        // ... (Mantengo la lógica por si el mUiController fuera nulo accidentalmente)
        // Pero idealmente esta lógica se mueva a cada controlador
        String freqStr = (mCurrentBand >= 3) ? String.valueOf(freq) : String.format(java.util.Locale.US, "%.1f", freq / 1000.0);
        if (rdsName != null && !rdsName.isEmpty()) setTextIfChanged(tvFrequency, rdsName);
        else setTextIfChanged(tvFrequency, freqStr);
    }


    /**
     * V16.2: Delegado a ThemeManager.applySkin().
     * Conservamos el wrapper público para compatibilidad con DialogManager y NightModeManager.
     */
    public void applySkin(com.example.openradiofm.ui.theme.ThemeManager.Skin skin) {
        if (mThemeManager != null) mThemeManager.applySkin(skin);
        
        boolean isNight = (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
        boolean isClear = (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);

        if (mUiController != null) {
            mUiController.applySkin(isNight);
        } else if (mIsSimpleLayout && mSimpleLayoutManager != null) {
            // Legacy/Fallback for SimpleLayoutManager directly
            mSimpleLayoutManager.applyColors(isNight);
        }
        
        // V2.5: Aplicación centralizada de colores azul noche al final de applySkin
        if (mNightModeManager != null) {
            if (isNight) {
                mNightModeManager.applyNightModeColors(mLastFreq);
            } else {
                mNightModeManager.resetNightModeColors(mLastFreq);
            }
        }

        // CLEAR: iconos de botones en negro (y al salir, restaurar).
        // V2.6: NO ejecutar cuando isNight — NightModeManager gestiona los filtros de botones.
        // applyClearButtonIconTint(false) borra los filtros Y tags, destruyendo el tintado azul.
        if (!isNight) {
            applyClearButtonIconTint(isClear);
        }

        // Shared Clock Visibility Color
        if (tvDigitalClock != null) {
            if (isNight) {
                tvDigitalClock.setTextColor(getResources().getColor(R.color.night_blue_primary, null));
            } else {
                boolean isLight = (mThemeManager != null
                        && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
                tvDigitalClock.setTextColor(isLight ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
            }
        }
    }

    private void applyClearButtonIconTint(boolean enabled) {
        final int tint = android.graphics.Color.BLACK;
        final android.graphics.PorterDuff.Mode mode = android.graphics.PorterDuff.Mode.SRC_IN;

        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown,
                R.id.btnFavPrev, R.id.btnFavNext,
                R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff
        };

        for (int id : buttonIds) {
            android.view.View v = findViewById(id);
            if (v instanceof android.widget.ImageView) {
                if (enabled) setColorFilterIfChanged((android.widget.ImageView) v, tint, mode);
                else setColorFilterIfChanged((android.widget.ImageView) v, null, null);
            }
        }

        // Iconos de estado que se ven como "botones" en algunos layouts
        int[] iconIds = {
                R.id.ivAfIcon, R.id.ivTaIcon, R.id.ivTpIcon,
                R.id.ivStereoIcon, R.id.ivDataActivityIcon
        };
        for (int id : iconIds) {
            android.view.View v = findViewById(id);
            if (v instanceof android.widget.ImageView) {
                if (enabled) setColorFilterIfChanged((android.widget.ImageView) v, tint, mode);
                else setColorFilterIfChanged((android.widget.ImageView) v, null, null);
            }
        }
    }

    // V16: applyNightModeColors() y resetNightModeColors() movidos a NightModeManager

    // V4: Frequency Step Helpers (Manual Tuning)
    public void stepFreqUp() {
        if (mEngine != null) {
            runOnUiThread(() -> {
                if (mLogoManager != null) mLogoManager.clearLogo();
            });
            mEngine.stepUp();
            refreshRadioStatus();
        }
    }

    public void stepFreqDown() {
        if (mEngine != null) {
            runOnUiThread(() -> {
                if (mLogoManager != null) mLogoManager.clearLogo();
            });
            mEngine.stepDown();
            refreshRadioStatus();
        }
    }

    // V4: Swipe Listener Class

    // V16: Delegaciones a NightModeManager
    public void checkAndApplyNightMode() {
        if (mNightModeManager != null) mNightModeManager.checkAndApplyNightMode();
    }

    // V16: Delegaciones a HistoryManager
    private void addToHistory(int freq) {
        if (mHistoryManager != null) mHistoryManager.addToHistory(freq);
    }

    public void saveFavoritesToFile() {
        if (mHistoryManager != null) mHistoryManager.saveFavoritesToFile();
    }

    public void loadFavoritesFromFile() {
        if (mHistoryManager != null) mHistoryManager.loadFavoritesFromFile();
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
                getString(R.string.language_japanese),
                getString(R.string.language_hungarian)
        };

        String[] languageCodes = { "es", "en", "ru", "ro", "uk", "sr", "fr", "zh", "ja", "hu" };
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_language_selector, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        
        android.widget.GridView gvLanguages = dialogView.findViewById(R.id.gvOptions);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancelSelect);

        // Adaptador simple para el GridView
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this, R.layout.item_language, languages) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                String currentLang = mPrefs.getString("app_language", "es");
                if (languageCodes[position].equals(currentLang)) {
                    tv.setBackgroundResource(R.drawable.bg_glass_card_blue); // Resaltar actual
                    boolean isLight = (mThemeManager != null
                            && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
                    tv.setTextColor(isLight ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
                return tv;
            }
        };

        gvLanguages.setAdapter(adapter);
        gvLanguages.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLang = languageCodes[position];
            String selectedLangName = languages[position];

            setLocale(selectedLang);
            showStyledToast(String.format(getString(R.string.language_changed), selectedLangName));

            dialog.dismiss();
            recreate();
        });

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.8f);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
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
            case "fr":
                langName = getString(R.string.language_french);
                break;
            case "de":
                langName = getString(R.string.language_german);
                break;
            case "pt":
                langName = getString(R.string.language_portuguese);
                break;
            case "it":
                langName = getString(R.string.language_italian);
                break;
            case "hu":
                langName = getString(R.string.language_hungarian);
                break;
            case "zh":
                langName = getString(R.string.language_chinese);
                break;
            case "ja":
                langName = getString(R.string.language_japanese);
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

    /**
     * K706: sin READ_PHONE_STATE el sistema no notifica llamadas y la FM puede seguir sonando.
     */
    private void requestReadPhoneStateForK706IfNeeded() {
        if (isFinishing() || isDestroyed()) return;
        if (!(mEngine instanceof K706Engine)) return;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            ((K706Engine) mEngine).registerPhoneStateListenerIfPermitted();
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            ((K706Engine) mEngine).registerPhoneStateListenerIfPermitted();
            return;
        }
        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQ_READ_PHONE_STATE_K706);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                showToast("Permisos concedidos");
                if (mDialogManager != null)
                    mDialogManager.showSaveLoadFavoritesDialog();
            } else {
                showToast("Se requieren permisos de almacenamiento para guardar favoritos.");
            }
        } else if (requestCode == REQ_READ_PHONE_STATE_K706) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mEngine instanceof K706Engine) {
                    ((K706Engine) mEngine).registerPhoneStateListenerIfPermitted();
                }
                showToast("Llamadas: la radio se silenciará automáticamente");
            } else {
                showToast("Sin permiso de teléfono la FM puede seguir sonando durante llamadas");
            }
        }
    }

    // V15: Wrappers de compatibilidad para diálogos llamados desde otras clases
    public void showEngineSelector() {
        if (mDialogManager != null)
            mDialogManager.showEngineSelector();
    }

    // V5.5: setMute delegado a mPlaybackManager (ver PlaybackManager.java)
    private void setMute(boolean mute) {
        if (mPlaybackManager != null) {
            mPlaybackManager.setMute(mute);
        }
    }

    public void onSeekUpEvent() {
        Log.d(TAG, "onSeekUpEvent call");
        if (mEngine != null)
            mEngine.seekUp();
    }

    public void onSeekDownEvent() {
        Log.d(TAG, "onSeekDownEvent call");
        if (mEngine != null)
            mEngine.seekDown();
    }




    /**
     * V4.3: Helper to check if a frequency is stored in presets
     */
    private boolean isStationMemorized(int freq) {
        if (mPresetManager == null)
            return false;

        for (int i = 0; i < PRESETS_COUNT; i++) {
            if (mPresetManager.getFreq(i) == freq)
                return true;
        }
        return false;
    }

    /**
     * V4.3: Helper to get the 1-based index of a preset frequency
     */
    private int getPresetIndex(int freq) {
        if (mPresetManager == null)
            return 0;

        for (int i = 0; i < PRESETS_COUNT; i++) {
            if (mPresetManager.getFreq(i) == freq)
                return i + 1;
        }
        return 0;
    }

    /**
     * V11.7: Muestra el diálogo de Escaneo Selectivo (Solo K706).
     */
    /**
     * V9.5: AutoScan Toggle — click 1 inicia, click 2 detiene.
     */

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
        if (mgr == null)
            return false;
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
        if (mgr == null)
            return false;
        try {
            java.lang.reflect.Method m = mgr.getClass().getMethod(methodName);
            m.invoke(mgr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendMcuTunerCmd(byte subCmd, byte param1, byte param2) {
        if (mHardwareManager != null) mHardwareManager.sendMcuTunerCmd(subCmd, param1, param2);
    }

    /**
     * V13.9: Aplica la visibilidad de la barra de estado según las preferencias y el layout.
     */
    public void applyStatusBarVisibility() {
        if (mPrefs == null) return;
        boolean showStatusBarV2 = mPrefs.getBoolean("pref_show_status_bar_v2", false);
        runOnUiThread(() -> {
            if (showStatusBarV2) {
                getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });
    }

    /**
     * V18.5: Alterna entre el logo del coche y el reloj digital.
     */
    public void applyLogoModePreference() {
        if (mPrefs == null) return;
        int logoMode = mPrefs.getInt("pref_logo_mode", 0); // 0=Car, 1=Clock
        runOnUiThread(() -> {
            ImageView ivCarLogo = findViewById(R.id.ivCarLogo);
            if (tvDigitalClock != null) {
                if (logoMode == 1) {
                    tvDigitalClock.setVisibility(View.VISIBLE);
                    if (ivCarLogo != null) ivCarLogo.setVisibility(View.GONE);
                    mClockHandler.removeCallbacks(mClockRunnable);
                    mClockHandler.post(mClockRunnable);
                } else {
                    tvDigitalClock.setVisibility(View.GONE);
                    if (ivCarLogo != null) {
                        ivCarLogo.setVisibility(View.VISIBLE);
                        mLogoManager.loadCarLogo();
                    }
                    mClockHandler.removeCallbacks(mClockRunnable);
                }
            }
        });
    }

    /**
     * V13.9: Centralized reset when frequency changes.
     */
    private void handleFrequencyChange(int freq) {
        if (freq == mLastFreq)
            return;

        mLogoUiGeneration.incrementAndGet();
        mLastFreq = freq;
        mLastLogoUrl = ""; // Force logo reload
        mCurrentPi = null;
        mCurrentPty = null;
        mLastPs = ""; // V18.6.4: Clear cached RDS name to avoid stale display on new freq
        mHasRdsLock = false;

        if (mRdsManager != null) {
            // MT8163: handleFrequencyChange puede venir desde un hilo de polling del engine.
            // RDSManager.reset(true) toca TextViews (setText) y puede crashear por CalledFromWrongThreadException.
            // La limpieza visual ya se hace más abajo dentro de runOnUiThread().
            mRdsManager.reset(false);
        }

        if (mOnlineStreamManager != null && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading())) {
            mOnlineStreamManager.stopStream();
        }

        runOnUiThread(() -> {
            if (tvRdsName != null) {
                tvRdsName.setText("");
                tvRdsName.setVisibility(View.VISIBLE);
            }
            if (tvRdsInfo != null) {
                tvRdsInfo.setText("");
                tvRdsInfo.setVisibility(View.VISIBLE);
            }
            if (ivStereoIcon != null) {
                ivStereoIcon.setVisibility(View.INVISIBLE);
            }
            if (tvPty != null) {
                tvPty.setText(getString(R.string.pty_none));
            }

            // Clear logos immediately
            if (mLogoManager != null) {
                ImageView ivMainLogo = findViewById(R.id.ivMainLogo);
                if (ivMainLogo != null)
                    mLogoManager.applyFallbackLogo(ivMainLogo);
                mLogoManager.updateDynamicBackground(null);
            }
        });

        // V13.9: Durante el escaneo, OMITIMOS guardar historial y persistencia para mayor fluidez
        if (mIsScanning) {
            Log.d(TAG, "Scanning in progress: skipping history/persistence for freq " + freq);
            return;
        }

        // V13.9: Logic moved from refreshRadioStatus
        if (mPrefs != null) {
            if (mPrefs.getBoolean("pref_save_history", true)) {
                addToHistory(freq);
            }
            mPrefs.edit().putInt("pref_last_freq", freq).apply();
            Log.d(TAG, "Last freq saved & History updated: " + freq);
        }

        Log.d(TAG, "Frequency changed to " + freq + " - UI Reset triggered");

        // V16: Update MediaSession Metadata
        if (mMediaSessionManager != null) {
            String title = (freq / 1000.0) + " MHz";
            mMediaSessionManager.updateMetadata(title, "OpenRadioFM", null);
        }

        // V5.2: Update Launcher Widget on frequency shift
        sendWidgetUpdateIntent(freq, mCurrentBand, null);
    }

    // V18.6: Métodos para ocultación automática de controles
    public void resetAutoHideTimer() {
        if (mAutoHideHandler == null || mAutoHideRunnable == null) return;
        mAutoHideHandler.removeCallbacks(mAutoHideRunnable);
        if (mPrefs.getBoolean("pref_auto_hide_controls", false) && mIsV3) {
            mAutoHideHandler.postDelayed(mAutoHideRunnable, 5000); // 5 segundos
        }
    }

    public void showBottomControls() {
        if (bottomControls == null) return;
        if (mControlsHidden) {
            mControlsHidden = false;
            bottomControls.animate()
                    .translationY(0)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .start();
        }
        resetAutoHideTimer();
    }

    public void hideBottomControls() {
        if (bottomControls == null || mControlsHidden) return;
        if (!mPrefs.getBoolean("pref_auto_hide_controls", false)) return;
        
        mControlsHidden = true;
        bottomControls.animate()
                .translationY(bottomControls.getHeight() + 100)
                .setDuration(500)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .start();
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (mIsV3 && mPrefs.getBoolean("pref_auto_hide_controls", false)) {
            if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                showBottomControls();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * V18.6: Centraliza el cambio de layout (V2 -> V3 -> Simple)
     */
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        // V18.x: Volante / teclas media = cambiar frecuencia (seek), no memorias
        switch (keyCode) {
            case android.view.KeyEvent.KEYCODE_MEDIA_NEXT:
            case android.view.KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                Log.d(TAG, "Hardware Key: NEXT -> seekUp");
                if (mEngine != null) {
                    mEngine.seekUp();
                    return true;
                }
                if (mPresetManager != null) mPresetManager.playNextPreset();
                return true;
            case android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case android.view.KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                Log.d(TAG, "Hardware Key: PREV -> seekDown");
                if (mEngine != null) {
                    mEngine.seekDown();
                    return true;
                }
                if (mPresetManager != null) mPresetManager.playPrevPreset();
                return true;
            case android.view.KeyEvent.KEYCODE_MEDIA_PLAY:
            case android.view.KeyEvent.KEYCODE_MEDIA_PAUSE:
            case android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                setMute(!mMuteState);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void toggleLayoutMode() {
        if (!mIsV3 && !mIsSimpleLayout) { // Estamos en V2 -> Ir a V3
            mPrefs.edit().putBoolean("pref_layout_v3", true).putBoolean("pref_layout_simple", false).apply();
            showToast("Layout: V3 (Horizontal)");
        } else if (mIsV3) { // Estamos en V3 -> Ir a Simple
            mPrefs.edit().putBoolean("pref_layout_v3", false).putBoolean("pref_layout_simple", true).apply();
            showToast("Layout: Simple (Minimalista)");
        } else { // Estamos en Simple -> Ir a V2
            mPrefs.edit().putBoolean("pref_layout_v3", false).putBoolean("pref_layout_simple", false).apply();
            showToast("Layout: V2 (Vertical)");
        }
        
        // V20.1: Pequeña pausa de seguridad antes de recrear para permitir que las SharedPreferences persistan 
        // y evitar una recreación "sucia" que el sistema pueda interpretar como un crash. (V20.3: Simplificado para evitar saltos de AudioFocus)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::recreate, 400);
    }

    /**
     * V2.3: Helpers to avoid flicker by only updating views if values actually change.
     */
    public static boolean setTextIfChanged(android.widget.TextView tv, String text) {
        if (tv == null || text == null) return false;
        if (!tv.getText().toString().equals(text)) {
            tv.setText(text);
            return true;
        }
        return false;
    }

    public static void setTextColorIfChanged(android.widget.TextView tv, int color) {
        if (tv == null) return;
        if (tv.getCurrentTextColor() != color) {
            tv.setTextColor(color);
        }
    }

    public static void setVisibilityIfChanged(android.view.View v, int visibility) {
        if (v == null) return;
        if (v.getVisibility() != visibility) {
            v.setVisibility(visibility);
        }
    }

    public static void setColorFilterIfChanged(android.widget.ImageView iv, Integer color, android.graphics.PorterDuff.Mode mode) {
        if (iv == null) return;
        // Nota: No hay un getter directo y fiable para el filtro en APIs antiguas,
        // pero setFilter con el mismo valor suele ser menos costoso que invalidate() total.
        // Optamos por un tag para trackear el estado manual.
        Object current = iv.getTag(R.id.tag_color_filter);
        if (color == null) {
            if (current != null) {
                iv.clearColorFilter();
                iv.setTag(R.id.tag_color_filter, null);
            }
        } else if (!color.equals(current)) {
            iv.setColorFilter(color, mode);
            iv.setTag(R.id.tag_color_filter, color);
        }
    }

    public static void setImageResourceIfChanged(android.widget.ImageView iv, int resId) {
        if (iv == null) return;
        Object current = iv.getTag(R.id.tag_image_res);
        if (current == null || (int)current != resId) {
            iv.setImageResource(resId);
            iv.setTag(R.id.tag_image_res, resId);
            
            // V2.5: Re-aplicar el filtro de color si existe en el tag.
            // Android a veces limpia el colorFilter al llamar a setImageResource.
            Object filterColor = iv.getTag(R.id.tag_color_filter);
            if (filterColor instanceof Integer) {
                iv.setColorFilter((Integer) filterColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }
    /**
     * V20.0: Detecta automáticamente si la pantalla tiene una densidad alta (DPI)
     * y ajusta las guías y el tamaño de los botones para que no se deformen.
     */
    private void adjustLayoutForDPI() {
        if (!mIsV3) return; 
        
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density = metrics.density;
        float width = metrics.widthPixels;
        float height = metrics.heightPixels;
        float aspectRatio = width / height;
        
        android.util.Log.d(TAG, "DPI/Aspect Detection: density=" + density + " ratio=" + aspectRatio + " res=" + width + "x" + height);

        // V20.0: Optimización para pantallas Cuadradas/Altas (5:4 o 4:3)
        // Estas pantallas suelen tener un ancho en DP menor (sw) que las 16:9
        boolean isTallScreen = aspectRatio < 1.45f;

        if (density > 1.0f || isTallScreen) {
            // 1. Subir la guía del texto (0.36 -> 0.32) para dar mucho más espacio abajo
            // En pantallas altas (5:4), usamos un valor intermedio si no ha sido ya ajustado por el XML base
            android.view.View guidelineView = findViewById(R.id.guideline_v3_freq_bottom);
            if (guidelineView instanceof androidx.constraintlayout.widget.Guideline) {
                float targetPercent = isTallScreen ? 0.38f : 0.32f;
                ((androidx.constraintlayout.widget.Guideline) guidelineView).setGuidelinePercent(targetPercent);
            }
            
            // 2. Capar el tamaño de la fuente de la frecuencia
            if (tvFrequency instanceof androidx.appcompat.widget.AppCompatTextView) {
                int maxSp = isTallScreen ? 75 : 72; // Un poco más en pantallas altas
                ((androidx.appcompat.widget.AppCompatTextView)tvFrequency).setAutoSizeTextTypeUniformWithConfiguration(
                    12, maxSp, 2, android.util.TypedValue.COMPLEX_UNIT_SP);
            }

            // 3. Achicar los iconos RDS (originalmente 56dp)
            int rdsSize = (int) ((isTallScreen ? 38 : 42) * density);
            scaleView(findViewById(R.id.ivAfIcon), rdsSize, rdsSize);
            scaleView(findViewById(R.id.ivTaIcon), rdsSize, rdsSize);
            scaleView(findViewById(R.id.ivTpIcon), rdsSize, rdsSize);
            
            // 4. Achicar el logo principal
            int logoSize = (int) ((isTallScreen ? 44 : 48) * density);
            scaleView(findViewById(R.id.ivMainLogo), logoSize, logoSize);

            android.util.Log.d(TAG, "DPI dynamic scaling applied (Tall=" + isTallScreen + ")");
        }
    }

    private void scaleView(android.view.View v, int w, int h) {
        if (v != null) {
            android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
            if (params != null) {
                params.width = w;
                params.height = h;
                v.setLayoutParams(params);
            }
        }
    }
}


