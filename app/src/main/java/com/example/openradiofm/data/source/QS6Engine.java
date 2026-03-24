package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.example.openradiofm.utils.MetadataUtils;
import com.nwd.radio.service.RadioCallback;
import com.nwd.radio.service.RadioFeature;
import com.nwd.radio.service.data.Frequency;
import com.nwd.radio.service.data.RadioPoint;

/**
 * V14.0+: Motor nativo AIDL para QS6 G5 (NWD) — {@code com.nwd.radio.service.RadioService}.
 * V22.0: Callbacks AIDL completos (PTY, DX/local, estéreo piloto+decodificador), {@code toggleRdsFeature}
 * alineado con {@link RadioEngine} (0/1/2), {@code gotoPreset}, sync tras bind y fallback stopScan.
 */
public class QS6Engine implements RadioEngine {
    private static final String TAG = "QS6Engine";
    /** Filtra en logcat: debe aparecer junto a MediaFocusControl con callingPack=com.example.openradiofm */
    private static final String FOCUS_TAG = "OpenRadioFM-AudioFocus";
    private Context mContext;
    private RadioEngineCallback mCallback;
    private long mInitElapsedMs = -1L;
    private boolean mStartupAutoScanBlocked = false;

    // Estado local sincronizado por AIDL
    private int mCurrentFreq = 87500;
    private int mCurrentBand = 0;
    /** Piloto RDS / portadora con subportadora estéreo (callback {@code notifyStereo}). */
    private boolean mIsStereo = false;
    /**
     * Decodificación estéreo activa en el stack NWD ({@code setStreroOn} / {@code notifyStereoOn}).
     * La UI usa {@link #effectiveStereoForUi()}: sin decodificador no se muestra estéreo aunque haya piloto.
     */
    private boolean mStereoDecoderOn = true;
    private boolean mIsMute = false;
    private boolean mIsAfEnabled = false;
    private boolean mIsTaEnabled = false;
    private boolean mIsTpEnabled = false;
    private boolean mIsScanning = false;
    private boolean mIsDxLocal = false;
    private String mLastPs = "";
    private int mLastReportedFreq = -1;
    private String mLastReportedPs = "";
    private final android.os.Handler mMainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    // Última sintonía solicitada por la app (protección anti-override de la radio nativa al arranque).
    private int mRequestedFreqKhz = -1;
    private int mRequestedBand = 0;
    private long mRequestedTuneElapsedMs = -1L;
    private int mRequestedTuneReasserts = 0;
    private int mLastStableFreqKhz = -1;
    private int mLastStableBand = 0;
    private long mLastStableSeenElapsedMs = -1L;

    // Intents de Emisión (Encendido/Apagado General del MCU)
    private static final String ACTION_CHANGE_SOURCE = "com.nwd.action.ACTION_CHANGE_SOURCE";
    private static final String ACTION_KEY_VALUE = "com.nwd.action.ACTION_KEY_VALUE";
    private static final String ACTION_START_NWD_ACTIVITY = "com.nwd.action.ACTION_START_NWD_ACTIVITY";
    
    // V21.0: Shadow Motor Intents (NWD Broadcasts)
    private static final String ACTION_SEND_RADIO_FREQUENCE_NEW = "com.nwd.action.ACTION_SEND_RADIO_FREQUENCE_NEW";
    private static final String ACTION_SEND_RADIO_RDS_RT = "com.nwd.action.ACTION_SEND_RADIO_RDS_RT";
    
    // V21.0: Shadow Motor Control Intents (NWD Commands)
    private static final String ACTION_SET_RADIO_FREQUENCE = "com.nwd.action.ACTION_SET_RADIO_FREQUENCE";
    private static final String ACTION_SEARCH_UP = "com.nwd.action.ACTION_SEARCH_UP";
    private static final String ACTION_SEARCH_DOWN = "com.nwd.action.ACTION_SEARCH_DOWN";
    private static final String ACTION_AMS = "com.nwd.action.ACTION_AMS";

    // V21.0: Shadow Motor Settings Keys
    private static final String SETTING_NWD_FREQ = "nwd_radio_current_freq";
    private static final String SETTING_NWD_PS = "nwd_radio_current_ps_data";
    /** Banda actual que escribe NWD junto a la frecuencia (ver Fase D / RadioProtocalUtil). */
    private static final String SETTING_NWD_BAND = "nwd_radio_current_band";
    /**
     * Máscara RDS escrita por NWD al recibir el grupo MCU ({@code RadioProtocalUtil.responseRDS}).
     * Bit TP = Traffic Program (emisora TP), no el interruptor TA ni AF.
     * Alineado con {@code com.nwd.radio.service.RadioConstant.RDSType} en smali OEM.
     */
    private static final String SETTING_NWD_RDS_MASK = "nwd_radio_rds_enable";
    private static final int NWD_RDS_BIT_AF = 0x1;
    private static final int NWD_RDS_BIT_TA = 0x2;
    /** Traffic Program (programa de tráfico) — solo debe activar el icono TP cuando el bit viene del RDS real. */
    private static final int NWD_RDS_BIT_TP = 0x8;
    private static final int NWD_RDS_BIT_RDS = 0x80;

    // KEY_VALUE Mapeos
    private static final byte KEY_FM = 0x48;

    private static final byte SOURCE_RADIO = 0x04;
    private static final byte SOURCE_ANDROID = 0x00;

    // AIDL stubs
    private RadioFeature mNwdService;
    private boolean mIsBound = false;
    private int mRetryCount = 0;
    private static final int MAX_RETRIES = 5;
    private static final long STARTUP_SCAN_GUARD_WINDOW_MS = 15000L;
    private static final long REQUESTED_TUNE_PROTECT_WINDOW_MS = 12000L;
    private static final long STABLE_FREQ_RESTORE_WINDOW_MS = 25000L;

    /** Paquete del servicio de radio NWD (QS6). */
    private static final String NWD_RADIO_PACKAGE = "com.nwd.radio.service";

    /**
     * Ajustes de audio OEM (logcat radio nativa: {@code PlayradiocmdCommon__GotoAudioSetting} →
     * {@code ActivityTaskManager} lanza {@code com.nwd.audioset/.home_horizontalActivity}).
     */
    private static final String NWD_AUDIO_SETTINGS_PACKAGE = "com.nwd.audioset";
    /** Actividad horizontal que abre el botón EQ en {@code com.nwd.radio} (mismo target que la OEM). */
    private static final String NWD_AUDIO_SETTINGS_CLASS = "com.nwd.audioset.home_horizontalActivity";

    /**
     * Paquete alternativo en algunos firmwares / documentación previa; se intenta después de {@link #NWD_AUDIO_SETTINGS_PACKAGE}.
     */
    private static final String NWD_EQ_PACKAGE = "com.nwd.eq";

    /**
     * AppID de {@code com.nwd.radio} en {@code ApplicationList.xml} / SourceConstant.
     * SprdRadioManager / AWRadioManager solo ejecutan {@code InitFM()}+{@code SendArmFmMediaPlay()}
     * si {@code ACTION_APP_IN_OUT} trae {@code extra_app_id == 8}; el default del intent es 4 (launcher).
     */
    private static final int NWD_APPLICATION_ID_RADIO = 8;

    /** Nombre con typo en firmware NWD (SprdRadioManager$1). Cierra el camino de audio FM en ARM. */
    private static final String ACTION_EXIT_ARM_FM_RADIO = "com.nwd.android.ACTION_EXIT_ARM_FM_RAIDO";

    /**
     * Evita spam de ACTION_CHANGE_SOURCE: onResume + init + recovery disparaban docenas de broadcasts
     * y el SourceMgr (com.nwd.setting.service) puede quedar en estado incoherente.
     */
    /** -1 = aún no hemos enviado {@code ACTION_CHANGE_SOURCE} a radio en esta sesión. */
    private long mLastSourceRadioBroadcastElapsedMs = -1L;
    private static final long MIN_PLAY_AUDIO_INTERVAL_MS = 900L;
    /**
     * Varios {@code force=true} en arranque (onResume + postInit + recovery) mandaban 3×
     * {@code ACTION_CHANGE_SOURCE} en &lt;1s y el SourceMgr NWD puede quedar sin audio.
     */
    /** > ~1,7s entre el primer arranque y enforceAudioRecovery; si es menor, se repite InitFM() y hay “salto” de audio. */
    private static final long MIN_FORCE_SOURCE_BROADCAST_MS = 2400L;

    /** Mismo criterio: evitar segundo {@code ACTION_APP_IN_OUT} (extra_app_id=8) en ráfaga. */
    private long mLastNwdAppEnterBroadcastElapsedMs = -1L;
    private static final long MIN_NWD_APP_ENTER_INTERVAL_MS = 2400L;

    /**
     * QS6 (NWD): el reproductor de música suena porque pide AudioFocus; la FM a veces queda muda
     * si solo enviamos broadcasts. Pedimos el mismo tipo de foco ({@code USAGE_MEDIA}) que
     * {@code com.nwd.setting.service} en los logs del sistema para que el HAL enrute el DSP.
     */
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener;
    private AudioFocusRequest mAudioFocusRequest;
    private boolean mIsAudioFocusHeld = false;
    /** Si cambia la forma del {@link AudioFocusRequest}, incrementar para recrear el builder. */
    private static final int AUDIO_FOCUS_REQUEST_BUILD = 2;
    private int mAudioFocusRequestBuild = 0;

    /**
     * True mientras queremos FM en primer plano (no hemos llamado a {@link #requestStopAudio}).
     * Tras conectar NWD, el sistema a veces envía {@link AudioManager#AUDIOFOCUS_LOSS} al instante;
     * sin re-pedir foco la salida queda muda pese a RDS/tuner OK.
     */
    private volatile boolean mWantsFmAudioRoute = false;
    private static final long FOCUS_RECLAIM_AFTER_LOSS_MS = 320L;
    private static final long FOCUS_LOSS_RECLAIM_GUARD_MS = 1400L;
    private volatile long mIgnoreFocusLossReclaimUntilElapsedMs = 0L;
    private final Runnable mReclaimAudioFocusRunnable = () -> {
        if (!mWantsFmAudioRoute || mContext == null) return;
        Log.i(FOCUS_TAG, "Reclaim: nuevo requestAudioFocus tras AUDIOFOCUS_LOSS (QS6/NWD)");
        requestAndroidAudioFocusForFmOnMainThread();
    };

