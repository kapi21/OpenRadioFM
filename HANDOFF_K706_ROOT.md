# Handoff — K706 Root Edition (OpenRadioFM)

Documento para retomar el trabajo sin perder contexto. **Rama:** `K706_Root`. **Dispositivo de referencia:** QF K706, ROM última operativa, ADB típico `192.168.1.98:9876` (ajustar si cambia).

---

## 1. Resumen ejecutivo

Objetivo: que **OpenRadioFM** sea la **radio principal** en K706, usando **solo el motor K706**, con **neutralización reversible** de la FM OEM (`com.android.fmradio.ext`). **No** se persigue desactivar `com.txz.radio` (radio online).

Se cubren dos vías:

| Nivel | Descripción | Estado en repo |
|--------|-------------|----------------|
| **A** | APK + comandos `su` (asistente “modo root” en app) | Parcial: lógica/documentación en checklist; pantalla asistente pendiente según checklist |
| **B** | Módulo **Magisk** (`magisk/K706_Root/`) | Implementado: `service.sh`, `uninstall.sh`, `customize.sh`, `META-INF`, `build_k706_root_zip.bat` |

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

- En cada boot: `pm disable-user` + `force-stop` de `com.android.fmradio.ext`.
- **Atajos RADIO multi-launcher:** escaneo de `/data/data/*/shared_prefs/*.xml` que referencien FM OEM; backup `*.bak_orf`; `sed` a componente OpenRadioFM (incluye variantes slash, guion, y reemplazo de paquete/clase por separado).
- **Desinstalación:** `pm enable` OEM + restauración de todos los `*.bak_orf` bajo `shared_prefs`.
- **ZIP:** `magisk/build_k706_root_zip.bat` normaliza **CRLF → LF** en scripts antes de comprimir (evita fallos de instalación en Magisk por finales de línea Windows).

### 2.3 Repo / calidad

- `.gitattributes`: `magisk/**` con `eol=lf`.
- `.gitignore`: `magisk/**/*.zip`, `_tmp_magisk/`.

---

## 3. Pendientes conocidos (prioridad sugerida)

1. **Magisk App — “unzip error”**  
   A veces la app Magisk sigue fallando al instalar el ZIP aunque CLI/`magisk --install-module` o generación con BAT + LF hayan funcionado antes. **Siguiente paso:** reproducir con el ZIP exacto, comprobar estructura (raíz del ZIP), permisos, y trazas Magisk; comparar con instalación por ADB.

2. **Widget `CustomRadioWidget` (launcher `gradient.black` / clases `autohome`)**  
   Si el **click** sigue fallando tras prefs + broadcasts, el destino puede ir **hardcodeado** en el APK del launcher. Entonces: log completo con línea `java.lang....`, y `adb shell su -c "grep -r fmradio /data/data/com.android.launcher.gradient.black"` (u otro paquete HOME) para ver si queda algo fuera de `shared_prefs` (p. ej. base de datos).

3. **Nivel A en app**  
   Pantalla “Modo root” con aplicar/restaurar, logs y persistencia opcional (`BOOT_COMPLETED`), según `K706_ROOT_CHECKLIST.md`.

4. **Opcional:** Magisk **systemless** priv-app de OpenRadioFM (firma/permisos — ver checklist §3.2).

---

## 4. Roadmap corto (orden sugerido)

| Orden | Tarea |
|-------|--------|
| 1 | Cerrar diagnóstico **unzip** en Magisk App (ZIP de referencia + logs). |
| 2 | Validar **widget RADIO** en `launcher.gradient.black` tras último commit (QF + prefs); si falla, ver §3.2. |
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
| `magisk/K706_Root/service.sh` | Boot: disable OEM + parche prefs |
| `magisk/K706_Root/uninstall.sh` | Rollback OEM + prefs |
| `magisk/K706_Root/README_K706_ROOT_MAGISK.md` | Instrucciones del módulo |
| `magisk/build_k706_root_zip.bat` | Build ZIP con LF |
| `app/.../K706Engine.java` | Broadcast QF a launchers |
| `app/.../WidgetBroadcastManager.java` | Mismo criterio de paquetes |
| `app/.../util/LauncherIntentUtils.java` | Resolución HOME |
| `app/.../LogoManager.java` | Fix Glide fallback |

---

*Última actualización del handoff: 2026-04-22 (preparado para continuar al día siguiente).*
