package com.example.openradiofm.data.source;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
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
    private final android.content.SharedPreferences mPrefs;
    private final ITsCommon mTsCommon;
    private final ITsSpeechRadio mTsSpeechRadio;
    
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private AudioFocusRequest mAudioFocusRequest;
    private boolean mIsAudioFocusHeld = false;

    public MTK8259_8667RadioManager(Context context, ITsCommon tsCommon, ITsSpeechRadio tsSpeechRadio) {
        this.mContext = context.getApplicationContext();
        this.mTsCommon = tsCommon;
        this.mTsSpeechRadio = tsSpeechRadio;
        this.mPrefs = mContext.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
        
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusChangeListener = focusChange -> {
            Log.d(TAG, "onAudioFocusChange: " + focusChange + " (Ignored in MTK8259 to prevent mute bugs)");
            /*
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    setMute(true);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    setMute(false);
                    break;
            }
            */
        };
    }

    private boolean isV5StreamMixerCompatEnabled() {
        try {
            return mPrefs != null && mPrefs.getBoolean("pref_mtk8259_v5_stream_mixer_compat", false);
        } catch (Exception ignored) {
            return false;
        }
    }



    public boolean isConnected() {
        return mTsCommon != null && mTsSpeechRadio != null;
    }

    public void openDevice() throws RemoteException {
        if (mTsSpeechRadio != null) {
            Log.d(TAG, "openDevice(): OpenRadioCh() + EnterMode(1)");
            mTsSpeechRadio.OpenRadioCh();
            if (mTsCommon != null) {
                mTsCommon.EnterMode(1); // Forzar conmutación de audio a Radio
            }
            // requestAudioFocus(); // V19.3: Desactivado por Csaba para evitar mute en recreación
        }
    }

    public void closeDevice() throws RemoteException {
        if (mTsSpeechRadio != null) {
            Log.d(TAG, "closeDevice(): CloseRadioCh() + EnterMode(0)");
            mTsSpeechRadio.CloseRadioCh();
            if (mTsCommon != null) {
                mTsCommon.EnterMode(0); // Devolver audio al sistema
            }
            // abandonAudioFocus(); // V19.3: Desactivado por Csaba
        }
    }

    /**
     * V21.4: Mismo cierre de canal que {@link #closeDevice()} pero sin soltar el servicio AIDL.
     * Streaming: solo {@code CloseRadioCh()} puede dejar FM mezclándose con ExoPlayer; se añade
     * {@code EnterMode(0)} para devolver el mixer al sistema (alineado con {@link #closeDevice()}).
     * <p>
     * Referencia APK OpenRadioFM v5.0 (Stability Beta): {@code setOnlineStreamingActive} en el engine
     * era no-op; solo {@code switchToAndroidAudio()} llamaba a {@code CloseRadioCh()} (sin
     * {@code EnterMode}). Si Csaba/OEM reporta regresión con esta ruta, valorar fallback solo
     * {@code CloseRadioCh()} o pref OEM (comparar con smali de una descompilación local, no versionada).
     */
    public void switchMixerToAndroidAudio() {
        try {
            if (isV5StreamMixerCompatEnabled()) {
                // Compat v5.0: solo CloseRadioCh, sin mute explícito ni EnterMode.
                if (mTsSpeechRadio != null) {
                    mTsSpeechRadio.CloseRadioCh();
                }
                Log.d(TAG, "switchMixerToAndroidAudio(): LEGACY v5.0 -> CloseRadioCh only");
                return;
            }
            // V21.5: NO llamamos setMute(true) aquí. El fix de ACTION_FORCE_PLAY ya evita que
            // el canal FM se reactive durante el streaming. setMute(true) silenciaba el amplificador
            // del coche y ExoPlayer no era audible (bug "LIVE activa el mute" - Csaba).
            if (mTsSpeechRadio != null) {
                mTsSpeechRadio.CloseRadioCh();
            }
            if (mTsCommon != null) {
                mTsCommon.EnterMode(0);
            }
            Log.d(TAG, "switchMixerToAndroidAudio(): CloseRadioCh + EnterMode(0) [sin mute]");
        } catch (Throwable t) {
            Log.e(TAG, "switchMixerToAndroidAudio failed", t);
        }
    }

    /**
     * V21.4: Volver a ruta FM (OpenRadioCh + EnterMode(1)), alineado con {@link #openDevice()}.
     */
    public void switchMixerToFmAudio() {
        try {
            if (isV5StreamMixerCompatEnabled()) {
                // Compat v5.0: solo OpenRadioCh, sin mute ni EnterMode.
                if (mTsSpeechRadio != null) {
                    mTsSpeechRadio.OpenRadioCh();
                }
                Log.d(TAG, "switchMixerToFmAudio(): LEGACY v5.0 -> OpenRadioCh only");
                return;
            }
            setMute(false);
            if (mTsSpeechRadio != null) {
                mTsSpeechRadio.OpenRadioCh();
            }
            if (mTsCommon != null) {
                mTsCommon.EnterMode(1);
            }
            Log.d(TAG, "switchMixerToFmAudio(): setMute(false) + OpenRadioCh + EnterMode(1)");
        } catch (Throwable t) {
            Log.e(TAG, "switchMixerToFmAudio failed", t);
        }
    }
    
    // --- Lógica de AudioFocus (V19.2) ---
    
    public void requestAudioFocus() {
        if (mAudioManager == null || mIsAudioFocusHeld) return;
        
        try {
            int result;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                        .build();
                result = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            } else {
                result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mIsAudioFocusHeld = true;
                Log.d(TAG, "AudioFocus GRANTED");
            } else {
                Log.w(TAG, "AudioFocus DENIED");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting AudioFocus", e);
        }
    }

    public void abandonAudioFocus() {
        if (mAudioManager == null || !mIsAudioFocusHeld) return;
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && mAudioFocusRequest != null) {
                mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
            } else {
                mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
            }
            mIsAudioFocusHeld = false;
            Log.d(TAG, "AudioFocus ABANDONED");
        } catch (Exception e) {
            Log.e(TAG, "Error abandoning AudioFocus", e);
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

    // V18.6.4: Csaba confirma que onNextFreq/onPrevFreq solo ciclan presets de fábrica.
    // Fix: Calcular la frecuencia manualmente y usar gotoFreq().
    public void onManualUpEvent() throws RemoteException {
        int freq = getCurrentFreq();
        int step = isAmBand() ? 9 : 100; // 9 kHz para AM, 100 kHz para FM
        gotoFreq(freq + step);
    }

    public void onManualDownEvent() throws RemoteException {
        int freq = getCurrentFreq();
        int step = isAmBand() ? 9 : 100;
        gotoFreq(freq - step);
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

    public String getRtSafe() {
        if (mTsCommon == null) return null;
        try {
            // V18.9: El análisis de ingeniería inversa confirma que GetPtyStr() en Topway devuelve el Radio Text (RT).
            return mTsCommon.GetPtyStr();
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

    public String getPtySafe() {
        if (mTsCommon == null) return null;
        try {
            // V18.9: El análisis de ingeniería inversa confirma que GetCategory() devuelve el PTY.
            String category = mTsCommon.GetCategory();
            if (category == null) return null;
            return mapPtyToId(category.trim());
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            return null;
        }
    }

    // V18.6.4: Csaba confirma que AudioManager NO tiene efecto en el volumen de radio.
    // Solo mTsCommon.Mute() funciona. Alternativa: OpenRadioCh/CloseRadioCh.
    public void setMute(boolean mute) {
        try {
            if (mTsCommon != null && mute != mTsCommon.IsMute()) {
                mTsCommon.Mute();
                Log.d(TAG, "Mute toggled via TsCommon: target=" + mute);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Mute failed", t);
        }
    }


    /**
     * V20.0: Fuerza un estado de sonido activo.
     * Útil para solventar el bug de audio silenciado al arrancar en MTK8259.
     */
    // V18.6.4: Simplificado — solo mTsCommon.Mute() + OpenRadioCh() (sin AudioManager)
    public void forceUnmute() {
        try {
            // Asegurar canal abierto
            if (mTsSpeechRadio != null) {
                mTsSpeechRadio.OpenRadioCh();
            }
        } catch (Exception e) {
            Log.e(TAG, "forceUnmute failed", e);
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

    // --- Nuevos métodos RDS Status (V18.9) ---

    public boolean isAfActiveSafe() {
        if (mTsCommon == null) return false;
        try {
            return mTsCommon.RdsAf();
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            return false;
        }
    }

    public boolean isTaActiveSafe() {
        if (mTsCommon == null) return false;
        try {
            return mTsCommon.RdsTa();
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            return false;
        }
    }

    public boolean isTpActiveSafe() {
        if (mTsCommon == null) return false;
        try {
            return mTsCommon.RdsTp();
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            return false;
        }
    }

    public void toggleAfSafe() {
        if (mTsCommon == null) return;
        try {
            mTsCommon.RdsAfSwitch();
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            Log.e(TAG, "toggleAfSafe not supported", e);
        }
    }

    public void toggleTaSafe() {
        if (mTsCommon == null) return;
        try {
            mTsCommon.RdsTaSwitch();
        } catch (AbstractMethodError | NoSuchMethodError | Exception e) {
            Log.e(TAG, "toggleTaSafe not supported", e);
        }
    }
}
