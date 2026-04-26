package com.example.openradiofm.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hcn.autoradio.IRadioCallBack;
import com.hcn.autoradio.IRadioServiceAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * MT8163TunerAdapter (HCN Platform)
 * 
 * Centraliza el control AIDL para dispositivos MediaTek MT8163 (HCN Autoradio).
 * Implementa el patrón Singleton y abstrae la complejidad de reconexión y callbacks.
 */
public class MT8163TunerAdapter {
    private static final String TAG = "MT8163TunerAdapter";
    
    private static final String HCN_PACKAGE = "com.hcn.autoradio";
    private static final String HCN_SERVICE_ACTION = "com.hcn.autoradio.FM_PLUG_SERVICE";
    private static final String HCN_WAKE_ACTION = "com.hcn.autoradio.FMRADIO_START";

    private static MT8163TunerAdapter sInstance;
    private Context mContext;
    private IRadioServiceAPI mService;
    private boolean mIsConnecting = false;
    
    // Lista de callbacks locales para propagar eventos a múltiples Engine si fuera necesario
    private final List<AdapterCallback> mCallbacks = new ArrayList<>();

    public interface AdapterCallback {
        void onEvent(int code, String data);
        void onServiceConnected();
        void onServiceDisconnected();
    }

    public static synchronized MT8163TunerAdapter getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MT8163TunerAdapter(context.getApplicationContext());
        }
        return sInstance;
    }

    private MT8163TunerAdapter(Context context) {
        this.mContext = context;
    }

    public void connect() {
        if (mService != null || mIsConnecting) return;
        mIsConnecting = true;
        
        try {
            // Wake up el hardware mediante broadcast (Shadow Motor Pattern)
            Intent wake = new Intent(HCN_WAKE_ACTION);
            wake.setPackage(HCN_PACKAGE);
            mContext.sendBroadcast(wake);
            
            // Bind al servicio AIDL
            Intent intent = new Intent(HCN_SERVICE_ACTION);
            intent.setPackage(HCN_PACKAGE);
            boolean success = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "MT8163: Iniciando binding a FM_PLUG_SERVICE... Exito=" + success);
        } catch (Exception e) {
            Log.e(TAG, "MT8163: Error al intentar conectar con el servicio", e);
            mIsConnecting = false;
        }
    }

    public void disconnect() {
        if (mService != null) {
            try {
                mContext.unbindService(mConnection);
            } catch (Exception ignored) {}
            mService = null;
        }
        mIsConnecting = false;
    }

    public boolean isConnected() {
        return mService != null;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = IRadioServiceAPI.Stub.asInterface(binder);
            mIsConnecting = false;
            Log.i(TAG, "MT8163: Servicio conectado exitosamente.");
            
            try {
                // Registrar callback AIDL global
                mService.registerRadioCallback(new IRadioCallBack.Stub() {
                    @Override
                    public void onEvent(int code, String data) {
                        synchronized (mCallbacks) {
                            for (AdapterCallback cb : mCallbacks) {
                                cb.onEvent(code, data);
                            }
                        }
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "MT8163: Error registrando callback AIDL", e);
            }

            synchronized (mCallbacks) {
                for (AdapterCallback cb : mCallbacks) cb.onServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsConnecting = false;
            Log.w(TAG, "MT8163: Servicio desconectado inesperadamente.");
            synchronized (mCallbacks) {
                for (AdapterCallback cb : mCallbacks) cb.onServiceDisconnected();
            }
        }
    };

    public void addCallback(AdapterCallback cb) {
        synchronized (mCallbacks) {
            if (!mCallbacks.contains(cb)) mCallbacks.add(cb);
        }
    }

    public void removeCallback(AdapterCallback cb) {
        synchronized (mCallbacks) {
            mCallbacks.remove(cb);
        }
    }

    // === Envolturas de Métodos AIDL (Safe Calls) ===

    public void gotoFreq(int freq) {
        if (mService != null) {
            try { mService.gotoFreq(freq); } catch (RemoteException e) { Log.w(TAG, "gotoFreq failed", e); }
        }
    }

    public int getCurrentFreq() {
        if (mService != null) {
            try { return mService.getCurrentFreq(); } catch (RemoteException e) { return 0; }
        }
        return 0;
    }

    public int getCurrentBand() {
        if (mService != null) {
            try { return mService.getCurrentBand(); } catch (RemoteException e) { return 0; }
        }
        return 0;
    }

    public void seekUp() {
        if (mService != null) {
            try { mService.onSeekDownEvent(); } catch (RemoteException e) { Log.w(TAG, "seekUp failed", e); }
        }
    }

    public void seekDown() {
        if (mService != null) {
            try { mService.onSeekUpEvent(); } catch (RemoteException e) { Log.w(TAG, "seekDown failed", e); }
        }
    }

    public void stepUp() {
        if (mService != null) {
            try { mService.onManualUpEvent(); } catch (RemoteException e) { Log.w(TAG, "stepUp failed", e); }
        }
    }

    public void stepDown() {
        if (mService != null) {
            try { mService.onManualDownEvent(); } catch (RemoteException e) { Log.w(TAG, "stepDown failed", e); }
        }
    }

    public void bandCycle() {
        if (mService != null) {
            try { mService.onBandEvent(); } catch (RemoteException e) { Log.w(TAG, "bandCycle failed", e); }
        }
    }

    public void scan() {
        if (mService != null) {
            try { mService.onScanEvent(); } catch (RemoteException e) { Log.w(TAG, "scan failed", e); }
        }
    }

    public void stopScan() {
        if (mService != null) {
            try { mService.onPSEvent(); } catch (RemoteException e) { Log.w(TAG, "stopScan failed", e); }
        }
    }

    public boolean isStereo() {
        if (mService != null) {
            try { return mService.IsStereo(); } catch (RemoteException e) { return false; }
        }
        return false;
    }

    public boolean requestPlayAudio() {
        if (mService != null) {
            try { return mService.requestPlayAudio(); } catch (RemoteException e) { return false; }
        }
        return false;
    }

    public void toggleRdsFeature(int type) {
        if (mService != null) {
            try { mService.toggleRdsFeature(type); } catch (RemoteException e) { Log.w(TAG, "toggleRds failed", e); }
        }
    }
}
