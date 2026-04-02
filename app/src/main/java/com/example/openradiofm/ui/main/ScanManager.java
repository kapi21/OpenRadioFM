package com.example.openradiofm.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.os.Handler;
import android.os.Looper;
import com.example.openradiofm.R;
import com.example.openradiofm.data.source.RadioEngineCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * V5.0: Gestor de Escaneo centralizado.
 * Libera a MainActivity y DialogManager de la lógica de búsqueda de emisoras.
 */
public class ScanManager {
    /** Mismo color que el toggle manual (indicador “escaneando”). */
    private static final int SCAN_ACTIVE_COLOR = Color.parseColor("#00E676");
    private static final int MAX_RESULTS = 18;
    /** Tiempo mínimo en una frecuencia antes de validar (QS6: scan AMS iba demasiado rápido). */
    private static final long RDS_WAIT_MS = 5200L;
    /** Tras aceptar emisora, espera extra para PS/logo/cache antes de guardar preset. */
    private static final long AUTOSAVE_HOLD_MS = 6000L;
    /** Barrido lento: no usar AMS(); ir de emisora en emisora con seekUp + pausa. */
    /** Primera búsqueda: dejar tiempo a RDS en la frecuencia actual antes del primer seekUp. */
    private static final long SLOW_SEEK_FIRST_MS = 11000L;
    private static final long SLOW_SEEK_INTERVAL_MS = 8500L;
    private static final int TOLERANCE_KHZ = 50;
    /** Inicio de banda FM (87,5 MHz): el AutoScan siempre arranca aquí para cubrir toda la banda. */
    private static final int FM_BAND_START_KHZ = 87500;
    /** Fin de banda FM (108,0 MHz). Al alcanzarlo, el autoscan lento termina solo. */
    private static final int FM_BAND_END_KHZ = 108000;
    // Umbrales suaves: si no hay señal, preferimos PS confirmado.
    private static final int MIN_RSSI_ACCEPT = 4;
    private static final int MIN_SNR_ACCEPT = 4;

    private final MainActivity mActivity;
    private final List<StationAdapter.ScannedStation> mCapturedList = new ArrayList<>();
    private StationAdapter mStationAdapter;
    private boolean mIsScanning = false;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private static class Pending {
        final int freqKhz;
        long firstSeenMs;
        int rssi;
        int snr;
        String ps;
        boolean accepted;

        Pending(int freqKhz, long nowMs, int rssi, int snr) {
            this.freqKhz = freqKhz;
            this.firstSeenMs = nowMs;
            this.rssi = rssi;
            this.snr = snr;
        }
    }

    private final Map<Integer, Pending> mPendingByKey = new HashMap<>();
    private final Map<Integer, Boolean> mAutoSavedByKey = new HashMap<>();
    private int mLastRssi = 0;
    private int mLastSnr = 0;

