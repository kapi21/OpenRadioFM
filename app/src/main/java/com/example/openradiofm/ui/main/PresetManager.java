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
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.openradiofm.R;
import com.example.openradiofm.data.repository.RadioRepository;
import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.util.AppIoExecutor;

/**
 * V13: Gestor de Presets para reducir carga en MainActivity.
 */
public class PresetManager {
    private final MainActivity mActivity;
    private final RadioRepository mRepository;
    private final SharedPreferences mPrefs;
    private final int mPresetsCount;
    private final int[] mPresets;

    /** Filas clonadas para scroll en bucle (opcional); mismo slot que el preset principal. */
    private static final class LoopMirror {
        final ImageView iv;
        final TextView tv;

        LoopMirror(ImageView iv, TextView tv) {
            this.iv = iv;
            this.tv = tv;
        }
    }

    @SuppressWarnings("unchecked")
    private final java.util.ArrayList<LoopMirror>[] mLoopMirrors;

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
        mLoopMirrors = new java.util.ArrayList[count];
        for (int i = 0; i < count; i++) {
            mLoopMirrors[i] = new java.util.ArrayList<>();
        }
    }

    public void clearLoopMirrors() {
        for (int i = 0; i < mPresetsCount; i++) {
            mLoopMirrors[i].clear();
        }
    }

    public void registerLoopMirror(int slot, View cardIgnored, ImageView iv, TextView tv) {
        if (slot < 0 || slot >= mPresetsCount) return;
        if (iv == null && tv == null) return;
        mLoopMirrors[slot].add(new LoopMirror(iv, tv));
    }

    private void setPresetSlotText(int slot, CharSequence text) {
        if (slot < 0 || slot >= mPresetsCount) return;
        if (tvPresets[slot] != null) {
            tvPresets[slot].setText(text);
            tvPresets[slot].setVisibility(View.VISIBLE);
        }
        for (LoopMirror m : mLoopMirrors[slot]) {
            if (m.tv != null) {
                m.tv.setText(text);
                m.tv.setVisibility(View.VISIBLE);
            }
        }
    }

    private void clearPresetSlotVisuals(int slot) {
        if (slot < 0 || slot >= mPresetsCount) return;
        if (tvPresets[slot] != null) {
            tvPresets[slot].setText("---");
            tvPresets[slot].setVisibility(View.VISIBLE);
        }
        if (ivPresets[slot] != null) {
            ivPresets[slot].setImageDrawable(null);
            ivPresets[slot].setBackground(null);
        }
        for (LoopMirror mir : mLoopMirrors[slot]) {
            if (mir.tv != null) {
                mir.tv.setText("---");
                mir.tv.setVisibility(View.VISIBLE);
            }
            if (mir.iv != null) {
                mir.iv.setImageDrawable(null);
                mir.iv.setBackground(null);
            }
        }
    }

    private void glideLogoIntoPresetSlot(int slot, Object model) {
        if (slot < 0 || slot >= mPresetsCount || model == null) return;
        java.util.ArrayList<ImageView> targets = new java.util.ArrayList<>(4);
        if (ivPresets[slot] != null) targets.add(ivPresets[slot]);
        for (LoopMirror m : mLoopMirrors[slot]) {
            if (m.iv != null) targets.add(m.iv);
        }
        for (ImageView target : targets) {
            Glide.with(target)
                    .load(model)
                    .apply(new RequestOptions()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
                    .transform(new RoundedCorners(20))
                    .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model,
                                Target<android.graphics.drawable.Drawable> t, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                Target<android.graphics.drawable.Drawable> t, DataSource dataSource, boolean isFirstResource) {
                            target.setBackground(null);
                            return false;
                        }
                    })
                    .into(target);
        }
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
            clearPresetSlotVisuals(index);
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
                        setPresetSlotText(fIndex, String.valueOf(fFreq));
                    } else {
                        setPresetSlotText(fIndex, String.format(java.util.Locale.US, "%.1f", fFreq / 1000.0));
                    }
                } else {
                    if (tvPresets[fIndex] != null) tvPresets[fIndex].setVisibility(View.VISIBLE);
                    for (LoopMirror m : mLoopMirrors[fIndex]) {
                        if (m.tv != null) m.tv.setVisibility(View.VISIBLE);
                    }
                }
            }

            // QS6: durante lock transitorio, no pedir nombre asíncrono para evitar
            // mostrar temporalmente el PS de la emisora anterior.
            if (lockActive) {
                // Mantenemos el texto actual (normalmente el último válido del slot).
            } else {
            // V18.2: Mover la obtención de info a hilo secundario para evitar congelar la UI
            final int bgGen = mActivity.getUiWorkGeneration();
            AppIoExecutor.execute(() -> {
                if (mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed()) return;
                if (mActivity.getUiWorkGeneration() != bgGen) return;
                RadioStation s = null;
                if (mActivity.mEngine == null || !mActivity.mEngine.isScanning()) {
                    s = mRepository.getStationInfo(fFreq, null);
                }
                final String displayName = (s != null) ? s.getName() : "";
                
                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                    if (mActivity.getUiWorkGeneration() != bgGen) return;
                    if (mTextRequestSeqPerSlot[fIndex] != textRequestSeq) return;
                    if (mPresets[fIndex] != fFreqForSlot) return;
                    if (displayName != null && !displayName.isEmpty() && !displayName.matches("\\d+")) {
                        setPresetSlotText(fIndex, displayName);
                    } else {
                        // V18.2: Formateo dinámico según banda (AM en kHz sin decimales)
                        boolean lockNow = mPresetTextLockUntilMs[fIndex] > android.os.SystemClock.elapsedRealtime();
                        if (!lockNow) {
                            if (fBand >= 3) { // BAND_AM1 o BAND_AM2 (o SW)
                                setPresetSlotText(fIndex, String.valueOf(fFreq));
                            } else {
                                setPresetSlotText(fIndex, String.format(java.util.Locale.US, "%.1f", fFreq / 1000.0));
                            }
                        }
                    }
                    if (tvPresets[fIndex] != null) {
                        tvPresets[fIndex].setVisibility(View.VISIBLE);
                    }
                });
            });
            }
        }

        final int fFreqForLogo = freq;
        final int requestSeq = mLogoRequestSeq.incrementAndGet();
        mLogoRequestSeqPerSlot[fIndex] = requestSeq;
        // Anti-flicker: no limpiar el logo al pulsar/refrescar. Solo se sustituye cuando llega uno nuevo válido.
        if (lockActive && ivPresets[fIndex] != null) {
            // QS6: durante el lock anti-arrastre, intentar resolver logo local/cache al instante
            // para evitar que el box quede vacío 1-2s.
            final int bgGenLogo = mActivity.getUiWorkGeneration();
            AppIoExecutor.execute(() -> {
                if (mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed()) return;
                if (mActivity.getUiWorkGeneration() != bgGenLogo) return;
                RadioStation s = mRepository.getStationInfo(fFreqForLogo, null);
                final String immediateLogo = (s != null) ? s.getLogoUrl() : null;
                if (immediateLogo == null || immediateLogo.isEmpty()) return;

                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                    if (mActivity.getUiWorkGeneration() != bgGenLogo) return;
                    if (mLogoRequestSeqPerSlot[fIndex] != requestSeq) return;
                    if (mPresets[fIndex] != fFreqForLogo) return;
                    if (ivPresets[fIndex] == null && mLoopMirrors[fIndex].isEmpty()) return;
                    glideLogoIntoPresetSlot(fIndex, immediateLogo);
                });
            });
        }
        if (!lockActive && (mActivity.mEngine == null || !mActivity.mEngine.isScanning())) {
            mRepository.getStationInfo(freq, logoUrl -> {
                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                    // Guard anti-stale: descartar callbacks viejos (slot reutilizado o frecuencia cambiada).
                    if (mLogoRequestSeqPerSlot[fIndex] != requestSeq) return;
                    if (mPresets[fIndex] != fFreqForLogo) return;

                    if (logoUrl != null) {
                        if (ivPresets[fIndex] != null) {
                            ivPresets[fIndex].setBackground(null);
                        }
                        if (ivPresets[fIndex] != null || !mLoopMirrors[fIndex].isEmpty()) {
                            glideLogoIntoPresetSlot(fIndex, logoUrl);
                        }
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
     * V14.2/V21.3: Navegación secuencial por slots (0…N-1) con bucle: tras el último ocupado
     * vuelve al primero. Fuera de un preset (sintonía manual), “siguiente” = primer slot ocupado
     * en orden creciente de índice.
     */
    public int getNextSequentialFavorite(int currentFreq) {
        int currentIndex = -1;
        int tolerance = 50;

        for (int i = 0; i < mPresetsCount; i++) {
            if (mPresets[i] > 0 && Math.abs(mPresets[i] - currentFreq) <= tolerance) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            for (int s = 0; s < mPresetsCount; s++) {
                if (mPresets[s] > 0) return mPresets[s];
            }
            return -1;
        }

        // En un preset: recorrer el anillo sin volver al mismo índice hasta agotar el resto
        // (evita devolver la misma frecuencia si solo hay un preset guardado).
        for (int step = 1; step < mPresetsCount; step++) {
            int nextIdx = (currentIndex + step) % mPresetsCount;
            if (mPresets[nextIdx] > 0) {
                return mPresets[nextIdx];
            }
        }
        return -1;
    }

    /**
     * Igual que {@link #getNextSequentialFavorite} pero hacia atrás: desde el primer slot ocupado
     * en orden de índice al estar en manual; en un preset, bucle al slot ocupado anterior.
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

        if (currentIndex == -1) {
            for (int s = mPresetsCount - 1; s >= 0; s--) {
                if (mPresets[s] > 0) return mPresets[s];
            }
            return -1;
        }

        for (int step = 1; step < mPresetsCount; step++) {
            int prevIdx = (currentIndex - step + mPresetsCount) % mPresetsCount;
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
     * V16: Misma lógica que los botones prev/next de pantalla (bucle por slots), para volante
     * y {@link com.example.openradiofm.service.RadioMediaService}.
     */
    public void playNextPreset() {
        if (mActivity == null) return;
        mActivity.runOnUiThread(() -> mActivity.gotoNextFavorite());
    }

    /**
     * @see #playNextPreset
     */
    public void playPrevPreset() {
        if (mActivity == null) return;
        mActivity.runOnUiThread(() -> mActivity.gotoPreviousFavorite());
    }

    public int getFreq(int index) {
        return (index >= 0 && index < mPresetsCount) ? mPresets[index] : 0;
    }

    public void applyFonts(android.graphics.Typeface typeface) {
        for (int i = 0; i < mPresetsCount; i++) {
            if (tvPresets[i] != null) {
                tvPresets[i].setTypeface(typeface);
            }
            for (LoopMirror m : mLoopMirrors[i]) {
                if (m.tv != null) {
                    m.tv.setTypeface(typeface);
                }
            }
        }
    }

    /**
     * V18.6.2: Cleanup to avoid memory leaks and pending callbacks.
     */
    public void release() {
        clearLoopMirrors();
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
