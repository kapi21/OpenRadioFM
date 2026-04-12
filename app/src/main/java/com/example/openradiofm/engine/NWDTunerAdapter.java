package com.example.openradiofm.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
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
    private RadioFeature mService;
    private boolean mIsBound = false;
    private RadioCallback mCallback;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = RadioFeature.Stub.asInterface(service);
            mIsBound = true;
            Log.d(TAG, "NWD RadioService conectado.");
            if (mCallback != null && mService != null) {
                try {
                    mService.registCallback(mCallback);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error al registrar callback en el bind", e);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBound = false;
            Log.w(TAG, "NWD RadioService desconectado.");
        }
    };

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
        if (mService != null) {
            if (mCallback != null) {
                try { mService.unRegistCallback(mCallback); } catch (Exception ignored) {}
            }
            try {
                mContext.unbindService(mConnection);
            } catch (Exception ignored) {}
            mService = null;
        }
        mIsBound = false;
    }

    public void bind() {
        if (mIsBound) return;
        Intent intent = new Intent("com.nwd.radio.service.ACTION_RADIO_SERVICE");
        intent.setPackage("com.nwd.radio.service");
        try {
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Error al vincular el servicio NWD", e);
        }
    }

    public void setCallback(RadioCallback callback) {
        this.mCallback = callback;
        if (mIsBound && mService != null) {
            try {
                mService.registCallback(callback);
            } catch (RemoteException e) {
                Log.e(TAG, "Error al registrar callback", e);
            }
        }
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

    public boolean isConnected() {
        return mIsBound && mService != null;
    }
}
