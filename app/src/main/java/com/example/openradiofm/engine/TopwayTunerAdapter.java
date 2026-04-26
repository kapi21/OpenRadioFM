package com.example.openradiofm.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ts.main.common.ITsCommon;
import com.ts.tsspeechlib.radio.ITsSpeechRadio;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador modular para la plataforma TopWay / TS (MTK8259 / MT8667).
 * Centraliza el vínculo doble (ITsCommon e ITsSpeechRadio) y la lógica de reconexión.
 */
public class TopwayTunerAdapter {
    private static final String TAG = "TopwayTunerAdapter";
    private static TopwayTunerAdapter sInstance;

    private final Context mContext;
    private ITsCommon mCommonService;
    private ITsSpeechRadio mSpeechService;
    
    private boolean mCommonBound = false;
    private boolean mSpeechBound = false;
    
    private final List<AdapterCallback> mCallbacks = new ArrayList<>();

    public interface AdapterCallback {
        void onServiceConnected();
        void onServiceDisconnected();
    }

    private final ServiceConnection mCommonConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "ITsCommon conectado");
            mCommonService = ITsCommon.Stub.asInterface(service);
            mCommonBound = true;
            checkAndNotify();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "ITsCommon desconectado");
            mCommonService = null;
            mCommonBound = false;
            notifyDisconnected();
        }
    };

    private final ServiceConnection mSpeechConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "ITsSpeechRadio conectado");
            mSpeechService = ITsSpeechRadio.Stub.asInterface(service);
            mSpeechBound = true;
            checkAndNotify();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "ITsSpeechRadio desconectado");
            mSpeechService = null;
            mSpeechBound = false;
            notifyDisconnected();
        }
    };

    private TopwayTunerAdapter(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized TopwayTunerAdapter getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TopwayTunerAdapter(context);
        }
        return sInstance;
    }

    public void bind() {
        if (mCommonBound && mSpeechBound) return;
        
        Log.i(TAG, "Iniciando doble vínculo Topway (Common + Speech)...");
        
        // 1. Vincular Common
        Intent commonIntent = new Intent();
        commonIntent.setClassName("com.ts.MainUI", "com.ts.main.common.MainUI");
        try {
            mContext.bindService(commonIntent, mCommonConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Error vinculando ITsCommon", e);
        }

        // 2. Vincular Speech
        Intent speechIntent = new Intent();
        speechIntent.setClassName("com.ts.MainUI", "com.ts.tsspeechlib.radio.TsRadioService");
        try {
            mContext.bindService(speechIntent, mSpeechConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Error vinculando ITsSpeechRadio", e);
        }
    }

    public void unbind() {
        if (mCommonBound) {
            try { mContext.unbindService(mCommonConnection); } catch (Exception ignored) {}
            mCommonBound = false;
        }
        if (mSpeechBound) {
            try { mContext.unbindService(mSpeechConnection); } catch (Exception ignored) {}
            mSpeechBound = false;
        }
        mCommonService = null;
        mSpeechService = null;
    }

    private void checkAndNotify() {
        if (mCommonService != null && mSpeechService != null) {
            synchronized (mCallbacks) {
                for (AdapterCallback cb : mCallbacks) cb.onServiceConnected();
            }
        }
    }

    private void notifyDisconnected() {
        synchronized (mCallbacks) {
            for (AdapterCallback cb : mCallbacks) cb.onServiceDisconnected();
        }
    }

    public void addCallback(AdapterCallback cb) {
        synchronized (mCallbacks) {
            if (!mCallbacks.contains(cb)) mCallbacks.add(cb);
        }
        if (isConnected()) cb.onServiceConnected();
    }

    public void removeCallback(AdapterCallback cb) {
        synchronized (mCallbacks) {
            mCallbacks.remove(cb);
        }
    }

    public boolean isConnected() {
        return mCommonService != null && mSpeechService != null;
    }

    // --- API de Control (Speech) ---

    public void turnBandAndFq(int band, int fq) {
        if (mSpeechService != null) {
            try { mSpeechService.TurnBandAndFq(band, fq); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void seekUp() {
        if (mSpeechService != null) {
            try { mSpeechService.SeekUp(); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void seekDown() {
        if (mSpeechService != null) {
            try { mSpeechService.SeekDn(); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void openRadioCh() {
        if (mSpeechService != null) {
            try { mSpeechService.OpenRadioCh(); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void closeRadioCh() {
        if (mSpeechService != null) {
            try { mSpeechService.CloseRadioCh(); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public int getRadioBand() {
        if (mSpeechService != null) {
            try { return mSpeechService.getRadioBand(); } catch (RemoteException e) { return 0; }
        }
        return 0;
    }

    // --- API de Estado (Common) ---

    public String getFreq() {
        if (mCommonService != null) {
            try { return mCommonService.GetFreq(); } catch (RemoteException e) { return null; }
        }
        return null;
    }

    public void enterMode(int mode) {
        if (mCommonService != null) {
            try { mCommonService.EnterMode(mode); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void mute() {
        if (mCommonService != null) {
            try { mCommonService.Mute(); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public boolean isMute() {
        if (mCommonService != null) {
            try { return mCommonService.IsMute(); } catch (RemoteException e) { return false; }
        }
        return false;
    }

    public boolean getRadioSTState() {
        if (mCommonService != null) {
            try { return mCommonService.GetRadioSTState(); } catch (RemoteException e) { return false; }
        }
        return false;
    }

    public String getPsName() {
        if (mCommonService != null) {
            try { return mCommonService.GetPsName(); } catch (Exception e) { return null; }
        }
        return null;
    }

    public String getPtyStr() {
        if (mCommonService != null) {
            try { return mCommonService.GetPtyStr(); } catch (Exception e) { return null; }
        }
        return null;
    }

    public String getCategory() {
        if (mCommonService != null) {
            try { return mCommonService.GetCategory(); } catch (Exception e) { return null; }
        }
        return null;
    }

    public boolean rdsAf() {
        if (mCommonService != null) {
            try { return mCommonService.RdsAf(); } catch (Exception e) { return false; }
        }
        return false;
    }

    public boolean rdsTa() {
        if (mCommonService != null) {
            try { return mCommonService.RdsTa(); } catch (Exception e) { return false; }
        }
        return false;
    }

    public boolean rdsTp() {
        if (mCommonService != null) {
            try { return mCommonService.RdsTp(); } catch (Exception e) { return false; }
        }
        return false;
    }

    public void onRadioFM() {
        if (mSpeechService != null) {
            try { mSpeechService.onRadioFM(); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void onRadioAM() {
        if (mSpeechService != null) {
            try { mSpeechService.onRadioAM(); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void onSelectedFreq(int index) {
        if (mSpeechService != null) {
            try { mSpeechService.onSelectedFreq(index); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void rdsAfSwitch() {
        if (mCommonService != null) {
            try { mCommonService.RdsAfSwitch(); } catch (Exception e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void rdsTaSwitch() {
        if (mCommonService != null) {
            try { mCommonService.RdsTaSwitch(); } catch (Exception e) { Log.e(TAG, "Remote error", e); }
        }
    }

    public void gotoEq() {
        if (mCommonService != null) {
            try { mCommonService.GotoEq(); } catch (RemoteException e) { Log.e(TAG, "Remote error", e); }
        }
    }
}
