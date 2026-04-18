package com.example.openradiofm.ui.main;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.RadioEngineCallback;

public class EngineCallbackCoordinator implements RadioEngineCallback, RDSManager.RDSListener {
    private static final String TAG = "EngineCallbackCoordinator";
    private final RadioUiHost mHost;

    public EngineCallbackCoordinator(RadioUiHost host) {
        this.mHost = host;
    }

    @Override
    public void onFrequencyChanged(int freqKhz) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onFrequencyChanged(freqKhz);
        }
        mHost.handleFrequencyChange(freqKhz);
        if (mHost.getScanManager() != null && mHost.getScanManager().isScanning()) {
            try { mHost.getScanManager().onScanFrequencyChanged(freqKhz); } catch (Exception ignored) {}
        }
        mHost.runOnHostUiThread(() -> {
            if (mHost.getUiController() != null) {
                mHost.getUiController().updateFrequency(freqKhz, null, mHost.getUiCurrentBand() >= 3);
            } else {
                mHost.updateFrequencyDisplay(freqKhz, null);
            }
            if (mHost.getSkinCoordinator() != null) mHost.getSkinCoordinator().reapplyVisualStateForCurrentSkin();
        });
    }

    private static String unitShortText(int band) {
        return (band == 3 || band == 4) ? "kHz" : "MHz";
    }

    @Override
    public void onBandChanged(int band) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onBandChanged(band);
        }
        mHost.runOnHostUiThread(() -> {
            mHost.incrementLogoUiGeneration();
            mHost.clearStationLogoUi();
            mHost.setUiCurrentBand(band);
            mHost.setLastStoredBand(band);
            mHost.persistLastBandPreference(band);
            if (mHost.getPresetManager() != null) {
                mHost.getPresetManager().refreshPresetsCache(band);
                mHost.getPresetManager().refreshButtons(band);
            }
            if (mHost.getUiController() != null) {
                mHost.getUiController().updateBandIndicator(band);
            } else {
                mHost.updateBandImage(band);
            }

            TextView ivUnitLabel = (TextView) mHost.findHostViewById(R.id.ivUnitLabel);
            if (ivUnitLabel != null) {
                MainActivity.setTextIfChanged(ivUnitLabel, unitShortText(band));
            }

            if (mHost.getSkinCoordinator() != null) mHost.getSkinCoordinator().reapplyVisualStateForCurrentSkin();

            if (mHost.getRadioEngine() != null) {
                mHost.sendWidgetUpdate(mHost.getRadioEngine().getCurrentFreq(), band, mHost.getLastPs());
            }
        });
    }

    @Override
    public void onStereoChanged(boolean stereo) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onStereoChanged(stereo);
        }
        mHost.runOnHostUiThread(() -> {
            if (mHost.getUiController() != null) {
                mHost.getUiController().updateStereo(stereo);
            } else {
                mHost.refreshStereoIndicatorUi(null);
            }

            if (mHost.getSignalMeterCoordinator() != null && mHost.getSignalMeterCoordinator().useBars()) {
                mHost.getSignalMeterCoordinator().refreshFromEngineFlags();
            } else {
                ImageView ivSignalLevel = (ImageView) mHost.findHostViewById(R.id.ivSignalLevel);
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
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onRdsName(name);
        }
        if (mHost.getFreqStateManager() != null && mHost.getFreqStateManager().shouldBlockTransitionalRdsName(name)) {
            Log.d(TAG, "onRdsName: bloqueado PS previo en transición (" + (name != null ? name.trim() : "") + ")");
            return;
        }
        mHost.runOnHostUiThread(() -> {
            if (mHost.getRdsManager() != null) {
                mHost.getRdsManager().onRdsName(name);
                boolean newLock = mHost.getRdsManager().hasRdsLock();
                mHost.setHasRdsLock(newLock);
                maybeTickRdsLock(newLock);
                // MediaSession/NowPlaying se actualiza desde RadioMediaService (source of truth).
            }
            if (mHost.getUiController() != null) {
                mHost.getUiController().updateRDS(name);
            }

            if (mHost.getRadioEngine() != null) {
                mHost.sendWidgetUpdate(mHost.getRadioEngine().getCurrentFreq(), mHost.getUiCurrentBand(), name);
            }
        });
    }

    @Override
    public void onRdsText(String text) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onRdsText(text);
        }
        mHost.runOnHostUiThread(() -> {
            if (mHost.getRdsManager() != null) {
                mHost.getRdsManager().onRdsText(text);
                boolean newLock = mHost.getRdsManager().hasRdsLock();
                mHost.setHasRdsLock(newLock);
                maybeTickRdsLock(newLock);
                // MediaSession/NowPlaying se actualiza desde RadioMediaService (source of truth).
            }
            if (mHost.getUiController() != null) {
                mHost.getUiController().updateRDSText(text);
            }
        });
    }

    private void maybeTickRdsLock(boolean hasLockNow) {
        long now = android.os.SystemClock.elapsedRealtime();
        boolean risingEdge = hasLockNow && !mHost.getHadRdsLockForTick();
        mHost.setHadRdsLockForTick(hasLockNow);
        if (!risingEdge) return;
        if (now - mHost.getLastRdsLockTickUptimeMs() < 650L) return;
        mHost.setLastRdsLockTickUptimeMs(now);

        TextView ps = (TextView) mHost.findHostViewById(R.id.tvRdsName);
        TextView pty = (TextView) mHost.findHostViewById(R.id.tvPty);
        MainActivity.tickFlashText(ps);
        MainActivity.tickFlashText(pty);
    }

    @Override
    public void onRdsPty(String pty) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onRdsPty(pty);
        }
        if (mHost.getRadioRepository() != null && mHost.getRadioEngine() != null && pty != null && !pty.trim().isEmpty()) {
            try {
                mHost.getRadioRepository().saveRdsPty(mHost.getRadioEngine().getCurrentFreq(), pty);
            } catch (Exception ignored) {}
        }
        mHost.runOnHostUiThread(() -> {
            if (mHost.getRdsManager() != null) {
                mHost.getRdsManager().onRdsPty(pty);
                mHost.setCurrentPty(mHost.getRdsManager().getCurrentPty());
            }
            if (mHost.getUiController() != null) {
                mHost.getUiController().updatePTY(pty);
            }
        });
    }

    @Override
    public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onRdsStatus(afEnabled, taEnabled, tpEnabled);
        }
        mHost.runOnHostUiThread(() -> {
            if (mHost.getUiController() != null) {
                mHost.getUiController().updateRdsStatus(afEnabled, taEnabled, tpEnabled);
            } else {
                ImageView ivAfIcon = (ImageView) mHost.findHostViewById(R.id.ivAfIcon);
                ImageView ivTaIcon = (ImageView) mHost.findHostViewById(R.id.ivTaIcon);
                ImageView ivTpIcon = (ImageView) mHost.findHostViewById(R.id.ivTpIcon);
                if (ivAfIcon != null) ivAfIcon.setAlpha(afEnabled ? 1.0f : 0.2f);
                if (ivTaIcon != null) ivTaIcon.setAlpha(taEnabled ? 1.0f : 0.2f);
                if (ivTpIcon != null) ivTpIcon.setAlpha(tpEnabled ? 1.0f : 0.2f);
            }
            Log.d(TAG, "Engine RDS Status: AF=" + afEnabled + " TA=" + taEnabled + " TP=" + tpEnabled);
        });
    }

    @Override
    public void onRdsPi(String piCode) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onRdsPi(piCode);
        }
        mHost.setCurrentPi(piCode);
        if (mHost.getRadioRepository() != null && mHost.getRadioEngine() != null) {
            int freq = mHost.getRadioEngine().getCurrentFreq();
            mHost.getRadioRepository().saveRdsPi(freq, piCode);
        }
        mHost.runOnHostUiThread(() -> {
            if (mHost.getRdsManager() != null) {
                mHost.getRdsManager().onRdsPi(piCode);
            }
        });
    }

    // RDSManager.RDSListener implementation
    @Override
    public void onRdsNameConfirmed(String name) {
        if (mHost.getFreqStateManager() != null && mHost.getFreqStateManager().shouldBlockTransitionalRdsName(name)) {
            Log.d(TAG, "RDS guard activo: ignorando PS transitorio '" + name + "'");
            return;
        }
        if (mHost.getRadioRepository() != null && mHost.getRadioEngine() != null) {
            int freq = mHost.getRadioEngine().getCurrentFreq();
            mHost.getRadioRepository().saveRdsName(freq, name);

            if (mHost.getScanManager() != null && mHost.getScanManager().isScanning()) {
                try { mHost.getScanManager().onScanPsConfirmed(freq, name); } catch (Exception ignored) {}
            }

            mHost.runOnHostUiThread(() -> mHost.updateFrequencyDisplay(freq, name));

            if (mHost.getLogoManager() != null) {
                mHost.getLogoManager().updateStationLogo(freq, mHost.getUiCurrentBand(), null);
            }

            if (mHost.getPresetManager() != null) {
                mHost.getPresetManager().updateCardVisuals(-1, freq, mHost.getUiCurrentBand());
            }
        }
    }

    @Override
    public void onRdsMetadataUpdated() {
        // Futuras acciones cuando cambien metadatos globales
    }

    @Override
    public int getCurrentFrequency() {
        return mHost.getRadioEngine() != null ? mHost.getRadioEngine().getCurrentFreq() : 0;
    }

    @Override
    public int getCurrentBand() {
        return mHost.getUiCurrentBand();
    }

    @Override
    public void onDxLocalChanged(boolean isLocal) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onDxLocalChanged(isLocal);
        }
        mHost.runOnHostUiThread(() -> mHost.syncLocDxButtonVisual(isLocal));
    }

    @Override
    public void onScanStatusChanged(boolean scanning) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onScanStatusChanged(scanning);
        }
        mHost.runOnHostUiThread(() -> {
            final boolean uiScanning = (mHost.getScanManager() != null)
                    ? mHost.getScanManager().adjustEngineScanningForAutoScanUi(scanning)
                    : scanning;
            mHost.setUiScanningFlag(uiScanning);
            if (mHost.getScanManager() != null) {
                mHost.getScanManager().applyEngineScanState(uiScanning);
            }
            if (!scanning && mHost.getScanManager() != null && mHost.getScanManager().getStationAdapter() != null) {
                Log.d(TAG, "Scan finished callback received");
            }

            if (!scanning && mHost.getRadioEngine() != null) {
                if (mHost.getScanManager() != null && mHost.getScanManager().shouldDeferOemFrequencySyncAfterSlowAutoscan()) {
                    // Autoscan lento por sobrescritura
                } else {
                    int currentFreq = mHost.getRadioEngine().getCurrentFreq();
                    mHost.setLastFreqKhz(-1);
                    mHost.handleFrequencyChange(currentFreq);
                }
            }
        });
    }

    @Override
    public void onRawEvent(int code, String data) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onRawEvent(code, data);
        }
        if (mHost.getK706EngineeringDialog() != null && mHost.getK706EngineeringDialog().isShowing()) {
            mHost.getK706EngineeringDialog().addRdsLog(data);
        }
        if (mHost.getQs6EngineeringDialog() != null && mHost.getQs6EngineeringDialog().isShowing()) {
            mHost.getQs6EngineeringDialog().addRdsLog(data);
        }
    }

    @Override
    public void onSignalUpdate(int rssi, int snr) {
        if (mHost.getRadioSessionController() != null) {
            mHost.getRadioSessionController().onSignalUpdate(rssi, snr);
        }
        if (mHost.getScanManager() != null && mHost.getScanManager().isScanning()) {
            try { mHost.getScanManager().onSignalUpdate(rssi, snr); } catch (Exception ignored) {}
        }
        mHost.runOnHostUiThread(() -> {
            if (mHost.getK706EngineeringDialog() != null && mHost.getK706EngineeringDialog().isShowing()) {
                mHost.getK706EngineeringDialog().updateSignalQuality(rssi, snr);
            }
            if (mHost.getQs6EngineeringDialog() != null && mHost.getQs6EngineeringDialog().isShowing()) {
                mHost.getQs6EngineeringDialog().updateSignalQuality(rssi, snr);
            }
            if (mHost.getSignalMeterCoordinator() != null) {
                mHost.getSignalMeterCoordinator().onRssiSnr(rssi, snr);
            }
        });
    }

    @Override
    public void onHwAutomationEvent(int type, boolean active) {
        mHost.runOnHostUiThread(() -> {
            switch (type) {
                case 122: // Lights
                    mHost.handleHwLightsAutomation(active);
                    break;
                case 123: // Reverse
                    mHost.handleHwReverseMute(active);
                    break;
                case 124: // Handbrake
                    mHost.handleHwHandbrakeSafety(active);
                    break;
                case 125: // ACC
                    mHost.handleHwAccState(active);
                    if (mHost.getRadioSessionController() != null) {
                        mHost.getRadioSessionController().onAccChanged(active);
                    }
                    break;
            }
        });
    }
}
