package com.example.openradiofm.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.text.method.LinkMovementMethod;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;

import com.example.openradiofm.R;
import com.example.openradiofm.util.HiHackBootReminder;

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

        builder.setPositiveButton(mActivity.getString(R.string.dialog_btn_save), (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                // V16.4: Usar setCustomName (CUSTOM_) en vez de saveRdsName (RDS_)
                mActivity.mRepository.setCustomName(currentFreq, newName);
                mActivity.showToast(mActivity.getString(R.string.toast_station_name_saved, newName));

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

        builder.setNegativeButton(mActivity.getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.setNeutralButton(mActivity.getString(R.string.dialog_btn_restore_original), (dialog, which) -> {
            // V16.4: Limpiar nombre custom
            mActivity.mRepository.setCustomName(currentFreq, null);
            mActivity.showToast(mActivity.getString(R.string.toast_station_name_restored));

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
        TextView tvThemeSummary = dialog.findViewById(R.id.tvThemeSummary);
        TextView tvFontNameSummary = dialog.findViewById(R.id.tvFontNameSummary);
        TextView tvBackgroundStatus = dialog.findViewById(R.id.tvBackgroundStatus);
        View rowA11yHijack = dialog.findViewById(R.id.rowA11yHijack);
        TextView tvA11yHijackStatus = dialog.findViewById(R.id.tvA11yHijackStatus);

        TextView tvSummaryReliefHd = dialog.findViewById(R.id.tvSummaryReliefHd);
        TextView tvSummaryNightMode = dialog.findViewById(R.id.tvSummaryNightMode);
        TextView tvSummaryNightLogos = dialog.findViewById(R.id.tvSummaryNightLogos);
        TextView tvSummaryStatusBar = dialog.findViewById(R.id.tvSummaryStatusBar);
        TextView tvSummaryAutoHide = dialog.findViewById(R.id.tvSummaryAutoHide);
        TextView tvSummaryLogosOnline = dialog.findViewById(R.id.tvSummaryLogosOnline);
        TextView tvSummaryCloudContrib = dialog.findViewById(R.id.tvSummaryCloudContrib);
        TextView tvSummarySaveHistory = dialog.findViewById(R.id.tvSummarySaveHistory);

        // Night schedule views
        View rowNightSchedule = dialog.findViewById(R.id.rowNightSchedule);
        TextView tvNightStart = dialog.findViewById(R.id.tvNightStart);
        TextView tvNightEnd = dialog.findViewById(R.id.tvNightEnd);
        TextView tvNightScheduleSummary = dialog.findViewById(R.id.tvNightScheduleSummary);

        androidx.appcompat.widget.SwitchCompat swLogosOnline = dialog.findViewById(R.id.switchLogosOnline);
        androidx.appcompat.widget.SwitchCompat swReliefHd = dialog.findViewById(R.id.switchReliefHd);
        View rowReliefHd = dialog.findViewById(R.id.rowReliefHd);
        androidx.appcompat.widget.SwitchCompat swNight = dialog.findViewById(R.id.switchNightMode);
        androidx.appcompat.widget.SwitchCompat swNightLogos = dialog.findViewById(R.id.switchNightLogos);
        androidx.appcompat.widget.SwitchCompat swHistory = dialog.findViewById(R.id.switchSaveHistory);
        androidx.appcompat.widget.SwitchCompat swCloudContrib = dialog.findViewById(R.id.switchCloudContrib);
        androidx.appcompat.widget.SwitchCompat swStatusBar = dialog.findViewById(R.id.swStatusBar);
        androidx.appcompat.widget.SwitchCompat swAutoHide = dialog.findViewById(R.id.swAutoHideControls);
        androidx.appcompat.widget.SwitchCompat swPresetScrollLoop = dialog.findViewById(R.id.swPresetScrollLoop);
        TextView tvSummaryPresetScrollLoop = dialog.findViewById(R.id.tvSummaryPresetScrollLoop);
        View rowSignalMeterBars = dialog.findViewById(R.id.rowSignalMeterBars);
        androidx.appcompat.widget.SwitchCompat swSignalMeterBars = dialog.findViewById(R.id.swSignalMeterBars);
        TextView tvSummarySignalMeterBars = dialog.findViewById(R.id.tvSummarySignalMeterBars);
        androidx.appcompat.widget.SwitchCompat swHihackBootReminder = dialog.findViewById(R.id.switchHihackBootReminder);
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

        // Country Row (under language)
        View rowCountry = dialog.findViewById(R.id.rowCountry);
        TextView tvCurrentCountry = dialog.findViewById(R.id.tvCurrentCountry);
        if (tvCurrentCountry != null) {
            String cc = com.example.openradiofm.utils.CountryPrefs.getCountry(mActivity);
            tvCurrentCountry.setText(mActivity.countryLabelForCode(cc));
        }
        if (rowCountry != null) {
            rowCountry.setOnClickListener(v -> {
                mActivity.showCountrySelectorDialog(false);
                dialog.dismiss();
            });
        }

        // HiHack (Accessibility) status + shortcut
        if (rowA11yHijack != null && tvA11yHijackStatus != null) {
            boolean enabled = MainActivity.isFactoryRadioHijackerAccessibilityEnabled(mActivity);
            String state = enabled ? mActivity.getString(R.string.a11y_hihack_enabled)
                    : mActivity.getString(R.string.a11y_hihack_disabled);
            tvA11yHijackStatus.setText(state + " • " + mActivity.getString(R.string.a11y_hihack_open_settings));
            tvA11yHijackStatus.setTextColor(enabled ? Color.parseColor("#44FF44") : Color.parseColor("#FF4444"));
            rowA11yHijack.setOnClickListener(v -> {
                try {
                    Intent i = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(i);
                } catch (Exception e) {
                    mActivity.showStyledToast(mActivity.getString(R.string.error_opening_settings));
                }
            });
        }
        if (swHihackBootReminder != null) {
            swHihackBootReminder.setChecked(mActivity.mPrefs.getBoolean(HiHackBootReminder.PREF_BOOT_REMINDER, true));
            swHihackBootReminder.setOnCheckedChangeListener((bv, checked) ->
                    mActivity.mPrefs.edit().putBoolean(HiHackBootReminder.PREF_BOOT_REMINDER, checked).apply());
        }

        // Previews
        updateSettingsPreviews(viewColorPreview, tvFontPreview);
        fillThemeSummary(tvThemeSummary);
        fillFontNameSummary(tvFontNameSummary);

        // Night schedule initial text from prefs
        if (tvNightStart != null && tvNightEnd != null) {
            int startHour = mActivity.mPrefs.getInt("pref_night_start", 19);
            int startMin = mActivity.mPrefs.getInt("pref_night_start_min", 0);
            int endHour = mActivity.mPrefs.getInt("pref_night_end", 7);
            int endMin = mActivity.mPrefs.getInt("pref_night_end_min", 0);
            tvNightStart.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", startHour, startMin));
            tvNightEnd.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", endHour, endMin));
            updateNightScheduleSummary(tvNightScheduleSummary, tvNightStart, tvNightEnd);
        }

        // V19.7: Indicador de Conexión a Supabase (Automático)
        TextView tvSupabaseStatus = dialog.findViewById(R.id.tvSupabaseStatus);
        if (tvSupabaseStatus != null) {
            tvSupabaseStatus.setVisibility(View.VISIBLE);
            tvSupabaseStatus.setText(mActivity.getString(R.string.supabase_status_connecting));
            tvSupabaseStatus.setTextColor(Color.parseColor("#888888"));

            mActivity.mRepository.getSupabaseSource().checkConnection(connected -> {
                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed() || !dialog.isShowing()) return;
                    if (connected) {
                        tvSupabaseStatus.setText(mActivity.getString(R.string.supabase_status_online));
                        tvSupabaseStatus.setTextColor(Color.parseColor("#44FF44")); // Verde
                    } else {
                        tvSupabaseStatus.setText(mActivity.getString(R.string.supabase_status_offline));
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
            bindSwitchSummary(tvSummaryLogosOnline, swLogosOnline.isChecked());
            swLogosOnline.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_logos_online", checked).apply();
                bindSwitchSummary(tvSummaryLogosOnline, checked);
                if (checked) {
                    mActivity.showToast(mActivity.getString(R.string.toast_logos_online_on_1));
                    mActivity.showToast(mActivity.getString(R.string.toast_logos_online_on_2));
                } else {
                    mActivity.showToast(mActivity.getString(R.string.toast_logos_online_off));
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
            bindSwitchSummary(tvSummaryStatusBar, swStatusBar.isChecked());
            swStatusBar.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_show_status_bar_v2", checked).apply();
                bindSwitchSummary(tvSummaryStatusBar, checked);
                mActivity.showToast(checked ? mActivity.getString(R.string.status_bar_enabled)
                        : mActivity.getString(R.string.status_bar_disabled));
                mActivity.applyStatusBarVisibility();
            });
        }

        if (swAutoHide != null) {
            swAutoHide.setChecked(mActivity.mPrefs.getBoolean("pref_auto_hide_controls", false));
            bindSwitchSummary(tvSummaryAutoHide, swAutoHide.isChecked());
            swAutoHide.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_auto_hide_controls", checked).apply();
                bindSwitchSummary(tvSummaryAutoHide, checked);
                mActivity.showToast(checked ? mActivity.getString(R.string.auto_hide_enabled)
                        : mActivity.getString(R.string.auto_hide_disabled));
                // V18.6: Reiniciar el temporizador si se activa
                if (checked) mActivity.resetAutoHideTimer();
                else mActivity.showBottomControls();
            });
        }

        if (swPresetScrollLoop != null) {
            swPresetScrollLoop.setChecked(mActivity.mPrefs.getBoolean("pref_preset_scroll_loop", false));
            bindSwitchSummary(tvSummaryPresetScrollLoop, swPresetScrollLoop.isChecked());
            swPresetScrollLoop.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_preset_scroll_loop", checked).apply();
                bindSwitchSummary(tvSummaryPresetScrollLoop, checked);
                mActivity.showToast(mActivity.getString(R.string.preset_scroll_loop_recreate_hint));
                dialog.dismiss();
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(mActivity::recreate, 280);
            });
        }

        if (rowSignalMeterBars != null) {
            rowSignalMeterBars.setVisibility(mActivity.mIsSimpleLayout ? View.GONE : View.VISIBLE);
        }
        if (swSignalMeterBars != null) {
            swSignalMeterBars.setChecked(mActivity.mPrefs.getBoolean(SignalMeterCoordinator.PREF_USE_BARS, false));
            bindSwitchSummary(tvSummarySignalMeterBars, swSignalMeterBars.isChecked());
            swSignalMeterBars.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean(SignalMeterCoordinator.PREF_USE_BARS, checked).apply();
                bindSwitchSummary(tvSummarySignalMeterBars, checked);
                mActivity.applySignalMeterPreferenceFromSettings();
            });
        }

        // Dev kill-switch: ocultar opción de relieve si se deshabilita desde Engineering.
        boolean devReliefUiEnabled = true;
        try {
            devReliefUiEnabled = mActivity.mPrefs.getBoolean("pref_dev_relief_hd_enabled", true);
        } catch (Exception ignored) {}
        if (rowReliefHd != null) {
            rowReliefHd.setVisibility(devReliefUiEnabled ? View.VISIBLE : View.GONE);
        }
        if (swReliefHd != null) {
            if (!devReliefUiEnabled) {
                try {
                    // Forzar estado OFF si se ocultó la opción (evita dejar el relieve “pegado”).
                    mActivity.mPrefs.edit().putBoolean("pref_relief_hd", false).apply();
                    mActivity.applyReliefHd(false);
                } catch (Exception ignored) {}
                swReliefHd.setChecked(false);
                swReliefHd.setEnabled(false);
                bindSwitchSummary(tvSummaryReliefHd, false);
            }
            boolean reliefEnabled = mActivity.mPrefs.getBoolean("pref_relief_hd", false);
            swReliefHd.setChecked(reliefEnabled);
            bindSwitchSummary(tvSummaryReliefHd, reliefEnabled);
            swReliefHd.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_relief_hd", checked).apply();
                bindSwitchSummary(tvSummaryReliefHd, checked);
                mActivity.applyReliefHd(checked);
                mActivity.showToast(checked ? mActivity.getString(R.string.toast_relief_hd_on)
                        : mActivity.getString(R.string.toast_relief_hd_off));
            });
        }

        if (swNight != null) {
            boolean nightEnabled = mActivity.mPrefs.getBoolean("pref_night_mode_auto", false);
            swNight.setChecked(nightEnabled);
            bindSwitchSummary(tvSummaryNightMode, nightEnabled);
            if (rowNightSchedule != null)
                rowNightSchedule.setVisibility(nightEnabled ? View.VISIBLE : View.GONE);
            swNight.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_night_mode_auto", checked).apply();
                bindSwitchSummary(tvSummaryNightMode, checked);
                if (rowNightSchedule != null)
                    rowNightSchedule.setVisibility(checked ? View.VISIBLE : View.GONE);
                if (checked) {
                    mActivity.checkAndApplyNightMode();
                    mActivity.showToast(mActivity.getString(R.string.toast_night_mode_auto_on));
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
                            updateNightScheduleSummary(tvNightScheduleSummary, tvNightStart, tvNightEnd);

                            android.app.TimePickerDialog endDialog = new android.app.TimePickerDialog(
                                    mActivity,
                                    (view2, endHourOfDay, endMinute) -> {
                                        mActivity.mPrefs.edit()
                                                .putInt("pref_night_end", endHourOfDay)
                                                .putInt("pref_night_end_min", endMinute)
                                                .apply();
                                        tvNightEnd.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", endHourOfDay, endMinute));
                                        updateNightScheduleSummary(tvNightScheduleSummary, tvNightStart, tvNightEnd);

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
            bindSwitchSummary(tvSummaryNightLogos, nightLogos);

            swNightLogos.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_night_logos", checked).apply();
                bindSwitchSummary(tvSummaryNightLogos, checked);
                mActivity.showToast(checked ? mActivity.getString(R.string.toast_night_logos_tint_on)
                        : mActivity.getString(R.string.toast_night_logos_tint_off));

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
                try { mActivity.refreshDataActivityIndicator(); } catch (Exception ignored) {}
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
            bindSwitchSummary(tvSummarySaveHistory, swHistory.isChecked());
            swHistory.setOnCheckedChangeListener((bv, checked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_save_history", checked).apply();
                bindSwitchSummary(tvSummarySaveHistory, checked);
            });
        }

        if (swCloudContrib != null) {
            swCloudContrib.setChecked(mActivity.mPrefs.getBoolean("pref_cloud_contrib", true));
            bindSwitchSummary(tvSummaryCloudContrib, swCloudContrib.isChecked());
            swCloudContrib.setOnCheckedChangeListener((v, isChecked) -> {
                mActivity.mPrefs.edit().putBoolean("pref_cloud_contrib", isChecked).apply();
                bindSwitchSummary(tvSummaryCloudContrib, isChecked);
                mActivity.showStyledToast(isChecked ? mActivity.getString(R.string.toast_contrib_on)
                        : mActivity.getString(R.string.toast_contrib_off));
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

        // Layout V2 (vertical): lado de presets
        View rowLayout2Side = dialog.findViewById(R.id.rowLayout2Side);
        TextView tvCurrentLayout2Side = dialog.findViewById(R.id.tvCurrentLayout2Side);
        if (tvCurrentLayout2Side != null) {
            boolean presetsRight = mActivity.mPrefs.getBoolean("pref_layout2_presets_right", false);
            tvCurrentLayout2Side.setText(presetsRight
                    ? mActivity.getString(R.string.layout2_side_presets_right)
                    : mActivity.getString(R.string.layout2_side_presets_left));
        }
        if (rowLayout2Side != null) {
            rowLayout2Side.setOnClickListener(v -> {
                showLayout2SideSelector();
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
                    mActivity.getString(R.string.engine_standard),
                    mActivity.getString(R.string.engine_jancar)
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

        // Icon pack row
        View rowIconPack = dialog.findViewById(R.id.rowIconPack);
        TextView tvCurrentIconPack = dialog.findViewById(R.id.tvCurrentIconPack);
        if (tvCurrentIconPack != null) {
            int pack = mActivity.mPrefs.getInt("pref_icon_pack", 0);
            String label;
            if (pack == 1) {
                label = mActivity.getString(R.string.icon_pack_color);
            } else if (pack == 2) {
                label = mActivity.getString(R.string.icon_pack_google);
            } else if (pack == 3) {
                label = mActivity.getString(R.string.icon_pack_lucide);
            } else if (pack == 4) {
                label = mActivity.getString(R.string.icon_pack_remix);
            } else if (pack == 5) {
                label = mActivity.getString(R.string.icon_pack_awesome);
            } else if (pack == 6) {
                label = mActivity.getString(R.string.icon_pack_tabler);
            } else {
                label = mActivity.getString(R.string.icon_pack_default);
            }
            tvCurrentIconPack.setText(label);
        }
        if (rowIconPack != null) {
            rowIconPack.setOnClickListener(v -> {
                showIconPackSelector();
                dialog.dismiss();
            });
        }

        // Preset numbers row (1..18)
        View rowPresetNumbers = dialog.findViewById(R.id.rowPresetNumbers);
        TextView tvCurrentPresetNumbers = dialog.findViewById(R.id.tvCurrentPresetNumbers);
        if (tvCurrentPresetNumbers != null) {
            int style = mActivity.mPrefs.getInt(MainActivity.PREF_PRESET_NUMBERS_STYLE, 0);
            tvCurrentPresetNumbers.setText(style == 1
                    ? mActivity.getString(R.string.preset_numbers_tabler)
                    : mActivity.getString(R.string.preset_numbers_default));
        }
        if (rowPresetNumbers != null) {
            rowPresetNumbers.setOnClickListener(v -> {
                showPresetNumbersSelector();
                dialog.dismiss();
            });
        }

        cardTheme.setOnClickListener(v -> {
            if (mActivity.mPrefs.getBoolean("pref_night_mode_auto", false)) {
                mActivity.showToast(mActivity.getString(R.string.night_mode_manual_blocked));
                return;
            }
            showThemeSelector(dialog, viewColorPreview, tvFontPreview, tvThemeSummary);
        });
        cardFonts.setOnClickListener(v -> showFontSelector(dialog, tvFontPreview, tvFontNameSummary));
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

        View rootCard = dialogView.findViewById(R.id.grid_selector_root);
        if (rootCard != null) {
            try {
                rootCard.setBackgroundResource(mActivity.getSkinDrawableId());
            } catch (Exception ignored) {
            }
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvSelectTitle);
        if (tvTitle != null) {
            tvTitle.setText(title);
            try {
                tvTitle.setTypeface(mActivity.getSystemTypeface());
            } catch (Exception ignored) {
            }
        }

        android.widget.GridView gvOptions = dialogView.findViewById(R.id.gvOptions);
        View btnCancel = dialogView.findViewById(R.id.btnCancelSelect);

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(mActivity, R.layout.item_language, options) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                try {
                    tv.setTypeface(mActivity.getSystemTypeface());
                } catch (Exception ignored) {
                }
                if (position == currentIndex) {
                    tv.setBackgroundResource(R.drawable.bg_glass_card_blue);
                    boolean isLight = (mActivity.mThemeManager != null
                            && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
                    tv.setTextColor(isLight ? Color.BLACK : Color.WHITE);
                } else {
                    tv.setBackgroundResource(R.drawable.bg_submenu_box);
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
            window.setDimAmount(0.7f);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();

        try {
            mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());
        } catch (Exception ignored) {
        }
    }

    public void showThemeSelector(android.app.Dialog parentDialog, View colorPreview, TextView fontPreview,
            TextView themeSummary) {
        com.example.openradiofm.ui.theme.ThemeManager.Skin[] allSkins = com.example.openradiofm.ui.theme.ThemeManager.Skin.values();
        java.util.ArrayList<com.example.openradiofm.ui.theme.ThemeManager.Skin> skinValuesList = new java.util.ArrayList<>();
        java.util.ArrayList<String> skinsList = new java.util.ArrayList<>();
        for (com.example.openradiofm.ui.theme.ThemeManager.Skin s : allSkins) {
            // CLEAR desactivado por ahora: no mostrar en selector
            if (s == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR) continue;
            // Dev kill-switch: ocultar Day Mode si se deshabilitó.
            if (s == com.example.openradiofm.ui.theme.ThemeManager.Skin.DAY_MODE
                    && mActivity != null
                    && mActivity.mPrefs != null
                    && !mActivity.mPrefs.getBoolean("pref_dev_day_mode_enabled", true)) {
                continue;
            }
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
                fillThemeSummary(themeSummary);
            }
        });
    }

    public void showFontSelector(android.app.Dialog parentDialog, TextView fontPreview, TextView fontNameSummary) {
        String[] fonts = { mActivity.getString(R.string.font_default), mActivity.getString(R.string.font_bebas),
                mActivity.getString(R.string.font_digital), mActivity.getString(R.string.font_modern),
                mActivity.getString(R.string.font_orbitron), mActivity.getString(R.string.font_formula1) };
        int currentFontIdx = mActivity.mPrefs.getInt("pref_font_type", 0);

        showGridSelector(mActivity.getString(R.string.select_typography), fonts, currentFontIdx, w -> {
            mActivity.mPrefs.edit().putInt("pref_font_type", w).apply();
            mActivity.applyFonts();
            updateSettingsPreviews(null, fontPreview);
            fillFontNameSummaryAtIndex(fontNameSummary, w);
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

    /** Variante forzada (primer inicio): no se puede cancelar, y el callback decide qué hacer. */
    public void showLanguageSelector(boolean forceChoose, java.util.function.Consumer<String> onLangSelected) {
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
            int idx = Math.max(0, Math.min(w, codes.length - 1));
            if (onLangSelected != null) onLangSelected.accept(codes[idx]);
        }, forceChoose);
    }

    public void showCountrySelector(
            boolean forceChoose,
            String[] labels,
            String[] codes,
            int currentIndex,
            java.util.function.Consumer<String> onSelectedCode
    ) {
        showGridSelector(mActivity.getString(R.string.select_country_title), labels, currentIndex, w -> {
            int idx = Math.max(0, Math.min(w, codes.length - 1));
            if (onSelectedCode != null) onSelectedCode.accept(codes[idx]);
            mActivity.showStyledToast(String.format(mActivity.getString(R.string.country_saved), labels[idx]));
        }, forceChoose);
    }

    private void showGridSelector(String title, String[] options, int currentIndex, java.util.function.Consumer<Integer> onSelect, boolean forceChoose) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View dialogView = inflater.inflate(R.layout.dialog_language_selector, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        View rootCard = dialogView.findViewById(R.id.grid_selector_root);
        if (rootCard != null) {
            try {
                rootCard.setBackgroundResource(mActivity.getSkinDrawableId());
            } catch (Exception ignored) {
            }
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvSelectTitle);
        if (tvTitle != null) {
            tvTitle.setText(title);
            try {
                tvTitle.setTypeface(mActivity.getSystemTypeface());
            } catch (Exception ignored) {
            }
        }

        android.widget.GridView gvOptions = dialogView.findViewById(R.id.gvOptions);
        View btnCancel = dialogView.findViewById(R.id.btnCancelSelect);
        if (btnCancel != null) {
            btnCancel.setVisibility(forceChoose ? View.GONE : View.VISIBLE);
        }

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(mActivity, R.layout.item_language, options) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                try {
                    tv.setTypeface(mActivity.getSystemTypeface());
                } catch (Exception ignored) {
                }
                if (position == currentIndex) {
                    tv.setBackgroundResource(R.drawable.bg_glass_card_blue);
                    boolean isLight = (mActivity.mThemeManager != null
                            && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
                    tv.setTextColor(isLight ? Color.BLACK : Color.WHITE);
                } else {
                    tv.setBackgroundResource(R.drawable.bg_submenu_box);
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

        if (!forceChoose && btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.7f);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(!forceChoose);
        dialog.setCanceledOnTouchOutside(!forceChoose);
        dialog.show();

        try {
            mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());
        } catch (Exception ignored) {
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
        textView.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY));
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
                mActivity.getString(R.string.engine_standard),
                mActivity.getString(R.string.engine_jancar)
        };
        int currentIdx = mActivity.mPrefs.getInt("pref_radio_engine", 0);

        showGridSelector(mActivity.getString(R.string.radio_engine), options, currentIdx, which -> {
            mActivity.mPrefs.edit().putInt("pref_radio_engine", which).apply();
            mActivity.showToast(mActivity.getString(R.string.engine_changed, options[which]));
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
            mActivity.showToast(mActivity.getString(R.string.toast_logo_mode, options[which]));
            
            // V18.5: Aplicar cambio inmediatamente
            mActivity.applyLogoModePreference();
            
            // Reabrir ajustes
            showPremiumSettingsDialog();
        });
    }

    public void showLayout2SideSelector() {
        String[] options = {
                mActivity.getString(R.string.layout2_side_presets_left),
                mActivity.getString(R.string.layout2_side_presets_right)
        };
        int currentIdx = mActivity.mPrefs.getBoolean("pref_layout2_presets_right", false) ? 1 : 0;
        showGridSelector(mActivity.getString(R.string.layout2_side_label), options, currentIdx, which -> {
            boolean presetsRight = which == 1;
            mActivity.mPrefs.edit().putBoolean("pref_layout2_presets_right", presetsRight).apply();
            mActivity.showToast(options[which]);
            try { mActivity.applyLayout2SidePreference(); } catch (Exception ignored) {}
            // Recreate para relayout estable de chains/guidelines en V2.
            mActivity.recreate();
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
            mActivity.showToast(mActivity.getString(R.string.toast_logo_provider, options[which]));
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
            mActivity.showToast(mActivity.getString(R.string.toast_steering_mode, options[which]));
            showPremiumSettingsDialog();
        });
    }

    public void showIconPackSelector() {
        String[] options = {
                mActivity.getString(R.string.icon_pack_default),
                mActivity.getString(R.string.icon_pack_color),
                mActivity.getString(R.string.icon_pack_google),
                mActivity.getString(R.string.icon_pack_lucide),
                mActivity.getString(R.string.icon_pack_remix),
                mActivity.getString(R.string.icon_pack_awesome),
                mActivity.getString(R.string.icon_pack_tabler),
        };
        int currentIdx = mActivity.mPrefs.getInt("pref_icon_pack", 0);
        showGridSelector(mActivity.getString(R.string.select_icon_pack), options, currentIdx, which -> {
            mActivity.mPrefs.edit().putInt("pref_icon_pack", which).apply();
            try { mActivity.applyIconPack(); } catch (Exception ignored) {}
            showPremiumSettingsDialog();
        });
    }

    public void showPresetNumbersSelector() {
        String[] options = {
                mActivity.getString(R.string.preset_numbers_default),
                mActivity.getString(R.string.preset_numbers_tabler),
        };
        int currentIdx = mActivity.mPrefs.getInt(MainActivity.PREF_PRESET_NUMBERS_STYLE, 0);
        showGridSelector(mActivity.getString(R.string.select_preset_numbers), options, currentIdx, which -> {
            mActivity.mPrefs.edit().putInt(MainActivity.PREF_PRESET_NUMBERS_STYLE, which).apply();
            try {
                if (mActivity.mPresetNumberIconManager != null) mActivity.mPresetNumberIconManager.clearCache();
                mActivity.refreshRadioStatus();
            } catch (Exception ignored) {}
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

        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View dialogView = inflater.inflate(R.layout.dialog_station_history, null);

        View rootCard = dialogView.findViewById(R.id.station_history_dialog_root);
        if (rootCard != null) {
            try {
                rootCard.setBackgroundResource(mActivity.getSkinDrawableId());
            } catch (Exception ignored) {
            }
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvStationHistoryTitle);
        if (tvTitle != null) {
            try {
                tvTitle.setTypeface(mActivity.getSystemTypeface());
            } catch (Exception ignored) {
            }
        }

        ListView lv = dialogView.findViewById(R.id.lvStationHistory);
        View btnCancel = dialogView.findViewById(R.id.btnCancelStationHistory);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, R.layout.item_fav_file_row,
                R.id.tvFavFileName, displayNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
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
            lv.setOnItemClickListener((parent, itemView, which, id) -> {
                if (mActivity.mEngine != null) {
                    mActivity.mEngine.tune(Integer.parseInt(freqs[which]));
                }
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
                mActivity.showToast(mActivity.getString(R.string.toast_history_deleted_full));
            }
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        View btnEditor = dialog.findViewById(R.id.btnOpenFavEditor);
        if (btnEditor != null) {
            btnEditor.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(mActivity.getString(R.string.save_load_fav_editor_url)));
                    mActivity.startActivity(intent);
                } catch (Exception e) {
                    Log.w(TAG, "open fav editor url", e);
                }
            });
        }

        // V15.6: Aplicar fuente recursiva al diálogo Save/Load usando MainActivity
        mActivity.applyRecursiveFont(dialog.getWindow().getDecorView(), mActivity.getSystemTypeface());

        dialog.show();
    }

    public void showBackupStateDialog() {
        if (!mActivity.checkStoragePermissions()) {
            mActivity.requestStoragePermissions();
            return;
        }

        Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_backup_state);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        dialog.findViewById(R.id.btnBackupMenuExport).setOnClickListener(v -> {
            if (mActivity.mHistoryManager != null) {
                mActivity.mHistoryManager.saveMenuOptionsToFile();
            }
            dialog.dismiss();
        });
        dialog.findViewById(R.id.btnBackupMenuImport).setOnClickListener(v -> {
            if (mActivity.mHistoryManager != null) {
                mActivity.mHistoryManager.loadMenuOptionsFromFile();
            }
            dialog.dismiss();
        });
        dialog.findViewById(R.id.btnBackupFullExport).setOnClickListener(v -> {
            if (mActivity.mHistoryManager != null) {
                mActivity.mHistoryManager.saveFullBackupToZip();
            }
            dialog.dismiss();
        });
        dialog.findViewById(R.id.btnBackupFullImport).setOnClickListener(v -> {
            if (mActivity.mHistoryManager != null) {
                mActivity.mHistoryManager.loadFullBackupFromZip();
            }
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

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
                        mActivity.showToast(mActivity.getString(R.string.toast_easter_egg));
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

    private void bindSwitchSummary(TextView tv, boolean on) {
        if (tv == null) return;
        tv.setText(on ? mActivity.getString(R.string.settings_value_on)
                : mActivity.getString(R.string.settings_value_off));
    }

    private void fillThemeSummary(TextView tv) {
        if (tv == null || mActivity.mThemeManager == null) return;
        com.example.openradiofm.ui.theme.ThemeManager.Skin s = mActivity.mThemeManager.getCurrentSkin();
        tv.setText(s != null ? s.displayName : "");
    }

    private void fillFontNameSummary(TextView tv) {
        fillFontNameSummaryAtIndex(tv, mActivity.mPrefs.getInt("pref_font_type", 0));
    }

    private void fillFontNameSummaryAtIndex(TextView tv, int idx) {
        if (tv == null) return;
        String[] fonts = {
                mActivity.getString(R.string.font_default),
                mActivity.getString(R.string.font_bebas),
                mActivity.getString(R.string.font_digital),
                mActivity.getString(R.string.font_modern),
                mActivity.getString(R.string.font_orbitron),
                mActivity.getString(R.string.font_formula1)
        };
        if (idx >= 0 && idx < fonts.length) {
            tv.setText(fonts[idx]);
        } else {
            tv.setText(fonts[0]);
        }
    }

    private static void updateNightScheduleSummary(TextView summary, TextView startTv, TextView endTv) {
        if (summary == null || startTv == null || endTv == null) return;
        summary.setText(startTv.getText().toString() + " — " + endTv.getText().toString());
    }
}
