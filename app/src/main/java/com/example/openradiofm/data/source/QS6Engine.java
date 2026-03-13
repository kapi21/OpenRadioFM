package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nwd.radio.service.RadioCallback;
import com.nwd.radio.service.RadioFeature;
import com.nwd.radio.service.data.Frequency;
import com.nwd.radio.service.data.RadioPoint;

/**
 * V14.0: Motor Nativo Avanzado mediante IPC AIDL para QS6 G5 (NWD).
 * Se conecta al servicio oficial com.nwd.radio.service.RadioService,
 * ganando acceso profundo a RDS, presets y sincronía bidireccional perfecta.
 */
public class QS6Engine implements RadioEngine {
    private static final String TAG = "QS6Engine";
    private Context mContext;
    private RadioEngineCallback mCallback;

    // Estado local sincronizado por AIDL
    private int mCurrentFreq = 87500;
    private int mCurrentBand = 0;
    private boolean mIsStereo = false;
    private boolean mIsMute = false;
    private boolean mIsAfEnabled = false;
    private boolean mIsTaEnabled = false;
    private boolean mIsTpEnabled = false;
    private boolean mIsScanning = false;
    private boolean mIsDxLocal = false;
    private final android.os.Handler mMainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    // Intents de Emisión (Encendido/Apagado General del MCU)
    private static final String ACTION_CHANGE_SOURCE = "com.nwd.action.ACTION_CHANGE_SOURCE";
    private static final String ACTION_KEY_VALUE = "com.nwd.action.ACTION_KEY_VALUE";
    private static final String ACTION_START_NWD_ACTIVITY = "com.nwd.action.ACTION_START_NWD_ACTIVITY";

    // KEY_VALUE Mapeos
    private static final byte KEY_FM = 0x48;

    private static final byte SOURCE_RADIO = 0x04;
    private static final byte SOURCE_ANDROID = 0x00;

    // AIDL stubs
    private RadioFeature mNwdService;
    private boolean mIsBound = false;
    private int mRetryCount = 0;
    private static final int MAX_RETRIES = 3;

