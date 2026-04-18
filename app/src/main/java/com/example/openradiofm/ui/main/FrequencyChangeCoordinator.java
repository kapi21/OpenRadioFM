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

    void handleFrequencyChange(int freq) {
        if (freq == mA.mLastFreq) {
            return;
        }

        if (android.os.SystemClock.elapsedRealtime() < mA.mShutdownPersistGuardUntilMs) {
            Log.d(MainActivity.TAG, "Shutdown guard: skipping frequency callback " + freq);
            return;
        }

        boolean suppressStartupPersist = false;
        if (mA.mStartupSavedFreqKhz > 0
                && android.os.SystemClock.elapsedRealtime() < mA.mStartupPersistGuardUntilMs) {
            if (freq != mA.mStartupSavedFreqKhz && (freq == 87600 || freq == 87500)) {
                suppressStartupPersist = true;
                Log.d(MainActivity.TAG, "Startup guard: suppress persist for bootstrap freq " + freq
                        + " (saved=" + mA.mStartupSavedFreqKhz + ")");
                if ((mA.mMode == MainActivity.FmMode.FM_QS6 || mA.mMode == MainActivity.FmMode.FM_K706
                        || mA.mMode == MainActivity.FmMode.FM_JANCAR_IVI) && mA.mEngine != null && mA.mStartupRetuneAttempts < 3) {
                    final int targetFreq = mA.mStartupSavedFreqKhz;
                    final int targetBand = mA.mLastBand;
                    mA.mStartupRetuneAttempts++;
                    mA.mMainHandler.postDelayed(() -> {
                        try {
                            if (mA.isFinishing() || mA.isDestroyed() || mA.mEngine == null) return;
                            int current = mA.mEngine.getCurrentFreq();
                            if (current == 87600 || current == 87500) {
                                Log.d(MainActivity.TAG, "Startup guard: re-assert saved station "
                                        + targetFreq + "/B" + targetBand
                                        + " (attempt " + mA.mStartupRetuneAttempts + ")");
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
            if (freq == mA.mStartupSavedFreqKhz) {
                mA.mStartupPersistGuardUntilMs = 0L;
            }
        }

        if ((mA.mMode == MainActivity.FmMode.FM_QS6 || mA.mMode == MainActivity.FmMode.FM_K706
                || mA.mMode == MainActivity.FmMode.FM_JANCAR_IVI) && (freq == 87500 || freq == 87600)) {
            boolean userRequestedRecently =
                    mA.mUserRequestedFreqKhz == freq
                            && android.os.SystemClock.elapsedRealtime() <= mA.mUserRequestedFreqUntilMs;
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
        applyFrequencyChangeHeavy(freq, mFreqHeavySuppressPersist);
        mFreqHeavyLastCompleteMs = android.os.SystemClock.elapsedRealtime();
    }

    /** Trabajo pesado tras coalescencia (logo, RDS, historial, widget, MediaSession). */
    private void applyFrequencyChangeHeavy(int freq, boolean suppressStartupPersist) {
        mA.mLogoUiGeneration.incrementAndGet();
        mA.mPrevStationNameBeforeTune = mA.mLastPs != null ? mA.mLastPs : "";
        mA.mRdsTransitionGuardUntilMs = android.os.SystemClock.elapsedRealtime() + MainActivity.RDS_TRANSITION_GUARD_MS;
        mA.mLastFreq = freq;
        mA.mCloudContribAllowedAfterMs = android.os.SystemClock.elapsedRealtime() + MainActivity.CLOUD_CONTRIB_FREQ_SETTLE_MS;
        mA.mLastBand = mA.mCurrentBand;
        mA.mLastLogoUrl = ""; // Force logo reload
        mA.mCurrentPi = null;
        mA.mCurrentPty = null;
        mA.mLastPs = ""; // V18.6.4: Clear cached RDS name to avoid stale display on new freq
        mA.mHasRdsLock = false;
        mA.mHadRdsLockForTick = false;

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
            if (mA.tvRdsName != null) {
                mA.tvRdsName.setText("");
                mA.tvRdsName.setVisibility(View.VISIBLE);
            }
            if (mA.tvRdsInfo != null) {
                mA.tvRdsInfo.setText("");
                mA.tvRdsInfo.setVisibility(View.VISIBLE);
            }
            mA.refreshStereoIndicatorUi(null);
            if (mA.tvPty != null) {
                mA.tvPty.setText(mA.getString(R.string.pty_none));
            }

            mA.clearStationLogoUi();
        });

        if (mA.mIsScanning) {
            Log.d(MainActivity.TAG, "Scanning in progress: skipping history/persistence for freq " + freq);
            return;
        }

        if (mA.mPrefs != null) {
            if (!suppressStartupPersist && mA.mPrefs.getBoolean("pref_save_history", true)) {
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
