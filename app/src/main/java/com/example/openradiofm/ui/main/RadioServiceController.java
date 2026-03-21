package com.example.openradiofm.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;

import com.hcn.autoradio.IRadioServiceAPI;
import com.example.openradiofm.data.source.RadioEngine;
import com.example.openradiofm.data.source.MT8163Engine;
import java.util.List;

/**
 * Gestiona el descubrimiento de hardware y la conexión al servicio de radio
 * AIDL.
 */
public class RadioServiceController {
    private static final String TAG = "RadioServiceController";

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

    public void start() {
        MainActivity.FmMode mode = detectMode();
        Log.e(TAG, "=> START() INVOCADO. MODO DETECTADO: " + mode);

        if (mListener != null) {
            mListener.onModeDetected(mode);
        }

        // Si es K706, no necesita servicio AIDL de tipo IRadioServiceAPI (usa
        // McuService)
        if (mode == MainActivity.FmMode.FM_K706) {
            try {
                com.example.openradiofm.data.source.K706Engine engine = new com.example.openradiofm.data.source.K706Engine();
                // Ojo: MainActivity necesita que se llame a init antes de hacer cualquier cosa
                if (engine.init(mContext)) {
                    if (mListener != null)
                        mListener.onEngineReady(engine);
                } else {
                    Log.e(TAG, "Error iniciando K706Engine (init devolvió false)");
                }
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error iniciando K706Engine", e);
            }
        } else if (mode == MainActivity.FmMode.FM_QS6) {
            // V14.0: QS6 (NWD) ahora utiliza un engine que se auto-vincula (bindService
            // interno en init)
            Log.e(TAG, "=> RAMA QS6 ALCANZADA. INSTANCIANDO MOTOR QS6Engine...");
            try {
                com.example.openradiofm.data.source.QS6Engine engine = new com.example.openradiofm.data.source.QS6Engine();
                Log.e(TAG, "=> QS6Engine INSTANCIADO OK. LLAMANDO A .init()...");
                if (engine.init(mContext)) {
                    Log.e(TAG, "=> QS6Engine INIT EXITOSO. AVISANDO A LA INTERFAZ...");
                    if (mListener != null)
                        mListener.onEngineReady(engine);
                } else {
                    Log.e(TAG, "Error iniciando QS6Engine V14 (init devolvió false)");
                }
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error iniciando QS6Engine", e);
            }
        }

        // Para MT8163 intentamos la vinculación AIDL clásica
        Log.e(TAG, "=> RAMA MT8163/Hcn ALCANZADA. LLAMANDO A conectarRadio().");
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
        if (engineIdx == 1)
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

    private void tryStartTsEngine() {
        Log.e(TAG, "tryStartTsEngine(): Iniciando vínculo doble TS...");
        conectarTsCommon();
        conectarTsSpeechRadio();
    }

    private void conectarTsCommon() {
        Log.d(TAG, "conectarTsCommon() ENTER");
        Intent intent = new Intent();
        intent.setClassName("com.ts.MainUI", "com.ts.main.common.MainUI");
        mContext.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "TsCommon Connected");
                mTsCommon = com.ts.main.common.ITsCommon.Stub.asInterface(service);
                checkAndStartTsEngine();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) { mTsCommon = null; }
        }, Context.BIND_AUTO_CREATE);
    }

    private void conectarTsSpeechRadio() {
        Log.d(TAG, "conectarTsSpeechRadio() ENTER");
        Intent intent = new Intent();
        intent.setClassName("com.ts.MainUI", "com.ts.tsspeechlib.radio.TsRadioService");
        mContext.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mTsSpeechRadio = com.ts.tsspeechlib.radio.ITsSpeechRadio.Stub.asInterface(service);
                checkAndStartTsEngine();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) { mTsSpeechRadio = null; }
        }, Context.BIND_AUTO_CREATE);
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
            mContext.unbindService(mConnection);
        } catch (Exception ignored) {
        }
    }
}
