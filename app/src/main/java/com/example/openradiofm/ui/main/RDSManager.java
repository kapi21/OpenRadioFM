package com.example.openradiofm.ui.main;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.RdsDatabase;
import com.example.openradiofm.data.source.SupabaseLogoSource;
import com.example.openradiofm.utils.MetadataUtils;
import com.example.openradiofm.utils.PtyManager;

/**
 * Fase 3: Gestor de RDS y PTY.
 * Centraliza la visualización de nombres de emisoras, RT (Radio Text) y códigos PTY.
 */
public class RDSManager {
    private static final String TAG = "RDSManager";

    private final Context mContext;
    private final TextView tvRdsName;
    private final TextView tvRdsInfo;
    private final TextView tvPty;
    private final RdsDatabase mRdsDb;
    private final RDSListener mListener;

    private String mCurrentPi;
    private String mCurrentPty;
    /** V21.2: volatile: lectura segura desde hilos de fondo (executor de estación) vs escritura en UI. */
    private volatile String mLastConfirmedName; // V5.3: RDS PS Substitution
    private String mCustomNameOverride; // V16.4: Override del usuario al editar nombre
    private boolean mHasRdsLock = false;

    public interface RDSListener {
        void onRdsNameConfirmed(String name);
        void onRdsMetadataUpdated();
        int getCurrentFrequency();
        int getCurrentBand();
    }

    public RDSManager(Context context, View rootView, RdsDatabase rdsDb, RDSListener listener) {
        this.mContext = context;
        this.mRdsDb = rdsDb;
        this.mListener = listener;

        this.tvRdsName = rootView.findViewById(R.id.tvRdsName);
        this.tvRdsInfo = rootView.findViewById(R.id.tvRdsInfo);
        this.tvPty = rootView.findViewById(R.id.tvPty);
    }

    public void onRdsName(String name) {
        if (name != null && SupabaseLogoSource.isGarbageZeroPs(name)) {
            return;
        }
        if (tvRdsName != null && name != null && !name.isEmpty()) {
            MainActivity.setTextIfChanged(tvRdsName, name);
            tvRdsName.setVisibility(View.VISIBLE);
            mHasRdsLock = true;

            if (mCurrentPi != null && mRdsDb != null) {
                mRdsDb.savePiName(mCurrentPi, name);
            }

            if (mListener != null) {
                mLastConfirmedName = name; // V5.3: RDS PS Substitution
                mListener.onRdsNameConfirmed(name);
            }
        }
    }

    public void onRdsText(String text) {
        String cleanedText = MetadataUtils.cleanRdsText(text);
        if (tvRdsInfo != null) {
            String targetText = (cleanedText == null || cleanedText.isEmpty()) ? "" : cleanedText;
            String current = tvRdsInfo.getText().toString();
            if (!current.equals(targetText)) {
                tvRdsInfo.setText(targetText);
                tvRdsInfo.setSelected(true);
                tvRdsInfo.setVisibility(View.VISIBLE);
            }
            mHasRdsLock = (cleanedText != null && !cleanedText.isEmpty());
        }
    }

    public void onRdsPty(String pty) {
        if (pty == null || pty.trim().isEmpty()) {
            // No pisar PTY válido con eventos vacíos intermitentes.
            return;
        }
        mCurrentPty = pty;
        updatePtyUI(pty);
    }

    public void onRdsPi(String piCode) {
        this.mCurrentPi = piCode;
        if (mRdsDb != null) {
            String savedName = mRdsDb.getNameForPi(piCode);
            if (savedName != null && tvRdsName != null) {
                MainActivity.setTextIfChanged(tvRdsName, savedName);
                tvRdsName.setVisibility(View.VISIBLE);
                if (mListener != null) {
                    mLastConfirmedName = savedName; // V5.3: RDS PS Substitution
                    mListener.onRdsNameConfirmed(savedName);
                }
            }
        }
    }

    public void updatePtyUI(String pty) {
        String displayLabel = PtyManager.getPtyDisplayLabel(mContext, pty);

        if (tvPty != null) {
            tvPty.setText(displayLabel);
            tvPty.setVisibility(View.VISIBLE);
            tvPty.setSelected(true); // V16.x: Activar Marquee
        }
    }

