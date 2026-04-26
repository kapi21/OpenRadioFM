## Informe — Rollback de radio OEM (`com.android.fmradio.ext`) al desinstalar el módulo

### Contexto
En la rama `K706_Root` distribuimos un módulo Magisk que monta un APK trampolín en `system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk` (mismo paquete/actividad que el OEM) para que el launcher/widget que invoca `com.android.fmradio.ext/com.android.fmradio.FmMainActivity` termine abriendo OpenRadioFM.

Al desinstalar el módulo, Magisk deja de montar el overlay y el sistema vuelve a resolver el paquete OEM desde `/system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk`.

### Síntoma observado
Tras desinstalar el módulo, la “radio nativa”:
- aparecía con nombre anómalo (p. ej. `res/anim/abc_fade_in.xml`) y
- al abrirla podía cerrarse o no aparecer como app “instalada” para el usuario.

En `dumpsys package com.android.fmradio.ext` se observó:
- `codePath=/system/priv-app/QF_FMRadioExt`
- pero para **User 0**: `installed=false` (aunque el paquete existe en `/system`).

En ese estado, `pm path com.android.fmradio.ext` puede salir vacío y el launcher queda en un estado inconsistente.

Además, en algunos firmwares se observó crash adicional al abrir el OEM:

```text
java.lang.SecurityException: Must hold the MODIFY_PHONE_STATE permission.
... MediaSessionService.enforcePhoneStatePermission ...
```

En `privapp-permissions-platform.xml`, el bloque de `com.android.fmradio.ext` no incluía
`android.permission.MODIFY_PHONE_STATE` (solo permisos FM/Audio/etc.).

### Recuperación manual que funcionó (validado en hardware)
Estos comandos “rehabilitan” el OEM para **User 0**:

```sh
adb -s 192.168.1.98:9876 shell su -c "cmd package install-existing --user 0 com.android.fmradio.ext || pm install-existing --user 0 com.android.fmradio.ext"
adb -s 192.168.1.98:9876 shell su -c "pm enable --user 0 com.android.fmradio.ext"
```

Notas:
- `com.android.fmradio` **no existe** en este firmware (da `Unknown package`).
- El error previo era **estado por-usuario** (`installed=false`), no “APK OEM roto”.

### Cambio aplicado en el módulo (fix definitivo)
Se actualizó `magisk/K706_Root/uninstall.sh` para que el rollback haga siempre:
- `install-existing` para User 0
- `enable` para User 0

y se eliminó el uso de `pm uninstall --user 0` en rollback.

Motivo: en estas ROMs, `uninstall --user 0` puede dejar el paquete en estados extraños; la operación correcta es reinstalar la “system app” para el usuario mediante `install-existing`.

Para el crash de `MODIFY_PHONE_STATE`, se añadió una whitelist adicional vía overlay Magisk:
`magisk/K706_Root/system/etc/permissions/privapp-permissions-openradiofm-fmradio.xml`

Nota: este fix de permisos solo aplica **mientras el módulo está instalado/activo**.

### Alternativa recomendada a "desinstalar para volver a OEM"
Si el firmware OEM sin módulo crashea por permisos, en vez de desinstalar se recomienda usar
un **modo OEM** dentro del propio módulo:

- `orf_oem_mode` presente en el directorio del módulo → el launcher abre la OEM real (bind-mount desde mirror Magisk).
- sin `orf_oem_mode` → el launcher abre OpenRadioFM vía trampolín.

Esto permite "volver a OEM" sin perder el overlay de permisos.

### Verificación recomendada (post-uninstall)

```sh
adb shell su -c "pm path com.android.fmradio.ext"
adb shell su -c "dumpsys package com.android.fmradio.ext | grep -E 'codePath=|installed=|enabled=' | head -n 30"
```

Estado esperado (User 0):
- `installed=true`
- `enabled=1`
- `codePath=/system/priv-app/QF_FMRadioExt`

