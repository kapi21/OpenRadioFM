package com.example.openradiofm.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.hcn.autoradio.IRadioServiceAPI;
import com.example.openradiofm.data.source.RadioEngine;
import com.example.openradiofm.data.source.MT8163Engine;
import com.example.openradiofm.service.RadioMediaService;

import android.os.Build;

import java.util.List;

/**
 * Gestiona el descubrimiento de hardware y la conexión al servicio de radio
 * AIDL.
 */
public class RadioServiceController {
    private static final String TAG = "RadioServiceController";

    /** Evita ráfagas de bind a com.hcn.autoradio (varias instancias de este controller). */
    private static final Object sMt8163StartLock = new Object();
    private static long sLastMt8163StartWallMs;

    /**
     * Motor instanciado localmente (QS6/K706) compartido entre MainActivity y RadioMediaService.
     * Ambos crean su propio RadioServiceController; sin esto se llamaría a start() dos veces y habría
     * doble {@code QS6Engine} / doble bind a NWD.
     */
    private static volatile RadioEngine sSharedLocalEngine;
    private static final Object SHARED_LOCAL_ENGINE_LOCK = new Object();

    /**
     * Llamar desde {@link com.example.openradiofm.data.source.QS6Engine#release()} (y K706 si aplica)
     * para no reutilizar un motor ya cerrado.
     */
    public static void clearSharedLocalEngineIfSame(RadioEngine engine) {
        if (engine == null) return;
        synchronized (SHARED_LOCAL_ENGINE_LOCK) {
            if (sSharedLocalEngine == engine) {
                sSharedLocalEngine = null;
                Log.d(TAG, "Motor local compartido liberado (referencia única)");
            }
        }
    }

    public interface ServiceListener {
        void onModeDetected(MainActivity.FmMode mode);

        void onEngineReady(RadioEngine engine);

        void onServiceConnected(IRadioServiceAPI service);

        void onServiceDisconnected();
    }

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final ServiceListener mListener;
    private IRadioServiceAPI mRadioService;
    private boolean mBound = false;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Servicio conectado: " + name.flattenToShortString());
            mRadioService = IRadioServiceAPI.Stub.asInterface(service);
            mBound = true;

            // Re-evaluar modo basado en el paquete conectado
            MainActivity.FmMode newMode = MainActivity.FmMode.FM_BASICO;
            String pkg = name.getPackageName();
            if (pkg.equals("com.hcn.autoradio"))
                newMode = MainActivity.FmMode.FM_MT8163;
            else if (pkg.equals("com.nwd.radio.service"))
                newMode = MainActivity.FmMode.FM_QS6;

