package com.android.fmradio;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

/**
 * Trampolín Magisk: el launcher QuickFish abre {@code com.android.fmradio.ext/com.android.fmradio.FmMainActivity}.
 * Redirige a OpenRadioFM sin cargar la radio OEM.
 */
public class FmMainActivity extends Activity {

    private static final String TAG = "ORF_FM_STUB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logIncomingIntent(getIntent());
        try {
            Intent i = new Intent();
            i.setComponent(new ComponentName(
                    "com.example.openradiofm",
                    "com.example.openradiofm.ui.main.MainActivity"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } catch (Exception ignored) {
        }
        finish();
    }

    private static void logIncomingIntent(Intent in) {
        try {
            if (in == null) {
                Log.i(TAG, "Intent=null");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Intent action=").append(in.getAction())
                    .append(" data=").append(in.getDataString())
                    .append(" flags=0x").append(Integer.toHexString(in.getFlags()));
            Set<String> cats = in.getCategories();
            if (cats != null && !cats.isEmpty()) {
                sb.append(" categories=").append(cats);
            }
            Bundle ex = in.getExtras();
            if (ex != null && !ex.isEmpty()) {
                sb.append(" extras{");
                for (String k : ex.keySet()) {
                    Object v = ex.get(k);
                    sb.append(k).append("=");
                    if (v instanceof String) sb.append("\"").append(v).append("\"");
                    else sb.append(String.valueOf(v));
                    sb.append(", ");
                }
                sb.append("}");
            } else {
                sb.append(" extras{}");
            }
            Log.i(TAG, sb.toString());
        } catch (Throwable t) {
            Log.w(TAG, "logIncomingIntent failed", t);
        }
    }
}
