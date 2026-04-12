package com.example.openradiofm.ui.main;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.example.openradiofm.data.source.RadioEngine;

/**
 * V22: Gestiona el estado de frecuencia, guardas de arranque/apagado/bootstrap,
 * transiciones RDS y refresh guards.
 *
 * Extraído de MainActivity para reducir acoplamiento y mejorar fluidez.
 * Centraliza lógica duplicada entre motores QS6/K706/Jancar.
 */
public class FrequencyStateManager {

    private static final String TAG = "FreqStateMgr";

    // === Constantes ===
    /** Tiempo de guarda para evitar arrastre de RDS/logo de la emisora anterior. */
    private static final long RDS_TRANSITION_GUARD_MS = 1200L;
    /** Margen tras cambiar de frecuencia antes de contribuir metadatos a la nube. */
    private static final long CLOUD_CONTRIB_FREQ_SETTLE_MS = 1750L;
    private static final String PREF_QS6_BOOTSTRAP_SANITIZED = "pref_qs6_bootstrap_sanitized";
    private static final String PREF_K706_BOOTSTRAP_SANITIZED = "pref_k706_bootstrap_sanitized";

    // === Estado de guardas de arranque ===
    private int mStartupSavedFreqKhz = -1;
    private long mStartupPersistGuardUntilMs = 0L;
    private int mStartupRetuneAttempts = 0;

    // === Estado de guardas de apagado ===
    private long mShutdownPersistGuardUntilMs = 0L;
    private volatile boolean mPowerOffRequested = false;

    // === Estado de sintonía solicitada por usuario ===
    private int mUserRequestedFreqKhz = -1;
    private long mUserRequestedFreqUntilMs = 0L;

    // === Estado de refresco de UI (master guard) ===
    private volatile int mLastRefreshFreq = -1;
    private volatile int mLastRefreshBand = -1;
    private volatile long mLastFullRefreshTime = 0;

    // === Estado de transición RDS ===
    private volatile String mPrevStationNameBeforeTune = "";
    private volatile long mRdsTransitionGuardUntilMs = 0L;

    // === Control de contribución a la nube ===
    private long mCloudContribAllowedAfterMs = 0L;

    // =========================================================================
    // Result DTO
    // =========================================================================

    /** Resultado de evaluar un cambio de frecuencia en handleFrequencyChange. */
    public static class FreqChangeResult {
        /** true → ignorar completamente el cambio. */
        public boolean blocked;
        /** true → no persistir en SharedPreferences. */
        public boolean suppressPersist;
        /** true → resintonizar a la frecuencia guardada (startup guard). */
        public boolean shouldRetune;
        public int retuneFreq;
        public int retuneBand;
    }

    // =========================================================================
    // Inicialización
    // =========================================================================

    /** Llama desde onCreate tras leer pref_last_freq. */
    public void prepareForStartup(int savedFreqKhz) {
        mStartupSavedFreqKhz = savedFreqKhz;
        mStartupPersistGuardUntilMs = SystemClock.elapsedRealtime() + 6000L;
        mStartupRetuneAttempts = 0;
    }

