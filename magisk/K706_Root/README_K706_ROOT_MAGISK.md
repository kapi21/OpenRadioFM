# OpenRadioFM — K706 Root Hijack (Magisk)

## Qué hace
- **APK trampolín (QF K706):** monta `system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk` (ruta real en unidades con `pm path com.android.fmradio.ext` → `package:/system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk`). Mantiene el mismo `package` `com.android.fmradio.ext` y la actividad `com.android.fmradio.FmMainActivity`, pero al abrirlas **redirige a OpenRadioFM**. Así el widget del launcher (`Function.onRadio` → `RunApp`) deja de lanzar `ActivityNotFoundException` cuando la radio OEM estaba deshabilitada.
- Incluye un **FmService** mínimo con la acción `com.android.fmradio.IFmRadioService` (stub) para evitar fallos triviales de bind; el sintonizador real sigue siendo OpenRadioFM / motor K706.
- En cada arranque: **`pm enable`** `com.android.fmradio.ext` (el paquete debe estar habilitado para que el `ComponentName` explícito resuelva contra el APK montado).
- **Icono RADIO (prefs):** recorre `/data/data/*/shared_prefs/*.xml` con referencias OEM y sustituye por OpenRadioFM donde aplique.
- Al desinstalar el módulo, Magisk quita el overlay (vuelve el APK de fábrica), **`pm enable`** y restauración de `*.bak_orf`.

### Otra ROM / otra ruta bajo `/system/priv-app/`
Si `pm path com.android.fmradio.ext` **no** es `QF_FMRadioExt/QF_FMRadioExt.apk`, hay que renombrar la carpeta y el `.apk` dentro del ZIP del módulo (o duplicar el árbol) para coincidir con la ruta real antes de empaquetar.

## Instalación
1) Comprime la carpeta `magisk/K706_Root/` como ZIP **manteniendo la estructura completa**, incluyendo `META-INF/`:

```
META-INF/com/google/android/update-binary
META-INF/com/google/android/updater-script
module.prop
customize.sh
service.sh
uninstall.sh
```

2) Instala el ZIP desde Magisk → Modules.
3) Reinicia la unidad.

## Verificación (ADB)
```sh
adb connect 192.168.1.98:9876
adb -s 192.168.1.98:9876 shell su -c "pm list packages | grep -F com.android.fmradio.ext"
adb -s 192.168.1.98:9876 shell su -c "pm disable-user --user 0 com.android.fmradio.ext"
```

## Rollback
- Desinstala el módulo desde Magisk y reinicia.

