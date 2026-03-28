package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.openradiofm.R;
import com.example.openradiofm.service.RadioMediaService;

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
    /**
     * Deja que {@link RadioMediaService} ejecute el handoff de MediaSession antes de
     * {@link ExoPlayer#release()} (sesión de audio 6xx del stream). Evita carrera con SourceService.
     */
    private static final int MT8163_EXO_RELEASE_AFTER_HANDOFF_MS = 120;

    private final Context mContext;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final PlaybackManager mPlaybackManager;
    private ExoPlayer mExoPlayer;
    private String mCurrentStreamUrl;
    private boolean mIsLoading = false;
    private boolean mIsPlaying = false;
    private StreamListener mListener;

    public interface StreamListener {
        void onStreamStatusChanged(boolean isLoading, boolean isPlaying);
        void onStreamError(String message);

        /** Antes de iniciar un stream (p. ej. cancelar reconexión HCN diferida en MT8163). */
        default void onBeforeStreamStart() {}

        /** Tras parar streaming en MT8163: posponer bind a FMPlugService (evita force-stop OEM). */
        default void onStreamStoppedMt8163() {}
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

        // V18.6.5: Si la URL es una playlist M3U/PLS, resolver en segundo plano
        // antes de iniciar ExoPlayer. Los ficheros .m3u son simples listas de texto
        // con la URL real del stream, que ExoPlayer no sabe interpretar directamente.
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".m3u") || lowerUrl.endsWith(".pls")) {
            Log.d(TAG, "Detectada playlist (" + url + "), resolviendo URL real...");
            mIsLoading = true;
            updateUI();
            new Thread(() -> {
                String resolved = resolvePlaylistUrl(url);
                if (resolved != null && !resolved.isEmpty()) {
                    Log.d(TAG, "Playlist resuelta: " + resolved);
                    // Volver al hilo principal para iniciar ExoPlayer  
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> startStreamDirect(resolved));
                } else {
                    Log.e(TAG, "No se pudo resolver la playlist: " + url);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        mIsLoading = false;
                        updateUI();
                        if (mListener != null) mListener.onStreamError("No se pudo leer la playlist M3U");
                    });
                }
            }).start();
            return;
        }

        startStreamDirect(url);
    }

    /**
     * V18.6.5: Resuelve una playlist M3U/PLS descargando su contenido y extrayendo
     * la primera URL de stream válida. Debe llamarse desde un hilo de fondo.
     */
    private String resolvePlaylistUrl(String playlistUrl) {
        try {
            java.net.URL u = new java.net.URL(playlistUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(true);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // M3U: líneas que empiezan por http son URLs de stream
                if (line.startsWith("http://") || line.startsWith("https://")) {
                    reader.close();
                    conn.disconnect();
                    return line;
                }
                // PLS: File1=http://...
                if (line.startsWith("File") && line.contains("=")) {
                    String val = line.substring(line.indexOf("=") + 1).trim();
                    if (val.startsWith("http://") || val.startsWith("https://")) {
                        reader.close();
                        conn.disconnect();
                        return val;
                    }
                }
            }
            reader.close();
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error resolviendo playlist: " + playlistUrl, e);
        }
        return null;
    }

    private void startStreamDirect(String url) {
        // ExoPlayer debe crearse y usarse en el hilo principal; si no, Parcel/codec pueden fallar
        // ("Expecting binder but got null") en algunos firmwares.
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> startStreamDirect(url));
            return;
        }

        // V18.6.5: Normalización de URL para maximizar compatibilidad en radios chinas.
        // Muchos servidores Icecast en puertos no estándar (ej: 8222) tienen certificados SSL
        // que fallan en hardware antiguo o Android < 9. Forzamos HTTP si detectamos estos puertos.
        String normalizedUrl;
        if (url.startsWith("https://") && (url.contains(":8") || url.contains(":9"))) {
            Log.w(TAG, "Forzando HTTP para puerto Icecast no estándar: " + url);
            normalizedUrl = url.replace("https://", "http://");
        } else {
            normalizedUrl = url;
        }
        final String finalUrl = normalizedUrl;

        mCurrentStreamUrl = finalUrl;
        if (mListener != null) {
            mListener.onBeforeStreamStart();
        }
        // No notificar onStreamStoppedMt8163: es un reinicio de stream, no vuelta a FM.
        stopStreamInternal(false);

        mIsLoading = true;
        updateUI();

        // 1. Conmutar canal MCU a Android (El hardware de la radio debe saber que Android va a sonar)
        if (mPlaybackManager != null) {
            if (mPlaybackManager.getEngine() != null) {
                mPlaybackManager.getEngine().setOnlineStreamingActive(true); 
                mPlaybackManager.getEngine().switchToAndroidAudio(); // V18.6: K706 lo necesita para retornar el canal de audio
            }
        }

        // 2. Inicializar ExoPlayer
        mExoPlayer = new ExoPlayer.Builder(mContext).build();
        
        // El parámetro 'false' indica que ExoPlayer NO manejará automáticamente el AudioFocus con el sistema.
        // Además, usamos atributos de "Navegación GPS" para engañar al coche. Si usamos USAGE_MEDIA,
        // el sistema MTK detecta que abrimos un hilo de audio musical y asesina el backend de la radio.
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build();
        mExoPlayer.setAudioAttributes(audioAttributes, false);

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
                            Log.d(TAG, "ExoPlayer reproduciendo: " + finalUrl);
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
                String errorCodeName = error.getErrorCodeName();
                Log.e(TAG, "ExoPlayer Error (" + errorCodeName + "): " + error.getMessage(), error);
                
                // V18.6: Autorecuperación si la conexión a internet es lenta y nos caemos del "Live Window" HLS
                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    Log.w(TAG, "BehindLiveWindowException - Intentando resincronizar streaming en vivo...");
                    if (mExoPlayer != null) {
                        mExoPlayer.seekToDefaultPosition();
                        mExoPlayer.prepare();
                    }
                    return;
                }

                if (mListener != null) {
                    mListener.onStreamError("Error (" + errorCodeName + "): " + error.getMessage());
                }
                stopStream();
            }
        });

        try {
            MediaItem.Builder mediaItemBuilder = new MediaItem.Builder().setUri(finalUrl);
            
            // V18.4/V18.6.5: Forzar detección de tipo si la URL es ambigua
            String lowerUrl = finalUrl.toLowerCase();
            if (lowerUrl.contains("m3u8")) {
                mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8);
            } else if (lowerUrl.contains(".aac") || lowerUrl.contains("type=aac")) {
                mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.AUDIO_AAC);
            } else if (lowerUrl.endsWith("/stream") || lowerUrl.contains(".mp3") || lowerUrl.contains("icecast")) {
                // V18.6.5: Forzar MPEG para el caso específico del usuario (Elite Comunicación)
                mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.AUDIO_MPEG);
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
        stopStreamInternal(true);
    }

    /**
     * @param notifyMt8163DeferredHcn {@code false} cuando solo se limpia antes de un nuevo stream
     *                                (evita programar reconexión HCN + bind que rompe el arranque).
     */
    private void stopStreamInternal(boolean notifyMt8163DeferredHcn) {
        mIsLoading = false;
        mIsPlaying = false;

        // V21.5 FIX: Bajar el flag streaming ANTES de cualquier otra operación.
        // Así, si PlaybackManager.setMute(false) llega a enforceAudioRecovery(),
        // ya verá streaming=false y podrá recuperar el canal FM correctamente.
        if (mPlaybackManager != null && mPlaybackManager.getEngine() != null) {
            mPlaybackManager.getEngine().setOnlineStreamingActive(false);
        }

        // MT8163: bajar la sesión de medios antes de reconectar HCN (reduce force-stop en SourceService).
        boolean mt8163 = mPlaybackManager != null && mPlaybackManager.getEngine() != null
                && "MT8163".equals(mPlaybackManager.getEngine().getEngineName());
        try {
            if (mt8163) {
                Intent handoff = new Intent(mContext, RadioMediaService.class);
                handoff.setAction(RadioMediaService.ACTION_MT8163_FM_HANDOFF);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(handoff);
                } else {
                    mContext.startService(handoff);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "MT8163 FM handoff (MediaSession)", e);
        }

        updateUI();

        if (mt8163 && mExoPlayer != null) {
            mMainHandler.postDelayed(
                    () -> finishStreamStopAfterExoRelease(notifyMt8163DeferredHcn),
                    MT8163_EXO_RELEASE_AFTER_HANDOFF_MS);
            Log.d(TAG, "MT8163: liberación ExoPlayer diferida " + MT8163_EXO_RELEASE_AFTER_HANDOFF_MS + "ms tras handoff");
            return;
        }

        finishStreamStopAfterExoRelease(notifyMt8163DeferredHcn);
    }

    private void finishStreamStopAfterExoRelease(boolean notifyMt8163DeferredHcn) {
        if (mExoPlayer != null) {
            try {
                mExoPlayer.stop();
                mExoPlayer.release(); // ExoPlayer libera automáticamente el AudioFocus al destruirse
            } catch (Exception e) {
                Log.e(TAG, "Error liberando ExoPlayer", e);
            }
            mExoPlayer = null;
        }

        // Recuperar FM solo cuando el usuario para el stream (notifyMt8163DeferredHcn == true).
        // Si es false, venimos de startStreamDirect(): limpiamos ExoPlayer y enseguida pedimos canal
        // Android + nuevo player; un postDelayed aquí ejecutaba switchToFmAudio() ~150ms después
        // y en K706 forzaba SetChannel(2) — síntoma: streaming "activo" pero audio sigue en FM.
        if (notifyMt8163DeferredHcn && mPlaybackManager != null) {
            long delayMs = 150L;
            try {
                if (mPlaybackManager.getEngine() != null
                        && "MT8163".equals(mPlaybackManager.getEngine().getEngineName())) {
                    delayMs = 450L;
                }
            } catch (Exception ignored) {}
            mMainHandler.postDelayed(() -> {
                if (mPlaybackManager != null) {
                    mPlaybackManager.setMute(false);
                    if (mPlaybackManager.getEngine() != null) {
                        mPlaybackManager.getEngine().switchToFmAudio();
                    }
                }
                try {
                    if (mPlaybackManager != null && mPlaybackManager.getEngine() != null
                            && "MT8163".equals(mPlaybackManager.getEngine().getEngineName())) {
                        Intent done = new Intent(mContext, RadioMediaService.class);
                        done.setAction(RadioMediaService.ACTION_MT8163_FM_HANDOFF_COMPLETE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mContext.startForegroundService(done);
                        } else {
                            mContext.startService(done);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "MT8163 FM handoff complete (MediaSession)", e);
                }
            }, delayMs);
        }

        Log.d(TAG, "Streaming detenido por completo.");

        if (notifyMt8163DeferredHcn && mListener != null) {
            try {
                if (mPlaybackManager != null && mPlaybackManager.getEngine() != null
                        && "MT8163".equals(mPlaybackManager.getEngine().getEngineName())) {
                    mListener.onStreamStoppedMt8163();
                }
            } catch (Exception e) {
                Log.w(TAG, "onStreamStoppedMt8163", e);
            }
        }

        if (notifyMt8163DeferredHcn && mPlaybackManager != null && mPlaybackManager.getEngine() != null
                && "MT8163".equals(mPlaybackManager.getEngine().getEngineName())) {
            try {
                Toast.makeText(mContext.getApplicationContext(),
                        mContext.getString(R.string.mt8163_stream_stopped_restart_hint),
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.w(TAG, "Toast MT8163 stream stopped", e);
            }
        }
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
        mIsLoading = false;
        mIsPlaying = false;
        if (mExoPlayer != null) {
            try {
                mExoPlayer.stop();
                mExoPlayer.release();
            } catch (Exception e) {}
            mExoPlayer = null;
        }
        // No llamamos a stopStream() completo para evitar reconectar el mixer de hardware FM
        // si la aplicación se está cerrando o apagando (onDestroy).
    }
}
