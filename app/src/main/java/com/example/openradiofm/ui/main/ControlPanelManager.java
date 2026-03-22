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

    // Easter Egg Variables
    private int mTestClickCount = 0;
    private long mTestStartTime = 0;

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

        // GPS Button with Hidden Test Menu
        View btnGps = mActivity.findViewById(R.id.btnGps);
        if (btnGps != null) {
            btnGps.setOnClickListener(v -> {
                long now = System.currentTimeMillis();

                if (mTestClickCount == 0 || (now - mTestStartTime) > 3000) {
                    mTestClickCount = 1;
                    mTestStartTime = now;
                } else {
                    mTestClickCount++;
                }

                if (mTestClickCount >= 5) {
                    mTestClickCount = 0; // Reset
                    if (mActivity.mMode == MainActivity.FmMode.FM_K706) {
                        mActivity.mEngineeringDialog = new K706EngineeringDialog(mActivity);
                        mActivity.mEngineeringDialog.setOnDismissListener(dialog -> mActivity.mEngineeringDialog = null);
                        mActivity.mEngineeringDialog.show();
                    } else if (mActivity.mMode == MainActivity.FmMode.FM_MT8163) {
                        new EngineeringModeDialog(mActivity).show();
                    } else if (mActivity.mMode == MainActivity.FmMode.FM_QS6) {
                        mActivity.mQs6EngineeringDialog = new QS6EngineeringDialog(mActivity);
                        mActivity.mQs6EngineeringDialog.setOnDismissListener(
                                dialog -> mActivity.mQs6EngineeringDialog = null);
                        mActivity.mQs6EngineeringDialog.show();
                    } else {
                        mActivity.showToast("Modo básico: Menú de ingeniería no disponible");
                    }
                } else {
                    if (mTestClickCount == 1) {
                        try {
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="));
                            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mActivity.startActivity(mapIntent);
                        } catch (Exception e) {
                            mActivity.showToast("No se encontró aplicación de GPS");
                        }
                    }
                }
            });
        }

        // AutoScan Button
        ImageButton btnAutoScan = mActivity.findViewById(R.id.btnAutoScan);
        if (btnAutoScan != null) {
            btnAutoScan.setImageResource(R.drawable.radio_scan_icon_f);
            btnAutoScan.setOnClickListener(v -> {
                if (mActivity.mScanManager != null) {
                    mActivity.mScanManager.toggleAutoScan(btnAutoScan);
                }
            });
        }

        // Power Off delegada a DeviceManager
        ImageButton btnPowerOff = mActivity.findViewById(R.id.btnPowerOff);
        if (btnPowerOff != null) {
            btnPowerOff.setOnClickListener(v -> {
                mActivity.animateButton(btnPowerOff);
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
