package com.example.openradiofm.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import com.example.openradiofm.data.source.MT8163Engine;
import com.example.openradiofm.data.source.RadioEngine;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * V5.5: Gestor de Reproducción y Audio.
 * Centraliza la lógica de Mute/Unmute, la recuperación de audio del hardware
 * y la recepción de comandos externos desde Android Auto (MediaControlReceiver).
 *
 * Antes de V5.5, esta lógica residía directamente en MainActivity.
 */
public class PlaybackManager {
    private static final String TAG = "PlaybackManager";
    public static final String ACTION_MEDIA_CONTROL = "com.example.openradiofm.MEDIA_CONTROL";

    private final Context mContext;
    private final AudioManager mAudioManager;
    private RadioEngine mEngine;
    private boolean mMuteState = false;
    private boolean mIsMutedBySystem = false; // V4.8: Track if mute was automatic
    private boolean mMediaReceiverRegistered = false;
    private PlaybackListener mListener;
    /** Volúmenes guardados por tipo de stream al mutear MT8163 (STREAM_MUSIC + STREAM_FM oculto si existe). */
    private final HashMap<Integer, Integer> mMt8163SavedStreamVols = new HashMap<>();
    private Integer mSavedMusicVolumeForReverse = null;
    /** True si pedimos STREAM_MUSIC=0 y el volumen sigue siendo > 0 (política OEM / AudioService). */
    private boolean mMt8163StreamVolumeMuteRejected = false;
    /**
     * Tras {@code injectKey(VOLUME_MUTE)} el sistema a veces deja de reflejar el mute en STREAM_MUSIC;
     * {@link StatusRefreshCoordinator} no debe forzar {@code setMute(false)} en ese intervalo (un solo gesto usuario).
     */
    private static final long MT8163_POST_USER_INJECT_SYNC_GUARD_MS = 2800L;
    private long mMt8163LastUserInjectElapsedRealtime = 0L;

    /**
     * Interfaz para notificar a la UI de cambios en el estado de reproducción.
     */
    public interface PlaybackListener {
        void onMuteStateChanged(boolean isMuted);
        void onMediaCommand(String command);
    }

