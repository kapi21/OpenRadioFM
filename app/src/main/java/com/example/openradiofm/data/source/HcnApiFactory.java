package com.example.openradiofm.data.source;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

import com.hcn.autoradio.IRadioServiceAPI;

/**
 * Egyetlen belépési pont a com.hcn.autoradio Binder becsomagolásához.
 * Felismeri, hogy a szolgáltatás a régi (MT8163, 25 tranzakció) vagy az új
 * (Junsun V7 / MT8768, 31 tranzakció) szerződést használja.
 *
 * Felismerés: a 26-os tranzakció a V7-en IsSeek() (csak olvas, mellékhatás nélkül),
 * a 25 tranzakciós szolgáltatásnál ismeretlen kód, amit a Binder false-szal utasít el.
 *
 * Kézi felülbírálás (RadioPresets SharedPreferences, "pref_hcn_contract"):
 *   0 = automatikus (alap), 25 = MT8163 kényszerítése, 31 = V7 kényszerítése.
 */
public final class HcnApiFactory {
    private static final String TAG = "HcnApiFactory";
    public static final String PREF_CONTRACT = "pref_hcn_contract";

    private static IBinder sProbedBinder;
    private static boolean sProbedV31;

    private HcnApiFactory() {}

    public static IRadioServiceAPI wrap(Context context, IBinder binder) {
        if (binder == null) return null;
        return isV31(context, binder)
                ? new HcnV31Api(binder)
                : IRadioServiceAPI.Stub.asInterface(binder);
    }

    public static boolean isV31(Context context, IBinder binder) {
        int forced = 0;
        try {
            if (context != null) {
                forced = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                        .getInt(PREF_CONTRACT, 0);
            }
        } catch (Exception ignored) {}
        if (forced == 31) return true;
        if (forced == 25) return false;
        synchronized (HcnApiFactory.class) {
            if (binder == sProbedBinder) return sProbedV31;
        }
        boolean v31 = probe(binder);
        synchronized (HcnApiFactory.class) {
            sProbedBinder = binder;
            sProbedV31 = v31;
        }
        Log.i(TAG, "HCN szerzodes: " + (v31 ? "V31 (Junsun V7 / MT8768)" : "25 (MT8163)"));
        return v31;
    }

    private static boolean probe(IBinder binder) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (!HcnV31Api.DESCRIPTOR.equals(binder.getInterfaceDescriptor())) return false;
            data.writeInterfaceToken(HcnV31Api.DESCRIPTOR);
            if (!binder.transact(26, data, reply, 0)) return false;
            reply.readException();
            reply.readInt();
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