    /**
     * Sanitización unificada de frecuencia bootstrap (QS6 y K706).
     * Si la pref guardada es 87.5/87.6 (bootstrap del MCU), intenta recuperar
     * la frecuencia real del motor.
     *
     * Antes de V22 esta lógica estaba duplicada en MainActivity (una copia
     * para QS6 y otra idéntica para K706).
     *
     * @return int[]{freq, band} posiblemente corregidos
     */
    public int[] sanitizeBootstrapFrequency(SharedPreferences prefs, MainActivity.FmMode mode,
                                             int lastFreq, int lastBand, RadioEngine engine) {
        String prefKey;
        if (mode == MainActivity.FmMode.FM_QS6) {
            prefKey = PREF_QS6_BOOTSTRAP_SANITIZED;
        } else if (mode == MainActivity.FmMode.FM_K706) {
            prefKey = PREF_K706_BOOTSTRAP_SANITIZED;
        } else {
            return new int[]{lastFreq, lastBand};
        }

        if (prefs.getBoolean(prefKey, false)) {
            return new int[]{lastFreq, lastBand};
        }
        if (lastFreq != 87500 && lastFreq != 87600) {
            return new int[]{lastFreq, lastBand};
        }
        if (engine == null) {
            prefs.edit().putBoolean(prefKey, true).apply();
            return new int[]{lastFreq, lastBand};
        }

        try {
            int engineFreq = engine.getCurrentFreq();
            int engineBand = engine.getCurrentBand();
            if (engineFreq > 0 && engineFreq != 87500 && engineFreq != 87600) {
                prefs.edit()
                        .putInt("pref_last_freq", engineFreq)
                        .putInt("pref_last_band", engineBand)
                        .putBoolean(prefKey, true)
                        .apply();
                Log.d(TAG, mode + " sanitize: bootstrap pref replaced with engine freq "
                        + engineFreq + "/B" + engineBand);
                return new int[]{engineFreq, engineBand};
            } else {
                prefs.edit().putBoolean(prefKey, true).apply();
                Log.d(TAG, mode + " sanitize: bootstrap pref kept (no reliable engine freq yet)");
            }
        } catch (Exception e) {
            prefs.edit().putBoolean(prefKey, true).apply();
            Log.w(TAG, mode + " sanitize check failed", e);
        }
        return new int[]{lastFreq, lastBand};
    }

    // =========================================================================
    // Evaluación de cambio de frecuencia
    // =========================================================================

    /**
     * Evalúa si un cambio de frecuencia debe procesarse y qué acciones tomar.
     * Centraliza las guardas de arranque, apagado y bootstrap espurio que
     * antes estaban inline en handleFrequencyChange().
     */
    public FreqChangeResult evaluateFrequencyChange(int newFreq, int lastFreq, int currentBand,
                                                     MainActivity.FmMode mode, RadioEngine engine) {
        FreqChangeResult r = new FreqChangeResult();

        // Misma frecuencia → no procesar
        if (newFreq == lastFreq) {
            r.blocked = true;
            return r;
        }

        // Guarda de apagado
        if (SystemClock.elapsedRealtime() < mShutdownPersistGuardUntilMs) {
            Log.d(TAG, "Shutdown guard: skipping frequency callback " + newFreq);
            r.blocked = true;
            return r;
        }

        // Guarda de arranque
        if (mStartupSavedFreqKhz > 0
                && SystemClock.elapsedRealtime() < mStartupPersistGuardUntilMs) {
            if (newFreq != mStartupSavedFreqKhz && (newFreq == 87600 || newFreq == 87500)) {
                r.suppressPersist = true;
                Log.d(TAG, "Startup guard: suppress persist for bootstrap freq " + newFreq
                        + " (saved=" + mStartupSavedFreqKhz + ")");
                // Refuerzo de sintonía activa (QS6/K706/Jancar)
                if ((mode == MainActivity.FmMode.FM_QS6 || mode == MainActivity.FmMode.FM_K706
                        || mode == MainActivity.FmMode.FM_JANCAR_IVI)
                        && engine != null && mStartupRetuneAttempts < 3) {
                    mStartupRetuneAttempts++;
                    r.shouldRetune = true;
                    r.retuneFreq = mStartupSavedFreqKhz;
                    r.retuneBand = currentBand;
                }
            }
            if (newFreq == mStartupSavedFreqKhz) {
                mStartupPersistGuardUntilMs = 0L;
            }
        }

        // Guarda de bootstrap espurio QS6/K706
        if ((mode == MainActivity.FmMode.FM_QS6 || mode == MainActivity.FmMode.FM_K706
                || mode == MainActivity.FmMode.FM_JANCAR_IVI)
                && (newFreq == 87500 || newFreq == 87600)) {
            boolean userRequestedRecently =
                    mUserRequestedFreqKhz == newFreq
                            && SystemClock.elapsedRealtime() <= mUserRequestedFreqUntilMs;
            if (!userRequestedRecently) {
                r.suppressPersist = true;
                Log.d(TAG, "Bootstrap persist guard: suppress " + newFreq
                        + " (no recent user request, mode=" + mode + ")");
            }
        }

        return r;
    }

