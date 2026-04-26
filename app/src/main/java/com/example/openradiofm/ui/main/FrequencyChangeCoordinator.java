package com.example.openradiofm.ui.main;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.QS6Engine;

/**
 * Coalescencia (~280 ms) y trabajo pesado al cambiar de frecuencia (Fase 2 refactor 5.2.0.MCU).
 * <p>
 * Antes vivía en {@link MainActivity#handleFrequencyChange(int)}; se extrae para acotar responsabilidades
 * y facilitar pruebas sin tocar el resto de la activity.
 * </p>
 */
public final class FrequencyChangeCoordinator {
    private static final String TAG = "OpenRadioFm";

    /**
     * QS6/OEM: ráfagas de callbacks + broadcasts al launcher pueden crear un bucle (freq inestable, PS pegado).
     * Coalescencia del trabajo pesado salvo que pasen ~280 ms desde el último ciclo.
     */
    private static final long FREQ_CHANGE_HEAVY_COALESCE_MS = 280L;

    private final RadioUiHost mHost;
    private int mFreqHeavyPendingFreq = -1;
    private boolean mFreqHeavySuppressPersist = false;
    private long mFreqHeavyLastCompleteMs = 0L;
    private final Runnable mFreqHeavyRunnable = this::runPendingFrequencyChangeHeavy;

    public FrequencyChangeCoordinator(RadioUiHost host) {
        mHost = host;
    }

    void cancelPendingHeavy() {
        mHost.getMainHandler().removeCallbacks(mFreqHeavyRunnable);
    }

    /**
     * Tras {@link MainActivity#gotoFreq(int)}: el motor suele notificar la misma frecuencia y
     * {@link #handleFrequencyChange(int)} sale por {@code freq == mLastFreq} sin aplicar estado pesado.
     * Esta ruta unifica el mismo trabajo (cloud guard, RDS reset, streaming, MediaSession, widget, prefs)
     * sin coalescencia. No añade historial (comportamiento histórico de {@code gotoFreq}).
     * <p>
     * QS6: {@code gotoFreq} ya primó nombre RDS/caché y guardas antes del {@code tune}; no se pisan
     * {@code tvRdsName}/{@code tvRdsInfo} ni se duplica el bump de generación de logo / guarda RDS.
     * </p>
     */
    void finishUserTuneFromUi(int freq, boolean isQs6) {
        mHost.getMainHandler().removeCallbacks(mFreqHeavyRunnable);
        mFreqHeavyPendingFreq = -1;
        applyFrequencyChangeHeavy(freq, false, true, !isQs6, isQs6, true);
        mFreqHeavyLastCompleteMs = android.os.SystemClock.elapsedRealtime();
    }

