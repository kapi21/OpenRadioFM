package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * Helpers para detectar el launcher/home activo (K706/QuickFish suele usar "theme packages").
 */
final class LauncherIntentUtils {

    private LauncherIntentUtils() {}

    static String getDefaultHomePackage(Context context) {
        if (context == null) return null;
        try {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            PackageManager pm = context.getPackageManager();
            ResolveInfo ri = pm.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
            if (ri == null || ri.activityInfo == null) return null;
            String pkg = ri.activityInfo.packageName;
            if (pkg == null || pkg.isEmpty()) return null;
            // "android" suele indicar que no hay default; en head units normalmente sí hay.
            if ("android".equals(pkg)) return null;
            return pkg;
        } catch (Exception ignored) {
            return null;
        }
    }
}

