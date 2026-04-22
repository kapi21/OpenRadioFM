package com.example.openradiofm.data.source;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.example.openradiofm.util.LauncherIntentUtils;
import com.hcn.autoradio.IRadioCallBack;
import com.hcn.autoradio.IRadioServiceAPI;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * V5.0: Motor K706 — Wrapper sobre K706RadioManager.
 *
 * K706RadioManager ya implementa IRadioServiceAPI (AIDL).
 * Esta clase lo envuelve en la interfaz RadioEngine unificada
 * y traduce los callbacks del AIDL a RadioEngineCallback.
 */
public class K706Engine implements RadioEngine {

    private static final String TAG = "K706Engine";

    private K706RadioManager mManager;
    private RadioEngineCallback mCallback;
    // Track AF/TA state internally (K706RadioManager fields are private)
    private boolean mAfEnabled = false;
    private boolean mTaEnabled = false;
    private boolean mTpEnabled = false;

    @Override
    public boolean init(Context context) {
        try {
            mManager = new K706RadioManager(context);

            // Registrar callback AIDL que traduce a RadioEngineCallback
            mManager.registerRadioCallback(new IRadioCallBack.Stub() {
                @Override
                public void onEvent(int code, String data) {
                    if (mCallback == null) return;
                    handleCallback(code, data);
                }
            });

            Log.d(TAG, "K706Engine inicializado correctamente.");

            // K706: mantener AudioFocus solo para routing (mandos OEM / Media keys),
            // sin activar audio FM hasta que el usuario haga PLAY/unmute.
            try {
                mManager.requestAudioFocusOnlyForRouting();
            } catch (Exception ignored) {}

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando K706Engine", e);
            return false;
        }
    }

    @Override
    public void release() {
        release(false); // Por defecto, liberación completa
    }

    @Override
    public void release(boolean persist) {
        if (persist) {
            Log.d(TAG, "release(persist=true): Recreación detectada. Manteniendo K706 vivo.");
            return;
        }

        Log.d(TAG, "release(persist=false): Soltando recursos K706");
        if (mManager != null) {
            try { mManager.closeDevice(); } catch (Exception ignored) {}
        }
        mCallback = null;
        mManager = null;
        com.example.openradiofm.ui.main.RadioServiceController.clearSharedLocalEngineIfSame(this);
    }

    @Override
    public void closeDevice() {
        if (mManager != null) {
            try { mManager.closeDevice(); } catch (RemoteException e) { Log.e(TAG, "closeDevice", e); }
        }
    }

    @Override
    public String getEngineName() {
        return "K706";
    }

    // === Tuning ===

    @Override
    public void tune(int freqKhz) {
        if (mManager == null) return;
        try { mManager.gotoFreq(freqKhz); } catch (RemoteException e) { Log.e(TAG, "tune", e); }
    }

    @Override
    public void setBand(int band) {
        if (mManager == null) return;
        try { mManager.onBandEvent(); } catch (RemoteException e) { Log.e(TAG, "setBand", e); }
    }

    @Override
    public int getCurrentFreq() {
        if (mManager == null) return 87500;
        try {
            // K706RadioManager ya devuelve la freq en kHz a través de updateFrequency
            return mManager.getCurrentFreq();
        } catch (RemoteException e) { Log.e(TAG, "getCurrentFreq", e); return 87500; }
    }

    @Override
    public int getCurrentBand() {
        if (mManager == null) return 0;
        try { return mManager.getCurrentBand(); } catch (RemoteException e) { return 0; }
    }

    @Override
    public void seekUp() {
        if (mManager == null) return;
        try { mManager.onSeekUpEvent(); } catch (RemoteException e) { Log.e(TAG, "seekUp", e); }
    }

    @Override
    public void seekDown() {
        if (mManager == null) return;
        try { mManager.onSeekDownEvent(); } catch (RemoteException e) { Log.e(TAG, "seekDown", e); }
    }

    @Override
    public void stepUp() {
        if (mManager == null) return;
        try { mManager.onManualUpEvent(); } catch (RemoteException e) { Log.e(TAG, "stepUp", e); }
    }

    @Override
    public void stepDown() {
        if (mManager == null) return;
        try { mManager.onManualDownEvent(); } catch (RemoteException e) { Log.e(TAG, "stepDown", e); }
    }

    @Override
    public void scan() {
        if (mManager == null) return;
        try { mManager.onScanEvent(); } catch (RemoteException e) { Log.e(TAG, "scan", e); }
    }

