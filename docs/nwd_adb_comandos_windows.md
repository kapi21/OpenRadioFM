# Comandos ADB (Windows) — paquetes NWD

Usa **`.\adb`** cuando `adb.exe` esté en la **carpeta actual** (típico: `Android\Sdk\platform-tools`).

Abre **cmd** o **PowerShell** en esa carpeta y ejecuta:

```bat
.\adb shell pm list packages | findstr nwd
.\adb shell pm path com.nwd.radio
.\adb shell pm path com.nwd.radio.service
.\adb shell pm path com.nwd.kernel
.\adb shell dumpsys package com.nwd.radio | findstr version
.\adb shell dumpsys package com.nwd.radio.service | findstr version
.\adb shell dumpsys package com.nwd.kernel | findstr version
```

## PowerShell (alternativa a `findstr`)

```powershell
.\adb shell pm list packages | Select-String nwd
.\adb shell pm path com.nwd.radio
.\adb shell pm path com.nwd.radio.service
.\adb shell pm path com.nwd.kernel
.\adb shell dumpsys package com.nwd.radio | Select-String version
.\adb shell dumpsys package com.nwd.radio.service | Select-String version
.\adb shell dumpsys package com.nwd.kernel | Select-String version
```

## Script en el repo

Desde la raíz del proyecto puedes usar (ajusta `ADB` si hace falta):

```bat
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
scripts\nwd_pm_info.bat
```

O copia `scripts\nwd_pm_info.bat` dentro de `platform-tools` y ejecútalo allí para usar `.\adb` por defecto.

## Dónde guardar los APK (`adb pull`)

Convención del proyecto: **`C:\@MIS PROYECTOS\K706_RE\QS NWD\tools\`** (misma carpeta que los árboles descompilados). Ejemplos de rutas en `docs/NWD_FASE_A_MAPA_SISTEMA.md` §A.0.
