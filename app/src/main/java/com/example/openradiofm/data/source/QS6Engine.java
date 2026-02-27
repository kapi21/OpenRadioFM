package com.example.openradiofm.data.source;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * V12.0: Motor para plataformas QS6 G5 (NWD).
 * Utiliza el servicio com.nwd.radio.service y reporta estado via Broadcasts.
 */
public class QS6Engine implements RadioEngine {
    private static final String TAG = "QS6Engine";
    private Context mContext;
    private RadioEngineCallback mCallback;
    private int mCurrentFreq = 87500;
    private int mCurrentBand = 0;
    private boolean mIsStereo = false;
    private boolean mIsConnected = false;

    // Constantes NWD
    private static final String ACTION_RADIO_SERVICE = "com.nwd.radio.service.ACTION_RADIO_SERVICE";
    private static final String PACKAGE_RADIO_SERVICE = "com.nwd.radio.service";
    private static final String ACTION_REPORT_FREQ = "com.nwd.action.ACTION_SEND_RADIO_FREQUENCE";
    
    // UART RDS (F0 05 ...)
    private static final String ACTION_UART_RECV = "com.nwd.action.ACTION_UART_RECV";

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(android.content.ComponentName name, IBinder service) {
            Log.d(TAG, "Servicio NWD Radio conectado.");
            mIsConnected = true;
            // Aquí podríamos obtener una interfaz AIDL si supiéramos el nombre generado por el Stub
        }

        @Override
        public void onServiceDisconnected(android.content.ComponentName name) {
            Log.d(TAG, "Servicio NWD Radio desconectado.");
            mIsConnected = false;
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_REPORT_FREQ.equals(action)) {
                // NWD suele enviar la frecuencia en un extra llamado "frequence" o "freq"
                // Basado en dumpsys, es probable que sea "frequence"
                int freq = intent.getIntExtra("frequence", -1);
                if (freq == -1) freq = intent.getIntExtra("freq", -1);
                
                if (freq > 0) {
                    mCurrentFreq = freq;
                    if (mCallback != null) {
                        mCallback.onFrequencyChanged(mCurrentFreq);
                    }
                }
                
                int band = intent.getIntExtra("band", -1);
                if (band != -1) {
                    mCurrentBand = band;
                    // Mapear bandas si es necesario
                }
            } else if (ACTION_UART_RECV.equals(action)) {
                // Parsear RDS desde UART si está disponible
                byte[] data = intent.getByteArrayExtra("data");
                if (data != null) {
                    handleUartPacket(data);
                }
            }
        }
    };

    @Override
    public boolean init(Context context) {
        this.mContext = context;
        Log.d(TAG, "Iniciando motor QS6 (NWD G5)");
        
        // 1. Vincular al servicio principal
        try {
            Intent intent = new Intent(ACTION_RADIO_SERVICE);
            intent.setPackage(PACKAGE_RADIO_SERVICE);
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Error vinculando al servicio NWD: " + e.getMessage());
        }

        // 2. Registrar receptores de estado
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REPORT_FREQ);
        filter.addAction(ACTION_UART_RECV);
        context.registerReceiver(mReceiver, filter);
        
        return true;
    }

    @Override
    public void release() {
        if (mContext != null) {
            mContext.unbindService(mConnection);
            mContext.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public String getEngineName() {
        return "QS6-NWD-G5";
    }

    @Override
    public void tune(int freqKhz) {
        // Enviar intent de control a NWD
        Intent intent = new Intent("com.nwd.action.ACTION_SET_RADIO_FREQUENCE");
        intent.putExtra("frequence", freqKhz);
        mContext.sendBroadcast(intent);
    }

    @Override
    public int getCurrentFreq() {
        return mCurrentFreq;
    }

    @Override
    public int getCurrentBand() {
        return mCurrentBand;
    }

    @Override
    public void seekUp() {
        mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_RADIO_SEARCH_UP"));
    }

    @Override
    public void seekDown() {
        mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_RADIO_SEARCH_DOWN"));
    }

    @Override
    public void stepUp() {
        tune(mCurrentFreq + 100);
    }

    @Override
    public void stepDown() {
        tune(mCurrentFreq - 100);
    }

    @Override
    public void scan() {
        mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_RADIO_SCAN"));
    }

    @Override
    public void stopScan() {
        // NWD suele detener al sintonizar o con un comando específico
    }

    @Override
    public void bandCycle() {
        mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_RADIO_BAND_CHANGE"));
    }

    @Override
    public boolean isStereo() {
        return mIsStereo;
    }

    @Override
    public void setStereo(boolean enable) {
        // Implementar via Intent si existe
    }

    @Override
    public void setMute(boolean mute) {
        Intent intent = new Intent("com.nwd.action.ACTION_SET_MUTE");
        intent.putExtra("mute", mute ? 1 : 0);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void openEq(Context context) {
        try {
            Intent intent = new Intent("com.nwd.action.ACTION_START_NWD_ACTIVITY");
            intent.putExtra("pkg", "com.nwd.eq");
            context.startActivity(intent);
        } catch (Exception e) {}
    }

    @Override
    public boolean requestPlayAudio() {
        // NWD maneja el foco internamente usualmente
        return true;
    }

    @Override
    public void toggleRdsFeature(int type) {
        // AF/TA via Intents
    }

    @Override
    public boolean isAfEnabled() { return false; }
    @Override
    public boolean isTaEnabled() { return false; }
    @Override
    public boolean isTpEnabled() { return false; }
    
    @Override
    public boolean isScanning() {
        // V12.2: Implementación mínima para satisfacer la interfaz
        return false;
    }

    @Override
    public void toggleDxLocal() {
        mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_RADIO_LOC_DX"));
    }

    @Override
    public boolean isDxLocal() { return false; }

    @Override
    public void gotoPreset(int index) {
        // goto index
    }

    @Override
    public void nextFavorite() {
        // No soportado nativamente en QS6 vía intents por ahora
    }

    @Override
    public void prevFavorite() {
        // No soportado nativamente en QS6 vía intents por ahora
    }

    @Override
    public void setCallback(RadioEngineCallback cb) {
        this.mCallback = cb;
    }

    private void handleUartPacket(byte[] data) {
        // F0 05 ... Badajoz.91.3
        if (data.length > 2 && data[0] == (byte)0xF0 && data[1] == (byte)0x05) {
            try {
                // Saltar encabezado (aprox 7-8 bytes según logcat)
                String text = new String(data, 8, data.length - 8, "UTF-8").trim();
                if (!text.isEmpty() && mCallback != null) {
                    mCallback.onRdsText(text);
                }
            } catch (Exception e) {}
        }
    }
}
