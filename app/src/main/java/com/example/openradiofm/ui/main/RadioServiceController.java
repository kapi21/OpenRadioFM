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
 * Gestiona el descubrimiento de hardware y la conexión al servicio de radio AIDL.
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
    
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Servicio conectado: " + name.flattenToShortString());
            mRadioService = IRadioServiceAPI.Stub.asInterface(service);
            
            // Re-evaluar modo basado en el paquete conectado
            MainActivity.FmMode newMode = MainActivity.FmMode.FM_BASICO;
            String pkg = name.getPackageName();
            if (pkg.equals("com.hcn.autoradio")) newMode = MainActivity.FmMode.FM_MT8163;
            else if (pkg.equals("com.nwd.radio.service")) newMode = MainActivity.FmMode.FM_QS6;
            
            if (mListener != null) {
                mListener.onModeDetected(newMode);
                mListener.onServiceConnected(mRadioService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
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
        Log.d(TAG, "Iniciando RadioServiceController. Modo detectado: " + mode);
        
        if (mListener != null) {
            mListener.onModeDetected(mode);
        }

        // Si es K706, no necesita servicio AIDL de tipo IRadioServiceAPI (usa McuService)
        if (mode == MainActivity.FmMode.FM_K706) {
            try {
                com.example.openradiofm.data.source.K706Engine engine = new com.example.openradiofm.data.source.K706Engine();
                // Ojo: MainActivity necesita que se llame a init antes de hacer cualquier cosa
                // En MT8163/QS6 init() se llama justo tras instanciar. Lo hacemos igual aquí.
                if (engine.init(mContext)) {
                    if (mListener != null) mListener.onEngineReady(engine);
                } else {
                    Log.e(TAG, "Error iniciando K706Engine (init devolvió false)");
                }
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error iniciando K706Engine", e);
            }
        }

        // Para QS6 y MT8163 intentamos la vinculación AIDL
        conectarRadio();
    }

    private void conectarRadio() {
        int engineIdx = mPrefs.getInt("pref_radio_engine", 0);
        String[][] allProviders = getAllProviders();

        // Si hay una selección manual de motor AIDL (3-6) o QS6 (2)
        if (engineIdx >= 2) {
            int targetIdx = -1;
            switch (engineIdx) {
                case 2: targetIdx = 6; break; // QS6
                case 3: targetIdx = 0; break; // MT8163/HCN
                case 4: targetIdx = 1; break; // MediaTek
                case 5: targetIdx = 4; break; // TopWay TS
                case 6: targetIdx = 2; break; // Standard
            }
            
            if (targetIdx >= 0 && targetIdx < allProviders.length) {
                if (bindToProvider(allProviders[targetIdx])) {
                    Log.d(TAG, "Forzando motor manual índice: " + engineIdx + " (Provider " + targetIdx + ")");
                    return;
                }
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
        try {
            if (mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
                Log.d(TAG, "Vinculando a proveedor: " + provider[0]);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private MainActivity.FmMode detectMode() {
        int engineIdx = mPrefs.getInt("pref_radio_engine", 0);
        
        // Prioridad: Selección manual del usuario
        if (engineIdx == 1) return MainActivity.FmMode.FM_K706;
        if (engineIdx == 2) return MainActivity.FmMode.FM_QS6;
        if (engineIdx >= 3 && engineIdx <= 5) return MainActivity.FmMode.FM_MT8163;
        if (engineIdx == 6) return MainActivity.FmMode.FM_BASICO;

        // Si es Automático (0), intentamos detectar el hardware
        if (isQS6()) return MainActivity.FmMode.FM_QS6;
        if (isK706()) return MainActivity.FmMode.FM_K706;
        if (hasCarRadioService()) return MainActivity.FmMode.FM_MT8163;
        return MainActivity.FmMode.FM_BASICO;
    }

    private boolean isK706() {
        try {
            Object service = mContext.getSystemService("mcu_service");
            return service != null && service.getClass().getName().contains("McuManager");
        } catch (Exception e) { return false; }
    }

    private boolean isQS6() {
        try {
            Intent intent = new Intent("com.nwd.radio.service.ACTION_RADIO_SERVICE");
            intent.setPackage("com.nwd.radio.service");
            List<ResolveInfo> list = mContext.getPackageManager().queryIntentServices(intent, 0);
            return list != null && !list.isEmpty();
        } catch (Exception e) { return false; }
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
            if (checkProvider(pm, provider)) return true;
        }
        return false;
    }

    private boolean checkProvider(PackageManager pm, String[] provider) {
        try {
            Intent intent = new Intent(provider[1]);
            intent.setPackage(provider[0]);
            List<ResolveInfo> list = pm.queryIntentServices(intent, 0);
            return list != null && !list.isEmpty();
        } catch (Exception e) { return false; }
    }

    private String[][] getAllProviders() {
        return new String[][] {
            { "com.hcn.autoradio", "com.hcn.autoradio.FM_PLUG_SERVICE" },             // 0: MT8163/HCN
            { "com.mediatek.fmradio", "com.mediatek.fmradio.IFmRadioService" },       // 1: MediaTek
            { "com.android.fmradio", "com.android.fmradio.IFmRadioService" },          // 2: Standard
            { "com.android.fmradio", "com.android.fmradio.FmRadioService" },           // 3: Standard (Alt)
            { "com.ts.mainui", "com.ts.mainui.radio.IRadioService" },                  // 4: TopWay (TS)
            { "com.syu.radio", "com.syu.radio.IRadioService" },                        // 5: SYU
            { "com.nwd.radio.service", "com.nwd.radio.service.ACTION_RADIO_SERVICE" }  // 6: QS6 (NWD)
        };
    }

    public void release() {
        try {
            mContext.unbindService(mConnection);
        } catch (Exception ignored) {}
    }
}
