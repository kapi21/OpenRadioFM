# Regenera MainActivityBootstrap desde MainActivity (líneas 1282-1730 del onCreate).
# Tras generar: corregir a mano si cambia el rango; no sustituir dentro de R.id.* ni claves Bundle.
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
main_py = root / "app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java"
lines = main_py.read_text(encoding="utf-8").splitlines(keepends=True)
chunk = "".join(lines[1281:1730])

repls = [
    ("new SkinCoordinator(this)", "new SkinCoordinator(a)"),
    ("new StatusRefreshCoordinator(this)", "new StatusRefreshCoordinator(a)"),
    ("new EngineCallbackCoordinator(this)", "new EngineCallbackCoordinator(a)"),
    ("new LifecycleCoordinator(this)", "new LifecycleCoordinator(a)"),
    ("new HardwareKeyCoordinator(this)", "new HardwareKeyCoordinator(a)"),
    ("new UiViewMediator(this)", "new UiViewMediator(a)"),
    ("new HardwareManager(this)", "new HardwareManager(a)"),
    ("new IconPackManager(this,", "new IconPackManager(a,"),
    ("new PresetNumberIconManager(this)", "new PresetNumberIconManager(a)"),
    (
        'getSharedPreferences("RadioPresets", MODE_PRIVATE)',
        'a.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)',
    ),
    ("applyStatusBarVisibility()", "a.applyStatusBarVisibility()"),
    ("setContentView(", "a.setContentView("),
    ("new SimpleLayoutController(this)", "new SimpleLayoutController(a)"),
    ("new V3LayoutController(this)", "new V3LayoutController(a)"),
    ("new MainLayoutController(this)", "new MainLayoutController(a)"),
    ("applyLayout2SidePreference()", "a.applyLayout2SidePreference()"),
    ("mUiMediator.bindViews()", "a.mUiMediator.bindViews()"),
    ("mSignalBarsView = findViewById", "a.mSignalBarsView = a.findViewById"),
    ("new SignalMeterCoordinator(this)", "new SignalMeterCoordinator(a)"),
    ("mSignalMeterCoordinator.bind", "a.mSignalMeterCoordinator.bind"),
    ("mSignalMeterCoordinator.applyModeVisibility()", "a.mSignalMeterCoordinator.applyModeVisibility()"),
    ("ensureFirstRunLanguageAndCountry()", "a.ensureFirstRunLanguageAndCountry()"),
    (
        "mUiController.initViews(findViewById(android.R.id.content))",
        "a.mUiController.initViews(a.findViewById(android.R.id.content))",
    ),
    ("applyFonts()", "a.applyFonts()"),
    ("applyIconPack()", "a.applyIconPack()"),
    ("applyReliefHd(", "a.applyReliefHd("),
    ("checkSelfPermission(", "a.checkSelfPermission("),
    ("requestPermissions(", "a.requestPermissions("),
    ("new LogoManager(this)", "new LogoManager(a)"),
    (
        "new com.example.openradiofm.data.source.SupabaseSyncManager(this,",
        "new com.example.openradiofm.data.source.SupabaseSyncManager(a,",
    ),
    (
        "new RadioServiceController(this, mPrefs, mServiceListener)",
        "new RadioServiceController(a, a.mPrefs, a.mServiceListener)",
    ),
    ("new NightModeManager(this, mPrefs,", "new NightModeManager(a, a.mPrefs,"),
    ("new DayModeManager(this, mPrefs,", "new DayModeManager(a, a.mPrefs,"),
    ("updateFrequencyDisplay(", "a.updateFrequencyDisplay("),
    ("new HistoryManager(this, mPrefs)", "new HistoryManager(a, a.mPrefs)"),
    ("new MediaSessionManager(this)", "new MediaSessionManager(a)"),
    ("mMediaSessionManager.connect()", "a.mMediaSessionManager.connect()"),
    ("new ControlPanelManager(this)", "new ControlPanelManager(a)"),
    ("new PlaybackManager(this)", "new PlaybackManager(a)"),
    ("mPlaybackManager.init(mEngine,", "a.mPlaybackManager.init(a.mEngine,"),
    ("mMuteState =", "a.mMuteState ="),
    ("runOnUiThread(() -> {", "a.runOnUiThread(() -> {"),
    ("if (mUiController != null)", "if (a.mUiController != null)"),
    ("mUiController.updateMute", "a.mUiController.updateMute"),
    ("if (mUiMediator.btnMute != null)", "if (a.mUiMediator.btnMute != null)"),
    ("mUiMediator.btnMute.", "a.mUiMediator.btnMute."),
    (
        "setImageResourceIfChanged(mUiMediator.btnMute,",
        "MainActivity.setImageResourceIfChanged(a.mUiMediator.btnMute,",
    ),
    ("if (mIconPackManager != null)", "if (a.mIconPackManager != null)"),
    ("mIconPackManager.apply(mUiMediator.btnMute,", "a.mIconPackManager.apply(a.mUiMediator.btnMute,"),
    ("boolean usePresetMode = mPrefs != null", "boolean usePresetMode = a.mPrefs != null"),
    ("&& mPrefs.getInt", "&& a.mPrefs.getInt"),
    ("if (mPresetManager != null)", "if (a.mPresetManager != null)"),
    ("mPresetManager.playNextPreset()", "a.mPresetManager.playNextPreset()"),
    ("} else if (mEngine != null)", "} else if (a.mEngine != null)"),
    ("mEngine.seekUp()", "a.mEngine.seekUp()"),
    ("mPresetManager.playPrevPreset()", "a.mPresetManager.playPrevPreset()"),
    ("mEngine.seekDown()", "a.mEngine.seekDown()"),
    ("mPlaybackManager.registerMediaReceiver()", "a.mPlaybackManager.registerMediaReceiver()"),
    ("new DeviceManager(this)", "new DeviceManager(a)"),
    ("mLogoManager.loadCustomBackground()", "a.mLogoManager.loadCustomBackground()"),
    ("mLogoManager.loadCarLogo()", "a.mLogoManager.loadCarLogo()"),
    ("new SimpleLayoutManager(this)", "new SimpleLayoutManager(a)"),
]
for old, new in repls:
    chunk = chunk.replace(old, new)

