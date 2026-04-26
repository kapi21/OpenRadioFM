package com.example.openradiofm.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * Detecta el paquete del launcher / HOME por defecto.
 * Útil para enviar {@code com.qf.radio.update_action} al launcher real (p. ej.
 * {@code com.android.launcher.gradient.black}), no solo a paquetes fijos OEM.
 */
public final class LauncherIntentUtils {

    private LauncherIntentUtils() {}

    public static String getDefaultHomePackage(Context context) {
        if (context == null) return null;
        try {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            PackageManager pm = context.getPackageManager();
            ResolveInfo ri = pm.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
            if (ri == null || ri.activityInfo == null) return null;
            String pkg = ri.activityInfo.packageName;
            if (pkg == null || pkg.isEmpty()) return null;
            if ("android".equals(pkg)) return null;
            return pkg;
        } catch (Exception ignored) {
            return null;
        }
    }
}