    @Override
    public void stopScan() {
        if (mManager == null) return;
        try { mManager.onPSEvent(); } catch (RemoteException e) { Log.e(TAG, "stopScan", e); }
    }

    @Override
    public boolean isScanning() {
        if (mManager == null) return false;
        try { return mManager.isScanning(); } catch (Exception e) { return false; }
    }

    @Override
    public void bandCycle() {
        if (mManager == null) return;
        try { mManager.onBandEvent(); } catch (RemoteException e) { Log.e(TAG, "bandCycle", e); }
    }

    // === Audio ===

    @Override
    public boolean isStereo() {
        if (mManager == null) return false;
        try { return mManager.IsStereo(); } catch (RemoteException e) { return false; }
    }

    @Override
    public void setStereo(boolean enable) {
        if (mManager == null) return;
        try {
            // V7.2f: Comando hardware 0x10 para Stereo (1) / Mono (0)
            mManager.setStereoMode(enable);
        } catch (Exception e) {
            Log.e(TAG, "setStereo error", e);
        }
    }

    @Override
    public void setMute(boolean mute) {
        if (mManager == null) return;
        try { mManager.setMute(mute); } catch (Exception e) { Log.e(TAG, "setMute error", e); }
    }

    @Override
    public void releaseAudioFocusOnlyForBackground() {
        if (mManager == null) return;
        try {
            mManager.releaseAudioFocusOnlyForBackground();
        } catch (Exception e) {
            Log.w(TAG, "releaseAudioFocusOnlyForBackground", e);
        }
    }

    @Override
    public void openEq(Context context) {
        try {
            android.content.Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.qf.soundeffect");
            if (launchIntent != null) {
                launchIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            } else {
                // Fallback: abrir ajustes de sonido de Android
                android.content.Intent intent = new android.content.Intent("android.intent.action.MAIN");
                intent.setClassName("com.android.settings", "com.android.settings.Settings$SoundSettingsActivity");
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "No se pudo abrir el EQ", e);
        }
    }

    @Override
    public boolean requestPlayAudio() {
        if (mManager == null) return false;
        try { return mManager.requestPlayAudio(); } catch (RemoteException e) { return false; }
    }

    @Override
    public void enforceAudioRecovery() {
        if (mManager == null) return;
        try { mManager.enforceAudioRecovery(); } catch (Exception e) { Log.e(TAG, "enforceAudioRecovery", e); }
    }

    @Override
    public void switchToAndroidAudio() {
        if (mManager == null) return;
        try { 
            mManager.setOnlineStreamingActive(true); // V18.3: Evitar mutes por competencia de foco
            mManager.returnAudioChannel(); 
        } catch (Exception e) { Log.e(TAG, "switchToAndroidAudio", e); }
    }