fields = [
    "mSkinCoordinator",
    "mStatusRefreshCoordinator",
    "mEngineCallbackCoordinator",
    "mLifecycleCoordinator",
    "mHardwareKeyCoordinator",
    "mFreqStateManager",
    "mSignalMeterCoordinator",
    "mLastFreq",
    "mIsV3",
    "mIsRecreating",
    "mHardwareManager",
    "mWidgetBroadcastManager",
    "mPrefs",
    "mIconPackManager",
    "mPresetNumberIconManager",
    "mIsSimpleLayout",
    "mUiController",
    "mUiMediator",
    "mRepository",
    "mSupabaseSyncManager",
    "mServiceController",
    "mNightModeManager",
    "mDayModeManager",
    "mRdsManager",
    "mLastPs",
    "mHistoryManager",
    "mMediaSessionManager",
    "mControlPanelManager",
    "mPlaybackManager",
    "mDeviceManager",
    "mSimpleLayoutManager",
    "mMode",
    "mEngine",
    "mStartupSavedFreqKhz",
    "mLastBand",
    "mCurrentBand",
    "mStartupPersistGuardUntilMs",
    "mStartupRetuneAttempts",
    "mClockHandler",
    "mClockRunnable",
    "tvFrequency",
    "tvRdsName",
    "tvRdsInfo",
    "tvPty",
    "ivBandIndicator",
    "ivUnitLabel",
    "ivFavoriteIndicator",
    "ivStereoIcon",
    "ivAfIcon",
    "ivTaIcon",
    "ivTpIcon",
    "ivDataActivity",
    "mThemeManager",
    "mAutoHideHandler",
    "mAutoHideRunnable",
    "mControlsHidden",
    "mDialogManager",
    "mPresetManager",
    "mSignalBarsView",
    "mLogoManager",
]
for fld in fields:
    chunk = re.sub(r"(?<!a\.)\b" + fld + r"\b", "a." + fld, chunk)
