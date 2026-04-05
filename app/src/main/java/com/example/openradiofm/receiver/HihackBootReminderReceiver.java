package com.example.openradiofm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.openradiofm.util.HiHackBootReminder;

/**
 * Tras reinicio, muchas ROM apagan HiHack; no se puede volver a encender sin el usuario.
 * Esperamos unos segundos y marcamos {@link HiHackBootReminder#PREF_PENDING_AFTER_BOOT} si sigue apagado.
 */
public class HihackBootReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        final Context app = context.getApplicationContext();
        final PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                Thread.sleep(18_000L);
                HiHackBootReminder.markPendingIfHihackStillOff(app);
            } catch (InterruptedException ignored) {
            } finally {
                pendingResult.finish();
            }
        }, "OpenRadioFM-HiHackBoot").start();
    }
}
