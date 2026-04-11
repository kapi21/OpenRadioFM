package com.example.openradiofm.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Indicador de señal FM por segmentos (reemplazo visual del icono {@code level_signal}).
 * Colores según apariencia: classic (blanco + alfas), noche (azul tema), día (negro + alfas).
 */
public class SignalBarsView extends View {

    public static final int APPEAR_CLASSIC = 0;
    public static final int APPEAR_NIGHT = 1;
    public static final int APPEAR_DAY = 2;

    private static final int BAR_COUNT = 5;

    private int mAppearance = APPEAR_CLASSIC;
    /** Número de segmentos “encendidos”, 0–5 (de izquierda a derecha). */
    private int mLitCount = 0;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mBar = new RectF();

    private int mNightRgb = 0x4D9FFF;

    public SignalBarsView(Context context) {
        super(context);
        init(context);
    }

    public SignalBarsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SignalBarsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        try {
            mNightRgb = ContextCompat.getColor(context, com.example.openradiofm.R.color.night_blue_primary);
        } catch (Exception ignored) {}
    }

    public void setAppearance(int appearance) {
        if (mAppearance == appearance) return;
        mAppearance = appearance;
        invalidate();
    }

    /**
     * @param litCount segmentos encendidos, clampa 0–5.
     */
    public void setLitCount(int litCount) {
        int v = Math.max(0, Math.min(BAR_COUNT, litCount));
        if (v == mLitCount) return;
        mLitCount = v;
        invalidate();
    }

    public int getLitCount() {
        return mLitCount;
    }

    private static int dp(View v, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, v.getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float gap = dp(this, 1.5f);
        float corner = dp(this, 1f);
        float totalGap = gap * (BAR_COUNT - 1);
        float barW = (w - totalGap) / BAR_COUNT;
        float padV = dp(this, 3f);
        float top = padV;
        float bottom = h - padV;

        int inactive;
        int active;
        switch (mAppearance) {
            case APPEAR_NIGHT:
                inactive = Color.argb(70, Color.red(mNightRgb), Color.green(mNightRgb), Color.blue(mNightRgb));
                active = Color.argb(255, Color.red(mNightRgb), Color.green(mNightRgb), Color.blue(mNightRgb));
                break;
            case APPEAR_DAY:
                inactive = Color.argb(90, 0, 0, 0);
                active = Color.argb(255, 0, 0, 0);
                break;
            case APPEAR_CLASSIC:
            default:
                inactive = Color.argb(85, 255, 255, 255);
                active = Color.argb(255, 255, 255, 255);
                break;
        }

        float x = 0;
        for (int i = 0; i < BAR_COUNT; i++) {
            boolean lit = i < mLitCount;
            mPaint.setColor(lit ? active : inactive);
            float t = lit ? top : top + (bottom - top) * 0.35f;
            mBar.set(x, t, x + barW, bottom);
            canvas.drawRoundRect(mBar, corner, corner, mPaint);
            x += barW + gap;
        }
    }
}
