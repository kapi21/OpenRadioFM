package com.example.openradiofm.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.openradiofm.R;
import java.util.List;

/**
 * V18.6: Adaptador para la lista de emisoras escaneadas.
 * Extraído de MainActivity para mejorar la mantenibilidad.
 */
public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private final List<ScannedStation> mCapturedList;
    private final MainActivity mActivity;

    public static class ScannedStation {
        public int frequency;
        public String name = "Buscando RDS...";

        public ScannedStation(int f) {
            this.frequency = f;
        }
    }

    public static class StationViewHolder extends RecyclerView.ViewHolder {
        TextView freq, name;
        Button[] presets = new Button[18];

        StationViewHolder(View root) {
            super(root);
            freq = root.findViewById(R.id.tvFreq);
            name = root.findViewById(R.id.tvName);
            for (int i = 0; i < 18; i++) {
                int resId = root.getResources().getIdentifier("btnP" + (i + 1), "id", root.getContext().getPackageName());
                presets[i] = root.findViewById(resId);
            }
        }
    }

    public StationAdapter(MainActivity activity, List<ScannedStation> capturedList) {
        this.mActivity = activity;
        this.mCapturedList = capturedList;
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new StationViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_scanned_station, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder h, int p) {
        ScannedStation s = mCapturedList.get(p);
        h.freq.setText(String.format(java.util.Locale.US, "%.2f MHz", s.frequency / 1000.0f));
        h.name.setText(s.name);
        for (int i = 0; i < 18; i++) {
            final int slot = i;
            h.presets[i].setOnClickListener(v -> {
                if (mActivity.mPresetManager != null) {
                    mActivity.mPresetManager.savePreset(mActivity.mCurrentBand, slot, s.frequency,
                            s.name.equals("Buscando RDS...") ? "" : s.name);
                    mActivity.showToast("Guardado en Slot " + (slot + 1));
                    mActivity.refreshPresetButtons();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mCapturedList.size();
    }
}
