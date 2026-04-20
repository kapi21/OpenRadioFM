package com.example.openradiofm.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
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
import android.graphics.drawable.Drawable;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.graphics.Bitmap;
import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.Settings;


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
import com.example.openradiofm.ui.widget.SignalBarsView;
import com.example.openradiofm.AppConstants;
import com.example.openradiofm.service.RadioMediaService;

import java.util.concurrent.ExecutorService;

/**
 * Pantalla principal de la radio FM.
 *
 * Responsabilidades:
 * - Conectarse al servicio de radio del coche (IRadioServiceAPI).
 * - Mostrar frecuencia, nombre RDS y texto RDS.
 * - Gestionar presets, logos locales y botones de control.
 *
 * Notas de dise├▒o:
 * - El sondeo del estado de la radio se hace en un hilo de fondo mediante
 * Timer,
 * y solo las actualizaciones de UI pasan por runOnUiThread() para no bloquear
 * el hilo principal.
 * - Los recursos de hardware (servicio, proceso root, listener RDS oculto) se
 * liberan expl├¡citamente en onDestroy() para evitar fugas de memoria.
 */
public class MainActivity extends AppCompatActivity implements RadioUiHost {

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

    static final String TAG = "OpenRadioFm";
    private static final int PRESETS_COUNT = AppConstants.PRESETS_COUNT; // Fuente ├║nica global