    public PlaybackManager(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Configura el motor de radio y el listener.
     */
    public void init(RadioEngine engine, PlaybackListener listener) {
        this.mEngine = engine;
        this.mListener = listener;
    }

    /**
     * Registra el BroadcastReceiver para los comandos de Android Auto.
     */
    public void registerMediaReceiver() {
        if (mMediaReceiverRegistered) return;
        try {
            mContext.registerReceiver(mMediaControlReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
            mMediaReceiverRegistered = true;
            Log.d(TAG, "MediaControlReceiver registrado");
        } catch (Exception e) {
            Log.e(TAG, "Error registrando MediaControlReceiver", e);
        }
    }

    /**
     * Desregistra el BroadcastReceiver de forma segura.
     */
    public void unregisterMediaReceiver() {
        try {
            if (!mMediaReceiverRegistered) return;
            mContext.unregisterReceiver(mMediaControlReceiver);
            mMediaReceiverRegistered = false;
            Log.d(TAG, "MediaControlReceiver desregistrado");
        } catch (Exception ignored) {}
    }

    /**
     * Controla el estado de Mute/Unmute de la radio.
     * - Envía el comando al motor de radio.
     * - Fuerza la recuperación de audio si se está desmuteando.
     * - Controla el stream de audio de Android.
     *
     * @param userOemMcuMuteKeyStep si true (gesto de usuario / sesión media), MT8163 puede inyectar {@code VOLUME_MUTE}
     *                              por McuManager (toggle hardware). No usar en recuperación automática ni al iniciar.
     */
    public void setMute(boolean mute, boolean userOemMcuMuteKeyStep) {
        mMuteState = mute;

        // V11: Via RadioEngine
        if (mEngine != null) {
            mEngine.setMute(mute);
            // V17.0/V20.1: Si estamos desmuteando, forzar recuperación agresiva del canal hardware
            // Lo hacemos SIEMPRE que se pida false, incluso si ya creíamos estar en false,
            // por si el hardware se ha muteado externamente (ej: tras un layout crash).
            if (!mute) {
                String engineName = null;
                try {
                    engineName = mEngine.getEngineName();
                } catch (Exception ignored) {}
                boolean isQs6 = engineName != null && engineName.toUpperCase().contains("QS6");
                // QS6 ya gestiona unmute con AudioFocus/HAL en su propio engine.
                // Evitamos recovery agresivo aquí para no provocar ping-pong de foco.
                // K706/MT8163: durante streaming online, enforceAudioRecovery() forzaría SetChannel(2)
                // y anularía el canal Android que usa ExoPlayer (síntoma: icono streaming pero audio FM).
                boolean streaming = false;
                try {
                    streaming = mEngine.isOnlineStreamingActive();
                } catch (Exception ignored) {}
                if (!isQs6 && !streaming) {
                    mEngine.enforceAudioRecovery();
                }
            }
        }

        // MT8163: STREAM_MUSIC suele ser la única vía audible desde la app cuando el HAL ignora fm_mute
        // y ExtAudioMuxer solo tiene JNI en system_server. Por defecto ON si la clave no existe;
        // el usuario puede desactivarlo en modo ingeniería si no quiere acoplar el volumen del sistema.
        try {
            boolean isMT8163 = mEngine != null && "MT8163".equals(mEngine.getEngineName());
            android.content.SharedPreferences prefs = mContext.getSharedPreferences(
                    "RadioPresets", Context.MODE_PRIVATE);
            boolean allowGlobal = prefs.getBoolean("pref_mt8163_global_stream_mute", true);
            // Sin HCN (MCU direct), muchos firmwares ignoran fm_mute/hcn_fm_mute en el HAL;
            // STREAM_MUSIC es a menudo el único mute audible desde la app.
            boolean mcuDirect = prefs.getBoolean("pref_mt8163_mcu_direct", false);
            boolean useStreamMute = allowGlobal || mcuDirect;

            if (isMT8163 && useStreamMute && mAudioManager != null) {
                if (mute) {
                    if (mcuDirect && !allowGlobal) {
                        Log.d(TAG, "MT8163 MCU-direct: bajando streams candidatos (HAL fm_mute / MUSIC a veces no afecta FM)");
                    }
                    int[] streams = collectMt8163MuteCandidateStreams();
                    for (int stream : streams) {
                        int smax = mAudioManager.getStreamMaxVolume(stream);
                        if (smax <= 0) continue;
                        if (!mMt8163SavedStreamVols.containsKey(stream)) {
                            int current = mAudioManager.getStreamVolume(stream);
                            mMt8163SavedStreamVols.put(stream, Math.max(current, 1));
                        }
                        mAudioManager.setStreamVolume(stream, 0, 0);
                    }
                    int volAfter = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mMt8163StreamVolumeMuteRejected = (volAfter > 0);
                    if (mMt8163StreamVolumeMuteRejected) {
                        Log.w(TAG, "STREAM_MUSIC no bajó a 0 (política OEM). Evitando sync que forzaría unmute.");
                    }
                } else {
                    mMt8163StreamVolumeMuteRejected = false;
                    for (Map.Entry<Integer, Integer> e : new HashMap<>(mMt8163SavedStreamVols).entrySet()) {
                        int stream = e.getKey();
                        int restore = e.getValue();
                        int max = mAudioManager.getStreamMaxVolume(stream);
                        restore = Math.min(Math.max(restore, 0), Math.max(max, 0));
                        mAudioManager.setStreamVolume(stream, restore, 0);
                    }
                    mMt8163SavedStreamVols.clear();
                }
            } else if (isMT8163) {
                mMt8163StreamVolumeMuteRejected = false;
            }
        } catch (Exception e) {
            Log.w(TAG, "Mute global STREAM_MUSIC falló", e);
        }

        if (userOemMcuMuteKeyStep && mEngine instanceof MT8163Engine) {
            ((MT8163Engine) mEngine).applyUserOemMuteThroughMcuKey();
            try {
                if (mContext.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                        .getBoolean("pref_mt8163_mcu_inject_mute_key", true)) {
                    mMt8163LastUserInjectElapsedRealtime = android.os.SystemClock.elapsedRealtime();
                }
            } catch (Exception ignored) {
            }
        }

        // Notificar a la UI
        if (mListener != null) {
            mListener.onMuteStateChanged(mute);
        }

        // V18.6: Iniciar el Foreground Service cuando la radio está activa
        // Importante: el servicio de medios debe poder controlar la radio sin bucles.
        // Si este PlaybackManager se está ejecutando DENTRO del propio servicio, no re-iniciamos nada.
        if (!mute && !(mContext instanceof com.example.openradiofm.service.RadioMediaService)) {
            Intent serviceIntent = new Intent(mContext, com.example.openradiofm.service.RadioMediaService.class);
            String engineName = null;
            try {
                if (mEngine != null) engineName = mEngine.getEngineName();
            } catch (Exception ignored) {}
            boolean isQs6 = engineName != null && engineName.toUpperCase().contains("QS6");
            // V21.5 (Csaba fix): En MTK8259, ACTION_FORCE_PLAY dispara enforceAudioRecovery()
            // en el RadioMediaService, que llama a OpenRadioCh() y reactiva el canal FM
            // mientras el stream de ExoPlayer sigue activo → mezcla de audio.
            // Igual que QS6, el MTK8259 gestiona su propia recuperación de audio internamente.
            boolean is8259 = engineName != null && engineName.toUpperCase().contains("8259");
            boolean skipForcePlay = false;
            try {
                if (mEngine != null) {
                    skipForcePlay = mEngine.shouldSkipMediaServiceForcePlayOnUnmute();
                }
            } catch (Exception ignored) {}
            if (!isQs6 && !is8259 && !skipForcePlay) {
                serviceIntent.setAction(com.example.openradiofm.service.RadioMediaService.ACTION_FORCE_PLAY);
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                mContext.startForegroundService(serviceIntent);
            } else {
                mContext.startService(serviceIntent);
            }
        }

        // V4.8: Gestión de AudioFocus delegada al Engine (K706RadioManager)
        // No solicitamos focus aquí para evitar que el MCU conmute al canal Android y silencie la radio FM.
        if (mute) {
            // Solo notificamos al engine
        } else {
            mIsMutedBySystem = false;
        }

        Log.d(TAG, "Mute state: " + (mute ? "MUTED" : "UNMUTED") + " (System: " + mIsMutedBySystem + ")");
    }

    /** Mute/unmute sin paso OEM McuManager (audio focus, arranque, sincronización). */
    public void setMute(boolean mute) {
        setMute(mute, false);
    }

    /**
     * Baja temporalmente el volumen durante marcha atrás (BACKCAR/REVERSE) y lo restaura al salir.
     *
     * Nota: en algunos firmwares OEM el audio FM no está ligado a STREAM_MUSIC; en esos casos esto
     * puede no tener efecto. Aun así es la opción menos invasiva frente a mutear el tuner.
     */
    public void setReverseDucking(boolean reverseOn) {
        if (mAudioManager == null) return;
        try {
            if (reverseOn) {
                if (mSavedMusicVolumeForReverse == null) {
                    int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mSavedMusicVolumeForReverse = Math.max(current, 0);
                }
                int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int target = Math.max(1, (int) Math.floor(max * 0.25));
                // Si ya estaba más bajo (usuario), no subimos nada: solo limitamos hacia abajo.
                int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (current > target) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);
                }
                Log.d(TAG, "Reverse ducking ON: target=" + target + " saved=" + mSavedMusicVolumeForReverse);
            } else {
                if (mSavedMusicVolumeForReverse != null) {
                    int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int restore = Math.min(Math.max(mSavedMusicVolumeForReverse, 0), Math.max(max, 0));
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, restore, 0);
                    Log.d(TAG, "Reverse ducking OFF: restored=" + restore);
                }
                mSavedMusicVolumeForReverse = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Reverse ducking falló", e);
            if (!reverseOn) mSavedMusicVolumeForReverse = null;
        }
    }