while "a.a." in chunk:
    chunk = chunk.replace("a.a.", "a.")

chunk = chunk.replace("MODE_PRIVATE", "Context.MODE_PRIVATE")
chunk = re.sub(r"(?<!a\.)findViewById\(", "a.findViewById(", chunk)
chunk = chunk.replace("setVolumeControlStream(", "a.setVolumeControlStream(")
chunk = chunk.replace("Log.d(TAG,", "Log.d(MainActivity.TAG,")
chunk = chunk.replace("Log.i(TAG,", "Log.i(MainActivity.TAG,")
chunk = chunk.replace("Log.w(TAG,", "Log.w(MainActivity.TAG,")
chunk = chunk.replace("isV3LayoutActive()", "a.isV3LayoutActive()")
chunk = chunk.replace("hideBottomControls()", "a.hideBottomControls()")
chunk = chunk.replace("showBottomControls()", "a.showBottomControls()")
chunk = chunk.replace("resetAutoHideTimer()", "a.resetAutoHideTimer()")
chunk = chunk.replace("applyLogoModePreference()", "a.applyLogoModePreference()")
chunk = chunk.replace("setupOnlineStreaming()", "a.setupOnlineStreaming()")
chunk = chunk.replace("animateButton(", "a.animateButton(")
chunk = chunk.replace("refreshStereoIndicatorUi(", "a.refreshStereoIndicatorUi(")
chunk = chunk.replace("showToast(", "a.showToast(")
chunk = chunk.replace("getString(R.string", "a.getString(R.string")
chunk = chunk.replace("applySkin(", "a.applySkin(")
chunk = chunk.replace("checkAndApplyNightMode()", "a.checkAndApplyNightMode()")
chunk = chunk.replace("setupCreditsEasterEgg()", "a.setupCreditsEasterEgg()")
chunk = chunk.replace(
    "scheduleRadioUiResyncAfterRecreation()", "a.scheduleRadioUiResyncAfterRecreation()"
)
chunk = chunk.replace("adjustLayoutForDPI()", "a.adjustLayoutForDPI()")
chunk = chunk.replace("BAND_FM1", "MainActivity.BAND_FM1")
chunk = chunk.replace("BAND_AM1", "MainActivity.BAND_AM1")
chunk = chunk.replace("PREF_QS6_BOOTSTRAP_SANITIZED", "MainActivity.PREF_QS6_BOOTSTRAP_SANITIZED")
chunk = chunk.replace("PREF_K706_BOOTSTRAP_SANITIZED", "MainActivity.PREF_K706_BOOTSTRAP_SANITIZED")
chunk = chunk.replace("FmMode.", "MainActivity.FmMode.")
chunk = chunk.replace(
    "new com.example.openradiofm.ui.theme.ThemeManager(this)",
    "new com.example.openradiofm.ui.theme.ThemeManager(a)",
)
chunk = chunk.replace(
    "mSimpleLayoutManager.initViews(findViewById(android.R.id.content))",
    "a.mSimpleLayoutManager.initViews(a.findViewById(android.R.id.content))",
)

header = """package com.example.openradiofm.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.openradiofm.R;

/**
 * Secuencia de inicialización de {@link MainActivity#onCreate} tras {@code super.onCreate}
 * (Fase 1 refactor 5.2.0.MCU).
 */
final class MainActivityBootstrap {

    private MainActivityBootstrap() {}

    static void runAfterSuper(MainActivity a, Bundle savedInstanceState) {
"""
footer = """
    }
}
"""
out = header + chunk + footer
out_path = root / "app/src/main/java/com/example/openradiofm/ui/main/MainActivityBootstrap.java"
out_path.write_text(out, encoding="utf-8", newline="\n")
print("OK", out_path)
