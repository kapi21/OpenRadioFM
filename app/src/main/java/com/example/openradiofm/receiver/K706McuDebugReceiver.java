package com.example.openradiofm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import com.example.openradiofm.data.source.K706Engine;
import com.example.openradiofm.data.source.K706RadioManager;
import com.example.openradiofm.data.source.RadioEngine;
import com.example.openradiofm.ui.main.RadioServiceController;

/**
 * Receiver de depuración para forzar comandos MCU vía ADB.
 *
 * Seguridad: solo acepta broadcasts enviados por shell (uid 2000) o por la propia app.
 *
 * Uso (ejemplos):
 * adb shell am broadcast -a com.example.openradiofm.K706_MCU_DEBUG -n com.example.openradiofm/.receiver.K706McuDebugReceiver --ez rds true --ez af true --ez ta false
 */
public final class K706McuDebugReceiver extends BroadcastReceiver {
    private static final String TAG = "K706McuDebugRx";
    public static final String ACTION = "com.example.openradiofm.K706_MCU_DEBUG";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (!ACTION.equals(intent.getAction())) return;

        // Nota: en BroadcastReceiver no siempre está disponible una API estable para obtener el uid emisor
        // en todas las toolchains/stubs. Usamos Binder.getCallingUid() como aproximación (suficiente para ADB).
        int senderUid;
        try {
            senderUid = android.os.Binder.getCallingUid();
        } catch (Throwable t) {
            senderUid = -1;
        }

        // Allow shell (adb) or self.
        if (senderUid != Process.SHELL_UID && senderUid != Process.myUid()) {
            Log.w(TAG, "Rejected broadcast from uid=" + senderUid);
            return;
        }

        RadioEngine engine = RadioServiceController.peekSharedLocalEngine();
        if (!(engine instanceof K706Engine)) {
            Log.w(TAG, "No K706Engine active (engine=" + (engine != null ? engine.getEngineName() : "null") + ")");
            return;
        }
        K706RadioManager mgr = ((K706Engine) engine).getManager();
        if (mgr == null) {
            Log.w(TAG, "K706RadioManager null");
            return;
        }

        // Optional idempotent setters.
        if (intent.hasExtra("rds")) {
            boolean v = intent.getBooleanExtra("rds", true);
            mgr.setRdsEnabled(v);
        }
        if (intent.hasExtra("af")) {
            boolean v = intent.getBooleanExtra("af", false);
            mgr.setAfEnabled(v);
        }
        if (intent.hasExtra("ta")) {
            boolean v = intent.getBooleanExtra("ta", false);
            mgr.setTaEnabled(v);
        }

        // A convenience "pulse": force reassert listener (handshake 1001 + register).
        if (intent.getBooleanExtra("reassert", false)) {
            mgr.reassertMcuInfoListener();
        }

        Log.i(TAG, "Applied MCU debug cmd: rds=" + (intent.hasExtra("rds") ? intent.getBooleanExtra("rds", true) : null)
                + " af=" + (intent.hasExtra("af") ? intent.getBooleanExtra("af", false) : null)
                + " ta=" + (intent.hasExtra("ta") ? intent.getBooleanExtra("ta", false) : null)
                + " reassert=" + intent.getBooleanExtra("reassert", false));
    }
}

