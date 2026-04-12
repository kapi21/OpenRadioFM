package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.openradiofm.AppConstants;
import com.example.openradiofm.R;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tira de presets en bucle: triplica el contenido y ajusta el scroll para que desde el último
 * se pueda seguir deslizando hacia el primero (y viceversa). Opcional vía {@code pref_preset_scroll_loop}.
 */
public final class InfinitePresetScrollHelper {

    private static final String PREFS = "RadioPresets";
    private static final String PREF_KEY = "pref_preset_scroll_loop";
    private static final String TAG_ATTACHED = "infinite_preset_loop";

    private InfinitePresetScrollHelper() {}

    public static boolean isEnabled(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(PREF_KEY, false);
    }

    public static void attachIfNeeded(MainActivity activity) {
        if (activity == null || !isEnabled(activity)) return;

        if (activity.mIsSimpleLayout) return;

        if (activity.isV3LayoutActive()) {
            HorizontalScrollView hsv = activity.findViewById(R.id.horizontalScrollPresets);
            if (hsv != null && !TAG_ATTACHED.equals(hsv.getTag())) {
                attachHorizontalV3(activity, hsv);
                hsv.setTag(TAG_ATTACHED);
            }
        } else {
            ScrollView sv = activity.findViewById(R.id.scrollViewPresets);
            if (sv != null && !TAG_ATTACHED.equals(sv.getTag())) {
                attachVerticalV2(activity, sv);
                sv.setTag(TAG_ATTACHED);
            }
        }
    }

