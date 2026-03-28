package com.example.openradiofm.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.core.view.ViewCompat;

import com.example.openradiofm.R;

/**
 * Gestor del panel de control.
 * Encapsula la lógica de interacción de los botones físicos (Seek, Control y Utilidades).
 */
public class ControlPanelManager {

    private static final String TAG = "ControlPanelManager";
    private final MainActivity mActivity;

    public ControlPanelManager(MainActivity activity) {
        this.mActivity = activity;
    }

    public void initViews() {
        setupControlButtons();
        setupSeekButtons();
    }

    private void setupControlButtons() {
        // EQ Logic
        ImageButton btnEq = mActivity.findViewById(R.id.btnSettings);
        if (btnEq != null) {
            btnEq.setOnClickListener(v -> {
                if (mActivity.mEngine != null) {
                    mActivity.mEngine.openEq(mActivity);
                } else {
                    mActivity.showToast("Ecualizador no disponible (Motor no iniciado)");
                }
            });
        }

        // Mute Logic delegada a PlaybackManager
        ImageButton btnMute = mActivity.findViewById(R.id.btnMute);
        if (btnMute != null) {
            btnMute.setOnClickListener(v -> {
                if (mActivity.mPlaybackManager != null) {
                    mActivity.mPlaybackManager.setMute(!mActivity.mPlaybackManager.isMuted());
                }
            });
        }

        // GPS: toque = mapas; mantener 6s = menú ingeniería (K706 / MT8163 / MTK8259 / QS6)
        View btnGps = mActivity.findViewById(R.id.btnGps);
        if (btnGps != null) {
            btnGps.setOnClickListener(v -> openGpsMapsIntent());
            final long HOLD_MS = 6000L;
            final android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
            final Runnable[] r = new Runnable[1];
            final boolean[] fired = new boolean[] { false };
            final float[] downX = new float[] { 0f };
            final float[] downY = new float[] { 0f };
            final int slop = android.view.ViewConfiguration.get(mActivity).getScaledTouchSlop();
            btnGps.setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                if (action == android.view.MotionEvent.ACTION_DOWN) {
                    fired[0] = false;
                    downX[0] = event.getX();
                    downY[0] = event.getY();
                    r[0] = () -> {
                        try {
                            fired[0] = true;
                            openEngineeringMenuFromGpsLongPress();
                        } catch (Exception ignored) {}
                    };
                    h.postDelayed(r[0], HOLD_MS);
                } else if (action == android.view.MotionEvent.ACTION_MOVE) {
                    float dx = Math.abs(event.getX() - downX[0]);
                    float dy = Math.abs(event.getY() - downY[0]);
                    if (dx > slop || dy > slop) {
                        if (r[0] != null) {
                            h.removeCallbacks(r[0]);
                            r[0] = null;
                        }
                    }
                } else if (action == android.view.MotionEvent.ACTION_UP
                        || action == android.view.MotionEvent.ACTION_CANCEL) {
                    if (r[0] != null) {
                        h.removeCallbacks(r[0]);
                        r[0] = null;
                    }
                }
                // Si ya disparó el modo ingeniería, consumir el UP para que no abra mapas.
                if (fired[0] && (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL)) {
                    return true;
                }
                return false; // permitir click normal
            });
        }

        // AutoScan Button (activo solo si modo desarrollo en menú ingeniería)
        ImageButton btnAutoScan = mActivity.findViewById(R.id.btnAutoScan);
        if (btnAutoScan != null) {
            btnAutoScan.setImageResource(R.drawable.radio_scan_icon_f);
            btnAutoScan.setOnClickListener(v -> {
                if (!mActivity.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                        .getBoolean(DevAutoscanToggleHelper.PREF_DEV_AUTOSCAN_ENABLED, false)) {
                    mActivity.showToast(mActivity.getString(R.string.under_study));
                    return;
                }
                if (mActivity.mScanManager == null) {
                    mActivity.mScanManager = new ScanManager(mActivity);
                }
                mActivity.mScanManager.toggleAutoScan(btnAutoScan);
            });
            mActivity.applyDevAutoScanButtonState();
        }

        // Si hay un pack de iconos seleccionado, aplicarlo tras setear los defaults.
        try { mActivity.applyIconPack(); } catch (Exception ignored) {}

        // Power Off delegada a DeviceManager
        ImageButton btnPowerOff = mActivity.findViewById(R.id.btnPowerOff);
        if (btnPowerOff != null) {
            btnPowerOff.setOnClickListener(v -> {
                mActivity.animateButton(btnPowerOff);
                mActivity.prepareForPowerOff();
                if (mActivity.mDeviceManager != null) {
                    mActivity.mDeviceManager.powerOff();
                }
            });
        }

        // BAND Switch
        ImageButton btnBand = mActivity.findViewById(R.id.btnBand);
        if (btnBand != null) {
            btnBand.setOnClickListener(v -> {
                if (mActivity.mEngine != null) {
                    mActivity.mEngine.bandCycle();
                } else {
                    mActivity.showToast("Motor de radio no iniciado");
                }
            });

            btnBand.setOnLongClickListener(v -> {
                if (mActivity.mEngine != null && mActivity.mEngine.getEngineName().equals("MTK8259_8667")) {
                    boolean amEnabled = mActivity.mPrefs.getBoolean("pref_enable_am", true);
                    if (!amEnabled) {
                        return true;
                    }
                    mActivity.animateButton(v);
                    mActivity.mEngine.toggleRdsFeature(99);
                    return true;
                }
                return false;
            });
        }

        // LOC/DX Switch
        ImageButton btnLocDx = mActivity.findViewById(R.id.btnLocDx);
        if (btnLocDx != null) {
            btnLocDx.setOnClickListener(v -> {
                if (mActivity.mEngine != null) {
                    mActivity.mEngine.toggleDxLocal();
                }
            });

            btnLocDx.setOnLongClickListener(v -> {
                if (ViewCompat.isAttachedToWindow(v)) {
                    mActivity.toggleLayoutMode();
                }
                return true;
            });
        }

        // Extra Button 1 - Premium Settings / Android Settings
        ImageButton btnExtra1 = mActivity.findViewById(R.id.btnExtra1);
        if (btnExtra1 != null) {
            btnExtra1.setOnClickListener(v -> {
                if (mActivity.mDialogManager != null) mActivity.mDialogManager.showPremiumSettingsDialog();
            });

            btnExtra1.setOnLongClickListener(v -> {
                try {
                    Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(settingsIntent);
                } catch (Exception e) {
                    mActivity.showStyledToast(mActivity.getString(R.string.error_opening_settings));
                }
                return true;
            });
        }

        // Extra Button 2 - Save/Load Favorites
        ImageButton btnExtra2 = mActivity.findViewById(R.id.btnExtra2);
        if (btnExtra2 != null) {
            btnExtra2.setOnClickListener(v -> {
                if (mActivity.mDialogManager != null) mActivity.mDialogManager.showSaveLoadFavoritesDialog();
            });
        }
    }

    private void openGpsMapsIntent() {
        try {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="));
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(mapIntent);
        } catch (Exception e) {
            mActivity.showToast("No se encontró aplicación de GPS");
        }
    }

    private void openEngineeringMenuFromGpsLongPress() {
        if (mActivity.mMode == MainActivity.FmMode.FM_K706) {
            mActivity.mEngineeringDialog = new K706EngineeringDialog(mActivity);
            mActivity.mEngineeringDialog.setOnDismissListener(dialog -> mActivity.mEngineeringDialog = null);
            mActivity.mEngineeringDialog.show();
        } else if (mActivity.mMode == MainActivity.FmMode.FM_MT8163) {
            new EngineeringModeDialog(mActivity).show();
        } else if (mActivity.mMode == MainActivity.FmMode.FM_8259_8667) {
            new EngineeringModeDialog(mActivity).show();
        } else if (mActivity.mMode == MainActivity.FmMode.FM_QS6) {
            mActivity.mQs6EngineeringDialog = new QS6EngineeringDialog(mActivity);
            mActivity.mQs6EngineeringDialog.setOnDismissListener(
                    dialog -> mActivity.mQs6EngineeringDialog = null);
            mActivity.mQs6EngineeringDialog.show();
        } else {
            mActivity.showToast("Modo básico: Menú de ingeniería no disponible");
        }
    }

    private void setupSeekButtons() {
        ImageButton btnSeekDown = mActivity.findViewById(R.id.btnSeekDown);
        ImageButton btnSeekUp = mActivity.findViewById(R.id.btnSeekUp);
        ImageButton btnFavPrev = mActivity.findViewById(R.id.btnFavPrev);
        ImageButton btnFavNext = mActivity.findViewById(R.id.btnFavNext);

        if (btnSeekDown != null) {
            btnSeekDown.setOnClickListener(v -> {
                mActivity.animateButton(btnSeekDown);
                mActivity.stepFreqDown();
            });
            btnSeekDown.setOnLongClickListener(v -> {
                Log.d(TAG, "Seek Down (Long Click) triggered");
                mActivity.onSeekDownEvent();
                return true;
            });
        }

        if (btnSeekUp != null) {
            btnSeekUp.setOnClickListener(v -> {
                mActivity.animateButton(btnSeekUp);
                mActivity.stepFreqUp();
            });
            btnSeekUp.setOnLongClickListener(v -> {
                Log.d(TAG, "Seek Up (Long Click) triggered");
                mActivity.onSeekUpEvent();
                return true;
            });
        }

        if (btnFavPrev != null) {
            btnFavPrev.setOnClickListener(v -> {
                mActivity.animateButton(btnFavPrev);
                mActivity.gotoPreviousFavorite();
            });
        }

        if (btnFavNext != null) {
            btnFavNext.setOnClickListener(v -> {
                mActivity.animateButton(btnFavNext);
                mActivity.gotoNextFavorite();
            });
        }
        
        // La asignación original a btnBand en setupSeekButtons ya no es necesaria pues la hemos
        // consolidad en setupControlButtons().
    }
}
