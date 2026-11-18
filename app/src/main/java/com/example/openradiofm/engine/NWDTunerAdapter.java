package com.example.openradiofm.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.nwd.radio.service.RadioCallback;
import com.nwd.radio.service.RadioFeature;

/**
 * Adaptador unificado para el SDK de Nowada (QS6/NWD).
 * Encapsula la conexión AIDL y los comandos de hardware (incluyendo redundancia Shadow Motor).
 */
public class NWDTunerAdapter {
    private static final String TAG = "NWDTunerAdapter";
    private static NWDTunerAdapter sInstance;

    private final Context mContext;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    // volatile: escritos desde main (ServiceConnection) y leídos desde hilos de comando/Binder.
    private volatile RadioFeature mService;
    private volatile boolean mIsBound = false;
    private volatile RadioCallback mCallback;
    /**
     * Intención de estar vinculado. false tras disconnect(): cualquier onServiceConnected tardío,
     * reconexión pendiente o muerte del binder se ignora (evita reconexión zombi tras liberar).
     */
    private volatile boolean mWantBound = false;

    // QS6/NWD: tras reboot el servicio OEM puede estar "frío" (driver /dev/NwdRadio no listo).
    // Implementamos reintentos con backoff para reenganchar callbacks sin depender de abrir la app nativa.
    private int mReconnectAttempts = 0;
    private boolean mReconnectScheduled = false;
    private IBinder.DeathRecipient mDeathRecipient;
    /** Binder al que está enlazado mDeathRecipient (para unlinkToDeath en disconnect). */
    private IBinder mLinkedBinder;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (!mWantBound) {
                // Bind en vuelo que llega tras disconnect(): soltarlo, no revivir la conexión.
                Log.d(TAG, "onServiceConnected tras disconnect(): ignorado");
                try { mContext.unbindService(mConnection); } catch (Exception ignored) {}
                return;
            }
            if (service == null) {
                Log.w(TAG, "onServiceConnected con binder null");
                scheduleReconnect("nullBinder");
                return;
            }
            unlinkDeathRecipient();
            mService = RadioFeature.Stub.asInterface(service);
            mIsBound = true;
            mReconnectAttempts = 0;
            mReconnectScheduled = false;
            Log.d(TAG, "NWD RadioService conectado.");

            try {
                final IBinder linked = service;
                mDeathRecipient = () -> {
                    // binderDied llega en hilo Binder: mutar estado en main.
                    mMainHandler.post(() -> {
                        if (mLinkedBinder != linked) return; // muerte de un binder ya sustituido
                        Log.w(TAG, "NWD binder murió");
                        mLinkedBinder = null;
                        mService = null;
                        mIsBound = false;
                        if (mWantBound) scheduleReconnect("binderDied");
                    });
                };
                service.linkToDeath(mDeathRecipient, 0);
                mLinkedBinder = service;
            } catch (Throwable ignored) {}

            final RadioCallback cb = mCallback;
            if (cb != null) {
                try {
                    mService.registCallback(cb);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error al registrar callback en el bind", e);
                    scheduleReconnect("registCallbackRemoteException");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBound = false;
            Log.w(TAG, "NWD RadioService desconectado.");
            if (mWantBound) scheduleReconnect("onServiceDisconnected");
        }
    };

    private void unlinkDeathRecipient() {
        if (mLinkedBinder != null && mDeathRecipient != null) {
            try { mLinkedBinder.unlinkToDeath(mDeathRecipient, 0); } catch (Exception ignored) {}
        }
        mLinkedBinder = null;
        mDeathRecipient = null;
    }

    private NWDTunerAdapter(Context ctx) {
        this.mContext = ctx.getApplicationContext();
        bind();
    }

