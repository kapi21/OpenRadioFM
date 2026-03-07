package com.example.openradiofm.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.example.openradiofm.R;

import java.util.ArrayList;
import java.util.List;

/**
 * V16: Servicio para compatibilidad con Android Auto y controles de medios.
 * Permite que el sistema y el coche interactúen con la radio como una app de medios estándar.
 */
public class RadioMediaService extends MediaBrowserServiceCompat {
    private static final String TAG = "RadioMediaService";
    private static final String MEDIA_ROOT_ID = "radio_root";
    private static final String PRESETS_ID = "presets_folder";

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Inicializar MediaSession
        mMediaSession = new MediaSessionCompat(this, TAG);

        // 2. Definir los flags: Soporta comandos de transporte y botones de medios
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );

        // 3. Inicializar el estado de reproducción (Radio siempre "Playing" si está encendida)
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                );
        mMediaSession.setPlaybackState(mStateBuilder.build());

        // 4. Establecer el Callback (será conectado a MainActivity / RadioEngine)
        // Por ahora se queda vacío, se gestiona vía MediaSessionManager
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                // Comandado por MediaSessionManager / Broadcast
                sendBroadcastToActivity("ACTION_PLAY");
            }

            @Override
            public void onPause() {
                sendBroadcastToActivity("ACTION_PAUSE");
            }

            @Override
            public void onSkipToNext() {
                sendBroadcastToActivity("ACTION_NEXT");
            }

            @Override
            public void onSkipToPrevious() {
                sendBroadcastToActivity("ACTION_PREV");
            }

            @Override
            public void onCustomAction(String action, Bundle extras) {
                if ("ACTION_UPDATE_METADATA".equals(action) && extras != null) {
                    extras.setClassLoader(MediaMetadataCompat.class.getClassLoader());
                    MediaMetadataCompat metadata = extras.getParcelable("metadata"); // Simplificado
                    // En la práctica, extraemos los campos del bundle directamente para evitar errores de ClassLoader
                    MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
                    if (extras.containsKey(MediaMetadataCompat.METADATA_KEY_TITLE)) {
                        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, extras.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                    }
                    if (extras.containsKey(MediaMetadataCompat.METADATA_KEY_ARTIST)) {
                        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, extras.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                    }
                    if (extras.containsKey(MediaMetadataCompat.METADATA_KEY_ALBUM)) {
                        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, extras.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
                    }
                    // Artwork...
                    mMediaSession.setMetadata(builder.build());
                }
            }
        });

        // 5. Vincular el token a la sesión
        setSessionToken(mMediaSession.getSessionToken());
    }

    private void sendBroadcastToActivity(String action) {
        Intent intent = new Intent("com.example.openradiofm.MEDIA_CONTROL");
        intent.putExtra("command", action);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // Permitir que cualquier app (ej: Android Auto) navegue por los favoritos (presets)
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        if (MEDIA_ROOT_ID.equals(parentId)) {
            // Devolvemos una lista de categorías (ej: Favoritos)
            List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
            items.add(new MediaBrowserCompat.MediaItem(
                    new MediaDescriptionCompat.Builder()
                            .setMediaId(PRESETS_ID)
                            .setTitle(getString(R.string.my_favorites))
                            .setSubtitle("Emisoras memorizadas")
                            .build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
            result.sendResult(items);
        } else if (PRESETS_ID.equals(parentId)) {
            // Aquí se enviarán las emisoras guardadas. 
            // Esta lógica se actualizará dinámicamente cuando MainActivity esté conectada.
            result.sendResult(null); // Pendiente implementación dinámica
        } else {
            result.sendResult(null);
        }
    }

    @Override
    public void onDestroy() {
        mMediaSession.release();
        super.onDestroy();
    }
}