    public static boolean isFactoryRadioHijackerAccessibilityEnabled(Context context) {
        try {
            if (context == null) return false;
            int a11yEnabled = 0;
            try {
                a11yEnabled = Settings.Secure.getInt(
                        context.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_ENABLED
                );
            } catch (Exception ignored) {}
            if (a11yEnabled != 1) return false;

            String enabled = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (enabled == null || enabled.isEmpty()) return false;

            ComponentName cn = new ComponentName(context, com.example.openradiofm.services.FactoryRadioHijackerService.class);
            String flattened = cn.flattenToString(); // package/class

            // Lista separada por ':' (Settings.Secure)
            // Comparaci├│n case-sensitive (Android almacena as├¡).
            String[] parts = enabled.split(":");
            for (String p : parts) {
                if (flattened.equals(p)) return true;
                // Algunas ROM guardan el nombre "short" de la clase.
                if ((context.getPackageName() + "/" + com.example.openradiofm.services.FactoryRadioHijackerService.class.getName()).equals(p)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * {@link com.example.openradiofm.services.FactoryRadioHijackerService}: true entre
     * {@code onResume} y {@code onPause}. Muchas ROM de cabecera no llaman {@code onStop} al ir al
     * launcher; el hijacker reenvia teclas MEDIA solo cuando esta bandera es false.
     */
    static volatile boolean sMainActivityResumed = false;

    /**
     * True con motor K706 o QS6: {@link com.example.openradiofm.services.FactoryRadioHijackerService}
     * reenvia teclas MEDIA a {@link com.example.openradiofm.service.RadioMediaService} cuando
     * esta activity no esta resumed (launcher al frente). No aplica a MT8163 u otros.
     */
    static volatile boolean sWheelMediaBridgeActive = false;

    /** Para {@link com.example.openradiofm.services.FactoryRadioHijackerService} (otro paquete). */
    public static boolean isMainActivityResumed() {
        return sMainActivityResumed;
    }

    public static boolean isWheelMediaBridgeActive() {
        return sWheelMediaBridgeActive;
    }

    public static void setWheelMediaBridgeActive(boolean active) {
        sWheelMediaBridgeActive = active;
    }

    // Band Constants (package: usadas por MainActivityBootstrap)
    static final int BAND_FM1 = 0;
    static final int BAND_FM2 = 1;
    static final int BAND_FM3 = 2;
    static final int BAND_AM1 = 3;
    static final int BAND_AM2 = 4;

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
        /** Jancar IVI ({@code com.jancar.services} / IRadio AIDL), p. ej. MTK8227L. */
        FM_JANCAR_IVI,
        FM_8259_8667,
        AM
    }

    FmMode mMode = FmMode.FM_BASICO;

    IRadioServiceAPI mRadioService;
    com.example.openradiofm.data.repository.RadioRepository mRepository;
    android.content.SharedPreferences mPrefs;
    HiddenRadioPlayer mHiddenPlayer;

    // V5.0: Capa de abstracci├│n de hardware
    RadioEngine mEngine;
    boolean mIsScanning = false;
    ScanManager mScanManager;
    DialogManager mDialogManager;
    LogoManager mLogoManager;
    RadioServiceController mServiceController;
    RDSManager mRdsManager;
    StandardLayoutManager mStandardLayoutManager;
    SimpleLayoutManager mSimpleLayoutManager;
    ControlPanelManager mControlPanelManager;

    boolean mIsSimpleLayout = false;
    boolean mIsV3 = false;

    /** Layout horizontal V3 real (no Simple). Usar en logo/skins para no mezclar flags sueltos. */
    public boolean isV3LayoutActive() {
        return mIsV3 && !mIsSimpleLayout;
    }
    boolean mIsMinimal = false; // V19.2
    boolean mControlsHidden = false;
    android.os.Handler mAutoHideHandler;
    Runnable mAutoHideRunnable;

    // V21.0: UI Controllers Refactor
    BaseLayoutController mUiController;
    final android.os.Handler mMainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    /**
     * Tras {@link RadioMediaService#ACTION_MT8163_FM_HANDOFF}: ejecutar {@code conectarRadio()}.
     * El handoff evita que SourceService mate el proceso si la MediaSession segu├¡a en PLAYING.
     */
    private final Runnable mHcnBindAfterHandoffRunnable = () -> {
        try {
            if (isFinishing() || isDestroyed()) return;
            if (mMode != FmMode.FM_MT8163 || mServiceController == null) return;
            if (mRadioService != null) return;
            android.util.Log.i(TAG, "MT8163: conectarRadio() tras handoff previo");
            mServiceController.start();
        } catch (Exception e) {
            android.util.Log.w(TAG, "mHcnBindAfterHandoffRunnable", e);
        }
    };

    void removeHcnBindAfterHandoffCallbacks() {
        mMainHandler.removeCallbacks(mHcnBindAfterHandoffRunnable);
    }

    private static final long MT8163_MS_AFTER_SESSION_HANDOFF_BEFORE_HCN_BIND = 550L;

    /**
     * OEM: bajar MediaSession a STOPPED y esperar un tick antes de bind a {@code com.hcn.autoradio},
     * o el mux puede hacer {@code forceStopPackage} sobre OpenRadioFM.
     */
    @Override
    public void requestHcnBindWithMediaSessionHandoff(String reasonForLog) {
        if (mMode != FmMode.FM_MT8163 || mServiceController == null) return;
        if (mRadioService != null) return;
        try {
            android.util.Log.i(TAG, "MT8163: handoff MediaSession antes de bind HCN (" + reasonForLog + ")");
            Intent h = new Intent(this, RadioMediaService.class);
            h.setAction(RadioMediaService.ACTION_MT8163_FM_HANDOFF);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(h);
            } else {
                startService(h);
            }
        } catch (Exception e) {
            android.util.Log.w(TAG, "requestHcnBindWithMediaSessionHandoff", e);
        }
        removeHcnBindAfterHandoffCallbacks();
        mMainHandler.postDelayed(mHcnBindAfterHandoffRunnable, MT8163_MS_AFTER_SESSION_HANDOFF_BEFORE_HCN_BIND);
    }

    // V16: Managers de Modo Nocturno e Historial
    SkinCoordinator mSkinCoordinator;
    FrequencyChangeCoordinator mFrequencyChangeCoordinator;
    StatusRefreshCoordinator mStatusRefreshCoordinator;
    EngineCallbackCoordinator mEngineCallbackCoordinator;
    LifecycleCoordinator mLifecycleCoordinator;
    HardwareKeyCoordinator mHardwareKeyCoordinator;
    UiViewMediator mUiMediator;
    FrequencyStateManager mFreqStateManager;
    NightModeManager mNightModeManager;
    DayModeManager mDayModeManager;
    com.example.openradiofm.ui.theme.ThemeManager.Skin mLastSkinAppliedForBackground = null;
    HistoryManager mHistoryManager;
    MediaSessionManager mMediaSessionManager;
    ThemeManager mThemeManager; // V16.2: Skin manager
    IconPackManager mIconPackManager;
    PresetNumberIconManager mPresetNumberIconManager;

    // N├║meros de presets (1..18) para el indicador de favorito
    public static final String PREF_PRESET_NUMBERS_STYLE = "pref_preset_numbers_style"; // 0=default(drawable), 1=tabler(assets svg)

    // V18.5: Reloj Digital
    android.os.Handler mClockHandler;
    Runnable mClockRunnable;

    // V5.5: Managers de Audio y Dispositivo
    PlaybackManager mPlaybackManager;
    DeviceManager mDeviceManager;
    HardwareManager mHardwareManager;
    WidgetBroadcastManager mWidgetBroadcastManager; // V23.0: Desacoplamiento de widgets OEM
    RadioSessionController mSessionController;


    // V11: RDS PI Database Identification
    private com.example.openradiofm.data.source.RdsDatabase mRdsDb;
    String mCurrentPi = null;

    // V13: Gestor de Presets (Reducci├│n de MainActivity)
    PresetManager mPresetManager;

    // V24.5: K706 Engineering & Hardware Automation
    K706EngineeringDialog mEngineeringDialog = null;
    int mLastFreq = -1;
    /** Guardas de arranque (bootstrap MCU) y sintonía explícita; ver {@link StartupFreqPersistGuards}. */
    final StartupFreqPersistGuards mStartupFqGuards = new StartupFreqPersistGuards();
    int mLastBand = BAND_FM1;
    long mShutdownPersistGuardUntilMs = 0L;
    /** True solo durante el flujo de PowerOff (evita bridge de volante en onStop). */
    volatile boolean mPowerOffRequested = false;
    static final String PREF_QS6_BOOTSTRAP_SANITIZED = "pref_qs6_bootstrap_sanitized";
    /** Misma idea que QS6: evitar prefs contaminadas con 87.5/87.6 tras reinicio de unidad (MCU arranca antes que la app). */
    static final String PREF_K706_BOOTSTRAP_SANITIZED = "pref_k706_bootstrap_sanitized";
    String mLastPs = ""; // V18.6: Almacena el nombre RDS/Custom actual
    final RdsLockUiTickState mRdsLockUiTick = new RdsLockUiTickState();
    String mCurrentPty = null;
    String mLastLogoUrl = "";
    java.util.Map<String, String> mLogoCachePerBand = new java.util.HashMap<>();
    /** Evita arrastre de RDS/logo de la emisora anterior tras un cambio de frecuencia (QS6/NWD). */
    static final long RDS_TRANSITION_GUARD_MS = 1200L;
    final RdsLogoTransitionState mRdsLogoTransition = new RdsLogoTransitionState();
    /** Margen tras cambiar de frecuencia antes de contribuir metadatos a la nube. */
    static final long CLOUD_CONTRIB_FREQ_SETTLE_MS = 1750L;
    long mCloudContribAllowedAfterMs = 0L;
    /**
     * Se incrementa en {@link #onDestroy()} cuando la activity termina ({@code isFinishing()}), para que
     * tareas en {@link com.example.openradiofm.util.AppIoExecutor} ligadas a esta instancia aborten cooperativamente.
     */
    private final java.util.concurrent.atomic.AtomicInteger mUiWorkGeneration = new java.util.concurrent.atomic.AtomicInteger(0);

    public int getUiWorkGeneration() {
        return mUiWorkGeneration.get();
    }

    com.example.openradiofm.data.source.SupabaseSyncManager mSupabaseSyncManager;
    com.example.openradiofm.ui.main.OnlineStreamManager mOnlineStreamManager;

    @Override
    public boolean isQs6TransitionGuardActive() {
        try {
            boolean isQs6 = mEngine != null
                    && mEngine.getEngineName() != null
                    && mEngine.getEngineName().toUpperCase().contains("QS6");
            return isQs6 && android.os.SystemClock.elapsedRealtime() < mRdsLogoTransition.rdsTransitionGuardUntilMs;
        } catch (Exception ignored) {
            return false;
        }
    }

    // V5.0: UI Elements (Fixing Compilation Errors)
    TextView tvPty;
    SignalBarsView mSignalBarsView;
    SignalMeterCoordinator mSignalMeterCoordinator;
    ImageView ivAfIcon, ivTaIcon, ivTpIcon; // RDS Status Icons
    android.widget.FrameLayout ivDataActivity; // V16.2: Cloud Data indicator (Wrapper)
    int mActiveDataOps = 0; // V16.2: Concurrent Supabase Operations (StreamingUiCoordinator)
    DataActivityIndicatorManager mDataActivityIndicatorManager;
    long mLastInternetCheckTime = 0;
    boolean mLastInternetCache = false;

    // V2.6: Master Guard for refreshRadioStatus
    private int mLastRefreshFreq = -1;
    private int mLastRefreshBand = -1;
    private long mLastFullRefreshTime = 0;
    
    // V21.1: Throttling de tareas UI no cr├¡ticas (fluidez)
    private static final long NIGHT_MODE_CHECK_INTERVAL_MS = 5_000;
    private static final long DATA_ACTIVITY_UI_INTERVAL_MS = 1_000;
    private long mLastNightModeCheckTime = 0;
    private long mLastDataActivityUiTime = 0;
    
    // V21.1: Evitar crear hilos por cada refresh (coalescing de station info)
    java.util.concurrent.ExecutorService mStationInfoExecutor;
    final java.util.concurrent.atomic.AtomicInteger mStationInfoSeq = new java.util.concurrent.atomic.AtomicInteger(0);
    volatile int mLastStationInfoRequestedSeq = 0;

    boolean mMuteState = false;

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

    /** Men├║ ingenier├¡a QS6 / NWD (pulsaci├│n larga en GPS). */
    QS6EngineeringDialog mQs6EngineeringDialog = null;

    int mCurrentBand = 0;
    boolean mIsRecreating = false; // V20.3: Flag to distinguish between Cold Start and Layout Switch

    

    /**
     * Nombre estable cacheado por frecuencia (prioridad: CUSTOM_ > RDS_) para pintura r├ípida en QS6.
     */
    private String getStableCachedNameForFrequency(int freqKhz) {
        try {
            android.content.SharedPreferences namesPrefs = getSharedPreferences("RadioStationNames", Context.MODE_PRIVATE);
            String custom = namesPrefs.getString("CUSTOM_" + freqKhz, null);
            if (custom != null) {
                custom = custom.trim();
                if (!custom.isEmpty()) return custom;
            }

            String rds = namesPrefs.getString("RDS_" + freqKhz, null);
            if (rds != null) {
                rds = rds.trim();
                if (!rds.isEmpty()
                        && !com.example.openradiofm.data.source.SupabaseLogoSource.isGarbageZeroPs(rds)) {
                    return rds;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public boolean hasStableCachedNameForFrequency(int freqKhz) {
        String name = getStableCachedNameForFrequency(freqKhz);
        return name != null && !name.trim().isEmpty();
    }



    // V18.6: StationAdapter and ScannedStation moved to separate files

    // M├®todos delegados al PresetManager para compatibilidad con c├│digo existente
    public void gotoFreq(int freq) {
        if (mEngine != null) {
            boolean isQs6 = false;
            try {
                isQs6 = mEngine.getEngineName() != null
                        && mEngine.getEngineName().toUpperCase().contains("QS6");
            } catch (Exception ignored) {}
            final boolean isQs6Final = isQs6;

            // V20.0: Limpieza inmediata de UI para evitar logos "pegados" si el hardware falla o es lento
            runOnUiThread(() -> {
                clearStationLogoUi();
                if (isQs6Final) {
                    String cachedName = getStableCachedNameForFrequency(freq);
                    // En QS6, fijar la frecuencia objetivo al instante evita mostrar la anterior
                    // mientras el stack OEM entrega callbacks transitorios tras TUNE.
                    updateFrequencyDisplay(freq, cachedName);
                    if (tvRdsName != null) tvRdsName.setText(cachedName != null ? cachedName : "");
                    if (tvRdsInfo != null) tvRdsInfo.setText("");
                    if (tvPty != null) tvPty.setText(getString(R.string.pty_none));
                }
            });

            if (isQs6) {
                mRdsLogoTransition.prevStationNameBeforeTune = mLastPs != null ? mLastPs : "";
                mRdsLogoTransition.rdsTransitionGuardUntilMs = android.os.SystemClock.elapsedRealtime() + RDS_TRANSITION_GUARD_MS;
                mRdsLogoTransition.logoUiGeneration.incrementAndGet();
            }
            try {
                com.example.openradiofm.utils.RadioActivityFileLogger.logBasic(this, "UI", "gotoFreq(" + freq + ") band=" + mCurrentBand);
            } catch (Exception ignored) {}
            mEngine.tune(freq);
            mStartupFqGuards.userRequestedFreqKhz = freq;
            mStartupFqGuards.userRequestedFreqUntilMs = android.os.SystemClock.elapsedRealtime() + 12000L;
            // Antes del callback del motor: misma frecuencia evita doble trabajo en handleFrequencyChange.
            mLastFreq = freq;
            if (mFrequencyChangeCoordinator != null) {
                // Misma tubería que el motor (sin coalescencia): cloud/RDS/streaming/MediaSession/widget/prefs.
                // Historial no (comportamiento histórico de gotoFreq). QS6: no pisa el PS primado arriba.
                mFrequencyChangeCoordinator.finishUserTuneFromUi(freq, isQs6);
            } else {
                mLastBand = mCurrentBand;
                if (mPrefs != null) {
                    mPrefs.edit()
                            .putInt("pref_last_freq", freq)
                            .putInt("pref_last_band", mCurrentBand)
                            .apply();
                }
            }
            if (isQs6) {
                // Peque├▒o retraso: evita leer getCurrentFreq() a├║n viejo en QS6 justo tras TUNE.
                mMainHandler.postDelayed(this::refreshRadioStatus, 220);
            } else {
                refreshRadioStatus();
            }
        }
    }

    public void gotoPreset(int index) {
        if (mPresetManager != null) {
            int freq = mPresetManager.getFreq(index);
            if (freq > 0) {
                final int targetIndex = index;
                final int targetFreq = freq;
                mPresetManager.preparePresetSelection(index);
                gotoFreq(freq);
                // V21.4: Tras pulsar un preset, preparePresetSelection limpia el icono del slot;
                // refreshRadioStatus no llama a refreshButtons al cambiar solo la frecuencia,
                // as├¡ que los logos de preset quedaban vac├¡os hasta otro evento (p. ej. RDS).
                runOnUiThread(() -> {
                    if (mPresetManager != null) {
                        mPresetManager.refreshButtons(mCurrentBand);
                    }
                });
                mMainHandler.postDelayed(() -> {
                    if (isFinishing() || isDestroyed() || mPresetManager == null) return;
                    mPresetManager.refreshButtons(mCurrentBand);
                }, 400);

                // QS6: tras el lock transitorio anti-arrastre (Ôëê2.2s), refrescar expl├¡citamente
                // el slot pulsado para recuperar nombre/logo sin esperar a otro evento externo.
                mMainHandler.postDelayed(() -> {
                    if (isFinishing() || isDestroyed() || mPresetManager == null) return;
                    int currentPresetFreq = mPresetManager.getFreq(targetIndex);
                    if (currentPresetFreq != targetFreq) return; // slot reutilizado/cambiado
                    mPresetManager.updateCardVisuals(targetIndex, targetFreq, mCurrentBand);
                }, 2350);
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
            showToast(getString(R.string.toast_no_other_favorites));
        }
    }

    /**
     * V14.0: Salta al favorito anterior guardado en la banda actual.
     * V21.3: Ahora usa navegaci├│n secuencial por slots.
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
            showToast(getString(R.string.toast_no_other_favorites));
        }
    }

    // V3.0: Background personalizado

    TextView tvFrequency, tvRdsName, tvRdsInfo;
    private android.view.View boxFrequency;
    TextView ivBandIndicator;
    TextView ivUnitLabel;
    ImageView ivFavoriteIndicator;
    TextView ivStereoIcon;

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
    // M├ís robusto que Timer y evita fugas de memoria.
    private java.util.concurrent.ScheduledExecutorService mPollingExecutor;

    /**
     * Inicia el sondeo peri├│dico del estado de la radio.
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
            // Ejecutamos la l├│gica de refresco directamente en el hilo del executor.
            // Dentro de refreshRadioStatus() se usa runOnUiThread() solo para la UI.
            refreshRadioStatus();
        }, 500, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Detiene el sondeo de estado si est├í activo.
     */
    @Override
    public void stopStatusPolling() {
        if (mPollingExecutor != null) {
            mPollingExecutor.shutdownNow();
            mPollingExecutor = null;
        }
    }

    // V5.0: Callbacks unificados del RadioEngine (MainActivity implementa
    // RadioEngineCallback)
    

    

    

    

    /**
     * Icono cloud: delega en {@link #updateDataActivityUI()} (p. ej. tras di├ílogos de tema).
     */
    public void refreshDataActivityIndicator() {
        StreamingUiCoordinator.updateDataActivityUi(this);
    }

    /**
     * V16.2: Actualiza el estado visual del icono de actividad de datos.
     * Implementación en {@link StreamingUiCoordinator#updateDataActivityUi(MainActivity)}.
     */
    @Override
    public void updateDataActivityUI() {
        StreamingUiCoordinator.updateDataActivityUi(this);
    }

    public void ensureDataActivityIndicatorManager() {
        StreamingUiCoordinator.ensureDataActivityIndicatorManager(this);
    }

    /**
     * V17.0: Configura el toggle de Radio Online vs Radio FM.
     */
    public void setupOnlineStreaming() {
        StreamingUiCoordinator.install(this);
    }

    // Blink / alpha / tint del cloud movidos a DataActivityIndicatorManager

    /** @see StreamingUiCoordinator#isInternetReachable(MainActivity) */
    public boolean isInternetAvailable() {
        return StreamingUiCoordinator.isInternetReachable(this);
    }


    

    /**
     * Opci├│n A: "tick" visual (flash/fade) cuando se engancha RDS lock.
     * Dispara solo en flanco de subida y con anti-spam.
     */
    void maybeTickRdsLock(boolean hasLockNow) {
        long now = android.os.SystemClock.elapsedRealtime();
        boolean risingEdge = hasLockNow && !mRdsLockUiTick.hadLockForTick;
        mRdsLockUiTick.hadLockForTick = hasLockNow;
        if (!risingEdge) return;
        if (now - mRdsLockUiTick.lastTickUptimeMs < 650L) return;
        mRdsLockUiTick.lastTickUptimeMs = now;

        android.widget.TextView ps = findViewById(R.id.tvRdsName);
        android.widget.TextView pty = findViewById(R.id.tvPty);
        tickFlashText(ps);
        tickFlashText(pty);
    }

    public static void tickFlashText(android.widget.TextView tv) {
        if (tv == null) return;
        int original = tv.getCurrentTextColor();
        int highlight = android.graphics.Color.parseColor("#FFFFF59D"); // amarillo suave

        tv.animate().cancel();
        tv.setAlpha(1.0f);

        // Flash breve: color ÔåÆ original + mini fade.
        tv.setTextColor(highlight);
        tv.animate()
                .alpha(0.55f)
                .setDuration(90)
                .withEndAction(() -> {
                    try {
                        tv.setTextColor(original);
                        tv.animate().alpha(1.0f).setDuration(160).start();
                    } catch (Exception ignored) {}
                })
                .start();
    }


    




    

    

    

    

    

    

    

    // V8.5: Credits Easter Egg Variables (Restored)
    private int mCreditsClickCount = 0;
    private long mCreditsStartTime = 0;

    private void sendMcuKey(int key) {
        if (mHardwareManager != null) mHardwareManager.sendMcuKey(key);
    }


    final RadioServiceController.ServiceListener mServiceListener = new RadioServiceController.ServiceListener() {
        @Override
        public void onModeDetected(FmMode mode) {
            mMode = mode; // Se asigna sincronamente antes de volver a la cola de eventos
            sWheelMediaBridgeActive = (mMode == FmMode.FM_K706 || mMode == FmMode.FM_QS6);
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
                            // V16.3: El cambio de contador debe ser en el UI thread para evitar desincronizaci├│n
                            if (active) mActiveDataOps++;
                            else if (mActiveDataOps > 0) mActiveDataOps--;
                            
                            updateDataActivityUI();
                        });
                    });
                    mRepository.setCloudContributionGuard(() -> {
                        if (mEngine != null && mEngine.isScanning()) return false;
                        return android.os.SystemClock.elapsedRealtime() >= mCloudContribAllowedAfterMs;
                    });
                }

                // Database RDS PI
                if (mRdsDb == null) {
                    mRdsDb = new com.example.openradiofm.data.source.RdsDatabase(MainActivity.this);
                }

                // Inicializar Managers que dependen del repo/db
                if (mPresetManager == null) {
                    mPresetManager = new PresetManager(MainActivity.this, mRepository, mPrefs, PRESETS_COUNT);
                    View root = findViewById(android.R.id.content);
                    mPresetManager.bindViews(root, isV3LayoutActive());
                    InfinitePresetScrollHelper.attachIfNeeded(MainActivity.this);
                    // MT8163 (y en general cold start): evitar cargar todo el estado de presets/logos
                    // en el mismo tick en el que se infla el layout (reduce jank: "Skipped frames").
                    if (root != null) {
                        root.post(() -> {
                            if (isFinishing() || isDestroyed() || mPresetManager == null) return;
                            mPresetManager.refreshPresetsCache(mCurrentBand);
                            // Arranque rápido: texto primero, logos después.
                            mPresetManager.refreshButtons(mCurrentBand, false);
                            mPresetManager.syncLoopMirrorPresetVisualsWithMainSlots();
                            mMainHandler.postDelayed(() -> {
                                if (isFinishing() || isDestroyed() || mPresetManager == null) return;
                                mPresetManager.refreshButtons(mCurrentBand, true);
                            }, 1400);
                        });
                    } else {
                        mPresetManager.refreshPresetsCache(mCurrentBand);
                        mPresetManager.refreshButtons(mCurrentBand, false);
                        mPresetManager.syncLoopMirrorPresetVisualsWithMainSlots();
                        mMainHandler.postDelayed(() -> {
                            if (isFinishing() || isDestroyed() || mPresetManager == null) return;
                            mPresetManager.refreshButtons(mCurrentBand, true);
                        }, 1400);
                    }
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
                            mEngineCallbackCoordinator);
                    setupRdsText();
                }
            });
        }

