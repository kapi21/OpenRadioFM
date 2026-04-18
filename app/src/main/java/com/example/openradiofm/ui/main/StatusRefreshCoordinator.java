package com.example.openradiofm.ui.main;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.openradiofm.R;
import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.ui.theme.ThemeManager;

public class StatusRefreshCoordinator {
    private static final String TAG = "StatusRefreshCoordinator";
    private static final long DATA_ACTIVITY_UI_INTERVAL_MS = 1000;
    private static final long NIGHT_MODE_CHECK_INTERVAL_MS = 60000;

    private final MainActivity mActivity;
    private long mLastDataActivityUiTime = 0;
    private long mLastNightModeCheckTime = 0;

    public StatusRefreshCoordinator(MainActivity activity) {
        this.mActivity = activity;
    }

    public void refreshRadioStatus() {
        if (mActivity.mEngine == null) return;

        long now = System.currentTimeMillis();
        final boolean shouldUpdateDataUi = (now - mLastDataActivityUiTime) >= DATA_ACTIVITY_UI_INTERVAL_MS;
        final boolean shouldCheckNight = (now - mLastNightModeCheckTime) >= NIGHT_MODE_CHECK_INTERVAL_MS;
        if (shouldUpdateDataUi) mLastDataActivityUiTime = now;
        if (shouldCheckNight) mLastNightModeCheckTime = now;
        
        if (shouldUpdateDataUi || shouldCheckNight) {
            mActivity.runOnUiThread(() -> {
                if (shouldUpdateDataUi) mActivity.updateDataActivityUI();
                if (shouldCheckNight) mActivity.checkAndApplyNightMode();
            });
        }

        boolean isStreaming = mActivity.getOnlineStreamManager() != null
                && (mActivity.getOnlineStreamManager().isPlaying() || mActivity.getOnlineStreamManager().isLoading());

        if (mActivity.mPlaybackManager != null && mActivity.mEngine != null &&
            ("MTK8259_8667".equals(mActivity.mEngine.getEngineName()) ||
             ("MT8163".equals(mActivity.mEngine.getEngineName())
                     && (mActivity.mPrefs.getBoolean("pref_mt8163_global_stream_mute", true)
                     || mActivity.mPrefs.getBoolean("pref_mt8163_mcu_direct", false))))) {
            AudioManager am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                boolean isSystemMuted;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    boolean muteFlag = am.isStreamMute(AudioManager.STREAM_MUSIC);
                    boolean volumeZero = am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                    isSystemMuted = muteFlag || volumeZero;
                } else {
                    isSystemMuted = am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                }
                
                if (!isSystemMuted && mActivity.mPlaybackManager.isMuted()) {
                    if (mActivity.mPlaybackManager.isMt8163StreamVolumeMuteRejectedByOem()) {
                        // STREAM_MUSIC no refleja mute (app sin permiso); no forzar unmute espurio.
                    } else if (mActivity.mPlaybackManager.shouldSuppressMt8163StreamMuteSync()) {
                        // Tras inject VOLUME_MUTE el HU puede “desenganchar” STREAM_MUSIC del mute real;
                        // no interpretar como “usuario desmuteó por volumen” con un solo toque en la app.
                    } else {
                        Log.d(TAG, "Mute sync: System unmuted, updating UI/Engine");
                        mActivity.mPlaybackManager.setMute(false);
                    }
                }
            }
        }

        int freq = isStreaming ? mActivity.mLastFreq : mActivity.mEngine.getCurrentFreq();
        if (freq <= 0) return;

        int band = isStreaming ? mActivity.mCurrentBand : mActivity.mEngine.getCurrentBand();
        boolean isStereo = isStreaming || mActivity.mEngine.isStereo();
        boolean isLocal = !isStreaming && mActivity.mEngine.isDxLocal();

        boolean stateChanged = (mActivity.mFreqStateManager != null)
                ? mActivity.mFreqStateManager.shouldFullRefresh(freq, band) : true;

        if (!stateChanged) {
            mActivity.runOnUiThread(() -> {
                mActivity.refreshStereoIndicatorUi(isStereo);
                mActivity.syncLocDxButtonVisual(isLocal);
                
                ImageView ivSignalLevel = mActivity.findViewById(R.id.ivSignalLevel);
                if (ivSignalLevel != null && (mActivity.mSignalMeterCoordinator == null || !mActivity.mSignalMeterCoordinator.useBars())) {
                    int sigColor = isStereo ? android.graphics.Color.parseColor("#00E676") : android.graphics.Color.parseColor("#FFD600");
                    ivSignalLevel.setColorFilter(sigColor, android.graphics.PorterDuff.Mode.SRC_IN);
                }
            });
            return;
        }

        mActivity.runOnUiThread(() -> {
            if (mActivity.mRdsManager != null) mActivity.mRdsManager.reset(true);
            mActivity.clearStationLogoUi();
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updateRDS("");
            }
        });

        if (band != mActivity.mCurrentBand) {
            String logMsg = "Band shift detected: " + mActivity.mCurrentBand + " -> " + band;
            mActivity.mCurrentBand = band;
            Log.d(TAG, logMsg);
            if (mActivity.mPresetManager != null) {
                mActivity.mPresetManager.refreshPresetsCache(band);
                mActivity.runOnUiThread(() -> mActivity.mPresetManager.refreshButtons(band));
            }
        }

        if (freq != mActivity.mLastFreq) {
            Log.d(TAG, "Ultima frecuencia guardada: " + freq);
        }

        final int fFreq = freq;
        final int fBand = band;
        final boolean fIsAm = (band == 3 || band == 4); // BAND_AM1=3, BAND_AM2=4
        final boolean fIsLocal = isLocal;
        final boolean fIsStreaming = isStreaming;

        final int seq = mActivity.mStationInfoSeq.incrementAndGet();
        mActivity.mLastStationInfoRequestedSeq = seq;
        if (mActivity.mStationInfoExecutor == null) {
            mActivity.mStationInfoExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
        }
        mActivity.mStationInfoExecutor.execute(() -> {
            if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
            if (seq != mActivity.mLastStationInfoRequestedSeq) return;
            final boolean qs6TransitionActive = mActivity.isQs6TransitionGuardActive();

            RadioStation station = null;
            if (mActivity.mRepository != null && !mActivity.mIsScanning) {
                String livePs = null;
                if (!qs6TransitionActive && !fIsStreaming && mActivity.mRdsManager != null && mActivity.mEngine != null && fFreq == mActivity.mEngine.getCurrentFreq()) {
                    String cn = mActivity.mRdsManager.getConfirmedName();
                    if (cn != null && !cn.trim().isEmpty()) {
                        livePs = cn.trim();
                    }
                }
                station = mActivity.mRepository.getStationInfo(fFreq, null, livePs);
            }
            if (seq != mActivity.mLastStationInfoRequestedSeq) return;

            final String rdsNameRaw = (station != null) ? station.getName() : "";
            final boolean hasStableCachedName = mActivity.hasStableCachedNameForFrequency(fFreq);
            final String rdsName = (qs6TransitionActive && !hasStableCachedName) ? "" : rdsNameRaw;
            final String stationPty = (station != null) ? station.getPty() : null;
            mActivity.mLastPs = rdsName;
            final String repoLogoForUi = (station != null) ? station.getLogoUrl() : null;

            mActivity.runOnUiThread(() -> {
                if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                if (seq != mActivity.mLastStationInfoRequestedSeq) return;

                boolean isNight = (mActivity.mThemeManager != null && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.NIGHT_MODE);

                if (mActivity.mUiController != null) {
                    mActivity.mUiController.updateFrequency(fFreq, rdsName, fIsAm);
                    mActivity.mUiController.applySkin(isNight);
                    mActivity.mUiController.updateBandIndicator(fBand);
                    
                    if ((mActivity.mCurrentPty == null || mActivity.mCurrentPty.trim().isEmpty())
                            && stationPty != null && !stationPty.trim().isEmpty()
                            && mActivity.mRdsManager != null) {
                        mActivity.mRdsManager.onRdsPty(stationPty);
                        mActivity.mCurrentPty = stationPty;
                        mActivity.mUiController.updatePTY(stationPty);
                    }

                    if (mActivity.mLogoManager != null) {
                        boolean qs6GuardActive = mActivity.isQs6TransitionGuardActive();
                        String cachedLogo = mActivity.mLogoCachePerBand.get(fBand + "_" + fFreq);
                        String repoLogo = repoLogoForUi;
                        String preferredLogo = (cachedLogo != null && !cachedLogo.trim().isEmpty()) ? cachedLogo : ((repoLogo != null && !repoLogo.trim().isEmpty()) ? repoLogo : null);

                        if (preferredLogo != null) {
                            mActivity.mLogoManager.updateStationLogo(fFreq, fBand, preferredLogo);
                        } else if (qs6GuardActive) {
                            mActivity.clearStationLogoUi();
                        } else {
                            mActivity.mLogoManager.updateStationLogo(fFreq, fBand, null);
                        }
                    }

                    boolean isFav = mActivity.isStationMemorized(fFreq);
                    int pIndex = mActivity.getPresetIndex(fFreq);
                    mActivity.mUiController.updateFavoriteIndicator(isFav, pIndex, isNight);
                    
                    if (isNight && mActivity.mNightModeManager != null) {
                        mActivity.mNightModeManager.applyNightModeColors(mActivity.mLastFreq);
                    }
                    mActivity.updateDataActivityUI();
                } else {
                    int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);
                    android.widget.TextView ivUnitLabel = mActivity.findViewById(R.id.ivUnitLabel);
                    android.widget.TextView tvFrequency = mActivity.findViewById(R.id.tvFrequency);
                    
                    if (isNight) {
                        MainActivity.setTextColorIfChanged(ivUnitLabel, nightBlue);
                        MainActivity.setTextColorIfChanged(tvFrequency, nightBlue);
                    } else {
                        boolean isLight = mActivity.mThemeManager != null && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR;
                        MainActivity.setTextColorIfChanged(ivUnitLabel, isLight ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                        MainActivity.setTextColorIfChanged(tvFrequency, android.graphics.Color.WHITE);
                    }
                    mActivity.updateFrequencyDisplay(fFreq, rdsName);
                    mActivity.updateBandImage(fBand);
                    if (isNight && mActivity.mNightModeManager != null) {
                        mActivity.mNightModeManager.applyNightModeColors(mActivity.mLastFreq);
                    }
                    mActivity.updateDataActivityUI();
                }

                if (mActivity.mMediaSessionManager != null) {
                    float freqDisplay = fFreq / 1000.0f;
                    String freqStr = String.format(java.util.Locale.US, "%.1f MHz", freqDisplay);
                    mActivity.mMediaSessionManager.updateMetadata(rdsName, freqStr, null);
                }

                mActivity.syncLocDxButtonVisual(fIsLocal);
                mActivity.sendWidgetUpdate(fFreq, fBand, rdsName);
                mActivity.refreshStereoIndicatorUi(fIsStreaming || (mActivity.mEngine != null && mActivity.mEngine.isStereo()));
            });
        });
    }
}
