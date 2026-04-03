package com.example.openradiofm.ui.main;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.data.repository.RadioRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * V16: Gestor de Historial y Exportación/Importación de Favoritos.
 * Centraliza la persistencia de emisoras recientes y la gestión
 * de archivos .fav en /sdcard/RadioLogos/.
 */
public class HistoryManager {
    private static final String TAG = "HistoryManager";
    private static final int MAX_HISTORY_SIZE = 15;
    private static final String HISTORY_PREF_KEY = "pref_station_history";

    private final MainActivity mActivity;
    private final SharedPreferences mPrefs;
    
    // V21.1: Compatibilidad storage (preferir app-specific, fallback legacy /sdcard)
    private File getAppRadioLogosDir() {
        File external = mActivity.getExternalFilesDir(null);
        File base = (external != null) ? external : mActivity.getFilesDir();
        return new File(base, "RadioLogos");
    }
    
    private File getLegacyRadioLogosDir() {
        return new File("/sdcard/RadioLogos");
    }
    
    private File getPreferredRadioLogosDir() {
        File legacy = getLegacyRadioLogosDir();
        try {
            if ((legacy.exists() || legacy.mkdirs()) && legacy.canWrite()) return legacy;
        } catch (Exception ignored) {}
        File appDir = getAppRadioLogosDir();
        try { appDir.mkdirs(); } catch (Exception ignored) {}
        return appDir;
    }

    public HistoryManager(MainActivity activity, SharedPreferences prefs) {
        this.mActivity = activity;
        this.mPrefs = prefs;
    }

    // ========================
    // === Historial Reciente ===
    // ========================

