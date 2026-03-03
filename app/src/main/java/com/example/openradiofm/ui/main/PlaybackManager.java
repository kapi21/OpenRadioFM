package com.example.openradiofm.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private RadioEngine mEngine;
    private boolean mMuteState = false;
    private PlaybackListener mListener;

    /**
     * Interfaz para notificar a la UI de cambios en el estado de reproducción.
     */
    public interface PlaybackListener {
        void onMuteStateChanged(boolean isMuted);
        void onMediaCommand(String command);
    }

    public PlaybackManager(Context context) {
        this.mContext = context;
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
        try {
            mContext.registerReceiver(mMediaControlReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
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
            mContext.unregisterReceiver(mMediaControlReceiver);
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
            // V17.0: Si estamos desmuteando, forzar recuperación agresiva del canal hardware
            if (!mute) {
                mEngine.enforceAudioRecovery();
            }
        }

        // Controlar el stream de audio de Android
        android.media.AudioManager am = (android.media.AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        mute ? android.media.AudioManager.ADJUST_MUTE : android.media.AudioManager.ADJUST_UNMUTE, 0);
            } else {
                am.setStreamMute(android.media.AudioManager.STREAM_MUSIC, mute);
            }
        }

        // Notificar a la UI
        if (mListener != null) {
            mListener.onMuteStateChanged(mute);
        }

        Log.d(TAG, "Mute state: " + (mute ? "MUTED" : "UNMUTED"));
    }

    public boolean isMuted() {
        return mMuteState;
    }

    public void setEngine(RadioEngine engine) {
        this.mEngine = engine;
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
