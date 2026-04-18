package com.example.openradiofm.ui.main;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.example.openradiofm.R;

/**
 * Configuración de streaming online, icono de actividad de datos y listeners asociados
 * (Fase 3 refactor 5.2.0.MCU — extracción desde {@link MainActivity#setupOnlineStreaming()}).
 */
public final class StreamingUiCoordinator {

    private StreamingUiCoordinator() {}

    static void install(MainActivity a) {
        a.mOnlineStreamManager = new OnlineStreamManager(a, a.mPlaybackManager);
        a.mOnlineStreamManager.setListener(new OnlineStreamManager.StreamListener() {
            @Override
            public void onStreamStatusChanged(boolean isLoading, boolean isPlaying) {
                a.runOnUiThread(() -> a.updateDataActivityUI());
            }

            @Override
            public void onStreamError(String message) {
                a.runOnUiThread(() -> a.showToast(message));
            }

            @Override
            public void onBeforeStreamStart() {
                a.removeHcnBindAfterHandoffCallbacks();
            }

            @Override
            public void onStreamStoppedMt8163() {
                com.example.openradiofm.data.source.MT8163Engine.setBlockHcnServiceBindAfterStreamEnd(true);
                try {
                    boolean mcuDirect = false;
                    try {
                        mcuDirect = a.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                                .getBoolean("pref_mt8163_mcu_direct", false);
                    } catch (Exception ignored) {}
                    if (!mcuDirect) {
                        android.content.Intent wakeIntent = new android.content.Intent("com.hcn.autoradio.FMRADIO_START");
                        wakeIntent.setPackage("com.hcn.autoradio");
                        wakeIntent.addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        a.sendBroadcast(wakeIntent);
                    }
                } catch (Exception ignored) {}
                a.removeHcnBindAfterHandoffCallbacks();
            }
        });

        if (a.ivDataActivity != null) {
            a.ivDataActivity.setOnTouchListener((v, event) -> {
                if (a.mUiMediator.ivDataActivityIcon == null) return false;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        a.mUiMediator.ivDataActivityIcon.setAlpha(0.42f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        a.mUiMediator.ivDataActivityIcon.setAlpha(1.0f);
                        break;
                    default:
                        break;
                }
                return false;
            });

            a.ivDataActivity.setOnClickListener(v -> {
                if (a.mOnlineStreamManager != null && (a.mOnlineStreamManager.isPlaying() || a.mOnlineStreamManager.isLoading())) {
                    a.mOnlineStreamManager.stopStream();
                    a.showToast(a.getString(R.string.toast_returning_fm));
                    return;
                }

                int freq = (a.mEngine != null) ? a.mEngine.getCurrentFreq() : -1;
                if (freq <= 0) return;

                final int bgGen = a.getUiWorkGeneration();
                com.example.openradiofm.util.AppIoExecutor.execute(() -> {
                    if (a.isFinishing() || a.isDestroyed()) return;
                    if (a.getUiWorkGeneration() != bgGen) return;
                    try {
                        com.example.openradiofm.data.model.RadioStation station =
                                a.mRepository.getStationInfo(freq, null);
                        String url = (station != null) ? station.getStreamUrl() : null;
                        if (url == null || url.isEmpty()) {
                            a.runOnUiThread(() -> {
                                if (!a.isFinishing()) {
                                    a.showToast(a.getString(R.string.toast_stream_searching));
                                }
                            });
                            url = a.mRepository.resolveStreamUrlForFrequency(freq);
                        }
                        final String streamUrl = url;
                        a.runOnUiThread(() -> {
                            if (a.isFinishing() || a.isDestroyed()) return;
                            if (a.getUiWorkGeneration() != bgGen) return;
                            if (streamUrl != null && !streamUrl.isEmpty()) {
                                a.mOnlineStreamManager.startStream(streamUrl);
                                a.showToast(a.getString(R.string.toast_stream_starting));
                            } else {
                                a.showToast(a.getString(R.string.toast_stream_unavailable));
                            }
                        });
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG, "Streaming: getStationInfo falló", e);
                        a.runOnUiThread(() -> {
                            if (!a.isFinishing()) {
                                a.showToast(a.getString(R.string.toast_station_load_error));
                            }
                        });
                    }
                });
            });

            a.ivDataActivity.setOnLongClickListener(v -> {
                int freq = (a.mEngine != null) ? a.mEngine.getCurrentFreq() : -1;
                if (freq > 0) {
                    a.showToast(a.getString(R.string.toast_station_cache_sync));
                    a.mRepository.clearCacheForFrequency(freq);

                    if (a.mOnlineStreamManager != null && (a.mOnlineStreamManager.isPlaying() || a.mOnlineStreamManager.isLoading())) {
                        a.mOnlineStreamManager.stopStream();
                    }

                    final int bgGenCache = a.getUiWorkGeneration();
                    com.example.openradiofm.util.AppIoExecutor.execute(() -> {
                        if (a.isFinishing() || a.isDestroyed()) return;
                        if (a.getUiWorkGeneration() != bgGenCache) return;
                        a.mRepository.getStationInfo(freq, logoUrl -> {
                            String name = (a.mRdsManager != null) ? a.mRdsManager.getDisplayName(freq) : a.mLastPs;
                            a.runOnUiThread(() -> {
                                if (a.isFinishing() || a.isDestroyed()) return;
                                if (a.getUiWorkGeneration() != bgGenCache) return;
                                a.updateFrequencyDisplay(freq, name);
                            });
                        });
                    });
                }
                return true;
            });
        }
    }
}
