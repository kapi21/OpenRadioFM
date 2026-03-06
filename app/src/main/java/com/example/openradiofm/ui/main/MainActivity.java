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
    public RadioServiceController mServiceController;
    public RDSManager mRdsManager;

    // V16: Managers de Modo Nocturno e Historial
    public NightModeManager mNightModeManager;
    public HistoryManager mHistoryManager;
    public MediaSessionManager mMediaSessionManager;
    public ThemeManager mThemeManager; // V16.2: Skin manager

    // V5.5: Managers de Audio y Dispositivo
    public PlaybackManager mPlaybackManager;
    public DeviceManager mDeviceManager;


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

    public boolean mIsV3 = false; // V5.4: Track Layout 3 active

    // --- Clases de Soporte para Escaneo Selectivo (V12.1: Reubicadas para
    // estabilidad) ---
    public static class ScannedStation {
        public int frequency;
        public String name = "Buscando RDS...";

        public ScannedStation(int f) {
            this.frequency = f;
        }
    }

    public class StationViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView freq, name;
        android.widget.Button[] presets = new android.widget.Button[12];

        StationViewHolder(android.view.View root) {
            super(root);
            freq = root.findViewById(R.id.tvFreq);
            name = root.findViewById(R.id.tvName);
            for (int i = 0; i < 12; i++) {
                int resId = getResources().getIdentifier("btnP" + (i + 1), "id", getPackageName());
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
            for (int i = 0; i < 12; i++) {
                final int slot = i;
                h.presets[i].setOnClickListener(v -> {
                    if (mPresetManager != null) {
                        mPresetManager.savePreset(mCurrentBand, slot, s.frequency,
                                s.name.equals("Buscando RDS...") ? "" : s.name);
                        showToast("Guardado en Slot " + (slot + 1));
                        refreshPresetButtons();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mCapturedList.size();
        }
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
    private void gotoPreviousFavorite() {
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

    // V9.9: Hack para el problema del K706 donde MediaFocusControl "roba"
    // el canal pero no nos envía OnAudioFocusChange (solo abandona
    // customAudioFocus).
    private BroadcastReceiver mBtStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.qf.action.BT_STATE".equals(intent.getAction())) {
                // state=0 (Desconectado), state=1 (Conectando), state=2 (Conectado)
                int state = intent.getIntExtra("state", -1);
                Log.d(TAG, "BT_STATE Broadcast Received: " + state);

                if (mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
                    com.example.openradiofm.data.source.K706RadioManager k706Manager = ((com.example.openradiofm.data.source.K706Engine) mEngine)
                            .getManager();

                    if (state == 0) {
                        Log.d(TAG, "Bluetooth Desconectado: Forzando recuperación de audio FM (SetChannel 2)");
                        // Tras unos milisegundos para dejar que el sistema asimile la desconexión
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                k706Manager.enforceAudioChannelRecovery(); // Need to add this helper in
                                                                           // K706RadioManager
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
        if (ivDataActivity == null) {
            Log.w(TAG, "updateDataActivityUI: ivDataActivity is NULL (not found in layout)");
            return;
        }

        boolean onlineEnabled = mPrefs.getBoolean("pref_logos_online", false);
        boolean isConnected = false;
        try {
            isConnected = isInternetAvailable();
        } catch (Exception e) {
            // Error silenciado, se asume sin conexión para la lógica pero permitiendo visibilidad
        }
        
        Log.d(TAG, "updateDataActivityUI: onlineEnabled=" + onlineEnabled + ", isConnected=" + isConnected + ", mActiveDataOps=" + mActiveDataOps);

        if (!onlineEnabled) {
            ivDataActivity.setVisibility(View.INVISIBLE);
            stopDataBlink();
            return;
        }

        // V16.2: Siempre visible si está activado para indicar soporte nube.
        ivDataActivity.setVisibility(View.VISIBLE);
        

        if (mActiveDataOps > 0) {
            startDataBlink();
        } else {
            stopDataBlink();
        }

        // V17.0: Indicador visual de Streaming Online activo
        if (mOnlineStreamManager != null && ivDataActivityIcon != null) {
            if (mOnlineStreamManager.isPlaying()) {
                // Streaming active -> RED
                ivDataActivityIcon.setVisibility(View.VISIBLE);
                ivDataActivityIcon.setColorFilter(android.graphics.Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (mOnlineStreamManager.isLoading()) {
                ivDataActivityIcon.setColorFilter(android.graphics.Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // V17.4: Al limpiar filtros, respetar el color azul noche si el modo noche está activo
                if (mNightModeManager != null && mNightModeManager.isNightTime() && 
                    mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
                    int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
                    ivDataActivityIcon.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    ivDataActivityIcon.clearColorFilter();
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
            if (!scanning && mStationAdapter != null) {
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

        // Registrar receiver para las desconexiones Bluetooth de la placa QF (K706)
        IntentFilter filter = new IntentFilter("com.qf.action.BT_STATE");
        registerReceiver(mBtStateReceiver, filter);

        // V3.0: Layout Selection
        mPrefs = getSharedPreferences("RadioPresets", MODE_PRIVATE); // Init prefs early
        mIsV3 = mPrefs.getBoolean("pref_layout_v3", false);

        // V4.7: Manejo de Barra de Estado (Fullscreen condicional)
        applyStatusBarVisibility();

        setContentView(mIsV3 ? R.layout.activity_main_v3 : R.layout.activity_main);

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
                        btnMute.setImageResource(isMuted ? R.drawable.radio_mute_p : R.drawable.radio_mute_n);
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

        // V13: Cargar última frecuencia guardada
        if (mLastFreq == -1) {
            mLastFreq = mPrefs.getInt("pref_last_freq", 87500);
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

        // Configurar controles (EQ, Mute, Test, AutoScan, LOC/DX)
        setupControlButtons();

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

        // Seeking Logic
        setupSeekButtons();

        applyFonts();

        // V8.5: Easter Egg (Credits) - Restored
        setupCreditsEasterEgg();

        if (mServiceController != null)
            mServiceController.start();
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
            // V4.6: Se elimina la pulsación larga para el menú premium aquí, ahora está en
            // btnExtra1
        }

        // V5.5: Mute Logic delegada a PlaybackManager
        ImageButton btnMute = findViewById(R.id.btnMute);
        if (btnMute != null) {
            btnMute.setOnClickListener(v -> {
                if (mPlaybackManager != null) {
                    mPlaybackManager.setMute(!mMuteState);
                }
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
            // Deshabilitado por petición, causaba problemas
            /*
             * btnAutoScan.setOnLongClickListener(v -> {
             * if (mMode == FmMode.FM_K706) {
             * mDialogManager.showSelectiveScanDialog();
             * return true;
             * } else {
             * showToast("Escaneo selectivo solo disponible en motor K706");
             * return false;
             * }
             * });
             */
        }


        // V5.5: Power Off delegado a DeviceManager
        if (btnPowerOff != null) {
            btnPowerOff.setOnClickListener(v -> {
                animateButton(btnPowerOff);
                if (mDeviceManager != null) {
                    mDeviceManager.powerOff();
                }
            });
        }

        // BAND Switch — V5.0: Via RadioEngine
        btnBand = findViewById(R.id.btnBand);
        if (btnBand != null) {
            btnBand.setOnClickListener(v -> {
                if (mEngine != null) {
                    mEngine.bandCycle();
                } else {
                    showToast("Motor de radio no iniciado");
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

        // V4.0: Extra Button 1 - Android Settings (V4.6: Now opens Premium Menu on
        // short click)
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
    }

    @Override
    protected void onDestroy() {
        // V5.5: Limpieza delegada a DeviceManager
        stopStatusPolling();

        if (mDeviceManager != null) {
            mDeviceManager.releaseAllResources();
        }

        // Recursos no gestionados por DeviceManager (legacy específico)
        try {
            unregisterReceiver(mBtStateReceiver);
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
        if (boxFrequency != null) {
            // Click normal: Historial (y contador de créditos)
            boxFrequency.setOnClickListener(v -> handleCreditsClick());

            boxFrequency.setOnLongClickListener(v -> {
                if (mDialogManager != null) {
                    mDialogManager.showEditNameDialog();
                    return true;
                }
                return false;
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
                Log.d(TAG, "Seek Down (Long Click) triggered");
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
                Log.d(TAG, "Seek Up (Long Click) triggered");
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

        // V4: Bind Frequency Box for gestures (Fluid Drag) - ELIMINADO para simplificar
        if (tvFrequency != null) {
            tvFrequency.setClickable(true); 
        }

        // Loop Band Logic
        if (btnBand != null) {
            btnBand.setOnClickListener(v -> {
                if (mEngine != null)
                    mEngine.bandCycle();
            });
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
        runOnUiThread(this::updateDataActivityUI);

        int freq = mEngine.getCurrentFreq();
        if (freq <= 0)
            return;

        int band = mEngine.getCurrentBand();
        boolean isStereo = mEngine.isStereo();
        boolean isLocal = mEngine.isDxLocal();

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
        com.example.openradiofm.data.model.RadioStation station = null;
        if (mRepository != null && !mIsScanning) {
            station = mRepository.getStationInfo(freq, null);
        }
        String rdsName = (station != null) ? station.getName() : "";
        final int fFreq = freq;
        final int fBand = band;
        final boolean fIsAm = (band == BAND_AM1 || band == BAND_AM2);
        final boolean fIsLocal = isLocal;

        runOnUiThread(() -> {
            // Sync color filters if in Night Mode
            if (mThemeManager != null && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
                int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
                if (ivUnitLabel != null)
                    ivUnitLabel.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                if (tvFrequency != null)
                    tvFrequency.setTextColor(nightBlue);
            } else {
                if (tvFrequency != null)
                    tvFrequency.setTextColor(android.graphics.Color.WHITE);
            }

            updateFrequencyDisplay(fFreq);

            // V4.0: Logo & Background (Always refresh logo in polling for consistency)
            if (mLogoManager != null) {
                String cachedLogo = mLogoCachePerBand.get(fBand + "_" + fFreq);
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
        if (mMode == FmMode.FM_QS6) {
            return;
        }

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
            if (ex.getMessage() == null || !ex.getMessage().contains("Permission Denial")) {
                Log.e(TAG, "Error sending widget broadcast: " + ex.getMessage());
            }
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
            // V5.5: Resolución de nombre delegada al RDSManager (RDS live > customName > frecuencia)
            // V13.9: Durante el escaneo, NO buscamos nombres en DB para ganar fluidez
            String displayName = null;
            if (!mIsScanning && mRdsManager != null) {
                displayName = mRdsManager.getDisplayName(freq);
            }
            
            // V17.4: Fallback adicional al repositorio si el RDSManager no reconoce el nombre (ej: al volver a sintonizar)
            if ((displayName == null || displayName.isEmpty()) && mRepository != null && !mIsScanning) {
                com.example.openradiofm.data.model.RadioStation station = mRepository.getStationInfo(freq, null);
                if (station != null) {
                    displayName = station.getName();
                }
            }
            
            if (displayName != null && !displayName.isEmpty()) {
                tvFrequency.setText(displayName);
                // V16.4: Forzar un re-layout para asegurar que el auto-sizing se active correctamente
                // si el nombre es largo, evitando que se vea entrecortado en Layout 2.
                tvFrequency.requestLayout();
            } else if (mCurrentBand == BAND_AM1 || mCurrentBand == BAND_AM2) {
                tvFrequency.setText(String.valueOf(freq));
            } else {
                // V12.3: Usar double para evitar errores de precisión de punto flotante (+/- 0.05)
                tvFrequency.setText(String.format(java.util.Locale.US, "%.2f", (double) freq / 1000.0));
            }

            // Get State
            boolean isNight = (mThemeManager != null && mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
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
                if (btnPowerOff != null)
                    btnPowerOff.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
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
                if (btnPowerOff != null)
                    btnPowerOff.clearColorFilter();
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
                ivSignalLevel.setColorFilter(signalColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }

            // Re-apply stereo visibility based on immediate hardware state
            if (ivStereoIcon != null) {
                boolean hasStereo = mEngine != null && mEngine.isStereo();
                ivStereoIcon.setVisibility(hasStereo ? View.VISIBLE : View.INVISIBLE);
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
    }

    // V16: applyNightModeColors() y resetNightModeColors() movidos a NightModeManager

    // V4: Frequency Step Helpers (Manual Tuning)
    private void stepFreqUp() {
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

    private void stepFreqDown() {
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
            window.setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#E6121212")));
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

    private void onSeekUpEvent() {
        Log.d(TAG, "onSeekUpEvent call");
        if (mEngine != null)
            mEngine.seekUp();
    }

    private void onSeekDownEvent() {
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
    private void toggleAutoScan(ImageButton btn) {
        if (mEngine == null)
            return;
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
        if (mEngine != null)
            mEngine.scan();
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
        if (mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
            com.example.openradiofm.data.source.K706RadioManager mgr = ((com.example.openradiofm.data.source.K706Engine) mEngine)
                    .getManager();
            if (mgr != null) {
                try {
                    java.lang.reflect.Method sendCmd = com.example.openradiofm.data.source.K706RadioManager.class
                            .getDeclaredMethod(
                                    "sendCmd", byte.class, byte.class, byte.class);
                    sendCmd.setAccessible(true);
                    sendCmd.invoke(mgr, subCmd, param1, param2);
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "sendMcuTunerCmd error", e);
                }
            }
        }
    }

    /**
     * V13.9: Aplica la visibilidad de la barra de estado según las preferencias y el layout.
     */
    public void applyStatusBarVisibility() {
        if (mPrefs == null) return;
        boolean showStatusBarV2 = mPrefs.getBoolean("pref_show_status_bar_v2", false);
        runOnUiThread(() -> {
            if (mIsV3 || (!mIsV3 && showStatusBarV2)) {
                getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

    // V5.5: mMediaControlReceiver migrado a PlaybackManager (ver PlaybackManager.java)
}


