package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.openradiofm.service.RadioMediaService;
import com.example.openradiofm.data.source.MT8163Engine;
import com.example.openradiofm.services.FactoryRadioHijackerService;


public class LifecycleCoordinator {
    private static final String TAG = "LifecycleCoordinator";
    private final MainActivity mActivity;

    public LifecycleCoordinator(MainActivity activity) {
        this.mActivity = activity;
    }

    public void onStart() {
        MainActivity.sMainActivityStarted = true;
    }

    public void onResume() {
        boolean liveActive = mActivity.mOnlineStreamManager != null && 
                (mActivity.mOnlineStreamManager.isPlaying() || mActivity.mOnlineStreamManager.isLoading());
        Log.d(TAG, "onResume: liveActive=" + liveActive);
        
        if (liveActive) {
            return;
        }
        
        // Re-conectar si la app vuelve al frente y el servicio nativo fue matado
        if (mActivity.mRadioService == null && mActivity.mMode == MainActivity.FmMode.FM_MT8163 && mActivity.mServiceController != null) {
            // we need to access MT8163Engine.isHcnServiceBindBlockedAfterStreamEnd()
            // It might be private or we might need to import it.
            // Assuming it's accessible or we fix it.
            if (com.example.openradiofm.data.source.MT8163Engine.isHcnServiceBindBlockedAfterStreamEnd()) {
                Log.i(TAG, "onResume: bind HCN en ventana post-streaming (~12s); reintento automático al expirar");
                try {
                    Intent wakeIntent = new Intent("com.hcn.autoradio.FMRADIO_START");
                    wakeIntent.setPackage("com.hcn.autoradio");
                    wakeIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(wakeIntent);
                } catch (Exception ignored) {}
                return;
            }
            Log.w(TAG, "onResume: mRadioService nulo (posible force-stop). Reactivando servicio...");
            try {
                Intent wakeIntent = new Intent("com.hcn.autoradio.FMRADIO_START");
                wakeIntent.setPackage("com.hcn.autoradio");
                wakeIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(wakeIntent);
            } catch (Exception ignored) {}

            mActivity.requestHcnBindWithMediaSessionHandoff("onResume");
            return;
        }
        
        if (mActivity.mPlaybackManager != null) {
            mActivity.mPlaybackManager.resumeIfMutedBySystem();
            
            if (!mActivity.mPlaybackManager.isMuted() && mActivity.mEngine != null) {
                mActivity.mEngine.switchToFmAudio();
            }
        }

        if (mActivity.mEngine != null && mActivity.mScanManager != null) {
            boolean scanning = mActivity.mEngine.isScanning();
            mActivity.mIsScanning = scanning;
            mActivity.mScanManager.applyEngineScanState(scanning);
        }
    }

    public void onStop() {
        MainActivity.sMainActivityStarted = false;
        boolean liveActive = mActivity.mOnlineStreamManager != null
                && (mActivity.mOnlineStreamManager.isPlaying() || mActivity.mOnlineStreamManager.isLoading());
        
        if (!mActivity.mPowerOffRequested
                && !liveActive
                && (mActivity.mMode == MainActivity.FmMode.FM_K706 || mActivity.mMode == MainActivity.FmMode.FM_QS6)
                && mActivity.mPlaybackManager != null) {
            try {
                Intent media = new Intent(mActivity, RadioMediaService.class);
                media.setAction(RadioMediaService.ACTION_FORCE_SESSION_ACTIVE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mActivity.startForegroundService(media);
                } else {
                    mActivity.startService(media);
                }
            } catch (Exception e) {
                Log.w(TAG, "onStop: elevate RadioMediaService for steering (K706/QS6)", e);
            }
        }
        if (!liveActive && mActivity.mEngine != null && !mActivity.isChangingConfigurations()) {
            try {
                mActivity.mEngine.releaseAudioFocusOnlyForBackground();
            } catch (Exception e) {
                Log.w(TAG, "onStop: releaseAudioFocusOnlyForBackground", e);
            }
        }
    }

    public void onDestroy() {
        MainActivity.sWheelMediaBridgeActive = false;
        
        if (mActivity.mMainHandler != null) mActivity.mMainHandler.removeCallbacksAndMessages(null);
        if (mActivity.mAutoHideHandler != null) mActivity.mAutoHideHandler.removeCallbacksAndMessages(null);
        if (mActivity.mClockHandler != null) mActivity.mClockHandler.removeCallbacksAndMessages(null);

        boolean recreating = mActivity.isChangingConfigurations() || !mActivity.isFinishing();
        Log.d(TAG, "onDestroy: Limpiando recursos. recreating=" + recreating + " (isFinishing=" + mActivity.isFinishing() + ")");
        
        mActivity.stopStatusPolling();
        
        if (mActivity.mStationInfoExecutor != null) {
            try {
                mActivity.mStationInfoExecutor.shutdownNow();
            } catch (Exception ignored) {}
            mActivity.mStationInfoExecutor = null;
        }

        if (mActivity.mMediaSessionManager != null) {
            mActivity.mMediaSessionManager.disconnect();
        }

        if (mActivity.mDeviceManager != null) {
            mActivity.mDeviceManager.releaseAllResources(recreating);
        }

        try {
            if (mActivity.mHardwareManager != null) {
                mActivity.mHardwareManager.unregisterReceivers();
            }
        } catch (Exception e) {}

        if (mActivity.mHiddenPlayer != null) {
            mActivity.mHiddenPlayer.release();
            mActivity.mHiddenPlayer = null;
        }

        if (mActivity.mOnlineStreamManager != null) {
            mActivity.mOnlineStreamManager.release();
            mActivity.mOnlineStreamManager = null;
        }

        if (mActivity.mPresetManager != null) mActivity.mPresetManager.release();
        if (mActivity.mLogoManager != null) mActivity.mLogoManager.release();
        if (mActivity.mRdsManager != null) mActivity.mRdsManager.release();
        if (mActivity.mUiController != null) mActivity.mUiController.release();
    }

    public void prepareForPowerOff() {
        try {
            com.example.openradiofm.services.FactoryRadioHijackerService.markPowerOffForHijack(mActivity);
        } catch (Exception ignored) {}
        mActivity.mShutdownPersistGuardUntilMs = android.os.SystemClock.elapsedRealtime() + 9000L;
        mActivity.mPowerOffRequested = true;

        try {
            if (mActivity.mPlaybackManager != null) {
                mActivity.mPlaybackManager.setMute(true);
            }
            if (mActivity.mUiController != null) {
                mActivity.mUiController.updateMute(true);
            }
        } catch (Exception ignored) {}

        if (mActivity.mPrefs != null && mActivity.mLastFreq > 0) {
            mActivity.mPrefs.edit()
                    .putInt("pref_last_freq", mActivity.mLastFreq)
                    .putInt("pref_last_band", mActivity.mCurrentBand)
                    .apply();
        }
    }
}
