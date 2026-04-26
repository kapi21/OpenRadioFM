# Handoff — K706 Root Edition (OpenRadioFM)

Documento para retomar el trabajo sin perder contexto. **Rama:** `K706_Root`. **Dispositivo de referencia:** QF K706, ROM última operativa, ADB típico `192.168.1.98:9876` (ajustar si cambia).

### Para mañana (instalación Magisk)

- **Build ZIP (OK):** `magisk\build_k706_root_zip.bat` ya genera `magisk\k706.zip` con normalización **LF** (sin `\r`) y permisos correctos para scripts.
- **Siguiente bloqueo a cerrar:** validar instalación en hardware (UI Magisk vs CLI).
- **Instalación recomendada (CLI):** `adb push magisk\k706.zip /data/local/tmp/k706.zip` y `su -c 'magisk --install-module /data/local/tmp/k706.zip'`; **reiniciar** y verificar `/data/adb/modules/openradiofm_k706_root/`.
- **Si falla desde la UI:** capturar `logcat` durante el flash e intentar de nuevo con CLI.
- **Hitos git recientes:** `655da9aa` (trampolín APK + overlay), `d6c867fb` (QF broadcast por paquete), `8ca78af6` (docs handoff).

---

## 1. Resumen ejecutivo

Objetivo: que **OpenRadioFM** sea la **radio principal** en K706, usando **solo el motor K706**, con **neutralización reversible** de la FM OEM (`com.android.fmradio.ext`). **No** se persigue desactivar `com.txz.radio` (radio online).

Se cubren dos vías:

| Nivel | Descripción | Estado en repo |
|--------|-------------|----------------|
| **A** | APK + comandos `su` (asistente “modo root” en app) | Parcial: lógica/documentación en checklist; pantalla asistente pendiente según checklist |
| **B** | Módulo **Magisk** (`magisk/K706_Root/` + `:stub-fmradio`) | Implementado: overlay `QF_FMRadioExt`, scripts, ZIP vía `build_k706_root_zip.bat`; **instalación en unidad** en depuración |

---

## 2. Qué está hecho (código y módulo)

### 2.1 App (Java)

- **Motor K706 / widget OEM:** `K706Engine.notifyWidgetUpdate` envía `com.qf.radio.update_action` a `com.android.launcher.movablecell`, `com.android.auto.autohome` y al **paquete HOME resuelto** (`LauncherIntentUtils.getDefaultHomePackage`), p. ej. `com.android.launcher.gradient.black`.
- **WidgetBroadcastManager:** mismo criterio de destinos para el fallback K706 sin engine.
- **LauncherIntentUtils:** en `com.example.openradiofm.util` (API pública).
- **MainActivity:** al entrar por icono RADIO genérico, `handleLauncherRadioEntry()` fuerza estado de widget y broadcasts `/customize/radio/*` al launcher activo.
- **LogoManager / Glide:** corrección de crash al fallar carga de logo (p. ej. 89,1 MHz): `applyFallbackLogo()` diferido con `Handler.post` fuera del callback de Glide.
- **OemRadioWidgetReceiver:** intercepta `/customize/radio/*` hacia la radio OEM y reenvía al `RadioMediaService`.

### 2.2 Magisk (`magisk/K706_Root/`)

- **Magisk v1.1+:** overlay del APK en `system/priv-app/QF_FMRadioExt/` (trampolín `:stub-fmradio`) + `pm enable` de `com.android.fmradio.ext` (el widget llama a `FmMainActivity` por `ComponentName` explícito; deshabilitar el paquete provoca `ActivityNotFoundException`).
- **Atajos RADIO multi-launcher:** escaneo de `/data/data/*/shared_prefs/*.xml` que referencien FM OEM; backup `*.bak_orf`; `sed` a componente OpenRadioFM (incluye variantes slash, guion, y reemplazo de paquete/clase por separado).
- **Desinstalación:** `pm enable` OEM + restauración de todos los `*.bak_orf` bajo `shared_prefs`.
- **ZIP:** `magisk/build_k706_root_zip.bat` normaliza **CRLF → LF** en scripts antes de comprimir (evita fallos de instalación en Magisk por finales de línea Windows).

### 2.3 Repo / calidad

