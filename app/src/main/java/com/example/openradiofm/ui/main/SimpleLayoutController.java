package com.example.openradiofm.ui.main;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.openradiofm.R;
import com.example.openradiofm.ui.theme.ThemeManager;

/**
 * V21.0: Controlador para el Layout Simple (Minimalista).
 */
public class SimpleLayoutController extends BaseLayoutController {
    private TextView tvFrequency, tvRdsInfo;
    private ImageView ivMainLogo;

    public SimpleLayoutController(MainActivity activity) {
        super(activity);
    }

    @Override
    public void initViews(View root) {
        tvFrequency = root.findViewById(R.id.tvFrequency);
        tvRdsInfo = root.findViewById(R.id.tvRdsInfo);
        ivMainLogo = root.findViewById(R.id.ivMainLogo);
        
        if (tvRdsInfo != null) {
            tvRdsInfo.setSelected(true); // Para marquee
        }
    }

    @Override
    public void updateFrequency(int freq, String ps, boolean isAm) {
        if (tvFrequency != null) {
            String text;
            if (ps != null && !ps.isEmpty()) {
                text = ps;
            } else if (isAm) {
                text = String.valueOf(freq);
            } else {
                text = String.format(java.util.Locale.US, "%.1f", freq / 1000.0);
            }
            MainActivity.setTextIfChanged(tvFrequency, text);
        }
    }

    @Override
    public void updateRDS(String text) {
        // En SimpleLayout el nombre PS ya se muestra en el campo de frecuencia
        // via updateFrequency(). Ignoramos aquí para evitar duplicación.
    }

    @Override
    public void updateRDSText(String text) {
        MainActivity.setTextIfChanged(tvRdsInfo, text);
    }

    @Override
    public void updatePTY(String pty) {
        // Simple layout no tiene PTY visible por diseño
    }

    @Override
    public void updateRdsStatus(boolean af, boolean ta, boolean tp) {
        // No hay iconos de estado en Simple
    }

    @Override
    public void updateSignal(int level, String label, String color) {
        // No hay indicador de señal en Simple
    }

    @Override
    public void updateStereo(boolean stereo) {
        // No hay icono de estéreo en Simple
    }

    @Override
    public void applySkin(boolean isNight) {
        int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);
        boolean isLight = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
        int normalColor = isLight ? Color.BLACK : Color.WHITE;
        int color = isNight ? nightBlue : normalColor;
        MainActivity.setTextColorIfChanged(tvFrequency, color);
        MainActivity.setTextColorIfChanged(tvRdsInfo, color);
    }

    @Override
    public void updateFavoriteIndicator(boolean isFavorite, int presetIdx, boolean isNight) {
        // No hay indicador de favorito en Simple
    }

    @Override
    public void updateBandIndicator(int band) {
        // No hay indicador de banda en Simple
    }

    @Override
    public void updateMute(boolean muted) {
        // Nada que hacer en Simple por ahora
    }

    @Override
    public void updateLogo(android.graphics.Bitmap bitmap) {
        if (ivMainLogo != null) {
            if (bitmap != null) {
                ivMainLogo.setImageBitmap(bitmap);
                ivMainLogo.setAlpha(1.0f);
            } else {
                ivMainLogo.setImageResource(R.mipmap.ic_launcher);
                ivMainLogo.setAlpha(0.2f);
            }
        }
    }
}