    private boolean isInsideStartupScanGuardWindow() {
        if (mInitElapsedMs <= 0L) return false;
        long delta = SystemClock.elapsedRealtime() - mInitElapsedMs;
        return delta >= 0 && delta <= STARTUP_SCAN_GUARD_WINDOW_MS;
    }

    private void stopUnexpectedStartupScan(String reason) {
        if (mStartupAutoScanBlocked || !isInsideStartupScanGuardWindow()) return;
        mStartupAutoScanBlocked = true;
        Log.w(TAG, "QS6: auto-scan inesperado al iniciar; forzando stopScan (" + reason + ")");
        mMainHandler.post(() -> {
            stopScan();
            // Algunos firmwares, tras cortar el scan, saltan al preset nativo #1 (87.6).
            // Reafirmamos la última sintonía solicitada/estable por la app.
            final int targetFreq = (mRequestedFreqKhz > 0) ? mRequestedFreqKhz : mLastStableFreqKhz;
            final int targetBand = (mRequestedFreqKhz > 0) ? mRequestedBand : mLastStableBand;
            if (targetFreq > 0) {
                mMainHandler.postDelayed(() -> {
                    try {
                        if (mNwdService == null) return;
                        Log.w(TAG, "QS6: post-stopScan reassert -> " + targetFreq + "/B" + targetBand);
                        tuneWithBand(targetFreq, targetBand);
                    } catch (Exception e) {
                        Log.w(TAG, "QS6: post-stopScan reassert failed", e);
                    }
                }, 220L);
            }
        });
    }

    // V21.0: Shadow Motor Components
    private android.content.BroadcastReceiver mShadowReceiver;
    private android.database.ContentObserver mSettingsObserver;