    /**
     * Añade una frecuencia al historial de emisoras recientes.
     * La frecuencia más reciente va al inicio; se eliminan duplicados.
     *
     * @param freq Frecuencia en kHz (ej: 96900 = 96.9 MHz).
     */
    public void addToHistory(int freq) {
        if (freq <= 0) return;

        String historyStr = mPrefs.getString(HISTORY_PREF_KEY, "");
        List<String> history = new ArrayList<>();

        if (!historyStr.isEmpty()) {
            history.addAll(Arrays.asList(historyStr.split(",")));
        }

        String freqStr = String.valueOf(freq);
        history.remove(freqStr); // Eliminar duplicado si existe
        history.add(0, freqStr); // Insertar al inicio

        // Limitar tamaño
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(history.get(i));
            if (i < history.size() - 1) sb.append(",");
        }
        mPrefs.edit().putString(HISTORY_PREF_KEY, sb.toString()).apply();
        Log.d(TAG, "History updated: " + sb.toString());
    }

    /**
     * Limpia todo el historial de emisoras recientes.
     */
    public void clearHistory() {
        mPrefs.edit().remove(HISTORY_PREF_KEY).apply();
    }

    /**
     * Obtiene el historial como cadena separada por comas.
     */
    public String getHistoryString() {
        return mPrefs.getString(HISTORY_PREF_KEY, "");
    }

    // ====================================
    // === Exportar/Importar Favoritos ===
    // ====================================

    /**
     * V16.5: Borra todos los favoritos de todas las bandas (FM1-5, AM1-2).
     */
    public void deleteAllFavorites() {
        SharedPreferences.Editor editor = mPrefs.edit();
        for (int b = 0; b < 5; b++) {
            for (int p = 1; p <= 20; p++) {
                editor.remove("P" + p + "_B" + b);
            }
        }
        editor.apply();
        
        mActivity.showToast(mActivity.getString(R.string.all_favorites_deleted));
        
        if (mActivity.mPresetManager != null) {
            mActivity.mPresetManager.refreshPresetsCache(mActivity.getCurrentBand());
        }
        mActivity.refreshPresetButtons();
    }

    /**
     * Guarda los favoritos actuales en un archivo .fav (JSON) en /sdcard/RadioLogos/.
     */
    public void saveFavoritesToFile() {
        try {
            File radioLogosDir = getPreferredRadioLogosDir();
            if (!radioLogosDir.exists()) radioLogosDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            File favFile = new File(radioLogosDir, "favoritos_" + timestamp + ".fav");

            org.json.JSONObject jsonRoot = new org.json.JSONObject();
            org.json.JSONArray presetsArray = new org.json.JSONArray();

            RadioRepository repo = mActivity.mRepository;

            // V16.5: Sincronizado con loops de DialogManager (5 bandas, hasta 20 presets)
            for (int band = 0; band < 5; band++) {
                for (int i = 1; i <= 20; i++) {
                    String key = "P" + i + "_B" + band;
                    int freq = mPrefs.getInt(key, 0);

                    if (freq > 0) {
                        org.json.JSONObject presetObj = new org.json.JSONObject();
                        presetObj.put("preset", i);
                        presetObj.put("band", band);
                        presetObj.put("frequency", freq);

                        // Obtener nombre custom del repositorio
                        if (repo != null) {
                            RadioStation s = repo.getStationInfo(freq, null);
                            if (s != null && s.getName() != null && !s.getName().isEmpty()) {
                                presetObj.put("custom_name", s.getName());
                            }
                        }

                        presetsArray.put(presetObj);
                    }
                }
            }

            jsonRoot.put("presets", presetsArray);
            jsonRoot.put("version", "1.1");
            jsonRoot.put("timestamp", timestamp);

            FileWriter writer = new FileWriter(favFile);
            writer.write(jsonRoot.toString(2));
            writer.close();

            mActivity.showStyledToast(
                    String.format(mActivity.getString(R.string.favorites_saved), favFile.getName()));

        } catch (Exception e) {
            mActivity.showStyledToast(
                    String.format(mActivity.getString(R.string.error_saving_favorites), e.getMessage()));
            Log.e(TAG, "Error saving favorites", e);
        }
    }

    /**
     * Muestra un selector para cargar favoritos desde un archivo .fav.
     */
    public void loadFavoritesFromFile() {
        try {
            // Buscar en legacy si existe, sino usar app-specific
            File legacy = getLegacyRadioLogosDir();
            File radioLogosDir = (legacy.exists() && legacy.isDirectory()) ? legacy : getPreferredRadioLogosDir();
            if (!radioLogosDir.exists() || !radioLogosDir.isDirectory()) {
                mActivity.showStyledToast(mActivity.getString(R.string.folder_not_found));
                return;
            }

            File[] favFiles = radioLogosDir.listFiles((dir, name) -> name.endsWith(".fav"));

            if (favFiles == null || favFiles.length == 0) {
                mActivity.showStyledToast(mActivity.getString(R.string.no_favorites_files));
                return;
            }

            Arrays.sort(favFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            String[] fileNames = new String[favFiles.length];
            for (int i = 0; i < favFiles.length; i++) {
                fileNames[i] = favFiles[i].getName();
            }

            final File[] finalFavFiles = favFiles;

            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View dialogView = inflater.inflate(R.layout.dialog_favorites_file_picker, null);

            View rootCard = dialogView.findViewById(R.id.fav_file_dialog_root);
            if (rootCard != null) {
                try {
                    rootCard.setBackgroundResource(mActivity.getSkinDrawableId());
                } catch (Exception ignored) {
                }
            }

            TextView tvTitle = dialogView.findViewById(R.id.tvFavPickerTitle);
            if (tvTitle != null) {
                try {
                    tvTitle.setTypeface(mActivity.getSystemTypeface());
                } catch (Exception ignored) {
                }
            }

            ListView lv = dialogView.findViewById(R.id.lvFavFiles);
            View btnCancel = dialogView.findViewById(R.id.btnCancelFavPicker);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, R.layout.item_fav_file_row,
                    R.id.tvFavFileName, fileNames) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    TextView tv = v.findViewById(R.id.tvFavFileName);
                    if (tv != null) {
                        try {
                            tv.setTypeface(mActivity.getSystemTypeface());
                        } catch (Exception ignored) {
                        }
                    }
                    return v;
                }
            };

            AlertDialog dialog = new AlertDialog.Builder(mActivity).setView(dialogView).create();

            if (lv != null) {
                lv.setAdapter(adapter);
                lv.setOnItemClickListener((d, itemView, which, id) -> {
                    loadFavoritesFromSpecificFile(finalFavFiles[which]);
                    dialog.dismiss();
                });
            }

            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> dialog.dismiss());
            }

            Window window = dialog.getWindow();
            if (window != null) {
                window.setDimAmount(0.7f);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            dialog.show();

            try {
                mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());
            } catch (Exception ignored) {
            }

        } catch (Exception e) {
            mActivity.showStyledToast(
                    String.format(mActivity.getString(R.string.error_loading_favorites), e.getMessage()));
            Log.e(TAG, "Error loading favorites", e);
        }
    }

    /**
     * Carga favoritos desde un archivo .fav específico, restaurando presets y nombres.
     */
    private void loadFavoritesFromSpecificFile(File favFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(favFile));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            org.json.JSONObject jsonRoot = new org.json.JSONObject(jsonString.toString());
            org.json.JSONArray presetsArray = jsonRoot.getJSONArray("presets");

            SharedPreferences.Editor editor = mPrefs.edit();
            // Limpiar favoritos existentes antes de cargar
            for (int band = 0; band < 5; band++) {
                for (int i = 1; i <= 20; i++) {
                    editor.remove("P" + i + "_B" + band);
                }
            }

            RadioRepository repo = mActivity.mRepository;
            int loadedCount = 0;
            for (int i = 0; i < presetsArray.length(); i++) {
                org.json.JSONObject presetObj = presetsArray.getJSONObject(i);
                int presetNum = presetObj.getInt("preset");
                int freq = presetObj.getInt("frequency");
                int band = presetObj.optInt("band", 0);

                editor.putInt("P" + presetNum + "_B" + band, freq);

                if (presetObj.has("custom_name") && repo != null) {
                    String customName = presetObj.getString("custom_name");
                    repo.setCustomName(freq, customName);
                }

                loadedCount++;
            }

            editor.apply();
            mActivity.refreshPresetButtons();

            mActivity.showStyledToast(
                    String.format(mActivity.getString(R.string.favorites_loaded), loadedCount, favFile.getName()));

        } catch (Exception e) {
            mActivity.showStyledToast(
                    String.format(mActivity.getString(R.string.error_loading_favorites), e.getMessage()));
            Log.e(TAG, "Error loading favorites from file", e);
        }
    }
}