    @Override
    public void switchToFmAudio() {
        if (mManager == null) return;
        try {
            mManager.setOnlineStreamingActive(false); // V18.3: Volvemos a modo radio estándar
            // requestPlayAudio limpia flags de llamada/transiente y fuerza la secuencia FM completa;
            // enforceAudioChannelRecovery solo no bastaba tras Spotify/AA sin AUDIOFOCUS_GAIN.
            mManager.requestPlayAudio();
        } catch (Exception e) { Log.e(TAG, "switchToFmAudio", e); }
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {
        if (mManager != null) {
            mManager.setOnlineStreamingActive(active);
        }
    }

    @Override
    public boolean isOnlineStreamingActive() {
        return mManager != null && mManager.isOnlineStreamingActive();
    }

    // === RDS ===

    @Override
    public void toggleRdsFeature(int type) {
        if (mManager == null) return;
        try { mManager.toggleRdsFeature(type); } catch (Exception e) { Log.e(TAG, "toggleRdsFeature", e); }
    }

    @Override
    public boolean isAfEnabled() {
        return mAfEnabled;
    }

    @Override
    public boolean isTaEnabled() {
        return mTaEnabled;
    }

    @Override
    public boolean isTpEnabled() {
        return mTpEnabled;
    }

    // === DX/Local ===

    @Override
    public void toggleDxLocal() {
        if (mManager == null) return;
        try { mManager.onLocDxEvent(); } catch (RemoteException e) { Log.e(TAG, "toggleDxLocal", e); }
    }

    @Override
    public boolean isDxLocal() {
        if (mManager == null) return false;
        try { return mManager.IsDxLocal(); } catch (RemoteException e) { return false; }
    }

    // === Presets ===

    @Override
    public void gotoPreset(int index) {
        if (mManager == null) return;
        try { mManager.gotoFreqIndex(index); } catch (RemoteException e) { Log.e(TAG, "gotoPreset", e); }
    }

    @Override
    public void nextFavorite() {
        if (mManager == null) return;
        try { mManager.onNextFavoriteEvent(); } catch (RemoteException e) { Log.e(TAG, "nextFavorite", e); }
    }

    @Override
    public void prevFavorite() {
        if (mManager == null) return;
        try { mManager.onPreFavoriteEvent(); } catch (RemoteException e) { Log.e(TAG, "prevFavorite", e); }
    }

    @Override
    public void setTunerSensitivity(int level) {
        if (mManager != null) {
            mManager.setTunerSensitivity(level);
        }
    }

    // === Callbacks ===

    @Override
    public void setCallback(RadioEngineCallback cb) {
        this.mCallback = cb;
    }

    /** Para combinar con {@link CompositeRadioEngineCallback} cuando el motor es compartido. */
    public RadioEngineCallback getCallback() {
        return mCallback;
    }

    /**
     * Traduce los códigos de callback del AIDL K706 a RadioEngineCallback.
     * Los códigos vienen de IRadioCallBack.RadioCallback(code, data).
     */
    private void handleCallback(int code, String data) {
        if (mCallback == null) return;

        switch (code) {
            case 100: // Frequency changed
                try {
                    int rawFreq = Integer.parseInt(data);
                    // K706RadioManager ya envía la frecuencia en kHz (ej. 87500)
                    mCallback.onFrequencyChanged(rawFreq);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "onEvent(100) frecuencia inválida: " + data, e);
                }
                break;
            case 101: // Band changed
                try {
                    mCallback.onBandChanged(Integer.parseInt(data));
                } catch (NumberFormatException ignored) {}
                break;
            case 102: // Stereo changed
                mCallback.onStereoChanged("1".equals(data));
                break;
            case 103: // RDS PS (name)
                mCallback.onRdsName(data);
                break;
            case 104: // RDS RT (text)
                mCallback.onRdsText(data);
                break;
            case 105: // RDS PTY
                mCallback.onRdsPty(data);
                break;
            case 106: // DX/Local changed
                mCallback.onDxLocalChanged("1".equals(data));
                break;
            case 107: // PI Code Detected
                mCallback.onRdsPi(data);
                break;
            case 108: // Scan Status Changed
                mCallback.onScanStatusChanged("1".equals(data));
                break;
            case 111: // AF/TP status indicators
                if (data != null) {
                    if (data.startsWith("AF:")) {
                        mAfEnabled = data.contains("1");
                    } else if (data.startsWith("TP:")) {
                        mTpEnabled = data.contains("1");
                    }
                    mCallback.onRdsStatus(mAfEnabled, mTaEnabled, mTpEnabled);
                }
                break;
            case 112: // TA switch status from B3
                if (data != null) {
                    if (data.contains(":1")) {
                        mTaEnabled = true;
                    } else if (data.contains(":0")) {
                        mTaEnabled = false;
                    }
                    mCallback.onRdsStatus(mAfEnabled, mTaEnabled, mTpEnabled);
                }
                break;
            case 122: // Lights
                mCallback.onHwAutomationEvent(122, "1".equals(data));
                break;
            case 123: // Reverse
                mCallback.onHwAutomationEvent(123, "1".equals(data));
                break;
            case 124: // Handbrake
                mCallback.onHwAutomationEvent(124, "1".equals(data));
                break;
            case 125: // ACC
                mCallback.onHwAutomationEvent(125, "1".equals(data));
                break;
            default:
                mCallback.onRawEvent(code, data);
                break;
        }
    }

    /**
     * Acceso directo al K706RadioManager para Engineering Dialog.
     */
    public K706RadioManager getManager() {
        return mManager;
    }

    /**
     * Tras conceder {@link android.Manifest.permission#READ_PHONE_STATE} (Android 6+),
     * silencia la FM durante llamadas y restaura al colgar.
     */
    public void registerPhoneStateListenerIfPermitted() {
        if (mManager != null) {
            mManager.registerPhoneStateListenerIfPermitted();
        }
    }

    /**
     * Tras el flash de la radio OEM (p. ej. HiHack), el MCU puede dejar de enviar RDS a este proceso.
     * Re-registra el {@code IMcuListener} sin soltar el resto del HAL.
     */
    public void reassertMcuTelemetryListener() {
        if (mManager == null) return;
        try {
            mManager.reassertMcuInfoListener();
        } catch (Exception e) {
            Log.w(TAG, "reassertMcuTelemetryListener", e);
        }
    }