- `.gitattributes`: `magisk/**` con `eol=lf`.
- `.gitignore`: `magisk/**/*.zip`, `_tmp_magisk/`.

---

## 3. Pendientes conocidos (prioridad sugerida)

1. **Instalación del módulo Magisk (v1.1 con APK en `system/`)**  
   Reporte: **error al instalar** (detalle pendiente). Prioridad: capturar texto del error, probar solo ADB `magisk --install-module`, y comparar ZIP del .bat vs empaquetado manual. Ver bloque *Para mañana* arriba.

2. **Magisk App — “unzip error” / fallos de UI**  
   Histórico: la app Magisk a veces falla aunque CLI funcione. Con el módulo grande (~600 KB+ por el stub) vigilar límites de rutas y formato ZIP.

3. **Widget `CustomRadioWidget` (launcher `gradient.black`)**  
   **Causa conocida:** `ActivityNotFoundException` al OEM por `ComponentName` explícito → mitigado con **trampolín** `:stub-fmradio` + `pm enable` (no deshabilitar el paquete). Requiere módulo instalado y reinicio.  
   **Bug corregido en app:** `K706Engine` ya no aborta todo el bucle de `com.qf.radio.update_action` si un paquete lanza `SecurityException`.

4. **Nivel A en app**  
   Pantalla “Modo root” con aplicar/restaurar, logs y persistencia opcional (`BOOT_COMPLETED`), según `K706_ROOT_CHECKLIST.md`.

5. **Opcional:** Magisk **systemless** priv-app de OpenRadioFM (firma/permisos — ver checklist §3.2).

---

## 4. Roadmap corto (orden sugerido)

| Orden | Tarea |
|-------|--------|
| 1 | **Instalación módulo v1.1:** reproducir error, ADB CLI, formato ZIP. |
| 2 | Validar **widget RADIO** tras módulo + reinicio (trampolín `FmMainActivity`). |
| 3 | Completar **Nivel A** (UI root + comandos documentados). |
| 4 | QA checklist §5 (volante, AA/Z-Link, rollback). |
| 5 | Decidir merge hacia rama de producto (p. ej. `5.2.0.MCU`) o mantener `K706_Root` como línea larga. |

Roadmap más amplio del producto: `_DOCS/roadmap.md`.

---

## 5. Comandos útiles (ADB)

```bash
adb connect 192.168.1.98:9876
adb -s 192.168.1.98:9876 shell su -c "pm list packages | grep -E 'fmradio|openradio'"
adb -s 192.168.1.98:9876 shell su -c "cmd package dump com.android.fmradio.ext | head -80"
adb -s 192.168.1.98:9876 shell su -c "dumpsys package com.example.openradiofm | grep -A2 MAIN"
```

Instalación módulo por CLI (alternativa a la UI Magisk):

```bash
adb push openradiofm_k706.zip /data/local/tmp/
adb shell su -c "magisk --install-module /data/local/tmp/openradiofm_k706.zip"
```

Generar ZIP en Windows (desde repo):

```bat
magisk\build_k706_root_zip.bat
```

---

## 6. Commits recientes de referencia (rama `K706_Root`)

Ver historial local:

```bash
git log --oneline -15
```

Hitos recientes documentados en `CHANGELOG.md` bajo **K706 Root Edition**.

---

## 7. Archivos clave

| Ruta | Rol |
|------|-----|
| `K706_ROOT_CHECKLIST.md` | Checklist de implementación |
| `magisk/K706_Root/service.sh` | Boot: `pm enable` fmradio + parche prefs |
| `stub-fmradio/` | APK trampolín (mismo package/actividad que OEM) |
| `magisk/K706_Root/uninstall.sh` | Rollback OEM + prefs |
| `magisk/K706_Root/README_K706_ROOT_MAGISK.md` | Instrucciones del módulo |
| `magisk/build_k706_root_zip.bat` | Build ZIP con LF |
| `app/.../K706Engine.java` | Broadcast QF a launchers |
| `app/.../WidgetBroadcastManager.java` | Mismo criterio de paquetes |
| `app/.../util/LauncherIntentUtils.java` | Resolución HOME |
| `app/.../LogoManager.java` | Fix Glide fallback |

---

*Última actualización del handoff: 2026-04-23 — instalación Magisk en unidad pendiente de cerrar.*
