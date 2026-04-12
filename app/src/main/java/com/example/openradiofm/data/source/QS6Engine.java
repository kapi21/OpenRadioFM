package com.example.openradiofm.data.source;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import java.io.UnsupportedEncodingException;

import com.example.openradiofm.engine.NWDTunerAdapter;
import com.example.openradiofm.ui.main.RadioServiceController;
import com.nwd.radio.service.RadioCallback;

/**
 * V22.3: Motor modular para QS6 G5 (NWD) — Delegando sintonía al NWDTunerAdapter.
 * Corrección de nombres de métodos de RadioCallback y restauración de compatibilidad.
 */
public class QS6Engine implements RadioEngine {
    private static final String TAG = "QS6Engine";
    
    private Context mContext;
    private RadioEngineCallback mCallback;
    private NWDTunerAdapter mAdapter;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private int mCurrentFreq = 87500;
    private int mCurrentBand = 0;
    private boolean mIsMute = false;
    private boolean mIsAfEnabled = false;
    private boolean mIsTaEnabled = false;
    private boolean mIsTpEnabled = false;
    private boolean mIsScanning = false;
    private boolean mIsDxLocal = false;
    private boolean mOnlineStreamingActive = false;

    // Constantes NWD (Verificadas vía RE Fase B)
    private static final String ACTION_CHANGE_SOURCE = "com.nwd.action.ACTION_CHANGE_SOURCE";
    private static final String ACTION_REQUEST_CHANGE_SOURCE = "com.nwd.action.ACTION_REQUEST_CHANGE_SOURCE";
    private static final String ACTION_APP_IN_OUT = "com.nwd.action.ACTION_APP_IN_OUT";
    private static final String ACTION_EXIT_ARM_FM_RAIDO = "com.nwd.android.ACTION_EXIT_ARM_FM_RAIDO"; // RE §B.5: Typo RAIDO obligatorio
    
    // Claves Settings.System (vía RE Fase D: NWD-D001)
    private static final String KEY_NWD_RADIO_BACK_SERVICE_ON = "nwd_radio_back_service_on";
    private static final String KEY_NWD_RADIO_CURRENT_FREQ = "nwd_radio_current_freq";
    private static final String KEY_NWD_RADIO_CURRENT_BAND = "nwd_radio_current_band";
    private static final String KEY_NWD_RADIO_CURRENT_PS_DATA = "nwd_radio_current_ps_data";
    private static final String KEY_MCU_CURRENT_SOURCE = "mcu_current_source";

    private static final int SOURCE_RADIO = 0x04;      // RE: 0x04 = Radio
    private static final int SOURCE_ANDROID = 0x00;    // RE: 0x00 = Android/System
    private static final int APP_ID_RADIO = 8;         // RE: AppID 8 activa InitFM()

    @Override
    public boolean init(Context context) {
        mContext = context;
        mAdapter = NWDTunerAdapter.getInstance(context);
        mAdapter.setCallback(mNwdCallback);
        mAdapter.connect();
        return true;
    }

