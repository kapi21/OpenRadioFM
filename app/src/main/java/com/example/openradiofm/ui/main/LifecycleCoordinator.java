package com.example.openradiofm.ui.main;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.openradiofm.service.RadioMediaService;
import com.example.openradiofm.data.source.MT8163Engine;
import com.example.openradiofm.services.FactoryRadioHijackerService;
import com.example.openradiofm.utils.RadioActivityFileLogger;


public class LifecycleCoordinator {
    private static final String TAG = "LifecycleCoordinator";
    private final RadioUiHost mHost;

    public LifecycleCoordinator(RadioUiHost host) {
        this.mHost = host;
    }

    public void onStart() {
    }

    public void onResume() {
        MainActivity.sMainActivityResumed = true;
        RadioActivityFileLogger.noteMainActivityResumed(true);
        OnlineStreamManager osm = mHost.getOnlineStreamManager();
        boolean liveActive = osm != null && (osm.isPlaying() || osm.isLoading());
        Log.d(TAG, "onResume: liveActive=" + liveActive);

        if (liveActive) {
            return;
        }

        // Re-conectar si la app vuelve al frente y el servicio nativo fue matado
        if (mHost.getRadioService() == null && mHost.getFmMode() == MainActivity.FmMode.FM_MT8163
                && mHost.getServiceController() != null) {
            // Cold start: si el bind a HCN está en vuelo, NO hacer handoff/wake (se confunde con force-stop)
            // y mete jank en el primer render.
            if (RadioServiceController.isMt8163HcnBindInFlight()) {
                Log.d(TAG, "onResume: MT8163 bind HCN en vuelo; omitiendo recovery/handoff");
                return;
            }
            if (MT8163Engine.isHcnServiceBindBlockedAfterStreamEnd()) {
                Log.i(TAG, "onResume: bind HCN en ventana post-streaming (~12s); reintento automático al expirar");
                try {
                    Intent wakeIntent = new Intent("com.hcn.autoradio.FMRADIO_START");
                    wakeIntent.setPackage("com.hcn.autoradio");
                    wakeIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mHost.hostSendBroadcast(wakeIntent);
                } catch (Exception ignored) {}
                return;
            }
            Log.w(TAG, "onResume: mRadioService nulo (posible force-stop). Reactivando servicio...");
            try {
                Intent wakeIntent = new Intent("com.hcn.autoradio.FMRADIO_START");
                wakeIntent.setPackage("com.hcn.autoradio");
                wakeIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mHost.hostSendBroadcast(wakeIntent);
            } catch (Exception ignored) {}

            mHost.requestHcnBindWithMediaSessionHandoff("onResume");
            return;
        }

        PlaybackManager playback = mHost.getPlaybackManager();
        if (playback != null) {
            playback.resumeIfMutedBySystem();

            if (!playback.isMuted() && mHost.getRadioEngine() != null) {
                mHost.getRadioEngine().switchToFmAudio();
            }
        }

        if (mHost.getRadioEngine() != null && mHost.getScanManager() != null) {
            boolean oemScanning = mHost.getRadioEngine().isScanning();
            boolean uiScanning = mHost.getScanManager().adjustEngineScanningForAutoScanUi(oemScanning);
            mHost.setUiScanningFlag(uiScanning);
            mHost.getScanManager().applyEngineScanState(uiScanning);
        }
    }

    public void onPause() {
        MainActivity.sMainActivityResumed = false;
        RadioActivityFileLogger.noteMainActivityResumed(false);
    }

