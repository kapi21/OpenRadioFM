package com.example.openradiofm.service;

import android.content.Intent;
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
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import android.graphics.Bitmap;

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

    private static final String CHANNEL_ID = "radio_channel";
    private static final int NOTIFICATION_ID = 101;

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;
    
    // V2.6: State guards for notification to avoid Binder flood
    private String mLastNotifiedTitle = "";
    private String mLastNotifiedArtist = "";
    private Bitmap mLastNotifiedLogo = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

        // 1. Inicializar MediaSession
        mMediaSession = new MediaSessionCompat(this, TAG);

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
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                );
        mMediaSession.setPlaybackState(mStateBuilder.build());

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
                    // updateNotification(); // REMOVED: onMetadataChanged will trigger it via callback
                }
            }
        });

        // 5. Vincular el token a la sesión
        setSessionToken(mMediaSession.getSessionToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // V18.6: Iniciar como Foreground Service para evitar que Android lo mate
        startForeground(NOTIFICATION_ID, buildNotification("Sintonizando...", "Radio FM", null));
        return START_STICKY;
    }

    private void updateNotification() {
        MediaMetadataCompat metadata = mMediaSession.getController().getMetadata();
        String title = "Cargando...";
        String artist = "Radio FM";
        Bitmap logo = null;

        if (metadata != null) {
            title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            logo = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
        }

        // V2.6: Master Guard for System IPC
        if (title != null && title.equals(mLastNotifiedTitle) && 
            artist != null && artist.equals(mLastNotifiedArtist) &&
            ((logo == null && mLastNotifiedLogo == null) || (logo != null && logo.sameAs(mLastNotifiedLogo)))) {
            return;
        }

        mLastNotifiedTitle = title;
        mLastNotifiedArtist = artist;
        mLastNotifiedLogo = logo;

        mNotificationManager.notify(NOTIFICATION_ID, buildNotification(title, artist, logo));
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
                    .setOnlyAlertOnce(true)
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
            Log.e(TAG, "Error building notification: " + e.getMessage());
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
