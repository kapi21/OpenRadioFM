package com.example.openradiofm.ui.main;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.RadioEngineCallback;
import com.example.openradiofm.ui.main.RDSManager;

public class EngineCallbackCoordinator implements RadioEngineCallback, RDSManager.RDSListener {
    private static final String TAG = "EngineCallbackCoordinator";
    private final MainActivity mActivity;

    public EngineCallbackCoordinator(MainActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public void onFrequencyChanged(int freqKhz) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onFrequencyChanged(freqKhz);
        }
        mActivity.handleFrequencyChange(freqKhz);
        if (mActivity.mScanManager != null && mActivity.mScanManager.isScanning()) {
            try { mActivity.mScanManager.onScanFrequencyChanged(freqKhz); } catch (Exception ignored) {}
        }
        mActivity.runOnUiThread(() -> {
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updateFrequency(freqKhz, null, mActivity.mCurrentBand >= 3);
            } else {
                mActivity.updateFrequencyDisplay(freqKhz, null);
            }
            if (mActivity.mSkinCoordinator != null) mActivity.mSkinCoordinator.reapplyVisualStateForCurrentSkin();
        });
    }

    private static String unitShortText(int band) {
        return (band == 3 || band == 4) ? "kHz" : "MHz";
    }

    @Override
    public void onBandChanged(int band) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onBandChanged(band);
        }
        mActivity.runOnUiThread(() -> {
            mActivity.mLogoUiGeneration.incrementAndGet();
            mActivity.clearStationLogoUi();
            mActivity.mCurrentBand = band;
            mActivity.mLastBand = band;
            if (mActivity.mPrefs != null) {
                mActivity.mPrefs.edit().putInt("pref_last_band", band).apply();
            }
            if (mActivity.mPresetManager != null) {
                mActivity.mPresetManager.refreshPresetsCache(band);
                mActivity.mPresetManager.refreshButtons(band);
            }
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updateBandIndicator(band);
            } else {
                mActivity.updateBandImage(band);
            }

            TextView ivUnitLabel = mActivity.findViewById(R.id.ivUnitLabel);
            if (ivUnitLabel != null) {
                MainActivity.setTextIfChanged(ivUnitLabel, unitShortText(band));
            }
            
            if (mActivity.mSkinCoordinator != null) mActivity.mSkinCoordinator.reapplyVisualStateForCurrentSkin();
            
            if (mActivity.mEngine != null) {
                mActivity.sendWidgetUpdate(mActivity.mEngine.getCurrentFreq(), band, mActivity.mLastPs);
            }
        });
    }

    @Override
    public void onStereoChanged(boolean stereo) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onStereoChanged(stereo);
        }
        mActivity.runOnUiThread(() -> {
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updateStereo(stereo);
            } else {
                TextView ivStereoIcon = mActivity.findViewById(R.id.ivStereoIcon);
                if (ivStereoIcon != null) MainActivity.setVisibilityIfChanged(ivStereoIcon, stereo ? View.VISIBLE : View.INVISIBLE);
            }
            
            if (mActivity.mSignalMeterCoordinator != null && mActivity.mSignalMeterCoordinator.useBars()) {
                mActivity.mSignalMeterCoordinator.refreshFromEngineFlags();
            } else {
                ImageView ivSignalLevel = mActivity.findViewById(R.id.ivSignalLevel);
                if (ivSignalLevel != null) {
                    int color = stereo ? android.graphics.Color.parseColor("#00E676")
                            : android.graphics.Color.parseColor("#FFD600");
                    ivSignalLevel.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        });
    }

    @Override
    public void onRdsName(final String name) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onRdsName(name);
        }
        if (mActivity.mFreqStateManager != null && mActivity.mFreqStateManager.shouldBlockTransitionalRdsName(name)) {
            Log.d(TAG, "onRdsName: bloqueado PS previo en transición (" + (name != null ? name.trim() : "") + ")");
            return;
        }
        mActivity.runOnUiThread(() -> {
            if (mActivity.mRdsManager != null) {
                mActivity.mRdsManager.onRdsName(name);
                boolean newLock = mActivity.mRdsManager.hasRdsLock();
                mActivity.mHasRdsLock = newLock;
                maybeTickRdsLock(newLock);
                // MediaSession/NowPlaying se actualiza desde RadioMediaService (source of truth).
            }
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updateRDS(name);
            }
            
            if (mActivity.mEngine != null) {
                mActivity.sendWidgetUpdate(mActivity.mEngine.getCurrentFreq(), mActivity.mCurrentBand, name);
            }
        });
    }

    @Override
    public void onRdsText(String text) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onRdsText(text);
        }
        mActivity.runOnUiThread(() -> {
            if (mActivity.mRdsManager != null) {
                mActivity.mRdsManager.onRdsText(text);
                boolean newLock = mActivity.mRdsManager.hasRdsLock();
                mActivity.mHasRdsLock = newLock;
                maybeTickRdsLock(newLock);
                // MediaSession/NowPlaying se actualiza desde RadioMediaService (source of truth).
            }
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updateRDSText(text);
            }
        });
    }

    private void maybeTickRdsLock(boolean hasLockNow) {
        long now = android.os.SystemClock.elapsedRealtime();
        boolean risingEdge = hasLockNow && !mActivity.mHadRdsLockForTick;
        mActivity.mHadRdsLockForTick = hasLockNow;
        if (!risingEdge) return;
        if (now - mActivity.mLastRdsLockTickUptimeMs < 650L) return;
        mActivity.mLastRdsLockTickUptimeMs = now;

        TextView ps = mActivity.findViewById(R.id.tvRdsName);
        TextView pty = mActivity.findViewById(R.id.tvPty);
        MainActivity.tickFlashText(ps);
        MainActivity.tickFlashText(pty);
    }

    @Override
    public void onRdsPty(String pty) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onRdsPty(pty);
        }
        if (mActivity.mRepository != null && mActivity.mEngine != null && pty != null && !pty.trim().isEmpty()) {
            try {
                mActivity.mRepository.saveRdsPty(mActivity.mEngine.getCurrentFreq(), pty);
            } catch (Exception ignored) {}
        }
        mActivity.runOnUiThread(() -> {
            if (mActivity.mRdsManager != null) {
                mActivity.mRdsManager.onRdsPty(pty);
                mActivity.mCurrentPty = mActivity.mRdsManager.getCurrentPty();
            }
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updatePTY(pty);
            }
        });
    }

    @Override
    public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onRdsStatus(afEnabled, taEnabled, tpEnabled);
        }
        mActivity.runOnUiThread(() -> {
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updateRdsStatus(afEnabled, taEnabled, tpEnabled);
            } else {
                ImageView ivAfIcon = mActivity.findViewById(R.id.ivAfIcon);
                ImageView ivTaIcon = mActivity.findViewById(R.id.ivTaIcon);
                ImageView ivTpIcon = mActivity.findViewById(R.id.ivTpIcon);
                if (ivAfIcon != null) ivAfIcon.setAlpha(afEnabled ? 1.0f : 0.2f);
                if (ivTaIcon != null) ivTaIcon.setAlpha(taEnabled ? 1.0f : 0.2f);
                if (ivTpIcon != null) ivTpIcon.setAlpha(tpEnabled ? 1.0f : 0.2f);
            }
            Log.d(TAG, "Engine RDS Status: AF=" + afEnabled + " TA=" + taEnabled + " TP=" + tpEnabled);
        });
    }

    @Override
    public void onRdsPi(String piCode) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onRdsPi(piCode);
        }
        mActivity.mCurrentPi = piCode;
        if (mActivity.mRepository != null && mActivity.mEngine != null) {
            int freq = mActivity.mEngine.getCurrentFreq();
            mActivity.mRepository.saveRdsPi(freq, piCode);
        }
        mActivity.runOnUiThread(() -> {
            if (mActivity.mRdsManager != null) {
                mActivity.mRdsManager.onRdsPi(piCode);
            }
        });
    }

    // RDSManager.RDSListener implementation
    @Override
    public void onRdsNameConfirmed(String name) {
        if (mActivity.mFreqStateManager != null && mActivity.mFreqStateManager.shouldBlockTransitionalRdsName(name)) {
            Log.d(TAG, "RDS guard activo: ignorando PS transitorio '" + name + "'");
            return;
        }
        if (mActivity.mRepository != null && mActivity.mEngine != null) {
            int freq = mActivity.mEngine.getCurrentFreq();
            mActivity.mRepository.saveRdsName(freq, name);

            if (mActivity.mScanManager != null && mActivity.mScanManager.isScanning()) {
                try { mActivity.mScanManager.onScanPsConfirmed(freq, name); } catch (Exception ignored) {}
            }

            mActivity.runOnUiThread(() -> mActivity.updateFrequencyDisplay(freq, name));

            if (mActivity.mLogoManager != null) {
                mActivity.mLogoManager.updateStationLogo(freq, mActivity.mCurrentBand, null);
            }

            if (mActivity.mPresetManager != null) {
                mActivity.mPresetManager.updateCardVisuals(-1, freq, mActivity.mCurrentBand);
            }
        }
    }

    @Override
    public void onRdsMetadataUpdated() {
        // Futuras acciones cuando cambien metadatos globales
    }

    @Override
    public int getCurrentFrequency() {
        return mActivity.mEngine != null ? mActivity.mEngine.getCurrentFreq() : 0;
    }

    @Override
    public int getCurrentBand() {
        return mActivity.mCurrentBand;
    }

    @Override
    public void onDxLocalChanged(boolean isLocal) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onDxLocalChanged(isLocal);
        }
        mActivity.runOnUiThread(() -> mActivity.syncLocDxButtonVisual(isLocal));
    }

    @Override
    public void onScanStatusChanged(boolean scanning) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onScanStatusChanged(scanning);
        }
        mActivity.runOnUiThread(() -> {
            mActivity.mIsScanning = scanning;
            if (mActivity.mScanManager != null) {
                mActivity.mScanManager.applyEngineScanState(scanning);
            }
            if (!scanning && mActivity.mScanManager != null && mActivity.mScanManager.getStationAdapter() != null) {
                Log.d(TAG, "Scan finished callback received");
            }
            
            if (!scanning && mActivity.mEngine != null) {
                if (mActivity.mScanManager != null && mActivity.mScanManager.shouldDeferOemFrequencySyncAfterSlowAutoscan()) {
                    // Autoscan lento por sobrescritura
                } else {
                    int currentFreq = mActivity.mEngine.getCurrentFreq();
                    mActivity.mLastFreq = -1;
                    mActivity.handleFrequencyChange(currentFreq);
                }
            }
        });
    }

    @Override
    public void onRawEvent(int code, String data) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onRawEvent(code, data);
        }
        if (mActivity.mEngineeringDialog != null && mActivity.mEngineeringDialog.isShowing()) {
            mActivity.mEngineeringDialog.addRdsLog(data);
        }
        if (mActivity.mQs6EngineeringDialog != null && mActivity.mQs6EngineeringDialog.isShowing()) {
            mActivity.mQs6EngineeringDialog.addRdsLog(data);
        }
    }

    @Override
    public void onSignalUpdate(int rssi, int snr) {
        if (mActivity.mSessionController != null) {
            mActivity.mSessionController.onSignalUpdate(rssi, snr);
        }
        if (mActivity.mScanManager != null && mActivity.mScanManager.isScanning()) {
            try { mActivity.mScanManager.onSignalUpdate(rssi, snr); } catch (Exception ignored) {}
        }
        mActivity.runOnUiThread(() -> {
            if (mActivity.mEngineeringDialog != null && mActivity.mEngineeringDialog.isShowing()) {
                mActivity.mEngineeringDialog.updateSignalQuality(rssi, snr);
            }
            if (mActivity.mQs6EngineeringDialog != null && mActivity.mQs6EngineeringDialog.isShowing()) {
                mActivity.mQs6EngineeringDialog.updateSignalQuality(rssi, snr);
            }
            if (mActivity.mSignalMeterCoordinator != null) {
                mActivity.mSignalMeterCoordinator.onRssiSnr(rssi, snr);
            }
        });
    }

    @Override
    public void onHwAutomationEvent(int type, boolean active) {
        mActivity.runOnUiThread(() -> {
            switch (type) {
                case 122: // Lights
                    mActivity.handleHwLightsAutomation(active);
                    break;
                case 123: // Reverse
                    mActivity.handleHwReverseMute(active);
                    break;
                case 124: // Handbrake
                    mActivity.handleHwHandbrakeSafety(active);
                    break;
                case 125: // ACC
                    mActivity.handleHwAccState(active);
                    if (mActivity.mSessionController != null) {
                        mActivity.mSessionController.onAccChanged(active);
                    }
                    break;
            }
        });
    }
}
