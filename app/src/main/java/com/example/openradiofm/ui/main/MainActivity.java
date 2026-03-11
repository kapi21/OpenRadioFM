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
import android.graphics.Bitmap;


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
    public boolean mControlsHidden = false;
    private android.view.View bottomControls;
    private android.os.Handler mAutoHideHandler;
    private Runnable mAutoHideRunnable;

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


    // V11: RDS PI Database Identification
    private com.example.openradiofm.data.source.RdsDatabase mRdsDb;
    private String mCurrentPi = null;

    // V13: Gestor de Presets (Reducción de MainActivity)
    public PresetManager mPresetManager;
    public int mLastFreq = -1;
    public String mLastPs = ""; // V18.6: Almacena el nombre RDS/Custom actual
    public boolean mHasRdsLock = false;
    public String mCurrentPty = null;
    public String mLastLogoUrl = "";
    public java.util.Map<String, String> mLogoCachePerBand = new java.util.HashMap<>();
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

    // V9.9: RDS Debugging Tracker
    public K706EngineeringDialog mEngineeringDialog = null;

    public int mCurrentBand = 0;

    public int getCurrentBand() {
        return mCurrentBand;
    }



    // V18.6: StationAdapter and ScannedStation moved to separate files

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
    public void gotoNextFavorite() {
        if (mEngine == null || mPresetManager == null)
            return;

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
    public void gotoPreviousFavorite() {
        if (mEngine == null || mPresetManager == null)
            return;

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
        handleFrequencyChange(freqKhz);
        runOnUiThread(() -> updateFrequencyDisplay(freqKhz));
    }

    @Override
    public void onBandChanged(int band) {
        runOnUiThread(() -> {
            mCurrentBand = band;
            if (mPresetManager != null) {
                mPresetManager.refreshPresetsCache(band);
                mPresetManager.refreshButtons(band);
            }
            updateBandImage(band);
        });
    }

    @Override
    public void onStereoChanged(boolean stereo) {
        runOnUiThread(() -> {
            if (ivStereoIcon != null)
                ivStereoIcon.setVisibility(stereo ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
            if (ivSignalLevel != null) {
                // V12.4: Actualizar color de señal según estado Stereo (Verde=Stereo,
                // Amarillo=Mono)
                int color = stereo ? android.graphics.Color.parseColor("#00E676")
                        : android.graphics.Color.parseColor("#FFD600");
                ivSignalLevel.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        });
    }

    @Override
    public void onRdsName(final String name) {
        runOnUiThread(() -> {
            if (mRdsManager != null) {
                mRdsManager.onRdsName(name);
                mHasRdsLock = mRdsManager.hasRdsLock();
                // V16: Sincronizar con MediaSession (Android Auto)
                if (mMediaSessionManager != null) {
                    String freqText = String.format(java.util.Locale.US, "%.2f MHz", (mEngine != null ? mEngine.getCurrentFreq() : 0) / 1000.0);
                    mMediaSessionManager.updateMetadata(name, freqText, null);
                }
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
        if (mOnlineStreamManager != null && ivDataActivityIcon != null) {
            if (mOnlineStreamManager.isPlaying()) {
                // Streaming active -> RED
                setVisibilityIfChanged(ivDataActivityIcon, View.VISIBLE);
                setColorFilterIfChanged(ivDataActivityIcon, android.graphics.Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (mOnlineStreamManager.isLoading()) {
                setColorFilterIfChanged(ivDataActivityIcon, android.graphics.Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // V17.4: Al limpiar filtros, respetar el color azul noche si el modo noche está activo
                if (mNightModeManager != null && mNightModeManager.isNightTime() && 
                    mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
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
                int freq = (mEngine != null) ? mEngine.getCurrentFreq() : -1;
                if (freq <= 0) return;

                // Obtener datos de la emisora actual (incluyendo streamUrl)
                com.example.openradiofm.data.model.RadioStation station = mRepository.getStationInfo(freq, null);
                if (station != null && station.getStreamUrl() != null && !station.getStreamUrl().isEmpty()) {
                    mOnlineStreamManager.toggleStream(station.getStreamUrl());
                    if (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading()) {
                        showToast("Iniciando Radio Online...");
                    } else {
                        showToast("Volviendo a Radio FM...");
                    }
                } else {
                    showToast("Streaming no disponible para esta emisora");
                }
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
                            runOnUiThread(() -> updateFrequencyDisplay(freq));
                        });
                        runOnUiThread(() -> updateFrequencyDisplay(freq));
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
            android.net.NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } catch (Exception e) {
            Log.e(TAG, "isInternetAvailable: Error checking connection", e);
            return false;
        }
    }


    @Override
    public void onRdsText(String text) {
        runOnUiThread(() -> {
            if (mRdsManager != null) {
                mRdsManager.onRdsText(text);
                mHasRdsLock = mRdsManager.hasRdsLock();
                // V16: Sincronizar con MediaSession (Android Auto)
                if (mMediaSessionManager != null) {
                    mMediaSessionManager.updateRds(text);
                }
            }
        });
    }


    @Override
    public void onRdsPty(String pty) {
        runOnUiThread(() -> {
            if (mRdsManager != null) {
                mRdsManager.onRdsPty(pty);
                mCurrentPty = mRdsManager.getCurrentPty();
            }
        });
    }




    @Override
    public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
        runOnUiThread(() -> {
            if (ivAfIcon != null)
                ivAfIcon.setAlpha(afEnabled ? 1.0f : 0.2f);
            if (ivTaIcon != null)
                ivTaIcon.setAlpha(taEnabled ? 1.0f : 0.2f);
            if (ivTpIcon != null)
                ivTpIcon.setAlpha(tpEnabled ? 1.0f : 0.2f);
            Log.d(TAG, "Engine RDS Status: AF=" + afEnabled + " TA=" + taEnabled + " TP=" + tpEnabled);
        });
    }

    @Override
    public void onRdsPi(String piCode) {
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
                runOnUiThread(() -> updateFrequencyDisplay(freq));

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
        runOnUiThread(() -> {
            if (btnLocDx != null) {
                btnLocDx.setSelected(isLocal);
                btnLocDx.setAlpha(1.0f);
                // V9: LOCAL=radio_loc_p (active/filled), DX=radio_loc_n (normal/outline)
                btnLocDx.setImageResource(isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
            }
        });
    }

    @Override
    public void onScanStatusChanged(boolean scanning) {
        runOnUiThread(() -> {
            mIsScanning = scanning; // V13.9: Track global scanning state
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
                updateFrequencyDisplay(currentFreq);
            }
        });
    }

    @Override
    public void onRawEvent(int code, String data) {
        // Forward to engineering dialog if open
        if (mEngineeringDialog != null && mEngineeringDialog.isShowing()) {
            mEngineeringDialog.addRdsLog(data);
        }
    }

    @Override
    public void onSignalUpdate(int rssi, int snr) {
        runOnUiThread(() -> {
            if (mEngineeringDialog != null && mEngineeringDialog.isShowing()) {
                mEngineeringDialog.updateSignalQuality(rssi, snr);
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
            mEngine.setCallback(MainActivity.this);

            // V5.5: Sincronizar managers con el nuevo motor
            if (mPlaybackManager != null) {
                mPlaybackManager.setEngine(engine);
            }
            if (mDeviceManager != null) {
                mDeviceManager.init(engine, mPlaybackManager, mMediaSessionManager,
                        mServiceController, mRdsManager, mRepository, mPollingExecutor);
            }

            // Si el motor no se ha inicializado todavía (ej: K706), lo hacemos aquí
            if (mEngine.getCurrentFreq() <= 0) {
                mEngine.init(MainActivity.this);
            }

            mCurrentBand = mEngine.getCurrentBand();
            startStatusPolling();

            runOnUiThread(() -> {
                showToast("Hardware: " + mEngine.getEngineName());
                refreshPresetsCache();
                refreshPresetButtons();
                refreshRadioStatus();
            });
        }

        @Override
        public void onServiceConnected(IRadioServiceAPI service) {
            mRadioService = service;
            Log.d(TAG, "onServiceConnected: Servicio AIDL legado (HCN) recibido. Inicializando motor...");

            // Solo manejamos MT8163/HCN aquí, ya que QS6 se inicializa de forma asíncrona e
            // independiente.
            if (mMode == FmMode.FM_MT8163) {
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

        if (savedInstanceState != null) {
            mLastFreq = savedInstanceState.getInt("mLastFreq", -1);
            mIsV3 = savedInstanceState.getBoolean("mIsV3", false);
            Log.d(TAG, "State Restored: Freq=" + mLastFreq);
        }

        // V18.6: MCU and BT logic controlled by HardwareManager
        mHardwareManager = new HardwareManager(this);
        mHardwareManager.registerReceivers();

        // V3.0: Layout Selection
        mPrefs = getSharedPreferences("RadioPresets", MODE_PRIVATE); // Init prefs early
        mIsV3 = mPrefs.getBoolean("pref_layout_v3", false);
        mIsSimpleLayout = mPrefs.getBoolean("pref_layout_simple", false);

        // V4.8: Manejo de Barra de Estado (Fullscreen condicional)
        applyStatusBarVisibility();

        if (mIsSimpleLayout) {
            setContentView(R.layout.activity_simple_radio);
        } else {
            setContentView(mIsV3 ? R.layout.activity_main_v3 : R.layout.activity_main);
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
        mNightModeManager = new NightModeManager(this, mPrefs, freq -> updateFrequencyDisplay(freq));
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
                    ImageButton btnMute = findViewById(R.id.btnMute);
                    if (btnMute != null) {
                        btnMute.setSelected(isMuted);
                        // V18.6: Forzar actualización de imagen según el estado real
                        if (isMuted) {
                            btnMute.setImageResource(R.drawable.radio_mute_p);
                            btnMute.setAlpha(1.0f);
                        } else {
                            btnMute.setImageResource(R.drawable.radio_mute_n);
                            btnMute.setAlpha(1.0f);
                            btnMute.setSelected(false); // Refuerzo
                        }
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

        // V16.2: Inicializar ThemeManager y registrar listener para Night Mode
        mThemeManager = new com.example.openradiofm.ui.theme.ThemeManager(this);
        mThemeManager.setLayoutPrefs(mPrefs); // V16.2: Pasar las SharedPreferences correctas
        mThemeManager.setSkinAppliedListener(skin -> {
            if (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
                if (mNightModeManager != null) mNightModeManager.applyNightModeColors(mLastFreq);
            } else {
                if (mNightModeManager != null) mNightModeManager.resetNightModeColors(mLastFreq);
            }
        });
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d(TAG, "onResume: Restaurando hardware de audio");
        
        // V4.8: En K706, es mejor dejar que el Engine gestione el foco y el canal.
        if (mPlaybackManager != null) {
            mPlaybackManager.resumeIfMutedBySystem();
            
            // Si NO está muteado, nos aseguramos de que el canal FM esté activo en el MCU
            if (!mPlaybackManager.isMuted() && mEngine != null) {
                mEngine.switchToFmAudio();
            }
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
        // V5.5: Limpieza delegada a DeviceManager
        stopStatusPolling();

        if (mDeviceManager != null) {
            mDeviceManager.releaseAllResources();
        }

        // V18.5: Limpiar reloj
        if (mClockHandler != null) {
            mClockHandler.removeCallbacks(mClockRunnable);
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

        // V16.2: Refrescar estado de conectividad/datos del icono de Supabase
        runOnUiThread(() -> {
            updateDataActivityUI();
            checkAndApplyNightMode(); // V18.x: Forzar check de modo noche periódico (transiciones por tiempo)
        });

        // V18.6: Si estamos reproduciendo streaming online, saltamos la interrogación síncrona al hardware.
        // El hardware en MT8163 se apaga (muere) al tomar el audio, por lo que consultarle congela la UI.
        boolean isStreaming = mOnlineStreamManager != null && mOnlineStreamManager.isPlaying();

        // V18.6: Sincronizar estado visual del Mute con el sistema real (Solo MTK para evitar regresiones en K706)
        if (mPlaybackManager != null && mEngine != null && "MTK8259_8667".equals(mEngine.getEngineName())) {
            android.media.AudioManager am = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                boolean isSystemMuted;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    isSystemMuted = am.isStreamMute(android.media.AudioManager.STREAM_MUSIC);
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
            runOnUiThread(() -> {
               if (ivStereoIcon != null) setVisibilityIfChanged(ivStereoIcon, isStereo ? View.VISIBLE : View.INVISIBLE);
               if (btnLocDx != null) btnLocDx.setSelected(isLocal);
            });
            return;
        }

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

        // V4.3: Hardware Toggle for AM
        boolean amEnabled = mPrefs.getBoolean("pref_enable_am", true);
        boolean isAm = (band == BAND_AM1 || band == BAND_AM2);
        if (isAm && !amEnabled) {
            mEngine.bandCycle();
            return;
        }

        String bandCacheKey = band + "_" + freq;

        if (freq != mLastFreq) {
            Log.d(TAG, "Ultima frecuencia guardada: " + freq);
        }

        final int fFreq = freq;
        final int fBand = band;
        final boolean fIsAm = (band == BAND_AM1 || band == BAND_AM2);
        final boolean fIsLocal = isLocal;

        // V18.6: Mover recuperación de información de emisora a hilo secundario
        new Thread(() -> {
            com.example.openradiofm.data.model.RadioStation station = null;
            if (mRepository != null && !mIsScanning) {
                station = mRepository.getStationInfo(fFreq, null);
            }
            final String rdsName = (station != null) ? station.getName() : "";
            mLastPs = rdsName; // V18.6: Sincronizar campo para acceso externo

            runOnUiThread(() -> {
                // Get State
                boolean isNight = (mThemeManager != null && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
                int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
                int white = android.graphics.Color.WHITE;

                // Sync color filters if in Night Mode
                if (isNight) {
                    setColorFilterIfChanged(ivUnitLabel, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                    setTextColorIfChanged(tvFrequency, nightBlue);
                } else {
                    setColorFilterIfChanged(ivUnitLabel, null, null);
                    setTextColorIfChanged(tvFrequency, white);
                }

                updateFrequencyDisplay(fFreq);

                // V18.6: Actualizar metadatos de la sesión de medios inmediatamente
                if (mMediaSessionManager != null) {
                    float freqDisplay = fFreq / 1000.0f;
                    String freqStr = String.format(java.util.Locale.US, "%.1f MHz", freqDisplay);
                    mMediaSessionManager.updateMetadata(rdsName, freqStr, null);
                }

                // V4.0: Logo & Background (Always refresh logo in polling for consistency)
                if (mLogoManager != null) {
                    String cachedLogo = mLogoCachePerBand.get(fBand + "_" + fFreq);
                    mLogoManager.updateStationLogo(fFreq, fBand, cachedLogo);
                }

                updateBandImage(fBand);
                if (btnLocDx != null) {
                    btnLocDx.setSelected(fIsLocal);
                    setImageResourceIfChanged(btnLocDx, fIsLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
                }
            });

            sendWidgetUpdateIntent(fFreq, fBand, rdsName);
        }).start();
    }

    /**
     * V5.2: Broadcast to K706 Launcher Widget
     */
    private void sendWidgetUpdateIntent(int freq, int band, String rdsName) {
        if (mMode == FmMode.FM_QS6) {
            return;
        }

        // V2.5: Deep Guard to avoid "Permission Denial" Binder flood
        if (freq == mLastBroadcastFreq && band == mLastBroadcastBand && 
            ((rdsName == null && mLastBroadcastPs == null) || (rdsName != null && rdsName.equals(mLastBroadcastPs)))) {
            return;
        }

        mLastBroadcastFreq = freq;
        mLastBroadcastBand = band;
        mLastBroadcastPs = rdsName;

        try {
            android.content.Intent intent = new android.content.Intent("com.qf.radio.update_action");

            // Format frequency string (e.g., "92.20") exactly as K706 native
            // FmUtils.formatStation does.
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
            intent.putExtra("com.qf.radio.update_action_searching_key", false); // We don't track search status globally
                                                                                // here yet

            String widgetName = (rdsName != null && !rdsName.isEmpty() && !rdsName.equals("STATION NAME")
                    && !rdsName.equals("STATION")) ? rdsName : "";
            intent.putExtra("com.qf.radio.update_action_name_key", widgetName);

            // Try to make the intent explicit to bypass background implicit broadcast
            // restrictions
            intent.setPackage("com.android.auto.autohome");

            // Send standard broadcast (system apps or apps with correct permission will
            // receive it)
            sendBroadcast(intent);
        } catch (Exception ex) {
            // V16.2: Silenciar si es error de permisos (esperable en widgets de terceros)
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
            resId = R.drawable.radio_fm1;
        } else if (band == BAND_AM2) {
            if (resId == 0)
                resId = R.drawable.radio_fm2;
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
    void updateFrequencyDisplay(int freq) {
        if (freq <= 0)
            return; // V11.7: Evitar mostrar 00.0/0
        if (tvFrequency != null) {
            // V2.1: MOSTRAR FRECUENCIA NUMÉRICA INSTANTÁNEAMENTE
            // V2.2: Solo si ha cambiado para evitar "jitter"
            String freqStr;
            if (mCurrentBand >= 3) { // AM1, AM2, SW
                freqStr = String.valueOf(freq);
            } else {
                freqStr = String.format(java.util.Locale.US, "%.1f", freq / 1000.0);
            }
            setTextIfChanged(tvFrequency, freqStr);

            // V18.2: Mover resolución de nombres lenta a hilo secundario
            new Thread(() -> {
                String finalDisplayName = null;
                if (!mIsScanning && mRdsManager != null) {
                    finalDisplayName = mRdsManager.getDisplayName(freq);
                }
                
                // Fallback al repositorio si el RDSManager no reconoce el nombre
                if ((finalDisplayName == null || finalDisplayName.isEmpty()) && mRepository != null && !mIsScanning) {
                    com.example.openradiofm.data.model.RadioStation station = mRepository.getStationInfo(freq, null);
                    if (station != null) {
                        finalDisplayName = station.getName();
                    }
                }
                
                final String resultName = finalDisplayName;
                if (resultName != null && !resultName.isEmpty()) {
                    runOnUiThread(() -> {
                        setTextIfChanged(tvFrequency, resultName);
                    });
                }
            }).start();

            // Get State
            boolean isNight = (mThemeManager != null && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
            boolean isFavorite = isStationMemorized(freq);
            int idx = getPresetIndex(freq);

            // Colors
            int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
            int white = android.graphics.Color.WHITE;

            // 1. Dial Color & Unit Label
            if (isNight) {
                // V5.6: Always Night Blue in Night Mode as requested
                setTextColorIfChanged(tvFrequency, nightBlue);
                setColorFilterIfChanged(ivUnitLabel, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                setTextColorIfChanged(tvRdsName, nightBlue);
                setTextColorIfChanged(tvRdsInfo, nightBlue);
                setTextColorIfChanged(tvPty, nightBlue);
                setColorFilterIfChanged(btnPowerOff, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // Normal Mode -> Always White
                setTextColorIfChanged(tvFrequency, white);
                setColorFilterIfChanged(ivUnitLabel, null, null);
                setTextColorIfChanged(tvRdsName, white);
                setTextColorIfChanged(tvRdsInfo, white);
                setTextColorIfChanged(tvPty, white);
                setColorFilterIfChanged(btnPowerOff, null, null);
            }

            // 2. Favorite Icon
            if (ivFavoriteIndicator != null) {
                if (isFavorite && idx > 0) {
                    setVisibilityIfChanged(ivFavoriteIndicator, View.VISIBLE);
                    int resId = getResources().getIdentifier("radio_icon_p" + String.format("%02d", idx), "drawable",
                            getPackageName());
                    setImageResourceIfChanged(ivFavoriteIndicator, resId != 0 ? resId : R.drawable.radio_icon_p01);

                    // Tint logic
                    setColorFilterIfChanged(ivFavoriteIndicator, isNight ? nightBlue : white, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    setVisibilityIfChanged(ivFavoriteIndicator, View.GONE);
                }
            }

            // V16.2: Centralizar lógica de RDS en el manager (Name, RT, PTY)
            if (mRdsManager != null) {
                mRdsManager.updateRDSDisplay(freq, isNight, nightBlue, white);
            }

            // 4. Signal Level Coloring (V4.2 Refinement)
            if (ivSignalLevel != null) {
                int signalColor;
                boolean hasStereo = false;
                if (mEngine != null)
                    hasStereo = mEngine.isStereo();

                if (mHasRdsLock && hasStereo) {
                    signalColor = android.graphics.Color.parseColor("#00E676"); // Green
                } else if (hasStereo) {
                    signalColor = android.graphics.Color.parseColor("#FFD600"); // Yellow (Solo si hay estéreo mínimo,
                                                                                // lo consideramos emisión válida normal
                                                                                // en FM)
                } else if (mCurrentBand == BAND_AM1 || mCurrentBand == BAND_AM2) {
                    // AM bands don't usually have stereo, maybe we show yellow if we are tuned?
                    // We assume that the tune locked, so we provide default AM color
                    signalColor = android.graphics.Color.parseColor("#FFD600"); // Yellow for AM
                } else {
                    signalColor = android.graphics.Color.parseColor("#FF5252"); // Red (No emisión reconocible)
                }
                if (!isNight) {
                    setColorFilterIfChanged(ivSignalLevel, signalColor, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    setColorFilterIfChanged(ivSignalLevel, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }

            // Re-apply stereo visibility based on immediate hardware state
            if (ivStereoIcon != null) {
                boolean hasStereo = mEngine != null && mEngine.isStereo();
                setVisibilityIfChanged(ivStereoIcon, hasStereo ? View.VISIBLE : View.INVISIBLE);
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
     * V16.2: Delegado a ThemeManager.applySkin().
     * Conservamos el wrapper público para compatibilidad con DialogManager y NightModeManager.
     */
    public void applySkin(com.example.openradiofm.ui.theme.ThemeManager.Skin skin) {
        if (mThemeManager != null) mThemeManager.applySkin(skin);
        
        boolean isNight = (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);

        // V18.6: Propagate skin change to SimpleLayoutManager
        if (mIsSimpleLayout && mSimpleLayoutManager != null) {
            mSimpleLayoutManager.applyColors(isNight);
            if (tvDigitalClock != null) {
                if (isNight) {
                    tvDigitalClock.setTextColor(getResources().getColor(R.color.night_blue_primary, null));
                } else {
                    tvDigitalClock.setTextColor(android.graphics.Color.WHITE);
                }
            }
        }
    }

    // V16: applyNightModeColors() y resetNightModeColors() movidos a NightModeManager

    // V4: Frequency Step Helpers (Manual Tuning)
    public void stepFreqUp() {
        mCurrentPty = null;
        if (mEngine == null)
            return;
        int current = mEngine.getCurrentFreq();
        int band = mEngine.getCurrentBand();
        boolean isAm = (band == BAND_AM1 || band == BAND_AM2);

        int newFreq;
        if (isAm) {
            newFreq = current + 9;
            if (newFreq > 1620)
                newFreq = 522;
        } else {
            // V12.3: Paso de 100 kHz (0.1 MHz) estándar en Europa, evita el parpadeo de
            // 0.05
            newFreq = current + 100;
            if (newFreq > 108000)
                newFreq = 87500;
        }
        mEngine.tune(newFreq);
    }

    public void stepFreqDown() {
        mCurrentPty = null;
        if (mEngine == null)
            return;
        int current = mEngine.getCurrentFreq();
        int band = mEngine.getCurrentBand();
        boolean isAm = (band == BAND_AM1 || band == BAND_AM2);

        int newFreq;
        if (isAm) {
            newFreq = current - 9;
            if (newFreq < 522)
                newFreq = 1620;
        } else {
            // V12.3: Paso de 100 kHz (0.1 MHz)
            newFreq = current - 100;
            if (newFreq < 87500)
                newFreq = 108000;
        }
        mEngine.tune(newFreq);
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
                    tv.setTextColor(android.graphics.Color.WHITE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                showToast("Permisos concedidos. Probando acceso...");
                if (mDialogManager != null)
                    mDialogManager.showSaveLoadFavoritesDialog();
                showToast("Se requieren permisos de almacenamiento para guardar favoritos.");
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

    public void gotoFreq(int freq) {
        mCurrentPty = null; // V5.2: Reset PTY on tune
        if (mEngine != null)
            mEngine.tune(freq);
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

        mLastFreq = freq;
        mLastLogoUrl = ""; // Force logo reload
        mCurrentPi = null;
        mCurrentPty = null;
        mHasRdsLock = false;

        if (mRdsManager != null) {
            mRdsManager.reset(true);
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
        // V18.x: Interceptar teclas de medios de hardware (volante/unidad)
        switch (keyCode) {
            case android.view.KeyEvent.KEYCODE_MEDIA_NEXT:
            case android.view.KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                Log.d(TAG, "Hardware Key detected: NEXT");
                if (mPresetManager != null) mPresetManager.playNextPreset();
                return true;
            case android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case android.view.KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                Log.d(TAG, "Hardware Key detected: PREV");
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
        recreate();
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
        }
    }
}


