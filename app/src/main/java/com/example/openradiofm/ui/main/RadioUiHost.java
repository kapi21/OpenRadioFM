package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.example.openradiofm.data.source.HiddenRadioPlayer;
import com.example.openradiofm.data.source.RadioEngine;
import com.example.openradiofm.ui.theme.ThemeManager;
import com.hcn.autoradio.IRadioServiceAPI;

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
}