    private final RadioCallback.Stub mNwdCallback = new RadioCallback.Stub() {
        @Override public void notifyState(byte state) throws RemoteException {}

        @Override public void notifyCurrentFrequency(byte bandType, int frequency, String psName, int prefabIndex) throws RemoteException {
            mMainHandler.post(() -> {
                // V22.4: Mapeo robusto. Algunos firmwares NWD reportan 1-based o 0-based.
                // OpenRadioFM: 0-2 FM, 3-4 AM.
                mCurrentBand = (int) bandType;
                
                // V22.4: Mapeo robusto. NWD reporta en unidades de 10kHz (ej: 8750 o 10150).
                // OpenRadioFM usa kHz (ej: 87500 o 101500).
                int rawFreq = frequency;
                if (bandType < 3) {
                    // FM: 87.5 - 108.0 MHz
                    if (rawFreq < 20000) {
                        mCurrentFreq = rawFreq * 10;
                    } else if (rawFreq > 500000) {
                        mCurrentFreq = rawFreq / 10; // Caso raro: Hz?
                    } else {
                        mCurrentFreq = rawFreq;
                    }
                    if (mCurrentBand >= 3) mCurrentBand = 0;
                } else {
                    // AM: 522 - 1620 kHz
                    mCurrentFreq = rawFreq;
                    if (mCurrentBand < 3) mCurrentBand = 3;
                }

                // V22.5: Independencia Total -> Escribir en Settings.System para que el MCU/OS vean nuestro estado
                syncToNwdSystemSettings(mCurrentFreq, mCurrentBand, psName);

                if (mCallback != null) {
                    mCallback.onFrequencyChanged(mCurrentFreq);
                    mCallback.onBandChanged(mCurrentBand);
                    if (psName != null && !psName.isEmpty()) mCallback.onRdsName(psName);
                }
            });
        }

        @Override public void notifyNearOn(boolean isOn) throws RemoteException {
            mIsDxLocal = isOn;
            if (mCallback != null) mMainHandler.post(() -> mCallback.onDxLocalChanged(mIsDxLocal));
        }

        @Override public void notifyStereo(boolean isStereo) throws RemoteException {}
        @Override public void notifyStereoOn(boolean isOn) throws RemoteException {}

        @Override public void notifyRDSStateChange() throws RemoteException {
            // Notificación genérica de cambio en RDS (AF/TA/TP)
            if (mAdapter != null) {
                mIsAfEnabled = mAdapter.getRDSState(1); // SDK: 1=AF? a verificar
                mIsTaEnabled = mAdapter.getRDSState(2);
                if (mCallback != null) mMainHandler.post(() -> mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled));
            }
        }

        @Override public void notifyCurrentPTYType(byte ptyType) throws RemoteException {
            if (mCallback != null) mMainHandler.post(() -> mCallback.onRdsPty(String.valueOf(ptyType)));
        }

        @Override public void notifyPrefabFrequency(com.nwd.radio.service.data.Frequency[] frequencys) throws RemoteException {}
        @Override public void notifyPrefabPTYType(byte ptyType) throws RemoteException {}
        @Override public void notifyRadioPoint(com.nwd.radio.service.data.RadioPoint[] radioPoints) throws RemoteException {}

