package com.example.openradiofm.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.example.openradiofm.data.source.K706Engine;
import com.example.openradiofm.data.source.K706RadioManager;

/**
 * V18.6: Gestor de Hardware y MCU.
 * Encapsula la comunicación de bajo nivel, comandos de sintonizador y receivers de sistema.
 */
public class HardwareManager {
    private static final String TAG = "HardwareManager";
    private final MainActivity mActivity;
    
    private final BroadcastReceiver mBtStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.qf.action.BT_STATE".equals(intent.getAction())) {
                int state = intent.getIntExtra("state", -1);
                Log.d(TAG, "BT_STATE Broadcast Received: " + state);

                if (mActivity.mEngine instanceof K706Engine) {
                    K706RadioManager k706Manager = ((K706Engine) mActivity.mEngine).getManager();
                    handleK706AudioRecovery(k706Manager, state);
                }
            }
        }
    };

    public HardwareManager(MainActivity activity) {
        this.mActivity = activity;
    }

    public void registerReceivers() {
        IntentFilter filter = new IntentFilter("com.qf.action.BT_STATE");
        mActivity.registerReceiver(mBtStateReceiver, filter);
    }

    public void unregisterReceivers() {
        try {
            mActivity.unregisterReceiver(mBtStateReceiver);
        } catch (Exception e) {
            Log.w(TAG, "Error unregistering BT receiver: " + e.getMessage());
        }
    }

    private void handleK706AudioRecovery(K706RadioManager k706Manager, int state) {
        if (state == 0) { // Desconectado
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    k706Manager.enforceAudioChannelRecovery();
                    mActivity.showToast("Recuperando Audio FM...");
                } catch (Exception e) {
                    Log.e(TAG, "Error recuperando canal FM tras BT", e);
                }
            }, 500);
        } else if (state == 2) { // Conectado
            try {
                k706Manager.setMute(true);
                k706Manager.returnAudioChannel();
            } catch (Exception e) {
                Log.e(TAG, "Error cediendo canal FM tras BT connect", e);
            }
        }
    }

    /**
     * Envía una tecla al MCU del coche usando reflexión sobre android.carsource.McuManager.
     */
    public void sendMcuKey(int key) {
        try {
            Class<?> mcuClass = Class.forName("android.carsource.McuManager");
            java.lang.reflect.Method getInstance = mcuClass.getMethod("getsInstance");
            Object instance = getInstance.invoke(null);
            java.lang.reflect.Method injectKey = mcuClass.getMethod("injectKeyEventTimeout", int.class, int.class);
            injectKey.invoke(instance, key, 0x32);
            Log.d(TAG, "MCU Key injected: " + key);
        } catch (Exception e) {
            Log.e(TAG, "Error injecting MCU key: " + e.getMessage());
            mActivity.showToast("Hardware EQ no soportado en este dispositivo");
        }
    }

    /**
     * Envía comandos específicos al sintonizador via MCU (Protocolo QF/K706).
     */
    public void sendMcuTunerCmd(byte subCmd, byte param1, byte param2) {
        try {
            Intent intent = new Intent("com.qf.intent.action.TUNER_CMD");
            intent.putExtra("sub_cmd", subCmd);
            intent.putExtra("param1", param1);
            intent.putExtra("param2", param2);
            mActivity.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error sending MCU Tuner Cmd", e);
        }
    }
}