    public static synchronized NWDTunerAdapter getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new NWDTunerAdapter(ctx);
        }
        return sInstance;
    }

    public void connect() {
        bind();
    }

    public void disconnect() {
        mWantBound = false;
        // Cancelar reconexiones pendientes (scheduleReconnect): si no, re-vinculaban tras liberar.
        mMainHandler.removeCallbacksAndMessages(null);
        mReconnectScheduled = false;
        mReconnectAttempts = 0;
        unlinkDeathRecipient();
        final RadioFeature svc = mService;
        final RadioCallback cb = mCallback;
        if (svc != null && cb != null) {
            try { svc.unRegistCallback(cb); } catch (Exception ignored) {}
        }
        // Siempre: también cubre un bind en vuelo (mService aún null).
        try {
            mContext.unbindService(mConnection);
        } catch (Exception ignored) {}
        mService = null;
        mIsBound = false;
    }

    /** Suelta el callback si sigue siendo {@code cb} (evita que el singleton retenga un motor liberado). */
    public void clearCallback(RadioCallback cb) {
        if (cb == null || mCallback != cb) return;
        final RadioFeature svc = mService;
        if (svc != null) {
            try { svc.unRegistCallback(cb); } catch (Exception ignored) {}
        }
        mCallback = null;
    }

    public void bind() {
        mWantBound = true;
        if (mIsBound) return;
        try {
            Intent svc = new Intent("com.nwd.radio.service.ACTION_RADIO_SERVICE");
            svc.setPackage("com.nwd.radio.service");

            // Warm-up: intentar arrancar el servicio explícitamente (sin UI).
            try {
                Intent explicit = new Intent(svc);
                explicit.setComponent(new ComponentName("com.nwd.radio.service", "com.nwd.radio.service.RadioService"));
                mContext.startService(explicit);
            } catch (Exception ignored) {}

            // Rebind limpio si quedó un bind a medias.
            try { mContext.unbindService(mConnection); } catch (Exception ignored) {}
            mContext.bindService(svc, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Error al vincular el servicio NWD", e);
            scheduleReconnect("bindException");
        }
    }

    public void setCallback(RadioCallback callback) {
        final RadioCallback old = mCallback;
        this.mCallback = callback;
        final RadioFeature svc = mService;
        if (mIsBound && svc != null) {
            if (old != null && old != callback) {
                try { svc.unRegistCallback(old); } catch (Exception ignored) {}
            }
            if (callback == null) return;
            try {
                svc.registCallback(callback);
            } catch (RemoteException e) {
                Log.e(TAG, "Error al registrar callback", e);
                scheduleReconnect("setCallbackRemoteException");
            }
        }
    }

    private void scheduleReconnect(String reason) {
        if (mReconnectScheduled) return;
        mReconnectScheduled = true;
        long delayMs;
        // Backoff rápido al principio; luego tope 10s
        if (mReconnectAttempts <= 0) delayMs = 350L;
        else if (mReconnectAttempts == 1) delayMs = 750L;
        else if (mReconnectAttempts == 2) delayMs = 1400L;
        else if (mReconnectAttempts == 3) delayMs = 2200L;
        else delayMs = Math.min(10000L, 3000L + (mReconnectAttempts * 700L));
        mReconnectAttempts++;
        Log.w(TAG, "scheduleReconnect(" + reason + ") in " + delayMs + "ms (attempt " + mReconnectAttempts + ")");
        mMainHandler.postDelayed(() -> {
            mReconnectScheduled = false;
            if (!mWantBound || mIsBound) return;
            bind();
        }, delayMs);
    }

    public void tune(int freqKhz) {
        int band = (freqKhz > 30000) ? 0 : 3; // Estimación: FM si > 30MHz
        int sdkFreq = (band < 3) ? freqKhz / 10 : freqKhz;
        Log.d(TAG, "tune: " + freqKhz + "Khz -> sdkFreq: " + sdkFreq + " band: " + band);
        if (mService != null) {
            try {
                mService.setCurrentFrequency(sdkFreq, (byte) band, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en tune() AIDL", e);
                sendShadowTune(sdkFreq, band);
            }
        } else {
            sendShadowTune(sdkFreq, band);
        }
    }
    
    public void tuneWithBand(int freqKhz, int band) {
        int sdkFreq = (band < 3) ? freqKhz / 10 : freqKhz;
        Log.d(TAG, "tuneWithBand: " + freqKhz + "Khz, band: " + band + " -> sdkFreq: " + sdkFreq);
        if (mService != null) {
            try {
                mService.setCurrentFrequency(sdkFreq, (byte) band, 0);
            } catch (RemoteException e) {
                sendShadowTune(sdkFreq, band);
            }
        } else {
            sendShadowTune(sdkFreq, band);
        }
    }

    private void sendShadowTune(int sdkFreq, int band) {
        Intent intent = new Intent("com.nwd.action.ACTION_SET_RADIO_FREQUENCE");
        intent.putExtra("extra_frequence", sdkFreq);
        intent.putExtra("extra_band", (byte) band);
        mContext.sendBroadcast(intent);
    }

    public void seek(boolean up) {
        if (mService != null) {
            try {
                mService.search(up);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en seek() AIDL", e);
                sendShadowSeek(up);
            }
        } else {
            sendShadowSeek(up);
        }
    }

    private void sendShadowSeek(boolean up) {
        String action = up ? "com.nwd.action.ACTION_SEARCH_UP" : "com.nwd.action.ACTION_SEARCH_DOWN";
        mContext.sendBroadcast(new Intent(action));
    }

    public void autoScan() {
        if (mService != null) {
            try {
                mService.AMS();
            } catch (RemoteException e) {
                Log.e(TAG, "Error en AMS AIDL", e);
                mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_AMS"));
            }
        } else {
            mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_AMS"));
        }
    }

    public void bandCycle() {
        if (mService != null) {
            try {
                mService.changeBand();
            } catch (RemoteException e) {
                Log.e(TAG, "Error en changeBand AIDL", e);
                sendShadowBandCycle();
            }
        } else {
            sendShadowBandCycle();
        }
    }

    private void sendShadowBandCycle() {
        Intent intent = new Intent("com.nwd.action.ACTION_KEY_VALUE");
        intent.putExtra("extra_key_value", (byte) 0x48); // KEY_FM
        mContext.sendBroadcast(intent);
    }

    public void setMute(boolean mute) {
        // V22.5: El comando 0x05 causaba cambio de banda en QS6 G5.
        // El muteo real se gestiona ahora en QS6Engine vía cambio de fuente (Source Switch),
        // que es el método oficial de la pila NWD para silenciar la radio.
        Log.d(TAG, "setMute(" + mute + ") - delegando a engine via source switch");
    }

    public void setBand(int band) {
        if (mService != null) {
            try {
                mService.setCurrentFrequency(0, (byte) band, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en setBand AIDL", e);
            }
        }
    }

    public void stopScan() {
        // El SDK no tiene stopSearch. Volver a llamar a search con false o similar a veces funciona.
        // Alternativa: Shadow command
        mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_STOP_SEARCH"));
    }

    public void setRDSState(int bit, boolean on) {
        if (mService != null) {
            try {
                mService.setRDSState((byte) bit, on);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en setRDSState", e);
            }
        }
    }

    public boolean getRDSState(int bit) {
        if (mService != null) {
            try {
                return mService.getRDSState(bit);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en getRDSState", e);
            }
        }
        return false;
    }

    public void setBackService(boolean on) {
        if (mService != null) {
            try {
                mService.setRadioBackServiceOn(on);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en setBackService", e);
            }
        }
    }

    public void sendRadioCommand(int data0, int data1) {
        if (mService != null) {
            try {
                mService.sendRadioCommand((byte) data0, (byte) data1);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en sendRadioCommand", e);
            }
        }
    }

    public void intro() {
        if (mService != null) {
            try {
                mService.INTRO();
            } catch (RemoteException e) {
                Log.e(TAG, "Error en INTRO", e);
            }
        }
    }

    public void setStereoOn(boolean on) {
        if (mService != null) {
            try {
                mService.setStreroOn(on);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en setStreroOn", e);
            }
        }
    }

    public void prefeb(boolean up) {
        if (mService != null) {
            try {
                mService.prefeb(up);
            } catch (RemoteException e) {
                Log.e(TAG, "Error en prefeb", e);
            }
        }
    }

    public void saveCurrentFrequency(int index) {
        if (mService != null) {
            try {
                int idx = Math.max(0, Math.min(index, 15));
                mService.saveCurrentFrequency((byte) (idx & 0xFF));
            } catch (RemoteException e) {
                Log.e(TAG, "Error en saveCurrentFrequency", e);
            }
        }
    }

    public String getDebugStatus() {
        if (mService == null) return "AIDL=NULL";
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("AIDL=OK");
            try {
                sb.append(" state=").append((int) mService.getRadioState());
            } catch (Throwable ignored) {}
            try {
                sb.append(" scan=").append(mService.getCurrentScanState());
            } catch (Throwable ignored) {}
            try {
                sb.append(" stereo=").append(mService.isStreroOn());
            } catch (Throwable ignored) {}
            try {
                sb.append(" hasStereo=").append(mService.isHasStrero());
            } catch (Throwable ignored) {}
            try {
                sb.append(" near=").append(mService.isNearOn());
            } catch (Throwable ignored) {}
            try {
                sb.append(" backSvc=").append(mService.isRadioBackServiceOn());
            } catch (Throwable ignored) {}
            try {
                String rt = mService.getRtMessage();
                if (rt != null && !rt.isEmpty()) sb.append(" rt=").append(rt);
            } catch (Throwable ignored) {}
            return sb.toString();
        } catch (Throwable t) {
            return "AIDL_ERR:" + t.getClass().getSimpleName();
        }
    }

    public boolean isConnected() {
        return mIsBound && mService != null;
    }
}
