package com.example.openradiofm.ui.main;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.openradiofm.R;

/**
 * Mediador para centralizar el acceso a las vistas globales de MainActivity.
 * Reduce el acoplamiento directo y limpia el código de la actividad.
 */
public class UiViewMediator {
    private final MainActivity mActivity;

    // Vistas de Control Global (Comunes a todos los layouts)
    public ImageButton btnMute;
    public ImageButton btnBand;
    public ImageButton btnPowerOff;
    public ImageButton btnAutoScan;
    public ImageButton btnLocDx;
    public View bottomControls;
    public View rootLayout;
    
    // Vistas de Estado Global
    public TextView tvDigitalClock;
    public FrameLayout ivDataActivity; // Contenedor para el indicador de datos
    public ImageView ivDataActivityIcon;
    public ImageView ivCarLogo;
    public ImageView ivMainLogo;
    public ImageView ivDynamicBackground;
    public View boxFrequency;
    public View boxIconsTop;
    
    // Vistas que a veces son usadas directamente por Managers/Coordinadores
    public ImageView ivSignalLevel;
    public View mSignalBarsView;

    public UiViewMediator(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Vincula las vistas una vez que el layout ha sido inflado.
     */
    public void bindViews() {
        btnMute = mActivity.findViewById(R.id.btnMute);
        btnBand = mActivity.findViewById(R.id.btnBand);
        btnPowerOff = mActivity.findViewById(R.id.btnPowerOff);
        btnAutoScan = mActivity.findViewById(R.id.btnAutoScan);
        btnLocDx = mActivity.findViewById(R.id.btnLocDx);
        bottomControls = mActivity.findViewById(R.id.bottomControls);
        rootLayout = mActivity.findViewById(R.id.rootLayout);

        tvDigitalClock = mActivity.findViewById(R.id.tvDigitalClock);
        ivDataActivity = mActivity.findViewById(R.id.ivDataActivity);
        ivDataActivityIcon = mActivity.findViewById(R.id.ivDataActivityIcon);
        ivCarLogo = mActivity.findViewById(R.id.ivCarLogo);
        ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);
        ivDynamicBackground = mActivity.findViewById(R.id.ivDynamicBackground);
        boxFrequency = mActivity.findViewById(R.id.boxFrequency);
        boxIconsTop = mActivity.findViewById(R.id.boxIconsTopLayout2);

        ivSignalLevel = mActivity.findViewById(R.id.ivSignalLevel);
        mSignalBarsView = mActivity.findViewById(R.id.viewSignalBars);
    }

    public void setMuteStatus(boolean isMuted) {
        if (btnMute != null) {
            btnMute.setImageResource(isMuted ? R.drawable.radio_mute_p : R.drawable.radio_mute_n);
        }
    }

    public void toggleBottomControls(boolean show) {
        if (bottomControls != null) {
            bottomControls.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
