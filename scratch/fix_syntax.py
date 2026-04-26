import sys
import re

filepath = 'app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java'
try:
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    method_start_token = 'public void applyStatusBarVisibility() {'
    start_idx = content.find(method_start_token)
    if start_idx == -1:
        print('Method start not found')
        sys.exit(1)

    end_token = 'public void applyLogoModePreference()'
    end_idx = content.find(end_token)
    if end_idx == -1:
        print('Next method start not found')
        sys.exit(1)

    new_method = """public void applyStatusBarVisibility() {
        if (mPrefs == null || getWindow() == null || getWindow().getDecorView() == null) return;
        boolean showStatusBarV2 = mPrefs.getBoolean("pref_show_status_bar_v2", false);
        runOnUiThread(() -> {
            try {
                if (getWindow() == null) return;
                final android.view.Window window = getWindow();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    android.view.WindowInsetsController c = window.getInsetsController();
                    if (c != null) {
                        if (showStatusBarV2) {
                            c.show(android.view.WindowInsets.Type.statusBars());
                        } else {
                            c.hide(android.view.WindowInsets.Type.statusBars());
                        }
                    }
                }
                
                final android.view.View decor = window.getDecorView();
                if (decor != null) {
                    if (showStatusBarV2) {
                        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        decor.setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_VISIBLE);
                    } else {
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        decor.setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                }
            } catch (Exception e) {
                android.util.Log.w("OpenRadioFm", "applyStatusBarVisibility deferred error: " + e.getMessage());
            }
        });
    }

    """

    content = content[:start_idx] + new_method + content[end_idx:]

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print('Method replaced successfully')
except Exception as e:
    print(e)
