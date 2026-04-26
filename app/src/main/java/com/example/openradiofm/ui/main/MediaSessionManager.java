package com.example.openradiofm.ui.main;

import android.content.ComponentName;
import android.graphics.Bitmap;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.example.openradiofm.service.RadioMediaService;

import java.util.Locale;

/**
 * V16: Gestor de la Sesión de Medios.
 * Sincroniza el estado de la UI (Frecuencia, RDS, Logos) con la MediaSession
 * del RadioMediaService para que se vean en Android Auto y notificaciones.
 */
public class MediaSessionManager {
    private static final String TAG = "MediaSessionManager";

    private final MainActivity mActivity;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    public MediaSessionManager(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Inicia la conexión con el MediaBrowserService.
     */
    public void connect() {
        mMediaBrowser = new MediaBrowserCompat(mActivity,
                new ComponentName(mActivity, RadioMediaService.class),
                mConnectionCallbacks,
                null);
        mMediaBrowser.connect();
    }

    /**
     * Cierra la conexión.
     */
    public void disconnect() {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
        }
    }

    /**
     * Actualiza los metadatos de la sesión: Emisora, RDS y Logo.
     */
    public void updateMetadata(String title, String subtitle, Bitmap logo) {
        // Source of truth: RadioMediaService actualiza la MediaSession desde callbacks del engine/prefs.
        // Mantener método por compatibilidad con código legacy; intencionalmente no empujamos metadata desde la UI.
        if (mMediaController == null) return;
    }

    public void updateRds(String rdsText) {
        // Source of truth: RadioMediaService. Mantener por compatibilidad.
        if (mMediaController == null) return;
    }

    public void updatePlaybackState(int state) {
        // En una radio, el estado suele ser siempre PLAYING o PAUSED(MUTE/OFF)
        if (mMediaController == null) return;
        // Lógica de estado simplificada para el sistema
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
                    mMediaController = new MediaControllerCompat(mActivity, token);
                    MediaControllerCompat.setMediaController(mActivity, mMediaController);
                    mMediaController.registerCallback(mControllerCallback);
                    Log.d(TAG, "MediaSession Connected Successfully");
                }



                @Override
                public void onConnectionSuspended() {
                    Log.w(TAG, "MediaSession Connection Suspended");
                }

                @Override
                public void onConnectionFailed() {
                    Log.e(TAG, "MediaSession Connection Failed");
                }
            };

    private final MediaControllerCompat.Callback mControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    // Sincronizar UI si el sistema envía comandos (ej: volante)
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                }
            };
}
