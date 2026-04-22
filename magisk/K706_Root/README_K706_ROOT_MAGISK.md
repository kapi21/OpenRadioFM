# OpenRadioFM — K706 Root Hijack (Magisk)

## Qué hace
- **APK trampolín (QF K706):** monta `system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk` (ruta real en unidades con `pm path com.android.fmradio.ext` → `package:/system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk`). Mantiene el mismo `package` `com.android.fmradio.ext` y la actividad `com.android.fmradio.FmMainActivity`, pero al abrirlas **redirige a OpenRadioFM**. Así el widget del launcher (`Function.onRadio` → `RunApp`) deja de lanzar `ActivityNotFoundException` cuando el destino OEM no está disponible.
- Incluye un **FmService** mínimo con la acción `com.android.fmradio.IFmRadioService` (stub) para evitar fallos triviales de bind; el sintonizador real sigue siendo OpenRadioFM / motor K706.
- En cada arranque: **`pm enable`** `com.android.fmradio.ext` (el paquete debe estar habilitado para que el `ComponentName` explícito del launcher resuelva contra el APK montado).
- **Icono RADIO (prefs):** recorre `/data/data/*/shared_prefs/*.xml` con referencias OEM y sustituye por OpenRadioFM donde aplique.
- Al desinstalar el módulo, Magisk quita el overlay (vuelve el APK de fábrica), **`pm enable`** y restauración de `*.bak_orf`.

### Otra ROM / otra ruta bajo `/system/priv-app/`
Si `pm path com.android.fmradio.ext` **no** es `QF_FMRadioExt/QF_FMRadioExt.apk`, hay que renombrar la carpeta y el `.apk` dentro del ZIP del módulo (o duplicar el árbol) para coincidir con la ruta real antes de empaquetar.

---

## Instalación (recomendado)

**Desde el repo en Windows**, genera el ZIP completo (scripts LF + APK trampolín + `system/`):

```bat
magisk\build_k706_root_zip.bat
```

Salida: `magisk\k706.zip` (no uses solo una copia manual de la carpeta `K706_Root` sin el árbol `system/`, o faltará el stub).

Luego, en la radio:

1. Magisk → Módulos → Instalar desde almacenamiento **o**
2. Por ADB (suele ser más fiable si la app Magisk falla):

```sh
adb push magisk/k706.zip /data/local/tmp/k706.zip
adb shell su -c "magisk --install-module /data/local/tmp/k706.zip"
adb reboot
```

Tras el flash, Magisk 27 puede dejar archivos primero en `/data/adb/modules_update/`; un **reinicio** completa la activación en `/data/adb/modules/`.

---

## Si falla la instalación

- Anota el **mensaje exacto** (p. ej. unzip, espacio, firma).
- Confirma que el ZIP contiene en la **raíz**: `module.prop`, `customize.sh`, `service.sh`, `uninstall.sh`, `META-INF/`, y **`system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk`**.
- Prueba **solo** la vía ADB anterior; captura `logcat` al instalar desde la app Magisk si sigue fallando.
- En algunos entornos, un ZIP creado solo con PowerShell `Compress-Archive` puede dar problemas al instalador: regenera con `build_k706_root_zip.bat` o prueba 7-Zip con ZIP estándar (Deflate).

---

## Verificación (ADB)

```sh
adb connect 192.168.1.98:9876
adb shell su -c "pm path com.android.fmradio.ext"
adb shell su -c "ls -la /data/adb/modules/openradiofm_k706_root/"
adb shell su -c "wc -c /data/adb/modules/openradiofm_k706_root/service.sh"
```

No deshabilites manualmente `com.android.fmradio.ext` con `pm disable-user` si quieres que el widget del launcher abra el trampolín.

---

## Rollback

- Desinstala el módulo desde Magisk y reinicia (vuelve el APK OEM de fábrica en esa ruta).
