package com.example.openradiofm.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Bitmap;
import android.view.KeyEvent;

import com.example.openradiofm.R;
import com.example.openradiofm.AppConstants;
import com.example.openradiofm.data.source.RadioEngine;
import com.example.openradiofm.data.source.K706RadioManager;
import com.example.openradiofm.ui.main.PlaybackManager;
import com.example.openradiofm.ui.main.RadioServiceController;
import com.example.openradiofm.ui.main.RadioSessionController;
import com.example.openradiofm.ui.main.RadioSessionState;
import com.example.openradiofm.widget.OpenRadioFmWidgetProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * V16: Servicio para compatibilidad con Android Auto y controles de medios.
 * Permite que el sistema y el coche interactúen con la radio como una app de medios estándar.
 */
public class RadioMediaService extends MediaBrowserServiceCompat {
    private static final String TAG = "RadioMediaService";
    /** Start command from UI to force PLAYING MediaSession state. */
    public static final String ACTION_FORCE_PLAY = "com.example.openradiofm.action.FORCE_PLAY";
    /**
     * QS6/K706: mantener MediaSession "activa" para mandos en segundo plano SIN reactivar audio.
     * En algunos OEM, si la sesión no está en PLAYING/FGS, NEXT/PREV/SEEK se enrutan a la radio nativa.
     */
    public static final String ACTION_FORCE_SESSION_ACTIVE = "com.example.openradiofm.action.FORCE_SESSION_ACTIVE";
    /** Widget escritorio: memorias (no sigue el ajuste del volante seek vs preset). */
    public static final String ACTION_WIDGET_PREV_PRESET = "com.example.openradiofm.action.WIDGET_PREV_PRESET";
    public static final String ACTION_WIDGET_NEXT_PRESET = "com.example.openradiofm.action.WIDGET_NEXT_PRESET";
    /** Widget escritorio: seek directo (sin presets). */
    public static final String ACTION_WIDGET_SEEK_DOWN = "com.example.openradiofm.action.WIDGET_SEEK_DOWN";
    public static final String ACTION_WIDGET_SEEK_UP = "com.example.openradiofm.action.WIDGET_SEEK_UP";
    /** Widget escritorio: mute/unmute (play/pause). */
    public static final String ACTION_WIDGET_TOGGLE_MUTE = "com.example.openradiofm.action.WIDGET_TOGGLE_MUTE";
    /**
     * MT8163: al pasar de streaming online a FM, SourceService puede force-stop si nuestra
     * sesión sigue como “reproductor activo” y el mux entrega audio a com.hcn.autoradio.
     * Pausamos la sesión antes de reconectar el servicio FM.
     */
    public static final String ACTION_MT8163_FM_HANDOFF = "com.example.openradiofm.action.MT8163_FM_HANDOFF";
    /**
     * Tras {@link #ACTION_MT8163_FM_HANDOFF}, liberar ExoPlayer y conmutar FM HAL; cuando el audio FM
     * está estable, volver a marcar la sesión como PLAYING (sin crear AudioTrack: solo metadata).
     */
    public static final String ACTION_MT8163_FM_HANDOFF_COMPLETE =
            "com.example.openradiofm.action.MT8163_FM_HANDOFF_COMPLETE";
    private static final String MEDIA_ROOT_ID = "radio_root";
    private static final String BANDS_ID = "bands";
    private static final String BAND_PREFIX = "band:"; // band:<idx>
    private static final String PRESET_PREFIX = "preset:"; // preset:<band>:<slot>:<freqKhz>
    private static final String RECENTS_ID = "recent";
    private static final String SUGGESTED_ID = "suggested";
    private static final String RECENT_PREFIX = "recent:"; // recent:<freqKhz>
    private static final String SUGGEST_PREFIX = "suggest:"; // suggest:<freqKhz>
    private static final String FAVORITES_ID = "favorites"; // Requerido por Zlink/Android Auto

    private static final String CHANNEL_ID = "radio_channel";
    private static final int NOTIFICATION_ID = 101;

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;

    // Reproducción (hardware) controlada desde el servicio para que Android Auto funcione sin UI
    private RadioServiceController mRadioServiceController;
    private RadioEngine mEngine;
    private PlaybackManager mPlaybackManager;
    private RadioSessionController mSessionController;
    private boolean mIsPlaying = false;

    // Preferencias y datos para nombres/presets (sin depender del repositorio/UI)
    private android.content.SharedPreferences mPresetPrefs; // "RadioPresets"
    private android.content.SharedPreferences mStationNamePrefs; // "RadioStationNames"

    /**
     * Evita doble seek/play cuando el OEM manda KEY_DOWN (lo gestiona super) y luego KEY_UP con el mismo {@link KeyEvent#getDownTime()}.
     */
    private long mLastHandledMediaKeyDownTime = Long.MIN_VALUE;

    // OEM cold start: cola mínima de comandos hasta que el engine esté listo
    private final Object mCommandLock = new Object();
    private boolean mPendingPlay = false;
    private boolean mPendingPause = false;
    private int mPendingTuneFreqKhz = -1;
    private int mPendingSkip = 0; // -1 prev, +1 next
    private boolean mPendingSkipPresetMode = false; // true=presets, false=seek
    private final AtomicBoolean mEngineInitStarted = new AtomicBoolean(false);
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    // Estado OEM para restaurar tras pérdidas de foco en K706
    private boolean mUserPaused = false;
    private boolean mWasPlayingBeforeFocusLoss = false;
    
    // V2.6: State guards for notification to avoid Binder flood
    private String mLastNotifiedTitle = "";
    private String mLastNotifiedArtist = "";
    private Bitmap mLastNotifiedLogo = null;

    /**
     * K706/QuickFish: el routing de teclas del widget depende del "last audio source" OEM,
     * que se actualiza al pedir AudioFocus (vía MediaFocusControl + requestCustomAudioFocus).
     * Pedimos foco desde el servicio para ganar propiedad incluso con launcher al frente.
     */
    private static final String K706_STEERING_FOCUS_TAG = "OpenRadioFM-K706-SteeringFocus";
    private AudioManager mK706SteeringAudioManager;
    private AudioManager.OnAudioFocusChangeListener mK706SteeringFocusListener;
    private AudioFocusRequest mK706SteeringFocusRequest;
    private boolean mK706SteeringFocusHeld = false;

    /**
     * QS6 (NWD): segundo {@link AudioManager#requestAudioFocus} desde este servicio en foreground.
     * Algunas unidades enlazan el bus de audio al cliente de medios activo, no solo al proceso de la Activity.
     */
    private static final String QS6_SVC_FOCUS_TAG = "OpenRadioFM-MediaSvc-Focus";
    private AudioManager mQs6ServiceAudioManager;
    private AudioManager.OnAudioFocusChangeListener mQs6ServiceFocusListener;
    private AudioFocusRequest mQs6ServiceFocusRequest;

