package com.example.openradiofm.ui.main;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.example.openradiofm.R;
import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.ui.theme.ThemeManager;

public class StatusRefreshCoordinator {
    private static final String TAG = "StatusRefreshCoordinator";
    private static final long DATA_ACTIVITY_UI_INTERVAL_MS = 1000;
    private static final long NIGHT_MODE_CHECK_INTERVAL_MS = 60000;

    private final RadioUiHost mHost;
    private long mLastDataActivityUiTime = 0;
    private long mLastNightModeCheckTime = 0;

    public StatusRefreshCoordinator(RadioUiHost host) {
        this.mHost = host;
    }

    public void refreshRadioStatus() {
        if (mHost.getRadioEngine() == null) return;

        long now = System.currentTimeMillis();
        final boolean shouldUpdateDataUi = (now - mLastDataActivityUiTime) >= DATA_ACTIVITY_UI_INTERVAL_MS;
        final boolean shouldCheckNight = (now - mLastNightModeCheckTime) >= NIGHT_MODE_CHECK_INTERVAL_MS;
        if (shouldUpdateDataUi) mLastDataActivityUiTime = now;
        if (shouldCheckNight) mLastNightModeCheckTime = now;

        if (shouldUpdateDataUi || shouldCheckNight) {
            mHost.runOnHostUiThread(() -> {
                if (shouldUpdateDataUi) mHost.updateDataActivityUI();
                if (shouldCheckNight) mHost.checkAndApplyNightMode();
            });
        }

        boolean isStreaming = mHost.getOnlineStreamManager() != null
                && (mHost.getOnlineStreamManager().isPlaying() || mHost.getOnlineStreamManager().isLoading());

        if (mHost.getPlaybackManager() != null && mHost.getRadioEngine() != null &&
            ("MTK8259_8667".equals(mHost.getRadioEngine().getEngineName()) ||
             ("MT8163".equals(mHost.getRadioEngine().getEngineName())
                     && (mHost.getRadioPresets().getBoolean("pref_mt8163_global_stream_mute", true)
                     || mHost.getRadioPresets().getBoolean("pref_mt8163_mcu_direct", false))))) {
            AudioManager am = (AudioManager) mHost.getHostContext().getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                boolean isSystemMuted;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    boolean muteFlag = am.isStreamMute(AudioManager.STREAM_MUSIC);
                    boolean volumeZero = am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                    isSystemMuted = muteFlag || volumeZero;
                } else {
                    isSystemMuted = am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                }

                if (!isSystemMuted && mHost.getPlaybackManager().isMuted()) {
                    if (mHost.getPlaybackManager().isMt8163StreamVolumeMuteRejectedByOem()) {
                        // STREAM_MUSIC no refleja mute (app sin permiso); no forzar unmute espurio.
                    } else if (mHost.getPlaybackManager().shouldSuppressMt8163StreamMuteSync()) {
                        // Tras inject VOLUME_MUTE el HU puede "desenganchar" STREAM_MUSIC del mute real;
                        // no interpretar como "usuario desmuteó por volumen" con un solo toque en la app.
                    } else {
                        Log.d(TAG, "Mute sync: System unmuted, updating UI/Engine");
                        mHost.getPlaybackManager().setMute(false);
                    }
                }
            }
        }

        int freq = isStreaming ? mHost.getLastFreqKhz() : mHost.getRadioEngine().getCurrentFreq();
        if (freq <= 0) return;

        int band = isStreaming ? mHost.getUiCurrentBand() : mHost.getRadioEngine().getCurrentBand();
        boolean isStereo = isStreaming || mHost.getRadioEngine().isStereo();
        boolean isLocal = !isStreaming && mHost.getRadioEngine().isDxLocal();

        boolean stateChanged = (mHost.getFreqStateManager() != null)
                ? mHost.getFreqStateManager().shouldFullRefresh(freq, band) : true;

        if (!stateChanged) {
            mHost.runOnHostUiThread(() -> {
                mHost.refreshStereoIndicatorUi(isStereo);
                mHost.syncLocDxButtonVisual(isLocal);

                ImageView ivSignalLevel = (ImageView) mHost.findHostViewById(R.id.ivSignalLevel);
                if (ivSignalLevel != null && (mHost.getSignalMeterCoordinator() == null || !mHost.getSignalMeterCoordinator().useBars())) {
                    int sigColor = isStereo ? android.graphics.Color.parseColor("#00E676") : android.graphics.Color.parseColor("#FFD600");
                    ivSignalLevel.setColorFilter(sigColor, android.graphics.PorterDuff.Mode.SRC_IN);
                }
            });
            return;
        }

        if (band != mHost.getUiCurrentBand()) {
            String logMsg = "Band shift detected: " + mHost.getUiCurrentBand() + " -> " + band;
            mHost.setUiCurrentBand(band);
            Log.d(TAG, logMsg);
            if (mHost.getPresetManager() != null) {
                mHost.getPresetManager().refreshPresetsCache(band);
                mHost.runOnHostUiThread(() -> mHost.getPresetManager().refreshButtons(band));
            }
        }

        if (freq != mHost.getLastFreqKhz()) {
            Log.d(TAG, "Ultima frecuencia guardada: " + freq);
        }

        final int fFreq = freq;
        final int fBand = band;
        final boolean fIsAm = (band == 3 || band == 4); // BAND_AM1=3, BAND_AM2=4
        final boolean fIsLocal = isLocal;
        final boolean fIsStreaming = isStreaming;

        final int seq = mHost.hostNextStationInfoSequence();
        mHost.setLastStationInfoRequestedSeq(seq);
        if (mHost.getStationInfoExecutor() == null) {
            mHost.setStationInfoExecutor(java.util.concurrent.Executors.newSingleThreadExecutor());
        }

        /*
         * Orden crítico: antes se hacía reset( RDS ) en runOnUiThread y a la vez execute() en el
         * executor; el hilo de fondo podía llamar getConfirmedName() ANTES del reset y volver a
         * pintar el PS antiguo en tvFrequency (V3 muestra el PS ahí) — p. ej. "ANTENA 2" pegado al zapping.
         * Encadenamos el trabajo en background después del reset y forzamos MHz hasta nuevo PS fiable.
         */
        mHost.runOnHostUiThread(() -> {
            if (mHost.getRdsManager() != null) mHost.getRdsManager().reset(true);
            mHost.clearStationLogoUi();
            if (mHost.getUiController() != null) {
                mHost.getUiController().updateRDS("");
                mHost.getUiController().updateFrequency(fFreq, "", fIsAm);
            } else {
                mHost.updateFrequencyDisplay(fFreq, "");
            }

            mHost.getStationInfoExecutor().execute(() -> {
            if (mHost.isHostFinishing() || mHost.isHostDestroyed()) return;
            if (seq != mHost.getLastStationInfoRequestedSeq()) return;
            final boolean qs6TransitionActive = mHost.isQs6TransitionGuardActive();

            RadioStation station = null;
            if (mHost.getRadioRepository() != null && !mHost.isUiScanning()) {
                String livePs = null;
                if (!qs6TransitionActive && !fIsStreaming && mHost.getRdsManager() != null && mHost.getRadioEngine() != null && fFreq == mHost.getRadioEngine().getCurrentFreq()) {
                    String cn = mHost.getRdsManager().getConfirmedName();
                    if (cn != null && !cn.trim().isEmpty()) {
                        livePs = cn.trim();
                    }
                }
                station = mHost.getRadioRepository().getStationInfo(fFreq, null, livePs);
            }
            if (seq != mHost.getLastStationInfoRequestedSeq()) return;

            final String rdsNameRaw = (station != null) ? station.getName() : "";
            final boolean hasStableCachedName = mHost.hasStableCachedNameForFrequency(fFreq);
            final String rdsName = (qs6TransitionActive && !hasStableCachedName) ? "" : rdsNameRaw;
            final String stationPty = (station != null) ? station.getPty() : null;
            mHost.setLastPs(rdsName);
            final String repoLogoForUi = (station != null) ? station.getLogoUrl() : null;

            mHost.runOnHostUiThread(() -> {
                if (mHost.isHostFinishing() || mHost.isHostDestroyed()) return;
                if (seq != mHost.getLastStationInfoRequestedSeq()) return;

                boolean isNight = (mHost.getThemeManager() != null && mHost.getThemeManager().getActiveSkin() == ThemeManager.Skin.NIGHT_MODE);

                if (mHost.getUiController() != null) {
                    mHost.getUiController().updateFrequency(fFreq, rdsName, fIsAm);
                    mHost.getUiController().applySkin(isNight);
                    mHost.getUiController().updateBandIndicator(fBand);

                    if ((mHost.getCurrentPty() == null || mHost.getCurrentPty().trim().isEmpty())
                            && stationPty != null && !stationPty.trim().isEmpty()
                            && mHost.getRdsManager() != null) {
                        mHost.getRdsManager().onRdsPty(stationPty);
                        mHost.setCurrentPty(stationPty);
                        mHost.getUiController().updatePTY(stationPty);
                    }

                    if (mHost.getLogoManager() != null) {
                        boolean qs6GuardActive = mHost.isQs6TransitionGuardActive();
                        String cachedLogo = mHost.getLogoCachePerBand().get(fBand + "_" + fFreq);
                        String repoLogo = repoLogoForUi;
                        String preferredLogo = (cachedLogo != null && !cachedLogo.trim().isEmpty()) ? cachedLogo : ((repoLogo != null && !repoLogo.trim().isEmpty()) ? repoLogo : null);

                        if (preferredLogo != null) {
                            mHost.getLogoManager().updateStationLogo(fFreq, fBand, preferredLogo);
                        } else if (qs6GuardActive) {
                            mHost.clearStationLogoUi();
                        } else {
                            mHost.getLogoManager().updateStationLogo(fFreq, fBand, null);
                        }
                    }

                    boolean isFav = mHost.isStationMemorized(fFreq);
                    int pIndex = mHost.getPresetIndex(fFreq);
                    mHost.getUiController().updateFavoriteIndicator(isFav, pIndex, isNight);

                    if (isNight && mHost.getNightModeManager() != null) {
                        mHost.getNightModeManager().applyNightModeColors(mHost.getLastFreqKhz());
                    }
                    mHost.updateDataActivityUI();
                } else {
                    int nightBlue = mHost.getHostContext().getResources().getColor(R.color.night_blue_primary, null);
                    android.widget.TextView ivUnitLabel = (android.widget.TextView) mHost.findHostViewById(R.id.ivUnitLabel);
                    android.widget.TextView tvFrequency = (android.widget.TextView) mHost.findHostViewById(R.id.tvFrequency);

                    if (isNight) {
                        MainActivity.setTextColorIfChanged(ivUnitLabel, nightBlue);
                        MainActivity.setTextColorIfChanged(tvFrequency, nightBlue);
                    } else {
                        boolean isLight = mHost.getThemeManager() != null && mHost.getThemeManager().getActiveSkin() == ThemeManager.Skin.CLEAR;
                        MainActivity.setTextColorIfChanged(ivUnitLabel, isLight ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                        MainActivity.setTextColorIfChanged(tvFrequency, android.graphics.Color.WHITE);
                    }
                    mHost.updateFrequencyDisplay(fFreq, rdsName);
                    mHost.updateBandImage(fBand);
                    if (isNight && mHost.getNightModeManager() != null) {
                        mHost.getNightModeManager().applyNightModeColors(mHost.getLastFreqKhz());
                    }
                    mHost.updateDataActivityUI();
                }

                if (mHost.getMediaSessionManager() != null) {
                    float freqDisplay = fFreq / 1000.0f;
                    String freqStr = String.format(java.util.Locale.US, "%.1f MHz", freqDisplay);
                    mHost.getMediaSessionManager().updateMetadata(rdsName, freqStr, null);
                }

                mHost.syncLocDxButtonVisual(fIsLocal);
                mHost.sendWidgetUpdate(fFreq, fBand, rdsName);
                mHost.refreshStereoIndicatorUi(fIsStreaming || (mHost.getRadioEngine() != null && mHost.getRadioEngine().isStereo()));
            });
        });
        });
    }
}
