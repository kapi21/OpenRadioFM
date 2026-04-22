package com.android.fmradio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Stub del servicio OEM; devuelve un Binder vacío para que no falle un bind superficial.
 * OpenRadioFM usa el motor K706, no este AIDL.
 */
public class FmService extends Service {

    private final IBinder mBinder = new Binder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
