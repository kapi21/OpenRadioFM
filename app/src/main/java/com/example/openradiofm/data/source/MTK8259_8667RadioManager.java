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
        // Mapeo: 0,1,2 = FM, 4 = AM1, 5 = AM2. 
        // Csaba sugiere mapear 4->3 y 5->4 para evitar huecos en la UI.
        if (band == 4) return 3;
        if (band == 5) return 4;
        return band;
    }

    public boolean isAmBand() throws RemoteException {
        if (mTsSpeechRadio == null) return false;
        int band = mTsSpeechRadio.getRadioBand();
        // Hardware bands: 4=AM1, 5=AM2.
        return band == 4 || band == 5;
    }

    public boolean isStereo() {
        try {
            if (mTsCommon != null) {
                return mTsCommon.GetRadioSTState();
            }
        } catch (Throwable t) {
            Log.e(TAG, "isStereo failed", t);
        }
        return false;
    }

    public void gotoFreq(int freqKhz) throws RemoteException {
        if (mTsSpeechRadio == null) return;

        // Bandas en HW: FM=0,1,2, AM=4,5. 
        // Usamos la banda actual del hardware para no saltar de grupo (AM1->AM2)
        int tsBand = mTsSpeechRadio.getRadioBand();
        if (tsBand < 0) tsBand = 0;
        
        int tsFreq = freqKhz;
        // Si estamos en banda FM (0-2), la frecuencia se divide por 10 para el HW (ej: 8750)
        if (tsBand <= 2 && tsFreq > 20000) {
            tsFreq = tsFreq / 10;
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
            String pty = mTsCommon.GetPtyStr();
            if (pty == null) return null;
            return mapPtyToId(pty.trim()); // V18.9: También mapear el PTY directo a ID
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            return null;
        }
    }

    private String mapPtyToId(String category) {
        if (category == null) return null;

        String clean = category.replace(",", "").trim();
        String lower = clean.toLowerCase();

        switch (lower) {
            case "news": return "1";
            case "current affairs": return "2";
            case "information": return "3";
            case "sport": return "4";
            case "education": return "5";
            case "drama": return "6";
            case "culture":
            case "cultures": return "7";
            case "science": return "8";
            case "varied": return "9";
            case "pop music": return "10";
            case "rock music": return "11";
            case "easy listening": return "12";
            case "light classical": return "13";
            case "serious classical": return "14";
            case "other music": return "15";
            case "weather": return "16";
            case "finance": return "17";
            case "children": return "18";
            case "social affairs": return "19";
            case "religion": return "20";
            case "phone in": return "21";
            case "travel": return "22";
            case "leisure": return "23";
            case "jazz": return "24";
            case "country": return "25";
            case "national music": return "26";
            case "oldies": return "27";
            case "folk": return "28";
            case "documentary": return "29";
            default: return clean; // V18.9: Devolver el original si no es una categoría conocida
        }
    }

    public String getCategorySafe() {
        if (mTsCommon == null) return null;
        try {
            String category = mTsCommon.GetCategory();
            if (category == null) return null;
            return mapPtyToId(category.trim()); // V18.9: Usar el mapeador con fallback
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            return null;
        }
    }

    public void setMute(boolean mute) {
        try {
            // V18.6: Usar AudioManager de Android para evitar que el MainUI de Topway
            // bloquee la barra de volumen o deje el mute de hardware persistente.
            android.media.AudioManager am = (android.media.AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, 
                        mute ? android.media.AudioManager.ADJUST_MUTE : android.media.AudioManager.ADJUST_UNMUTE, 0);
                } else {
                    am.setStreamMute(android.media.AudioManager.STREAM_MUSIC, mute);
                }
                Log.d(TAG, "Mute set to " + mute + " via AudioManager");
            }
            
            // V18.6: Usar tanto AudioManager como mTsCommon para asegurar el mute.
            // Csaba reporta que solo AudioManager no es suficiente en algunas unidades.
            if (mTsCommon != null && mute != mTsCommon.IsMute()) {
                mTsCommon.Mute();
                Log.d(TAG, "Hardware Mute toggled via TsCommon");
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