    void handleFrequencyChange(int freq) {
        if (freq == mHost.getLastFreqKhz()) {
            return;
        }

        if (android.os.SystemClock.elapsedRealtime() < mHost.getShutdownPersistGuardUntilMs()) {
            Log.d(TAG, "Shutdown guard: skipping frequency callback " + freq);
            return;
        }

        StartupFreqPersistGuards g = mHost.getStartupFreqPersistGuards();
        boolean suppressStartupPersist = false;
        if (g.startupSavedFreqKhz > 0
                && android.os.SystemClock.elapsedRealtime() < g.startupPersistGuardUntilMs) {
            if (freq != g.startupSavedFreqKhz && (freq == 87600 || freq == 87500)) {
                suppressStartupPersist = true;
                Log.d(TAG, "Startup guard: suppress persist for bootstrap freq " + freq
                        + " (saved=" + g.startupSavedFreqKhz + ")");
                if ((mHost.getFmMode() == MainActivity.FmMode.FM_QS6 || mHost.getFmMode() == MainActivity.FmMode.FM_K706
                        || mHost.getFmMode() == MainActivity.FmMode.FM_JANCAR_IVI)
                        && mHost.getRadioEngine() != null && g.startupRetuneAttempts < 3) {
                    final int targetFreq = g.startupSavedFreqKhz;
                    final int targetBand = mHost.getLastStoredBand();
                    g.startupRetuneAttempts++;
                    mHost.getMainHandler().postDelayed(() -> {
                        try {
                            if (mHost.isHostFinishing() || mHost.isHostDestroyed() || mHost.getRadioEngine() == null) {
                                return;
                            }
                            int current = mHost.getRadioEngine().getCurrentFreq();
                            if (current == 87600 || current == 87500) {
                                Log.d(TAG, "Startup guard: re-assert saved station "
                                        + targetFreq + "/B" + targetBand
                                        + " (attempt " + g.startupRetuneAttempts + ")");
                                if (mHost.getRadioEngine() instanceof QS6Engine) {
                                    ((QS6Engine) mHost.getRadioEngine()).tuneWithBand(targetFreq, targetBand);
                                } else {
                                    mHost.getRadioEngine().tune(targetFreq);
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Startup guard re-assert failed", e);
                        }
                    }, 260L);
                }
            }
            if (freq == g.startupSavedFreqKhz) {
                g.startupPersistGuardUntilMs = 0L;
            }
        }

        if ((mHost.getFmMode() == MainActivity.FmMode.FM_QS6 || mHost.getFmMode() == MainActivity.FmMode.FM_K706
                || mHost.getFmMode() == MainActivity.FmMode.FM_JANCAR_IVI) && (freq == 87500 || freq == 87600)) {
            boolean userRequestedRecently =
                    g.userRequestedFreqKhz == freq
                            && android.os.SystemClock.elapsedRealtime() <= g.userRequestedFreqUntilMs;
            if (!userRequestedRecently) {
                suppressStartupPersist = true;
                Log.d(TAG, "Bootstrap persist guard: suppress " + freq + " (no recent user request, mode="
                        + mHost.getFmMode() + ")");
            }
        }

        mFreqHeavyPendingFreq = freq;
        mFreqHeavySuppressPersist = suppressStartupPersist;
        mHost.getMainHandler().removeCallbacks(mFreqHeavyRunnable);
        long now = android.os.SystemClock.elapsedRealtime();
        long wait = FREQ_CHANGE_HEAVY_COALESCE_MS - (now - mFreqHeavyLastCompleteMs);
        if (wait <= 0L) {
            mHost.getMainHandler().post(mFreqHeavyRunnable);
        } else {
            mHost.getMainHandler().postDelayed(mFreqHeavyRunnable, wait);
        }
    }

    private void runPendingFrequencyChangeHeavy() {
        int freq = mFreqHeavyPendingFreq;
        if (freq < 0) return;
        mFreqHeavyPendingFreq = -1;
        if (freq == mHost.getLastFreqKhz()) {
            mFreqHeavyLastCompleteMs = android.os.SystemClock.elapsedRealtime();
            return;
        }
        applyFrequencyChangeHeavy(freq, mFreqHeavySuppressPersist, false, true, false, false);
        mFreqHeavyLastCompleteMs = android.os.SystemClock.elapsedRealtime();
    }

    /**
     * @param suppressStartupPersist guardas de arranque/apagado (ruta motor)
     * @param skipHistory            {@code true} en sintonía explícita desde UI ({@code gotoFreq} no añadía historial)
     * @param clearRdsTextWidgetsOnUi {@code false} en QS6 tras primar nombre en {@code gotoFreq}
     * @param skipLogoBumpAndTransitionGuard {@code true} si QS6 ya aplicó bump/guarda antes del {@code tune}
     * @param allowWorkWhileScanning {@code true} solo para {@link #finishUserTuneFromUi}; {@code gotoFreq} persistía aunque {@code mIsScanning}
     */
    private void applyFrequencyChangeHeavy(int freq, boolean suppressStartupPersist, boolean skipHistory,
            boolean clearRdsTextWidgetsOnUi, boolean skipLogoBumpAndTransitionGuard,
            boolean allowWorkWhileScanning) {
        if (!skipLogoBumpAndTransitionGuard) {
            mHost.beginRdsLogoTransitionAfterTune(mHost.getLastPs());
        }
        mHost.setLastFreqKhz(freq);
        mHost.armCloudContribFreqSettleWindow();
        mHost.setLastStoredBand(mHost.getUiCurrentBand());
        mHost.clearCachedLogoUrl();
        mHost.setCurrentPi(null);
        mHost.setCurrentPty(null);
        mHost.setLastPs("");
        mHost.setHasRdsLock(false);
        mHost.setHadRdsLockForTick(false);

        if (mHost.getRdsManager() != null) {
            // MT8163: handleFrequencyChange puede venir desde un hilo de polling del engine.
            // RDSManager.reset(true) toca TextViews (setText) y puede crashear por CalledFromWrongThreadException.
            // La limpieza visual ya se hace más abajo dentro de runOnHostUiThread().
            mHost.getRdsManager().reset(false);
        }

        OnlineStreamManager osm = mHost.getOnlineStreamManager();
        if (osm != null && (osm.isPlaying() || osm.isLoading())) {
            osm.stopStream();
        }

        mHost.runOnHostUiThread(() -> {
            if (mHost.isHostFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && mHost.isHostDestroyed())) {
                return;
            }
            if (clearRdsTextWidgetsOnUi) {
                TextView tvRdsName = (TextView) mHost.findHostViewById(R.id.tvRdsName);
                if (tvRdsName != null) {
                    tvRdsName.setText("");
                    tvRdsName.setVisibility(View.VISIBLE);
                }
                TextView tvRdsInfo = (TextView) mHost.findHostViewById(R.id.tvRdsInfo);
                if (tvRdsInfo != null) {
                    tvRdsInfo.setText("");
                    tvRdsInfo.setVisibility(View.VISIBLE);
                }
                TextView tvPty = (TextView) mHost.findHostViewById(R.id.tvPty);
                if (tvPty != null) {
                    tvPty.setText(mHost.getHostContext().getString(R.string.pty_none));
                }
            }
            mHost.refreshStereoIndicatorUi(null);

            mHost.clearStationLogoUi();
        });

        if (mHost.isUiScanning() && !allowWorkWhileScanning) {
            Log.d(TAG, "Scanning in progress: skipping history/persistence for freq " + freq);
            return;
        }

        if (mHost.getRadioPresets() != null) {
            if (!suppressStartupPersist && !skipHistory && mHost.getRadioPresets().getBoolean("pref_save_history", true)) {
                mHost.addFreqToHistory(freq);
            }
            if (!suppressStartupPersist) {
                mHost.getRadioPresets().edit()
                        .putInt("pref_last_freq", freq)
                        .putInt("pref_last_band", mHost.getUiCurrentBand())
                        .apply();
                Log.d(TAG, "Last freq saved & History updated: " + freq);
            } else {
                Log.d(TAG, "Startup guard: skipping pref_last_freq/history persist for " + freq);
            }
        }

        Log.d(TAG, "Frequency changed to " + freq + " - UI Reset triggered");

        if (mHost.getMediaSessionManager() != null) {
            String title = (freq / 1000.0) + " MHz";
            mHost.getMediaSessionManager().updateMetadata(title, "OpenRadioFM", null);
        }

        mHost.sendWidgetUpdate(freq, mHost.getUiCurrentBand(), null);
    }
}
