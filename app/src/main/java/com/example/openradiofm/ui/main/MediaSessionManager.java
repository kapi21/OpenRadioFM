package com.example.openradiofm.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.example.openradiofm.service.RadioMediaService;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // V2.4: State guards to avoid redundant system updates
    private String mLastTitle = "";
    private String mLastSubtitle = "";
    private Bitmap mLastLogo = null;
    private String mLastRds = "";

    private static final Pattern FREQ_SUBTITLE_PATTERN = Pattern.compile(
            "^\\s*([0-9]+)\\s*([.,])\\s*([0-9]+)\\s*MHz\\s*$", Pattern.CASE_INSENSITIVE);

    /** Unifica "94,2 MHz" / "94.20 MHz" / "94.2 MHz" para no duplicar envíos a MediaSession. */
    private static String normalizeFreqMHzSubtitle(String subtitle) {
        if (subtitle == null) return "";
        Matcher m = FREQ_SUBTITLE_PATTERN.matcher(subtitle.trim());
        if (!m.matches()) return subtitle.trim();
        try {
            double v = Double.parseDouble(m.group(1) + "." + m.group(3));
            return String.format(Locale.US, "%.1f MHz", v);
        } catch (NumberFormatException e) {
            return subtitle.trim();
        }
    }

    private static String metadataDedupKey(String title, String subtitle) {
        String t = title != null ? title.trim().replaceAll("\\s+", " ") : "";
        String s = subtitle != null ? normalizeFreqMHzSubtitle(subtitle) : "";
        return t + "\u0001" + s;
    }

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
        if (mMediaController == null) return;

        // V2.4: Guard (título + MHz normalizado + logo) evita ráfagas por locale/comas/decimales.
        boolean logoSame = (logo == null && mLastLogo == null) || (logo != null && mLastLogo != null && logo.sameAs(mLastLogo));
        if (metadataDedupKey(title, subtitle).equals(metadataDedupKey(mLastTitle, mLastSubtitle)) && logoSame) {
            return;
        }

        mLastTitle = title;
        mLastSubtitle = subtitle;
        mLastLogo = logo;

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subtitle);

        if (logo != null) {
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, logo);
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, logo);
        }

        mMediaController.getTransportControls().sendCustomAction("ACTION_UPDATE_METADATA", builder.build().getBundle());
        Log.d(TAG, "Metadata update sent: " + title + " (" + subtitle + ")");
    }

    public void updateRds(String rdsText) {
        if (mMediaController == null || rdsText == null) return;
        
        // V2.4: Guard
        if (rdsText.equals(mLastRds)) return;
        mLastRds = rdsText;

        // El RDS se suele mapear al álbum o subtítulo en Android Auto
        MediaMetadataCompat current = mMediaController.getMetadata();
        MediaMetadataCompat.Builder builder = (current != null) 
            ? new MediaMetadataCompat.Builder(current) 
            : new MediaMetadataCompat.Builder();
        
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, rdsText);
        mMediaController.getTransportControls().sendCustomAction("ACTION_UPDATE_METADATA", builder.build().getBundle());
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