    private boolean usePresetModeForSteering() {
        return mPresetPrefs != null && mPresetPrefs.getInt("pref_steering_next_prev_mode", 0) == 1;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

        // Preferencias utilizadas por la app (mismo naming que MainActivity/RadioRepository)
        mPresetPrefs = getSharedPreferences("RadioPresets", MODE_PRIVATE);
        mStationNamePrefs = getSharedPreferences("RadioStationNames", MODE_PRIVATE);
        writeOemStateToPrefs("BOOT");

        // K706 OEM: escuchar eventos reales de AudioFocus del SoC para sincronizar MediaSession
        try {
            IntentFilter f = new IntentFilter(K706RadioManager.ACTION_OEM_AUDIO_FOCUS);
            registerReceiver(mOemFocusReceiver, f);
        } catch (Exception e) {
            Log.w(TAG, "No se pudo registrar mOemFocusReceiver", e);
        }

        // Inicializar motor de radio (hardware) dentro del servicio para no depender de MainActivity
        try {
            mRadioServiceController = new RadioServiceController(this, mPresetPrefs, new RadioServiceController.ServiceListener() {
                @Override
                public void onModeDetected(com.example.openradiofm.ui.main.MainActivity.FmMode mode) {
                    // No-op: el servicio no necesita la UI
                }

                @Override
                public void onEngineReady(RadioEngine engine) {
                    mEngine = engine;
                    if (mPlaybackManager == null) {
                        mPlaybackManager = new PlaybackManager(RadioMediaService.this);
                        mPlaybackManager.init(mEngine, null);
                    } else {
                        mPlaybackManager.setEngine(mEngine);
                    }
                    // Inicializar controlador de sesión compartido (estado lógico de radio)
                    try {
                        mSessionController = com.example.openradiofm.ui.main.RadioServiceController
                                .getOrCreateSharedSessionController(
                                        RadioMediaService.this,
                                        mEngine,
                                        mPresetPrefs,
                                        mStationNamePrefs
                                );
                    } catch (Exception e) {
                        Log.w(TAG, "No se pudo inicializar RadioSessionController en el servicio", e);
                    }
                    try {
                        // El servicio también necesita callbacks para actualizar metadata/estado a Android Auto.
                        // Si MainActivity ya registró callback en el motor compartido (QS6/K706), combinar (no pisar la UI).
                        com.example.openradiofm.data.source.RadioEngineCallback uiCb = null;
                        if (mEngine instanceof com.example.openradiofm.data.source.QS6Engine) {
                            uiCb = ((com.example.openradiofm.data.source.QS6Engine) mEngine).getCallback();
                        } else if (mEngine instanceof com.example.openradiofm.data.source.K706Engine) {
                            uiCb = ((com.example.openradiofm.data.source.K706Engine) mEngine).getCallback();
                        }
                        if (uiCb != null) {
                            mEngine.setCallback(new com.example.openradiofm.data.source.CompositeRadioEngineCallback(
                                    uiCb, mEngineCallback));
                        } else {
                            mEngine.setCallback(mEngineCallback);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "No se pudo setear callback al engine desde el servicio", e);
                    }
                    Log.d(TAG, "Engine listo dentro del servicio: " + (engine != null ? engine.getEngineName() : "null"));

                    // Aplicar comandos pendientes (si llegaron antes de inicializar el engine)
                    flushPendingCommands();

                    if (engine instanceof com.example.openradiofm.data.source.QS6Engine) {
                        ensureQs6ServiceAudioFocus();
                    }
                }

                @Override
                public void onServiceConnected(com.hcn.autoradio.IRadioServiceAPI service) {
                    // No-op: el motor MT8163 se inicializa desde MainActivity vía callback onEngineReady
                }

                @Override
                public void onServiceDisconnected() {
                    // No-op
                }
            });
            // Iniciamos bajo demanda (botones/Auto). Evita trabajo innecesario si nadie controla medios.
            // El arranque en frío se dispara desde maybeStartEngine().
        } catch (Throwable t) {
            Log.e(TAG, "No se pudo inicializar RadioServiceController en el servicio", t);
        }

        // 1. Inicializar MediaSession
        mMediaSession = new MediaSessionCompat(this, TAG);

        // 1b. Enrutado explícito de MEDIA_BUTTON (Android 8+ / OEM): sin esto el sistema
        // puede no enviar volante/teclas cuando la app está en segundo plano.
        try {
            ComponentName mbr = new ComponentName(this, MediaButtonBootstrapReceiver.class);
            PendingIntent mbrPi = PendingIntent.getBroadcast(this, 0,
                    new Intent(Intent.ACTION_MEDIA_BUTTON).setComponent(mbr),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            mMediaSession.setMediaButtonReceiver(mbrPi);
        } catch (Exception e) {
            Log.w(TAG, "setMediaButtonReceiver falló", e);
        }

        // 2. Definir los flags: Soporta comandos de transporte y botones de medios
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        
        // V18.x: Muy importante: Activar la sesión para que el sistema le envíe comandos
        mMediaSession.setActive(true);

        // 3. Inicializar el estado de reproducción (Radio siempre "Playing" si está encendida)
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_FAST_FORWARD |
                        PlaybackStateCompat.ACTION_REWIND
                );
        setPlaybackState(false);

        // V18.6: Registrar cambios de estado para actualizar la notificación
        mMediaSession.getController().registerCallback(new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                updateNotification();
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                updateNotification();
            }
        });

        // 4. Establecer el Callback (será conectado a MainActivity / RadioEngine)
        // Por ahora se queda vacío, se gestiona vía MediaSessionManager
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                handlePlay();
            }

            @Override
            public void onPause() {
                handlePause();
            }

            @Override
            public void onSkipToNext() {
                handleSteeringSkip(+1);
            }

            @Override
            public void onSkipToPrevious() {
                handleSteeringSkip(-1);
            }

            @Override
            public void onFastForward() {
                // Algunas cabeceras OEM mapean NEXT del volante a FAST_FORWARD.
                handleSteeringSkip(+1);
            }

            @Override
            public void onRewind() {
                handleSteeringSkip(-1);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                KeyEvent ke = extractMediaKeyEvent(mediaButtonIntent);
                boolean superHandled = super.onMediaButtonEvent(mediaButtonIntent);
                if (superHandled) {
                    if (ke != null && ke.getAction() == KeyEvent.ACTION_DOWN) {
                        mLastHandledMediaKeyDownTime = ke.getDownTime();
                    }
                    return true;
                }
                if (ke == null) {
                    return false;
                }
                // Algunos launchers/widgets (p. ej. SYU/K706) solo entregan ACTION_UP.
                if (ke.getAction() == KeyEvent.ACTION_UP
                        && ke.getDownTime() != mLastHandledMediaKeyDownTime
                        && !ke.isCanceled()
                        && dispatchMediaKeyFromOemKeyEvent(ke)) {
                    mLastHandledMediaKeyDownTime = ke.getDownTime();
                    return true;
                }
                return false;
            }

            @Override
            public void onPlayFromMediaId(String mediaId, Bundle extras) {
                try {
                    int freq = parseFreqFromMediaId(mediaId);
                    if (freq > 0 && mEngine != null) {
                        handlePlay();
                        mEngine.tune(freq);
                    } else {
                        if (freq > 0) {
                            enqueueTune(freq);
                        } else {
                            enqueuePlay();
                        }
                        maybeStartEngine();
                        handlePlay(); // estado/notification inmediata
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error en onPlayFromMediaId(" + mediaId + ")", e);
                    handlePlay();
                }
            }

            @Override
            public void onCustomAction(String action, Bundle extras) {
                // Seguridad/consistencia: ignorar updates externos de metadata.
                // La fuente de verdad es el propio servicio vía callbacks del engine/prefs.
                if ("ACTION_UPDATE_METADATA".equals(action)) {
                    Log.d(TAG, "Ignorado ACTION_UPDATE_METADATA (source of truth = RadioMediaService)");
                }
            }
        });

        // 5. Vincular el token a la sesión
        setSessionToken(mMediaSession.getSessionToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // OEM safety (Android 8+): si nos arrancan con startForegroundService(),
        // debemos llamar a startForeground() rápidamente o Android mata el proceso.
        // Entramos en foreground con una notificación "pausada" y salimos si procede.
        try {
            startForeground(NOTIFICATION_ID, buildNotification(getSafeTitle(), getSafeArtist(), getSafeLogo()));
            // K706 / QS6: si la FM sigue “al aire” (!userPaused + playing), mantener FGS aunque onStartCommand
            // se dispare con intent vacío; si no, Android no enruta MEDIA_BUTTON al volante en segundo plano.
            boolean steeringKeepFg = mRadioServiceController != null
                    && mRadioServiceController.isSteeringWheelMediaBridgeMode()
                    && !mUserPaused && mIsPlaying;
            if (!mIsPlaying && !steeringKeepFg) {
                stopForeground(false); // mantener notificación visible
            }
        } catch (Exception e) {
            // Fallback: al menos publicar notificación
            ensureNotificationVisible();
        }

        // Motor antes que MEDIA_BUTTON: si el volante dispara skip en el mismo
        // onStartCommand, el engine ya debe existir o la cola cold-start debe poder vaciarse.
        // MT8163: NO — el bind a com.hcn.autoradio lo gestiona solo MainActivity; duplicar
        // bind aquí dispara mux/SourceService y puede force-stop la app.
        maybeStartEngine();

        // Procesar botones de medios (notificación, Android Auto, volante)
        try {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        } catch (Exception e) {
            Log.w(TAG, "MediaButtonReceiver.handleIntent falló", e);
        }

        // Arranque desde UI: asegurar sesión en PLAYING para que volante/media keys
        // se enruten a esta app también cuando MainActivity está en segundo plano.
        if (intent != null && ACTION_FORCE_PLAY.equals(intent.getAction())) {
            handlePlay();
        }
        // Bridge silencioso: solo activar sesión/FGS para capturar MEDIA_BUTTON; no tocar mute/audio route.
        if (intent != null && ACTION_FORCE_SESSION_ACTIVE.equals(intent.getAction())) {
            forceSessionActiveForSteering();
        }

        if (intent != null && ACTION_MT8163_FM_HANDOFF.equals(intent.getAction())) {
            handleMt8163FmHandoff();
        }

        if (intent != null && ACTION_MT8163_FM_HANDOFF_COMPLETE.equals(intent.getAction())) {
            handleMt8163FmHandoffComplete();
        }

        if (intent != null && ACTION_WIDGET_PREV_PRESET.equals(intent.getAction())) {
            handleWidgetPresetSkip(-1);
        }
        if (intent != null && ACTION_WIDGET_NEXT_PRESET.equals(intent.getAction())) {
            handleWidgetPresetSkip(1);
        }
        if (intent != null && ACTION_WIDGET_SEEK_DOWN.equals(intent.getAction())) {
            handleWidgetSeek(-1);
        }
        if (intent != null && ACTION_WIDGET_SEEK_UP.equals(intent.getAction())) {
            handleWidgetSeek(1);
        }
        if (intent != null && ACTION_WIDGET_TOGGLE_MUTE.equals(intent.getAction())) {
            handleWidgetToggleMute();
        }

        return START_NOT_STICKY;
    }

    /**
     * Activa la sesión en modo "captura" (PLAYING) para que el OEM enrute MEDIA_BUTTON a esta app,
     * pero sin desmutear ni forzar recuperación de audio.
     */
    private void forceSessionActiveForSteering() {
        try {
            // No cambiar mUserPaused: solo queremos routing de teclas.
            mIsPlaying = true;
            setPlaybackState(true);
            ensureNotificationVisible();
            startForeground(NOTIFICATION_ID, buildNotification(getSafeTitle(), getSafeArtist(), getSafeLogo()));
            // K706 OEM: asegurar que el sistema nos marca como "audio source" actual.
            ensureK706OemSteeringAudioFocus();
        } catch (Exception e) {
            Log.w(TAG, "forceSessionActiveForSteering falló", e);
        }
    }

    private void handleWidgetSeek(int direction) {
        try {
            Log.i(TAG, "handleWidgetSeek direction=" + direction + " engine=" + (mEngine != null));
            // K706 OEM widget: asegurar FGS + sesión activa antes del comando.
            // Evita timeouts de startForegroundService() y mejora routing como "last audio source".
            try { forceSessionActiveForSteering(); } catch (Exception ignored) {}
            maybeStartEngine();
            if (mEngine != null) {
                if (direction > 0) mEngine.seekUp();
                else mEngine.seekDown();
                // QS/OEM: el callback de frecuencia puede llegar tarde; refresco diferido por si la Activity no está viva.
                scheduleHomeWidgetRefreshFallback();
            } else {
                Log.w(TAG, "handleWidgetSeek: motor null tras maybeStartEngine (arranque en cola)");
            }
        } catch (Exception e) {
            Log.w(TAG, "handleWidgetSeek(" + direction + ") falló", e);
        }
    }

    private void handleWidgetToggleMute() {
        try {
            try { forceSessionActiveForSteering(); } catch (Exception ignored) {}
            // Reutilizar semántica de PLAY/PAUSE: PLAY=unmute, PAUSE=mute.
            if (mUserPaused) {
                handlePlay();
            } else {
                handlePause();
            }
        } catch (Exception e) {
            Log.w(TAG, "handleWidgetToggleMute falló", e);
        }
    }

    private void handleMt8163FmHandoff() {
        try {
            Log.i(TAG, "MT8163 FM handoff: sesión STOPPED (antes de soltar ExoPlayer/audio web)");
            mIsPlaying = false;
            applyOemStreamingHandoffPlaybackState();
            try {
                stopForeground(false);
            } catch (Exception ignored) {}
            ensureNotificationVisible();
        } catch (Exception e) {
            Log.w(TAG, "handleMt8163FmHandoff falló", e);
        }
    }

    /**
     * STATE_STOPPED + velocidad 0: menos “reproductor activo” que PAUSED en algunos OEM (SourceService).
     */
    private void applyOemStreamingHandoffPlaybackState() {
        if (mMediaSession == null || mStateBuilder == null) return;
        mStateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0L, 0f);
        mMediaSession.setPlaybackState(mStateBuilder.build());
    }

    private void handleMt8163FmHandoffComplete() {
        try {
            Log.d(TAG, "MT8163 FM handoff complete: restaurando sesión PLAYING (solo FM, sin AudioTrack de stream)");
            mIsPlaying = true;
            setPlaybackState(true);
            ensureNotificationVisible();
        } catch (Exception e) {
            Log.w(TAG, "handleMt8163FmHandoffComplete falló", e);
        }
    }

    /**
     * Widget propio: la UI solo actualiza desde {@link com.example.openradiofm.ui.main.MainActivity#sendWidgetUpdateIntent};
     * con la app en segundo plano o sin Activity, el servicio debe refrescar frecuencia/PS (p. ej. seek desde widget en QS6).
     */
    private void scheduleHomeWidgetRefreshFallback() {
        mMainHandler.removeCallbacks(mWidgetRefreshFallbackRunnable);
        mMainHandler.postDelayed(mWidgetRefreshFallbackRunnable, 450);
        mMainHandler.postDelayed(mWidgetRefreshFallbackRunnable, 1200);
    }

    private final Runnable mWidgetRefreshFallbackRunnable = this::updateHomeAppWidgetSync;

    private void updateHomeAppWidgetSync() {
        try {
            int freq = getLiveFreqKhzOrDefault(0);
            int band = getCurrentBandOrDefault(0);
            String ps = "";
            if (mSessionController != null) {
                RadioSessionState s = mSessionController.getCurrentState();
                if (s != null && s.rdsName != null && !s.rdsName.trim().isEmpty()) {
                    ps = s.rdsName.trim();
                }
            }
            if (ps.isEmpty() && freq > 0 && mStationNamePrefs != null) {
                String rds = mStationNamePrefs.getString("RDS_" + freq, null);
                if (rds != null && !rds.trim().isEmpty()) {
                    ps = rds.trim();
                } else {
                    String custom = mStationNamePrefs.getString("CUSTOM_" + freq, null);
                    if (custom != null && !custom.trim().isEmpty()) {
                        ps = custom.trim();
                    }
                }
            }
            OpenRadioFmWidgetProvider.updateStationDisplay(getApplicationContext(), freq, band, ps);
        } catch (Exception e) {
            Log.w(TAG, "updateHomeAppWidgetSync falló", e);
        }
    }

    private final com.example.openradiofm.data.source.RadioEngineCallback mEngineCallback =
            new com.example.openradiofm.data.source.RadioEngineCallback() {
                @Override
                public void onFrequencyChanged(int freqKhz) {
                    // Actualizar title/artist basado en datos guardados (CUSTOM/RDS) para Android Auto
                    updateMetadataFromPrefs(freqKhz);
                    saveRecentFrequency(freqKhz);
                    if (mSessionController != null) {
                        mSessionController.onFrequencyChanged(freqKhz);
                    }
                    updateHomeAppWidgetSync();
                }

                @Override
                public void onBandChanged(int band) {
                    if (mSessionController != null) {
                        mSessionController.onBandChanged(band);
                    }
                    updateHomeAppWidgetSync();
                }

                @Override
                public void onStereoChanged(boolean stereo) {
                    if (mSessionController != null) {
                        mSessionController.onStereoChanged(stereo);
                    }
                }

                @Override
                public void onRdsName(String name) {
                    // Se persistirá por RadioRepository en UI normalmente, aquí solo refrescamos
                    updateMetadataName(name);
                    if (mSessionController != null) {
                        mSessionController.onRdsName(name);
                    }
                    updateHomeAppWidgetSync();
                }

                @Override
                public void onRdsText(String text) {
                    persistRtForCurrentFreq(text);
                    updateMetadataFromPrefs(getLiveFreqKhzOrDefault(0));
                    if (mSessionController != null) {
                        mSessionController.onRdsText(text);
                    }
                }

                @Override
                public void onRdsPty(String pty) {
                    persistPtyForCurrentFreq(pty);
                    updateMetadataFromPrefs(getLiveFreqKhzOrDefault(0));
                    if (mSessionController != null) {
                        mSessionController.onRdsPty(pty);
                    }
                }

                @Override
                public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
                    if (mSessionController != null) {
                        mSessionController.onRdsStatus(afEnabled, taEnabled, tpEnabled);
                    }
                }
                @Override
                public void onRdsPi(String piCode) {
                    persistPiForCurrentFreq(piCode);
                    updateMetadataFromPrefs(getLiveFreqKhzOrDefault(0));
                    if (mSessionController != null) {
                        mSessionController.onRdsPi(piCode);
                    }
                }
                @Override
                public void onDxLocalChanged(boolean isLocal) {
                    if (mSessionController != null) {
                        mSessionController.onDxLocalChanged(isLocal);
                    }
                }

                @Override
                public void onScanStatusChanged(boolean scanning) {
                    if (mSessionController != null) {
                        mSessionController.onScanStatusChanged(scanning);
                    }
                }

                @Override
                public void onRawEvent(int code, String data) {
                    if (mSessionController != null) {
                        mSessionController.onRawEvent(code, data);
                    }
                }

                @Override
                public void onSignalUpdate(int rssi, int snr) {
                    if (mSessionController != null) {
                        mSessionController.onSignalUpdate(rssi, snr);
                    }
                }

                @Override
                public void onHwAutomationEvent(int type, boolean active) {
                    if (mSessionController == null) return;
                    if (type == 125) { // ACC
                        mSessionController.onAccChanged(active);
                    }
                }
            };

    private void handlePlay() {
        if (mEngine == null) {
            enqueuePlay();
            maybeStartEngine();
            mIsPlaying = true;
            setPlaybackState(true);
            ensureNotificationVisible();
            return;
        }
        mUserPaused = false;
        mIsPlaying = true;
        setPlaybackState(true);
        writeOemStateToPrefs("PLAY");

        // Control de hardware centralizado (si existe session controller, lo usa; si no, fallback legacy)
        try {
            if (mSessionController != null) {
                mSessionController.play();
            } else if (mPlaybackManager != null) {
                mPlaybackManager.setMute(false);
            } else if (mEngine != null) {
                mEngine.setMute(false);
                mEngine.enforceAudioRecovery();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar PLAY (unmute)", e);
        }

        // En PLAY sí somos foreground (media playback)
        startForeground(NOTIFICATION_ID, buildNotification(getSafeTitle(), getSafeArtist(), getSafeLogo()));

        if (mEngine instanceof com.example.openradiofm.data.source.QS6Engine) {
            ensureQs6ServiceAudioFocus();
        }
    }

    private void handlePause() {
        final boolean wasPlaying = mIsPlaying;
        if (mEngine == null) {
            enqueuePause();
            maybeStartEngine();
            mIsPlaying = false;
            setPlaybackState(false);
            ensureNotificationVisible();
            return;
        }
        mUserPaused = true;
        mIsPlaying = false;
        setPlaybackState(false);
        writeOemStateToPrefs("PAUSE");

        // OEM: Pausa = silenciar radio SOLO si realmente estaba sonando.
        // Algunos clientes (Android Auto/Zlink) pueden enviar PAUSE al conectar/sondear la sesión.
        if (wasPlaying) {
            try {
                if (mSessionController != null) {
                    mSessionController.pause();
                } else if (mPlaybackManager != null) {
                    mPlaybackManager.setMute(true);
                } else if (mEngine != null) {
                    mEngine.setMute(true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al ejecutar PAUSE (mute)", e);
            }
        }

        // Salimos del modo foreground, pero mantenemos la notificación visible (paused)
        try {
            stopForeground(false);
        } catch (Exception ignored) {}
        updateNotification();
        ensureNotificationVisible();

        if (wasPlaying && mEngine instanceof com.example.openradiofm.data.source.QS6Engine) {
            abandonQs6ServiceAudioFocus();
        }
    }

    private void ensureQs6ServiceAudioFocus() {
        try {
            if (mQs6ServiceAudioManager == null) {
                mQs6ServiceAudioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
                mQs6ServiceFocusListener = focusChange ->
                        Log.d(TAG, "QS6 MediaSvc onAudioFocusChange=" + focusChange);
            }
            if (mQs6ServiceAudioManager == null) return;

            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (mQs6ServiceFocusRequest == null) {
                    AudioAttributes aa = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build();
                    mQs6ServiceFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(aa)
                            .setWillPauseWhenDucked(false)
                            .setOnAudioFocusChangeListener(mQs6ServiceFocusListener, new Handler(Looper.getMainLooper()))
                            .build();
                }
                result = mQs6ServiceAudioManager.requestAudioFocus(mQs6ServiceFocusRequest);
            } else {
                result = mQs6ServiceAudioManager.requestAudioFocus(mQs6ServiceFocusListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
            Log.i(QS6_SVC_FOCUS_TAG, "requestAudioFocus result=" + result + " (RadioMediaService)");
        } catch (Exception e) {
            Log.w(TAG, "ensureQs6ServiceAudioFocus falló", e);
        }
    }

    private void abandonQs6ServiceAudioFocus() {
        try {
            if (mQs6ServiceAudioManager == null || mQs6ServiceFocusListener == null) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mQs6ServiceFocusRequest != null) {
                mQs6ServiceAudioManager.abandonAudioFocusRequest(mQs6ServiceFocusRequest);
            } else {
                mQs6ServiceAudioManager.abandonAudioFocus(mQs6ServiceFocusListener);
            }
            Log.i(QS6_SVC_FOCUS_TAG, "abandonAudioFocus (RadioMediaService)");
        } catch (Exception e) {
            Log.w(TAG, "abandonQs6ServiceAudioFocus falló", e);
        }
    }

    private void enqueuePlay() {
        synchronized (mCommandLock) {
            mPendingPlay = true;
            mPendingPause = false;
        }
    }

    private void enqueuePause() {
        synchronized (mCommandLock) {
            mPendingPause = true;
            mPendingPlay = false;
        }
    }

    private void enqueueTune(int freqKhz) {
        synchronized (mCommandLock) {
            mPendingTuneFreqKhz = freqKhz;
        }
    }

    private void enqueueSkip(int direction, boolean presetMode) {
        synchronized (mCommandLock) {
            mPendingSkip += direction;
            if (mPendingSkip > 3) mPendingSkip = 3;
            if (mPendingSkip < -3) mPendingSkip = -3;
            // Guardar el modo más reciente; si el usuario cambia el ajuste, la siguiente pulsación lo actualizará.
            mPendingSkipPresetMode = presetMode;
        }
    }

    private void maybeStartEngine() {
        try {
            if (mEngine != null) return;
            if (mRadioServiceController == null) return;
            if (mRadioServiceController.isFmMt8163Mode()) {
                Log.d(TAG, "maybeStartEngine: omitido (MT8163/HCN enlazado solo desde MainActivity)");
                return;
            }
            if (mEngineInitStarted.compareAndSet(false, true)) {
                Log.d(TAG, "Iniciando RadioServiceController.start() (OEM cold start)");
                mRadioServiceController.start();
            } else if (mEngine == null) {
                Log.d(TAG, "maybeStartEngine: reintento (motor aún null)");
                mRadioServiceController.start();
            }
        } catch (Exception e) {
            Log.w(TAG, "maybeStartEngine() falló", e);
        }
    }

    /**
     * Volante / notificación / Auto: siguiente/anterior según ajuste seek vs preset.
     * @param direction +1 siguiente, -1 anterior.
     */
    /** Widget: siempre memorias (preset), con fallback a seek si no hay siguiente/previa. */
    private void handleWidgetPresetSkip(int direction) {
        if (direction == 0) return;
        try {
            try { forceSessionActiveForSteering(); } catch (Exception ignored) {}
            if (mEngine != null) {
                boolean moved = direction > 0 ? playSequentialPreset(+1) : playSequentialPreset(-1);
                if (!moved) {
                    if (direction > 0) mEngine.seekUp();
                    else mEngine.seekDown();
                }
                handlePlay();
                scheduleHomeWidgetRefreshFallback();
            } else {
                enqueueSkip(direction > 0 ? +1 : -1, true);
                maybeStartEngine();
                mIsPlaying = true;
                setPlaybackState(true);
                ensureNotificationVisible();
                scheduleHomeWidgetRefreshFallback();
            }
        } catch (Exception e) {
            Log.w(TAG, "handleWidgetPresetSkip(" + direction + ") falló", e);
        }
    }

    private void handleSteeringSkip(int direction) {
        if (direction == 0) return;
        try {
            boolean presetMode = usePresetModeForSteering();
            if (mEngine != null) {
                if (presetMode) {
                    boolean moved = direction > 0 ? playSequentialPreset(+1) : playSequentialPreset(-1);
                    if (!moved) {
                        if (direction > 0) mEngine.seekUp();
                        else mEngine.seekDown();
                    }
                } else {
                    if (direction > 0) mEngine.seekUp();
                    else mEngine.seekDown();
                }
                handlePlay();
            } else {
                enqueueSkip(direction > 0 ? +1 : -1, presetMode);
                maybeStartEngine();
                mIsPlaying = true;
                setPlaybackState(true);
                ensureNotificationVisible();
            }
        } catch (Exception e) {
            Log.w(TAG, "handleSteeringSkip(" + direction + ") falló", e);
        }
    }

    private void flushPendingCommands() {
        if (mEngine == null) return;

        boolean doPlay;
        boolean doPause;
        int tune;
        int skip;
        boolean skipPresetMode;
        synchronized (mCommandLock) {
            doPlay = mPendingPlay;
            doPause = mPendingPause;
            tune = mPendingTuneFreqKhz;
            skip = mPendingSkip;
            skipPresetMode = mPendingSkipPresetMode;

            mPendingPlay = false;
            mPendingPause = false;
            mPendingTuneFreqKhz = -1;
            mPendingSkip = 0;
            mPendingSkipPresetMode = false;
        }

        try {
            if (tune > 0) {
                mEngine.tune(tune);
                updateMetadataFromPrefs(tune);
                doPlay = true;
                doPause = false;
            }

            if (skip != 0) {
                int times = Math.abs(skip);
                for (int i = 0; i < times; i++) {
                    if (skipPresetMode) {
                        boolean moved = playSequentialPreset(skip > 0 ? +1 : -1);
                        if (!moved) {
                            if (skip > 0) mEngine.seekUp();
                            else mEngine.seekDown();
                        }
                    } else {
                        if (skip > 0) mEngine.seekUp();
                        else mEngine.seekDown();
                    }
                }
                doPlay = true;
                doPause = false;
            }

            if (doPlay) handlePlay();
            else if (doPause) handlePause();
        } catch (Exception e) {
            Log.w(TAG, "flushPendingCommands() falló", e);
        }
    }

    private boolean isSteeringMediaBackgroundPlatform() {
        try {
            if (mEngine != null) {
                String n = mEngine.getEngineName();
                if (n != null) {
                    if ("K706".equals(n)) return true;
                    if (n.toUpperCase(Locale.US).contains("QS6")) return true;
                }
            }
        } catch (Exception ignored) {}
        return mRadioServiceController != null && mRadioServiceController.isSteeringWheelMediaBridgeMode();
    }

    /**
     * K706 / QS6: al ir a launcher el SoC puede emitir LOSS; si la sesión queda PAUSED, el sistema
     * envía KEYCODE_MEDIA al launcher / OEM en lugar de ACTION_MEDIA_BUTTON aquí.
     */
    private void refreshSteeringMediaSessionAndForeground() {
        if (!isSteeringMediaBackgroundPlatform() || mUserPaused || mMediaSession == null) return;
        try {
            mIsPlaying = true;
            setPlaybackState(true);
            startForeground(NOTIFICATION_ID, buildNotification(getSafeTitle(), getSafeArtist(), getSafeLogo()));
            ensureK706OemSteeringAudioFocus();
            Log.d(TAG, "Steering: sesión PLAYING+FGS mantenida para mandos en segundo plano (K706/QS6)");
        } catch (Exception e) {
            Log.w(TAG, "refreshSteeringMediaSessionAndForeground", e);
        }
    }

    private boolean isK706PlatformForOemSteering() {
        try {
            if (mEngine != null) {
                String n = mEngine.getEngineName();
                return "K706".equals(n);
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Pide AudioFocus (sin tocar mute) para que QuickFish actualice sys.qf.last_audio_src.
     * Esto hace que el puente OEM que hoy entrega los KEYCODE_MEDIA_* a la radio de fábrica
     * los rerutee hacia el "lastAudioSource" (nuestra app), como ocurre con NaviMods.
     */
    private void ensureK706OemSteeringAudioFocus() {
        if (!isK706PlatformForOemSteering()) return;
        // Si el motor ya está activo (caso normal cuando venimos desde UI),
        // NO pidamos un segundo AudioFocus con otro clientId: en algunas ROMs K706
        // eso genera AUDIOFOCUS_LOSS al request del engine y termina abandonándolo.
        if (mEngine != null) return;
        if (mK706SteeringFocusHeld) return;
        try {
            if (mK706SteeringAudioManager == null) {
                mK706SteeringAudioManager =
                        (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
                mK706SteeringFocusListener = focusChange ->
                        Log.d(K706_STEERING_FOCUS_TAG, "onAudioFocusChange=" + focusChange);
            }
            if (mK706SteeringAudioManager == null) return;

            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (mK706SteeringFocusRequest == null) {
                    AudioAttributes aa = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build();
                    mK706SteeringFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(aa)
                            .setWillPauseWhenDucked(false)
                            .setOnAudioFocusChangeListener(mK706SteeringFocusListener,
                                    new Handler(Looper.getMainLooper()))
                            .build();
                }
                result = mK706SteeringAudioManager.requestAudioFocus(mK706SteeringFocusRequest);
            } else {
                result = mK706SteeringAudioManager.requestAudioFocus(
                        mK706SteeringFocusListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                );
            }
            mK706SteeringFocusHeld = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
            Log.i(K706_STEERING_FOCUS_TAG, "requestAudioFocus result=" + result
                    + " held=" + mK706SteeringFocusHeld);
        } catch (Exception e) {
            Log.w(TAG, "ensureK706OemSteeringAudioFocus falló", e);
        }
    }

    private void abandonK706OemSteeringAudioFocus() {
        try {
            if (!mK706SteeringFocusHeld || mK706SteeringAudioManager == null) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mK706SteeringFocusRequest != null) {
                mK706SteeringAudioManager.abandonAudioFocusRequest(mK706SteeringFocusRequest);
            } else if (mK706SteeringFocusListener != null) {
                mK706SteeringAudioManager.abandonAudioFocus(mK706SteeringFocusListener);
            }
            mK706SteeringFocusHeld = false;
            Log.i(K706_STEERING_FOCUS_TAG, "abandonAudioFocus (K706 steering)");
        } catch (Exception e) {
            Log.w(TAG, "abandonK706OemSteeringAudioFocus falló", e);
        }
    }

    private final BroadcastReceiver mOemFocusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent == null) return;
            String event = intent.getStringExtra(K706RadioManager.EXTRA_FOCUS_EVENT);
            if (event == null) return;

            switch (event) {
                case K706RadioManager.EVENT_LOSS:
                case K706RadioManager.EVENT_LOSS_TRANSIENT:
                    if (isSteeringMediaBackgroundPlatform() && !mUserPaused) {
                        if (mIsPlaying) mWasPlayingBeforeFocusLoss = true;
                        writeOemStateToPrefs(event);
                        refreshSteeringMediaSessionAndForeground();
                        ensureNotificationVisible();
                        break;
                    }
                    // Si estaba sonando y el usuario NO había pausado, recordamos y reflejamos PAUSE
                    if (mIsPlaying && !mUserPaused) {
                        mWasPlayingBeforeFocusLoss = true;
                    }
                    // Reflejar estado pausado en el sistema (sin forzar mute extra; K706 ya gestiona canal/mute)
                    mIsPlaying = false;
                    setPlaybackState(false);
                    writeOemStateToPrefs(event);
                    ensureNotificationVisible();
                    break;

                case K706RadioManager.EVENT_GAIN:
                    // Restaurar solo si fue interrupción (no pausa del usuario)
                    if (mWasPlayingBeforeFocusLoss && !mUserPaused) {
                        mWasPlayingBeforeFocusLoss = false;
                        handlePlay();
                    } else {
                        mWasPlayingBeforeFocusLoss = false;
                        writeOemStateToPrefs(event);
                    }
                    break;
            }
        }
    };

    private void writeOemStateToPrefs(String lastEvent) {
        try {
            if (mPresetPrefs == null) return;
            mPresetPrefs.edit()
                    .putString("oem_last_focus_event", lastEvent != null ? lastEvent : "N/A")
                    .putBoolean("oem_user_paused", mUserPaused)
                    .putBoolean("oem_was_playing_before_focus_loss", mWasPlayingBeforeFocusLoss)
                    .putBoolean("oem_is_playing", mIsPlaying)
                    .apply();
        } catch (Exception ignored) {}
    }

    /**
     * Algunas ROM bloquean notificaciones (log "Not allowed show notification"); evitar spam de {@code notify}.
     * {@link #startForeground(int, Notification)} sigue llamándose donde la política del servicio lo exige.
     */
    private boolean mayPostNotifications() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
            }
            if (mNotificationManager != null) {
                return mNotificationManager.areNotificationsEnabled();
            }
        } catch (Exception ignored) {}
        return true;
    }

    private void ensureNotificationVisible() {
        if (!mayPostNotifications()) {
            return;
        }
        try {
            mNotificationManager.notify(NOTIFICATION_ID, buildNotification(getSafeTitle(), getSafeArtist(), getSafeLogo()));
        } catch (Exception e) {
            Log.w(TAG, "No se pudo publicar notificación", e);
        }
    }

    private void setPlaybackState(boolean playing) {
        int state = playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        mStateBuilder.setState(state, position, playing ? 1.0f : 0.0f);
        mMediaSession.setPlaybackState(mStateBuilder.build());
    }

    private void updateMetadataFromPrefs(int freqKhz) {
        try {
            // Preferimos el estado vivo de sesión si está disponible, y hacemos fallback a prefs/cache legacy.
            RadioSessionState live = null;
            try {
                if (mSessionController != null) {
                    live = mSessionController.getCurrentState();
                }
            } catch (Exception ignored) {}

            String name = null;
            if (mStationNamePrefs != null) {
                name = mStationNamePrefs.getString("CUSTOM_" + freqKhz, null);
                if (name == null || name.isEmpty()) name = mStationNamePrefs.getString("RDS_" + freqKhz, null);
                if (name == null || name.isEmpty()) name = "";
            }

            String title;
            if (live != null && live.rdsName != null && !live.rdsName.trim().isEmpty()) {
                title = live.rdsName.trim();
            } else if (name != null && !name.isEmpty() && !name.matches("\\d+")) {
                title = name;
            } else {
                int f = (live != null && live.freqKhz > 0) ? live.freqKhz : freqKhz;
                title = formatFrequency(f);
            }

            String pty = (mStationNamePrefs != null) ? mStationNamePrefs.getString("PTY_" + freqKhz, null) : null;
            String pi = (mStationNamePrefs != null) ? mStationNamePrefs.getString("PI_" + freqKhz, null) : null;
            String rt = (mStationNamePrefs != null) ? mStationNamePrefs.getString("RT_" + freqKhz, null) : null;

            // Preferir estado vivo si coincide con la frecuencia actual
            if (live != null && (live.freqKhz <= 0 || live.freqKhz == freqKhz)) {
                if ((rt == null || rt.isEmpty()) && live.rdsText != null && !live.rdsText.trim().isEmpty()) {
                    rt = live.rdsText.trim();
                }
                if ((pty == null || pty.isEmpty()) && live.pty != null && !live.pty.trim().isEmpty()) {
                    pty = live.pty.trim();
                }
                if ((pi == null || pi.isEmpty()) && live.pi != null && !live.pi.trim().isEmpty()) {
                    pi = live.pi.trim();
                }
            }

            // Fallback legacy eliminado: si el dato no está vivo, depender de prefs persistidas.

            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "OpenRadioFM")
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Radio FM");

            // OEM-like enrichment: RT como "álbum"/descripción, PTY como género, PI como id/extra
            if (rt != null && !rt.trim().isEmpty()) {
                builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, rt.trim());
                builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, rt.trim());
            }
            if (pty != null && !pty.trim().isEmpty()) {
                builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, pty.trim());
            }
            if (pi != null && !pi.trim().isEmpty()) {
                builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, pi.trim());
            }
            mMediaSession.setMetadata(builder.build());
        } catch (Exception e) {
            Log.w(TAG, "updateMetadataFromPrefs() falló", e);
        }
    }

    private void updateMetadataName(String rdsName) {
        try {
            if (rdsName == null || rdsName.trim().isEmpty()) return;
            MediaMetadataCompat current = mMediaSession.getController().getMetadata();
            MediaMetadataCompat.Builder builder = (current != null)
                    ? new MediaMetadataCompat.Builder(current)
                    : new MediaMetadataCompat.Builder();
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, rdsName.trim());
            builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, rdsName.trim());
            mMediaSession.setMetadata(builder.build());
        } catch (Exception e) {
            Log.w(TAG, "updateMetadataName() falló", e);
        }
    }

    private String formatFrequency(int freqKhz) {
        // FM típico: 87500 -> 87.5
        if (freqKhz <= 0) return "---";
        return String.format(java.util.Locale.US, "%.1f", freqKhz / 1000.0);
    }

    private int parseFreqFromMediaId(String mediaId) {
        if (mediaId == null) return -1;
        // preset:<band>:<slot>:<freqKhz>
        if (mediaId.startsWith(PRESET_PREFIX)) {
            String[] parts = mediaId.substring(PRESET_PREFIX.length()).split(":");
            if (parts.length >= 3) {
                try { return Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
            }
        }
        // recent:<freqKhz>
        if (mediaId.startsWith(RECENT_PREFIX)) {
            try { return Integer.parseInt(mediaId.substring(RECENT_PREFIX.length())); }
            catch (NumberFormatException ignored) {}
        }
        // suggest:<freqKhz>
        if (mediaId.startsWith(SUGGEST_PREFIX)) {
            try { return Integer.parseInt(mediaId.substring(SUGGEST_PREFIX.length())); }
            catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private void persistRtForCurrentFreq(String rt) {
        try {
            if (mStationNamePrefs == null) return;
            int f = getLiveFreqKhzOrDefault(-1);
            if (f <= 0) return;
            if (rt == null) return;
            String cleaned = rt.trim();
            if (cleaned.isEmpty()) return;
            mStationNamePrefs.edit().putString("RT_" + f, cleaned).apply();
        } catch (Exception ignored) {}
    }

    private void persistPtyForCurrentFreq(String pty) {
        try {
            if (mStationNamePrefs == null) return;
            int f = getLiveFreqKhzOrDefault(-1);
            if (f <= 0) return;
            if (pty == null) return;
            String cleaned = pty.trim();
            if (cleaned.isEmpty()) return;
            mStationNamePrefs.edit().putString("PTY_" + f, cleaned).apply();
        } catch (Exception ignored) {}
    }

    private void persistPiForCurrentFreq(String pi) {
        try {
            if (mStationNamePrefs == null) return;
            int f = getLiveFreqKhzOrDefault(-1);
            if (f <= 0) return;
            if (pi == null) return;
            String cleaned = pi.trim();
            if (cleaned.isEmpty()) return;
            mStationNamePrefs.edit().putString("PI_" + f, cleaned).apply();
        } catch (Exception ignored) {}
    }

    private int getLiveFreqKhzOrDefault(int defaultValue) {
        try {
            if (mSessionController != null) {
                RadioSessionState s = mSessionController.getCurrentState();
                if (s != null && s.freqKhz > 0) return s.freqKhz;
            }
        } catch (Exception ignored) {}
        try {
            if (mEngine != null) {
                int f = mEngine.getCurrentFreq();
                if (f > 0) return f;
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }

    private void saveRecentFrequency(int freqKhz) {
        try {
            if (mPresetPrefs == null) return;
            if (freqKhz <= 0) return;

            // Guardamos como CSV "freq1,freq2,...", más reciente primero
            String csv = mPresetPrefs.getString("recent_freqs", "");
            java.util.ArrayList<Integer> list = new java.util.ArrayList<>();
            if (csv != null && !csv.trim().isEmpty()) {
                String[] parts = csv.split(",");
                for (String p : parts) {
                    try {
                        int f = Integer.parseInt(p.trim());
                        if (f > 0 && f != freqKhz) list.add(f);
                    } catch (Exception ignored) {}
                }
            }
            list.add(0, freqKhz);
            while (list.size() > 12) list.remove(list.size() - 1);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(list.get(i));
            }
            mPresetPrefs.edit().putString("recent_freqs", sb.toString()).apply();
        } catch (Exception ignored) {}
    }

    /**
     * Navegación de presets consistente con MainActivity/PresetManager para mandos del volante
     * cuando la app está en segundo plano.
     *
     * @param direction +1 siguiente, -1 anterior.
     * @return true si se pudo sintonizar un preset válido.
     */
    private boolean playSequentialPreset(int direction) {
        try {
            if (mEngine == null || mPresetPrefs == null) return false;
            final int currentBand = getCurrentBandOrDefault(0);
            final int currentFreq = getLiveFreqKhzOrDefault(0);
            final int target = resolveSequentialPreset(currentBand, currentFreq, direction);
            if (target <= 0) return false;
            mEngine.tune(target);
            updateMetadataFromPrefs(target);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "playSequentialPreset() falló", e);
            return false;
        }
    }

    private int getCurrentBandOrDefault(int defaultBand) {
        try {
            if (mSessionController != null) {
                RadioSessionState s = mSessionController.getCurrentState();
                if (s != null && s.band >= 0) return s.band;
            }
        } catch (Exception ignored) {}
        try {
            if (mEngine != null) {
                int b = mEngine.getCurrentBand();
                if (b >= 0) return b;
            }
        } catch (Exception ignored) {}
        return defaultBand;
    }

    private int resolveSequentialPreset(int band, int currentFreq, int direction) {
        if (mPresetPrefs == null || direction == 0) return -1;
        final int count = AppConstants.PRESETS_COUNT;
        if (count <= 0) return -1;

        final int[] presets = new int[count];
        for (int i = 0; i < count; i++) {
            String key = "P" + (i + 1) + "_B" + band;
            presets[i] = mPresetPrefs.getInt(key, 0);
        }

        final int tolerance = 50; // 0.05 MHz
        int currentIndex = -1;
        for (int i = 0; i < count; i++) {
            if (presets[i] > 0 && Math.abs(presets[i] - currentFreq) <= tolerance) {
                currentIndex = i;
                break;
            }
        }

        if (direction > 0) {
            if (currentIndex == -1) {
                for (int s = 0; s < count; s++) {
                    if (presets[s] > 0) return presets[s];
                }
                return -1;
            }
            for (int step = 1; step < count; step++) {
                int nextIdx = (currentIndex + step) % count;
                if (presets[nextIdx] > 0) return presets[nextIdx];
            }
        } else {
            if (currentIndex == -1) {
                for (int s = count - 1; s >= 0; s--) {
                    if (presets[s] > 0) return presets[s];
                }
                return -1;
            }
            for (int step = 1; step < count; step++) {
                int prevIdx = (currentIndex - step + count) % count;
                if (presets[prevIdx] > 0) return presets[prevIdx];
            }
        }
        return -1;
    }

    private void updateNotification() {
        // Preferimos la misma fuente que usa foreground/ensureNotificationVisible:
        // RadioSessionState (si existe) -> metadata de MediaSession.
        String title = getSafeTitle();
        String artist = getSafeArtist();
        Bitmap logo = getSafeLogo();

        // V2.6: Master Guard for System IPC
        if (title != null && title.equals(mLastNotifiedTitle) && 
            artist != null && artist.equals(mLastNotifiedArtist) &&
            ((logo == null && mLastNotifiedLogo == null) || (logo != null && logo.sameAs(mLastNotifiedLogo)))) {
            return;
        }

        mLastNotifiedTitle = title;
        mLastNotifiedArtist = artist;
        mLastNotifiedLogo = logo;

        if (!mayPostNotifications()) {
            return;
        }
        mNotificationManager.notify(NOTIFICATION_ID, buildNotification(title, artist, logo));
    }

    private String getSafeTitle() {
        try {
            if (mSessionController != null) {
                RadioSessionState s = mSessionController.getCurrentState();
                if (s != null && s.rdsName != null && !s.rdsName.trim().isEmpty()) {
                    return s.rdsName.trim();
                }
                if (s != null && s.freqKhz > 0) {
                    // Fallback simple a frecuencia formateada si no hay nombre
                    return String.format(java.util.Locale.US, "%.1f", s.freqKhz / 1000.0);
                }
            }
        } catch (Exception ignored) {}
        MediaMetadataCompat metadata = mMediaSession.getController().getMetadata();
        if (metadata == null) return "OpenRadioFM";
        String t = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        return (t == null || t.isEmpty()) ? "OpenRadioFM" : t;
    }

    private String getSafeArtist() {
        try {
            if (mSessionController != null) {
                RadioSessionState s = mSessionController.getCurrentState();
                if (s != null && s.rdsText != null && !s.rdsText.trim().isEmpty()) {
                    // Mostramos RT como subtítulo (más útil que un "Radio FM" fijo)
                    return s.rdsText.trim();
                }
                if (s != null && s.pty != null && !s.pty.trim().isEmpty()) {
                    return s.pty.trim();
                }
            }
        } catch (Exception ignored) {}
        MediaMetadataCompat metadata = mMediaSession.getController().getMetadata();
        if (metadata == null) return "Radio FM";
        String a = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        return (a == null || a.isEmpty()) ? "Radio FM" : a;
    }

    private Bitmap getSafeLogo() {
        MediaMetadataCompat metadata = mMediaSession.getController().getMetadata();
        if (metadata == null) return null;
        return metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "OpenRadioFM Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Controles de reproducción de la radio");
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String title, String subtitle, Bitmap logo) {
        try {
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, com.example.openradiofm.ui.main.MainActivity.class),
                    PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(subtitle)
                    .setLargeIcon(logo)
                    .setContentIntent(contentIntent)
                    .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                    .setOnlyAlertOnce(true)
                    .setOngoing(mIsPlaying)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(new MediaStyle()
                            .setMediaSession(mMediaSession.getSessionToken())
                            .setShowActionsInCompactView(0, 1, 2)); // Corregido: Prev (0), Play/Pause (1), Next (2)

            // Acción 0: Anterior
            builder.addAction(new NotificationCompat.Action(R.drawable.radio_seekdown_n, "Anterior",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
            
            // Acción 1: Play/Pause (con validación de nulo)
            PlaybackStateCompat state = mMediaSession.getController().getPlaybackState();
            boolean isPlaying = (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING);
            
            if (isPlaying) {
                builder.addAction(new NotificationCompat.Action(R.drawable.radio_mute_n, "Pausar",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)));
            } else {
                builder.addAction(new NotificationCompat.Action(R.drawable.radio_mute_p, "Reproducir",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)));
            }

            // Acción 2: Siguiente
            builder.addAction(new NotificationCompat.Action(R.drawable.radio_seekup_n, "Siguiente",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));

            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "Error building notification", e);
            // Fallback básico para evitar que el servicio muera
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .build();
        }
    }

    private void sendBroadcastToActivity(String action) {
        Intent intent = new Intent("com.example.openradiofm.MEDIA_CONTROL");
        intent.putExtra("command", action);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // OEM hardening: permitimos Android Auto y sistema. Evitamos exponer el catálogo
        // a apps arbitrarias (reduce abuso de intents/escucha de presets).
        if (!isTrustedMediaClient(clientPackageName)) {
            Log.w(TAG, "Cliente MediaBrowser no confiable rechazado: " + clientPackageName);
            return null;
        }
        Bundle extras = new Bundle();
        extras.putBoolean(BrowserRoot.EXTRA_OFFLINE, true);
        extras.putBoolean(BrowserRoot.EXTRA_RECENT, true);
        extras.putBoolean(BrowserRoot.EXTRA_SUGGESTED, true);
        return new BrowserRoot(MEDIA_ROOT_ID, extras);
    }

    private boolean isTrustedMediaClient(String pkg) {
        if (pkg == null) return false;
        // Android Auto y componentes del sistema suelen estar en estas familias.
        if (pkg.startsWith("com.google.android.projection")) return true; // Android Auto (projection)
        if (pkg.contains("android.auto") || pkg.contains("projection.gearhead")) return true; // variantes
        if (pkg.startsWith("com.google.android.gms")) return true; // Servicios Google (Auto)
        if (pkg.startsWith("com.google.android.")) return true; // otros componentes de Auto/Car en OEMs
        if (pkg.startsWith("com.android.")) return true; // sistema / launcher coche (p. ej. autohome)
        if (pkg.startsWith("android")) return true;

        // K706 / Topway / QuickFish: stack SYU y broadcasts QF no usan prefijo com.android.*
        if (pkg.startsWith("com.syu.")) return true;
        if (pkg.startsWith("com.qf.")) return true;
        
        // V21.1: Incluir Zlink (cliente común en hardware OEM para Android Auto por cable/inalámbrico)
        if (pkg.equals("com.zjinnova.zlink")) return true;
        if (pkg.contains("zlink")) return true;
        if (pkg.contains("carlink") || pkg.contains("tlink") || pkg.contains("easyconn")) return true;
        
        // Permitir también la propia app
        if (pkg.equals(getPackageName())) return true;

        // Resto de launchers / IVI preinstalados (paquetes de sistema)
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(pkg, 0);
            int f = ai.flags;
            if ((f & ApplicationInfo.FLAG_SYSTEM) != 0) return true;
            if ((f & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) return true;
        } catch (PackageManager.NameNotFoundException ignored) {}
        return false;
    }

    @Nullable
    private static KeyEvent extractMediaKeyEvent(@Nullable Intent intent) {
        if (intent == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent.class);
        }
        return intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
    }

    private boolean dispatchMediaKeyFromOemKeyEvent(KeyEvent ke) {
        switch (ke.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                handleSteeringSkip(+1);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                handleSteeringSkip(-1);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                handlePlay();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                handlePause();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (mIsPlaying) {
                    handlePause();
                } else {
                    handlePlay();
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        if (MEDIA_ROOT_ID.equals(parentId)) {
            List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
            items.add(makeBrowsable(FAVORITES_ID, "Favoritos", "Mis emisoras"));
            items.add(makeBrowsable(RECENTS_ID, "Recientes", "Últimas emisoras"));
            items.add(makeBrowsable(SUGGESTED_ID, "Sugeridos", "Recomendados"));
            // Bandas como raíz (OEM-like)
            items.add(makeBrowsable("band:0", "FM1", "Presets"));
            items.add(makeBrowsable("band:1", "FM2", "Presets"));
            items.add(makeBrowsable("band:2", "FM3", "Presets"));
            items.add(makeBrowsable("band:3", "AM1", "Presets"));
            items.add(makeBrowsable("band:4", "AM2", "Presets"));
            result.sendResult(items);
        } else if (RECENTS_ID.equals(parentId)) {
            result.sendResult(loadRecents());
        } else if (SUGGESTED_ID.equals(parentId)) {
            result.sendResult(loadSuggested());
        } else if (FAVORITES_ID.equals(parentId) || "Favorites".equals(parentId)) {
            result.sendResult(loadAllFavorites());
        } else if (parentId.startsWith(BAND_PREFIX)) {
            int band = parseBand(parentId);
            result.sendResult(loadPresetsForBand(band));
        } else {
            result.sendResult(new ArrayList<>());
        }
    }

    private List<MediaBrowserCompat.MediaItem> loadAllFavorites() {
        List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
        if (mPresetPrefs == null) return items;

        // Recorrer todas las bandas y slots para buscar frecuencias guardadas
        // Asume BAND_FM1 a BAND_AM2 (0 a 4) y 6 slots por banda
        for (int b = 0; b < 5; b++) {
            for (int s = 0; s < 6; s++) {
                String key = "P" + (s + 1) + "_B" + b;
                int f = mPresetPrefs.getInt(key, 0);
                if (f > 0) {
                    String name = getStoredName(f);
                    String vId = PRESET_PREFIX + b + ":" + s + ":" + f;
                    
                    // Solo añadir si no hemos añadido esta frecuencia exacta ya para evitar duplicados en la lista global de favoritos
                    boolean exists = false;
                    for (MediaBrowserCompat.MediaItem item : items) {
                        if (item.getDescription().getMediaId() != null && item.getDescription().getMediaId().endsWith(":" + f)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        Bundle extras = new Bundle();
                        extras.putInt("freqKhz", f);
                        extras.putInt("band", b);
                        items.add(new MediaBrowserCompat.MediaItem(
                                new MediaDescriptionCompat.Builder()
                                        .setMediaId(vId)
                                        .setTitle(name != null && !name.isEmpty() ? name : String.format(Locale.getDefault(), "%.1f MHz", f / 1000.0f))
                                        .setSubtitle("Favorito")
                                        .setExtras(extras)
                                        .build(),
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                        ));
                    }
                }
            }
        }
        return items;
    }

    private List<MediaBrowserCompat.MediaItem> loadRecents() {
        List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
        if (mPresetPrefs == null) return items;
        String csv = mPresetPrefs.getString("recent_freqs", "");
        if (csv == null || csv.trim().isEmpty()) return items;

        String[] parts = csv.split(",");
        for (String p : parts) {
            try {
                int f = Integer.parseInt(p.trim());
                if (f <= 0) continue;
                String name = getStoredName(f);
                String title = (name != null && !name.isEmpty() && !name.matches("\\d+")) ? name : formatFrequency(f);
                items.add(new MediaBrowserCompat.MediaItem(
                        new MediaDescriptionCompat.Builder()
                                .setMediaId(RECENT_PREFIX + f)
                                .setTitle(title)
                                .setSubtitle("Reciente")
                                .build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                ));
            } catch (Exception ignored) {}
        }
        return items;
    }

    private List<MediaBrowserCompat.MediaItem> loadSuggested() {
        // Estrategia simple OEM-like:
        // - Si hay recents, sugerimos los 3 primeros.
        // - Si no, sugerimos presets FM1.
        List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
        List<MediaBrowserCompat.MediaItem> rec = loadRecents();
        for (int i = 0; i < rec.size() && i < 3; i++) {
            MediaDescriptionCompat d = rec.get(i).getDescription();
            String id = d != null ? d.getMediaId() : null;
            int f = parseFreqFromMediaId(id);
            if (f > 0) {
                items.add(new MediaBrowserCompat.MediaItem(
                        new MediaDescriptionCompat.Builder()
                                .setMediaId(SUGGEST_PREFIX + f)
                                .setTitle(d.getTitle())
                                .setSubtitle("Sugerido")
                                .build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                ));
            }
        }

        if (!items.isEmpty()) return items;

        // Fallback: FM1 (band 0)
        List<MediaBrowserCompat.MediaItem> fm1 = loadPresetsForBand(0);
        for (int i = 0; i < fm1.size() && i < 6; i++) {
            MediaDescriptionCompat d = fm1.get(i).getDescription();
            String id = d != null ? d.getMediaId() : null;
            int f = parseFreqFromMediaId(id);
            if (f > 0) {
                items.add(new MediaBrowserCompat.MediaItem(
                        new MediaDescriptionCompat.Builder()
                                .setMediaId(SUGGEST_PREFIX + f)
                                .setTitle(d.getTitle())
                                .setSubtitle("Sugerido")
                                .build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                ));
            }
        }
        return items;
    }

    private int parseBand(String parentId) {
        try {
            String idx = parentId.substring(BAND_PREFIX.length());
            return Integer.parseInt(idx);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private MediaBrowserCompat.MediaItem makeBrowsable(String mediaId, String title, String subtitle) {
        return new MediaBrowserCompat.MediaItem(
                new MediaDescriptionCompat.Builder()
                        .setMediaId(mediaId)
                        .setTitle(title)
                        .setSubtitle(subtitle)
                        .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        );
    }

    private List<MediaBrowserCompat.MediaItem> loadPresetsForBand(int band) {
        List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
        if (mPresetPrefs == null) return items;

        final int presetsCount = AppConstants.PRESETS_COUNT;
        for (int slot = 0; slot < presetsCount; slot++) {
            String key = "P" + (slot + 1) + "_B" + band;
            int freq = mPresetPrefs.getInt(key, 0);
            if (freq <= 0) continue;

            String displayName = getStoredName(freq);
            String title = (displayName != null && !displayName.isEmpty() && !displayName.matches("\\d+"))
                    ? displayName
                    : formatFrequency(freq);

            String mediaId = PRESET_PREFIX + band + ":" + slot + ":" + freq;

            Bundle extras = new Bundle();
            extras.putInt("freqKhz", freq);
            extras.putInt("band", band);
            extras.putInt("slot", slot);

            items.add(new MediaBrowserCompat.MediaItem(
                    new MediaDescriptionCompat.Builder()
                            .setMediaId(mediaId)
                            .setTitle(title)
                            .setSubtitle("Preset P" + (slot + 1))
                            .setExtras(extras)
                            .build(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            ));
        }
        return items;
    }

    private String getStoredName(int freqKhz) {
        if (mStationNamePrefs == null) return "";
        String custom = mStationNamePrefs.getString("CUSTOM_" + freqKhz, null);
        if (custom != null && !custom.isEmpty()) return custom;
        String rds = mStationNamePrefs.getString("RDS_" + freqKhz, null);
        if (rds != null && !rds.isEmpty()) return rds;
        return "";
    }

    @Override
    public void onDestroy() {
        abandonK706OemSteeringAudioFocus();
        abandonQs6ServiceAudioFocus();
        try {
            stopForeground(true);
        } catch (Exception ignored) {}

        try {
            if (mRadioServiceController != null) {
                mRadioServiceController.release();
                mRadioServiceController = null;
            }
        } catch (Exception ignored) {}

        try {
            if (mPlaybackManager != null) {
                // Liberar referencia (no mata hardware por sí mismo)
                mPlaybackManager = null;
            }
        } catch (Exception ignored) {}

        try {
            if (mEngine != null) {
                mEngine.release();
                mEngine = null;
            }
        } catch (Exception ignored) {}

        try {
            unregisterReceiver(mOemFocusReceiver);
        } catch (Exception ignored) {}

        try {
            if (mMediaSession != null) {
                mMediaSession.setActive(false);
                mMediaSession.release();
                mMediaSession = null;
            }
        } catch (Exception ignored) {}
        super.onDestroy();
    }
}