    private static void attachHorizontalV3(MainActivity activity, HorizontalScrollView hsv) {
        if (!(hsv.getChildAt(0) instanceof LinearLayout)) return;
        LinearLayout strip = (LinearLayout) hsv.getChildAt(0);
        if (strip.getChildCount() != AppConstants.PRESETS_COUNT) return;

        PresetManager pm = activity.mPresetManager;
        if (pm == null) return;
        pm.clearLoopMirrors();

        View[] originals = new View[AppConstants.PRESETS_COUNT];
        for (int i = 0; i < AppConstants.PRESETS_COUNT; i++) {
            originals[i] = strip.getChildAt(0);
            strip.removeViewAt(0);
        }

        LayoutInflater inf = LayoutInflater.from(activity);
        for (int i = 0; i < AppConstants.PRESETS_COUNT; i++) {
            addLoopSlotV3(inf, strip, activity, pm, i);
        }
        for (int i = 0; i < AppConstants.PRESETS_COUNT; i++) {
            strip.addView(originals[i]);
        }
        for (int i = 0; i < AppConstants.PRESETS_COUNT; i++) {
            addLoopSlotV3(inf, strip, activity, pm, i);
        }

        final int[] pageW = new int[1];
        final AtomicBoolean adjusting = new AtomicBoolean(false);

        Runnable measureAndScroll = () -> {
            int w = horizontalPageSpanPx(strip, 0, AppConstants.PRESETS_COUNT);
            if (w <= 0) return;
            pageW[0] = w;
            hsv.scrollTo(w, 0);
        };

        strip.post(measureAndScroll);
        strip.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (pageW[0] <= 0) measureAndScroll.run();
        });

        hsv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (adjusting.get()) return;
            int pw = pageW[0];
            if (pw <= 0) return;
            int maxScroll = strip.getWidth() - hsv.getWidth();
            if (maxScroll <= 0) return;
            float edge = Math.max(8, pw * 0.12f);
            if (scrollX < edge) {
                adjusting.set(true);
                hsv.scrollTo(scrollX + pw, 0);
                adjusting.set(false);
            } else if (scrollX > maxScroll - edge) {
                adjusting.set(true);
                hsv.scrollTo(scrollX - pw, 0);
                adjusting.set(false);
            }
        });
    }

    private static void attachVerticalV2(MainActivity activity, ScrollView sv) {
        if (!(sv.getChildAt(0) instanceof LinearLayout)) return;
        LinearLayout strip = (LinearLayout) sv.getChildAt(0);
        if (strip.getChildCount() != AppConstants.PRESETS_COUNT) return;

        PresetManager pm = activity.mPresetManager;
        if (pm == null) return;
        pm.clearLoopMirrors();

        View[] originals = new View[AppConstants.PRESETS_COUNT];
        for (int i = 0; i < AppConstants.PRESETS_COUNT; i++) {
            originals[i] = strip.getChildAt(0);
            strip.removeViewAt(0);
        }

        LayoutInflater inf = LayoutInflater.from(activity);
        for (int i = 0; i < AppConstants.PRESETS_COUNT; i++) {
            addLoopSlotV2(inf, strip, activity, pm, i);
        }
        for (int i = 0; i < AppConstants.PRESETS_COUNT; i++) {
            strip.addView(originals[i]);
        }
        for (int i = 0; i < AppConstants.PRESETS_COUNT; i++) {
            addLoopSlotV2(inf, strip, activity, pm, i);
        }

        final int[] pageH = new int[1];
        final AtomicBoolean adjusting = new AtomicBoolean(false);

        Runnable measureAndScroll = () -> {
            int h = verticalPageSpanPx(strip, 0, AppConstants.PRESETS_COUNT);
            if (h <= 0) return;
            pageH[0] = h;
            sv.scrollTo(0, h);
        };

        strip.post(measureAndScroll);
        strip.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (pageH[0] <= 0) measureAndScroll.run();
        });

        sv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (adjusting.get()) return;
            int ph = pageH[0];
            if (ph <= 0) return;
            int maxScroll = strip.getHeight() - sv.getHeight();
            if (maxScroll <= 0) return;
            float edge = Math.max(8, ph * 0.12f);
            if (scrollY < edge) {
                adjusting.set(true);
                sv.scrollTo(0, scrollY + ph);
                adjusting.set(false);
            } else if (scrollY > maxScroll - edge) {
                adjusting.set(true);
                sv.scrollTo(0, scrollY - ph);
                adjusting.set(false);
            }
        });
    }

    private static void addLoopSlotV3(LayoutInflater inf, LinearLayout strip, MainActivity activity,
            PresetManager pm, int slotIndex) {
        View row = inf.inflate(R.layout.preset_loop_slot_v3, strip, false);
        strip.addView(row);
        View card = row.findViewById(R.id.cardPresetLoop);
        ImageView iv = row.findViewById(R.id.ivPresetLoop);
        TextView tv = row.findViewById(R.id.tvPresetLoop);
        final int slot = slotIndex;
        if (card != null) {
            card.setOnClickListener(v -> activity.gotoPreset(slot));
            card.setOnLongClickListener(v -> {
                activity.savePreset(slot);
                return true;
            });
        }
        pm.registerLoopMirror(slot, card, iv, tv);
    }

    private static void addLoopSlotV2(LayoutInflater inf, LinearLayout strip, MainActivity activity,
            PresetManager pm, int slotIndex) {
        View row = inf.inflate(R.layout.preset_loop_slot_v2, strip, false);
        strip.addView(row);
        ImageView iv = row.findViewById(R.id.ivPresetLoop);
        TextView tv = row.findViewById(R.id.tvPresetLoop);
        final int slot = slotIndex;
        row.setOnClickListener(v -> activity.gotoPreset(slot));
        row.setOnLongClickListener(v -> {
            activity.savePreset(slot);
            return true;
        });
        pm.registerLoopMirror(slot, row, iv, tv);
    }

    private static int horizontalPageSpanPx(LinearLayout strip, int from, int count) {
        int w = 0;
        int end = Math.min(from + count, strip.getChildCount());
        for (int i = from; i < end; i++) {
            View c = strip.getChildAt(i);
            int piece = c.getWidth();
            ViewGroup.LayoutParams lp = c.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams m = (ViewGroup.MarginLayoutParams) lp;
                piece += m.leftMargin + m.rightMargin;
            }
            w += piece;
        }
        return w;
    }

    private static int verticalPageSpanPx(LinearLayout strip, int from, int count) {
        int h = 0;
        int end = Math.min(from + count, strip.getChildCount());
        for (int i = from; i < end; i++) {
            View c = strip.getChildAt(i);
            int piece = c.getHeight();
            ViewGroup.LayoutParams lp = c.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams m = (ViewGroup.MarginLayoutParams) lp;
                piece += m.topMargin + m.bottomMargin;
            }
            h += piece;
        }
        return h;
    }
}