    /**
     * V4.8: Desmudea solo si el estado de mute fue provocado por el sistema.
     */
    public void resumeIfMutedBySystem() {
        if (mIsMutedBySystem) {
            Log.d(TAG, "resumeIfMutedBySystem: Recuperando audio automático");
            setMute(false);
        }
    }

    // V4.8: La gestión de AudioFocus se ha movido al RadioEngine (K706RadioManager)
    // para asegurar que el canal del MCU (SetChannel 2) se recupere correctamente.

    /**
     * V4.8: Listener para reaccionar cuando otra app pide el audio.
     */

    public boolean isMuted() {
        return mMuteState;
    }

    /**
     * Si el OEM bloquea {@code setStreamVolume(STREAM_MUSIC, 0)}, {@link StatusRefreshCoordinator}
     * no debe interpretar “sistema desmuteado” y llamar {@link #setMute(boolean)}(false).
     */
    public boolean isMt8163StreamVolumeMuteRejectedByOem() {
        return mMt8163StreamVolumeMuteRejected;
    }

    /**
     * Evita que la sincronización por volumen Android anule el mute por tecla OEM justo después del inject.
     */
    public boolean shouldSuppressMt8163StreamMuteSync() {
        if (mEngine == null || !"MT8163".equals(mEngine.getEngineName())) {
            return false;
        }
        if (mMt8163LastUserInjectElapsedRealtime == 0L) {
            return false;
        }
        return android.os.SystemClock.elapsedRealtime() - mMt8163LastUserInjectElapsedRealtime
                < MT8163_POST_USER_INJECT_SYNC_GUARD_MS;
    }

