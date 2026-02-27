package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.openradiofm.R;
import com.example.openradiofm.data.repository.RadioRepository;
import com.example.openradiofm.data.model.RadioStation;

/**
 * V13: Gestor de Presets para reducir carga en MainActivity.
 */
public class PresetManager {
    private final MainActivity mActivity;
    private final RadioRepository mRepository;
    private final SharedPreferences mPrefs;
    private final int mPresetsCount;
    private final int[] mPresets;
    
    private final View[] cardPresets;
    private final TextView[] tvPresets;
    private final ImageView[] ivPresets;

    public PresetManager(MainActivity activity, RadioRepository repository, SharedPreferences prefs, int count) {
        this.mActivity = activity;
        this.mRepository = repository;
        this.mPrefs = prefs;
        this.mPresetsCount = count;
        this.mPresets = new int[count];
        
        this.cardPresets = new View[count];
        this.tvPresets = new TextView[count];
        this.ivPresets = new ImageView[count];
    }

    public void bindViews(View root, boolean isV3) {
        for (int i = 0; i < mPresetsCount; i++) {
            int cardId = mActivity.getResources().getIdentifier("cardP" + (i + 1), "id", mActivity.getPackageName());
            int textId = mActivity.getResources().getIdentifier("tvP" + (i + 1), "id", mActivity.getPackageName());
            int imgId = mActivity.getResources().getIdentifier("ivP" + (i + 1), "id", mActivity.getPackageName());

            cardPresets[i] = root.findViewById(cardId);
            tvPresets[i] = root.findViewById(textId);
            ivPresets[i] = root.findViewById(imgId);
            
            final int index = i;
            if (cardPresets[i] != null) {
                cardPresets[i].setOnClickListener(v -> mActivity.gotoPreset(index));
                cardPresets[i].setOnLongClickListener(v -> {
                    mActivity.savePreset(index);
                    return true;
                });
            }
        }
    }

    public void refreshPresetsCache(int currentBand) {
        for (int i = 0; i < mPresetsCount; i++) {
            String key = "P" + (i + 1) + "_B" + currentBand;
            mPresets[i] = mPrefs.getInt(key, 0);
        }
    }

    public void refreshButtons(int currentBand) {
        for (int i = 0; i < mPresetsCount; i++) {
            updateCardVisuals(i, mPresets[i], currentBand);
        }
    }

    public void updateCardVisuals(int index, int freq, int currentBand) {
        if (index == -1) {
            // Buscar si esta frecuencia está en los presets actuales
            for (int i = 0; i < mPresetsCount; i++) {
                if (mPresets[i] == freq) {
                    index = i;
                    break;
                }
            }
        }
        
        if (index < 0 || index >= mPresetsCount) return;

        if (freq <= 0) {
            if (tvPresets[index] != null) {
                tvPresets[index].setText("---");
                tvPresets[index].setVisibility(View.VISIBLE);
            }
            if (ivPresets[index] != null) {
                ivPresets[index].setImageDrawable(null);
                ivPresets[index].setBackground(null);
            }
            return;
        }

        if (tvPresets[index] != null) {
            RadioStation s = mRepository.getStationInfo(freq, null);
            String displayName = s.getName();
            if (displayName != null && !displayName.isEmpty() && !displayName.matches("\\d+")) {
                tvPresets[index].setText(displayName);
            } else {
                // V12.2: Si estamos escaneando y no hay nombre, NO mostrar la frecuencia para evitar "correr"
                if (mActivity.mEngine != null && mActivity.mEngine.isScanning()) {
                    tvPresets[index].setText("Buscando...");
                } else {
                    tvPresets[index].setText(String.format(java.util.Locale.US, "%.1f", freq / 1000.0));
                }
            }
            tvPresets[index].setVisibility(View.VISIBLE);
        }

        final int fIndex = index;
        mRepository.getStationInfo(freq, logoUrl -> {
            mActivity.runOnUiThread(() -> {
                if (logoUrl != null && ivPresets[fIndex] != null) {
                    Glide.with(mActivity)
                            .load(logoUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(ivPresets[fIndex]);
                } else if (ivPresets[fIndex] != null) {
                    ivPresets[fIndex].setImageDrawable(null);
                }
            });
        });
    }

    /**
     * V13: Guarda un favorito en una banda y slot específicos.
     */
    public void savePreset(int band, int slot, int freq, String name) {
        if (slot < 0 || slot >= mPresetsCount) return;

        // 1. Guardar en SharedPreferences
        String key = "P" + (slot + 1) + "_B" + band;
        mPrefs.edit().putInt(key, freq).apply();

        // 2. Actualizar cache local si es la banda actual
        mPresets[slot] = freq;

        // 3. Guardar nombre personalizado si se proporciona
        if (name != null && !name.isEmpty() && mRepository != null) {
            mRepository.saveRdsName(freq, name);
        }

        // 4. Refrescar visualmente el botón
        updateCardVisuals(slot, freq, band);
    }

    /**
     * V14.2: Navegación por Software - Busca el siguiente favorito.
     */
    public int getNextFavorite(int currentFreq) {
        int nextFreq = -1;
        int firstFreq = -1;
        int tolerance = 50; // 0.05 MHz

        for (int i = 0; i < mPresetsCount; i++) {
            int f = mPresets[i];
            if (f <= 0) continue;

            if (firstFreq == -1 || f < firstFreq) firstFreq = f;

            if (f > (currentFreq + tolerance)) {
                if (nextFreq == -1 || f < nextFreq) {
                    nextFreq = f;
                }
            }
        }

        if (nextFreq == -1) nextFreq = firstFreq;
        return (nextFreq != -1 && Math.abs(nextFreq - currentFreq) > tolerance) ? nextFreq : -1;
    }

    /**
     * V14.2: Navegación por Software - Busca el favorito anterior.
     */
    public int getPreviousFavorite(int currentFreq) {
        int prevFreq = -1;
        int lastFreq = -1;
        int tolerance = 50; // 0.05 MHz

        for (int i = 0; i < mPresetsCount; i++) {
            int f = mPresets[i];
            if (f <= 0) continue;

            if (lastFreq == -1 || f > lastFreq) lastFreq = f;

            if (f < (currentFreq - tolerance)) {
                if (prevFreq == -1 || f > prevFreq) {
                    prevFreq = f;
                }
            }
        }

        if (prevFreq == -1) prevFreq = lastFreq;
        return (prevFreq != -1 && Math.abs(prevFreq - currentFreq) > tolerance) ? prevFreq : -1;
    }

    public int getFreq(int index) {
        return (index >= 0 && index < mPresetsCount) ? mPresets[index] : 0;
    }

    public void applyFonts(android.graphics.Typeface typeface) {
        for (TextView tv : tvPresets) {
            if (tv != null) {
                tv.setTypeface(typeface);
            }
        }
    }
}