    // Implementación de la Callback del IPC hacia NWD
    private final RadioCallback.Stub mNwdCallback = new RadioCallback.Stub() {
        @Override
        public void notifyCurrentFrequency(byte bandType, int frequency, String psName, int prefabIndex) {
            // V19.1: Refinamiento de frecuencia según banda (QS6). 
            // - FM (band < 3): frequency viene en décimas de MHz (ej: 9690 -> 96900 kHz)
            // - AM/SW (band >= 3): frequency suele venir en kHz directos (ej: 1080 -> 1080 kHz)
            int freqKhz;
            if (bandType < 3) {
                freqKhz = frequency * 10;
            } else {
                freqKhz = frequency;
            }

            mCurrentFreq = freqKhz;
            mCurrentBand = (int) bandType;
            
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onBandChanged((int) bandType);
                    mCallback.onFrequencyChanged(freqKhz);
                    if (psName != null && !psName.trim().isEmpty()) {
                        mCallback.onRdsName(psName);
                    }
                }
            });
            
            Log.d(TAG, "NWD AIDL notifyCurrentFrequency -> FreqRaw: " + frequency + ", FreqKhz: " + freqKhz + ", Band: " + bandType + ", PS: " + psName);
        }

        @Override
        public void notifyCurrentIsTA(boolean isTA) {
            mIsTaEnabled = isTA;
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRdsStatus(mIsAfEnabled, isTA, mIsTpEnabled);
                }
            });
        }

        @Override
        public void notifyCurrentPTYType(byte ptyType) {
        }

        @Override
        public void notifyNearOn(boolean isOn) {
        }

        @Override
        public void notifyPrefabFrequency(Frequency[] frequencys) {
        }

        @Override
        public void notifyPrefabPTYType(byte ptyType) {
        }

        @Override
        public void notifyRDSStateChange() {
            // V19.5: Consultar estados reales tras el cambio
            performAidlCall("notifyRDSStateChange", () -> {
                mIsAfEnabled = mNwdService.getRDSState(1); // 1 = AF en NWD
                mIsTaEnabled = mNwdService.getRDSState(0); // 0 = TA en NWD
                mMainHandler.post(() -> {
                    if (mCallback != null) {
                        mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                    }
                });
            });
        }

        @Override
        public void notifyRadioPoint(RadioPoint[] radioPoints) {
        }

        @Override
        public void notifyRadioScanState(int state) {
            Log.d(TAG, "NWD AIDL ScanState -> " + state);
            mIsScanning = (state != 0);
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onScanStatusChanged(mIsScanning);
                }
            });
        }

        @Override
        public void notifyRdsShowState(boolean isShow) {
        }

        @Override
        public void notifyRtMessage(String rtMessage) {
            Log.d(TAG, "NWD AIDL notifyRtMessage -> " + rtMessage);
            mMainHandler.post(() -> {
                if (mCallback != null && rtMessage != null) {
                    mCallback.onRdsText(rtMessage);
                }
            });
        }

        @Override
        public void notifyState(byte state) {
        }

        @Override
        public void notifyStereo(boolean isStereo) {
            mIsStereo = isStereo;
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onStereoChanged(isStereo);
                }
            });
        }

        @Override
        public void notifyStereoOn(boolean isOn) {
        }
    };

    // Conexión del Servicio Android al servicio NWD
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mNwdService = RadioFeature.Stub.asInterface(service);
            mIsBound = true;
            Log.d(TAG, "QS6 Service onServiceConnected -> Vinculado a NWD RadioService AIDL");

            // V20.0: Registrar receptor de muerte del proceso para detectar caídas del sistema
            try {
                service.linkToDeath(() -> {
                    Log.e(TAG, "!!! QS6 SERVICE DIED !!! El proceso com.nwd.radio.service ha muerto inesperadamente.");
                    mNwdService = null;
                    mIsBound = false;
                    // Intentar recuperar audio si es posible
                    if (mContext != null) {
                        Log.w(TAG, "QS6: Intentando recuperación de emergencia...");
                    }
                }, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Error vinculando DeathRecipient en QS6", e);
            }

            // V17.6: El registro de callbacks y configuración de fondo se hace en un hilo separado
            // para evitar congelar la UI si el servicio remoto está bloqueado inicializando.
            new Thread(() -> {
                Log.d(TAG, "QS6 (Background): Registrando callbacks y modo de fondo...");
                performAidlCall("registCallback", () -> mNwdService.registCallback(mNwdCallback));
                performAidlCall("setRadioBackServiceOn", () -> mNwdService.setRadioBackServiceOn(true));
                Log.d(TAG, "QS6 (Background): Configuración inicial completada.");
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "QS6 Service onServiceDisconnected -> Desvinculado de NWD RadioService");
            mNwdService = null;
            mIsBound = false;
        }
    };

    /**
     * V20.0: Helper para ejecutar llamadas AIDL con detección de muerte de objeto.
     * Si detecta una DeadObjectException, intenta reiniciar el vínculo.
     */
    private interface AidlRunnable {
        void run() throws RemoteException;
    }

    private synchronized void performAidlCall(String callName, AidlRunnable action) {
        if (mNwdService == null) {
            Log.w(TAG, "QS6: Intento de llamada '" + callName + "' con Servicio NWD nulo.");
            return;
        }

        try {
            action.run();
            mRetryCount = 0; // Resetear contador si tiene éxito
        } catch (DeadObjectException e) {
            Log.e(TAG, "!!! QS6 DEAD OBJECT DETECTED en '" + callName + "' !!!");
            handleServiceDeath();
        } catch (RemoteException e) {
            Log.e(TAG, "Error remoto en '" + callName + "'", e);
        }
    }

    private void handleServiceDeath() {
        mNwdService = null;
        mIsBound = false;
        
        if (mRetryCount < MAX_RETRIES) {
            mRetryCount++;
            Log.w(TAG, "QS6: Re-vinculando servicio (Intento " + mRetryCount + "/" + MAX_RETRIES + ")...");
            if (mContext != null) {
                // Forzar un bindService limpio
                init(mContext);
            }
        } else {
            Log.e(TAG, "QS6: Se alcanzó el máximo de reintentos de reconexión. El hardware NWD no responde.");
        }
    }

    public QS6Engine() {
    }

    @Override
    public boolean init(Context context) {
        this.mContext = context;
        Log.d(TAG, "Iniciando motor QS6 (Plan Nivel 2 - NWD AIDL IPC)");

        // 1. Iniciamos el intent al servicio original
        Intent intent = new Intent("com.nwd.radio.service.ACTION_RADIO_SERVICE");
        intent.setPackage("com.nwd.radio.service");

        // Android 11+ requiere resolver el componente si se usa bindService a otro
        // paquete (independientemente del manifest parfois)
        android.content.pm.PackageManager pm = context.getPackageManager();
        java.util.List<android.content.pm.ResolveInfo> resolveInfo = pm.queryIntentServices(intent, 0);

        if (resolveInfo != null && !resolveInfo.isEmpty()) {
            android.content.pm.ResolveInfo serviceInfo = resolveInfo.get(0);
            String packageName = serviceInfo.serviceInfo.packageName;
            String className = serviceInfo.serviceInfo.name;
            ComponentName component = new ComponentName(packageName, className);

            Intent explicitIntent = new Intent(intent);
            explicitIntent.setComponent(component);
            boolean bindResult = context.bindService(explicitIntent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "QS6 Service Bind Request -> Success: " + bindResult + " | " + component.flattenToString());
        } else {
            Log.e(TAG,
                    "QS6 Error: No se ha podido resolver el intent mediante query. Usando component fall-back directo.");

            Intent fallback = new Intent();
            fallback.setComponent(new ComponentName("com.nwd.radio.service", "com.nwd.radio.service.RadioService"));
            boolean fallBind = context.bindService(fallback, mConnection,
                    Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
            Log.d(TAG, "QS6 Service Fallback Bind Request -> Success: " + fallBind);
        }

        // 2. Despertar hardware y encender sistema de audio principal (Source NWD)
        // V17.6: Ejecutamos en hilo de fondo para no penalizar el arranque de la app
        new Thread(() -> {
            try {
                requestWakeUp();
                Thread.sleep(500); // Dar un respiro al hardware
                requestPlayAudio();
            } catch (Exception e) {}
        }).start();

        return true;
    }

    @Override
    public void release() {
        release(false); // Por defecto, liberación completa
    }

    @Override
    public void release(boolean isChangingConfigurations) {
        if (isChangingConfigurations) {
            Log.d(TAG, "QS6: Recreación por cambio de configuración detectada. MANTENIENDO vínculo AIDL vivo.");
            // En QS6, si mantenemos el vínculo (bind) y el callback registrado, 
            // la radio sigue sonando y el hardware no se entera de que la Activity "murió" un instante.
            return;
        }

        Log.d(TAG, "QS6: release() - Soltando recursos y desvinculando AIDL definitivamente");
        if (mIsBound && mNwdService != null) {
            performAidlCall("unRegistCallback", () -> mNwdService.unRegistCallback(mNwdCallback));
            try {
                mContext.unbindService(mConnection);
            } catch (Exception e) {
                Log.e(TAG, "Error al desvincular servicio", e);
            }
            mIsBound = false;
            mNwdService = null;
        }
        // V18.4: Forzar limpieza de hilos o estados si fuera necesario
    }

    @Override
    public void closeDevice() {
        Log.d(TAG, "QS6: Cierre total solicitado (Power Off) - Secuencia V18.4 Definitiva");
        
        // V18.4: Secuencia ultra-agresiva sincronizada
        try {
            // 1. Desvincular callback AIDL INMEDIATAMENTE
            if (mIsBound && mNwdService != null) {
                Log.d(TAG, "QS6 (V18.4): Desvinculando Callback AIDL preventivamente...");
                performAidlCall("unRegistCallback", () -> mNwdService.unRegistCallback(mNwdCallback));
            }

            // 2. Muteo REDUNDANTE
            setMute(true);
            mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_MUTE")); 

            // 3. Detener servicio de audio en segundo plano (AIDL)
            if (mIsBound && mNwdService != null) {
                Log.d(TAG, "QS6 (V18.4): Deteniendo RadioBackService...");
                performAidlCall("setRadioBackServiceOn", () -> mNwdService.setRadioBackServiceOn(false));
            }

            // 4. Cambiar fuente a ANDROID (0) - Indica al MCU que deje de rutear el chip de radio
            // V18.4: Cambiado ACTION_CHANGE_SOURCE por ACTION_REQUEST_CHANGE_SOURCE según logs nativos
            requestStopAudio();
            
            // 5. Notificar salida de aplicación con reset de estado
            Intent inOutIntent = new Intent("com.nwd.action.ACTION_APP_IN_OUT");
            inOutIntent.setPackage("com.nwd.radio.service");
            inOutIntent.putExtra("extra_app_in_out", 0);
            inOutIntent.putExtra("extra_app_reset", 1); 
            mContext.sendBroadcast(inOutIntent);

            // 5.1 Opcional: Multitask Button State (visto en logs nativos)
            Intent multiTaskIntent = new Intent("com.nwd.action.ACTION_MUTILTASK_BUTTON_STATE_CHANGE");
            multiTaskIntent.setPackage("com.nwd.radio.service");
            mContext.sendBroadcast(multiTaskIntent);

            // 5.2 Opcional: Quitar icono de barra de estado
            Intent iconIntent = new Intent("com.nwd.android.ACTION_SET_STATUSBAR_ICON");
            iconIntent.setPackage("com.nwd.radio.service");
            iconIntent.putExtra("type", 0);
            iconIntent.putExtra("state", false);
            mContext.sendBroadcast(iconIntent);

            // 6. Retardo de seguridad EXTENDIDO (1000ms)
            // El MCU de Qualcomm NWD es asíncrono y lento procesando la matriz de audio.
            Log.d(TAG, "QS6 (V18.4): Esperando 1000ms para conmutación de hardware...");
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            
            // 7. Liberación final de recursos (unbind)
            release();

        } catch (Exception e) {
            Log.e(TAG, "Error crítico en secuencia de apagado V18.4", e);
            release();
        }
    }

    @Override
    public String getEngineName() {
        return "QS6-AIDL-Engine";
    }

    @Override
    public void tune(int freqKhz) {
        // V19.1: Escalar sintonía según banda para QS6
        int nwdFreq;
        if (mCurrentFreq < 30000) { // Aproximación para AM
            nwdFreq = freqKhz;
        } else {
            nwdFreq = freqKhz / 10;
        }

        performAidlCall("tune", () -> {
            // La firma de NWD es setCurrentFrequency(frequency, bandType, prefebIndex)
            mNwdService.setCurrentFrequency(nwdFreq, (byte) mCurrentBand, 0);
            Log.d(TAG, "QS6 AIDL TUNE: " + freqKhz + " (" + nwdFreq + ") Band=" + mCurrentBand);
        });
    }

    @Override
    public int getCurrentFreq() {
        return mCurrentFreq;
    }

    @Override
    public int getCurrentBand() {
        return mCurrentBand;
    }

    @Override
    public void seekUp() {
        performAidlCall("seekUp", () -> {
            mNwdService.search(true);
        });
    }

    @Override
    public void seekDown() {
        performAidlCall("seekDown", () -> {
            mNwdService.search(false);
        });
    }

    @Override
    public void stepUp() {
        // V19.1: Paso dinámico. 9 kHz para AM (zona EU), 100 kHz para FM.
        int step = (mCurrentBand < 3) ? 100 : 9;
        tune(mCurrentFreq + step);
    }

    @Override
    public void stepDown() {
        int step = (mCurrentBand < 3) ? 100 : 9;
        tune(mCurrentFreq - step);
    }

    @Override
    public void scan() {
        performAidlCall("scan", () -> {
            mNwdService.AMS();
        });
    }

    @Override
    public void stopScan() {
        performAidlCall("stopScan", () -> {
            mNwdService.changeBand();
        });
    }

    @Override
    public void bandCycle() {
        if (!mIsBound || mNwdService == null) {
            // Fallback por Broadcast si AIDL no está listo
            Intent intent = new Intent(ACTION_KEY_VALUE);
            intent.putExtra("extra_key_value", KEY_FM);
            mContext.sendBroadcast(intent);
            return;
        }
        performAidlCall("bandCycle", () -> {
            mNwdService.changeBand();
        });
    }

    @Override
    public boolean isStereo() {
        return mIsStereo;
    }

    @Override
    public void setStereo(boolean enable) {
        performAidlCall("setStereo", () -> {
            mNwdService.setStreroOn(enable);
        });
    }

    @Override
    public void setMute(boolean mute) {
        try {
            Intent intent = new Intent("com.nwd.action.ACTION_SET_MUTE");
            intent.putExtra("mute", mute ? 1 : 0);
            mContext.sendBroadcast(intent);
            this.mIsMute = mute;
        } catch (Exception e) {
        }
    }

    @Override
    public void openEq(Context context) {
        try {
            Intent intent = new Intent("com.nwd.action.ACTION_START_NWD_ACTIVITY");
            intent.putExtra("pkg", "com.nwd.eq");
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean requestPlayAudio() {
        if (mContext == null)
            return false;
        try {
            Log.d(TAG, "Iniciando Audio NWD Radio -> ACTION_CHANGE_SOURCE a SOURCE_RADIO");
            Intent intent = new Intent(ACTION_CHANGE_SOURCE);
            intent.putExtra("extra_source_id", SOURCE_RADIO);
            mContext.sendBroadcast(intent); // System wide
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void enforceAudioRecovery() {
        Log.d(TAG, "QS6: Forzando recuperación de audio (Recovery V20.0 - Robustness Focus)");
        // V20.0: Incrementar retardos para dar margen al Kernel de Qualcomm (asíncrono)
        new Thread(() -> {
            try {
                requestWakeUp();
                Thread.sleep(800); // Antes 300ms
                requestPlayAudio();
                Thread.sleep(500); // Antes 200ms
                // Forzar sintonía a la frecuencia actual para "enganchar" el tuner
                tune(mCurrentFreq);
                setMute(false);
                Log.d(TAG, "QS6: Audio Recovery completado con re-tune a " + mCurrentFreq);
            } catch (Exception e) {
                Log.e(TAG, "Error en secuencia de recuperación de audio QS6", e);
            }
        }).start();
    }

    @Override
    public void switchToAndroidAudio() {
        requestStopAudio(); // Cambia a SOURCE_ANDROID
    }

    @Override
    public void switchToFmAudio() {
        requestPlayAudio(); // Cambia a SOURCE_RADIO
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {
        // En QS6 el cambio de fuente ya es potente, no necesitamos flag interno por ahora
        if (active) {
            switchToAndroidAudio();
        }
    }

    public void requestStopAudio() {
        try {
            Log.d(TAG, "QS6: Solicitando cambio de fuente -> ACTION_REQUEST_CHANGE_SOURCE (0)");
            // V18.4: En Qualcomm NWD, REQUEST_CHANGE_SOURCE es la vía oficial "amigable"
            Intent intent = new Intent("com.nwd.action.ACTION_REQUEST_CHANGE_SOURCE");
            intent.setPackage("com.nwd.radio.service");
            intent.putExtra("extra_source_id", SOURCE_ANDROID); // 0
            mContext.sendBroadcast(intent);
            
            // También enviamos el ACTION_CHANGE_SOURCE directo por si el gestor de peticiones falla
            Intent intentDirect = new Intent(ACTION_CHANGE_SOURCE);
            intentDirect.setPackage("com.nwd.radio.service");
            intentDirect.putExtra("extra_source_id", SOURCE_ANDROID); // 0
            mContext.sendBroadcast(intentDirect);
        } catch (Exception e) {
            Log.e(TAG, "Error enviando fuente stop", e);
        }
    }

    @Override
    public void toggleRdsFeature(int type) {
        performAidlCall("toggleRdsFeature", () -> {
            // NWD mapea: 0=TA, 1=AF (visto en logs y AIDL)
            byte rdsType = (type == 1) ? (byte) 1 : (byte) 0;
            boolean currentState = mNwdService.getRDSState(rdsType);
            mNwdService.setRDSState(rdsType, !currentState);
            Log.d(TAG, "Toggle RDS Feature " + type + " (NWD Type " + rdsType + ") -> " + !currentState);
        });
    }

    @Override
    public boolean isAfEnabled() {
        return mIsAfEnabled;
    }

    @Override
    public boolean isTaEnabled() {
        return mIsTaEnabled;
    }

    @Override
    public boolean isTpEnabled() {
        return mIsTpEnabled;
    }

    @Override
    public boolean isScanning() {
        return mIsScanning;
    }

    @Override
    public void toggleDxLocal() {
        performAidlCall("toggleDxLocal", () -> {
            boolean isNear = mNwdService.isNearOn();
            mNwdService.setNearOn(!isNear);
        });
    }

    @Override
    public boolean isDxLocal() {
        return mIsDxLocal;
    }

    @Override
    public void gotoPreset(int index) {
        // Podría implementarse con getPrefabFrequency, etc.
    }

    @Override
    public void nextFavorite() {
        performAidlCall("nextFavorite", () -> {
            mNwdService.prefeb(true);
        });
    }

    @Override
    public void prevFavorite() {
        performAidlCall("prevFavorite", () -> {
            mNwdService.prefeb(false);
        });
    }

    @Override
    public void setCallback(RadioEngineCallback cb) {
        this.mCallback = cb;
    }

    /**
     * V17.5: Envía el broadcast mágico que despierta el hardware NWD sin abrir la interfaz.
     */
    private void requestWakeUp() {
        if (mContext == null) return;
        try {
            Log.d(TAG, "QS6: Despertando hardware mediante ACTION_START_NWD_ACTIVITY");
            Intent intent = new Intent(ACTION_START_NWD_ACTIVITY);
            intent.putExtra("pkg", "com.nwd.radio");
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error enviando wake-up broadcast", e);
        }
    }
}
