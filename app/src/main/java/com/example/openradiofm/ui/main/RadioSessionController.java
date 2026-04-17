package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.example.openradiofm.data.source.RadioEngine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Controlador de sesión de radio.
 *
 * Centraliza el estado lógico de la radio (frecuencia, banda, mute, RDS básico)
 * y ofrece una API de alto nivel para que Activity/servicios disparen acciones
 * sin duplicar lógica ni estado.
 *
 * V1: implementación mínima, sin cambiar todavía la lógica de MainActivity.
 */
public class RadioSessionController {

    private final Context appContext;
    private final RadioEngine engine;
    private final PlaybackManager playbackManager;
    private final SharedPreferences presetPrefs;
    private final SharedPreferences stationNamePrefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Set<RadioSessionListener> listeners =
            Collections.synchronizedSet(new HashSet<RadioSessionListener>());

    private volatile RadioSessionState currentState = RadioSessionState.initial();

    public RadioSessionController(
            @NonNull Context context,
            @NonNull RadioEngine engine,
            PlaybackManager playbackManager,
            SharedPreferences presetPrefs,
            SharedPreferences stationNamePrefs
    ) {
        this.appContext = context.getApplicationContext();
        this.engine = engine;
        this.playbackManager = playbackManager;
        this.presetPrefs = presetPrefs;
        this.stationNamePrefs = stationNamePrefs;
    }

    // region Public API

    public RadioSessionState getCurrentState() {
        return currentState;
    }

    public void addListener(@NonNull RadioSessionListener listener) {
        listeners.add(listener);
        // Entregar estado actual inmediatamente
        dispatchStateToListener(listener, currentState);
    }

    public void removeListener(@NonNull RadioSessionListener listener) {
        listeners.remove(listener);
    }

    public void play() {
        updateState(currentState.freqKhz, currentState.band, true, false,
                currentState.accOn, currentState.rdsName, currentState.rdsText, currentState.pty, currentState.pi);
        // Audio
        try {
            if (playbackManager != null) {
                playbackManager.setMute(false);
            } else {
                engine.setMute(false);
                engine.enforceAudioRecovery();
            }
        } catch (Exception ignored) {
        }
    }

    public void pause() {
        updateState(currentState.freqKhz, currentState.band, false, true,
                currentState.accOn, currentState.rdsName, currentState.rdsText, currentState.pty, currentState.pi);
        try {
            if (playbackManager != null) {
                playbackManager.setMute(true);
            } else {
                engine.setMute(true);
            }
        } catch (Exception ignored) {
        }
    }

    public void toggleMute() {
        if (currentState.isMuted) {
            play();
        } else {
            pause();
        }
    }

    public void tuneTo(int freqKhz) {
        if (freqKhz <= 0) return;
        engine.tune(freqKhz);
        updateState(freqKhz, currentState.band, currentState.isPlaying, currentState.isMuted,
                currentState.accOn, currentState.rdsName, currentState.rdsText, currentState.pty, currentState.pi);
    }

    public void nextFavorite() {
        engine.nextFavorite();
    }

    public void prevFavorite() {
        engine.prevFavorite();
    }

    public void setBand(int band) {
        updateState(currentState.freqKhz, band, currentState.isPlaying, currentState.isMuted,
                currentState.accOn, currentState.rdsName, currentState.rdsText, currentState.pty, currentState.pi);
    }

    // endregion

    // region Engine event hooks

    public void onFrequencyChanged(int freqKhz) {
        // Al sintonizar otra frecuencia el PS/RDS anterior deja de ser válido; si lo conservamos,
        // MediaSession y logos resuelven nombres/archivos incorrectos hasta el siguiente RDS.
        updateState(freqKhz, currentState.band, currentState.isPlaying, currentState.isMuted,
                currentState.accOn, "", "", "", "");
    }

    public void onBandChanged(int band) {
        updateState(currentState.freqKhz, band, currentState.isPlaying, currentState.isMuted,
                currentState.accOn, currentState.rdsName, currentState.rdsText, currentState.pty, currentState.pi);
    }

    public void onStereoChanged(boolean stereo) {
        // No se refleja de momento en el estado de sesión.
    }

    public void onRdsName(String name) {
        updateState(currentState.freqKhz, currentState.band, currentState.isPlaying, currentState.isMuted,
                currentState.accOn, name, currentState.rdsText, currentState.pty, currentState.pi);
    }

    public void onRdsText(String text) {
        updateState(currentState.freqKhz, currentState.band, currentState.isPlaying, currentState.isMuted,
                currentState.accOn, currentState.rdsName, text, currentState.pty, currentState.pi);
    }

    public void onRdsPty(String pty) {
        updateState(currentState.freqKhz, currentState.band, currentState.isPlaying, currentState.isMuted,
                currentState.accOn, currentState.rdsName, currentState.rdsText, pty, currentState.pi);
    }

    public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
        // No se modela aún a nivel de sesión.
    }

    public void onRdsPi(String piCode) {
        updateState(currentState.freqKhz, currentState.band, currentState.isPlaying, currentState.isMuted,
                currentState.accOn, currentState.rdsName, currentState.rdsText, currentState.pty, piCode);
    }

    public void onAccChanged(boolean accOn) {
        updateState(currentState.freqKhz, currentState.band, currentState.isPlaying, currentState.isMuted,
                accOn, currentState.rdsName, currentState.rdsText, currentState.pty, currentState.pi);
    }

    public void onDxLocalChanged(boolean isLocal) {
        // No se modela aún.
    }

    public void onScanStatusChanged(boolean scanning) {
        // No se modela aún.
    }

    public void onRawEvent(int code, String data) {
        // Sin-op por ahora.
    }

    public void onSignalUpdate(int rssi, int snr) {
        // No se modela aún en el estado compartido.
    }

    // endregion

    // region Internal helpers

    private void updateState(
            int freqKhz,
            int band,
            boolean isPlaying,
            boolean isMuted,
            Boolean accOn,
            String rdsName,
            String rdsText,
            String pty,
            String pi
    ) {
        currentState = new RadioSessionState(
                freqKhz,
                band,
                isPlaying,
                isMuted,
                accOn,
                rdsName,
                rdsText,
                pty,
                pi
        );
        dispatchState(currentState);
    }

    private void dispatchState(final RadioSessionState state) {
        // Garantizamos que los listeners reciban callbacks en el hilo principal.
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (listeners) {
                    for (RadioSessionListener l : listeners) {
                        dispatchStateToListener(l, state);
                    }
                }
            }
        });
    }

    @MainThread
    private void dispatchStateToListener(RadioSessionListener listener, RadioSessionState state) {
        if (listener != null && state != null) {
            listener.onRadioSessionStateChanged(state);
        }
    }

    // endregion
}

