package com.example.openradiofm.data.source;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.ts.tsspeechlib.radio.ITsSpeechRadio;
import com.ts.main.common.ITsCommon;

/**
 * Manager para unidades Topway/TS (MTK8259/8667).
 * 
 * Basado en la versión de GDUCK con mejoras en la lógica de bandas
 * y protecciones para RDS.
 */
public class MTK8259_8667RadioManager {

    private static final String TAG = "MTK8259_8667RM";

    private final Context mContext;
    private final ITsCommon mTsCommon;
    private final ITsSpeechRadio mTsSpeechRadio;

    public MTK8259_8667RadioManager(Context context, ITsCommon tsCommon, ITsSpeechRadio tsSpeechRadio) {
        this.mContext = context.getApplicationContext();
        this.mTsCommon = tsCommon;
        this.mTsSpeechRadio = tsSpeechRadio;
    }

    public boolean isConnected() {
        return mTsCommon != null && mTsSpeechRadio != null;
    }

    public void openDevice() throws RemoteException {
        if (mTsSpeechRadio != null) {
            Log.d(TAG, "openDevice(): OpenRadioCh()");
            mTsSpeechRadio.OpenRadioCh();
        }
    }

    public void closeDevice() throws RemoteException {
        if (mTsSpeechRadio != null) {
            Log.d(TAG, "closeDevice(): CloseRadioCh()");
            mTsSpeechRadio.CloseRadioCh();
        }
    }

    public int getCurrentBand() throws RemoteException {
        if (mTsSpeechRadio == null) return 0;
        int band = mTsSpeechRadio.getRadioBand();
        // Mapeo: 0,1,2 = FM, 4,5 = AM. 
        // Para compatibilidad con OpenRadioFM devolvemos la banda cruda.
        return band;
    }

    public boolean isAmBand() throws RemoteException {
        if (mTsSpeechRadio == null) return false;
        return mTsSpeechRadio.getRadioBand() >= 4;
    }

    public void gotoFreq(int freqKhz) throws RemoteException {
        if (mTsSpeechRadio == null) return;

        int tsBand = isAmBand() ? 4 : 0;
        int tsFreq = freqKhz;

        if (tsBand == 0 && tsFreq > 20000) {
            tsFreq = tsFreq / 10; // FM: 87.50 -> 8750
        }

        Log.d(TAG, "gotoFreq(): input=" + freqKhz + ", tsBand=" + tsBand + ", tsFreq=" + tsFreq);
        mTsSpeechRadio.TurnBandAndFq(tsBand, tsFreq);
    }

    public void onSeekUpEvent() throws RemoteException {
        if (mTsSpeechRadio != null) mTsSpeechRadio.SeekUp();
    }

    public void onSeekDownEvent() throws RemoteException {
        if (mTsSpeechRadio != null) mTsSpeechRadio.SeekDn();
    }

    public void onManualUpEvent() throws RemoteException {
        if (mTsSpeechRadio != null) mTsSpeechRadio.onNextFreq();
    }

    public void onManualDownEvent() throws RemoteException {
        if (mTsSpeechRadio != null) mTsSpeechRadio.onPrevFreq();
    }

    /**
     * Ciclo corto: Solo cambia dentro del grupo actual (FM1->FM2->FM3 o AM1->AM2)
     */
    public void onBandCycle() throws RemoteException {
        if (mTsSpeechRadio == null) return;
        
        if (isAmBand()) {
            Log.d(TAG, "BandCycle AM -> onRadioAM()");
            mTsSpeechRadio.onRadioAM();
        } else {
            Log.d(TAG, "BandCycle FM -> onRadioFM()");
            mTsSpeechRadio.onRadioFM();
        }
    }

    /**
     * Intercambio AM/FM (Sugerencia de GDUCK para simplificar)
     */
    public void toggleAmFm() throws RemoteException {
        if (mTsSpeechRadio == null) return;
        
        if (isAmBand()) {
            Log.d(TAG, "Toggle AM to FM -> onRadioFM()");
            mTsSpeechRadio.onRadioFM();
        } else {
            Log.d(TAG, "Toggle FM to AM -> onRadioAM()");
            mTsSpeechRadio.onRadioAM();
        }
    }

    public int getCurrentFreq() throws RemoteException {
        if (mTsCommon == null) return 87500;

        String freq = mTsCommon.GetFreq();
        if (freq == null) return 87500;

        String normalized = freq.replace(".", "").replace(",", "").trim();
        try {
            int value = Integer.parseInt(normalized);
            if (!isAmBand() && value < 20000) {
                value *= 10;
            }
            return value;
        } catch (Exception e) {
            Log.e(TAG, "getCurrentFreq parse failed: " + freq, e);
            return 87500;
        }
    }

    // --- Métodos RDS seguros (Protegidos contra NoSuchMethodError) ---

    public String getPsNameSafe() {
        if (mTsCommon == null) return null;
        try {
            return mTsCommon.GetPsName();
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            // NoSuchMethodError ocurre si el MainUI.apk no tiene el MOD de GDUCK
            return null;
        }
    }

    public String getPtyStrSafe() {
        if (mTsCommon == null) return null;
        try {
            return mTsCommon.GetPtyStr();
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            return null;
        }
    }

    public void setMute(boolean mute) {
        try {
            if (mTsCommon != null) {
                if (mute && !mTsCommon.IsMute()) {
                    mTsCommon.Mute();
                } else if (!mute && mTsCommon.IsMute()) {
                    mTsCommon.Mute(); // El comando es un toggle en TS
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Mute failed", t);
        }
    }

    public void openEq() {
        try {
            if (mTsCommon != null) mTsCommon.GotoEq();
        } catch (Throwable t) {
            Log.e(TAG, "openEq failed", t);
        }
    }

    public ITsSpeechRadio getRawService() {
        return mTsSpeechRadio;
    }
}
