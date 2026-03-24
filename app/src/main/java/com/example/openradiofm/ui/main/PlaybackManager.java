package com.example.openradiofm.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import com.example.openradiofm.data.source.RadioEngine;

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
    private Integer mSavedMusicVolume = null; // Para mute global fiable (MT8163)

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
     */
    public void setMute(boolean mute) {
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
                if (!isQs6) {
                    mEngine.enforceAudioRecovery();
                }
            }
        }

        // MT8163: Mute global opcional (compatibilidad).
        // En muchas ROMs OEM, ADJUST_MUTE/UNMUTE se ignora; por eso usamos setStreamVolume(0) + restauración.
        try {
            boolean isMT8163 = mEngine != null && "MT8163".equals(mEngine.getEngineName());
            boolean allowGlobal = mContext
                    .getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                    .getBoolean("pref_mt8163_global_stream_mute", false);

            if (isMT8163 && allowGlobal && mAudioManager != null) {
                if (mute) {
                    if (mSavedMusicVolume == null) {
                        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        mSavedMusicVolume = Math.max(current, 1);
                    }
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                } else {
                    int restore = (mSavedMusicVolume != null) ? mSavedMusicVolume : 1;
                    int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    restore = Math.min(Math.max(restore, 1), Math.max(max, 1));
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, restore, 0);
                    mSavedMusicVolume = null;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Mute global STREAM_MUSIC falló", e);
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
            // En QS6 ya estamos haciendo unmute desde el propio engine; forzar PLAY en el servicio
            // duplica setMute(false) y provoca peticiones redundantes de AudioFocus.
            if (!isQs6) {
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

    public void setEngine(RadioEngine engine) {
        this.mEngine = engine;
    }

    public RadioEngine getEngine() {
        return mEngine;
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
                    setMute(false); // Play es desmutear y recuperar canal
                    break;
                case "ACTION_PAUSE":
                    setMute(true); // Pause es silenciar la radio
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
