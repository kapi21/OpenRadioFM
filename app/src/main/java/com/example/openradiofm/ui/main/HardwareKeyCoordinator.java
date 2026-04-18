package com.example.openradiofm.ui.main;

import android.util.Log;
import android.view.KeyEvent;

public class HardwareKeyCoordinator {
    private static final String TAG = "HardwareKeyCoordinator";
    private final RadioUiHost mHost;

    public HardwareKeyCoordinator(RadioUiHost host) {
        this.mHost = host;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final boolean usePresetMode = mHost.getRadioPresets() != null
                && mHost.getRadioPresets().getInt("pref_steering_next_prev_mode", 0) == 1;

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                Log.d(TAG, "Hardware Key: NEXT -> " + (usePresetMode ? "nextPreset" : "seekUp"));
                if (usePresetMode) {
                    if (mHost.getPresetManager() != null) mHost.getPresetManager().playNextPreset();
                    return true;
                }
                if (mHost.getRadioEngine() != null) {
                    mHost.getRadioEngine().seekUp();
                    return true;
                }
                if (mHost.getPresetManager() != null) mHost.getPresetManager().playNextPreset();
                return true;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                Log.d(TAG, "Hardware Key: PREV -> " + (usePresetMode ? "prevPreset" : "seekDown"));
                if (usePresetMode) {
                    if (mHost.getPresetManager() != null) mHost.getPresetManager().playPrevPreset();
                    return true;
                }
                if (mHost.getRadioEngine() != null) {
                    mHost.getRadioEngine().seekDown();
                    return true;
                }
                if (mHost.getPresetManager() != null) mHost.getPresetManager().playPrevPreset();
                return true;

            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                mHost.setMute(!mHost.isMuteState());
                return true;
        }
        return false;
    }
}