    public void onStop() {
        OnlineStreamManager osm = mHost.getOnlineStreamManager();
        boolean liveActive = osm != null && (osm.isPlaying() || osm.isLoading());

        if (!mHost.isPowerOffRequested()
                && !liveActive
                && (mHost.getFmMode() == MainActivity.FmMode.FM_K706 || mHost.getFmMode() == MainActivity.FmMode.FM_QS6)
                && mHost.getPlaybackManager() != null) {
            try {
                Intent media = new Intent(mHost.getHostContext(), RadioMediaService.class);
                media.setAction(RadioMediaService.ACTION_FORCE_SESSION_ACTIVE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mHost.hostStartForegroundService(media);
                } else {
                    mHost.hostStartService(media);
                }
            } catch (Exception e) {
                Log.w(TAG, "onStop: elevate RadioMediaService for steering (K706/QS6)", e);
            }
        }
        boolean isK706OrQs6 = (mHost.getFmMode() == MainActivity.FmMode.FM_K706
                || mHost.getFmMode() == MainActivity.FmMode.FM_QS6);
        if (!isK706OrQs6 && !liveActive && mHost.getRadioEngine() != null && !mHost.isHostChangingConfigurations()) {
            try {
                mHost.getRadioEngine().releaseAudioFocusOnlyForBackground();
            } catch (Exception e) {
                Log.w(TAG, "onStop: releaseAudioFocusOnlyForBackground", e);
            }
        }
    }

    public void onDestroy() {
        MainActivity.sWheelMediaBridgeActive = false;

        if (mHost.getMainHandler() != null) mHost.getMainHandler().removeCallbacksAndMessages(null);
        if (mHost.getAutoHideHandler() != null) mHost.getAutoHideHandler().removeCallbacksAndMessages(null);
        if (mHost.getClockHandler() != null) mHost.getClockHandler().removeCallbacksAndMessages(null);

        boolean recreating = mHost.isHostChangingConfigurations() || !mHost.isHostFinishing();
        Log.d(TAG, "onDestroy: Limpiando recursos. recreating=" + recreating + " (isFinishing=" + mHost.isHostFinishing() + ")");

        mHost.stopStatusPolling();

        if (mHost.getStationInfoExecutor() != null) {
            try {
                mHost.getStationInfoExecutor().shutdownNow();
            } catch (Exception ignored) {}
            mHost.setStationInfoExecutor(null);
        }

        if (mHost.getMediaSessionManager() != null) {
            mHost.getMediaSessionManager().disconnect();
        }

        if (mHost.getDeviceManager() != null) {
            mHost.getDeviceManager().releaseAllResources(recreating);
        }

        try {
            if (mHost.getHardwareManager() != null) {
                mHost.getHardwareManager().unregisterReceivers();
            }
        } catch (Exception e) {}

        if (mHost.getHiddenPlayer() != null) {
            mHost.getHiddenPlayer().release();
            mHost.setHiddenPlayer(null);
        }

        if (mHost.getOnlineStreamManager() != null) {
            mHost.getOnlineStreamManager().release();
            mHost.setOnlineStreamManagerRef(null);
        }

        if (mHost.getPresetManager() != null) mHost.getPresetManager().release();
        if (mHost.getLogoManager() != null) mHost.getLogoManager().release();
        if (mHost.getRdsManager() != null) mHost.getRdsManager().release();
        if (mHost.getUiController() != null) mHost.getUiController().release();
    }

    public void prepareForPowerOff() {
        try {
            FactoryRadioHijackerService.markPowerOffForHijack(mHost.getHostContext());
        } catch (Exception ignored) {}
        mHost.setShutdownPersistGuardUntilMs(android.os.SystemClock.elapsedRealtime() + 9000L);
        mHost.setPowerOffRequested(true);

        try {
            if (mHost.getPlaybackManager() != null) {
                mHost.getPlaybackManager().setMute(true);
            }
            if (mHost.getUiController() != null) {
                mHost.getUiController().updateMute(true);
            }
        } catch (Exception ignored) {}

        if (mHost.getRadioPresets() != null && mHost.getLastFreqKhz() > 0) {
            mHost.getRadioPresets().edit()
                    .putInt("pref_last_freq", mHost.getLastFreqKhz())
                    .putInt("pref_last_band", mHost.getUiCurrentBand())
                    .apply();
        }
    }
}
