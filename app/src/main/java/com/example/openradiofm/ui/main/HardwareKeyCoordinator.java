package com.example.openradiofm.ui.main;

import android.util.Log;
import android.view.KeyEvent;

public class HardwareKeyCoordinator {
    private static final String TAG = "HardwareKeyCoordinator";
    private final MainActivity mActivity;

    public HardwareKeyCoordinator(MainActivity activity) {
        this.mActivity = activity;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final boolean usePresetMode = mActivity.mPrefs != null
                && mActivity.mPrefs.getInt("pref_steering_next_prev_mode", 0) == 1;

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                Log.d(TAG, "Hardware Key: NEXT -> " + (usePresetMode ? "nextPreset" : "seekUp"));
                if (usePresetMode) {
                    if (mActivity.mPresetManager != null) mActivity.mPresetManager.playNextPreset();
                    return true;
                }
                if (mActivity.mEngine != null) {
                    mActivity.mEngine.seekUp();
                    return true;
                }
                if (mActivity.mPresetManager != null) mActivity.mPresetManager.playNextPreset();
                return true;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                Log.d(TAG, "Hardware Key: PREV -> " + (usePresetMode ? "prevPreset" : "seekDown"));
                if (usePresetMode) {
                    if (mActivity.mPresetManager != null) mActivity.mPresetManager.playPrevPreset();
                    return true;
                }
                if (mActivity.mEngine != null) {
                    mActivity.mEngine.seekDown();
                    return true;
                }
                if (mActivity.mPresetManager != null) mActivity.mPresetManager.playPrevPreset();
                return true;

            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                mActivity.setMute(!mActivity.mMuteState);
                return true;
        }
        return false;
    }
}