    // =========================================================================
    // Transiciones RDS
    // =========================================================================

    /**
     * Marca el inicio de una transición de frecuencia para evitar arrastre
     * de RDS/logo de la emisora anterior.
     */
    public void beginFrequencyTransition(String currentPs) {
        mPrevStationNameBeforeTune = currentPs != null ? currentPs : "";
        mRdsTransitionGuardUntilMs = SystemClock.elapsedRealtime() + RDS_TRANSITION_GUARD_MS;
    }

    /** Actualiza la guarda de contribución a la nube tras un cambio de frecuencia. */
    public void resetCloudContribGuard() {
        mCloudContribAllowedAfterMs = SystemClock.elapsedRealtime() + CLOUD_CONTRIB_FREQ_SETTLE_MS;
    }

    /** ¿Está permitido contribuir metadatos a la nube? */
    public boolean isCloudContribAllowed() {
        return SystemClock.elapsedRealtime() >= mCloudContribAllowedAfterMs;
    }

    /**
     * ¿Está activa la guarda de transición RDS (QS6/NWD)?
     * Evalúa tipo de motor + tiempo transcurrido.
     */
    public boolean isRdsTransitionGuardActive(RadioEngine engine) {
        try {
            boolean isQs6 = engine != null
                    && engine.getEngineName() != null
                    && engine.getEngineName().toUpperCase().contains("QS6");
            return isQs6 && SystemClock.elapsedRealtime() < mRdsTransitionGuardUntilMs;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * ¿Debería bloquearse un PS entrante por ser arrastre de la emisora anterior?
     */
    public boolean shouldBlockTransitionalRdsName(String incomingName) {
        long now = SystemClock.elapsedRealtime();
        String incoming = incomingName != null ? incomingName.trim() : "";
        return now < mRdsTransitionGuardUntilMs
                && !incoming.isEmpty()
                && mPrevStationNameBeforeTune != null
                && !mPrevStationNameBeforeTune.isEmpty()
                && mPrevStationNameBeforeTune.equalsIgnoreCase(incoming);
    }

    /** Acceso al timestamp de guarda para código que aún lo necesite. */
    public long getRdsTransitionGuardUntilMs() {
        return mRdsTransitionGuardUntilMs;
    }

    // =========================================================================
    // User Tune / PowerOff
    // =========================================================================

    /** Marca que el usuario solicitó explícitamente esta frecuencia. */
    public void markUserTune(int freqKhz) {
        mUserRequestedFreqKhz = freqKhz;
        mUserRequestedFreqUntilMs = SystemClock.elapsedRealtime() + 12000L;
    }

    /** Activa guardas de apagado. */
    public void prepareForPowerOff() {
        mShutdownPersistGuardUntilMs = SystemClock.elapsedRealtime() + 9000L;
        mPowerOffRequested = true;
    }

    public boolean isPowerOffRequested() {
        return mPowerOffRequested;
    }

    // =========================================================================
    // Refresh Guard (Master Guard de refreshRadioStatus)
    // =========================================================================

    /**
     * Evalúa si freq/band cambiaron respecto al último refresco completo.
     * Si cambiaron, actualiza las guardas internas automáticamente.
     *
     * @return true si se necesita un refresco completo de UI.
     */
    public boolean shouldFullRefresh(int freq, int band) {
        boolean changed = (freq != mLastRefreshFreq || band != mLastRefreshBand);
        if (changed) {
            mLastRefreshFreq = freq;
            mLastRefreshBand = band;
            mLastFullRefreshTime = System.currentTimeMillis();
        }
        return changed;
    }

    /** Resetea las guardas para forzar un refresco completo en la próxima llamada. */
    public void invalidateRefreshGuard() {
        mLastRefreshFreq = -1;
        mLastRefreshBand = -1;
    }
}
