package com.example.openradiofm.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.openradiofm.R;

/**
 * V15: Gestor de Diálogos para reducir carga en MainActivity.
 */
public class DialogManager {
    private final MainActivity mActivity;
    private static final String TAG = "DialogManager";

    public DialogManager(MainActivity activity) {
        this.mActivity = activity;
    }

    public void showEditNameDialog() {
        if (mActivity.mEngine == null) return;
        int currentFreq = mActivity.mEngine.getCurrentFreq();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Editar nombre de emisora");
        builder.setMessage("Frecuencia: " + String.format("%.1f MHz", currentFreq / 1000.0));

        final EditText input = new EditText(mActivity);
        input.setSingleLine(true);

        // Pre-llenar con el nombre actual (si es custom o RDS)
        com.example.openradiofm.data.model.RadioStation s = mActivity.mRepository.getStationInfo(currentFreq, null);
        if (s.getName() != null) {
            input.setText(s.getName());
            input.setSelectAllOnFocus(true);
        }

        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                mActivity.mRepository.saveRdsName(currentFreq, newName);
                mActivity.showToast("Nombre guardado: " + newName);
                
                // Refrescar para que el PresetManager vea el cambio (V13)
                if (mActivity.mPresetManager != null) {
                    mActivity.mPresetManager.updateCardVisuals(-1, currentFreq, mActivity.getCurrentBand());
                }
                mActivity.refreshRadioStatus();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Restaurar Original", (dialog, which) -> {
            mActivity.mRepository.saveRdsName(currentFreq, null);
            mActivity.showToast("Nombre restaurado");
            if (mActivity.mPresetManager != null) {
                mActivity.mPresetManager.updateCardVisuals(-1, currentFreq, mActivity.getCurrentBand());
            }
            mActivity.refreshRadioStatus();
        });

        builder.show();
        input.requestFocus();
    }

    public void showPremiumSettingsDialog() {
        android.app.Dialog dialog = new android.app.Dialog(mActivity);
        dialog.setContentView(R.layout.dialog_premium_settings);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        View cardTheme = dialog.findViewById(R.id.cardTheme);
        View cardFonts = dialog.findViewById(R.id.cardFonts);
        View cardBackground = dialog.findViewById(R.id.cardBackground);
        View viewColorPreview = dialog.findViewById(R.id.viewColorPreview);
        TextView tvFontPreview = dialog.findViewById(R.id.tvFontPreview);
        TextView tvBackgroundStatus = dialog.findViewById(R.id.tvBackgroundStatus);

        androidx.appcompat.widget.SwitchCompat swLogosOnline = dialog.findViewById(R.id.switchLogosOnline);
        androidx.appcompat.widget.SwitchCompat swNight = dialog.findViewById(R.id.switchNightMode);
        androidx.appcompat.widget.SwitchCompat swHistory = dialog.findViewById(R.id.switchSaveHistory);
        androidx.appcompat.widget.SwitchCompat swGestures = dialog.findViewById(R.id.switchSwipeGestures);
        androidx.appcompat.widget.SwitchCompat swStatusBarV2 = dialog.findViewById(R.id.switchStatusBarV2);
        androidx.appcompat.widget.SwitchCompat swAm = dialog.findViewById(R.id.switchEnableAm);

        // Language Row
        View rowLanguage = dialog.findViewById(R.id.rowLanguage);
        TextView tvCurrentLanguage = dialog.findViewById(R.id.tvCurrentLanguage);
        mActivity.updateCurrentLanguageText(tvCurrentLanguage);
        if (rowLanguage != null) {
            rowLanguage.setOnClickListener(v -> {
                showNewLanguageSelector();
                dialog.dismiss();
            });
        }

        // Previews
        updateSettingsPreviews(viewColorPreview, tvFontPreview);
        if (tvBackgroundStatus != null) {
            int bgIdx = mActivity.mPrefs.getInt("pref_bg_mode", 1);
            String[] modes = { mActivity.getString(R.string.bg_pure_black), mActivity.getString(R.string.bg_fixed_image), mActivity.getString(R.string.bg_dynamic_logo) };
            if (bgIdx >= 0 && bgIdx < modes.length) tvBackgroundStatus.setText(modes[bgIdx]);
        }

        // Switches
        if (swLogosOnline != null) {
            swLogosOnline.setChecked(mActivity.mPrefs.getBoolean("pref_logos_online", false));
            swLogosOnline.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_logos_online", checked).apply();
                mActivity.showToast(checked ? "Logos Online: Activado" : "Logos Online: Desactivado");
            });
        }

        if (swStatusBarV2 != null) {
            swStatusBarV2.setChecked(mActivity.mPrefs.getBoolean("pref_show_status_bar_v2", false));
            swStatusBarV2.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_show_status_bar_v2", checked).apply();
                mActivity.showToast(checked ? mActivity.getString(R.string.status_bar_enabled) : mActivity.getString(R.string.status_bar_disabled));
                if (!mActivity.mIsV3) mActivity.showToast("Reinicia la app para aplicar el cambio de barra de estado");
            });
        }

        if (swNight != null) {
            boolean nightEnabled = mActivity.mPrefs.getBoolean("pref_night_mode_auto", false);
            swNight.setChecked(nightEnabled);
            View rowNightSchedule = dialog.findViewById(R.id.rowNightSchedule);
            if (rowNightSchedule != null) rowNightSchedule.setVisibility(nightEnabled ? View.VISIBLE : View.GONE);
            swNight.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_night_mode_auto", checked).apply();
                if (rowNightSchedule != null) rowNightSchedule.setVisibility(checked ? View.VISIBLE : View.GONE);
                if (checked) {
                    mActivity.checkAndApplyNightMode();
                    mActivity.showToast("Modo Noche Automático: Activado");
                }
            });
        }

        if (swAm != null) {
            swAm.setChecked(mActivity.mPrefs.getBoolean("pref_enable_am", true));
            swAm.setOnCheckedChangeListener((bv, checked) -> mActivity.mPrefs.edit().putBoolean("pref_enable_am", checked).apply());
        }

        cardTheme.setOnClickListener(v -> {
            if (mActivity.mPrefs.getBoolean("pref_night_mode_auto", false)) {
                mActivity.showToast("Desactiva Modo Noche Automático para elegir skin manualmente");
                return;
            }
            showThemeSelector(dialog, viewColorPreview, tvFontPreview);
        });
        cardFonts.setOnClickListener(v -> showFontSelector(dialog, tvFontPreview));
        cardBackground.setOnClickListener(v -> showBackgroundSelector(dialog, tvBackgroundStatus));

        dialog.findViewById(R.id.btnAbout).setOnClickListener(v -> showAboutDialog());
        dialog.findViewById(R.id.btnCloseSettings).setOnClickListener(v -> dialog.dismiss());
        
        // V15.1: Aplicar fuente de forma recursiva al diálogo de ajustes
        applyRecursiveFont(dialog.getWindow().getDecorView(), getSystemTypeface());
        
        dialog.show();
    }

    public void showThemeSelector(Dialog parentDialog, View colorPreview, TextView fontPreview) {
        String[] skins = { "Night Mode", "Classic", "Orange", "Blue", "Green", "Purple", "Red", "Yellow", "Cyan", "Pink", "White" };
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.select_skin)
                .setItems(skins, (d, w) -> {
                    com.example.openradiofm.ui.theme.ThemeManager.Skin[] skinValues = com.example.openradiofm.ui.theme.ThemeManager.Skin.values();
                    if (w < skinValues.length) {
                        new com.example.openradiofm.ui.theme.ThemeManager(mActivity).setSkin(skinValues[w]);
                        mActivity.applySkin(skinValues[w]);
                        updateSettingsPreviews(colorPreview, fontPreview);
                    }
                }).create();
        applyPremiumListStyle(dialog);
        dialog.show();
    }

    public void showFontSelector(Dialog parentDialog, TextView fontPreview) {
        String[] fonts = { mActivity.getString(R.string.font_default), mActivity.getString(R.string.font_bebas), mActivity.getString(R.string.font_digital), mActivity.getString(R.string.font_modern), mActivity.getString(R.string.font_orbitron), mActivity.getString(R.string.font_formula1) };
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.select_typography)
                .setItems(fonts, (d, w) -> {
                    mActivity.mPrefs.edit().putInt("pref_font_type", w).apply();
                    mActivity.applyFonts();
                    updateSettingsPreviews(null, fontPreview);
                }).create();
        applyPremiumListStyle(dialog);
        dialog.show();
    }

    public void showBackgroundSelector(Dialog parentDialog, TextView tvStatus) {
        String[] modes = { mActivity.getString(R.string.bg_pure_black), mActivity.getString(R.string.bg_fixed_image), mActivity.getString(R.string.bg_dynamic_logo) };
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.select_bg_mode)
                .setItems(modes, (d, w) -> {
                    mActivity.mPrefs.edit().putInt("pref_bg_mode", w).apply();
                    mActivity.loadCustomBackground();
                    mActivity.loadCarLogo();
                    mActivity.updateDynamicBackground(mActivity.mLastLogoUrl);
                    if (tvStatus != null) tvStatus.setText(modes[w]);
                }).create();
        applyPremiumListStyle(dialog);
        dialog.show();
    }

    public void showNewLanguageSelector() {
        String[] languages = { "Español (ES)", "English (EN)", "Français (FR)", "Deutsch (DE)", "Português (PT)", "Italiano (IT)", "Русский (RU)", "Română (RO)", "Українська (UK)", "Srpski (SR)" };
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.select_language)
                .setItems(languages, (d, w) -> {
                    String[] codes = { "es", "en", "fr", "de", "pt", "it", "ru", "ro", "uk", "sr" };
                    if (w < codes.length) {
                        mActivity.mPrefs.edit().putString("app_language", codes[w]).apply();
                        mActivity.showToast("Idioma cambiado: " + languages[w] + ". Reinicia la app.");
                    }
                }).create();
        applyPremiumListStyle(dialog);
        dialog.show();
    }

    private void applyPremiumListStyle(AlertDialog dialog) {
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.bg_submenu_box));
            window.setDimAmount(0.6f);
        }
        
        // V15.1: Aplicar fuente del sistema a la vista raíz del diálogo
        applyRecursiveFont(dialog.getWindow().getDecorView(), getSystemTypeface());
    }

    /**
     * Obtiene la fuente configurada actualmente en las preferencias.
     */
    private Typeface getSystemTypeface() {
        int fontIdx = mActivity.mPrefs.getInt("pref_font_type", 0);
        try {
            int[] fontRes = { 0, R.font.bebas, R.font.digital, R.font.inter, R.font.orbitron, R.font.formula1 };
            if (fontIdx > 0 && fontIdx < fontRes.length) {
                return androidx.core.content.res.ResourcesCompat.getFont(mActivity, fontRes[fontIdx]);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading font: " + fontIdx, e);
        }
        return Typeface.DEFAULT_BOLD;
    }

    /**
     * Aplica una fuente de forma recursiva a todos los TextViews (y derivados) en un árbol de vistas.
     */
    private void applyRecursiveFont(View v, Typeface tf) {
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyRecursiveFont(vg.getChildAt(i), tf);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(tf);
        }
    }

    public void showAboutDialog() {
        Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_about);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        // Versión dinámica
        try {
            TextView tvVersion = dialog.findViewById(R.id.tvAppVersion);
            if (tvVersion != null) {
                String versionName = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName;
                tvVersion.setText("Versión " + versionName);
            }
        } catch (Exception ignored) {}

        // Crédito Icons8 con Link
        TextView tvIcons8 = dialog.findViewById(R.id.tvIcons8Credit);
        if (tvIcons8 != null) {
            String text = "Icons by <a href='https://icons8.com/'>Icons8</a>";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvIcons8.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvIcons8.setText(Html.fromHtml(text));
            }
            tvIcons8.setMovementMethod(LinkMovementMethod.getInstance());
        }

        View btnClose = dialog.findViewById(R.id.btnClose);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        // V15.1: Aplicar fuente recursiva al diálogo About
        applyRecursiveFont(dialog.getWindow().getDecorView(), getSystemTypeface());

        dialog.show();
    }

    public void showEngineSelector() {
        String[] options = { mActivity.getString(R.string.engine_auto), "HCN", "MTK", "Standard", "TS" };
        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.radio_engine)
                .setItems(options, (dialog, which) -> {
                    mActivity.mPrefs.edit().putInt("pref_radio_engine", which).apply();
                    mActivity.showToast("Motor cambiado: " + options[which]);
                    mActivity.conectarRadio();
                }).show();
    }

    public void showHistoryDialog() {
        String historyStr = mActivity.mPrefs.getString("pref_station_history", "");
        if (historyStr.isEmpty()) {
            mActivity.showStyledToast(mActivity.getString(R.string.history_empty));
            return;
        }

        String[] freqs = historyStr.split(",");
        String[] displayNames = new String[freqs.length];
        for (int i = 0; i < freqs.length; i++) {
            int f = Integer.parseInt(freqs[i]);
            displayNames[i] = String.format("%.2f MHz", f / 1000.0f);
        }

        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(mActivity.getString(R.string.station_history))
                .setItems(displayNames, (d, w) -> {
                    if (mActivity.mEngine != null) {
                        mActivity.mEngine.tune(Integer.parseInt(freqs[w]));
                    }
                }).create();
        
        applyPremiumListStyle(dialog);
        dialog.show();
    }

    public void showSaveLoadFavoritesDialog() {
        if (!mActivity.checkStoragePermissions()) {
            mActivity.requestStoragePermissions();
            return;
        }

        Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_save_load);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        dialog.findViewById(R.id.btnSave).setOnClickListener(v -> {
            mActivity.saveFavoritesToFile();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnLoad).setOnClickListener(v -> {
            mActivity.loadFavoritesFromFile();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnDeleteAllFavs).setOnClickListener(v -> {
            mActivity.mPrefs.edit().clear().apply();
            mActivity.showToast("Todos los favoritos han sido borrados");
            mActivity.refreshPresetButtons();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnClearHistory).setOnClickListener(v -> {
            mActivity.mPrefs.edit().remove("pref_station_history").apply();
            mActivity.showToast("El historial ha sido borrado");
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        
        // V15.1: Aplicar fuente recursiva al diálogo Save/Load
        applyRecursiveFont(dialog.getWindow().getDecorView(), getSystemTypeface());
        
        dialog.show();
    }

    public void showSelectiveScanDialog() {
        if (mActivity.mEngine == null || mActivity.mMode != MainActivity.FmMode.FM_K706) return;

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

        mActivity.mCapturedList.clear();
        mActivity.mStationAdapter = mActivity.new StationAdapter();
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(mActivity));
        rv.setAdapter(mActivity.mStationAdapter);

        view.findViewById(R.id.btnNextScan).setOnClickListener(v -> {
            mActivity.mEngine.seekUp();
            tvStatus.setText("Buscando siguiente...");
        });

        dialog.setOnDismissListener(d -> mActivity.mEngine.setCallback(mActivity));

        mActivity.mEngine.setCallback(new com.example.openradiofm.data.source.RadioEngineCallback() {
            private int lastFreqReported = 0;

            @Override
            public void onFrequencyChanged(int freqKhz) {
                lastFreqReported = freqKhz;
                mActivity.runOnUiThread(() -> {
                    if (tvFreq != null) tvFreq.setText(String.format("%.2f MHz", (double)freqKhz / 1000.0));
                    tvStatus.setText("Escaneando...");
                });
            }

            @Override public void onBandChanged(int band) {}
            @Override public void onStereoChanged(boolean stereo) {}
            @Override public void onRdsName(String name) {
                mActivity.runOnUiThread(() -> {
                    if(!mActivity.mCapturedList.isEmpty() && (mActivity.mCapturedList.get(0).name == null || mActivity.mCapturedList.get(0).name.equals("Buscando RDS..."))) {
                        mActivity.mCapturedList.get(0).name = name;
                        if (mActivity.mStationAdapter != null) mActivity.mStationAdapter.notifyItemChanged(0);
                    }
                });
            }
            @Override public void onRdsText(String text) {}
            @Override public void onRdsPty(String pty) {}
            @Override public void onRdsStatus(boolean af, boolean ta, boolean tp) {}
            @Override public void onRdsPi(String piCode) {}
            @Override public void onDxLocalChanged(boolean isLocal) {}

            @Override public void onScanStatusChanged(boolean scanning) {
                mActivity.runOnUiThread(() -> {
                    if (!scanning) {
                        tvStatus.setText("Escaneo pausado / finalizado");
                        if (lastFreqReported > 0) {
                            boolean alreadyInList = false;
                            for(MainActivity.ScannedStation s : mActivity.mCapturedList) {
                                if(Math.abs(s.frequency - lastFreqReported) < 50) alreadyInList = true;
                            }
                            if(!alreadyInList) {
                                MainActivity.ScannedStation newStation = new MainActivity.ScannedStation(lastFreqReported);
                                mActivity.mCapturedList.add(0, newStation);
                                if (mActivity.mStationAdapter != null) mActivity.mStationAdapter.notifyItemInserted(0);
                                rv.scrollToPosition(0);
                                tvStatus.setText("Identificando emisora (RDS)...");
                            }
                        }
                    } else {
                        tvStatus.setText("Buscando siguiente...");
                    }
                });
            }

            @Override public void onRawEvent(int code, String data) {}
        });

        dialog.show();
        mActivity.mEngine.seekUp();
    }

    public void showCreditsDialog() {
        android.app.Dialog dialog = new android.app.Dialog(mActivity);
        dialog.setContentView(R.layout.dialog_credits);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        // Easter Egg Logic
        View ivLogo = dialog.findViewById(R.id.ivHackerImage);
        if (ivLogo != null) {
            ivLogo.setOnClickListener(new View.OnClickListener() {
                private int clicks = 0;
                @Override
                public void onClick(View v) {
                    clicks++;
                    if (clicks >= 7) {
                        mActivity.showToast("Easter Egg Activated!");
                        clicks = 0;
                    }
                }
            });
        }

        dialog.findViewById(R.id.btnOkCredits).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateSettingsPreviews(View colorView, TextView fontView) {
        if (colorView != null) {
            int color = new com.example.openradiofm.ui.theme.ThemeManager(mActivity).getAccentColor();
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(color);
            colorView.setBackground(shape);
        }
        if (fontView != null) {
            int fontIdx = mActivity.mPrefs.getInt("pref_font_type", 0);
            applyFontToView(fontView, fontIdx);
        }
    }

    private void applyFontToView(TextView tv, int fontType) {
        Typeface typeface = Typeface.DEFAULT_BOLD;
        try {
            int[] fontRes = { 0, R.font.bebas, R.font.digital, R.font.inter, R.font.orbitron, R.font.formula1 };
            if (fontType > 0 && fontType < fontRes.length) 
                typeface = androidx.core.content.res.ResourcesCompat.getFont(mActivity, fontRes[fontType]);
        } catch (Exception ignored) {}
        tv.setTypeface(typeface);
    }
}