            if (mListener != null) {
                mListener.onModeDetected(newMode);
                mListener.onServiceConnected(mRadioService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "Servicio DESCONECTADO (Muerte o force-stop): " + name.flattenToShortString());
            mRadioService = null;
            mBound = false;
            if (mListener != null) {
                mListener.onServiceDisconnected();
            }
        }
    };

    public RadioServiceController(Context context, SharedPreferences prefs, ServiceListener listener) {
        this.mContext = context;
        this.mPrefs = prefs;
        this.mListener = listener;
    }

    /**
     * MT8163/HCN: un solo {@code bindService} debe vivir en MainActivity. RadioMediaService
     * no debe duplicar la vinculación (provoca force-stop en SourceService de algunos OEM).
     */
    public boolean isFmMt8163Mode() {
        return detectMode() == MainActivity.FmMode.FM_MT8163;
    }

    /** K706: modo MCU Topway; mandos en segundo plano dependen de sesión de medios + servicio en foreground. */
    public boolean isK706Mode() {
        return detectMode() == MainActivity.FmMode.FM_K706;
    }

    public void start() {
        MainActivity.FmMode mode = detectMode();
        Log.i(TAG, "=> START() INVOCADO. MODO DETECTADO: " + mode);

        if (mListener != null) {
            mListener.onModeDetected(mode);
        }

        // Si es K706, no necesita servicio AIDL de tipo IRadioServiceAPI (usa
        // McuService)
        if (mode == MainActivity.FmMode.FM_K706) {
            try {
                synchronized (SHARED_LOCAL_ENGINE_LOCK) {
                    if (sSharedLocalEngine instanceof com.example.openradiofm.data.source.K706Engine) {
                        Log.i(TAG, "=> K706Engine ya activo — reutilizando instancia compartida");
                        if (mListener != null)
                            mListener.onEngineReady(sSharedLocalEngine);
                        return;
                    }
                    com.example.openradiofm.data.source.K706Engine engine = new com.example.openradiofm.data.source.K706Engine();
                    if (engine.init(mContext)) {
                        sSharedLocalEngine = engine;
                        if (mListener != null)
                            mListener.onEngineReady(engine);
                    } else {
                        Log.w(TAG, "Error iniciando K706Engine (init devolvió false)");
                    }
                }
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error iniciando K706Engine", e);
            }
        } else if (mode == MainActivity.FmMode.FM_QS6) {
            // V14.0: QS6 (NWD) ahora utiliza un engine que se auto-vincula (bindService
            // interno en init)
            try {
                synchronized (SHARED_LOCAL_ENGINE_LOCK) {
                    if (sSharedLocalEngine instanceof com.example.openradiofm.data.source.QS6Engine) {
                        Log.i(TAG, "=> QS6Engine ya activo — reutilizando instancia compartida (evita doble START MainActivity+RadioMediaService)");
                        if (mListener != null)
                            mListener.onEngineReady(sSharedLocalEngine);
                        return;
                    }
                    Log.i(TAG, "=> RAMA QS6 ALCANZADA. INSTANCIANDO MOTOR QS6Engine...");
                    com.example.openradiofm.data.source.QS6Engine engine = new com.example.openradiofm.data.source.QS6Engine();
                    Log.i(TAG, "=> QS6Engine INSTANCIADO OK. LLAMANDO A .init()...");
                    if (engine.init(mContext)) {
                        Log.i(TAG, "=> QS6Engine INIT EXITOSO. AVISANDO A LA INTERFAZ...");
                        sSharedLocalEngine = engine;
                        try {
                            Intent mediaSvc = new Intent(mContext, RadioMediaService.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mContext.startForegroundService(mediaSvc);
                            } else {
                                mContext.startService(mediaSvc);
                            }
                            Log.i(TAG, "QS6: RadioMediaService arrancado en paralelo (AudioFocus / sesión medios)");
                        } catch (Exception e) {
                            Log.w(TAG, "QS6: no se pudo arrancar RadioMediaService anticipado", e);
                        }
                        if (mListener != null)
                            mListener.onEngineReady(engine);
                    } else {
                        Log.w(TAG, "Error iniciando QS6Engine V14 (init devolvió false)");
                    }
                }
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error iniciando QS6Engine", e);
            }
        }

        // Post-streaming en ROM OEM: bind a com.hcn.autoradio desde aquí dispara
        // SourceService.muxMediaPlayer → forceStopPackage(com.example.openradiofm).
        // onResume() ya omite el bind, pero start() se ejecuta antes (p. ej. recreación por layout).
        if (MT8163Engine.isHcnServiceBindBlockedAfterStreamEnd()) {
            Log.i(TAG, "start(): omitiendo conectarRadio() (bloqueo post-streaming; evita force-stop OEM)");
            return;
        }

        // Para MT8163 intentamos la vinculación AIDL clásica
        Log.i(TAG, "=> RAMA MT8163/Hcn ALCANZADA. LLAMANDO A conectarRadio().");
        synchronized (sMt8163StartLock) {
            long now = SystemClock.elapsedRealtime();
            if (now - sLastMt8163StartWallMs < 350) {
                Log.w(TAG, "conectarRadio: suprimido (ráfaga MT8163)");
                return;
            }
            sLastMt8163StartWallMs = now;
        }
        conectarRadio();
    }

    private void conectarRadio() {
        int engineIdx = mPrefs.getInt("pref_radio_engine", 0);
        String[][] allProviders = getAllProviders();

        // Si hay una selección manual de motor AIDL (3-6) o QS6 (2)
        if (engineIdx >= 2) {
            int targetIdx = -1;
            switch (engineIdx) {
                case 2:
                    targetIdx = 6;
                    break; // QS6
                case 3:
                    targetIdx = 0;
                    break; // MT8163/HCN
                case 4:
                    targetIdx = 1;
                    break; // MediaTek
                case 5:
                    targetIdx = 4;
                    break; // TopWay TS
                case 6:
                    targetIdx = 7; // Mediatek 8259/8667
                    break; 
                case 7:
                    targetIdx = 2; // Standard
                    break; // Mediatek 8259/8667
                default:
                    targetIdx = -1;
                    break;
            }

            if (targetIdx >= 0 && targetIdx < allProviders.length) {
                if (targetIdx == 7) {
                   tryStartTsEngine();
                   return;
                }
                if (bindToProvider(allProviders[targetIdx])) {
                    Log.d(TAG, "Forzando motor manual índice: " + engineIdx + " (Provider " + targetIdx + ")");
                    return;
                }
            }
        } else if (engineIdx == 0) {
            // Automatic detection logic
            MainActivity.FmMode detectedMode = detectMode();
            if (detectedMode == MainActivity.FmMode.FM_8259_8667) {
                tryStartTsEngine();
                return;
            }
        }

        boolean bound = false;
        for (String[] provider : allProviders) {
            if (bindToProvider(provider)) {
                bound = true;
                break;
            }
        }

        if (!bound) {
            Log.w(TAG, "No se pudo vincular a ningún proveedor RDS/AIDL.");
        }
    }

    private boolean bindToProvider(String[] provider) {
        Intent intent = new Intent(provider[1]);
        intent.setPackage(provider[0]);
        // V21.4: Asegurar que se incluya el flag por si el paquete está force-stopped
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        try {
            if (mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
                Log.d(TAG, "Vinculando a proveedor: " + provider[0]);
                mBound = true;
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private MainActivity.FmMode detectMode() {
        int engineIdx = mPrefs.getInt("pref_radio_engine", 0);

        // Prioridad: Selección manual del usuario
        if (engineIdx == 1) return MainActivity.FmMode.FM_K706;
        if (engineIdx == 2) return MainActivity.FmMode.FM_QS6;
        if (engineIdx >= 3 && engineIdx <= 5) return MainActivity.FmMode.FM_MT8163;
        if (engineIdx == 6) return MainActivity.FmMode.FM_BASICO;
        if (engineIdx == 7) return MainActivity.FmMode.FM_8259_8667;

        // Si es Automático (0), intentamos detectar el hardware
        if (isTS8259()) return MainActivity.FmMode.FM_8259_8667;
        if (isQS6()) return MainActivity.FmMode.FM_QS6;
        if (isK706()) return MainActivity.FmMode.FM_K706;
        if (hasCarRadioService()) return MainActivity.FmMode.FM_MT8163;
        return MainActivity.FmMode.FM_BASICO;
    }

    private boolean isTS8259() {
        try {
            // El colaborador indica que el nombre correcto es com.ts.MainUI (mayúsculas importan)
            mContext.getPackageManager().getPackageInfo("com.ts.MainUI", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isK706() {
        try {
            Object service = mContext.getSystemService("mcu_service");
            return service != null && service.getClass().getName().contains("McuManager");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isQS6() {
        try {
            mContext.getPackageManager().getPackageInfo("com.nwd.radio.service", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasCarRadioService() {
        PackageManager pm = mContext.getPackageManager();
        int engineIdx = mPrefs.getInt("pref_radio_engine", 0);
        String[][] allProviders = getAllProviders();

        if (engineIdx > 0) {
            int target = engineIdx - 1;
            if (target < allProviders.length) {
                return checkProvider(pm, allProviders[target]);
            }
        }

        for (String[] provider : allProviders) {
            if (checkProvider(pm, provider))
                return true;
        }
        return false;
    }

    private boolean checkProvider(PackageManager pm, String[] provider) {
        try {
            Intent intent = new Intent(provider[1]);
            intent.setPackage(provider[0]);
            List<ResolveInfo> list = pm.queryIntentServices(intent, 0);
            return list != null && !list.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private String[][] getAllProviders() {
        return new String[][] {
                { "com.hcn.autoradio", "com.hcn.autoradio.FM_PLUG_SERVICE" }, // 0: MT8163/HCN
                { "com.mediatek.fmradio", "com.mediatek.fmradio.IFmRadioService" }, // 1: MediaTek
                { "com.android.fmradio", "com.android.fmradio.IFmRadioService" }, // 2: Standard
                { "com.android.fmradio", "com.android.fmradio.FmRadioService" }, // 3: Standard (Alt)
                { "com.ts.MainUI", "com.ts.MainUI.radio.IRadioService" }, // 4: TopWay (TS) - Estandarizado MainUI
                { "com.syu.radio", "com.syu.radio.IRadioService" }, // 5: SYU
                { "com.nwd.radio.service", "com.nwd.radio.service.ACTION_RADIO_SERVICE" }, // 6: QS6 (NWD)
                { "com.ts.MainUI", "com.ts.main.common.MainUI" }, // 7: Mediatek 8259/8667 (Speech/TS)
                { "com.ts.MainUI", "com.ts.tsspeechlib.radio.TsRadioService" } // 8: Mediatek 8259/8667 Additional
        };
    }

    // --- Lógica específica para Mediatek 8259/8667 (Doble vínculo AIDL) ---

    private com.ts.main.common.ITsCommon mTsCommon;
    private com.ts.tsspeechlib.radio.ITsSpeechRadio mTsSpeechRadio;
    private boolean mTsCommonBound = false;
    private boolean mTsSpeechBound = false;
    private final ServiceConnection mTsCommonConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "TsCommon Connected");
            mTsCommon = com.ts.main.common.ITsCommon.Stub.asInterface(service);
            checkAndStartTsEngine();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTsCommon = null;
            mTsCommonBound = false;
        }
    };
    private final ServiceConnection mTsSpeechConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTsSpeechRadio = com.ts.tsspeechlib.radio.ITsSpeechRadio.Stub.asInterface(service);
            checkAndStartTsEngine();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTsSpeechRadio = null;
            mTsSpeechBound = false;
        }
    };

    private void tryStartTsEngine() {
        Log.i(TAG, "tryStartTsEngine(): Iniciando vínculo doble TS...");
        conectarTsCommon();
        conectarTsSpeechRadio();
    }

    private void conectarTsCommon() {
        Log.d(TAG, "conectarTsCommon() ENTER");
        Intent intent = new Intent();
        intent.setClassName("com.ts.MainUI", "com.ts.main.common.MainUI");
        try {
            mTsCommonBound = mContext.bindService(intent, mTsCommonConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            mTsCommonBound = false;
            Log.w(TAG, "conectarTsCommon: bindService falló", e);
        }
    }

    private void conectarTsSpeechRadio() {
        Log.d(TAG, "conectarTsSpeechRadio() ENTER");
        Intent intent = new Intent();
        intent.setClassName("com.ts.MainUI", "com.ts.tsspeechlib.radio.TsRadioService");
        try {
            mTsSpeechBound = mContext.bindService(intent, mTsSpeechConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            mTsSpeechBound = false;
            Log.w(TAG, "conectarTsSpeechRadio: bindService falló", e);
        }
    }

    private void checkAndStartTsEngine() {
        if (mTsCommon != null && mTsSpeechRadio != null) {
            try {
                com.example.openradiofm.data.source.MTK8259_8667Engine engine = 
                    new com.example.openradiofm.data.source.MTK8259_8667Engine(mTsCommon, mTsSpeechRadio);
                if (engine.init(mContext)) {
                    if (mListener != null) mListener.onEngineReady(engine);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error iniciando MTK8259_8667Engine", e);
            }
        }
    }

    public void release() {
        try {
            if (mBound) {
                mContext.unbindService(mConnection);
            }
        } catch (Exception ignored) {
        } finally {
            mBound = false;
            mRadioService = null;
        }
        try {
            if (mTsCommonBound) {
                mContext.unbindService(mTsCommonConnection);
                mTsCommonBound = false;
            }
        } catch (Exception ignored) {}
        try {
            if (mTsSpeechBound) {
                mContext.unbindService(mTsSpeechConnection);
                mTsSpeechBound = false;
            }
        } catch (Exception ignored) {}
        mTsCommon = null;
        mTsSpeechRadio = null;
    }
}