        @Override public void notifyCurrentIsTA(boolean isTA) throws RemoteException {
            mIsTaEnabled = isTA;
            if (mCallback != null) mMainHandler.post(() -> mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled));
        }

        @Override public void notifyRdsShowState(boolean isShow) throws RemoteException {}

        @Override public void notifyRtMessage(String rtMessage) throws RemoteException {
            if (mCallback != null) mCallback.onRdsText(rtMessage);
        }

        @Override public void notifyRadioScanState(int state) throws RemoteException {
            mIsScanning = (state != 0);
        }
    };

    @Override
    public void release() {
        closeDevice(); // Asegurar cierre de audio antes de desconectar AIDL
        if (mAdapter != null) {
            mAdapter.disconnect();
        }
        mCallback = null;
        RadioServiceController.clearSharedLocalEngineIfSame(this);
    }

    @Override public void tune(int freqKhz) { 
        mAdapter.tune(freqKhz); 
        mCurrentFreq = freqKhz;
        // V22.5: Forzar actualización a Settings para que el MCU reciba la nueva frecuencia vía KernelService
        syncToNwdSystemSettings(freqKhz, mCurrentBand, "");
    }
    @Override public void seekUp() { mAdapter.seek(true); }
    @Override public void seekDown() { mAdapter.seek(false); }
    
    @Override 
    public void setMute(boolean mute) { 
        mIsMute = mute; 
        // V22.5: En QS6, sendRadioCommand(0x05) cambia de banda.
        // Implementamos muteo vía cambio de fuente para asegurar silencio absoluto sin efectos secundarios.
        if (mute) switchToAndroidAudio();
        else switchToFmAudio();
    }
    @Override public void setBand(int band) { mAdapter.setBand(band); mCurrentBand = band; }
    @Override public void bandCycle() { mAdapter.bandCycle(); }

    @Override public String getEngineName() { return "QS6 (Modular)"; }
    @Override public int getCurrentFreq() { return mCurrentFreq; }
    @Override public int getCurrentBand() { return mCurrentBand; }
    @Override public void stopScan() { mAdapter.stopScan(); }

    @Override
    public boolean requestPlayAudio() {
        Log.d(TAG, "QS6: requestPlayAudio -> SOURCE_RADIO (" + SOURCE_RADIO + ")");
        try {
            // 1. Cambio de fuente (Muestra volumen/MCU routing)
            Intent intentReq = new Intent(ACTION_REQUEST_CHANGE_SOURCE);
            intentReq.putExtra("extra_source_id", (byte) SOURCE_RADIO);
            mContext.sendBroadcast(intentReq);

            Intent intentDirect = new Intent(ACTION_CHANGE_SOURCE);
            intentDirect.putExtra("extra_source_id", (byte) SOURCE_RADIO);
            mContext.sendBroadcast(intentDirect);

            // 2. Activación de App FM (Crítico para InitFM en Sprd/AW)
            // RE §B.3: requiere extra_app_id=8 para pasar de launcher (4) a Radio.
            Intent intentApp = new Intent(ACTION_APP_IN_OUT);
            intentApp.putExtra("extra_app_id", APP_ID_RADIO);
            intentApp.putExtra("extra_app_operation", 1);
            intentApp.putExtra("extra_app_event", 0);
            mContext.sendBroadcast(intentApp);
            
            // V22.6: Asegurar que el sistema vea el cambio de fuente y el servicio activo
            syncToNwdSystemSettings(mCurrentFreq, mCurrentBand, "");
            
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "QS6: Error grave activando audio FM", e);
            return false;
        }
    }

    @Override
    public void switchToAndroidAudio() {
        Log.d(TAG, "QS6: switchToAndroidAudio -> SOURCE_ANDROID");
        try {
            // V22.6: Añadir broadcast de petición para mayor redundancia (simétrico a requestPlayAudio)
            Intent intentReq = new Intent(ACTION_REQUEST_CHANGE_SOURCE);
            intentReq.putExtra("extra_source_id", (byte) SOURCE_ANDROID);
            mContext.sendBroadcast(intentReq);

            Intent intent = new Intent(ACTION_CHANGE_SOURCE);
            intent.putExtra("extra_source_id", (byte) SOURCE_ANDROID);
            mContext.sendBroadcast(intent);

            // Notificamos salida de la App Radio (opcional, ayuda a liberar ARM FM)
            Intent intentApp = new Intent(ACTION_APP_IN_OUT);
            intentApp.putExtra("extra_app_id", APP_ID_RADIO);
            intentApp.putExtra("extra_app_operation", 0); // 0 = Out
            mContext.sendBroadcast(intentApp);
            
            // Forzar sincronización de claves de sistema para que el MCU vea el cambio de fuente
            syncToNwdSystemSettings(mCurrentFreq, mCurrentBand, "");
        } catch (Exception ignored) {}
    }

    @Override public void enforceAudioRecovery() { requestPlayAudio(); tune(mCurrentFreq); }
    @Override public void switchToFmAudio() { requestPlayAudio(); }
    @Override public boolean isOnlineStreamingActive() { return mOnlineStreamingActive; }
    @Override public void setOnlineStreamingActive(boolean active) { 
        mOnlineStreamingActive = active; 
        if (active) switchToAndroidAudio();
    }

    @Override public void toggleRdsFeature(int type) { 
        int bit = (type == 1) ? 0x01 : (type == 2 ? 0x02 : 0x80);
        boolean cur = (type == 1) ? mIsAfEnabled : (type == 2 ? mIsTaEnabled : true);
        mAdapter.setRDSState(bit, !cur);
    }
    
    @Override public boolean isAfEnabled() { return mIsAfEnabled; }
    @Override public boolean isTaEnabled() { return mIsTaEnabled; }
    @Override public boolean isTpEnabled() { return mIsTpEnabled; }
    @Override public boolean isScanning() { return mIsScanning; }
    @Override public void toggleDxLocal() { mAdapter.setRDSState(0x08, !mIsDxLocal); }
    @Override public boolean isDxLocal() { return mIsDxLocal; }
    @Override public void setCallback(RadioEngineCallback cb) { this.mCallback = cb; }

    // === Métodos de compatibilidad para MainActivity y otros ===
    public boolean isNwdServiceBound() { return mAdapter != null && mAdapter.isConnected(); }
    public void tuneWithBand(int freq, int band) { if (mAdapter != null) mAdapter.tuneWithBand(freq, band); }
    public RadioEngineCallback getCallback() { return mCallback; }
    public void wakeNwdRadioFromEngineeringMenu() { requestPlayAudio(); }
    public boolean isStereoPilotReported() { return false; }
    public boolean isStereoDecoderEnabled() { return true; }

    @Override public void stepUp() { tune(mCurrentFreq + 50); }
    @Override public void stepDown() { tune(mCurrentFreq - 50); }
    @Override public void scan() { if (mAdapter != null) mAdapter.autoScan(); }
    @Override public boolean isStereo() { return true; }
    @Override public void setStereo(boolean enable) { if(mAdapter!=null) mAdapter.sendRadioCommand(0x04, enable ? 0x01 : 0x00); }
    @Override public void openEq(Context context) {}
    @Override 
    public void closeDevice() {
        Log.d(TAG, "QS6: closeDevice -> Liberando recursos de audio");
        try {
            // 1. Apagar audio en MCU
            switchToAndroidAudio();
            
            // 2. Mandar señal de salida ARM (RE §B.5)
            mContext.sendBroadcast(new Intent(ACTION_EXIT_ARM_FM_RAIDO));
            
            // 3. Notificar salida de App a través de SourceManager
            Intent intentApp = new Intent(ACTION_APP_IN_OUT);
            intentApp.putExtra("extra_app_id", APP_ID_RADIO);
            intentApp.putExtra("extra_app_operation", 0); // 0 = EXIT/OUT
            mContext.sendBroadcast(intentApp);

            // 4. Apagar persistencia en settings
            Settings.System.putInt(mContext.getContentResolver(), KEY_NWD_RADIO_BACK_SERVICE_ON, 0);
        } catch (Exception e) {
            Log.w(TAG, "Error en closeDevice", e);
        }
    }
    @Override public void gotoPreset(int index) {}
    @Override public void nextFavorite() {}
    @Override public void prevFavorite() {}

    /**
     * V22.5: Sincroniza el estado de OpenRadioFM con las claves de Settings.System de Nowada.
     * Esto permite que el MCUDeviceManager del sistema lea nuestra frecuencia y la envíe al MCU/LCD,
     * logrando independencia total de la app de radio nativa.
     */
    private void syncToNwdSystemSettings(int freqKhz, int band, String psName) {
        try {
            android.content.ContentResolver cr = mContext.getContentResolver();
            
            // 1. Frecuencia (en unidades NWD: décimas de MHz para FM, kHz para AM)
            int nwdFreq = (band < 3) ? freqKhz / 10 : freqKhz;
            Settings.System.putInt(cr, KEY_NWD_RADIO_CURRENT_FREQ, nwdFreq);
            Settings.System.putInt(cr, KEY_NWD_RADIO_CURRENT_BAND, band);
            
            // V22.6: Reflejar el estado de mute en la fuente del sistema para evitar que el MCU
            // fuerce la apertura del audio de radio durante actualizaciones de estado asíncronas.
            int source = mIsMute ? SOURCE_ANDROID : SOURCE_RADIO;
            Settings.System.putInt(cr, KEY_MCU_CURRENT_SOURCE, source);
            Settings.System.putInt(cr, KEY_NWD_RADIO_BACK_SERVICE_ON, mIsMute ? 0 : 1);

            // 2. PS (RDS Name) en formato hexadecimal (RE §D.4)
            if (psName != null && !psName.isEmpty()) {
                String hexPs = bytesToHex(psName.getBytes("UTF-8"));
                if (hexPs.length() > 16) hexPs = hexPs.substring(0, 16); // NWD usa 8 bytes (16 hex)
                Settings.System.putString(cr, KEY_NWD_RADIO_CURRENT_PS_DATA, hexPs);
            }
        } catch (Exception e) {
            Log.w(TAG, "syncToNwdSystemSettings error", e);
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