        @Override
        public void onEngineReady(RadioEngine engine) {
            mEngine = engine;
            // Si RadioMediaService ya registr├│ callback en el motor compartido (QS6/K706), combinar (no perder metadata Auto).
            com.example.openradiofm.data.source.RadioEngineCallback existingCb = null;
            if (mEngine instanceof com.example.openradiofm.data.source.QS6Engine) {
                existingCb = ((com.example.openradiofm.data.source.QS6Engine) mEngine).getCallback();
            } else if (mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
                existingCb = ((com.example.openradiofm.data.source.K706Engine) mEngine).getCallback();
            }
            if (existingCb != null && existingCb != MainActivity.this) {
                mEngine.setCallback(new com.example.openradiofm.data.source.CompositeRadioEngineCallback(
                        mEngineCallbackCoordinator, existingCb));
            } else {
                mEngine.setCallback(mEngineCallbackCoordinator);
            }

            if (mEngine instanceof com.example.openradiofm.data.source.MT8163Engine) {
                RadioServiceController.registerSharedMt8163Engine(mEngine);
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

            // Inicializar controlador de sesi├│n compartido usando el mismo motor y playback manager
            try {
                if (mSessionController == null) {
                    mSessionController = RadioServiceController.getOrCreateSharedSessionController(
                            MainActivity.this,
                            mEngine,
                            mPrefs,
                            getSharedPreferences("RadioStationNames", Context.MODE_PRIVATE)
                    );
                    // Listener opcional: refrescar algunos elementos de UI cuando cambie el estado global
                    mSessionController.addListener(state -> {
                        // Por ahora solo sincronizamos banda/frecuencia b├ísicos si el engine est├í listo
                        if (state != null && mEngine != null) {
                            mLastFreq = state.freqKhz > 0 ? state.freqKhz : mLastFreq;
                            mCurrentBand = state.band;
                        }
                    });
                }
            } catch (Exception e) {
                Log.w(TAG, "No se pudo inicializar RadioSessionController en MainActivity", e);
            }

            // Si el motor no se ha inicializado todavía (ej: K706), lo hacemos aquí.
            // MT8163: init() ya se llamó en onServiceConnected antes de onEngineReady; getCurrentFreq()
            // sigue en 0 hasta el primer tune — repetir init duplicaba bind HCN + HiddenRadioPlayer (log 2×).
            if (mEngine.getCurrentFreq() <= 0
                    && !(mEngine instanceof com.example.openradiofm.data.source.MT8163Engine)) {
                mEngine.init(MainActivity.this);
            }

            mCurrentBand = mEngine.getCurrentBand();

            // V20.0: Encapsular l├│gica de post-inicializaci├│n para permitir retardo t├íctico
            final Runnable postInitAction = () -> {
                // V18.6.3: Sintonizar a la ├║ltima frecuencia guardada (pref_last_freq).
                // - Motores que reportan 0 hasta init: siempre tune si mLastFreq > 0.
                // - QS6: getCurrentFreq() arranca en 87500 por defecto (nunca <= 0), as├¡ que sin este
                //   caso nunca se restauraba la emisora anterior tras cerrar la app.
                // - K706: tras reinicio de unidad el MCU puede quedar en 87.5/87.6 hasta que tune(); antes
                //   solo se comparaba freq si era QS6, as├¡ que no se restauraba pref_last_freq al primer arranque.
                if (mEngine != null && mLastFreq > 0) {
                    boolean tuneToLast = false;
                    if (mEngine.getCurrentFreq() <= 0) {
                        tuneToLast = true;
                    } else if ((mMode == FmMode.FM_QS6 || mMode == FmMode.FM_K706
                            || mMode == FmMode.FM_JANCAR_IVI) && !mIsRecreating) {
                        try {
                            if (mEngine.getCurrentFreq() != mLastFreq) {
                                tuneToLast = true;
                            }
                        } catch (Exception ignored) {
                            tuneToLast = true;
                        }
                    }
                    if (tuneToLast) {
                        Log.d(TAG, "Startup: Tuning to last saved frequency " + mLastFreq + " (mode=" + mMode + ")");
                        if (mMode == FmMode.FM_QS6 && mEngine instanceof QS6Engine) {
                            ((QS6Engine) mEngine).tuneWithBand(mLastFreq, mLastBand);
                        } else {
                            mEngine.tune(mLastFreq);
                        }
                        // QS6/K706: el stack OEM o el MCU pueden reimponer 87.5/87.6 justo tras el init.
                        // Reforzamos la sinton├¡a a la ├║ltima guardada una vez pasa la r├ífaga de callbacks iniciales.
                        if (mMode == FmMode.FM_QS6 || mMode == FmMode.FM_K706
                                || mMode == FmMode.FM_JANCAR_IVI) {
                            final int targetFreq = mLastFreq;
                            final int targetBand = mLastBand;
                            mMainHandler.postDelayed(() -> {
                                try {
                                    if (isFinishing() || isDestroyed() || mEngine == null) return;
                                    int current = mEngine.getCurrentFreq();
                                    int currentBand = mEngine.getCurrentBand();
                                    if (current != targetFreq || currentBand != targetBand) {
                                        Log.d(TAG, "Startup reinforce: retuning "
                                                + current + "/B" + currentBand + " -> "
                                                + targetFreq + "/B" + targetBand + " (mode=" + mMode + ")");
                                        if (mEngine instanceof QS6Engine) {
                                            ((QS6Engine) mEngine).tuneWithBand(targetFreq, targetBand);
                                        } else {
                                            mEngine.tune(targetFreq);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Startup reinforce retune", e);
                                }
                            }, 1400L);
                        }
                    }
                }

                startStatusPolling();

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (mEngine != null) {
                        showToast(getString(R.string.toast_hardware_colon, mEngine.getEngineName()));
                        refreshPresetsCache();
                        refreshPresetButtons();
                        refreshRadioStatus();
                        
                        // V20.3/V21.1: Agresivo siempre que estemos en Cold Start. 
                        // En recreaci├│n (layout switch) delegar en el init del motor si ya hay audio 
                        // pero si detectamos mute injustificado, forzar recuperaci├│n.
                        // V18.6.5: NO desmutear si LIVE streaming est├í activo (evita audio duplicado).
                        boolean liveActive = mOnlineStreamManager != null && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading());
                        if (mPlaybackManager != null && !liveActive) {
                            if (!mIsRecreating) {
                                Log.d(TAG, "Startup Audio Recovery (Cold Start): Forzando desmuteo");
                                mPlaybackManager.setMute(false);
                            } else if (mMuteState) {
                                Log.d(TAG, "Startup Audio Recovery (Recreation): Detectado mute previo, forzando recuperaci├│n");
                                mPlaybackManager.setMute(false);
                            }
                            // K706: Tras init MCU + AudioFocus, a veces un LOSS espurio o el bind de
                            // MediaBrowser deja el canal FM muteado aunque PlaybackManager crea estar en false.
                            // Un segundo/tercer setMute(false) retardado fuerza enforceAudioRecovery() y RPC coherente.
                            if (mMode == FmMode.FM_K706 && mEngine != null) {
                                mMainHandler.postDelayed(() -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    boolean live = mOnlineStreamManager != null
                                            && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading());
                                    if (live || mPlaybackManager == null || mEngine == null) return;
                                    Log.d(TAG, "K706: recuperaci├│n de audio retardada (+450ms) tras arranque");
                                    mPlaybackManager.setMute(false);
                                }, 450L);
                                mMainHandler.postDelayed(() -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    boolean live = mOnlineStreamManager != null
                                            && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading());
                                    if (live || mPlaybackManager == null || mEngine == null) return;
                                    Log.d(TAG, "K706: recuperaci├│n de audio retardada (+1500ms) tras arranque");
                                    mPlaybackManager.setMute(false);
                                }, 1500L);
                            }
                        } else if (liveActive) {
                            Log.d(TAG, "Startup Audio Recovery: LIVE activo, no se desmutea la radio FM");
                        }
                    }
                });

                if (mMode == FmMode.FM_K706 && mEngine instanceof K706Engine
                        && getIntent() != null
                        && getIntent().getBooleanExtra(
                                com.example.openradiofm.services.FactoryRadioHijackerService.EXTRA_FROM_HIJACKER,
                                false)) {
                    getIntent().removeExtra(
                            com.example.openradiofm.services.FactoryRadioHijackerService.EXTRA_FROM_HIJACKER);
                    IntentRouter.scheduleK706McuListenerReassertAfterOem(MainActivity.this, "from_hijacker_cold", 850L);
                }
            };

            // V22.4: Retardo de estabilización de 1200ms específico para QS6 (NWD) 
            // para evitar DeadObjectException y asegurar que el servicio de audio esté listo.
            if (mEngine != null && mEngine.getEngineName().contains("QS6")) {
                Log.d(TAG, "QS6 Startup: Aplicando pausa de estabilización de 1200ms...");
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(postInitAction, 1200);
            } else {
                postInitAction.run();
            }
        }

        @Override
        public void onServiceConnected(IRadioServiceAPI service) {
            mRadioService = service;
            Log.d(TAG, "onServiceConnected: Servicio AIDL legado (HCN) recibido. Inicializando motor...");

            // Solo manejamos MT8163/HCN aqu├¡, ya que QS6 se inicializa de forma as├¡ncrona e
            // independiente.
            if (mMode == FmMode.FM_MT8163) {
                // V21.3: Si ya tenemos el motor MT8163 creado, reutilizarlo en vez de crear uno nuevo.
                // Esto previene la duplicidad de hilos de polling y fugas de recursos.
                if (mEngine instanceof com.example.openradiofm.data.source.MT8163Engine) {
                    Log.i(TAG, "onServiceConnected: Reutilizando instancia existente del motor MT8163.");
                    ((com.example.openradiofm.data.source.MT8163Engine) mEngine).updateService(mRadioService);
                    return;
                }

                RadioEngine engine = new com.example.openradiofm.data.source.MT8163Engine();
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
        MainActivityBootstrap.runAfterSuper(this, savedInstanceState);
    }

    /**
     * Antes de {@link #recreate()} al ciclar V2/V3/Simple: invalida trabajo asíncrono de logo/station-info
     * y libera Glide en la jerarquía actual para no arrastrar bitmaps ni peticiones al siguiente layout.
     */
    private void prepareForLayoutModeRecreate() {
        try {
            mRdsLogoTransition.logoUiGeneration.incrementAndGet();
        } catch (Exception ignored) {}
        try {
            int s = mStationInfoSeq.incrementAndGet();
            mLastStationInfoRequestedSeq = s;
        } catch (Exception ignored) {}
        if (mLogoManager != null) {
            try {
                mLogoManager.release();
            } catch (Exception e) {
                Log.w(TAG, "prepareForLayoutModeRecreate: LogoManager.release", e);
            }
        }
    }

    /**
     * Si {@link #mIsRecreating} (recreación con estado guardado), fuerza otro {@link #refreshRadioStatus()}
     * tras un tick para que logo/RDS/presets coincidan con el nuevo XML (el primero puede adelantarse al layout).
     */
    void scheduleRadioUiResyncAfterRecreation() {
        if (!mIsRecreating) return;
        mMainHandler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            mLastRefreshFreq = -1;
            mLastRefreshBand = -1;
            refreshRadioStatus();
        }, 200L);
    }

    private static final String PREF_ONBOARDING_DONE = "pref_onboarding_lang_country_done";
    private static final String PREF_ONBOARDING_LANG_DONE = "pref_onboarding_lang_done";
    private static final String PREF_ONBOARDING_COUNTRY_DONE = "pref_onboarding_country_done";

    void ensureFirstRunLanguageAndCountry() {
        try {
            if (mPrefs == null) return;
            if (mPrefs.getBoolean(PREF_ONBOARDING_DONE, false)) return;

            boolean langDone = mPrefs.getBoolean(PREF_ONBOARDING_LANG_DONE, false);
            boolean countryDone = mPrefs.getBoolean(PREF_ONBOARDING_COUNTRY_DONE, false)
                    || com.example.openradiofm.utils.CountryPrefs.isCountrySet(this);

            // Paso 1: Idioma (forzado) si no se ha completado aún.
            if (!langDone) {
                DialogManager dm = (mDialogManager != null) ? mDialogManager : new DialogManager(this);
                dm.showLanguageSelector(true, selectedLang -> {
                    try {
                        if (selectedLang == null || selectedLang.trim().isEmpty()) return;
                        mPrefs.edit().putString("app_language", selectedLang.trim()).putBoolean(PREF_ONBOARDING_LANG_DONE, true).apply();
                        recreate(); // reaplicar contexto/strings
                    } catch (Exception ignored) {}
                });
                return;
            }

            // Paso 2: País (forzado) si no se ha completado aún.
            if (!countryDone) {
                showCountrySelectorDialog(true);
                return;
            }

            mPrefs.edit().putBoolean(PREF_ONBOARDING_COUNTRY_DONE, true).putBoolean(PREF_ONBOARDING_DONE, true).apply();
        } catch (Exception ignored) {}
    }

    /** Si {@code forceChoose} es true, el diálogo no se puede cancelar. */
    public void showCountrySelectorDialog(boolean forceChoose) {
        try {
            final String[] codes = new String[] { "ES", "RU", "IT", "GR", "RO", "HU", "PT", "DE", "PL", "SI", "OT" };
            final String[] labels = new String[] {
                    getString(R.string.country_es),
                    getString(R.string.country_ru),
                    getString(R.string.country_it),
                    getString(R.string.country_gr),
                    getString(R.string.country_ro),
                    getString(R.string.country_hu),
                    getString(R.string.country_pt),
                    getString(R.string.country_de),
                    getString(R.string.country_pl),
                    getString(R.string.country_si),
                    getString(R.string.country_other)
            };

            String current = com.example.openradiofm.utils.CountryPrefs.getCountry(this);
            int checked = 0;
            for (int i = 0; i < codes.length; i++) {
                if (codes[i].equalsIgnoreCase(current)) { checked = i; break; }
            }
            final int[] selected = new int[] { checked };

            // Usar el mismo estilo “grid selector” que el resto de menús (idioma/tema/fuentes)
            DialogManager dm = (mDialogManager != null) ? mDialogManager : new DialogManager(this);
            dm.showCountrySelector(forceChoose, labels, codes, checked, selectedCode -> {
                try {
                    com.example.openradiofm.utils.CountryPrefs.setCountry(this, selectedCode);
                    if (mPrefs != null) {
                        boolean langDone = mPrefs.getBoolean(PREF_ONBOARDING_LANG_DONE, false);
                        mPrefs.edit()
                                .putBoolean(PREF_ONBOARDING_COUNTRY_DONE, true)
                                .putBoolean(PREF_ONBOARDING_DONE, langDone)
                                .apply();
                    }
                } catch (Exception ignored) {}
            });

            // Si es parte del onboarding, marcarlo como completado (se guarda al seleccionar).
            if (forceChoose && mPrefs != null) {
                // Se marca definitivamente cuando ya haya un país configurado.
                // Esto evita dar por completado el paso si el usuario no selecciona nada.
                // (El selector forzado no tiene cancelar).
            }
        } catch (Exception ignored) {}
    }

    public String countryLabelForCode(String countryCode) {
        String cc = (countryCode == null) ? "" : countryCode.trim().toUpperCase();
        switch (cc) {
            case "RU": return getString(R.string.country_ru);
            case "IT": return getString(R.string.country_it);
            case "GR": return getString(R.string.country_gr);
            case "RO": return getString(R.string.country_ro);
            case "HU": return getString(R.string.country_hu);
            case "PT": return getString(R.string.country_pt);
            case "DE": return getString(R.string.country_de);
            case "PL": return getString(R.string.country_pl);
            case "SI": return getString(R.string.country_si);
            case "OT": return getString(R.string.country_other);
            case "ES":
            default: return getString(R.string.country_es);
        }
    }

    /**
     * Tras cambiar drawables de pack (PNG/SVG), reaplica tintes de skin (noche, CLEAR)
     * y el icono cloud (streaming / idle / modo noche).
     */
    private void reapplySkinTintsAfterIconPack() {
        reapplyVisualStateForCurrentSkin();
    }

    /**
     * Re-tinta un control tras sustituir el drawable (pack); coherente con modo noche / CLEAR.
     */
    void retintControlButtonForCurrentSkin(ImageView iv) {
        if (iv == null || mThemeManager == null) return;
        com.example.openradiofm.ui.theme.ThemeManager.Skin skin = mThemeManager.getActiveSkin();
        if (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
            int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
            setColorFilterIfChanged(iv, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.DAY_MODE) {
            setColorFilterIfChanged(iv, android.graphics.Color.BLACK, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR) {
            setColorFilterIfChanged(iv, android.graphics.Color.BLACK, android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            setColorFilterIfChanged(iv, null, null);
        }
    }

    /**
     * Sincroniza el botón LOC/DX con el estado del motor (drawable por defecto, pack y tinte de skin).
     */
    @Override
    public void syncLocDxButtonVisual(boolean isLocal) {
        if (mUiMediator.btnLocDx == null) return;
        mUiMediator.btnLocDx.setSelected(isLocal);
        mUiMediator.btnLocDx.setAlpha(1.0f);
        setImageResourceIfChanged(mUiMediator.btnLocDx, isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
        if (mIconPackManager != null) {
            mIconPackManager.apply(mUiMediator.btnLocDx,
                    isLocal ? "radio_loc_p" : "radio_loc_n",
                    isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
        }
        retintControlButtonForCurrentSkin(mUiMediator.btnLocDx);
    }

    /**
     * Aplica el pack de iconos seleccionado a los ImageButtons/ImageViews visibles.
     * Si un PNG del pack no existe, se mantiene el drawable resource actual.
     */
    public void applyIconPack() {
        if (mIconPackManager == null) return;
        try {
            boolean loc = mEngine != null && mEngine.isDxLocal();
            // Controles comunes (Layout V2/V3)
            mIconPackManager.apply((ImageView) findViewById(R.id.btnSeekDown), "seek_down", R.drawable.seek_down);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnSeekUp), "seek_up", R.drawable.seek_up);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnFavPrev), "btn_previous_n", R.drawable.btn_previous_n);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnFavNext), "btn_next_n", R.drawable.btn_next_n);

            mIconPackManager.apply((ImageView) findViewById(R.id.btnSettings), "radio_eq_n", R.drawable.radio_eq_n);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnBand), "radio_band_n", R.drawable.radio_band_n);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnLocDx),
                    loc ? "radio_loc_p" : "radio_loc_n",
                    loc ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnAutoScan), "radio_scan_icon_f", R.drawable.radio_scan_icon_f);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnMute), "radio_mute_n", R.drawable.radio_mute_n);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnGps), "radio_gps", R.drawable.radio_gps);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnExtra1), "ic_android_settings", R.drawable.ic_android_settings);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnExtra2), "ic_save_load", R.drawable.ic_save_load);
            mIconPackManager.apply((ImageView) findViewById(R.id.btnPowerOff), "power_off", R.drawable.power_off);

            mIconPackManager.apply((ImageView) findViewById(R.id.ivDataActivityIcon), "cloud", R.drawable.cloud);
        } catch (Exception ignored) {}
        applyDevAutoScanButtonState();
        reapplySkinTintsAfterIconPack();
    }

    /** Refleja pref_dev_autoscan_enabled en el botón AutoScan (alpha / aspecto “experimental”). */
    public void applyDevAutoScanButtonState() {
        android.widget.ImageButton btn = findViewById(R.id.btnAutoScan);
        if (btn == null) {
            return;
        }
        boolean on = false;
        try {
            android.content.SharedPreferences p = mPrefs != null ? mPrefs
                    : getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
            on = p.getBoolean(DevAutoscanToggleHelper.PREF_DEV_AUTOSCAN_ENABLED,
                    DevAutoscanToggleHelper.DEFAULT_DEV_AUTOSCAN_ENABLED);
        } catch (Exception ignored) {}
        btn.setAlpha(on ? 1f : 0.45f);
    }

    /**
     * Layout V2 (vertical): permite espejar columnas.
     * - false: presets a la izquierda (actual)
     * - true: presets a la derecha (espejo)
     */
    public void applyLayout2SidePreference() {
        if (mIsSimpleLayout || isV3LayoutActive() || mPrefs == null) return;
        View root = findViewById(R.id.rootLayout);
        if (!(root instanceof androidx.constraintlayout.widget.ConstraintLayout)) return;
        androidx.constraintlayout.widget.ConstraintLayout cl = (androidx.constraintlayout.widget.ConstraintLayout) root;
        boolean presetsRight = mPrefs.getBoolean("pref_layout2_presets_right", false);

        // Importante: solo intercambiar columnas 1 y 3. La columna central (entre guideline_col1 y guideline_col2)
        // debe permanecer igual que en el layout original.
        try {
            androidx.constraintlayout.widget.ConstraintSet set = new androidx.constraintlayout.widget.ConstraintSet();
            set.clone(cl);

            final int parent = androidx.constraintlayout.widget.ConstraintSet.PARENT_ID;
            final int col1 = R.id.guideline_col1;
            final int col2 = R.id.guideline_col2;

            // Columna 1: presets (ScrollView)
            final int presets = R.id.scrollViewPresets;
            if (presetsRight) {
                // Presets -> derecha (entre guideline_col2 y parent)
                set.clear(presets, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(presets, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(presets, androidx.constraintlayout.widget.ConstraintSet.START, col2, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(presets, androidx.constraintlayout.widget.ConstraintSet.END, parent, androidx.constraintlayout.widget.ConstraintSet.END, 0);

                // Columna 3 -> izquierda (entre parent y guideline_col1), manteniendo pares en 2 columnas
                set.clear(R.id.boxLogo, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.boxLogo, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.boxLogo, androidx.constraintlayout.widget.ConstraintSet.START, parent, androidx.constraintlayout.widget.ConstraintSet.START, 0);
                set.connect(R.id.boxLogo, androidx.constraintlayout.widget.ConstraintSet.END, col1, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                // Fila 1: Extra1 | Extra2
                set.clear(R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.START, parent, androidx.constraintlayout.widget.ConstraintSet.START, 0);
                set.connect(R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.END, R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                set.clear(R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.START, R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.END, col1, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                // Fila 2: Mute | GPS
                set.clear(R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.START, parent, androidx.constraintlayout.widget.ConstraintSet.START, 0);
                set.connect(R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.END, R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                set.clear(R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.START, R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.END, col1, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                // Fila 3: Settings | PowerOff
                set.clear(R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.START, parent, androidx.constraintlayout.widget.ConstraintSet.START, 0);
                set.connect(R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.END, R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                set.clear(R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.START, R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.END, col1, androidx.constraintlayout.widget.ConstraintSet.START, 0);
            } else {
                // Original: presets -> izquierda (entre parent y guideline_col1)
                set.clear(presets, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(presets, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(presets, androidx.constraintlayout.widget.ConstraintSet.START, parent, androidx.constraintlayout.widget.ConstraintSet.START, 0);
                set.connect(presets, androidx.constraintlayout.widget.ConstraintSet.END, col1, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                // Original: columna 3 -> derecha (entre guideline_col2 y parent), manteniendo pares en 2 columnas
                set.clear(R.id.boxLogo, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.boxLogo, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.boxLogo, androidx.constraintlayout.widget.ConstraintSet.START, col2, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.boxLogo, androidx.constraintlayout.widget.ConstraintSet.END, parent, androidx.constraintlayout.widget.ConstraintSet.END, 0);

                set.clear(R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.START, col2, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.END, R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                set.clear(R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.START, R.id.btnExtra1, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnExtra2, androidx.constraintlayout.widget.ConstraintSet.END, parent, androidx.constraintlayout.widget.ConstraintSet.END, 0);

                set.clear(R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.START, col2, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.END, R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                set.clear(R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.START, R.id.btnMute, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnGps, androidx.constraintlayout.widget.ConstraintSet.END, parent, androidx.constraintlayout.widget.ConstraintSet.END, 0);

                set.clear(R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.START, col2, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.END, R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.START, 0);

                set.clear(R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.START);
                set.clear(R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.END);
                set.connect(R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.START, R.id.btnSettings, androidx.constraintlayout.widget.ConstraintSet.END, 0);
                set.connect(R.id.btnPowerOff, androidx.constraintlayout.widget.ConstraintSet.END, parent, androidx.constraintlayout.widget.ConstraintSet.END, 0);
            }

            set.applyTo(cl);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "onNewIntent: App ya activa, refrescando parámetros (Single Instance).");
        IntentRouter.dispatchNewIntent(this, intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLifecycleCoordinator != null) mLifecycleCoordinator.onResume();
    }

    @Override
    protected void onPause() {
        if (mLifecycleCoordinator != null) mLifecycleCoordinator.onPause();
        super.onPause();
    }

    private static final String PREF_HIHACK_HEALTH_WARNED_AT_MS = "pref_hihack_health_warned_at_ms";
    private static final long HIHACK_HEARTBEAT_STALE_MS = 2 * 60_000L; // 2 min

    private void maybeWarnHihackNotWorking() {
        if (mPrefs == null) return;
        if (!isFactoryRadioHijackerAccessibilityEnabled(this)) return;

        long lastHb;
        try {
            lastHb = mPrefs.getLong(com.example.openradiofm.services.FactoryRadioHijackerService.PREF_HIHACK_HEARTBEAT_MS, 0L);
        } catch (Exception e) {
            return;
        }
        if (lastHb <= 0L) return;

        long now = System.currentTimeMillis();
        if (now - lastHb <= HIHACK_HEARTBEAT_STALE_MS) return;

        long lastWarn = 0L;
        try { lastWarn = mPrefs.getLong(PREF_HIHACK_HEALTH_WARNED_AT_MS, 0L); } catch (Exception ignored) {}
        if (now - lastWarn < 10 * 60_000L) return; // no spamear (10 min)

        mPrefs.edit().putLong(PREF_HIHACK_HEALTH_WARNED_AT_MS, now).apply();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.a11y_hihack_not_working_title))
                .setMessage(getString(R.string.a11y_hihack_not_working_message))
                .setPositiveButton(getString(R.string.a11y_hihack_not_working_open_accessibility), (d, w) -> {
                    try {
                        Intent i = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    } catch (Exception ignored) {}
                })
                .setNegativeButton(getString(R.string.a11y_hihack_not_working_later), null)
                .show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyStatusBarVisibility();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLifecycleCoordinator != null) mLifecycleCoordinator.onStart();
    }

    /**
     * QS6/NWD: al ir a segundo plano, dejar de reclamar AudioFocus (sin conmutar fuente MCU
     * a Android — menos agresivo que {@code switchToAndroidAudio()}). Evita competir con el
     * reproductor nativo. onResume sigue llamando a {@code switchToFmAudio()} cuando toca.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mLifecycleCoordinator != null) mLifecycleCoordinator.onStop();
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
        if (mUiMediator.ivMainLogo != null && !mIsSimpleLayout) {
            // Click normal: Cambiar Color (Ciclar Skin)
            mUiMediator.ivMainLogo.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = mThemeManager.cycleSkin();
                applySkin(next);
                showToast(getString(R.string.toast_skin_colon, next.displayName));
            });

            // Long click: Modo Noche (toggle)
            mUiMediator.ivMainLogo.setOnLongClickListener(v -> {
                cycleClassicNightDay();
                return true;
            });
        }

        // V18.6: Reloj Digital también permite ciclar skin
        
        if (mUiMediator.tvDigitalClock != null) {
            mUiMediator.tvDigitalClock.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = mThemeManager.cycleSkin();
                applySkin(next);
                showToast(getString(R.string.toast_skin_colon, next.displayName));
            });
            // Long click: Modo Noche (toggle)
            mUiMediator.tvDigitalClock.setOnLongClickListener(v -> {
                cycleClassicNightDay();
                return true;
            });
        }

        // V3: Logo coche (slot superior derecho) = mismos gestos que el reloj: tap cicla skin, largo = modo noche
        if (mIsV3) {
            if (mUiMediator.ivCarLogo != null) {
                mUiMediator.ivCarLogo.setOnClickListener(v -> {
                    com.example.openradiofm.ui.theme.ThemeManager.Skin next = mThemeManager.cycleSkin();
                    applySkin(next);
                    showToast(getString(R.string.toast_skin_colon, next.displayName));
                });
                mUiMediator.ivCarLogo.setOnLongClickListener(v -> {
                    cycleClassicNightDay();
                    return true;
                });
            }
        }
    }

    /**
     * Cicla rápidamente entre CLASSIC -> NIGHT_MODE -> DAY_MODE -> CLASSIC.
     * Respeta el kill-switch de desarrollo de Day Mode.
     */
    private void cycleClassicNightDay() {
        if (mSkinCoordinator != null) mSkinCoordinator.cycleClassicNightDay();
    }

    /**
     * Toggle manual de Modo Noche (sin depender del auto-night).
     * - Si estamos en NIGHT_MODE, vuelve al skin persistido (o CLASSIC si no existe).
     * - Si no, activa NIGHT_MODE y recuerda el skin previo.
     */
    public void toggleNightMode() {
        if (mSkinCoordinator != null) mSkinCoordinator.toggleNightMode();
    }

    @Override
    protected void onDestroy() {
        if (mFrequencyChangeCoordinator != null) mFrequencyChangeCoordinator.cancelPendingHeavy();
        super.onDestroy();
        if (mLifecycleCoordinator != null) mLifecycleCoordinator.onDestroy();
        // QS6: el cliente experimental de KernelService se mantiene vivo entre aperturas del menú de ingeniería,
        // pero debe liberarse cuando la Activity termina de verdad.
        try {
            if (isFinishing() && mMode == FmMode.FM_QS6) {
                QS6EngineeringDialog.releaseSharedKernelClient();
            }
        } catch (Exception ignored) {}
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
     * V15.6: Aplica la tipografía global inmediatamente tras cargar el layout.
     * Implementación en {@link SkinCoordinator#applyFonts()}.
     */
    public void applyFonts() {
        if (mSkinCoordinator != null) mSkinCoordinator.applyFonts();
    }

    /**
     * V15.6: Aplica una fuente de forma recursiva a TextViews bajo {@code v}.
     * Implementación en {@link SkinCoordinator#applyRecursiveFont(android.view.View, android.graphics.Typeface)}.
     */
    public void applyRecursiveFont(View v, android.graphics.Typeface tf) {
        if (mSkinCoordinator != null) mSkinCoordinator.applyRecursiveFont(v, tf);
    }

    public void seekUp() {
        if (mEngine != null) {
            runOnUiThread(this::clearStationLogoUi);
            try { com.example.openradiofm.utils.RadioActivityFileLogger.logBasic(this, "UI", "seekUp()"); } catch (Exception ignored) {}
            mEngine.seekUp();
        }
    }

    public void seekDown() {
        if (mEngine != null) {
            runOnUiThread(this::clearStationLogoUi);
            try { com.example.openradiofm.utils.RadioActivityFileLogger.logBasic(this, "UI", "seekDown()"); } catch (Exception ignored) {}
            mEngine.seekDown();
        }
    }

    /**
     * Indicador ST: color según skin y alpha según recepción estéreo y {@code pref_stereo_mode_on}.
     * Siempre visible; en mono (recepción) queda ~medio alpha; con “forzar mono” del usuario, más apagado.
     *
     * @param rxActiveIfNotNull si no es null, valor ya combinado (p. ej. streaming || isStereo) para el alpha;
     *                          si es null, se calcula aquí.
     */
    @Override
    public void refreshStereoIndicatorUi(Boolean rxActiveIfNotNull) {
        if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed())) {
            return;
        }
        TextView st = findViewById(R.id.ivStereoIcon);
        if (st == null) return;

        boolean streaming = mOnlineStreamManager != null
                && (mOnlineStreamManager.isPlaying() || mOnlineStreamManager.isLoading());
        final boolean rxActive;
        if (rxActiveIfNotNull != null) {
            rxActive = rxActiveIfNotNull;
        } else {
            rxActive = streaming || (mEngine != null && mEngine.isStereo());
        }

        boolean manualStereo = mPrefs != null && mPrefs.getBoolean("pref_stereo_mode_on", true);

        ThemeManager.Skin skin = mThemeManager != null
                ? mThemeManager.getActiveSkin() : ThemeManager.Skin.CLASSIC;
        int nightBlue = getResources().getColor(R.color.night_blue_primary, null);
        int color;
        if (skin == ThemeManager.Skin.NIGHT_MODE) {
            color = nightBlue;
        } else if (skin == ThemeManager.Skin.DAY_MODE || skin == ThemeManager.Skin.CLEAR) {
            color = Color.BLACK;
        } else {
            color = Color.WHITE;
        }
        setTextColorIfChanged(st, color);
        if (skin == ThemeManager.Skin.NIGHT_MODE) {
            st.setShadowLayer(8f, 0, 0, color);
        } else {
            st.setShadowLayer(0, 0, 0, 0);
        }

        st.setVisibility(View.VISIBLE);
        if (!manualStereo) {
            st.setAlpha(0.4f);
        } else {
            st.setAlpha(rxActive ? 1.0f : 0.55f);
        }
    }

    public void refreshPresetButtons() {
        if (mPresetManager != null) {
            mPresetManager.refreshButtons(mCurrentBand);
        }
    }

    /**
     * VXX: Opcional "Relieve HD" para logos (launcher y presets).
     * Se aplica/retira con setForeground para no tocar el contenido de Glide/bitmaps.
     */
    public void applyReliefHd(boolean enabled) {
        Drawable relief = null;
        if (enabled) {
            try {
                relief = ContextCompat.getDrawable(this, R.drawable.fg_logo_relief);
            } catch (Exception ignored) {
                // Si el drawable no está disponible por algún motivo, no romper UI.
                relief = null;
            }
        }

        // 1) Logo principal
        try {
            if (mUiMediator.ivMainLogo != null) {
                mUiMediator.ivMainLogo.setForeground(relief);
                mUiMediator.ivMainLogo.setForegroundGravity(android.view.Gravity.FILL);
            }
        } catch (Exception ignored) {}

        // 2) Presets (ivP1..ivP18)
        try {
            for (int i = 1; i <= 18; i++) {
                int ivId = getResources().getIdentifier("ivP" + i, "id", getPackageName());
                if (ivId == 0) continue;
                ImageView iv = findViewById(ivId);
                if (iv != null) {
                    iv.setForeground(relief);
                    iv.setForegroundGravity(android.view.Gravity.FILL);
                }
            }
        } catch (Exception ignored) {}
    }

    public boolean usesTablerPresetNumbers() {
        try {
            return mPrefs != null && mPrefs.getInt(PREF_PRESET_NUMBERS_STYLE, 0) == 1;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Devuelve el drawable para el número de preset (si el estilo lo requiere).
     * Si devuelve null, el caller debe usar el drawable del APK (radio_icon_pXX).
     */
    public Drawable getPresetNumberDrawable(int presetIdx) {
        if (!usesTablerPresetNumbers()) return null;
        if (mPresetNumberIconManager == null) return null;
        return mPresetNumberIconManager.loadNumberSmallSvg(presetIdx);
    }

    public int getPresetNumberResId(int presetIdx) {
        try {
            int resId = getResources().getIdentifier(
                    "radio_icon_p" + String.format("%02d", presetIdx),
                    "drawable",
                    getPackageName()
            );
            return resId != 0 ? resId : R.drawable.radio_icon_p01;
        } catch (Exception ignored) {
            return R.drawable.radio_icon_p01;
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

    void setupCreditsEasterEgg() {
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
            Log.e(TAG, "Excepción al crear carpeta RadioLogos", e);
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

    /**
     * Reset unificado del arte de emisora (logo, fondo dinámico en V3, Glide, etc.).
     * Delega en {@link BaseLayoutController#updateLogo} cuando hay controlador
     * (V2/V3/Simple); si no, llama directamente a {@link LogoManager#clearLogo()}.
     */
    @Override
    public void clearStationLogoUi() {
        if (isFinishing()) return;
        if (android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed()) return;
        if (mUiController != null) {
            mUiController.updateLogo(null);
        } else if (mLogoManager != null) {
            mLogoManager.clearLogo();
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
        if (mStatusRefreshCoordinator != null) mStatusRefreshCoordinator.refreshRadioStatus();
    }

    /**
     * V5.2: Broadcast to K706 Launcher Widget.
     *
     * @deprecated Usar {@link #sendWidgetUpdate(int, int, String)} en su lugar.
     */
    @Deprecated
    public void sendWidgetUpdateIntent(int freq, int band, String rdsName) {
        sendWidgetUpdate(freq, band, rdsName);
    }

    /**
     * V23.0: Actualiza los widgets del launcher (OEM y propio).
     * Delega la lógica de broadcasts específicos al motor de radio activo para
     * evitar dependencias directas con {@code com.qf.*} u otras plataformas en la UI.
     */
    @Override
    public void sendWidgetUpdate(int freq, int band, String rdsName) {
        if (mWidgetBroadcastManager != null) {
            mWidgetBroadcastManager.sendUpdate(this, freq, band, rdsName, 
                    getPresetIndex(freq), (mEngine != null && mEngine.isStereo()), 
                    mRepository, mEngine);
        }
    }

    /**
     * Calcula una calidad de señal estimada basándose en flags disponibles.
     * Algoritmo basado en CHIP_RADIO_SNR_RSSI.md
     */

    private static String bandShortText(int band) {
        if (band == BAND_FM1) return "FM1";
        if (band == BAND_FM2) return "FM2";
        if (band == BAND_FM3) return "FM3";
        if (band == BAND_AM1) return "AM1";
        if (band == BAND_AM2) return "AM2";
        return "FM1";
    }

    private static String unitShortText(int band) {
        return (band == BAND_AM1 || band == BAND_AM2) ? "kHz" : "MHz";
    }

    @Override
    public void updateBandImage(int band) {
        if (ivBandIndicator != null) {
            setTextIfChanged(ivBandIndicator, bandShortText(band));
            // El color (noche/clear) lo gestionan Theme/Night managers y controllers.
            if (mUiMediator.btnBand != null) setImageResourceIfChanged(mUiMediator.btnBand, R.drawable.radio_band_n);
        } else if (mUiMediator.btnBand != null) {
            // Fallback legacy si falta el view del layout.
            setImageResourceIfChanged(mUiMediator.btnBand, R.drawable.radio_band_n);
        }
        if (ivUnitLabel != null) {
            setTextIfChanged(ivUnitLabel, unitShortText(band));
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

    public void restartAppForSettings() {
        try {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        } catch (Exception e) {
            try {
                recreate();
            } catch (Exception ignored) {}
        }
    }

    private void launchExternalApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            showToast(getString(R.string.toast_app_not_installed, packageName));
        }
    }

    // V4.0: Saved Preset Indicator & Color Logic (Unified)
    // V4.8.6: Ahora acepta rdsName para evitar hilos redundantes si ya se obtuvo antes.
    @Override
    public void updateFrequencyDisplay(int freq, String rdsName) {
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
        if (mSkinCoordinator != null) mSkinCoordinator.applySkin(skin);
    }

    /**
     * Aplica el “estado visual” completo en un orden único para evitar pisadas:
     * Theme -> Controllers -> Night/Day -> Clear -> Cloud.
     */
    private void applyVisualStateForSkin(com.example.openradiofm.ui.theme.ThemeManager.Skin prevSkin,
                                         com.example.openradiofm.ui.theme.ThemeManager.Skin skin) {
        boolean isNight = (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
        boolean isClear = (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
        boolean isDay = (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.DAY_MODE);

        // Layout V3: nunca mostrar el logo pequeño (evita que reaparezca tras cambios de layout/zapping).
        try {
            if (isV3LayoutActive()) {
                android.view.View v = findViewById(R.id.ivMainLogo);
                if (v instanceof android.widget.ImageView) {
                    android.widget.ImageView iv = (android.widget.ImageView) v;
                    setVisibilityIfChanged(iv, android.view.View.GONE);
                    iv.setImageDrawable(null);
                }
            }
        } catch (Exception ignored) {}

        // Controllers: textos base (ellos mismos consultan activeSkin para DAY/CLEAR).
        if (mUiController != null) {
            mUiController.applySkin(isNight);
        } else if (mIsSimpleLayout && mSimpleLayoutManager != null) {
            mSimpleLayoutManager.applyColors(isNight);
        }

        // Night mode (azul)
        if (mNightModeManager != null) {
            if (isNight) mNightModeManager.applyNightModeColors(mLastFreq);
            else mNightModeManager.resetNightModeColors(mLastFreq);
        }

        // Day mode (negro). No resetear salvo al salir de DAY_MODE.
        try {
            if (mDayModeManager != null) {
                if (isDay) {
                    mDayModeManager.applyDayModeColors(mLastFreq);
                } else if (prevSkin == com.example.openradiofm.ui.theme.ThemeManager.Skin.DAY_MODE) {
                    mDayModeManager.resetDayModeColors(mLastFreq);
                }
            }
        } catch (Exception ignored) {}

        // Clear button tint:
        // - Nunca ejecutar en isNight (evita borrar tintes azules).
        // - No ejecutar en isDay: DayModeManager gestiona el tinte negro; llamar con enabled=false lo borraría.
        if (!isNight && !isDay) {
            applyClearButtonIconTint(isClear);
        }

        // Cloud al final (idle/streaming/noche/día/clear).
        updateDataActivityUI();

        // Reloj: reaplicar siempre aquí para que no se quede “pegado” tras cambios de layout/skin.
        try {
            if (mUiMediator.tvDigitalClock != null) {
                if (isNight) {
                    mUiMediator.tvDigitalClock.setTextColor(getResources().getColor(R.color.night_blue_primary, null));
                } else if (isDay || isClear) {
                    mUiMediator.tvDigitalClock.setTextColor(android.graphics.Color.BLACK);
                } else {
                    mUiMediator.tvDigitalClock.setTextColor(android.graphics.Color.WHITE);
                }
            }
        } catch (Exception ignored) {}

        try {
            if (mSignalMeterCoordinator != null) {
                mSignalMeterCoordinator.applyModeVisibility();
                mSignalMeterCoordinator.applyBarsAppearanceFromSkin();
            }
        } catch (Exception ignored) {}

        try {
            if (mPresetManager != null) {
                mPresetManager.syncLoopMirrorPresetVisualsWithMainSlots();
            }
        } catch (Exception ignored) {}
    }

    /** Tras cambiar la preferencia en Ajustes premium (sin recrear la actividad). */
    public void applySignalMeterPreferenceFromSettings() {
        if (mSignalMeterCoordinator != null) {
            mSignalMeterCoordinator.applyModeVisibility();
            mSignalMeterCoordinator.applyBarsAppearanceFromSkin();
        }
        try {
            refreshRadioStatus();
        } catch (Exception ignored) {}
    }

    /** Reaplica tintes/colores del skin activo (para repintados por band/freq/icon pack). */
    private void reapplyVisualStateForCurrentSkin() {
        if (mSkinCoordinator != null) mSkinCoordinator.reapplyVisualStateForCurrentSkin();
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

        // Iconos de estado (cloud: colores en updateDataActivityUI() para no pisar streaming)
        int[] iconIds = {
                R.id.ivAfIcon, R.id.ivTaIcon, R.id.ivTpIcon,
                R.id.ivStereoIcon
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
            runOnUiThread(this::clearStationLogoUi);
            mEngine.stepUp();
            refreshRadioStatus();
        }
    }

    public void stepFreqDown() {
        if (mEngine != null) {
            runOnUiThread(this::clearStationLogoUi);
            mEngine.stepDown();
            refreshRadioStatus();
        }
    }

    // V4: Swipe Listener Class

    // V16: Delegaciones a NightModeManager
    @Override
    public void checkAndApplyNightMode() {
        if (mNightModeManager != null) mNightModeManager.checkAndApplyNightMode();
    }

    // V16: Delegaciones a HistoryManager
    void addToHistory(int freq) {
        if (mHistoryManager != null) mHistoryManager.addToHistory(freq);
    }

    public void saveFavoritesToFile() {
        if (mHistoryManager != null) mHistoryManager.saveFavoritesToFile();
    }

    public void loadFavoritesFromFile() {
        if (mHistoryManager != null) mHistoryManager.loadFavoritesFromFile();
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
            }, IntentRouter.REQ_STORAGE_IMPORT_EXPORT);
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
        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, IntentRouter.REQ_READ_PHONE_STATE_K706);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        IntentRouter.dispatchPermissionsResult(this, requestCode, permissions, grantResults);
    }

    // V15: Wrappers de compatibilidad para diálogos llamados desde otras clases
    public void showEngineSelector() {
        if (mDialogManager != null)
            mDialogManager.showEngineSelector();
    }

    // V5.5: setMute delegado a mPlaybackManager (ver PlaybackManager.java)
    @Override
    public void setMute(boolean mute) {
        if (mPlaybackManager != null) {
            mPlaybackManager.setMute(mute, true);
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
    @Override
    public boolean isStationMemorized(int freq) {
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
    @Override
    public int getPresetIndex(int freq) {
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

    /**
     * Helper interno: obtiene el singleton {@code QFTunerManager} via reflection.
     * <b>Solo válido en plataforma K706.</b> En cualquier otro engine devuelve null
     * sin intentar la reflexión para evitar excepciones innecesarias.
     *
     * @deprecated El control del tuner QF debe delegar en {@link K706Engine} cuando sea posible.
     */
    @Deprecated
    private Object getQFTunerManager() {
        // Guard: no intentar reflexión en plataformas que no son K706
        if (!(mEngine instanceof com.example.openradiofm.data.source.K706Engine)) {
            return null;
        }
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
        if (mPrefs == null || getWindow() == null || getWindow().getDecorView() == null) return;
        boolean showStatusBarV2 = mPrefs.getBoolean("pref_show_status_bar_v2", false);
        runOnUiThread(() -> {
            try {
                if (getWindow() == null) return;
                final android.view.Window window = getWindow();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    android.view.WindowInsetsController c = window.getInsetsController();
                    if (c != null) {
                        if (showStatusBarV2) {
                            c.show(android.view.WindowInsets.Type.statusBars());
                        } else {
                            c.hide(android.view.WindowInsets.Type.statusBars());
                        }
                    }
                }
                
                final android.view.View decor = window.getDecorView();
                if (decor != null) {
                    if (showStatusBarV2) {
                        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        decor.setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_VISIBLE);
                    } else {
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        decor.setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                }
            } catch (Exception e) {
                android.util.Log.w("OpenRadioFm", "applyStatusBarVisibility deferred error: " + e.getMessage());
            }
        });
    }

    public void applyLogoModePreference() {
        if (mPrefs == null) return;
        int logoMode = mPrefs.getInt("pref_logo_mode", 0); // 0=Car, 1=Clock
        runOnUiThread(() -> {
            if (mUiMediator.tvDigitalClock != null) {
                if (logoMode == 1) {
                    mUiMediator.tvDigitalClock.setVisibility(View.VISIBLE);
                    if (mUiMediator.ivCarLogo != null) mUiMediator.ivCarLogo.setVisibility(View.GONE);
                    mClockHandler.removeCallbacks(mClockRunnable);
                    mClockHandler.post(mClockRunnable);
                } else {
                    mUiMediator.tvDigitalClock.setVisibility(View.GONE);
                    if (mUiMediator.ivCarLogo != null) {
                        mUiMediator.ivCarLogo.setVisibility(View.VISIBLE);
                        mLogoManager.loadCarLogo();
                    }
                    mClockHandler.removeCallbacks(mClockRunnable);
                }
            }
        });
    }

    /**
     * Activa una guarda temporal para que callbacks tard├¡os del stack QS6/K706 durante apagado
     * no pisen la ├║ltima emisora guardada.
     */
    public void prepareForPowerOff() {
        if (mLifecycleCoordinator != null) mLifecycleCoordinator.prepareForPowerOff();
    }

    /**
     * K706: "PowerOff" UI en modo B = minimizar a HOME sin silenciar ni disparar cierre total.
     * Evita el flujo prepareForPowerOff() porque puede mutear y además algunos launchers
     * interpretan el cierre como cambio de fuente y ejecutan forceStopPackage().
     */
    public void minimizeToHomeKeepingMediaControl() {
        // Silencio en background (modo B): apagar sonido de FM y soltar AudioFocus,
        // pero mantener MediaSession/FGS para el widget de música.
        try {
            if (mEngine != null) {
                mEngine.setMute(true);
                mEngine.releaseAudioFocusOnlyForBackground();
            } else if (mPlaybackManager != null) {
                // fallback legacy
                mPlaybackManager.setMute(true);
            }
        } catch (Exception ignored) {}

        try {
            // Asegurar que el servicio de medios mantiene sesión/FGS para el widget de música.
            Intent media = new Intent(getApplicationContext(), com.example.openradiofm.service.RadioMediaService.class);
            media.setAction(com.example.openradiofm.service.RadioMediaService.ACTION_FORCE_SESSION_ACTIVE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(media);
            } else {
                startService(media);
            }
        } catch (Exception ignored) {}

        try {
            // No marcar powerOffRequested: eso activa lógica de apagado (mute/guards).
            setPowerOffRequested(false);
        } catch (Exception ignored) {}

        try {
            moveTaskToBack(true);
        } catch (Exception e) {
            // fallback
            try { finish(); } catch (Exception ignored) {}
        }
    }

    /**
     * V13.9: Centralized reset when frequency changes.
     */
    @Override
    public void handleFrequencyChange(int freq) {
        if (mFrequencyChangeCoordinator != null) {
            mFrequencyChangeCoordinator.handleFrequencyChange(freq);
        }
    }

    // V18.6: M├®todos para ocultaci├│n autom├ítica de controles
    public void resetAutoHideTimer() {
        if (mAutoHideHandler == null || mAutoHideRunnable == null) return;
        mAutoHideHandler.removeCallbacks(mAutoHideRunnable);
        if (mPrefs.getBoolean("pref_auto_hide_controls", false) && isV3LayoutActive()) {
            mAutoHideHandler.postDelayed(mAutoHideRunnable, 5000); // 5 segundos
        }
    }

    public void showBottomControls() {
        if (mUiMediator.bottomControls == null) return;
        if (mControlsHidden) {
            mControlsHidden = false;
            mUiMediator.bottomControls.animate()
                    .translationY(0)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .start();
        }
        resetAutoHideTimer();
    }

    public void hideBottomControls() {
        if (mUiMediator.bottomControls == null || mControlsHidden) return;
        if (!mPrefs.getBoolean("pref_auto_hide_controls", false)) return;
        
        mControlsHidden = true;
        mUiMediator.bottomControls.animate()
                .translationY(mUiMediator.bottomControls.getHeight() + 100)
                .setDuration(500)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .start();
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (isV3LayoutActive() && mPrefs.getBoolean("pref_auto_hide_controls", false)) {
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
        // K706: salir con BACK debe minimizar en vez de cerrar.
        // Si la Activity termina (finish), el launcher puede ejecutar forceStopPackage()
        // y se pierde el control del widget genérico de música.
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            try {
                moveTaskToBack(true);
                return true;
            } catch (Exception ignored) {}
        }
        if (mHardwareKeyCoordinator != null && mHardwareKeyCoordinator.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void toggleLayoutMode() {
        if (!mIsV3 && !mIsSimpleLayout) { // Estamos en V2 -> Ir a V3
            mPrefs.edit().putBoolean("pref_layout_v3", true).putBoolean("pref_layout_simple", false).apply();
            showToast(getString(R.string.toast_layout_v3));
        } else if (mIsV3) { // Estamos en V3 -> Ir a Simple
            mPrefs.edit().putBoolean("pref_layout_v3", false).putBoolean("pref_layout_simple", true).apply();
            showToast(getString(R.string.toast_layout_simple));
        } else { // Estamos en Simple -> Ir a V2
            mPrefs.edit().putBoolean("pref_layout_v3", false).putBoolean("pref_layout_simple", false).apply();
            showToast(getString(R.string.toast_layout_v2));
        }
        
        // V20.1: Pausa breve para que persistan prefs; antes de recreate invalidar Glide/petitions en curso.
        mMainHandler.postDelayed(() -> {
            prepareForLayoutModeRecreate();
            recreate();
        }, 400);
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
        Object current = iv.getTag(R.id.tag_color_filter);
        if (color == null) {
            if (current != null) {
                iv.clearColorFilter();
                iv.setTag(R.id.tag_color_filter, null);
            }
        } else {
            // Siempre aplicar si color != null: setImageDrawable/setImageResource puede haber
            // invalidado el filtro visual manteniendo el tag (packs PNG/SVG, PictureDrawable).
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
     * V20.0: Detecta autom├íticamente si la pantalla tiene una densidad alta (DPI)
     * y ajusta las gu├¡as y el tama├▒o de los botones para que no se deformen.
     */
    void adjustLayoutForDPI() {
        if (!isV3LayoutActive()) return; 
        
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density = metrics.density;
        float width = metrics.widthPixels;
        float height = metrics.heightPixels;
        float aspectRatio = width / height;
        
        android.util.Log.d(TAG, "DPI/Aspect Detection: density=" + density + " ratio=" + aspectRatio + " res=" + width + "x" + height);

        // V20.0: Optimizaci├│n para pantallas Cuadradas/Altas (5:4 o 4:3)
        // Estas pantallas suelen tener un ancho en DP menor (sw) que las 16:9
        boolean isTallScreen = aspectRatio < 1.45f;

        if (density > 1.0f || isTallScreen) {
            // 1. Subir la gu├¡a del texto (0.36 -> 0.32) para dar mucho m├ís espacio abajo
            // En pantallas altas (5:4), usamos un valor intermedio si no ha sido ya ajustado por el XML base
            android.view.View guidelineView = findViewById(R.id.guideline_v3_freq_bottom);
            if (guidelineView instanceof androidx.constraintlayout.widget.Guideline) {
                float targetPercent = isTallScreen ? 0.38f : 0.32f;
                ((androidx.constraintlayout.widget.Guideline) guidelineView).setGuidelinePercent(targetPercent);
            }
            
            // 2. Capar el tama├▒o de la fuente de la frecuencia
            if (tvFrequency instanceof androidx.appcompat.widget.AppCompatTextView) {
                int maxSp = isTallScreen ? 75 : 72; // Un poco m├ís en pantallas altas
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

    public int getCurrentBand() {
        if (mEngine != null) {
            return mEngine.getCurrentBand();
        }
        return mLastBand;
    }


    @Override
    public OnlineStreamManager getOnlineStreamManager() {
        return mOnlineStreamManager;
    }

    // --- RadioUiHost (Fase 0 refactor 5.2.0.MCU) ---

    @Override
    public Context getHostContext() {
        return this;
    }

    @Override
    public boolean isHostFinishing() {
        return isFinishing();
    }

    @Override
    public boolean isHostDestroyed() {
        return isDestroyed();
    }

    @Override
    public void runOnHostUiThread(Runnable action) {
        runOnUiThread(action);
    }

    @Override
    public boolean isHostChangingConfigurations() {
        return isChangingConfigurations();
    }

    @Override
    public void hostSendBroadcast(Intent intent) {
        sendBroadcast(intent);
    }

    @Override
    public void hostStartForegroundService(Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void hostStartService(Intent intent) {
        startService(intent);
    }

    @Override
    public IRadioServiceAPI getRadioService() {
        return mRadioService;
    }

    @Override
    public MainActivity.FmMode getFmMode() {
        return mMode;
    }

    @Override
    public RadioServiceController getServiceController() {
        return mServiceController;
    }

    @Override
    public PlaybackManager getPlaybackManager() {
        return mPlaybackManager;
    }

    @Override
    public RadioEngine getRadioEngine() {
        return mEngine;
    }

    @Override
    public ScanManager getScanManager() {
        return mScanManager;
    }

    @Override
    public void setUiScanningFlag(boolean value) {
        mIsScanning = value;
    }

    @Override
    public SharedPreferences getRadioPresets() {
        return mPrefs;
    }

    @Override
    public int getLastFreqKhz() {
        return mLastFreq;
    }

    @Override
    public int getUiCurrentBand() {
        return mCurrentBand;
    }

    @Override
    public void setShutdownPersistGuardUntilMs(long elapsedRealtimeMs) {
        mShutdownPersistGuardUntilMs = elapsedRealtimeMs;
    }

    @Override
    public long getShutdownPersistGuardUntilMs() {
        return mShutdownPersistGuardUntilMs;
    }

    @Override
    public StartupFreqPersistGuards getStartupFreqPersistGuards() {
        return mStartupFqGuards;
    }

    @Override
    public void beginRdsLogoTransitionAfterTune(String lastPsSnapshot) {
        mRdsLogoTransition.logoUiGeneration.incrementAndGet();
        mRdsLogoTransition.prevStationNameBeforeTune = lastPsSnapshot != null ? lastPsSnapshot : "";
        mRdsLogoTransition.rdsTransitionGuardUntilMs = android.os.SystemClock.elapsedRealtime() + RDS_TRANSITION_GUARD_MS;
    }

    @Override
    public void armCloudContribFreqSettleWindow() {
        mCloudContribAllowedAfterMs = android.os.SystemClock.elapsedRealtime() + CLOUD_CONTRIB_FREQ_SETTLE_MS;
    }

    @Override
    public void clearCachedLogoUrl() {
        mLastLogoUrl = "";
    }

    @Override
    public void addFreqToHistory(int freqKhz) {
        addToHistory(freqKhz);
    }

    @Override
    public void setPowerOffRequested(boolean value) {
        mPowerOffRequested = value;
    }

    @Override
    public boolean isPowerOffRequested() {
        return mPowerOffRequested;
    }

    @Override
    public Handler getMainHandler() {
        return mMainHandler;
    }

    @Override
    public Handler getAutoHideHandler() {
        return mAutoHideHandler;
    }

    @Override
    public Handler getClockHandler() {
        return mClockHandler;
    }

    @Override
    public ExecutorService getStationInfoExecutor() {
        return mStationInfoExecutor;
    }

    @Override
    public void setStationInfoExecutor(ExecutorService executor) {
        mStationInfoExecutor = executor;
    }

    @Override
    public MediaSessionManager getMediaSessionManager() {
        return mMediaSessionManager;
    }

    @Override
    public DeviceManager getDeviceManager() {
        return mDeviceManager;
    }

    @Override
    public HardwareManager getHardwareManager() {
        return mHardwareManager;
    }

    @Override
    public HiddenRadioPlayer getHiddenPlayer() {
        return mHiddenPlayer;
    }

    @Override
    public void setHiddenPlayer(HiddenRadioPlayer player) {
        mHiddenPlayer = player;
    }

    @Override
    public void setOnlineStreamManagerRef(OnlineStreamManager manager) {
        mOnlineStreamManager = manager;
    }

    @Override
    public PresetManager getPresetManager() {
        return mPresetManager;
    }

    @Override
    public LogoManager getLogoManager() {
        return mLogoManager;
    }

    @Override
    public RDSManager getRdsManager() {
        return mRdsManager;
    }

    @Override
    public BaseLayoutController getUiController() {
        return mUiController;
    }

    @Override
    public boolean isMuteState() {
        return mMuteState;
    }

    @Override
    public ThemeManager getThemeManager() {
        return mThemeManager;
    }

    @Override
    public boolean isRdsLockHeld() {
        return mRdsLockUiTick.hasLock;
    }

    @Override
    public android.view.View findHostViewById(int id) {
        return findViewById(id);
    }

    @Override
    public SignalMeterCoordinator getSignalMeterCoordinator() {
        return mSignalMeterCoordinator;
    }

    @Override
    public FrequencyStateManager getFreqStateManager() {
        return mFreqStateManager;
    }

    @Override
    public SkinCoordinator getSkinCoordinator() {
        return mSkinCoordinator;
    }

    @Override
    public NightModeManager getNightModeManager() {
        return mNightModeManager;
    }

    @Override
    public com.example.openradiofm.data.repository.RadioRepository getRadioRepository() {
        return mRepository;
    }

    @Override
    public RadioSessionController getRadioSessionController() {
        return mSessionController;
    }

    @Override
    public K706EngineeringDialog getK706EngineeringDialog() {
        return mEngineeringDialog;
    }

    @Override
    public QS6EngineeringDialog getQs6EngineeringDialog() {
        return mQs6EngineeringDialog;
    }

    @Override
    public java.util.Map<String, String> getLogoCachePerBand() {
        return mLogoCachePerBand;
    }

    @Override
    public boolean isUiScanning() {
        return mIsScanning;
    }

    @Override
    public int hostNextStationInfoSequence() {
        return mStationInfoSeq.incrementAndGet();
    }

    @Override
    public int getLastStationInfoRequestedSeq() {
        return mLastStationInfoRequestedSeq;
    }

    @Override
    public void setLastStationInfoRequestedSeq(int seq) {
        mLastStationInfoRequestedSeq = seq;
    }

    @Override
    public void setUiCurrentBand(int band) {
        mCurrentBand = band;
    }

    @Override
    public void setLastFreqKhz(int freqKhz) {
        mLastFreq = freqKhz;
    }

    @Override
    public String getLastPs() {
        return mLastPs;
    }

    @Override
    public void setLastPs(String ps) {
        mLastPs = ps != null ? ps : "";
    }

    @Override
    public String getCurrentPty() {
        return mCurrentPty;
    }

    @Override
    public void setCurrentPty(String pty) {
        mCurrentPty = pty;
    }

    @Override
    public void setHasRdsLock(boolean value) {
        mRdsLockUiTick.hasLock = value;
    }

    @Override
    public boolean getHadRdsLockForTick() {
        return mRdsLockUiTick.hadLockForTick;
    }

    @Override
    public void setHadRdsLockForTick(boolean value) {
        mRdsLockUiTick.hadLockForTick = value;
    }

    @Override
    public long getLastRdsLockTickUptimeMs() {
        return mRdsLockUiTick.lastTickUptimeMs;
    }

    @Override
    public void setLastRdsLockTickUptimeMs(long ms) {
        mRdsLockUiTick.lastTickUptimeMs = ms;
    }

    @Override
    public void incrementLogoUiGeneration() {
        mRdsLogoTransition.logoUiGeneration.incrementAndGet();
    }

    @Override
    public void persistLastBandPreference(int band) {
        if (mPrefs != null) {
            mPrefs.edit().putInt("pref_last_band", band).apply();
        }
    }

    @Override
    public int getLastStoredBand() {
        return mLastBand;
    }

    @Override
    public void setLastStoredBand(int band) {
        mLastBand = band;
    }

    @Override
    public String getCurrentPi() {
        return mCurrentPi;
    }

    @Override
    public void setCurrentPi(String pi) {
        mCurrentPi = pi;
    }

    // === V24.5: HARDWARE AUTOMATION HANDLERS (K706 EXCLUSIVE) ===

    @Override
    public void handleHwLightsAutomation(boolean lightsOn) {
        if (mPrefs == null || !mPrefs.getBoolean("pref_hw_auto_night", true)) return;
        
        if (mThemeManager != null) {
            com.example.openradiofm.ui.theme.ThemeManager.Skin targetSkin = lightsOn ? 
                com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE : null;
            
            if (targetSkin == null) {
                // Restaurar el anterior según pref
                int savedIdx = mPrefs.getInt("pref_skin_v2", 0);
                targetSkin = com.example.openradiofm.ui.theme.ThemeManager.Skin.values()[savedIdx];
            }
            
            if (mThemeManager.getActiveSkin() != targetSkin) {
                Log.d(TAG, "HW_AUTO: Syncing skin to " + targetSkin.displayName + " (Lights=" + lightsOn + ")");
                mThemeManager.setSkin(targetSkin);
                applySkin(targetSkin);
            }
        }
    }

    @Override
    public void handleHwReverseMute(boolean reverseOn) {
        if (mPrefs == null || !mPrefs.getBoolean("pref_hw_reverse_mute", true)) return;
        
        if (mPlaybackManager != null) {
            if (reverseOn) {
                Log.d(TAG, "HW_AUTO: Reverse gear detected, ducking volume...");
                mPlaybackManager.setReverseDucking(true);
            } else {
                Log.d(TAG, "HW_AUTO: Reverse gear off, restoring volume...");
                mPlaybackManager.setReverseDucking(false);
            }
        }
    }

    @Override
    public void handleHwHandbrakeSafety(boolean engaged) {
        if (mPrefs == null || !mPrefs.getBoolean("pref_hw_handbrake", true)) return;
        
        if (engaged) {
            Log.d(TAG, "HW_AUTO: Handbrake engaged (Safe)");
        } else {
            Log.d(TAG, "HW_AUTO: Handbrake disengaged (Drive Mode)");
        }
    }

    /** ACC (contacto) ON/OFF. Útil para política de auto-recovery/persistencia. */
    @Override
    public void handleHwAccState(boolean accOn) {
        try {
            if (mPrefs != null) {
                mPrefs.edit().putBoolean("pref_hw_acc_on", accOn).apply();
            }
        } catch (Exception ignored) {}
        Log.d(TAG, "HW_AUTO: ACC=" + (accOn ? "ON" : "OFF"));
    }
}


