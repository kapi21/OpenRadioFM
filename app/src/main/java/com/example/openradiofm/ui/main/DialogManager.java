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
        if (mActivity.mEngine == null)
            return;
        int currentFreq = mActivity.mEngine.getCurrentFreq();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.edit_station_name));
        builder.setMessage(mActivity.getString(R.string.frequency_label, String.format(java.util.Locale.getDefault(), "%.1f", currentFreq / 1000.0)));

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
                // V16.4: Usar setCustomName (CUSTOM_) en vez de saveRdsName (RDS_)
                mActivity.mRepository.setCustomName(currentFreq, newName);
                mActivity.showToast("Nombre guardado: " + newName);

                // V16.4: Notificar al RDSManager para que el custom tenga prioridad
                if (mActivity.mRdsManager != null) {
                    mActivity.mRdsManager.setCustomNameOverride(newName);
                }

                // Refrescar para que el PresetManager vea el cambio
                if (mActivity.mPresetManager != null) {
                    mActivity.mPresetManager.updateCardVisuals(-1, currentFreq, mActivity.getCurrentBand());
                }
                // V16.4: Forzar refresco de la frecuencia en pantalla
                mActivity.runOnUiThread(() -> {
                    // V18.6: Limpiar caché de logos de la Activity para que busque el nuevo nombre
                    int band = mActivity.getCurrentBand();
                    mActivity.mLogoCachePerBand.remove(band + "_" + currentFreq);
                    mActivity.mLastLogoUrl = ""; // Forzar que LogoManager no ignore el cambio
                    
                    mActivity.updateFrequencyDisplay(currentFreq, newName);
                    mActivity.refreshRadioStatus();
                });
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Restaurar Original", (dialog, which) -> {
            // V16.4: Limpiar nombre custom
            mActivity.mRepository.setCustomName(currentFreq, null);
            mActivity.showToast("Nombre restaurado");

            // V16.4: Limpiar override en RDSManager
            if (mActivity.mRdsManager != null) {
                mActivity.mRdsManager.clearCustomNameOverride();
            }

            if (mActivity.mPresetManager != null) {
                mActivity.mPresetManager.updateCardVisuals(-1, currentFreq, mActivity.getCurrentBand());
            }
            // V16.4: Forzar refresco
            mActivity.runOnUiThread(() -> {
                // V18.6: Limpiar caché al restaurar
                int band = mActivity.getCurrentBand();
                mActivity.mLogoCachePerBand.remove(band + "_" + currentFreq);
                mActivity.mLastLogoUrl = "";
                
                mActivity.updateFrequencyDisplay(currentFreq, null);
                mActivity.refreshRadioStatus();
            });
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

        // Night schedule views
        View rowNightSchedule = dialog.findViewById(R.id.rowNightSchedule);
        TextView tvNightStart = dialog.findViewById(R.id.tvNightStart);
        TextView tvNightEnd = dialog.findViewById(R.id.tvNightEnd);

        androidx.appcompat.widget.SwitchCompat swLogosOnline = dialog.findViewById(R.id.switchLogosOnline);
        androidx.appcompat.widget.SwitchCompat swNight = dialog.findViewById(R.id.switchNightMode);
        androidx.appcompat.widget.SwitchCompat swNightLogos = dialog.findViewById(R.id.switchNightLogos);
        androidx.appcompat.widget.SwitchCompat swHistory = dialog.findViewById(R.id.switchSaveHistory);
        androidx.appcompat.widget.SwitchCompat swCloudContrib = dialog.findViewById(R.id.switchCloudContrib);
        androidx.appcompat.widget.SwitchCompat swStatusBar = dialog.findViewById(R.id.swStatusBar);
        androidx.appcompat.widget.SwitchCompat swAutoHide = dialog.findViewById(R.id.swAutoHideControls);
        // androidx.appcompat.widget.SwitchCompat swAm = dialog.findViewById(R.id.switchEnableAm); // Removed v21.3

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

        // Night schedule initial text from prefs
        if (tvNightStart != null && tvNightEnd != null) {
            int startHour = mActivity.mPrefs.getInt("pref_night_start", 19);
            int startMin = mActivity.mPrefs.getInt("pref_night_start_min", 0);
            int endHour = mActivity.mPrefs.getInt("pref_night_end", 7);
            int endMin = mActivity.mPrefs.getInt("pref_night_end_min", 0);
            tvNightStart.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", startHour, startMin));
            tvNightEnd.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", endHour, endMin));
        }

        // V19.7: Indicador de Conexión a Supabase (Automático)
        TextView tvSupabaseStatus = dialog.findViewById(R.id.tvSupabaseStatus);
        if (tvSupabaseStatus != null) {
            tvSupabaseStatus.setVisibility(View.VISIBLE);
            tvSupabaseStatus.setText("• Conectando...");
            tvSupabaseStatus.setTextColor(Color.parseColor("#888888"));

            mActivity.mRepository.getSupabaseSource().checkConnection(connected -> {
                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed() || !dialog.isShowing()) return;
                    if (connected) {
                        tvSupabaseStatus.setText("• Online");
                        tvSupabaseStatus.setTextColor(Color.parseColor("#44FF44")); // Verde
                    } else {
                        tvSupabaseStatus.setText("• Offline");
                        tvSupabaseStatus.setTextColor(Color.parseColor("#FF4444")); // Rojo
                    }
                });
            });
        }
        if (tvBackgroundStatus != null) {
            int bgIdx = mActivity.mPrefs.getInt("pref_bg_mode", 1);
            String[] modes = { mActivity.getString(R.string.bg_pure_black),
                    mActivity.getString(R.string.bg_fixed_image), mActivity.getString(R.string.bg_dynamic_logo) };
            if (bgIdx >= 0 && bgIdx < modes.length)
                tvBackgroundStatus.setText(modes[bgIdx]);
        }

        // Switches
        if (swLogosOnline != null) {
            swLogosOnline.setChecked(mActivity.mPrefs.getBoolean("pref_logos_online", true));
            swLogosOnline.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_logos_online", checked).apply();
                if (checked) {
                    mActivity.showToast("Logos Online: Activado (Requiere Internet)");
                    mActivity.showToast("Se consultará la base de datos centralizada para buscar logos HD");
                } else {
                    mActivity.showToast("Logos Online: Desactivado");
                }
            });
        }

        // Logo Provider Row
        View rowLogoProvider = dialog.findViewById(R.id.rowLogoProvider);
        TextView tvCurrentLogoProvider = dialog.findViewById(R.id.tvCurrentLogoProvider);
        if (tvCurrentLogoProvider != null) {
            int providerIdx = mActivity.mPrefs.getInt("pref_logo_provider", 0); // 0=Supabase, 1=Web, 2=Both
            String[] providers = {
                    mActivity.getString(R.string.provider_supabase),
                    mActivity.getString(R.string.provider_radiobrowser),
                    mActivity.getString(R.string.provider_both)
            };
            if (providerIdx >= 0 && providerIdx < providers.length) {
                tvCurrentLogoProvider.setText(providers[providerIdx]);
            }
        }
        if (rowLogoProvider != null) {
            rowLogoProvider.setOnClickListener(v -> {
                showLogoProviderSelector();
                dialog.dismiss();
            });
        }

        if (swStatusBar != null) {
            swStatusBar.setChecked(mActivity.mPrefs.getBoolean("pref_show_status_bar_v2", false));
            swStatusBar.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_show_status_bar_v2", checked).apply();
                mActivity.showToast(checked ? mActivity.getString(R.string.status_bar_enabled)
                        : mActivity.getString(R.string.status_bar_disabled));
                mActivity.applyStatusBarVisibility();
            });
        }

        if (swAutoHide != null) {
            swAutoHide.setChecked(mActivity.mPrefs.getBoolean("pref_auto_hide_controls", false));
            swAutoHide.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_auto_hide_controls", checked).apply();
                mActivity.showToast(checked ? mActivity.getString(R.string.auto_hide_enabled)
                        : mActivity.getString(R.string.auto_hide_disabled));
                // V18.6: Reiniciar el temporizador si se activa
                if (checked) mActivity.resetAutoHideTimer();
                else mActivity.showBottomControls();
            });
        }

        if (swNight != null) {
            boolean nightEnabled = mActivity.mPrefs.getBoolean("pref_night_mode_auto", false);
            swNight.setChecked(nightEnabled);
            if (rowNightSchedule != null)
                rowNightSchedule.setVisibility(nightEnabled ? View.VISIBLE : View.GONE);
            swNight.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_night_mode_auto", checked).apply();
                if (rowNightSchedule != null)
                    rowNightSchedule.setVisibility(checked ? View.VISIBLE : View.GONE);
                if (checked) {
                    mActivity.checkAndApplyNightMode();
                    mActivity.showToast("Modo Noche Automático: Activado");
                }
            });
        }

        // Edit night schedule on tap
        if (rowNightSchedule != null && tvNightStart != null && tvNightEnd != null) {
            View.OnClickListener editSchedule = v -> {
                int startHour = mActivity.mPrefs.getInt("pref_night_start", 19);
                int startMin = mActivity.mPrefs.getInt("pref_night_start_min", 0);
                int endHour = mActivity.mPrefs.getInt("pref_night_end", 7);
                int endMin = mActivity.mPrefs.getInt("pref_night_end_min", 0);

                // Simple hour picker using TimePickerDialog for start and end consecutively
                android.app.TimePickerDialog startDialog = new android.app.TimePickerDialog(
                        mActivity,
                        (view, hourOfDay, minute) -> {
                            mActivity.mPrefs.edit()
                                    .putInt("pref_night_start", hourOfDay)
                                    .putInt("pref_night_start_min", minute)
                                    .apply();
                            tvNightStart.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute));

                            android.app.TimePickerDialog endDialog = new android.app.TimePickerDialog(
                                    mActivity,
                                    (view2, endHourOfDay, endMinute) -> {
                                        mActivity.mPrefs.edit()
                                                .putInt("pref_night_end", endHourOfDay)
                                                .putInt("pref_night_end_min", endMinute)
                                                .apply();
                                        tvNightEnd.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", endHourOfDay, endMinute));

                                        mActivity.showToast(mActivity.getString(
                                                R.string.night_schedule_updated,
                                                tvNightStart.getText(),
                                                tvNightEnd.getText()
                                        ));
                                        // Reaplicar modo noche con nuevo horario
                                        mActivity.checkAndApplyNightMode();
                                    },
                                    endHour,
                                    endMin,
                                    true
                            );
                            endDialog.show();
                        },
                        startHour,
                        startMin,
                        true
                );
                startDialog.show();
            };

            rowNightSchedule.setOnClickListener(editSchedule);
            tvNightStart.setOnClickListener(editSchedule);
            tvNightEnd.setOnClickListener(editSchedule);
        }

        if (swNightLogos != null) {
            boolean nightLogos = mActivity.mPrefs.getBoolean("pref_night_logos", true);
            swNightLogos.setChecked(nightLogos);

            swNightLogos.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_night_logos", checked).apply();
                mActivity.showToast(checked ? "Teñir logos en modo noche" : "Logos sin tinte en modo noche");

                // Aplicar inmediatamente el cambio sobre el estado visual actual
                if (mActivity.mNightModeManager != null) {
                    boolean autoNight = mActivity.mPrefs.getBoolean("pref_night_mode_auto", false);
                    boolean isNightTime = mActivity.mNightModeManager.isNightTime();
                    boolean isNightSkin = mActivity.mThemeManager != null
                            && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE;

                    if (isNightSkin || (autoNight && isNightTime)) {
                        mActivity.mNightModeManager.applyNightModeColors(mActivity.mLastFreq);
                    } else {
                        mActivity.mNightModeManager.resetNightModeColors(mActivity.mLastFreq);
                    }
                }
            });
        }

        /*
        if (swAm != null) {
            swAm.setChecked(mActivity.mPrefs.getBoolean("pref_enable_am", true));
            swAm.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_enable_am", checked).apply();
                // Csaba: Si se desactiva AM estando en AM, saltar a FM para evitar cuelgues
                if (!checked && (mActivity.mCurrentBand == 3 || mActivity.mCurrentBand == 4)) {
                    if (mActivity.mEngine != null) {
                        if (mActivity.mEngine.getEngineName().equals("MTK8259_8667")) {
                            mActivity.mEngine.toggleRdsFeature(99);
                        } else {
                            mActivity.mEngine.bandCycle(); // Forzar salto fuera de AM
                        }
                    }
                }
            });
        }
        */

        if (swHistory != null) {
            swHistory.setChecked(mActivity.mPrefs.getBoolean("pref_save_history", true));
            swHistory.setOnCheckedChangeListener(
                    (bv, checked) -> mActivity.mPrefs.edit().putBoolean("pref_save_history", checked).apply());
        }

        if (swCloudContrib != null) {
            swCloudContrib.setChecked(mActivity.mPrefs.getBoolean("pref_cloud_contrib", true));
            swCloudContrib.setOnCheckedChangeListener((v, isChecked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_cloud_contrib", isChecked).apply();
                mActivity.showStyledToast(isChecked ? "Contribución activada" : "Contribución desactivada");
            });
        }

        // Logo Mode Row (V18.5)
        View rowLogoMode = dialog.findViewById(R.id.rowLogoMode);
        TextView tvCurrentLogoMode = dialog.findViewById(R.id.tvCurrentLogoMode);
        if (tvCurrentLogoMode != null) {
            int logoMode = mActivity.mPrefs.getInt("pref_logo_mode", 0); // 0=Car, 1=Clock
            tvCurrentLogoMode.setText(logoMode == 0 ? mActivity.getString(R.string.logo_mode_car) : mActivity.getString(R.string.logo_mode_clock));
        }
        if (rowLogoMode != null) {
            rowLogoMode.setOnClickListener(v -> {
                showLogoModeSelector();
                dialog.dismiss();
            });
        }

        // Engine Row Logic
        View rowEngine = dialog.findViewById(R.id.rowEngine);
        TextView tvCurrentEngine = dialog.findViewById(R.id.tvCurrentEngine);
        if (tvCurrentEngine != null) {
            int engineIdx = mActivity.mPrefs.getInt("pref_radio_engine", 0);
            String[] engineNames = {
                    mActivity.getString(R.string.engine_auto),
                    mActivity.getString(R.string.engine_k706),
                    mActivity.getString(R.string.engine_qs6),
                    mActivity.getString(R.string.engine_mt8163),
                    mActivity.getString(R.string.engine_mtk),
                    mActivity.getString(R.string.engine_ts),
                    mActivity.getString(R.string.engine_mtk8259), // V18.6
                    mActivity.getString(R.string.engine_standard)
            };
            if (engineIdx >= 0 && engineIdx < engineNames.length) {
                tvCurrentEngine.setText(engineNames[engineIdx]);
            }
        }
        if (rowEngine != null) {
            rowEngine.setOnClickListener(v -> {
                showEngineSelector();
                dialog.dismiss();
            });
        }

        // Steering NEXT/PREV row
        View rowSteeringMode = dialog.findViewById(R.id.rowSteeringMode);
        TextView tvCurrentSteeringMode = dialog.findViewById(R.id.tvCurrentSteeringMode);
        if (tvCurrentSteeringMode != null) {
            int mode = mActivity.mPrefs.getInt("pref_steering_next_prev_mode", 0); // 0=seek, 1=preset
            tvCurrentSteeringMode.setText(mode == 1
                    ? mActivity.getString(R.string.steering_mode_preset)
                    : mActivity.getString(R.string.steering_mode_seek));
        }
        if (rowSteeringMode != null) {
            rowSteeringMode.setOnClickListener(v -> {
                showSteeringModeSelector();
                dialog.dismiss();
            });
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
        View btnAcknowledgements = dialog.findViewById(R.id.btnAcknowledgements);
        if (btnAcknowledgements != null) {
            btnAcknowledgements.setOnClickListener(v -> showAcknowledgementsDialog());
        }
        dialog.findViewById(R.id.btnCloseSettings).setOnClickListener(v -> dialog.dismiss());

        // V15.6: Aplicar fuente de forma recursiva al diálogo de ajustes usando el
        // gestor de MainActivity
        mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());

        dialog.show();
    }

    /**
     * V18.6: Selector genérico en cuadrícula de 2 columnas para unificar la UI
     */
    private void showGridSelector(String title, String[] options, int currentIndex, java.util.function.Consumer<Integer> onSelect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View dialogView = inflater.inflate(R.layout.dialog_language_selector, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvSelectTitle);
        if (tvTitle != null) tvTitle.setText(title);

        android.widget.GridView gvOptions = dialogView.findViewById(R.id.gvOptions);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancelSelect);

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(mActivity, R.layout.item_language, options) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                if (position == currentIndex) {
                    tv.setBackgroundResource(R.drawable.bg_glass_card_blue);
                    boolean isLight = (mActivity.mThemeManager != null
                            && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
                    tv.setTextColor(isLight ? Color.BLACK : Color.WHITE);
                } else {
                    boolean isLight = (mActivity.mThemeManager != null
                            && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
                    tv.setTextColor(Color.parseColor(isLight ? "#BB000000" : "#BBFFFFFF"));
                }
                return tv;
            }
        };

        if (gvOptions != null) {
            gvOptions.setAdapter(adapter);
            gvOptions.setOnItemClickListener((parent, view, position, id) -> {
                onSelect.accept(position);
                dialog.dismiss();
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.8f);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
    }

    public void showThemeSelector(android.app.Dialog parentDialog, View colorPreview, TextView fontPreview) {
        com.example.openradiofm.ui.theme.ThemeManager.Skin[] allSkins = com.example.openradiofm.ui.theme.ThemeManager.Skin.values();
        java.util.ArrayList<com.example.openradiofm.ui.theme.ThemeManager.Skin> skinValuesList = new java.util.ArrayList<>();
        java.util.ArrayList<String> skinsList = new java.util.ArrayList<>();
        for (com.example.openradiofm.ui.theme.ThemeManager.Skin s : allSkins) {
            // CLEAR desactivado por ahora: no mostrar en selector
            if (s == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR) continue;
            skinValuesList.add(s);
            skinsList.add(s.displayName);
        }
        com.example.openradiofm.ui.theme.ThemeManager.Skin[] skinValues = skinValuesList.toArray(new com.example.openradiofm.ui.theme.ThemeManager.Skin[0]);
        String[] skins = skinsList.toArray(new String[0]);

        int currentSkinIdx = 0;
        com.example.openradiofm.ui.theme.ThemeManager.Skin current = mActivity.mThemeManager.getCurrentSkin();
        for (int i = 0; i < skinValues.length; i++) {
            if (skinValues[i] == current) {
                currentSkinIdx = i;
                break;
            }
        }

        showGridSelector(mActivity.getString(R.string.select_skin), skins, currentSkinIdx, w -> {
            if (w < skinValues.length) {
                mActivity.mThemeManager.setSkin(skinValues[w]);
                mActivity.applySkin(skinValues[w]);
                updateSettingsPreviews(colorPreview, fontPreview);
            }
        });
    }

    public void showFontSelector(android.app.Dialog parentDialog, TextView fontPreview) {
        String[] fonts = { mActivity.getString(R.string.font_default), mActivity.getString(R.string.font_bebas),
                mActivity.getString(R.string.font_digital), mActivity.getString(R.string.font_modern),
                mActivity.getString(R.string.font_orbitron), mActivity.getString(R.string.font_formula1) };
        int currentFontIdx = mActivity.mPrefs.getInt("pref_font_type", 0);

        showGridSelector(mActivity.getString(R.string.select_typography), fonts, currentFontIdx, w -> {
            mActivity.mPrefs.edit().putInt("pref_font_type", w).apply();
            mActivity.applyFonts();
            updateSettingsPreviews(null, fontPreview);
        });
    }

    public void showBackgroundSelector(android.app.Dialog parentDialog, TextView tvStatus) {
        String[] modes = { mActivity.getString(R.string.bg_pure_black), mActivity.getString(R.string.bg_fixed_image),
                mActivity.getString(R.string.bg_dynamic_logo) };
        int currentBgIdx = mActivity.mPrefs.getInt("pref_bg_mode", 1);

        showGridSelector(mActivity.getString(R.string.select_bg_mode), modes, currentBgIdx, w -> {
            mActivity.mPrefs.edit().putInt("pref_bg_mode", w).apply();
            mActivity.mLogoManager.loadCustomBackground();
            mActivity.mLogoManager.loadCarLogo();
            mActivity.mLogoManager.updateDynamicBackground(mActivity.mLastLogoUrl);
            if (tvStatus != null)
                tvStatus.setText(modes[w]);
        });
    }

    public void showNewLanguageSelector() {
        String[] languages = {
                "Español (ES)", "English (EN)", "Français (FR)", "Deutsch (DE)",
                "Português (PT)", "Italiano (IT)", "Русский (RU)", "Română (RO)",
                "Українська (UK)", "Srpski (SR)", "中文 (ZH)", "日本語 (JA)", "Magyar (HU)"
        };
        String[] codes = { "es", "en", "fr", "de", "pt", "it", "ru", "ro", "uk", "sr", "zh", "ja", "hu" };
        String currentLang = mActivity.mPrefs.getString("app_language", "es");
        int currentIndex = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(currentLang)) {
                currentIndex = i;
                break;
            }
        }

        showGridSelector(mActivity.getString(R.string.select_language), languages, currentIndex, w -> {
            String selectedLang = codes[w];
            String selectedLangName = languages[w];
            mActivity.mPrefs.edit().putString("app_language", selectedLang).apply();
            mActivity.showStyledToast(String.format(mActivity.getString(R.string.language_changed), selectedLangName));
            mActivity.recreate();
        });
    }

    private void applyPremiumListStyle(AlertDialog dialog) {
        if (dialog == null)
            return;
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.bg_submenu_box));
            window.setDimAmount(0.6f);
        }

        // V15.6: Aplicar fuente del sistema a la vista raíz del diálogo usando
        // MainActivity
        mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());
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
                String versionName = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(),
                        0).versionName;
                tvVersion.setText(mActivity.getString(R.string.version, versionName));
            }
        } catch (Exception ignored) {
        }

        // Créditos con Links
        TextView tvIcons8 = dialog.findViewById(R.id.tvIcons8Credit);
        TextView tvTdtchannels = dialog.findViewById(R.id.tvTdtchannelsCredit);

        if (tvIcons8 != null) {
            String text = "Icons by <a href='https://icons8.com/'>Icons8</a>";
            applyHtmlLink(tvIcons8, text);
        }

        if (tvTdtchannels != null) {
            String text = "Streaming & Logos by <a href='https://www.tdtchannels.com'>TDTchannels</a>";
            applyHtmlLink(tvTdtchannels, text);
        }

        View ivAboutLogo = dialog.findViewById(R.id.ivAboutAppLogo);
        if (ivAboutLogo != null) {
            ivAboutLogo.setOnClickListener(v -> {
                dialog.dismiss();
                showCreditsDialog();
            });
        }

        View btnClose = dialog.findViewById(R.id.btnClose);
        if (btnClose != null)
            btnClose.setOnClickListener(v -> dialog.dismiss());

        // V15.6: Aplicar fuente recursiva al diálogo About usando MainActivity
        mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());

        dialog.show();
    }

    public void showAcknowledgementsDialog() {
        Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_acknowledgements);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        TextView tvLink = dialog.findViewById(R.id.tvRadioAndroidLink);
        if (tvLink != null) {
            tvLink.setOnClickListener(v -> showRadioAndroidQrDialog());
        }

        View btnClose = dialog.findViewById(R.id.btnCloseAcknowledgements);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());
        dialog.show();
    }

    public void showRadioAndroidQrDialog() {
        Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_radio_android_qr);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.8f);
        }

        View btnClose = dialog.findViewById(R.id.btnCloseQr);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());
        dialog.show();
    }

    private void applyHtmlLink(TextView textView, String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(html));
        }
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void showEngineSelector() {
        String[] options = {
                mActivity.getString(R.string.engine_auto),
                mActivity.getString(R.string.engine_k706),
                mActivity.getString(R.string.engine_qs6),
                mActivity.getString(R.string.engine_mt8163),
                mActivity.getString(R.string.engine_mtk),
                mActivity.getString(R.string.engine_ts),
                mActivity.getString(R.string.engine_mtk8259),
                mActivity.getString(R.string.engine_standard)
        };
        int currentIdx = mActivity.mPrefs.getInt("pref_radio_engine", 0);

        showGridSelector(mActivity.getString(R.string.radio_engine), options, currentIdx, which -> {
            mActivity.mPrefs.edit().putInt("pref_radio_engine", which).apply();
            mActivity.showToast("Motor cambiado: " + options[which]);
            mActivity.mServiceController.start();
        });
    }

    public void showLogoModeSelector() {
        String[] options = {
                mActivity.getString(R.string.logo_mode_car),
                mActivity.getString(R.string.logo_mode_clock)
        };
        int currentIdx = mActivity.mPrefs.getInt("pref_logo_mode", 0);

        showGridSelector(mActivity.getString(R.string.logo_mode_label), options, currentIdx, which -> {
            mActivity.mPrefs.edit().putInt("pref_logo_mode", which).apply();
            mActivity.showToast("Modo de logo: " + options[which]);
            
            // V18.5: Aplicar cambio inmediatamente
            mActivity.applyLogoModePreference();
            
            // Reabrir ajustes
            showPremiumSettingsDialog();
        });
    }

    public void showLogoProviderSelector() {
        String[] options = {
                mActivity.getString(R.string.provider_supabase),
                mActivity.getString(R.string.provider_radiobrowser),
                mActivity.getString(R.string.provider_both)
        };
        int currentIdx = mActivity.mPrefs.getInt("pref_logo_provider", 0);

        showGridSelector(mActivity.getString(R.string.logo_provider), options, currentIdx, which -> {
            mActivity.mPrefs.edit().putInt("pref_logo_provider", which).apply();
            mActivity.showToast("Proveedor de logos: " + options[which]);
            // Reabrir ajustes para ver el cambio (opcional)
            showPremiumSettingsDialog();
        });
    }

    public void showSteeringModeSelector() {
        String[] options = {
                mActivity.getString(R.string.steering_mode_seek),
                mActivity.getString(R.string.steering_mode_preset)
        };
        int currentIdx = mActivity.mPrefs.getInt("pref_steering_next_prev_mode", 0);
        showGridSelector(mActivity.getString(R.string.select_steering_mode), options, currentIdx, which -> {
            mActivity.mPrefs.edit().putInt("pref_steering_next_prev_mode", which).apply();
            mActivity.showToast("Mandos volante NEXT/PREV: " + options[which]);
            showPremiumSettingsDialog();
        });
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
            if (mActivity.mHistoryManager != null) {
                mActivity.mHistoryManager.deleteAllFavorites();
            }
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnClearHistory).setOnClickListener(v -> {
            if (mActivity.mHistoryManager != null) {
                mActivity.mHistoryManager.clearHistory();
                mActivity.showToast("El historial ha sido borrado");
            }
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        // V15.6: Aplicar fuente recursiva al diálogo Save/Load usando MainActivity
        mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());

        dialog.show();
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
            int color = mActivity.mThemeManager.getAccentColor();
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
        } catch (Exception ignored) {
        }
        tv.setTypeface(typeface);
    }
}