    public void reset(boolean clearTexts) {
        mCurrentPi = null;
        mCurrentPty = null;
        mLastConfirmedName = null; // V5.3: RDS PS Substitution Reset
        mCustomNameOverride = null; // V16.4: Reset custom override
        mHasRdsLock = false;

        if (clearTexts) {
            if (tvRdsName != null) tvRdsName.setText("");
            if (tvRdsInfo != null) tvRdsInfo.setText("");
            if (tvPty != null) tvPty.setText(mContext.getString(R.string.pty_none));
        }
    }

    /**
     * V16.2: Centraliza la visualización de RDS en una sola llamada desde MainActivity.
     * Mueve la lógica de visibilidad y colores fuera de la Activity.
     */
    public void updateRDSDisplay(int freqKhz, boolean isNight, int nightBlue, int white) {
        // 1. RDS Name (PS) - Aunque esté GONE en XML, mantenemos la lógica para retrocompatibilidad
        String displayName = getDisplayName(freqKhz);
        if (tvRdsName != null) {
            MainActivity.setTextIfChanged(tvRdsName, displayName != null ? displayName : "");
            MainActivity.setTextColorIfChanged(tvRdsName, isNight ? nightBlue : white);
        }

        // 2. RDS Info (RT)
        if (tvRdsInfo != null) {
            String currentText = tvRdsInfo.getText().toString();
            // Si el texto es el por defecto o está vacío, lo limpiamos
            if (currentText.isEmpty() || currentText.equals("RDS TEXT INFO") || currentText.equals("RDS Info Text")) {
                MainActivity.setTextIfChanged(tvRdsInfo, "");
            }
            MainActivity.setVisibilityIfChanged(tvRdsInfo, View.VISIBLE);
            MainActivity.setTextColorIfChanged(tvRdsInfo, isNight ? nightBlue : white);
            if (!tvRdsInfo.isSelected()) {
                tvRdsInfo.setSelected(true); // Forzar marquee solo una vez
            }
        }

        // 3. PTY
        updatePtyUI(mCurrentPty);
        if (tvPty != null) {
            MainActivity.setTextColorIfChanged(tvPty, isNight ? nightBlue : white);
        }
    }

    public String getCurrentPty() { return mCurrentPty; }
    public boolean hasRdsLock() { return mHasRdsLock; }
    public String getConfirmedName() { return mLastConfirmedName; } // V5.3: RDS PS Substitution

    /**
     * V5.5: Devuelve el nombre que debería sustituir a la frecuencia numérica.
     * Prioridad:
     * 1. RDS PS en vivo (confirmado por el broadcast)
     * 2. Nombre personalizado del usuario (guardado en SharedPreferences)
     * 3. null (mostrar frecuencia numérica)
     */
    public String getDisplayName(int freqKhz) {
        // V16.4: 1. Nombre custom del usuario (máxima prioridad si fue editado explícitamente)
        if (mCustomNameOverride != null && !mCustomNameOverride.isEmpty()) {
            return mCustomNameOverride;
        }
        // 2. Nombre personalizado guardado en SharedPreferences
        try {
            String customName = mContext.getSharedPreferences("RadioStationNames",
                    android.content.Context.MODE_PRIVATE)
                    .getString("CUSTOM_" + freqKhz, null);
            if (customName != null && !customName.isEmpty()) {
                return customName;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading custom name for freq " + freqKhz, e);
        }
        // 3. RDS en vivo
        if (mLastConfirmedName != null && !mLastConfirmedName.isEmpty()) {
            return mLastConfirmedName;
        }
        
        // 4. RDS Guardado (Histórico)
        try {
            String rdsSaved = mContext.getSharedPreferences("RadioStationNames",
                    android.content.Context.MODE_PRIVATE)
                    .getString("RDS_" + freqKhz, null);
            if (rdsSaved != null && !rdsSaved.isEmpty()) {
                return rdsSaved;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading saved RDS name for freq " + freqKhz, e);
        }
        // 4. Sin nombre
        return null;
    }

    /**
     * V16.4: Establece un override de nombre custom del usuario.
     * Tiene máxima prioridad en getDisplayName() hasta resetear.
     */
    public void setCustomNameOverride(String name) {
        mCustomNameOverride = name;
    }

    /**
     * V16.4: Limpia el override de nombre custom.
     * Se vuelve a la lógica normal de prioridades.
     */
    public void clearCustomNameOverride() {
        mCustomNameOverride = null;
    }

    /**
     * V18.6.2: Cleanup to avoid memory leaks.
     */
    public void release() {
        // Nothing heavy to release here yet as they are simple TextViews,
        // but we keep the pattern for consistency.
        reset(true);
    }
}
