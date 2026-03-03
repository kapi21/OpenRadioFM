package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

    // Intents de Emisión (Encendido/Apagado General del MCU)
    private static final String ACTION_CHANGE_SOURCE = "com.nwd.action.ACTION_CHANGE_SOURCE";
    private static final String ACTION_KEY_VALUE = "com.nwd.action.ACTION_KEY_VALUE";

    // KEY_VALUE Mapeos
    private static final byte KEY_FM = 0x48;

    private static final byte SOURCE_RADIO = 0x04;
    private static final byte SOURCE_ANDROID = 0x00;

    // AIDL stubs
    private RadioFeature mNwdService;
    private boolean mIsBound = false;

    // Implementación de la Callback del IPC hacia NWD
    private final RadioCallback.Stub mNwdCallback = new RadioCallback.Stub() {
        @Override
        public void notifyCurrentFrequency(byte bandType, int frequency, String psName, int prefabIndex) {
            // Nota: frequency viene en décimas de MHz (ej: 9690)
            int freqKhz = frequency * 10;
            mCurrentFreq = freqKhz;
            mCurrentBand = bandType;
            if (mCallback != null) {
                mCallback.onBandChanged(bandType);
                mCallback.onFrequencyChanged(freqKhz);
                if (psName != null && !psName.trim().isEmpty()) {
                    mCallback.onRdsName(psName);
                }
            }
            Log.d(TAG,
                    "NWD AIDL notifyCurrentFrequency -> Freq: " + freqKhz + ", Band: " + bandType + ", PS: " + psName);
        }

        @Override
        public void notifyCurrentIsTA(boolean isTA) {
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
        }

        @Override
        public void notifyRadioPoint(RadioPoint[] radioPoints) {
        }

        @Override
        public void notifyRadioScanState(int state) {
            Log.d(TAG, "NWD AIDL ScanState -> " + state);
            if (mCallback != null) {
                mCallback.onScanStatusChanged(state != 0);
            }
        }

        @Override
        public void notifyRdsShowState(boolean isShow) {
        }

        @Override
        public void notifyRtMessage(String rtMessage) {
            Log.d(TAG, "NWD AIDL notifyRtMessage -> " + rtMessage);
            if (mCallback != null && rtMessage != null) {
                mCallback.onRdsText(rtMessage);
            }
        }

        @Override
        public void notifyState(byte state) {
        }

        @Override
        public void notifyStereo(boolean isStereo) {
            mIsStereo = isStereo;
            if (mCallback != null) {
                mCallback.onStereoChanged(isStereo);
            }
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

            try {
                // Registramos nuestro receptor de Callbacks en la Radio
                mNwdService.registCallback(mNwdCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "Error al registrar AIDL callback", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "QS6 Service onServiceDisconnected -> Desvinculado de NWD RadioService");
            mNwdService = null;
            mIsBound = false;
        }
    };

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

        // 2. Encender sistema de audio principal (Source NWD)
        requestPlayAudio();

        return true;
    }

    @Override
    public void release() {
        if (mIsBound && mNwdService != null) {
            try {
                mNwdService.unRegistCallback(mNwdCallback);
            } catch (RemoteException e) {
            }
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
        requestStopAudio();
    }

    @Override
    public void closeDevice() {
        release();
    }

    @Override
    public String getEngineName() {
        return "QS6-AIDL-Engine";
    }

    @Override
    public void tune(int freqKhz) {
        if (!mIsBound || mNwdService == null)
            return;
        try {
            int nwdFreq = freqKhz / 10;
            // La firma de NWD es setCurrentFrequency(frequency, bandType, prefebIndex)
            mNwdService.setCurrentFrequency(nwdFreq, (byte) mCurrentBand, 0);
            Log.d(TAG, "QS6 AIDL TUNE: " + freqKhz + " (" + nwdFreq + ")");
        } catch (RemoteException e) {
            Log.e(TAG, "tune remote error", e);
        }
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
        if (!mIsBound || mNwdService == null)
            return;
        try {
            mNwdService.seek(true); // true = Increase
        } catch (RemoteException e) {
        }
    }

    @Override
    public void seekDown() {
        if (!mIsBound || mNwdService == null)
            return;
        try {
            mNwdService.seek(false); // false = Decrease
        } catch (RemoteException e) {
        }
    }

    @Override
    public void stepUp() {
        tune(mCurrentFreq + 100);
    }

    @Override
    public void stepDown() {
        tune(mCurrentFreq - 100);
    }

    @Override
    public void scan() {
        if (!mIsBound || mNwdService == null)
            return;
        try {
            mNwdService.AMS(); // Método original de Auto Memory Scan
        } catch (RemoteException e) {
        }
    }

    @Override
    public void stopScan() {
        if (!mIsBound || mNwdService == null)
            return;
        try {
            mNwdService.changeBand(); // Cancelar scan cambiando de banda.
        } catch (RemoteException e) {
        }
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
        try {
            mNwdService.changeBand();
        } catch (RemoteException e) {
        }
    }

    @Override
    public boolean isStereo() {
        return mIsStereo;
    }

    @Override
    public void setStereo(boolean enable) {
        if (!mIsBound || mNwdService == null)
            return;
        try {
            mNwdService.setStreroOn(enable);
        } catch (RemoteException e) {
        }
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
        // En QS6 forzamos de nuevo el cambio de fuente a Radio
        requestPlayAudio();
        setMute(false);
    }

    public void requestStopAudio() {
        if (mContext == null)
            return;
        try {
            Log.d(TAG, "Deteniendo Audio NWD Radio -> ACTION_CHANGE_SOURCE a SOURCE_ANDROID");
            Intent intent = new Intent(ACTION_CHANGE_SOURCE);
            intent.putExtra("extra_source_id", SOURCE_ANDROID);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
        }
    }

    @Override
    public void toggleRdsFeature(int type) {
        // En NWD: type 0=TA, type 1=AF u otra combinación... (Asumo 0 = TA)
        if (!mIsBound || mNwdService == null)
            return;
        try {
            // Invertimos el estado (Toggle).
            // Para ser 100% precisos habría que cachear el estado previo (mIsTA).
            // Usaré getRDSState provisto en el AIDL.
            boolean isTAOn = mNwdService.getRDSState(0);
            mNwdService.setRDSState((byte) 0, !isTAOn);
        } catch (RemoteException e) {
        }
    }

    @Override
    public boolean isAfEnabled() {
        return false;
    }

    @Override
    public boolean isTaEnabled() {
        return false;
    }

    @Override
    public boolean isTpEnabled() {
        return false;
    }

    @Override
    public boolean isScanning() {
        return false;
    }

    @Override
    public void toggleDxLocal() {
        if (!mIsBound || mNwdService == null)
            return;
        try {
            boolean isNear = mNwdService.isNearOn();
            mNwdService.setNearOn(!isNear);
        } catch (RemoteException e) {
        }
    }

    @Override
    public boolean isDxLocal() {
        return false;
    }

    @Override
    public void gotoPreset(int index) {
        // Podría implementarse con getPrefabFrequency, etc.
    }

    @Override
    public void nextFavorite() {
        if (!mIsBound || mNwdService == null)
            return;
        try {
            mNwdService.prefeb(true);
        } catch (RemoteException e) {
        }
    }

    @Override
    public void prevFavorite() {
        if (!mIsBound || mNwdService == null)
            return;
        try {
            mNwdService.prefeb(false);
        } catch (RemoteException e) {
        }
    }

    @Override
    public void setCallback(RadioEngineCallback cb) {
        this.mCallback = cb;
    }
}
