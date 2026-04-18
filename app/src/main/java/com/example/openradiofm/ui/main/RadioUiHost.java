package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;

import com.example.openradiofm.data.repository.RadioRepository;
import com.example.openradiofm.data.source.HiddenRadioPlayer;
import com.example.openradiofm.data.source.RadioEngine;
import com.example.openradiofm.ui.theme.ThemeManager;
import com.hcn.autoradio.IRadioServiceAPI;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Contrato de acceso a la pantalla principal de radio (Fase 0 refactor 5.2.0.MCU).
 * <p>
 * Centraliza lecturas y operaciones que antes exponían {@link MainActivity} completa a
 * coordinadores. Las implementaciones deben delegar en el estado actual de la activity
 * sin cambiar la semántica.
 * </p>
 *
 * @see MainActivity
 */
public interface RadioUiHost {

    // --- Puente Activity / Context ---

    Context getHostContext();

    boolean isHostFinishing();

    boolean isHostDestroyed();

    void runOnHostUiThread(Runnable action);

    boolean isHostChangingConfigurations();

    void hostSendBroadcast(Intent intent);

    void hostStartForegroundService(Intent intent);

    void hostStartService(Intent intent);

    void requestHcnBindWithMediaSessionHandoff(String reasonForLog);

    void stopStatusPolling();

    // --- Estado radio / servicios ---

    OnlineStreamManager getOnlineStreamManager();

    IRadioServiceAPI getRadioService();

    MainActivity.FmMode getFmMode();

    RadioServiceController getServiceController();

    PlaybackManager getPlaybackManager();

    RadioEngine getRadioEngine();

    ScanManager getScanManager();

    void setUiScanningFlag(boolean value);

    SharedPreferences getRadioPresets();

    /** Última frecuencia conocida en kHz (campo {@code mLastFreq}). */
    int getLastFreqKhz();

    /**
     * Banda lógica de la UI ({@code mCurrentBand}); puede diferir momentáneamente de
     * {@link RadioEngine#getCurrentBand()} durante transiciones.
     */
    int getUiCurrentBand();

    void setShutdownPersistGuardUntilMs(long elapsedRealtimeMs);

    void setPowerOffRequested(boolean value);

    boolean isPowerOffRequested();

    Handler getMainHandler();

    Handler getAutoHideHandler();

    Handler getClockHandler();

    ExecutorService getStationInfoExecutor();

    void setStationInfoExecutor(ExecutorService executor);

    MediaSessionManager getMediaSessionManager();

    DeviceManager getDeviceManager();

    HardwareManager getHardwareManager();

    HiddenRadioPlayer getHiddenPlayer();

    void setHiddenPlayer(HiddenRadioPlayer player);

    void setOnlineStreamManagerRef(OnlineStreamManager manager);

    PresetManager getPresetManager();

    LogoManager getLogoManager();

    RDSManager getRdsManager();

    BaseLayoutController getUiController();

    void setMute(boolean mute);

    boolean isMuteState();

    ThemeManager getThemeManager();

    boolean isRdsLockHeld();

    // --- Coordinación UI motor / refresco (StatusRefreshCoordinator, EngineCallbackCoordinator) ---

    View findHostViewById(int id);

    SignalMeterCoordinator getSignalMeterCoordinator();

    FrequencyStateManager getFreqStateManager();

    SkinCoordinator getSkinCoordinator();

    NightModeManager getNightModeManager();

    RadioRepository getRadioRepository();

    RadioSessionController getRadioSessionController();

    K706EngineeringDialog getK706EngineeringDialog();

    QS6EngineeringDialog getQs6EngineeringDialog();

    Map<String, String> getLogoCachePerBand();

    void updateDataActivityUI();

    void checkAndApplyNightMode();

    void syncLocDxButtonVisual(boolean isLocal);

    void refreshStereoIndicatorUi(Boolean stereo);

    void clearStationLogoUi();

    void sendWidgetUpdate(int freq, int band, String rdsName);

    void updateFrequencyDisplay(int freq, String rdsName);

    void updateBandImage(int band);

    void handleFrequencyChange(int freqKhz);

    boolean isQs6TransitionGuardActive();

    boolean hasStableCachedNameForFrequency(int freqKhz);

    boolean isStationMemorized(int freq);

    int getPresetIndex(int freq);

    boolean isUiScanning();

    int hostNextStationInfoSequence();

    int getLastStationInfoRequestedSeq();

    void setLastStationInfoRequestedSeq(int seq);

    void setUiCurrentBand(int band);

    void setLastFreqKhz(int freqKhz);

    String getLastPs();

    void setLastPs(String ps);

    String getCurrentPty();

    void setCurrentPty(String pty);

    void setHasRdsLock(boolean value);

    boolean getHadRdsLockForTick();

    void setHadRdsLockForTick(boolean value);

    long getLastRdsLockTickUptimeMs();

    void setLastRdsLockTickUptimeMs(long ms);

    void incrementLogoUiGeneration();

    void persistLastBandPreference(int band);

    int getLastStoredBand();

    void setLastStoredBand(int band);

    String getCurrentPi();

    void setCurrentPi(String pi);

    void handleHwLightsAutomation(boolean lightsOn);

    void handleHwReverseMute(boolean reverseOn);

    void handleHwHandbrakeSafety(boolean handbrakeUp);

    void handleHwAccState(boolean accOn);
}
