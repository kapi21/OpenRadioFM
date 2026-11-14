package com.example.openradiofm.data.source;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.hcn.autoradio.IRadioCallBack;
import com.hcn.autoradio.IRadioServiceAPI;

/**
 * Junsun V7 (MT8768) / HCN "31-es" szerződés adapter.
 *
 * A projekt AIDL-je (IRadioServiceAPI) az MT8163-as 25 tranzakciós szerződés. A V7 stock
 * com.hcn.autoradio szolgáltatása azonos descriptorral, de a 17. tranzakciótól eltérő
 * kiosztással rendelkezik (favoriteCurrentFreq, getCurrentFreqRdsPs, ... beékelődnek).
 * Ez az osztály az ismert 25-ös interfészt a V7 tranzakciókódjaira fordítja nyers
 * transact()-tal, így a MT8163Engine / RadioServiceController változtatás nélkül használhatja.
 *
 * Tranzakciókiosztás a V7-en (Radio+ IRadioServiceAPI.aidl alapján):
 *  1-16 azonos a 25-ös szerződéssel
 *  17 favoriteCurrentFreq  18 getCurrentBand  19 getCurrentFreq  20 getCurrentFreqRdsPs
 *  21 getFreqIsFavorite    22 currentFreqIsFavorite
 *  23 IsAS 24 IsPS 25 IsScan 26 IsSeek 27 IsStereo 28 IsDxLocal
 *  29 requestPlayAudio 30 requestAudioFocus 31 releaseAudioFocus
 */
final class HcnV31Api implements IRadioServiceAPI {
    private static final String TAG = "HcnV31Api";
    static final String DESCRIPTOR = "com.hcn.autoradio.IRadioServiceAPI";

    private final IBinder binder;

    HcnV31Api(IBinder binder) { this.binder = binder; }

    @Override public IBinder asBinder() { return binder; }

    private interface Writer { void write(Parcel data); }

    private int call(int code, Writer writer, boolean readsInt) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            if (writer != null) writer.write(data);
            if (!binder.transact(code, data, reply, 0)) {
                throw new RemoteException("HCN V31: tranzakcio elutasitva: " + code);
            }
            reply.readException();
            return readsInt ? reply.readInt() : 0;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private void command(int code) throws RemoteException { call(code, null, false); }
    private boolean flag(int code) throws RemoteException { return call(code, null, true) != 0; }

    // --- 1-16: azonos kódok ---
    @Override public void registerRadioClientBinder(IBinder b) throws RemoteException {
        call(1, d -> d.writeStrongBinder(b), false);
    }
    @Override public void unRegisterRadioClientBinder() throws RemoteException { command(2); }
    @Override public void registerRadioCallback(IRadioCallBack cb) throws RemoteException {
        call(3, d -> d.writeStrongBinder(cb == null ? null : cb.asBinder()), false);
    }
    @Override public void unRegisterRadioCallback(IRadioCallBack cb) throws RemoteException {
        call(4, d -> d.writeStrongBinder(cb == null ? null : cb.asBinder()), false);
    }
    @Override public void onBandEvent() throws RemoteException { command(5); }
    @Override public void onASEvent() throws RemoteException { command(6); }
    @Override public void onPSEvent() throws RemoteException { command(7); }
    @Override public void onLocDxEvent() throws RemoteException { command(8); }
    @Override public void onSeekDownEvent() throws RemoteException { command(9); }
    @Override public void onSeekUpEvent() throws RemoteException { command(10); }
    @Override public void onManualUpEvent() throws RemoteException { command(11); }
    @Override public void onManualDownEvent() throws RemoteException { command(12); }
    @Override public void onScanEvent() throws RemoteException { command(13); }
    @Override public void gotoFreq(int freq) throws RemoteException {
        call(14, d -> d.writeInt(freq), false);
    }
    @Override public void gotoFreq2(String freq) throws RemoteException {
        call(15, d -> d.writeString(freq), false);
    }
    @Override public void gotoFreqIndex(int index) throws RemoteException {
        call(16, d -> d.writeInt(index), false);
    }

    // --- eltolt kódok ---
    @Override public int getCurrentBand() throws RemoteException { return call(18, null, true); }
    @Override public int getCurrentFreq() throws RemoteException { return call(19, null, true); }
    @Override public boolean IsAS() throws RemoteException { return flag(23); }
    @Override public boolean IsPS() throws RemoteException { return flag(24); }
    @Override public boolean IsScan() throws RemoteException { return flag(25); }
    @Override public boolean IsSeek() throws RemoteException { return flag(26); }
    @Override public boolean IsStereo() throws RemoteException { return flag(27); }
    @Override public boolean IsDxLocal() throws RemoteException { return flag(28); }

    /**
     * 29: audio utvonal; 30: requestAudioFocus, ami a V7-en egyben a gyartoi unmute is
     * (Radio+ RadioPlaybackService szerint). A 30-as hiba nem fatalis.
     */
    @Override public boolean requestPlayAudio() throws RemoteException {
        boolean ok = flag(29);
        try {
            command(30);
        } catch (RemoteException | RuntimeException e) {
            Log.w(TAG, "requestAudioFocus (V31 #30) sikertelen: " + e);
        }
        return ok;
    }

    /** A V7 AIDL-ben nincs; az RDS/AF/TA kapcsolast a HiddenRadioPlayer vegzi. */
    @Override public void toggleRdsFeature(int type) {
        Log.d(TAG, "toggleRdsFeature(" + type + "): V31 szolgaltatasban nincs; kihagyva");
    }
}
