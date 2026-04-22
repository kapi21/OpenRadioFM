package com.android.fmradio;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

/**
 * Trampolín Magisk: el launcher QuickFish abre {@code com.android.fmradio.ext/com.android.fmradio.FmMainActivity}.
 * Redirige a OpenRadioFM sin cargar la radio OEM.
 */
public class FmMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