    /**
     * NWD (Bengal/QS) a veces envía extras como {@link Byte} en lugar de {@link Integer}.
     * {@link Intent#getIntExtra(String, int)} lanza {@link ClassCastException} en ese caso.
     */
    private static int getNumericExtraAsInt(Intent intent, String key, int defaultValue) {
        if (intent == null || !intent.hasExtra(key)) return defaultValue;
        android.os.Bundle extras = intent.getExtras();
        if (extras == null) return defaultValue;
        Object val = extras.get(key);
        if (val == null) return defaultValue;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Byte) return ((Byte) val).intValue();
        if (val instanceof Short) return ((Short) val).intValue();
        if (val instanceof Long) return ((Long) val).intValue();
        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * NWD guarda {@link #SETTING_NWD_PS} como cadena hexadecimal (8 bytes RDS → 16 chars, a veces 32).
     * Si se muestra tal cual, la UI enseña "434f5045..." o "0000000000000000".
     */
    private static boolean looksLikeNwdHexPsBlob(String s) {
        if (s == null) return false;
        int len = s.length();
        if (len != 8 && len != 16 && len != 32) return false;
        if ((len & 1) != 0) return false;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ((c < '0' || c > '9') && (c < 'a' || c > 'f') && (c < 'A' || c > 'F')) {
                return false;
            }
        }
        // 8 chars solo 0-9 son casi siempre basura (p. ej. "87600000"), no PS hex real.
        if (len == 8) {
            boolean hasAlphaHex = false;
            for (int i = 0; i < len; i++) {
                char c = s.charAt(i);
                if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                    hasAlphaHex = true;
                    break;
                }
            }
            if (!hasAlphaHex) return false;
        }
        return true;
    }

    /**
     * El firmware a veces mete la frecuencia en el campo PS (solo dígitos).
     */
    private static boolean looksLikeNumericFrequencyMasqueradingAsPs(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.isEmpty()) return false;
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) return false;
        }
        try {
            int v = Integer.parseInt(t);
            int len = t.length();
            if (len >= 5 && len <= 6 && v >= 87500 && v <= 108000) return true;
            if (len == 5 && v >= 76000 && v < 87500) return true;
            if (len == 4 && v >= 8750 && v <= 10800) return true;
            if (len >= 3 && len <= 4 && v >= 100 && v <= 1999) return true;
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    /**
     * {@link #SETTING_NWD_FREQ}: FM suele ir en décimas de MHz (8760); a veces en kHz (87600);
     * MW puede aparecer como 531 o 5310 (×10 erróneo).
     */
    private static int nwdSystemSettingFreqToKhz(int freqRaw) {
        if (freqRaw <= 0) return -1;
        if (freqRaw >= 8750 && freqRaw <= 10800) {
            return freqRaw * 10;
        }
        if (freqRaw >= 87500 && freqRaw <= 108000) {
            return freqRaw;
        }
        if (freqRaw >= 76000 && freqRaw < 87500) {
            return freqRaw;
        }
        if (freqRaw >= 100 && freqRaw <= 1999) {
            return freqRaw;
        }
        if (freqRaw >= 2000 && freqRaw <= 30000 && freqRaw % 10 == 0) {
            int d = freqRaw / 10;
            if (d >= 100 && d <= 2000) {
                return d;
            }
        }
        return freqRaw;
    }

    /**
     * Mapea banda NWD a índice UI (0=FM1, 1=FM2, 2=FM3, 3+=AM/SW).
     * En aire FM la frecuencia suele seguir en 65–120 MHz al cambiar FM1↔FM2↔FM3: hay que
     * respetar {@code bandFromNwd} 0..2; forzar todo a FM1 rompía el indicador hasta saltar a AM.
     * Valores raros del OEM en FM (p. ej. 4) se tratan como FM1.
     */
    private static int coerceQs6BandForDisplay(int freqKhz, int bandFromNwd) {
        if (freqKhz >= 65000 && freqKhz <= 120000) {
            if (bandFromNwd >= 0 && bandFromNwd <= 2) {
                return bandFromNwd;
            }
            return 0;
        }
        if (freqKhz >= 100 && freqKhz <= 2500) {
            return bandFromNwd >= 3 ? bandFromNwd : 3;
        }
        return bandFromNwd;
    }

    private static String decodeHexPsToAscii(String hex) {
        StringBuilder ascii = new StringBuilder(hex.length() / 2);
        for (int i = 0; i < hex.length(); i += 2) {
            int b = Integer.parseInt(hex.substring(i, i + 2), 16);
            if (b >= 32 && b < 127) {
                ascii.append((char) b);
            } else {
                ascii.append(' ');
            }
        }
        return ascii.toString();
    }

    /**
     * Convierte PS crudo (hex NWD o texto AIDL) a texto legible para la UI.
     */
    private static String normalizeNwdPsDisplay(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        if (looksLikeNwdHexPsBlob(s)) {
            s = decodeHexPsToAscii(s);
        }
        s = s.replace('\t', ' ').trim();
        s = s.replaceAll("\\s+", " ").trim();
        if (s.isEmpty()) return null;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 32 && c < 127) {
                sb.append(c);
            }
        }
        s = sb.toString().trim();
        if (s.isEmpty()) return null;
        if (looksLikeNumericFrequencyMasqueradingAsPs(s)) {
            return null;
        }
        // Buffer RDS vacío / hex decodificado a espacios pero a veces llega como "000…" texto (18+ ceros).
        if (looksLikeZeroPaddingPs(s)) {
            return null;
        }
        return s;
    }

    /** PS que es solo ceros ASCII (OEM NWD sin señal RDS útil). */
    private static boolean looksLikeZeroPaddingPs(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.length() < 4) return false;
        for (int i = 0; i < t.length(); i++) {
            if (t.charAt(i) != '0') return false;
        }
        return true;
    }

    /** Nombres PTY RDS (tabla EU, 0–31). */
    private static final String[] RDS_PTY_EU = {
            "—", "Noticias", "Actualidad", "Información", "Deportes", "Educación", "Drama",
            "Cultura", "Ciencia", "Variedades", "Pop", "Rock", "Fácil", "Clásica lig.",
            "Clásica", "Otra música", "Tiempo", "Economía", "Infantil", "Social",
            "Religión", "Teléfono", "Viajes", "Ocio", "Jazz", "Country", "Música nacional",
            "Oldies", "Folk", "Documental", "Test alarma", "Alarma"
    };

    private static String rdsPtyEuString(byte ptyType) {
        int i = ptyType & 0xff;
        if (i >= 0 && i < RDS_PTY_EU.length) {
            return RDS_PTY_EU[i];
        }
        return "PTY " + i;
    }

    private boolean effectiveStereoForUi() {
        return mStereoDecoderOn && mIsStereo;
    }

    private void postStereoUiIfNeeded() {
        mMainHandler.post(() -> {
            if (mCallback != null) {
                mCallback.onStereoChanged(effectiveStereoForUi());
            }
        });
    }

    /**
     * Actualiza solo el indicador TP desde el bit 0x8. En {@code ArmRadioManager} {@code getRDSState(8)}
     * suele devolver false; entonces se usa {@link #SETTING_NWD_RDS_MASK} (misma máscara que escribe el MCU).
     */
    private void refreshTpTrafficProgramFromNwd() {
        boolean tp = false;
        if (mNwdService != null) {
            try {
                tp = mNwdService.getRDSState(NWD_RDS_BIT_TP);
            } catch (RemoteException e) {
                Log.w(TAG, "refreshTpTrafficProgram: getRDSState(TP)", e);
            }
        }
        if (!tp) {
            tp = readTpBitFromSystemRdsMask();
        }
        mIsTpEnabled = tp;
    }

    private boolean readTpBitFromSystemRdsMask() {
        if (mContext == null) return false;
        try {
            int mask = android.provider.Settings.System.getInt(
                    mContext.getContentResolver(), SETTING_NWD_RDS_MASK, 0);
            return (mask & NWD_RDS_BIT_TP) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** AF/TA según máscara AIDL + TP real (bit 0x8 / Settings). Llamar dentro de {@code performAidlCall} o con servicio ya listo. */
    private void refreshAfTaTpFromNwdService() {
        if (mNwdService == null) return;
        try {
            mIsAfEnabled = mNwdService.getRDSState(NWD_RDS_BIT_AF);
            mIsTaEnabled = mNwdService.getRDSState(NWD_RDS_BIT_TA);
        } catch (RemoteException e) {
            Log.w(TAG, "refreshAfTaTpFromNwdService", e);
        }
        refreshTpTrafficProgramFromNwd();
    }

    // Implementación de la Callback del IPC hacia NWD
    private final RadioCallback.Stub mNwdCallback = new RadioCallback.Stub() {
        @Override
        public void notifyCurrentFrequency(byte bandType, int frequency, String psName, int prefabIndex) {
            // V19.1: Refinamiento de frecuencia según banda (QS6). 
            // - FM (band < 3): frequency viene en décimas de MHz (ej: 9690 -> 96900 kHz)
            // - AM/SW (band >= 3): frequency suele venir en kHz directos (ej: 1080 -> 1080 kHz)
            int freqKhz;
            if (bandType < 3) {
                freqKhz = frequency * 10;
            } else {
                freqKhz = frequency;
            }

            mCurrentFreq = freqKhz;
            int bandDisp = coerceQs6BandForDisplay(freqKhz, (int) bandType);
            mCurrentBand = bandDisp;
            mLastReportedFreq = freqKhz;
            final String psDisp = normalizeNwdPsDisplay(psName);
            if (psDisp != null) {
                mLastPs = psDisp;
                mLastReportedPs = psDisp;
            }

            final int bandForUi = bandDisp;
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onBandChanged(bandForUi);
                    mCallback.onFrequencyChanged(freqKhz);
                    if (psDisp != null) {
                        mCallback.onRdsName(psDisp);
                    }
                }
            });

            Log.d(TAG, "SRC=AIDL notifyCurrentFrequency -> FreqRaw: " + frequency + ", FreqKhz: " + freqKhz
                    + ", Band: " + bandType + "→" + bandDisp + ", PS: " + psName
                    + (psDisp != null ? (" → disp: " + psDisp) : ""));
        }

        @Override
        public void notifyCurrentIsTA(boolean isTA) {
            // isTA = anuncio de tráfico en antena (TA on-air), no el interruptor TA ni el TP.
            refreshTpTrafficProgramFromNwd();
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRdsStatus(mIsAfEnabled, isTA, mIsTpEnabled);
                }
            });
        }

        @Override
        public void notifyCurrentPTYType(byte ptyType) {
            final String label = rdsPtyEuString(ptyType);
            Log.d(TAG, "NWD AIDL notifyCurrentPTYType -> " + (ptyType & 0xff) + " " + label);
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRdsPty(label);
                }
            });
        }

        @Override
        public void notifyNearOn(boolean isOn) {
            // AIDL: isOn=true → LOC; isOn=false → DX (ver INTELIGENCIA_QS_NWD §7.1; no confundir con
            // el entero "near state" en logcat MCU: 0=LOC, 1=DX).
            mIsDxLocal = isOn;
            Log.d(TAG, "NWD AIDL notifyNearOn -> local/near=" + isOn);
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onDxLocalChanged(isOn);
                }
            });
        }

        @Override
        public void notifyPrefabFrequency(Frequency[] frequencys) {
            int n = frequencys == null ? 0 : frequencys.length;
            Log.d(TAG, "NWD AIDL notifyPrefabFrequency -> count=" + n);
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRawEvent(200, "prefab_freq_count=" + n);
                }
            });
        }

        @Override
        public void notifyPrefabPTYType(byte ptyType) {
            final String label = rdsPtyEuString(ptyType);
            Log.d(TAG, "NWD AIDL notifyPrefabPTYType -> " + label);
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRdsPty(label);
                }
            });
        }

        @Override
        public void notifyRDSStateChange() {
            // Tras cambio RDS (incl. bit TP en máscara MCU): AF/TA = bits 1 y 2; TP = bit 0x8 o Settings.
            performAidlCall("notifyRDSStateChange", () -> {
                refreshAfTaTpFromNwdService();
                mMainHandler.post(() -> {
                    if (mCallback != null) {
                        mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                    }
                });
            });
        }

        @Override
        public void notifyRadioPoint(RadioPoint[] radioPoints) {
            int n = radioPoints == null ? 0 : radioPoints.length;
            Log.d(TAG, "NWD AIDL notifyRadioPoint -> count=" + n);
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRawEvent(201, "radio_point_count=" + n);
                }
            });
        }

        @Override
        public void notifyRadioScanState(int state) {
            Log.d(TAG, "NWD AIDL ScanState -> " + state);
            mIsScanning = (state != 0);
            if (mIsScanning) {
                stopUnexpectedStartupScan("notifyRadioScanState=" + state);
            }
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onScanStatusChanged(mIsScanning);
                }
            });
        }

        @Override
        public void notifyRdsShowState(boolean isShow) {
            Log.d(TAG, "NWD AIDL notifyRdsShowState -> " + isShow);
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRawEvent(202, "rds_show=" + isShow);
                }
            });
        }

        @Override
        public void notifyRtMessage(String rtMessage) {
            final String rtClean = MetadataUtils.cleanRdsText(rtMessage);
            Log.d(TAG, "NWD AIDL notifyRtMessage -> " + rtMessage);
            mMainHandler.post(() -> {
                if (mCallback != null && rtClean != null && !rtClean.isEmpty()) {
                    mCallback.onRdsText(rtClean);
                }
            });
        }

        @Override
        public void notifyState(byte state) {
            Log.d(TAG, "NWD AIDL notifyState -> " + state);
            // En algunas ROM NWD, state=3 tras init equivale a modo búsqueda/scan espontáneo.
            if ((state & 0xff) == 3) {
                stopUnexpectedStartupScan("notifyState=3");
            }
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRawEvent(state & 0xff, "nwd_radio_state");
                }
            });
        }

        @Override
        public void notifyStereo(boolean isStereo) {
            mIsStereo = isStereo;
            postStereoUiIfNeeded();
            Log.d(TAG, "NWD AIDL notifyStereo (piloto)=" + isStereo + " decoder=" + mStereoDecoderOn
                    + " ui=" + effectiveStereoForUi());
        }

        @Override
        public void notifyStereoOn(boolean isOn) {
            mStereoDecoderOn = isOn;
            postStereoUiIfNeeded();
            Log.d(TAG, "NWD AIDL notifyStereoOn (decodificador)=" + isOn);
        }
    };

    // V21.0: Componentes del Shadow Motor
    private void setupShadowMotor() {
        if (mContext == null) return;
        
        try {
            // V21.0: Limpieza preventiva si ya estaban registrados (evita duplicidad)
            cleanupShadowMotor();

            // 1. Broadcast Receiver para eventos crudos de NWD
            mShadowReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_SEND_RADIO_FREQUENCE_NEW.equals(action)) {
                        int freqRaw = getNumericExtraAsInt(intent, "extra_frequence", -1);
                        int band = getNumericExtraAsInt(intent, "extra_band", -1);
                        String psRaw = intent.getStringExtra("extra_ps_name");
                        String psNorm = normalizeNwdPsDisplay(psRaw);

                        if (freqRaw != -1) {
                            // Escalar frecuencia (NWD suele mandar décimas de MHz en FM)
                            int freqKhz = (band >= 0 && band < 3) ? freqRaw * 10 : freqRaw;
                            int bandDisp = (band >= 0)
                                    ? coerceQs6BandForDisplay(freqKhz, band)
                                    : coerceQs6BandForDisplay(freqKhz, mCurrentBand);

                            // Solo notificar si hay cambio real para evitar bucles con AIDL
                            boolean psDiffers = psNorm != null && !psNorm.equals(mLastPs);
                            boolean bandDiffers = bandDisp != mCurrentBand;
                            if (freqKhz != mCurrentFreq || psDiffers || bandDiffers) {
                                Log.d(TAG, "Shadow Motor (Broadcast) -> Freq: " + freqKhz + ", bandRaw=" + band
                                        + " → disp=" + bandDisp + ", PS: " + psNorm);
                                updateLocalState(freqKhz, psRaw, band >= 0 ? bandDisp : null, "SHADOW_BROADCAST");
                            }
                        }
                    } else if (ACTION_SEND_RADIO_RDS_RT.equals(action)) {
                        String rt = intent.getStringExtra("extra_rds_rt");
                        String rtClean = MetadataUtils.cleanRdsText(rt);
                        if (rtClean != null && !rtClean.isEmpty() && mCallback != null) {
                            mMainHandler.post(() -> mCallback.onRdsText(rtClean));
                        }
                    }
                }
            };

            android.content.IntentFilter filter = new android.content.IntentFilter();
            filter.addAction(ACTION_SEND_RADIO_FREQUENCE_NEW);
            filter.addAction(ACTION_SEND_RADIO_RDS_RT);
            
            // CRÍTICO V21.1: Android 13+ (API 33+) requiere flags de exportación.
            // Al escuchar broadcasts de NWD (otra app), DEBE ser RECEIVER_EXPORTED.
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                mContext.registerReceiver(mShadowReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                mContext.registerReceiver(mShadowReceiver, filter);
            }

            // 2. ContentObserver para Settings.System
            mSettingsObserver = new android.database.ContentObserver(mMainHandler) {
                @Override
                public void onChange(boolean selfChange, android.net.Uri uri) {
                    if (mContext == null) return;
                    try {
                        if (uri != null && uri.equals(android.provider.Settings.System.getUriFor(SETTING_NWD_RDS_MASK))) {
                            boolean prevTp = mIsTpEnabled;
                            refreshTpTrafficProgramFromNwd();
                            if (prevTp != mIsTpEnabled && mCallback != null) {
                                mMainHandler.post(() -> {
                                    if (mCallback != null) {
                                        mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                                    }
                                });
                            }
                            return;
                        }
                        android.content.ContentResolver cr = mContext.getContentResolver();
                        if (uri != null && uri.equals(android.provider.Settings.System.getUriFor(SETTING_NWD_BAND))) {
                            applyBandFromSettingsSystem(cr);
                            return;
                        }
                        int freqRaw = android.provider.Settings.System.getInt(cr, SETTING_NWD_FREQ, -1);
                        String psRaw = android.provider.Settings.System.getString(cr, SETTING_NWD_PS);
                        String psNorm = normalizeNwdPsDisplay(psRaw);

                        if (freqRaw != -1) {
                            int freqKhz = nwdSystemSettingFreqToKhz(freqRaw);
                            if (freqKhz <= 0) {
                                return;
                            }

                            Integer bandUi = readBandDisplayFromSettings(cr, freqKhz);
                            boolean freqChanged = freqKhz != mCurrentFreq;
                            boolean psChanged = psNorm != null && !psNorm.equals(mLastReportedPs);
                            boolean bandChanged = bandUi != null && bandUi != mCurrentBand;
                            if (freqChanged || psChanged || bandChanged) {
                                Log.d(TAG, "Shadow Motor (Settings) -> raw=" + freqRaw + " → kHz=" + freqKhz
                                        + ", bandUi=" + bandUi + ", PS: " + psNorm);
                                updateLocalState(freqKhz, psRaw, bandUi, "SHADOW_SETTINGS");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error en Shadow SettingsObserver", e);
                    }
                }
            };

            android.content.ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(
                android.provider.Settings.System.getUriFor(SETTING_NWD_FREQ),
                false,
                mSettingsObserver
            );
            cr.registerContentObserver(
                android.provider.Settings.System.getUriFor(SETTING_NWD_PS),
                false,
                mSettingsObserver
            );
            cr.registerContentObserver(
                android.provider.Settings.System.getUriFor(SETTING_NWD_RDS_MASK),
                false,
                mSettingsObserver
            );
            cr.registerContentObserver(
                android.provider.Settings.System.getUriFor(SETTING_NWD_BAND),
                false,
                mSettingsObserver
            );

            Log.i(TAG, "Shadow Motor iniciado (Broadcast + Settings)");
        } catch (Exception e) {
            Log.e(TAG, "Error FATAL al iniciar Shadow Motor", e);
            Log.e(TAG, "setupShadowMotor", e);
        }
    }

    private void cleanupShadowMotor() {
        if (mContext == null) return;
        if (mShadowReceiver != null) {
            try { mContext.unregisterReceiver(mShadowReceiver); } catch (Exception ignored) {}
            mShadowReceiver = null;
        }
        if (mSettingsObserver != null) {
            try { mContext.getContentResolver().unregisterContentObserver(mSettingsObserver); } catch (Exception ignored) {}
            mSettingsObserver = null;
        }
    }

    /** Banda UI desde {@link #SETTING_NWD_BAND} + frecuencia actual en kHz; null si la clave no existe. */
    private static Integer readBandDisplayFromSettings(android.content.ContentResolver cr, int freqKhz) {
        try {
            int bandRaw = android.provider.Settings.System.getInt(cr, SETTING_NWD_BAND, -1);
            if (bandRaw < 0) return null;
            return coerceQs6BandForDisplay(freqKhz, bandRaw);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Solo cambió {@link #SETTING_NWD_BAND} (p. ej. ciclo FM1→FM2 sin mover frecuencia en Settings todavía).
     */
    private void applyBandFromSettingsSystem(android.content.ContentResolver cr) {
        if (mContext == null || cr == null) return;
        try {
            int bandRaw = android.provider.Settings.System.getInt(cr, SETTING_NWD_BAND, -1);
            if (bandRaw < 0) return;
            int freqRaw = android.provider.Settings.System.getInt(cr, SETTING_NWD_FREQ, -1);
            int freqKhz = freqRaw > 0 ? nwdSystemSettingFreqToKhz(freqRaw) : mCurrentFreq;
            if (freqKhz <= 0) freqKhz = mCurrentFreq;
            int bandDisp = coerceQs6BandForDisplay(freqKhz, bandRaw);
            String psRaw = android.provider.Settings.System.getString(cr, SETTING_NWD_PS);
            updateLocalState(freqKhz, psRaw, bandDisp, "SETTINGS_BAND");
        } catch (Exception e) {
            Log.e(TAG, "applyBandFromSettingsSystem", e);
        }
    }

    /**
     * Shadow: frecuencia + PS; {@code bandForUi} si viene de broadcast/Settings (coercionada).
     */
    private void updateLocalState(int freqKhz, String psRaw, Integer bandForUi, String source) {
        long now = SystemClock.elapsedRealtime();
        boolean looksBootstrap = (freqKhz == 87600 || freqKhz == 87500);
        boolean startupWindow = isInsideStartupScanGuardWindow();
        int incomingBand = bandForUi != null ? bandForUi : mCurrentBand;
        if (!looksBootstrap && freqKhz > 0) {
            mLastStableFreqKhz = freqKhz;
            mLastStableBand = incomingBand;
            mLastStableSeenElapsedMs = now;
        }

        boolean canRestoreStable = startupWindow
                && looksBootstrap
                && mLastStableFreqKhz > 0
                && (now - mLastStableSeenElapsedMs) <= STABLE_FREQ_RESTORE_WINDOW_MS
                && mLastStableFreqKhz != freqKhz;
        if (canRestoreStable) {
            if (mRequestedTuneReasserts < 4) {
                mRequestedTuneReasserts++;
                final int targetFreq = mLastStableFreqKhz;
                final int targetBand = mLastStableBand;
                mMainHandler.postDelayed(() -> {
                    try {
                        if (mIsBound && mNwdService != null) {
                            Log.w(TAG, "SRC=ENGINE_GUARD startup stable-restore " + freqKhz + " -> "
                                    + targetFreq + "/B" + targetBand
                                    + " intento=" + mRequestedTuneReasserts);
                            tuneWithBand(targetFreq, targetBand);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "QS6: error en startup stable-restore", e);
                    }
                }, 140L);
            }
            return;
        }

        boolean protectedRequestedTune = mRequestedFreqKhz > 0
                && mRequestedTuneElapsedMs > 0
                && (now - mRequestedTuneElapsedMs) <= REQUESTED_TUNE_PROTECT_WINDOW_MS;
        if (protectedRequestedTune && looksBootstrap && freqKhz != mRequestedFreqKhz) {
            // El stack NWD nativo a veces reinyecta 87.6 tras haber sintonizado correctamente.
            // Ignoramos ese rebote y re-afirmamos la sintonía solicitada por la app.
            if (mRequestedTuneReasserts < 3) {
                mRequestedTuneReasserts++;
                final int targetFreq = mRequestedFreqKhz;
                final int targetBand = mRequestedBand;
                mMainHandler.postDelayed(() -> {
                    try {
                        if (mIsBound && mNwdService != null) {
                            Log.w(TAG, "SRC=ENGINE_GUARD bootstrap override detectado (" + freqKhz
                                    + "), re-afirmando " + targetFreq + "/B" + targetBand
                                    + " intento=" + mRequestedTuneReasserts);
                            tuneWithBand(targetFreq, targetBand);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "QS6: error re-afirmando sintonía solicitada", e);
                    }
                }, 180L);
            }
            return;
        }

        String ps = normalizeNwdPsDisplay(psRaw);
        boolean bandWillChange = bandForUi != null && bandForUi != mCurrentBand;
        if (freqKhz == mLastReportedFreq && (ps == null || ps.equals(mLastReportedPs)) && !bandWillChange) {
            return;
        }

        if (bandForUi != null) {
            mCurrentBand = bandForUi;
        }
        mCurrentFreq = freqKhz;
        if (ps != null) {
            mLastPs = ps;
        }

        mLastReportedFreq = freqKhz;
        if (freqKhz == mRequestedFreqKhz) {
            mRequestedTuneReasserts = 0;
        }
        if (ps != null) {
            mLastReportedPs = ps;
        }
        Log.d(TAG, "SRC=" + source + " updateLocalState -> freq=" + freqKhz
                + ", band=" + mCurrentBand + ", ps=" + ps);

        final boolean bandChanged = bandWillChange;
        final int bandOut = mCurrentBand;
        mMainHandler.post(() -> {
            if (mCallback != null) {
                if (bandChanged) {
                    mCallback.onBandChanged(bandOut);
                }
                mCallback.onFrequencyChanged(freqKhz);
                if (ps != null) {
                    mCallback.onRdsName(ps);
                }
            }
        });
    }

    // Conexión del Servicio Android al servicio NWD
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mNwdService = RadioFeature.Stub.asInterface(service);
            mIsBound = true;
            Log.d(TAG, "QS6 Service onServiceConnected -> Vinculado a NWD RadioService AIDL");

            // V20.0: Registrar receptor de muerte del proceso para detectar caídas del sistema
            try {
                service.linkToDeath(() -> {
                    Log.e(TAG, "!!! QS6 SERVICE DIED !!! El proceso com.nwd.radio.service ha muerto inesperadamente.");
                    // Mismo flujo que DeadObject: unbind + re-init (evita conexión zombie).
                    mMainHandler.post(() -> handleServiceDeath());
                }, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Error vinculando DeathRecipient en QS6", e);
            }

            // V17.6: El registro de callbacks y configuración de fondo se hace en un hilo separado
            // para evitar congelar la UI si el servicio remoto está bloqueado inicializando.
            new Thread(() -> {
                Log.d(TAG, "QS6 (Background): Registrando callbacks y modo de fondo...");
                performAidlCall("registCallback", () -> mNwdService.registCallback(mNwdCallback));
                performAidlCall("setRadioBackServiceOn", () -> mNwdService.setRadioBackServiceOn(true));
                // Algunas builds NWD usan INTRO() como paso de inicialización del stack (nombre heredado).
                performAidlCall("INTRO", () -> mNwdService.INTRO());
                performAidlCall("getRadioState", () -> {
                    byte st = mNwdService.getRadioState();
                    Log.i(TAG, "QS6 AIDL getRadioState=" + st + " (tras INTRO/setRadioBackServiceOn)");
                });
                // Sincronía estado UI con NWD (estudio Fase C/D: getCurrentScanState, isNearOn, RDS, estéreo).
                performAidlCall("syncNwdUiState", () -> {
                    mIsDxLocal = mNwdService.isNearOn();
                    mIsScanning = mNwdService.getCurrentScanState() != 0;
                    refreshAfTaTpFromNwdService();
                    mStereoDecoderOn = mNwdService.isStreroOn();
                });
                mMainHandler.post(() -> {
                    if (mCallback == null) return;
                    mCallback.onDxLocalChanged(mIsDxLocal);
                    mCallback.onScanStatusChanged(mIsScanning);
                    mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                    mCallback.onStereoChanged(effectiveStereoForUi());
                });
                Log.d(TAG, "QS6 (Background): Configuración inicial completada.");
                // Refuerzo: el MCU a veces solo enruta audio tras un segundo empujón (logs: set radio switch to true).
                // Solo AIDL: el cambio de fuente ya lo hace init/onResume/recovery con broadcast implícito.
                mMainHandler.postDelayed(() -> {
                    if (mNwdService == null) return;
                    performAidlCall("setRadioBackServiceOn(retry)", () -> mNwdService.setRadioBackServiceOn(true));
                    Log.d(TAG, "QS6: Handshake post-AIDL (solo setRadioBackServiceOn, 350ms)");
                }, 350);
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "QS6 Service onServiceDisconnected -> Desvinculado de NWD RadioService");
            mNwdService = null;
            mIsBound = false;
        }
    };

    /**
     * V20.0: Helper para ejecutar llamadas AIDL con detección de muerte de objeto.
     * Si detecta una DeadObjectException, intenta reiniciar el vínculo.
     */
    private interface AidlRunnable {
        void run() throws RemoteException;
    }

    private synchronized void performAidlCall(String callName, AidlRunnable action) {
        if (mNwdService == null) {
            Log.w(TAG, "QS6: Intento de llamada '" + callName + "' con Servicio NWD nulo.");
            return;
        }

        try {
            action.run();
            mRetryCount = 0; // Resetear contador si tiene éxito
        } catch (DeadObjectException e) {
            Log.e(TAG, "!!! QS6 DEAD OBJECT DETECTED en '" + callName + "' !!!");
            handleServiceDeath();
        } catch (RemoteException e) {
            Log.e(TAG, "Error remoto en '" + callName + "'", e);
        }
    }

    private void handleServiceDeath() {
        // Desvincular antes de volver a bindService; si no, Android puede dejar el ServiceConnection
        // colgando y provocar doble bind / DeadObject en bucle.
        if (mContext != null && mIsBound) {
            try {
                mContext.unbindService(mConnection);
            } catch (Exception ignored) {
            }
        }
        mNwdService = null;
        mIsBound = false;

        if (mRetryCount < MAX_RETRIES) {
            mRetryCount++;
            Log.w(TAG, "QS6: Re-vinculando servicio (Intento " + mRetryCount + "/" + MAX_RETRIES + ")...");
            if (mContext != null) {
                init(mContext);
            }
        } else {
            Log.e(TAG, "QS6: Se alcanzó el máximo de reintentos de reconexión. El hardware NWD no responde.");
        }
    }

    /**
     * Broadcasts de cambio de fuente / audio: deben ser IMPLÍCITOS (sin {@code setPackage}).
     * En QS6, {@code com.nwd.setting.service} (SourceMgr) arbitra el audio; si el intent solo
     * va a {@code com.nwd.radio.service}, el gestor de fuentes no conmuta y la FM puede quedar muda.
     */
    private void sendSourceSystemBroadcast(Intent intent) {
        if (mContext == null || intent == null) return;
        try {
            intent.setPackage(null);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "sendSourceSystemBroadcast falló", e);
        }
    }

    /**
     * Para intents que deben ir solo al APK de radio (p. ej. wake interno); si falla, reintenta implícito.
     */
    private void sendNwdBroadcast(Intent intent) {
        if (mContext == null || intent == null) return;
        try {
            intent.setPackage(NWD_RADIO_PACKAGE);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.w(TAG, "sendNwdBroadcast (con paquete) falló: " + e.getMessage());
            try {
                intent.setPackage(null);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(intent);
            } catch (Exception e2) {
                Log.e(TAG, "sendNwdBroadcast (fallback) falló", e2);
            }
        }
    }

    /** Inicializa AudioManager y el listener (una vez). */
    private void setupAudioFocus() {
        if (mContext == null) return;
        if (mAudioManager != null) return;
        Context app = mContext.getApplicationContext();
        mAudioManager = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusListener = focusChange -> {
            Log.d(TAG, "QS6 AudioFocus change: " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    mIsAudioFocusHeld = true;
                    if (mWantsFmAudioRoute) {
                        applyQs6HalAudioHints("after-audiofocus-gain");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mIsAudioFocusHeld = false;
                    if (SystemClock.elapsedRealtime() < mIgnoreFocusLossReclaimUntilElapsedMs) {
                        Log.d(TAG, "QS6: AUDIOFOCUS_LOSS ignorado temporalmente (guard anti ping-pong)");
                        break;
                    }
                    // Un solo reclaim diferido: el inmediato + el de 320ms generaban ráfagas de
                    // requestAudioFocus sin mejorar el audio en ROMs NWD (LOSS viene del SourceMgr).
                    scheduleAudioFocusReclaimAfterLoss("AUDIOFOCUS_LOSS");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
                default:
                    break;
            }
        };
    }

    /**
     * Pistas al Audio HAL (mismo estilo que MT8163/K706). En algunos SoC Qualcomm/NWD el DSP FM
     * escucha {@code fm_radio_on} aunque el servicio NWD falle abriendo {@code /dev/audio-dsp}.
     */
    private void applyQs6HalAudioHints(String reason) {
        setupAudioFocus();
        if (mAudioManager == null) return;
        try {
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            Log.d(TAG, "QS6: setMode(MODE_NORMAL) (" + reason + ")");
        } catch (Exception e) {
            Log.w(TAG, "QS6: setMode(MODE_NORMAL) falló", e);
        }
        String[] paramsList = new String[] {
                "fm_radio_on=1;fm_mute=0",
                "fm_radio_on=1",
        };
        for (String p : paramsList) {
            try {
                mAudioManager.setParameters(p);
                Log.i(FOCUS_TAG, "setParameters (" + reason + "): " + p);
            } catch (Exception e) {
                Log.w(TAG, "setParameters falló (" + reason + "): " + p, e);
            }
        }
    }

    private void scheduleAudioFocusReclaimAfterLoss(String reason) {
        if (!mWantsFmAudioRoute) return;
        mMainHandler.removeCallbacks(mReclaimAudioFocusRunnable);
        mMainHandler.postDelayed(mReclaimAudioFocusRunnable, FOCUS_RECLAIM_AFTER_LOSS_MS);
        Log.d(TAG, "QS6: programado reclaim AudioFocus en " + FOCUS_RECLAIM_AFTER_LOSS_MS + "ms (" + reason + ")");
    }

    private void cancelAudioFocusReclaim() {
        mMainHandler.removeCallbacks(mReclaimAudioFocusRunnable);
    }

    /**
     * Alineado con {@code KernelUtils.appStart(Context, appId)} y SprdRadioManager$1 / AWRadioManager$1:
     * hace falta {@code extra_app_id=8} para que el servicio NWD llame {@code InitFM()} y enrute audio.
     */
    private void notifyNwdThirdPartyRadioAppInOut(boolean appEntered) {
        if (mContext == null) return;
        try {
            if (appEntered) {
                long now = SystemClock.elapsedRealtime();
                if (mLastNwdAppEnterBroadcastElapsedMs >= 0
                        && (now - mLastNwdAppEnterBroadcastElapsedMs) < MIN_NWD_APP_ENTER_INTERVAL_MS) {
                    Log.d(TAG, "QS6: APP_IN_OUT enter omitido (" + MIN_NWD_APP_ENTER_INTERVAL_MS
                            + "ms) — evita doble InitFM / salto de audio");
                    return;
                }
                mLastNwdAppEnterBroadcastElapsedMs = now;
                Intent i = new Intent("com.nwd.action.ACTION_APP_IN_OUT");
                i.putExtra("extra_app_id", NWD_APPLICATION_ID_RADIO);
                i.putExtra("extra_app_operation", 1);
                i.putExtra("extra_app_event", 0);
                i.putExtra("extra_app_in_out", 1);
                i.putExtra("extra_app_reset", 0);
                sendSourceSystemBroadcast(i);
                Log.i(TAG, "QS6: ACTION_APP_IN_OUT extra_app_id=8 op=1 (InitFM en Sprd/AW — ver QS NWD/tools ingeniería inversa)");
            } else {
                mLastNwdAppEnterBroadcastElapsedMs = -1L;
                Intent exit = new Intent(ACTION_EXIT_ARM_FM_RADIO);
                sendSourceSystemBroadcast(exit);
                Log.d(TAG, "QS6: " + ACTION_EXIT_ARM_FM_RADIO + " (ExitFm en stack ARM)");
            }
        } catch (Exception e) {
            Log.w(TAG, "notifyNwdThirdPartyRadioAppInOut falló", e);
        }
    }

    /**
     * Pide foco de audio como app de medios (solo desde el hilo principal).
     * En ROMs NWD, llamar {@code requestAudioFocus} desde un hilo de fondo puede no registrar la petición
     * en AudioService (no verás {@code com.example.openradiofm} en MediaFocusControl).
     */
    private void requestAndroidAudioFocusForFmOnMainThread() {
        if (mContext == null) return;
        setupAudioFocus();
        if (mAudioManager == null) return;
        String pkg = mContext.getApplicationContext().getPackageName();
        Log.i(FOCUS_TAG, "QS6Engine.requestAudioFocus pkg=" + pkg + " (debe coincidir con MediaFocusControl)");
        try {
            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (mAudioFocusRequest == null || mAudioFocusRequestBuild != AUDIO_FOCUS_REQUEST_BUILD) {
                    AudioAttributes aa = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build();
                    // Igual que MemorySettingService en log: flags=0x0 (sin DELAY_OK).
                    mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(aa)
                            .setWillPauseWhenDucked(false)
                            .setOnAudioFocusChangeListener(mAudioFocusListener, mMainHandler)
                            .build();
                    mAudioFocusRequestBuild = AUDIO_FOCUS_REQUEST_BUILD;
                }
                result = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            } else {
                result = mAudioManager.requestAudioFocus(mAudioFocusListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mIsAudioFocusHeld = true;
                Log.i(FOCUS_TAG, "GRANTED result=" + result + " pkg=" + pkg);
                Log.d(TAG, "QS6: AudioFocus GRANTED para FM (USAGE_MEDIA)");
                applyQs6HalAudioHints("after-audiofocus-granted");
            } else if (result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                Log.i(FOCUS_TAG, "DELAYED result=" + result + " pkg=" + pkg);
                Log.d(TAG, "QS6: AudioFocus DELAYED — esperando concesión del sistema");
            } else {
                Log.w(FOCUS_TAG, "NO concedido result=" + result + " pkg=" + pkg);
                Log.w(TAG, "QS6: AudioFocus no concedido (result=" + result + ") — probar de nuevo al cambiar de pantalla");
            }
        } catch (Exception e) {
            Log.e(FOCUS_TAG, "Excepción en requestAudioFocus pkg=" + pkg, e);
            Log.e(TAG, "QS6: requestAudioFocus falló", e);
        }
    }

    private void abandonAndroidAudioFocusForFm() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            abandonAndroidAudioFocusForFmOnMainThread();
        } else {
            mMainHandler.post(this::abandonAndroidAudioFocusForFmOnMainThread);
        }
    }

    private void abandonAndroidAudioFocusForFmOnMainThread() {
        if (mAudioManager == null || mAudioFocusListener == null) return;
        String pkg = mContext != null ? mContext.getApplicationContext().getPackageName() : "?";
        Log.i(FOCUS_TAG, "abandonAudioFocus pkg=" + pkg);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mAudioFocusRequest != null) {
                mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
            } else {
                mAudioManager.abandonAudioFocus(mAudioFocusListener);
            }
            Log.d(TAG, "QS6: AudioFocus abandonado");
        } catch (Exception e) {
            Log.e(TAG, "QS6: abandonAudioFocus falló", e);
        } finally {
            mIsAudioFocusHeld = false;
        }
    }

    public QS6Engine() {
    }

    @Override
    public boolean init(Context context) {
        this.mContext = context;
        mInitElapsedMs = SystemClock.elapsedRealtime();
        mStartupAutoScanBlocked = false;
        setupAudioFocus();
        Log.d(TAG, "Iniciando motor QS6 (Plan Nivel 2 - NWD AIDL IPC)");

        // 1. Iniciamos el intent al servicio original
        Intent intent = new Intent("com.nwd.radio.service.ACTION_RADIO_SERVICE");
        intent.setPackage("com.nwd.radio.service");

        // Android 11+ requiere resolver el componente si se usa bindService a otro
        // paquete (independientemente del manifest parfois)
        android.content.pm.PackageManager pm = context.getPackageManager();
        java.util.List<android.content.pm.ResolveInfo> resolveInfo = pm.queryIntentServices(intent, 0);

        if (resolveInfo != null && !resolveInfo.isEmpty()) {
            android.content.pm.ResolveInfo serviceInfo = resolveInfo.get(0);
            String packageName = serviceInfo.serviceInfo.packageName;
            String className = serviceInfo.serviceInfo.name;
            ComponentName component = new ComponentName(packageName, className);

            Intent explicitIntent = new Intent(intent);
            explicitIntent.setComponent(component);
            boolean bindResult = context.bindService(explicitIntent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "QS6 Service Bind Request -> Success: " + bindResult + " | " + component.flattenToString());
        } else {
            Log.e(TAG,
                    "QS6 Error: No se ha podido resolver el intent mediante query. Usando component fall-back directo.");

            Intent fallback = new Intent();
            fallback.setComponent(new ComponentName("com.nwd.radio.service", "com.nwd.radio.service.RadioService"));
            boolean fallBind = context.bindService(fallback, mConnection,
                    Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
            Log.d(TAG, "QS6 Service Fallback Bind Request -> Success: " + fallBind);
        }

        // 2. Iniciar Shadow Motor (REDUNDANCIA V21.0)
        setupShadowMotor();

        // 3. Despertar hardware (sin ACTION_CHANGE_SOURCE aquí: onResume + recovery ya lo hacen;
        // un tercer disparo en init desincronizaba el SourceMgr en QS6).
        new Thread(() -> {
            try {
                requestWakeUp();
                Thread.sleep(500);
            } catch (Exception e) {}
        }).start();

        return true;
    }

    @Override
    public void release() {
        release(false); // Por defecto, liberación completa
    }

    @Override
    public void release(boolean isChangingConfigurations) {
        if (isChangingConfigurations) {
            Log.d(TAG, "QS6: Recreación por cambio de configuración detectada. MANTENIENDO vínculo AIDL vivo.");
            // En QS6, si mantenemos el vínculo (bind) y el callback registrado, 
            // la radio sigue sonando y el hardware no se entera de que la Activity "murió" un instante.
            return;
        }

        Log.d(TAG, "QS6: release() - Soltando recursos y desvinculando AIDL definitivamente");

        mWantsFmAudioRoute = false;
        cancelAudioFocusReclaim();
        abandonAndroidAudioFocusForFm();
        
        // V21.0: Limpieza forzada de Shadow Motor
        cleanupShadowMotor();

        if (mIsBound && mNwdService != null) {
            performAidlCall("unRegistCallback", () -> mNwdService.unRegistCallback(mNwdCallback));
            try {
                mContext.unbindService(mConnection);
            } catch (Exception e) {
                Log.e(TAG, "Error al desvincular servicio", e);
            }
            mIsBound = false;
            mNwdService = null;
        }
        // V18.4: Forzar limpieza de hilos o estados si fuera necesario
        com.example.openradiofm.ui.main.RadioServiceController.clearSharedLocalEngineIfSame(this);
    }

    @Override
    public void closeDevice() {
        Log.d(TAG, "QS6: Cierre total solicitado (Power Off) - Secuencia V18.4 Definitiva");
        
        // V18.4: Secuencia ultra-agresiva sincronizada
        try {
            // 1. Desvincular callback AIDL INMEDIATAMENTE
            if (mIsBound && mNwdService != null) {
                Log.d(TAG, "QS6 (V18.4): Desvinculando Callback AIDL preventivamente...");
                performAidlCall("unRegistCallback", () -> mNwdService.unRegistCallback(mNwdCallback));
            }

            // 2. Muteo REDUNDANTE
            setMute(true);
            mContext.sendBroadcast(new Intent("com.nwd.action.ACTION_MUTE")); 

            // 3. Detener servicio de audio en segundo plano (AIDL)
            if (mIsBound && mNwdService != null) {
                Log.d(TAG, "QS6 (V18.4): Deteniendo RadioBackService...");
                performAidlCall("setRadioBackServiceOn", () -> mNwdService.setRadioBackServiceOn(false));
            }

            // 4. Cambiar fuente a ANDROID (0) - Indica al MCU que deje de rutear el chip de radio
            // V18.4: Cambiado ACTION_CHANGE_SOURCE por ACTION_REQUEST_CHANGE_SOURCE según logs nativos
            requestStopAudio();
            
            // 5. Notificar salida de aplicación con reset de estado
            Intent inOutIntent = new Intent("com.nwd.action.ACTION_APP_IN_OUT");
            inOutIntent.setPackage("com.nwd.radio.service");
            inOutIntent.putExtra("extra_app_in_out", 0);
            inOutIntent.putExtra("extra_app_reset", 1); 
            mContext.sendBroadcast(inOutIntent);

            // 5.1 Opcional: Multitask Button State (visto en logs nativos)
            Intent multiTaskIntent = new Intent("com.nwd.action.ACTION_MUTILTASK_BUTTON_STATE_CHANGE");
            multiTaskIntent.setPackage("com.nwd.radio.service");
            mContext.sendBroadcast(multiTaskIntent);

            // 5.2 Opcional: Quitar icono de barra de estado
            Intent iconIntent = new Intent("com.nwd.android.ACTION_SET_STATUSBAR_ICON");
            iconIntent.setPackage("com.nwd.radio.service");
            iconIntent.putExtra("type", 0);
            iconIntent.putExtra("state", false);
            mContext.sendBroadcast(iconIntent);

            // 6. Retardo de seguridad EXTENDIDO (1000ms)
            // El MCU de Qualcomm NWD es asíncrono y lento procesando la matriz de audio.
            Log.d(TAG, "QS6 (V18.4): Esperando 1000ms para conmutación de hardware...");
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            
            // 7. Liberación final de recursos (unbind)
            release();

        } catch (Exception e) {
            Log.e(TAG, "Error crítico en secuencia de apagado V18.4", e);
            release();
        }
    }

    @Override
    public String getEngineName() {
        return "QS6-AIDL-Engine";
    }

    @Override
    public void tune(int freqKhz) {
        tuneWithBand(freqKhz, mCurrentBand);
    }

    /**
     * Sintoniza con banda explícita para evitar que el stack OEM fuerce FM1
     * cuando la restauración de arranque debería quedar en FM2/FM3/AM.
     */
    public void tuneWithBand(int freqKhz, int targetBand) {
        mRequestedFreqKhz = freqKhz;
        mRequestedBand = targetBand;
        mRequestedTuneElapsedMs = SystemClock.elapsedRealtime();
        mRequestedTuneReasserts = 0;

        // V19.1: Escalar sintonía según banda para QS6
        int nwdFreq;
        if (targetBand >= 3) { // AM1/AM2: NWD usa kHz directos
            nwdFreq = freqKhz;
        } else {
            nwdFreq = freqKhz / 10; // FM: NWD usa unidades de 10 kHz
        }

        final byte bandByte = (byte) targetBand;
        performAidlCall("tune", () -> {
            // La firma de NWD es setCurrentFrequency(frequency, bandType, prefebIndex)
            mNwdService.setCurrentFrequency(nwdFreq, bandByte, 0);
            Log.d(TAG, "QS6 AIDL TUNE: " + freqKhz + " (" + nwdFreq + ") Band=" + targetBand);
        });

        // V21.0: Redundancia por Intent si el servicio AIDL está bloqueado o ausente
        if (!mIsBound || mNwdService == null) {
            Log.d(TAG, "QS6 Shadow Motor: Enviando TUNE vía Intent (Redundancia)");
            Intent intent = new Intent(ACTION_SET_RADIO_FREQUENCE);
            intent.putExtra("extra_frequence", nwdFreq);
            intent.putExtra("extra_band", bandByte);
            mContext.sendBroadcast(intent);
        }

        // Mantener coherencia local inmediata hasta que lleguen callbacks AIDL/Broadcast.
        mCurrentBand = targetBand;
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
        performAidlCall("seekUp", () -> {
            mNwdService.search(true);
        });
        
        // V21.0: Redundancia
        if (!mIsBound || mNwdService == null) {
            mContext.sendBroadcast(new Intent(ACTION_SEARCH_UP));
        }
    }

    @Override
    public void seekDown() {
        performAidlCall("seekDown", () -> {
            mNwdService.search(false);
        });
        
        // V21.0: Redundancia
        if (!mIsBound || mNwdService == null) {
            mContext.sendBroadcast(new Intent(ACTION_SEARCH_DOWN));
        }
    }

    @Override
    public void stepUp() {
        // V19.1: Paso dinámico. 9 kHz para AM (zona EU), 100 kHz para FM.
        int step = (mCurrentBand < 3) ? 100 : 9;
        tune(mCurrentFreq + step);
    }

    @Override
    public void stepDown() {
        int step = (mCurrentBand < 3) ? 100 : 9;
        tune(mCurrentFreq - step);
    }

    @Override
    public void scan() {
        performAidlCall("scan", () -> {
            mNwdService.AMS();
        });
        
        // V21.0: Redundancia
        if (!mIsBound || mNwdService == null) {
            mContext.sendBroadcast(new Intent(ACTION_AMS));
        }
    }

    @Override
    public void stopScan() {
        performAidlCall("stopScan", () -> {
            // En NWD, AMS actúa como toggle de auto-scan. changeBand aquí podía
            // derivar al preset nativo #1 (87.6) en algunos arranques.
            mNwdService.AMS();
        });
        if ((!mIsBound || mNwdService == null) && mContext != null) {
            Intent intent = new Intent(ACTION_AMS);
            mContext.sendBroadcast(intent);
        }
    }

    @Override
    public void bandCycle() {
        if (!mIsBound || mNwdService == null) {
            // Fallback por Broadcast si AIDL no está listo
            Intent intent = new Intent(ACTION_KEY_VALUE);
            intent.putExtra("extra_key_value", KEY_FM);
            mContext.sendBroadcast(intent);
            return;
        }
        performAidlCall("bandCycle", () -> {
            mNwdService.changeBand();
            // El OEM a veces no envía notifyCurrentFrequency al instante; alinear UI con getCurrentFrequency().
            pushUiFromCurrentFrequencyAidl();
        });
    }

    /**
     * Lee {@link RadioFeature#getCurrentFrequency()} y actualiza banda/frecuencia/PS en la UI.
     * Útil tras {@code changeBand()} cuando el callback AIDL llega tarde o la coerción antigua ocultaba FM2/FM3.
     */
    private void pushUiFromCurrentFrequencyAidl() {
        if (mNwdService == null) return;
        try {
            Frequency f = mNwdService.getCurrentFrequency();
            if (f == null) return;
            byte bandType = f.getBandType();
            int raw = f.getFrequency();
            int freqKhz = bandType < 3 ? raw * 10 : raw;
            int bandDisp = coerceQs6BandForDisplay(freqKhz, bandType);
            final String psDisp = normalizeNwdPsDisplay(f.getPSName());
            mCurrentFreq = freqKhz;
            mCurrentBand = bandDisp;
            mLastReportedFreq = freqKhz;
            if (psDisp != null) {
                mLastPs = psDisp;
                mLastReportedPs = psDisp;
            }
            final int fk = freqKhz;
            final int bd = bandDisp;
            mMainHandler.post(() -> {
                if (mCallback == null) return;
                mCallback.onBandChanged(bd);
                mCallback.onFrequencyChanged(fk);
                if (psDisp != null) {
                    mCallback.onRdsName(psDisp);
                }
            });
        } catch (RemoteException e) {
            Log.w(TAG, "pushUiFromCurrentFrequencyAidl", e);
        }
    }

    @Override
    public boolean isStereo() {
        return effectiveStereoForUi();
    }

    @Override
    public void setStereo(boolean enable) {
        mStereoDecoderOn = enable;
        postStereoUiIfNeeded();
        performAidlCall("setStereo", () -> {
            mNwdService.setStreroOn(enable);
        });
    }

    @Override
    public void setMute(boolean mute) {
        try {
            Intent intent = new Intent("com.nwd.action.ACTION_SET_MUTE");
            intent.putExtra("mute", mute ? 1 : 0);
            mContext.sendBroadcast(intent);
            this.mIsMute = mute;
            if (mute) {
                // Mute real: cortar ruta FM (SOURCE_ANDROID) porque algunas ROM ignoran el mute lógico.
                requestStopAudio();
                applyQs6AndroidMuteHint(true);
            } else {
                // Unmute ligero: recuperar foco + hint HAL, sin recovery agresivo.
                requestPlayAudio();
                applyQs6AndroidMuteHint(false);
            }
        } catch (Exception e) {
            Log.w(TAG, "QS6 setMute falló", e);
        }
    }

    /**
     * Mute/desmute a nivel Audio HAL (Android) para QS6.
     * No cambia la fuente MCU; solo fuerza estado fm_mute para hacer el botón más consistente.
     */
    private void applyQs6AndroidMuteHint(boolean mute) {
        setupAudioFocus();
        if (mAudioManager == null) return;
        String p = mute ? "fm_radio_on=1;fm_mute=1" : "fm_radio_on=1;fm_mute=0";
        try {
            mAudioManager.setParameters(p);
            Log.i(FOCUS_TAG, "setParameters (mute-toggle): " + p);
        } catch (Exception e) {
            Log.w(TAG, "QS6 applyQs6AndroidMuteHint falló: " + p, e);
        }
    }

    @Override
    public void openEq(Context context) {
        if (context == null) return;
        try {
            android.content.pm.PackageManager pm = context.getPackageManager();
            // 1) Misma ruta que la radio OEM: GotoAudioSetting → com.nwd.audioset/.home_horizontalActivity
            Intent explicit = new Intent();
            explicit.setComponent(new ComponentName(NWD_AUDIO_SETTINGS_PACKAGE, NWD_AUDIO_SETTINGS_CLASS));
            explicit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(explicit);
                Log.d(TAG, "openEq: explicit " + NWD_AUDIO_SETTINGS_PACKAGE + "/" + NWD_AUDIO_SETTINGS_CLASS);
                return;
            } catch (Exception e) {
                Log.d(TAG, "openEq: explicit audioset falló, probando launcher…");
            }
            Intent launchAudio = pm.getLaunchIntentForPackage(NWD_AUDIO_SETTINGS_PACKAGE);
            if (launchAudio != null) {
                launchAudio.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchAudio);
                Log.d(TAG, "openEq: getLaunchIntentForPackage " + NWD_AUDIO_SETTINGS_PACKAGE);
                return;
            }
            // 2) Otros firmwares: paquete com.nwd.eq (o bridge NWD)
            Intent directEq = pm.getLaunchIntentForPackage(NWD_EQ_PACKAGE);
            if (directEq != null) {
                directEq.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(directEq);
                Log.d(TAG, "openEq: launch " + NWD_EQ_PACKAGE);
                return;
            }
            // 3) OEM NWD: despacho por paquete (probar audioset antes que eq)
            if (tryOpenEqNwdBridge(context, NWD_AUDIO_SETTINGS_PACKAGE)) return;
            if (tryOpenEqNwdBridge(context, NWD_EQ_PACKAGE)) return;
            // 4) Último recurso: panel de sonido de Android
            Intent sound = new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
            sound.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sound);
            Log.d(TAG, "openEq: fallback ACTION_SOUND_SETTINGS");
        } catch (Exception e) {
            Log.e(TAG, "openEq failed", e);
        }
    }

    /**
     * {@link #ACTION_START_NWD_ACTIVITY} + extra {@code pkg} (mismo patrón que despertar la radio OEM).
     *
     * @return true si {@link Context#startActivity(Intent)} tuvo éxito
     */
    private static boolean tryOpenEqNwdBridge(Context context, String pkg) {
        Intent bridge = new Intent(ACTION_START_NWD_ACTIVITY);
        bridge.putExtra("pkg", pkg);
        bridge.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        try {
            context.sendBroadcast(bridge);
            Log.d(TAG, "openEq: broadcast " + ACTION_START_NWD_ACTIVITY + " pkg=" + pkg);
        } catch (Exception e) {
            Log.w(TAG, "openEq: broadcast falló pkg=" + pkg, e);
        }
        Intent startAct = new Intent(ACTION_START_NWD_ACTIVITY);
        startAct.putExtra("pkg", pkg);
        startAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(startAct);
            Log.d(TAG, "openEq: startActivity bridge pkg=" + pkg);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean requestPlayAudio() {
        return requestPlayAudioInternal(false);
    }

    /**
     * Igual que {@link #requestPlayAudio()} pero ignora el throttle (arranque en frío, recuperación).
     */
    public boolean requestPlayAudioForced() {
        return requestPlayAudioInternal(true);
    }

    /**
     * Encadena foco + broadcast en el hilo principal cuando el llamador viene de un hilo de fondo
     * (init, enforceAudioRecovery); evita que MediaFocusControl nunca vea a {@code com.example.openradiofm}.
     */
    private boolean requestPlayAudioInternal(boolean force) {
        if (mContext == null) {
            return false;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return requestPlayAudioInternalOnMain(force);
        }
        mMainHandler.post(() -> requestPlayAudioInternalOnMain(force));
        return true;
    }

    private boolean requestPlayAudioInternalOnMain(boolean force) {
        mWantsFmAudioRoute = true;
        mIgnoreFocusLossReclaimUntilElapsedMs = SystemClock.elapsedRealtime() + FOCUS_LOSS_RECLAIM_GUARD_MS;
        cancelAudioFocusReclaim();
        // Siempre pedir AudioFocus al volver a FM aunque el broadcast esté en throttle.
        requestAndroidAudioFocusForFmOnMainThread();
        long now = SystemClock.elapsedRealtime();
        if (!force && mLastSourceRadioBroadcastElapsedMs >= 0
                && (now - mLastSourceRadioBroadcastElapsedMs) < MIN_PLAY_AUDIO_INTERVAL_MS) {
            Log.d(TAG, "QS6: requestPlayAudio omitido (throttle " + MIN_PLAY_AUDIO_INTERVAL_MS + "ms)");
            return true;
        }
        if (force && mLastSourceRadioBroadcastElapsedMs >= 0
                && (now - mLastSourceRadioBroadcastElapsedMs) < MIN_FORCE_SOURCE_BROADCAST_MS) {
            Log.d(TAG, "QS6: ACTION_CHANGE_SOURCE coalescido (" + MIN_FORCE_SOURCE_BROADCAST_MS
                    + "ms) — ya se pidió foco arriba");
            return true;
        }
        mLastSourceRadioBroadcastElapsedMs = now;
        try {
            Log.d(TAG, "Iniciando Audio NWD Radio -> ACTION_CHANGE_SOURCE a SOURCE_RADIO (broadcast implícito)");
            Intent intent = new Intent(ACTION_CHANGE_SOURCE);
            intent.putExtra("extra_source_id", SOURCE_RADIO);
            sendSourceSystemBroadcast(intent);
            notifyNwdThirdPartyRadioAppInOut(true);
            applyQs6HalAudioHints("after-change-source-radio");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void enforceAudioRecovery() {
        Log.d(TAG, "QS6: Forzando recuperación de audio (Recovery V20.0 - Robustness Focus)");
        // V20.0: Incrementar retardos para dar margen al Kernel de Qualcomm (asíncrono)
        new Thread(() -> {
            try {
                requestWakeUp();
                Thread.sleep(800); // Antes 300ms
                mMainHandler.post(() -> {
                    requestPlayAudioInternalOnMain(true);
                    mMainHandler.postDelayed(() -> {
                        tune(mCurrentFreq);
                        setMute(false);
                        applyQs6HalAudioHints("after-recovery-tune");
                        Log.d(TAG, "QS6: Audio Recovery completado con re-tune a " + mCurrentFreq);
                    }, 450);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error en secuencia de recuperación de audio QS6", e);
            }
        }).start();
    }

    @Override
    public void switchToAndroidAudio() {
        requestStopAudio(); // Cambia a SOURCE_ANDROID
    }

    /**
     * Segundo plano: menos intrusivo que {@link #switchToAndroidAudio()}.
     * Evita el ping-pong con el reproductor NWD (reclaim + requestAudioFocus) sin
     * mandar {@code SOURCE_ANDROID} ni salir del ARM FM.
     * Al volver, {@link #requestPlayAudio()} / {@link #switchToFmAudio()} restauran foco y ruta.
     */
    @Override
    public void releaseAudioFocusOnlyForBackground() {
        cancelAudioFocusReclaim();
        mWantsFmAudioRoute = false;
        abandonAndroidAudioFocusForFm();
        Log.d(TAG, "QS6: segundo plano — AudioFocus abandonado + reclaim cancelado (sin cambiar fuente MCU)");
    }

    @Override
    public void switchToFmAudio() {
        requestPlayAudio(); // Cambia a SOURCE_RADIO
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {
        if (active) {
            switchToAndroidAudio();
        } else {
            // OnlineStreamManager ya llama a switchToFmAudio(); esto refuerza el HAL al cerrar streaming.
            if (Looper.myLooper() == Looper.getMainLooper()) {
                applyQs6HalAudioHints("streaming-inactive");
            } else {
                mMainHandler.post(() -> applyQs6HalAudioHints("streaming-inactive"));
            }
        }
    }

    public void requestStopAudio() {
        mWantsFmAudioRoute = false;
        cancelAudioFocusReclaim();
        notifyNwdThirdPartyRadioAppInOut(false);
        try {
            Log.d(TAG, "QS6: Solicitando cambio de fuente -> SOURCE_ANDROID (broadcast implícito)");
            Intent intent = new Intent("com.nwd.action.ACTION_REQUEST_CHANGE_SOURCE");
            intent.putExtra("extra_source_id", SOURCE_ANDROID);
            sendSourceSystemBroadcast(intent);

            Intent intentDirect = new Intent(ACTION_CHANGE_SOURCE);
            intentDirect.putExtra("extra_source_id", SOURCE_ANDROID);
            sendSourceSystemBroadcast(intentDirect);
        } catch (Exception e) {
            Log.e(TAG, "Error enviando fuente stop", e);
        } finally {
            abandonAndroidAudioFocusForFm();
        }
    }

    @Override
    public void toggleRdsFeature(int type) {
        if (type == 99) {
            Log.d(TAG, "toggleRdsFeature(99): reservado UI — ignorado en QS6");
            return;
        }
        if (type != 0 && type != 1 && type != 2) {
            Log.w(TAG, "toggleRdsFeature: tipo no soportado en QS6: " + type);
            return;
        }
        performAidlCall("toggleRdsFeature", () -> {
            // Máscara NWD: AF=0x1, TA=0x2, RDS master=0x80 (RadioManager.setRDSState or-int).
            if (type == 1) {
                boolean cur = mNwdService.getRDSState(NWD_RDS_BIT_AF);
                mNwdService.setRDSState((byte) NWD_RDS_BIT_AF, !cur);
                Log.d(TAG, "Toggle RDS AF -> " + !cur);
            } else if (type == 2) {
                boolean cur = mNwdService.getRDSState(NWD_RDS_BIT_TA);
                mNwdService.setRDSState((byte) NWD_RDS_BIT_TA, !cur);
                Log.d(TAG, "Toggle RDS TA (interruptor 0x2) -> " + !cur);
            } else {
                boolean cur = mNwdService.getRDSState(NWD_RDS_BIT_RDS);
                mNwdService.setRDSState((byte) NWD_RDS_BIT_RDS, !cur);
                Log.d(TAG, "Toggle RDS global (0x80) -> " + !cur);
            }
            refreshAfTaTpFromNwdService();
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                }
            });
        });
    }

    @Override
    public boolean isAfEnabled() {
        return mIsAfEnabled;
    }

    @Override
    public boolean isTaEnabled() {
        return mIsTaEnabled;
    }

    @Override
    public boolean isTpEnabled() {
        return mIsTpEnabled;
    }

    @Override
    public boolean isScanning() {
        return mIsScanning;
    }

    /**
     * Conmuta LOC/DX vía {@link com.nwd.radio.service.RadioFeature#setNearOn(boolean)}.
     * OEM: {@code isNearOn true} = LOC, {@code false} = DX; coherente con {@link #isDxLocal()}.
     */
    @Override
    public void toggleDxLocal() {
        performAidlCall("toggleDxLocal", () -> {
            boolean isNear = mNwdService.isNearOn();
            mNwdService.setNearOn(!isNear);
            mIsDxLocal = !isNear;
            mMainHandler.post(() -> {
                if (mCallback != null) {
                    mCallback.onDxLocalChanged(mIsDxLocal);
                }
            });
        });
    }

    @Override
    public boolean isDxLocal() {
        return mIsDxLocal;
    }

    @Override
    public void gotoPreset(int index) {
        if (index < 0 || mContext == null) {
            return;
        }
        performAidlCall("gotoPreset", () -> {
            Frequency[] presets = mNwdService.getPrefabFrequency();
            if (presets == null || index >= presets.length) {
                Log.w(TAG, "gotoPreset: sin datos o índice fuera de rango: " + index
                        + (presets == null ? " (null)" : (" len=" + presets.length)));
                return;
            }
            Frequency f = presets[index];
            if (f == null) {
                Log.w(TAG, "gotoPreset: preset nulo en slot " + index);
                return;
            }
            byte band = f.getBandType();
            int raw = f.getFrequency();
            mNwdService.setCurrentFrequency(raw, band, index);
            Log.d(TAG, "gotoPreset AIDL slot=" + index + " rawFreq=" + raw + " band=" + band);
        });
    }

    @Override
    public void nextFavorite() {
        performAidlCall("nextFavorite", () -> {
            mNwdService.prefeb(true);
        });
    }

    @Override
    public void prevFavorite() {
        performAidlCall("prevFavorite", () -> {
            mNwdService.prefeb(false);
        });
    }

    @Override
    public void setCallback(RadioEngineCallback cb) {
        this.mCallback = cb;
    }

    /** Para combinar con {@link CompositeRadioEngineCallback} cuando el motor es compartido. */
    public RadioEngineCallback getCallback() {
        return mCallback;
    }

    /**
     * Menú ingeniería: {@code true} si el AIDL a {@code com.nwd.radio.service} está activo.
     */
    public boolean isNwdServiceBound() {
        return mIsBound && mNwdService != null;
    }

    /** Diagnóstico: piloto RDS / portadora 19 kHz ({@code notifyStereo}), antes del filtro UI. */
    public boolean isStereoPilotReported() {
        return mIsStereo;
    }

    /** Diagnóstico: decodificador estéreo NWD ({@code notifyStereoOn} / {@code isStreroOn}). */
    public boolean isStereoDecoderEnabled() {
        return mStereoDecoderOn;
    }

    /**
     * Menú ingeniería: mismo broadcast que el arranque en frío ({@link #requestWakeUp()}) — despierta
     * el stack NWD ({@code com.nwd.radio}).
     */
    public void wakeNwdRadioFromEngineeringMenu() {
        requestWakeUp();
    }

    /**
     * V17.5: Envía el broadcast mágico que despierta el hardware NWD sin abrir la interfaz.
     */
    private void requestWakeUp() {
        if (mContext == null) return;
        try {
            Log.d(TAG, "QS6: Despertando hardware mediante ACTION_START_NWD_ACTIVITY");
            Intent intent = new Intent(ACTION_START_NWD_ACTIVITY);
            intent.putExtra("pkg", "com.nwd.radio");
            sendNwdBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error enviando wake-up broadcast", e);
        }
    }
}
