package com.example.openradiofm.data.source;

import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import java.io.UnsupportedEncodingException;

import com.example.openradiofm.engine.NWDTunerAdapter;
import com.example.openradiofm.ui.main.Qs6KernelMcuClient;
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
    private Qs6KernelMcuClient mKernel;
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
    // QS6/NWD: el firmware puede no limpiar RT al cambiar de banda/frecuencia.
    private String mLastRt = "";
    /** Anti-spam: evita ráfagas de broadcasts de audio routing en arranque. */
    private long mLastRequestPlayAudioWallMs = 0L;
    /**
     * AutoScan (ScanManager): el barrido lento usa seek/pasos OEM; priorizar AIDL mientras está activo.
     * El resto del tiempo: MCU primero donde aplica.
     */
    private boolean mAutoScanOemPreferred = false;
    /** Un aviso por instancia si el espejo a Settings.System no está permitido en este firmware. */
    private boolean mWarnedNwdSettingsMirrorUnavailable = false;

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
    // Claves no confirmadas: se intentan leer si existen en el firmware.
    private static final String KEY_NWD_RADIO_CURRENT_RT_MESSAGE = "nwd_radio_current_rt_message";
    private static final String KEY_NWD_RADIO_CURRENT_PTY = "nwd_radio_current_pty";

    private static final int SOURCE_RADIO = 0x04;      // RE: 0x04 = Radio
    private static final int SOURCE_ANDROID = 0x00;    // RE: 0x00 = Android/System
    private static final int APP_ID_RADIO = 8;         // RE: AppID 8 activa InitFM()

    @Override
    public boolean init(Context context) {
        mContext = context;
        mKernel = new Qs6KernelMcuClient(context);
        try { mKernel.connect(); } catch (Throwable ignored) {}

        // Mantener AIDL como fallback temporal de RX hasta que exista RX MCU accesible a third‑party.
        // La preferencia es enviar órdenes por MCU (KernelService), no por AIDL.
        mAdapter = NWDTunerAdapter.getInstance(context);
        mAdapter.setCallback(mNwdCallback);
        try { mAdapter.connect(); } catch (Throwable ignored) {}

        try { registerNwdSettingsObservers(); } catch (Throwable ignored) {}

        // QS6: minimizar dependencia del stack OEM.
        // No “arrancar la nativa” (UI) nunca, pero sí puede ser necesario pedir routing de audio
        // si el sistema quedó en SOURCE_ANDROID o tras streaming. Solo hacerlo si parece necesario.
        try { maybeRequestPlayAudioOnStartup(); } catch (Throwable ignored) {}
        return true;
    }

    private final RadioCallback.Stub mNwdCallback = new RadioCallback.Stub() {
        @Override public void notifyState(byte state) throws RemoteException {}

        @Override public void notifyCurrentFrequency(byte bandType, int frequency, String psName, int prefabIndex) throws RemoteException {
            mMainHandler.post(() -> {
                final int prevFreq = mCurrentFreq;
                final int prevBand = mCurrentBand;
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

                if (mCurrentFreq != prevFreq || mCurrentBand != prevBand) {
                    clearRtIfNeeded(false);
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
            // Evitar RT "pegado" fuera de FM / durante transiciones.
            if (mCurrentBand >= 3) return; // AM
            if (rtMessage == null) rtMessage = "";
            final String trimmed = rtMessage.trim();
            if (trimmed.equals(mLastRt)) return;
            mLastRt = trimmed;
            if (mCallback != null) mCallback.onRdsText(trimmed);
        }

        @Override public void notifyRadioScanState(int state) throws RemoteException {
            mIsScanning = (state != 0);
        }
    };

    @Override
    public void release() {
        closeDevice(); // Asegurar cierre de audio antes de desconectar AIDL
        try { unregisterNwdSettingsObservers(); } catch (Throwable ignored) {}
        try { if (mKernel != null) mKernel.disconnect(); } catch (Throwable ignored) {}
        mAutoScanOemPreferred = false;
        if (mAdapter != null) {
            mAdapter.disconnect();
        }
        mCallback = null;
        RadioServiceController.clearSharedLocalEngineIfSame(this);
    }

    @Override public void tune(int freqKhz) { 
        mCurrentFreq = freqKhz;
        if (mAutoScanOemPreferred) {
            try { if (mAdapter != null) mAdapter.tuneWithBand(freqKhz, mCurrentBand); } catch (Throwable ignored) {}
            return;
        }
        // Preferencia: orden directa por MCU (KernelService)
        try {
            if (mKernel != null) {
                int freqUnits = (mCurrentBand < 3) ? Math.max(0, freqKhz / 10) : Math.max(0, freqKhz);
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmTune(freqUnits, (byte) (mCurrentBand & 0xFF), 0));
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU tune failed, fallback to AIDL", t);
        }
        try { if (mAdapter != null) mAdapter.tuneWithBand(freqKhz, mCurrentBand); } catch (Throwable ignored) {}
    }
    @Override public void seekUp() {
        // Seek de emisora (salto de estación): actionType 3/4 en protocolo NWD.
        if (mAutoScanOemPreferred) {
            try { if (mAdapter != null) mAdapter.seek(true); } catch (Throwable ignored) {}
            return;
        }
        try {
            if (mKernel != null) {
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmSearchUp());
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU seekUp failed, fallback to AIDL", t);
        }
        try { if (mAdapter != null) mAdapter.seek(true); } catch (Throwable ignored) {}
    }
    @Override public void seekDown() {
        if (mAutoScanOemPreferred) {
            try { if (mAdapter != null) mAdapter.seek(false); } catch (Throwable ignored) {}
            return;
        }
        try {
            if (mKernel != null) {
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmSearchDown());
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU seekDown failed, fallback to AIDL", t);
        }
        try { if (mAdapter != null) mAdapter.seek(false); } catch (Throwable ignored) {}
    }
    
    @Override 
    public void setMute(boolean mute) { 
        mIsMute = mute; 
        // V22.5: En QS6, sendRadioCommand(0x05) cambia de banda.
        // Implementamos muteo vía cambio de fuente para asegurar silencio absoluto sin efectos secundarios.
        if (mute) switchToAndroidAudio();
        else switchToFmAudio();
    }
    @Override public void setBand(int band) {
        mCurrentBand = band;
        // La forma “segura” por MCU es bandCycle + tuneWithBand. Aquí mantenemos AIDL como fallback.
        // En la práctica, la app cambia banda al sintonizar con banda explícita.
        try { if (mAdapter != null) mAdapter.setBand(band); } catch (Throwable ignored) {}
    }
    @Override public void bandCycle() {
        try {
            if (mKernel != null) {
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmBandCycle());
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU bandCycle failed, fallback to AIDL", t);
        }
        try { if (mAdapter != null) mAdapter.bandCycle(); } catch (Throwable ignored) {}
    }

    @Override public String getEngineName() { return "QS6 (Modular)"; }
    @Override public int getCurrentFreq() { return mCurrentFreq; }
    @Override public int getCurrentBand() { return mCurrentBand; }
    @Override public void stopScan() {
        // STOP_SEARCH: broadcast “shadow” (hay firmwares donde no existe stop por MCU).
        try { if (mAdapter != null) mAdapter.stopScan(); } catch (Throwable ignored) {}
    }

    @Override
    public boolean requestPlayAudio() {
        Log.d(TAG, "QS6: requestPlayAudio -> SOURCE_RADIO (" + SOURCE_RADIO + ")");
        try {
            // Anti-spam (arranque/recreate/resume)
            long now = android.os.SystemClock.elapsedRealtime();
            if (now - mLastRequestPlayAudioWallMs < 1200L) {
                Log.d(TAG, "QS6: requestPlayAudio suppressed (burst)");
                return true;
            }
            mLastRequestPlayAudioWallMs = now;

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
    @Override
    public void toggleDxLocal() {
        // UI espera "isLocal": true -> LOC (icono lleno), false -> DX.
        final boolean targetLocal = !mIsDxLocal;
        // Preferencia: MCU (KernelService) usando NearOn.
        try {
            if (mKernel != null) {
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmNearOn(targetLocal));
                mIsDxLocal = targetLocal;
                if (mCallback != null) mMainHandler.post(() -> mCallback.onDxLocalChanged(mIsDxLocal));
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU toggleDxLocal failed, fallback to AIDL", t);
        }
        try {
            if (mAdapter != null) {
                mAdapter.setRDSState(0x08, targetLocal);
            }
        } catch (Throwable ignored) {}
        mIsDxLocal = targetLocal;
        if (mCallback != null) mMainHandler.post(() -> mCallback.onDxLocalChanged(mIsDxLocal));
    }
    @Override public boolean isDxLocal() { return mIsDxLocal; }
    @Override public void setCallback(RadioEngineCallback cb) { this.mCallback = cb; }

    // === Métodos de compatibilidad para MainActivity y otros ===
    public boolean isNwdServiceBound() { return mAdapter != null && mAdapter.isConnected(); }

    /** Llamado por ScanManager al inicio/fin del AutoScan lento (QS6). */
    public void setAutoScanOemPreferred(boolean oemPreferred) {
        mAutoScanOemPreferred = oemPreferred;
    }

    public void tuneWithBand(int freqKhz, int band) {
        mCurrentFreq = freqKhz;
        mCurrentBand = band;
        if (mAutoScanOemPreferred) {
            try { if (mAdapter != null) mAdapter.tuneWithBand(freqKhz, band); } catch (Throwable ignored) {}
            return;
        }
        try {
            if (mKernel != null) {
                int freqUnits = (band < 3) ? Math.max(0, freqKhz / 10) : Math.max(0, freqKhz);
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmTune(freqUnits, (byte) (band & 0xFF), 0));
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU tuneWithBand failed, fallback to AIDL", t);
        }
        try { if (mAdapter != null) mAdapter.tuneWithBand(freqKhz, band); } catch (Throwable ignored) {}
    }
    public RadioEngineCallback getCallback() { return mCallback; }
    public void wakeNwdRadioFromEngineeringMenu() { requestPlayAudio(); }
    public boolean isStereoPilotReported() { return false; }
    public boolean isStereoDecoderEnabled() { return true; }

    @Override
    public void stepUp() {
        // En QS6/NWD, el “paso fino” observado en campo se corresponde con ACTION (1,1)/(2,1).
        if (mAutoScanOemPreferred) {
            tune(mCurrentFreq + 50);
            return;
        }
        try {
            if (mKernel != null) {
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmSeekUp());
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU stepUp failed, fallback to tune(+50)", t);
        }
        tune(mCurrentFreq + 50);
    }

    @Override
    public void stepDown() {
        if (mAutoScanOemPreferred) {
            tune(mCurrentFreq - 50);
            return;
        }
        try {
            if (mKernel != null) {
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmSeekDown());
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU stepDown failed, fallback to tune(-50)", t);
        }
        tune(mCurrentFreq - 50);
    }
    @Override public void scan() {
        // Consigna: el autoscan lo gestionamos con nuestro ScanManager (no AMS/OEM).
        // Dejamos no-op para evitar disparar la app nativa/servicio OEM.
        Log.d(TAG, "scan(): no-op (ScanManager propio)");
    }
    @Override public boolean isStereo() {
        // El SDK expone estado estéreo, pero no siempre es fiable/instantáneo.
        // Para evitar que la UI oscile, mantenemos una respuesta conservadora.
        return true;
    }
    @Override public void setStereo(boolean enable) {
        // Preferencia: MCU (KernelService). Si no está soportado por firmware, fallback a AIDL.
        try {
            if (mKernel != null) {
                mKernel.requestRaw(Qs6KernelMcuClient.buildFmSetStereoOn(enable));
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "MCU setStereo failed, fallback to AIDL", t);
        }
        try { if (mAdapter != null) mAdapter.setStereoOn(enable); } catch (Throwable ignored) {}
    }
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
    @Override
    public void gotoPreset(int index) {
        // En QS6, OpenRadioFM gestiona presets propios (SharedPreferences) desde MainActivity.
        // Este método se usa sobre todo por integraciones genéricas; si se llama aquí,
        // intentamos delegar al OEM “prefeb” cuando sea posible.
        try {
            if (mAdapter != null) {
                // No existe un "gotoPreset(index)" documentado en RadioFeature; prefeb navega por lista interna.
                // Mantenemos comportamiento no-op seguro.
                Log.d(TAG, "gotoPreset(" + index + "): no soportado por NWD prefeb (usa MainActivity presets)");
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void nextFavorite() {
        // NWD: prefeb(true) → siguiente “prefab/preset” del servicio OEM (si existe).
        try { if (mAdapter != null) mAdapter.prefeb(true); } catch (Throwable ignored) {}
    }

    @Override
    public void prevFavorite() {
        try { if (mAdapter != null) mAdapter.prefeb(false); } catch (Throwable ignored) {}
    }

    // =============================================================================================
    // RX “Shadow” (Settings.System) — para reducir dependencia de callbacks AIDL.
    // =============================================================================================

    private ContentObserver mNwdSettingsObserver;
    private boolean mObserversRegistered = false;

    private void registerNwdSettingsObservers() {
        if (mContext == null || mObserversRegistered) return;
        android.content.ContentResolver cr = mContext.getContentResolver();
        mNwdSettingsObserver = new ContentObserver(mMainHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                try { pollNwdSettingsAndFire(); } catch (Throwable ignored) {}
            }
        };
        try { cr.registerContentObserver(Settings.System.getUriFor(KEY_NWD_RADIO_CURRENT_FREQ), false, mNwdSettingsObserver); } catch (Throwable ignored) {}
        try { cr.registerContentObserver(Settings.System.getUriFor(KEY_NWD_RADIO_CURRENT_BAND), false, mNwdSettingsObserver); } catch (Throwable ignored) {}
        try { cr.registerContentObserver(Settings.System.getUriFor(KEY_NWD_RADIO_CURRENT_PS_DATA), false, mNwdSettingsObserver); } catch (Throwable ignored) {}
        try { cr.registerContentObserver(Settings.System.getUriFor(KEY_NWD_RADIO_CURRENT_RT_MESSAGE), false, mNwdSettingsObserver); } catch (Throwable ignored) {}
        try { cr.registerContentObserver(Settings.System.getUriFor(KEY_NWD_RADIO_CURRENT_PTY), false, mNwdSettingsObserver); } catch (Throwable ignored) {}
        mObserversRegistered = true;
        // Primer pull inmediato
        try { pollNwdSettingsAndFire(); } catch (Throwable ignored) {}
    }

    private void unregisterNwdSettingsObservers() {
        if (mContext == null || !mObserversRegistered) return;
        try {
            if (mNwdSettingsObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(mNwdSettingsObserver);
            }
        } catch (Throwable ignored) {
        } finally {
            mNwdSettingsObserver = null;
            mObserversRegistered = false;
        }
    }

    private void pollNwdSettingsAndFire() {
        if (mContext == null) return;
        android.content.ContentResolver cr = mContext.getContentResolver();

        int band = mCurrentBand;
        int freqKhz = mCurrentFreq;
        try {
            band = Settings.System.getInt(cr, KEY_NWD_RADIO_CURRENT_BAND, mCurrentBand);
            int nwdFreq = Settings.System.getInt(cr, KEY_NWD_RADIO_CURRENT_FREQ, (band < 3) ? (mCurrentFreq / 10) : mCurrentFreq);
            freqKhz = (band < 3) ? (nwdFreq * 10) : nwdFreq;
        } catch (Throwable ignored) {}

        boolean bandChanged = (band != mCurrentBand);
        if (band != mCurrentBand) {
            mCurrentBand = band;
            if (mCallback != null) mCallback.onBandChanged(band);
        }
        boolean freqChanged = (freqKhz > 0 && freqKhz != mCurrentFreq);
        if (freqChanged) {
            mCurrentFreq = freqKhz;
            if (mCallback != null) mCallback.onFrequencyChanged(freqKhz);
        }

        // PS: hex de 8 bytes (16 hex) según RE §D.4
        try {
            String hexPs = Settings.System.getString(cr, KEY_NWD_RADIO_CURRENT_PS_DATA);
            String ps = decodeHexPsToString(hexPs);
            if (ps != null && !ps.isEmpty() && mCallback != null) mCallback.onRdsName(ps);
        } catch (Throwable ignored) {}

        // RT/PTY: solo si el firmware publica claves (no garantizado).
        try {
            String rt = Settings.System.getString(cr, KEY_NWD_RADIO_CURRENT_RT_MESSAGE);
            // Limpiar RT si cambiamos a AM o si cambia banda/frecuencia (evita "RT pegado" al pasar por AM).
            if (mCurrentBand >= 3 || bandChanged || freqChanged) {
                clearRtIfNeeded(false);
            } else if (rt != null) {
                String trimmed = rt.trim();
                if (!trimmed.isEmpty() && !trimmed.equals(mLastRt) && mCallback != null) {
                    mLastRt = trimmed;
                    mCallback.onRdsText(trimmed);
                }
            }
        } catch (Throwable ignored) {}

        try {
            String pty = Settings.System.getString(cr, KEY_NWD_RADIO_CURRENT_PTY);
            if (pty != null && !pty.trim().isEmpty() && mCallback != null) mCallback.onRdsPty(pty);
        } catch (Throwable ignored) {}
    }

    private void clearRtIfNeeded(boolean alsoClearSettings) {
        try {
            if (!mLastRt.isEmpty()) {
                mLastRt = "";
                if (mCallback != null) mCallback.onRdsText("");
            }
        } catch (Throwable ignored) {}

        if (!alsoClearSettings || mContext == null) return;
        try {
            Settings.System.putString(mContext.getContentResolver(), KEY_NWD_RADIO_CURRENT_RT_MESSAGE, "");
        } catch (Throwable ignored) {}
    }

    private static String decodeHexPsToString(String hex) {
        if (hex == null) return null;
        String clean = hex.trim().replace(" ", "");
        if (clean.isEmpty() || (clean.length() % 2) != 0) return null;
        int n = clean.length() / 2;
        if (n <= 0) return null;
        byte[] bytes = new byte[n];
        try {
            for (int i = 0; i < n; i++) {
                int hi = Character.digit(clean.charAt(i * 2), 16);
                int lo = Character.digit(clean.charAt(i * 2 + 1), 16);
                if (hi < 0 || lo < 0) return null;
                bytes[i] = (byte) ((hi << 4) | lo);
            }
            String s = new String(bytes, "UTF-8");
            // El PS suele venir con padding \0 o espacios
            s = s.replace("\u0000", "").trim();
            return s;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * V22.5: Sincroniza el estado de OpenRadioFM con las claves de Settings.System de Nowada.
     * Esto permite que el MCUDeviceManager del sistema lea nuestra frecuencia y la envíe al MCU/LCD,
     * logrando independencia total de la app de radio nativa.
     */
    private void syncToNwdSystemSettings(int freqKhz, int band, String psName) {
        if (mContext == null) return;
        ContentResolver cr = mContext.getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(mContext)) {
            warnNwdSettingsMirrorOnce("Settings.System.canWrite=false");
            return;
        }
        int nwdFreq = (band < 3) ? freqKhz / 10 : freqKhz;
        int source = mIsMute ? SOURCE_ANDROID : SOURCE_RADIO;
        boolean any = false;
        any |= safePutSystemInt(cr, KEY_NWD_RADIO_CURRENT_FREQ, nwdFreq);
        any |= safePutSystemInt(cr, KEY_NWD_RADIO_CURRENT_BAND, band);
        any |= safePutSystemInt(cr, KEY_MCU_CURRENT_SOURCE, source);
        any |= safePutSystemInt(cr, KEY_NWD_RADIO_BACK_SERVICE_ON, mIsMute ? 0 : 1);
        if (psName != null && !psName.isEmpty()) {
            try {
                String hexPs = bytesToHex(psName.getBytes("UTF-8"));
                if (hexPs.length() > 16) hexPs = hexPs.substring(0, 16);
                any |= safePutSystemString(cr, KEY_NWD_RADIO_CURRENT_PS_DATA, hexPs);
            } catch (Throwable ignored) {}
        }
        if (!any) {
            warnNwdSettingsMirrorOnce("claves NWD rechazadas por el sistema (p. ej. definidas como Secure)");
        }
    }

    private void warnNwdSettingsMirrorOnce(String reason) {
        if (mWarnedNwdSettingsMirrorUnavailable) return;
        mWarnedNwdSettingsMirrorUnavailable = true;
        Log.w(TAG, "Espejo NWD en Settings no disponible: " + reason
                + ". La UI RDS sigue por AIDL; si el equipo lo permite, concede modificación de ajustes del sistema.");
    }

    private static boolean safePutSystemInt(ContentResolver cr, String key, int value) {
        try {
            Settings.System.putInt(cr, key, value);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean safePutSystemString(ContentResolver cr, String key, String value) {
        try {
            Settings.System.putString(cr, key, value);
            return true;
        } catch (Throwable ignored) {
            return false;
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

    /**
     * Arranque QS6: pedir SOURCE_RADIO solo si parece necesario.
     * Evita tocar audio si estamos muteados o en streaming.
     */
    private void maybeRequestPlayAudioOnStartup() {
        if (mContext == null) return;
        if (mIsMute) return;
        if (mOnlineStreamingActive) return;

        try {
            android.content.ContentResolver cr = mContext.getContentResolver();
            int source = Settings.System.getInt(cr, KEY_MCU_CURRENT_SOURCE, -1);
            int backSvc = Settings.System.getInt(cr, KEY_NWD_RADIO_BACK_SERVICE_ON, -1);

            // Si ya está en radio, no forzar.
            if (source == SOURCE_RADIO) {
                return;
            }
            // Si hay back service ON pero fuente desconocida, evitamos interferir en caliente.
            // Solo forzar si parece que la radio está "apagada" (backSvc==0) o sin dato.
            if (backSvc == 1 && source == -1) {
                return;
            }
        } catch (Throwable ignored) {
            // Si no podemos leer settings, dejamos el arranque neutro (sin forzar audio).
            return;
        }

        // Diferido: deja que el sistema asiente callbacks iniciales y evita choque con tune de arranque.
        mMainHandler.postDelayed(() -> {
            try {
                if (mIsMute || mOnlineStreamingActive) return;
                requestPlayAudio();
            } catch (Throwable ignored) {}
        }, 450L);
    }
}
