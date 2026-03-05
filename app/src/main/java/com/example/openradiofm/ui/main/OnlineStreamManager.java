package com.example.openradiofm.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * V18.0: Gestor de Streaming Online migrado a ExoPlayer (Media3).
 * ExoPlayer maneja nativamente el AudioFocus de Android de forma mucho más robusta,
 * lo cual soluciona los conflictos de "robar" el audio en las radios chinas (MCU).
 */
public class OnlineStreamManager {
    private static final String TAG = "OnlineStreamManager";

    private final Context mContext;
    private final PlaybackManager mPlaybackManager;
    private ExoPlayer mExoPlayer;
    private String mCurrentStreamUrl;
    private boolean mIsLoading = false;
    private boolean mIsPlaying = false;
    private StreamListener mListener;

    public interface StreamListener {
        void onStreamStatusChanged(boolean isLoading, boolean isPlaying);
        void onStreamError(String message);
    }

    public OnlineStreamManager(Context context, PlaybackManager playbackManager) {
        this.mContext = context;
        this.mPlaybackManager = playbackManager;
    }

    public void setListener(StreamListener listener) {
        this.mListener = listener;
    }

    public void toggleStream(String url) {
        if (mIsPlaying || mIsLoading) {
            stopStream();
        } else {
            startStream(url);
        }
    }

    public void startStream(String url) {
        if (url == null || url.isEmpty()) {
            if (mListener != null) mListener.onStreamError("URL de streaming no disponible");
            return;
        }

        mCurrentStreamUrl = url;
        stopStream(); // Limpiar instancias previas

        mIsLoading = true;
        updateUI();

        // 1. Conmutar canal MCU a Android (El hardware de la radio debe saber que Android va a sonar)
        if (mPlaybackManager != null) {
            if (mPlaybackManager.getEngine() != null) {
                mPlaybackManager.getEngine().setOnlineStreamingActive(true); // V18.4: Notificar modo streaming
                mPlaybackManager.getEngine().switchToAndroidAudio();
            }
        }

        // 2. Inicializar ExoPlayer
        mExoPlayer = new ExoPlayer.Builder(mContext).build();
        
        // Configurar Atributos de Audio de Media3
        // El parámetro 'true' indica que ExoPlayer manejará automáticamente el AudioFocus con el sistema.
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        mExoPlayer.setAudioAttributes(audioAttributes, true);

        // Configurar Listener de eventos
        mExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        mIsLoading = true;
                        mIsPlaying = false;
                        updateUI();
                        break;
                    case Player.STATE_READY:
                        mIsLoading = false;
                        if (mExoPlayer.getPlayWhenReady()) {
                            mIsPlaying = true;
                            Log.d(TAG, "ExoPlayer reproduciendo: " + url);
                        }
                        updateUI();
                        break;
                    case Player.STATE_ENDED:
                        stopStream();
                        break;
                    case Player.STATE_IDLE:
                        break;
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "ExoPlayer Error genérico", error);
                if (mListener != null) {
                    mListener.onStreamError("Error de reproducción: " + error.getMessage());
                }
                stopStream();
            }
        });

        try {
            MediaItem.Builder mediaItemBuilder = new MediaItem.Builder().setUri(url);
            
            // V18.4: Forzar detección de tipo si la URL es ambigua
            if (url.toLowerCase().contains("m3u8")) {
                mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8);
            } else if (url.toLowerCase().contains(".aac") || url.toLowerCase().contains("type=aac")) {
                mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.AUDIO_AAC);
            }
            
            mExoPlayer.setMediaItem(mediaItemBuilder.build());
            mExoPlayer.prepare();
            mExoPlayer.play(); // Arranca en cuanto esté listo
        } catch (Exception e) {
            mIsLoading = false;
            Log.e(TAG, "Error preparando ExoPlayer", e);
            if (mListener != null) mListener.onStreamError("Error crítico al iniciar stream");
            stopStream();
        }
    }

    public void stopStream() {
        mIsLoading = false;
        mIsPlaying = false;

        if (mExoPlayer != null) {
            try {
                mExoPlayer.stop();
                mExoPlayer.release(); // ExoPlayer libera automáticamente el AudioFocus al destruirse
            } catch (Exception e) {
                Log.e(TAG, "Error liberando ExoPlayer", e);
            }
            mExoPlayer = null;
        }

        // Recuperar audio de la radio física
        if (mPlaybackManager != null) {
            // Desmuteamos si acaso
            mPlaybackManager.setMute(false);
            if (mPlaybackManager.getEngine() != null) {
                mPlaybackManager.getEngine().setOnlineStreamingActive(false); // V18.4: Volver a modo radio
                mPlaybackManager.getEngine().switchToFmAudio();
            }
        }

        updateUI();
        Log.d(TAG, "Streaming detenido por completo.");
    }

    private void updateUI() {
        if (mListener != null) {
            mListener.onStreamStatusChanged(mIsLoading, mIsPlaying);
        }
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void release() {
        stopStream();
    }
}
