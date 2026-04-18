package com.example.openradiofm.ui.main;

import android.util.Log;
import android.view.View;

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
    /**
     * QS6/OEM: ráfagas de callbacks + broadcasts al launcher pueden crear un bucle (freq inestable, PS pegado).
     * Coalescencia del trabajo pesado salvo que pasen ~280 ms desde el último ciclo.
     */
    private static final long FREQ_CHANGE_HEAVY_COALESCE_MS = 280L;

    private final MainActivity mA;
    private int mFreqHeavyPendingFreq = -1;
    private boolean mFreqHeavySuppressPersist = false;
    private long mFreqHeavyLastCompleteMs = 0L;
    private final Runnable mFreqHeavyRunnable = this::runPendingFrequencyChangeHeavy;

    public FrequencyChangeCoordinator(MainActivity activity) {
        mA = activity;
    }

    void cancelPendingHeavy() {
        mA.mMainHandler.removeCallbacks(mFreqHeavyRunnable);
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
        mA.mMainHandler.removeCallbacks(mFreqHeavyRunnable);
        mFreqHeavyPendingFreq = -1;
        applyFrequencyChangeHeavy(freq, false, true, !isQs6, isQs6, true);
        mFreqHeavyLastCompleteMs = android.os.SystemClock.elapsedRealtime();
    }

    void handleFrequencyChange(int freq) {
        if (freq == mA.mLastFreq) {
            return;
        }

        if (android.os.SystemClock.elapsedRealtime() < mA.mShutdownPersistGuardUntilMs) {
            Log.d(MainActivity.TAG, "Shutdown guard: skipping frequency callback " + freq);
            return;
        }

        boolean suppressStartupPersist = false;
        if (mA.mStartupFqGuards.startupSavedFreqKhz > 0
                && android.os.SystemClock.elapsedRealtime() < mA.mStartupFqGuards.startupPersistGuardUntilMs) {
            if (freq != mA.mStartupFqGuards.startupSavedFreqKhz && (freq == 87600 || freq == 87500)) {
                suppressStartupPersist = true;
                Log.d(MainActivity.TAG, "Startup guard: suppress persist for bootstrap freq " + freq
                        + " (saved=" + mA.mStartupFqGuards.startupSavedFreqKhz + ")");
                if ((mA.mMode == MainActivity.FmMode.FM_QS6 || mA.mMode == MainActivity.FmMode.FM_K706
                        || mA.mMode == MainActivity.FmMode.FM_JANCAR_IVI) && mA.mEngine != null && mA.mStartupFqGuards.startupRetuneAttempts < 3) {
                    final int targetFreq = mA.mStartupFqGuards.startupSavedFreqKhz;
                    final int targetBand = mA.mLastBand;
                    mA.mStartupFqGuards.startupRetuneAttempts++;
                    mA.mMainHandler.postDelayed(() -> {
                        try {
                            if (mA.isFinishing() || mA.isDestroyed() || mA.mEngine == null) return;
                            int current = mA.mEngine.getCurrentFreq();
                            if (current == 87600 || current == 87500) {
                                Log.d(MainActivity.TAG, "Startup guard: re-assert saved station "
                                        + targetFreq + "/B" + targetBand
                                        + " (attempt " + mA.mStartupFqGuards.startupRetuneAttempts + ")");
                                if (mA.mEngine instanceof QS6Engine) {
                                    ((QS6Engine) mA.mEngine).tuneWithBand(targetFreq, targetBand);
                                } else {
                                    mA.mEngine.tune(targetFreq);
                                }
                            }
                        } catch (Exception e) {
                            Log.w(MainActivity.TAG, "Startup guard re-assert failed", e);
                        }
                    }, 260L);
                }
            }
            if (freq == mA.mStartupFqGuards.startupSavedFreqKhz) {
                mA.mStartupFqGuards.startupPersistGuardUntilMs = 0L;
            }
        }

        if ((mA.mMode == MainActivity.FmMode.FM_QS6 || mA.mMode == MainActivity.FmMode.FM_K706
                || mA.mMode == MainActivity.FmMode.FM_JANCAR_IVI) && (freq == 87500 || freq == 87600)) {
            boolean userRequestedRecently =
                    mA.mStartupFqGuards.userRequestedFreqKhz == freq
                            && android.os.SystemClock.elapsedRealtime() <= mA.mStartupFqGuards.userRequestedFreqUntilMs;
            if (!userRequestedRecently) {
                suppressStartupPersist = true;
                Log.d(MainActivity.TAG, "Bootstrap persist guard: suppress " + freq + " (no recent user request, mode="
                        + mA.mMode + ")");
            }
        }

        mFreqHeavyPendingFreq = freq;
        mFreqHeavySuppressPersist = suppressStartupPersist;
        mA.mMainHandler.removeCallbacks(mFreqHeavyRunnable);
        long now = android.os.SystemClock.elapsedRealtime();
        long wait = FREQ_CHANGE_HEAVY_COALESCE_MS - (now - mFreqHeavyLastCompleteMs);
        if (wait <= 0L) {
            mA.mMainHandler.post(mFreqHeavyRunnable);
        } else {
            mA.mMainHandler.postDelayed(mFreqHeavyRunnable, wait);
        }
    }

    private void runPendingFrequencyChangeHeavy() {
        int freq = mFreqHeavyPendingFreq;
        if (freq < 0) return;
        mFreqHeavyPendingFreq = -1;
        if (freq == mA.mLastFreq) {
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
            mA.mRdsLogoTransition.logoUiGeneration.incrementAndGet();
            mA.mRdsLogoTransition.prevStationNameBeforeTune = mA.mLastPs != null ? mA.mLastPs : "";
            mA.mRdsLogoTransition.rdsTransitionGuardUntilMs = android.os.SystemClock.elapsedRealtime() + MainActivity.RDS_TRANSITION_GUARD_MS;
        }
        mA.mLastFreq = freq;
        mA.mCloudContribAllowedAfterMs = android.os.SystemClock.elapsedRealtime() + MainActivity.CLOUD_CONTRIB_FREQ_SETTLE_MS;
        mA.mLastBand = mA.mCurrentBand;
        mA.mLastLogoUrl = ""; // Force logo reload
        mA.mCurrentPi = null;
        mA.mCurrentPty = null;
        mA.mLastPs = ""; // V18.6.4: Clear cached RDS name to avoid stale display on new freq
        mA.mRdsLockUiTick.hasLock = false;
        mA.mRdsLockUiTick.hadLockForTick = false;

        if (mA.mRdsManager != null) {
            // MT8163: handleFrequencyChange puede venir desde un hilo de polling del engine.
            // RDSManager.reset(true) toca TextViews (setText) y puede crashear por CalledFromWrongThreadException.
            // La limpieza visual ya se hace más abajo dentro de runOnUiThread().
            mA.mRdsManager.reset(false);
        }

        if (mA.mOnlineStreamManager != null && (mA.mOnlineStreamManager.isPlaying() || mA.mOnlineStreamManager.isLoading())) {
            mA.mOnlineStreamManager.stopStream();
        }

        mA.runOnUiThread(() -> {
            if (mA.isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && mA.isDestroyed())) {
                return;
            }
            if (clearRdsTextWidgetsOnUi) {
                if (mA.tvRdsName != null) {
                    mA.tvRdsName.setText("");
                    mA.tvRdsName.setVisibility(View.VISIBLE);
                }
                if (mA.tvRdsInfo != null) {
                    mA.tvRdsInfo.setText("");
                    mA.tvRdsInfo.setVisibility(View.VISIBLE);
                }
                if (mA.tvPty != null) {
                    mA.tvPty.setText(mA.getString(R.string.pty_none));
                }
            }
            mA.refreshStereoIndicatorUi(null);

            mA.clearStationLogoUi();
        });

        if (mA.mIsScanning && !allowWorkWhileScanning) {
            Log.d(MainActivity.TAG, "Scanning in progress: skipping history/persistence for freq " + freq);
            return;
        }

        if (mA.mPrefs != null) {
            if (!suppressStartupPersist && !skipHistory && mA.mPrefs.getBoolean("pref_save_history", true)) {
                mA.addToHistory(freq);
            }
            if (!suppressStartupPersist) {
                mA.mPrefs.edit()
                        .putInt("pref_last_freq", freq)
                        .putInt("pref_last_band", mA.mCurrentBand)
                        .apply();
                Log.d(MainActivity.TAG, "Last freq saved & History updated: " + freq);
            } else {
                Log.d(MainActivity.TAG, "Startup guard: skipping pref_last_freq/history persist for " + freq);
            }
        }

        Log.d(MainActivity.TAG, "Frequency changed to " + freq + " - UI Reset triggered");

        if (mA.mMediaSessionManager != null) {
            String title = (freq / 1000.0) + " MHz";
            mA.mMediaSessionManager.updateMetadata(title, "OpenRadioFM", null);
        }

        mA.sendWidgetUpdateIntent(freq, mA.mCurrentBand, null);
    }
}