    /**
     * Acceso como IRadioServiceAPI para uso exclusivo del Engineering Dialog.
     * <b>No llamar desde la UI ni desde otros managers.</b>
     *
     * @deprecated Uso interno únicamente. Acceder solo desde {@code K706EngineeringDialog}.
     */
    @Deprecated
    public IRadioServiceAPI asAidl() {
        return mManager;
    }

    // === Widget / Launcher OEM ===

    /**
     * V23.0: Envía el broadcast {@code com.qf.radio.update_action} al launcher K706/QuickFish.
     * Movido aquí desde WidgetBroadcastManager para que la UI no tenga dependencia directa
     * con clases {@code com.qf.*}.
     */
    @Override
    public void notifyWidgetUpdate(Context context, int freqKhz, int band,
                                   int presetIdx, String rdsName) {
        // Algunas ROMs bloquean este broadcast por permisos/signature; si ocurre una vez,
        // deshabilitamos reintentos para evitar spam de logcat y Binder flood.
        if (QfWidgetBroadcastGuard.isDisabled()) return;
        try {
            final boolean isAm = (band == 3 || band == 4);
            final String freqStr;
            final int nativeFreq;
            if (isAm) {
                freqStr = String.valueOf(freqKhz);
                nativeFreq = freqKhz;
            } else {
                java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
                java.text.DecimalFormatSymbols dfs =
                        new java.text.DecimalFormatSymbols(java.util.Locale.US);
                df.setDecimalFormatSymbols(dfs);
                freqStr = df.format(freqKhz / 1000.0f);
                nativeFreq = freqKhz / 10;
            }

            final String widgetName = (rdsName != null && !rdsName.isEmpty()
                    && !"STATION NAME".equals(rdsName) && !"STATION".equals(rdsName))
                    ? rdsName : "OpenRadioFM";

            android.content.Intent qf = new android.content.Intent("com.qf.radio.update_action");
            qf.putExtra("com.qf.radio.update_action_key",          freqStr);
            qf.putExtra("com.qf.radio.update_action_freq_key",     nativeFreq);
            qf.putExtra("com.qf.radio.update_action_band_key",     band);
            qf.putExtra("com.qf.radio.update_action_preset_key",   presetIdx);
            qf.putExtra("com.qf.radio.update_action_searching_key", false);
            qf.putExtra("com.qf.radio.update_action_name_key",     widgetName);
            // K706 (QuickFish): el widget puede vivir en el HOME real (p. ej. launcher.gradient.black)
            // aunque las clases vengan de com.android.auto.autohome.* — hay que dirigir el broadcast ahí también.
            Set<String> targets = new LinkedHashSet<>();
            targets.add("com.android.launcher.movablecell");
            targets.add("com.android.auto.autohome");
            String home = LauncherIntentUtils.getDefaultHomePackage(context);
            if (home != null && !home.isEmpty()) {
                targets.add(home);
            }
            boolean sent = false;
            for (String pkg : targets) {
                sent |= sendBroadcastToPackage(context, qf, pkg);
            }
            if (!sent) {
                // Último intento: broadcast implícito (puede estar filtrado por la ROM)
                context.sendBroadcast(qf);
            }

            Log.d(TAG, "notifyWidgetUpdate: QF broadcast enviado -> " + freqStr + " band=" + band);
        } catch (SecurityException se) {
            QfWidgetBroadcastGuard.disable(se);
        } catch (Exception e) {
            Log.w(TAG, "notifyWidgetUpdate: error enviando broadcast QF", e);
        }
    }

    private static boolean sendBroadcastToPackage(Context context, android.content.Intent base, String pkg) {
        if (context == null || base == null || pkg == null || pkg.isEmpty()) return false;
        try {
            android.content.Intent i = new android.content.Intent(base);
            i.setPackage(pkg);
            context.sendBroadcast(i);
            return true;
        } catch (SecurityException se) {
            // Si la ROM restringe el destino, dejamos que el guard superior lo gestione.
            throw se;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Guarda estática para cortar reintentos tras "Permission Denial" al broadcast QF.
     * Evita que llamadas frecuentes (cambios de frecuencia) llenen el log y gasten CPU.
     */
    private static final class QfWidgetBroadcastGuard {
        private static volatile boolean sDisabled = false;
        private static volatile boolean sLoggedDisable = false;

        static boolean isDisabled() {
            return sDisabled;
        }

        static void disable(SecurityException se) {
            sDisabled = true;
            if (!sLoggedDisable) {
                sLoggedDisable = true;
                Log.w(TAG, "notifyWidgetUpdate: QF broadcast deshabilitado por permisos (SecurityException)", se);
            }
        }
    }
}

