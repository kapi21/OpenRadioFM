package com.example.openradiofm.ui.main;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
    private static final long RDS_WAIT_MS = 1700L;
    private static final int TOLERANCE_KHZ = 50;
    // Umbrales suaves: si no hay señal, preferimos PS confirmado.
    private static final int MIN_RSSI_ACCEPT = 6;
    private static final int MIN_SNR_ACCEPT = 6;

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
            startSmartCaptureUiIfNeeded();
        } else {
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
            mActivity.mEngine.scan();
            mIsScanning = true;
            applyScanButtonVisual(true, btn);
            mActivity.showToast("AutoScan iniciado...");
            startSmartCaptureUiIfNeeded();
        } else {
            mActivity.mEngine.stopScan();
            mIsScanning = false;
            applyScanButtonVisual(false, btn);
            mActivity.showToast("AutoScan detenido");
        }
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
        mLastScanFreq = freqKhz;

        // Actualizar UI del diálogo si existe
        if (mTvScanFreq != null) {
            mTvScanFreq.setText(String.format(java.util.Locale.US, "%.2f MHz", (double) freqKhz / 1000.0));
        }
        if (mTvScanStatus != null) {
            mTvScanStatus.setText(mActivity.getString(R.string.scanning));
        }

        if (mCapturedList.size() >= MAX_RESULTS) {
            // Ya tenemos suficientes: detener para evitar basura.
            try { if (mActivity.mEngine != null) mActivity.mEngine.stopScan(); } catch (Exception ignored) {}
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

        if (mCapturedList.size() >= MAX_RESULTS) {
            try { if (mActivity.mEngine != null) mActivity.mEngine.stopScan(); } catch (Exception ignored) {}
        }
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