    public void setEngine(RadioEngine engine) {
        this.mEngine = engine;
    }

    public RadioEngine getEngine() {
        return mEngine;
    }

    /**
     * Streams a intentar silenciar en MT8163 cuando el FM no va por STREAM_MUSIC (reflexión STREAM_FM / valor 10 típico MTK).
     */
    private static int[] collectMt8163MuteCandidateStreams() {
        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        set.add(AudioManager.STREAM_MUSIC);
        addPublicStreamConstant(set, AudioManager.class, "STREAM_FM");
        addPublicStreamConstant(set, AudioManager.class, "STREAM_FM_RX");
        try {
            Class<?> audioSystem = Class.forName("android.media.AudioSystem");
            addPublicStreamConstant(set, audioSystem, "STREAM_FM");
        } catch (ClassNotFoundException ignored) {
        }
        if (!set.contains(10)) {
            set.add(10);
        }
        int[] out = new int[set.size()];
        int i = 0;
        for (int s : set) {
            out[i++] = s;
        }
        return out;
    }

    private static void addPublicStreamConstant(LinkedHashSet<Integer> set, Class<?> cls, String fieldName) {
        try {
            java.lang.reflect.Field f = cls.getField(fieldName);
            set.add(f.getInt(null));
        } catch (Throwable ignored) {
        }
    }

    /**
     * BroadcastReceiver para comandos de Android Auto y MediaSession.
     */
    private final BroadcastReceiver mMediaControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra("command");
            if (command == null) return;
            Log.d(TAG, "Media Control Received: " + command);

            switch (command) {
                case "ACTION_PLAY":
                    setMute(false, true); // Play es desmutear y recuperar canal
                    break;
                case "ACTION_PAUSE":
                    setMute(true, true); // Pause es silenciar la radio
                    break;
                case "ACTION_NEXT":
                case "ACTION_PREV":
                    // Delegamos a la UI a través del listener
                    if (mListener != null) {
                        mListener.onMediaCommand(command);
                    }
                    break;
            }
        }
    };
}