    // VXX: AutoScan inteligente (captura + guardado manual a presets)
    private AlertDialog mAutoScanDialog;
    private TextView mTvScanTitle;
    private TextView mTvScanFreq;
    private TextView mTvScanStatus;
    private Button mBtnStopScan;
    private Button mBtnNextScan;
    private int mLastScanFreq = 0;
    private boolean mAutoOverwritePresets = false;
    private int mNextAutoPresetSlot = 0;
    /** true = AutoScan usa seekUp periódico (lento); no llamar engine.scan() ni stopScan() OEM. */
    private boolean mSlowSeekAutoScan = false;
    private boolean mSlowFinishPosted = false;
    private final Runnable mSlowSeekRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsScanning || !mAutoOverwritePresets || !mSlowSeekAutoScan) return;
            if (mActivity == null || mActivity.mEngine == null) return;
            if (mNextAutoPresetSlot >= MAX_RESULTS) {
                finishSlowAutoScanInternal(null);
                return;
            }
            try {
                int cur = mActivity.mEngine.getCurrentFreq();
                if (isFmBandUi() && cur >= FM_BAND_END_KHZ) {
                    finishSlowAutoScanInternal(null);
                    return;
                }
                mActivity.mEngine.seekUp();
            } catch (Exception ignored) {}
            mMainHandler.postDelayed(this, SLOW_SEEK_INTERVAL_MS);
        }
    };

    public ScanManager(MainActivity activity) {
        this.mActivity = activity;
    }

    public List<StationAdapter.ScannedStation> getCapturedList() {
        return mCapturedList;
    }

    public StationAdapter getStationAdapter() {
        return mStationAdapter;
    }

    public boolean isScanning() {
        return mIsScanning;
    }

    /**
     * Sincroniza el estado local y el aspecto del botón con el motor (NWD/K706).
     * Llamar desde el hilo UI cuando {@link com.example.openradiofm.data.source.RadioEngineCallback#onScanStatusChanged}
     * o tras {@link com.example.openradiofm.data.source.RadioEngine#isScanning()} en onResume.
     */
    public void applyEngineScanState(boolean scanning) {
        mIsScanning = scanning;
        applyScanButtonVisual(scanning, null);
        if (scanning) {
            if (!mAutoOverwritePresets) {
                startSmartCaptureUiIfNeeded();
            }
        } else {
            mAutoOverwritePresets = false;
            onSmartScanFinished();
        }
    }

    private void applyScanButtonVisual(boolean scanning, ImageButton btn) {
        ImageButton target = btn != null ? btn : mActivity.findViewById(R.id.btnAutoScan);
        if (target == null) {
            return;
        }
        if (scanning) {
            target.setColorFilter(SCAN_ACTIVE_COLOR, PorterDuff.Mode.SRC_IN);
        } else {
            target.clearColorFilter();
        }
    }

    /**
     * Alterna el escaneo automático estándar.
     */
    public void toggleAutoScan(ImageButton btn) {
        if (mActivity.mEngine == null) return;
        
        if (!mIsScanning) {
            showAutoScanOverwriteConfirm(btn);
        } else {
            if (mSlowSeekAutoScan) {
                mMainHandler.removeCallbacks(mSlowSeekRunnable);
                mSlowSeekAutoScan = false;
                mAutoOverwritePresets = false;
                mIsScanning = false;
                applyScanButtonVisual(false, btn);
                mActivity.showToast("AutoScan detenido");
            } else {
                mActivity.mEngine.stopScan();
                mIsScanning = false;
                applyScanButtonVisual(false, btn);
                mActivity.showToast("AutoScan detenido");
            }
        }
    }

    private void showAutoScanOverwriteConfirm(ImageButton btn) {
        try {
            final Dialog dialog = new Dialog(mActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_autoscan_confirm);
            Window w = dialog.getWindow();
            if (w != null) {
                w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
                w.setDimAmount(0.7f);
            }

            View rootCard = dialog.findViewById(R.id.autoscan_dialog_root);
            if (rootCard != null) {
                try {
                    rootCard.setBackgroundResource(mActivity.getSkinDrawableId());
                } catch (Exception ignored) {
                }
            }

            View btnCancel = dialog.findViewById(R.id.btnAutoscanCancel);
            View btnStart = dialog.findViewById(R.id.btnAutoscanStart);
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> dialog.dismiss());
            }
            if (btnStart != null) {
                btnStart.setOnClickListener(v -> {
                    dialog.dismiss();
                    startAutoScanOverwrite(btn);
                });
            }

            try {
                Typeface tf = mActivity.getSystemTypeface();
                if (tf != null && dialog.getWindow() != null) {
                    mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), tf);
                }
            } catch (Exception ignored) {
            }

            dialog.show();
        } catch (Exception e) {
            startAutoScanOverwrite(btn);
        }
    }

    private void startAutoScanOverwrite(ImageButton btn) {
        if (mActivity.mEngine == null) return;
        mAutoOverwritePresets = true;
        mSlowFinishPosted = false;
        mNextAutoPresetSlot = 0;
        mAutoSavedByKey.clear();
        mCapturedList.clear();
        mPendingByKey.clear();

        // Limpiar presets 1-18 de la banda actual antes de empezar.
        try {
            if (mActivity.mPresetManager != null) {
                final int band = mActivity.mCurrentBand;
                for (int i = 0; i < MAX_RESULTS; i++) {
                    mActivity.mPresetManager.savePreset(band, i, 0, "");
                }
                mActivity.refreshPresetButtons();
            }
        } catch (Exception ignored) {}

        try {
            mIsScanning = true;
            mSlowSeekAutoScan = true;
            applyScanButtonVisual(true, btn);
            mActivity.showToast("AutoScan lento: buscando emisoras… (pulsa de nuevo para parar)");

            final boolean fmUi = isFmBandUi();
            if (fmUi) {
                try {
                    mActivity.mEngine.tune(FM_BAND_START_KHZ);
                } catch (Exception ignored) {
                }
            }

            // Primera captura: en FM tras sintonizar 87,5 MHz; fallback si el motor aún no informa freq.
            mMainHandler.postDelayed(() -> {
                if (!mIsScanning || !mAutoOverwritePresets) return;
                try {
                    int f = mActivity.mEngine.getCurrentFreq();
                    if (f > 0) {
                        onScanFrequencyChanged(f);
                    } else if (fmUi) {
                        onScanFrequencyChanged(FM_BAND_START_KHZ);
                    }
                } catch (Exception ignored) {}
            }, 800);

            // Barrido lento: seekUp cada SLOW_SEEK_INTERVAL_MS (no AMS / scan rápido).
            mMainHandler.removeCallbacks(mSlowSeekRunnable);
            mMainHandler.postDelayed(mSlowSeekRunnable, SLOW_SEEK_FIRST_MS);
        } catch (Exception e) {
            mIsScanning = false;
            mSlowSeekAutoScan = false;
            applyScanButtonVisual(false, btn);
            mActivity.showToast("No se pudo iniciar AutoScan");
        }
    }

    private void finishSlowAutoScanInternal(ImageButton btn) {
        if (!mSlowSeekAutoScan || mSlowFinishPosted) return;
        mSlowFinishPosted = true;
        mMainHandler.removeCallbacks(mSlowSeekRunnable);
        mSlowSeekAutoScan = false;
        mAutoOverwritePresets = false;
        mIsScanning = false;
        ImageButton target = btn != null ? btn : mActivity.findViewById(R.id.btnAutoScan);
        applyScanButtonVisual(false, target);
        int saved = mNextAutoPresetSlot;
        mActivity.showToast("AutoScan terminado (" + saved + " memorias)");
    }

    /** VXX: Alimentado desde MainActivity.onSignalUpdate durante el escaneo. */
    public void onSignalUpdate(int rssi, int snr) {
        mLastRssi = rssi;
        mLastSnr = snr;
    }

    /**
     * VXX: Llamar desde MainActivity.onFrequencyChanged mientras el motor está escaneando.
     */
    public void onScanFrequencyChanged(int freqKhz) {
        if (!mIsScanning) return;
        if (freqKhz <= 0) return;
        final int prevScanFreq = mLastScanFreq;

        if (mSlowSeekAutoScan && isFmBandUi()) {
            if (freqKhz >= FM_BAND_END_KHZ) {
                finishSlowAutoScanInternal(null);
                return;
            }
            // Algunos tuners no reportan 108.0 y saltan de ~107,9 MHz al inicio de FM.
            if (prevScanFreq >= 107500 && freqKhz < 92000) {
                finishSlowAutoScanInternal(null);
                return;
            }
        }

        mLastScanFreq = freqKhz;

        // Actualizar UI del diálogo si existe
        if (mTvScanFreq != null) {
            mTvScanFreq.setText(String.format(java.util.Locale.US, "%.2f MHz", (double) freqKhz / 1000.0));
        }
        if (mTvScanStatus != null) {
            mTvScanStatus.setText(mActivity.getString(R.string.scanning));
        }

        if (mCapturedList.size() >= MAX_RESULTS) {
            if (mSlowSeekAutoScan) {
                finishSlowAutoScanInternal(null);
            } else {
                try { if (mActivity.mEngine != null) mActivity.mEngine.stopScan(); } catch (Exception ignored) {}
            }
            return;
        }

        // Registrar pendiente y esperar RDS un poco antes de aceptarla.
        final int key = normalizeKey(freqKhz);
        if (isAlreadyAccepted(freqKhz)) return;
        Pending p = mPendingByKey.get(key);
        if (p == null) {
            final long now = android.os.SystemClock.elapsedRealtime();
            p = new Pending(freqKhz, now, mLastRssi, mLastSnr);
            mPendingByKey.put(key, p);
            final Pending pRef = p;
            mMainHandler.postDelayed(() -> validatePending(pRef), RDS_WAIT_MS);
        } else {
            // Re-visit: refrescar señal/timestamp pero mantener la espera original.
            p.rssi = mLastRssi;
            p.snr = mLastSnr;
        }
    }

    /**
     * VXX: Llamar desde MainActivity cuando se confirma PS (mejor que onRdsName "crudo").
     */
    public void onScanPsConfirmed(int freqKhz, String psName) {
        if (!mIsScanning) return;
        if (freqKhz <= 0) return;
        if (psName == null) return;
        String name = psName.trim();
        if (name.isEmpty()) return;
        // 1) Si ya está en lista, actualizar nombre.
        for (int i = 0; i < mCapturedList.size(); i++) {
            StationAdapter.ScannedStation s = mCapturedList.get(i);
            if (Math.abs(s.frequency - freqKhz) < TOLERANCE_KHZ) {
                if (s.name == null || s.name.equals("Buscando RDS...") || s.name.equals("Esperando RDS...")) {
                    s.name = name;
                    if (mStationAdapter != null) mStationAdapter.notifyItemChanged(i);
                }
                return;
            }
        }

        // 2) Si estaba pendiente, aceptarla inmediatamente (RDS confirmado = válida).
        final int key = normalizeKey(freqKhz);
        Pending p = mPendingByKey.get(key);
        if (p == null) {
            p = new Pending(freqKhz, android.os.SystemClock.elapsedRealtime(), mLastRssi, mLastSnr);
            mPendingByKey.put(key, p);
        }
        p.ps = name;
        if (!p.accepted) {
            acceptStation(freqKhz, name);
            p.accepted = true;
        }
    }

    /** FM1/FM2/FM3 en UI (0..2); AM y otras bandas no usan límite 108 MHz. */
    private boolean isFmBandUi() {
        return mActivity != null && mActivity.mCurrentBand >= 0 && mActivity.mCurrentBand <= 2;
    }

    private int normalizeKey(int freqKhz) {
        // Agrupar por tolerancia para evitar duplicados cercanos durante seek/scan.
        return (freqKhz / TOLERANCE_KHZ) * TOLERANCE_KHZ;
    }

    private boolean isAlreadyAccepted(int freqKhz) {
        for (StationAdapter.ScannedStation s : mCapturedList) {
            if (Math.abs(s.frequency - freqKhz) < TOLERANCE_KHZ) return true;
        }
        return false;
    }

    private boolean isStrongEnough(int rssi, int snr) {
        return (rssi >= MIN_RSSI_ACCEPT) || (snr >= MIN_SNR_ACCEPT);
    }

    private void validatePending(Pending p) {
        if (!mIsScanning) return;
        if (p == null || p.accepted) return;
        if (mCapturedList.size() >= MAX_RESULTS) return;
        if (isAlreadyAccepted(p.freqKhz)) {
            p.accepted = true;
            return;
        }

        // Si hay PS confirmado, aceptar. Si no, solo aceptar si la señal es suficiente.
        if (p.ps != null && !p.ps.trim().isEmpty()) {
            acceptStation(p.freqKhz, p.ps);
            p.accepted = true;
            return;
        }

        if (isStrongEnough(p.rssi, p.snr)) {
            acceptStation(p.freqKhz, "Esperando RDS...");
            p.accepted = true;
        } else {
            // Débil y sin RDS → ruido: descartar.
            mPendingByKey.remove(normalizeKey(p.freqKhz));
        }
    }

    private void acceptStation(int freqKhz, String name) {
        if (mCapturedList.size() >= MAX_RESULTS) return;
        StationAdapter.ScannedStation st = new StationAdapter.ScannedStation(freqKhz);
        if (name != null && !name.trim().isEmpty()) st.name = name;
        mCapturedList.add(st);
        if (mStationAdapter != null) mStationAdapter.notifyItemInserted(mCapturedList.size() - 1);

        // Auto-guardado inmediato (con hold) a presets 1-18 mientras escanea.
        if (mAutoOverwritePresets) {
            final int key = normalizeKey(freqKhz);
            if (!Boolean.TRUE.equals(mAutoSavedByKey.get(key))) {
                // Reservar slot ya para evitar duplicados en carreras
                mAutoSavedByKey.put(key, true);
                final int slot = mNextAutoPresetSlot;
                mNextAutoPresetSlot++;
                mMainHandler.postDelayed(() -> autoSaveToPreset(slot, freqKhz), AUTOSAVE_HOLD_MS);
            }
        }

        if (mCapturedList.size() >= MAX_RESULTS) {
            if (mSlowSeekAutoScan) {
                finishSlowAutoScanInternal(null);
            } else {
                try { if (mActivity.mEngine != null) mActivity.mEngine.stopScan(); } catch (Exception ignored) {}
            }
        }
    }

    private void autoSaveToPreset(int slot, int freqKhz) {
        if (!mIsScanning) return;
        if (slot < 0 || slot >= MAX_RESULTS) return;
        if (mActivity == null || mActivity.mPresetManager == null) return;

        // Intentar usar el mejor nombre que tengamos (PS confirmado > placeholder)
        String bestName = "";
        try {
            for (StationAdapter.ScannedStation s : mCapturedList) {
                if (Math.abs(s.frequency - freqKhz) <= TOLERANCE_KHZ) {
                    if (s.name != null) {
                        String n = s.name.trim();
                        if (!n.isEmpty() && !n.equals("Esperando RDS...") && !n.equals("Buscando RDS...")) {
                            bestName = n;
                        }
                    }
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Calentar cache de logo en background (si hay repo); el preset usará cache más adelante.
        try {
            if (mActivity.mRepository != null) {
                final String psHint = bestName != null && !bestName.trim().isEmpty() ? bestName.trim() : null;
                mActivity.mRepository.getStationInfo(freqKhz, url -> {}, psHint);
            }
        } catch (Exception ignored) {}

        try {
            final int band = mActivity.mCurrentBand;
            mActivity.mPresetManager.savePreset(band, slot, freqKhz, bestName != null ? bestName : "");
            mActivity.runOnUiThread(() -> {
                try { mActivity.refreshPresetButtons(); } catch (Exception ignored) {}
            });
            if (mTvScanStatus != null) {
                final String label = (bestName != null && !bestName.trim().isEmpty()) ? bestName.trim() : (freqKhz / 1000.0) + " MHz";
                mTvScanStatus.setText("Guardado en preset " + (slot + 1) + ": " + label);
            }
        } catch (Exception ignored) {}
    }

    private void startSmartCaptureUiIfNeeded() {
        // Reusar el layout existente del scan selectivo (pero sin interceptar callbacks)
        if (mAutoScanDialog != null && mAutoScanDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_selective_scan, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        mAutoScanDialog = dialog;

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#E6121212")));
        }

        mTvScanTitle = view.findViewById(R.id.tvScanTitle);
        mTvScanFreq = view.findViewById(R.id.tvCurrentScanFreq);
        mTvScanStatus = view.findViewById(R.id.tvScanStatus);
        androidx.recyclerview.widget.RecyclerView rv = view.findViewById(R.id.rvCapturedStations);
        mBtnStopScan = view.findViewById(R.id.btnStopScan);
        mBtnNextScan = view.findViewById(R.id.btnNextScan);

        if (mTvScanTitle != null) mTvScanTitle.setText("ESCANEANDO EMISORAS...");
        if (mTvScanStatus != null) mTvScanStatus.setText(mActivity.getString(R.string.searching_next));

        mCapturedList.clear();
        mPendingByKey.clear();
        mStationAdapter = new StationAdapter(mActivity, mCapturedList);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(mActivity));
        rv.setAdapter(mStationAdapter);

        if (mBtnStopScan != null) {
            mBtnStopScan.setOnClickListener(v -> {
                if (mActivity.mEngine != null) mActivity.mEngine.stopScan();
            });
        }
        if (mBtnNextScan != null) {
            mBtnNextScan.setOnClickListener(v -> {
                if (mActivity.mEngine != null) mActivity.mEngine.seekUp();
                if (mTvScanStatus != null) mTvScanStatus.setText(mActivity.getString(R.string.searching_next));
            });
        }

        dialog.setOnDismissListener(d -> {
            mAutoScanDialog = null;
            mTvScanTitle = null;
            mTvScanFreq = null;
            mTvScanStatus = null;
            mBtnStopScan = null;
            mBtnNextScan = null;
        });

        dialog.show();
    }

    private void onSmartScanFinished() {
        if (mAutoScanDialog == null || !mAutoScanDialog.isShowing()) return;
        if (mTvScanTitle != null) mTvScanTitle.setText("RESULTADOS DE AUTOSCAN");
        if (mTvScanStatus != null) mTvScanStatus.setText(mActivity.getString(R.string.scan_completed));
        if (mLastScanFreq > 0 && mTvScanFreq != null) {
            mTvScanFreq.setText(String.format(java.util.Locale.US, "%.2f MHz", (double) mLastScanFreq / 1000.0));
        }
        if (mBtnStopScan != null) {
            mBtnStopScan.setText("CERRAR");
            mBtnStopScan.setOnClickListener(v -> {
                try { mAutoScanDialog.dismiss(); } catch (Exception ignored) {}
            });
        }
        if (mBtnNextScan != null) {
            mBtnNextScan.setText("SOBRESCRIBIR PRESETS");
            mBtnNextScan.setOnClickListener(v -> confirmOverwritePresets());
        }
    }

    private void confirmOverwritePresets() {
        if (mActivity == null) return;
        if (mCapturedList.isEmpty()) {
            mActivity.showToast("No se han encontrado emisoras válidas.");
            return;
        }
        new AlertDialog.Builder(mActivity)
                .setTitle("Sobrescribir Presets")
                .setMessage("¿Quieres sobrescribir los presets 1-18 con las emisoras encontradas? Esto reemplazará los presets actuales.")
                .setNegativeButton("CANCELAR", (d, w) -> {})
                .setPositiveButton("SÍ, SOBRESCRIBIR", (d, w) -> overwritePresets18())
                .show();
    }

    private void overwritePresets18() {
        if (mActivity.mPresetManager == null) return;
        final int band = mActivity.mCurrentBand;
        int written = 0;
        for (int i = 0; i < MAX_RESULTS; i++) {
            if (i < mCapturedList.size()) {
                StationAdapter.ScannedStation s = mCapturedList.get(i);
                String name = (s.name != null && !s.name.equals("Buscando RDS...") && !s.name.equals("Esperando RDS..."))
                        ? s.name : "";
                mActivity.mPresetManager.savePreset(band, i, s.frequency, name);
                written++;
            } else {
                // Limpiar slots restantes
                mActivity.mPresetManager.savePreset(band, i, 0, "");
            }
        }
        mActivity.refreshPresetButtons();
        mActivity.showToast("Presets sobrescritos (" + written + " emisoras).");
        try { if (mAutoScanDialog != null) mAutoScanDialog.dismiss(); } catch (Exception ignored) {}
    }

    /**
     * Muestra el diálogo de Escaneo Selectivo (Originalmente en DialogManager).
     */
    public void showSelectiveScanDialog() {
        if (mActivity.mEngine == null || mActivity.mMode != MainActivity.FmMode.FM_K706)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_selective_scan, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#E6121212")));
        }

        TextView tvFreq = view.findViewById(R.id.tvCurrentScanFreq);
        TextView tvStatus = view.findViewById(R.id.tvScanStatus);
        androidx.recyclerview.widget.RecyclerView rv = view.findViewById(R.id.rvCapturedStations);
        
        view.findViewById(R.id.btnStopScan).setOnClickListener(v -> {
            mActivity.mEngine.stopScan();
            dialog.dismiss();
        });

        mCapturedList.clear();
        mStationAdapter = new StationAdapter(mActivity, mCapturedList);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(mActivity));
        rv.setAdapter(mStationAdapter);

        view.findViewById(R.id.btnNextScan).setOnClickListener(v -> {
            mActivity.mEngine.seekUp();
            tvStatus.setText(mActivity.getString(R.string.searching_next));
        });

        // Al cerrar, restaurar el callback principal a MainActivity
        dialog.setOnDismissListener(d -> mActivity.mEngine.setCallback(mActivity));

        // Interceptar eventos de motor durante el escaneo selectivo
        mActivity.mEngine.setCallback(new RadioEngineCallback() {
            private int lastFreqReported = 0;

            @Override
            public void onFrequencyChanged(int freqKhz) {
                lastFreqReported = freqKhz;
                mActivity.runOnUiThread(() -> {
                    if (tvFreq != null)
                        tvFreq.setText(String.format(java.util.Locale.US, "%.2f MHz", (double) freqKhz / 1000.0));
                    tvStatus.setText(mActivity.getString(R.string.scanning));
                });
            }

            @Override public void onBandChanged(int band) {}
            @Override public void onStereoChanged(boolean stereo) {}

            @Override
            public void onRdsName(String name) {
                mActivity.runOnUiThread(() -> {
                    if (!mCapturedList.isEmpty() && (mCapturedList.get(0).name == null
                            || mCapturedList.get(0).name.equals("Buscando RDS..."))) {
                        mCapturedList.get(0).name = name;
                        if (mStationAdapter != null)
                            mStationAdapter.notifyItemChanged(0);
                    }
                });
            }

            @Override public void onRdsText(String text) {}
            @Override public void onRdsPty(String pty) {}
            @Override public void onRdsStatus(boolean af, boolean ta, boolean tp) {}
            @Override public void onRdsPi(String piCode) {}
            @Override public void onDxLocalChanged(boolean isLocal) {}

            @Override
            public void onScanStatusChanged(boolean scanning) {
                mActivity.runOnUiThread(() -> {
                    if (!scanning) {
                        tvStatus.setText(mActivity.getString(R.string.scan_completed));
                        if (lastFreqReported > 0) {
                            boolean alreadyInList = false;
                            for (StationAdapter.ScannedStation s : mCapturedList) {
                                if (Math.abs(s.frequency - lastFreqReported) < 50)
                                    alreadyInList = true;
                            }
                            if (!alreadyInList) {
                                StationAdapter.ScannedStation newStation = new StationAdapter.ScannedStation(lastFreqReported);
                                mCapturedList.add(0, newStation);
                                if (mStationAdapter != null)
                                    mStationAdapter.notifyItemInserted(0);
                                rv.scrollToPosition(0);
                                tvStatus.setText(mActivity.getString(R.string.identifying_rds));
                            }
                        }
                    } else {
                        tvStatus.setText(mActivity.getString(R.string.searching_next));
                    }
                });
            }

            @Override public void onRawEvent(int code, String data) {}
            @Override public void onSignalUpdate(int rssi, int snr) {}
        });

        dialog.show();
        mActivity.mEngine.seekUp();
    }
}
