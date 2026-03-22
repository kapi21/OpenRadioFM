package com.example.openradiofm.ui.main;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.openradiofm.R;
import com.example.openradiofm.data.source.RadioEngineCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * V5.0: Gestor de Escaneo centralizado.
 * Libera a MainActivity y DialogManager de la lógica de búsqueda de emisoras.
 */
public class ScanManager {
    /** Mismo color que el toggle manual (indicador “escaneando”). */
    private static final int SCAN_ACTIVE_COLOR = Color.parseColor("#00E676");
    private final MainActivity mActivity;
    private final List<StationAdapter.ScannedStation> mCapturedList = new ArrayList<>();
    private StationAdapter mStationAdapter;
    private boolean mIsScanning = false;

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
        } else {
            mActivity.mEngine.stopScan();
            mIsScanning = false;
            applyScanButtonVisual(false, btn);
            mActivity.showToast("AutoScan detenido");
        }
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
