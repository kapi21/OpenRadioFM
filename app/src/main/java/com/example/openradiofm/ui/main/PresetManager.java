package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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

    // VXX: Guard para evitar tremble QS6 entre nombre y frecuencia en el preset pulsado.
    // Solo bloquea el texto (fallback a frecuencia) durante un corto transitorio.
    private final long[] mPresetTextLockUntilMs;
    // VXX: Durante transitorio QS6, forzar limpieza del logo del slot pulsado (anti-arrastre).
    private final long[] mPresetLogoForceClearUntilMs;
    
    private final View[] cardPresets;
    private final TextView[] tvPresets;
    private final ImageView[] ivPresets;
    private final int[] mLogoRequestSeqPerSlot;
    private final int[] mTextRequestSeqPerSlot;
    private final int[] mLastVisualFreqPerSlot;
    private final java.util.concurrent.atomic.AtomicInteger mLogoRequestSeq = new java.util.concurrent.atomic.AtomicInteger(0);
    private final java.util.concurrent.atomic.AtomicInteger mTextRequestSeq = new java.util.concurrent.atomic.AtomicInteger(0);

    public PresetManager(MainActivity activity, RadioRepository repository, SharedPreferences prefs, int count) {
        this.mActivity = activity;
        this.mRepository = repository;
        this.mPrefs = prefs;
        this.mPresetsCount = count;
        this.mPresets = new int[count];
        
        this.cardPresets = new View[count];
        this.tvPresets = new TextView[count];
        this.ivPresets = new ImageView[count];
        this.mLogoRequestSeqPerSlot = new int[count];
        this.mTextRequestSeqPerSlot = new int[count];
        this.mLastVisualFreqPerSlot = new int[count];
        this.mPresetTextLockUntilMs = new long[count];
        this.mPresetLogoForceClearUntilMs = new long[count];
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

        final boolean lockActive = mPresetTextLockUntilMs[index] > android.os.SystemClock.elapsedRealtime();
        // Los logos de presets deben quedar estables una vez grabados: no forzar "clear" en transitorios.
        final boolean forceClearLogo = false;

        if (freq <= 0) {
            mLastVisualFreqPerSlot[index] = 0;
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
        final boolean freqChangedForSlot = mLastVisualFreqPerSlot[index] != freq;
        mLastVisualFreqPerSlot[index] = freq;
        final int fIndex = index;
        final int fFreqForSlot = freq;
        final int textRequestSeq = mTextRequestSeq.incrementAndGet();
        mTextRequestSeqPerSlot[fIndex] = textRequestSeq;
        if (tvPresets[index] != null) {
            final int fFreq = freq;
            final int fBand = currentBand;
            if (freqChangedForSlot) {
                // Solo cuando cambia de emisora en ese slot; evita "temblor" en refrescos repetidos.
                if (!lockActive) {
                    if (fBand >= 3) {
                        tvPresets[fIndex].setText(String.valueOf(fFreq));
                    } else {
                        tvPresets[fIndex].setText(String.format(java.util.Locale.US, "%.1f", fFreq / 1000.0));
                    }
                }
                tvPresets[fIndex].setVisibility(View.VISIBLE);
            }

            // QS6: durante lock transitorio, no pedir nombre asíncrono para evitar
            // mostrar temporalmente el PS de la emisora anterior.
            if (lockActive) {
                // Mantenemos el texto actual (normalmente el último válido del slot).
            } else {
            // V18.2: Mover la obtención de info a hilo secundario para evitar congelar la UI
            new Thread(() -> {
                if (mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed()) return;
                RadioStation s = null;
                if (mActivity.mEngine == null || !mActivity.mEngine.isScanning()) {
                    s = mRepository.getStationInfo(fFreq, null);
                }
                final String displayName = (s != null) ? s.getName() : "";
                
                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                    if (mTextRequestSeqPerSlot[fIndex] != textRequestSeq) return;
                    if (mPresets[fIndex] != fFreqForSlot) return;
                    if (displayName != null && !displayName.isEmpty() && !displayName.matches("\\d+")) {
                        tvPresets[fIndex].setText(displayName);
                    } else {
                        // V18.2: Formateo dinámico según banda (AM en kHz sin decimales)
                        boolean lockNow = mPresetTextLockUntilMs[fIndex] > android.os.SystemClock.elapsedRealtime();
                        if (!lockNow) {
                            if (fBand >= 3) { // BAND_AM1 o BAND_AM2 (o SW)
                                tvPresets[fIndex].setText(String.valueOf(fFreq));
                            } else {
                                tvPresets[fIndex].setText(String.format(java.util.Locale.US, "%.1f", fFreq / 1000.0));
                            }
                        }
                    }
                    tvPresets[fIndex].setVisibility(View.VISIBLE);
                });
            }).start();
            }
        }

        final int fFreqForLogo = freq;
        final int requestSeq = mLogoRequestSeq.incrementAndGet();
        mLogoRequestSeqPerSlot[fIndex] = requestSeq;
        // Anti-flicker: no limpiar el logo al pulsar/refrescar. Solo se sustituye cuando llega uno nuevo válido.
        if (lockActive && ivPresets[fIndex] != null) {
            // QS6: durante el lock anti-arrastre, intentar resolver logo local/cache al instante
            // para evitar que el box quede vacío 1-2s.
            new Thread(() -> {
                if (mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed()) return;
                RadioStation s = mRepository.getStationInfo(fFreqForLogo, null);
                final String immediateLogo = (s != null) ? s.getLogoUrl() : null;
                if (immediateLogo == null || immediateLogo.isEmpty()) return;

                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                    if (mLogoRequestSeqPerSlot[fIndex] != requestSeq) return;
                    if (mPresets[fIndex] != fFreqForLogo) return;
                    if (ivPresets[fIndex] == null) return;
                    Glide.with(ivPresets[fIndex])
                            .load(immediateLogo)
                            .transform(new RoundedCorners(20))
                            .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    // Quitar el fallback detrás para que no se vea a través del alfa.
                                    if (ivPresets[fIndex] != null) {
                                        ivPresets[fIndex].setBackground(null);
                                    }
                                    return false;
                                }
                            })
                            .into(ivPresets[fIndex]);
                });
            }).start();
        }
        if (!lockActive && (mActivity.mEngine == null || !mActivity.mEngine.isScanning())) {
            mRepository.getStationInfo(freq, logoUrl -> {
                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                    // Guard anti-stale: descartar callbacks viejos (slot reutilizado o frecuencia cambiada).
                    if (mLogoRequestSeqPerSlot[fIndex] != requestSeq) return;
                    if (mPresets[fIndex] != fFreqForLogo) return;

                    if (logoUrl != null && ivPresets[fIndex] != null) {
                        // Al aplicar logo real, quitar cualquier background residual.
                        ivPresets[fIndex].setBackground(null);
                        Glide.with(ivPresets[fIndex])
                                .load(logoUrl)
                                .transform(new RoundedCorners(20))
                                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        if (ivPresets[fIndex] != null) ivPresets[fIndex].setBackground(null);
                                        return false;
                                    }
                                })
                                .into(ivPresets[fIndex]);
                    } else if (ivPresets[fIndex] != null) {
                        // Si no hay logo, mantener el que ya esté (estabilidad visual).
                    }
                });
            });
        }
    }

    /**
     * Cancela cargas pendientes del slot pulsado y limpia visual inmediato.
     */
    public void preparePresetSelection(int index) {
        if (index < 0 || index >= mPresetsCount) return;
        // Solo cancelar callbacks antiguos; NO limpiar logos (evita parpadeo al pulsar).
        mLogoRequestSeqPerSlot[index] = mLogoRequestSeq.incrementAndGet();
        mTextRequestSeqPerSlot[index] = mTextRequestSeq.incrementAndGet();

        // Solo QS6: mientras llega la confirmación RDS, evitar que el preset "pinte" frecuencia interina.
        boolean isQs6 = false;
        try {
            isQs6 = mActivity.mEngine != null
                    && mActivity.mEngine.getEngineName() != null
                    && mActivity.mEngine.getEngineName().toUpperCase().contains("QS6");
        } catch (Exception ignored) {}
        mPresetTextLockUntilMs[index] = isQs6 ? android.os.SystemClock.elapsedRealtime() + 2200L : 0L;
        mPresetLogoForceClearUntilMs[index] = 0L;
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
     * V14.2/V21.3: Navegación Secuencial - Busca el siguiente slot ocupado.
     */
    public int getNextSequentialFavorite(int currentFreq) {
        int currentIndex = -1;
        int tolerance = 50;

        // 1. Identificar si estamos en un preset conocido
        for (int i = 0; i < mPresetsCount; i++) {
            if (mPresets[i] > 0 && Math.abs(mPresets[i] - currentFreq) <= tolerance) {
                currentIndex = i;
                break;
            }
        }

        // 2. Buscar el siguiente slot ocupado circularmente
        for (int i = 1; i <= mPresetsCount; i++) {
            int nextIdx = (currentIndex + i) % mPresetsCount;
            if (mPresets[nextIdx] > 0) {
                return mPresets[nextIdx];
            }
        }
        return -1;
    }

    /**
     * V14.2/V21.3: Navegación Secuencial - Busca el slot ocupado anterior.
     */
    public int getPreviousSequentialFavorite(int currentFreq) {
        int currentIndex = -1;
        int tolerance = 50;

        for (int i = 0; i < mPresetsCount; i++) {
            if (mPresets[i] > 0 && Math.abs(mPresets[i] - currentFreq) <= tolerance) {
                currentIndex = i;
                break;
            }
        }

        // Si currentIndex es -1 (frecuencia manual), empezamos desde el final o desde 0
        int start = (currentIndex == -1) ? 0 : currentIndex;

        for (int i = 1; i <= mPresetsCount; i++) {
            int prevIdx = (start - i + mPresetsCount) % mPresetsCount;
            if (mPresets[prevIdx] > 0) {
                return mPresets[prevIdx];
            }
        }
        return -1;
    }

    /**
     * V14.2: Navegación por Software (Legacy) - Busca el siguiente favorito más cercano por frecuencia.
     * Mantenida por compatibilidad.
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
     * V14.2: Navegación por Software (Legacy) - Busca el favorito anterior más cercano por frecuencia.
     * Mantenida por compatibilidad.
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

    /**
     * V16: Reproduce el siguiente favorito disponible.
     */
    public void playNextPreset() {
        if (mActivity == null) return;
        int currentFreq = (mActivity.mEngine != null) ? mActivity.mEngine.getCurrentFreq() : 0;
        int next = getNextFavorite(currentFreq);
        if (next != -1) {
            final int target = next;
            mActivity.runOnUiThread(() -> mActivity.gotoFreq(target));
        }
    }

    /**
     * V16: Reproduce el favorito anterior disponible.
     */
    public void playPrevPreset() {
        if (mActivity == null) return;
        int currentFreq = (mActivity.mEngine != null) ? mActivity.mEngine.getCurrentFreq() : 0;
        int prev = getPreviousFavorite(currentFreq);
        if (prev != -1) {
            final int target = prev;
            mActivity.runOnUiThread(() -> mActivity.gotoFreq(target));
        }
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

    /**
     * V18.6.2: Cleanup to avoid memory leaks and pending callbacks.
     */
    public void release() {
        // V18.6.3: Glide handles this automatically, but if we clear, 
        // we MUST use the view context and NOT the activity to avoid crashes 
        // when this is called from onDestroy().
        for (int i = 0; i < mPresetsCount; i++) {
            if (ivPresets[i] != null) {
                try {
                    // Glide.with(ivPresets[i]) is safe even if Activity is destroyed
                    // whereas Glide.with(mActivity) would crash.
                    Glide.with(ivPresets[i].getContext()).clear(ivPresets[i]);
                } catch (Exception ignored) {}
                ivPresets[i] = null;
            }
            if (tvPresets[i] != null) tvPresets[i] = null;
        }
    }
}
