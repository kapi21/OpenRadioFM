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
        try {
            Log.d(TAG, "Power Off: Cerrando dispositivo y finalizando aplicación");
            if (mEngine != null) {
                mEngine.closeDevice();
            }
            mActivity.finish();
        } catch (Exception e) {
            Log.e(TAG, "Error durante la secuencia de apagado", e);
            mActivity.finish();
        }
    }

    /**
     * Libera todos los recursos del sistema.
     * Debe llamarse desde onDestroy() de la Activity.
     */
    public void releaseAllResources() {
        Log.d(TAG, "Liberando todos los recursos...");

        // 1. Desconectar MediaSession (Android Auto)
        if (mMediaSessionManager != null) {
            mMediaSessionManager.disconnect();
        }

        // 2. Desregistrar receiver de controles multimedia
        if (mPlaybackManager != null) {
            mPlaybackManager.unregisterMediaReceiver();
        }

        // 3. Liberar motor de radio
        if (mEngine != null) {
            mEngine.release();
        }

        // 4. Liberar servicio de radio
        if (mServiceController != null) {
            mServiceController.release();
        }

        // 5. Limpiar RDS y repositorio
        if (mRdsManager != null) {
            mRdsManager.reset(true);
        }
        if (mRepository != null) {
            mRepository.shutdown();
        }

        // 6. Detener ejecutor de polling
        if (mPollingExecutor != null) {
            mPollingExecutor.shutdown();
        }

        Log.d(TAG, "Todos los recursos liberados correctamente");
    }
}
