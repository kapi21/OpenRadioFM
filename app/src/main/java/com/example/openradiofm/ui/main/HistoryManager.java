package com.example.openradiofm.ui.main;

import android.app.AlertDialog;
import android.content.Context;
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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    // ==========================
    // === Backup estado app ====
    // ==========================

    public void saveMenuOptionsToFile() {
        try {
            File dir = getPreferredRadioLogosDir();
            if (!dir.exists()) dir.mkdirs();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File out = new File(dir, "opciones_" + timestamp + ".ors"); // OpenRadio Settings

            org.json.JSONObject root = new org.json.JSONObject();
            root.put("schemaVersion", 1);
            root.put("timestamp", timestamp);
            root.put("type", "menu_only");

            // RadioPresets: solo claves de opciones (pref_*)
            root.put("RadioPresets", dumpPrefsFiltered("RadioPresets", true));
            // ThemePrefs: completo (solo opciones)
            root.put("ThemePrefs", dumpPrefsFiltered("ThemePrefs", false));

            FileWriter w = new FileWriter(out);
            w.write(root.toString(2));
            w.close();

            mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_saved), out.getName()));
        } catch (Exception e) {
            mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_error), e.getMessage()));
            Log.e(TAG, "saveMenuOptionsToFile", e);
        }
    }

    public void loadMenuOptionsFromFile() {
        try {
            File legacy = getLegacyRadioLogosDir();
            File dir = (legacy.exists() && legacy.isDirectory()) ? legacy : getPreferredRadioLogosDir();
            File[] files = dir.listFiles((d, name) -> name.endsWith(".ors"));
            if (files == null || files.length == 0) {
                mActivity.showStyledToast(mActivity.getString(R.string.backup_no_files));
                return;
            }
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            String[] names = new String[files.length];
            for (int i = 0; i < files.length; i++) names[i] = files[i].getName();

            final File[] finalFiles = files;
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View dialogView = inflater.inflate(R.layout.dialog_favorites_file_picker, null);
            TextView tvTitle = dialogView.findViewById(R.id.tvFavPickerTitle);
            if (tvTitle != null) tvTitle.setText(mActivity.getString(R.string.backup_state_menu_import));
            ListView lv = dialogView.findViewById(R.id.lvFavFiles);
            View btnCancel = dialogView.findViewById(R.id.btnCancelFavPicker);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, R.layout.item_fav_file_row,
                    R.id.tvFavFileName, names);
            if (lv != null) {
                lv.setAdapter(adapter);
                lv.setOnItemClickListener((parent, itemView, which, id) -> {
                    try {
                        boolean layoutChanged = applyMenuOptionsFromJson(finalFiles[which]);
                        mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_loaded), finalFiles[which].getName()));
                        promptRestartAfterImport(layoutChanged);
                    } catch (Exception e) {
                        mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_error), e.getMessage()));
                    }
                });
            }
            AlertDialog dialog = new AlertDialog.Builder(mActivity).setView(dialogView).create();
            if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
            Window window = dialog.getWindow();
            if (window != null) {
                window.setDimAmount(0.7f);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.show();
            try { mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface()); } catch (Exception ignored) {}
        } catch (Exception e) {
            mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_error), e.getMessage()));
            Log.e(TAG, "loadMenuOptionsFromFile", e);
        }
    }

    public void saveFullBackupToZip() {
        new Thread(() -> {
            android.app.Dialog progress = null;
            try {
                File dir = getPreferredRadioLogosDir();
                if (!dir.exists()) dir.mkdirs();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File out = new File(dir, "backup_" + timestamp + ".orzip");

                final java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean(false);
                // CRÍTICO: el diálogo debe crearse en el hilo UI (si no, "Can't create handler inside thread...").
                progress = createAndShowProgressOnUiThread(
                        mActivity.getString(R.string.backup_state_full_export),
                        mActivity.getString(R.string.backup_state_full_export_desc),
                        cancelled
                );

                org.json.JSONObject root = new org.json.JSONObject();
                root.put("schemaVersion", 1);
                root.put("timestamp", timestamp);
                root.put("type", "full");
                root.put("RadioPresets", dumpPrefsFiltered("RadioPresets", false));
                root.put("ThemePrefs", dumpPrefsFiltered("ThemePrefs", false));
                root.put("RadioStationNames", dumpPrefsFiltered("RadioStationNames", false));
                root.put("OpenRadioFmWidget", dumpPrefsFiltered("OpenRadioFmWidget", false));

                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
                byte[] jsonBytes = root.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                zos.putNextEntry(new ZipEntry("state.json"));
                zos.write(jsonBytes);
                zos.closeEntry();

                File logosDir = dir;
                zipFolderImagesWithProgress(zos, logosDir, "RadioLogos/", progress, cancelled);
                zos.close();

                android.app.Dialog doneProgress = progress;
                mActivity.runOnUiThread(() -> {
                    try { doneProgress.dismiss(); } catch (Exception ignored) {}
                    mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_saved), out.getName()));
                });
            } catch (Exception e) {
                android.app.Dialog errProgress = progress;
                mActivity.runOnUiThread(() -> {
                    try { if (errProgress != null) errProgress.dismiss(); } catch (Exception ignored) {}
                    mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_error), e.getMessage()));
                });
                Log.e(TAG, "saveFullBackupToZip", e);
            }
        }).start();
    }

    public void loadFullBackupFromZip() {
        try {
            File legacy = getLegacyRadioLogosDir();
            File dir = (legacy.exists() && legacy.isDirectory()) ? legacy : getPreferredRadioLogosDir();
            File[] files = dir.listFiles((d, name) -> name.endsWith(".orzip"));
            if (files == null || files.length == 0) {
                mActivity.showStyledToast(mActivity.getString(R.string.backup_no_files));
                return;
            }
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            String[] names = new String[files.length];
            for (int i = 0; i < files.length; i++) names[i] = files[i].getName();

            final File[] finalFiles = files;
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View dialogView = inflater.inflate(R.layout.dialog_favorites_file_picker, null);
            TextView tvTitle = dialogView.findViewById(R.id.tvFavPickerTitle);
            if (tvTitle != null) tvTitle.setText(mActivity.getString(R.string.backup_state_full_import));
            ListView lv = dialogView.findViewById(R.id.lvFavFiles);
            View btnCancel = dialogView.findViewById(R.id.btnCancelFavPicker);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, R.layout.item_fav_file_row,
                    R.id.tvFavFileName, names);
            AlertDialog dialog = new AlertDialog.Builder(mActivity).setView(dialogView).create();
            if (lv != null) {
                lv.setAdapter(adapter);
                lv.setOnItemClickListener((parent, itemView, which, id) -> {
                    try {
                        final java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean(false);
                        final android.app.Dialog progress = buildBackupProgressDialog(mActivity.getString(R.string.backup_state_full_import),
                                mActivity.getString(R.string.backup_state_full_import_desc), cancelled);
                        mActivity.runOnUiThread(() -> {
                            try { progress.show(); } catch (Exception ignored) {}
                        });
                        new Thread(() -> {
                            boolean layoutChanged = false;
                            try {
                                layoutChanged = applyFullBackupFromZip(finalFiles[which], getPreferredRadioLogosDir(), progress, cancelled);
                                final boolean finalLayoutChanged = layoutChanged;
                                mActivity.runOnUiThread(() -> {
                                    try { progress.dismiss(); } catch (Exception ignored) {}
                                    mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_loaded), finalFiles[which].getName()));
                                    promptRestartAfterImport(finalLayoutChanged);
                                });
                            } catch (Exception e) {
                                mActivity.runOnUiThread(() -> {
                                    try { progress.dismiss(); } catch (Exception ignored) {}
                                    mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_error), e.getMessage()));
                                });
                            }
                        }).start();
                    } catch (Exception e) {
                        mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_error), e.getMessage()));
                    }
                    dialog.dismiss();
                });
            }
            if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
            Window window = dialog.getWindow();
            if (window != null) {
                window.setDimAmount(0.7f);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.show();
            try { mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface()); } catch (Exception ignored) {}
        } catch (Exception e) {
            mActivity.showStyledToast(String.format(mActivity.getString(R.string.backup_error), e.getMessage()));
            Log.e(TAG, "loadFullBackupFromZip", e);
        }
    }

    private org.json.JSONObject dumpPrefsFiltered(String name, boolean onlyMenuKeys) throws Exception {
        SharedPreferences p = mActivity.getSharedPreferences(name, Context.MODE_PRIVATE);
        Map<String, ?> all = p.getAll();
        org.json.JSONObject o = new org.json.JSONObject();
        for (String k : all.keySet()) {
            if (onlyMenuKeys) {
                if (!k.startsWith("pref_")) continue;
            }
            Object v = all.get(k);
            if (v instanceof Boolean || v instanceof Integer || v instanceof Long || v instanceof Float || v instanceof String) {
                o.put(k, v);
            } else if (v instanceof java.util.Set) {
                org.json.JSONArray arr = new org.json.JSONArray();
                for (Object it : (java.util.Set<?>) v) arr.put(String.valueOf(it));
                o.put(k, arr);
            }
        }
        return o;
    }

    private boolean applyMenuOptionsFromJson(File f) throws Exception {
        boolean beforeV3 = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                .getBoolean("pref_layout_v3", false);
        boolean beforeSimple = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                .getBoolean("pref_layout_simple", false);

        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new FileReader(f));
        String line;
        while ((line = r.readLine()) != null) sb.append(line).append("\n");
        r.close();
        org.json.JSONObject root = new org.json.JSONObject(sb.toString());
        if (root.has("RadioPresets")) applyPrefsObject("RadioPresets", root.getJSONObject("RadioPresets"), true);
        if (root.has("ThemePrefs")) applyPrefsObject("ThemePrefs", root.getJSONObject("ThemePrefs"), false);

        boolean afterV3 = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                .getBoolean("pref_layout_v3", false);
        boolean afterSimple = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                .getBoolean("pref_layout_simple", false);
        return beforeV3 != afterV3 || beforeSimple != afterSimple;
    }

    private void applyPrefsObject(String prefsName, org.json.JSONObject obj, boolean onlyMenuKeys) throws Exception {
        SharedPreferences p = mActivity.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = p.edit();
        java.util.Iterator<String> it = obj.keys();
        while (it.hasNext()) {
            String k = it.next();
            if (onlyMenuKeys && !k.startsWith("pref_")) continue;
            Object v = obj.get(k);
            if (v instanceof Boolean) e.putBoolean(k, (Boolean) v);
            else if (v instanceof Integer) e.putInt(k, (Integer) v);
            else if (v instanceof Long) e.putLong(k, (Long) v);
            else if (v instanceof Double) e.putFloat(k, ((Double) v).floatValue());
            else if (v instanceof String) e.putString(k, (String) v);
            else if (v instanceof org.json.JSONArray) {
                java.util.HashSet<String> set = new java.util.HashSet<>();
                org.json.JSONArray arr = (org.json.JSONArray) v;
                for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                e.putStringSet(k, set);
            }
        }
        e.apply();
    }

    private void zipFolderImages(ZipOutputStream zos, File folder, String prefix) throws Exception {
        if (folder == null || !folder.exists() || !folder.isDirectory()) return;
        File[] list = folder.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) continue;
            String n = f.getName().toLowerCase(Locale.ROOT);
            boolean ok = n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".webp");
            if (!ok) continue;
            ZipEntry ze = new ZipEntry(prefix + f.getName());
            zos.putNextEntry(ze);
            copy(new BufferedInputStream(new FileInputStream(f)), zos);
            zos.closeEntry();
        }
    }

    private boolean applyFullBackupFromZip(File zip, File targetDir, android.app.Dialog progress,
                                          java.util.concurrent.atomic.AtomicBoolean cancelled) throws Exception {
        // Detectar cambio de layout (por si hay que reiniciar)
        boolean beforeV3 = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                .getBoolean("pref_layout_v3", false);
        boolean beforeSimple = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                .getBoolean("pref_layout_simple", false);

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
        ZipEntry ze;
        String stateJson = null;
        int filesDone = 0;
        while ((ze = zis.getNextEntry()) != null) {
            if (cancelled != null && cancelled.get()) {
                try { zis.close(); } catch (Exception ignored) {}
                throw new Exception("Cancelado");
            }
            String name = ze.getName();
            if ("state.json".equals(name)) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                copy(zis, baos);
                stateJson = baos.toString("UTF-8");
            } else if (name.startsWith("RadioLogos/") && !ze.isDirectory()) {
                String fileName = name.substring("RadioLogos/".length());
                File out = new File(targetDir, fileName);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(out));
                copy(zis, os);
                os.close();
                filesDone++;
                updateProgressDialog(progress, "Restaurando logos… " + filesDone, -1);
            }
            zis.closeEntry();
        }
        zis.close();
        if (stateJson != null) {
            org.json.JSONObject root = new org.json.JSONObject(stateJson);
            if (root.has("RadioPresets")) applyPrefsObject("RadioPresets", root.getJSONObject("RadioPresets"), false);
            if (root.has("ThemePrefs")) applyPrefsObject("ThemePrefs", root.getJSONObject("ThemePrefs"), false);
            if (root.has("RadioStationNames")) applyPrefsObject("RadioStationNames", root.getJSONObject("RadioStationNames"), false);
            if (root.has("OpenRadioFmWidget")) applyPrefsObject("OpenRadioFmWidget", root.getJSONObject("OpenRadioFmWidget"), false);
        }

        boolean afterV3 = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                .getBoolean("pref_layout_v3", false);
        boolean afterSimple = mActivity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                .getBoolean("pref_layout_simple", false);
        return beforeV3 != afterV3 || beforeSimple != afterSimple;
    }

    private void zipFolderImagesWithProgress(ZipOutputStream zos, File folder, String prefix,
                                            android.app.Dialog progress,
                                            java.util.concurrent.atomic.AtomicBoolean cancelled) throws Exception {
        if (folder == null || !folder.exists() || !folder.isDirectory()) return;
        File[] list = folder.listFiles();
        if (list == null) return;
        // Solo imágenes
        java.util.ArrayList<File> imgs = new java.util.ArrayList<>();
        for (File f : list) {
            if (f.isDirectory()) continue;
            String n = f.getName().toLowerCase(Locale.ROOT);
            boolean ok = n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".webp");
            if (ok) imgs.add(f);
        }
        int total = imgs.size();
        for (int i = 0; i < total; i++) {
            if (cancelled != null && cancelled.get()) throw new Exception("Cancelado");
            File f = imgs.get(i);
            updateProgressDialog(progress, "Empaquetando logos… " + (i + 1) + "/" + total,
                    total == 0 ? 0 : (int) (((i + 1) * 100f) / total));
            ZipEntry ze = new ZipEntry(prefix + f.getName());
            zos.putNextEntry(ze);
            copy(new BufferedInputStream(new FileInputStream(f)), zos);
            zos.closeEntry();
        }
    }

    private android.app.Dialog buildBackupProgressDialog(String title, String initialMessage,
                                                        java.util.concurrent.atomic.AtomicBoolean cancelled) {
        android.app.Dialog d = new android.app.Dialog(mActivity);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_backup_progress);
        d.setCancelable(false);
        try {
            TextView tvT = d.findViewById(R.id.tvBackupProgressTitle);
            TextView tvM = d.findViewById(R.id.tvBackupProgressMessage);
            android.widget.ProgressBar pb = d.findViewById(R.id.pbBackup);
            if (tvT != null) tvT.setText(title);
            if (tvM != null) tvM.setText(initialMessage);
            if (pb != null) {
                pb.setIndeterminate(false);
                pb.setProgress(0);
            }
            View btn = d.findViewById(R.id.btnCancelBackup);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    if (cancelled != null) cancelled.set(true);
                });
            }
        } catch (Exception ignored) {}
        return d;
    }

    private android.app.Dialog createAndShowProgressOnUiThread(String title, String initialMessage,
                                                               java.util.concurrent.atomic.AtomicBoolean cancelled) throws Exception {
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final android.app.Dialog[] holder = new android.app.Dialog[1];
        final Exception[] err = new Exception[1];
        mActivity.runOnUiThread(() -> {
            try {
                android.app.Dialog d = buildBackupProgressDialog(title, initialMessage, cancelled);
                holder[0] = d;
                try { d.show(); } catch (Exception ignored) {}
            } catch (Exception e) {
                err[0] = e;
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        if (err[0] != null) throw err[0];
        return holder[0];
    }

    private void updateProgressDialog(android.app.Dialog d, String message, int percent) {
        if (d == null) return;
        mActivity.runOnUiThread(() -> {
            try {
                TextView tvM = d.findViewById(R.id.tvBackupProgressMessage);
                android.widget.ProgressBar pb = d.findViewById(R.id.pbBackup);
                if (tvM != null && message != null) tvM.setText(message);
                if (pb != null && percent >= 0) pb.setProgress(percent);
            } catch (Exception ignored) {}
        });
    }

    private void promptRestartAfterImport(boolean layoutChanged) {
        try {
            String msg = layoutChanged
                    ? "Ajustes cargados. Para aplicar el cambio de layout, reinicia la app."
                    : "Ajustes cargados. Algunas opciones pueden requerir reiniciar la app.";
            new AlertDialog.Builder(mActivity)
                    .setTitle("OpenRadioFM")
                    .setMessage(msg)
                    .setPositiveButton("Reiniciar ahora", (d, w) -> mActivity.restartAppForSettings())
                    .setNegativeButton("Más tarde", null)
                    .show();
        } catch (Exception ignored) {}
    }

    private static void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) >= 0) {
            out.write(buf, 0, n);
        }
        out.flush();
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

            // Recargar caché en memoria del PresetManager; si no, refreshButtons sigue
            // mostrando frecuencias antiguas hasta reiniciar o cambiar de layout/banda.
            if (mActivity.mPresetManager != null) {
                mActivity.mPresetManager.refreshPresetsCache(mActivity.getCurrentBand());
            }
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
