package com.example.openradiofm.ui.main;

import android.app.Activity;
import android.util.Log;

import com.example.openradiofm.data.source.RadioEngine;

/**
 * V5.5: Gestor de Dispositivo y Ciclo de Vida del Hardware.
 * Centraliza la lógica de apagado (Power Off), limpieza de recursos
 * y cierre del hardware de radio.
 *
 * Antes de V5.5, esta lógica residía directamente en MainActivity.onDestroy()
 * y en los listeners de los botones de la UI.
 */
public class DeviceManager {
    private static final String TAG = "DeviceManager";

    private final Activity mActivity;
    private RadioEngine mEngine;
    private PlaybackManager mPlaybackManager;
    private MediaSessionManager mMediaSessionManager;
    private RadioServiceController mServiceController;
    private RDSManager mRdsManager;
    private com.example.openradiofm.data.repository.RadioRepository mRepository;
    private java.util.concurrent.ExecutorService mPollingExecutor;

    public DeviceManager(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * Configura las referencias a los componentes que necesitan limpieza.
     */
    public void init(RadioEngine engine,
                     PlaybackManager playbackManager,
                     MediaSessionManager mediaSessionManager,
                     RadioServiceController serviceController,
                     RDSManager rdsManager,
                     com.example.openradiofm.data.repository.RadioRepository repository,
                     java.util.concurrent.ExecutorService pollingExecutor) {
        this.mEngine = engine;
        this.mPlaybackManager = playbackManager;
        this.mMediaSessionManager = mediaSessionManager;
        this.mServiceController = serviceController;
        this.mRdsManager = rdsManager;
        this.mRepository = repository;
        this.mPollingExecutor = pollingExecutor;
    }

    public void setEngine(RadioEngine engine) {
        this.mEngine = engine;
    }

    public void setPollingExecutor(java.util.concurrent.ExecutorService executor) {
        this.mPollingExecutor = executor;
    }

    /**
     * Apaga la radio y cierra la aplicación.
     * Secuencia:
     * 1. Cerrar el dispositivo hardware (closeDevice)
     * 2. Finalizar la actividad (finish)
     */
    public void powerOff() {
        // K706: "PowerOff" en UI se usa como "salir/minimizar".
        // Hacer finish() aquí provoca que el launcher trate el cierre como cambio de fuente y puede
        // ejecutar forceStopPackage(com.example.openradiofm), rompiendo el widget genérico de música.
        try {
            Log.d(TAG, "Power Off(UI): minimizando app (sin cerrar Activity ni apagar HW)");
            mActivity.moveTaskToBack(true);
        } catch (Exception e) {
            Log.e(TAG, "Power Off(UI): moveTaskToBack falló, usando finish()", e);
            mActivity.finish();
        }
    }

    /**
     * Libera todos los recursos del sistema.
     * Debe llamarse desde onDestroy() de la Activity.
     * @param isChangingConfigurations true si es una recreación por cambio de layout, 
     *                                 false si es un cierre definitivo (atrás, exit).
     */
    public void releaseAllResources(boolean isChangingConfigurations) {
        Log.d(TAG, "Liberando recursos... (isChangingConfigurations=" + isChangingConfigurations + ")");

        // 1. Desconectar MediaSession (Android Auto)
        if (mMediaSessionManager != null) {
            mMediaSessionManager.disconnect();
        }

        // 2. Desregistrar receiver de controles multimedia
        if (mPlaybackManager != null) {
            mPlaybackManager.unregisterMediaReceiver();
        }

        // 3. Liberar motor de radio (Pasando flag de persistencia)
        if (mEngine != null) {
            mEngine.release(isChangingConfigurations);
        }

        // 4. Liberar servicio de radio (ServiceController)
        if (mServiceController != null) {
            // V20.0: Podríamos silenciar el release del controller también si fuera necesario,
            // pero mEngine.release(true) ya protege el vínculo principal de QS6.
            if (!isChangingConfigurations) {
                mServiceController.release();
            }
        }

        // 5. Limpiar RDS y repositorio (Solo si es cierre real)
        if (!isChangingConfigurations) {
            if (mRdsManager != null) {
                mRdsManager.reset(true);
            }
            if (mRepository != null) {
                mRepository.shutdown();
            }
            if (mPollingExecutor != null) {
                mPollingExecutor.shutdown();
            }
        }

        Log.d(TAG, "Liberación de recursos finalizada.");
    }
}
